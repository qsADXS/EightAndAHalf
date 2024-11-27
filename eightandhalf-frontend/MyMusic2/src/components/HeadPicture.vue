<template>
    <div class="head-picture">
        <img :src="headpicture" alt="头像" />
    </div>
</template>

<script>
export default {
    data() {
        return {
            headpicture: '../assets/Login/eight_and_half_logo1.jpg',
        };

    },
    // props: {
    //     headPicture: {
    //         type: String,
    //         default: '',
    //     },
    // },
};
</script>

<style scoped>
.head-picture {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    overflow: hidden;
}
</style>