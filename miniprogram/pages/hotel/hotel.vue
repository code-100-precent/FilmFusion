<template>
  <view class="hotel-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>
    
    <NavBar :show-back="true"></NavBar>

    <view class="content">
      <!-- Search Bar -->
      <view class="search-bar">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#9ca3af"></uni-icons>
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索酒店名称、地址..."
            @confirm="handleSearch"
            @input="handleSearch"
          />
        </view>
      </view>

      <!-- Hotel List -->
      <scroll-view
        class="hotel-list"
        scroll-y
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="handleRefresh"
      >
        <view v-if="loading && hotels.length === 0" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="hotels.length === 0" class="empty-wrapper">
          <Empty text="暂无住宿"></Empty>
        </view>
        <view v-else class="hotel-list-container">
          <view
            v-for="(hotel, index) in hotels"
            :key="hotel.id"
            class="hotel-item"
            :style="{ 'animation-delay': index * 0.05 + 's' }"
            @click="goToDetail(hotel.id)"
          >
            <view class="hotel-cover-wrapper">
              <image 
                v-if="getFileUrl(hotel.cover)" 
                :src="getFileUrl(hotel.cover)" 
                class="hotel-cover" 
                mode="aspectFill"
                @error="handleImageError"
              />
              <view v-else class="cover-placeholder">
                <uni-icons type="image" size="32" color="#d1d5db"></uni-icons>
                <text class="placeholder-text">暂无</text>
              </view>
              
              <!-- 状态指示器 -->
              <view v-if="hotel.status === 1" class="status-indicator status-online">
                可用
              </view>
              <view class="hotel-type">{{ hotel.type }}</view>
            </view>
            
            <view class="hotel-info">
              <view class="hotel-header">
                <text class="hotel-name">{{ hotel.name }}</text>
              </view>
              <text class="hotel-desc">{{ formatDescription(hotel.description) }}</text>
              <view class="hotel-footer">
                <text class="hotel-price">¥{{ hotel.price }}/天</text>
              </view>
            </view>
          </view>
          
          <!-- 加载更多 -->
          <view v-if="hasMore && !loading" class="load-more" @click="loadMore">
            <text>加载更多</text>
          </view>
          
          <view v-if="!hasMore && hotels.length > 0" class="no-more">
            <text>没有更多了</text>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
import { getHotelPage } from '@/services/backend-api'
import { getFileUrl } from '@/utils'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      hotels: [],
      loading: false,
      refreshing: false,
      hasMore: true,
      keyword: '',
      nextCursor: null
    }
  },
  onLoad() {
    this.loadHotels()
  },
  methods: {
    async loadHotels(isRefresh = false) {
      if (this.loading) return
      
      this.loading = true
      
      try {
        const params = {
          cursor: isRefresh ? null : this.nextCursor,
          size: 10
        }
        
        if (this.keyword) {
          params.name = this.keyword
        }
        
        const res = await getHotelPage(params)
        
        // 添加调试日志，查看API返回的数据结构
        console.log('住宿API返回结果:', res)
        
        // 根据实际API返回结构处理数据
        if (res && res.records) {
          const newHotels = Array.isArray(res.records) ? res.records : []
          
          // 数据映射，确保字段名称与模板一致
          const mappedHotels = newHotels.map(hotel => ({
            id: hotel.id,
            name: hotel.name || '',
            description: hotel.description || '',
            address: hotel.address || '',
            phone: hotel.phone || '',
            contactName: hotel.contactName || '',
            cover: hotel.cover || hotel.image || hotel.thumbCover || '',
            price: hotel.price || '',
            status: hotel.status || 0,
            facilities: hotel.facilities || '',
            latitude: hotel.latitude || 0,
            longitude: hotel.longitude || 0
          }))
          
          if (isRefresh) {
            this.hotels = mappedHotels
            this.nextCursor = res.nextCursor
          } else {
            this.hotels = [...this.hotels, ...mappedHotels]
            this.nextCursor = res.nextCursor
          }
          
          // 判断是否还有更多数据
          this.hasMore = res.hasMore !== undefined ? res.hasMore : newHotels.length >= 10
          
          console.log('加载住宿数据成功，数量:', mappedHotels.length)
        } else {
          uni.showToast({
            title: '数据格式错误',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载住宿列表失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    
    loadMore() {
      if (this.hasMore && !this.loading) {
        this.loadHotels()
      }
    },
    
    handleRefresh() {
      this.refreshing = true
      this.loadHotels(true)
    },
    
    handleSearch() {
      this.current = 1
      this.loadHotels(true)
    },
    
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/hotel/detail?id=${id}`
      })
    },
    
    parseFacilities(facilities) {
      if (!facilities) return []
      if (typeof facilities === 'string') {
        return facilities.split(',').map(f => f.trim()).filter(f => f)
      }
      return facilities
    },
    
    formatDescription(desc) {
      if (!desc) return ''
      if (desc.length > 40) {
        return desc.substring(0, 40) + '...'
      }
      return desc
    },
    
    handleImageError(e) {
      console.log('图片加载失败:', e)
      // 可以在这里设置默认图片或其他处理逻辑
    },
    
    getFileUrl(url) {
      return getFileUrl(url)
    }
  }
}
</script>

<style lang="scss" scoped>
.hotel-page {
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
  background: linear-gradient(to top, #ffffff 0%, #20b2aa 100%);
  z-index: 0;
}

.content {
  padding: 16rpx;
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  width: 100%;
  height: calc(100vh - 132rpx);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.search-bar {
  margin-bottom: 24rpx;
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

.hotel-list-container {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding-bottom: 20rpx;
}

.hotel-item {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  animation: fadeInUp 0.6s ease-out forwards;
  opacity: 0;
  padding: 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hotel-item:active {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.1);
}

.hotel-cover-wrapper {
  width: 200rpx;
  height: 200rpx;
  position: relative;
  background: #f3f4f6;
  flex-shrink: 0;
}

.hotel-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
  gap: 8rpx;
}

.placeholder-text {
  font-size: 20rpx;
  color: #9ca3af;
  margin-top: 0;
}

.status-indicator {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  padding: 4rpx 12rpx;
  background: rgba(239, 68, 68, 0.9);
  color: #fff;
  font-size: 20rpx;
  border-radius: 8rpx;
  font-weight: 500;
  z-index: 2;
  
  &.status-online {
    background: rgba(16, 185, 129, 0.9);
  }
}

.hotel-type {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  padding: 4rpx 12rpx;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 20rpx;
  border-radius: 8rpx;
  font-weight: 500;
  z-index: 2;
}

.hotel-info {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.hotel-header {
  margin-bottom: 8rpx;
}

.hotel-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f2937;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hotel-desc {
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.4;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.hotel-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 0;
  border-top: none;
}

.hotel-price {
  font-size: 28rpx;
  font-weight: 700;
  color: #f59e0b;
}

.load-more,
.no-more {
  text-align: center;
  padding: 30rpx 0;
  font-size: 24rpx;
  color: #9ca3af;
}

.load-more {
  color: #667eea;
  font-weight: 500;
}

/* 响应式布局 */
@media screen and (max-width: 750rpx) {
  .hotel-page {
    padding-top: 120rpx;
  }
  
  .gradient-bg {
    height: 260rpx;
  }
  
  .content {
    padding-top: 16rpx;
  }
  
  .search-bar {
    margin: 0 16rpx 12rpx;
    padding: 12rpx 16rpx;
  }
  
  .search-input-wrapper {
    padding: 12rpx 16rpx;
  }
  
  .category-filter {
    margin: 0 16rpx 12rpx;
  }
  
  .category-item {
    padding: 12rpx 18rpx;
    font-size: 24rpx;
  }
  
  .hotel-list {
    height: calc(100vh - 220rpx);
    padding: 0 16rpx;
  }
  
  .hotel-card {
    margin-bottom: 16rpx;
  }
  
  .hotel-cover {
    height: 200rpx;
  }
  
  .hotel-content {
    padding: 20rpx;
  }
  
  .hotel-name {
    font-size: 28rpx;
  }
  
  .hotel-desc {
    font-size: 24rpx;
    margin-bottom: 16rpx;
  }
  
  .info-item {
    font-size: 22rpx;
  }
  
  .facility-tag {
    padding: 4rpx 10rpx;
    font-size: 20rpx;
  }
  
  .view-detail {
    font-size: 24rpx;
    padding: 8rpx 12rpx;
  }
}

@media screen and (min-width: 1200rpx) {
  .hotel-page {
    padding-top: 140rpx;
  }
  
  .gradient-bg {
    height: 340rpx;
  }
  
  .content {
    padding-top: 24rpx;
  }
  
  .search-bar {
    margin: 0 32rpx 24rpx;
    padding: 20rpx 24rpx;
  }
  
  .search-input-wrapper {
    padding: 18rpx 24rpx;
  }
  
  .category-filter {
    margin: 0 32rpx 24rpx;
  }
  
  .category-item {
    padding: 18rpx 28rpx;
    font-size: 28rpx;
  }
  
  .hotel-list {
    height: calc(100vh - 300rpx);
    padding: 0 32rpx;
  }
  
  .hotel-card {
    margin-bottom: 24rpx;
  }
  
  .hotel-cover {
    height: 280rpx;
  }
  
  .hotel-content {
    padding: 28rpx;
  }
  
  .hotel-name {
    font-size: 36rpx;
  }
  
  .hotel-desc {
    font-size: 28rpx;
    margin-bottom: 24rpx;
  }
  
  .info-item {
    font-size: 26rpx;
  }
  
  .facility-tag {
    padding: 8rpx 16rpx;
    font-size: 24rpx;
  }
  
  .view-detail {
    font-size: 28rpx;
    padding: 12rpx 20rpx;
  }
}
</style>