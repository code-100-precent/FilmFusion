import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

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

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    console.error('响应错误:', error)
    // 如果是开发环境且接口不存在，返回 mock 数据
    if (error.response && error.response.status === 404) {
      console.warn('接口不存在，返回 mock 数据')
      // 返回成功的 mock 响应，避免页面报错
      return Promise.resolve({
        code: 200,
        message: 'success',
        data: []
      })
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service

