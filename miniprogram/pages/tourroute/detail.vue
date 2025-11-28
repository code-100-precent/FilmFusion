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
            <map 
              class="route-map" 
              :latitude="mapCenter.latitude" 
              :longitude="mapCenter.longitude" 
              :scale="11"
              :markers="markers"
              :polyline="polyline"
              show-location
              @markertap="onMarkerTap"
            ></map>
          </view>
          <view class="route-points" v-if="routePoints.length > 0">
            <view class="points-title">途经景点</view>
            <view 
              v-for="(point, index) in routePoints" 
              :key="index" 
              class="point-item"
              @click="navigateToPoint(point)"
            >
              <view class="point-number">{{ index + 1 }}</view>
              <view class="point-info">
                <text class="point-name">{{ point.name }}</text>
                <text class="point-desc">{{ point.description }}</text>
              </view>
              <view class="nav-icon">
                <uni-icons type="paperplane" size="18" color="#6366f1"></uni-icons>
              </view>
            </view>
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
import { getTourById } from '@/services/backend-api'

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
      // 地图相关数据
      mapCenter: {
        latitude: 30.0,
        longitude: 103.0
      },
      markers: [],
      polyline: [],
      routePoints: []
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
      try {
        const res = await getTourById(id)
        
        if (res.code === 200 && res.data) {
          this.route = {
            id: res.data.id,
            name: res.data.name,
            description: res.data.description,
            theme: res.data.theme,
            features: res.data.features,
            cover: res.data.cover,
            transportInfo: res.data.transport,
            accommodation: res.data.hotel,
            foodRecommendation: res.data.food,
            ipWorks: res.data.ipWorks || '暂无相关影视作品',
            image: res.data.image,
            latitude: res.data.latitude || 30.075,
            longitude: res.data.longitude || 102.993
          }
          
          // 加载路线数据
          this.loadRouteData(id)
        } else {
          uni.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          })
          this.route = null
        }
      } catch (error) {
        console.error('加载线路详情失败:', error)
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        })
        this.route = null
      } finally {
        this.loading = false
      }
    },
    
    // 加载路线数据（模拟数据）
    loadRouteData(id) {
      // 两组不同的模拟数据
      const routeDataMap = {
        // 第一组：碧峰峡-蒙顶山-上里古镇 三日游
        1: {
          points: [
            {
              name: '碧峰峡景区',
              latitude: 30.0123,
              longitude: 102.9876,
              description: '国家5A级景区，野生动物园'
            },
            {
              name: '蒙顶山',
              latitude: 30.0678,
              longitude: 102.8765,
              description: '世界茶文化发源地'
            },
            {
              name: '上里古镇',
              latitude: 29.9234,
              longitude: 103.0345,
              description: '历史文化名镇'
            }
          ],
          color: '#6366f1' // 蓝紫色路线
        },
        // 第二组：二郎山-泸定桥-海螺沟 探险游
        2: {
          points: [
            {
              name: '二郎山',
              latitude: 29.8567,
              longitude: 102.7654,
              description: '川藏线重要地标'
            },
            {
              name: '泸定桥',
              latitude: 29.9145,
              longitude: 102.2345,
              description: '红色旅游经典景区'
            },
            {
              name: '海螺沟',
              latitude: 29.5678,
              longitude: 102.0123,
              description: '冰川森林公园'
            },
            {
              name: '磨西古镇',
              latitude: 29.5890,
              longitude: 102.1234,
              description: '川西风情小镇'
            }
          ],
          color: '#10b981' // 绿色路线
        }
      }
      
      // 根据ID选择路线数据，默认使用第一组
      const routeData = routeDataMap[id] || routeDataMap[1]
      this.routePoints = routeData.points
      
      // 初始化地图
      this.initMap(routeData.points, routeData.color)
    },
    
    // 初始化地图标记和路线
    async initMap(points, lineColor = '#6366f1') {
      if (!points || points.length === 0) return
      
      // 创建标记点
      this.markers = points.map((point, index) => ({
        id: index,
        latitude: point.latitude,
        longitude: point.longitude,
        title: point.name,
        iconPath: '/static/location.png',
        width: 30,
        height: 30,
        callout: {
          content: `${index + 1}. ${point.name}`,
          color: '#ffffff',
          fontSize: 12,
          borderRadius: 8,
          bgColor: lineColor,
          padding: 6,
          display: 'ALWAYS'
        }
      }))
      
      // 获取真实道路路线
      await this.fetchRealRoutes(points, lineColor)
      
      // 计算地图中心点（所有点的平均值）
      this.calculateMapCenter(points)
    },
    
    // 获取真实道路路线（使用腾讯地图API）
    async fetchRealRoutes(points, lineColor) {
      if (!points || points.length < 2) {
        this.polyline = []
        return
      }
      
      // 1. 尝试从缓存读取
      // 生成一个唯一的缓存Key，基于路线ID和点的数量
      const cacheKey = `route_cache_${this.route.id}_${points.length}`
      const cachedPolyline = uni.getStorageSync(cacheKey)
      
      if (cachedPolyline) {
        console.log('命中本地缓存，无需调用API')
        this.polyline = JSON.parse(cachedPolyline)
        // 即使有缓存，也重新设置颜色（因为颜色可能变了）
        if (this.polyline && this.polyline.length > 0) {
          this.polyline[0].color = lineColor
          this.polyline[0].borderColor = lineColor
        }
        return
      }
      
      // 2. 无缓存，调用API
      uni.showLoading({
        title: '正在规划路线...',
        mask: true
      })
      
      try {
        const allRoutePoints = []
        
        for (let i = 0; i < points.length - 1; i++) {
          const from = points[i]
          const to = points[i + 1]
          
          // 进一步增加延迟：1.5秒
          // 确保绝对安全，特别是在开发工具热重载时
          if (i > 0) {
            await new Promise(resolve => setTimeout(resolve, 1500))
          }
          
          let routePoints = await this.getDirectionRoute(from, to)
          
          // 失败重试
          if (!routePoints || routePoints.length === 0) {
            console.log('请求被限流，等待3秒后重试...')
            await new Promise(resolve => setTimeout(resolve, 3000))
            routePoints = await this.getDirectionRoute(from, to)
          }
          
          if (routePoints && routePoints.length > 0) {
            if (i === 0) {
              allRoutePoints.push(...routePoints)
            } else {
              allRoutePoints.push(...routePoints.slice(1))
            }
          } else {
            // 降级为直线
            console.log('获取路线失败，降级为直线')
            if (i === 0) allRoutePoints.push({ latitude: from.latitude, longitude: from.longitude })
            allRoutePoints.push({ latitude: to.latitude, longitude: to.longitude })
          }
        }
        
        if (allRoutePoints.length > 0) {
          console.log('成功获取路线点数量:', allRoutePoints.length)
          
          const polylineData = [{
            points: allRoutePoints,
            color: lineColor,
            width: 8, // 加宽一点
            dottedLine: false,
            arrowLine: true, // 保持开启，如果还不显示再关掉
            borderColor: lineColor,
            borderWidth: 1
          }]
          
          this.polyline = polylineData
          
          // 3. 保存到缓存
          // 只有当获取到了真实路线点（点数明显多于直线）才缓存
          if (allRoutePoints.length > points.length * 2) {
            try {
              uni.setStorageSync(cacheKey, JSON.stringify(polylineData))
              console.log('路线数据已缓存')
            } catch (e) {
              console.error('缓存失败', e)
            }
          }
        }
      } catch (error) {
        console.error('获取路线完全失败:', error)
        // 降级方案
        const coordinates = points.map(point => ({
          latitude: point.latitude,
          longitude: point.longitude
        }))
        
        this.polyline = [{
          points: coordinates,
          color: lineColor,
          width: 4,
          dottedLine: false,
          arrowLine: true,
          borderColor: lineColor,
          borderWidth: 2
        }]
      } finally {
        uni.hideLoading()
      }
    },
    
    // 调用腾讯地图API获取两点之间的真实道路路线
    getDirectionRoute(from, to) {
      return new Promise((resolve) => {
        // 腾讯地图Key
        const key = '6EFBZ-D7SKZ-2SAX3-TQORD-F7EST-BAFTP'
        
        // 如果没有配置key，使用模拟的曲线路线
        if (key === 'YOUR_TENCENT_MAP_KEY') {
          console.log('未配置腾讯地图Key，使用模拟曲线路线')
          resolve(this.generateCurvedRoute(from, to))
          return
        }
        
        // 调用腾讯地图路线规划API
        uni.request({
          url: 'https://apis.map.qq.com/ws/direction/v1/driving/',
          data: {
            from: `${from.latitude},${from.longitude}`,
            to: `${to.latitude},${to.longitude}`,
            key: key,
            output: 'json'
          },
          success: (res) => {
            if (res.data.status === 0 && res.data.result && res.data.result.routes) {
              const route = res.data.result.routes[0]
              if (route && route.polyline) {
                // 修复：检查polyline类型
                if (Array.isArray(route.polyline)) {
                  // 如果是数组，直接使用（腾讯地图API有时直接返回坐标数组）
                  // 注意：数组格式通常是 [lat, lng, lat, lng...] 或者是 [{lat,lng}...]
                  // 这里假设是 [lat, lng, lat, lng...] 格式，需要转换
                  const points = []
                  for (let i = 0; i < route.polyline.length; i += 2) {
                    points.push({
                      latitude: route.polyline[i],
                      longitude: route.polyline[i+1]
                    })
                  }
                  resolve(points)
                } else if (typeof route.polyline === 'string') {
                  // 如果是字符串，进行解码
                  const points = this.decodePolyline(route.polyline)
                  resolve(points)
                } else {
                  console.warn('未知的polyline格式:', typeof route.polyline)
                  resolve(this.generateCurvedRoute(from, to))
                }
              } else {
                resolve(this.generateCurvedRoute(from, to))
              }
            } else {
              console.error('腾讯地图API返回错误:', res.data)
              // 如果是QPS限制，返回null以便上层重试
              if (res.data.status === 120) {
                resolve(null)
              } else {
                resolve(this.generateCurvedRoute(from, to))
              }
            }
          },
          fail: (err) => {
            console.error('调用腾讯地图API失败:', err)
            resolve(this.generateCurvedRoute(from, to))
          }
        })
      })
    },
    
    // 解析腾讯地图返回的polyline编码
    decodePolyline(encoded) {
      const points = []
      let index = 0
      let lat = 0
      let lng = 0
      
      while (index < encoded.length) {
        let b, shift = 0, result = 0
        do {
          b = encoded.charCodeAt(index++) - 63
          result |= (b & 0x1f) << shift
          shift += 5
        } while (b >= 0x20)
        const dlat = ((result & 1) ? ~(result >> 1) : (result >> 1))
        lat += dlat
        
        shift = 0
        result = 0
        do {
          b = encoded.charCodeAt(index++) - 63
          result |= (b & 0x1f) << shift
          shift += 5
        } while (b >= 0x20)
        const dlng = ((result & 1) ? ~(result >> 1) : (result >> 1))
        lng += dlng
        
        points.push({
          latitude: lat / 1e5,
          longitude: lng / 1e5
        })
      }
      
      return points
    },
    
    // 生成模拟的曲线路线（当API不可用时使用）
    generateCurvedRoute(from, to, segments = 20) {
      const points = []
      
      for (let i = 0; i <= segments; i++) {
        const t = i / segments
        
        // 使用贝塞尔曲线生成平滑路径
        const lat = from.latitude + (to.latitude - from.latitude) * t
        const lng = from.longitude + (to.longitude - from.longitude) * t
        
        // 添加一些随机偏移，模拟道路弯曲
        const offset = Math.sin(t * Math.PI) * 0.01
        
        points.push({
          latitude: lat + offset,
          longitude: lng + offset * 0.5
        })
      }
      
      return points
    },
    
    // 计算地图中心点
    calculateMapCenter(points) {
      if (!points || points.length === 0) return
      
      const sumLat = points.reduce((sum, p) => sum + p.latitude, 0)
      const sumLng = points.reduce((sum, p) => sum + p.longitude, 0)
      
      this.mapCenter = {
        latitude: sumLat / points.length,
        longitude: sumLng / points.length
      }
    },
    
    // 点击地图标记
    onMarkerTap(e) {
      const markerId = e.detail.markerId
      if (markerId >= 0 && markerId < this.routePoints.length) {
        const point = this.routePoints[markerId]
        uni.showModal({
          title: point.name,
          content: point.description,
          confirmText: '导航',
          cancelText: '关闭',
          success: (res) => {
            if (res.confirm) {
              this.navigateToPoint(point)
            }
          }
        })
      }
    },
    
    // 导航到指定景点
    navigateToPoint(point) {
      if (!point || !point.latitude || !point.longitude) {
        uni.showToast({
          title: '暂无位置信息',
          icon: 'none'
        })
        return
      }
      
      uni.openLocation({
        latitude: point.latitude,
        longitude: point.longitude,
        name: point.name,
        address: point.description,
        scale: 15,
        success: () => {
          console.log('打开地图成功')
        },
        fail: (err) => {
          console.error('打开地图失败:', err)
          uni.showToast({
            title: '打开地图失败',
            icon: 'none'
          })
        }
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

.route-points {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.points-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.point-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: all 0.2s;
  cursor: pointer;
}

.point-item:active {
  background: #f3f4f6;
  transform: scale(0.98);
}

.point-item:last-child {
  margin-bottom: 0;
}

.point-number {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.point-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.point-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.point-desc {
  font-size: 12px;
  color: #9ca3af;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
