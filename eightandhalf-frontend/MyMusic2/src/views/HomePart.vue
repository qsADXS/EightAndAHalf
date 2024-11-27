<template>
    <div>
        <h1>首页</h1>
        <CarouselComponent />
        
    </div>
</template>


<style scoped>
h1 {
    color: brown;
    font-size: 50px;
} 

</style>


<script>
 import CarouselComponent from '@/components/CarouselComponent.vue';

export default {
    name: 'HomePart',
     components: {
         CarouselComponent
     } 
};
</script>