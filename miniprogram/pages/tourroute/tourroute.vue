<template>
  <view class="tourroute-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>

    <!-- 自定义返回按钮 -->
    <view class="custom-header">
      <view class="back-button" @click="handleBack">
        <uni-icons type="left" color="white" size="20" />
      </view>
    </view>

    <view class="content">
      <!-- 页面标题 -->
      <view class="page-title">
        <text class="title-text">跟着影视游雅安</text>
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
      <scroll-view
        class="route-list"
        scroll-y
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="handleRefresh"
      >
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
            class="route-card"
            @click="goToDetail(route.id)"
          >
            <view class="route-cover-wrapper">
              <image :src="route.cover || defaultCover" class="route-cover" mode="aspectFill"></image>
              <view class="route-tag">{{ route.theme }}</view>
            </view>
            <view class="route-info">
              <view class="route-header">
                <text class="route-title">{{ route.name }}</text>
              </view>
              <text class="route-description">{{ route.description }}</text>
              <view class="route-features">
                <text class="feature-tag">{{ route.features }}</text>
              </view>
              <view class="route-footer">
                <text class="view-more">查看详情</text>
                <uni-icons type="right" size="14" color="#6366f1"></uni-icons>
              </view>
            </view>
          </view>
        </view>

        <view v-if="hasMore && !loading" class="load-more">
          <text>上拉加载更多</text>
        </view>
        <view v-if="!hasMore && routes.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
      </scroll-view>
    </view>

    <!-- 底部导航栏 -->
    <TabBar :current="'tourroute'"></TabBar>
  </view>
</template>

<script>
import TabBar from '@/components/TabBar/TabBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'

export default {
  components: {
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
    handleBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack()
      } else {
        uni.reLaunch({ url: '/pages/index/index' })
      }
    },
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

<style scoped lang="scss">
.tourroute-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f8fafc;
  position: relative;
}

.gradient-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 0;
}

.custom-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 44px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  z-index: 100;
  padding-top: env(safe-area-inset-top);
}

.back-button {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.back-button:active {
  background: rgba(255, 255, 255, 0.2);
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
  overflow: hidden;
  padding-top: 44px;
}

.page-title {
  padding: 20px 16px 10px;
  text-align: center;
}

.title-text {
  font-size: 28px;
  font-weight: bold;
  color: white;
}

.search-bar {
  padding: 0 16px 16px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 8px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-input {
  flex: 1;
  margin-left: 8px;
  font-size: 14px;
  color: #333;
}

.route-list {
  flex: 1;
  padding: 0 16px;
}

.route-card {
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.route-card:active {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.route-cover-wrapper {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.route-cover {
  width: 100%;
  height: 100%;
}

.route-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(99, 102, 241, 0.9);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.route-info {
  padding: 16px;
}

.route-header {
  margin-bottom: 8px;
}

.route-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  display: block;
}

.route-description {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  display: block;
  margin-bottom: 12px;
}

.route-features {
  margin-bottom: 12px;
}

.feature-tag {
  display: inline-block;
  background: #f3f4f6;
  color: #6366f1;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.route-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  color: #6366f1;
  font-size: 13px;
}

.view-more {
  margin-right: 4px;
}

.loading-wrapper,
.empty-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.load-more,
.no-more {
  text-align: center;
  padding: 20px;
  color: #9ca3af;
  font-size: 13px;
}
</style>

