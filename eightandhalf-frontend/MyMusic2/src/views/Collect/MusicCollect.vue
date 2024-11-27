<template>
  <div class="music-chart">
    <!-- 顶部导航 -->
    <header class="header">
      <h1>热门100首</h1>
      <h1>我的收藏</h1>

    </header>
          <h1>我的收藏</h1>

    <!-- 排行榜列表 -->
    <div class="chart-list">
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
        <li @click="deletemusic(popover.song.musicId)">取消收藏</li>
      </ul>
    </div>


  </div>
</template>

<script>

import { getCollectMusic } from '@/api/music/getcollectmusic';
import { colletSong } from '@/api/music/collect';
import { deleteFtomList } from '@/api/music/deletefromlist';
import { deleteCollectSong } from '@/api/music/deletecollect';
export default{
    data(){
        return{
            playlistid:'',
            rankList:[],
            popover: {
              show: false,
              x: 0,
              y: 0,
              song: null,
            },
        }
    },
    methods:{
        async getRankList(){
            const res = await getCollectMusic();
            console.log(res);
            this.rankList = res.data.musicList;
            console.log(this.rankList);
            this.playlistid = res.data.playlistInfo.playlistId;
            console.log(this.playlistid);
        },
        playSong(id){
            this.$router.push({
                path:'/audioPlay',
                query:{
                    id
                }
            })
        },
      togglePopover(event, song) {
        // 获取按钮的位置
        const rect = event.target.getBoundingClientRect();
        const popoverWidth = 120; // 气泡框宽度（与CSS中一致）
        const popoverHeight = 60; // 气泡框大约高度（内容变化时可调整）
        const windowWidth = window.innerWidth;
        const windowHeight = window.innerHeight;

        let x = rect.left + window.scrollX;
        let y = rect.top + rect.height + window.scrollY;

        // 检查气泡框是否超出右边界
        if (x + popoverWidth > windowWidth) {
          x = windowWidth - popoverWidth - 10; // 靠右对齐并留出边距
        }

        // 检查气泡框是否超出底部边界
        if (y + popoverHeight > windowHeight) {
          y = rect.top + window.scrollY - popoverHeight; // 向上显示
        }

        this.popover = {
          show: !this.popover.show,
          x,
          y,
          song,
        };
      },
        deletemusic(id) {
          deleteFtomList(id,this.playlistid)
           .then((res) => {
              console.log(res);
              this.getRankList();
            })
           .catch((err) => {
              console.error(err);
            });
            this.popover.show = false;
        },
        addToPlaylist(song) {
          alert(`将歌曲 "${song.musicName}" 添加到歌单`);
          this.popover.show = false;
        },
        playAll() {
          alert('播放全部');
        },

    },
    created(){
        this.getRankList();
    }

}

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

</style>

