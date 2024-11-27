<template>
  <div class="music-page">
    <!-- 背景图 -->
    <div class="background">
      <div class="overlay"></div>
    </div>

    <!-- 推荐音乐网格 -->
    <div class="music-grid">
      <div
        v-for="(song, index) in songs"
        :key="index"
        class="music-card"
        @click="playSong(song.musicId)"
      >
        <img :src="song.coverUrl" alt="封面" class="cover" />
        <div class="info">
          <h3 class="title">{{ song.musicName }}</h3>
          <p class="artist">{{ song.author }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getAIrecommend } from '@/api/music/getAIrecommend';
export default {
  data() {
    return {
      songs: [
      ],
    };
  },
  methods: {
    playSong(id) {
      // 跳转到播放页面
      this.$router.push({ path: "/audioPlay", query: { id } });
    },
  },
  created(){
    getAIrecommend().then(res=>{
        this.songs = res.data.musicList;
    })
  }
};
</script>

<style scoped>
/* 背景样式 */
/* 背景样式 */
.music-page {
  position: relative;
  height: 100vh;
  overflow: hidden;
  color: white;
  font-family: 'Arial', sans-serif;
}

.background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('@/assets/music/music.jpg') no-repeat center center/cover; /* 使用 cover 确保图片自动裁剪并填充 */
  z-index: -1;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: -1;
}


/* 音乐卡片网格布局 */
.music-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  padding: 40px;
  margin-top: 50px;
}

/* 音乐卡片样式 */
.music-card {
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
  text-align: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}

.music-card:hover {
  transform: translateY(-10px) scale(1.05);
  box-shadow: 0 8px 12px rgba(0, 0, 0, 0.5);
}

/* 封面图 */
.cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

/* 信息样式 */
.info {
  padding: 16px;
}

.title {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
}

.artist {
  font-size: 14px;
  color: #ccc;
  margin: 8px 0 0;
}
</style>
