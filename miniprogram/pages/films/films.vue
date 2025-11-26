<template>
  <view class="films-page">
    <!-- Gradient Background -->
    <view class="gradient-bg"></view>
    
    <NavBar :show-back="true"></NavBar>

    <view class="content">
      <!-- Page Title -->
      <view class="page-title">
        <text class="title-text">光影雅安</text>
      </view>

      <!-- Search Bar -->
      <view class="search-bar">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#9ca3af"></uni-icons>
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索影视作品..."
            @confirm="handleSearch"
            @input="handleSearch"
          />
        </view>
      </view>

      <!-- Category Filter -->
      <view class="category-filter">
        <scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
          <view 
            v-for="category in categories" 
            :key="category"
            class="category-item"
            :class="{ active: selectedCategory === category }"
            @click="selectCategory(category)">
            <text>{{ category }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- Film List -->
      <scroll-view
        class="film-list"
        scroll-y
        @scrolltolower="loadMore"
      >
        <view v-if="films.length === 0" class="empty-wrapper">
          <Empty text="暂无影视作品"></Empty>
        </view>
        <view v-else class="film-grid">
          <view
            v-for="film in films"
            :key="film.id"
            class="film-card"
            @click="goToDetail(film.id)"
          >
            <view class="film-cover-wrapper">
              <image :src="film.poster" class="film-cover" mode="aspectFill"></image>
              <view class="film-type-badge">{{ film.type }}</view>
            </view>
            <view class="film-info">
              <text class="film-title">{{ film.name }}</text>
              <text class="film-meta">{{ film.releaseDate }} 上映</text>
              <view class="film-tags">
                <text class="film-tag">{{ film.genre }}</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import Empty from '../../components/Empty/Empty.vue'

export default {
  components: {
    NavBar,
    Empty
  },
  data() {
    return {
      keyword: '',
      selectedCategory: '全部',
      categories: ['全部', '电影', '电视剧', '网络剧', '微短剧', '纪录片'],
      films: [],
      allFilms: [
        {
          id: 1,
          name: "云上雅安",
          type: "纪录片",
          genre: "人文/自然",
          releaseDate: "2023-05-12",
          poster: "https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/film_poster_1.jpg",
          director: "张三",
          actors: ["李四", "王五"],
          description: "一部展示雅安自然风光和人文历史的纪录片。",
          scenes: ["碧峰峡", "上里古镇"]
        },
        {
          id: 2,
          name: "茶马古道",
          type: "电视剧",
          genre: "历史/剧情",
          releaseDate: "2022-10-01",
          poster: "https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/film_poster_2.jpg",
          director: "李四",
          actors: ["赵六", "钱七"],
          description: "讲述茶马古道上的传奇故事。",
          scenes: ["蒙顶山", "望鱼古镇"]
        },
         {
          id: 3,
          name: "熊猫奇缘",
          type: "电影",
          genre: "喜剧/冒险",
          releaseDate: "2024-01-25",
          poster: "https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/film_poster_3.jpg",
          director: "王五",
          actors: ["孙八", "周九"],
          description: "一只大熊猫的冒险之旅。",
          scenes: ["熊猫基地"]
        }
      ]
    }
  },
  onLoad() {
    this.loadFilms()
  },
  methods: {
    loadFilms() {
      let result = this.allFilms;
      
      if (this.selectedCategory !== '全部') {
        result = result.filter(f => f.type === this.selectedCategory);
      }
      
      if (this.keyword) {
        result = result.filter(f => f.name.includes(this.keyword));
      }
      
      this.films = result;
    },
    handleSearch() {
      this.loadFilms()
    },
    selectCategory(category) {
      this.selectedCategory = category
      this.loadFilms()
    },
    loadMore() {
      // Mock load more
    },
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/films/detail?id=${id}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.films-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.gradient-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 33.33vh;
  background: linear-gradient(to top, #ffffff 0%, #ef4444 100%);
  z-index: 0;
  opacity: 0.1;
}

.content {
  padding: 16rpx 24rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  width: 100%;
  position: relative;
  z-index: 1;
  height: calc(100vh - 132rpx);
  display: flex;
  flex-direction: column;
}

.page-title {
  padding: 32rpx 0 8rpx 0;
  width: 100%;
}

.title-text {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.search-bar {
  margin-top: 16rpx;
  margin-bottom: 16rpx;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 0 20rpx;
  height: 72rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #1f2937;
}

.category-filter {
  margin-bottom: 16rpx;
}

.category-scroll {
  width: 100%;
  white-space: nowrap;
}

.category-item {
  display: inline-block;
  padding: 10rpx 24rpx;
  margin-right: 12rpx;
  background: #fff;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #6b7280;
  transition: all 0.3s;
  border: 1rpx solid transparent;
  
  &.active {
    background: #ef4444;
    color: #fff;
    box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.3);
  }
}

.film-list {
  flex: 1;
  min-height: 0;
}

.film-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  padding-bottom: 16rpx;
}

.film-card {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.film-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.film-cover-wrapper {
  width: 100%;
  aspect-ratio: 3/4;
  position: relative;
  background: #f3f4f6;
}

.film-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.film-type-badge {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  padding: 4rpx 12rpx;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 20rpx;
  border-radius: 8rpx;
}

.film-info {
  padding: 16rpx;
}

.film-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8rpx;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.film-meta {
  font-size: 20rpx;
  color: #9ca3af;
  margin-bottom: 12rpx;
  display: block;
}

.film-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.film-tag {
  font-size: 20rpx;
  color: #ef4444;
  background: #fef2f2;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}
</style>
