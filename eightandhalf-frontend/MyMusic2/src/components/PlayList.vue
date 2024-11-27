<template>
    <div class="playlist-container">
        <div class="header">
            <div class="album-cover"><img :src=coverImg alt=""></div>
            <div class="info">
                <h2>{{ playlist.name }}</h2>
                <div class="metadata">{{ playlist.creator }} • {{ playlist.date }}</div>
                <div class="controls">
                    <button @click="playAll">播放</button>
                    <button>收藏</button>
                    <button>分享</button>
                    <button>下载</button>
                </div>
            </div>
        </div>
        <div class="song-list">
            <div class="song" v-for="(song, index) in playlist.songs" :key="index">
                <span class="play-button">▶</span>
                <div class="details">
                    <span class="title">{{ song.title }}</span>
                    <span class="artist">{{ song.artist }}</span>
                    <span class="album">{{ song.album }}</span>
                    <span class="duration">{{ song.duration }}</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref } from 'vue';

export default {
    name: 'PlayList',
    coverImg: '',
    setup() {
        const playlist = ref({
            name: '我喜欢的音乐',
            creator: '九十度灰不落秋',
            date: '2019-07-04',
            songs: [
                { title: 'How Long', artist: 'Charlie Puth', album: 'Voicenotes', duration: '03:20' },
                { title: 'Talking to the Moon', artist: 'Bruno Mars', album: 'It’s Better If You Don’t Understand', duration: '03:27' },
                { title: 'Turn Around (Album Version)', artist: 'Tonya Mitchell', album: 'I Represent', duration: '04:41' },
                { title: 'I’m Not Perfect', artist: 'Bruno Mars', album: 'It’s Better If You Don’t Understand', duration: '03:58' },
                { title: 'The Way You Look Tonight', artist: 'Bruno Mars', album: 'It’s Better If You Don’t Understand', duration: '04:10' },
                // 添加更多歌曲
            ],
        });

        const playAll = () => {
            alert('播放全部歌曲');
        };

        return {
            playlist,
            playAll,
        };
    },
};
</script>

<style scoped>
body {
    font-family: Arial, sans-serif;
    display: flex;
    justify-content: center;
    background-color: #f5f5f5;
}

.playlist-container {
    width: 800px;
    background-color: #ffffff;
    border-radius: 8px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    padding: 20px;
}

.header {
    display: flex;
    align-items: center;
}

.album-cover {
    width: 120px;
    height: 120px;
    border-radius: 8px;
    background-color: #ddd;
    margin-right: 20px;
}

.info {
    flex: 1;
}

.info h2 {
    margin: 0;
    font-size: 24px;
}

.metadata {
    color: #666;
}

.controls {
    display: flex;
    gap: 10px;
    margin-top: 10px;
}

.controls button {
    padding: 6px 12px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    background-color: #0073e6;
    color: white;
}

.controls button:hover {
    background-color: #005bb5;
}

.song-list {
    margin-top: 20px;
}

.song {
    display: flex;
    padding: 10px 0;
    border-bottom: 1px solid #eee;
    align-items: center;
}

.play-button {
    margin-right: 15px;
    cursor: pointer;
    color: #666;
}

.details {
    flex: 1;
    display: flex;
    justify-content: space-between;
}

.title {
    font-weight: bold;
}

.artist,
.album,
.duration {
    color: #666;
    font-size: 14px;
}
</style>
