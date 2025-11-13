<template>
  <PageShell
    style="margin-top: 50px"
    title="雅安取景手册"
    :show-back="false"
    :show-tab="true"
    tab-key="scenes"
    :refresher-enabled="false"
  >
    <view class="hero-card">
      <view>
        <view class="hero-card__title">雅安取景手册</view>
        <view class="hero-card__subtitle">文化、山水、年代、建筑与特色资源一站汇总</view>
		</view>
      <view class="hero-card__meta">
        <view class="hero-card__tag">实时同步</view>
        <view class="hero-card__tag">资源共建</view>
				</view>
			</view>

    <view v-if="error" class="error-block">
      <text class="error-block__title">数据加载失败</text>
      <text class="error-block__desc">{{ error }}</text>
      <button class="error-block__btn" @click="fetchScenes">重新加载</button>
    </view>

    <view v-else>
      <view v-for="group in groups" :key="group.key" class="section">
        <view class="section__header">
          <view class="section__title">
            <text>{{ group.title }}</text>
            <text v-if="group.description" class="section__hint">{{ group.description }}</text>
						</view>
          <view class="section__badge">{{ group.items.length }} 处</view>
					</view>

        <view v-if="loading" class="grid">
          <view v-for="n in 4" :key="n" class="card-skeleton">
            <view class="card-skeleton__thumb" />
            <view class="card-skeleton__line card-skeleton__line--lg" />
            <view class="card-skeleton__line" />
				</view>
			</view>

        <view v-else-if="group.items.length === 0" class="empty">
          <Empty text="暂无资源，稍后再来看看" />
			</view>

        <view v-else class="grid">
          <view v-for="item in group.items" :key="item.poiId" class="card" @click="openPoiDetail(item)">
            <image class="card__thumb" :src="item.imageUrl || placeholders[group.key] || defaultCover" mode="aspectFill" />
            <view class="card__body">
              <view class="card__title">{{ item.name }}</view>
              <text class="card__desc">{{ item.intro || '暂无介绍' }}</text>
              <view class="card__tags" v-if="item.tags && item.tags.length">
                <text v-for="tag in item.tags" :key="tag" class="card__tag">#{{ tag }}</text>
						</view>
					</view>
				</view>
			</view>
				</view>
			</view>
  </PageShell>
</template>

<script>
import PageShell from '../../components/layout/PageShell.vue'
import Empty from '../../components/Empty/Empty.vue'
import { getPoiList } from '../../services/api'

const DEFAULT_COVER = 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=800&q=60'
const PLACEHOLDERS = {
  culture: 'https://images.unsplash.com/photo-1545239351-1141bd82e8a6?auto=format&fit=crop&w=800&q=60',
  nature: 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=800&q=60',
  arch: 'https://images.unsplash.com/photo-1600585154340-0ef3c08dcdb6?auto=format&fit=crop&w=800&q=60',
  memory: 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=60'
}
const TYPE_LABELS = {
  culture: { title: '雅州文化', description: '传统人文与历史足迹' },
  nature: { title: '生态山水', description: '峡谷飞瀑与自然生态' },
  arch: { title: '建筑场景', description: '城市与民居空间' },
  memory: { title: '年代记忆', description: '年代风貌与工业遗址' }
}

export default {
  components: {
    PageShell,
    Empty
  },
	data() {
		return {
      loading: false,
      refreshing: false,
      error: '',
      poiList: [],
      defaultCover: DEFAULT_COVER,
      placeholders: PLACEHOLDERS
    }
  },
  computed: {
    groups() {
      const map = {}
      this.poiList.forEach((item) => {
        const key = (item.type || 'others').toLowerCase()
        if (!map[key]) {
          map[key] = []
        }
        map[key].push(item)
      })

      return Object.keys(map).map((key) => {
        const meta = TYPE_LABELS[key] || { title: key.toUpperCase() }
        return {
          key,
          title: meta.title,
          description: meta.description,
          items: map[key]
        }
      })
    }
  },
  methods: {
    async fetchScenes() {
      this.loading = true
      this.error = ''
      try {
        const { data } = await getPoiList()
        this.poiList = data || []
      } catch (err) {
        this.error = (err && err.message) || '无法获取取景点数据'
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    handleRefresh() {
      if (this.loading) return
      this.refreshing = true
      this.fetchScenes()
    },
    openPoiDetail(item) {
      if (!item || !item.poiId) {
        uni.showToast({
          title: '数据异常',
          icon: 'none'
        })
        return
      }
      // 记录浏览历史
      const token = uni.getStorageSync('token')
      if (token) {
        const visitorInfo = uni.getStorageSync('visitorInfo')
        if (visitorInfo && visitorInfo.visitorId) {
          // 异步记录，不阻塞跳转
          import('../../services/api').then(({ addBrowse }) => {
            addBrowse({ poiId: item.poiId, visitorId: visitorInfo.visitorId }).catch(() => {
              // 静默失败
            })
          })
        }
      }
      // 跳转到详情页（如果存在）
      uni.navigateTo({
        url: `/pages/poi-detail/poi-detail?poiId=${item.poiId}`,
        fail: () => {
          uni.showToast({
            title: '详情页开发中',
            icon: 'none'
          })
        }
      })
    }
  },
  onLoad() {
    this.fetchScenes()
  }
}
</script>

<style scoped lang="scss">
.hero-card {
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  border-radius: 32rpx;
  padding: 48rpx 42rpx;
  margin-bottom: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 28rpx;
  border: 1rpx solid rgba(99, 102, 241, 0.16);
  box-shadow: 0 18rpx 48rpx rgba(99, 102, 241, 0.15);
  color: #312e81;
}

.hero-card__title {
  font-size: 40rpx;
  font-weight: 700;
  margin-bottom: 8rpx;
}

.hero-card__subtitle {
  font-size: 26rpx;
  color: rgba(49, 46, 129, 0.76);
}

.hero-card__meta {
  display: flex;
  gap: 16rpx;
}

.hero-card__tag {
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  background: rgba(79, 70, 229, 0.12);
  font-size: 22rpx;
  color: #4338ca;
}

.error-block {
  background: #fff5f5;
  border-radius: 24rpx;
  padding: 48rpx;
  border: 1rpx solid rgba(248, 113, 113, 0.24);
  text-align: center;
  color: #b91c1c;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.error-block__title {
  font-size: 32rpx;
  font-weight: 600;
}

.error-block__desc {
  font-size: 26rpx;
  color: rgba(185, 28, 28, 0.78);
}

.error-block__btn {
  margin-top: 12rpx;
  height: 88rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #f97316, #ef4444);
  color: #fff;
  font-size: 28rpx;
  border: none;
}

.section {
  margin-bottom: 48rpx;
}

.section__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  gap: 24rpx;
}

.section__title {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  font-size: 34rpx;
  font-weight: 700;
  color: #1f2937;
}

.section__hint {
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 400;
}

.section__badge {
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  background: rgba(79, 70, 229, 0.08);
  font-size: 24rpx;
  color: #4338ca;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20rpx;
}

.card {
  background: #ffffff;
  border-radius: 28rpx;
  overflow: hidden;
  border: 1rpx solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 18rpx 36rpx rgba(30, 64, 175, 0.08);
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 24rpx 48rpx rgba(30, 64, 175, 0.12);
}

.card__thumb {
  width: 100%;
  height: 220rpx;
}

.card__body {
  padding: 26rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.card__title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
}

.card__desc {
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.6;
}

.card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.card__tag {
  font-size: 22rpx;
  color: #4338ca;
  background: rgba(67, 56, 202, 0.08);
  border-radius: 12rpx;
  padding: 6rpx 16rpx;
}

.card-skeleton {
  background: #fff;
  border-radius: 28rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  border: 1rpx solid rgba(226, 232, 240, 0.6);
}

.card-skeleton__thumb {
  height: 220rpx;
  border-radius: 24rpx;
  background: linear-gradient(90deg, #f3f4f6 25%, #edeef3 50%, #f3f4f6 75%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}

.card-skeleton__line {
  height: 26rpx;
  border-radius: 12rpx;
  background: linear-gradient(90deg, #f3f4f6 25%, #edeef3 50%, #f3f4f6 75%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}

.card-skeleton__line--lg {
  width: 80%;
}

.empty {
  background: #fff;
  border-radius: 24rpx;
  padding: 48rpx 24rpx;
  border: 1rpx solid rgba(226, 232, 240, 0.6);
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>

