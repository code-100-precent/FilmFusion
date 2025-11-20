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

    <!-- 底部导航栏 -->
    <TabBar :current="'tourroute'"></TabBar>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import TabBar from '@/components/TabBar/TabBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'

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
      routes: [],
      loading: false,
      refreshing: false,
      hasMore: true,
      currentPage: 1,
      pageSize: 10,
      defaultCover: 'https://via.placeholder.com/400x200?text=Tour+Route',
      mockRoutes: [
  {
    id: 1,
    name: '熊猫家园探秘游',
    description: '以在雅安拍摄的熊猫主题影视作品为线索，串联碧峰峡熊猫基地、宝兴蜂桶寨邓池沟等景区景点。',
    theme: '熊猫文化',
    features: '熊猫文化深度游+熊猫文创市集',
    cover: 'https://via.placeholder.com/400x200?text=Panda+Route',
    transportInfo: '自驾或包车，全程约2-3天',
    accommodation: '碧峰峡景区周边民宿、五星级酒店',
    foodRecommendation: '雅安特色美食：雨城茶、野生菌、竹笋等'
  },
  {
    id: 2,
    name: '世界茶源寻根游',
    description: '结合以茶园为背景的影视作品，涵盖蒙顶山茶园、牛碾坪、大地指纹等景点。',
    theme: '茶文化',
    features: '茶文化体验+主题民宿+茶艺表演',
    cover: 'https://via.placeholder.com/400x200?text=Tea+Route',
    transportInfo: '自驾或包车，全程约2天',
    accommodation: '蒙顶山茶园周边特色民宿',
    foodRecommendation: '蒙顶山茶、茶叶蛋、茶香鸡等'
  },
  {
    id: 3,
    name: '红色文化体验游',
    description: '围绕红色题材影视作品在雅安的拍摄地，打造包含红军长征翻越夹金山纪念馆等景点的主题线路。',
    theme: '红色文化',
    features: '红色记忆追溯+对话人文山川',
    cover: 'https://via.placeholder.com/400x200?text=Red+Culture',
    transportInfo: '自驾或包车，全程约2-3天',
    accommodation: '宝兴县城周边酒店',
    foodRecommendation: '红军餐、山野菜、高山蔬菜等'
  },
  {
    id: 4,
    name: '川西古镇风情游',
    description: '循着古镇题材影视作品的拍摄轨迹，将上里古镇、望鱼古镇等串联起来。',
    theme: '古镇文化',
    features: '古镇休闲+民俗体验',
    cover: 'https://via.placeholder.com/400x200?text=Ancient+Town',
    transportInfo: '自驾或包车，全程约2天',
    accommodation: '古镇内特色客栈',
    foodRecommendation: '古镇特色小吃、手工豆制品等'
  },
      {
        id: 5,
        name: '峡谷秘境探险游',
        description: '依据在峡谷取景的影视作品，串联大渡河峡谷、二郎山喇叭河等景点。',
        theme: '峡谷探险',
        features: '原生态美景+峡谷探秘',
        cover: 'https://via.placeholder.com/400x200?text=Canyon+Adventure',
        transportInfo: '自驾或包车，全程约2-3天',
        accommodation: '峡谷周边度假村',
        foodRecommendation: '河鱼、野生菌、山野菜等'
      }
      ]
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      // 模拟网络延迟
      await new Promise(resolve => setTimeout(resolve, 500))

      let filtered = this.mockRoutes
      if (this.keyword) {
        filtered = this.mockRoutes.filter(route =>
          route.name.includes(this.keyword) || route.theme.includes(this.keyword)
        )
      }

      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      this.routes = filtered.slice(0, end)
      this.hasMore = end < filtered.length

      this.loading = false
    },
    handleSearch() {
      this.currentPage = 1
      this.loadData()
    },
    async handleRefresh() {
      this.refreshing = true
      this.currentPage = 1
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
  padding: 20rpx 32rpx;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
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
  gap: 16rpx;
}

.route-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 20rpx;
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
  width: 140rpx;
  height: 140rpx;
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
  margin-bottom: 0;
}

.route-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  word-break: break-all;
}

.route-meta {
  font-size: 22rpx;
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

