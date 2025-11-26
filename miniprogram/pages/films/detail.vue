<template>
  <view class="film-detail-page">
    <NavBar :show-back="true" title="作品详情"></NavBar>
    
    <view v-if="film" class="content">
      <!-- Poster & Header -->
      <view class="header-section">
        <image :src="film.poster" class="poster-bg" mode="aspectFill"></image>
        <view class="header-content">
          <image :src="film.poster" class="poster-img" mode="aspectFill"></image>
          <view class="header-info">
            <text class="film-title">{{ film.name }}</text>
            <text class="film-type">{{ film.type }} / {{ film.genre }}</text>
            <text class="film-date">{{ film.releaseDate }} 上映</text>
          </view>
        </view>
      </view>

      <!-- Info Section -->
      <view class="info-section">
        <view class="section-title">作品简介</view>
        <text class="description">{{ film.description }}</text>
        
        <view class="meta-row">
          <text class="label">导演：</text>
          <text class="value">{{ film.director }}</text>
        </view>
        <view class="meta-row">
          <text class="label">主演：</text>
          <text class="value">{{ film.actors.join(' / ') }}</text>
        </view>
      </view>

      <!-- Scenes Section -->
      <view class="scenes-section">
        <view class="section-title">雅安取景地</view>
        <view class="scene-list">
          <view v-for="(scene, index) in film.scenes" :key="index" class="scene-item">
            <uni-icons type="location-filled" size="20" color="#ef4444"></uni-icons>
            <text class="scene-name">{{ scene }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <view v-else class="loading-wrapper">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'

export default {
  components: {
    NavBar
  },
  data() {
    return {
      film: null,
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
  onLoad(options) {
    if (options.id) {
      this.loadFilmDetail(options.id)
    }
  },
  methods: {
    loadFilmDetail(id) {
      // Mock API call
      const film = this.allFilms.find(f => f.id == id);
      if (film) {
        this.film = film;
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.film-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx; /* Adjust based on NavBar height */
  box-sizing: border-box;
}

.header-section {
  position: relative;
  height: 400rpx;
  overflow: hidden;
}

.poster-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  filter: blur(20px);
  opacity: 0.6;
  transform: scale(1.2);
}

.header-content {
  position: relative;
  z-index: 1;
  display: flex;
  padding: 40rpx 32rpx;
  gap: 32rpx;
  height: 100%;
  box-sizing: border-box;
  background: rgba(0, 0, 0, 0.3);
}

.poster-img {
  width: 200rpx;
  height: 280rpx;
  border-radius: 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.3);
  flex-shrink: 0;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  color: #fff;
  padding-bottom: 10rpx;
}

.film-title {
  font-size: 40rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
}

.film-type, .film-date {
  font-size: 26rpx;
  opacity: 0.9;
  margin-bottom: 8rpx;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.5);
}

.info-section, .scenes-section {
  background: #fff;
  margin: 32rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24rpx;
  padding-left: 16rpx;
  border-left: 8rpx solid #ef4444;
  line-height: 1;
}

.description {
  font-size: 28rpx;
  color: #4b5563;
  line-height: 1.8;
  margin-bottom: 24rpx;
  display: block;
}

.meta-row {
  display: flex;
  margin-bottom: 12rpx;
  font-size: 28rpx;
}

.label {
  color: #6b7280;
  width: 100rpx;
  flex-shrink: 0;
}

.value {
  color: #1f2937;
  flex: 1;
}

.scene-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.scene-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx;
  background: #f9fafb;
  border-radius: 12rpx;
}

.scene-name {
  font-size: 28rpx;
  color: #374151;
  font-weight: 500;
}

.loading-wrapper {
  padding: 100rpx;
  text-align: center;
  color: #9ca3af;
}
</style>
