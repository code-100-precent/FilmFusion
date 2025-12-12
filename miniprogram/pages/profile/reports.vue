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
      <view v-else-if="reports.length > 0">
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
            
            <view class="info-group" v-if="hasFiles(selectedReport)">
              <text class="group-title">相关文件</text>
              <view class="file-item" v-if="selectedReport.shootPermit">
                <view class="file-info">
                  <uni-icons type="paperclip" size="20" color="#6366f1"></uni-icons>
                  <text class="file-name">影视拍摄许可证</text>
                </view>
                <view class="file-actions">
                  <text class="action-btn preview-btn" @click="previewFile(selectedReport.shootPermit, '影视拍摄许可证')">预览</text>
                  <text class="action-btn download-btn" @click="downloadFile(selectedReport.shootPermit, '影视拍摄许可证')">下载</text>
                </view>
              </view>
              <view class="file-item" v-if="selectedReport.approvalFile">
                <view class="file-info">
                  <uni-icons type="paperclip" size="20" color="#6366f1"></uni-icons>
                  <text class="file-name">立项审批文件</text>
                </view>
                <view class="file-actions">
                  <text class="action-btn preview-btn" @click="previewFile(selectedReport.approvalFile, '立项审批文件')">预览</text>
                  <text class="action-btn download-btn" @click="downloadFile(selectedReport.approvalFile, '立项审批文件')">下载</text>
                </view>
              </view>
              <view class="file-item" v-if="selectedReport.shootApply">
                <view class="file-info">
                  <uni-icons type="paperclip" size="20" color="#6366f1"></uni-icons>
                  <text class="file-name">协拍服务申请表</text>
                </view>
                <view class="file-actions">
                  <text class="action-btn preview-btn" @click="previewFile(selectedReport.shootApply, '协拍服务申请表')">预览</text>
                  <text class="action-btn download-btn" @click="downloadFile(selectedReport.shootApply, '协拍服务申请表')">下载</text>
                </view>
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
    console.log('报备列表页面加载')
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
          this.total = pagination.totalItems || (pagination.total || 0)
          this.hasMore = this.reports.length < this.total
        } else {
          console.warn('响应格式异常:', res)
          if (reset) {
            this.reports = []
          }
          // 显示错误提示，但不要让页面一直处于加载状态
          uni.showToast({
            title: '数据加载失败',
            icon: 'none'
          })
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
    },
    // 检查是否有文件
    hasFiles(report) {
      if (!report) return false
      return !!(report.shootPermit || report.approvalFile || report.shootApply)
    },
    // 预览文件
    previewFile(fileUrl, fileName) {
      console.log('预览文件被点击:', fileUrl, fileName)
      
      if (!fileUrl) {
        uni.showToast({
          title: '文件不存在',
          icon: 'none'
        })
        return
      }
      
      // 处理JSON格式的文件URL
      let actualFileUrl = fileUrl
      try {
        // 尝试解析JSON格式的URL
        if (fileUrl.includes('originUrl')) {
          // 处理可能被反引号包裹的JSON字符串
          let jsonString = fileUrl
          
          // 去除可能的前缀和后缀
          if (fileUrl.startsWith('`')) {
            jsonString = fileUrl.replace(/^`+/, '').replace(/`+$/, '') // 去掉首尾的所有反引号
          }
          
          // 处理可能包含的URL前缀
          if (jsonString.includes('http://') || jsonString.includes('https://')) {
            // 提取JSON部分
            const jsonMatch = jsonString.match(/\{.*"originUrl".*\}/)
            if (jsonMatch) {
              jsonString = jsonMatch[0]
            }
          }
          
          console.log('处理前的JSON字符串:', jsonString)
          
          const urlObj = JSON.parse(jsonString)
          actualFileUrl = urlObj.originUrl || urlObj.thumbUrl || fileUrl
          console.log('解析后的实际文件URL:', actualFileUrl)
        }
      } catch (e) {
        console.log('JSON解析失败，使用原始URL，错误信息:', e)
        // 如果解析失败，尝试从字符串中提取originUrl
        try {
          const originUrlMatch = fileUrl.match(/"originUrl"\s*:\s*"([^"]+)"/)
          if (originUrlMatch && originUrlMatch[1]) {
            actualFileUrl = originUrlMatch[1]
            console.log('通过正则提取的originUrl:', actualFileUrl)
          }
        } catch (regexError) {
          console.log('正则提取也失败，使用原始URL:', regexError)
        }
      }
      
      // 处理文件URL
      const processedUrl = getFileUrl(actualFileUrl)
      console.log('处理后的URL:', processedUrl)
      
      // 获取文件扩展名
      const fileExt = actualFileUrl.split('.').pop().toLowerCase()
      console.log('文件扩展名:', fileExt)
      
      // 判断是否为图片文件
      const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
      
      if (imageExts.includes(fileExt)) {
        console.log('是图片文件，开始预览')
        // 图片文件直接预览
        uni.previewImage({
          urls: [processedUrl],
          current: processedUrl,
          success: () => {
            console.log('图片预览成功')
          },
          fail: (err) => {
            console.error('图片预览失败:', err)
            uni.showToast({
              title: '预览失败',
              icon: 'none'
            })
          }
        })
      } else {
        console.log('非图片文件，直接下载')
        // 非图片文件直接下载，传递原始URL以便downloadFile方法处理
        this.downloadFile(fileUrl, fileName)
      }
    },
    // 下载文件
    downloadFile(fileUrl, fileName) {
      if (!fileUrl) {
        uni.showToast({
          title: '文件不存在',
          icon: 'none'
        })
        return
      }
      
      // 处理JSON格式的文件URL
      let actualFileUrl = fileUrl
      try {
        // 尝试解析JSON格式的URL
        if (fileUrl.includes('originUrl')) {
          // 处理可能被反引号包裹的JSON字符串
          let jsonString = fileUrl
          
          // 去除可能的前缀和后缀
          if (fileUrl.startsWith('`')) {
            jsonString = fileUrl.replace(/^`+/, '').replace(/`+$/, '') // 去掉首尾的所有反引号
          }
          
          // 处理可能包含的URL前缀
          if (jsonString.includes('http://') || jsonString.includes('https://')) {
            // 提取JSON部分
            const jsonMatch = jsonString.match(/\{.*"originUrl".*\}/)
            if (jsonMatch) {
              jsonString = jsonMatch[0]
            }
          }
          
          console.log('下载文件 - 处理前的JSON字符串:', jsonString)
          
          const urlObj = JSON.parse(jsonString)
          actualFileUrl = urlObj.originUrl || urlObj.thumbUrl || fileUrl
          console.log('下载文件 - 解析后的实际文件URL:', actualFileUrl)
        }
      } catch (e) {
        console.log('下载文件 - JSON解析失败，使用原始URL，错误信息:', e)
        // 如果解析失败，尝试从字符串中提取originUrl
        try {
          const originUrlMatch = fileUrl.match(/"originUrl"\s*:\s*"([^"]+)"/)
          if (originUrlMatch && originUrlMatch[1]) {
            actualFileUrl = originUrlMatch[1]
            console.log('下载文件 - 通过正则提取的originUrl:', actualFileUrl)
          }
        } catch (regexError) {
          console.log('下载文件 - 正则提取也失败，使用原始URL:', regexError)
        }
      }
      
      // 处理文件URL
      const processedUrl = getFileUrl(actualFileUrl)
      console.log('下载文件 - 最终处理后的URL:', processedUrl)
      
      // 显示下载提示
      uni.showLoading({
        title: '下载中...'
      })
      
      uni.downloadFile({
        url: processedUrl,
        success: (res) => {
          uni.hideLoading()
          if (res.statusCode === 200) {
            // 保存文件到本地
            uni.saveFile({
              tempFilePath: res.tempFilePath,
              success: (saveRes) => {
                uni.showToast({
                  title: '下载成功',
                  icon: 'success'
                })
                console.log('文件保存路径:', saveRes.savedFilePath)
              },
              fail: (err) => {
                console.error('文件保存失败:', err)
                uni.showToast({
                  title: '保存失败',
                  icon: 'none'
                })
              }
            })
          }
        },
        fail: (err) => {
          uni.hideLoading()
          console.error('文件下载失败:', err)
          uni.showToast({
            title: '下载失败',
            icon: 'none'
          })
        }
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
  padding-bottom: 8rpx;
  border-bottom: 2rpx solid #e5e7eb;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 24rpx;
  margin-bottom: 16rpx;
  background-color: #f9fafb;
  border-radius: 12rpx;
  border: 2rpx solid #e5e7eb;
}

.file-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.file-name {
  font-size: 28rpx;
  color: #374151;
  margin-left: 12rpx;
}

.file-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  cursor: pointer;
  text-align: center;
  min-width: 80rpx;
}

.preview-btn {
  color: #6366f1;
  background-color: #eef2ff;
  border: 2rpx solid #c7d2fe;
  font-weight: 500;
}

.download-btn {
  color: #10b981;
  background-color: #ecfdf5;
  border: 2rpx solid #a7f3d0;
  font-weight: 500;
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

