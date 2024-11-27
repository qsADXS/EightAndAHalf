<template>
  <div class="music-chart">
    <!-- 顶部导航 -->
    <header class="header">
      <h1>热门100首</h1>
    </header>

    <!-- 排行榜列表 -->
    <div class="chart-list">
      <h1>搜索结果</h1>
      <div
          v-for="(song, index) in rankList"
          :key="song.id"
          class="chart-item"
      >
        <div class="rank">{{ index + 1 }}</div>
        <img :src="song.coverUrl" alt="cover" class="cover" />
        <div class="song-info">
          <p class="song-name">{{ song.musicName }}</p>
          <p class="artist">{{ song.author }}</p>
        </div>
        <div class="actions">
          <button @click="playSong(song.musicId)" class="play-btn">▶️</button>
          <button
              class="more-btn"
              @click="togglePopover($event, song)"
          >···</button>
        </div>
      </div>
    </div>

    <!-- 气泡框 -->
    <div
        v-if="popover.show"
        class="popover"
        :style="{ top: popover.y + 'px', left: popover.x + 'px' }"
    >
      <ul>
        <li @click="addToFavorites(popover.song.musicId)">收藏歌曲</li>
        <li @click="openAddToPlaylistForm(popover.song.musicId)">添加到歌单</li>
      </ul>
    </div>

    <!-- 表单弹窗 -->
    <div v-if="showForm" class="form-modal">
      <div class="form-content">
        <h3>选择歌单</h3>
        <form @submit.prevent="submitToPlaylist">
          <div>
            <label for="playlist">选择歌单：</label>
            <select id="playlist" v-model="selectedPlaylist" required>
              <option
                  v-for="playlist in usercreatedList"
                  :key="playlist.id"
                  :value="playlist.id"
              >
                {{ playlist.name }}
              </option>
            </select>
          </div>
          <div class="form-actions">
            <button type="submit" class="submit-btn">确认添加</button>
            <button type="button" class="cancel-btn" @click="closeForm">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { getRankListAPI } from '@/api/music/ranklist';
import { colletSong } from '@/api/music/collect';
import { getUserMusicList } from '@/api/music/getusermusiclist';
import { addToMusicList } from '@/api/music/addtomusiclist';
import { getUserInfo } from '@/api/music/getuserinfo';
import { searchMusicAPI } from '@/api/music/search';

export default {
  data() {
    return {
      searchvalue: '',
      rankList: [],
      musicaddid: '',
      popover: {
        show: false,
        x: 0,
        y: 0,
        song: null,
      },
      showForm: false, // 控制表单显示
      selectedPlaylist: '', // 当前选中的歌单
      usercreatedList: [], // 用户创建的歌单列表
    };
  },
  watch: {
    '$route.query': {
      immediate: true,
      handler(newQuery) {
        this.searchvalue = newQuery.searchvalue || '';
        this.getSearchMusic();
      }
    }
  },
  methods: {
    async getSearchMusic() {
      console.log(this.searchvalue);
      const res = await searchMusicAPI(this.searchvalue);
      this.rankList = res.data.items;
    },
    async getRankList() {
      const res = await getRankListAPI(100);
      this.rankList = res.data.musicList;
    },
    async fetchUserPlaylists() {
      try {
        const res = await getUserInfo(); // 获取用户信息
        const userId = res.data.userId;
        const playlistRes = await getUserMusicList(userId, 0); // 获取用户歌单列表
        this.usercreatedList = playlistRes.data.userPlaylistList.map((playlist) => ({
          id: playlist.playlistId,
          name: playlist.playlistName,
        }));
      } catch (error) {
        console.error("获取用户歌单失败:", error);
      }
    },
    playSong(id) {
      this.$router.push({
        path: '/audioPlay',
        query: {
          id,
        },
      });
    },
    togglePopover(event, song) {
      const rect = event.target.getBoundingClientRect();
      const popoverWidth = 120;
      const popoverHeight = 60;
      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;

      let x = rect.left + window.scrollX;
      let y = rect.top + rect.height + window.scrollY;

      if (x + popoverWidth > windowWidth) {
        x = windowWidth - popoverWidth - 10;
      }
      if (y + popoverHeight > windowHeight) {
        y = rect.top + window.scrollY - popoverHeight;
      }

      this.popover = {
        show: !this.popover.show,
        x,
        y,
        song,
      };
    },
    addToFavorites(id) {
      colletSong(id).then((res) => {
        console.log("收藏歌曲:", id);
        console.log(res);
      });
      this.popover.show = false;
    },
    openAddToPlaylistForm(id) {
      this.musicaddid = id;
      this.popover.show = false;
      this.showForm = true; // 打开表单
    },
    submitToPlaylist() {
      console.log(this.musicaddid);
      console.log("添加到歌单:", this.selectedPlaylist);
      addToMusicList(this.musicaddid, this.selectedPlaylist).then((res) => {
        console.log("添加到歌单:", this.selectedPlaylist);
        console.log(res);
      });
      this.showForm = false; // 关闭表单
    },
    closeForm() {
      this.showForm = false;
    },
  },
  created() {
    this.searchvalue = this.$route.query.searchvalue;
    this.getSearchMusic();
    this.fetchUserPlaylists();
  },
};
</script>

<style scoped>
.music-chart {
  font-family: Arial, sans-serif;
  color: #333;
  background-color: #f9f9f9;
  padding: 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header h1 {
  font-size: 24px;
  margin: 0;
}

.search-box {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.chart-list {
  margin-top: 16px;
}

.chart-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid #eee;
}

.rank {
  font-size: 18px;
  font-weight: bold;
  width: 40px;
  text-align: center;
}

.cover {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  margin-right: 16px;
}

.song-info {
  flex-grow: 1;
}

.song-name {
  font-size: 16px;
  margin: 0;
}

.artist {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.actions {
  display: flex;
  align-items: center;
}

.play-btn,
.more-btn {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  margin-left: 8px;
}

.footer {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
}

.popover {
  position: absolute;
  z-index: 1000;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  padding: 4px 0; /* 缩小内边距 */
  width: 120px; /* 缩小宽度 */
}

.popover ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.popover li {
  padding: 6px 12px; /* 缩小每项的点击区域 */
  cursor: pointer;
  font-size: 12px; /* 缩小文字大小 */
  color: #333;
}

.popover li:hover {
  background-color: #f5f5f5;
}
.form-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.form-content {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  max-width: 400px;
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.submit-btn,
.cancel-btn {
  padding: 10px 15px;
  cursor: pointer;
}

</style>

