<template>
  <view class="article-detail-page">
    <NavBar :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y v-if="!loading && article">
      <!-- 文章标题 -->
      <view class="article-header">
        <text class="article-title">{{ article.title }}</text>
        <view class="article-meta">
          <text class="meta-item">{{ article.issueUnit }}</text>
          <text class="meta-divider">·</text>
          <text class="meta-item">{{ formatDate(article.issueTime) }}</text>
        </view>
      </view>

      <!-- 文章内容 -->
      <view class="article-body">
        <text class="article-content">{{ article.content }}</text>
      </view>
    </scroll-view>

    <view v-if="loading" class="loading-wrapper">
      <Loading></Loading>
    </view>

    <view v-if="!loading && !article" class="empty-wrapper">
      <Empty text="文章不存在"></Empty>
    </view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'
import { getArticleById } from '../../services/backend-api'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      article: null,
      loading: false
    }
  },
  onLoad(options) {
    const id = parseInt(options.id)
    if (id) {
      this.loadArticle(id)
    }
  },
  methods: {
    async loadArticle(id) {
      this.loading = true
      try {
        const res = await getArticleById(id)
        if (res.code === 200 && res.data) {
          this.article = res.data
        } else {
          uni.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载文章失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      let date
      // 处理数组格式的日期（后端 LocalDateTime 序列化为数组）
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
    }
  }
}
</script>

<style lang="scss" scoped>
.article-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
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

.article-header {
  width: 100%;
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 32rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
}

.article-title {
  display: block;
  font-size: 40rpx;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.5;
  margin-bottom: 24rpx;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 26rpx;
  color: #6b7280;
}

.meta-item {
  color: #6b7280;
}

.meta-divider {
  color: #d1d5db;
}

.article-body {
  width: 100%;
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 32rpx;
  box-sizing: border-box;
}

.article-content {
  display: block;
  font-size: 30rpx;
  line-height: 1.8;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-all;
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 32rpx;
}
</style>

