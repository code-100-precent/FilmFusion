<template>
  <view class="index-page">
    <!-- 顶部导航栏 -->
    <NavBar :show-back="false" background="transparent" :shadow="false" class="custom-nav">
      <view class="nav-title">
        <text class="nav-main">拍在雅安</text>
        <text class="nav-sub">FILM IN YA'AN</text>
      </view>
    </NavBar>

    <scroll-view class="content" scroll-y @scrolltolower="onScrollToLower" enhanced :bounces="true">
      <!-- 创意Banner区域 -->
      <view class="creative-banner">
        <swiper
          class="banner-swiper"
          :indicator-dots="true"
          :autoplay="true"
          :interval="5000"
          :duration="1000"
          :circular="true"
          indicator-color="rgba(255,255,255,0.3)"
          indicator-active-color="#D4AF37"
          @change="onBannerChange"
        >
          <swiper-item v-for="(item, index) in banners" :key="index">
            <view class="banner-item">
              <image :src="item.imageUrl" class="banner-image" mode="cover"></image>
              <view class="banner-overlay"></view>
              <view class="banner-content">
                <view class="banner-tag">{{ index === 0 ? '平台服务' : index === 1 ? '取景胜地' : '专业支持' }}</view>
                <text class="banner-title">{{ item.title }}</text>
                <text class="banner-desc">{{ item.desc }}</text>
                <view class="banner-cta" @click="handleBannerClick(item)">
                  <text>立即了解</text>
                  <uni-icons type="arrowright" size="18" color="#1B3C35"></uni-icons>
                </view>
              </view>
            </view>
          </swiper-item>
        </swiper>
      </view>

      <!-- 创意功能入口 -->
      <view class="creative-functions">
        <view class="section-header">
          <text class="section-title">核心服务</text>
        </view>
        <view class="function-grid">
          <view
            v-for="(item, index) in functions"
            :key="index"
            class="function-card"
            :style="{ '--card-index': index }"
            @click="handleFunctionClick(item)"
          >
            <view class="function-icon" :style="{ background: item.bgColor, color: item.color }">
              <uni-icons :type="item.icon" size="36"></uni-icons>
            </view>
            <view class="function-info">
              <text class="function-title">{{ item.text }}</text>
              <text class="function-desc">{{ item.desc }}</text>
            </view>
            <uni-icons type="arrowright" size="18" color="#999" class="function-arrow"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 双栏内容区域 -->
      <view class="dual-section">
        <!-- 左侧：影视资讯 -->
        <view class="dual-column left-column">
          <view class="section-header small-header">
            <text class="section-title">影视资讯</text>
            <view class="section-more" @click="goToNews">
              <text>全部</text>
              <uni-icons type="arrowright" size="14" color="#666"></uni-icons>
            </view>
          </view>
          
          <view v-if="loading" class="loading-wrapper">
            <Loading></Loading>
          </view>
          <view v-else-if="articles.length === 0" class="empty-wrapper">
            <Empty text="暂无资讯"></Empty>
          </view>
          <view v-else class="news-list">
            <view
              v-for="(article, index) in articles.slice(0, 3)"
              :key="article.id"
              class="news-item"
              @click="goToArticleDetail(article.id)"
            >
              <view class="news-item-header">
                <text class="news-item-tag">{{ article.issueUnit || '官方发布' }}</text>
                <text class="news-item-date">{{ formatDate(article.issueTime) }}</text>
              </view>
              <text class="news-item-title">{{ article.title }}</text>
              <image v-if="index === 0" :src="getArticleCover(article.cover)" class="news-item-image" mode="aspectFill"></image>
            </view>
          </view>
        </view>

        <!-- 右侧：热门取景地 -->
        <view class="dual-column right-column">
          <view class="section-header small-header">
            <text class="section-title">热门取景</text>
            <view class="section-more" @click="goToLocations">
              <text>探索</text>
              <uni-icons type="arrowright" size="14" color="#666"></uni-icons>
            </view>
          </view>
          
          <view v-if="loading" class="loading-wrapper">
            <Loading></Loading>
          </view>
          <view v-else-if="locations.length === 0" class="empty-wrapper">
            <Empty text="暂无场地"></Empty>
          </view>
          <view v-else class="location-grid">
            <view
              v-for="location in locations.slice(0, 2)"
              :key="location.id"
              class="location-item"
              @click="goToLocationDetail(location.id)"
            >
              <view class="location-item-image-box">
                <image :src="location.cover" class="location-item-image" mode="cover"></image>
                <view class="location-item-price" v-if="location.price">¥{{ location.price }}/天</view>
              </view>
              <view class="location-item-info">
                <text class="location-item-name">{{ location.name }}</text>
                <text class="location-item-type">{{ location.type }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 特色主题区域 -->
      <view class="featured-section">
        <view class="section-header">
          <text class="section-title">雅安印象</text>
        </view>
        <view class="featured-grid">
          <view class="featured-card nature" @click="goToNature">
            <view class="featured-icon">
              <uni-icons type="tree" size="40" color="#fff"></uni-icons>
            </view>
            <text class="featured-title">自然风光</text>
            <text class="featured-desc">探索自然之美</text>
          </view>
          <view class="featured-card culture" @click="goToCulture">
            <view class="featured-icon">
              <uni-icons type="book" size="40" color="#fff"></uni-icons>
            </view>
            <text class="featured-title">文化底蕴</text>
            <text class="featured-desc">感受历史魅力</text>
          </view>
          <view class="featured-card modern" @click="goToModern">
            <view class="featured-icon">
              <uni-icons type="building" size="40" color="#fff"></uni-icons>
            </view>
            <text class="featured-title">现代都市</text>
            <text class="featured-desc">体验城市活力</text>
          </view>
        </view>
      </view>

      <!-- 底部区域 -->
      <view class="footer-section">
        <view class="footer-content">
          <text class="footer-title">专业影视拍摄服务平台</text>
          <text class="footer-subtitle">为您提供一站式影视拍摄解决方案</text>
          <view class="footer-cta" @click="goToServices">
            <text>联系我们</text>
          </view>
        </view>
      </view>

      <!-- 底部间距 -->
      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- 底部导航栏 -->
    <TabBar :current="'index'" class="custom-tabbar"></TabBar>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import TabBar from '../../components/TabBar/TabBar.vue'
import Loading from '../../components/Loading/Loading.vue'
import Empty from '../../components/Empty/Empty.vue'
import { getArticlePage, getLocationPage } from '../../services/backend-api'
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
          imageUrl: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/%E8%B5%B5%E6%AF%8F%E2%80%94%E2%80%94%E3%80%8A%E8%90%A8%E9%87%8C%E5%AE%89%E5%A4%9A%E6%9B%BC%E3%80%8B%E2%80%94%E2%80%94%E7%9F%B3%E6%A3%89%E5%8E%BF%E8%9F%B9%E8%9E%BA%E8%97%8F%E6%97%8F%E4%B9%A1%E6%B1%9F%E5%9D%9D%E6%9D%91%EF%BC%8C%E5%B0%94%E8%8B%8F%E8%97%8F%E6%97%8F%E5%A6%87%E5%A5%B3%E8%B7%B3%E8%B5%B7%E6%AC%A2%E5%BF%AB%E7%9A%84%E6%AD%8C%E8%88%96%E2%80%9C%E8%90%A8%E9%87%8C%E5%AE%89%E5%A4%9A%E6%9B%BC%E2%80%9D%E3%80%82%E6%AF%8F%E9%80%A2%E8%8A%82%E6%97%A5%E5%BA%86%E5%85%B8%E6%88%96%E7%A5%AD%E7%A5%80%E6%B4%BB%E5%8A%A8%EF%BC%8C%E5%B0%94%E8%8B%8F%E8%97%8F%E6%97%8F%E9%83%BD%E4%BC%9A%E4%BB%A5%E6%AD%8C%E8%88%96%E5%BD%A2%E5%BC%8F%E8%A1%A8%E8%BE%BE%E5%96%9C%E6%82%A6%E5%BF%83%E6%83%85%EF%BC%8C%E2%80%9C%E8%90%A8%E9%87%8C%E5%AE%89%E5%A4%9A%E6%9B%BC%EF%BC%88%E6%84%8F%E6%80%9D%E6%98%AF%E5%A4%A7%E5%AE%B6%E5%94%B1%E8%B5%B7%E6%9D%A5%E3%80%81%E8%B7%B3%E8%B5%B7%E6%9D%A5%EF%BC%89%E2%80%9D%E5%B0%B1%E6%98%AF%E5%85%B6%E4%B8%AD%E4%B9%8B%E4%B8%80%EF%BC%8C%E8%BF%99%E6%98%AF%E5%B0%94%E8%8B%8F%E8%97%8F%E6%97%8F%E5%85%88%E6%B0%91%E7%95%99%E7%BB%99%E5%90%8E%E4%BA%A7%E5%9C%88%E5%85%B6%E4%B8%AD%E4%B9%8B%E4%B8%80%EF%BC%8C%E8%BF%99%E6%98%AF%E5%B0%94%E8%8B%8F%E8%97%8F%E6%97%8F%E5%85%88%E6%B0%91%E7%95%99%E7%BB%99%E5%90%8E%E4%BA%A7%E7%9A%84%E7%8F%8D%E8%B4%B5%E6%96%87%E5%8C%96%E9%81%97%E4%BA%A7%E3%80%82%E2%80%94%E2%80%9413608260099.jpg',
          path: '/pages/services/services'
        },
        {
          title: '发现精彩取景点',
          desc: '探索雅安最美拍摄场景',
          imageUrl: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/%E5%AE%8B%E6%9C%89%E5%AE%89-%E3%80%8A%E6%AD%A3%E6%98%AF%E9%87%87%E8%8C%B6%E5%AD%A3%E3%80%8B%2B%E3%80%8122%E5%B9%B4%E6%8B%8D%E4%BA%8E%E9%9B%85%E5%AE%89%E5%90%8D%E5%B1%B1%E5%8C%BA%E7%BA%A2%E6%98%9F%E9%95%87.JPG',
          path: '/pages/scenes/scenes'
        },
        {
          title: '专业协拍服务',
          desc: '提供全方位影视制作支持',
          imageUrl: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251124103009.jpg',
          path: '/pages/filming/filming'
        }
      ],
      functions: [
        { icon: 'videocam', text: '光影雅安', desc: '影视作品', color: '#D4AF37', bgColor: 'rgba(212, 175, 55, 0.15)', path: '/pages/films/films' },
        { icon: 'location', text: '拍摄场地', desc: '完美取景', color: '#2E7D32', bgColor: 'rgba(46, 125, 50, 0.15)', path: '/pages/scenes/scenes' },
        { icon: 'calendar', text: '剧组报备', desc: '便捷手续', color: '#1B3C35', bgColor: 'rgba(27, 60, 53, 0.15)', path: '/pages/filing/filing' },
        { icon: 'phone', text: '协拍服务', desc: '专业支持', color: '#C62828', bgColor: 'rgba(198, 40, 40, 0.15)', path: '/pages/services/services' },
        { icon: 'chatbubble', text: '影视资讯', desc: '行业动态', color: '#00838F', bgColor: 'rgba(0, 131, 143, 0.15)', path: '/pages/news/news' },
        { icon: 'map', text: '影视游', desc: '探寻足迹', color: '#558B2F', bgColor: 'rgba(85, 139, 47, 0.15)', path: '/pages/tourroute/tourroute' }
      ],
      articles: [],
      locations: [],
      currentBanner: 0
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

          if (articleRes && articleRes.records) {
            this.articles = Array.isArray(articleRes.records) ? articleRes.records : []
          }
          if (locationRes && locationRes.records) {
            this.locations = Array.isArray(locationRes.records) ? locationRes.records : []
          }
        })
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    },
    onBannerChange(e) {
      this.currentBanner = e.detail.current
    },
    handleBannerClick(item) {
      if (item.path) {
        uni.navigateTo({
          url: item.path
        })
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
    goToNature() {
      uni.navigateTo({
        url: '/pages/scenes/scenes?type=自然风光'
      })
    },
    goToCulture() {
      uni.navigateTo({
        url: '/pages/scenes/scenes?type=文化底蕴'
      })
    },
    goToModern() {
      uni.navigateTo({
        url: '/pages/scenes/scenes?type=现代都市'
      })
    },
    goToServices() {
      uni.navigateTo({
        url: '/pages/services/services'
      })
    },
    getArticleCover(cover) {
      return (!cover || cover.includes('example.com')) ? 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/%E6%8B%8D%E5%9C%A8%E9%9B%85%E5%AE%89.png' : cover
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      let date
      if (Array.isArray(dateStr)) {
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
  background: linear-gradient(180deg, #F8FAFB 0%, #EDF3F5 100%);
  position: relative;
}

/* 导航样式 */
.custom-nav {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.nav-title {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.nav-main {
  font-size: 38rpx;
  font-weight: 800;
  color: #FFFFFF;
  letter-spacing: 3rpx;
  text-shadow: 0 3rpx 12rpx rgba(0, 0, 0, 0.4);
}

.nav-sub {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.95);
  letter-spacing: 5rpx;
  margin-top: 2rpx;
  font-weight: 600;
  text-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.2);
}

.content {
  position: relative;
  height: 100vh;
  padding-top: calc(88rpx + env(safe-area-inset-top)); /* 避开导航栏 */
  box-sizing: border-box;
}

/* 创意Banner样式 - 优化更紧凑 */
.creative-banner {
  margin: -20rpx -30rpx 28rpx;
  overflow: hidden;
}

.banner-swiper {
  height: 360rpx;
}

.banner-item {
  position: relative;
  height: 100%;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
  transition: transform 3s ease;
  filter: brightness(0.95);
}

.banner-swiper .swiper-item-active .banner-image {
  transform: scale(1.08);
  filter: brightness(1);
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.15) 0%, rgba(0, 0, 0, 0.65) 100%);
}

.banner-content {
  position: absolute;
  bottom: 48rpx;
  left: 48rpx;
  right: 48rpx;
  z-index: 2;
  animation: bannerFadeIn 0.8s ease-out;
}

@keyframes bannerFadeIn {
  from {
    opacity: 0;
    transform: translateY(24rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.banner-tag {
  display: inline-block;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.95), rgba(212, 175, 55, 0.85));
  color: #1B3C35;
  padding: 6rpx 18rpx;
  border-radius: 18rpx;
  font-size: 20rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
  backdrop-filter: blur(10rpx);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
}

.banner-title {
  font-size: 42rpx;
  font-weight: 800;
  color: #FFFFFF;
  text-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.4);
  display: block;
  margin-bottom: 12rpx;
  line-height: 1.3;
  letter-spacing: 1rpx;
}

.banner-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
  display: block;
  margin-bottom: 20rpx;
  line-height: 1.5;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
}

.banner-cta {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  background: linear-gradient(135deg, #FFFFFF, #F5F5F5);
  color: #1B3C35;
  padding: 10rpx 28rpx;
  border-radius: 28rpx;
  font-size: 24rpx;
  font-weight: 700;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.25);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.banner-cta:active {
  transform: scale(0.95);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.2);
}

/* 创意功能入口 - 优化更紧凑 */
.creative-functions {
  margin: 0 24rpx 28rpx;
}

.section-header {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 24rpx;
  position: relative;
}

.small-header {
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #1B3C35;
  position: relative;
  padding: 0 28rpx;
  letter-spacing: 1rpx;
}

.section-title::before,
.section-title::after {
  content: '';
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48rpx;
  height: 3rpx;
  background: linear-gradient(90deg, transparent, #D4AF37, transparent);
}

.section-title::before {
  left: 0;
}

.section-title::after {
  right: 0;
}

.small-header .section-title {
  font-size: 30rpx;
  padding: 0;
}

.small-header .section-title::before,
.small-header .section-title::after {
  display: none;
}

.section-more {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 24rpx;
  color: #666;
  font-weight: 500;
  transition: all 0.3s ease;
}

.section-more:active {
  color: #D4AF37;
}

.function-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.function-card {
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFBFC 100%);
  border-radius: 18rpx;
  padding: 24rpx 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1rpx solid rgba(27, 60, 53, 0.08);
  display: flex;
  align-items: center;
  gap: 16rpx;
  position: relative;
  overflow: hidden;
}

.function-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 5rpx;
  height: 100%;
  background: linear-gradient(180deg, #D4AF37 0%, #2E7D32 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.function-card:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 14rpx rgba(0, 0, 0, 0.08);
}

.function-card:active::before {
  opacity: 1;
}

.function-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.function-card:active .function-icon {
  transform: scale(1.08) rotate(5deg);
}

.function-info {
  flex: 1;
  min-width: 0;
}

.function-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1B3C35;
  display: block;
  margin-bottom: 6rpx;
  letter-spacing: 0.5rpx;
}

.function-desc {
  font-size: 22rpx;
  color: #888;
  display: block;
  line-height: 1.4;
}

.function-arrow {
  opacity: 0.5;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.function-card:active .function-arrow {
  transform: translateX(6rpx);
  opacity: 1;
  color: #D4AF37;
}

/* 双栏内容区域 - 优化布局 */
.dual-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin: 0 24rpx 28rpx;
}

.dual-column {
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFBFC 100%);
  border-radius: 18rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(27, 60, 53, 0.08);
}

.loading-wrapper,
.empty-wrapper {
  padding: 40rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 资讯列表 - 更紧凑 */
.news-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.news-item {
  background: linear-gradient(135deg, #F8FAFC 0%, #F5F7FA 100%);
  border-radius: 14rpx;
  padding: 16rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-left: 3rpx solid transparent;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.news-item:active {
  transform: translateX(6rpx);
  border-left-color: #D4AF37;
  box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.15);
}

.news-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.news-item-tag {
  font-size: 20rpx;
  color: #1B3C35;
  background: rgba(212, 175, 55, 0.12);
  padding: 4rpx 10rpx;
  border-radius: 10rpx;
  font-weight: 600;
}

.news-item-date {
  font-size: 20rpx;
  color: #999;
  font-weight: 500;
}

.news-item-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #1B3C35;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  overflow: hidden;
  letter-spacing: 0.5rpx;
}

.news-item-image {
  width: 100%;
  height: 140rpx;
  border-radius: 10rpx;
  margin-top: 12rpx;
  object-fit: cover;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

/* 场地列表 - 优化视觉效果 */
.location-grid {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.location-item {
  background: #FFFFFF;
  border-radius: 14rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 14rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1rpx solid rgba(27, 60, 53, 0.05);
}

.location-item:active {
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.1);
}

.location-item-image-box {
  position: relative;
  width: 100%;
  height: 160rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #F5F7FA, #E8F0F2);
}

.location-item-image {
  width: 100%;
  height: 100%;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  object-fit: cover;
}

.location-item:active .location-item-image {
  transform: scale(1.08);
}

.location-item-price {
  position: absolute;
  bottom: 10rpx;
  right: 10rpx;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.98), rgba(212, 175, 55, 0.92));
  color: #1B3C35;
  font-size: 22rpx;
  font-weight: 800;
  padding: 6rpx 14rpx;
  border-radius: 14rpx;
  backdrop-filter: blur(10rpx);
  box-shadow: 0 3rpx 10rpx rgba(0, 0, 0, 0.2);
}

.location-item-info {
  padding: 14rpx;
}

.location-item-name {
  font-size: 26rpx;
  font-weight: 700;
  color: #1B3C35;
  display: block;
  margin-bottom: 8rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.5rpx;
}

.location-item-type {
  font-size: 20rpx;
  color: #2E7D32;
  background: rgba(46, 125, 50, 0.1);
  padding: 4rpx 10rpx;
  border-radius: 10rpx;
  display: inline-block;
  font-weight: 600;
}

/* 特色主题区域 - 优化卡片设计 */
.featured-section {
  margin: 0 24rpx 28rpx;
}

.featured-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 14rpx;
}

.featured-card {
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFBFC 100%);
  border-radius: 18rpx;
  padding: 28rpx 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
  position: relative;
  overflow: hidden;
  border: 1rpx solid rgba(27, 60, 53, 0.08);
}

.featured-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5rpx;
  background: linear-gradient(90deg, #D4AF37, #2E7D32, #00838F);
  opacity: 0.8;
}

.featured-card:active {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.1);
}

.featured-card:active::before {
  opacity: 1;
}

.featured-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16rpx;
  background: linear-gradient(135deg, #D4AF37, #2E7D32);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4rpx 16rpx rgba(212, 175, 55, 0.3);
}

.featured-card:active .featured-icon {
  transform: scale(1.12) rotate(10deg);
  box-shadow: 0 6rpx 20rpx rgba(212, 175, 55, 0.4);
}

.featured-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #1B3C35;
  display: block;
  margin-bottom: 6rpx;
  letter-spacing: 0.5rpx;
}

.featured-desc {
  font-size: 22rpx;
  color: #888;
  display: block;
  line-height: 1.4;
  font-weight: 500;
}

/* 底部区域 - 优化渐变和间距 */
.footer-section {
  background: linear-gradient(135deg, #1B3C35 0%, #2E7D32 50%, #1B3C35 100%);
  border-radius: 24rpx;
  margin: 0 24rpx 32rpx;
  padding: 36rpx 32rpx;
  text-align: center;
  box-shadow: 0 8rpx 28rpx rgba(27, 60, 53, 0.2);
  position: relative;
  overflow: hidden;
}

.footer-section::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.1) 0%, transparent 70%);
  animation: pulse 8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  position: relative;
  z-index: 1;
}

.footer-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #FFFFFF;
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
}

.footer-subtitle {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.5;
  font-weight: 500;
}

.footer-cta {
  background: linear-gradient(135deg, #D4AF37, #C9A961);
  color: #1B3C35;
  padding: 12rpx 36rpx;
  border-radius: 28rpx;
  font-size: 24rpx;
  font-weight: 700;
  margin-top: 8rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6rpx 20rpx rgba(212, 175, 55, 0.4);
}

.footer-cta:active {
  transform: scale(0.95);
  box-shadow: 0 4rpx 14rpx rgba(212, 175, 55, 0.3);
}

/* 底部间距 */
.bottom-spacer {
  height: 100rpx;
}

/* 响应式设计 - 优化适配 */
@media screen and (max-width: 750rpx) {
  .dual-section {
    grid-template-columns: 1fr;
    gap: 20rpx;
  }

  .featured-grid {
    grid-template-columns: 1fr 1fr;
    gap: 16rpx;
  }

  .banner-swiper {
    height: 340rpx;
  }

  .banner-content {
    bottom: 36rpx;
    left: 36rpx;
    right: 36rpx;
  }

  .banner-title {
    font-size: 38rpx;
  }

  .banner-desc {
    font-size: 24rpx;
  }

  .section-title {
    font-size: 32rpx;
  }
}

@media screen and (max-width: 480rpx) {
  .function-grid {
    grid-template-columns: 1fr;
  }

  .featured-grid {
    grid-template-columns: 1fr;
  }

  .banner-swiper {
    height: 300rpx;
  }

  .banner-content {
    bottom: 28rpx;
    left: 28rpx;
    right: 28rpx;
  }

  .banner-title {
    font-size: 34rpx;
  }
}
</style>