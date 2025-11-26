<template>
  <view class="detail-page">
    <NavBar :show-back="true" :title="route.name"></NavBar>

    <scroll-view class="content" scroll-y>
      <view v-if="loading" class="loading-wrapper">
        <Loading></Loading>
      </view>
      <view v-else-if="route">
        <!-- 封面图 -->
        <view class="cover-wrapper">
          <image :src="route.cover || defaultCover" class="cover" mode="aspectFill"></image>
          <view class="cover-overlay">
            <view class="theme-tag">{{ route.theme }}</view>
          </view>
        </view>

        <!-- 基本信息 -->
        <view class="info-section">
          <view class="section-title">{{ route.name }}</view>
          <view class="section-content">
            <text>{{ route.description }}</text>
          </view>
        </view>

        <!-- 线路特色 -->
        <view class="info-section">
          <view class="section-title">线路特色</view>
          <view class="section-content">
            <view class="feature-item">
              <uni-icons type="star-filled" size="16" color="#fbbf24"></uni-icons>
              <text>{{ route.features }}</text>
            </view>
          </view>
        </view>

        <!-- 路线地图 -->
        <view class="info-section">
          <view class="section-title">路线地图</view>
          <view class="map-wrapper">
            <map class="route-map" :latitude="29.98" :longitude="102.98" :scale="9"></map>
          </view>
        </view>

        <!-- 影视场景 -->
        <view class="info-section">
          <view class="section-title">影视场景</view>
          <view class="section-content">
            <view class="info-item">
              <uni-icons type="videocam-filled" size="16" color="#ef4444"></uni-icons>
              <text>相关作品：{{ route.ipWorks }}</text>
            </view>
          </view>
        </view>

        <!-- 交通方式 -->
        <view class="info-section">
          <view class="section-title">交通方式</view>
          <view class="section-content">
            <view class="info-item">
              <uni-icons type="location-filled" size="16" color="#6366f1"></uni-icons>
              <text>{{ route.transportInfo }}</text>
            </view>
          </view>
        </view>

        <!-- 住宿推荐 -->
        <view class="info-section">
          <view class="section-title">住宿推荐</view>
          <view class="section-content">
            <view class="info-item">
              <uni-icons type="home" size="16" color="#10b981"></uni-icons>
              <text>{{ route.accommodation }}</text>
            </view>
          </view>
        </view>

        <!-- 美食推荐 -->
        <view class="info-section">
          <view class="section-title">美食推荐</view>
          <view class="section-content">
            <view class="info-item">
              <uni-icons type="fire" size="16" color="#ef4444"></uni-icons>
              <text>{{ route.foodRecommendation }}</text>
            </view>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="action-buttons">
          <button class="btn btn-primary" @click="handleShare">
            <uni-icons type="share" size="18" color="white"></uni-icons>
            分享线路
          </button>
          <button class="btn btn-secondary" @click="handleCollect">
            <uni-icons type="heart" size="18" :color="collected ? '#ef4444' : 'white'"></uni-icons>
            {{ collected ? '已收藏' : '收藏' }}
          </button>
        </view>

        <view style="height: 20px;"></view>
      </view>
      <view v-else class="empty-wrapper">
        <Empty text="线路不存在"></Empty>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import Loading from '@/components/Loading/Loading.vue'
import Empty from '@/components/Empty/Empty.vue'

export default {
  components: {
    NavBar,
    Loading,
    Empty
  },
  data() {
    return {
      route: null,
      loading: true,
      collected: false,
      defaultCover: 'https://via.placeholder.com/400x200?text=Tour+Route',
      mockRoutes: {
  1: {
    id: 1,
    name: '熊猫家园探秘游',
    description: '以在雅安拍摄的熊猫主题影视作品为线索，串联碧峰峡熊猫基地、宝兴蜂桶寨邓池沟等景区景点。线路介绍中注明各景点在影视作品中的出现场景，附游览路线图、交通方式、周边住宿及特色美食推荐，让游客在游览中追寻影视足迹，感受雅安的生态之美。',
    theme: '熊猫文化',
    features: '熊猫文化深度游+熊猫文创市集',
    cover: 'https://via.placeholder.com/400x200?text=Panda+Route',
    transportInfo: '自驾或包车，全程约2-3天。路线：碧峰峡→蜂桶寨邓池沟→貊貊家园、龙苍沟→栗子坪',
    accommodation: '碧峰峡景区周边民宿、五星级酒店',
    foodRecommendation: '雅安特色美食：雨城茶、野生菌、竹笋等',
    ipWorks: '《熊猫计划》《熊猫守护日记》《我和花花是发小》《寻找会说话的貊貊》'
  },
  2: {
    id: 2,
    name: '世界茶源寻根游',
    description: '结合以茶园为背景的影视作品，涵盖蒙顶山茶园、牛碾坪、大地指纹等景点。介绍茶园在影视作品里呈现的四季美景、采茶场景等，提供采茶、制茶体验、特色民宿等推荐。',
    theme: '茶文化',
    features: '茶文化体验+主题民宿+茶艺表演',
    cover: 'https://via.placeholder.com/400x200?text=Tea+Route',
    transportInfo: '自驾或包车，全程约2天。路线：蒙顶山→牛碾坪→大地指纹',
    accommodation: '蒙顶山茶园周边特色民宿',
    foodRecommendation: '蒙顶山茶、茶叶蛋、茶香鸡等',
    ipWorks: '《茶马古道》《爸爸的茶》'
  },
  3: {
    id: 3,
    name: '红色文化体验游',
    description: '围绕红色题材影视作品在雅安的拍摄地，打造包含红军长征翻越夹金山纪念馆、石棉安顺场、宝兴夹金山等景点的主题线路。',
    theme: '红色文化',
    features: '红色记忆追溯+对话人文山川',
    cover: 'https://via.placeholder.com/400x200?text=Red+Culture',
    transportInfo: '自驾或包车，全程约2-3天。路线：名山百丈关→石棉安顺场→宝兴夹金山',
    accommodation: '宝兴县城周边酒店',
    foodRecommendation: '红军餐、山野菜、高山蔬菜等',
    ipWorks: '《红色粮仓》《突进夹金山》《星火》'
  },
  4: {
    id: 4,
    name: '川西古镇风情游',
    description: '循着古镇题材影视作品的拍摄轨迹，将上里古镇、望鱼古镇等串联起来。介绍古镇在影视中展现的独特建筑风貌、民俗风情等场景。',
    theme: '古镇文化',
    features: '古镇休闲+民俗体验',
    cover: 'https://via.placeholder.com/400x200?text=Ancient+Town',
    transportInfo: '自驾或包车，全程约2天。路线：上里古镇→望鱼古镇→清溪古镇→龙门古镇',
    accommodation: '古镇内特色客栈',
    foodRecommendation: '古镇特色小吃、手工豆制品等',
    ipWorks: '《情归上里》《西康往事》《"声"生不息》'
  },
  5: {
    id: 5,
    name: '峡谷秘境探险游',
    description: '依据在峡谷取景的影视作品，串联大渡河峡谷、二郎山喇叭河等景点。介绍峡谷在影视中展现的险峻地势、独特生态等场景。',
    theme: '峡谷探险',
    features: '原生态美景+峡谷探秘',
    cover: 'https://via.placeholder.com/400x200?text=Canyon+Adventure',
    transportInfo: '自驾或包车，全程约2-3天。路线：二郎山喇叭河→龙苍沟→大渡河峡谷→东拉山大峡谷',
    accommodation: '峡谷周边度假村',
    foodRecommendation: '河鱼、野生菌、山野菜等',
        ipWorks: '《囧途之预演告别》'
      }
      }
    }
  },
  onLoad(options) {
    const id = parseInt(options.id)
    if (id) {
      this.loadData(id)
    }
  },
  methods: {
    async loadData(id) {
      this.loading = true
      // 模拟网络延迟
      await new Promise(resolve => setTimeout(resolve, 500))

      this.route = this.mockRoutes[id] || null
      this.loading = false
    },
    handleShare() {
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    handleCollect() {
      this.collected = !this.collected
      uni.showToast({
        title: this.collected ? '已收藏' : '已取消收藏',
        icon: 'success'
      })
    }
  }
}
</script>

<style scoped lang="scss">
.detail-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f8fafc;
}

.content {
  flex: 1;
  overflow-y: auto;
}

.cover-wrapper {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
}

.cover {
  width: 100%;
  height: 100%;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.3) 100%);
  display: flex;
  align-items: flex-end;
  padding: 16px;
}

.theme-tag {
  background: rgba(99, 102, 241, 0.9);
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.info-section {
  background: white;
  margin: 12px 16px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.section-content {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
}

.feature-item,
.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
}

.feature-item:last-child,
.info-item:last-child {
  margin-bottom: 0;
}

.feature-item uni-icons,
.info-item uni-icons {
  margin-right: 8px;
  margin-top: 2px;
  flex-shrink: 0;
}

.feature-item text,
.info-item text {
  flex: 1;
}

.action-buttons {
  display: flex;
  gap: 12px;
  padding: 16px;
  margin: 12px 0;
}

.btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 8px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-secondary {
  background: white;
  color: #6366f1;
  border: 2px solid #6366f1;
}

.loading-wrapper,
.empty-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.map-wrapper {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
}

.route-map {
  width: 100%;
  height: 100%;
}
</style>

