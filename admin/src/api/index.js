import request from '@/utils/request'
import { mockApi } from '@/utils/mock'

// 是否启用 Mock（开发环境默认启用）
const USE_MOCK = false

// ==================== 管理员相关接口 ====================

/**
 * 管理员密码登录
 */
export const adminLogin = (email, password) => {
  return request({
    url: '/admin/login/password',
    method: 'post',
    params: {
      email,
      password
    }
  })
}

/**
 * 管理员邮箱登录（需要验证码）
 */
export const adminLoginByEmail = (data) => {
  return request({
    url: '/admin/login/email',
    method: 'post',
    data
  })
}

/**
 * 管理员注册（直接填写信息注册）
 */
export const adminRegister = (data) => {
  return request({
    url: '/admin/register',
    method: 'post',
    data
  })
}

/**
 * 获取当前管理员信息
 */
export const getAdminInfo = () => {
  return request({
    url: '/admin/info',
    method: 'get'
  })
}

/**
 * 管理员退出登录
 */
export const adminLogout = () => {
  return request({
    url: '/admin/logout',
    method: 'post'
  })
}

/**
 * 更新管理员信息
 */
export const updateAdminInfo = (data) => {
  return request({
    url: '/admin/update',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 */
export const changePassword = (oldPassword, newPassword) => {
  return request({
    url: '/admin/change-password',
    method: 'post',
    params: {
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
    url: '/admin/upload-avatar',
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

// ==================== 反馈管理 ====================
export const getFeedbackPage = (pageRequest) => {
  return request({
    url: '/admin/feedback/page',
    method: 'post',
    data: pageRequest
  })
}

export const getFeedbackById = (id) => {
  return request({
    url: `/admin/feedback/${id}`,
    method: 'get'
  })
}

export const deleteFeedback = (id) => {
  return request({
    url: `/admin/feedback/${id}`,
    method: 'delete'
  })
}

export const updateFeedbackStatus = (id, status) => {
  return request({
    url: '/admin/feedback/update-status',
    method: 'post',
    params: {
      id,
      status
    }
  })
}

export const batchUpdateFeedbackStatus = (ids, status) => {
  return request({
    url: '/admin/feedback/batch-update-status',
    method: 'post',
    params: {
      ids: ids.join(','),
      status
    }
  })
}
// ==================== 雅安相关接口 ====================

// 文章管理
/**
 * 添加文章
 */
export const addArticle = (data) => {
  return request({
    url: '/admin/article/add',
    method: 'post',
    data
  })
}

/**
 * 更新文章
 */
export const updateArticle = (data) => {
  return request({
    url: '/admin/article/update',
    method: 'put',
    data
  })
}

/**
 * 删除文章
 */
export const deleteArticle = (id) => {
  return request({
    url: `/admin/article/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除文章
 */
export const batchDeleteArticle = (ids) => {
  return request({
    url: '/admin/article/batch-delete',
    method: 'post',
    data: {
      ids
    }
  })
}

/**
 * 根据ID获取文章
 */
export const getArticleById = (id) => {
  return request({
    url: `/admin/article/${id}`,
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
export const getArticlePage = (pageRequest) => {
  return request({
    url: '/admin/article/page',
    method: 'post',
    data: pageRequest
  })
}

// 电视剧管理
/**
 * 添加电视剧
 */
export const addDrama = (data) => {
  return request({
    url: '/admin/drama/add',
    method: 'post',
    data
  })
}

/**
 * 更新电视剧
 */
export const updateDrama = (data) => {
  return request({
    url: '/admin/drama/update',
    method: 'put',
    data
  })
}

/**
 * 删除电视剧
 */
export const deleteDrama = (id) => {
  return request({
    url: `/admin/drama/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除电视剧
 */
export const batchDeleteDrama = (ids) => {
  return request({
    url: '/admin/drama/batch-delete',
    method: 'post',
    data: {
      ids
    }
  })
}

/**
 * 根据ID获取电视剧
 */
export const getDramaById = (id) => {
  return request({
    url: `/admin/drama/${id}`,
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
export const getDramaPage = (pageRequest) => {
  return request({
    url: '/admin/drama/page',
    method: 'post',
    data: pageRequest
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
    url: '/admin/location/add',
    method: 'post',
    data
  })
}

/**
 * 更新场地
 */
export const updateLocation = (data) => {
  return request({
    url: '/admin/location/update',
    method: 'put',
    data
  })
}

/**
 * 删除场地
 */
export const deleteLocation = (id) => {
  return request({
    url: `/admin/location/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除场地
 */
export const batchDeleteLocation = (ids) => {
  return request({
    url: '/admin/location/batch-delete',
    method: 'post',
    data: {
      ids
    }
  })
}

/**
 * 根据ID获取场地
 */
export const getLocationById = (id) => {
  return request({
    url: `/admin/location/${id}`,
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
export const getLocationPage = (pageRequest) => {
  return request({
    url: '/admin/location/page',
    method: 'post',
    data: pageRequest
  })
}

// 服务管理
/**
 * 添加服务
 */
export const addService = (data) => {
  return request({
    url: '/admin/service/add',
    method: 'post',
    data
  })
}

/**
 * 更新服务
 */
export const updateService = (data) => {
  return request({
    url: '/admin/service/update',
    method: 'put',
    data
  })
}

/**
 * 删除服务
 */
export const deleteService = (id) => {
  return request({
    url: `/admin/service/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除服务
 */
export const batchDeleteService = (ids) => {
  return request({
    url: '/admin/service/batch-delete',
    method: 'post',
    data: {
      ids
    }
  })
}

/**
 * 根据ID获取服务
 */
export const getServiceById = (id) => {
  return request({
    url: `/admin/service/${id}`,
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
export const getServicePage = (pageRequest) => {
  return request({
    url: '/admin/service/page',
    method: 'post',
    data: pageRequest
  })
}

// 电视剧拍摄报告管理
/**
 * 添加报告
 */
export const addReport = (data) => {
  return request({
    url: '/admin/report/add',
    method: 'post',
    data
  })
}

/**
 * 更新报告
 */
export const updateReport = (data) => {
  return request({
    url: '/admin/report/update',
    method: 'put',
    data
  })
}

/**
 * 删除报告
 */
export const deleteReport = (id) => {
  return request({
    url: `/admin/report/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除报告
 */
export const batchDeleteReport = (ids) => {
  return request({
    url: '/admin/report/batch-delete',
    method: 'post',
    data: {
      ids
    }
  })
}

/**
 * 根据ID获取报告
 */
export const getReportById = (id) => {
  return request({
    url: `/admin/report/${id}`,
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
export const getReportPage = (pageRequest) => {
  return request({
    url: '/admin/report/page',
    method: 'post',
    data: pageRequest
  })
}

