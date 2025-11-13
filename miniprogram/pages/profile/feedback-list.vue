<template>
  <view class="feedback-list-page">
    <NavBar title="我的反馈" :show-back="true"></NavBar>

    <scroll-view
      class="content"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view v-if="loading && feedbacks.length === 0" class="loading-wrapper">
        <Loading></Loading>
      </view>
      <view v-else-if="feedbacks.length === 0" class="empty-wrapper">
        <Empty text="暂无反馈记录"></Empty>
      </view>
      <view v-else>
        <view
          v-for="feedback in feedbacks"
          :key="feedback.id"
          class="feedback-card"
        >
          <view class="feedback-header">
            <view class="feedback-type">{{ feedback.type }}</view>
            <view class="feedback-status" :class="getStatusClass(feedback.status)">
              {{ getStatusText(feedback.status) }}
            </view>
          </view>
          <text class="feedback-content">{{ feedback.content }}</text>
          <view class="feedback-footer">
            <text class="feedback-time">{{ formatDate(feedback.createdAt) }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
import { getMyFeedbackPage } from '../../services/api'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      feedbacks: [],
      current: 1,
      size: 10,
      total: 0,
      loading: false,
      refreshing: false,
      hasMore: true
    }
  },
  onLoad() {
    this.loadFeedbacks()
  },
  methods: {
    async loadFeedbacks(reset = false) {
      if (this.loading) return

      if (reset) {
        this.current = 1
        this.feedbacks = []
        this.hasMore = true
      }

      this.loading = true
      try {
        const res = await getMyFeedbackPage({
          current: this.current,
          size: this.size
        })

        if (res.code === 200) {
          const dataList = Array.isArray(res.data) ? res.data : []
          const pagination = res.pagination || {}
          
          if (reset) {
            this.feedbacks = dataList
          } else {
            this.feedbacks = [...this.feedbacks, ...dataList]
          }
          this.total = pagination.totalItems || 0
          this.hasMore = this.feedbacks.length < this.total
        }
      } catch (error) {
        console.error('加载反馈失败:', error)
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
      this.loadFeedbacks(true)
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.current++
      this.loadFeedbacks()
    },
    getStatusClass(status) {
      if (status === 'PENDING') return 'status-pending'
      if (status === 'PROCESSING') return 'status-processing'
      if (status === 'RESOLVED') return 'status-resolved'
      return 'status-pending'
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待处理',
        'PROCESSING': '处理中',
        'RESOLVED': '已解决'
      }
      return statusMap[status] || '待处理'
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
      } else if (typeof dateStr === 'string') {
        date = new Date(dateStr)
      } else {
        return ''
      }
      
      if (isNaN(date.getTime())) return ''
      
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
  }
}
</script>

<style lang="scss" scoped>
.feedback-list-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  width: 100%;
  height: calc(100vh - 132rpx);
  padding: 24rpx 32rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.feedback-card {
  width: 100%;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
}

.feedback-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.feedback-type {
  background: #eef2ff;
  color: #6366f1;
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.feedback-status {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  
  &.status-pending {
    background: #fef3c7;
    color: #f59e0b;
  }
  
  &.status-processing {
    background: #dbeafe;
    color: #3b82f6;
  }
  
  &.status-resolved {
    background: #d1fae5;
    color: #059669;
  }
}

.feedback-content {
  display: block;
  font-size: 28rpx;
  line-height: 1.6;
  color: #374151;
  margin-bottom: 20rpx;
  white-space: pre-wrap;
}

.feedback-footer {
  display: flex;
  justify-content: flex-end;
}

.feedback-time {
  font-size: 24rpx;
  color: #9ca3af;
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 32rpx;
}
</style>

