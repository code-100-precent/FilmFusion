<template>
  <view class="services-page">
    <!-- 渐变背景层 -->
    <view class="gradient-bg"></view>
    
    <NavBar :show-back="false"></NavBar>

    <view class="content">
      <!-- 搜索栏 -->
      <view class="search-bar">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#999999"></uni-icons>
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            placeholder="搜索服务"
            placeholder-style="color: #666666"
            @confirm="handleSearch"
            @input="handleSearch"
          />
        </view>
      </view>

      <!-- 模块分组列表 -->
      <scroll-view
        class="module-list-scroll"
        scroll-y
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="handleRefresh"
      >
        <view v-if="loading && moduleGroups.length === 0" class="loading-wrapper">
          <Loading></Loading>
        </view>
        <view v-else-if="moduleGroups.length === 0" class="empty-wrapper">
          <Empty text="暂无服务"></Empty>
        </view>
        <view v-else class="module-groups">
          <!-- 遍历每个模块分组 -->
          <view
            v-for="(group, groupIndex) in moduleGroups"
            :key="group.module.id"
            class="module-group"
            :style="{ 'animation-delay': groupIndex * 0.05 + 's' }"
          >
            <!-- 模块标题 -->
            <view class="module-header">
              <view class="module-title-row">
                <uni-icons type="flag-filled" size="20" color="#D4AF37"></uni-icons>
                <text class="module-name">{{ group.module.name }}</text>
                <text class="module-count">({{ group.totalCount }})</text>
              </view>
              <text v-if="group.module.description" class="module-desc">{{ group.module.description }}</text>
            </view>

            <!-- 服务卡片 -->
            <view
              v-if="group.shoot"
              class="shoot-card-compact"
              @click="goToDetail(group.shoot.id)"
            >
              <!-- 服务封面图片 -->
              <view class="shoot-cover-compact">
                <image 
                  v-if="getFileUrl(group.shoot.thumbImage || group.shoot.image)"
                  :src="getFileUrl(group.shoot.thumbImage || group.shoot.image)" 
                  class="cover-image-compact" 
                  mode="aspectFill"
                ></image>
                <view v-else class="cover-placeholder-compact">
                  <uni-icons type="image" size="32" color="#666666"></uni-icons>
                </view>
              </view>
              
              <view class="shoot-content-compact">
                <text class="shoot-name-compact">{{ group.shoot.name }}</text>
                <text class="shoot-desc-compact">{{ group.shoot.description }}</text>
                <view class="shoot-info-compact">
                  <view class="info-item-compact">
                    <uni-icons type="location" size="14" color="#999999"></uni-icons>
                    <text>{{ group.shoot.address }}</text>
                  </view>
                  <view class="info-item-compact">
                    <uni-icons type="phone" size="14" color="#999999"></uni-icons>
                    <text>{{ group.shoot.phone }}</text>
                  </view>
                </view>
              </view>
            </view>

            <!-- 查看更多按钮 -->
            <view
              v-if="group.totalCount > 1"
              class="view-more-btn"
              @click="goToModuleList(group.module.id, group.module.name)"
            >
              <text>查看全部 {{ group.totalCount }} 个服务</text>
              <uni-icons type="arrowright" size="16" color="#D4AF37"></uni-icons>
            </view>

            <!-- 无服务提示 -->
            <view v-if="!group.shoot" class="no-shoot-tip">
              <text>暂无服务</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 底部导航栏 -->
    <TabBar :current="'services'"></TabBar>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import TabBar from '../../components/TabBar/TabBar.vue'
import Loading from '../../components/Loading/Loading.vue'
import Empty from '../../components/Empty/Empty.vue'
// 使用真实后端API
import { getShootPage, getModulePage } from '../../services/backend-api'
import { getFileUrl } from '../../utils'

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
      moduleGroups: [], // 模块分组数据
      loading: false,
      refreshing: false
    }
  },
  onLoad() {
    this.loadModuleGroups()
  },
  methods: {
    async loadModuleGroups(reset = false) {
      if (this.loading) return

      if (reset) {
        this.moduleGroups = []
      }

      this.loading = true
      try {
        // 1. 加载所有模块
        let modules = []
        try {
          const moduleRes = await getModulePage({
            size: 100,
            keyword: this.keyword || undefined
          })

          if (moduleRes && moduleRes.records) {
            modules = Array.isArray(moduleRes.records) ? moduleRes.records : []
          }
        } catch (moduleError) {
          console.error('加载模块失败，使用降级方案:', moduleError)
          // 降级方案：如果模块接口不可用，直接加载所有服务并按 moduleId 分组
          try {
            const allShootsRes = await getShootPage({
              size: 1000,
              keyword: this.keyword || undefined
            })
            
            if (allShootsRes && allShootsRes.records) {
              const shoots = Array.isArray(allShootsRes.records) ? allShootsRes.records : []
              
              // 按 moduleId 分组
              const groupMap = new Map()
              shoots.forEach(shoot => {
                const moduleId = shoot.moduleId || 0
                const moduleName = shoot.moduleName || '未分类服务'
                
                if (!groupMap.has(moduleId)) {
                  groupMap.set(moduleId, {
                    module: {
                      id: moduleId,
                      name: moduleName,
                      description: ''
                    },
                    shoots: []
                  })
                }
                groupMap.get(moduleId).shoots.push(shoot)
              })
              
              // 转换为 moduleGroups 格式
              this.moduleGroups = Array.from(groupMap.values()).map(group => ({
                module: group.module,
                shoot: group.shoots[0],
                totalCount: group.shoots.length
              }))
              
              this.loading = false
              this.refreshing = false
              return
            }
          } catch (fallbackError) {
            console.error('降级方案也失败:', fallbackError)
            throw moduleError // 抛出原始错误
          }
        }

        if (modules.length === 0) {
          this.moduleGroups = []
          return
        }

        // 2. 为每个模块加载对应的服务
        const groupPromises = modules.map(async (module) => {
          try {
            const shootRes = await getShootPage({
              moduleId: module.id,
              size: 1, // 只加载第一个
              keyword: this.keyword || undefined
            })

            // 获取该模块的总数
            let totalCount = 0
            let firstShoot = null

            if (shootRes && shootRes.records) {
              const shoots = Array.isArray(shootRes.records) ? shootRes.records : []
              firstShoot = shoots.length > 0 ? shoots[0] : null
              
              // 如果后端返回了总数，使用它；否则再请求一次获取总数
              if (shootRes.total !== undefined) {
                totalCount = shootRes.total
              } else {
                // 请求获取总数（不带size限制）
                const countRes = await getShootPage({
                  moduleId: module.id,
                  size: 1000,
                  keyword: this.keyword || undefined
                })
                totalCount = countRes && countRes.records ? countRes.records.length : 0
              }
            }

            return {
              module: module,
              shoot: firstShoot,
              totalCount: totalCount
            }
          } catch (error) {
            console.error(`加载模块 ${module.name} 的服务失败:`, error)
            return {
              module: module,
              shoot: null,
              totalCount: 0
            }
          }
        })

        this.moduleGroups = await Promise.all(groupPromises)
        
        // 过滤掉没有服务的模块（可选）
        // this.moduleGroups = this.moduleGroups.filter(g => g.totalCount > 0)

      } catch (error) {
        console.error('加载模块分组失败:', error)
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
      this.loadModuleGroups(true)
    },
    
    handleRefresh() {
      this.refreshing = true
      this.loadModuleGroups(true)
    },
    
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/shoot/detail?id=${id}`
      })
    },
    
    goToModuleList(moduleId, moduleName) {
      uni.navigateTo({
        url: `/pages/services/module-list?moduleId=${moduleId}&moduleName=${encodeURIComponent(moduleName)}`
      })
    },
    
    getFileUrl(url) {
      return getFileUrl(url)
    }
  }
}
</script>

<style lang="scss" scoped>
.services-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #121212;
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
  background: linear-gradient(to top, #121212 0%, #000000 100%);
  z-index: 0;
}

.content {
  padding: 16rpx 24rpx;
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  width: 100%;
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.search-bar {
  margin-bottom: 24rpx;
  flex-shrink: 0;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 0 20rpx;
  height: 72rpx;
  background: #1E1E1E;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
  width: 100%;
  box-sizing: border-box;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #FFFFFF;
}

.module-list-scroll {
  flex: 1;
  min-height: 0;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.module-groups {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding-bottom: 20rpx;
}

.module-group {
  background: #1E1E1E;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
  border: 1rpx solid rgba(255, 255, 255, 0.05);
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

.module-header {
  padding: 24rpx 24rpx 16rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.08);
}

.module-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.module-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #D4AF37;
  flex: 1;
}

.module-count {
  font-size: 24rpx;
  color: #999999;
  font-weight: 500;
}

.module-desc {
  font-size: 24rpx;
  color: #CCCCCC;
  line-height: 1.6;
  display: block;
}

.shoot-card-compact {
  display: flex;
  padding: 20rpx;
  gap: 20rpx;
  transition: background 0.3s;
}

.shoot-card-compact:active {
  background: rgba(255, 255, 255, 0.03);
}

.shoot-cover-compact {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  overflow: hidden;
  background: #2C2C2C;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image-compact {
  width: 100%;
  height: 100%;
  display: block;
}

.cover-placeholder-compact {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2C2C2C;
}

.shoot-content-compact {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.shoot-name-compact {
  font-size: 30rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 8rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.shoot-desc-compact {
  font-size: 26rpx;
  color: #CCCCCC;
  line-height: 1.6;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  flex: 1;
}

.shoot-info-compact {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.info-item-compact {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #999999;
  
  text {
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.view-more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 20rpx;
  background: rgba(212, 175, 55, 0.08);
  border-top: 1rpx solid rgba(212, 175, 55, 0.15);
  transition: all 0.3s;
  
  text {
    font-size: 26rpx;
    color: #D4AF37;
    font-weight: 500;
  }
  
  &:active {
    background: rgba(212, 175, 55, 0.15);
  }
}

.no-shoot-tip {
  padding: 40rpx 20rpx;
  text-align: center;
  
  text {
    font-size: 26rpx;
    color: #666666;
  }
}

.loading-wrapper,
.empty-wrapper {
  padding: 100rpx 0;
  display: flex;
  justify-content: center;
}
</style>
