<template>
    <el-carousel :interval="4000" type="card" height="300px">
      <el-carousel-item v-for="item in 6" :key="item">
        <img :src="getImagePath(item)" alt="carousel-image" class="carousel-image" />
      </el-carousel-item>
    </el-carousel>
  </template>
  
  <script setup>
  const images = import.meta.glob('../assets/Carousel/image*.jpg', { eager: true });
  
  function getImagePath(item) {
    const imagePath = `../assets/Carousel/image${item}.jpg`;
    return images[imagePath] ? images[imagePath].default : '';
  }
  </script>
  
  <style scoped>
  .carousel-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .el-carousel__item h3 {
    color: #475669;
    font-size: 14px;
    opacity: 0.75;
    line-height: 200px;
    margin: 0;
  }
  
  .el-carousel__item:nth-child(2n) {
    background-color: #99a9bf;
  }
  
  .el-carousel__item:nth-child(2n+1) {
    background-color: #d3dce6;
  }
  </style>
  