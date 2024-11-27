<template>
    <div class="comment-section">
      <!-- 发布评论 -->
      <div class="comment-input">
        <el-input
          type="textarea"
          v-model="newComment"
          placeholder="写下你的评论..."
          :rows="3"
        ></el-input>
        <el-button type="primary" @click="submitComment" :loading="isSubmitting" :style="{backgroundColor:'rgb(34,144,148)'}">发布</el-button>
      </div>
  
      <!-- 展示评论 -->
      <div class="comment-list">
        <div
          class="comment-item"
          v-for="(parentComment, index) in parentComments"
          :key="parentComment.commentId"
        >
          <!-- 父评论 -->
          <div class="parent-comment">
            <el-avatar :src="getCachedAvatar(parentComment.userId)" />
            <div class="comment-content">
              <p class="username">{{ getCachedUserName(parentComment.userId) }}</p>
              <p class="content">{{ parentComment.commentContent }}</p>
              <div class="comment-actions">
                <span class="action" @click="likeComment(parentComment)">
                  👍 {{ parentComment.likeCount }}
                </span>
                <span class="action" @click="showReplyBox(parentComment.commentId)">回复</span>
              </div>
            </div>
          </div>
  
          <!-- 子评论 -->
          <div class="child-comments" v-if="childCommentsMap[parentComment.commentId]">
            <div
              class="child-comment"
              v-for="child in childCommentsMap[parentComment.commentId]"
              :key="child.commentId"
            >
              <el-avatar :src="getCachedAvatar(child.userId)" />
              <div class="comment-content">
                <p class="username">{{ getCachedUserName(child.userId) }}</p>
                <p class="content">{{ child.commentContent }}</p>
              </div>
            </div>
          </div>
  
          <!-- 回复输入框 -->
          <div class="reply-input" v-if="replyingTo === parentComment.commentId">
            <el-input
              type="textarea"
              v-model="replyContent"
              placeholder="写下你的回复..."
              rows="2"
            ></el-input>
            <el-button type="primary" @click="submitReply(parentComment.commentId)":style="{backgroundColor:'rgb(34,144,148)'}">回复</el-button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, onMounted } from "vue";
  import { getUsersInfo } from "@/api/user/getInfo";
  import defaultAvatar from "@/assets/avator/xingyue.jpg";
  
  // 响应式缓存对象，用于存储用户信息
  const userCache = ref({}); // { userId: { userName: "xxx", avatarUrl: "xxx" } }
  
  // 从 props 获取评论列表和博客 ID
  const props = defineProps({
    comments: {
      type: Array,
      required: true,
    },
    blogId: {
      type: String,
      required: true,
    },
  });
  
  const emit = defineEmits(["postComment", "postReply", "likeComment"]);
  
  const newComment = ref("");
  const replyContent = ref("");
  const replyingTo = ref(null);
  const isSubmitting = ref(false);
  
  // 父评论和子评论映射
  const parentComments = computed(() =>
    props.comments.filter((comment) => !comment.parentId)
  );
  
  const childCommentsMap = computed(() => {
    const map = {};
   
    props.comments.forEach((comment) => {
      if (comment.parentId) {
        console.log(comment.parentId);
        if (!map[comment.parentId]) {
          map[comment.parentId] = [];
        }
        map[comment.parentId].push(comment);
      }
    });
    console.log(map);
    return map;
  });
  
  // 提交评论
  const submitComment = () => {
    console.log("提交评论");
    if (!newComment.value.trim()) return;
    isSubmitting.value = true;
    emit("postMyComment", newComment.value.trim(), props.blogId, () => {
      isSubmitting.value = false;
      newComment.value = "";
    });
  };
  
  // 显示回复框
  const showReplyBox = (commentId) => {
    replyingTo.value = commentId;
  };
  
  // 提交回复
  const submitReply = (parentId) => {
    if (!replyContent.value.trim()) return;
    emit("postReply", parentId, replyContent.value.trim(),props.blogId, () => {
      replyingTo.value = null;
      replyContent.value = "";
    });
  };
  
  // 点赞评论
  const likeComment = (comment) => {
    emit("likeComment", comment.commentId);
  };
  
  // 获取缓存的头像 URL
  const getCachedAvatar = (userId) => {
    return userCache.value[userId]?.avatarUrl || defaultAvatar;
  };
  
  // 获取缓存的用户名
  const getCachedUserName = (userId) => {
    return userCache.value[userId]?.userName || "加载中...";
  };
  
  // 批量加载用户信息
  const preloadUserInfo = async () => {
    const userIds = [...new Set(props.comments.map((comment) => comment.userId))];
    const response = await getUsersInfo(userIds);
    response.forEach((user) => {
      userCache.value[user.userId] = {
        userName: user.userName || "未命名用户",
        avatarUrl: user.avatarUrl || defaultAvatar,
      };
    });
  };
  
  // 在组件挂载时预加载用户信息
  onMounted(() => {
    preloadUserInfo();
  });
  </script>
  
  <style scoped>
  .comment-section {
    padding: 16px;
    border-top: 1px solid #ebeef5;
  }
  
  .comment-input {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
  }
  
  .comment-list {
    margin-top: 16px;
  }
  
  .comment-item {
    margin-bottom: 16px;
    border-bottom: 1px solid #ebeef5;
    padding-bottom: 16px;
  }
  
  .parent-comment,
  .child-comment {
    display: flex;
    gap: 8px;
  }
  
  .comment-content {
    flex: 1;
  }
  
  .username {
    font-weight: bold;
    margin-bottom: 4px;
  }
  
  .content {
    margin-bottom: 8px;
  }
  
  .comment-actions {
    display: flex;
    gap: 16px;
    font-size: 14px;
  }
  
  .action {
    cursor: pointer;
    color: rgb(34,144,148);
  }
  
  .child-comments {
    margin-left: 48px;
    margin-top: 8px;
  }
  
  .reply-input {
    margin-left: 48px;
    margin-top: 8px;
    display: flex;
    gap: 8px;
  }
  </style>
  