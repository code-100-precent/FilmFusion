<template>
  <view class="index-page">
    <!-- 顶部导航栏 -->
    <view class="page-header">
      <view class="nav-title">
        <text class="nav-main">拍在雅安</text>
        <text class="nav-sub">FILM IN YA'AN</text>
      </view>
    </view>

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
            <view class="banner-item" @click="handleBannerClick(item)">
              <image :src="item.imageUrl" class="banner-image" mode="cover"></image>
              <view class="banner-overlay"></view>
              <view class="banner-content">
                <view class="banner-tag">{{ item.tag || '平台服务' }}</view>
                <text class="banner-title">{{ item.title }}</text>
                <text class="banner-desc">{{ item.desc }}</text>
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

      <!-- 影视资讯区域 -->
      <view class="content-section news-section">
        <view class="section-header small-header">
          <text class="section-title">影视资讯</text>
          <view class="section-more" @click="goToNews">
            <text>查看全部</text>
            <uni-icons type="arrowright" size="16" color="#D4AF37"></uni-icons>
          </view>
        </view>

        <view v-if="loading" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="articles.length === 0" class="empty-wrapper">
          <Empty text="暂无资讯"></Empty>
        </view>
        <view v-else class="news-cards">
          <view
            v-for="article in articles.slice(0, 3)"
            :key="article.id"
            class="news-card"
            @click="goToArticleDetail(article.id)"
          >
            <image :src="getArticleCover(article)" class="news-card-image" mode="aspectFill"></image>
            <view class="news-card-content">
              <view class="news-card-header">
                <text class="news-card-tag">{{ article.issueUnit || '官方发布' }}</text>
                <text class="news-card-date">{{ formatDate(article.issueTime) }}</text>
              </view>
              <text class="news-card-title">{{ article.title }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 热门取景地区域 -->
      <view class="content-section location-section">
        <view class="section-header small-header">
          <text class="section-title">热门取景地</text>
          <view class="section-more" @click="goToLocations">
            <text>探索更多</text>
            <uni-icons type="arrowright" size="16" color="#D4AF37"></uni-icons>
          </view>
        </view>

        <view v-if="loading" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="locations.length === 0" class="empty-wrapper">
          <Empty text="暂无场地"></Empty>
        </view>
        <view v-else class="location-cards">
          <view
            v-for="location in locations.slice(0, 4)"
            :key="location.id"
            class="location-card"
            @click="goToLocationDetail(location.id)"
          >
            <view class="location-card-image-box">
              <image 
                v-if="getFileUrl(location.cover || location.thumbImage)"
                :src="getFileUrl(location.cover || location.thumbImage)" 
                class="location-card-image" 
                mode="aspectFill"
              ></image>
              <view v-else class="location-card-overlay" style="background: #f3f4f6; display: flex; align-items: center; justify-content: center;">
                 <uni-icons type="image" size="30" color="#d1d5db"></uni-icons>
              </view>
              <view class="location-card-price" v-if="location.price">¥{{ location.price }}/天</view>
            </view>
            <view class="location-card-info">
              <text class="location-card-name">{{ location.name }}</text>
              <text class="location-card-type">{{ getLocationTypeLabel(location.type) }}</text>
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
          <view class="featured-card nature">
            <view class="featured-icon">
              <uni-icons type="image-filled" size="40" color="#fff"></uni-icons>
            </view>
            <text class="featured-title">自然风光</text>
            <text class="featured-desc">探索自然之美</text>
          </view>
          <view class="featured-card culture">
            <view class="featured-icon">
              <uni-icons type="star-filled" size="40" color="#fff"></uni-icons>
            </view>
            <text class="featured-title">文化底蕴</text>
            <text class="featured-desc">感受历史魅力</text>
          </view>
          <view class="featured-card modern">
            <view class="featured-icon">
              <uni-icons type="home-filled" size="40" color="#fff"></uni-icons>
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
    <TabBar :current="'index'"></TabBar>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import TabBar from '../../components/TabBar/TabBar.vue'
import Loading from '../../components/Loading/Loading.vue'
import Empty from '../../components/Empty/Empty.vue'
import { getArticlePage, getLocationPage, getBannerPage } from '../../services/backend-api'
import { loadingMixin, getFileUrl } from '../../utils/index'

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
      banners: [],
      // targetModule值与页面路径的映射关系
      modulePathMap: {
        '文章列表': '/pages/news/news',
        '电视剧列表': '/pages/films/films',
        '场地列表': '/pages/scenes/scenes',
        '服务列表': '/pages/services/services',
        '住宿列表': '/pages/hotel/hotel',
        '旅游线路': '/pages/tourroute/tourroute',
        '政策列表': '/pages/policy/policy'
      },
      functions: [
        { icon: 'videocam', text: '光影雅安', desc: '影视作品', color: '#D4AF37', bgColor: 'rgba(212, 175, 55, 0.15)', path: '/pages/films/films' },
        { icon: 'location', text: '拍摄场地', desc: '完美取景', color: '#2E7D32', bgColor: 'rgba(46, 125, 50, 0.15)', path: '/pages/scenes/scenes' },
        { icon: 'calendar', text: '剧组报备', desc: '便捷手续', color: '#1B3C35', bgColor: 'rgba(27, 60, 53, 0.15)', path: '/pages/filing/filing' },
        { icon: 'phone', text: '协拍服务', desc: '专业支持', color: '#C62828', bgColor: 'rgba(198, 40, 40, 0.15)', path: '/pages/services/services' },
        { icon: 'home', text: '住宿服务', desc: '舒适休息', color: '#7B1FA2', bgColor: 'rgba(123, 31, 162, 0.15)', path: '/pages/hotel/hotel' },
        { icon: 'chatbubble', text: '影视资讯', desc: '行业动态', color: '#00838F', bgColor: 'rgba(0, 131, 143, 0.15)', path: '/pages/news/news' },
        { icon: 'map', text: '影视游', desc: '探寻足迹', color: '#558B2F', bgColor: 'rgba(85, 139, 47, 0.15)', path: '/pages/tourroute/tourroute' },
        { icon: 'info', text: '视听政策', desc: '扶持政策', color: '#3b82f6', bgColor: 'rgba(59, 130, 246, 0.15)', path: '/pages/policy/policy' }
      ],
      articles: [],
      locations: [],
      currentBanner: 0,
      locationCategories: [
        { label: '自然风光', value: 'natural' },
        { label: '历史建筑', value: 'historical' },
        { label: '现代建筑', value: 'modern' },
        { label: '文化场所', value: 'cultural' },
        { label: '商业场所', value: 'commercial' },
        { label: '公园景点', value: 'park' },
        { label: '其他', value: 'other' }
      ]
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    getFileUrl, // Expose helper to template
    async loadData() {
      try {
        await this.withLoading(async () => {
          const [bannerRes, articleRes, locationRes] = await Promise.all([
            getBannerPage({ current: 1, size: 10 }),
            getArticlePage({ current: 1, size: 5 }),
            getLocationPage({ current: 1, size: 5 })
          ])

          // 处理轮播图数据
          if (bannerRes && bannerRes.data) {
            this.banners = bannerRes.data.map((banner, index) => {
              let path = '/pages/services/services'
              const targetModule = banner.targetModule || ''
              
              // 处理带参数的targetModule (例如 "政策列表?type=省级")
              const [moduleName, queryString] = targetModule.split('?')
              const basePath = this.modulePathMap[moduleName]
              
              if (basePath) {
                path = queryString ? `${basePath}?${queryString}` : basePath
              }

              return {
                title: banner.imageName || '雅安影视服务',
                desc: '', // 不再显示任何描述文本
                imageUrl: banner.imageUrl,
                tag: index === 0 ? '平台服务' : index === 1 ? '取景胜地' : '专业支持',
                targetModule: banner.targetModule, // 保存targetModule用于跳转
                path: path
              }
            })
          }

          // 处理文章数据（游标分页）
          if (articleRes) {
            // 游标分页响应格式: { records: [...], cursor: "xxx", hasMore: true/false }
            if (articleRes.records) {
              this.articles = Array.isArray(articleRes.records) ? articleRes.records : []
            } else if (Array.isArray(articleRes)) {
              // 如果直接返回数组，则使用数组
              this.articles = articleRes
            } else {
              this.articles = []
            }
          }
          
          // 处理取景地数据（游标分页）
          if (locationRes) {
            // 游标分页响应格式: { records: [...], cursor: "xxx", hasMore: true/false }
            if (locationRes.records) {
              this.locations = Array.isArray(locationRes.records) ? locationRes.records : []
            } else if (Array.isArray(locationRes)) {
              // 如果直接返回数组，则使用数组
              this.locations = locationRes
            } else {
              this.locations = []
            }
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
      // 优先使用根据targetModule映射的路径
      if (item.path) {
        // 判断是否为tabBar页面
        const tabBarPages = ['/pages/index/index', '/pages/profile/profile'];
        if (tabBarPages.includes(item.path)) {
          uni.switchTab({
            url: item.path
          });
        } else {
          uni.navigateTo({
            url: item.path
          });
        }
      } else {
        // 如果没有对应路径，则默认跳转到服务页面
        uni.navigateTo({
          url: '/pages/services/services'
        });
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
    goToServices() {
      uni.navigateTo({
        url: '/pages/profile/help'
      })
    },
    getArticleCover(article) {
      // 1. 优先使用 thumbImage
      if (article.thumbImage) {
        return getFileUrl(article.thumbImage)
      }
      // 2. 其次使用 image (如果是数组取第一个)
      if (article.image) {
        return getFileUrl(article.image)
      }
      // 3. 最后尝试 cover (兼容旧数据)
      if (article.cover) {
        return getFileUrl(article.cover)
      }
      // 4. 默认图片
      return 'http://162.14.106.139:8080/api/files/origin/1765767098667_%E6%8B%8D%E5%9C%A8%E9%9B%85%E5%AE%89_compressed.png'
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
    },
    getLocationTypeLabel(value) {
      if (!value) return ''
      const category = this.locationCategories.find(c => c.value === value)
      return category ? category.label : value
    }
  }
}
</script>

<style lang="scss" scoped>
.index-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #F8FAFB 0%, #EDF3F5 100%);
  position: relative;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏样式 */
.page-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, #1B3C35 0%, #2E7D32 100%);
  padding-top: calc(env(safe-area-inset-top) + 60rpx);
  box-shadow: 0 4rpx 20rpx rgba(27, 60, 53, 0.25);
}

.nav-title {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 auto;
  justify-content: center;
  padding: 16rpx 32rpx 20rpx;
  margin: 0 auto;
}

.nav-main {
  font-size: 36rpx;
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
  flex: 1;
  position: relative;
  margin-top: calc(env(safe-area-inset-top) + 160rpx);
  padding-bottom: calc(env(safe-area-inset-bottom) + 120rpx);
  box-sizing: border-box;
}

/* 创意Banner样式 - 优化更紧凑 */
.creative-banner {
  margin: 0 0 28rpx;
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
  line-height: 1.5;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
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

/* 内容区域通用样式 */
.content-section {
  margin: 0 24rpx 28rpx;
  background: linear-gradient(135deg, #FFFFFF 0%, #FAFBFC 100%);
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid rgba(27, 60, 53, 0.08);
}

.loading-wrapper,
.empty-wrapper {
  padding: 60rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 影视资讯区域 - 优化排版 */
.news-cards {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.news-card {
  display: flex;
  gap: 16rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1rpx solid rgba(0, 0, 0, 0.04);
}

.news-card:active {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 24rpx rgba(212, 175, 55, 0.15);
}

.news-card-image {
  width: 200rpx;
  height: 140rpx;
  flex-shrink: 0;
  object-fit: cover;
  background: linear-gradient(135deg, #F5F7FA, #E8F0F2);
}

.news-card-content {
  flex: 1;
  padding: 16rpx 16rpx 16rpx 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.news-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
  gap: 8rpx;
}

.news-card-tag {
  font-size: 20rpx;
  color: #1B3C35;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.15), rgba(212, 175, 55, 0.1));
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-weight: 700;
  white-space: nowrap;
}

.news-card-date {
  font-size: 20rpx;
  color: #999;
  font-weight: 500;
  white-space: nowrap;
}

.news-card-title {
  font-size: 28rpx;
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

/* 热门取景地区域 - 优化排版 */
.location-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.location-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1rpx solid rgba(0, 0, 0, 0.04);
}

.location-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(46, 125, 50, 0.15);
}

.location-card-image-box {
  position: relative;
  width: 100%;
  height: 180rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #F5F7FA, #E8F0F2);
}

.location-card-image {
  width: 100%;
  height: 100%;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  object-fit: cover;
}

.location-card:active .location-card-image {
  transform: scale(1.1);
}

.location-card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.3) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.location-card:active .location-card-overlay {
  opacity: 1;
}

.location-card-price {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.98), rgba(212, 175, 55, 0.92));
  color: #1B3C35;
  font-size: 22rpx;
  font-weight: 800;
  padding: 6rpx 14rpx;
  border-radius: 16rpx;
  backdrop-filter: blur(10rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.25);
}

.location-card-info {
  padding: 16rpx;
  background: #FFFFFF;
}

.location-card-name {
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

.location-card-type {
  font-size: 20rpx;
  color: #2E7D32;
  background: linear-gradient(135deg, rgba(46, 125, 50, 0.12), rgba(46, 125, 50, 0.08));
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  display: inline-block;
  font-weight: 700;
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

/* 底部间距 - 增加高度避免被TabBar遮挡 */
.bottom-spacer {
  height: 160rpx;
}

/* 响应式设计 - 优化适配 */
@media screen and (max-width: 750rpx) {
  .location-cards {
    grid-template-columns: 1fr;
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


