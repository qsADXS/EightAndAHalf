<template>
  <div class="music-container">
    <!-- 左侧侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-title">我的歌单</div>

      <div class="dropdown-item" @click="openAvatarDialog">
        <el-icon><Camera /></el-icon>
        <span>&nbsp;创建歌单</span>
        <AddmusicList ref="avatarDialog" />
      </div>

      
      <ul class="playlist">
        <li
          v-for="(playlist, index) in playlists"
          :key="playlist.playlistId"
          :class="{ active: activePlaylistIndex === index }"
          @click="selectPlaylist(index)"
        >
          <img :src="playlist.playListCoverUrl" alt="cover" class="playlist-cover" />
          <span class="playlist-name">{{ playlist.playlistName }}</span>
        </li>
      </ul>
    </aside>

    <!-- 右侧歌单详情 -->
    <main class="playlist-details" v-if="activePlaylist">
      <div class="playlist-header">
        <img :src="activePlaylist.playListCoverUrl" alt="cover" class="playlist-cover-large" />
        <div class="playlist-info">
          <h2 class="playlist-title">{{ activePlaylist.playlistName }}</h2>
          <p class="playlist-description">
            {{ activePlaylist.description || "暂无描述" }}
          </p>
        </div>
      </div>

      <!-- 歌单内歌曲列表 -->
      <div class="chart-list">
        <h1>歌曲列表</h1>
        <div
          v-for="(song, index) in musicList"
          :key="song.musicId"
          class="chart-item"
        >
          <img :src="song.coverUrl" alt="cover" class="cover" />
          <div class="song-info">
            <p class="song-name">{{ song.musicName }}</p>
            <p class="artist">{{ song.author }}</p>
          </div>
          <div class="actions">
            <button @click="playSong(song.musicId)" class="play-btn">▶️</button>
            <button
              class="more-btn"
              @click="deletemusic(song.musicId)"
            ></button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from "vue";
import { getContentofList } from "@/api/music/getlist";
import { getUserInfo } from "@/api/music/getuserinfo";
import { getUserMusicList } from "@/api/music/getusermusiclist";
import AddmusicList from "@/components/AddmusicList.vue";
import { Camera } from "@element-plus/icons-vue";
import { useRouter } from 'vue-router';


export default {
  setup() {
    // Reactive data
    const playlists = ref([]); // 存储所有歌单，包括收藏和创建
    const musicList = ref([]); // 当前歌单的歌曲列表
    const activePlaylistIndex = ref(0); // 当前选中的歌单索引
    const avatarDialog = ref(null); // 头像弹窗引用

    // Computed property for active playlist
    const activePlaylist = computed(() => playlists.value[activePlaylistIndex.value] || null);

    // Methods
    const openAvatarDialog = () => {
      avatarDialog.value.openUpdateDialog();
    };

    const selectPlaylist = async (index) => {
      activePlaylistIndex.value = index;
      await fetchPlaylistDetails(playlists.value[index].playlistId);
    };

    const deletemusic = (id)=>{
      console.log(playlists.value[activePlaylistIndex.value]);

    };
    const fetchPlaylists = async () => {
      try {
        const userInfo = await getUserInfo();
        const userId = userInfo.data.userId;

        const favoriteResponse = await getUserMusicList(userId, 0);
        const favoritePlaylists = favoriteResponse.data.userPlaylistList;

        const createdResponse = await getUserMusicList(userId, 1);
        const createdPlaylists = createdResponse.data.userPlaylistList;

        playlists.value = [...favoritePlaylists, ...createdPlaylists];

        // 默认加载第一个歌单的详细信息
        if (playlists.value.length > 0) {
          await fetchPlaylistDetails(playlists.value[0].playlistId);
        }
      } catch (error) {
        console.error("获取歌单失败:", error);
      }
    };

    const fetchPlaylistDetails = async (playlistId) => {
      try {
        const response = await getContentofList(playlistId);
        const data = response.data;
        musicList.value = data.musicList || [];
      } catch (error) {
        console.error("获取歌单详细信息失败:", error);
      }
    };

    const router = useRouter();

    const playSong = (id) => {
      router.push({
        path: '/audioPlay',
        query: { id },
      });
    };


    const togglePopover = (event, song) => {
      console.log("操作菜单弹出:", song);
      // 可实现更多操作，如收藏或分享
    };

    // Lifecycle hook
    onMounted(() => {
      fetchPlaylists();
    });

    return {
      playlists,
      musicList,
      activePlaylistIndex,
      activePlaylist,
      avatarDialog,
      openAvatarDialog,
      selectPlaylist,
      playSong,
      togglePopover,
    };
  },
  components: {
    AddmusicList,
    Camera,
  },
};
</script>

<style scoped>
.music-container {
  display: flex;
  width: 100%;
  height: 100vh;
}

/* 左侧侧边栏 */
.sidebar {
  width: 300px;
  background: #f5f5f5;
  padding: 16px;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
}

.sidebar-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 16px;
}

.playlist {
  list-style: none;
  padding: 0;
  margin: 0;
}

.playlist li {
  display: flex;
  align-items: center;
  padding: 10px;
  margin-bottom: 8px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.playlist li.active,
.playlist li:hover {
  background-color: #e0f7fa;
}

.playlist-cover {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  margin-right: 12px;
}

.playlist-name {
  font-size: 14px;
  color: #333;
}

/* 右侧歌单详情 */
.playlist-details {
  flex: 1;
  padding: 16px;
  background: #fff;
  overflow-y: auto;
}

.playlist-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.playlist-cover-large {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  margin-right: 20px;
}

.playlist-info {
  max-width: calc(100% - 140px);
}

.playlist-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.playlist-description {
  color: #666;
  font-size: 14px;
  margin-top: 8px;
}

/* 歌单内歌曲列表 */
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

.song-info {
  flex-grow: 1;
}

.song-name {
  font-size: 16px;
  margin: 0;
}

.actions button {
  margin-left: 8px;
  cursor: pointer;
}

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
.dropdown-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: background-color 0.2s;
}
</style>
