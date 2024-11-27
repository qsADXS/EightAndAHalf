<template>
  <div class="dropdown-container" v-if="modelValue" @click.stop>
    <div class="dropdown-item" @click="goToDynamic"  v-if="isLoggedIn">
      <el-icon><BellFilled /></el-icon>
      <span>&nbsp;我的动态{{ dynamicCount }}</span>
    </div>
    <div class="dropdown-item" @click="goToFollow" v-if="isLoggedIn">
      <el-icon><Star /></el-icon>
      <span>&nbsp;关注 {{ followCount }}位</span>
    </div>
    <div class="dropdown-item" @click="goToFans" v-if="isLoggedIn">
      <el-icon><User /></el-icon>
      <span>&nbsp;粉丝 {{ fansCount }}位</span>
    </div>
    <hr v-if="isLoggedIn"/>
<!--    <div class="dropdown-item" @click="goToPrivacy">-->
<!--      <el-icon><Setting /></el-icon>-->
<!--      <span>&nbsp;隐私设置</span>-->
<!--    </div>-->
    <div class="dropdown-item" @click="openAvatarDialog" v-if="isLoggedIn">
      <el-icon><Camera /></el-icon>
      <span>&nbsp;更改头像</span>
      <UpdateAvatar ref="avatarDialog" />
    </div>
    <hr v-if="isLoggedIn"/>
    <div class="dropdown-item" @click="openLoginDialog" v-if="!isLoggedIn">
      <el-icon><UserFilled /></el-icon>
      <span>&nbsp;登录</span>
      <LoginDialog ref="loginDialog" />
    </div>
    <div class="dropdown-item" @click="openRegisterDialog" v-if="!isLoggedIn">
      <el-icon><UserFilled /></el-icon>
      <span>&nbsp;注册</span>
      <RegisterDialog ref="registerDialog" />
    </div>
    <div class="dropdown-item" @click="openMFADialog" v-if="isLoggedIn">
      <el-icon><UserFilled /></el-icon>
      <span>&nbsp;绑定MFA</span>
      <MFADialog ref="mfaDialog" />
    </div>
    <div class="dropdown-item logout" @click="logout" v-if="isLoggedIn">
      <el-icon><SwitchFilled /></el-icon>
      <span>&nbsp;退出登录</span>
    </div>
  </div>
</template>

<script setup>
import { Setting, Camera, SwitchFilled, UserFilled, Star } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";
import { Mylogout } from "@/api/user/logout/index";
import LoginDialog from "@/components/LoginDialog.vue";
import MFADialog from "@/components/MFADialog.vue";
import RegisterDialog from "@/components/RegisterDialog.vue"; // 引入注册组件
import UpdateAvatar from "./UpdateAvatar.vue";
import { useUserInfoStore } from "@/store/userStore";

const avatarDialog = ref(null);
const loginDialog = ref(null);
const registerDialog = ref(null); // 注册弹窗引用
const mfaDialog = ref(null);
const userInfoStore=useUserInfoStore();
const followCount = computed(()=>
{
  return userInfoStore.getUserTotalFollowers||0;
});
const fansCount = computed(()=> {
  return userInfoStore.getUserTotalFans||0;
});

// 打开登录弹窗
const openLoginDialog = () => {
  loginDialog.value.show();
};

// 打开注册弹窗
const openRegisterDialog = () => {
  registerDialog.value.show();
};

// 打开更新头像弹窗
const openAvatarDialog = () => {
  avatarDialog.value.openUpdateDialog();
};

// 打开MFA弹窗
const openMFADialog = () => {
  mfaDialog.value.show();
};

// 定义props用于接收父组件的参数
defineProps({
  modelValue: {
    type: Boolean,
    required: false,
  },
});



// 定义emit，用于更新父组件的显示状态
const emit = defineEmits(["update:modelValue"]);

const router = useRouter();

// 跳转到动态页面
const goToDynamic = () => {
  router.push("/MyDynamicPage");
};

// 跳转到关注页面
const goToFollow = () => {
  router.push("/musicCommunity");
};

// 跳转到粉丝页面
const goToFans = () => {
  router.push("/musicCommunity");
};

// 隐私设置
const goToPrivacy = () => {
  router.push("/privacy");
};

// 退出登录
const logout = () => {
  console.log("退出登录逻辑...");
  Mylogout();
};
//用户是否登录
const isLoggedIn = computed(() => !!userInfoStore.getUserId);

</script>

<style scoped>
.dropdown-container {
  position: absolute;
  top: 50px;
  right: 0;
  background-color: #fff;
  width: 200px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  padding: 10px 0;
  z-index: 1000;
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: background-color 0.2s;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
}

.dropdown-item div {
  font-weight: bold;
  margin-right: 10px;
}

.logout {
  color: red;
}

hr {
  margin: 5px 0;
  border: none;
  border-top: 1px solid #eee;
}
</style>
