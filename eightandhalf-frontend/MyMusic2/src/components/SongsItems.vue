<template>
    <div class="playlist-container">
        <div class="header">
            <div :style="albumCoverStyle"></div>
            <div class="info">
                <h2>{{ listname }}</h2>
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
            <div class="song" v-for="(song, index) in songs" :key="index">
                <span class="play-button" @click="play()">▶</span>
                <div class="details">
                    <span class="title">{{ song.name }}</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'PlayList',
    props: {
        songs: {
            type: Array,
            required: true,
        },
    },
    computed: {
        albumCoverStyle() {
            return {
                width: '120px',
                height: '120px',
                borderRadius: '8px',
                backgroundColor: '#ddd',
                marginRight: '20px',
                backgroundImage: this.imageUrl ? `url(${this.imageUrl})` : 'none',
            };
        },
    },
    data() {
        return {
            listname: '',
            playlist: {
                name: '我喜欢的音乐',
                creator: '爱斯基摩人',
                date: '2018-06-04',
                songs: [

                ],
                coverImgUrl: '',
                imageUrl: "",

            }
        };
    },
    methods: {
        playAll() {
            alert('播放全部歌曲');
        },
        play() {

        }
    },

    created() {
        this.listname = this.$route.query.listname;
        this.playlist.coverImgUrl = this.$route.query.coverImgUrl;
        this.imageUrl = this.$route.query.coverImgUrl;

    }
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
    background-image: none;
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
