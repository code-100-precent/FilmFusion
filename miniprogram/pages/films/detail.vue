<template>
  <view class="film-detail-page">
    <NavBar :show-back="true"></NavBar>
    
    <view v-if="film" class="content">
      <!-- Poster & Header -->
      <view class="header-section">
        <image :src="posterUrl" class="poster-bg" mode="aspectFill"></image>
        <view class="header-content">
          <image :src="posterUrl" class="poster-img" mode="aspectFill"></image>
          <view class="header-info">
            <text class="film-title">{{ film.name }}</text>
            <text class="film-type" v-if="film.genre">{{ film.type }} | 备案号：{{ film.genre }}</text>
            <text class="film-type" v-else>{{ film.type }}</text>
            <text class="film-date" v-if="film.releaseDate">收录时间：{{ film.releaseDate }}</text>
          </view>
        </view>
      </view>

      <!-- Info Section -->
      <view class="info-section">
        <view class="section-title">作品简介</view>
        <text class="description">{{ film.description }}</text>
        
        <view class="meta-row" v-if="film.prodCompany">
          <text class="label">出品单位：</text>
          <text class="value">{{ film.prodCompany }}</text>
        </view>
        <view class="meta-row" v-if="film.service">
          <text class="label">播放平台：</text>
          <text class="value">{{ film.service }}</text>
        </view>
        <view class="meta-row" v-if="film.actors && film.actors.length > 0">
          <text class="label">主要演员：</text>
          <text class="value">{{ film.actors.join(' / ') }}</text>
        </view>
        <view class="meta-row" v-if="film.crewDescription">
          <text class="label">幕后制作：</text>
          <text class="value">{{ film.crewDescription }}</text>
        </view>
      </view>

      <!-- Stage Photos Section -->
      <view class="stage-photos-section" v-if="bannerImages.length > 0">
        <view class="section-title">剧照赏析</view>
        <swiper 
          v-if="bannerImages.length > 1"
          class="stage-swiper" 
          circular 
          indicator-dots 
          autoplay 
          :interval="4000" 
          :duration="500"
          indicator-active-color="#ef4444"
          indicator-color="rgba(255, 255, 255, 0.5)"
        >
          <swiper-item v-for="(img, index) in bannerImages" :key="index">
            <image :src="img" class="stage-image" mode="aspectFill" @click="previewImage(index)"></image>
          </swiper-item>
        </swiper>
        <image 
          v-else
          :src="bannerImages[0]" 
          class="stage-image" 
          mode="aspectFill" 
          @click="previewImage(0)"
          style="width: 100%; height: 400rpx; border-radius: 12rpx;"
        ></image>
      </view>

      <!-- Scenes Section -->
      <view class="scenes-section">
        <view class="section-title">雅安取景地</view>
        <!-- 如果有关联的景点详情，显示图片卡片 -->
        <view v-if="relatedLocations.length > 0" class="location-cards">
          <view 
            v-for="(location, index) in relatedLocations" 
            :key="index" 
            class="location-card"
            @click="goToLocationDetail(location.id)"
          >
            <image 
              :src="getLocationCoverUrl(location)" 
              class="location-image" 
              mode="aspectFill"
            ></image>
            <view class="location-info">
              <text class="location-name">{{ location.name }}</text>
              <text class="location-address" v-if="location.address">{{ location.address }}</text>
            </view>
          </view>
        </view>
        <!-- 如果没有关联景点详情，显示文字列表 -->
        <view v-else class="scene-list">
          <view v-for="(scene, index) in film.scenes" :key="index" class="scene-item">
            <uni-icons type="location-filled" size="20" color="#ef4444"></uni-icons>
            <text class="scene-name">{{ scene }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <view v-else-if="loading" class="loading-wrapper">
      <text>加载中...</text>
    </view>
    <view v-else class="error-wrapper">
      <text>作品信息加载失败</text>
    </view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import { getDramaById, getLocationById } from '../../services/backend-api'
// 导入文件URL处理函数
import { getFileUrl } from '../../utils'

export default {
  components: {
    NavBar
  },
  data() {
    return {
      film: null,
      loading: true,
      relatedLocations: [] // 相关景点详情列表
    }
  },
  computed: {
    // 处理海报URL
    posterUrl() {
      return this.film ? getFileUrl(this.film.poster) : ''
    },
    // 处理剧照/多图
    bannerImages() {
      if (!this.film) return []
      let images = []
      if (this.film.images && Array.isArray(this.film.images)) {
        images = this.film.images.map(img => getFileUrl(img))
      } else if (typeof this.film.images === 'string' && this.film.images) {
        images = this.film.images.split(',').map(img => getFileUrl(img))
      }
      
      // 用户要求：详情页面展示的图片是除了第一个以外的图片（第一个通常为封面）
      // 对于影视作品，封面已在顶部海报区域显示，因此这里总是移除第一张
      if (images.length > 0) {
        return images.slice(1)
      }
      
      return images
    }
  },
  onLoad(options) {
    if (options.id) {
      // 检查用户是否已登录
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        // 延迟跳转，让用户看到提示
        setTimeout(() => {
          uni.navigateTo({
            url: '/pages/login/login'
          })
        }, 1500)
        this.loading = false
      } else {
        this.loadFilmDetail(options.id)
      }
    }
  },
  methods: {
    async loadFilmDetail(id) {
      this.loading = true;
      try {
        const response = await getDramaById(id);
        
        if (response.code === 200) {
          const data = response.data || {}
          // 确保 actors 和 scenes 是数组格式
          if (data) {
            this.film = {
              ...data,
              // 映射字段
              description: data.dramaDescription || data.description || '暂无简介',
              // 处理演员名单 (支持逗号、中文逗号、顿号分隔)
              actors: data.cast ? String(data.cast).split(/[,，、]/).map(s => s.trim()).filter(s => s) : [],
              // 处理取景地
              scenes: data.shootLocation ? String(data.shootLocation).split(/[,，、]/).map(s => s.trim()).filter(s => s) : [],
              // 映射海报
              poster: data.image || data.cover,
              // 映射轮播图 (如果没有多图，就用封面)
              images: (() => {
                if (data.images && data.images.length > 0) return data.images;
                if (data.image) {
                  return Array.isArray(data.image) ? data.image : String(data.image).split(',').map(s => s.trim()).filter(s => s);
                }
                return [];
              })(),
              // 映射其他显示字段
              prodCompany: data.prodCompany || '暂无信息',
              service: data.service,
              crewDescription: data.crewDescription,
              releaseDate: data.createdAt ? data.createdAt.substring(0, 10) : '',
              type: '影视备案', // 固定类型
              genre: data.filingNum, // 显示备案号
              locationId: data.locationId // 保存locationId用于加载景点详情
            };
            
            // 加载相关景点详情
            if (data.locationId) {
              await this.loadRelatedLocations(data.locationId);
            }
          }
        } else {
          uni.showToast({
            title: response.message || '获取作品详情失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('加载作品详情失败:', error);
        // 不重复显示toast，因为http拦截器已经处理了401错误的跳转
        // 只有在非401错误时才显示自定义提示
        if (error.statusCode !== 401) {
          uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
          });
        }
      } finally {
        this.loading = false;
      }
    },
    
    // 加载相关景点详情
    async loadRelatedLocations(locationIds) {
      try {
        // locationIds可能是逗号分隔的字符串
        const ids = String(locationIds).split(',').map(id => id.trim()).filter(id => id);
        
        if (ids.length === 0) {
          console.log('没有关联的景点ID')
          return;
        }
        
        console.log('开始加载景点详情，IDs:', ids)
        
        // 逐个获取location详情
        const locationPromises = ids.map(id => getLocationById(parseInt(id)));
        const results = await Promise.allSettled(locationPromises);
        
        // 过滤成功的结果
        this.relatedLocations = results
          .filter(r => r.status === 'fulfilled' && r.value?.code === 200 && r.value?.data)
          .map(r => {
            const location = r.value.data
            console.log('景点数据:', location.name, {
              cover: location.cover,
              thumbCover: location.thumbCover,
              image: location.image,
              thumbImage: location.thumbImage
            })
            return location
          });
        
        console.log('成功加载景点数量:', this.relatedLocations.length)
          
      } catch (error) {
        console.warn('加载相关景点失败:', error);
        // 静默失败，不影响主要内容显示
      }
    },
    
    // 获取景点封面图URL
    getLocationCoverUrl(location) {
      if (!location) {
        console.log('景点数据为空，使用默认封面')
        return '';
      }
      
      // 优先使用cover字段，然后是image、thumbCover、thumbImage
      let coverUrl = location.cover || location.image || location.thumbCover || location.thumbImage;
      
      console.log('处理景点封面:', location.name, '原始URL:', coverUrl)
      
      // 如果coverUrl是数组，取第一个
      if (Array.isArray(coverUrl) && coverUrl.length > 0) {
        coverUrl = coverUrl[0];
        console.log('从数组中取第一个:', coverUrl)
      }
      
      // 如果coverUrl是逗号分隔的字符串，取第一个
      if (typeof coverUrl === 'string' && coverUrl.includes(',')) {
        coverUrl = coverUrl.split(',')[0].trim();
        console.log('从逗号分隔字符串中取第一个:', coverUrl)
      }
      
      // 使用getFileUrl处理URL
      const processedUrl = coverUrl ? getFileUrl(coverUrl) : '';
      
      console.log('最终处理后的URL:', processedUrl)
      
      return processedUrl || '';
    },
    
    // 跳转到景点详情
    goToLocationDetail(id) {
      uni.navigateTo({
        url: `/pages/location/detail?id=${id}`
      });
    },
    previewImage(index) {
      if (this.bannerImages && this.bannerImages.length > 0) {
        uni.previewImage({
          urls: this.bannerImages,
          current: index
        })
      } else if (this.posterUrl) {
        uni.previewImage({
          urls: [this.posterUrl],
          current: 0
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.film-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx; /* Adjust based on NavBar height */
  box-sizing: border-box;
}

.header-section {
  position: relative;
  height: 400rpx;
  overflow: hidden;
}

.poster-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  filter: blur(20px);
  opacity: 0.6;
  transform: scale(1.2);
}

.header-content {
  position: relative;
  z-index: 1;
  display: flex;
  padding: 40rpx 32rpx;
  gap: 32rpx;
  height: 100%;
  box-sizing: border-box;
  background: rgba(0, 0, 0, 0.3);
}

.poster-img {
  width: 200rpx;
  height: 280rpx;
  border-radius: 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.3);
  flex-shrink: 0;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  color: #fff;
  padding-bottom: 10rpx;
}

.film-title {
  font-size: 40rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
}

.film-type, .film-date {
  font-size: 26rpx;
  opacity: 0.9;
  margin-bottom: 8rpx;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.5);
}

.info-section, .scenes-section, .stage-photos-section {
  background: #fff;
  margin: 32rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.stage-swiper {
  width: 100%;
  height: 400rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.stage-image {
  width: 100%;
  height: 100%;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24rpx;
  padding-left: 16rpx;
  border-left: 8rpx solid #ef4444;
  line-height: 1;
}

.description {
  font-size: 28rpx;
  color: #4b5563;
  line-height: 1.8;
  margin-bottom: 24rpx;
  display: block;
}

.meta-row {
  display: flex;
  margin-bottom: 12rpx;
  font-size: 28rpx;
}

.label {
  color: #6b7280;
  width: 150rpx;
  flex-shrink: 0;
}

.value {
  color: #1f2937;
  flex: 1;
}

.scene-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.scene-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx;
  background: #f9fafb;
  border-radius: 12rpx;
}

.scene-name {
  font-size: 28rpx;
  color: #374151;
  font-weight: 500;
}

/* 景点卡片样式 */
.location-cards {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.location-card {
  display: flex;
  gap: 16rpx;
  padding: 16rpx;
  background: #f9fafb;
  border-radius: 12rpx;
  transition: all 0.2s;
}

.location-card:active {
  background: #f3f4f6;
  transform: scale(0.98);
}

.location-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
  object-fit: cover;
}

.location-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8rpx;
  min-width: 0;
}

.location-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.location-address {
  font-size: 24rpx;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.loading-wrapper, .error-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #9ca3af;
}

.error-wrapper {
  color: #ef4444;
}
</style>
