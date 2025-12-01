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
            placeholder="搜索影视作品..."
            @confirm="handleSearch"
            @input="handleSearch"
          />
        </view>
      </view>

      <!-- Category Filter -->
      <view class="category-filter">
        <scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
          <view 
            v-for="category in categories" 
            :key="category"
            class="category-item"
            :class="{ active: selectedCategory === category }"
            @click="selectCategory(category)">
            <text>{{ category }}</text>
          </view>
        </scroll-view>
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
        <view v-else class="film-grid">
          <view
            v-for="film in films"
            :key="film.id"
            class="film-card"
            @click="goToDetail(film.id)"
          >
            <view class="film-cover-wrapper">
              <image :src="film.poster" class="film-cover" mode="aspectFill"></image>
              <view class="film-type-badge">{{ film.type }}</view>
            </view>
            <view class="film-info">
              <text class="film-title">{{ film.name }}</text>
              <text class="film-meta">{{ film.releaseDate }} 上映</text>
              <view class="film-tags">
                <text class="film-tag">{{ film.genre }}</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import Empty from '../../components/Empty/Empty.vue'
import { getDramaPage } from '../../services/backend-api'

export default {
  components: {
    NavBar,
    Empty
  },
    data() {
    return {
      keyword: '',
      selectedCategory: '全部',
      categories: ['全部', '自主创作', '外来剧组', '视听资讯', '视听四川', '电影', '电视剧'],
      films: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalItems: 0,
      hasMore: true,
      nextCursor: null // 添加游标字段
    }
  },
  onLoad() {
    this.loadFilms()
  },
  methods: {
    async loadFilms() {
      this.loading = true;
      try {
        const params = {
          current: 1,
          size: this.pageSize,
          keyword: this.keyword
        };
        
        // 如果选择了分类且不是"全部"，添加类型筛选
        if (this.selectedCategory !== '全部') {
          params.type = this.selectedCategory;
        }
        
        console.log('加载影视作品，参数:', params);
        const response = await getDramaPage(params);
        console.log('API返回结果:', response);
        
        // 后端返回的是游标分页格式：{records, nextCursor, hasMore}
        // http工具会直接返回这个对象（因为没有code字段）
        if (response.records) {
          this.films = response.records;
          this.currentPage = 1;
          this.nextCursor = response.nextCursor; // 保存游标
          this.hasMore = response.hasMore || false;
          this.totalItems = this.films.length; // 游标分页不返回总数
          
          console.log('成功加载影视作品，数量:', this.films.length, '下一页游标:', this.nextCursor);
        } else {
          // 如果是旧的PageResponse格式（有code字段）
          if (response.code === 200 && response.data) {
            const filmRecords = response.data.records || response.data || [];
            this.films = filmRecords;
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
      this.loadFilms()
    },
    selectCategory(category) {
      this.selectedCategory = category
      this.loadFilms()
    },
    async loadMore() {
      if (this.loading || !this.hasMore) {
        return;
      }
      
      this.loading = true;
      try {
        const params = {
          cursor: this.nextCursor, // 使用游标而不是页码
          size: this.pageSize,
          keyword: this.keyword
        };
        
        // 如果选择了分类且不是"全部"，添加类型筛选
        if (this.selectedCategory !== '全部') {
          params.type = this.selectedCategory;
        }
        
        console.log('加载更多，参数:', params);
        const response = await getDramaPage(params);
        
        // 处理游标分页格式
        if (response.records) {
          this.films = [...this.films, ...response.records];
          this.nextCursor = response.nextCursor;
          this.hasMore = response.hasMore || false;
          console.log('成功加载更多，新增数量:', response.records.length, '下一页游标:', this.nextCursor);
        } else if (response.code === 200 && response.data) {
          // 兼容旧格式
          const filmRecords = response.data.records || response.data || [];
          this.films = [...this.films, ...filmRecords];
          this.currentPage++;
          this.hasMore = response.data.hasMore !== undefined ? response.data.hasMore : (this.films.length < this.totalItems);
        }
      } catch (error) {
        console.error('加载更多失败:', error);
        uni.showToast({
          title: '加载更多失败',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
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

.film-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  padding-bottom: 16rpx;
}

.film-card {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.film-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.film-cover-wrapper {
  width: 100%;
  aspect-ratio: 3/4;
  position: relative;
  background: #f3f4f6;
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
  padding: 16rpx;
}

.film-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8rpx;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.film-meta {
  font-size: 20rpx;
  color: #9ca3af;
  margin-bottom: 12rpx;
  display: block;
}

.film-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.film-tag {
  font-size: 20rpx;
  color: #ef4444;
  background: #fef2f2;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}
</style>
