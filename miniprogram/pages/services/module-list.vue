<template>
  <view class="module-list-page">
    <view class="gradient-bg"></view>
    
    <NavBar :show-back="true"></NavBar>

    <view class="content">
      <!-- 模块标题 -->
      <view class="page-header">
        <text class="page-title">{{ moduleName }}</text>
        <text class="page-subtitle">共 {{ shoots.length }} 个服务</text>
      </view>

      <!-- 服务列表 -->
      <scroll-view
        class="shoot-list"
        scroll-y
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="handleRefresh"
      >
        <view v-if="loading && shoots.length === 0" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="shoots.length === 0" class="empty-wrapper">
          <Empty text="暂无服务"></Empty>
        </view>
        <view v-else class="shoot-cards">
          <view
            v-for="(shoot, index) in shoots"
            :key="shoot.id"
            class="shoot-card"
            :style="{ 'animation-delay': index * 0.05 + 's' }"
            @click="goToDetail(shoot.id)"
          >
            <view class="shoot-cover">
              <image 
                v-if="getFileUrl(shoot.thumbImage || shoot.image)"
                :src="getFileUrl(shoot.thumbImage || shoot.image)" 
                class="cover-image" 
                mode="aspectFill"
              ></image>
              <view v-else class="cover-placeholder">
                <uni-icons type="image" size="40" color="#666666"></uni-icons>
              </view>
            </view>
            
            <view class="shoot-content">
              <text class="shoot-name">{{ shoot.name }}</text>
              <text class="shoot-desc">{{ shoot.description }}</text>
              <view class="shoot-info">
                <view class="info-item">
                  <uni-icons type="location" size="14" color="#999999"></uni-icons>
                  <text>{{ shoot.address }}</text>
                </view>
                <view class="info-item">
                  <uni-icons type="phone" size="14" color="#999999"></uni-icons>
                  <text>{{ shoot.phone }}</text>
                </view>
                <view class="info-item">
                  <uni-icons type="person" size="14" color="#999999"></uni-icons>
                  <text>{{ shoot.contactName }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view v-if="hasMore && !loading" class="load-more">
          <text>上拉加载更多</text>
        </view>
        <view v-if="!hasMore && shoots.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import Loading from '../../components/Loading/Loading.vue'
import Empty from '../../components/Empty/Empty.vue'
import { getShootPage } from '../../services/backend-api'
import { getFileUrl } from '../../utils'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      moduleId: null,
      moduleName: '',
      shoots: [],
      nextCursor: null,
      loading: false,
      refreshing: false,
      hasMore: true
    }
  },
  onLoad(options) {
    this.moduleId = parseInt(options.moduleId)
    this.moduleName = decodeURIComponent(options.moduleName || '服务列表')
    this.loadShoots()
  },
  methods: {
    async loadShoots(reset = false) {
      if (this.loading) return

      if (reset) {
        this.shoots = []
        this.nextCursor = null
        this.hasMore = true
      }

      this.loading = true
      try {
        const res = await getShootPage({
          moduleId: this.moduleId,
          cursor: reset ? null : this.nextCursor,
          size: 10
        })

        if (res && res.records) {
          const dataList = Array.isArray(res.records) ? res.records : []
          
          if (reset) {
            this.shoots = dataList
          } else {
            this.shoots = [...this.shoots, ...dataList]
          }
          this.nextCursor = res.nextCursor
          this.hasMore = res.hasMore || false
        }
      } catch (error) {
        console.error('加载服务失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    
    handleRefresh() {
      this.refreshing = true
      this.loadShoots(true)
    },
    
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.loadShoots()
    },
    
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/shoot/detail?id=${id}`
      })
    },
    
    getFileUrl(url) {
      return getFileUrl(url)
    }
  }
}
</script>

<style lang="scss" scoped>
.module-list-page {
  min-height: 100vh;
  background: #121212;
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
  background: linear-gradient(to top, #121212 0%, #000000 100%);
  z-index: 0;
}

.content {
  padding: 0 24rpx 32rpx;
  position: relative;
  z-index: 1;
  height: calc(100vh - 132rpx);
  display: flex;
  flex-direction: column;
}

.page-header {
  padding: 24rpx 0;
  flex-shrink: 0;
}

.page-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #D4AF37;
  display: block;
  margin-bottom: 8rpx;
}

.page-subtitle {
  font-size: 26rpx;
  color: #999999;
  display: block;
}

.shoot-list {
  flex: 1;
  min-height: 0;
  
  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.shoot-cards {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding-bottom: 20rpx;
}

.shoot-card {
  display: flex;
  background: #1E1E1E;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
  border: 1rpx solid rgba(255, 255, 255, 0.05);
  animation: fadeInUp 0.6s ease-out forwards;
  opacity: 0;
  transition: all 0.3s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.shoot-card:active {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.4);
}

.shoot-cover {
  width: 200rpx;
  height: 240rpx;
  background: #2C2C2C;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  display: block;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2C2C2C;
}

.shoot-content {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.shoot-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 8rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.shoot-desc {
  font-size: 26rpx;
  color: #CCCCCC;
  line-height: 1.6;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  flex: 1;
}

.shoot-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #999999;
  
  text {
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}

.load-more,
.no-more {
  text-align: center;
  padding: 40rpx 0;
  font-size: 26rpx;
  color: #999999;
}
</style>
