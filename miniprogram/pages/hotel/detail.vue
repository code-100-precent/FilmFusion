<template>
  <view class="hotel-detail-page">
    <NavBar :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y v-if="!loading && hotel">
      <!-- 封面图 -->
      <view class="cover-wrapper">
        <image :src="hotel.cover || defaultCover" class="cover" mode="aspectFill"></image>
      </view>

      <!-- 基本信息 -->
      <view class="info-card">
        <view class="card-header">
          <text class="hotel-name">{{ hotel.name }}</text>
          <view class="hotel-status" :class="{ 'status-online': hotel.status === 1 }">
            {{ hotel.status === 1 ? '营业中' : '暂停营业' }}
          </view>
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
        </view>
      </view>

      <!-- 价格信息 -->
      <view class="info-card" v-if="hotel.price">
        <view class="card-title">参考价格</view>
        <view class="price-wrapper">
          <text class="price-value">¥{{ hotel.price }}</text>
          <text class="price-unit">/晚起</text>
        </view>
      </view>

      <!-- 设施服务 -->
      <view class="info-card" v-if="hotel.facilities">
        <view class="card-title">设施服务</view>
        <view class="facilities-wrapper">
          <view 
            v-for="(facility, index) in parseFacilities(hotel.facilities)" 
            :key="index" 
            class="facility-tag"
          >
            {{ facility }}
          </view>
        </view>
      </view>

      <!-- 导航按钮 -->
      <view class="action-section">
        <button class="nav-button" @click="navigateToHotel">
          <uni-icons type="paperplane" size="20" color="#fff"></uni-icons>
          <text>导航到这里</text>
        </button>
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
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.cover-wrapper {
  width: 100%;
  height: 400rpx;
  overflow: hidden;
}

.cover {
  width: 100%;
  height: 100%;
}

.info-card {
  width: 100%;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin: 24rpx 32rpx;
  box-sizing: border-box;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hotel-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
}

.hotel-status {
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

.facilities-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.facility-tag {
  padding: 12rpx 24rpx;
  background: #f3f4f6;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #6b7280;
}

.action-section {
  padding: 32rpx;
}

.nav-button {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 16rpx;
  padding: 28rpx;
  font-size: 32rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
  }
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 32rpx;
}
</style>
