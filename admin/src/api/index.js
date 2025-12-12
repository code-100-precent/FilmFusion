import request from '@/utils/request'

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
    data: formData
    // 不手动设置 Content-Type，让浏览器自动设置（包括 boundary）
  })
}

/**
 * 文件上传（通用）
 */
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file',
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
    url: '/file/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
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

// ==================== 用户管理 ====================

/**
 * 分页获取用户列表
 */
export const getUserPage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/user/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
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


export const getFeedbackPage = (current = 1, size = 10, keyword = '', status = '') => {
  return request({
    url: '/feedback/admin/page',
    method: 'get',
    params: {
      current,
      size,
      keyword,
      status
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

// ==================== 文章管理 ====================

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
    url: `/article/admin/update/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 删除文章
 */
export const deleteArticle = (id) => {
  return request({
    url: `/article/admin/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取文章
 */
export const getArticleById = (id) => {
  return request({
    url: `/article/${id}`,
    method: 'get'
  })
}

/**
 * 获取文章列表
 */
export const getArticlePage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/article/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
    }
  })
}

// ==================== 电视剧管理 ====================

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
    url: `/drama/admin/update/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 删除电视剧
 */
export const deleteDrama = (id) => {
  return request({
    url: `/drama/admin/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取电视剧
 */
export const getDramaById = (id) => {
  return request({
    url: `/drama/${id}`,
    method: 'get'
  })
}

/**
 * 获取电视剧列表
 */
export const getDramaList = (params) => {
  return request({
    url: '/drama/admin/page',
    method: 'get',
    params
  })
}

/**
 * 分页获取电视剧
 */
export const getDramaPage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/drama/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
    }
  })
}

// ==================== 场地管理 ====================

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
    url: `/location/admin/update/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 删除场地
 */
export const deleteLocation = (id) => {
  return request({
    url: `/location/admin/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取场地
 */
export const getLocationById = (id) => {
  return request({
    url: `/location/${id}`,
    method: 'get'
  })
}

/**
 * 获取场地列表
 */
export const getLocationList = (params) => {
  return request({
    url: '/location/admin/page',
    method: 'get',
    params
  })
}

/**
 * 分页获取场地
 */
export const getLocationPage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/location/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
    }
  })
}

// ==================== 服务管理 ====================

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
    url: `/shoot/admin/update/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 删除服务
 */
export const deleteService = (id) => {
  return request({
    url: `/shoot/admin/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取服务
 */
export const getServiceById = (id) => {
  return request({
    url: `/shoot/${id}`,
    method: 'get'
  })
}

/**
 * 获取服务列表
 */
export const getServiceList = (params) => {
  return request({
    url: '/shoot/admin/page',
    method: 'get',
    params
  })
}

/**
 * 分页获取服务
 */
export const getServicePage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/shoot/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
    }
  })
}

// ==================== 电视剧拍摄报告管理 ====================

/**
 * 添加报告（使用用户创建接口）
 */
export const addReport = (data) => {
  return request({
    url: '/report/create',
    method: 'post',
    data
  })
}

/**
 * 更新报告（用户更新接口）
 */
export const updateReport = (data) => {
  return request({
    url: `/report/update/${data.id}`,
    method: 'put',
    data
  })
}

/**
 * 管理员更新报告状态
 */
export const updateReportStatus = (data) => {
  return request({
    url: `/report/admin/update/${data.id}`,
    method: 'put',
    data: {
      status: data.status
    }
  })
}

/**
 * 删除报告
 */
export const deleteReport = (id) => {
  return request({
    url: `/report/admin/delete/${id}`,
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
    url: '/report/list',
    method: 'get',
    params
  })
}

/**
 * 分页获取报告
 */
export const getReportPage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/report/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
    }
  })
}

// ==================== Banner管理 ====================

/**
 * 分页获取Banner列表
 */
export const getBannerPage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/banner/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
    }
  })
}

/**
 * 根据ID获取Banner
 */
export const getBannerById = (id) => {
  return request({
    url: `/banner/${id}`,
    method: 'get'
  })
}

/**
 * 创建Banner
 */
export const createBanner = (data) => {
  return request({
    url: '/banner/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新Banner
 */
export const updateBanner = (id, data) => {
  return request({
    url: `/banner/admin/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除Banner
 */
export const deleteBanner = (id) => {
  return request({
    url: `/banner/admin/delete/${id}`,
    method: 'delete'
  })
}

// ==================== Hotel管理 ====================

/**
 * 分页获取酒店列表
 */
export const getHotelPage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/hotel/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
    }
  })
}

/**
 * 根据ID获取酒店
 */
export const getHotelById = (id) => {
  return request({
    url: `/hotel/${id}`,
    method: 'get'
  })
}

/**
 * 创建酒店
 */
export const createHotel = (data) => {
  return request({
    url: '/hotel/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新酒店
 */
export const updateHotel = (id, data) => {
  return request({
    url: `/hotel/admin/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除酒店
 */
export const deleteHotel = (id) => {
  return request({
    url: `/hotel/admin/delete/${id}`,
    method: 'delete'
  })
}

// ==================== Tour管理 ====================

/**
 * 分页获取旅游线路列表
 */
export const getTourPage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/tour/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
    }
  })
}

/**
 * 根据ID获取旅游线路
 */
export const getTourById = (id) => {
  return request({
    url: `/tour/${id}`,
    method: 'get'
  })
}

/**
 * 创建旅游线路
 */
export const createTour = (data) => {
  return request({
    url: '/tour/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新旅游线路
 */
export const updateTour = (id, data) => {
  return request({
    url: `/tour/admin/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除旅游线路
 */
export const deleteTour = (id) => {
  return request({
    url: `/tour/admin/delete/${id}`,
    method: 'delete'
  })
}

// ==================== Policy管理 ====================

/**
 * 分页获取政策列表
 */
export const getPolicyPage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/policy/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
    }
  })
}

export const getPolicyById = (id) => {
  return request({
    url: `/policy/${id}`,
    method: 'get'
  })
}

export const createPolicy = (data) => {
  return request({
    url: '/policy/admin/create',
    method: 'post',
    data
  })
}

export const updatePolicy = (id, data) => {
  return request({
    url: `/policy/admin/update/${id}`,
    method: 'put',
    data
  })
}

export const deletePolicy = (id) => {
  return request({
    url: `/policy/admin/delete/${id}`,
    method: 'delete'
  })
}
// ==================== 旅游线路管理 ====================

/**
 * 分页获取旅游线路列表
 */
export const getTourRoutePage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/tour/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
    }
  })
}

/**
 * 根据ID获取旅游线路
 */
export const getTourRouteById = (id) => {
  return request({
    url: `/tour/${id}`,
    method: 'get'
  })
}

/**
 * 创建旅游线路
 */
export const addTourRoute = (data) => {
  return request({
    url: '/tour/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新旅游线路
 */
export const updateTourRoute = (id, data) => {
  return request({
    url: `/tour/admin/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除旅游线路
 */
export const deleteTourRoute = (id) => {
  return request({
    url: `/tour/admin/delete/${id}`,
    method: 'delete'
  })
}

// ==================== 操作日志管理 ====================

/**
 * 分页获取操作日志列表
 */
export const getLogPage = (page = 1, size = 10, keyword = '') => {
  return request({
    url: '/operationlog/admin/page',
    method: 'get',
    params: {
      current: page,
      size: size,
      keyword: keyword
    }
  })
}
