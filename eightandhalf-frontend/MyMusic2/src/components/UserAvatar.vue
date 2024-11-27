<template>
  <div class="user-avatar-container" ref="dropdownContainer">
    <!-- 头像 -->
    <el-avatar :src="userAvatar" :style="{ width: '40px', height: '40px' }" />

    <!-- 用户名 -->
    <span class="user-name">{{ userName }}</span>

    <!-- 下拉箭头 -->
    <el-icon @click="toggleDropdown" :class="['dropdown-icon', { active: isDropdownVisible }]">
      <ArrowDownBold />
    </el-icon>

    <!-- 下拉框 -->
    <DropdownMenu v-model:modelValue="isDropdownVisible" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted,computed} from "vue";
import defaultAvatar from "@/assets/avator/xingyue.jpg"; // 静态资源导入
import DropdownMenu from "./DropdownMenu.vue"; // 引入下拉框组件
import { useUserInfoStore } from "@/store/userStore";




 const userInfoStore=useUserInfoStore();
 const userAvatar=computed(()=>userInfoStore.getUserAvatar);
 const userName = computed(()=>userInfoStore.getUserName);



// 控制下拉框显示状态
const isDropdownVisible = ref(false);

// 切换下拉框显示状态
const toggleDropdown = () => {
  isDropdownVisible.value = !isDropdownVisible.value;
};

// 关闭下拉框
const closeDropdown = () => {
  isDropdownVisible.value = false;
};

// 监听点击事件，判断是否点击在外部
const dropdownContainer = ref(null);

const handleClickOutside = (event) => {
  if (dropdownContainer.value && !dropdownContainer.value.contains(event.target)) {
    closeDropdown();
  }
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>

<style scoped>
.user-avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
}

.user-name {
  margin-left: 10px;
  font-size: 14px;
  color: #fff;
}

.dropdown-icon {
  margin-left: 5px;
  transition: transform 0.3s;
}

.dropdown-icon.active {
  transform: rotate(180deg);
}
</style>
