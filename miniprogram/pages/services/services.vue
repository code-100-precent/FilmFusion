<template>
  <PageShell
    title="一站式协拍对接"
    :show-back="false"
    :show-tab="true"
    tab-key="services"
    :refresher-enabled="false"
  >
    <view class="hero-banner">
      <view class="hero-banner__content">
        <view class="hero-banner__title">协拍服务总览</view>
        <view class="hero-banner__subtitle">场地、住宿、车辆与器材，快速直连本地服务商</view>
		</view>
      <view class="hero-banner__chips">
        <view class="chip">官方认证</view>
        <view class="chip">需求撮合</view>
				</view>
			</view>

    <view v-if="error" class="error-block">
      <text class="error-block__title">服务数据加载失败</text>
      <text class="error-block__desc">{{ error }}</text>
      <button class="error-block__btn" @click="fetchAll">重新加载</button>
						</view>

    <view v-else>
      <view v-for="group in groups" :key="group.key" class="group">
        <view class="group__header">
          <view>
            <view class="group__title">{{ group.title }}</view>
            <view class="group__hint">{{ group.description }}</view>
						</view>
          <view class="group__count">{{ group.items.length }} 家</view>
					</view>

        <view v-if="loading" class="service-skeleton">
          <view v-for="n in 4" :key="n" class="service-skeleton__item">
            <view class="service-skeleton__line service-skeleton__line--lg" />
            <view class="service-skeleton__line" />
				</view>
			</view>

        <view v-else-if="group.items.length === 0" class="empty">
          <Empty text="暂无数据，欢迎入驻合作" />
        </view>

        <view v-else class="service-list">
          <view v-for="item in group.items" :key="item.id" class="service-card">
            <view class="service-card__body">
              <view class="service-card__title">
                <text>{{ item.name }}</text>
                <view v-if="item.status === 1" class="service-card__status">可用</view>
              </view>
              <view v-if="item.description" class="service-card__desc">{{ item.description }}</view>
              <view v-if="item.address" class="service-card__meta">
                <uni-icons type="map-pin" size="16" color="#6366f1" />
                <text>{{ item.address }}</text>
              </view>
              <view v-if="item.price" class="service-card__meta">
                <uni-icons type="wallet" size="16" color="#14b8a6" />
                <text>{{ formatPrice(item.price) }}</text>
              </view>
              <view v-if="item.contactName" class="service-card__meta">
                <uni-icons type="person" size="16" color="#6366f1" />
                <text>{{ item.contactName }}</text>
              </view>
						</view>
            <view class="service-card__actions">
              <button class="service-card__btn" @click="call(item.contactPhone || item.phone)">
                电话
              </button>
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
import { getLocations, getShootServices } from '../../services/api'

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
      locations: [],
      shoots: []
    }
  },
  computed: {
    groups() {
      return [
        {
          key: 'locations',
          title: '拍摄场地',
          description: '市级推荐影视取景地，可预约踏勘与封控',
          items: (this.locations || []).map((item) => ({
            id: `location-${item.id}`,
            name: item.name,
            description: item.locationDescription,
            price: item.price,
            address: item.address,
            status: item.status,
            contactName: item.contactName,
            contactPhone: item.contactPhone
          }))
        },
        {
          key: 'shoot',
          title: '协拍服务',
          description: '住宿餐饮、车辆器材、后勤保障一键联络',
          items: (this.shoots || []).map((item) => ({
            id: `shoot-${item.id}`,
            name: item.name,
            description: item.description,
            price: item.price,
            address: item.address,
            status: item.status,
            contactName: item.contactName,
            contactPhone: item.phone
          }))
        }
      ]
		}
	},
	methods: {
    async fetchAll() {
      this.loading = true
      this.error = ''
      try {
        const [locationRes, shootRes] = await Promise.all([getLocations(), getShootServices()])
        this.locations = (locationRes && locationRes.data) || []
        this.shoots = (shootRes && shootRes.data) || []
      } catch (err) {
        this.error = (err && err.message) || '无法获取协拍服务数据'
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    handleRefresh() {
      if (this.loading) return
      this.refreshing = true
      this.fetchAll()
    },
    call(phone) {
      if (!phone) {
        uni.showToast({ title: '暂无联系电话', icon: 'none' })
        return
      }
      uni.makePhoneCall({ phoneNumber: phone })
    },
    formatPrice(price) {
      if (price === undefined || price === null || price === '') return '价格面议'
      const numeric = Number(price)
      if (Number.isNaN(numeric)) {
        return `${price}`
      }
      return `¥${numeric.toLocaleString('zh-CN')} 起`
    }
  },
  onLoad() {
    this.fetchAll()
	}
}
</script>

<style scoped lang="scss">
.hero-banner {
  background: linear-gradient(135deg, #dcfce7 0%, #dbeafe 100%);
  border-radius: 32rpx;
  padding: 42rpx 38rpx;
  margin-bottom: 36rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  border: 1rpx solid rgba(59, 130, 246, 0.12);
  box-shadow: 0 16rpx 44rpx rgba(22, 163, 74, 0.1);
  color: #0f172a;
}

.hero-banner__title {
  font-size: 40rpx;
  font-weight: 700;
}

.hero-banner__subtitle {
  font-size: 26rpx;
  color: rgba(15, 23, 42, 0.72);
  margin-top: 8rpx;
}

.hero-banner__chips {
  display: flex;
  gap: 16rpx;
}

.chip {
  padding: 12rpx 22rpx;
  border-radius: 999rpx;
  background: rgba(14, 165, 233, 0.14);
  font-size: 22rpx;
  color: #0ea5e9;
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

.group {
  margin-bottom: 42rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.group__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24rpx;
}

.group__title {
  font-size: 34rpx;
  font-weight: 700;
  color: #111827;
}

.group__hint {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 8rpx;
}

.group__count {
  padding: 10rpx 22rpx;
  border-radius: 999rpx;
  background: rgba(59, 130, 246, 0.08);
  color: #2563eb;
  font-size: 24rpx;
}

.service-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.service-card {
  display: flex;
  justify-content: space-between;
  gap: 24rpx;
  background: #fff;
  border-radius: 24rpx;
  border: 1rpx solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 16rpx 32rpx rgba(15, 23, 42, 0.08);
  padding: 26rpx 28rpx;
}

.service-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.service-card__title {
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #0f172a;
}

.service-card__status {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(16, 185, 129, 0.12);
  color: #0f766e;
  font-size: 22rpx;
}

.service-card__desc {
  font-size: 24rpx;
  color: #4b5563;
  line-height: 1.6;
}

.service-card__meta {
  display: flex;
  align-items: center;
  gap: 10rpx;
  font-size: 24rpx;
  color: #4b5563;
}

.service-card__actions {
  display: flex;
  align-items: center;
}

.service-card__btn {
  width: 120rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border: none;
  font-size: 26rpx;
}

.service-skeleton {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.service-skeleton__item {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  border: 1rpx solid rgba(226, 232, 240, 0.6);
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.service-skeleton__line {
  height: 26rpx;
  border-radius: 12rpx;
  background: linear-gradient(90deg, #f3f4f6 25%, #edeef3 50%, #f3f4f6 75%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}

.service-skeleton__line--lg {
  width: 70%;
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

