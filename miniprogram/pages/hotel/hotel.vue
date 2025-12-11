<template>
  <view class="hotel-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>
    
    <NavBar :show-back="true"></NavBar>

    <view class="content">
      <!-- 搜索栏 -->
      <view class="search-bar">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#9ca3af"></uni-icons>
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索住宿名称、地址..."
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
            :key="category.value"
            class="category-item"
            :class="{ active: selectedCategory === category.value }"
            @click="selectCategory(category.value)">
            <uni-icons :type="category.icon" size="20" :color="selectedCategory === category.value ? '#fff' : '#6b7280'"></uni-icons>
            <text>{{ category.label }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 住宿列表 -->
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
        <view v-else>
          <view
  v-for="(hotel, index) in hotels"
  :key="hotel.id"
  class="hotel-card"
  :style="{ 'animation-delay': index * 0.05 + 's' }"
  @click="goToDetail(hotel.id)"
>
  <!-- 住宿封面图片 -->
  <view class="hotel-cover">
    <image 
      v-if="hotel.cover" 
      :src="hotel.cover" 
      class="hotel-image" 
      mode="aspectFill"
      @error="handleImageError"
    />
    <view v-else class="cover-placeholder">
      <uni-icons type="image" size="40" color="#d1d5db"></uni-icons>
      <text class="placeholder-text">暂无图片</text>
    </view>
    
    <!-- 价格标签 -->
    <view class="price-tag">¥{{ hotel.price }}/天</view>
    
    <!-- 状态指示器 -->
    <view class="status-indicator" :class="{ 'status-online': hotel.status === 1 }">
      {{ hotel.status === 1 ? '可用' : '不可用' }}
    </view>
  </view>
  
  <view class="hotel-content">
    <view class="hotel-header">
      <view class="hotel-title-row">
        <text class="hotel-name">{{ hotel.name }}</text>
        <view class="hotel-type">{{ hotel.type }}</view>
      </view>
      <view class="hotel-status" :class="{ 'status-available': hotel.status === 1 }">
        {{ hotel.status === 1 ? '可用' : '不可用' }}
      </view>
    </view>
    
    <text class="hotel-desc">{{ hotel.description }}</text>
    
    <view class="hotel-info">
      <view class="info-item">
        <uni-icons type="location" size="16" color="#667eea"></uni-icons>
        <text>{{ hotel.address }}</text>
      </view>
      <view class="info-item">
        <uni-icons type="phone" size="16" color="#667eea"></uni-icons>
        <text>{{ hotel.contactPhone }}</text>
      </view>
    </view>
    
    <view class="hotel-footer">
      <text class="hotel-price">¥{{ hotel.price }}/天</text>
      <text class="view-detail">查看详情</text>
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
      selectedCategory: '',
      nextCursor: null,
      categories: [
        { label: '全部', value: '', icon: 'list' },
        { label: '星级酒店', value: '星级酒店', icon: 'star' },
        { label: '经济型', value: '经济型', icon: 'wallet' },
        { label: '民宿', value: '民宿', icon: 'home' },
        { label: '公寓', value: '公寓', icon: 'house' },
        { label: '青年旅社', value: '青年旅社', icon: 'staff' }
      ]
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
        
        if (this.selectedCategory) {
          params.type = this.selectedCategory
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
            cover: hotel.cover || hotel.thumbCover || '',
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
    
    selectCategory(category) {
      this.selectedCategory = category
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
    
    handleImageError(e) {
      console.log('图片加载失败:', e)
      // 可以在这里设置默认图片或其他处理逻辑
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
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 24rpx;
  margin-right: 12rpx;
  background: #fff;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #6b7280;
  transition: all 0.3s;
  border: 1rpx solid transparent;
}

.category-item.active {
  background: #667eea;
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

.hotel-list {
  height: calc(100vh - 240rpx);
  padding: 0 8rpx;
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.hotel-card {
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out forwards;
  opacity: 0;
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

.hotel-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.hotel-cover {
  position: relative;
  height: 320rpx;
  overflow: hidden;
}

.cover-image {
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
  gap: 16rpx;
}

.placeholder-text {
  font-size: 26rpx;
  color: #9ca3af;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  color: #9ca3af;
}

.placeholder-text {
  font-size: 24rpx;
  margin-top: 16rpx;
}

.price-tag {
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

.status-indicator {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  padding: 4rpx 12rpx;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 20rpx;
  border-radius: 8rpx;
  
  &.status-online {
    background: rgba(16, 185, 129, 0.8);
  }
}

.hotel-content {
  padding: 24rpx;
}

.hotel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.hotel-title-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.hotel-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
  flex: 1;
}

.hotel-type {
  padding: 6rpx 12rpx;
  background: #eef2ff;
  color: #667eea;
  font-size: 22rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.hotel-status {
  padding: 6rpx 12rpx;
  background: #fee2e2;
  color: #ef4444;
  font-size: 22rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.status-available {
  background: #d1fae5;
  color: #10b981;
}

.hotel-desc {
  display: block;
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 16rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.hotel-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 16rpx;
  padding: 16rpx;
  background: #f9fafb;
  border-radius: 12rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 24rpx;
  color: #374151;
}

.hotel-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16rpx;
  border-top: 1rpx solid #f3f4f6;
}

.hotel-price {
  font-size: 32rpx;
  font-weight: 700;
  color: #f59e0b;
}

.view-detail {
  font-size: 26rpx;
  color: #667eea;
  font-weight: 500;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  background: #eef2ff;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.hotel-card:active .view-detail {
  background: #e0e7ff;
}

.load-more,
.no-more {
  padding: 30rpx 0;
  text-align: center;
  font-size: 26rpx;
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