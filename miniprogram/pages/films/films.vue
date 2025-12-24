<template>
  <view class="films-page">
    <!-- Gradient Background -->
    <view class="gradient-bg"></view>
    
    <NavBar :show-back="true"></NavBar>

    <view class="content">
      <!-- Search Bar -->
      <view class="search-bar">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#9ca3af"></uni-icons>
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索"
            @confirm="handleSearch"
            @input="handleSearch"
          />
        </view>
      </view>

      <!-- Film List -->
      <scroll-view
        class="film-list"
        scroll-y
        @scrolltolower="loadMore"
      >
        <view v-if="films.length === 0 && !loading" class="empty-wrapper">
          <Empty text="暂无影视作品"></Empty>
        </view>
        <view v-else class="film-list-container">
          <view
            v-for="(film, index) in films"
            :key="film.id"
            class="film-item"
            :style="{ 'animation-delay': index * 0.05 + 's' }"
            @click="goToDetail(film.id)"
          >
            <view class="film-cover-wrapper">
              <image 
                :src="getPosterUrl(film.cover || film.image)" 
                class="film-cover" 
                mode="aspectFill"
                @error="handleImageError"
              ></image>
              <view class="film-type-badge" v-if="film.type">{{ film.type }}</view>
            </view>
            <view class="film-info">
              <view class="film-header">
                <text class="film-title">{{ film.name }}</text>
              </view>
              <text class="film-desc">{{ formatDescription(film.dramaDescription) }}</text>
              <view class="film-footer">
                <text class="film-meta-text" v-if="film.prodCompany">{{ film.prodCompany }}</text>
                <text class="film-meta-text" v-if="film.filingNum">备案号: {{ film.filingNum }}</text>
              </view>
            </view>
          </view>
        </view>
        
        <view v-if="hasMore && !loading" class="load-more">
          <text>上拉加载更多</text>
        </view>
        <view v-if="!hasMore && films.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import Empty from '../../components/Empty/Empty.vue'
import { getDramaPage } from '../../services/backend-api'
// 导入文件URL处理函数
import { getFileUrl } from '../../utils'

export default {
  components: {
    NavBar,
    Empty
  },
  data() {
    return {
      keyword: '',
      films: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalItems: 0,
      hasMore: true,
      nextCursor: null // 添加游标字段
    }
  },
  mounted() {
    // 页面加载时初始化数据
    this.loadFilms(true);
  },
  methods: {
    // 处理海报URL
    getPosterUrl(poster) {
      return getFileUrl(poster)
    },
    handleImageError(e) {
      // 图片加载失败处理
    },
    formatDescription(desc) {
      if (!desc) return ''
      if (desc.length > 40) {
        return desc.substring(0, 40) + '...'
      }
      return desc
    },
    async loadFilms(isRefresh = false) {
      if (this.loading) return;
      
      this.loading = true;
      try {
        const params = {
          cursor: isRefresh ? null : this.nextCursor, // 使用游标而不是页码
          size: this.pageSize,
          keyword: this.keyword
        };
        
        console.log('加载影视作品，参数:', params);
        const response = await getDramaPage(params);
        console.log('API返回结果:', response);
        
        // 后端返回的是游标分页格式：{records, nextCursor, hasMore}
        if (response.records) {
          if (isRefresh) {
            this.films = response.records;
          } else {
            this.films = [...this.films, ...response.records];
          }
          this.nextCursor = response.nextCursor; // 保存游标
          this.hasMore = response.hasMore !== undefined ? response.hasMore : (response.nextCursor !== null);
          this.totalItems = this.films.length; // 游标分页不返回总数
          
          console.log('成功加载影视作品，数量:', this.films.length, '下一页游标:', this.nextCursor);
        } else {
          // 如果是旧的PageResponse格式（有code字段）
          if (response.code === 200 && response.data) {
            const filmRecords = response.data.records || response.data || [];
            if (isRefresh) {
              this.films = filmRecords;
            } else {
              this.films = [...this.films, ...filmRecords];
            }
            this.totalItems = response.data.total || filmRecords.length;
            this.currentPage = 1;
            this.hasMore = response.data.hasMore !== undefined ? response.data.hasMore : (this.films.length < this.totalItems);
            console.log('成功加载影视作品（旧格式），数量:', this.films.length);
          } else {
            console.warn('API返回格式异常:', response);
            uni.showToast({
              title: '数据格式错误',
              icon: 'none'
            });
            this.films = [];
            this.hasMore = false;
          }
        }
      } catch (error) {
        console.error('加载影视作品失败:', error);
        uni.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        });
        this.films = [];
        this.hasMore = false;
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.loadFilms(true) // 搜索时刷新数据
    },
    async loadMore() {
      if (this.loading || !this.hasMore) {
        return;
      }
      
      // 使用loadFilms方法加载更多数据
      await this.loadFilms(false);
    },
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/films/detail?id=${id}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.films-page {
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
  background: linear-gradient(to top, #ffffff 0%, #ef4444 100%);
  z-index: 0;
  opacity: 0.1;
}

.content {
  padding: 16rpx 24rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  width: 100%;
  position: relative;
  z-index: 1;
  height: calc(100vh - 132rpx);
  display: flex;
  flex-direction: column;
}

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

.category-filter {
  margin-bottom: 16rpx;
}

.category-scroll {
  width: 100%;
  white-space: nowrap;
}

.category-item {
  display: inline-block;
  padding: 10rpx 24rpx;
  margin-right: 12rpx;
  background: #fff;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #6b7280;
  transition: all 0.3s;
  border: 1rpx solid transparent;
  
  &.active {
    background: #ef4444;
    color: #fff;
    box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.3);
  }
}

.film-list {
  flex: 1;
  min-height: 0;
}

.film-list-container {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding-bottom: 20rpx;
}

.film-item {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  padding: 0;
  animation: fadeInUp 0.6s ease-out forwards;
  opacity: 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.film-item:active {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.1);
}

.film-cover-wrapper {
  width: 200rpx;
  height: 280rpx;
  position: relative;
  background: #f3f4f6;
  flex-shrink: 0;
}

.film-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.film-type-badge {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  padding: 4rpx 12rpx;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 20rpx;
  border-radius: 8rpx;
}

.film-info {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.film-header {
  margin-bottom: 8rpx;
}

.film-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f2937;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.film-desc {
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.4;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-line-clamp: 3; /* Allow slightly more text for vertical card */
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.film-footer {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.film-meta-text {
  font-size: 22rpx;
  color: #9ca3af;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.load-more,
.no-more {
  text-align: center;
  padding: 30rpx 0;
  font-size: 24rpx;
  color: #9ca3af;
}

.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}
</style>
