<template>
  <div class="dynamic-posts">
    <div
      class="post-container"
      v-for="(post, index) in posts"
      :key="post.blogId"
    >
      <div class="post-header">
        <!-- 头像 -->
        <el-avatar
          :src="post.avatarUrl"
          class="user-avatar"
          @click="navigateToUser(post.userId)"
        />
        <div class="post-user-info">
          <p class="username">{{ post.username }}</p>
          <p class="created-time">{{ formatDate(post.createdAt) }}</p>
        </div>
      </div>

      <!-- 动态标题 -->
      <div class="post-title" v-if="post.title">
        <h3>{{ post.title }}</h3>
      </div>

      <!-- 动态内容 -->
      <div class="post-content">
        <p>{{ post.blogContent }}</p>
        <el-image
          v-if="post.url && isImage(post.url)"
          :src="post.url"
          class="post-image"
          fit="contain"
        />
      </div>

      <!-- 动态操作 -->
      <div class="post-actions">
        <el-button
          type="text"
          class="like-button"
          @click="toggleLike(post)"
        >
          <img :src="post.isLiked ? 'src/assets/AfterLike.png' : 'src/assets/like.png'" class="like-icon" />
          {{ post.likeCount }}
        </el-button>
        <el-button type="text" @click="toggleComments(post.blogId)">
          评论
        </el-button>
        <el-button type="text" @click="handleRepost(post.blogId)">
          转发
        </el-button>
      </div>

      <!-- 评论区域 -->
      <div v-if="post.isCommentOpen">
        <CommentSection
          :comments="post.comments"
          @postComment="handlePostComment(post.blogId)"
          @postReply="handlePostReply(post.blogId)"
          @likeComment="handleLikeComment"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElAvatar, ElImage, ElButton } from "element-plus";
import CommentSection from "@/components/CommentSection.vue";

const posts = ref([
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
    title: "你好啊，新来的",
    isCommentOpen: false, // 控制评论展开
    comments: [
      {
        commentId: "1",
        userId: "101",
        parentId: null,
        commentContent: "这是一条父评论",
        likeCount: 5,
        createdAt: "2024-11-01T10:00:00",
      },
      {
        commentId: "2",
        userId: "102",
        parentId: "1",
        commentContent: "这是一个子评论",
        likeCount: 3,
        createdAt: "2024-11-01T11:00:00",
      },
    ],
  },
]);

const toggleComments = (blogId) => {
  const post = posts.value.find((p) => p.blogId === blogId);
  if (post) post.isCommentOpen = !post.isCommentOpen;
};

const handlePostComment = (blogId) => (content, callback) => {
  console.log("发布评论:", { blogId, content });
  callback(); // 清空输入框
};

const handlePostReply = (blogId) => ({ parentId, content }, callback) => {
  console.log("发布回复:", { blogId, parentId, content });
  callback(); // 清空回复框
};

const handleLikeComment = (commentId) => {
  console.log("点赞评论:", commentId);
};

const toggleLike = (post) => {
  post.isLiked = !post.isLiked;
  post.likeCount += post.isLiked ? 1 : -1;
};

const handleRepost = (blogId) => {
  console.log("转发动态:", blogId);
};

const navigateToUser = (userId) => {
  console.log("跳转到用户主页:", userId);
};

const formatDate = (date) => {
  const d = new Date(date);
  return `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`;
};

const isImage = (url) => /\.(jpg|jpeg|png|gif|webp)$/.test(url);
</script>

<style scoped>
.dynamic-posts {
  width: 100%;
  margin: auto;
}

.post-container {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.post-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-avatar {
  width: 40px;
  height: 40px;
}

.post-user-info {
  flex: 1;
  margin-left: 10px;
}

.username {
  font-weight: bold;
}

.post-title {
  font-size: 18px;
  margin: 10px 0;
}

.post-content {
  margin: 10px 0;
}

.post-actions {
  display: flex;
  justify-content: space-around;
  margin-top: 10px;
}

.like-icon {
  width: 24px;
  height: 24px;
  margin-right: 5px;
}
</style>
