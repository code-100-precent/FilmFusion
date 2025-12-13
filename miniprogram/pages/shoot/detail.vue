<template>
  <view class="shoot-detail-page">
    <NavBar :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y v-if="!loading && shoot">
      <!-- 服务封面图片 -->
      <view class="cover-section">
        <image 
          v-if="getFileUrl(shoot.image || shoot.thumbImage)"
          :src="getFileUrl(shoot.image || shoot.thumbImage)" 
          class="cover-image" 
          mode="aspectFill" 
          @click="previewImage"
        ></image>
        <view v-else class="cover-placeholder">
          <uni-icons type="image" size="60" color="#d1d5db"></uni-icons>
          <text class="placeholder-text">暂无封面图片</text>
        </view>
      </view>

      <!-- 服务基本信息 -->
      <view class="info-card">
        <view class="card-header">
          <text class="shoot-name">{{ shoot.name }}</text>
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

      <!-- 价格信息 -->
      <view class="info-card">
        <view class="card-title">价格信息</view>
        <view class="price-wrapper">
          <text class="price-value">¥{{ shoot.price }}</text>
          <text class="price-unit">/次</text>
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
import { getShootById } from '../../services/backend-api'
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
      loading: false
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
    previewImage() {
      if (!this.shoot || (!this.shoot.image && !this.shoot.thumbImage)) {
        uni.showToast({
          title: '暂无图片可预览',
          icon: 'none'
        })
        return
      }
      
      const imageUrl = this.getFileUrl(this.shoot.image || this.shoot.thumbImage)
      uni.previewImage({
        urls: [imageUrl],
        current: imageUrl
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
  background: #f5f7fa;
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
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
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
  background: #f9fafb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.placeholder-text {
  font-size: 28rpx;
  color: #9ca3af;
}

.info-card {
  width: 100%;
  background: #fff;
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
  color: #1f2937;
  flex: 1;
}

.shoot-status {
  display: inline-block;
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  background: #fee2e2;
  color: #dc2626;
  
  &.status-online {
    background: #d1fae5;
    color: #059669;
  }
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24rpx;
}

.card-content {
  display: block;
  font-size: 28rpx;
  line-height: 1.8;
  color: #374151;
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
  color: #6b7280;
  min-width: 120rpx;
}

.info-value {
  font-size: 28rpx;
  color: #1f2937;
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
  color: #1f2937;
}

.price-unit {
  font-size: 28rpx;
  color: #6b7280;
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 32rpx;
}
</style>

