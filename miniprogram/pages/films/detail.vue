<template>
  <view class="film-detail-page">
    <NavBar :show-back="true"></NavBar>
    
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
    
    <view v-else-if="loading" class="loading-wrapper">
      <text>加载中...</text>
    </view>
    <view v-else class="error-wrapper">
      <text>作品信息加载失败</text>
    </view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import { getDramaById } from '../../services/backend-api'

export default {
  components: {
    NavBar
  },
  data() {
    return {
      film: null,
      loading: true
    }
  },
  onLoad(options) {
    if (options.id) {
      // 检查用户是否已登录
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        // 延迟跳转，让用户看到提示
        setTimeout(() => {
          uni.navigateTo({
            url: '/pages/login/login'
          })
        }, 1500)
        this.loading = false
      } else {
        this.loadFilmDetail(options.id)
      }
    }
  },
  methods: {
    async loadFilmDetail(id) {
      this.loading = true;
      try {
        const response = await getDramaById(id);
        
        if (response.code === 200) {
          // 确保 actors 和 scenes 是数组格式
          if (response.data) {
            this.film = {
              ...response.data,
              actors: Array.isArray(response.data.actors) ? response.data.actors : [],
              scenes: Array.isArray(response.data.scenes) ? response.data.scenes : []
            };
          }
        } else {
          uni.showToast({
            title: response.message || '获取作品详情失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('加载作品详情失败:', error);
        // 不重复显示toast，因为http拦截器已经处理了401错误的跳转
        // 只有在非401错误时才显示自定义提示
        if (error.statusCode !== 401) {
          uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
          });
        }
      } finally {
        this.loading = false;
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

.loading-wrapper, .error-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #9ca3af;
}

.error-wrapper {
  color: #ef4444;
}
</style>
