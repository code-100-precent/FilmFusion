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

// ==================== 影视服务相关API ====================

// 文章列表(分页)
export const getArticlePage = async (params) => {
  await delay()
  const { current = 1, size = 10, keyword } = params || {}

  let mockArticles = [
    {
      id: 1,
      title: '雅安影视产业发展迎来新机遇',
      content: '近日,雅安市文化和旅游局发布了最新的影视产业扶持政策...',
      cover: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251124103009.jpg',
      issueUnit: '雅安市文化和旅游局',
      issueTime: [2024, 11, 20, 10, 0, 0],
      createTime: '2024-11-20'
    },
    {
      id: 2,
      title: '《熊猫计划》在雅安取景拍摄圆满杀青',
      content: '由成龙主演的电影《熊猫计划》在雅安碧峰峡景区完成拍摄...',
      cover: null,
      issueUnit: '雅安日报',
      issueTime: [2024, 11, 18, 14, 30, 0],
      createTime: '2024-11-18'
    },
    {
      id: 3,
      title: '雅安打造西南影视拍摄基地',
      content: '雅安市将投资5亿元打造西南地区重要的影视拍摄基地...',
      cover: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/%E5%AE%8B%E6%9C%89%E5%AE%8F-%E3%80%8A%E6%AD%A3%E6%98%AF%E9%87%87%E8%8C%B6%E5%AD%A3%E3%80%8B%2B%E3%80%8122%E5%B9%B4%E6%8B%8D%E4%BA%8E%E9%9B%85%E5%AE%89%E5%90%8D%E5%B1%B1%E5%8C%BA%E7%BA%A2%E6%98%9F%E9%95%87.JPG',
      issueUnit: '四川日报',
      issueTime: [2024, 11, 15, 9, 0, 0],
      createTime: '2024-11-15'
    }
  ]

  if (keyword) {
    mockArticles = mockArticles.filter(a =>
      a.title.includes(keyword) || a.content.includes(keyword)
    )
  }

  const start = (current - 1) * size
  const end = start + size
  const data = mockArticles.slice(start, end)

  return {
    code: 200,
    message: '请求成功',
    data,
    pagination: {
      currentPage: current,
      pageSize: size,
      totalItems: mockArticles.length,
      totalPages: Math.ceil(mockArticles.length / size)
    }
  }
}

// 场地列表(分页)
export const getLocationPage = async (params) => {
  await delay()
  const { current = 1, size = 10, keyword, type } = params || {}

  let mockLocations = [
    {
      id: 1,
      name: '碧峰峡景区',
      type: '自然景观',
      locationDescription: '国家5A级旅游景区,以雅安大熊猫基地和峡谷风光闻名',
      address: '雅安市雨城区碧峰峡镇',
      cover: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/bifengxia.jpg',
      price: 5000,
      contactName: '张经理',
      contactPhone: '13800138001',
      status: 1,
      bestShootingTime: '春季(3-5月)、秋季(9-11月)',
      facilities: '停车场、餐饮、住宿'
    },
    {
      id: 2,
      name: '上里古镇',
      type: '人文景观',
      locationDescription: '四川十大古镇之一,保存完好的明清建筑群',
      address: '雅安市雨城区上里镇',
      cover: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/shangli.jpg',
      price: 3000,
      contactName: '李经理',
      contactPhone: '13800138002',
      status: 1,
      bestShootingTime: '全年适宜',
      facilities: '古建筑、石板路、特色民宿'
    },
    {
      id: 3,
      name: '蒙顶山茶园',
      type: '自然景观',
      locationDescription: '世界茶文化发源地,拥有万亩生态茶园',
      address: '雅安市名山区蒙顶山镇',
      cover: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/mengdingshan.jpg',
      price: 4000,
      contactName: '王经理',
      contactPhone: '13800138003',
      status: 1,
      bestShootingTime: '春季采茶季(3-4月)',
      facilities: '茶园、观景台、茶文化体验馆'
    },
    {
      id: 4,
      name: '雅安市文化中心',
      type: '城市场景',
      locationDescription: '现代化文化设施,适合城市题材拍摄',
      address: '雅安市雨城区雨城大道',
      cover: null,
      price: 2000,
      contactName: '赵经理',
      contactPhone: '13800138004',
      status: 1,
      bestShootingTime: '全年适宜',
      facilities: '演播厅、展览馆、会议室'
    }
  ]

  if (keyword) {
    mockLocations = mockLocations.filter(l =>
      l.name.includes(keyword) || l.locationDescription.includes(keyword) || l.address.includes(keyword)
    )
  }

  if (type) {
    mockLocations = mockLocations.filter(l => l.type === type)
  }

  const start = (current - 1) * size
  const end = start + size
  const data = mockLocations.slice(start, end)

  return {
    code: 200,
    message: '请求成功',
    data,
    pagination: {
      currentPage: current,
      pageSize: size,
      totalItems: mockLocations.length,
      totalPages: Math.ceil(mockLocations.length / size)
    }
  }
}

// 协拍服务列表(分页)
export const getShootPage = async (params) => {
  await delay()
  const { current = 1, size = 10, keyword, type } = params || {}

  let mockShoots = [
    {
      id: 1,
      name: '碧峰峡景区协拍服务',
      description: '提供景区内拍摄协调、场地租赁、群众演员等全方位服务',
      address: '雅安市雨城区碧峰峡镇',
      phone: '13800138001',
      contactName: '张经理',
      price: '5000起',
      status: 1,
      type: '场地服务',
      services: ['场地协调', '群众演员', '道具租赁']
    },
    {
      id: 2,
      name: '雅安影视器材租赁中心',
      description: '提供专业摄影摄像器材租赁,包括相机、灯光、收音等设备',
      address: '雅安市雨城区青衣江路',
      phone: '13800138005',
      contactName: '刘经理',
      price: '500/天起',
      status: 1,
      type: '器材租赁',
      services: ['相机租赁', '灯光设备', '收音设备', '航拍设备']
    },
    {
      id: 3,
      name: '雅安剧组食宿服务',
      description: '为剧组提供专业的餐饮和住宿服务,可定制菜单',
      address: '雅安市雨城区',
      phone: '13800138006',
      contactName: '陈经理',
      price: '80/人/天起',
      status: 1,
      type: '食宿服务',
      services: ['团队餐饮', '酒店住宿', '营养配餐']
    },
    {
      id: 4,
      name: '雅安影视车辆租赁',
      description: '提供各类拍摄用车,包括商务车、越野车、道具车等',
      address: '雅安市雨城区',
      phone: '13800138007',
      contactName: '周经理',
      price: '800/天起',
      status: 1,
      type: '车辆租赁',
      services: ['商务车', '越野车', '道具车', '货车']
    }
  ]

  if (keyword) {
    mockShoots = mockShoots.filter(s =>
      s.name.includes(keyword) || s.description.includes(keyword)
    )
  }

  if (type) {
    mockShoots = mockShoots.filter(s => s.type === type)
  }

  const start = (current - 1) * size
  const end = start + size
  const data = mockShoots.slice(start, end)

  return {
    code: 200,
    message: '请求成功',
    data,
    pagination: {
      currentPage: current,
      pageSize: size,
      totalItems: mockShoots.length,
      totalPages: Math.ceil(mockShoots.length / size)
    }
  }
}

// 剧组报备
export const createReport = async (params) => {
  await delay()
  console.log('剧组报备信息:', params)

  // 模拟保存到数据库
  const reportId = 'R' + Date.now()

  return {
    code: 200,
    message: '报备成功',
    data: {
      reportId,
      status: '待审核',
      submitTime: new Date().toISOString()
    }
  }
}

// 文件上传(模拟)
export const uploadFile = async (file) => {
  await delay(800)

  // 模拟文件上传,返回文件URL
  return {
    code: 200,
    message: '上传成功',
    data: {
      url: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/' + Date.now() + '.pdf',
      fileName: file.name || 'document.pdf',
      fileSize: file.size || 1024000
    }
  }
}

// 根据ID获取场地详情
export const getLocationById = async (id) => {
  await delay()

  const mockLocations = [
    {
      id: 1,
      name: '碧峰峡景区',
      type: '自然景观',
      locationDescription: '国家5A级旅游景区,以雅安大熊猫基地和峡谷风光闻名。景区内山峦叠翠,峡谷幽深,瀑布飞泻,是拍摄自然风光和动物题材的绝佳场所。',
      address: '雅安市雨城区碧峰峡镇',
      cover: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/bifengxia.jpg',
      price: 5000,
      contactName: '张经理',
      contactPhone: '13800138001',
      status: 1,
      bestShootingTime: '春季(3-5月)、秋季(9-11月)',
      facilities: '停车场、餐饮、住宿、观光车',
      latitude: 30.0123,
      longitude: 102.9876
    },
    {
      id: 2,
      name: '上里古镇',
      type: '人文景观',
      locationDescription: '四川十大古镇之一,保存完好的明清建筑群。古镇依山傍水,石板街道纵横交错,古朴典雅,是拍摄古装剧和历史题材的理想场所。',
      address: '雅安市雨城区上里镇',
      cover: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/shangli.jpg',
      price: 3000,
      contactName: '李经理',
      contactPhone: '13800138002',
      status: 1,
      bestShootingTime: '全年适宜',
      facilities: '古建筑、石板路、特色民宿、茶馆',
      latitude: 30.1234,
      longitude: 102.8765
    },
    {
      id: 3,
      name: '蒙顶山茶园',
      type: '自然景观',
      locationDescription: '世界茶文化发源地,拥有万亩生态茶园。茶园层层叠叠,绿意盎然,云雾缭绕,是拍摄茶文化和田园风光的最佳选择。',
      address: '雅安市名山区蒙顶山镇',
      cover: 'https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/mengdingshan.jpg',
      price: 4000,
      contactName: '王经理',
      contactPhone: '13800138003',
      status: 1,
      bestShootingTime: '春季采茶季(3-4月)',
      facilities: '茶园、观景台、茶文化体验馆、停车场',
      latitude: 30.0678,
      longitude: 103.0345
    },
    {
      id: 4,
      name: '雅安市文化中心',
      type: '城市场景',
      locationDescription: '现代化文化设施,适合城市题材拍摄。建筑设计现代简约,内部设施完善,可满足各类室内拍摄需求。',
      address: '雅安市雨城区雨城大道',
      cover: null,
      price: 2000,
      contactName: '赵经理',
      contactPhone: '13800138004',
      status: 1,
      bestShootingTime: '全年适宜',
      facilities: '演播厅、展览馆、会议室、化妆间',
      latitude: 30.0456,
      longitude: 102.9987
    }
  ]

  const location = mockLocations.find(l => l.id === id)

  if (!location) {
    return {
      code: 404,
      message: '场地不存在',
      data: null
    }
  }

  return {
    code: 200,
    message: '查询成功',
    data: location
  }
}


