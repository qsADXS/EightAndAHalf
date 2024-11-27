<template>
   <div class="RegisterDialog">
    <el-dialog
      v-model="visible"
      title="用户注册"
      width="400px"
      :close-on-click-modal="false"
      :center="true"
    >
      <el-form :model="form" :rules="rules" ref="registerForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
  
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
  
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
          />
        </el-form-item>
  
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
          />
        </el-form-item>
      </el-form>
  
      <template #footer>
        <el-button @click.stop="handleClose">取消</el-button>
        <el-button type="primary" @click="handleRegister">注册</el-button>
      </template>
    </el-dialog>
   </div>
</template>
  
  <script setup>
  import { ElMessage } from "element-plus";
  import { Register } from "@/api/user/register/index";
  import { ref } from "vue";
  
  const form = ref({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  
  const rules = {
    username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
    email: [
      { required: true, message: "请输入邮箱", trigger: "blur" },
      {
        type: "email",
        message: "请输入有效的邮箱地址",
        trigger: ["blur", "change"],
      },
    ],
    password: [{ required: true, message: "请输入密码", trigger: "blur" }],
    confirmPassword: [
      { required: true, message: "请确认密码", trigger: "blur" },
      {
        validator: (rule, value, callback) => {
          if (value !== form.value.password) {
            callback(new Error("两次输入的密码不一致"));
          } else {
            callback();
          }
        },
        trigger: "blur",
      },
    ],
  };
  
  const registerForm = ref(null);
  const visible = ref(false);
  
  const handleClose = () => {
    visible.value = false;
  };
  
  const handleRegister = () => {
    registerForm.value.validate((valid) => {
      if (valid) {
        Register(form.value)
          .then((res) => {
            if (res.base.code === 10000) {
              ElMessage.success("注册成功,快去登录吧！");
              visible.value = false; // 关闭注册弹窗
            } else {
              ElMessage.error(`注册失败：${res.base.message}`);
            }
          })
          .catch(() => {
            ElMessage.error("注册失败，请重试");
          });
      }
    });
  };
  
  // 暴露 show 方法给父组件
  const show = () => {
    visible.value = true;
  };
  defineExpose({
    show,
  });
  </script>
  
  <style scoped>
  .RegisterDialog{
    cursor: default;
  }
  .el-dialog {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  </style>
  