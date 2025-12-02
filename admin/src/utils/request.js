import axios from 'axios'
import router from '@/router'
import { useMessage } from 'naive-ui'

// 创建消息实例（在需要时使用）
let messageInstance = null

const getMessage = () => {
  if (!messageInstance) {
    // 延迟初始化，在组件挂载后使用
    const div = document.createElement('div')
    document.body.appendChild(div)
    // 这里会在组件中使用 useMessage，暂时使用简单的提示
  }
  return messageInstance
}

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      // 使用Authorization header，格式为 Bearer token
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 消息提示函数（使用原生方式，在组件中会使用 Naive UI 的 useMessage）
const showMessage = (message, type = 'error') => {
  // 创建消息元素
  const messageEl = document.createElement('div')
  messageEl.className = `naive-message naive-message-${type}`
  messageEl.textContent = message
  messageEl.style.cssText = `
    position: fixed;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    padding: 12px 20px;
    background: ${type === 'error' ? '#d03050' : type === 'success' ? '#18a058' : '#2080f0'};
    color: white;
    border-radius: 6px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    z-index: 10000;
    font-size: 14px;
    animation: messageSlideDown 0.3s ease;
  `
  document.body.appendChild(messageEl)
  
  setTimeout(() => {
    messageEl.style.animation = 'messageSlideUp 0.3s ease'
    setTimeout(() => {
      if (document.body.contains(messageEl)) {
        document.body.removeChild(messageEl)
      }
    }, 300)
  }, 3000)
}

// 添加动画样式
if (!document.getElementById('naive-message-styles')) {
  const style = document.createElement('style')
  style.id = 'naive-message-styles'
  style.textContent = `
    @keyframes messageSlideDown {
      from {
        opacity: 0;
        transform: translateX(-50%) translateY(-20px);
      }
      to {
        opacity: 1;
        transform: translateX(-50%) translateY(0);
      }
    }
    @keyframes messageSlideUp {
      from {
        opacity: 1;
        transform: translateX(-50%) translateY(0);
      }
      to {
        opacity: 0;
        transform: translateX(-50%) translateY(-20px);
      }
    }
  `
  document.head.appendChild(style)
}

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 记录成功响应的基本信息
    console.log(`请求成功: ${response.config.url}, 状态码: ${response.status}`)
    
    if (res.code === 200) {
      return res
    } else {
      // 统一错误处理
      const errorMsg = res.message || '请求失败'
      console.error(`业务逻辑错误: ${response.config.url}, 错误码: ${res.code}, 错误信息: ${errorMsg}`)
      showMessage(errorMsg, 'error')
      
      if (res.code === 401) {
        console.warn('认证失效，清除token并跳转到登录页')
        localStorage.removeItem('token')
        setTimeout(() => {
          router.push('/login')
        }, 1000)
      }
      
      // 构建详细的错误对象
      const error = new Error(errorMsg)
      error.code = res.code
      error.response = response
      error.requestUrl = response.config.url
      return Promise.reject(error)
    }
  },
  error => {
    // 增强错误日志记录
    const requestInfo = error.config ? 
      `${error.config.method?.toUpperCase()} ${error.config.url}` : '未知请求'
    
    console.error(`响应错误 - 请求: ${requestInfo}`)
    
    if (error.response) {
      // 服务器返回了错误状态码
      const { status, statusText, config } = error.response
      const data = error.response.data
      
      console.error(`HTTP错误: ${status} ${statusText}`)
      console.error(`请求详情: ${JSON.stringify(config)}`)
      console.error(`响应数据: ${JSON.stringify(data)}`)
      
      let errorMsg = `请求失败 (${status} ${statusText})`
      
      switch (status) {
        case 400:
          errorMsg = data?.message || '请求参数错误，请检查输入'
          break
        case 401:
          errorMsg = data?.message || '未登录或登录已过期，请重新登录'
          console.warn('认证失败，清除token并跳转到登录页')
          localStorage.removeItem('token')
          setTimeout(() => {
            router.push('/login')
          }, 1000)
          break
        case 403:
          errorMsg = data?.message || '没有权限访问此资源'
          break
        case 404:
          errorMsg = data?.message || `请求的资源不存在 (${config.url})`
          break
        case 500:
          errorMsg = data?.message || '服务器内部错误，请稍后重试'
          break
        default:
          errorMsg = data?.message || `请求失败 (${status} ${statusText})`
      }
      
      // 增强错误对象
      error.httpStatus = status
      error.errorType = 'http'
      error.requestUrl = config.url
      error.responseData = data
    } else if (error.request) {
      // 请求已发出但没有收到响应
      console.error('网络错误: 请求已发出但未收到响应')
      console.error('请求详情:', JSON.stringify(error.config))
      errorMsg = '网络连接失败，请检查网络连接或服务器状态'
      
      // 增强错误对象
      error.errorType = 'network'
      error.requestUrl = error.config?.url
    } else {
      // 其他错误
      console.error('请求配置错误:', error.message)
      errorMsg = error.message || '请求配置错误'
      
      // 增强错误对象
      error.errorType = 'config'
    }
    
    // 显示详细的错误信息
    showMessage(errorMsg, 'error')
    return Promise.reject(error)
  }
)

export default service

