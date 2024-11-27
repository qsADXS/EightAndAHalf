<template>
  <div class="sidebar">
    <!-- 菜单 -->
    <ul class="menu">
      <li v-for="(item, index) in menuItems" :key="index" :class="{ active: activeIndex === index }">
        <!-- 普通菜单项 -->
        <div class="menu-item" @click="togglePlaylist(index)">
          <span class="icon">{{ item.icon }}</span>
          {{ item.title }}
          <span class="arrow" :class="{ open: activeIndex === index }">▼</span>
        </div>
        <!-- 下拉菜单 -->
        <ul v-show="activeIndex === index" class="playlist">
          <li
            v-for="(playlist, idx) in item.playlists"
            :key="idx"
            @click="selectPlaylist(playlist)"
          >
            {{ playlist }}
          </li>
        </ul>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'Sidebar',
  props: {
    menuItems: {
      type: Array,
      required: true,
    },
  },
  data() {
    return {
      activeIndex: null, // 当前打开的菜单项索引
    };
  },
  methods: {
    togglePlaylist(index) {
      this.activeIndex = this.activeIndex === index ? null : index;
    },
    selectPlaylist(playlist) {
      this.$emit('playlistSelected', playlist);
    },
  },
};
</script>

<style scoped>
.sidebar {
  width: 240px;
  background-color: #f8f9fa; /* 浅白背景 */
  padding: 20px 0;
  height: 100vh;
  border-right: 1px solid #e0e0e0;
}

.menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
  color: #333;
  transition: background 0.2s;
}

.menu-item:hover {
  background-color: #e9ecef; /* 鼠标悬停背景色 */
}

.icon {
  font-size: 16px;
  margin-right: 10px;
}

.arrow {
  font-size: 12px;
  transition: transform 0.2s;
}

.arrow.open {
  transform: rotate(180deg);
}

.playlist {
  list-style: none;
  padding-left: 30px;
  margin: 5px 0;
  transition: all 0.3s ease;
}

.playlist > li {
  margin: 5px 0;
  cursor: pointer;
  font-size: 14px;
  color: #555;
}

.playlist > li:hover {
  color: #007bff; /* 鼠标悬停字体颜色 */
}

.active > .menu-item {
  background-color: #e9ecef; /* 激活菜单背景色 */
}
</style>
