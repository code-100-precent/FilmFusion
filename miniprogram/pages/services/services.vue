<template>
  <view class="services-page">
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
            placeholder="搜索"
            @confirm="handleSearch"
            @input="handleSearch"
          />
        </view>
      </view>

      <!-- 服务列表 -->
      <scroll-view
        class="shoot-list"
        scroll-y
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="handleRefresh"
      >
        <view v-if="loading && shoots.length === 0" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="shoots.length === 0" class="empty-wrapper">
          <Empty text="暂无服务"></Empty>
        </view>
        <view v-else>
          <view
            v-for="(shoot, index) in shoots"
            :key="shoot.id"
            class="shoot-card"
            :style="{ 'animation-delay': index * 0.05 + 's' }"
            @click="goToDetail(shoot.id)"
          >
            <!-- 服务封面图片 -->
            <view class="shoot-cover">
              <image 
                v-if="getFileUrl(shoot.image || shoot.thumbImage)"
                :src="getFileUrl(shoot.image || shoot.thumbImage)" 
                class="cover-image" 
                mode="aspectFill"
              ></image>
              <view v-else class="cover-placeholder">
                <uni-icons type="image" size="40" color="#d1d5db"></uni-icons>
              </view>
            </view>
            
            <view class="shoot-content">
              <view class="shoot-header">
                <view class="shoot-title-row">
                  <text class="shoot-name">{{ shoot.name }}</text>
                </view>
              </view>
              <text class="shoot-desc">{{ shoot.description }}</text>
              <view class="shoot-info">
                <view class="info-item">
                  <uni-icons type="location" size="16" color="#6366f1"></uni-icons>
                  <text>{{ shoot.address }}</text>
                </view>
                <view class="info-item">
                  <uni-icons type="phone" size="16" color="#6366f1"></uni-icons>
                  <text>{{ shoot.phone }}</text>
                </view>
                <view class="info-item">
                  <uni-icons type="person" size="16" color="#6366f1"></uni-icons>
                  <text>{{ shoot.contactName }}</text>
                </view>
              </view>
              <view class="shoot-footer">
                <text class="shoot-price">¥{{ shoot.price }}</text>
                <text class="view-detail">查看详情</text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="hasMore && !loading" class="load-more">
          <text>上拉加载更多</text>
        </view>
        <view v-if="!hasMore && shoots.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
      </scroll-view>
    </view>

    <!-- 底部导航栏 -->
    <TabBar :current="'services'"></TabBar>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import TabBar from '../../components/TabBar/TabBar.vue'
import Loading from '../../components/Loading/Loading.vue'
import Empty from '../../components/Empty/Empty.vue'
// 使用真实后端API
import { getShootPage } from '../../services/backend-api'
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
      shoots: [],
      nextCursor: null, // 游标分页
      loading: false,
      refreshing: false,
      hasMore: true
    }
  },
  onLoad() {
    this.loadShoots()
  },
  methods: {
    async loadShoots(reset = false) {
      if (this.loading) return

      if (reset) {
        this.shoots = []
        this.nextCursor = null
        this.hasMore = true
      }

      this.loading = true
      try {
        const res = await getShootPage({
          cursor: reset ? null : this.nextCursor,
          size: 10,
          keyword: this.keyword || undefined
        })

        // 处理游标分页响应
        if (res && res.records) {
          const dataList = Array.isArray(res.records) ? res.records : []
          
          if (reset) {
            this.shoots = dataList
          } else {
            this.shoots = [...this.shoots, ...dataList]
          }
          this.nextCursor = res.nextCursor
          this.hasMore = res.hasMore || false
        }
      } catch (error) {
        console.error('加载服务失败:', error)
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
      this.loadShoots(true)
    },
    
    handleRefresh() {
      this.refreshing = true
      this.loadShoots(true)
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.loadShoots()
    },
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/shoot/detail?id=${id}`
      })
    },
    getFileUrl(url) {
      return getFileUrl(url)
    }
  }
}
</script>

<style lang="scss" scoped>
.services-page {
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
  padding: 16rpx 24rpx;
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
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

.shoot-list {
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

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}

.shoot-card {
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

.shoot-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.shoot-cover {
  width: 100%;
  height: 200rpx;
  position: relative;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  display: block;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: #f9fafb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.shoot-content {
  padding: 24rpx;
}

.shoot-header {
  margin-bottom: 16rpx;
}

.shoot-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.shoot-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
}



.shoot-desc {
  display: block;
  font-size: 28rpx;
  color: #6b7280;
  line-height: 1.8;
  margin-bottom: 16rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.shoot-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 16rpx;
  padding: 16rpx;
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

.shoot-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16rpx;
  border-top: 1rpx solid #f3f4f6;
}

.shoot-price {
  font-size: 28rpx;
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
