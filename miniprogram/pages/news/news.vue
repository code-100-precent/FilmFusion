<template>
  <PageShell
    title="影视资讯"
    :show-back="false"
    :show-tab="true"
    tab-key="news"
    :refresher-enabled="false"
  >
    <view class="headline">
      <view class="headline__title">实时影讯 / 政策动态</view>
      <view class="headline__desc">雅安影视服务中心官方发布，第一时间掌握政策与活动</view>
    </view>

    <view v-if="error" class="error-block">
      <text class="error-block__title">资讯获取失败</text>
      <text class="error-block__desc">{{ error }}</text>
      <button class="error-block__btn" @click="fetchArticles">重新加载</button>
    </view>

    <view v-else-if="loading" class="article-skeleton">
      <view v-for="n in 4" :key="n" class="article-skeleton__card">
        <view class="article-skeleton__thumb" />
        <view class="article-skeleton__info">
          <view class="article-skeleton__line article-skeleton__line--lg" />
          <view class="article-skeleton__line" />
        </view>
      </view>
    </view>

    <view v-else-if="articles.length === 0" class="empty">
      <Empty text="暂未发布资讯，敬请期待" />
    </view>

    <view v-else class="article-list">
      <view
        v-for="article in articles"
        :key="article.id"
        class="article-card"
        @click="openArticleDetail"
      >
        <image
          class="article-card__cover"
          :src="article.coverUrl || defaultCover"
          mode="aspectFill"
        />
        <view class="article-card__content">
          <view class="article-card__title">{{ article.title }}</view>
          <view class="article-card__meta">
            <text>{{ article.issueUnit }}</text>
            <text>{{ formatDate(article.issueTime) }}</text>
		</view>
          <view class="article-card__excerpt">{{ extractPreview(article.content) }}</view>
          <view class="article-card__actions">
            <text>查看详情</text>
            <uni-icons type="right" size="16" color="#4f46e5" />
					</view>
				</view>
			</view>
	</view>
  </PageShell>
</template>

<script>
import PageShell from '../../components/layout/PageShell.vue'
import Empty from '../../components/Empty/Empty.vue'
import { getArticles } from '../../services/api'

const DEFAULT_COVER = 'https://images.unsplash.com/photo-1529158062015-cad636e69505?auto=format&fit=crop&w=800&q=60'

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
      articles: [],
      defaultCover: DEFAULT_COVER
		}
	},
	methods: {
    async fetchArticles() {
			this.loading = true
      this.error = ''
      try {
        const { data } = await getArticles()
        this.articles = data || []
      } catch (err) {
        this.error = (err && err.message) || '无法获取资讯'
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    handleRefresh() {
      if (this.loading) return
      this.refreshing = true
      this.fetchArticles()
    },
    extractPreview(content, length = 80) {
      if (!content) return ''
      const text = String(content).replace(/<[^>]+>/g, '').trim()
      return text.length > length ? `${text.slice(0, length)}...` : text
    },
    formatDate(value) {
      if (!value) return '未知时间'
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return value
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${date.getFullYear()}-${month}-${day}`
    },
    openArticleDetail() {
      uni.showToast({ title: '详情内容即将上线', icon: 'none' })
    }
  },
  onLoad() {
    this.fetchArticles()
	}
}
</script>

<style scoped lang="scss">
.headline {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 32rpx;
}

.headline__title {
  font-size: 38rpx;
  font-weight: 700;
  color: #111827;
}

.headline__desc {
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.5;
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

.article-skeleton {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.article-skeleton__card {
  display: flex;
  gap: 20rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 20rpx;
  border: 1rpx solid rgba(226, 232, 240, 0.6);
}

.article-skeleton__thumb {
  width: 220rpx;
  height: 160rpx;
  border-radius: 18rpx;
  background: linear-gradient(90deg, #f3f4f6 25%, #edeef3 50%, #f3f4f6 75%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}

.article-skeleton__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 12rpx 0;
}

.article-skeleton__line {
  height: 26rpx;
  border-radius: 12rpx;
  background: linear-gradient(90deg, #f3f4f6 25%, #edeef3 50%, #f3f4f6 75%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}

.article-skeleton__line--lg {
  width: 80%;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.article-card {
  display: flex;
  gap: 20rpx;
  background: #ffffff;
  border-radius: 28rpx;
  overflow: hidden;
  border: 1rpx solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 18rpx 32rpx rgba(15, 23, 42, 0.08);
}

.article-card__cover {
  width: 240rpx;
  height: 200rpx;
}

.article-card__content {
  flex: 1;
  padding: 24rpx 28rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.article-card__title {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
}

.article-card__meta {
  display: flex;
  justify-content: space-between;
  font-size: 22rpx;
  color: #6b7280;
}

.article-card__excerpt {
  font-size: 24rpx;
  color: #4b5563;
  line-height: 1.6;
}

.article-card__actions {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #4f46e5;
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

