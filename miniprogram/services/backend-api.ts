/**
 * 后端API接口 - 对接真实后端服务
 * 基于后端Controller实现的接口
 */

import { http } from '../utils/http'
import { baseURL } from '../utils/http'

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

// ==================== 2.1 住宿推荐 (Hotel) ====================

/**
 * 获取住宿列表（分页）
 */
export const getHotelPage = (params: {
    current?: number
    size?: number
    keyword?: string
}) => {
    return http<PageResponse<any>>({
        url: '/hotel/page',
        method: 'GET',
        data: params
    })
}

/**
 * 获取住宿详情
 */
export const getHotelById = (id: number) => {
    return http<any>({
        url: `/hotel/${id}`,
        method: 'GET'
    })
}

/**
 * 获取附近的住宿（用于tour详情页）
 * @param latitude 纬度
 * @param longitude 经度
 * @param size 返回数量，默认5个
 */
export const getNearbyHotels = (params: {
    latitude?: number
    longitude?: number
    size?: number
}) => {
    return http<PageResponse<any>>({
        url: '/hotel/page',
        method: 'GET',
        data: {
            current: 1,
            size: params.size || 5
        }
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

/**
 * 获取附近的拍摄场景（用于tour详情页）
 * @param latitude 纬度
 * @param longitude 经度
 * @param size 返回数量，默认5个
 */
export const getNearbyLocations = (params: {
    latitude?: number
    longitude?: number
    size?: number
}) => {
    return http<PageResponse<any>>({
        url: '/location/page',
        method: 'GET',
        data: {
            current: 1,
            size: params.size || 5
        }
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

// ==================== 4.1 视听政策 (Policy) ====================

/**
 * 获取政策列表（分页）
 */
export const getPolicyPage = (params: {
    current?: number
    size?: number
    keyword?: string
    type?: string
}) => {
    return http<PageResponse<any>>({
        url: '/policy/page',
        method: 'GET',
        data: params
    })
}

/**
 * 获取政策详情
 */
export const getPolicyById = (id: number) => {
    return http<any>({
        url: `/policy/${id}`,
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
        // 获取token
        const token = uni.getStorageSync('token')

        // 上传地址 - 使用统一的baseURL配置
        const uploadUrl = baseURL + '/file'

        // 上传文件
        uni.uploadFile({
            url: uploadUrl,
            filePath: filePath,
            name: 'file',
            method: 'POST',
            header: {
                'Authorization': token ? `Bearer ${token}` : '',
                'source-client': 'miniapp'
            },
            timeout: 60000,
            success: (res) => {
                // 检查HTTP状态码
                if (res.statusCode !== 200) {
                    reject({
                        code: res.statusCode,
                        message: `上传失败，HTTP状态码: ${res.statusCode}`
                    })
                    return
                }

                // 解析响应数据
                try {
                    const data = JSON.parse(res.data)

                    // 处理成功响应
                    if (data.code === 200 || data.code === 0) {
                        resolve({
                            code: data.code,
                            message: data.message || '请求成功',
                            data: data.data
                        })
                    } else {
                        // 处理失败响应
                        reject({
                            code: data.code || 500,
                            message: data.message || data.msg || '上传失败'
                        })
                    }
                } catch (parseError) {
                    // 响应解析失败
                    reject({
                        code: 500,
                        message: '服务器响应格式错误'
                    })
                }
            },
            fail: (err) => {
                // 请求失败
                reject({
                    code: 1000,
                    message: err.errMsg || '网络连接失败，请检查网络设置'
                })
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

    // 住宿推荐
    getHotelPage,
    getHotelById,
    getNearbyHotels,

    // 拍摄场景
    getLocationPage,
    getLocationById,
    getNearbyLocations,

    // 视听政策
    getArticlePage,
    getArticleById,
    getPolicyPage,
    getPolicyById,

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
