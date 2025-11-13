<template>
  <view class="services-page">
    <NavBar title="协拍服务" :show-back="false"></NavBar>

    <view class="content">
      <!-- 搜索栏 -->
      <view class="search-bar">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#9ca3af"></uni-icons>
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索服务名称、描述..."
            @confirm="handleSearch"
            @input="handleSearch"
          />
        </view>
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
        <view v-else>
          <view
            v-for="shoot in shoots"
            :key="shoot.id"
            class="shoot-card"
            @click="goToDetail(shoot.id)"
          >
            <view class="shoot-header">
              <view class="shoot-title-row">
                <text class="shoot-name">{{ shoot.name }}</text>
                <view class="shoot-status" :class="{ 'status-online': shoot.status === 1 }">
                  {{ shoot.status === 1 ? '上线' : '下线' }}
                </view>
              </view>
            </view>
            <text class="shoot-desc">{{ shoot.description }}</text>
            <view class="shoot-info">
              <view class="info-item">
                <uni-icons type="location" size="16" color="#6366f1"></uni-icons>
                <text>{{ shoot.address }}</text>
              </view>
              <view class="info-item">
                <uni-icons type="phone" size="16" color="#6366f1"></uni-icons>
                <text>{{ shoot.phone }}</text>
              </view>
              <view class="info-item">
                <uni-icons type="person" size="16" color="#6366f1"></uni-icons>
                <text>{{ shoot.contactName }}</text>
              </view>
            </view>
            <view class="shoot-footer">
              <text class="shoot-price">¥{{ shoot.price }}</text>
              <text class="view-detail">查看详情</text>
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

    <!-- 底部导航栏 -->
    <TabBar :current="'services'"></TabBar>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import TabBar from '../../components/TabBar/TabBar.vue'
import Loading from '../../components/Loading/Loading.vue'
import Empty from '../../components/Empty/Empty.vue'
import { getShootPage } from '../../services/api'

export default {
  components: {
    NavBar,
    TabBar,
    Loading,
    Empty
  },
  data() {
    return {
      keyword: '',
      shoots: [],
      current: 1,
      size: 10,
      total: 0,
      loading: false,
      refreshing: false,
      hasMore: true
    }
  },
  onLoad() {
    this.loadShoots()
  },
  methods: {
    async loadShoots(reset = false) {
      if (this.loading) return

      if (reset) {
        this.current = 1
        this.shoots = []
        this.hasMore = true
      }

      this.loading = true
      try {
        const res = await getShootPage({
          current: this.current,
          size: this.size,
          keyword: this.keyword || undefined
        })

        if (res.code === 200) {
          // 后端返回格式: { code: 200, message: "请求成功", data: [...], pagination: {...} }
          const dataList = Array.isArray(res.data) ? res.data : []
          const pagination = res.pagination || {}
          
          if (reset) {
            this.shoots = dataList
          } else {
            this.shoots = [...this.shoots, ...dataList]
          }
          this.total = pagination.totalItems || 0
          this.hasMore = this.shoots.length < this.total
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
    handleSearch() {
      this.loadShoots(true)
    },
    handleRefresh() {
      this.refreshing = true
      this.loadShoots(true)
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.current++
      this.loadShoots()
    },
    goToDetail(id) {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.services-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  padding: 20rpx 32rpx;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  width: 100%;
}

.search-bar {
  margin-top: 24rpx;
  margin-bottom: 32rpx;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 0 24rpx;
  height: 80rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #1f2937;
}

.shoot-list {
  height: calc(100vh - 88rpx - 200rpx);
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}

.shoot-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  width: 100%;
  box-sizing: border-box;
}

.shoot-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.shoot-header {
  margin-bottom: 20rpx;
}

.shoot-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.shoot-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
}

.shoot-status {
  padding: 6rpx 16rpx;
  background: #fee2e2;
  color: #ef4444;
  font-size: 22rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.status-online {
  background: #d1fae5;
  color: #10b981;
}

.shoot-desc {
  display: block;
  font-size: 28rpx;
  color: #6b7280;
  line-height: 1.8;
  margin-bottom: 24rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.shoot-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 24rpx;
  padding: 20rpx;
  background: #f9fafb;
  border-radius: 12rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 26rpx;
  color: #374151;
}

.shoot-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 1rpx solid #f3f4f6;
}

.shoot-price {
  font-size: 32rpx;
  font-weight: 700;
  color: #f59e0b;
}

.view-detail {
  font-size: 26rpx;
  color: #6366f1;
  font-weight: 500;
}

.load-more,
.no-more {
  text-align: center;
  padding: 40rpx 0;
  font-size: 26rpx;
  color: #9ca3af;
}
</style>
