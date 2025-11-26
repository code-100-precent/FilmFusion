/**
 * 后端API接口 - 对接真实后端服务
 * 基于后端Controller实现的接口
 */

import { http } from '../utils/http'

// ==================== 类型定义 ====================

// 分页响应类型
interface PageResponse<T> {
    code: number
    message: string
    data: T[]
    pagination: {
        currentPage: number
        pageSize: number
        totalItems: number
        totalPages: number
    }
}

// 通用响应类型
interface ApiResponse<T> {
    code: number
    message: string
    data: T
}

// ==================== 1. 光影雅安 (Drama) ====================

/**
 * 获取影视作品列表（分页）
 */
export const getDramaPage = (params: {
    current?: number
    size?: number
    keyword?: string
}) => {
    return http<PageResponse<any>>({
        url: '/drama/page',
        method: 'GET',
        data: params
    })
}

/**
 * 获取影视作品详情
 */
export const getDramaById = (id: number) => {
    return http<any>({
        url: `/drama/${id}`,
        method: 'GET'
    })
}

// ==================== 2. 跟着影视游雅安 (Tour) ====================

/**
 * 获取旅游线路列表（分页）
 */
export const getTourPage = (params: {
    current?: number
    size?: number
    keyword?: string
}) => {
    return http<PageResponse<any>>({
        url: '/tour/page',
        method: 'GET',
        data: params
    })
}

/**
 * 获取线路详情
 */
export const getTourById = (id: number) => {
    return http<any>({
        url: `/tour/${id}`,
        method: 'GET'
    })
}

// ==================== 3. 拍摄场景 (Location) ====================

/**
 * 获取拍摄场地列表（分页）
 */
export const getLocationPage = (params: {
    current?: number
    size?: number
    keyword?: string
    category?: string
}) => {
    return http<PageResponse<any>>({
        url: '/location/page',
        method: 'GET',
        data: params
    })
}

/**
 * 获取场地详情
 */
export const getLocationById = (id: number) => {
    return http<any>({
        url: `/location/${id}`,
        method: 'GET'
    })
}

// ==================== 4. 视听政策 (Article) ====================

/**
 * 获取政策列表（分页）
 */
export const getArticlePage = (params: {
    current?: number
    size?: number
    keyword?: string
    level?: string
}) => {
    return http<PageResponse<any>>({
        url: '/article/page',
        method: 'GET',
        data: params
    })
}

/**
 * 获取政策详情
 */
export const getArticleById = (id: number) => {
    return http<any>({
        url: `/article/${id}`,
        method: 'GET'
    })
}

// ==================== 5. 协拍服务 (Shoot) ====================

/**
 * 获取协拍服务列表（分页）
 */
export const getShootPage = (params: {
    current?: number
    size?: number
    keyword?: string
    category?: string
}) => {
    return http<PageResponse<any>>({
        url: '/shoot/page',
        method: 'GET',
        data: params
    })
}

/**
 * 获取协拍服务详情
 */
export const getShootById = (id: number) => {
    return http<any>({
        url: `/shoot/${id}`,
        method: 'GET'
    })
}

// ==================== 6. 剧组报备 (Report) ====================

/**
 * 创建剧组报备
 */
export const createReport = (data: any) => {
    return http<any>({
        url: '/report/create',
        method: 'POST',
        data
    })
}

/**
 * 获取我的报备列表
 */
export const getMyReportPage = (params: {
    current?: number
    size?: number
}) => {
    return http<PageResponse<any>>({
        url: '/report/my',
        method: 'GET',
        data: params
    })
}

/**
 * 获取报备详情
 */
export const getReportById = (id: number) => {
    return http<any>({
        url: `/report/${id}`,
        method: 'GET'
    })
}

/**
 * 更新报备
 */
export const updateReport = (id: number, data: any) => {
    return http<any>({
        url: `/report/update/${id}`,
        method: 'PUT',
        data
    })
}

/**
 * 删除报备
 */
export const deleteReport = (id: number) => {
    return http<any>({
        url: `/report/delete/${id}`,
        method: 'DELETE'
    })
}

// ==================== 7. 文件上传 (File) ====================

/**
 * 文件上传
 * @param filePath 本地文件路径
 */
export const uploadFile = (filePath: string) => {
    return new Promise<ApiResponse<any>>((resolve, reject) => {
        console.log(`[${new Date().toISOString()}] API-1: 开始上传文件函数调用，文件路径:`, filePath)
        
        const token = uni.getStorageSync('token')
        console.log(`[${new Date().toISOString()}] API-2: 检查token，状态:`, token ? '已获取token (长度:' + token.length + ')' : '无token')
        
        // 使用固定的开发环境URL，确保可访问
        const baseURL = 'http://localhost:8080/api'
        const uploadUrl = baseURL + '/file'
        console.log(`[${new Date().toISOString()}] API-3: 确定上传地址:`, uploadUrl)

        // 记录开始时间
        const startTime = Date.now()
        console.log(`[${new Date().toISOString()}] API-4: 准备调用uni.uploadFile...`)
        // 获取文件名
        const fileName = filePath.substring(filePath.lastIndexOf('\\') + 1)
        console.log(`[${new Date().toISOString()}] API-4.1: 文件名:`, fileName)

        uni.uploadFile({
            url: uploadUrl,
            filePath: filePath,
            name: 'file',
            // 设置method为PUT
            method: 'PUT',
            header: {
                'Authorization': token ? `Bearer ${token}` : '',
                'source-client': 'miniapp'
            },
            timeout: 60000, // 设置60秒超时
            success: (res) => {
                const endTime = Date.now()
                console.log(`[${new Date().toISOString()}] API-5: 上传请求成功返回 (耗时: ${endTime - startTime}ms)`)
                console.log(`[${new Date().toISOString()}] API-5.1: HTTP状态码: ${res.statusCode}`)
                console.log(`[${new Date().toISOString()}] API-5.2: 原始响应数据:`, res.data)
                
                // 检查响应状态码
                if (res.statusCode !== 200) {
                    const errorMsg = `上传失败，HTTP状态码: ${res.statusCode}`
                    console.error(`[${new Date().toISOString()}] API-ERROR:`, errorMsg)
                    reject({ code: res.statusCode, message: errorMsg, originalResponse: res })
                    return
                }
                
                try {
                    console.log(`[${new Date().toISOString()}] API-6: 开始解析JSON响应...`)
                    const data = JSON.parse(res.data)
                    console.log(`[${new Date().toISOString()}] API-7: 解析成功，响应数据:`, JSON.stringify(data))
                    
                    // 支持200和0两种成功状态码
                    if (data.code === 200 || data.code === 0) {
                        console.log(`[${new Date().toISOString()}] API-8: 上传成功，返回数据给调用方`)
                        // 确保返回的数据格式一致
                        resolve({
                            code: data.code,
                            message: data.message || '请求成功',
                            data: data.data
                        })
                    } else {
                        const errorMsg = data.message || data.msg || '上传失败'
                        console.error(`[${new Date().toISOString()}] API-ERROR: 上传失败，code: ${data.code}, message: ${errorMsg}`)
                        reject({
                            code: data.code,
                            message: errorMsg,
                            originalData: data
                        })
                    }
                } catch (parseError: any) {
                    console.error(`[${new Date().toISOString()}] API-ERROR: 响应数据解析失败:`, parseError)
                    console.error(`[${new Date().toISOString()}] API-ERROR: 无法解析的原始数据:`, res.data)
                    reject({
                        code: 500,
                        message: '服务器响应格式错误',
                        parseError: parseError?.message || 'JSON解析失败',
                        rawData: res.data
                    })
                }
            },
            fail: (err) => {
                const endTime = Date.now()
                console.error(`[${new Date().toISOString()}] API-ERROR: 上传请求失败 (耗时: ${endTime - startTime}ms):`, JSON.stringify(err))
                
                // 详细的错误类型判断
                let errorCode = 0
                let errorMsg = err.errMsg || '网络连接失败，请检查网络设置'
                
                if (errorMsg.includes('request:fail')) {
                    errorCode = 1001
                } else if (errorMsg.includes('timeout')) {
                    errorCode = 1002
                } else if (errorMsg.includes('ssl')) {
                    errorCode = 1003
                } else if (errorMsg.includes('network')) {
                    errorCode = 1004
                }
                
                const errorResponse = {
                    code: errorCode,
                    message: errorMsg,
                    originalError: err
                }
                
                console.error(`[${new Date().toISOString()}] API-ERROR: 错误详情:`, JSON.stringify(errorResponse))
                reject(errorResponse)
            },
            complete: () => {
                const endTime = Date.now()
                console.log(`[${new Date().toISOString()}] API-9: 上传操作完成 (总耗时: ${endTime - startTime}ms)`)
                console.log('----------------------------------------')
                console.log('\n')
            }
        })
    })
}

// ==================== 8. 用户认证 (User) ====================

/**
 * 用户登录
 */
export const login = (data: {
    username: string
    password: string
}) => {
    return http<any>({
        url: '/user/login',
        method: 'POST',
        data
    })
}

/**
 * 用户注册
 */
export const register = (data: {
    username: string
    password: string
    phone: string
    nickname?: string
}) => {
    return http<any>({
        url: '/user/register',
        method: 'POST',
        data
    })
}

/**
 * 获取当前用户信息
 */
export const getUserInfo = () => {
    return http<any>({
        url: '/user/info',
        method: 'GET'
    })
}

/**
 * 更新用户信息
 */
export const updateUserInfo = (data: any) => {
    return http<any>({
        url: '/user/info',
        method: 'PUT',
        data
    })
}

/**
 * 修改密码
 */
export const changePassword = (data: {
    oldPassword: string
    newPassword: string
}) => {
    return http<any>({
        url: '/user/password',
        method: 'PUT',
        data
    })
}

/**
 * 上传头像
 */
export const uploadAvatar = (filePath: string) => {
    return new Promise<ApiResponse<string>>((resolve, reject) => {
        const token = uni.getStorageSync('token')
        const baseURL = process.env.NODE_ENV === 'development'
            ? 'http://localhost:8080/api'
            : 'https://your-production-domain.com/api'

        uni.uploadFile({
            url: baseURL + '/user/avatar',
            filePath: filePath,
            name: 'file',
            header: {
                'Authorization': token ? `Bearer ${token}` : '',
                'source-client': 'miniapp'
            },
            method: 'PUT',
            success: (res) => {
                const data = JSON.parse(res.data)
                if (data.code === 200) {
                    resolve(data)
                } else {
                    uni.showToast({
                        icon: 'none',
                        title: data.message || '上传失败'
                    })
                    reject(data)
                }
            },
            fail: (err) => {
                uni.showToast({
                    icon: 'none',
                    title: '上传失败'
                })
                reject(err)
            }
        })
    })
}

/**
 * 退出登录
 */
export const logout = () => {
    return http<any>({
        url: '/user/logout',
        method: 'POST'
    })
}

/**
 * 检查是否是管理员
 */
export const isAdmin = () => {
    return http<boolean>({
        url: '/user/is-admin',
        method: 'GET'
    })
}

// ==================== 9. 首页配置 (Banner) ====================

/**
 * 获取首页轮播图列表
 */
export const getBannerPage = (params: {
    current?: number
    size?: number
    keyword?: string
}) => {
    return http<PageResponse<any>>({
        url: '/banner/page',
        method: 'GET',
        data: params
    })
}

/**
 * 获取轮播图详情
 */
export const getBannerById = (id: number) => {
    return http<any>({
        url: `/banner/${id}`,
        method: 'GET'
    })
}

// ==================== 导出所有API ====================
export default {
    // 光影雅安
    getDramaPage,
    getDramaById,

    // 跟着影视游雅安
    getTourPage,
    getTourById,

    // 拍摄场景
    getLocationPage,
    getLocationById,

    // 视听政策
    getArticlePage,
    getArticleById,

    // 协拍服务
    getShootPage,
    getShootById,

    // 剧组报备
    createReport,
    getMyReportPage,
    getReportById,
    updateReport,
    deleteReport,

    // 文件上传
    uploadFile,

    // 用户认证
    login,
    register,
    getUserInfo,
    updateUserInfo,
    changePassword,
    uploadAvatar,
    logout,
    isAdmin,

    // 首页配置
    getBannerPage,
    getBannerById
}
