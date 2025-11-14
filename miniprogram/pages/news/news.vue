<template>
  <view class="news-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>
    
    <NavBar :show-back="false"></NavBar>

    <view class="content">
      <!-- 页面标题 -->
      <view class="page-title">
        <text class="title-text">影视资讯</text>
      </view>
      <!-- 搜索栏 -->
      <view class="search-bar">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#9ca3af"></uni-icons>
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索资讯..."
            @confirm="handleSearch"
            @input="handleSearch"
          />
        </view>
      </view>

      <!-- 资讯列表 -->
      <scroll-view
        class="article-list"
        scroll-y
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="handleRefresh"
      >
        <view v-if="loading && articles.length === 0" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="articles.length === 0" class="empty-wrapper">
          <Empty text="暂无资讯"></Empty>
        </view>
        <view v-else>
          <view
            v-for="article in articles"
            :key="article.id"
            class="article-card"
            @click="goToDetail(article.id)"
          >
            <view class="article-header">
              <text class="article-title">{{ article.title }}</text>
            </view>
            <view class="article-meta">
              <text class="article-unit">{{ article.issueUnit }}</text>
              <text class="article-time">{{ formatDate(article.issueTime) }}</text>
            </view>
            <text class="article-content">{{ getContentPreview(article.content) }}</text>
            <view class="article-footer">
              <text class="read-more">查看详情</text>
              <uni-icons type="right" size="14" color="#6366f1"></uni-icons>
            </view>
          </view>
        </view>

        <view v-if="hasMore && !loading" class="load-more">
          <text>上拉加载更多</text>
        </view>
        <view v-if="!hasMore && articles.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
      </scroll-view>
    </view>

    <!-- 底部导航栏 -->
    <TabBar :current="'news'"></TabBar>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import TabBar from '../../components/TabBar/TabBar.vue'
import Loading from '../../components/Loading/Loading.vue'
import Empty from '../../components/Empty/Empty.vue'
import { getArticlePage } from '../../services/api'

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
      articles: [],
      current: 1,
      size: 10,
      total: 0,
      loading: false,
      refreshing: false,
      hasMore: true
    }
  },
  onLoad() {
    this.loadArticles()
  },
  methods: {
    async loadArticles(reset = false) {
      if (this.loading) return

      if (reset) {
        this.current = 1
        this.articles = []
        this.hasMore = true
      }

      this.loading = true
      try {
        const res = await getArticlePage({
          current: this.current,
          size: this.size,
          keyword: this.keyword || undefined
        })

        if (res.code === 200) {
          // 后端返回格式: { code: 200, message: "请求成功", data: [...], pagination: {...} }
          const dataList = Array.isArray(res.data) ? res.data : []
          const pagination = res.pagination || {}
          
          if (reset) {
            this.articles = dataList
          } else {
            this.articles = [...this.articles, ...dataList]
          }
          this.total = pagination.totalItems || 0
          this.hasMore = this.articles.length < this.total
        }
      } catch (error) {
        console.error('加载资讯失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    handleSearch() {
      this.loadArticles(true)
    },
    handleRefresh() {
      this.refreshing = true
      this.loadArticles(true)
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.current++
      this.loadArticles()
    },
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/article/detail?id=${id}`
      })
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      let date
      // 处理数组格式的日期（后端 LocalDateTime 序列化为数组）
      if (Array.isArray(dateStr)) {
        // 数组格式: [2024, 12, 15, 10, 0, 0]
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
    getContentPreview(content) {
      if (!content) return ''
      return content.length > 100 ? content.substring(0, 100) + '...' : content
    }
  }
}
</script>

<style lang="scss" scoped>
.news-page {
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
  margin-bottom: 32rpx;
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

.article-list {
  height: calc(100vh - 88rpx - 200rpx);
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}

.article-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  width: 100%;
  box-sizing: border-box;
}

.article-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.article-header {
  margin-bottom: 16rpx;
}

.article-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.5;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
  font-size: 24rpx;
  color: #9ca3af;
}

.article-content {
  display: block;
  font-size: 28rpx;
  color: #6b7280;
  line-height: 1.8;
  margin-bottom: 24rpx;
}

.article-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f3f4f6;
}

.read-more {
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
