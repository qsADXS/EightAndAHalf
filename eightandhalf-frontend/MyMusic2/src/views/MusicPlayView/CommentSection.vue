<template>
  <div class="comment-section">
    <!-- 评论标题 -->
    <h2>评论区</h2>

        <!-- 评论输入框 -->
    <div class="comment-input">
      <textarea
        v-model="yourcomment"
        placeholder="请输入您的评论..."
        class="input-area"
      ></textarea>
      <button @click="addMusicComment" class="submit-btn" :style="{backgroundColor:'rgb(34,144,148)'}">发表</button>
    </div>

    <!-- 评论列表 -->
    <div v-if="comments.length > 0" class="comment-list">
      <div v-for="(comment, index) in newcomments" :key="index" class="comment-item">
        <div class="comment-header">
          <span class="username" :style="{color:'rgb(34,144,148)'}">  {{ comment.userName }} </span>
          <span class="icon"> 
            <img 
            :src="comment.avatarUrl" 
            alt="avatar" 
            class="avatar" 
            @click="gotohisspace(comment.userId)" 
          />
          </span>
          <!-- <span class="timestamp">{{ comment.createdAt }}</span> -->
        </div>
        <p class="comment-text">{{ comment.commentContent }}</p>
      </div>
    </div>
    <div v-else class="no-comments">暂无评论。</div>
  </div>
</template>

<script>
import { addComment } from '@/api/music/addcomment';
import { getComment } from '@/api/music/getcomment';
import { getUsersInfo } from '@/api/user/getInfo';
import { method } from 'lodash';
import { isMethodSignature } from 'typescript';

export default {
  props:{
    newcomments:{
      type:Array,
      default:()=>[]
    },
    id:{
      type:Number,
      default:0
    }
  },
  data() {
    return {
      yourcomment: '',
      // 静态评论数据
      comments: [
        {

        },
      ],
    };
  },
  methods:{


    addMusicComment(){
      addComment(this.yourcomment,'music',this.id).then((res)=>{
        console.log(res);
        this.yourcomment = '';
      })
      getComment('music',this.id).then((res)=>{
        console.log(res);
        this.newcomments = res.data;
        console.log(this.newcomments);
      })
      window.location.reload();



    },
    gotohisspace(id){
      this.$router.push({
          name:"OtherUserPosts",
          params:{
            userId:id
          }
        })
    }

  },

};
</script>

<style scoped>
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 10px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.comment-section {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.comment-input {
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}

.input-area {
  width: 90%;
  height: 80px;
  padding: 10px;
  font-size: 1rem;
  border: 1px solid #ddd;
  border-radius: 5px;
  margin-bottom: 10px;
  resize: none;
}


.submit-btn {
  align-self: flex-end;
  background-color: #0073e6;
  color: white;
  border: none;
  padding: 10px 20px;
  font-size: 1rem;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 20px;
}

.comment-list {
  margin-bottom: 20px;
}

.comment-item {
  padding: 10px;
  border-bottom: 1px solid #ddd;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.username {
  font-weight: bold;
  color: #0073e6;
}

.timestamp {
  color: #999;
  font-size: 0.9rem;
}

.comment-text {
  color: #333;
  line-height: 1.4;
}

.no-comments {
  text-align: center;
  color: #999;
  font-size: 1rem;
}
</style>
