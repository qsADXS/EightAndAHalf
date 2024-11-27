<template>
  <div>
      <hr>
      <br>
      <br>
  </div>
  <div class="dynamic-page">
    <!-- 用户信息 -->
    <div class="user-info">
      <br>
      <br>
      <br>
      <br>
      <br>
      <br>

      <el-avatar :src="userAvatar" size="large" />
      <p>{{ userName }}</p>
    </div>

    <!-- 分隔线 -->
    <hr class="divider" />

    <!-- 动态列表 -->
    <div v-if="posts.length > 0">
      <DynamicPost :posts="posts" :showDelete="true" :isOwnPage="true"/>
    </div>
    <div v-else class="no-posts">
      <p>这个用户还没有发布任何动态，快来发布第一条动态吧！</p>
    </div>

    <!-- 自定义发布动态按钮 -->
    <button class="custom-publish-button" @click="openPublishDialog" :style="{backgroundColor:'rgb(34,144,148)'}" >
      <i class="el-icon-edit"></i> 发布动态
    </button>

    <!-- 发布动态弹窗 -->
    <PublishPostDialog ref="publishDialog" @postSuccess="refreshPosts" />
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useUserInfoStore } from "@/store/userStore";
import { getUserPosts } from "@/api/user/getUserPosts";
import DynamicPost from "@/components/DynamicPost.vue";
import PublishPostDialog from "@/components/PublishPostDialog.vue";
import { Edit } from "@element-plus/icons-vue"; // 引入图标

const userInfoStore = useUserInfoStore();
const userId = userInfoStore.getUserId;
const userName = userInfoStore.getUserName;
const userAvatar = userInfoStore.getUserAvatar;

const posts = ref([]);
const publishDialog = ref(null);

// 获取并处理动态列表数据
const fetchPosts = async () => {
  try {
    const response = await getUserPosts(userId, 1, 10);
    posts.value = response.data.map((post) => ({
      ...post,
      username: userName,
      avatarUrl: userAvatar,
    }));
    console.log(posts.value);
  } catch (error) {
    console.error("获取动态数据失败", error);
  }
};

// 打开发布动态弹窗
const openPublishDialog = () => {
  if (publishDialog.value) {
    publishDialog.value.show();
  }
};

// 发布成功后刷新动态列表
const refreshPosts = () => {
  fetchPosts();
};

onMounted(() => {
  fetchPosts(); // 初始化加载动态
});
</script>

<style scoped>
.dynamic-page {
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.divider {
  margin-bottom: 20px;
  border: none;
  border-top: 1px solid #ebeef5;
}

.no-posts {
  text-align: center;
  color: #909399;
  font-size: 16px;
  margin-top: 20px;
}

/* 自定义发布动态按钮样式 */
.custom-publish-button {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background-color: #4CAF50; /* 绿色主题背景色 */
  color: #ffffff;
  border: none;
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.2);
  transition: background-color 0.3s, box-shadow 0.3s;
}

.custom-publish-button:hover {
  background-color: #45a049; /* 悬停效果 */
  box-shadow: 0px 6px 12px rgba(0, 0, 0, 0.3);
}

.custom-publish-button i {
  font-size: 18px; /* 图标大小 */
}
</style>
