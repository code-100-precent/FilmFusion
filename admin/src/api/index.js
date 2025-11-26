import request from '@/utils/request'
import { mockApi } from '@/utils/mock'

// 是否启用 Mock（开发环境默认启用）
const USE_MOCK = false

// ==================== 管理员相关接口 ====================

/**
 * 管理员登录（使用用户登录接口，但需要 ADMIN 角色）
 */
export const adminLogin = (username, password) => {
  return request({
    url: '/user/login',
    method: 'post',
    data: {
      username,
      password
    }
  })
}

/**
 * 获取当前管理员信息
 */
export const getAdminInfo = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 管理员退出登录
 */
export const adminLogout = () => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

/**
 * 更新管理员信息
 */
export const updateAdminInfo = (data) => {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 */
export const changePassword = (oldPassword, newPassword) => {
  return request({
    url: '/user/password',
    method: 'put',
    data: {
      oldPassword,
      newPassword
    }
  })
}

/**
 * 上传头像
 */
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 文件上传（通用）
 */
export const uploadFile = (file, type = 'image') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  return request({
    url: '/common/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传头像
 */
export const uploadAvatarFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/common/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 游客管理
export const getVisitorPage = (pageRequest) => {
  return request({
    url: '/admin/visitor/page',
    method: 'post',
    data: pageRequest
  })
}

export const deleteVisitor = (id) => {
  return request({
    url: `/admin/visitor/${id}`,
    method: 'delete'
  })
}

export const getVisitorById = (id) => {
  return request({
    url: `/admin/visitor/${id}`,
    method: 'get'
  })
}

// 操作日志
export const getRecentOperationLogs = (limit = 10) => {
  return request({
    url: '/admin/operation-log/recent',
    method: 'get',
    params: { limit }
  })
}

export const getOperationLogPage = (pageRequest) => {
  return request({
    url: '/admin/operation-log/page',
    method: 'post',
    data: pageRequest
  })
}

// Mock 登录验证（实际项目中应该调用真实接口）
export const verifyToken = () => {
  const token = localStorage.getItem('token')
  if (token && token.startsWith('mock_admin_token_')) {
    // 返回 mock 数据
    return Promise.resolve({
      code: 200,
      message: '验证成功',
      data: {
        visitorId: 'admin',
        identity: '管理员'
      }
    })
  }
  return getAdminInfo()
}

// 校友管理
export const addAlumni = (data) => {
  return request({
    url: '/admin/alumni/add',
    method: 'post',
    data
  })
}

export const updateAlumni = (data) => {
  return request({
    url: '/admin/alumni/update',
    method: 'post',
    data
  })
}

export const deleteAlumni = (id) => {
  return request({
    url: `/admin/alumni/${id}`,
    method: 'delete'
  })
}

export const getAlumniById = (id) => {
  return request({
    url: `/admin/alumni/${id}`,
    method: 'get'
  })
}

export const getAlumniList = (params) => {
  return request({
    url: '/admin/alumni/list',
    method: 'get',
    params
  })
}

export const getAlumniPage = (pageRequest) => {
  return request({
    url: '/admin/alumni/page',
    method: 'post',
    data: pageRequest
  })
}

// POI 管理
export const addPOI = (data) => {
  return request({
    url: '/admin/poi/add',
    method: 'post',
    data
  })
}

export const updatePOI = (data) => {
  return request({
    url: '/admin/poi/update',
    method: 'post',
    data
  })
}

export const deletePOI = (id) => {
  return request({
    url: `/admin/poi/${id}`,
    method: 'delete'
  })
}

export const getPOIById = (id) => {
  return request({
    url: `/admin/poi/${id}`,
    method: 'get'
  })
}

export const getPOIList = () => {
  return request({
    url: '/admin/poi/list',
    method: 'get'
  })
}

export const getPOIPage = (pageRequest) => {
  return request({
    url: '/admin/poi/page',
    method: 'post',
    data: pageRequest
  })
}

// 校友故事管理
export const addStory = (data) => {
  return request({
    url: '/admin/story/add',
    method: 'post',
    data
  })
}

export const updateStory = (data) => {
  return request({
    url: '/admin/story/update',
    method: 'post',
    data
  })
}

export const auditStory = (data) => {
  return request({
    url: '/admin/story/audit',
    method: 'post',
    data
  })
}

export const deleteStory = (id) => {
  return request({
    url: `/admin/story/${id}`,
    method: 'delete'
  })
}

export const getStoryById = (id) => {
  return request({
    url: `/admin/story/${id}`,
    method: 'get'
  })
}

export const getStoryList = (params) => {
  return request({
    url: '/admin/story/list',
    method: 'get',
    params
  })
}

export const getStoryPage = (pageRequest) => {
  return request({
    url: '/admin/story/page',
    method: 'post',
    data: pageRequest
  })
}

// 情感标签管理
export const addTag = (data) => {
  return request({
    url: '/admin/tag/add',
    method: 'post',
    data
  })
}

export const updateTag = (data) => {
  return request({
    url: '/admin/tag/update',
    method: 'post',
    data
  })
}

export const deleteTag = (id) => {
  return request({
    url: `/admin/tag/${id}`,
    method: 'delete'
  })
}

export const getTagById = (id) => {
  return request({
    url: `/admin/tag/${id}`,
    method: 'get'
  })
}

export const getTagList = () => {
  return request({
    url: '/admin/tag/list',
    method: 'get'
  })
}

export const getTagPage = (pageRequest) => {
  return request({
    url: '/admin/tag/page',
    method: 'post',
    data: pageRequest
  })
}

// 路线管理
export const addRoute = (data) => {
  return request({
    url: '/admin/route/add',
    method: 'post',
    data
  })
}

export const updateRoute = (data) => {
  return request({
    url: '/admin/route/update',
    method: 'post',
    data
  })
}

export const deleteRoute = (id) => {
  return request({
    url: `/admin/route/${id}`,
    method: 'delete'
  })
}

export const getRouteById = (id) => {
  return request({
    url: `/admin/route/${id}`,
    method: 'get'
  })
}

export const getRouteList = () => {
  return request({
    url: '/admin/route/list',
    method: 'get'
  })
}

export const getRoutePage = (pageRequest) => {
  return request({
    url: '/admin/route/page',
    method: 'post',
    data: pageRequest
  })
}

// ==================== 用户管理 ====================
/**
 * 分页获取用户列表
 */
export const getUserPage = (current = 1, size = 10, keyword = '') => {
  return request({
    url: '/user/admin/page',
    method: 'get',
    params: {
      current,
      size,
      keyword
    }
  })
}

/**
 * 根据ID获取用户详情
 */
export const getUserById = (id) => {
  return request({
    url: `/user/admin/${id}`,
    method: 'get'
  })
}

/**
 * 创建用户
 */
export const createUser = (data) => {
  return request({
    url: '/user/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 */
export const updateUser = (userId, data) => {
  return request({
    url: `/user/admin/${userId}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 */
export const deleteUser = (id) => {
  return request({
    url: `/user/admin/${id}`,
    method: 'delete'
  })
}

// ==================== 反馈管理 ====================
export const getFeedbackPage = (current = 1, size = 10, keyword = '') => {
  return request({
    url: '/feedback/admin/page',
    method: 'get',
    params: {
      current,
      size,
      keyword
    }
  })
}

export const getFeedbackById = (id) => {
  return request({
    url: `/feedback/admin/${id}`,
    method: 'get'
  })
}

export const updateFeedbackStatus = (id, status) => {
  return request({
    url: `/feedback/admin/${id}`,
    method: 'put',
    data: {
      status
    }
  })
}

export const deleteFeedback = (id) => {
  return request({
    url: `/feedback/admin/${id}`,
    method: 'delete'
  })
}
// ==================== 雅安相关接口 ====================

// 文章管理
/**
 * 添加文章
 */
export const addArticle = (data) => {
  return request({
    url: '/article/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新文章
 */
export const updateArticle = (data) => {
  return request({
    url: `/article/admin/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 删除文章
 */
export const deleteArticle = (id) => {
  return request({
    url: `/article/admin/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取文章
 */
export const getArticleById = (id) => {
  return request({
    url: `/article/admin/${id}`,
    method: 'get'
  })
}

/**
 * 获取文章列表
 */
export const getArticleList = (params) => {
  return request({
    url: '/admin/article/list',
    method: 'get',
    params
  })
}

/**
 * 分页获取文章
 */
export const getArticlePage = (current = 1, size = 10, keyword = '') => {
  return request({
    url: '/article/admin/page',
    method: 'get',
    params: {
      current,
      size,
      keyword
    }
  })
}

// 电视剧管理
/**
 * 添加电视剧
 */
export const addDrama = (data) => {
  return request({
    url: '/drama/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新电视剧
 */
export const updateDrama = (data) => {
  return request({
    url: `/drama/admin/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 删除电视剧
 */
export const deleteDrama = (id) => {
  return request({
    url: `/drama/admin/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取电视剧
 */
export const getDramaById = (id) => {
  return request({
    url: `/drama/admin/${id}`,
    method: 'get'
  })
}

/**
 * 获取电视剧列表
 */
export const getDramaList = (params) => {
  return request({
    url: '/admin/drama/list',
    method: 'get',
    params
  })
}

/**
 * 分页获取电视剧
 */
export const getDramaPage = (current = 1, size = 10, keyword = '') => {
  return request({
    url: '/drama/admin/page',
    method: 'get',
    params: {
      current,
      size,
      keyword
    }
  })
}

// 图片管理
/**
 * 上传图片
 */
export const uploadImage = (formData) => {
  return request({
    url: '/admin/image/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除图片
 */
export const deleteImage = (id) => {
  return request({
    url: `/admin/image/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除图片
 */
export const batchDeleteImage = (ids) => {
  return request({
    url: '/admin/image/batch-delete',
    method: 'post',
    data: {
      ids
    }
  })
}

/**
 * 分页获取图片
 */
export const getImagePage = (pageRequest) => {
  return request({
    url: '/admin/image/page',
    method: 'post',
    data: pageRequest
  })
}

// 场地管理
/**
 * 添加场地
 */
export const addLocation = (data) => {
  return request({
    url: '/location/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新场地
 */
export const updateLocation = (data) => {
  return request({
    url: `/location/admin/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 删除场地
 */
export const deleteLocation = (id) => {
  return request({
    url: `/location/admin/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取场地
 */
export const getLocationById = (id) => {
  return request({
    url: `/location/admin/${id}`,
    method: 'get'
  })
}

/**
 * 获取场地列表
 */
export const getLocationList = (params) => {
  return request({
    url: '/admin/location/list',
    method: 'get',
    params
  })
}

/**
 * 分页获取场地
 */
export const getLocationPage = (current = 1, size = 10, keyword = '') => {
  return request({
    url: '/location/admin/page',
    method: 'get',
    params: {
      current,
      size,
      keyword
    }
  })
}

// 服务管理
/**
 * 添加服务
 */
export const addService = (data) => {
  return request({
    url: '/shoot/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新服务
 */
export const updateService = (data) => {
  return request({
    url: `/shoot/admin/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 删除服务
 */
export const deleteService = (id) => {
  return request({
    url: `/shoot/admin/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取服务
 */
export const getServiceById = (id) => {
  return request({
    url: `/shoot/admin/${id}`,
    method: 'get'
  })
}

/**
 * 获取服务列表
 */
export const getServiceList = (params) => {
  return request({
    url: '/admin/service/list',
    method: 'get',
    params
  })
}

/**
 * 分页获取服务
 */
export const getServicePage = (current = 1, size = 10, keyword = '') => {
  return request({
    url: '/shoot/admin/page',
    method: 'get',
    params: {
      current,
      size,
      keyword
    }
  })
}

// 电视剧拍摄报告管理
/**
 * 添加报告
 */
export const addReport = (data) => {
  return request({
    url: '/report/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新报告
 */
export const updateReport = (data) => {
  return request({
    url: `/report/admin/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 删除报告
 */
export const deleteReport = (id) => {
  return request({
    url: `/report/admin/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取报告
 */
export const getReportById = (id) => {
  return request({
    url: `/report/admin/${id}`,
    method: 'get'
  })
}

/**
 * 获取报告列表
 */
export const getReportList = (params) => {
  return request({
    url: '/admin/report/list',
    method: 'get',
    params
  })
}

/**
 * 分页获取报告
 */
export const getReportPage = (current = 1, size = 10, keyword = '') => {
  return request({
    url: '/report/admin/page',
    method: 'get',
    params: {
      current,
      size,
      keyword
    }
  })
}

// ==================== 旅游线路相关接口 ====================

/**
 * 分页获取旅游线路
 */
export const getTourRoutePage = (current = 1, size = 10, keyword = '') => {
  if (USE_MOCK) {
    return mockApi.getTourRoutePage(current, size, keyword)
  }
  return request({
    url: '/tourroute/page',
    method: 'get',
    params: {
      current,
      size,
      keyword
    }
  })
}

/**
 * 根据ID获取旅游线路
 */
export const getTourRouteById = (id) => {
  if (USE_MOCK) {
    return mockApi.getTourRouteById(id)
  }
  return request({
    url: `/tourroute/${id}`,
    method: 'get'
  })
}

/**
 * 新增旅游线路
 */
export const addTourRoute = (data) => {
  if (USE_MOCK) {
    return mockApi.addTourRoute(data)
  }
  return request({
    url: '/tourroute/admin',
    method: 'post',
    data
  })
}

/**
 * 更新旅游线路
 */
export const updateTourRoute = (data) => {
  if (USE_MOCK) {
    return mockApi.updateTourRoute(data)
  }
  return request({
    url: `/tourroute/admin/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 删除旅游线路
 */
export const deleteTourRoute = (id) => {
  if (USE_MOCK) {
    return mockApi.deleteTourRoute(id)
  }
  return request({
    url: `/tourroute/admin/${id}`,
    method: 'delete'
  })
}

// ==================== 政策相关接口 ====================

/**
 * 分页获取政策
 */
export const getPolicyPage = (current = 1, size = 10, keyword = '', type = '') => {
  if (USE_MOCK) {
    return mockApi.getPolicyPage(current, size, keyword, type)
  }
  return request({
    url: '/policy/page',
    method: 'get',
    params: {
      current,
      size,
      keyword,
      type
    }
  })
}

/**
 * 根据ID获取政策
 */
export const getPolicyById = (id) => {
  if (USE_MOCK) {
    return mockApi.getPolicyById(id)
  }
  return request({
    url: `/policy/${id}`,
    method: 'get'
  })
}

/**
 * 新增政策
 */
export const addPolicy = (data) => {
  if (USE_MOCK) {
    return mockApi.addPolicy(data)
  }
  return request({
    url: '/policy/admin',
    method: 'post',
    data
  })
}

/**
 * 更新政策
 */
export const updatePolicy = (data) => {
  if (USE_MOCK) {
    return mockApi.updatePolicy(data)
  }
  return request({
    url: `/policy/admin/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 删除政策
 */
export const deletePolicy = (id) => {
  if (USE_MOCK) {
    return mockApi.deletePolicy(id)
  }
  return request({
    url: `/policy/admin/${id}`,
    method: 'delete'
  })
}

