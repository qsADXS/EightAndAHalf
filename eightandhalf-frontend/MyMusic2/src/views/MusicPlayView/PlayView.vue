<template>
    <div class="container">
        <div class="play-image">
            <img v-bind:src="thissong.coverUrl" class="rotate">
        </div>
        <div class="play-music">
            <img v-if="isplay" src="@/assets/Play/play.png" class="play">
        </div>
    </div>
</template>



<style scoped>
.play-music {
    width: 100%;
    height: 100%;

    display: flex;
    justify-content: center;
    align-items: center;
}




.container {
    width: 100%;
    height: 100%;

    background: linear-gradient(to bottom, #393D4C, #181920);
}

.play-image {
    width: 100%;
    height: 100%;
}


.rotate {
    border-radius: 50%;

    width: 500px;

    /* 图片宽度 */
    animation: rotation 2s infinite linear;
    /* 旋转动画 */
}

@keyframes rotation {
    from {
        transform: rotate(0deg);
        /* 从0度开始 */
    }

    to {
        transform: rotate(360deg);
        /* 旋转到360度 */
    }
}
</style>



<script>
export default {
    data() {
        return {
            isplay: true,
            thissong: {
                "id": 242160865313427460,
                "userId": 241739622986682370,
                "musicUrl": "https://linbei-website.oss-cn-beijing.aliyuncs.com/242160863732174848.mp4",
                "coverUrl": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTJMQ7_UOL6kkFNmz29gZLQoFfcTe73m45_Yw&s",
                "author": "邓紫棋",
                "musicCategory": "funk",
                "musicName": "MyVideo",
                "description": "justforfun@233",
                "visitCount": 0,
                "subscribeCount": 0,
                "commentCount": 2,
                "createdAt": "2024-02-21T22:26:54",
                "updatedAt": "2024-02-21T22:26:54",
                "deletedAt": "2024-02-21T22:26:54"
            }

        }
    },
    methods: {
        playmusic() {

        },

        getsong() {

        }

    }
}


</script>