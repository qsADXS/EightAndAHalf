C<template>
  <div class="song-page">
    <!-- 歌曲信息 -->
    <div class="song-info">
      <h1>当前歌曲</h1>
      <p>歌名：夜空中最亮的星</p>
      <p>歌手：逃跑计划</p>
      <button @click="toggleSidebar">查看评论</button>
    </div>

    <!-- 评论侧边栏 -->
    <div class="comment-sidebar" :class="{ open: isSidebarOpen }">
      <CommentSection />
    </div>
  </div>
</template>

<script>
import CommentSection from "./CommentSection.vue"; // 导入评论组件

export default {
  components: {
    CommentSection, // 注册组件
  },
  data() {
    return {
      isSidebarOpen: false, // 控制侧边栏显示状态
    };
  },
  methods: {
    toggleSidebar() {
      this.isSidebarOpen = !this.isSidebarOpen; // 切换侧边栏显示状态
    },
  },
};
</script>

<style scoped>
/* 主页面样式 */
.song-page {
  display: flex;
  position: relative;
  padding: 20px;
}

.song-info {
  flex: 1;
}

button {
  padding: 10px 15px;
  background: #0073e6;
  color: #fff;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.3s;
}

button:hover {
  background: #005bb5;
}

/* 评论侧边栏样式 */
.comment-sidebar {
  position: fixed;
  top: 0;
  right: -400px; /* 初始隐藏在右侧 */
  width: 400px;
  height: 100%;
  background: #f9f9f9;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
  transition: right 0.3s ease-in-out;
  overflow-y: auto;
  z-index: 1000;
}

.comment-sidebar.open {
  right: 0; /* 显示侧边栏 */
}
</style>
