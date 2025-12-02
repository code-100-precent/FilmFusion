<template>
  <view class="tourroute-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>

    <NavBar :show-back="true"></NavBar>

    <!-- 内容区域 -->
    <scroll-view class="content" scroll-y @scrolltolower="loadMore" :refresher-enabled="true" :refresher-triggered="refreshing" @refresherrefresh="handleRefresh">
      <!-- 页面标题 -->
      <view class="page-title">
        <text class="title-text">跟着影视游</text>
      </view>

      <!-- 搜索栏 -->
      <view class="search-bar">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#9ca3af"></uni-icons>
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索线路..."
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
            v-for="route in routes"
            :key="route.id"
            class="route-item"
            @click="goToDetail(route.id)"
          >
            <view class="route-cover">
              <image :src="route.cover || defaultCover" class="route-image" mode="aspectFill"></image>
            </view>
            <view class="route-content">
              <view class="route-header">
                <text class="route-title">{{ route.name }}</text>
                <view class="route-theme" v-if="route.theme">{{ route.theme }}</view>
              </view>
              <text class="route-meta">{{ route.description }}</text>
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
      currentPage: 1,
      pageSize: 10,
      totalPages: 0,
      defaultCover: 'https://picsum.photos/400/200?random=default'
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    async loadData() {
      if (this.loading) return
      
      this.loading = true
      try {
        console.log('开始加载旅游线路，参数:', {
          currentPage: this.currentPage,
          pageSize: this.pageSize,
          keyword: this.keyword
        })
        
        // 模拟数据，用于开发测试和API调用失败时的备用
        const mockData = [
          {
            id: 1,
            name: '北京五日游',
            description: '游览故宫、长城、颐和园等著名景点',
            theme: '文化之旅',
            cover: 'https://picsum.photos/400/200?random=1',
            price: 1999,
            duration: '5天4晚',
            status: 1
          },
          {
            id: 2,
            name: '上海迪士尼乐园',
            description: '畅玩上海迪士尼乐园，感受童话世界',
            theme: '亲子游',
            cover: 'https://picsum.photos/400/200?random=2',
            price: 2599,
            duration: '3天2晚',
            status: 1
          },
          {
            id: 3,
            name: '张家界自然风光',
            description: '欣赏张家界独特的喀斯特地貌',
            theme: '自然探索',
            cover: 'https://picsum.photos/400/200?random=3',
            price: 2299,
            duration: '4天3晚',
            status: 1
          },
          {
            id: 4,
            name: '三亚阳光海岸',
            description: '享受阳光沙滩，体验海岛度假',
            theme: '海滩度假',
            cover: 'https://picsum.photos/400/200?random=4',
            price: 2899,
            duration: '5天4晚',
            status: 1
          }
        ]
        
        try {
          const res = await getTourPage({
            current: this.currentPage,
            size: this.pageSize,
            keyword: this.keyword || undefined
          })

          console.log('API响应:', JSON.stringify(res))
          
          if (res.code === 200 && res.data && res.data.length > 0) {
            const newRoutes = res.data.map(item => ({
              id: item.id,
              name: item.name,
              description: item.description || '',
              theme: item.theme,
              features: item.features,
              cover: item.cover || item.imageUrl || this.defaultCover,
              transport: item.transport,
              hotel: item.hotel,
              food: item.food,
              image: item.image,
              price: item.price,
              duration: item.duration,
              status: item.status,
              imageUrl: item.imageUrl
            }))

            console.log('解析后的数据:', newRoutes)
            
            if (this.currentPage === 1) {
              this.routes = newRoutes
            } else {
              this.routes = [...this.routes, ...newRoutes]
            }

            // Update pagination info
            if (res.pagination) {
              this.totalPages = res.pagination.totalPages
              this.hasMore = this.currentPage < this.totalPages
            } else {
              this.hasMore = newRoutes.length >= this.pageSize
            }
            
            console.log('加载完成，当前数据量:', this.routes.length)
          } else {
            console.warn('API返回数据为空或错误，使用模拟数据')
            // API返回空数据或错误，使用模拟数据
            if (this.currentPage === 1) {
              this.routes = mockData
            }
            this.hasMore = false
          }
        } catch (apiError) {
          console.error('API调用失败，使用模拟数据:', apiError.message)
          // API调用失败，使用模拟数据
          if (this.currentPage === 1) {
            this.routes = mockData
          }
          this.hasMore = false
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
      this.currentPage = 1
      this.routes = []
      this.loadData()
    },
    async handleRefresh() {
      this.refreshing = true
      this.currentPage = 1
      this.routes = []
      await this.loadData()
      this.refreshing = false
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.currentPage++
      this.loadData()
    },
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/tourroute/detail?id=${id}`
      })
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
  gap: 12rpx;
}

.route-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 16rpx;
  background: #f9fafb;
  border-radius: 12rpx;
  transition: all 0.3s;
  overflow: hidden;

  &:active {
    background: #f3f4f6;
    transform: translateX(4rpx);
  }
}

.route-cover {
  width: 120rpx;
  height: 120rpx;
  border-radius: 10rpx;
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
  gap: 8rpx;
  min-width: 0;
  justify-content: center;
}

.route-header {
  margin-bottom: 4rpx;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.route-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  word-break: break-all;
}

.route-theme {
  display: inline-block;
  font-size: 20rpx;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.1);
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
  margin-left: 12rpx;
  flex-shrink: 0;
}

.route-meta {
  font-size: 20rpx;
  color: #9ca3af;
  line-height: 1.4;
}

.load-more,
.no-more {
  text-align: center;
  padding: 40rpx 0;
  font-size: 26rpx;
  color: #9ca3af;
}

</style>

