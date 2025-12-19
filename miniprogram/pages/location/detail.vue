<template>
  <view class="location-detail-page">
    <NavBar :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y v-if="!loading && location">
      <!-- 场地封面图片/轮播图 -->
      <view class="cover-section">
        <swiper 
          v-if="bannerImages.length > 1" 
          class="banner-swiper" 
          circular 
          indicator-dots 
          autoplay 
          :interval="4000" 
          :duration="500"
          indicator-active-color="#6366f1"
          indicator-color="rgba(255, 255, 255, 0.5)"
        >
          <swiper-item v-for="(img, index) in bannerImages" :key="index">
            <image 
              :src="img" 
              class="cover-image" 
              mode="aspectFill" 
              @click="previewImage(index)"
            ></image>
          </swiper-item>
        </swiper>
        <image 
          v-else-if="bannerImages.length === 1"
          :src="bannerImages[0]" 
          class="cover-image" 
          mode="aspectFill" 
          @click="previewImage(0)"
        ></image>
        <view v-else class="cover-placeholder">
          <uni-icons type="image" size="60" color="#d1d5db"></uni-icons>
          <text class="placeholder-text">暂无图片</text>
        </view>
      </view>

      <!-- 场地基本信息 -->
      <view class="info-card">
        <view class="card-header">
          <text class="location-name">{{ location.name }}</text>
          <view class="location-badge">{{ getCategoryLabel(location.type) }}</view>
        </view>
      </view>

      <!-- 场地描述 -->
      <view class="info-card">
        <view class="card-title">场地描述</view>
        <text class="card-content">{{ location.locationDescription || '暂无描述' }}</text>
      </view>

      <!-- 联系信息 -->
      <view class="info-card">
        <view class="card-title">联系信息</view>
        
        <!-- 场地联系人 -->
        <view class="contact-section">
          <view class="section-subtitle">场地负责人</view>
          <view class="info-item">
            <uni-icons type="person" size="18" color="#6b7280"></uni-icons>
            <text class="info-label">姓名：</text>
            <text class="info-value">{{ location.locationPrincipalName || '暂无' }}</text>
          </view>
          <view class="info-item">
            <uni-icons type="phone" size="18" color="#6b7280"></uni-icons>
            <text class="info-label">电话：</text>
            <text class="info-value">{{ location.locationPrincipalPhone || '暂无' }}</text>
          </view>
        </view>

        <view class="divider" v-if="location.govPrincipalName || location.govPrincipalPhone"></view>

        <!-- 政府联系人 -->
        <view class="contact-section" v-if="location.govPrincipalName || location.govPrincipalPhone">
          <view class="section-subtitle">政府联络员</view>
          <view class="info-item">
            <uni-icons type="person" size="18" color="#6b7280"></uni-icons>
            <text class="info-label">姓名：</text>
            <text class="info-value">{{ location.govPrincipalName }}</text>
          </view>
          <view class="info-item">
            <uni-icons type="phone" size="18" color="#6b7280"></uni-icons>
            <text class="info-label">电话：</text>
            <text class="info-value">{{ location.govPrincipalPhone }}</text>
          </view>
        </view>

        <view class="divider"></view>

        <view class="info-item" style="margin-top: 24rpx;">
          <uni-icons type="location" size="18" color="#6b7280"></uni-icons>
          <text class="info-label">地址：</text>
          <text class="info-value">{{ location.address }}</text>
          <view class="nav-btn" @click="openLocation">
            <uni-icons type="paperplane" size="14" color="#fff"></uni-icons>
            <text>导航</text>
          </view>
        </view>
      </view>

      <!-- 价格信息 -->
      <view class="info-card">
        <view class="card-title">价格信息</view>
        <view class="price-wrapper">
          <text class="price-value">¥{{ location.price }}</text>
          <text class="price-unit">/天</text>
        </view>
      </view>

      <!-- 设施信息 -->
      <view class="info-card" v-if="location.facilities">
        <view class="card-title">配套设施</view>
        <view class="info-item">
          <uni-icons type="gear" size="18" color="#10b981"></uni-icons>
          <text class="info-value">{{ location.facilities }}</text>
        </view>
      </view>

      <!-- 相关影视 -->
      <view class="info-card" v-if="relatedDramas.length > 0">
        <view class="card-title">相关影视</view>
        <view class="drama-list">
          <view 
            v-for="drama in relatedDramas" 
            :key="drama.id" 
            class="drama-item"
            @click="goToDramaDetail(drama.id)"
          >
            <image 
              v-if="drama.poster"
              :src="getFileUrl(drama.poster)" 
              class="drama-poster" 
              mode="aspectFill"
            ></image>
            <view class="drama-info">
              <text class="drama-name">{{ drama.name }}</text>
              <text class="drama-type" v-if="drama.type">{{ drama.type }}</text>
            </view>
            <uni-icons type="right" size="16" color="#9ca3af"></uni-icons>
          </view>
        </view>
      </view>

    </scroll-view>

    <view v-if="loading" class="loading-wrapper">
      <Loading></Loading>
    </view>

    <view v-if="!loading && !location" class="empty-wrapper">
      <Empty text="场地不存在"></Empty>
    </view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
import { getLocationById, getDramaById } from '../../services/backend-api'
// 导入文件URL处理函数
import { getFileUrl } from '../../utils'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      location: null,
      loading: false,
      relatedDramas: [], // 相关影视作品列表
      categories: [
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
  computed: {
    // 处理封面图片URL
    coverImageUrl() {
      if (!this.location) return ''
      return getFileUrl(this.location.image || this.location.thumbImage)
    },
    // 处理轮播图图片
    bannerImages() {
      if (!this.location) return []
      
      let images = []
      
      // 1. 如果有images数组，优先使用
      if (this.location.images && Array.isArray(this.location.images) && this.location.images.length > 0) {
        images = this.location.images.map(img => getFileUrl(img))
      } 
      // 2. 如果images是逗号分隔的字符串
      else if (this.location.images && typeof this.location.images === 'string') {
        images = this.location.images.split(',').map(img => getFileUrl(img))
      }
      
      // 3. 如果没有images列表，尝试使用封面图/image字段
      if (images.length === 0) {
        const cover = this.location.image || this.location.thumbImage || this.location.cover
        if (Array.isArray(cover)) {
          images = cover.map(img => getFileUrl(img))
        } else if (typeof cover === 'string' && cover) {
          images = cover.split(',').map(img => getFileUrl(img))
        }
      }
      
      // 4. 去重
      images = [...new Set(images)].filter(img => img)
      
      // 用户要求：详情页面展示的图片是除了第一个以外的图片
      // 如果有多张图片，移除第一张（通常是列表封面）；如果只有一张，保留显示
      if (images.length > 1) {
        return images.slice(1)
      }
      
      return images
    }
  },
  onLoad(options) {
    const id = parseInt(options.id)
    if (id) {
      this.loadLocation(id)
    }
  },
  methods: {
    getFileUrl,
    getCategoryLabel(value) {
      if (!value) return ''
      const category = this.categories.find(c => c.value === value)
      return category ? category.label : value
    },
    async loadLocation(id) {
      this.loading = true
      try {
        const res = await getLocationById(id)
        if (res.code === 200 && res.data) {
          this.location = res.data
          
          // Mock coordinates if missing
          if (!this.location.latitude || !this.location.longitude) {
            this.location.latitude = 30.0 + Math.random() * 0.1
            this.location.longitude = 103.0 + Math.random() * 0.1
          }
          
          // Mock bestShootingTime if missing
          if (!this.location.bestShootingTime) {
            this.location.bestShootingTime = '春秋两季，清晨或傍晚光线最佳'
          }

          // 加载相关影视作品
          if (this.location.dramaId) {
            await this.loadRelatedDramas(this.location.dramaId)
          }
        } else {
          uni.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载场地失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    async loadRelatedDramas(dramaIds) {
      try {
        // dramaIds可能是逗号分隔的字符串
        const ids = String(dramaIds).split(',').map(id => id.trim()).filter(id => id)
        
        if (ids.length === 0) return
        
        // 逐个获取drama详情
        const dramaPromises = ids.map(id => getDramaById(parseInt(id)))
        const results = await Promise.allSettled(dramaPromises)
        
        // 过滤成功的结果
        this.relatedDramas = results
          .filter(r => r.status === 'fulfilled' && r.value?.code === 200 && r.value?.data)
          .map(r => ({
            id: r.value.data.id,
            name: r.value.data.name,
            poster: r.value.data.image || r.value.data.cover || r.value.data.poster,
            type: r.value.data.type || '影视作品'
          }))
      } catch (error) {
        console.warn('加载相关影视失败:', error)
        this.relatedDramas = []
      }
    },
    goToDramaDetail(id) {
      uni.navigateTo({
        url: `/pages/films/detail?id=${id}`
      })
    },
    openLocation() {
      if (!this.location || !this.location.latitude || !this.location.longitude) {
        uni.showToast({
          title: '暂无位置信息',
          icon: 'none'
        })
        return
      }
      
      uni.openLocation({
        latitude: parseFloat(this.location.latitude),
        longitude: parseFloat(this.location.longitude),
        name: this.location.name,
        address: this.location.address,
        success: function () {
          console.log('success');
        }
      });
    },
    previewImage(current = 0) {
      if (this.bannerImages.length === 0) {
        uni.showToast({
          title: '暂无图片可预览',
          icon: 'none'
        })
        return
      }
      
      // 使用处理过的URL进行预览
      // 如果current是索引（数字），则使用该索引对应的图片作为当前图片
      // 如果current是事件对象（点击image触发），则默认使用第一张
      let currentUrl = this.bannerImages[0]
      if (typeof current === 'number' && current >= 0 && current < this.bannerImages.length) {
        currentUrl = this.bannerImages[current]
      }
      
      uni.previewImage({
          urls: this.bannerImages,
          current: currentUrl
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.location-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  width: 100%;
  padding: 0 32rpx 32rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  /* 兼容火狐浏览器 */
  scrollbar-width: none;
  /* 兼容IE浏览器 */
  -ms-overflow-style: none;
}

.cover-section {
  width: 100%;
  margin: 32rpx 0;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  height: 400rpx; /* 固定高度 */
}

.banner-swiper {
  width: 100%;
  height: 100%;
}

.cover-image {
  width: 100%;
  height: 100%;
  display: block;
  transition: transform 0.3s ease;
}

.cover-image:active {
  transform: scale(0.98);
}

.cover-placeholder {
  width: 100%;
  height: 400rpx;
  background: #f9fafb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.placeholder-text {
  font-size: 28rpx;
  color: #9ca3af;
}

.info-card {
  width: 100%;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
  
  &:first-child {
    margin-top: 0;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.location-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
}

.location-badge {
  background: #eef2ff;
  color: #6366f1;
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  margin-left: 16rpx;
}

.location-status {
  display: inline-block;
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  background: #fee2e2;
  color: #dc2626;
  
  &.status-available {
    background: #d1fae5;
    color: #059669;
  }
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24rpx;
}

.card-content {
  display: block;
  font-size: 28rpx;
  line-height: 1.8;
  color: #374151;
  white-space: pre-wrap;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-bottom: 20rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.info-label {
  font-size: 28rpx;
  color: #6b7280;
  min-width: 120rpx;
}

.info-value {
  font-size: 28rpx;
  color: #1f2937;
  flex: 1;
  word-break: break-all;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx 16rpx;
  background: #6366f1;
  border-radius: 32rpx;
  color: #fff;
  font-size: 22rpx;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.nav-btn:active {
  opacity: 0.9;
}

.price-wrapper {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.price-value {
  font-size: 48rpx;
  font-weight: 600;
  color: #1f2937;
}

.price-unit {
  font-size: 28rpx;
  color: #6b7280;
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 32rpx;
}



.highlight {
  color: #f59e0b;
  font-weight: 600;
}

.contact-section {
  margin-bottom: 24rpx;
}

.section-subtitle {
  font-size: 26rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16rpx;
  padding-left: 16rpx;
  border-left: 6rpx solid #6366f1;
}

.divider {
  height: 1rpx;
  background: #e5e7eb;
  margin: 24rpx 0;
}

.drama-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.drama-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  background: #f9fafb;
  border-radius: 12rpx;
  transition: all 0.3s;
}

.drama-item:active {
  background: #f3f4f6;
  transform: scale(0.98);
}

.drama-poster {
  width: 100rpx;
  height: 140rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
  object-fit: cover;
}

.drama-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}

.drama-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.drama-type {
  font-size: 24rpx;
  color: #6b7280;
}
</style>

