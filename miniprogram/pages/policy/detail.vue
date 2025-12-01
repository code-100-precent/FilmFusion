<template>
  <view class="detail-page">
    <NavBar :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y>
      <view v-if="loading" class="loading-wrapper">
        <Loading></Loading>
      </view>
      <view v-else-if="policy">
        <!-- 政策标题卡片 -->
        <view class="header-card">
          <view class="policy-type-badge" :class="policy.type === '省级' ? 'type-prov' : 'type-city'">
            {{ policy.type }}
          </view>
          <view class="policy-title">{{ policy.title }}</view>
        </view>

        <!-- 基本信息 -->
        <view class="info-section">
          <view class="section-title">基本信息</view>
          <view class="info-list">
            <view class="info-item">
              <view class="info-label">
                <uni-icons type="person" size="16" color="#6366f1"></uni-icons>
                <text>发布单位</text>
              </view>
              <text class="info-value">{{ policy.issueUnit }}</text>
            </view>
            <view class="info-item">
              <view class="info-label">
                <uni-icons type="calendar" size="16" color="#10b981"></uni-icons>
                <text>发布时间</text>
              </view>
              <text class="info-value">{{ formatDate(policy.issueTime) }}</text>
            </view>
          </view>
        </view>

        <!-- 政策内容 -->
        <view class="info-section">
          <view class="section-title">政策内容</view>
          <view class="section-content">
            <text>{{ policy.content }}</text>
          </view>
        </view>

        <!-- 附件列表（如果有） -->
        <view v-if="policy.attachments && policy.attachments.length > 0" class="info-section">
          <view class="section-title">相关附件</view>
          <view class="attachment-list">
            <view v-for="(item, index) in policy.attachments" :key="index" class="attachment-item">
              <uni-icons type="paperclip" size="16" color="#6366f1"></uni-icons>
              <text class="attachment-name">{{ item.name }}</text>
              <text class="attachment-size">{{ formatFileSize(item.size) }}</text>
            </view>
          </view>
        </view>

        <view style="height: 20px;"></view>
      </view>
      <view v-else class="empty-wrapper">
        <Empty text="政策不存在"></Empty>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
import { getPolicyById } from '@/services/backend-api'

export default {
  components: { NavBar, Loading, Empty },
  data() {
    return {
      policyId: null,
      policy: null,
      loading: true
    }
  },
  onLoad(options) {
    if (options.id) {
      this.policyId = parseInt(options.id)
      this.loadData()
    } else {
      this.loading = false
      uni.showToast({
        title: '政策ID不存在',
        icon: 'none'
      })
    }
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getPolicyById(this.policyId)
        
        if (res.code === 200 && res.data) {
          this.policy = {
            id: res.data.id,
            title: res.data.title,
            type: res.data.type,
            issueUnit: res.data.issueUnit,
            issueTime: res.data.issueTime,
            content: res.data.content,
            attachments: res.data.attachments || []
          }
        } else {
          uni.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          })
          this.policy = null
        }
      } catch (error) {
        console.error('加载政策详情失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
        this.policy = null
      } finally {
        this.loading = false
      }
    },
    formatDate(dateArray) {
      if (!dateArray) return '-'
      
      // 处理数组格式 [2024, 5, 20, 10, 30]
      if (Array.isArray(dateArray)) {
        const year = dateArray[0]
        const month = String(dateArray[1]).padStart(2, '0')
        const day = String(dateArray[2]).padStart(2, '0')
        return `${year}-${month}-${day}`
      }
      
      // 处理字符串格式
      if (typeof dateArray === 'string') {
        const d = new Date(dateArray)
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
      }
      
      return '-'
    },
    formatFileSize(bytes) {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
    }
  }
}
</script>

<style scoped lang="scss">
.detail-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f8fafc;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  width: 100%;
  padding: 32rpx;
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

.header-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32rpx;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
}

.policy-type-badge {
  display: inline-block;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
  background: rgba(255, 255, 255, 0.25);
  color: white;
  backdrop-filter: blur(10rpx);
}

.policy-type-badge.type-prov {
  background: rgba(255, 255, 255, 0.3);
}

.policy-type-badge.type-city {
  background: rgba(255, 255, 255, 0.25);
}

.policy-title {
  font-size: 36rpx;
  font-weight: 700;
  color: white;
  line-height: 1.5;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.info-section {
  background: white;
  margin-bottom: 24rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #f3f4f6;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx;
  background: #f9fafb;
  border-radius: 16rpx;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
  color: #6b7280;
  font-weight: 500;
}

.info-value {
  font-size: 28rpx;
  color: #1f2937;
  font-weight: 600;
}

.section-content {
  font-size: 28rpx;
  color: #4b5563;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx;
  background: #f9fafb;
  border-radius: 16rpx;
  border: 2rpx solid #e5e7eb;
}

.attachment-name {
  flex: 1;
  font-size: 26rpx;
  color: #1f2937;
}

.attachment-size {
  font-size: 24rpx;
  color: #9ca3af;
}

.loading-wrapper,
.empty-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 600rpx;
}
</style>
