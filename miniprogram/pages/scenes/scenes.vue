<template>
  <view class="scenes-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>
    
    <NavBar :show-back="false"></NavBar>

    <view class="content">
      <!-- 页面标题 -->
      <view class="page-title">
        <text class="title-text">拍摄场地</text>
      </view>
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
        <view v-else>
          <view
            v-for="location in locations"
            :key="location.id"
            class="location-card"
            @click="goToDetail(location.id)"
          >
            <view class="location-header">
              <view class="location-title-row">
                <text class="location-name">{{ location.name }}</text>
                <view class="location-badge">{{ location.type }}</view>
              </view>
              <view class="location-status" :class="{ 'status-available': location.status === 1 }">
                {{ location.status === 1 ? '可用' : '不可用' }}
              </view>
            </view>
            <text class="location-desc">{{ location.locationDescription }}</text>
            <view class="location-info">
              <view class="info-item">
                <uni-icons type="location" size="16" color="#6366f1"></uni-icons>
                <text>{{ location.address }}</text>
              </view>
              <view class="info-item">
                <uni-icons type="phone" size="16" color="#6366f1"></uni-icons>
                <text>{{ location.contactPhone }}</text>
              </view>
              <view class="info-item">
                <uni-icons type="person" size="16" color="#6366f1"></uni-icons>
                <text>{{ location.contactName }}</text>
              </view>
            </view>
            <view class="location-footer">
              <text class="location-price">¥{{ location.price }}/天</text>
              <text class="view-detail">查看详情</text>
            </view>
          </view>
        </view>

        <view v-if="hasMore && !loading" class="load-more">
          <text>上拉加载更多</text>
        </view>
        <view v-if="!hasMore && locations.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
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
import { getLocationPage } from '../../services/api'

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
      locations: [],
      current: 1,
      size: 10,
      total: 0,
      loading: false,
      refreshing: false,
      hasMore: true
    }
  },
  onLoad() {
    this.loadLocations()
  },
  methods: {
    async loadLocations(reset = false) {
      if (this.loading) return

      if (reset) {
        this.current = 1
        this.locations = []
        this.hasMore = true
      }

      this.loading = true
      try {
        const res = await getLocationPage({
          current: this.current,
          size: this.size,
          keyword: this.keyword || undefined
        })

        if (res.code === 200) {
          // 后端返回格式: { code: 200, message: "请求成功", data: [...], pagination: {...} }
          const dataList = Array.isArray(res.data) ? res.data : []
          const pagination = res.pagination || {}
          
          if (reset) {
            this.locations = dataList
          } else {
            this.locations = [...this.locations, ...dataList]
          }
          this.total = pagination.totalItems || 0
          this.hasMore = this.locations.length < this.total
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
    handleRefresh() {
      this.refreshing = true
      this.loadLocations(true)
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.current++
      this.loadLocations()
    },
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/location/detail?id=${id}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.scenes-page {
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
  padding: 20rpx 32rpx;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
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
  height: calc(100vh - 88rpx - 200rpx);
  
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
  margin-top: 24rpx;
  margin-bottom: 32rpx;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 0 24rpx;
  height: 80rpx;
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

.location-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  width: 100%;
  box-sizing: border-box;
}

.location-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.location-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.location-title-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.location-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
}

.location-badge {
  padding: 6rpx 16rpx;
  background: #eef2ff;
  color: #6366f1;
  font-size: 22rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.location-status {
  padding: 6rpx 16rpx;
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

.location-desc {
  display: block;
  font-size: 28rpx;
  color: #6b7280;
  line-height: 1.8;
  margin-bottom: 24rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.location-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 24rpx;
  padding: 20rpx;
  background: #f9fafb;
  border-radius: 12rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 26rpx;
  color: #374151;
}

.location-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 1rpx solid #f3f4f6;
}

.location-price {
  font-size: 32rpx;
  font-weight: 700;
  color: #f59e0b;
}

.view-detail {
  font-size: 26rpx;
  color: #6366f1;
  font-weight: 500;
}

.load-more,
.no-more {
  text-align: center;
  padding: 40rpx 0;
  font-size: 26rpx;
  color: #9ca3af;
}
</style>
