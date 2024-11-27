<template>
  <div class="chat-page" :class="{ 'with-bg': !currentChatUser }">
  <div class="chat-room">
    <!-- 用户列表 -->
    <div class="user-list" v-loading="isLoading">
      <div class="user-list-title">
        <strong>我的好友</strong>
      </div>
      <!-- 判断好友列表是否为空 -->
      <div v-if="(!users||users.length === 0 )&& !isLoading" class="no-friends">
        什么？你还没有好友，快去交朋友啦！
      </div>
      <div
        v-else
        v-for="user in users"
        :key="user.userId"
        class="user-item"
        @click="openChat(user)"
      >
        <el-avatar :src="user.avatarUrl || defaultAvatar" />
        <span class="user-name">{{ user.userName }}</span>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-area" v-if="currentChatUser">
      <div class="chat-header">
        <el-avatar :src="currentChatUser.avatarUrl || defaultAvatar" />
        <span>{{ currentChatUser.userName }}</span>
      </div>

      <!-- 消息列表 -->
      <div class="message-list">
        <div
            v-for="message in messages"
            :key="message.messageId"
            :class="['message', message.sender === userId ? 'sent' : 'received']"
        >
          <el-avatar
              :src="message.sender === userId ? userInfoStore.getUserAvatar : currentChatUser.avatarUrl || defaultAvatar"
              class="message-avatar"
          />
          <div class="message-content">
            <p v-if="!isImage(message.content)">
              {{ message.content }}
            </p>
            <img v-else :src="message.content" class="message-image" />
          </div>
        </div>
      </div>

      <!-- 消息输入 -->
      <div class="message-input">
        <el-input
            type="textarea"
            v-model="newMessage"
            placeholder="输入消息..."
            @keydown="handleKeyDown"
        />
        <el-button @click="sendMessage">发送</el-button>
        <!-- 上传图片按钮 -->
        <input type="file" @change="handleImageUpload" accept="image/*" />
      </div>
    </div>

  </div>
</div>

</template>

<script lang="ts" setup>
import { ref, onMounted,nextTick } from 'vue';
import { getFriendList } from '@/api/user/getFriends';
import { getChatHistory, sendImageToUser } from '@/api/chat/index';
import { initializeWebSocket } from '@/utils/websocket';
import defaultAvatar from "@/assets/avator/xingyue.jpg";
import { useUserInfoStore } from '@/store/userStore';

interface User {
  userId: string;
  userName: string;
  avatarUrl: string | null;
}

interface Message {
  messageId: string;
  sender: string;
  receiver?: string;
  content: string;
  createdAt?: string;
  isLocal?: boolean;
}

const userInfoStore = useUserInfoStore();
const userId = userInfoStore.getUserId; // 从 store 中获取当前用户 ID
const users = ref<User[]>([]);
const messages = ref<Message[]>([]);
const newMessage = ref<string>("");
const currentChatUser = ref<User | null>(null);
const isLoading=ref(true);


let socket: WebSocket | null = null;

// 判断消息是否是图片链接
const isImage = (url: string): boolean => {
  return /\.(jpg|jpeg|png|gif|webp)$/.test(url) || url.startsWith("blob:");
};

const loadFollowedUsers = async () => {
  isLoading.value = true; // 开始加载
  if (userId) {
    try {
      const response = await getFriendList(userId, 0, 200);
      users.value = response.data;
      isLoading.value = false;
    } catch (error) {
      console.error("加载好友列表失败", error);
    } finally {
      isLoading.value = false; // 加载完成
    }
  }
};

// 打开与某个用户的聊天，获取历史消息
const openChat = async (user: User) => {
  currentChatUser.value = user;

  const maxRetries = 3;
  let attempts = 0;
  let history = null;

  while (attempts < maxRetries) {
    try {
      history = await getChatHistory(user.userId);
      if (history.data) {
        messages.value = history.data.items.map((item: any) => ({
          messageId: item.messageId,
          sender: item.userId,
          receiver: item.toUserId,
          content: item.messageContent,
          createdAt: item.createdAt,
          isLocal: false, // 标记为非本地图片
        }));
        console.log("成功获取历史消息");
        break; // 成功后跳出循环
      }
    } catch (error) {
      console.info(`获取历史消息尝试 ${attempts + 1} 失败:`, error);
    }
    attempts++;
  }

  if (!history) {
    console.error("多次尝试后未能获取历史消息");
    // 可以在这里添加一个提示消息，通知用户历史消息加载失败
  }
  scrollToBottom();
};

// 发送图片消息
const handleImageUpload = async (event: Event) => {
  if (userId && currentChatUser.value) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      try {
        // 生成本地图片预览 URL
        const localImageUrl = URL.createObjectURL(file);

        // 将本地图片暂时显示在消息列表中
        messages.value.push({
          messageId: Date.now().toString(),
          sender: userId,
          content: localImageUrl,
          isLocal: true, // 标记为本地图片
        });

        // 调用 API 发送图片给后端
        await sendImageToUser(currentChatUser.value.userName, file);

      } catch (error) {
        console.error("图片发送失败", error);
      }
    }
    scrollToBottom();
  }
};

// 发送文本消息
const sendMessage = () => {
  if (!newMessage.value.trim() || !currentChatUser.value||userId===null) return;

  const message = {
    type: 1,
    to: currentChatUser.value.userName,
    content: newMessage.value,
  };

  if (socket) {
    socket.send(JSON.stringify(message));
  }

  messages.value.push({
    messageId: Date.now().toString(),
    sender: userId,
    receiver: currentChatUser.value.userId,
    content: newMessage.value,
    createdAt: new Date().toISOString(),
  });

  newMessage.value = "";

  // 发送消息后，滚动到消息列表的最底部
  scrollToBottom();
};

//滚动到消息列表的最底部
const scrollToBottom = () => {
  nextTick(() => {
    const messageList = document.querySelector('.message-list');
    if (messageList) {
      // 获取最后一个消息元素
      const lastMessage = messageList.lastElementChild;
      if (lastMessage) {
        // 平滑滚动到最后一个消息元素
        lastMessage.scrollIntoView({ behavior: 'smooth' });
      }
    }
  });
};



// 处理接收的实时消息
const initializeChatSocket = () => {
  socket = initializeWebSocket((event) => {
    const messageContent = event.data; // 直接获取消息内容
    console.log("收到消息:", messageContent); // 输出消息内容以便调试

    // 生成 18 位的唯一 messageId：13位时间戳 + 5位随机数
    const uniqueMessageId = `${Date.now()}${Math.floor(10000 + Math.random() * 90000)}`; // 保证是18位

    // 检查当前用户和聊天用户是否存在
    if (currentChatUser.value && userId) {
      messages.value.push({
        sender: currentChatUser.value.userId,
        receiver: userId,
        content: messageContent,
        messageId: uniqueMessageId,  // 使用唯一生成的 messageId
      });
      console.log("已加入消息");
    }
  });
};

onMounted(() => {
  loadFollowedUsers();
  initializeChatSocket();
});

const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    if (event.altKey||event.shiftKey) {
      // Alt + Enter: 换行
      newMessage.value += '\n'; // 在输入框中添加换行符
      event.preventDefault(); // 阻止默认行为
    } else {
      // Enter: 发送消息
      sendMessage();
      event.preventDefault(); // 阻止默认行为
    }
  }
};
</script>

<style scoped>


.chat-page.with-bg {
  background-image:url("@/assets/avator/back.jpg") ; /* 替换为你的背景图片路径 */
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}


.user-list-title {
  font-size: 18px;
  font-weight: bold;
  padding: 10px 0;
  text-align: center;
}
.chat-room {
  display: flex;
  height: 100vh;
}
.user-list {
  width: 20%;
  border-right: 1px solid #ddd;
  height: 100vh;
  padding: 10px;
}
.no-friends {
  text-align: center;
  color: #888;
  margin-top: 20px;
  font-size: 14px;
  font-style: italic; /* 添加一点俏皮的字体样式 */
}
.user-item {
  cursor: pointer;
  padding: 10px 0;
  display: flex;
  align-items: center;
}

.user-item el-avatar {
  margin-right: 12px; /* 控制头像和用户名之间的间距 */
}

.user-name {
  margin-left: 15px;
  font-size: 16px;
}
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.chat-header {
  padding: 10px;
  border-bottom: 1px solid #ddd;
  display: flex;
  align-items: center;
}
.message-list {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
}
.message {
  display: flex;
  margin-bottom: 10px;
}
.sent {
  flex-direction: row-reverse;
  white-space: pre-wrap;
}
.received {
  flex-direction: row;
}
.message-avatar {
  width: 40px;
  height: 40px;
  margin: 0 10px;
}
.message-content {
  max-width: 60%;
  background-color: #f1f1f1;
  padding: 10px;
  border-radius: 5px;
}
.message-image {
  max-width: 100%;
  border-radius: 5px;
}
.message-input {
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px;
  display: flex;
  align-items: center;
  background-color: white; /* 添加背景色以覆盖其他内容 */
  border-top: 1px solid #ddd; /* 添加顶部边框以区分输入框和消息列表 */
  z-index: 10; /* 确保输入框在其他内容之上 */
}


</style>
