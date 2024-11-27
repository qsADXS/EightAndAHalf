<template>
    <div class="container">
        <div class="playlist">
            <Playlist />
        </div>
    </div>

</template>

<script>
import Playlist from '@/components/PlayList.vue';

export default {
    components: {
        Playlist,
    },
};
</script>

<style scoped>
.container {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    margin: 0 auto;
    top: 0px;
    padding: 20px;
    background-color: #f2f2f2;
}

.playlist {

    position: relative;
    top: 0px;

    right: 0;
    background-color: #f2f2f2;
    padding: 20px;
    margin: 0px;
}
</style>