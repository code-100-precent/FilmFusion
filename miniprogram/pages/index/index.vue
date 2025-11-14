<template>
  <view class="index-page">
    <!-- 自定义导航栏 -->
    <NavBar title="雅安影视服务" :show-back="false"></NavBar>

    <!-- 内容区域 -->
    <scroll-view class="content" scroll-y @scrolltolower="onScrollToLower">
      <!-- Banner轮播 -->
      <view class="banner-section">
        <swiper
          class="banner-swiper"
          :indicator-dots="true"
          :autoplay="true"
          :interval="4000"
          :duration="500"
          :circular="true"
          indicator-color="rgba(255,255,255,0.5)"
          indicator-active-color="#fff"
        >
          <swiper-item v-for="(item, index) in banners" :key="index">
            <view class="banner-item" :style="{ background: item.bg }">
              <text class="banner-title">{{ item.title }}</text>
              <text class="banner-desc">{{ item.desc }}</text>
            </view>
          </swiper-item>
        </swiper>
      </view>

      <!-- 功能入口 -->
      <view class="function-section">
        <view class="section-title">快速入口</view>
        <view class="function-grid">
          <view
            v-for="(item, index) in functions"
            :key="index"
            class="function-item"
            @click="handleFunctionClick(item)"
          >
            <view class="function-icon" :style="{ background: item.bgColor }">
              <uni-icons :type="item.icon" :color="item.color" size="32"></uni-icons>
            </view>
            <text class="function-text">{{ item.text }}</text>
          </view>
        </view>
      </view>

      <!-- 最新资讯 -->
      <view class="news-section">
        <view class="section-header">
          <view class="section-title">最新资讯</view>
          <text class="section-more" @click="goToNews">更多 ></text>
        </view>
        <view v-if="loading" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="articles.length === 0" class="empty-wrapper">
          <Empty text="暂无资讯"></Empty>
        </view>
        <view v-else class="article-list">
          <view
            v-for="article in articles"
            :key="article.id"
            class="article-item"
            @click="goToArticleDetail(article.id)"
          >
            <view class="article-content">
              <text class="article-title">{{ article.title }}</text>
              <text class="article-meta">{{ article.issueUnit }} · {{ formatDate(article.issueTime) }}</text>
            </view>
            <uni-icons type="right" size="16" color="#9ca3af"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 推荐场地 -->
      <view class="location-section">
        <view class="section-header">
          <view class="section-title">推荐场地</view>
          <text class="section-more" @click="goToLocations">更多 ></text>
        </view>
        <view v-if="loading" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="locations.length === 0" class="empty-wrapper">
          <Empty text="暂无场地"></Empty>
        </view>
        <scroll-view v-else class="location-scroll" scroll-x>
          <view class="location-list">
            <view
              v-for="location in locations"
              :key="location.id"
              class="location-card"
              @click="goToLocationDetail(location.id)"
            >
              <view class="location-header">
                <text class="location-name">{{ location.name }}</text>
                <view class="location-badge">{{ location.type }}</view>
              </view>
              <text class="location-desc">{{ location.locationDescription }}</text>
              <view class="location-footer">
                <text class="location-price">¥{{ location.price }}/天</text>
                <text class="location-address">{{ location.address }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 底部间距 -->
      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- 底部导航栏 -->
    <TabBar :current="'index'"></TabBar>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import TabBar from '../../components/TabBar/TabBar.vue'
import Loading from '../../components/Loading/Loading.vue'
import Empty from '../../components/Empty/Empty.vue'
import { getArticlePage, getLocationPage } from '../../services/api'

export default {
  components: {
    NavBar,
    TabBar,
    Loading,
    Empty
  },
  data() {
    return {
      loading: false,
      banners: [
        {
          title: '雅安影视服务',
          desc: '专业影视拍摄一站式服务平台',
          bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
        },
        {
          title: '发现精彩取景点',
          desc: '探索雅安最美拍摄场景',
          bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
        },
        {
          title: '专业协拍服务',
          desc: '提供全方位影视制作支持',
          bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
        }
      ],
      functions: [
        { icon: 'location', text: '拍摄场地', color: '#6366f1', bgColor: '#eef2ff', path: '/pages/scenes/scenes' },
        { icon: 'calendar', text: '剧组报备', color: '#8b5cf6', bgColor: '#f3e8ff', path: '/pages/filing/filing' },
        { icon: 'phone', text: '协拍服务', color: '#ec4899', bgColor: '#fce7f3', path: '/pages/services/services' },
        { icon: 'chatbubble', text: '影视资讯', color: '#06b6d4', bgColor: '#cffafe', path: '/pages/news/news' }
      ],
      articles: [],
      locations: []
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const [articleRes, locationRes] = await Promise.all([
          getArticlePage({ current: 1, size: 5 }),
          getLocationPage({ current: 1, size: 5 })
        ])

        if (articleRes && articleRes.code === 200) {
          // 后端返回格式: { code: 200, message: "请求成功", data: [...], pagination: {...} }
          this.articles = Array.isArray(articleRes.data) ? articleRes.data : []
        }
        if (locationRes && locationRes.code === 200) {
          // 后端返回格式: { code: 200, message: "请求成功", data: [...], pagination: {...} }
          this.locations = Array.isArray(locationRes.data) ? locationRes.data : []
        }
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    handleFunctionClick(item) {
      if (item.path) {
        if (item.path === '/pages/index/index' || item.path === '/pages/profile/profile') {
          uni.switchTab({
            url: item.path
          })
        } else {
          uni.navigateTo({
            url: item.path
          })
        }
      }
    },
    goToNews() {
      uni.reLaunch({
        url: '/pages/news/news'
      })
    },
    goToLocations() {
      uni.reLaunch({
        url: '/pages/scenes/scenes'
      })
    },
    goToArticleDetail(id) {
      uni.navigateTo({
        url: `/pages/article/detail?id=${id}`
      })
    },
    goToLocationDetail(id) {
      uni.navigateTo({
        url: `/pages/location/detail?id=${id}`
      })
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      let date
      // 处理数组格式的日期（后端 LocalDateTime 序列化为数组）
      if (Array.isArray(dateStr)) {
        // 数组格式: [2024, 12, 15, 10, 0, 0]
        if (dateStr.length >= 3) {
          date = new Date(dateStr[0], dateStr[1] - 1, dateStr[2])
        } else {
          return ''
        }
      } else if (typeof dateStr === 'string') {
        date = new Date(dateStr)
      } else {
        return ''
      }
      
      if (isNaN(date.getTime())) return ''
      
      const month = date.getMonth() + 1
      const day = date.getDate()
      return `${month}月${day}日`
    },
    onScrollToLower() {
      // 可以在这里实现加载更多
    }
  }
}
</script>

<style lang="scss" scoped>
.index-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  height: calc(100vh - 132rpx - 100rpx);
  padding: 0 32rpx;
  padding-bottom: 40rpx;
  box-sizing: border-box;
  width: 100%;
}

.banner-section {
  margin: 32rpx 0 40rpx;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  width: 100%;
  box-sizing: border-box;
}

.banner-swiper {
  width: 100%;
  height: 360rpx;
  box-sizing: border-box;
}

.banner-item {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.banner-title {
  font-size: 48rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 16rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
}

.banner-desc {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.function-section {
  background: #fff;
  margin-bottom: 32rpx;
  padding: 40rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.section-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 32rpx;
}

.function-grid {
  display: flex;
  justify-content: space-between;
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.function-icon {
  width: 100rpx;
  height: 100rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.function-text {
  font-size: 26rpx;
  color: #374151;
  font-weight: 500;
}

.news-section,
.location-section {
  background: #fff;
  margin-bottom: 32rpx;
  padding: 40rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.section-more {
  font-size: 26rpx;
  color: #6366f1;
  font-weight: 500;
}

.loading-wrapper,
.empty-wrapper {
  padding: 60rpx 0;
  display: flex;
  justify-content: center;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.article-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  background: #f9fafb;
  border-radius: 16rpx;
  transition: all 0.3s;
}

.article-item:active {
  background: #f3f4f6;
  transform: translateX(4rpx);
}

.article-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.article-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
}

.article-meta {
  font-size: 24rpx;
  color: #6b7280;
}

.location-scroll {
  white-space: nowrap;
}

.location-list {
  display: flex;
  gap: 24rpx;
  padding-bottom: 20rpx;
}

.location-card {
  width: 480rpx;
  padding: 32rpx;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  border-radius: 20rpx;
  border: 2rpx solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.location-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.location-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
}

.location-badge {
  padding: 8rpx 16rpx;
  background: #6366f1;
  color: #fff;
  font-size: 22rpx;
  border-radius: 8rpx;
}

.location-desc {
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.location-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8rpx;
}

.location-price {
  font-size: 28rpx;
  font-weight: 600;
  color: #f59e0b;
}

.location-address {
  font-size: 24rpx;
  color: #9ca3af;
}

.bottom-spacer {
  height: 40rpx;
}
</style>
