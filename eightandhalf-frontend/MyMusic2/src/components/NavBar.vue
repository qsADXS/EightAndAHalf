<template>
  <div>
    <div class="navbar" :class="{ 'fixed': isFixed }" ref="navbar">
      <div class="navbar-content">
        <!-- Logo -->
        <img src="../assets/Login/eight_and_half_logo1.jpg" alt="logo" class="logo" />

        <!-- 导航链接 -->
        <nav class="nav-links">
          <router-link to="/lookForMusic">发现音乐</router-link>
          <router-link to="/rankList">排行榜</router-link>
          <router-link to="/airecommend">AI推荐</router-link>
          <router-link to="/myMusic">我的音乐</router-link>
          <router-link to="/musicCommunity">社区</router-link>
          <router-link to="/create">创作</router-link>
        </nav>

        <div class="right-section">
          <!-- 搜索框 -->
          <div class="search-bar" style="display: flex; align-items: center; gap: 8px; position: relative;">
            <span class="search-icon">🔍</span>
            <input
              type="text"
              v-model="searchvalue"
              placeholder="音乐/视频/电台/用户"
              @input="handleInput"
              @focus="showSuggestions = true"
              @blur="hideSuggestions"
            />
            <el-button type="primary" icon="el-icon-search" size="small" @click="getSearchMusic">
              搜索
            </el-button>

            <!-- 补全框 -->
            <ul v-if="showSuggestions && suggestions.length" class="autocomplete-list">
              <li
                v-for="(item, index) in suggestions"
                :key="index"
                @mousedown.prevent="selectSuggestion(item)"
                class="autocomplete-item"
              >
                {{ item }}
              </li>
            </ul>
          </div>

          <!-- 最近查看记录 -->
          <div class="history-section" @click="gotohistory" @mouseenter="showHistory = true" @mouseleave="showHistory = false">
            <div class="message-icon">
              <img src="../assets/history/history.png" alt="history-icon" />
            </div>
            <div class="history-popup" v-if="showHistory">
              <div v-for="(song, index) in recentSongs" :key="index" class="history-item">
                <img :src="song.coverUrl" alt="cover" class="history-cover" />
                <div class="history-info">
                  <div class="history-title">{{ song.musicName }}</div>
                  <div class="history-author">歌手: {{ song.author }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 消息图标 -->
          <router-link to="/collect" class="message-icon">
            <img src="../assets/music/star.png" alt="message-icon" />
          </router-link>

          <!-- 消息图标 -->
          <router-link to="/chat" class="message-icon">
            <img src="../assets/message.png" alt="message-icon" />
          </router-link>

          <!-- 用户头像 -->
          <UserAvatar />
        </div>
      </div>
    </div>

    <!-- 页面内容 -->
    <router-view class="page-content" />
  </div>
</template>

<script>
import { searchComplete } from '@/api/music/searchcomplete';
import UserAvatar from './UserAvatar.vue';
import { getPlayHistory } from '@/api/music/getplayhistory';

export default {
  name: 'NavBar',
  components: {
    UserAvatar,
  },
  data() {
    return {
      searchvalue: '',
      isFixed: false,
      showHistory: false,
      showSuggestions: false,
      suggestions: [], // 动态补全列表
      recentSongs: [],
    };
  },
  methods: {
    async fetchRecentSongs() {
    try {
      const response = await getPlayHistory();
      if (response.base.code === 10000 && response.data.musicList) {
        // 只保留最新的十首歌
        this.recentSongs = response.data.musicList
          .slice(0, 10) // 获取前十首
          .map((item) => ({
            musicName: item.musicName,
            author: item.author,
            coverUrl: item.coverUrl,
          }));
      } else {
        console.error('获取播放记录失败', response.base.message);
      }
    } catch (error) {
      console.error('获取播放记录时发生错误', error);
    }
  },
    gotohistory() {
      this.$router.push({ name: 'historyView' });
    },
    getSearchMusic() {
      this.$router.push({ name: 'searchView', query: { searchvalue: this.searchvalue } });
    },
    async handleInput() {
      if (this.searchvalue.trim() === '') {
        this.suggestions = [];
        this.showSuggestions = false;
        return;
      }

      try {
        const response = await searchComplete(this.searchvalue);
        if (response.base.code === 10000) {
          this.suggestions = response.data || [];
          this.showSuggestions = true;
        } else {
          console.error('补全接口返回错误:', response.base.message);
        }
      } catch (error) {
        console.error('调用补全接口出错:', error);
      }
    },
    selectSuggestion(suggestion) {
      this.searchvalue = suggestion;
      this.showSuggestions = false;
      this.getSearchMusic();
    },
    hideSuggestions() {
      setTimeout(() => (this.showSuggestions = false), 200); // 延时隐藏，避免点击补全项时触发 blur
    },
  },
  mounted() {
    this.fetchRecentSongs();
  },
};
</script>


<style scoped>
/* 顶部导航栏样式 */
.navbar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  background-color: #229094;
  padding: 10px 20px;
  z-index: 10;
}

.navbar.fixed {
  position: fixed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.nav-links a {
  display: inline-block; /* 使链接块级显示 */
  padding: 10px 20px; /* 添加内边距 */
  color: #229094; /* 原本的文字颜色 */
  background-color: #229094; /* 原本的背景颜色 */
  text-decoration: none; /* 去掉下划线 */
  border-radius: 50px; /* 设置圆角半径 */
  transition: color 0.3s, background-color 0.3s; /* 添加颜色过渡效果 */
}

.nav-links a:hover {
  color: #FFFFFF; /* 鼠标悬浮时的文字颜色 */
  background-color: #31b3b9; /* 鼠标悬浮时的背景颜色 */
}

/* 页面整体布局调整 */
.page-content {
  padding-top: var(--navbar-height, 70px); /* 使用动态导航栏高度变量 */
}

/* 导航栏内容 */
.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  width: 40px;
  height: 40px;
  margin-right: 10px;
}

.nav-links {
  display: flex;
  gap: 20px;
  color: #fff;
  font-size: 16px;
}

.nav-links a {
  color: #fff;
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 4px;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 70px;
}

/* 最近播放记录 */
.message-icon img {
  width: 30px;
  height: 30px;
  cursor: pointer;
}

.history-section {
  position: relative;
}

.history-popup {
  position: absolute;
  top: 100%;
  left: 0;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  padding: 10px;
  width: 200px;
  z-index: 100;
}

.history-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.history-cover {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  margin-right: 10px;
}

.history-info {
  display: flex;
  flex-direction: column;
}

.history-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.history-author {
  font-size: 12px;
  color: #666;
}
/* 保持原有样式 */
/* 新增补全框样式 */
.autocomplete-list {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: 5px;
  list-style: none;
  padding: 0;
  z-index: 100;
}

.autocomplete-item {
  padding: 8px 12px;
  font-size: 14px;
  cursor: pointer;
  color: #333;
}

.autocomplete-item:hover {
  background-color: #f5f5f5;
}
</style>
