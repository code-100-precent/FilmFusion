/**
 * Mock 数据 - 完整的测试数据
 */

// 类型定义
interface Collection {
  id?: number
  poiId: string
  visitorId: string
  name?: string
  collectTime?: string
}

interface BrowseRecord {
  id?: number
  poiId: string
  visitorId: string
  name?: string
  browseTime?: string
}

interface Report {
  id: number
  [key: string]: any
}

// 模拟网络延迟
const delay = (ms = 500) => new Promise(resolve => setTimeout(resolve, ms))

// Mock 数据存储
const mockData = {
  // 用户数据
  visitors: [
    {
      visitorId: 'visitor_001',
      name: '张导演',
      phone: '13800138000',
      password: '123456',
      identity: '剧组',
      registerTime: '2024-01-15T10:30:00Z',
      lastLoginTime: '2024-12-20T14:20:00Z',
      avatar: '',
      token: 'mock_token_visitor_001'
    },
    {
      visitorId: 'visitor_002',
      name: '李制片',
      phone: '13900139000',
      password: '123456',
      identity: '制作人',
      registerTime: '2024-02-20T09:15:00Z',
      lastLoginTime: '2024-12-19T16:45:00Z',
      avatar: '',
      token: 'mock_token_visitor_002'
    }
  ],

  // POI 数据
  pois: [
    {
      poiId: 'poi_001',
      name: '雅安古城墙',
      x: 103.0034,
      y: 29.9784,
      type: '文化',
      intro: '雅安古城墙是雅安市重要的历史文化遗址，始建于明代，是研究古代城市防御体系的重要实物资料。城墙保存较为完整，展现了古代建筑工艺的精湛。',
      imageUrl: 'https://images.unsplash.com/photo-1545239351-1141bd82e8a6?auto=format&fit=crop&w=800&q=60',
      openTime: '08:00-18:00',
      tags: ['历史', '文化', '建筑']
    },
    {
      poiId: 'poi_002',
      name: '碧峰峡风景区',
      x: 102.9876,
      y: 30.0123,
      type: '自然',
      intro: '碧峰峡是雅安著名的自然风景区，以峡谷、瀑布、原始森林为主要景观。这里山清水秀，空气清新，是拍摄自然风光和生态题材的理想取景地。',
      imageUrl: 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=800&q=60',
      openTime: '07:00-19:00',
      tags: ['自然', '生态', '峡谷']
    },
    {
      poiId: 'poi_003',
      name: '上里古镇',
      x: 102.9543,
      y: 30.0456,
      type: '文化',
      intro: '上里古镇是四川保存最完好的明清古建筑群之一，古镇依山傍水，古色古香。石板街道、木质建筑、传统手工艺，为年代剧和古装剧提供了绝佳的拍摄场景。',
      imageUrl: 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=60',
      openTime: '全天开放',
      tags: ['古镇', '历史', '建筑']
    },
    {
      poiId: 'poi_004',
      name: '蒙顶山茶园',
      x: 103.0234,
      y: 30.0567,
      type: '自然',
      intro: '蒙顶山是中国茶文化的发源地之一，茶园梯田层层叠叠，云雾缭绕。这里不仅是茶叶产地，更是拍摄田园风光和茶文化题材的绝佳场所。',
      imageUrl: 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?auto=format&fit=crop&w=800&q=60',
      openTime: '08:00-17:00',
      tags: ['茶园', '自然', '文化']
    },
    {
      poiId: 'poi_005',
      name: '雅安熊猫基地',
      x: 102.9987,
      y: 30.0345,
      type: '建筑',
      intro: '雅安是大熊猫的故乡，熊猫基地集科研、保护、教育于一体。现代化的建筑设施与自然生态环境完美融合，适合拍摄科普、纪录片等题材。',
      imageUrl: 'https://images.unsplash.com/photo-1600585154340-0ef3c08dcdb6?auto=format&fit=crop&w=800&q=60',
      openTime: '09:00-17:00',
      tags: ['现代', '科普', '生态']
    },
    {
      poiId: 'poi_006',
      name: '青衣江畔',
      x: 103.0123,
      y: 29.9876,
      type: '自然',
      intro: '青衣江是雅安的母亲河，江畔风光旖旎，既有现代城市风貌，又保留着自然生态。适合拍摄城市风光、生活场景等题材。',
      imageUrl: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=800&q=60',
      openTime: '全天开放',
      tags: ['江景', '城市', '自然']
    },
    {
      poiId: 'poi_007',
      name: '雅安博物馆',
      x: 103.0012,
      y: 29.9923,
      type: '建筑',
      intro: '雅安博物馆展示了雅安深厚的历史文化底蕴，建筑风格现代大气。馆内丰富的文物和展品为历史题材影视作品提供了丰富的素材。',
      imageUrl: 'https://images.unsplash.com/photo-1600585154340-0ef3c08dcdb6?auto=format&fit=crop&w=800&q=60',
      openTime: '09:00-17:00',
      tags: ['文化', '历史', '现代']
    },
    {
      poiId: 'poi_008',
      name: '天全二郎山',
      x: 102.8765,
      y: 30.1234,
      type: '自然',
      intro: '二郎山是雅安重要的自然景观，山势雄伟，植被茂密。这里既有险峻的山峰，也有秀美的山谷，是拍摄自然风光和探险题材的理想地点。',
      imageUrl: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=800&q=60',
      openTime: '07:00-18:00',
      tags: ['山峰', '自然', '探险']
    }
  ],

  // 故事数据
  stories: [
    {
      storyId: 1001,
      title: '在雅安古城墙下的拍摄回忆',
      content: '去年夏天，我们剧组在雅安古城墙拍摄一部古装剧。清晨的阳光洒在古老的城墙上，那种历史的厚重感让人震撼。当地的工作人员非常配合，为我们提供了很多便利。拍摄过程中，我们还了解到了城墙背后的历史故事，这让我们的作品更加有深度。',
      publishTime: '2024-11-15T10:30:00Z',
      alumniId: 2001,
      poiId: 'poi_001',
      imageUrl: 'https://images.unsplash.com/photo-1545239351-1141bd82e8a6?auto=format&fit=crop&w=800&q=60',
      emotionTags: [
        { tagId: 3001, tagName: '感动' },
        { tagId: 3002, tagName: '历史' }
      ]
    },
    {
      storyId: 1002,
      title: '碧峰峡的绝美风光',
      content: '碧峰峡的风景真的太美了！我们在这里拍摄了一部自然纪录片，峡谷中的瀑布、原始森林，每一帧都是大片。特别是清晨的雾气，给整个场景增添了神秘感。这里的生态环境保护得非常好，让我们感受到了大自然的魅力。',
      publishTime: '2024-10-20T14:20:00Z',
      alumniId: 2002,
      poiId: 'poi_002',
      imageUrl: 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=800&q=60',
      emotionTags: [
        { tagId: 3003, tagName: '震撼' },
        { tagId: 3004, tagName: '自然' }
      ]
    },
    {
      storyId: 1003,
      title: '上里古镇的岁月静好',
      content: '上里古镇就像一幅活的历史画卷。我们在这里拍摄了一部年代剧，古镇的每一砖一瓦都充满了故事。当地居民非常热情，还给我们介绍了许多古镇的历史。拍摄期间，我们仿佛穿越回了那个年代，这种沉浸式的体验让我们的作品更加真实。',
      publishTime: '2024-09-25T16:45:00Z',
      alumniId: 2001,
      poiId: 'poi_003',
      imageUrl: 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=60',
      emotionTags: [
        { tagId: 3001, tagName: '感动' },
        { tagId: 3005, tagName: '怀旧' }
      ]
    },
    {
      storyId: 1004,
      title: '蒙顶山茶园的清新之旅',
      content: '在蒙顶山茶园拍摄茶文化纪录片是一次难忘的经历。清晨的茶园被薄雾笼罩，采茶工人在梯田间忙碌，构成了一幅美丽的田园画卷。我们不仅拍摄到了美丽的画面，还深入了解了中国茶文化的博大精深。',
      publishTime: '2024-08-10T09:15:00Z',
      alumniId: 2002,
      poiId: 'poi_004',
      imageUrl: 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?auto=format&fit=crop&w=800&q=60',
      emotionTags: [
        { tagId: 3004, tagName: '自然' },
        { tagId: 3006, tagName: '文化' }
      ]
    }
  ],

  // 路线数据
  routes: [
    {
      routeId: 4001,
      routeName: '雅安文化之旅',
      routeDesc: '适合拍摄历史文化题材，包含古城墙、上里古镇等文化景点，感受雅安深厚的历史底蕴。',
      duration: 120,
      suitableIdentity: '剧组',
      poisCount: 3,
      pois: [
        { poiId: 'poi_001', name: '雅安古城墙', order: 1 },
        { poiId: 'poi_003', name: '上里古镇', order: 2 },
        { poiId: 'poi_007', name: '雅安博物馆', order: 3 }
      ]
    },
    {
      routeId: 4002,
      routeName: '自然风光路线',
      routeDesc: '适合拍摄自然风光和生态题材，包含碧峰峡、蒙顶山茶园等自然景观，展现雅安的生态之美。',
      duration: 180,
      suitableIdentity: '制作人',
      poisCount: 3,
      pois: [
        { poiId: 'poi_002', name: '碧峰峡风景区', order: 1 },
        { poiId: 'poi_004', name: '蒙顶山茶园', order: 2 },
        { poiId: 'poi_006', name: '青衣江畔', order: 3 }
      ]
    },
    {
      routeId: 4003,
      routeName: '综合拍摄路线',
      routeDesc: '适合多种题材拍摄，包含文化、自然、现代建筑等多种类型的取景点，满足不同拍摄需求。',
      duration: 240,
      suitableIdentity: '其他',
      poisCount: 4,
      pois: [
        { poiId: 'poi_001', name: '雅安古城墙', order: 1 },
        { poiId: 'poi_002', name: '碧峰峡风景区', order: 2 },
        { poiId: 'poi_005', name: '雅安熊猫基地', order: 3 },
        { poiId: 'poi_007', name: '雅安博物馆', order: 4 }
      ]
    }
  ],

  // 收藏数据
  collections: [] as Collection[],

  // 浏览记录
  browseHistory: [] as BrowseRecord[],

  // 标签数据
  tags: [
    { tagId: 3001, tagName: '感动' },
    { tagId: 3002, tagName: '历史' },
    { tagId: 3003, tagName: '震撼' },
    { tagId: 3004, tagName: '自然' },
    { tagId: 3005, tagName: '怀旧' },
    { tagId: 3006, tagName: '文化' }
  ],

  // 场地数据
  locations: [
    {
      id: 1,
      name: '雅安影视拍摄基地',
      type: '影视基地',
      status: 1,
      locationDescription: '专业的影视拍摄基地，配备完善的拍摄设施和后勤保障，可提供室内外多种场景。',
      contactPhone: '0835-1234567',
      contactName: '张经理',
      address: '雅安市雨城区影视路1号',
      price: 5000,
      updatedAt: '2024-12-01T10:00:00Z'
    },
    {
      id: 2,
      name: '上里古镇拍摄场地',
      type: '古镇',
      status: 1,
      locationDescription: '明清古建筑群，适合拍摄古装剧和年代剧，可提供封控服务。',
      contactPhone: '0835-2345678',
      contactName: '李主任',
      address: '雅安市雨城区上里镇',
      price: 3000,
      updatedAt: '2024-11-15T14:30:00Z'
    },
    {
      id: 3,
      name: '碧峰峡自然拍摄区',
      type: '自然景区',
      status: 1,
      locationDescription: '自然风光优美，适合拍摄自然纪录片和风光片，需提前预约。',
      contactPhone: '0835-3456789',
      contactName: '王主管',
      address: '雅安市雨城区碧峰峡镇',
      price: 2000,
      updatedAt: '2024-12-10T09:20:00Z'
    }
  ],

  // 协拍服务数据
  shootServices: [
    {
      id: 1,
      name: '雅安影视器材租赁',
      description: '提供专业摄影器材、灯光设备、录音设备等全套影视拍摄器材租赁服务。',
      price: 800,
      status: 1,
      address: '雅安市雨城区器材路88号',
      phone: '0835-4567890',
      contactName: '赵经理',
      updatedAt: '2024-12-05T11:00:00Z'
    },
    {
      id: 2,
      name: '剧组住宿服务',
      description: '提供剧组人员住宿服务，距离主要拍摄地点近，交通便利，价格优惠。',
      price: 150,
      status: 1,
      address: '雅安市雨城区住宿街66号',
      phone: '0835-5678901',
      contactName: '钱主管',
      updatedAt: '2024-11-20T15:30:00Z'
    },
    {
      id: 3,
      name: '影视车辆租赁',
      description: '提供拍摄用车、演员接送车辆、器材运输车辆等各类车辆租赁服务。',
      price: 500,
      status: 1,
      address: '雅安市雨城区车辆路99号',
      phone: '0835-6789012',
      contactName: '孙经理',
      updatedAt: '2024-12-08T10:15:00Z'
    }
  ],

  // 文章数据
  articles: [
    {
      id: 1,
      title: '雅安市影视拍摄服务政策解读',
      issueUnit: '雅安市影视服务中心',
      issueTime: '2024-12-15T10:00:00Z',
      content: '为促进雅安市影视产业发展，我市出台了多项优惠政策支持影视拍摄。包括场地使用费减免、拍摄协调服务、政策咨询等。欢迎各影视制作单位来雅安取景拍摄。',
      coverUrl: 'https://images.unsplash.com/photo-1529158062015-cad636e69505?auto=format&fit=crop&w=800&q=60'
    },
    {
      id: 2,
      title: '2024年度优秀影视作品展映活动',
      issueUnit: '雅安市文化广电旅游局',
      issueTime: '2024-12-10T14:30:00Z',
      content: '为展示雅安影视产业发展成果，我市将举办2024年度优秀影视作品展映活动。活动将展映在雅安取景拍摄的优秀影视作品，并邀请主创团队分享拍摄经验。',
      coverUrl: 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=800&q=60'
    },
    {
      id: 3,
      title: '雅安取景地推荐：上里古镇',
      issueUnit: '雅安市影视服务中心',
      issueTime: '2024-12-05T09:20:00Z',
      content: '上里古镇是雅安市重要的影视取景地，以其保存完好的明清古建筑群而闻名。古镇依山傍水，古色古香，适合拍摄古装剧、年代剧等多种题材。',
      coverUrl: 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=60'
    },
    {
      id: 4,
      title: '影视拍摄报备流程优化通知',
      issueUnit: '雅安市影视服务中心',
      issueTime: '2024-11-28T16:00:00Z',
      content: '为进一步优化影视拍摄服务，我市简化了拍摄报备流程。现在可以通过线上平台提交报备材料，工作人员将在1个工作日内完成审核并联系确认。',
      coverUrl: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=60'
    }
  ],

  // 校友数据
  alumni: [
    {
      alumniId: 2001,
      name: '张导演',
      gradYear: '2010',
      major: '广播电视编导'
    },
    {
      alumniId: 2002,
      name: '李制片',
      gradYear: '2012',
      major: '影视制作'
    }
  ],

  // 报备记录
  reports: [] as Report[]
}

// 工具函数：模拟成功响应
const successResponse = <T>(data: T, message = '操作成功') => ({
  code: 200,
  message,
  data
})

// 工具函数：模拟错误响应
const errorResponse = (message = '操作失败', code = 400) => ({
  code,
  message,
  data: null
})

// Mock API 函数
export const mockApi = {
  // 游客登录
  visitorLogin: async (params: { phone: string; password: string }) => {
    await delay(800)
    const visitor = mockData.visitors.find(
      v => v.phone === params.phone && v.password === params.password
    )
    if (visitor) {
      return successResponse({
        ...visitor,
        token: `mock_token_${visitor.visitorId}_${Date.now()}`
      }, '登录成功')
    }
    throw new Error('手机号或密码错误')
  },

  // 游客注册
  visitorRegister: async (params: { phone: string; password: string; name: string; identity: string }) => {
    await delay(800)
    const exists = mockData.visitors.find(v => v.phone === params.phone)
    if (exists) {
      throw new Error('该手机号已注册')
    }
    const newVisitor = {
      visitorId: `visitor_${Date.now()}`,
      ...params,
      registerTime: new Date().toISOString(),
      lastLoginTime: new Date().toISOString(),
      avatar: '',
      token: `mock_token_visitor_${Date.now()}`
    }
    mockData.visitors.push(newVisitor)
    return successResponse(newVisitor, '注册成功')
  },

  // 获取游客信息
  getVisitorInfo: async () => {
    await delay(500)
    const token = uni.getStorageSync('token')
    if (!token) {
      throw new Error('未登录')
    }
    const visitorId = token.split('_')[2] || 'visitor_001'
    const visitor = mockData.visitors.find(v => v.visitorId === visitorId) || mockData.visitors[0]
    return successResponse(visitor)
  },

  // 更新游客信息
  updateVisitorInfo: async (params: any) => {
    await delay(600)
    const token = uni.getStorageSync('token')
    if (!token) {
      throw new Error('未登录')
    }
    const visitorId = token.split('_')[2] || 'visitor_001'
    const visitor = mockData.visitors.find(v => v.visitorId === visitorId) || mockData.visitors[0]
    Object.assign(visitor, params)
    return successResponse(visitor, '更新成功')
  },

  // 游客退出
  visitorLogout: async () => {
    await delay(300)
    return successResponse('退出成功', '退出成功')
  },

  // 获取POI列表
  getPoiList: async (params?: { type?: string; keyword?: string }) => {
    await delay(600)
    let result = [...mockData.pois]
    
    if (params?.type) {
      result = result.filter(p => p.type === params.type)
    }
    
    if (params?.keyword) {
      const keyword = params.keyword.toLowerCase()
      result = result.filter(p => 
        p.name.toLowerCase().includes(keyword) || 
        p.intro.toLowerCase().includes(keyword)
      )
    }
    
    return successResponse(result, '查询成功')
  },

  // 获取POI详情
  getPoiDetail: async (poiId: string) => {
    await delay(500)
    const poi = mockData.pois.find(p => p.poiId === poiId)
    if (!poi) {
      throw new Error('POI不存在')
    }
    
    // 获取该POI相关的故事
    const stories = mockData.stories.filter(s => s.poiId === poiId)
    
    return successResponse({
      ...poi,
      stories
    }, '查询成功')
  },

  // 获取故事列表
  getStoryList: async (params?: { poiId?: string; keyword?: string }) => {
    await delay(600)
    let result = [...mockData.stories]
    
    if (params?.poiId) {
      result = result.filter(s => s.poiId === params.poiId)
    }
    
    if (params?.keyword) {
      const keyword = params.keyword.toLowerCase()
      result = result.filter(s => 
        s.title.toLowerCase().includes(keyword) || 
        s.content.toLowerCase().includes(keyword)
      )
    }
    
    return successResponse(result, '查询成功')
  },

  // 获取故事详情
  getStoryDetail: async (storyId: number) => {
    await delay(500)
    const story = mockData.stories.find(s => s.storyId === storyId)
    if (!story) {
      throw new Error('故事不存在')
    }
    return successResponse(story, '查询成功')
  },

  // 获取路线列表
  getRouteList: async (params?: { identity?: string; duration?: number }) => {
    await delay(600)
    let result = [...mockData.routes]
    
    if (params?.identity) {
      result = result.filter(r => r.suitableIdentity === params.identity)
    }
    
    if (typeof params?.duration === 'number') {
      result = result.filter(r => r.duration <= params.duration!)
    }
    
    return successResponse(result, '查询成功')
  },

  // 获取路线详情
  getRouteDetail: async (routeId: number) => {
    await delay(500)
    const route = mockData.routes.find(r => r.routeId === routeId)
    if (!route) {
      throw new Error('路线不存在')
    }
    return successResponse(route, '查询成功')
  },

  // 生成路线
  generateRoute: async (params: { identity: string; duration: number }) => {
    await delay(800)
    const route = mockData.routes.find(
      r => r.suitableIdentity === params.identity && r.duration <= params.duration
    )
    if (route) {
      return successResponse(route, '生成成功')
    }
    // 如果没有匹配的，返回默认路线
    const defaultRoute = { ...mockData.routes[0] }
    return successResponse(defaultRoute, '生成成功')
  },

  // 获取场地列表
  getLocations: async () => {
    await delay(600)
    return successResponse(mockData.locations, '查询成功')
  },

  // 获取协拍服务列表
  getShootServices: async () => {
    await delay(600)
    return successResponse(mockData.shootServices, '查询成功')
  },

  // 获取文章列表
  getArticles: async () => {
    await delay(600)
    return successResponse(mockData.articles, '查询成功')
  },

  // 提交报备
  submitReport: async (payload: any) => {
    await delay(1000)
    const report = {
      id: Date.now(),
      ...payload,
      submitTime: new Date().toISOString(),
      status: '待审核'
    }
    mockData.reports.push(report)
    return successResponse(true, '提交成功，工作人员将在1个工作日内联系您')
  },

  // 上传文件
  uploadReportFile: async (filePath: string) => {
    await delay(1200)
    return successResponse({
      fileId: `file_${Date.now()}`,
      url: `https://mock-upload.com/files/${Date.now()}.jpg`
    }, '上传成功')
  },

  // 添加收藏
  addCollect: async (params: { poiId: string; visitorId: string }) => {
    await delay(500)
    const exists = mockData.collections.find(
      c => c.poiId === params.poiId && c.visitorId === params.visitorId
    )
    if (exists) {
      throw new Error('已收藏')
    }
    const poi = mockData.pois.find(p => p.poiId === params.poiId)
    if (!poi) {
      throw new Error('POI不存在')
    }
    mockData.collections.push({
      id: Date.now(),
      poiId: params.poiId,
      visitorId: params.visitorId,
      name: poi.name,
      collectTime: new Date().toISOString()
    })
    return successResponse(true, '收藏成功')
  },

  // 获取收藏列表
  getCollectList: async (visitorId: string) => {
    await delay(500)
    const result = mockData.collections.filter(c => c.visitorId === visitorId)
    return successResponse(result, '查询成功')
  },

  // 添加浏览记录
  addBrowse: async (params: { poiId: string; visitorId: string }) => {
    await delay(300)
    const poi = mockData.pois.find(p => p.poiId === params.poiId)
    if (!poi) {
      throw new Error('POI不存在')
    }
    // 移除重复记录
    mockData.browseHistory = mockData.browseHistory.filter(
      b => !(b.poiId === params.poiId && b.visitorId === params.visitorId)
    )
    mockData.browseHistory.unshift({
      id: Date.now(),
      poiId: params.poiId,
      visitorId: params.visitorId,
      name: poi.name,
      browseTime: new Date().toISOString()
    })
    return successResponse(true, '记录成功')
  },

  // 获取浏览记录列表
  getBrowseList: async (visitorId: string) => {
    await delay(500)
    const result = mockData.browseHistory.filter(b => b.visitorId === visitorId)
    return successResponse(result, '查询成功')
  },

  // 获取标签列表
  getTagList: async () => {
    await delay(400)
    return successResponse(mockData.tags, '查询成功')
  },

  // 修改密码
  changePassword: async (oldPassword: string, newPassword: string) => {
    await delay(600)
    const token = uni.getStorageSync('token')
    if (!token) {
      throw new Error('未登录')
    }
    const visitorId = token.split('_')[2] || 'visitor_001'
    const visitor = mockData.visitors.find(v => v.visitorId === visitorId) || mockData.visitors[0]
    if (visitor.password !== oldPassword) {
      throw new Error('原密码错误')
    }
    visitor.password = newPassword
    return successResponse('修改成功', '密码修改成功')
  },

  // 提交反馈
  submitFeedback: async (params: { type: string; content: string }) => {
    await delay(600)
    return successResponse(true, '反馈提交成功，感谢您的建议')
  },

  // 获取校友列表
  getAlumniList: async (params?: { keyword?: string }) => {
    await delay(500)
    let result = [...mockData.alumni]
    
    if (params?.keyword) {
      const keyword = params.keyword.toLowerCase()
      result = result.filter(a => 
        a.name.toLowerCase().includes(keyword) ||
        a.major?.toLowerCase().includes(keyword)
      )
    }
    
    return successResponse(result, '查询成功')
  }
}

export default mockApi

