// Mock 数据
export const mockData = {
  // 校友列表
  alumniList: [
    {
      alumniId: '2001',
      name: '张三',
      gradYear: '2010',
      major: '计算机科学与技术',
      career: '软件工程师',
      contact: '13800138000',
      avatarUrl: ''
    },
    {
      alumniId: '2002',
      name: '李四',
      gradYear: '2012',
      major: '电子信息工程',
      career: '硬件工程师',
      contact: '13900139000',
      avatarUrl: ''
    }
  ],
  
  // POI 列表
  poiList: [
    {
      poiId: '1',
      name: '图书馆',
      type: '建筑',
      x: 116.397128,
      y: 39.916527,
      intro: '学校主图书馆，藏书丰富',
      openTime: '08:00-22:00',
      imageUrl: ''
    },
    {
      poiId: '2',
      name: '中心花园',
      type: '景观',
      x: 116.397200,
      y: 39.916600,
      intro: '校园中心景观区',
      openTime: '全天开放',
      imageUrl: ''
    }
  ],
  
  // 故事列表
  storyList: [
    {
      storyId: '1001',
      title: '在图书馆的奋斗时光',
      content: '回忆在图书馆学习的日子...',
      publishTime: '2025-10-01',
      status: '待审核',
      alumni: {
        alumniId: '2001',
        name: '张三'
      },
      poi: {
        poiId: '1',
        name: '图书馆'
      }
    },
    {
      storyId: '1002',
      title: '校园美景',
      content: '校园中心花园的美景...',
      publishTime: '2025-10-02',
      status: '已发布',
      alumni: {
        alumniId: '2002',
        name: '李四'
      },
      poi: {
        poiId: '2',
        name: '中心花园'
      }
    }
  ],
  
  // 标签列表
  tagList: [
    {
      tagId: '3001',
      tagName: '励志',
      tagDesc: '体现奋斗、拼搏的情感'
    },
    {
      tagId: '3002',
      tagName: '温馨',
      tagDesc: '体现温暖、和谐的情感'
    }
  ],
  
  // 路线列表
  routeList: [
    {
      routeId: '4001',
      routeName: '新生参观路线',
      routeDesc: '适合新生了解校园',
      duration: 60,
      suitableIdentity: '新生',
      pois: [
        { poiId: '1', name: '图书馆' },
        { poiId: '2', name: '中心花园' }
      ]
    }
  ],

  // 旅游线路列表
  tourRouteList: [
    {
      id: 1,
      name: '熊猫家园探秘游',
      description: '以在雅安拍摄的熊猫主题影视作品为线索，串联碧峰峡熊猫基地、宝兴蜂桶寨邓池沟等景区景点。',
      theme: '熊猫文化',
      features: '熊猫文化深度游+熊猫文创市集',
      transportInfo: '自驾或包车，全程约2-3天',
      accommodation: '碧峰峡景区周边民宿、五星级酒店',
      foodRecommendation: '雅安特色美食：雨城茶、野生菌、竹笋等',
      routeMap: 'https://example.com/route1.jpg',
      status: 1,
      createdAt: '2024-01-15 10:00:00'
    },
    {
      id: 2,
      name: '世界茶源寻根游',
      description: '结合以茶园为背景的影视作品，涵盖蒙顶山茶园、牛碾坪、大地指纹等景点。',
      theme: '茶文化',
      features: '茶文化体验+主题民宿+茶艺表演',
      transportInfo: '自驾或包车，全程约2天',
      accommodation: '蒙顶山茶园周边特色民宿',
      foodRecommendation: '蒙顶山茶、茶叶蛋、茶香鸡等',
      routeMap: 'https://example.com/route2.jpg',
      status: 1,
      createdAt: '2024-01-16 10:00:00'
    },
    {
      id: 3,
      name: '红色文化体验游',
      description: '围绕红色题材影视作品在雅安的拍摄地，打造包含红军长征翻越夹金山纪念馆等景点的主题线路。',
      theme: '红色文化',
      features: '红色记忆追溯+对话人文山川',
      transportInfo: '自驾或包车，全程约2-3天',
      accommodation: '宝兴县城周边酒店',
      foodRecommendation: '红军餐、山野菜、高山蔬菜等',
      routeMap: 'https://example.com/route3.jpg',
      status: 1,
      createdAt: '2024-01-17 10:00:00'
    },
    {
      id: 4,
      name: '川西古镇风情游',
      description: '循着古镇题材影视作品的拍摄轨迹，将上里古镇、望鱼古镇等串联起来。',
      theme: '古镇文化',
      features: '古镇休闲+民俗体验',
      transportInfo: '自驾或包车，全程约2天',
      accommodation: '古镇内特色客栈',
      foodRecommendation: '古镇特色小吃、手工豆制品等',
      routeMap: 'https://example.com/route4.jpg',
      status: 1,
      createdAt: '2024-01-18 10:00:00'
    },
    {
      id: 5,
      name: '峡谷秘境探险游',
      description: '依据在峡谷取景的影视作品，串联大渡河峡谷、二郎山喇叭河等景点。',
      theme: '峡谷探险',
      features: '原生态美景+峡谷探秘',
      transportInfo: '自驾或包车，全程约2-3天',
      accommodation: '峡谷周边度假村',
      foodRecommendation: '河鱼、野生菌、山野菜等',
      routeMap: 'https://example.com/route5.jpg',
      status: 1,
      createdAt: '2024-01-19 10:00:00'
    }
  ],

  // 政策列表
  policyList: [
    {
      id: 1,
      title: '四川省影视产业发展扶持政策',
      type: '省级',
      issueUnit: '四川省文化和旅游厅',
      issueDate: '2024-01-10',
      content: '为促进四川省影视产业发展，提升文化软实力，特制定本政策。对在四川拍摄的影视作品给予资金扶持，最高补助500万元。',
      status: 1,
      createdAt: '2024-01-10 10:00:00'
    },
    {
      id: 2,
      title: '雅安市影视产业发展规划（2024-2030）',
      type: '市级',
      issueUnit: '雅安市文化和旅游局',
      issueDate: '2024-01-15',
      content: '将雅安打造成为西南地区重要的影视拍摄基地和影视产业集聚区。对来雅拍摄的剧组提供场地优惠、税收优惠等政策支持。',
      status: 1,
      createdAt: '2024-01-15 10:00:00'
    },
    {
      id: 3,
      title: '雅安市影视拍摄协拍服务补助办法',
      type: '市级',
      issueUnit: '雅安市文化和旅游局',
      issueDate: '2024-02-01',
      content: '对为影视剧组提供协拍服务的企业和个人给予补助。补助标准：场地服务补助最高50万元，其他服务补助最高30万元。',
      status: 1,
      createdAt: '2024-02-01 10:00:00'
    },
    {
      id: 4,
      title: '雅安市影视文旅融合发展实施方案',
      type: '市级',
      issueUnit: '雅安市文化和旅游局',
      issueDate: '2024-02-15',
      content: '推动影视产业与文旅产业融合发展。支持开发影视主题旅游线路，建设影视文化体验基地，打造影视文旅品牌。',
      status: 1,
      createdAt: '2024-02-15 10:00:00'
    }
  ]
}

// 模拟 API 延迟
const mockDelay = (ms = 500) => {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// Mock API 函数
export const mockApi = {
  // 校友相关
  getAlumniList: async (params) => {
    await mockDelay()
    let data = [...mockData.alumniList]
    
    if (params.keyword) {
      data = data.filter(item => item.name.includes(params.keyword))
    }
    if (params.gradYear) {
      data = data.filter(item => item.gradYear === params.gradYear)
    }
    
    return {
      code: 200,
      message: '查询成功',
      data
    }
  },
  
  addAlumni: async (data) => {
    await mockDelay()
    const newAlumni = {
      alumniId: String(Date.now()),
      ...data
    }
    mockData.alumniList.push(newAlumni)
    return {
      code: 200,
      message: '添加成功',
      data: {
        alumniId: newAlumni.alumniId
      }
    }
  },
  
  // POI 相关
  getPOIList: async () => {
    await mockDelay()
    return {
      code: 200,
      message: '查询成功',
      data: mockData.poiList
    }
  },
  
  addPOI: async (data) => {
    await mockDelay()
    const newPOI = {
      poiId: String(Date.now()),
      ...data
    }
    mockData.poiList.push(newPOI)
    return {
      code: 200,
      message: '添加成功',
      data: {
        poiId: newPOI.poiId
      }
    }
  },
  
  updatePOI: async (data) => {
    await mockDelay()
    const index = mockData.poiList.findIndex(item => item.poiId === data.poiId)
    if (index !== -1) {
      mockData.poiList[index] = { ...mockData.poiList[index], ...data }
    }
    return {
      code: 200,
      message: '更新成功',
      data: null
    }
  },
  
  // 故事相关
  getStoryList: async (params) => {
    await mockDelay()
    let data = [...mockData.storyList]
    
    if (params.status) {
      data = data.filter(item => item.status === params.status)
    }
    
    return {
      code: 200,
      message: '查询成功',
      data
    }
  },
  
  auditStory: async (data) => {
    await mockDelay()
    const story = mockData.storyList.find(item => item.storyId === data.storyId)
    if (story) {
      story.status = data.status
    }
    return {
      code: 200,
      message: '审核成功',
      data: null
    }
  },
  
  // 标签相关
  getTagList: async () => {
    await mockDelay()
    return {
      code: 200,
      message: '查询成功',
      data: mockData.tagList
    }
  },
  
  addTag: async (data) => {
    await mockDelay()
    const newTag = {
      tagId: String(Date.now()),
      ...data
    }
    mockData.tagList.push(newTag)
    return {
      code: 200,
      message: '添加成功',
      data: {
        tagId: newTag.tagId
      }
    }
  },
  
  // 路线相关
  getRouteList: async () => {
    await mockDelay()
    return {
      code: 200,
      message: '查询成功',
      data: mockData.routeList
    }
  },
  
  addRoute: async (data) => {
    await mockDelay()
    const newRoute = {
      routeId: String(Date.now()),
      ...data
    }
    mockData.routeList.push(newRoute)
    return {
      code: 200,
      message: '添加成功',
      data: {
        routeId: newRoute.routeId
      }
    }
  },
  
  updateRoute: async (data) => {
    await mockDelay()
    const index = mockData.routeList.findIndex(item => item.routeId === data.routeId)
    if (index !== -1) {
      mockData.routeList[index] = { ...mockData.routeList[index], ...data }
    }
    return {
      code: 200,
      message: '更新成功',
      data: null
    }
  },
  
  // 文件上传
  uploadFile: async (file) => {
    await mockDelay(1000)
    // 生成 mock URL
    const url = `https://mock-image.com/${file.name}`
    return {
      code: 200,
      message: '上传成功',
      data: {
        url
      }
    }
  },

  // 旅游线路相关
  getTourRoutePage: async (page, pageSize, keyword) => {
    await mockDelay()
    let data = [...mockData.tourRouteList]

    if (keyword) {
      data = data.filter(item => item.name.includes(keyword))
    }

    const totalItems = data.length
    const start = (page - 1) * pageSize
    const end = start + pageSize
    const pageData = data.slice(start, end)

    return {
      code: 200,
      message: '查询成功',
      data: pageData,
      pagination: {
        currentPage: page,
        pageSize: pageSize,
        totalItems: totalItems,
        totalPages: Math.ceil(totalItems / pageSize)
      }
    }
  },

  getTourRouteById: async (id) => {
    await mockDelay()
    const route = mockData.tourRouteList.find(item => item.id === id)
    return {
      code: 200,
      message: '查询成功',
      data: route || null
    }
  },

  addTourRoute: async (data) => {
    await mockDelay()
    const newRoute = {
      id: Math.max(...mockData.tourRouteList.map(r => r.id), 0) + 1,
      ...data,
      createdAt: new Date().toLocaleString()
    }
    mockData.tourRouteList.push(newRoute)
    return {
      code: 200,
      message: '添加成功',
      data: { id: newRoute.id }
    }
  },

  updateTourRoute: async (data) => {
    await mockDelay()
    const index = mockData.tourRouteList.findIndex(item => item.id === data.id)
    if (index !== -1) {
      mockData.tourRouteList[index] = { ...mockData.tourRouteList[index], ...data }
    }
    return {
      code: 200,
      message: '更新成功',
      data: null
    }
  },

  deleteTourRoute: async (id) => {
    await mockDelay()
    const index = mockData.tourRouteList.findIndex(item => item.id === id)
    if (index !== -1) {
      mockData.tourRouteList.splice(index, 1)
    }
    return {
      code: 200,
      message: '删除成功',
      data: null
    }
  },

  // 政策相关
  getPolicyPage: async (page, pageSize, keyword, type) => {
    await mockDelay()
    let data = [...mockData.policyList]

    if (keyword) {
      data = data.filter(item => item.title.includes(keyword))
    }
    if (type) {
      data = data.filter(item => item.type === type)
    }

    const totalItems = data.length
    const start = (page - 1) * pageSize
    const end = start + pageSize
    const pageData = data.slice(start, end)

    return {
      code: 200,
      message: '查询成功',
      data: pageData,
      pagination: {
        currentPage: page,
        pageSize: pageSize,
        totalItems: totalItems,
        totalPages: Math.ceil(totalItems / pageSize)
      }
    }
  },

  getPolicyById: async (id) => {
    await mockDelay()
    const policy = mockData.policyList.find(item => item.id === id)
    return {
      code: 200,
      message: '查询成功',
      data: policy || null
    }
  },

  addPolicy: async (data) => {
    await mockDelay()
    const newPolicy = {
      id: Math.max(...mockData.policyList.map(p => p.id), 0) + 1,
      ...data,
      createdAt: new Date().toLocaleString()
    }
    mockData.policyList.push(newPolicy)
    return {
      code: 200,
      message: '添加成功',
      data: { id: newPolicy.id }
    }
  },

  updatePolicy: async (data) => {
    await mockDelay()
    const index = mockData.policyList.findIndex(item => item.id === data.id)
    if (index !== -1) {
      mockData.policyList[index] = { ...mockData.policyList[index], ...data }
    }
    return {
      code: 200,
      message: '更新成功',
      data: null
    }
  },

  deletePolicy: async (id) => {
    await mockDelay()
    const index = mockData.policyList.findIndex(item => item.id === id)
    if (index !== -1) {
      mockData.policyList.splice(index, 1)
    }
    return {
      code: 200,
      message: '删除成功',
      data: null
    }
  }
}

