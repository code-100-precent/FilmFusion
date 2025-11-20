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
                <text class="policy-title">{{ policy.title }}</text>
              </view>
              <view class="policy-meta">
                <text class="policy-unit">{{ policy.issueUnit }}</text>
                <text class="policy-date">{{ formatDate(policy.issueDate) }}</text>
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

    <!-- 底部导航栏 -->
    <TabBar :current="'policy'"></TabBar>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import TabBar from '@/components/TabBar/TabBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'

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
      selectedType: '全部',
      policies: [],
      loading: false,
      refreshing: false,
      hasMore: true,
      currentPage: 1,
      pageSize: 10,
      typeOptions: ['全部', '省级', '市级'],
      mockPolicies: [
  {
    id: 1,
    title: '四川省影视产业发展扶持政策',
    type: '省级',
    issueUnit: '四川省文化和旅游厅',
    issueDate: '2024-01-10',
    content: '为促进四川省影视产业发展，提升文化软实力，特制定本政策。对在四川拍摄的影视作品给予资金扶持，最高补助500万元。'
  },
  {
    id: 2,
    title: '雅安市影视产业发展规划（2024-2030）',
    type: '市级',
    issueUnit: '雅安市文化和旅游局',
    issueDate: '2024-01-15',
    content: '将雅安打造成为西南地区重要的影视拍摄基地和影视产业集聚区。对来雅拍摄的剧组提供场地优惠、税收优惠等政策支持。'
  },
  {
    id: 3,
    title: '雅安市影视拍摄协拍服务补助办法',
    type: '市级',
    issueUnit: '雅安市文化和旅游局',
    issueDate: '2024-02-01',
    content: '对为影视剧组提供协拍服务的企业和个人给予补助。补助标准：场地服务补助最高50万元，其他服务补助最高30万元。'
  },
      {
        id: 4,
        title: '雅安市影视文旅融合发展实施方案',
        type: '市级',
        issueUnit: '雅安市文化和旅游局',
        issueDate: '2024-02-15',
        content: '推动影视产业与文旅产业融合发展。支持开发影视主题旅游线路，建设影视文化体验基地，打造影视文旅品牌。'
      }
      ]
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      // 模拟网络延迟
      await new Promise(resolve => setTimeout(resolve, 500))

      let filtered = this.mockPolicies

      if (this.keyword) {
        filtered = filtered.filter(policy =>
          policy.title.includes(this.keyword) || policy.content.includes(this.keyword)
        )
      }

      if (this.selectedType !== '全部') {
        filtered = filtered.filter(policy => policy.type === this.selectedType)
      }

      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      this.policies = filtered.slice(0, end)
      this.hasMore = end < filtered.length

      this.loading = false
    },
    handleSearch() {
      this.currentPage = 1
      this.loadData()
    },
    async handleRefresh() {
      this.refreshing = true
      this.currentPage = 1
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
    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
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
  padding: 20rpx 32rpx;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
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
  margin-top: 24rpx;
  margin-bottom: 24rpx;
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

.filter-bar {
  display: flex;
  gap: 16rpx;
  margin-bottom: 32rpx;
  overflow-x: auto;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.filter-tag {
  padding: 12rpx 28rpx;
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
  padding: 32rpx;
  margin-bottom: 24rpx;
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
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.policy-type-badge {
  padding: 8rpx 16rpx;
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
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.5;
  flex: 1;
}

.policy-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
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
  padding-top: 20rpx;
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

