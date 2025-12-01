<template>
  <view class="index-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>
    
    <NavBar :show-back="false"></NavBar>

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
        <scroll-view class="function-scroll" scroll-x :show-scrollbar="false" :enable-flex="true">
          <view class="function-grid">
            <view
              v-for="(item, index) in functions"
              :key="index"
              class="function-item"
              @click="handleFunctionClick(item)"
            >
              <view class="function-icon" :style="{ background: item.bgColor }">
                <uni-icons :type="item.icon" :color="item.color" size="30"></uni-icons>
              </view>
              <text class="function-text">{{ item.text }}</text>
              <text class="function-desc">{{ item.desc }}</text>
            </view>
          </view>
        </scroll-view>
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
            <view class="article-cover">
              <image :src="getArticleCover(article.cover)" class="cover-image" mode="aspectFill"></image>
            </view>
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
        <scroll-view 
          v-else 
          class="location-scroll" 
          scroll-x 
          :show-scrollbar="false"
          :enable-flex="true"
        >
          <view class="location-list">
            <view
              v-for="location in locations"
              :key="location.id"
              class="location-card"
              @click="goToLocationDetail(location.id)"
            >
              <view v-if="location.cover" class="location-cover">
                <image :src="location.cover" class="cover-image" mode="aspectFill"></image>
              </view>
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
// 使用真实后端API
import { getArticlePage, getLocationPage } from '../../services/backend-api'
// 使用加载状态管理Mixin
import { loadingMixin } from '../../utils/index'

export default {
  components: {
    NavBar,
    TabBar,
    Loading,
    Empty
  },
  mixins: [loadingMixin],
  data() {
    return {
      banners: [
        {
          title: '雅安影视服务',
          desc: '专业影视拍摄一站式服务平台',
          bg: 'url("https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/%E8%B5%B5%E6%AF%85%E2%80%94%E2%80%94%E3%80%8A%E8%90%A8%E9%87%8C%E5%AE%89%E5%A4%9A%E6%9B%BC%E3%80%8B%E2%80%94%E2%80%94%E7%9F%B3%E6%A3%89%E5%8E%BF%E8%9F%B9%E8%9E%BA%E8%97%8F%E6%97%8F%E4%B9%A1%E6%B1%9F%E5%9D%9D%E6%9D%91%EF%BC%8C%E5%B0%94%E8%8B%8F%E8%97%8F%E6%97%8F%E5%A6%87%E5%A5%B3%E8%B7%B3%E8%B5%B7%E6%AC%A2%E5%BF%AB%E7%9A%84%E6%AD%8C%E8%88%9E%E2%80%9C%E8%90%A8%E9%87%8C%E5%AE%89%E5%A4%9A%E6%9B%BC%E2%80%9D%E3%80%82%E6%AF%8F%E9%80%A2%E8%8A%82%E6%97%A5%E5%BA%86%E5%85%B8%E6%88%96%E7%A5%AD%E7%A5%80%E6%B4%BB%E5%8A%A8%EF%BC%8C%E5%B0%94%E8%8B%8F%E8%97%8F%E6%97%8F%E9%83%BD%E4%BC%9A%E4%BB%A5%E6%AD%8C%E8%88%9E%E5%BD%A2%E5%BC%8F%E8%A1%A8%E8%BE%BE%E5%96%9C%E6%82%A6%E5%BF%83%E6%83%85%EF%BC%8C%E2%80%9C%E8%90%A8%E9%87%8C%E5%AE%89%E5%A4%9A%E6%9B%BC%EF%BC%88%E6%84%8F%E6%80%9D%E6%98%AF%E5%A4%A7%E5%AE%B6%E5%94%B1%E8%B5%B7%E6%9D%A5%E3%80%81%E8%B7%B3%E8%B5%B7%E6%9D%A5%EF%BC%89%E2%80%9D%E5%B0%B1%E6%98%AF%E5%85%B6%E4%B8%AD%E4%B9%8B%E4%B8%80%EF%BC%8C%E8%BF%99%E6%98%AF%E5%B0%94%E8%8B%8F%E8%97%8F%E6%97%8F%E5%85%88%E6%B0%91%E7%95%99%E7%BB%99%E5%90%8E%E4%BA%BA%E7%9A%84%E7%8F%8D%E8%B4%B5%E6%96%87%E5%8C%96%E9%81%97%E4%BA%A7%E3%80%82%E2%80%94%E2%80%9413608260099.jpg") center/cover no-repeat'
        },
        {
          title: '发现精彩取景点',
          desc: '探索雅安最美拍摄场景',
          bg: 'url("https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/%E5%AE%8B%E6%9C%89%E5%AE%8F-%E3%80%8A%E6%AD%A3%E6%98%AF%E9%87%87%E8%8C%B6%E5%AD%A3%E3%80%8B%2B%E3%80%8122%E5%B9%B4%E6%8B%8D%E4%BA%8E%E9%9B%85%E5%AE%89%E5%90%8D%E5%B1%B1%E5%8C%BA%E7%BA%A2%E6%98%9F%E9%95%87.JPG") center/cover no-repeat'
        },
        {
          title: '专业协拍服务',
          desc: '提供全方位影视制作支持',
          bg: 'url("https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251124103009.jpg") center/cover no-repeat'
        }
      ],
      functions: [
        { icon: 'videocam', text: '光影雅安', desc: '影视作品展示', color: '#ef4444', bgColor: 'linear-gradient(135deg, #fecaca 0%, #fca5a5 100%)', path: '/pages/films/films' },
        { icon: 'location', text: '拍摄场地', desc: '寻找完美取景地', color: '#f59e0b', bgColor: 'linear-gradient(135deg, #fef3c7 0%, #fed7aa 100%)', path: '/pages/scenes/scenes' },
        { icon: 'calendar', text: '剧组报备', desc: '手续办理更便捷', color: '#8b5cf6', bgColor: 'linear-gradient(135deg, #f3e8ff 0%, #ddd6fe 100%)', path: '/pages/filing/filing' },
        { icon: 'phone', text: '协拍服务', desc: '专业团队支持', color: '#ec4899', bgColor: 'linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%)', path: '/pages/services/services' },
        { icon: 'chatbubble', text: '影视资讯', desc: '掌握行业动态', color: '#06b6d4', bgColor: 'linear-gradient(135deg, #cffafe 0%, #a5f3fc 100%)', path: '/pages/news/news' },
        { icon: 'map', text: '跟着影视游', desc: '探寻影视足迹', color: '#10b981', bgColor: 'linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%)', path: '/pages/tourroute/tourroute' },
        { icon: 'info', text: '视听政策', desc: '了解扶持政策', color: '#3b82f6', bgColor: 'linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%)', path: '/pages/policy/policy' }
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
      try {
        await this.withLoading(async () => {
          const [articleRes, locationRes] = await Promise.all([
            getArticlePage({ current: 1, size: 5 }),
            getLocationPage({ current: 1, size: 5 })
          ])

          // 处理文章数据 - 游标分页格式
          if (articleRes && articleRes.records) {
            this.articles = Array.isArray(articleRes.records) ? articleRes.records : []
          }
          // 处理场地数据 - 游标分页格式
          if (locationRes && locationRes.records) {
            this.locations = Array.isArray(locationRes.records) ? locationRes.records : []
          }
        })
      } catch (error) {
        console.error('加载数据失败:', error)
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
    getArticleCover(cover) {
      // 如果 cover 为 null、空或包含 example.com（测试URL），使用默认图片
      return (!cover || cover.includes('example.com')) ? 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/%E6%8B%8D%E5%9C%A8%E9%9B%85%E5%AE%89.png' : cover
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
      } else {
        date = new Date(dateStr)
      }
      
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
  padding-top: 20rpx;
  
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

.banner-section {
  margin: 24rpx 0 32rpx;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  width: 100%;
  box-sizing: border-box;
}

.banner-swiper {
  width: 100%;
  height: 320rpx;
  box-sizing: border-box;
}

.banner-item {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20rpx;  /* 减少内边距 */
  position: relative;
}

.banner-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1;
}

.banner-title,
.banner-desc {
  position: relative;
  z-index: 2;
}

.banner-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 16rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
  text-align: center;
}

.banner-desc {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
}

.function-section {
  background: #fff;
  margin-bottom: 24rpx;
  padding: 24rpx 16rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

/* 移除重复的样式定义 */

.function-scroll {
  width: 100%;
  white-space: nowrap;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;

  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.function-grid {
  display: inline-flex;
  gap: 12rpx;
  padding: 0 16rpx;
  white-space: nowrap;
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  min-width: 140rpx;
  width: 140rpx;
  padding: 16rpx 8rpx;
  border-radius: 16rpx;
  transition: all 0.3s ease;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.9) 100%);
  border: 1rpx solid rgba(229, 231, 235, 0.5);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
  white-space: normal;
}

.function-item:active {
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.12);
}

.function-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
  margin-bottom: 4rpx;
}

.function-text {
  font-size: 24rpx;
  color: #374151;
  font-weight: 600;
  text-align: center;
  line-height: 1.2;
}

.function-desc {
  font-size: 20rpx;
  color: #6b7280;
  text-align: center;
  line-height: 1.2;
  margin-top: 2rpx;
}

.news-section,
.location-section {
  background: #fff;
  margin-bottom: 24rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.news-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.location-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.section-more {
  font-size: 24rpx;
  color: #6366f1;
  font-weight: 500;
  line-height: 1.2;
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
  gap: 16rpx;
}

.article-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 16rpx;
  background: #f9fafb;
  border-radius: 12rpx;
  transition: all 0.3s;
  overflow: hidden;
}

.article-item:active {
  background: #f3f4f6;
  transform: translateX(4rpx);
}

.article-cover {
  width: 120rpx;
  height: 120rpx;
  border-radius: 10rpx;
  overflow: hidden;
  flex-shrink: 0;
  background: #f3f4f6;
}

.article-cover .cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
  justify-content: center;
}

.article-item > uni-icons {
  flex-shrink: 0;
  align-self: center;
  margin-top: 0;
}

.article-title {
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

.article-meta {
  font-size: 22rpx;
  color: #9ca3af;
  line-height: 1.4;
}

.location-scroll {
  width: 100%;
  white-space: nowrap;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  
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
  display: inline-flex;
  gap: 24rpx;
  padding: 0 0 20rpx 0;
  white-space: nowrap;
}

.location-card {
  width: 440rpx;
  min-width: 440rpx;
  padding: 0;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  border-radius: 20rpx;
  border: 2rpx solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 0;
  overflow: hidden;
  flex-shrink: 0;
}

.location-cover {
  width: 100%;
  height: 220rpx;
  overflow: hidden;
}

.location-cover .cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.location-card > .location-header,
.location-card > .location-desc,
.location-card > .location-footer {
  padding: 0 24rpx;
}

.location-card > .location-header {
  padding-top: 16rpx;
  padding-bottom: 8rpx;
}

.location-card > .location-desc {
  padding-bottom: 8rpx;
}

.location-card > .location-footer {
  padding-bottom: 16rpx;
  padding-top: 8rpx;
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


