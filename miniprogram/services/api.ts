/**
 * API 服务 - 对接后端真实接口
 */
import { http } from '../utils/http'

// API基础路径（已在http.ts中配置，这里只用于文档说明）
// const API_BASE = 'http://localhost:8080/api'

// 类型定义
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface VisitorLoginParams {
  phone: string
  password: string
}

interface VisitorRegisterParams {
  phone: string
  password: string
  name: string
  identity: string
}

interface VisitorVO {
  visitorId: string
  name: string
  phone: string
  identity: string
  registerTime?: string
  lastLoginTime?: string
  token?: string
}

interface POI {
  poiId: string
  name: string
  x: number
  y: number
  type: string
  intro: string
  imageUrl?: string
  openTime?: string
}

interface Story {
  storyId: number
  title: string
  content: string
  publishTime?: string
  alumniId?: number
  poiId?: string
  imageUrl?: string
}

interface Route {
  routeId: number
  routeName: string
  routeDesc?: string
  duration?: number
  suitableIdentity?: string
}

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

interface PageRequest {
  page?: number
  size?: number
  keyword?: string
  sortBy?: string
  sortOrder?: string
}

// 获取token
const getToken = (): string => {
  return uni.getStorageSync('token') || ''
}

// 设置token
const setToken = (token: string): void => {
  uni.setStorageSync('token', token)
}

// 清除token
const clearToken = (): void => {
  uni.removeStorageSync('token')
}

// 游客登录
export const visitorLogin = async (params: VisitorLoginParams): Promise<ApiResponse<VisitorVO>> => {
  try {
    console.log('visitorLogin 调用，参数:', { phone: params.phone, password: '***' })
    
    const res = await http<VisitorVO>({
      url: '/visitor/login',
      method: 'POST',
      data: params,
      header: {
        'Content-Type': 'application/json'
      }
    })
    
    console.log('visitorLogin 响应:', res)
    
    if (res.code === 200 && res.data && res.data.token) {
      setToken(res.data.token)
      // 保存用户信息
      uni.setStorageSync('visitorInfo', res.data)
    }
    
    return res as ApiResponse<VisitorVO>
  } catch (error: any) {
    console.error('visitorLogin 错误:', error)
    throw new Error(error.message || '登录失败')
  }
}

// 游客注册
export const visitorRegister = async (params: VisitorRegisterParams): Promise<ApiResponse<VisitorVO>> => {
  try {
    const res = await http<VisitorVO>({
      url: '/visitor/register',
      method: 'POST',
      data: params,
      header: {
        'Content-Type': 'application/json'
      }
    })
    
    if (res.code === 200 && res.data && res.data.token) {
      setToken(res.data.token)
      // 保存用户信息
      uni.setStorageSync('visitorInfo', res.data)
    }
    
    return res as ApiResponse<VisitorVO>
  } catch (error: any) {
    throw new Error(error.message || '注册失败')
  }
}

// 获取当前游客信息
export const getVisitorInfo = async (): Promise<ApiResponse<VisitorVO>> => {
  const token = getToken()
  if (!token) {
    throw new Error('未登录')
  }
  
  return http<VisitorVO>({
    url: '/visitor/info',
    method: 'GET',
    header: {
      'Authorization': `Bearer ${token}`
    }
  }) as Promise<ApiResponse<VisitorVO>>
}

// 更新游客信息
export const updateVisitorInfo = async (params: Partial<VisitorVO>): Promise<ApiResponse<VisitorVO>> => {
  const token = getToken()
  if (!token) {
    throw new Error('未登录')
  }
  
  return http<VisitorVO>({
    url: '/visitor/update',
    method: 'PUT',
    data: params,
    header: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    }
  }) as Promise<ApiResponse<VisitorVO>>
}

// 游客退出登录
export const visitorLogout = async (): Promise<ApiResponse<string>> => {
  const token = getToken()
  if (!token) {
    clearToken()
    return Promise.resolve({ code: 200, message: '已退出', data: 'success' } as ApiResponse<string>)
  }
  
  try {
    const res = await http<string>({
      url: '/visitor/logout',
      method: 'POST',
      header: {
        'Authorization': `Bearer ${token}`
      }
    })
    clearToken()
    uni.removeStorageSync('visitorInfo')
    return res as ApiResponse<string>
  } catch (error) {
    clearToken()
    uni.removeStorageSync('visitorInfo')
    return Promise.resolve({ code: 200, message: '已退出', data: 'success' } as ApiResponse<string>)
  }
}

// POI列表查询
export const getPoiList = async (params?: { type?: string; keyword?: string }): Promise<ApiResponse<POI[]>> => {
  const queryParams: PageRequest = {
    page: 1,
    size: 100
  }
  
  if (params?.keyword) {
    queryParams.keyword = params.keyword
  }
  
  try {
    // 如果有筛选条件，使用分页接口
    if (params?.type || params?.keyword) {
      const res = await http<{ records: POI[] }>({
        url: '/ofinterest/page',
        method: 'POST',
        data: queryParams,
        header: {
          'Content-Type': 'application/json'
        }
      })
      
      let records = res.data?.records || []
      // 如果指定了type，再过滤一次
      if (params?.type) {
        records = records.filter((p: POI) => p.type === params.type)
      }
      
      return {
        code: res.code || 200,
        message: res.message || '查询成功',
        data: records
      } as ApiResponse<POI[]>
    } else {
      // 没有筛选条件，直接用列表接口
      const res = await http<POI[]>({
        url: '/ofinterest',
        method: 'GET'
      })
      
      return res as ApiResponse<POI[]>
    }
  } catch (error: any) {
    throw new Error(error.message || '查询POI列表失败')
  }
}

// POI详情查询
export const getPoiDetail = async (poiId: string): Promise<ApiResponse<POI & { stories?: Story[] }>> => {
  try {
    const res = await http<POI>({
      url: `/ofinterest/${poiId}`,
      method: 'GET'
    })
    
    // 获取该POI相关的故事
    try {
      const storyRes = await http<{ records: Story[] }>({
        url: '/story/page',
        method: 'POST',
        data: {
          page: 1,
          size: 10,
          keyword: poiId
        },
        header: {
          'Content-Type': 'application/json'
        }
      })
      
      const stories = storyRes.data?.records || []
      return {
        code: res.code || 200,
        message: res.message || '查询成功',
        data: {
          ...res.data,
          stories: stories.filter((s: Story) => s.poiId === poiId)
        }
      } as ApiResponse<POI & { stories?: Story[] }>
    } catch (error) {
      // 如果获取故事失败，只返回POI信息
      return res as ApiResponse<POI & { stories?: Story[] }>
    }
  } catch (error: any) {
    throw new Error(error.message || '查询POI详情失败')
  }
}

// 校友故事列表
export const getStoryList = async (params?: { poiId?: string; tagId?: number; keyword?: string }): Promise<ApiResponse<Story[]>> => {
  const queryParams: PageRequest = {
    page: 1,
    size: 100
  }
  
  if (params?.keyword) {
    queryParams.keyword = params.keyword
  }
  
  try {
    const res = await http<{ records: Story[] }>({
      url: '/story/page',
      method: 'POST',
      data: queryParams,
      header: {
        'Content-Type': 'application/json'
      }
    })
    
    let records = res.data?.records || []
    
    // 客户端过滤
    if (params?.poiId) {
      records = records.filter((s: Story) => s.poiId === params.poiId)
    }
    
    // tagId过滤需要在后端实现，这里先返回所有记录
    // 如果需要按tagId过滤，需要后端支持
    
    return {
      code: res.code || 200,
      message: res.message || '查询成功',
      data: records
    } as ApiResponse<Story[]>
  } catch (error: any) {
    throw new Error(error.message || '查询故事列表失败')
  }
}

// 获取故事详情
export const getStoryDetail = async (storyId: number): Promise<ApiResponse<Story>> => {
  try {
    const res = await http<Story>({
      url: `/story/${storyId}`,
      method: 'GET'
    })
    
    return res as ApiResponse<Story>
  } catch (error: any) {
    throw new Error(error.message || '查询故事详情失败')
  }
}

// 路线列表
export const getRouteList = async (params?: { identity?: string; duration?: number }): Promise<ApiResponse<Route[]>> => {
  try {
    const res = await http<Route[]>({
      url: '/route',
      method: 'GET'
    })
    
    let routes = res.data || []
    
    // 客户端过滤
    if (params?.identity) {
      routes = routes.filter((r: Route) => r.suitableIdentity === params.identity)
    }
    
    if (params?.duration !== undefined) {
      routes = routes.filter((r: Route) => (r.duration || 0) <= params.duration!)
    }
    
    return {
      code: res.code || 200,
      message: res.message || '查询成功',
      data: routes
    } as ApiResponse<Route[]>
  } catch (error: any) {
    throw new Error(error.message || '查询路线列表失败')
  }
}

// 获取路线详情
export const getRouteDetail = async (routeId: number): Promise<ApiResponse<Route>> => {
  try {
    const res = await http<Route>({
      url: `/route/${routeId}`,
      method: 'GET'
    })
    
    return res as ApiResponse<Route>
  } catch (error: any) {
    throw new Error(error.message || '查询路线详情失败')
  }
}

// 路线生成（根据身份和时长生成路线）
export const generateRoute = async (params: { identity: string; duration: number }): Promise<ApiResponse<Route>> => {
  try {
    const routes = await getRouteList({ identity: params.identity, duration: params.duration })
    
    if (routes.data && routes.data.length > 0) {
      return {
        code: 200,
        message: '生成成功',
        data: routes.data[0]
      } as ApiResponse<Route>
    }
    
    // 如果没有匹配的，返回第一条路线
    const allRoutes = await getRouteList()
    if (allRoutes.data && allRoutes.data.length > 0) {
      return {
        code: 200,
        message: '生成成功',
        data: allRoutes.data[0]
      } as ApiResponse<Route>
    }
    
    throw new Error('没有可用的路线')
  } catch (error: any) {
    throw new Error(error.message || '生成路线失败')
  }
}

// 高德地图路径规划（支持从起点到终点的路径规划）
export const getAmapRoute = async (points: Array<{ x?: number; y?: number; latitude?: number; longitude?: number }>): Promise<ApiResponse<{
  points: Array<{ latitude: number; longitude: number }>
  distance: number
  duration: number
}>> => {
  // 简化版路径规划，直接连接所有点
  if (!points || points.length < 2) {
    return Promise.resolve({
      code: 200,
      message: '路径规划成功',
      data: {
        points: points ? points.map(p => ({
          latitude: p.y || p.latitude || 0,
          longitude: p.x || p.longitude || 0
        })) : [],
        distance: 0,
        duration: 0
      }
    } as ApiResponse<{ points: Array<{ latitude: number; longitude: number }>, distance: number, duration: number }>)
  }
  
  // 简单的路径插值
  const routePoints: Array<{ latitude: number; longitude: number }> = []
  for (let i = 0; i < points.length - 1; i++) {
    const from = points[i]
    const to = points[i + 1]
    const steps = 10
    for (let j = 0; j <= steps; j++) {
      const ratio = j / steps
      const lat = (from.y || from.latitude || 0) + ((to.y || to.latitude || 0) - (from.y || from.latitude || 0)) * ratio
      const lng = (from.x || from.longitude || 0) + ((to.x || to.longitude || 0) - (from.x || from.longitude || 0)) * ratio
      routePoints.push({ latitude: lat, longitude: lng })
    }
  }
  
  // 计算总距离（简化版）
  let totalDistance = 0
  for (let i = 0; i < routePoints.length - 1; i++) {
    const lat1 = routePoints[i].latitude
    const lng1 = routePoints[i].longitude
    const lat2 = routePoints[i + 1].latitude
    const lng2 = routePoints[i + 1].longitude
    
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
  
  return Promise.resolve({
    code: 200,
    message: '路径规划成功',
    data: {
      points: routePoints,
      distance: Math.round(totalDistance),
      duration: Math.round(totalDistance / 80) // 假设步行速度80米/分钟
    }
  } as ApiResponse<{ points: Array<{ latitude: number; longitude: number }>, distance: number, duration: number }>)
}

// 添加收藏
export const addCollect = async (params: { poiId: string; visitorId: string }): Promise<ApiResponse<boolean>> => {
  const token = getToken()
  if (!token) {
    throw new Error('未登录')
  }
  
  try {
    // 先获取POI信息
    const poiRes = await getPoiDetail(params.poiId)
    const poi = poiRes.data
    
    const res = await http<boolean>({
      url: '/collection',
      method: 'POST',
      data: {
        poiId: params.poiId,
        visitorId: params.visitorId,
        name: poi?.name || ''
      },
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
    
    return res as ApiResponse<boolean>
  } catch (error: any) {
    throw new Error(error.message || '收藏失败')
  }
}

// 收藏列表
export const getCollectList = async (visitorId: string): Promise<ApiResponse<Collection[]>> => {
  const token = getToken()
  if (!token) {
    throw new Error('未登录')
  }
  
  try {
    const res = await http<{ records: Collection[] }>({
      url: '/collection/page',
      method: 'POST',
      data: {
        page: 1,
        size: 100,
        keyword: visitorId
      },
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
    
    // 过滤出该游客的收藏
    const records = res.data?.records || []
    const filtered = records.filter((c: Collection) => c.visitorId === visitorId)
    
    return {
      code: res.code || 200,
      message: res.message || '查询成功',
      data: filtered
    } as ApiResponse<Collection[]>
  } catch (error: any) {
    throw new Error(error.message || '查询收藏列表失败')
  }
}

// 添加浏览记录
export const addBrowse = async (params: { poiId: string; visitorId: string }): Promise<ApiResponse<boolean>> => {
  const token = getToken()
  if (!token) {
    throw new Error('未登录')
  }
  
  try {
    // 先获取POI信息
    const poiRes = await getPoiDetail(params.poiId)
    const poi = poiRes.data
    
    const res = await http<boolean>({
      url: '/browserecord',
      method: 'POST',
      data: {
        poiId: params.poiId,
        visitorId: params.visitorId,
        name: poi?.name || ''
      },
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
    
    return res as ApiResponse<boolean>
  } catch (error: any) {
    throw new Error(error.message || '记录浏览失败')
  }
}

// 浏览记录列表
export const getBrowseList = async (visitorId: string): Promise<ApiResponse<BrowseRecord[]>> => {
  const token = getToken()
  if (!token) {
    throw new Error('未登录')
  }
  
  try {
    const res = await http<{ records: BrowseRecord[] }>({
      url: '/browserecord/page',
      method: 'POST',
      data: {
        page: 1,
        size: 100,
        keyword: visitorId
      },
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
    
    // 过滤出该游客的浏览记录
    const records = res.data?.records || []
    const filtered = records.filter((b: BrowseRecord) => b.visitorId === visitorId)
    
    return {
      code: res.code || 200,
      message: res.message || '查询成功',
      data: filtered
    } as ApiResponse<BrowseRecord[]>
  } catch (error: any) {
    throw new Error(error.message || '查询浏览记录失败')
  }
}

// 获取标签列表
export const getTagList = async (): Promise<ApiResponse<Array<{ tagId: number; tagName: string }>>> => {
  try {
    const res = await http<Array<{ tagId: number; tagName: string }>>({
      url: '/tag',
      method: 'GET'
    })
    
    return res as ApiResponse<Array<{ tagId: number; tagName: string }>>
  } catch (error: any) {
    throw new Error(error.message || '查询标签列表失败')
  }
}

// 修改密码
export const changePassword = async (oldPassword: string, newPassword: string): Promise<ApiResponse<string>> => {
  const token = getToken()
  if (!token) {
    throw new Error('未登录')
  }
  
  try {
    // 先验证旧密码，然后更新密码
    const res = await http<string>({
      url: '/visitor/change-password',
      method: 'POST',
      data: {
        oldPassword,
        newPassword
      },
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
    
    return res as ApiResponse<string>
  } catch (error: any) {
    throw new Error(error.message || '修改密码失败')
  }
}

// 提交反馈
export const submitFeedback = async (params: { type: string; content: string }): Promise<ApiResponse<boolean>> => {
  const token = getToken()
  if (!token) {
    throw new Error('未登录')
  }
  
  try {
    const res = await http<boolean>({
      url: '/feedback/submit',
      method: 'POST',
      data: {
        type: params.type,
        content: params.content
      },
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
    
    return res as ApiResponse<boolean>
  } catch (error: any) {
    throw new Error(error.message || '提交反馈失败')
  }
}

// 获取校友列表
export const getAlumniList = async (params?: { keyword?: string }): Promise<ApiResponse<Array<{ alumniId: number; name: string; gradYear?: string; major?: string }>>> => {
  const queryParams: PageRequest = {
    page: 1,
    size: 100
  }
  
  if (params?.keyword) {
    queryParams.keyword = params.keyword
  }
  
  try {
    const res = await http<{ records: Array<{ alumniId: number; name: string; gradYear?: string; major?: string }> }>({
      url: '/alumni/page',
      method: 'POST',
      data: queryParams,
      header: {
        'Content-Type': 'application/json'
      }
    })
    
    return {
      code: res.code || 200,
      message: res.message || '查询成功',
      data: res.data?.records || []
    } as ApiResponse<Array<{ alumniId: number; name: string; gradYear?: string; major?: string }>>
  } catch (error: any) {
    throw new Error(error.message || '查询校友列表失败')
  }
}
