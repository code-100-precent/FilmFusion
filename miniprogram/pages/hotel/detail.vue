<template>
  <view class="hotel-detail-page">
    <NavBar :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y v-if="!loading && hotel">
      <!-- 封面图 -->
      <view class="cover-section">
        <image :src="hotel.cover || defaultCover" class="cover-image" mode="aspectFill" @click="previewImage"></image>
      </view>

      <!-- 基本信息 -->
      <view class="info-card">
        <view class="card-header">
          <text class="hotel-name">{{ hotel.name }}</text>
          <view class="hotel-badge">住宿</view>
        </view>
        <view class="hotel-status" :class="{ 'status-available': hotel.status === 1 }">
          {{ hotel.status === 1 ? '营业中' : '暂停营业' }}
        </view>
      </view>

      <!-- 住宿描述 -->
      <view class="info-card">
        <view class="card-title">住宿介绍</view>
        <text class="card-content">{{ hotel.description || '暂无介绍' }}</text>
      </view>

      <!-- 联系信息 -->
      <view class="info-card">
        <view class="card-title">联系信息</view>
        <view class="info-item">
          <uni-icons type="person" size="18" color="#6b7280"></uni-icons>
          <text class="info-label">联系人：</text>
          <text class="info-value">{{ hotel.contactName || '暂无' }}</text>
        </view>
        <view class="info-item">
          <uni-icons type="phone" size="18" color="#6b7280"></uni-icons>
          <text class="info-label">联系电话：</text>
          <text class="info-value">{{ hotel.phone || '暂无' }}</text>
        </view>
        <view class="info-item">
          <uni-icons type="location" size="18" color="#6b7280"></uni-icons>
          <text class="info-label">地址：</text>
          <text class="info-value">{{ hotel.address || '暂无地址' }}</text>
          <view class="nav-btn" @click="navigateToHotel">
            <uni-icons type="paperplane" size="14" color="#fff"></uni-icons>
            <text>导航</text>
          </view>
        </view>
      </view>

      <!-- 价格信息 -->
      <view class="info-card" v-if="hotel.price">
        <view class="card-title">价格信息</view>
        <view class="price-wrapper">
          <text class="price-value">¥{{ hotel.price }}</text>
          <text class="price-unit">/晚起</text>
        </view>
      </view>

      <!-- 设施服务 -->
      <view class="info-card" v-if="hotel.facilities">
        <view class="card-title">配套设施</view>
        <view class="info-item">
          <uni-icons type="gear" size="18" color="#10b981"></uni-icons>
          <text class="info-value">{{ hotel.facilities }}</text>
        </view>
      </view>


    </scroll-view>

    <view v-if="loading" class="loading-wrapper">
      <Loading></Loading>
    </view>

    <view v-if="!loading && !hotel" class="empty-wrapper">
      <Empty text="住宿不存在"></Empty>
    </view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
import { getHotelById } from '@/services/backend-api'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      hotel: null,
      loading: false,
      defaultCover: 'https://via.placeholder.com/400x300?text=Hotel'
    }
  },
  onLoad(options) {
    const id = parseInt(options.id)
    if (id) {
      this.loadHotel(id)
    }
  },
  methods: {
    async loadHotel(id) {
      this.loading = true
      try {
        const res = await getHotelById(id)
        if (res.code === 200 && res.data) {
          this.hotel = res.data
        } else {
          uni.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载住宿详情失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    parseFacilities(facilities) {
      if (!facilities) return []
      if (typeof facilities === 'string') {
        return facilities.split(',').map(f => f.trim()).filter(f => f)
      }
      return facilities
    },
    
    previewImage() {
      if (!this.hotel || !this.hotel.cover) {
        uni.showToast({
          title: '暂无图片可预览',
          icon: 'none'
        })
        return
      }
      
      uni.previewImage({
        urls: [this.hotel.cover],
        current: this.hotel.cover
      })
    },
    
    navigateToHotel() {
      if (!this.hotel) return
      
      const lat = parseFloat(this.hotel.latitude)
      const lng = parseFloat(this.hotel.longitude)
      
      if (isNaN(lat) || isNaN(lng) || (Math.abs(lat) < 0.000001 && Math.abs(lng) < 0.000001)) {
        uni.showToast({
          title: '暂无位置信息',
          icon: 'none'
        })
        return
      }
      
      uni.openLocation({
        latitude: lat,
        longitude: lng,
        name: this.hotel.name || '目的地',
        address: this.hotel.address || '',
        scale: 15,
        success: () => {
          console.log('打开地图导航成功')
        },
        fail: (err) => {
          console.error('打开地图失败:', err)
          uni.showToast({
            title: '打开地图失败',
            icon: 'none'
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.hotel-detail-page {
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
  margin-bottom: 16rpx;
}

.hotel-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
}

.hotel-type {
  background: #eef2ff;
  color: #667eea;
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  margin-left: 16rpx;
}

.hotel-badge {
  background: #eef2ff;
  color: #6366f1;
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  margin-left: 16rpx;
}

.hotel-status {
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
  text-align: justify;
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

/* 响应式布局 */
@media screen and (max-width: 750rpx) {
  .hotel-detail-page {
    padding-top: 120rpx;
  }
  
  .cover-section {
    height: 280rpx;
  }
  
  .info-card {
    width: calc(100% - 32rpx);
    margin: 16rpx 16rpx;
    padding: 20rpx;
  }
  
  .hotel-name {
    font-size: 32rpx;
  }
  
  .card-title {
    font-size: 28rpx;
    margin-bottom: 16rpx;
  }
  
  .card-content {
    font-size: 26rpx;
  }
  
  .info-item {
    padding: 10rpx;
    margin-bottom: 16rpx;
  }
  
  .info-label,
  .info-value {
    font-size: 26rpx;
  }
  
  .price-value {
    font-size: 42rpx;
  }
  
  .price-unit {
    font-size: 26rpx;
  }
}

@media screen and (min-width: 1200rpx) {
  .hotel-detail-page {
    padding-top: 140rpx;
  }
  
  .cover-section {
    height: 420rpx;
  }
  
  .info-card {
    width: calc(100% - 48rpx);
    margin: 24rpx 24rpx;
    padding: 32rpx;
  }
  
  .hotel-name {
    font-size: 40rpx;
  }
  
  .card-title {
    font-size: 36rpx;
    margin-bottom: 24rpx;
  }
  
  .card-content {
    font-size: 30rpx;
  }
  
  .info-item {
    padding: 16rpx;
    margin-bottom: 24rpx;
  }
  
  .info-label,
  .info-value {
    font-size: 30rpx;
  }
  
  .price-value {
    font-size: 54rpx;
  }
  
  .price-unit {
    font-size: 30rpx;
  }
}
</style>
