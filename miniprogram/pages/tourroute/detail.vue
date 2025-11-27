<template>
  <view class="detail-page">
    <NavBar :show-back="true" :title="route.name"></NavBar>

    <scroll-view class="content" scroll-y>
      <view v-if="loading" class="loading-wrapper">
        <Loading></Loading>
      </view>
      <view v-else-if="route">
        <!-- 封面图 -->
        <view class="cover-wrapper">
          <image :src="route.cover || defaultCover" class="cover" mode="aspectFill"></image>
          <view class="cover-overlay">
            <view class="theme-tag">{{ route.theme }}</view>
          </view>
        </view>

        <!-- 基本信息 -->
        <view class="info-section">
          <view class="section-title">{{ route.name }}</view>
          <view class="section-content">
            <text>{{ route.description }}</text>
          </view>
        </view>

        <!-- 线路特色 -->
        <view class="info-section">
          <view class="section-title">线路特色</view>
          <view class="section-content">
            <view class="feature-item">
              <uni-icons type="star-filled" size="16" color="#fbbf24"></uni-icons>
              <text>{{ route.features }}</text>
            </view>
          </view>
        </view>

        <!-- 路线地图 -->
        <view class="info-section">
          <view class="section-title">路线地图</view>
          <view class="map-wrapper">
            <map class="route-map" :latitude="29.98" :longitude="102.98" :scale="9"></map>
          </view>
        </view>

        <!-- 影视场景 -->
        <view class="info-section">
          <view class="section-title">影视场景</view>
          <view class="section-content">
            <view class="info-item">
              <uni-icons type="videocam-filled" size="16" color="#ef4444"></uni-icons>
              <text>相关作品：{{ route.ipWorks }}</text>
            </view>
          </view>
        </view>

        <!-- 交通方式 -->
        <view class="info-section">
          <view class="section-title">交通方式</view>
          <view class="section-content">
            <view class="info-item">
              <uni-icons type="location-filled" size="16" color="#6366f1"></uni-icons>
              <text>{{ route.transportInfo }}</text>
            </view>
          </view>
        </view>

        <!-- 住宿推荐 -->
        <view class="info-section">
          <view class="section-title">住宿推荐</view>
          <view class="section-content">
            <view class="info-item">
              <uni-icons type="home" size="16" color="#10b981"></uni-icons>
              <text>{{ route.accommodation }}</text>
            </view>
          </view>
        </view>

        <!-- 美食推荐 -->
        <view class="info-section">
          <view class="section-title">美食推荐</view>
          <view class="section-content">
            <view class="info-item">
              <uni-icons type="fire" size="16" color="#ef4444"></uni-icons>
              <text>{{ route.foodRecommendation }}</text>
            </view>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="action-buttons">
          <button class="btn btn-primary" @click="handleShare">
            <uni-icons type="share" size="18" color="white"></uni-icons>
            分享线路
          </button>
          <button class="btn btn-secondary" @click="handleCollect">
            <uni-icons type="heart" size="18" :color="collected ? '#ef4444' : 'white'"></uni-icons>
            {{ collected ? '已收藏' : '收藏' }}
          </button>
        </view>

        <view style="height: 20px;"></view>
      </view>
      <view v-else class="empty-wrapper">
        <Empty text="线路不存在"></Empty>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
import { getTourById } from '@/services/backend-api'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      route: null,
      loading: true,
      collected: false,
      defaultCover: 'https://via.placeholder.com/400x200?text=Tour+Route'
    }
  },
  onLoad(options) {
    const id = parseInt(options.id)
    if (id) {
      this.loadData(id)
    }
  },
  methods: {
    async loadData(id) {
      this.loading = true
      try {
        const res = await getTourById(id)
        
        if (res.code === 200 && res.data) {
          this.route = {
            id: res.data.id,
            name: res.data.name,
            description: res.data.description,
            theme: res.data.theme,
            features: res.data.features,
            cover: res.data.cover,
            transportInfo: res.data.transport,
            accommodation: res.data.hotel,
            foodRecommendation: res.data.food,
            ipWorks: res.data.ipWorks || '暂无相关影视作品',
            image: res.data.image,
            latitude: res.data.latitude || 30.075,
            longitude: res.data.longitude || 102.993
          }
        } else {
          uni.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          })
          this.route = null
        }
      } catch (error) {
        console.error('加载线路详情失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
        this.route = null
      } finally {
        this.loading = false
      }
    },
    handleShare() {
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    handleCollect() {
      this.collected = !this.collected
      uni.showToast({
        title: this.collected ? '已收藏' : '已取消收藏',
        icon: 'success'
      })
    }
  }
}
</script>

<style scoped lang="scss">
.detail-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f8fafc;
}

.content {
  flex: 1;
  overflow-y: auto;
}

.cover-wrapper {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
}

.cover {
  width: 100%;
  height: 100%;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.3) 100%);
  display: flex;
  align-items: flex-end;
  padding: 16px;
}

.theme-tag {
  background: rgba(99, 102, 241, 0.9);
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.info-section {
  background: white;
  margin: 12px 16px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.section-content {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
}

.feature-item,
.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
}

.feature-item:last-child,
.info-item:last-child {
  margin-bottom: 0;
}

.feature-item uni-icons,
.info-item uni-icons {
  margin-right: 8px;
  margin-top: 2px;
  flex-shrink: 0;
}

.feature-item text,
.info-item text {
  flex: 1;
}

.action-buttons {
  display: flex;
  gap: 12px;
  padding: 16px;
  margin: 12px 0;
}

.btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 8px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-secondary {
  background: white;
  color: #6366f1;
  border: 2px solid #6366f1;
}

.loading-wrapper,
.empty-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.map-wrapper {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
}

.route-map {
  width: 100%;
  height: 100%;
}
</style>

