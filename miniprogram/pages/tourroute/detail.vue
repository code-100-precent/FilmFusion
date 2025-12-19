<template>
  <view class="detail-page">
    <NavBar :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y>
      <view v-if="loading" class="loading-wrapper">
        <Loading></Loading>
      </view>
      <view v-else-if="route">
        <!-- 封面图 -->
        <view class="cover-wrapper">
          <swiper 
            v-if="bannerImages.length > 1" 
            class="banner-swiper" 
            circular 
            indicator-dots 
            autoplay 
            :interval="4000" 
            :duration="500"
            indicator-active-color="#6366f1"
            indicator-color="rgba(255, 255, 255, 0.5)"
          >
            <swiper-item v-for="(img, index) in bannerImages" :key="index">
              <image :src="img" class="cover" mode="aspectFill" @click="previewImage(index)"></image>
            </swiper-item>
          </swiper>
          <image v-else :src="bannerImages[0] || defaultCover" class="cover" mode="aspectFill" @click="previewImage(0)"></image>
          
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
              v-if="!mapError"
              class="route-map" 
              :latitude="mapCenter.latitude" 
              :longitude="mapCenter.longitude" 
              :scale="11"
              :markers="markers"
              :polyline="polyline"
              show-location
              @markertap="onMarkerTap"
            ></map>
            <view v-else class="map-error-container">
              <uni-icons type="info-filled" size="40" color="#ef4444"></uni-icons>
              <text class="map-error-text">位置错误，请联系管理员。</text>
            </view>
          </view>
          <view class="route-points" v-if="routePoints.length > 0">
            <view class="points-title">
              <text>途经景点</text>
              <text class="points-count">（{{ routePoints.length }}个）</text>
            </view>
            <view 
              v-for="(point, index) in routePoints" 
              :key="index" 
              class="point-card-wrapper"
            >
              <view class="point-card" @click="onPointClick(index)">
                <image 
                  v-if="point.cover || point.thumbCover"
                  :src="getFileUrl(point.thumbCover || point.cover) || defaultCover" 
                  class="point-image" 
                  mode="aspectFill"
                ></image>
                <view class="point-content">
                  <view class="point-header">
                    <view class="point-number">{{ index + 1 }}</view>
                    <text class="point-name">{{ point.name }}</text>
                  </view>
                  <text v-if="point.address" class="point-address">{{ point.address }}</text>
                  <text class="point-desc">{{ point.description || point.locationDescription || point.type }}</text>
                </view>
                <view class="point-actions">
                  <view class="point-nav-icon" @click.stop="onPointClick(index)">
                    <uni-icons type="paperplane" size="20" color="#6366f1"></uni-icons>
                  </view>
                  <view 
                    v-if="point.relatedDramas && point.relatedDramas.length > 0"
                    class="point-expand-icon" 
                    @click.stop="togglePointExpand(index)"
                  >
                    <uni-icons 
                      :type="expandedPoints[index] ? 'up' : 'down'" 
                      size="20" 
                      color="#6366f1"
                    ></uni-icons>
                  </view>
                </view>
              </view>
              
              <!-- 相关影视下拉内容 -->
              <view 
                v-if="point.relatedDramas && point.relatedDramas.length > 0 && expandedPoints[index]" 
                class="point-dramas"
              >
                <view class="dramas-title">相关影视</view>
                <view 
                  v-for="drama in point.relatedDramas" 
                  :key="drama.id" 
                  class="drama-item-small"
                  @click="goToDramaDetail(drama.id)"
                >
                  <image 
                    v-if="drama.poster"
                    :src="getFileUrl(drama.poster)" 
                    class="drama-poster-small" 
                    mode="aspectFill"
                  ></image>
                  <view class="drama-info-small">
                    <text class="drama-name-small">{{ drama.name }}</text>
                    <text class="drama-type-small" v-if="drama.type">{{ drama.type }}</text>
                  </view>
                  <uni-icons type="right" size="14" color="#9ca3af"></uni-icons>
                </view>
              </view>
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
              <text>{{ route.accommodation || '暂无住宿推荐' }}</text>
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
import { getTourById, getNearbyHotels, getNearbyLocations, getLocationById, getDramaById } from '@/services/backend-api'
import { getFileUrl } from '@/utils'

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
      mapError: false,
      defaultCover: 'https://via.placeholder.com/750x400',
      mapContext: null,
      mapCenter: {
        latitude: 30.0,
        longitude: 103.0
      },
      markers: [],
      polyline: [],
      routePoints: [],
      // 周边数据
      nearbyHotels: [],
      // 景点展开状态
      expandedPoints: {}
    }
  },
  computed: {
    bannerImages() {
      if (!this.route) return []
      let images = []
      
      const imgData = this.route.image
      if (Array.isArray(imgData) && imgData.length > 0) {
        images = imgData.map(img => getFileUrl(img))
      } else if (typeof imgData === 'string' && imgData) {
        images = imgData.split(',').map(img => getFileUrl(img))
      }
      
      // If images list is empty, try to use cover/image
      if (images.length === 0) {
        const cover = this.route.image || this.route.cover || this.route.thumbImage
        if (Array.isArray(cover)) {
          images = cover.map(img => getFileUrl(img))
        } else if (typeof cover === 'string' && cover) {
          images = cover.split(',').map(img => getFileUrl(img))
        }
      }
      
      images = [...new Set(images)].filter(img => img)
      
      // 用户要求：详情页面展示的图片是除了第一个以外的图片
      // 如果有多张图片，移除第一张（通常是列表封面）；如果只有一张，保留显示
      if (images.length > 1) {
        return images.slice(1)
      }
      
      return images
    }
  },
  onLoad(options) {
    const id = parseInt(options.id)
    if (id) {
      this.loadData(id)
    }
  },
  methods: {
    getFileUrl,
    previewImage(index) {
      if (this.bannerImages && this.bannerImages.length > 0) {
        uni.previewImage({
          urls: this.bannerImages,
          current: index
        })
      }
    },
    async loadData(id) {
      this.loading = true
      try {
        const res = await getTourById(id)
        
        if (res.code === 200 && res.data) {
          // 处理封面图：如果后端未返回cover但返回了image，则使用image的第一张作为封面
          let coverUrl = res.data.cover
          if (!coverUrl && res.data.image) {
            if (Array.isArray(res.data.image) && res.data.image.length > 0) {
              coverUrl = res.data.image[0]
            } else if (typeof res.data.image === 'string') {
              coverUrl = res.data.image
            }
          }

          // 处理影视作品名称
          let ipWorksStr = res.data.ipWorks || '暂无相关影视作品'
          // 如果有dramaId，优先使用dramaId获取影视作品名称
          if (res.data.dramaId) {
            try {
              // dramaId可能是逗号分隔的字符串
              const dramaIds = String(res.data.dramaId).split(',').filter(id => id.trim())
              if (dramaIds.length > 0) {
                const dramaPromises = dramaIds.map(dId => getDramaById(parseInt(dId)))
                const dramaResults = await Promise.allSettled(dramaPromises)
                
                const dramaNames = dramaResults
                  .filter(r => r.status === 'fulfilled' && r.value?.code === 200 && r.value?.data)
                  .map(r => r.value.data.name)
                
                if (dramaNames.length > 0) {
                  ipWorksStr = dramaNames.join('、')
                }
              }
            } catch (e) {
              console.warn('获取关联影视作品失败:', e)
            }
          }

          this.route = {
            id: res.data.id,
            name: res.data.name,
            description: res.data.description,
            theme: res.data.theme,
            features: res.data.features,
            cover: coverUrl,
            transportInfo: res.data.transport,
            accommodation: res.data.hotel,
            foodRecommendation: res.data.food,
            ipWorks: ipWorksStr,
            image: res.data.image,
            latitude: res.data.latitude || 30.075,
            longitude: res.data.longitude || 102.993,
            locationId: res.data.locationId  // 途经景点ID列表
          }
          
          // 不再使用模拟数据，改为从后端加载真实的location数据
          // this.loadRouteData(id)
          
          // 只加载途经景点，不加载住宿列表（住宿推荐只显示文本）
          this.loadRoutePoints(this.route.latitude, this.route.longitude, this.route.locationId)
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
    
    // 只加载途经景点（不加载住宿）
    async loadRoutePoints(latitude, longitude, locationId) {
      try {
        // 如果tour有locationId，则根据ID获取对应的location
        // 如果没有，则获取周边的location
        if (locationId) {
          await this.loadLocationsByIds(locationId)
        } else {
          await this.loadNearbyLocations(latitude, longitude)
        }
      } catch (error) {
        console.error('加载途经景点失败:', error)
      }
    },
    
    // 加载周边住宿和景点（已废弃，保留以防需要）
    async loadNearbyData(latitude, longitude) {
      console.log('开始加载周边数据，经纬度:', latitude, longitude)
      try {
        // 加载周边住宿
        const hotelsRes = await getNearbyHotels({
          latitude,
          longitude,
          size: 5
        })
        console.log('住宿API返回结果:', hotelsRes)
        
        // 修复：API返回的是PageResponse，数据在records字段中
        if (hotelsRes.code === 200 && hotelsRes.data) {
          const hotelRecords = hotelsRes.data.records || hotelsRes.data || []
          this.nearbyHotels = hotelRecords.map(hotel => ({
            id: hotel.id,
            name: hotel.name,
            address: hotel.address,
            description: hotel.description,
            cover: hotel.cover,
            thumbCover: hotel.thumbCover,
            latitude: parseFloat(hotel.latitude) || 0,
            longitude: parseFloat(hotel.longitude) || 0
          }))
          console.log('成功加载住宿数据，数量:', this.nearbyHotels.length)
          console.log('住宿列表:', this.nearbyHotels)
        } else {
          console.warn('住宿API返回失败:', hotelsRes.message)
          this.nearbyHotels = []
        }
        
        // 加载途经景点
        // 如果tour有locationId，则根据ID获取对应的location
        // 如果没有，则获取周边的location
        if (this.route && this.route.locationId) {
          await this.loadLocationsByIds(this.route.locationId)
        } else {
          await this.loadNearbyLocations(latitude, longitude)
        }
      } catch (error) {
        console.error('加载周边数据失败:', error)
        this.nearbyHotels = []
        // 不显示错误提示，静默失败
      }
    },
    
    // 根据ID列表加载location
    async loadLocationsByIds(locationIds) {
      try {
        // locationIds可能是逗号分隔的字符串，如 "1,2,3"
        const ids = locationIds.split(',').map(id => id.trim()).filter(id => id)
        
        if (ids.length === 0) {
          console.log('没有途经景点ID')
          return
        }
        
        // 逐个获取location详情
        const locationPromises = ids.map(id => getLocationById(parseInt(id)))
        const results = await Promise.all(locationPromises)
        
        // 过滤成功的结果并映射数据
        this.routePoints = await Promise.all(
          results
            .filter(res => res.code === 200 && res.data)
            .map(async res => {
              const location = res.data
              const point = {
                id: location.id,
                name: location.name,
                address: location.address,
                description: location.locationDescription || location.type || '暂无描述',
                locationDescription: location.locationDescription,
                type: location.type,
                cover: location.cover,
                thumbCover: location.thumbCover,
                latitude: parseFloat(location.latitude) || 0,
                longitude: parseFloat(location.longitude) || 0,
                dramaId: location.dramaId,
                relatedDramas: []
              }
              
              // 加载相关影视
              if (location.dramaId) {
                point.relatedDramas = await this.loadDramasForPoint(location.dramaId)
              }
              
              return point
            })
        )
        
        // 使用真实的location数据初始化地图
        if (this.routePoints.length > 0) {
          this.initMap(this.routePoints, '#6366f1')
        }
      } catch (error) {
        console.error('根据ID加载景点失败:', error)
      }
    },
    
    // 为景点加载相关影视
    async loadDramasForPoint(dramaIds) {
      try {
        const ids = String(dramaIds).split(',').map(id => id.trim()).filter(id => id)
        if (ids.length === 0) return []
        
        const dramaPromises = ids.map(id => getDramaById(parseInt(id)))
        const results = await Promise.allSettled(dramaPromises)
        
        return results
          .filter(r => r.status === 'fulfilled' && r.value?.code === 200 && r.value?.data)
          .map(r => ({
            id: r.value.data.id,
            name: r.value.data.name,
            poster: r.value.data.image || r.value.data.cover || r.value.data.poster,
            type: r.value.data.type || '影视作品'
          }))
      } catch (error) {
        console.warn('加载景点相关影视失败:', error)
        return []
      }
    },
    
    // 切换景点展开状态
    togglePointExpand(index) {
      this.expandedPoints = {
        ...this.expandedPoints,
        [index]: !this.expandedPoints[index]
      }
    },
    
    // 跳转到影视详情
    goToDramaDetail(id) {
      uni.navigateTo({
        url: `/pages/films/detail?id=${id}`
      })
    },
    
    // 加载周边的location（当tour没有指定locationId时使用）
    async loadNearbyLocations(latitude, longitude) {
      try {
        const locationsRes = await getNearbyLocations({
          latitude,
          longitude,
          size: 10
        })
        if (locationsRes.code === 200 && locationsRes.data) {
          // 将location数据映射到routePoints
          this.routePoints = locationsRes.data.map(location => ({
            id: location.id,
            name: location.name,
            address: location.address,
            description: location.locationDescription || location.type || '暂无描述',
            locationDescription: location.locationDescription,
            type: location.type,
            cover: location.cover,
            thumbCover: location.thumbCover,
            latitude: parseFloat(location.latitude) || 0,
            longitude: parseFloat(location.longitude) || 0
          }))
          
          // 使用真实的location数据初始化地图
          if (this.routePoints.length > 0) {
            this.initMap(this.routePoints, '#6366f1')
          }
        }
      } catch (error) {
        console.error('加载周边景点失败:', error)
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
      this.mapError = false
      if (!points || points.length === 0) return
      
      // 检查经纬度是否规范
      const hasInvalidCoord = points.some(p => {
        const lat = parseFloat(p.latitude)
        const lng = parseFloat(p.longitude)
        return isNaN(lat) || isNaN(lng) || lat < -90 || lat > 90 || lng < -180 || lng > 180
      })

      if (hasInvalidCoord) {
        this.mapError = true
        return
      }

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
    
    // 获取路线（简化版：直接连接景点，显示箭头）
    async fetchRealRoutes(points, lineColor) {
      if (!points || points.length < 2) {
        this.polyline = []
        return
      }
      
      // 直接使用景点坐标作为路线点
      // 为了让箭头更明显，我们不使用真实道路，而是使用直线连接
      // 这样既"简洁"又"方向明确"
      
      const allRoutePoints = points.map(point => ({
        latitude: point.latitude,
        longitude: point.longitude
      }))
      
      const polylineData = [{
        points: allRoutePoints,
        color: lineColor,
        width: 7, // 调整宽度，使其更精致
        dottedLine: false,
        arrowLine: true, // 保持箭头
        // 移除白色边框，减少突兀感，使箭头与线条融合更自然
      }]
      
      this.polyline = polylineData
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
    
    // 点击景点列表项
    onPointClick(index) {
      if (index >= 0 && index < this.routePoints.length) {
        const point = this.routePoints[index]
        this.navigateToPoint(point)
      } else {
        console.error('无效的景点索引:', index)
      }
    },
    
    // 导航到指定景点
    navigateToPoint(point) {
      console.log('导航到景点:', point)
      
      // 尝试转换经纬度
      const lat = Number(point?.latitude)
      const lng = Number(point?.longitude)
      
      // 检查经纬度是否有效（非NaN且非0）
      if (!point || isNaN(lat) || isNaN(lng) || (lat === 0 && lng === 0)) {
        console.warn('位置信息无效:', point)
        uni.showToast({
          title: '暂无位置信息',
          icon: 'none'
        })
        return
      }
      
      uni.openLocation({
        latitude: lat,
        longitude: lng,
        name: point.name || '未知地点',
        address: point.address || point.description || '',
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
    },
    
    // 导航到住宿
    navigateToHotel(hotel) {
      console.log('准备导航到酒店:', hotel)
      
      // 确保经纬度是有效的数字
      const lat = parseFloat(hotel?.latitude)
      const lng = parseFloat(hotel?.longitude)
      
      if (!hotel || isNaN(lat) || isNaN(lng) || (Math.abs(lat) < 0.000001 && Math.abs(lng) < 0.000001)) {
        console.warn('酒店位置信息无效:', hotel)
        uni.showToast({
          title: '暂无位置信息',
          icon: 'none'
        })
        return
      }
      
      uni.openLocation({
        latitude: lat,
        longitude: lng,
        name: hotel.name || '目的地',
        address: hotel.address || '',
        scale: 15,
        success: () => {
          console.log('打开地图导航成功')
        },
        fail: (err) => {
          console.error('打开地图失败:', err)
          uni.showToast({
            title: '打开地图失败',
            icon: 'none'
          })
        }
      })
    },

    // 跳转到酒店详情
    goToHotelDetail(id) {
      uni.navigateTo({
        url: `/pages/hotel/detail?id=${id}`
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

.banner-swiper {
  width: 100%;
  height: 100%;
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
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-subtitle {
  font-size: 13px;
  font-weight: 400;
  color: #9ca3af;
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
  display: flex;
  align-items: center;
  gap: 6px;
}

.points-count {
  font-size: 13px;
  font-weight: 400;
  color: #9ca3af;
}

.point-card-wrapper {
  margin-bottom: 12px;
}

.point-card-wrapper:last-child {
  margin-bottom: 0;
}

.point-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 10px;
  transition: all 0.2s;
  cursor: pointer;
}

.point-card:active {
  background: #f3f4f6;
  transform: scale(0.98);
}

.point-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  flex-shrink: 0;
  object-fit: cover;
}

.point-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.point-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.point-number {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.point-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.point-address {
  font-size: 12px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.point-desc {
  font-size: 12px;
  color: #9ca3af;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.point-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

.point-nav-icon,
.point-expand-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.point-expand-icon:active {
  transform: scale(0.95);
  background: #f3f4f6;
}

.point-dramas {
  margin-top: 8px;
  padding: 12px;
  background: white;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
}

.dramas-title {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid #6366f1;
}

.drama-item-small {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: all 0.2s;
}

.drama-item-small:last-child {
  margin-bottom: 0;
}

.drama-item-small:active {
  background: #f3f4f6;
  transform: scale(0.98);
}

.drama-poster-small {
  width: 60px;
  height: 84px;
  border-radius: 6px;
  flex-shrink: 0;
  object-fit: cover;
}

.drama-info-small {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.drama-name-small {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.drama-type-small {
  font-size: 12px;
  color: #6b7280;
}


// 周边住宿和景点样式
.nearby-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nearby-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 10px;
  transition: all 0.2s;
  cursor: pointer;
}

.nearby-item:active {
  background: #f3f4f6;
  transform: scale(0.98);
}

.nearby-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  flex-shrink: 0;
  object-fit: cover;
}

.nearby-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.nearby-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nearby-address {
  font-size: 12px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nearby-desc {
  font-size: 12px;
  color: #9ca3af;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.nearby-action {
  flex-shrink: 0;
  padding-left: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 移除旧的背景样式，避免干扰新按钮 */
  background: transparent;
  box-shadow: none;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  background: #6366f1;
  padding: 8rpx 16rpx;
  border-radius: 32rpx;
  transition: all 0.3s;
  box-shadow: 0 2rpx 8rpx rgba(99, 102, 241, 0.3);
}

.nav-btn:active {
  transform: scale(0.95);
  background: #4f46e5;
}

.nav-text {
  font-size: 22rpx;
  color: #fff;
  font-weight: 500;
}

.map-error-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  border-radius: 8px;
}

.map-error-text {
  margin-top: 8px;
  font-size: 14px;
  color: #6b7280;
}
</style>
