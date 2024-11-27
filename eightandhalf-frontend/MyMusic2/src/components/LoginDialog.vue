<template>
  <div class="LoginDiaglog">
    <el-dialog
      v-model="visible"
      title="用户登录"
      width="400px"
      :close-on-click-modal="false"
      :center="true"
  >
    <el-form :model="form" :rules="rules" ref="loginForm" label-width="80px">
      <el-form-item label="用户名" v-if="!isEmailLogin" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名"/>
      </el-form-item>

      <el-form-item label="密码" v-if="!isEmailLogin" prop="password">
        <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码/MFA一次性代码"
        />
      </el-form-item>

      <el-form-item label="邮箱" v-if="isEmailLogin" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱"/>
      </el-form-item>

      <el-form-item label="验证码" v-if="isEmailLogin" prop="emailCode">
        <el-input v-model="form.emailCode" placeholder="请输入邮件验证码"/>

      </el-form-item>


    </el-form>

    <div class="switch">
      <el-button @click="toggleLoginMethod">
        {{ isEmailLogin ? '切换到用户名密码登录' : '切换到邮件验证码登录' }}
      </el-button>
      <el-button
          :disabled="isLoading || isCooldown"
          v-if="isEmailLogin"
          @click="handleGetEmailCode"
          type="primary">
        {{ isCooldown ? `重新获取(${countdown})` : (isLoading ? '获取验证码中...' : '获取验证码') }}
      </el-button>
    </div>


    <template #footer>
      <el-button @click.stop="handleClose">取消</el-button>
      <el-button type="primary" @click="handleLogin">登录</el-button>
    </template>
  </el-dialog>

  </div>
  
</template>

<script setup>
  import {ElMessage} from "element-plus";
  import {login} from "@/api/user/login"; // 假设 login 和 emailLogin 方法存放在 api/user/login.js 文件中
  import {ref, watch} from "vue";
  import {useUserInfoStore} from "@/store/userStore";
  import {emailLogin, getEmailCode} from "@/api/user/emailLogin"; // 假设 login 和 emailLogin 方法存放在 api/user/login.js 文件中


  const userInfoStore = useUserInfoStore();

  const form = ref({
    username: "",
    password: "",
    email: "",
    code: "", // 原验证码
    emailCode: "", // 邮件验证码
  });

  const rules = {
    username: [{required: true, message: "请输入用户名", trigger: "blur"}],
    password: [{required: true, message: "请输入密码", trigger: "blur"}],
    email: [{required: true, message: "请输入邮箱", trigger: "blur"}],
    emailCode: [{required: true, message: "请输入邮件验证码", trigger: "blur"}],
  };

  const loginForm = ref(null);
  const visible = ref(false);
  const isEmailLogin = ref(false); // 新增状态来切换登录方式
  const isLoading = ref(false); // 加载状态
  const isCooldown = ref(false); // 冷却状态
  const countdown = ref(60); // 倒计时

  const handleClose = () => {
    visible.value = false;
  };
  // 暴露 show 方法给父组件
  const show = () => {
    visible.value = true;
  };

  const toggleLoginMethod = () => {
    isEmailLogin.value = !isEmailLogin.value;
    // 清空表单
    // form.value.username = "";
    // form.value.password = "";
    // form.value.email = "";
    // form.value.code = "";
    form.value.emailCode = ""; // 清空邮件验证码
  };

  const handleGetEmailCode = () => {
    let email = form.value.email
    if (!email) {
      ElMessage.warning("请先输入邮箱");
      return;
    }

    var reg = /^[A-Za-z0-9\u4e00-\u9fa5]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
    if (!reg.test(email)) {
      ElMessage.warning("请输入有效邮箱");
      return;
    }
    isLoading.value = true; // 开始加载
    getEmailCode(form.value.email)
        .then((response) => {
          const res = response;
          if (res.base.code === 10000) {
            ElMessage.success("验证码已发送，请查收");
            userInfoStore.fetchUserInfo();
            startCooldown(); // 开始冷却
          } else {
            ElMessage.error(`获取验证码失败：${res.message}`);
          }
        })
        .catch((error) => {
          ElMessage.error(`获取验证码失败：${error.message}`);
        })
        .finally(() => {
          isLoading.value = false; // 结束加载
        });
  };

  const startCooldown = () => {
    isCooldown.value = true; // 开始冷却
    countdown.value = 60; // 重置倒计时

    const interval = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(interval);
        isCooldown.value = false; // 结束冷却
      }
    }, 1000);
  };

  const handleLogin = () => {
    loginForm.value.validate((valid) => {
      if (valid) {
        if (isEmailLogin.value) {
          emailLogin({email: form.value.email, code: form.value.emailCode}) // 使用 emailCode
              .then((response) => {
                const res = response;
                if (res.base.code === 10000) {
                  ElMessage.success("登录成功");
                  userInfoStore.fetchUserInfo();
                  visible.value = false;
                } else {
                  ElMessage.error(`登录失败：${res.message}`);
                }
              });
        } else {
          login(form.value)
              .then((response) => {
                const res = response;
                if (res.base.code === 10000) {
                  ElMessage.success("登录成功");
                  userInfoStore.fetchUserInfo();
                  visible.value = false; // 关闭登录弹窗
                } else {
                  ElMessage.error(`登录失败：${res.message}`);
                }
              });
        }
      }
    });
  };

  defineExpose({
    show,
  });
</script>

<style scoped>
.LoginDiaglog{
  cursor:default;
}
.el-dialog {
  display: flex;
  flex-direction: column; /* 使内容垂直排列 */
  align-items: center; /* 水平居中 */
  text-align: center;
}
.switch{
  display: flex;
  justify-content: center;
}


</style>
