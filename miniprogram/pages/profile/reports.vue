<template>
  <view class="reports-page">
    <NavBar title="我的报备" :show-back="true"></NavBar>

    <scroll-view
      class="content"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view v-if="loading && reports.length === 0" class="loading-wrapper">
        <Loading></Loading>
      </view>
      <view v-else-if="reports.length === 0" class="empty-wrapper">
        <Empty text="暂无报备记录"></Empty>
      </view>
      <view v-else>
        <view
          v-for="report in reports"
          :key="report.id"
          class="report-card"
        >
          <view class="report-header">
            <text class="report-name">{{ report.name }}</text>
            <view class="report-type">{{ report.type }}</view>
          </view>
          <view class="report-info">
            <text class="info-item">类型：{{ report.genre }}</text>
            <text class="info-item">集数：{{ report.episodes }}集</text>
            <text class="info-item">投资金额：¥{{ report.investAmount }}</text>
          </view>
          <view class="report-footer">
            <text class="report-time">{{ formatDate(report.createdAt) }}</text>
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
import { getMyReportPage } from '../../services/api'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      reports: [],
      current: 1,
      size: 10,
      total: 0,
      loading: false,
      refreshing: false,
      hasMore: true
    }
  },
  onLoad() {
    this.loadReports()
  },
  methods: {
    async loadReports(reset = false) {
      if (this.loading) return

      if (reset) {
        this.current = 1
        this.reports = []
        this.hasMore = true
      }

      this.loading = true
      try {
        const res = await getMyReportPage({
          current: this.current,
          size: this.size
        })

        if (res.code === 200) {
          const dataList = Array.isArray(res.data) ? res.data : []
          const pagination = res.pagination || {}
          
          if (reset) {
            this.reports = dataList
          } else {
            this.reports = [...this.reports, ...dataList]
          }
          this.total = pagination.totalItems || 0
          this.hasMore = this.reports.length < this.total
        }
      } catch (error) {
        console.error('加载报备失败:', error)
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
      this.loadReports(true)
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.current++
      this.loadReports()
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
.reports-page {
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

.report-card {
  width: 100%;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
}

.report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.report-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
}

.report-type {
  background: #eef2ff;
  color: #6366f1;
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  margin-left: 16rpx;
}

.report-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.info-item {
  font-size: 26rpx;
  color: #6b7280;
}

.report-footer {
  display: flex;
  justify-content: flex-end;
}

.report-time {
  font-size: 24rpx;
  color: #9ca3af;
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 32rpx;
}
</style>

