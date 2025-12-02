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
        <button class="add-btn" @click="goToFiling">新增报备</button>
      </view>
      <view v-else>
        <view
          v-for="report in reports"
          :key="report.id"
          class="report-card"
          @click="showDetailModal(report)"
        >
          <view class="report-header">
            <text class="report-name">{{ report.name }}</text>
            <view class="status-badge" :class="report.statusClass">
              {{ report.statusText }}
            </view>
          </view>
          <view class="report-info">
            <text class="info-item">类型：{{ report.genre }}</text>
            <text class="info-item">集数：{{ report.episodes }}集</text>
            <text class="info-item">投资金额：¥{{ report.investAmount }}</text>
          </view>
          <view class="report-footer">
            <text class="report-time">{{ formatDate(report.createdAt) }}</text>
            <view class="report-actions">
              <text class="action-btn edit-btn" @click.stop="editReport(report.id)">编辑</text>
              <text class="action-btn delete-btn" @click.stop="deleteReport(report.id)">删除</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 报备详情弹窗 -->
    <view class="detail-modal" v-if="showModal">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">报备详情</text>
          <text class="close-btn" @click="closeModal">×</text>
        </view>
        <scroll-view class="modal-body" scroll-y>
          <view class="detail-section" v-if="selectedReport">
            <view class="detail-header">
              <text class="report-name">{{ selectedReport.name }}</text>
              <view class="status-badge" :class="selectedReport.statusClass">
                {{ selectedReport.statusText }}
              </view>
            </view>
            
            <view class="info-group">
              <text class="group-title">基本信息</text>
              <view class="info-item">
                <text class="info-label">报备类型：</text>
                <text class="info-value">{{ selectedReport.type }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">作品类型：</text>
                <text class="info-value">{{ selectedReport.genre }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">拍摄集数：</text>
                <text class="info-value">{{ selectedReport.episodes }}集</text>
              </view>
              <view class="info-item">
                <text class="info-label">投资金额：</text>
                <text class="info-value">¥{{ selectedReport.investAmount }}</text>
              </view>
              <view class="info-item" v-if="selectedReport.crewScale">
                <text class="info-label">剧组规模：</text>
                <text class="info-value">{{ selectedReport.crewScale }}</text>
              </view>
            </view>
            
            <view class="info-group">
              <text class="group-title">拍摄计划</text>
              <view class="info-item">
                <text class="info-label">预计开始：</text>
                <text class="info-value">{{ formatDate(selectedReport.startDate) }}</text>
              </view>
              <view class="info-item" v-if="selectedReport.endDate">
                <text class="info-label">预计结束：</text>
                <text class="info-value">{{ formatDate(selectedReport.endDate) }}</text>
              </view>
            </view>
            
            <view class="info-group">
              <text class="group-title">联系人信息</text>
              <view class="info-item">
                <text class="info-label">联系人：</text>
                <text class="info-value">{{ selectedReport.contact }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">联系电话：</text>
                <text class="info-value">{{ selectedReport.phoneNumber }}</text>
              </view>
            </view>
            
            <view class="info-group">
              <text class="group-title">报备信息</text>
              <view class="info-item">
                <text class="info-label">报备时间：</text>
                <text class="info-value">{{ formatDateTime(selectedReport.createdAt) }}</text>
              </view>
              <view class="info-item" v-if="selectedReport.updatedAt">
                <text class="info-label">更新时间：</text>
                <text class="info-value">{{ formatDateTime(selectedReport.updatedAt) }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
        <view class="modal-footer">
          <button class="edit-btn" @click="editReport(selectedReport.id)">编辑报备</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
// 使用真实后端API
import { getMyReportPage, deleteReport as apiDeleteReport } from '../../services/backend-api'

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
      hasMore: true,
      showModal: false,
      selectedReport: null
    }
  },
  onLoad() {
    this.loadReports()
  },
  computed: {
    processedReports() {
      return this.reports.map(report => ({
        ...report,
        statusText: this.getStatusText(report.status),
        statusClass: this.getStatusClass(report.status)
      }))
    }
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

        if (res.code === 200 && res.data) {
          const dataList = Array.isArray(res.data) ? res.data : []
          const pagination = res.pagination || { totalItems: 0 }
          
          // 预处理每个报备项，添加状态文本和类名
          const processedList = dataList.map(report => ({
            ...report,
            statusText: this.getStatusText(report.status),
            statusClass: this.getStatusClass(report.status)
          }))
          
          if (reset) {
            this.reports = processedList
          } else {
            this.reports = [...this.reports, ...processedList]
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
    },
    getStatusText(status) {
      const statusMap = {
        PENDING: '待审核',
        APPROVED: '已通过',
        REJECTED: '已驳回',
        PROCESSING: '处理中',
        // 兼容中文状态
        '未处理': '待审核',
        '待审核': '待审核',
        '已通过': '已通过',
        '已驳回': '已驳回',
        '处理中': '处理中',
        // 兼容数字状态 (假设 0:待审核, 1:已通过, 2:已驳回)
        0: '待审核',
        1: '已通过',
        2: '已驳回'
      }
      return statusMap[status] || status || '未知状态'
    },
    getStatusClass(status) {
      const classMap = {
        PENDING: 'status-pending',
        APPROVED: 'status-approved',
        REJECTED: 'status-rejected',
        PROCESSING: 'status-processing',
        // 兼容中文状态
        '未处理': 'status-pending',
        '待审核': 'status-pending',
        '已通过': 'status-approved',
        '已驳回': 'status-rejected',
        '处理中': 'status-processing',
        // 兼容数字状态
        0: 'status-pending',
        1: 'status-approved',
        2: 'status-rejected'
      }
      return classMap[status] || ''
    },
    formatDateTime(dateStr) {
      if (!dateStr) return '-'
      let date
      if (Array.isArray(dateStr)) {
        if (dateStr.length >= 3) {
          // 处理 [year, month, day, hour, minute, second]
          const year = dateStr[0]
          const month = dateStr[1] - 1
          const day = dateStr[2]
          const hour = dateStr[3] || 0
          const minute = dateStr[4] || 0
          const second = dateStr[5] || 0
          date = new Date(year, month, day, hour, minute, second)
        } else {
          return '-'
        }
      } else if (typeof dateStr === 'string') {
        date = new Date(dateStr)
      } else {
        return '-'
      }
      
      if (isNaN(date.getTime())) return '-'
      
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    showDetailModal(report) {
      // 确保selectedReport也包含处理过的状态信息
      this.selectedReport = {
        ...report,
        statusText: report.statusText || this.getStatusText(report.status),
        statusClass: report.statusClass || this.getStatusClass(report.status)
      }
      this.showModal = true
    },
    closeModal() {
      this.showModal = false
      this.selectedReport = null
    },
    editReport(id) {
      this.closeModal()
      setTimeout(() => {
        uni.navigateTo({
          url: `/pages/filing/filing?id=${id}`
        })
      }, 300)
    },
    async deleteReport(id) {
      uni.showModal({
        title: '提示',
        content: '确定要删除这条报备记录吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              // 直接调用deleteReport API删除
              const result = await apiDeleteReport(id)
              if (result.code === 200 || result.success) {
                uni.showToast({ title: '删除成功', icon: 'success' })
                // 重新加载数据
                this.loadReports(true)
              } else {
                uni.showToast({ 
                  title: result.message || '删除失败', 
                  icon: 'none' 
                })
              }
            } catch (error) {
              console.error('删除报备失败:', error)
              uni.showToast({ title: '删除失败，请稍后重试', icon: 'none' })
            }
          }
        }
      })
    },
    goToFiling() {
      uni.navigateTo({
        url: '/pages/filing/filing'
      })
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

.status-badge {
  padding: 6rpx 16rpx;
  border-radius: 10rpx;
  font-size: 24rpx;
  font-weight: 500;
}

.status-pending {
  background: #fef3c7;
  color: #d97706;
}

.status-approved {
  background: #d1fae5;
  color: #065f46;
}

.status-rejected {
  background: #fee2e2;
  color: #b91c1c;
}

.status-processing {
  background: #dbeafe;
  color: #1d4ed8;
}

/* 详情弹窗样式 */
.detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 40rpx;
}

.modal-content {
  background: #fff;
  border-radius: 16rpx;
  width: 100%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 2rpx solid #f3f4f6;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  font-size: 48rpx;
  color: #6b7280;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-body {
  flex: 1;
  padding: 32rpx;
  overflow-y: auto;
}

.modal-footer {
  padding: 24rpx 32rpx;
  border-top: 2rpx solid #f3f4f6;
  display: flex;
  justify-content: center;
}

.detail-section {
  width: 100%;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32rpx;
}

.info-group {
  margin-bottom: 32rpx;
}

.group-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16rpx;
  display: block;
}

.info-item {
  display: flex;
  margin-bottom: 16rpx;
  font-size: 26rpx;
}

.info-label {
  color: #6b7280;
  width: 200rpx;
  flex-shrink: 0;
}

.info-value {
  color: #1f2937;
  flex: 1;
  word-break: break-all;
}

.description {
  font-size: 26rpx;
  color: #4b5563;
  line-height: 40rpx;
  white-space: pre-wrap;
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
  justify-content: space-between;
  align-items: center;
}

.report-time {
  font-size: 24rpx;
  color: #9ca3af;
}

.report-actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  font-size: 26rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.edit-btn {
  color: #6366f1;
  background: #eef2ff;
}

.delete-btn {
  color: #ef4444;
  background: #fee2e2;
}

.add-btn {
  margin-top: 32rpx;
  width: 240rpx;
  height: 80rpx;
  background: #6366f1;
  color: #fff;
  font-size: 28rpx;
  border-radius: 12rpx;
  border: none;
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 32rpx;
}
</style>

