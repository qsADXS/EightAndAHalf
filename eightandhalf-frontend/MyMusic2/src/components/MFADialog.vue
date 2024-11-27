<template>
  <div class="LoginDiaglog">
    <el-dialog
      v-model="visible"
      title="绑定MFA"
      width="400px"
      :close-on-click-modal="false"
      :center="true"
  >

    <div class="refresh">
      <!-- 如果 qrcode 为空，显示 imgUrl -->
      <img v-if="!qrcode" :src="imgUrl" alt="Image" />
      <!-- 如果 qrcode 不为空，显示 qrcode -->
      <img v-else :src="qrcode" alt="QR Code" />
    </div>
    <div class="link">
      <a href="https://support.microsoft.com/en-us/account-billing/download-microsoft-authenticator-351498fc-850a-45da-b7b6-27e523b8702a" target="_blank">
        MFA app下载地址
      </a>
    </div>
    <el-form :model="form" :rules="rules" ref="loginForm" label-width="60px">

      <el-form-item label="code" prop="code">
        <el-input v-model="form.code" placeholder="请输入code"/>
      </el-form-item>

    </el-form>
    <div class="refresh">
      <el-button @click="refreshQrCode">
        刷新二维码
      </el-button>

    </div>

    <template #footer>
      <el-button @click.stop="handleClose">取消</el-button>
      <el-button type="primary" @click="bind">绑定</el-button>
    </template>
  </el-dialog>

  </div>
  
</template>

<script setup>
  import {ElMessage} from "element-plus";
  import {ref,onBeforeUpdate} from "vue";
  import {getQrcode ,bindcode} from "@/api/user/MFA"; // 假设 login 和 emailLogin 方法存放在 api/user/login.js 文件中
  const form = ref({
    code: "",
  });

  const rules = {
    code: [{required: true, message: "请输入code", trigger: "blur"}],
  };

  const loginForm = ref(null);
  const visible = ref(false);
  let imgUrl = "https://s2.loli.net/2024/11/25/KDtkAjCp3gH46Nf.png";
  const qrcode = ref(null);
  const secret = ref(null);

  const handleClose = () => {
    visible.value = false;
  };
//刷新二维码
  const refreshQrCode = () => {
    getQrcode()
        .then((response) => {
          const res = response;
          if (res.base.code === 10000) {
            qrcode.value = res.data.qrcode;
            secret.value = res.data.secret;
            console.log(secret)
          } else {
            ElMessage.error(`登录失败：${res.message}`);
          }
        });

  }

  const toggleLoginMethod = () => {
    form.value.code = ""; // 验证码
  };

  const bind = () => {
    let code = form.value.code
    if (!code) {
      ElMessage.warning("请先输入code");
      return;
    }

    bindcode({code:code, secret:secret.value})
        .then((response) => {
          const res = response;
          if (res.base.code === 10000) {
            ElMessage.success("绑定成功");
            visible.value = false;
          } else {
            console.log(response)
            ElMessage.error(`绑定失败：${res.message}`);
          }
        })
        .catch((error) => {
          ElMessage.error(`失败：${error.message}`);
        });
  };


  onBeforeUpdate(()=>{
    refreshQrCode();
  })

  // 暴露 show 方法给父组件
  const show = () => {
    visible.value = true;
  };
  defineExpose({
    show,
  });
</script>

<style scoped>


.refresh{
  display: flex;
  justify-content: center;
}
.link{
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
  margin-top: 1px;
}


</style>
