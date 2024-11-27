<template>
  <div class="community-layout">
    <!-- 左侧导航栏 -->
    <el-menu
      class="side-menu"
      :default-active="activeItem"
      unique-opened
      router
    >
      <el-sub-menu
        v-for="(menu, index) in menus"
        :key="index"
        :index="menu.path"
        :opened="menu.showList"  
        @click="handleSubMenuClick(menu)"
      >
        <template #title>
          <el-icon><component :is="menu.icon" /></el-icon>
          <span>{{ menu.name }}</span>
        </template>

        <!-- 用户列表 -->
        <div
          v-if="menu.showList"
          class="user-list-container"
          @scroll="(event) => handleScroll(menu, event)"
        >
          <div 
            v-for="user in menu.userList" 
            :key="user.userId || user.userName" 
            class="user-item"
            @click="viewUserInfo(user, $event)"
          >
            <el-avatar :src="user.avatarUrl || defaultAvatar" />
            <span>{{ user.userName }}</span>
          </div>

          <div v-if="menu.isLoading" class="loading">加载中...</div>
          
          <div v-if="!menu.isLoading && menu.userList.length === 0" class="no-data">
            什么，你还没有{{ menu.name }}！
          </div>
        </div>
      </el-sub-menu>
    </el-menu>

    <!-- 右侧内容区域，展示动态内容 -->
    <div class="main-content">
      <DynamicPost :posts="posts" />
    </div>
  </div>
</template>

<script setup>
import { markRaw, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { User, UserFilled, Star } from "@element-plus/icons-vue";
import { getFollowList } from "@/api/user/getFollowers/index";
import { getFanList } from "@/api/user/getFans/index";
import { getFriendList } from "@/api/user/getFriends/index";
import { getAllFollowPosts } from "@/api/user/getAllFollowPosts/index"; // 导入获取所有动态的接口
import { useUserInfoStore } from "@/store/userStore";
import defaultAvatar from "@/assets/avator/xingyue.jpg";
import DynamicPost from "@/components/DynamicPost.vue";

const userInfoStore = useUserInfoStore();
const router = useRouter();
const posts = ref([]); // 动态 posts 数据
// 定义菜单数据和结构
const menus = ref([
  {
    name: "好友",
    path: "/friends",
    icon: markRaw(User),
    type: "friends",
    showList: false,
    userList: [],
    pageNum: 0,
    isLoading: false,
    isFinished: false,
  },
  {
    name: "粉丝",
    path: "/fans",
    icon: markRaw(UserFilled),
    type: "fans",
    showList: false,
    userList: [],
    pageNum: 0,
    isLoading: false,
    isFinished: false,
  },
  {
    name: "关注",
    path: "/following",
    icon: markRaw(Star),
    type: "following",
    showList: false,
    userList: [],
    pageNum: 0,
    isLoading: false,
    isFinished: false,
  },
]);

const activeItem = ref("/friends");


// 获取关注的所有动态数据
const fetchPosts = async () => {
  try {
    const response = await getAllFollowPosts(50); // 设置获取数量限制为 50
    console.log(response);
    if(response.length > 0){
      posts.value=response.map((post)=>({
        ...post,
        comments:[],
      }));
    }else{
      return FatherPosts.value;
    }

  } catch (error) {
    console.error("获取关注动态失败", error);
    posts.value = FatherPosts.value; // 如果获取失败，使用静态数据
  }
};

// 静态的默认动态内容，仅在没有动态时显示
const FatherPosts = ref([
  {
    blogId: "334536112959787009",
    userId: "338812323689009152",
    username: "八部半音乐小秘书",
    avatarUrl: "https://linbei-1326669563.cos.ap-shanghai.myqcloud.com/test/weixin.jpg",
    likeCount: 0,
    blogContent: "你好像没有关注别人呢，或者你关注的人还没有发过动态",
    url: "https://linbei-1326669563.cos.ap-shanghai.myqcloud.com/test/nahida.jpg",
    musicId: null,
    commentCount: 2,
    createdAt: "2024-11-02T20:13:10",
    title: "你好啊，新来的"
  },
]);

// 页面加载时获取数据
onMounted(() => {
  fetchPosts();
});

// 点击 el-sub-menu 展开/收起时展示用户列表
const handleSubMenuClick = async (menu) => {
  // 关闭其他菜单的展示状态
  menus.value.forEach((item) => {
    if (item !== menu) item.showList = false;
  });
  menu.showList = !menu.showList;
  if (menu.showList && menu.userList.length === 0 && !menu.isFinished) {
    await loadMore(menu);
  }
};

// 加载更多用户数据
const loadMore = async (menu) => {
  if (menu.isLoading || menu.isFinished) return;
  menu.isLoading = true;

  try {
    const userId = userInfoStore.getUserId;
    const pageSize = 10;
    let response;

    if (menu.type === "friends") {
      response = await getFriendList(userId, menu.pageNum, pageSize);
    } else if (menu.type === "fans") {
      response = await getFanList(userId, menu.pageNum, pageSize);
    } else if (menu.type === "following") {
      response = await getFollowList(userId, menu.pageNum, pageSize);
    }

    if (response.data && response.data.length > 0) {
      menu.userList = [...menu.userList, ...response.data];
      menu.pageNum++;
    } else {
      menu.isFinished = true;
    }
  } finally {
    menu.isLoading = false;
  }
};

// 监听滚动事件加载更多
const handleScroll = (menu, event) => {
  const container = event.target;
  if (container.scrollTop + container.clientHeight >= container.scrollHeight - 10) {
    if (!menu.isLoading && !menu.isFinished) {
      loadMore(menu);
    }
  }
};

// 查看用户详情或聊天，跳转至他人动态页面
const viewUserInfo = (user, event) => {
  event.stopPropagation();
  router.push({ name: "OtherUserPosts", params: { userId: user.userId } });
};
</script>

<style scoped>
.community-layout {
  display: flex;
  height: 100vh;
}

.side-menu {
  width: 240px;
  height: 100%;
  background-color: #f8f8f8;
  border-right: 1px solid #ddd;
}

.main-content {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
}

.user-list-container {
  padding: 10px;
  background-color: #f4f4f4;
  max-height: 300px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 5px 0;
  cursor: pointer;
}

.user-item span {
  margin-left: 8px;
  font-size: 14px;
}

.user-item:hover {
  background-color: #e6e6e6;
}

.loading {
  text-align: center;
  padding: 10px;
  color: #999;
}

.no-data {
  text-align: center;
  padding: 10px;
  color: #999;
}
</style>
