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
      <el-button
        :type="isFollowing ? 'primary' : 'default'"
        @click="toggleFollow"
        :style="{
    backgroundColor: isFollowing ? 'rgb(34, 144, 148)' : '#6c757d',  /* 使用 rgb 色值 */
    color: 'white',
    borderColor: isFollowing ? 'rgb(34, 144, 148)' : '#6c757d',  /* 使用 rgb 色值 */
    boxShadow: isFollowing ? '0 4px 8px rgba(34, 144, 148, 0.4)' : '0 4px 8px rgba(108, 117, 125, 0.4)',  /* 使用 rgba 阴影 */
    transition: 'all 0.3s ease'  /* 使用 CSS 过渡效果 */
  }"
      >
        {{ isFollowing ? "已关注" : "未关注" }}
      </el-button>
    </div>

    <!-- 分隔线 -->
    <hr class="divider" />

    <!-- 动态列表 -->
    <div v-if="posts.length > 0">
      <DynamicPost :posts="posts" />
    </div>
    <div v-else class="no-posts">
      <p>这个用户还没有发布任何动态！</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { getUserPosts } from "@/api/user/getUserPosts/index";
import { getUsersInfo } from "@/api/user/getInfo/index";
import { getFollowList } from "@/api/user/getFollowers/index"; // 引入关注列表API
import { follow, unfollow } from "@/api/user/Follow/index"; // 引入关注和取消关注API
import DynamicPost from "@/components/DynamicPost.vue";
import { useUserInfoStore } from "@/store/userStore";

const route = useRoute();
const userId = route.params.userId;

const userName = ref("");
const userAvatar = ref("");
const posts = ref([]);
const isFollowing = ref(false); // 跟踪关注状态
const userInfoStore=useUserInfoStore();

// 获取用户信息、动态和关注状态
const fetchUserData = async () => {
  try {
    // Step 1: 获取用户信息
    const userResponse = await getUsersInfo([userId]);
    if (userResponse) {
      const userInfo = userResponse[0];
      userName.value = userInfo.userName;
      userAvatar.value = userInfo.avatarUrl;
    }

    // Step 2: 获取用户动态
    const postResponse = await getUserPosts(userId, 1, 10);
    posts.value = postResponse.data.map((post) => ({
      ...post,
      username: userName.value,
      avatarUrl: userAvatar.value,
    }));

    // Step 3: 检查关注状态
    const followListResponse = await getFollowList(userInfoStore.getUserId,0,500);
    if (followListResponse.data.some(user => user.userId === userId)) {
      isFollowing.value = true;
    } else {
      isFollowing.value = false;
    }
  } catch (error) {
    console.error("获取用户数据或动态失败", error);
  }
};

// 切换关注状态
const toggleFollow = async () => {
  try {
    if (isFollowing.value) {
      await unfollow(userId);
      isFollowing.value = false;
    } else {
      await follow(userId);
      isFollowing.value = true;
    }
  } catch (error) {
    console.error("切换关注状态失败", error);
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchUserData();
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
</style>
