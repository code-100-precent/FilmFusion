/**
 * API 服务 - Mock 数据
 */

// Mock 数据
const mockData = {
  visitors: [
    { visitorId: '123', phone: '13800138000', password: '123456', name: '张三', identity: '新生' }
  ],
  pois: [
    {
      poiId: '1',
      name: '图书馆',
      x: 120.123,
      y: 30.456,
      type: '建筑',
      intro: '藏书百万的知识殿堂，是学习和研究的理想场所',
      imageUrl: '',
      openTime: '08:00-22:00'
    },
    {
      poiId: '2',
      name: '教学楼A',
      x: 120.124,
      y: 30.457,
      type: '建筑',
      intro: '主要教学楼，承载着日常教学活动',
      imageUrl: '',
      openTime: '07:00-21:00'
    },
    {
      poiId: '3',
      name: '食堂',
      x: 120.125,
      y: 30.458,
      type: '生活',
      intro: '学生食堂，提供各类美食',
      imageUrl: '',
      openTime: '06:30-19:00'
    },
    {
      poiId: '4',
      name: '体育馆',
      x: 120.126,
      y: 30.459,
      type: '运动',
      intro: '现代化的体育设施，提供各类运动场地',
      imageUrl: '',
      openTime: '06:00-22:00'
    },
    {
      poiId: '5',
      name: '学生活动中心',
      x: 120.127,
      y: 30.460,
      type: '活动',
      intro: '学生社团活动的主要场所',
      imageUrl: '',
      openTime: '08:00-21:00'
    }
  ],
  stories: [
    {
      storyId: '1001',
      title: '在图书馆的奋斗时光',
      content: '四年前，我是一名迷茫的新生。第一次走进图书馆，被那安静的氛围和满架的书籍所震撼。从那时起，图书馆成为了我大学生活中最重要的地方。每天清晨，我会第一个来到图书馆，选择靠窗的位置，开始一天的学习。在这里，我完成了无数次的作业，准备了一次次的考试，也在这里找到了人生的方向。如今即将毕业，回想起在图书馆的每一个日夜，都充满了感激。',
      publishTime: '2025-10-01',
      alumni: {
        alumniId: '2001',
        name: '张三',
        gradYear: '2010',
        major: '计算机科学与技术'
      },
      poi: {
        poiId: '1',
        name: '图书馆'
      },
      emotionTags: [
        { tagId: '3001', tagName: '励志' },
        { tagId: '3002', tagName: '青春' }
      ],
      imageUrl: ''
    },
    {
      storyId: '1002',
      title: '食堂里的温暖回忆',
      content: '记得刚入学时，对食堂的饭菜还不太适应。但慢慢地，我发现食堂阿姨们总是那么热情，她们会记住我们喜欢的菜，会在我们生病时特意准备清淡的饭菜。四年的大学时光，食堂见证了我的成长，也见证了我们的友谊。现在回想起来，那些在食堂一起吃饭、聊天的时光，是最珍贵的回忆。',
      publishTime: '2025-09-28',
      alumni: {
        alumniId: '2002',
        name: '李四',
        gradYear: '2012',
        major: '汉语言文学'
      },
      poi: {
        poiId: '3',
        name: '食堂'
      },
      emotionTags: [
        { tagId: '3003', tagName: '温暖' },
        { tagId: '3002', tagName: '青春' }
      ],
      imageUrl: ''
    }
  ],
  routes: [
    {
      routeId: '4001',
      routeName: '新生校园初体验路线',
      routeDesc: '适合新生快速了解校园核心建筑，包含图书馆、教学楼、食堂等主要场所',
      duration: 60,
      suitableIdentity: '新生',
      pois: [
        { poiId: '1', name: '图书馆', x: 120.123, y: 30.456, order: 1 },
        { poiId: '2', name: '教学楼A', x: 120.124, y: 30.457, order: 2 },
        { poiId: '3', name: '食堂', x: 120.125, y: 30.458, order: 3 }
      ]
    },
    {
      routeId: '4002',
      routeName: '运动健身路线',
      routeDesc: '适合喜欢运动的同学，包含体育馆、操场等运动场所',
      duration: 45,
      suitableIdentity: '其他',
      pois: [
        { poiId: '4', name: '体育馆', x: 120.126, y: 30.459, order: 1 }
      ]
    }
  ],
  collections: [],
  browseHistory: []
}

// 工具函数：模拟网络延迟
const delay = (ms = 500) => new Promise(resolve => setTimeout(resolve, ms))

// 工具函数：模拟成功响应
const successResponse = (data, message = '操作成功') => ({
  code: 200,
  message,
  data
})

// 游客登录
export const visitorLogin = async (params) => {
  await delay()
  const visitor = mockData.visitors.find(v => v.phone === params.phone && v.password === params.password)
  if (visitor) {
    return successResponse({
      visitorId: visitor.visitorId,
      token: `token-${visitor.visitorId}-${Date.now()}`,
      identity: params.identity
    }, '登录成功')
  }
  throw new Error('手机号或密码错误')
}

// 游客注册
export const visitorRegister = async (params) => {
  await delay()
  const exists = mockData.visitors.find(v => v.phone === params.phone)
  if (exists) {
    throw new Error('该手机号已注册')
  }
  const newVisitor = {
    visitorId: String(Date.now()),
    ...params
  }
  mockData.visitors.push(newVisitor)
  return successResponse({
    visitorId: newVisitor.visitorId
  }, '注册成功')
}

// POI列表查询
export const getPoiList = async (params) => {
  await delay()
  let result = [...mockData.pois]
  
  if (params && params.type) {
    result = result.filter(p => p.type === params.type)
  }
  
  if (params && params.keyword) {
    const keyword = params.keyword.toLowerCase()
    result = result.filter(p => p.name.toLowerCase().includes(keyword))
  }
  
  return successResponse(result, '查询成功')
}

// POI详情查询
export const getPoiDetail = async (poiId) => {
  await delay()
  const poi = mockData.pois.find(p => p.poiId === poiId)
  if (!poi) {
    throw new Error('POI不存在')
  }
  
  // 获取该POI相关的故事
  const stories = mockData.stories.filter(s => s.poi.poiId === poiId).map(s => ({
    storyId: s.storyId,
    title: s.title,
    summary: s.content.substring(0, 100) + '...',
    emotionTags: s.emotionTags.map(t => t.tagName)
  }))
  
  return successResponse({
    ...poi,
    stories
  }, '查询成功')
}

// 校友故事列表
export const getStoryList = async (params) => {
  await delay()
  let result = [...mockData.stories]
  
  if (params && params.poiId) {
    result = result.filter(s => s.poi.poiId === params.poiId)
  }
  
  if (params && params.tagId) {
    result = result.filter(s => s.emotionTags.some(t => t.tagId === params.tagId))
  }
  
  if (params && params.keyword) {
    const keyword = params.keyword.toLowerCase()
    result = result.filter(s => s.title.toLowerCase().includes(keyword))
  }
  
  return successResponse(result, '查询成功')
}

// 路线生成
export const generateRoute = async (params) => {
  await delay()
  const route = mockData.routes.find(r => r.suitableIdentity === params.identity && r.duration <= params.duration)
  if (route) {
    // 如果POI数量大于1，尝试优化路线顺序
    if (route.pois.length > 1) {
      route.pois = route.pois.sort((a, b) => a.order - b.order)
    }
    return successResponse(route, '生成成功')
  }
  // 如果没有匹配的，返回默认路线
  const defaultRoute = { ...mockData.routes[0] }
  if (defaultRoute.pois.length > 1) {
    defaultRoute.pois = defaultRoute.pois.sort((a, b) => a.order - b.order)
  }
  return successResponse(defaultRoute, '生成成功')
}

// 高德地图路径规划（支持从起点到终点的路径规划）
export const getAmapRoute = async (points) => {
  // 高德地图路径规划API
  // 文档：https://lbs.amap.com/api/webservice/guide/api/direction
  // 支持步行、驾车、公交等多种路径规划
  
  if (!points || points.length < 2) {
    return successResponse({
      points: points ? points.map(p => ({
        latitude: p.y || p.latitude,
        longitude: p.x || p.longitude
      })) : [],
      distance: 0,
      duration: 0
    }, '路径规划成功')
  }
  
  // 方式1：使用高德地图Web Service API（需要后端支持，或配置API Key）
  // const amapKey = 'your-amap-key' // 需要配置高德地图API Key
  // const origin = `${points[0].x},${points[0].y}`
  // const destination = `${points[points.length - 1].x},${points[points.length - 1].y}`
  // const waypoints = points.slice(1, -1).map(p => `${p.x},${p.y}`).join('|')
  // 
  // try {
  //   // 步行路径规划
  //   const url = `https://restapi.amap.com/v3/direction/walking?key=${amapKey}&origin=${origin}&destination=${destination}&extensions=all`
  //   const res = await uni.request({ url })
  //   if (res.data.status === '1' && res.data.route) {
  //     const path = res.data.route.paths[0]
  //     const steps = path.steps
  //     const routePoints = []
  //     steps.forEach(step => {
  //       const stepPoints = step.polyline.split(';')
  //       stepPoints.forEach(point => {
  //         const [lng, lat] = point.split(',')
  //         routePoints.push({ latitude: parseFloat(lat), longitude: parseFloat(lng) })
  //       })
  //     })
  //     return successResponse({
  //       points: routePoints,
  //       distance: path.distance,
  //       duration: path.duration
  //     }, '路径规划成功')
  //   }
  // } catch (error) {
  //   console.error('高德地图API调用失败', error)
  // }
  
  // 方式2：改进的路径规划算法（生成更真实的路径）
  await delay(300)
  
  const routePoints = []
  const from = points[0]
  const to = points[1]
  
  // 使用改进的路径插值算法
  const steps = 30 // 增加分段数量，使路径更平滑
  for (let i = 0; i <= steps; i++) {
    const ratio = i / steps
    // 使用缓动函数使路径更自然
    const easeRatio = ratio * ratio * (3 - 2 * ratio) // smoothstep函数
    
    const lat = (from.y || from.latitude) + ((to.y || to.latitude) - (from.y || from.latitude)) * easeRatio
    const lng = (from.x || from.longitude) + ((to.x || to.longitude) - (from.x || from.longitude)) * easeRatio
    
    // 添加轻微弯曲，模拟真实道路
    const curve = Math.sin(ratio * Math.PI) * 0.0003
    routePoints.push({
      latitude: lat + curve,
      longitude: lng
    })
  }
  
  // 计算总距离（使用更精确的Haversine公式）
  let totalDistance = 0
  for (let i = 0; i < routePoints.length - 1; i++) {
    const lat1 = routePoints[i].latitude
    const lng1 = routePoints[i].longitude
    const lat2 = routePoints[i + 1].latitude
    const lng2 = routePoints[i + 1].longitude
    
    // Haversine公式计算两点间距离
    const R = 6371000 // 地球半径（米）
    const dLat = (lat2 - lat1) * Math.PI / 180
    const dLng = (lng2 - lng1) * Math.PI / 180
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLng / 2) * Math.sin(dLng / 2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    const distance = R * c
    
    totalDistance += distance
  }
  
  return successResponse({
    points: routePoints,
    distance: Math.round(totalDistance),
    duration: Math.round(totalDistance / 80) // 假设步行速度80米/分钟
  }, '路径规划成功')
}

// 路线列表
export const getRouteList = async (params) => {
  await delay()
  let result = [...mockData.routes]
  
  if (params && params.identity) {
    result = result.filter(r => r.suitableIdentity === params.identity)
  }
  
  if (params && params.duration) {
    result = result.filter(r => r.duration <= params.duration)
  }
  
  return successResponse(result.map(r => ({
    routeId: r.routeId,
    routeName: r.routeName,
    routeDesc: r.routeDesc,
    duration: r.duration,
    suitableIdentity: r.suitableIdentity,
    poisCount: r.pois.length
  })), '查询成功')
}

// 添加收藏
export const addCollect = async (params) => {
  await delay()
  const exists = mockData.collections.find(c => c.poiId === params.poiId && c.visitorId === params.visitorId)
  if (exists) {
    throw new Error('已收藏')
  }
  const poi = mockData.pois.find(p => p.poiId === params.poiId)
  if (!poi) {
    throw new Error('POI不存在')
  }
  mockData.collections.push({
    poiId: params.poiId,
    name: poi.name,
    collectTime: new Date().toISOString().split('T')[0],
    visitorId: params.visitorId
  })
  return successResponse(null, '收藏成功')
}

// 收藏列表
export const getCollectList = async (visitorId) => {
  await delay()
  const result = mockData.collections.filter(c => c.visitorId === visitorId)
  return successResponse(result, '查询成功')
}

// 添加浏览记录
export const addBrowse = async (params) => {
  await delay()
  const poi = mockData.pois.find(p => p.poiId === params.poiId)
  if (!poi) {
    throw new Error('POI不存在')
  }
  // 移除重复记录
  mockData.browseHistory = mockData.browseHistory.filter(
    b => !(b.poiId === params.poiId && b.visitorId === params.visitorId)
  )
  mockData.browseHistory.unshift({
    poiId: params.poiId,
    name: poi.name,
    browseTime: new Date().toISOString().split('T')[0],
    visitorId: params.visitorId
  })
  return successResponse(null, '记录成功')
}

// 浏览记录列表
export const getBrowseList = async (visitorId) => {
  await delay()
  const result = mockData.browseHistory.filter(b => b.visitorId === visitorId)
  return successResponse(result, '查询成功')
}

