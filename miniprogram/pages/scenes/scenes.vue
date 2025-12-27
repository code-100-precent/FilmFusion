<template>
  <view class="scenes-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>
    
    <NavBar :show-back="false"></NavBar>

    <view class="content">
      <!-- 搜索栏 -->
      <view class="search-bar">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#9ca3af"></uni-icons>
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索场地名称、类型、地址..."
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

      <!-- 场地列表 -->
      <scroll-view
        class="location-list"
        scroll-y
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="handleRefresh"
      >
        <view v-if="loading && locations.length === 0" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="locations.length === 0" class="empty-wrapper">
          <Empty text="暂无场地"></Empty>
        </view>
        <view v-else class="location-list-container">
          <view
            v-for="(location, index) in locations"
            :key="location.id"
            class="location-item"
            :style="{ 'animation-delay': index * 0.05 + 's' }"
            @click="goToDetail(location.id)"
          >
            <!-- 场地封面图片 -->
            <view class="location-cover-wrapper">
                <image 
                  v-if="getFileUrl(location.cover || location.thumbImage)" 
                  :src="getFileUrl(location.cover || location.thumbImage)" 
                  class="location-cover" 
                  mode="aspectFill"
                  @error="handleImageError"
                />
                <view v-else class="cover-placeholder">
                  <uni-icons type="image" size="32" color="#d1d5db"></uni-icons>
                  <text class="placeholder-text">暂无</text>
                </view>
              </view>
            
            <view class="location-info">
              <view class="location-header">
                <text class="location-name">{{ location.name }}</text>
              </view>
              <text class="location-desc">{{ formatDescription(location.locationDescription) }}</text>
              <view class="location-tags" v-if="location.dramaId">
                <view class="drama-tag">
                  <uni-icons type="videocam" size="12" color="#ef4444"></uni-icons>
                  <text>相关影视</text>
                </view>
              </view>

            </view>
          </view>
        </view>

        <view v-if="hasMore && !loading" class="load-more">
          <text>上拉加载更多</text>
        </view>
        <view v-if="!hasMore && locations.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
        
        <!-- 底部占位符，防止被 TabBar 遮挡 -->
        <view class="bottom-spacer"></view>
      </scroll-view>
    </view>

    <!-- 底部导航栏 -->
    <TabBar :current="'scenes'"></TabBar>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import TabBar from '../../components/TabBar/TabBar.vue'
import Loading from '../../components/Loading/Loading.vue'
import Empty from '../../components/Empty/Empty.vue'
import { getLocationPage } from '../../services/backend-api'
// 导入文件URL处理函数
import { getFileUrl } from '../../utils'

export default {
  components: {
    NavBar,
    TabBar,
    Loading,
    Empty
  },
  data() {
    return {
      keyword: '',
      selectedCategory: 'natural',
      categories: [
        { label: '自然景观', value: 'natural', icon: 'image' },
        { label: '人文景观', value: 'humanities', icon: 'home' },
        { label: '城市场景', value: 'urban', icon: 'location' },
        { label: '特色场景', value: 'feature', icon: 'star' }
      ],
      allLocations: [],
      nextCursor: null, // 游标分页
      loading: false,
      refreshing: false,
      hasMore: true
    }
  },
  computed: {
    locations() {
      return this.allLocations.filter(item => item.type === this.selectedCategory)
    }
  },
  onLoad(options) {
    if (options && options.type) {
      this.selectedCategory = options.type
    }
    this.loadLocations()
  },
  methods: {
    async loadLocations(reset = false) {
      if (this.loading) return

      if (reset) {
        this.allLocations = []
        this.nextCursor = null
        this.hasMore = true
      }

      this.loading = true
      try {
        const res = await getLocationPage({
          cursor: reset ? null : this.nextCursor,
          size: 100,
          keyword: this.keyword || undefined
        })

        // 处理游标分页响应
        if (res && res.records) {
          const dataList = Array.isArray(res.records) ? res.records : []
          
          if (reset) {
            this.allLocations = dataList
          } else {
            this.allLocations = [...this.allLocations, ...dataList]
          }
          this.nextCursor = res.nextCursor
          this.hasMore = res.hasMore || false
        }
      } catch (error) {
        console.error('加载场地失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    handleSearch() {
      this.loadLocations(true)
    },
    selectCategory(category) {
      this.selectedCategory = category
    },
    handleRefresh() {
      this.refreshing = true
      this.loadLocations(true)
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.loadLocations()
    },
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/location/detail?id=${id}`
      })
    },
    handleImageError(e) {
      console.log('图片加载失败:', e)
      // 可以在这里设置默认图片或其他处理逻辑
    },
    getFileUrl(url) {
      return getFileUrl(url)
    },
    formatDescription(desc) {
      if (!desc) return ''
      if (desc.length > 40) {
        return desc.substring(0, 40) + '...'
      }
      return desc
    },
    getCategoryLabel(value) {
      const category = this.categories.find(c => c.value === value)
      return category ? category.label : value
    }
  }
}
</script>

<style lang="scss" scoped>
.scenes-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
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
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 16rpx 24rpx;
  box-sizing: border-box;
  width: 100%;
  position: relative;
  z-index: 1;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  /* 兼容火狐浏览器 */
  scrollbar-width: none;
  /* 兼容IE浏览器 */
  -ms-overflow-style: none;
}

.location-list {
  flex: 1;
  height: 0;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  /* 兼容火狐浏览器 */
  scrollbar-width: none;
  /* 兼容IE浏览器 */
  -ms-overflow-style: none;
}

/* 页面标题样式 */
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
  flex-shrink: 0;
}

.category-filter {
  margin-bottom: 16rpx;
  flex-shrink: 0;
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
  
  &.active {
    background: #6366f1;
    color: #fff;
    box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.3);
  }
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

.location-list {
  height: calc(100vh - 88rpx - 200rpx);
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}

.location-list-container {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding-bottom: 20rpx;
}

.location-item {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  padding: 0;
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

.location-item:active {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.1);
}

.location-cover-wrapper {
  width: 200rpx;
  height: 200rpx;
  position: relative;
  background: #f3f4f6;
  flex-shrink: 0;
}

.location-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.location-badge {
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



.location-info {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.location-header {
  margin-bottom: 8rpx;
}

.location-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f2937;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.location-desc {
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

.location-tags {
  display: flex;
  gap: 8rpx;
  margin-bottom: 8rpx;
}

.drama-tag {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 12rpx;
  background: #fef2f2;
  border-radius: 12rpx;
  font-size: 20rpx;
  color: #ef4444;
}



.view-detail {
  display: none;
}

.load-more,
.no-more {
  text-align: center;
  padding: 30rpx 0;
  font-size: 24rpx;
  color: #9ca3af;
}
.bottom-spacer {
  height: calc(140rpx + env(safe-area-inset-bottom));
  width: 100%;
}
</style>
