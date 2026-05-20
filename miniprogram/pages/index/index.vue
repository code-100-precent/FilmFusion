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

      <!-- 创意功能入口 - 改为国风Grid布局 -->
      <view class="creative-functions-wrapper">
        <!-- 弧形背景装饰 -->
        <view class="curved-bg"></view>
        
        <view class="creative-functions">
          <view class="function-grid">
            <view
              v-for="(item, index) in functions"
              :key="index"
              class="function-item"
              @click="handleFunctionClick(item)"
            >
              <view class="function-icon-wrapper">
                <view class="function-icon-bg"></view>
                <view class="function-icon-border"></view>
                <uni-icons :type="item.icon" size="32" :color="item.color" class="function-icon-main"></uni-icons>
                <!-- 装饰云纹 -->
                <view class="cloud-decor"></view>
              </view>
              <text class="function-title">{{ item.text }}</text>
            </view>
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
          <!-- 调试按钮 -->
          <view class="debug-btn" @click="showColorPicker = true">
            <uni-icons type="color" size="20" color="#D4AF37"></uni-icons>
          </view>
        </view>

        <view v-if="loading" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="articles.length === 0" class="empty-wrapper">
          <Empty text="暂无资讯"></Empty>
        </view>
        <scroll-view v-else class="news-scroll" scroll-x="true" enable-flex="true">
          <view
            v-for="article in articles"
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
        </scroll-view>
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
              <!-- 添加类型标签到图片左上角 -->
              <view class="location-type-tag">{{ getLocationTypeLabel(location.type) }}</view>
              
              <image 
                v-if="getFileUrl(location.cover || location.thumbImage)"
                :src="getFileUrl(location.cover || location.thumbImage)" 
                class="location-card-image" 
                mode="aspectFill"
              ></image>
              <view v-else class="location-card-overlay" style="background: #f3f4f6; display: flex; align-items: center; justify-content: center;">
                 <uni-icons type="image" size="30" color="#d1d5db"></uni-icons>
              </view>
              <view class="location-card-overlay"></view>
            </view>
            <view class="location-card-info">
              <text class="location-card-name">{{ location.name }}</text>
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
              <uni-icons type="image-filled" size="40" color="#D4AF37"></uni-icons>
            </view>
            <text class="featured-title">自然景观</text>
            <text class="featured-desc">探索自然之美</text>
          </view>
          <view class="featured-card culture">
            <view class="featured-icon">
              <uni-icons type="star-filled" size="40" color="#D4AF37"></uni-icons>
            </view>
            <text class="featured-title">文化底蕴</text>
            <text class="featured-desc">感受历史魅力</text>
          </view>
          <view class="featured-card modern">
            <view class="featured-icon">
              <uni-icons type="home-filled" size="40" color="#D4AF37"></uni-icons>
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

    <!-- 颜色选择器弹窗 -->
    <view v-if="showColorPicker" class="color-picker-modal" @click="showColorPicker = false">
      <view class="color-picker-content" @click.stop>
        <view class="picker-header">
          <text class="picker-title">背景颜色调试</text>
          <view class="close-btn" @click="showColorPicker = false">
            <uni-icons type="closeempty" size="24" color="#666"></uni-icons>
          </view>
        </view>

        <!-- 影视资讯模块 -->
        <view class="color-section">
          <text class="section-label">影视资讯模块</text>
          
          <!-- 颜色1 -->
          <view class="color-item">
            <view class="color-item-header">
              <text class="color-label">颜色1</text>
              <text class="rgb-text">{{ hexToRgb(newsColor1) }}</text>
            </view>
            <view class="color-preview" :style="{ background: newsColor1 }"></view>
            <view class="color-palette">
              <view
                v-for="(color, index) in colorPalette"
                :key="'news1-' + index"
                class="palette-item"
                :style="{ background: color }"
                @click="newsColor1 = color"
              >
                <view v-if="newsColor1 === color" class="selected-mark">
                  <uni-icons type="checkmarkempty" size="16" color="#fff"></uni-icons>
                </view>
              </view>
            </view>
          </view>

          <!-- 颜色2 -->
          <view class="color-item">
            <view class="color-item-header">
              <text class="color-label">颜色2</text>
              <text class="rgb-text">{{ hexToRgb(newsColor2) }}</text>
            </view>
            <view class="color-preview" :style="{ background: newsColor2 }"></view>
            <view class="color-palette">
              <view
                v-for="(color, index) in colorPalette"
                :key="'news2-' + index"
                class="palette-item"
                :style="{ background: color }"
                @click="newsColor2 = color"
              >
                <view v-if="newsColor2 === color" class="selected-mark">
                  <uni-icons type="checkmarkempty" size="16" color="#fff"></uni-icons>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 热门取景地模块 -->
        <view class="color-section">
          <text class="section-label">热门取景地模块</text>
          
          <!-- 颜色1 -->
          <view class="color-item">
            <view class="color-item-header">
              <text class="color-label">颜色1</text>
              <text class="rgb-text">{{ hexToRgb(locationColor1) }}</text>
            </view>
            <view class="color-preview" :style="{ background: locationColor1 }"></view>
            <view class="color-palette">
              <view
                v-for="(color, index) in colorPalette"
                :key="'loc1-' + index"
                class="palette-item"
                :style="{ background: color }"
                @click="locationColor1 = color"
              >
                <view v-if="locationColor1 === color" class="selected-mark">
                  <uni-icons type="checkmarkempty" size="16" color="#fff"></uni-icons>
                </view>
              </view>
            </view>
          </view>

          <!-- 颜色2 -->
          <view class="color-item">
            <view class="color-item-header">
              <text class="color-label">颜色2</text>
              <text class="rgb-text">{{ hexToRgb(locationColor2) }}</text>
            </view>
            <view class="color-preview" :style="{ background: locationColor2 }"></view>
            <view class="color-palette">
              <view
                v-for="(color, index) in colorPalette"
                :key="'loc2-' + index"
                class="palette-item"
                :style="{ background: color }"
                @click="locationColor2 = color"
              >
                <view v-if="locationColor2 === color" class="selected-mark">
                  <uni-icons type="checkmarkempty" size="16" color="#fff"></uni-icons>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="picker-actions">
          <view class="action-btn reset-btn" @click="resetColors">重置</view>
          <view class="action-btn confirm-btn" @click="showColorPicker = false">确定</view>
        </view>
      </view>
    </view>
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
        { icon: 'videocam', text: '光影雅安', desc: '影视作品', color: '#FFD700', bgColor: 'rgba(255, 215, 0, 0.25)', path: '/pages/films/films' },
        { icon: 'location', text: '拍摄场地', desc: '完美取景', color: '#00E676', bgColor: 'rgba(0, 230, 118, 0.25)', path: '/pages/scenes/scenes' },
        { icon: 'calendar', text: '剧组报备', desc: '便捷手续', color: '#2979FF', bgColor: 'rgba(41, 121, 255, 0.25)', path: '/pages/filing/filing' },
        { icon: 'phone', text: '协拍服务', desc: '专业支持', color: '#FF4081', bgColor: 'rgba(255, 64, 129, 0.25)', path: '/pages/services/services' },
        { icon: 'home', text: '住宿服务', desc: '舒适休息', color: '#AA00FF', bgColor: 'rgba(170, 0, 255, 0.25)', path: '/pages/hotel/hotel' },
        { icon: 'chatbubble', text: '影视资讯', desc: '行业动态', color: '#00E5FF', bgColor: 'rgba(0, 229, 255, 0.25)', path: '/pages/news/news' },
        { icon: 'map', text: '影视游', desc: '探寻足迹', color: '#FF9100', bgColor: 'rgba(255, 145, 0, 0.25)', path: '/pages/tourroute/tourroute' },
        { icon: 'info', text: '视听政策', desc: '扶持政策', color: '#FF3D00', bgColor: 'rgba(255, 61, 0, 0.25)', path: '/pages/policy/policy' }
      ],
      articles: [],
      locations: [],
      currentBanner: 0,
      locationCategories: [
        { label: '自然景观', value: 'natural' },
        { label: '人文景观', value: 'humanities' },
        { label: '城市场景', value: 'urban' },
        { label: '特色场景', value: 'feature' }
      ],
      // 颜色调试相关
      showColorPicker: false,
      newsColor1: '#c7d373',
      newsColor2: '#2ea47c',
      locationColor1: '#90aadf',
      locationColor2: '#90aadf',
      // 色板颜色
      colorPalette: [
        // 第一行 - 绿色系
        '#2ea47c', '#3cb371', '#20b2aa', '#00c9a7', '#00d084',
        '#48d1cc', '#40e0d0', '#7fffd4', '#66cdaa', '#98fb98',
        // 第二行 - 蓝色系
        '#4169e1', '#4682b4', '#5f9ea0', '#6495ed', '#87ceeb',
        '#87cefa', '#90aadf', '#add8e6', '#b0c4de', '#b0e0e6',
        // 第三行 - 紫色系
        '#8a2be2', '#9370db', '#9966cc', '#ba55d3', '#da70d6',
        '#dda0dd', '#ee82ee', '#d8bfd8', '#e6e6fa', '#f0e6ff',
        // 第四行 - 粉红色系
        '#ff1493', '#ff69b4', '#ff6eb4', '#ffb6c1', '#ffc0cb',
        '#ffb3d9', '#ffd1dc', '#ffe4e1', '#fff0f5', '#ffe4f0',
        // 第五行 - 红色系
        '#dc143c', '#ff0000', '#ff4500', '#ff6347', '#ff7f50',
        '#ff8c69', '#ffa07a', '#ffb6c1', '#ffc1cc', '#ffd7d7',
        // 第六行 - 橙黄色系
        '#ff8c00', '#ffa500', '#ffb347', '#ffc125', '#ffd700',
        '#ffe135', '#ffec8b', '#fff68f', '#fffacd', '#ffffe0',
        // 第七行 - 黄绿色系
        '#c7d373', '#d4d55c', '#daa520', '#bdb76b', '#f0e68c',
        '#eee8aa', '#f5deb3', '#ffe4b5', '#ffefd5', '#fff8dc',
        // 第八行 - 棕色系
        '#8b4513', '#a0522d', '#a52a2a', '#bc8f8f', '#cd853f',
        '#d2691e', '#deb887', '#f4a460', '#f5deb3', '#ffe4c4',
        // 第九行 - 灰色系
        '#2f4f4f', '#696969', '#708090', '#778899', '#808080',
        '#a9a9a9', '#c0c0c0', '#d3d3d3', '#dcdcdc', '#f5f5f5',
        // 第十行 - 特殊色
        '#000000', '#1a1a1a', '#333333', '#4d4d4d', '#666666',
        '#800000', '#8b0000', '#006400', '#00008b', '#ffffff'
      ]
    }
  },
  onLoad() {
    // 1. 尝试从缓存读取Banner，实现秒开
    try {
      const cachedBanners = uni.getStorageSync('index_banners_cache')
      if (cachedBanners && Array.isArray(cachedBanners)) {
        this.banners = cachedBanners
      }
    } catch (e) {
      console.error('读取Banner缓存失败', e)
    }
    
    // 2. 尝试从缓存读取颜色配置
    try {
      const cachedColors = uni.getStorageSync('index_colors_cache')
      if (cachedColors) {
        this.newsColor1 = cachedColors.newsColor1 || this.newsColor1
        this.newsColor2 = cachedColors.newsColor2 || this.newsColor2
        this.locationColor1 = cachedColors.locationColor1 || this.locationColor1
        this.locationColor2 = cachedColors.locationColor2 || this.locationColor2
      }
    } catch (e) {
      console.error('读取颜色缓存失败', e)
    }
    
    this.loadData()
  },
  computed: {
    newsGradient() {
      return `linear-gradient(160deg, ${this.newsColor1} 0%, ${this.newsColor2} 100%)`
    },
    locationGradient() {
      return `linear-gradient(160deg, ${this.locationColor1} 0%, ${this.locationColor2} 100%)`
    }
  },
  watch: {
    newsColor1() {
      this.saveColors()
    },
    newsColor2() {
      this.saveColors()
    },
    locationColor1() {
      this.saveColors()
    },
    locationColor2() {
      this.saveColors()
    }
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
            const cachedPaths = uni.getStorageSync('banner_local_paths') || {}
            const fs = uni.getFileSystemManager ? uni.getFileSystemManager() : null
            
            const newBanners = bannerRes.data.map((banner, index) => {
              let path = '/pages/services/services'
              const targetModule = banner.targetModule || ''
              
              // 处理带参数的targetModule (例如 "政策列表?type=省级")
              const [moduleName, queryString] = targetModule.split('?')
              const basePath = this.modulePathMap[moduleName]
              
              if (basePath) {
                path = queryString ? `${basePath}?${queryString}` : basePath
              }

              const remoteUrl = getFileUrl(banner.imageUrl)
              let displayUrl = remoteUrl
              
              // 尝试使用本地缓存图片
              if (fs && cachedPaths[remoteUrl]) {
                try {
                  fs.accessSync(cachedPaths[remoteUrl])
                  displayUrl = cachedPaths[remoteUrl]
                } catch (e) {
                  // 文件不存在，清除缓存记录
                  delete cachedPaths[remoteUrl]
                  uni.setStorageSync('banner_local_paths', cachedPaths)
                }
              }

              return {
                title: banner.imageName || '雅安影视服务',
                desc: '', // 不再显示任何描述文本
                imageUrl: displayUrl,
                originalUrl: remoteUrl, // 保存原始URL用于下载对比
                tag: index === 0 ? '平台服务' : index === 1 ? '取景胜地' : '专业支持',
                targetModule: banner.targetModule, // 保存targetModule用于跳转
                path: path
              }
            })
            
            this.banners = newBanners
            // 更新缓存
            try {
              uni.setStorageSync('index_banners_cache', newBanners)
            } catch (e) {
              console.error('缓存Banner失败', e)
            }
            
            // 触发后台下载更新缓存
            this.downloadAndCacheBannerImages(newBanners)
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
    downloadAndCacheBannerImages(banners) {
      // 仅在支持文件系统的环境下执行
      if (!uni.getFileSystemManager) return
      
      const cachedPaths = uni.getStorageSync('banner_local_paths') || {}
      let hasChange = false
      
      banners.forEach((banner, index) => {
        const remoteUrl = banner.originalUrl || banner.imageUrl
        // 如果已经是本地路径，或者已经缓存且文件存在，则跳过
        if (remoteUrl.startsWith('http') && (!cachedPaths[remoteUrl] || banner.imageUrl === remoteUrl)) {
          // 下载文件
          uni.downloadFile({
            url: remoteUrl,
            success: (res) => {
              if (res.statusCode === 200) {
                // 保存文件
                uni.saveFile({
                  tempFilePath: res.tempFilePath,
                  success: (saveRes) => {
                    const savedPath = saveRes.savedFilePath
                    cachedPaths[remoteUrl] = savedPath
                    uni.setStorageSync('banner_local_paths', cachedPaths)
                    
                    // 更新当前视图中的图片路径（如果还在显示该banner）
                    // 注意：这里修改this.banners会触发视图更新
                    if (this.banners[index] && (this.banners[index].originalUrl === remoteUrl || this.banners[index].imageUrl === remoteUrl)) {
                      this.banners[index].imageUrl = savedPath
                      // 同时更新页面缓存
                      uni.setStorageSync('index_banners_cache', this.banners)
                    }
                  }
                })
              }
            }
          })
        }
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
      return getFileUrl('files/origin/1765767098667_%E6%8B%8D%E5%9C%A8%E9%9B%85%E5%AE%89_compressed.png')
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
    },
    hexToRgb(hex) {
      const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
      if (result) {
        const r = parseInt(result[1], 16)
        const g = parseInt(result[2], 16)
        const b = parseInt(result[3], 16)
        return `rgb(${r}, ${g}, ${b})`
      }
      return hex
    },
    saveColors() {
      try {
        uni.setStorageSync('index_colors_cache', {
          newsColor1: this.newsColor1,
          newsColor2: this.newsColor2,
          locationColor1: this.locationColor1,
          locationColor2: this.locationColor2
        })
      } catch (e) {
        console.error('保存颜色配置失败', e)
      }
    },
    resetColors() {
      this.newsColor1 = '#c7d373'
      this.newsColor2 = '#2ea47c'
      this.locationColor1 = '#90aadf'
      this.locationColor2 = '#90aadf'
    }
  }
}
</script>

<style lang="scss" scoped>
.index-page {
  min-height: 100vh;
  background: radial-gradient(circle at 50% 0%, #2b2b2b 0%, #121212 60%, #000000 100%);
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
  background: rgba(20, 20, 20, 0.85);
  backdrop-filter: blur(20rpx);
  padding-top: calc(env(safe-area-inset-top) + 60rpx);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
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

/* 创意功能入口 - 国风Grid布局 */
.creative-functions-wrapper {
  position: relative;
  margin-top: -40rpx; /* 向上偏移，衔接Banner */
  z-index: 10;
  padding-bottom: 20rpx;
}

.curved-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 80rpx;
  background: radial-gradient(120% 100% at 50% 0%, rgba(20,20,20,0) 50%, #121212 51%);
  /* 这里的渐变模拟了弧形，但为了更像参考图，我们用一个更直接的圆弧遮罩 */
  background: transparent;
}

/* 使用伪元素制作顶部弧形分割线 */
.creative-functions-wrapper::before {
  content: '';
  position: absolute;
  top: 20rpx;
  left: -10%;
  width: 120%;
  height: 60rpx;
  background: #121212; /* 与页面背景一致 */
  border-radius: 50% 50% 0 0 / 100% 100% 0 0;
  z-index: 0;
  box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.5); /* 顶部阴影 */
  border-top: 1rpx solid rgba(255,215,0,0.1); /* 微弱的金线 */
}

/* 虚线装饰 - 模仿参考图 */
.creative-functions-wrapper::after {
  content: '';
  position: absolute;
  top: 32rpx;
  left: 5%;
  width: 90%;
  height: 40rpx;
  border-top: 2rpx dashed rgba(255,255,255,0.15);
  border-radius: 50% 50% 0 0 / 100% 100% 0 0;
  z-index: 1;
  pointer-events: none;
}

.creative-functions {
  position: relative;
  z-index: 2;
  margin: 0 24rpx;
  padding-top: 40rpx;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  row-gap: 32rpx;
  column-gap: 12rpx;
  padding: 10rpx 0;
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.function-icon-wrapper {
  position: relative;
  width: 108rpx;
  height: 108rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.function-item:active .function-icon-wrapper {
  transform: scale(0.92);
}

/* 国风徽章背景 */
.function-icon-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  /* 浅米色渐变，模仿宣纸/象牙白 */
  background: linear-gradient(135deg, #FFFBF0 0%, #F2E6CE 100%);
  box-shadow: 
    0 6rpx 16rpx rgba(0,0,0,0.4), /* 外部投影 */
    inset 0 -4rpx 8rpx rgba(0,0,0,0.1), /* 内部立体感 */
    inset 0 4rpx 8rpx rgba(255,255,255,0.8); /* 顶部高光 */
  z-index: 1;
}

/* 金色边框装饰 */
.function-icon-border {
  position: absolute;
  top: 6rpx;
  left: 6rpx;
  right: 6rpx;
  bottom: 6rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(212, 175, 55, 0.3);
  z-index: 2;
}

/* 云纹装饰 (右上角) */
.cloud-decor {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  width: 40rpx;
  height: 40rpx;
  background: radial-gradient(circle at 30% 30%, #fff 0%, #f0f0f0 100%);
  border-radius: 50%;
  opacity: 0.9;
  z-index: 3;
  box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cloud-decor::before {
  content: '';
  width: 24rpx;
  height: 24rpx;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 24 24' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M17.5,19c-0.83,0-1.5-0.67-1.5-1.5c0-0.83,0.67-1.5,1.5-1.5c0.83,0,1.5,0.67,1.5,1.5C19,18.33,18.33,19,17.5,19z M6.5,19 C5.67,19,5,18.33,5,17.5c0-0.83,0.67-1.5,1.5-1.5c0.83,0,1.5,0.67,1.5,1.5C8,18.33,7.33,19,6.5,19z M12,19c-2.21,0-4-1.79-4-4 c0-2.21,1.79-4,4-4s4,1.79,4,4C16,17.21,14.21,19,12,19z' fill='%23D4AF37' opacity='0.6'/%3E%3C/svg%3E");
  background-size: contain;
}

.function-icon-main {
  position: relative;
  z-index: 3;
  /* 图标添加轻微投影 */
  filter: drop-shadow(0 2rpx 2rpx rgba(0,0,0,0.2));
}

.function-title {
  font-size: 24rpx;
  color: #E0E0E0; /* 浅灰白，柔和一点 */
  font-weight: 500;
  text-align: center;
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.5);
}

/* 适配不同屏幕 */
@media screen and (max-width: 360rpx) {
  .function-icon-wrapper {
    width: 96rpx;
    height: 96rpx;
  }
  
  .function-title {
    font-size: 22rpx;
  }
}

/* 内容区域通用样式 - 移除统一背景，改为各模块独立配色 */
.content-section {
  margin: 0 24rpx 28rpx;
  border-radius: 24rpx;
  padding: 24rpx;
  position: relative;
  overflow: hidden;
  /* 移除之前的白色背景和边框 */
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.2); 
}

/* 顶部装饰条通用 - 改为更通用的混合模式 */
.content-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2rpx;
  background: rgba(255, 255, 255, 0.2);
  z-index: 1;
}

.loading-wrapper,
.empty-wrapper {
  padding: 60rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 标题样式 - 通用白色 */
.section-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #FFFFFF;
  position: relative;
  letter-spacing: 2rpx;
  text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.3);
}

.small-header {
  display: flex;
  align-items: center;
  justify-content: flex-start; /* 左对齐 */
  margin-bottom: 24rpx;
  gap: 12rpx; /* 标题和按钮之间的间距 */
}

.small-header .section-title {
  font-size: 32rpx;
  padding-left: 16rpx;
  border-left: 8rpx solid #D4AF37;
  line-height: 1;
  margin-right: 0; /* 移除右侧可能的间距 */
  padding-right: 0;
}

/* 查看更多按钮 - 简化版 */
.section-more {
  display: flex;
  align-items: center;
  gap: 2rpx;
  font-size: 20rpx; /* 更小字体 */
  color: rgba(255, 255, 255, 0.6); /* 浅一点的颜色 */
  font-weight: 400;
  transition: all 0.3s ease;
  background: transparent; /* 移除背景色 */
  padding: 0; /* 移除内边距 */
  margin-top: 4rpx; /* 微调垂直对齐 */
}

.section-more:active {
  color: #D4AF37;
  opacity: 0.8;
}

/* =========================================
   模块1：影视资讯 - 竹青色调 (Bamboo Green)
   ========================================= */
.news-section {
  background: linear-gradient(160deg, rgb(0, 0, 139) 0%, rgb(218, 165, 32) 100%);
  border: 1rpx solid rgba(46, 92, 85, 0.3);
}

/* 添加背景纹理 */
.news-section::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(circle at 100% 0%, rgba(148, 146, 198, 0.05) 0%, transparent 20%);
  pointer-events: none;
}

.news-scroll {
  white-space: nowrap;
  width: 100%;
  position: relative;
  z-index: 2;
}

.news-card {
  display: inline-flex;
  width: 520rpx;
  margin-right: 20rpx;
  gap: 16rpx;
  background: rgba(255, 255, 255, 0.05); /* 半透明背景 */
  border-radius: 16rpx;
  overflow: hidden;
  padding: 12rpx;
  transition: all 0.3s ease;
  border: 5rpx solid rgba(252, 248, 248, 0.1);
  vertical-align: top;
  white-space: normal;
}

.news-card:active {
  background: rgba(255, 255, 255, 0.1);
}

.news-card:last-child {
  margin-right: 0;
  /* border-bottom: 1rpx solid rgba(255, 255, 255, 0.05); 移除此规则，因为不再是列表 */
}

.news-card-image {
  width: 180rpx;
  height: 180rpx;
  flex-shrink: 0;
  object-fit: cover;
  background: rgba(0,0,0,0.2);
  border-radius: 8rpx;
}

.news-card-content {
  flex: 1;
  padding: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
  height: 180rpx;
}

.news-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4rpx;
}

.news-card-tag {
  font-size: 20rpx;
  color: #D4AF37;
  background: rgba(0, 0, 0, 0.3);
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  font-weight: 600;
  border: 1rpx solid rgba(212, 175, 55, 0.3);
}

.news-card-date {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.6);
}

.news-card-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #ffffff;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  overflow: hidden;
  margin-top: auto;
}

/* =========================================
   模块2：热门取景地 - 淡黄色调
   ========================================= */
.location-section {
  background: linear-gradient(160deg, rgba(218, 165, 32, 0.8) 0%, rgba(0, 0, 139, 0.8) 100%);
  border: 1rpx solid rgba(255, 235, 59, 0.3);
}

.location-section .section-title {
  color: #ffffff;
  text-shadow: none;
}

.location-section .section-more {
  color: rgba(176, 153, 145, 0.7);
}

.location-section .section-more:active {
  color: #3E2723;
}

.location-section::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(circle at 0% 100%, rgba(255,255,255,0.4) 0%, transparent 25%);
  pointer-events: none;
}

.location-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  position: relative;
  z-index: 2;
}

.location-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 1rpx solid rgba(255, 235, 59, 0.2);
}

.location-card:active {
  transform: translateY(-4rpx);
  border-color: #FBC02D;
}

.location-card-image-box {
  position: relative;
  width: 100%;
  height: 220rpx;
  overflow: hidden;
  background: rgba(0,0,0,0.05);
}

.location-card-image {
  width: 100%;
  height: 100%;
  transition: transform 0.5s ease;
  object-fit: cover;
}

.location-card:active .location-card-image {
  transform: scale(1.05);
}

.location-card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, transparent 60%, rgba(0, 0, 0, 0.7) 100%);
}

.location-type-tag {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8rpx);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 18rpx;
  color: #D4AF37;
  font-weight: 700;
  z-index: 2;
  border: 1rpx solid rgba(212, 175, 55, 0.3);
}

.location-type-tag::before {
  content: '';
  width: 6rpx;
  height: 6rpx;
  background: #D4AF37;
  border-radius: 50%;
  margin-right: 6rpx;
}

.location-card-info {
  padding: 16rpx 16rpx;
  background: transparent; /* 透明背景，由父级控制 */
}

.location-card-name {
  font-size: 26rpx;
  font-weight: 700;
  color: #333333;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}

/* =========================================
   模块3：雅安印象 - 特色主题区域
   ========================================= */
.featured-section {
  margin: 0 24rpx 28rpx;
  background: transparent; /* 恢复透明背景 */
  border-radius: 24rpx;
  padding: 0; /* 移除内边距 */
  position: relative;
  overflow: visible; /* 允许阴影溢出 */
  box-shadow: none; /* 移除容器阴影 */
  border: none; /* 移除边框 */
}

.featured-section .section-header {
  margin-bottom: 24rpx;
  /* 恢复标题样式 */
  display: flex;
  align-items: center;
}

.featured-section .section-title {
  color: rgb(96, 197, 129); /* 恢复深色标题 */
  text-shadow: none;
  border-left: 8rpx solid #d9c06f;
  padding-left: 16rpx;
  font-size: 32rpx;
  line-height: 1;
}

.featured-section::before {
  display: none; /* 移除装饰线 */
}

.featured-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16rpx;
  position: relative;
  z-index: 2;
}

.featured-card {
  background: #FFFFFF; /* 恢复纯白卡片 */
  border-radius: 16rpx;
  padding: 32rpx 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  text-align: center;
  position: relative;
  overflow: hidden;
  border: none;
}

.featured-card:active {
  background: #FAFAFA;
  transform: scale(0.98);
}

.featured-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4rpx;
  background: linear-gradient(90deg, #D4AF37, #F2E6CE);
  opacity: 0.8;
}

.featured-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20rpx;
  background: #FFFBF0; /* 浅金色背景 */
  color: #B8860B;
  border: none;
}

.featured-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #333333; /* 深色文字 */
  display: block;
  margin-bottom: 6rpx;
}

.featured-desc {
  font-size: 20rpx;
  color: #999999; /* 灰色文字 */
  display: block;
  line-height: 1.2;
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
  color: #000000;
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

/* 调试按钮样式 */
.debug-btn {
  width: 56rpx;
  height: 56rpx;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: auto;
  transition: all 0.3s ease;
  border: 2rpx solid rgba(212, 175, 55, 0.3);
  backdrop-filter: blur(10rpx);
}

.debug-btn:active {
  background: rgba(0, 0, 0, 0.5);
  transform: scale(0.95);
}

/* 颜色选择器弹窗 */
.color-picker-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  backdrop-filter: blur(10rpx);
}

.color-picker-content {
  background: #FFFFFF;
  border-radius: 24rpx;
  width: 100%;
  max-width: 600rpx;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 2rpx solid #F0F0F0;
}

.picker-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #333333;
}

.close-btn {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-btn:active {
  background: #F5F5F5;
}

.color-section {
  padding: 32rpx;
  border-bottom: 2rpx solid #F0F0F0;
}

.color-section:last-of-type {
  border-bottom: none;
}

.section-label {
  font-size: 28rpx;
  font-weight: 700;
  color: #333333;
  display: block;
  margin-bottom: 24rpx;
  padding-left: 16rpx;
  border-left: 6rpx solid #D4AF37;
}

.color-item {
  margin-bottom: 32rpx;
}

.color-item:last-child {
  margin-bottom: 0;
}

.color-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.color-label {
  font-size: 24rpx;
  color: #666666;
  font-weight: 600;
}

.rgb-text {
  font-size: 20rpx;
  color: #999999;
  font-family: 'Courier New', monospace;
  font-weight: 500;
}

.color-preview {
  width: 100%;
  height: 60rpx;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  border: 2rpx solid #E0E0E0;
}

.color-palette {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 8rpx;
}

.palette-item {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 2rpx solid rgba(0, 0, 0, 0.1);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.palette-item:active {
  transform: scale(0.9);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
}

.selected-mark {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6rpx;
}

.picker-actions {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx 32rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 700;
  transition: all 0.3s ease;
}

.reset-btn {
  background: #F5F5F5;
  color: #666666;
  border: 2rpx solid #E0E0E0;
}

.reset-btn:active {
  background: #E8E8E8;
}

.confirm-btn {
  background: linear-gradient(135deg, #D4AF37, #C9A961);
  color: #000000;
  box-shadow: 0 6rpx 20rpx rgba(212, 175, 55, 0.3);
}

.confirm-btn:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 14rpx rgba(212, 175, 55, 0.2);
}
</style>


