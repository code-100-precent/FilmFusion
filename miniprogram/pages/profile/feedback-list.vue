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
      <view v-else-if="!loading && feedbacks.length === 0" class="empty-wrapper">
        <Empty text="暂无反馈记录"></Empty>
      </view>
      <view v-else-if="feedbacks.length > 0">
        <view
          v-for="feedback in feedbacks"
          :key="feedback.id"
          class="feedback-card"
        >
          <view class="feedback-header">
            <view class="feedback-type">{{ feedback.type }}</view>
            <view class="feedback-status" :class="statusClassObj[feedback.status] || 'status-pending'">
              {{ statusTextMap[feedback.status] || '待处理' }}
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
import { getMyFeedbackPage } from '../../services/backend-api'

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
      hasMore: true,
      statusClassObj: {
        'PENDING': 'status-pending',
        'PROCESSING': 'status-processing',
        'RESOLVED': 'status-resolved'
      },
      statusTextMap: {
        'PENDING': '待处理',
        'PROCESSING': '处理中',
        'RESOLVED': '已解决'
      }
    }
  },
  onLoad() {
    console.log('反馈列表页面加载')
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

        console.log('反馈列表响应:', res)

        if (res && (res.code === 200 || res.code === undefined)) {
          // 处理响应数据 - 兼容多种数据格式
          let dataList = []
          let pagination = {}
          
          // 情况1: 标准格式 { code: 200, data: [], pagination: {} }
          if (res.data && Array.isArray(res.data)) {
            dataList = res.data
            pagination = res.pagination || {}
          }
          // 情况2: 数据直接在data字段中但不是数组
          else if (res.data && !Array.isArray(res.data)) {
            dataList = [res.data]
            pagination = res.pagination || {}
          }
          // 情况3: 没有code字段，直接是数据
          else if (res.code === undefined) {
            // 可能是游标分页格式或其他格式
            if (res.records && Array.isArray(res.records)) {
              dataList = res.records
            } else if (Array.isArray(res)) {
              dataList = res
            }
            pagination = res.pagination || {}
          }
          
          console.log('解析后的数据列表:', dataList)
          console.log('分页信息:', pagination)
          
          if (reset) {
            this.feedbacks = dataList
          } else {
            this.feedbacks = [...this.feedbacks, ...dataList]
          }
          this.total = pagination.totalItems || (pagination.total || 0)
          this.hasMore = this.feedbacks.length < this.total
          
          console.log('当前反馈列表:', this.feedbacks)
          console.log('总数:', this.total)
          console.log('是否还有更多:', this.hasMore)
        } else {
          console.warn('响应格式异常:', res)
          if (reset) {
            this.feedbacks = []
          }
          // 显示错误提示，但不要让页面一直处于加载状态
          uni.showToast({
            title: '数据加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载反馈失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
        if (reset) {
          this.feedbacks = []
        }
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
    formatDate(dateStr) {
      if (!dateStr) return ''
      let date
      if (Array.isArray(dateStr)) {
        // 处理数组格式 [2025, 11, 14, 0, 46, 35]
        if (dateStr.length >= 6) {
          const year = dateStr[0]
          const month = String(dateStr[1]).padStart(2, '0')
          const day = String(dateStr[2]).padStart(2, '0')
          const hour = String(dateStr[3] || 0).padStart(2, '0')
          const minute = String(dateStr[4] || 0).padStart(2, '0')
          const second = String(dateStr[5] || 0).padStart(2, '0')
          return `${year}-${month}-${day} ${hour}:${minute}:${second}`
        } else if (dateStr.length >= 3) {
          const year = dateStr[0]
          const month = String(dateStr[1]).padStart(2, '0')
          const day = String(dateStr[2]).padStart(2, '0')
          return `${year}-${month}-${day}`
        } else {
          return ''
        }
      } else if (typeof dateStr === 'string') {
        date = new Date(dateStr)
        if (isNaN(date.getTime())) return ''
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hour = String(date.getHours()).padStart(2, '0')
        const minute = String(date.getMinutes()).padStart(2, '0')
        const second = String(date.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hour}:${minute}:${second}`
      } else {
        return ''
      }
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
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  /* 兼容火狐浏览器 */
  scrollbar-width: none;
  /* 兼容IE浏览器 */
  -ms-overflow-style: none;
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
  
  &.status-rejected {
    background: #fee2e2;
    color: #dc2626;
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

