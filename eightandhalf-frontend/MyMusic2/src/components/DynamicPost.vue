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
        <!-- 删除动态按钮，只有在 showDelete 为 true 时显示 -->
        <el-button
          v-if="showDelete"
          type="text"
          icon="el-icon-delete"
          class="delete-button"
          @click="handleDelete(post.blogId)"
        >
          删除
        </el-button>
      </div>

      <!-- 动态标题 -->
      <div class="post-title" v-if="post.title">
        <h3>{{ post.title }}</h3>
      </div>

      <!-- 动态内容 -->
      <div class="post-content">
        <p>{{ post.blogContent }}</p>
        <!-- 仅在 post.url 存在且为图片时显示图片 -->
        <el-image
          v-if="post.url && isImage(post.url)"
          :src="post.url"
          class="post-image"
          @click="previewImage(post.url)"
          fit="contain"
        />

        <!-- 仅在 post.url 存在且为音频时显示音频链接 -->
        <el-link
          v-else-if="post.url && isAudio(post.url)"
          :href="post.url"
          target="_blank"
        >
          点击播放音乐
        </el-link>
      </div>

      <!-- 动态操作 -->
<div class="post-actions">
  <el-button
    type="text"
    class="like-button"
    @click="toggleLike(post)"
  >
    <img :src="post.isLiked ? AfterLike : like" class="like-icon" />
    <span :style="{ color: 'rgb(34, 144, 148)' }">{{ post.likeCount }}</span>
  </el-button>
  <el-button type="text" @click="toggleComments(post)">
    <span :style="{ color: 'rgb(34, 144, 148)' }">评论</span>
  </el-button>
  <el-button
    type="text"
    icon="el-icon-share"
    @click="handleRepost(post.blogId)"
  >
    <span :style="{ color: 'rgb(34, 144, 148)' }">转发</span>
  </el-button>
</div>


      <!-- 评论区域 -->
      <div v-if="post.isCommentOpen">
        <CommentSection
          :comments="post.comments"
          :blogId="post.blogId"
          @postMyComment="handlePostComment"
          @postReply="handlePostReply"
          @likeComment="handleLikeComment"
        />
      </div>

    </div>
  </div>

</template>

<script setup>
import { ElAvatar, ElButton, ElImage, ElLink, ElMessage, ElMessageBox } from "element-plus";
import { defineProps, onMounted } from "vue";
import { useRouter } from "vue-router";
import { likeBlog, unlikeBlog,likeComment,unlikeComment} from "@/api/user/Like/index";
import { DeletePost } from "@/api/user/DeletePosts";
import { transmit } from "@/api/user/transmitBlog";
import { getCommentList } from "@/api/user/getCommentList"; // 假设这是获取评论的 API 函数
import CommentSection from "./CommentSection.vue";
import { Comment } from "@/api/user/postComment";
import AfterLike from "@/assets/AfterLike.png";
import like from "@/assets/like.png";

const props = defineProps({
  posts: {
    type: Array,
    required: true,
  },
  showDelete: {
    type: Boolean,
    default: false,
  },
  isOwnPage: {
    type: Boolean,
    default: false,
  },
});

const router = useRouter();

const formatDate = (date) => {
  const d = new Date(date);
  return `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`;
};

const isImage = (url) => /\.(jpg|jpeg|png|gif|webp)$/.test(url);
const isAudio = (url) => /\.(mp3|wav|ogg)$/.test(url);

const navigateToUser = (userId) => {
  if (!props.isOwnPage) {
    router.push({ name: "OtherUserPosts", params: { userId } });
  }
};

const previewImage = (url) => {
  ElMessageBox.alert(`<img src="${url}" style="width:100%">`, "图片预览", {
    dangerouslyUseHTMLString: true,
  });
};

// 初始化点赞状态
onMounted(() => {
  props.posts.forEach((post) => {
    post.isLiked = false; // 默认未点赞，具体实现可基于用户数据初始化
    post.isCommentOpen = false;
    post.comments = []; // 默认评论为空
  });
});

const toggleComments = async (post) => {
  // 如果当前post已经有评论，直接切换显示状态
  if (post.comments.length > 0) {
    post.isCommentOpen = !post.isCommentOpen;
    return;
  }

  // 否则，获取评论数据并更新
  try {
    const res = await getCommentList(0,200,"blog",post.blogId); // 这是获取评论的 API
    if (res.base.code === 10000) {
      post.comments = res.data|| []; // 更新评论
      post.isCommentOpen = true; // 打开评论区
    }
  } catch (error) {
    ElMessage.error("获取评论失败");
    console.error("获取评论失败", error);
  }
};

const toggleLike = async (post) => {
  try {
    if (!post.isLiked) {
      const res = await likeBlog(post.blogId);
      if (res.base.code === -1) {
        ElMessage.info("你已经点过赞了！");
      } else if (res.base.code === 10000) {
        post.likeCount += 1;
        post.isLiked = true;
      }
    } else {
      const res = await unlikeBlog(post.blogId);
      if (res.base.code === 10000) {
        post.likeCount -= 1;
        post.isLiked = false;
      }
    }
  } catch (error) {
    console.error("点赞操作失败", error);
  }
};

const handlePostComment = async (content,blogId, callback) => {
    console.log("收到评论事件，评论内容：", content);
    const response = await Comment({ content:content, type: "blog", id: blogId });
    if (response.base.code === 10000) {
      const post = props.posts.find((p) => p.blogId === blogId);
      const res = await getCommentList(0,200,"blog",post.blogId); // 这是获取评论的 API
      if (res.base.code === 10000) {
       post.comments = res.data|| []; // 更新评论
      }
      if (post) {
        console.log("尝试更新");
        callback(); // 清空输入框
      }
     
    } else {
      ElMessage.error("评论失败，请稍后重试");
    }

};

const handlePostReply= async (parentId,content,blogId,callback)=>{
  console.log("收到回复评论事件，评论内容：", content);
  console.log("父评论", parentId);
  const response = await Comment({ content:content, type: "comment", id: parentId});
  if (response.base.code === 10000) {
    const post = props.posts.find((p) => p.blogId === blogId);
      const res = await getCommentList(0,200,"blog",post.blogId); // 这是获取评论的 API
      if (res.base.code === 10000) {
       post.comments = res.data|| []; // 更新评论
      }
      if (post) {
        console.log("尝试更新");
        callback(); // 清空输入框
      }
          
    } else {
      ElMessage.error("评论失败，请稍后重试");
    }
}

const handleLikeComment = async (commentId) => {
  try {
    const response = await likeComment(commentId); // 调用点赞 API
    if(response.base.code === -1){
      ElMessage.info("你已经点过赞啦！");
    }else if (response.base.code === 10000) {
      // 更新对应评论的点赞数
      props.posts.forEach((post) => {
        post.comments.forEach((comment) => {
          if (comment.commentId === commentId) {
            comment.likeCount += 1; // 点赞成功后更新数据
          }
        });
      });
    }
  } catch (error) {
    console.error("点赞失败", error);
  }
}


const handleRepost = async (blogId) => {
  const res = await transmit(blogId);
  if (res.base.code === 10000) {
    ElMessage.info("转发动态成功!");
  }
};

const handleDelete = (blogId) => {
  ElMessageBox.confirm("确定要删除这条动态吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      const res = await DeletePost(blogId);
      if (res.base && (res.base.code === 10000 || res.base.code === "10000")) {
        ElMessage.info("删除成功！虽然它还在列表中，但过几分钟它就自动消失了");
      }
    })
    .catch(() => {
      console.log("取消删除");
    });
};
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
  margin-bottom: 10px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  cursor: pointer;
}

.post-user-info {
  flex: 1;
  margin-left: 10px;
}

.username {
  font-weight: bold;
}

.created-time {
  font-size: 12px;
  color: #909399;
}

.post-title {
  font-weight: bold;
  font-size: 18px;
  margin: 10px 0;
}

.post-content {
  display: inline-block;
  margin: 10px 0;
}

.post-image {
  width: 100%;
  height: 400px;
  object-fit: contain;
  border-radius: 8px;
  cursor: pointer;
}

.like-button {
  font-size: 14px;
  padding: 0;
  margin-right: 10px;
}

.like-icon {
  width: 18px;
  height: 18px;
  margin-right: 5px;
}

.post-actions {
  display: flex;
  align-items: center;
}

.delete-button {
  color: #f56c6c;
  padding: 0;
}
</style>
