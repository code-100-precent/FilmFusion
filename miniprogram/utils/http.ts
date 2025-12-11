/**
 * HTTP 请求工具
 * 需要配置 baseURL
 */
import { API_BASE_URL } from './config'
import { processApiResponseFileUrls } from './apiProcessor'

// 使用配置文件中的API基础地址
export const baseURL = API_BASE_URL

// 为不同类型的请求创建不同的拦截器配置
const requestInterceptor = {
  // 拦截前触发
  invoke(options: UniApp.RequestOptions) {
    console.log('请求拦截器触发:', options.url)

    // 1. 非 http 开头需拼接地址
    if (!options.url.startsWith('http')) {
      options.url = baseURL + options.url
    }

    console.log('最终请求URL:', options.url)

    // 2. 请求超时, 默认 10s，但不覆盖已设置的值
    if (!options.timeout) {
      options.timeout = 10000
    }
    // 3. 添加小程序端请求头标识
    options.header = {
      ...options.header,
      'source-client': 'miniapp',
    }
    // 4. 如果需要添加 JWT 到请求头，可以在这里添加
    const token = uni.getStorageSync('token')
    if (token) {
      options.header = {
        ...options.header,
        'Authorization': `Bearer ${token}`
      }
    }

    console.log('请求头:', options.header)
  },
}
// 只对普通请求应用拦截器，不对uploadFile应用拦截器
uni.addInterceptor('request', requestInterceptor)

/**
 * 请求函数
 * @param  UniApp.RequestOptions
 * @returns Promise
 *  1. 返回 Promise 对象
 *  2. 获取数据成功
 *    2.1 提取核心数据 res.data
 *    2.2 添加类型，支持泛型
 *  3. 获取数据失败
 *    3.1 401错误  -> 清理用户信息，跳转到登录页
 *    3.2 其他错误 -> 根据后端错误信息轻提示
 *    3.3 网络错误 -> 提示用户换网络
 */
type Data<T> = {
  code: number
  message: string
  data: T
}

// 添加类型，支持泛型
export const http = <T>(options: UniApp.RequestOptions) => {
  // 返回 Promise 对象
  return new Promise<Data<T>>((resolve, reject) => {
    // 处理GET请求的参数，将data转为query string
    let url = options.url || ''
    if (options.method === 'GET' && options.data) {
      const params: string[] = []
      // 确保data是对象类型
      const dataObj = options.data as Record<string, any>
      if (typeof dataObj === 'object' && !Array.isArray(dataObj) && dataObj !== null) {
        Object.keys(dataObj).forEach(key => {
          const value = dataObj[key]
          if (value !== undefined && value !== null && value !== '') {
            // 对于数字类型的值，不转换为字符串，保持原样
            if (typeof value === 'number') {
              params.push(`${encodeURIComponent(key)}=${value}`)
            } else {
              params.push(`${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
            }
          }
        })
      }
      if (params.length > 0) {
        url += (url.includes('?') ? '&' : '?') + params.join('&')
      }
    }

    console.log('HTTP请求:', {
      url: url,
      method: options.method,
      data: options.data
    })

    uni.request({
      ...options,
      url: url,
      // GET请求不需要data字段
      data: options.method === 'GET' ? undefined : options.data,
      // 响应成功
      success(res) {
        console.log('HTTP响应:', {
          url: url,
          statusCode: res.statusCode,
          data: res.data
        })

        // 状态码 2xx
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const responseData = res.data as Data<T>
          // 检查业务状态码
          // 兼容两种格式：
          // 1. 标准格式: { code: 200, data: ... }
          // 2. 直接返回数据格式(如游标分页): { records: ..., nextCursor: ... } (无code字段)
          if (responseData.code === 200 || responseData.code === undefined) {
            resolve(responseData)
          } else {
            // 业务错误 (只有明确 code !== 200 时才视为错误)
            uni.showToast({
              icon: 'none',
              title: responseData.message || '请求失败',
            })
            reject(responseData)
          }
        } else if (res.statusCode === 401) {
          // 401错误 -> 清理token并跳转到登录页
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.showToast({
            icon: 'none',
            title: '登录已过期，请重新登录',
          })
          setTimeout(() => {
            uni.reLaunch({
              url: '/pages/login/login'
            })
          }, 1500)
          reject(res)
        } else {
          // 其他错误 -> 根据后端错误信息轻提示
          const responseData = res.data as Data<T>
          const errorMsg = responseData?.message || '请求错误'
          uni.showToast({
            icon: 'none',
            title: errorMsg,
          })
          reject(res)
        }
      },
      // 响应失败
      fail(err) {
        console.error('HTTP请求失败:', err)
        uni.showToast({
          icon: 'none',
          title: '网络错误，请检查网络连接',
        })
        reject(err)
      },
    })
  })
}

/**
 * 带文件URL处理的HTTP请求函数
 * @param options 请求选项
 * @param fileFields 需要处理的文件字段名数组
 * @returns Promise
 */
export const httpWithFileUrl = <T>(
  options: UniApp.RequestOptions,
  fileFields: string[] = ['image', 'imageUrl', 'thumbImage', 'poster', 'avatar']
): Promise<Data<T>> => {
  // 返回 Promise 对象
  return new Promise<Data<T>>((resolve, reject) => {
    // 处理GET请求的参数，将data转为query string
    let url = options.url || ''
    if (options.method === 'GET' && options.data) {
      const params: string[] = []
      // 确保data是对象类型
      const dataObj = options.data as Record<string, any>
      if (typeof dataObj === 'object' && !Array.isArray(dataObj) && dataObj !== null) {
        Object.keys(dataObj).forEach(key => {
          const value = dataObj[key]
          if (value !== undefined && value !== null && value !== '') {
            // 对于数字类型的值，不转换为字符串，保持原样
            if (typeof value === 'number') {
              params.push(`${encodeURIComponent(key)}=${value}`)
            } else {
              params.push(`${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
            }
          }
        })
      }
      if (params.length > 0) {
        url += (url.includes('?') ? '&' : '?') + params.join('&')
      }
    }

    console.log('HTTP请求(带文件URL处理):', {
      url: url,
      method: options.method,
      data: options.data
    })

    uni.request({
      ...options,
      url: url,
      // GET请求不需要data字段
      data: options.method === 'GET' ? undefined : options.data,
      // 响应成功
      success(res) {
        console.log('HTTP响应(带文件URL处理):', {
          url: url,
          statusCode: res.statusCode,
          data: res.data
        })

        // 状态码 2xx
        if (res.statusCode >= 200 && res.statusCode < 300) {
          let responseData = res.data as Data<T>
          
          // 处理文件URL
          if (responseData && (responseData.data || Array.isArray(responseData))) {
            responseData = processApiResponseFileUrls(responseData, fileFields)
          }
          
          // 检查业务状态码
          // 兼容两种格式：
          // 1. 标准格式: { code: 200, data: ... }
          // 2. 直接返回数据格式(如游标分页): { records: ..., nextCursor: ... } (无code字段)
          if (responseData.code === 200 || responseData.code === undefined) {
            resolve(responseData)
          } else {
            // 业务错误 (只有明确 code !== 200 时才视为错误)
            uni.showToast({
              icon: 'none',
              title: responseData.message || '请求失败',
            })
            reject(responseData)
          }
        } else if (res.statusCode === 401) {
          // 401错误 -> 清理token并跳转到登录页
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.showToast({
            icon: 'none',
            title: '登录已过期，请重新登录',
          })
          setTimeout(() => {
            uni.reLaunch({
              url: '/pages/login/login'
            })
          }, 1500)
          reject(res)
        } else {
          // 其他错误 -> 根据后端错误信息轻提示
          const responseData = res.data as Data<T>
          const errorMsg = responseData?.message || '请求错误'
          uni.showToast({
            icon: 'none',
            title: errorMsg,
          })
          reject(res)
        }
      },
      // 响应失败
      fail(err) {
        console.error('HTTP请求失败:', err)
        uni.showToast({
          icon: 'none',
          title: '网络错误，请检查网络连接',
        })
        reject(err)
      },
    })
  })
}
