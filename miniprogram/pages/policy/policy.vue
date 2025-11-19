<template>
  <view class="policy-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>

    <!-- 自定义返回按钮 -->
    <view class="custom-header">
      <view class="back-button" @click="handleBack">
        <uni-icons type="left" color="white" size="20" />
      </view>
    </view>

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
import TabBar from '@/components/TabBar/TabBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'

export default {
  components: {
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
    handleBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack()
      } else {
        uni.reLaunch({ url: '/pages/index/index' })
      }
    },
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
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f8fafc;
  position: relative;
}

.gradient-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 0;
}

.custom-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 44px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  z-index: 100;
  padding-top: env(safe-area-inset-top);
}

.back-button {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.back-button:active {
  background: rgba(255, 255, 255, 0.2);
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
  overflow: hidden;
  padding-top: 44px;
}

.page-title {
  padding: 20px 16px 10px;
  text-align: center;
}

.title-text {
  font-size: 28px;
  font-weight: bold;
  color: white;
}

.search-bar {
  padding: 0 16px 12px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 8px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-input {
  flex: 1;
  margin-left: 8px;
  font-size: 14px;
  color: #333;
}

.filter-bar {
  display: flex;
  gap: 8px;
  padding: 0 16px 12px;
  overflow-x: auto;
}

.filter-tag {
  padding: 6px 14px;
  background: white;
  border-radius: 20px;
  font-size: 13px;
  color: #6b7280;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
}

.filter-tag.active {
  background: #6366f1;
  color: white;
}

.policy-list {
  flex: 1;
  padding: 0 16px;
}

.policy-card {
  background: white;
  border-radius: 12px;
  margin-bottom: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.policy-card:active {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.policy-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.policy-type-badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
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
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  flex: 1;
}

.policy-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 12px;
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
  color: #6366f1;
  font-size: 13px;
}

.view-more {
  margin-right: 4px;
}

.loading-wrapper,
.empty-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.load-more,
.no-more {
  text-align: center;
  padding: 20px;
  color: #9ca3af;
  font-size: 13px;
}
</style>

