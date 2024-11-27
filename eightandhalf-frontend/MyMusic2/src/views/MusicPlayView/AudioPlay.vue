<template>
    <div class="container">
        <!-- 背景 -->
        <div class="background" :style="{ backgroundImage: `url(${backgroundImage})` }"></div>
        
        <!-- 播放器部分 -->
        <div class="audio-section">
            <div class="rotating-cover" :class="{ 'is-rotating': isPlaying }">
                <img :src="backgroundImage" alt="cover image" />
            </div>
            <AudioPlayer class="audio-player" :option="audioOptions" @play="onPlay" @pause="onPause" />
            <button @click="toggleSidebar" class="toggle-sidebar-button">查看评论</button>
        </div>

        <!-- 评论侧边栏 -->
        <div class="comment-sidebar" :class="{ open: isSidebarOpen }">
            <CommentSection :newcomments="newcomments" :id="id"/>
        </div>
    </div>
</template>

<script>
import AudioPlayer from 'vue3-audio-player';
import 'vue3-audio-player/dist/style.css';
import CommentSection from './CommentSection.vue';
import {computed} from 'vue';
import { getSongByIdAPI } from '@/api/music/getsongbyid';
import { addPlayVolume } from '@/api/music/addplayvolume';
import { addPlayHistory } from '@/api/music/addplayhistory';
import { getPlayHistory } from '@/api/music/getplayhistory';
import { addComment } from '@/api/music/addcomment';
import { getComment } from '@/api/music/getcomment';
import { id } from 'element-plus/es/locale/index.mjs';
import { getUsersInfo } from '@/api/user/getInfo';

export default {
    data() {
        return {
            id: '',
            isSidebarOpen: false,
            backgroundImage: '',
            audioOptions: {
                src: 'https://your-audio-file.mp3',
                title: 'My Awesome Song',
                coverImage: 'https://your-cover-image.png',
            },
            comments:[],
            userinfo:[],
            newcomments:[],
            isPlaying: false, // 新增变量，用于控制旋转状态

        };
    },
    methods: {
        onPlay() {
            this.isPlaying = true; // 当音频播放时，设置为true
        },
        onPause() {
            this.isPlaying = false; // 当音频暂停时，设置为false
        },
        toggleSidebar() {
            this.isSidebarOpen = !this.isSidebarOpen;
        },
    },
    components: {
        AudioPlayer,
        CommentSection,
    },
    created() {
        this.id = this.$route.query.id;
        addPlayVolume(this.id);
        addPlayHistory(this.id);
        getSongByIdAPI(this.id)
            .then((res) => {
                this.audioOptions.src = res.data.musicUrl;

              if (res.data.musicName.length > 10) {
                this.audioOptions.title = res.data.musicName.substring(0, 8) + '...';
              } else {
                this.audioOptions.title = res.data.musicName;
              }

                this.audioOptions.coverImage = "https://img0.baidu.com/it/u=3337065022,315419327&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500";
                this.backgroundImage = res.data.coverUrl;
            })
            .catch((err) => {
                console.log(err);
            });
getComment(this.id, 'music', '50', '0')
    .then((res) => {
        this.comments = res.data;
        console.log('comments:', res.data);

        let str = [];
        for (let i = 0; i < this.comments.length; i++) {
            str.push(this.comments[i].userId);
        }

        getUsersInfo(str)
            .then((res) => {
                console.log('userinfo:', res);
                this.userinfo = res;

                // 合并 comments 和 userinfo
                this.newcomments = this.comments.map(comment => {
                    // 在 userinfo 中查找与 comment.userId 对应的用户信息
                    const user = this.userinfo.find(user => user.userId === comment.userId);

                    // 返回一个包含评论和用户信息的新对象
                    return {
                        ...comment,
                        userName: user ? user.userName : '未知用户', // 添加用户名
                        avatarUrl: user ? user.avatarUrl : null,     // 添加用户头像
                    };
                });

                console.log('newcomments:', this.newcomments);
            })
            .catch((err) => {
                console.error('获取用户信息失败:', err);
            });

        console.log('userinfo:', res.data);
    })
    .catch((err) => {
        console.error('获取评论失败:', err);
    });

    },
};
</script>

<style scoped>
/* 背景设置 */
.background {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    filter: blur(20px);
    z-index: -1;
}

/* 播放器部分 */
.audio-section {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin-top: 80px; /* 避开旋转封面 */
}

/* 播放条更宽 */
.audio-player {
    width: 80%;
    margin-top: 16px;
}

/* 自定义播放按钮，背景设置为纯黑 */
.audio-player .audio-control-button {
    background-color: #000 !important; /* 设置纯黑背景 */
    background-image: none !important; /* 移除背景图 */
    color: #fff !important; /* 白色的图标 */
    border-radius: 50%; /* 圆形按钮 */
    width: 50px;
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 5px 10px rgba(0, 0, 0, 0.3); /* 添加按钮阴影 */
}

/* 按钮样式 */
.toggle-sidebar-button {
    margin-top: 20px;
    padding: 8px 16px;
    font-size: 16px;
    border: none;
    border-radius: 8px;
    background-color: #229094;
    color: #fff;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.toggle-sidebar-button:hover {
    background-color: #1c7a75;
}

/* 旋转封面 */
.rotating-cover {
    width: 400px;
    height: 400px;
    border-radius: 50%;
    animation: rotate 10s linear infinite;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    pointer-events: none;
}

.rotating-cover.is-rotating {
  animation-play-state: running; /* 开始旋转 */
}

.rotating-cover:not(.is-rotating) {
  animation-play-state: paused; /* 停止旋转 */
}

.rotating-cover img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
}

/* 评论侧边栏 */
.comment-sidebar {
    position: fixed;
    right: -300px;
    top: 0;
    height: 100vh;
    width: 300px;
    background: #fff;
    box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
    transition: right 0.3s ease;
    overflow-y: auto;
    z-index: 10;
}

.comment-sidebar.open {
    right: 0;
}

/* 动画效果 */
@keyframes rotate {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}
</style>
