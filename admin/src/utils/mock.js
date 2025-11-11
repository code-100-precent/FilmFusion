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
  }
}

