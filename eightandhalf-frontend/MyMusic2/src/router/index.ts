import { createRouter, createWebHistory } from "vue-router";
import LookForMusic from "@/views/LookForMusic/LookForMusic.vue";
import MyMusic from "@/views/MyMusic/MyMusic.vue";
import MusicCommunity from "@/views/Community/MusicCommunity.vue";
import PlayView from "@/views/MusicPlayView/PlayView.vue";
import TestMyComponents from "@/views/test/TestMyComponents.vue";
import MyDynamicPage from "@/views/Community/MyDynamicPage.vue";
import OtherUserPostPage from "@/views/Community/OtherUserPostPage.vue";
import Chat from "@/views/chat/Chat.vue";
import SearchView from "@/views/Search/SearchView.vue";
import RankList from "@/views/RankList/RankList.vue";
import SongList from "@/views/LookForMusic/SongList.vue";
import AudioPlay from "@/views/MusicPlayView/AudioPlay.vue";
import AiRecommend from "@/views/AIrecommend/AiRecommend.vue";
import MusicCollect from "@/views/Collect/MusicCollect.vue";
import CreateMusic from "@/views/Create/CreateMusic.vue";
import HistoryView from "@/views/MyMusic/HistoryView.vue";
import CreateMusiclist from "@/views/Create/CreateMusiclist.vue";



const routes = [
    {
        path: '/',
        redirect: '/lookForMusic'
    },

    {
        path: '/lookForMusic',
        component: LookForMusic
    },
    {
        path: '/myMusic',
        component: MyMusic
    },
    {
        path: '/musicCommunity',
        component: MusicCommunity
    },
    {
        path: '/playView',
        component: PlayView
    },
    {
        path: '/testMyComponents',
        component: TestMyComponents
    },
    {
        path: '/MyDynamicPage',
        component: MyDynamicPage
    },
    {
        path: '/user-posts/:userId',
        name: "OtherUserPosts",
        component: OtherUserPostPage,
        props: true,
    },
    {
        path: '/chat',
        component: Chat,
    },
    {
        path: '/search',
        name: "searchView",
        component: SearchView,
    },
    {
        path: '/rankList',
        name: "rankList",
        component: RankList,
    },
    {
        path: '/songList',
        name: "songList",
        component: SongList,
    },
    {
        path: '/audioPlay',
        name: "audioPlay",
        component: AudioPlay,
    },
    {
        path: '/airecommend',
        name: "aiRecommend",
        component: AiRecommend,
    },
    {
        path: '/collect',
        name: "collect",
        component: MusicCollect,
    },
    {
        path: '/create',
        name: "create",
        component: CreateMusic,
    },
    {
        path: '/history',
        name: "historyView",
        component: HistoryView,

    },
    {
        path: '/createList',
        name: "createList",
        component: CreateMusiclist,
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})


export default router;