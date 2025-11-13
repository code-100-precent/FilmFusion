/**
 * HTTP 请求工具
 * 需要配置 baseURL
 */
const baseURL = 'http://localhost:8080/api' // 请配置你的 API 基础地址

// 添加拦截器
const httpInterceptor = {
  // 拦截前触发
  invoke(options: UniApp.RequestOptions) {
    console.log('拦截器触发:', options.url)
    
    // 1. 非 http 开头需拼接地址
    if (!options.url.startsWith('http')) {
      options.url = baseURL + options.url
    }
    
    console.log('最终请求URL:', options.url)
    
    // 2. 请求超时, 默认 10s
    options.timeout = 10000
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
uni.addInterceptor('request', httpInterceptor)
uni.addInterceptor('uploadFile', httpInterceptor)

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
    console.log('HTTP请求:', {
      url: options.url,
      method: options.method,
      data: options.data
    })
    
    uni.request({
      ...options,
      // 响应成功
      success(res) {
        console.log('HTTP响应:', {
          url: options.url,
          statusCode: res.statusCode,
          data: res.data
        })
        
        // 状态码 2xx
        if (res.statusCode >= 200 && res.statusCode < 300) {
          // 提取核心数据 res.data
          resolve(res.data as Data<T>)
        } else if (res.statusCode === 401) {
          // 401错误 -> 清理token并跳转到登录页
          uni.removeStorageSync('token')
          uni.removeStorageSync('visitorInfo')
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
          const errorMsg = (res.data as Data<T>)?.message || '请求错误'
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
          title: '网络错误，换个网络试试',
        })
        reject(err)
      },
    })
  })
}
