<template>
  <view class="detail-page">
    <NavBar :show-back="true" :title="policy ? policy.title : ''"></NavBar>
    <scroll-view class="content" scroll-y>
      <view v-if="loading" class="loading-wrapper">
        <Loading></Loading>
      </view>
      <view v-else-if="policy">
        <view class="policy-header">
          <view class="policy-type-badge" :class="policy.type === '省级' ? 'type-prov' : 'type-city'">{{ policy.type }}</view>
          <view class="policy-title">{{ policy.title }}</view>
        </view>
        <view class="info-section">
          <view class="info-item">
            <text class="info-label">发布单位</text>
            <text class="info-value">{{ policy.issueUnit }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">发布日期</text>
            <text class="info-value">{{ formatDate(policy.issueDate) }}</text>
          </view>
        </view>
        <view class="content-section">
          <view class="section-title">政策内容</view>
          <view class="section-content"><text>{{ policy.content }}</text></view>
        </view>
        <view class="action-buttons">
          <button class="btn btn-primary" @click="handleShare">
            <uni-icons type="share" size="18" color="white"></uni-icons>
            分享政策
          </button>
          <button class="btn btn-secondary" @click="handleCollect">
            <uni-icons type="heart" size="18" :color="collected ? '#ef4444' : 'white'"></uni-icons>
            {{ collected ? '已收藏' : '收藏' }}
          </button>
        </view>
        <view class="related-section">
          <view class="section-title">相关政策</view>
          <view class="related-list">
            <view v-for="item in relatedPolicies" :key="item.id" class="related-item" @click="goToPolicy(item.id)">
              <text class="related-title">{{ item.title }}</text>
              <uni-icons type="right" size="14" color="#9ca3af"></uni-icons>
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

export default {
  components: { NavBar, Loading, Empty },
  data() {
    return {
      policy: null,
      loading: true,
      collected: false,
      relatedPolicies: [],
      mockPolicies: {
        1: { id: 1, title: '四川省影视产业发展扶持政策', type: '省级', issueUnit: '四川省文化和旅游厅', issueDate: '2024-01-10', content: '为促进四川省影视产业发展，提升文化软实力，特制定本政策。' },
        2: { id: 2, title: '雅安市影视产业发展规划', type: '市级', issueUnit: '雅安市文化和旅游局', issueDate: '2024-01-15', content: '为推动雅安市影视产业发展，特制定本规划。' },
        3: { id: 3, title: '雅安市影视拍摄协拍服务补助办法', type: '市级', issueUnit: '雅安市文化和旅游局', issueDate: '2024-02-01', content: '为鼓励社会力量参与影视拍摄协拍服务，特制定本办法。' },
        4: { id: 4, title: '雅安市影视文旅融合发展实施方案', type: '市级', issueUnit: '雅安市文化和旅游局', issueDate: '2024-02-15', content: '为推动影视产业与文旅产业融合发展，特制定本方案。' }
      }
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      await new Promise(resolve => setTimeout(resolve, 500))
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      const id = parseInt(currentPage.route.split('?id=')[1])
      this.policy = this.mockPolicies[id] || null
      if (this.policy) {
        this.relatedPolicies = Object.values(this.mockPolicies).filter(p => p.id !== id && p.type === this.policy.type).slice(0, 3)
      }
      this.loading = false
    },
    handleShare() {
      uni.showToast({ title: '分享成功', icon: 'success' })
    },
    handleCollect() {
      this.collected = !this.collected
      uni.showToast({ title: this.collected ? '已收藏' : '已取消收藏', icon: 'success' })
    },
    goToPolicy(id) {
      uni.redirectTo({ url: `/pages/policy/detail?id=${id}` })
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
.detail-page { display: flex; flex-direction: column; height: 100vh; background: #f8fafc; }
.content { flex: 1; overflow-y: auto; }
.policy-header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 24px 16px; color: white; }
.policy-type-badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 500; margin-bottom: 12px; background: rgba(255, 255, 255, 0.2); }
.policy-type-badge.type-prov { background: rgba(30, 64, 175, 0.3); }
.policy-type-badge.type-city { background: rgba(22, 101, 52, 0.3); }
.policy-title { font-size: 20px; font-weight: 600; line-height: 1.4; color: white; }
.info-section { background: white; margin: 12px 16px; padding: 16px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.info-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #f3f4f6; }
.info-item:last-child { border-bottom: none; }
.info-label { font-size: 14px; color: #6b7280; font-weight: 500; }
.info-value { font-size: 14px; color: #1f2937; font-weight: 500; }
.content-section { background: white; margin: 12px 16px; padding: 16px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.section-title { font-size: 16px; font-weight: 600; color: #1f2937; margin-bottom: 12px; }
.section-content { font-size: 14px; color: #6b7280; line-height: 1.8; white-space: pre-wrap; word-break: break-word; }
.action-buttons { display: flex; gap: 12px; padding: 16px; margin: 12px 0; }
.btn { flex: 1; display: flex; align-items: center; justify-content: center; gap: 8px; padding: 12px 16px; border-radius: 8px; border: none; font-size: 14px; font-weight: 500; }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
.btn-secondary { background: white; color: #6366f1; border: 2px solid #6366f1; }
.related-section { background: white; margin: 12px 16px; padding: 16px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.related-list { display: flex; flex-direction: column; gap: 12px; }
.related-item { display: flex; align-items: center; justify-content: space-between; padding: 12px; background: #f8fafc; border-radius: 8px; }
.related-title { font-size: 13px; color: #6b7280; flex: 1; }
.loading-wrapper, .empty-wrapper { display: flex; justify-content: center; align-items: center; height: 300px; }
</style>

