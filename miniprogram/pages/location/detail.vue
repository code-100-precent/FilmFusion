<template>
  <view class="location-detail-page">
    <NavBar title="场地详情" :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y v-if="!loading && location">
      <!-- 场地基本信息 -->
      <view class="info-card">
        <view class="card-header">
          <text class="location-name">{{ location.name }}</text>
          <view class="location-badge">{{ location.type }}</view>
        </view>
        <view class="location-status" :class="{ 'status-available': location.status === 1 }">
          {{ location.status === 1 ? '可用' : '不可用' }}
        </view>
      </view>

      <!-- 场地描述 -->
      <view class="info-card">
        <view class="card-title">场地描述</view>
        <text class="card-content">{{ location.locationDescription || '暂无描述' }}</text>
      </view>

      <!-- 联系信息 -->
      <view class="info-card">
        <view class="card-title">联系信息</view>
        <view class="info-item">
          <uni-icons type="person" size="18" color="#6b7280"></uni-icons>
          <text class="info-label">联系人：</text>
          <text class="info-value">{{ location.contactName }}</text>
        </view>
        <view class="info-item">
          <uni-icons type="phone" size="18" color="#6b7280"></uni-icons>
          <text class="info-label">联系电话：</text>
          <text class="info-value">{{ location.contactPhone }}</text>
        </view>
        <view class="info-item">
          <uni-icons type="location" size="18" color="#6b7280"></uni-icons>
          <text class="info-label">地址：</text>
          <text class="info-value">{{ location.address }}</text>
          <view class="nav-btn" @click="openLocation">
            <uni-icons type="paperplane" size="14" color="#fff"></uni-icons>
            <text>导航</text>
          </view>
        </view>
      </view>

      <!-- 价格信息 -->
      <view class="info-card">
        <view class="card-title">价格信息</view>
        <view class="price-wrapper">
          <text class="price-value">¥{{ location.price }}</text>
          <text class="price-unit">/天</text>
        </view>
      </view>

      <!-- 最佳拍摄时段 -->
      <view class="info-card" v-if="location.bestShootingTime">
        <view class="card-title">最佳拍摄时段</view>
        <view class="info-item">
          <uni-icons type="calendar" size="18" color="#f59e0b"></uni-icons>
          <text class="info-value highlight">{{ location.bestShootingTime }}</text>
        </view>
      </view>

      <!-- 设施信息 -->
      <view class="info-card" v-if="location.facilities">
        <view class="card-title">配套设施</view>
        <view class="info-item">
          <uni-icons type="gear" size="18" color="#10b981"></uni-icons>
          <text class="info-value">{{ location.facilities }}</text>
        </view>
      </view>

      <!-- 图片展示 -->
      <view class="info-card" v-if="location.cover">
        <view class="card-title">场地图片</view>
        <image :src="location.cover" class="location-image" mode="aspectFill" @click="previewImage"></image>
      </view>
    </scroll-view>

    <view v-if="loading" class="loading-wrapper">
      <Loading></Loading>
    </view>

    <view v-if="!loading && !location" class="empty-wrapper">
      <Empty text="场地不存在"></Empty>
    </view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
import { getLocationById } from '../../services/api'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      location: null,
      loading: false
    }
  },
  onLoad(options) {
    const id = parseInt(options.id)
    if (id) {
      this.loadLocation(id)
    }
  },
  methods: {
    async loadLocation(id) {
      this.loading = true
      try {
        const res = await getLocationById(id)
        if (res.code === 200 && res.data) {
          this.location = res.data
          // Mock coordinates if missing
          if (!this.location.latitude) {
            this.location.latitude = 30.0 + Math.random() * 0.1
            this.location.longitude = 103.0 + Math.random() * 0.1
          }
        } else {
          uni.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载场地失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    openLocation() {
      if (!this.location || !this.location.latitude || !this.location.longitude) {
        uni.showToast({
          title: '暂无位置信息',
          icon: 'none'
        })
        return
      }
      
      uni.openLocation({
        latitude: parseFloat(this.location.latitude),
        longitude: parseFloat(this.location.longitude),
        name: this.location.name,
        address: this.location.address,
        success: function () {
          console.log('success');
        }
      });
    },
    previewImage() {
      if (!this.location || !this.location.cover) return
      
      uni.previewImage({
        urls: [this.location.cover],
        current: this.location.cover
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.location-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  width: 100%;
  padding: 32rpx;
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

.info-card {
  width: 100%;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.location-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
}

.location-badge {
  background: #eef2ff;
  color: #6366f1;
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  margin-left: 16rpx;
}

.location-status {
  display: inline-block;
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  background: #fee2e2;
  color: #dc2626;
  
  &.status-available {
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

.nav-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx 16rpx;
  background: #6366f1;
  border-radius: 32rpx;
  color: #fff;
  font-size: 22rpx;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.nav-btn:active {
  opacity: 0.9;
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

.location-image {
  width: 100%;
  height: 400rpx;
  border-radius: 12rpx;
  background: #f3f4f6;
}

.highlight {
  color: #f59e0b;
  font-weight: 600;
}
</style>

