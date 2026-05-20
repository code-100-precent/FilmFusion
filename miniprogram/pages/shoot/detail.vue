<template>
  <view class="shoot-detail-page">
    <NavBar :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y v-if="!loading && shoot">
      <!-- 服务封面图片/轮播图 -->
      <view class="cover-section">
        <swiper 
          v-if="bannerImages.length > 1" 
          class="banner-swiper" 
          circular 
          indicator-dots 
          autoplay 
          :interval="4000" 
          :duration="500"
          indicator-active-color="#6366f1"
          indicator-color="rgba(255, 255, 255, 0.5)"
        >
          <swiper-item v-for="(img, index) in bannerImages" :key="index">
            <image 
              :src="img" 
              class="cover-image" 
              mode="aspectFill" 
              @click="previewImage(index)"
            ></image>
          </swiper-item>
        </swiper>
        <image 
          v-else-if="bannerImages.length === 1"
          :src="bannerImages[0]" 
          class="cover-image" 
          mode="aspectFill" 
          @click="previewImage(0)"
        ></image>
        <view v-else class="cover-placeholder">
          <uni-icons type="image" size="60" color="#d1d5db"></uni-icons>
          <text class="placeholder-text">暂无图片</text>
        </view>
      </view>

      <!-- 服务基本信息 -->
      <view class="info-card">
        <view class="card-header">
          <text class="shoot-name">{{ shoot.name }}</text>
        </view>
        <view v-if="shoot.moduleId && moduleInfo" class="module-tag">
          <uni-icons type="flag" size="16" color="#D4AF37"></uni-icons>
          <text>{{ moduleInfo.name }}</text>
        </view>
      </view>

      <!-- 服务描述 -->
      <view class="info-card">
        <view class="card-title">服务描述</view>
        <text class="card-content">{{ shoot.description || '暂无描述' }}</text>
      </view>

      <!-- 联系信息 -->
      <view class="info-card">
        <view class="card-title">联系信息</view>
        <view class="info-item">
          <uni-icons type="person" size="18" color="#6b7280"></uni-icons>
          <text class="info-label">联系人：</text>
          <text class="info-value">{{ shoot.contactName }}</text>
        </view>
        <view class="info-item">
          <uni-icons type="phone" size="18" color="#6b7280"></uni-icons>
          <text class="info-label">联系电话：</text>
          <text class="info-value">{{ shoot.phone }}</text>
        </view>
        <view class="info-item">
          <uni-icons type="location" size="18" color="#6b7280"></uni-icons>
          <text class="info-label">地址：</text>
          <text class="info-value">{{ shoot.address || '暂无地址' }}</text>
        </view>
      </view>

    </scroll-view>

    <view v-if="loading" class="loading-wrapper">
      <Loading></Loading>
    </view>

    <view v-if="!loading && !shoot" class="empty-wrapper">
      <Empty text="服务不存在"></Empty>
    </view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
import { getShootById, getModuleById } from '../../services/backend-api'
import { getFileUrl } from '../../utils'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      shoot: null,
      loading: false,
      moduleInfo: null // 模块信息
    }
  },
  computed: {
    // 处理封面图片URL
    coverImageUrl() {
      if (!this.shoot) return ''
      return getFileUrl(this.shoot.image || this.shoot.thumbImage)
    },
    // 处理轮播图图片
    bannerImages() {
      if (!this.shoot) return []
      
      let images = []
      
      // 1. 如果有images数组，优先使用
      if (this.shoot.images && Array.isArray(this.shoot.images) && this.shoot.images.length > 0) {
        images = this.shoot.images.map(img => getFileUrl(img))
      } 
      // 2. 如果images是逗号分隔的字符串
      else if (this.shoot.images && typeof this.shoot.images === 'string') {
        images = this.shoot.images.split(',').map(img => getFileUrl(img))
      }
      
      // 3. 如果没有images列表，尝试使用封面图
      if (images.length === 0) {
        const cover = this.shoot.image || this.shoot.cover || this.shoot.thumbImage
        if (Array.isArray(cover)) {
          images = cover.map(img => getFileUrl(img))
        } else if (typeof cover === 'string' && cover) {
          images = cover.split(',').map(img => getFileUrl(img))
        }
      }
      
      // 4. 去重
      images = [...new Set(images)].filter(img => img)
      
      // 用户要求：详情页面展示的图片是除了第一个以外的图片
      // 如果有多张图片，移除第一张（通常是列表封面）；如果只有一张，保留显示
      if (images.length > 1) {
        return images.slice(1)
      }
      
      return images
    }
  },
  onLoad(options) {
    const id = parseInt(options.id)
    if (id) {
      this.loadShoot(id)
    }
  },
  methods: {
    async loadShoot(id) {
      this.loading = true
      try {
        const res = await getShootById(id)
        
        if (res.code === 200 && res.data) {
          this.shoot = res.data
          
          // 如果有模块ID，加载模块信息
          if (this.shoot.moduleId) {
            this.loadModuleInfo(this.shoot.moduleId)
          }
        } else {
          uni.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载服务失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    async loadModuleInfo(moduleId) {
      try {
        const res = await getModuleById(moduleId)
        if (res.code === 200 && res.data) {
          this.moduleInfo = res.data
        }
      } catch (error) {
        console.error('加载模块信息失败:', error)
      }
    },
    previewImage(current = 0) {
      if (this.bannerImages.length === 0) {
        uni.showToast({
          title: '暂无图片可预览',
          icon: 'none'
        })
        return
      }
      
      // 使用处理过的URL进行预览
      let currentUrl = this.bannerImages[0]
      if (typeof current === 'number' && current >= 0 && current < this.bannerImages.length) {
        currentUrl = this.bannerImages[current]
      }
      
      uni.previewImage({
        urls: this.bannerImages,
        current: currentUrl
      })
    },
    getFileUrl(url) {
      return getFileUrl(url)
    }
  }
}
</script>

<style lang="scss" scoped>
.shoot-detail-page {
  min-height: 100vh;
  background: #121212;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  width: 100%;
  padding: 0 32rpx 32rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  /* 兼容火狐浏览器 */
  scrollbar-width: none;
  /* 兼容IE浏览器 */
  -ms-overflow-style: none;
}

.cover-section {
  width: 100%;
  margin: 32rpx 0;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
}

.banner-swiper {
  width: 100%;
  height: 400rpx;
}

.cover-image {
  width: 100%;
  height: 400rpx;
  display: block;
  transition: transform 0.3s ease;
}

.cover-image:active {
  transform: scale(0.98);
}

.cover-placeholder {
  width: 100%;
  height: 400rpx;
  background: #1E1E1E;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.placeholder-text {
  font-size: 28rpx;
  color: #999999;
}

.info-card {
  width: 100%;
  background: #1E1E1E;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
  
  &:first-child {
    margin-top: 0;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.shoot-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #FFFFFF;
  flex: 1;
}

.module-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 16rpx;
  padding: 8rpx 20rpx;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.15) 0%, rgba(244, 208, 63, 0.15) 100%);
  border: 1rpx solid rgba(212, 175, 55, 0.3);
  border-radius: 24rpx;
  
  text {
    font-size: 24rpx;
    color: #D4AF37;
    font-weight: 500;
  }
}

.shoot-status {
  display: inline-block;
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  background: #333333;
  color: #dc2626;
  
  &.status-online {
    background: #333333;
    color: #059669;
  }
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #D4AF37;
  margin-bottom: 24rpx;
}

.card-content {
  display: block;
  font-size: 28rpx;
  line-height: 1.8;
  color: #CCCCCC;
  white-space: pre-wrap;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-bottom: 20rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.info-label {
  font-size: 28rpx;
  color: #999999;
  min-width: 120rpx;
}

.info-value {
  font-size: 28rpx;
  color: #FFFFFF;
  flex: 1;
  word-break: break-all;
}

.price-wrapper {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.price-value {
  font-size: 48rpx;
  font-weight: 600;
  color: #D4AF37;
}

.price-unit {
  font-size: 28rpx;
  color: #999999;
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 32rpx;
}
</style>

