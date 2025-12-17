<template>
  <view class="tourroute-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>

    <NavBar :show-back="true"></NavBar>

    <!-- 内容区域 -->
    <scroll-view class="content" scroll-y @scrolltolower="loadMore" :refresher-enabled="true" :refresher-triggered="refreshing" @refresherrefresh="handleRefresh">
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

      <!-- 线路列表 -->
      <view class="route-list">
        <view v-if="loading && routes.length === 0" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="routes.length === 0" class="empty-wrapper">
          <Empty text="暂无线路"></Empty>
        </view>
        <view v-else>
          <view
            v-for="(route, index) in routes"
            :key="route.id"
            class="route-item"
            :style="{ 'animation-delay': index * 0.05 + 's' }"
            @click="goToDetail(route.id)"
          >
            <view class="route-cover">
              <image :src="getFileUrl(route.cover || route.image) || defaultCover" class="route-image" mode="aspectFill"></image>
            </view>
            <view class="route-content">
              <view class="route-header">
                <text class="route-title">{{ route.name }}</text>
                <view class="route-theme" v-if="route.theme">{{ route.theme }}</view>
              </view>
              <text class="route-meta">{{ formatDescription(route.description) }}</text>
            </view>
          </view>
        </view>

        <view v-if="hasMore && !loading" class="load-more">
          <text>上拉加载更多</text>
        </view>
        <view v-if="!hasMore && routes.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
      </view>
    </scroll-view>

  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
import { getTourPage } from '@/services/backend-api'
import { getFileUrl } from '@/utils/fileUrl'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      keyword: '',
      routes: [],
      loading: false,
      refreshing: false,
      hasMore: true,
      nextCursor: null, // 游标分页
      defaultCover: 'https://via.placeholder.com/400x200?text=Tour+Route'
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    getFileUrl, // 注册到 methods 以便模板使用
    async loadData(reset = false) {
      if (this.loading) return
      
      if (reset) {
        this.routes = []
        this.nextCursor = null
        this.hasMore = true
      }

      this.loading = true
      try {
        const res = await getTourPage({
          cursor: reset ? null : this.nextCursor,
          size: 10,
          keyword: this.keyword || undefined
        })

        // 处理游标分页响应
        if (res && res.records) {
          const newRoutes = res.records.map(item => ({
            id: item.id,
            name: item.name,
            description: item.description,
            theme: item.theme,
            features: item.features,
            cover: item.cover,
            transport: item.transport,
            hotel: item.hotel,
            food: item.food,
            image: item.image
          }))

          if (reset) {
            this.routes = newRoutes
          } else {
            this.routes = [...this.routes, ...newRoutes]
          }
          
          this.nextCursor = res.nextCursor
          this.hasMore = res.hasMore !== undefined ? res.hasMore : (res.nextCursor !== null)
        } else if (res && res.code === 200 && res.data) {
          // 兼容旧的响应格式
          const routeRecords = res.data.records || res.data || [];
          const newRoutes = routeRecords.map(item => ({
            id: item.id,
            name: item.name,
            description: item.description,
            theme: item.theme,
            features: item.features,
            cover: item.cover,
            transport: item.transport,
            hotel: item.hotel,
            food: item.food,
            image: item.image
          }))

          if (reset) {
            this.routes = newRoutes
          } else {
            this.routes = [...this.routes, ...newRoutes]
          }
          
          this.nextCursor = res.data.nextCursor
          this.hasMore = res.data.hasMore !== undefined ? res.data.hasMore : (res.data.nextCursor !== null)
        } else {
          uni.showToast({
            title: res?.message || '加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载线路失败:', error)
        console.error('错误详情:', error.message, error.response)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.loadData(true)
    },
    async handleRefresh() {
      this.refreshing = true
      await this.loadData(true)
      this.refreshing = false
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.loadData()
    },
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/tourroute/detail?id=${id}`
      })
    },
    formatDescription(desc) {
      if (!desc) return ''
      if (desc.length > 40) {
        return desc.substring(0, 40) + '...'
      }
      return desc
    }
  }
}
</script>

<style lang="scss" scoped>
.tourroute-page {
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
  height: calc(100vh - 132rpx - 140rpx);

  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
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
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
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
  flex: 1;
  box-sizing: border-box;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #1f2937;
}

.route-list {
  width: 100%;
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}

.route-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.route-item {
  display: flex;
  align-items: flex-start;
  gap: 0;
  padding: 0;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out forwards;
  opacity: 0;

  &:active {
    background: #fff;
    transform: translateY(-2rpx);
    box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.1);
  }
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

.route-cover {
  width: 200rpx;
  height: 200rpx;
  border-radius: 0;
  overflow: hidden;
  flex-shrink: 0;
  background: #f3f4f6;
}

.route-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.route-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
  height: 200rpx;
  padding: 20rpx;
  box-sizing: border-box;
}

.route-header {
  margin-bottom: 8rpx;
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 12rpx;
}

.route-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.route-theme {
  display: inline-block;
  font-size: 20rpx;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.1);
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
  margin-left: 0;
  flex-shrink: 0;
}

.route-meta {
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.load-more,
.no-more {
  text-align: center;
  padding: 40rpx 0;
  font-size: 26rpx;
  color: #9ca3af;
}

</style>

