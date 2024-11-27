<template>
    <div>
        <CarouselComponent />
        <el-container>
            <el-header>
                <!-- <h1>音乐列表</h1> -->
            </el-header>
            <h2>推荐歌单</h2>
            <el-main>
                <el-row>
                    <el-col :span="4" v-for="(song, index) in songs" :key="index">
                        <el-card class="song-card">
                            <div class="song-info" @click="gotosonglist(song.playlistId,song.playlistName,song.playListCoverUrl)">
                                <img :src="song.playListCoverUrl" alt="歌曲图片" width="150px" height="150px" />
                                <h3>{{ song.playlistName }}</h3>
                                <p>{{ song.artist }}</p>
                            </div>
                        </el-card>
                    </el-col>
                </el-row>
            </el-main>
        </el-container>
    </div>
</template>

<script>
import { getRecommendMusicAPI } from '@/api/music/gethotlist';
import CarouselComponent from '@/components/CarouselComponent.vue';



export default {
    components: {
        CarouselComponent
    },
    data() {
        return {
            songs: [
            ],

        };
    },
    methods: {
        getsonglist() {
            getRecommendMusicAPI().then(res => {
                console.log(res);
                console.log(222);
                this.songs = res.data.userPlaylistList;
            }).catch(err => {
                console.log(err);
            });
        },
        gotosonglist(id, name, img) {
            this.$router.push({
                path: '/songList',
                query: {
                    playlistId: id,
                    playlistName: name,
                    playListCoverUrl: img
                }
            });
        }

    },
    created() {
        this.getsonglist();
        console.log(111);
    }
};
</script>

<style scoped>
.song-card {
    margin: 15px 5px;
    /* 上下距离大一些，左右距离小一些 */
    width: 180px;
    height: 220px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.song-card:hover {
    transform: scale(1.05);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
}

.song-info img {
    width: 160px;
    height: 160px;
    border-radius: 8px;
}

.song-info h3 {
    font-size: 16px;
    margin-top: 10px;
    text-align: center;
}

.song-info {
    text-align: center;
}
</style>
