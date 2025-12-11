<template>
  <view class="policy-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>
    
    <NavBar :show-back="true"></NavBar>

    <view class="content">
      <!-- 页面标题 -->
      <view class="page-title">
        <text class="title-text">视听政策</text>
      </view>
      
      <!-- 搜索栏 -->
      <view class="search-bar">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#9ca3af"></uni-icons>
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索政策..."
            @confirm="handleSearch"
            @input="handleSearch"
          />
        </view>
      </view>

      <!-- 政策类型筛选 -->
      <view class="filter-bar">
        <view 
          v-for="type in typeOptions"
          :key="type"
          class="filter-tag"
          :class="{ active: selectedType === type }"
          @click="selectedType = type; handleSearch()"
        >
          {{ type }}
        </view>
      </view>

      <!-- 政策列表 -->
      <scroll-view
        class="policy-list"
        scroll-y
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="handleRefresh"
      >
        <view v-if="loading && policies.length === 0" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="policies.length === 0" class="empty-wrapper">
          <Empty text="暂无政策"></Empty>
        </view>
        <view v-else>
          <view
            v-for="policy in policies"
            :key="policy.id"
            class="policy-card"
            @click="goToDetail(policy.id)"
          >
            <view class="policy-info">
              <view class="policy-header">
                <view class="policy-type-badge" :class="policy.type === '省级' ? 'type-prov' : 'type-city'">
                  {{ policy.type }}
                </view>
                <text class="policy-title">{{ getTitlePreview(policy.title) }}</text>
              </view>
              <view class="policy-meta">
                <text class="policy-unit">{{ policy.issueUnit }}</text>
                <text class="policy-date">{{ formatDate(policy.issueTime) }}</text>
              </view>
              <view class="policy-footer">
                <text class="view-more">查看详情</text>
                <uni-icons type="right" size="14" color="#6366f1"></uni-icons>
              </view>
            </view>
          </view>
        </view>

        <view v-if="hasMore && !loading" class="load-more">
          <text>上拉加载更多</text>
        </view>
        <view v-if="!hasMore && policies.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
      </scroll-view>
    </view>

  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
import { getPolicyPage } from '@/services/backend-api'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      keyword: '',
      selectedType: '全部',
      policies: [],
      loading: false,
      refreshing: false,
      hasMore: true,
      currentPage: 1,
      pageSize: 10,
      totalPages: 0,
      typeOptions: ['全部', '省级', '市级']
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    async loadData() {
      if (this.loading) return
      
      this.loading = true
      try {
        const res = await getPolicyPage({
          current: this.currentPage,
          size: this.pageSize,
          keyword: this.keyword || undefined,
          type: this.selectedType === '全部' ? undefined : this.selectedType
        })

        // 处理游标分页响应格式
        if (res && res.records) {
          const newPolicies = res.records.map(item => ({
            id: item.id,
            title: item.title,
            type: item.type,
            issueUnit: item.issueUnit,
            issueTime: item.issueTime,
            content: item.content
          }))

          if (this.currentPage === 1) {
            this.policies = newPolicies
          } else {
            this.policies = [...this.policies, ...newPolicies]
          }

          // 更新游标分页信息
          this.hasMore = res.hasMore || false
        } else {
          console.error('政策数据格式错误:', res)
          uni.showToast({
            title: '数据格式错误',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载政策失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.policies = []
      this.loadData()
    },
    async handleRefresh() {
      this.refreshing = true
      this.currentPage = 1
      this.policies = []
      await this.loadData()
      this.refreshing = false
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.currentPage++
      this.loadData()
    },
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/policy/detail?id=${id}`
      })
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
    getTitlePreview(title) {
      if (!title) return ''
      return title.length > 35 ? title.substring(0, 35) + '...' : title
    }
  }
}
</script>

<style scoped lang="scss">
.policy-page {
  min-height: 100vh;
  background: #f5f7fa;
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
  background: linear-gradient(to top, #ffffff 0%, #20b2aa 100%);
  z-index: 0;
}

.content {
  padding: 16rpx 24rpx;
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  width: 100%;
  position: relative;
  z-index: 1;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  /* 兼容火狐浏览器 */
  scrollbar-width: none;
  /* 兼容IE浏览器 */
  -ms-overflow-style: none;
}

/* 页面标题样式 */
.page-title {
  padding: 32rpx 0 8rpx 0;
  width: 100%;
}

.title-text {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.search-bar {
  margin-top: 16rpx;
  margin-bottom: 16rpx;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 0 20rpx;
  height: 72rpx;
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

.filter-bar {
  display: flex;
  gap: 12rpx;
  margin-bottom: 16rpx;
  overflow-x: auto;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.filter-tag {
  padding: 10rpx 24rpx;
  background: #fff;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: #6b7280;
  white-space: nowrap;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  flex-shrink: 0;
}

.filter-tag.active {
  background: #6366f1;
  color: white;
  box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.3);
}

.policy-list {
  height: calc(100vh - 132rpx - 200rpx);
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}

.policy-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  width: 100%;
  box-sizing: border-box;
}

.policy-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.policy-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.policy-header {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.policy-type-badge {
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

.policy-type-badge.type-prov {
  background: #dbeafe;
  color: #1e40af;
}

.policy-type-badge.type-city {
  background: #dcfce7;
  color: #166534;
}

.policy-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.4;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.policy-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
  font-size: 24rpx;
  color: #9ca3af;
}

.policy-unit {
  flex: 1;
}

.policy-date {
  white-space: nowrap;
}

.policy-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f3f4f6;
}

.view-more {
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

