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

export const batchDeleteFeedback = (ids) => {
  return request({
    url: '/admin/feedback/batch-delete',
    method: 'post',
    data: {
      ids
    }
  })
}

