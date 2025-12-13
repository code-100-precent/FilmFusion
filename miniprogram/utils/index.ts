/**
 * 日期格式化函数
 * @param date 日期对象
 * @param format 日期格式，默认为 YYYY-MM-DD HH:mm:ss
 */
export const formatDate = (date: Date, format = 'YYYY-MM-DD HH:mm:ss') => {
  // 获取年月日时分秒，通过 padStart 补 0
  const year = String(date.getFullYear())
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  // 返回格式化后的结果
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

// 导出文件URL处理函数
export { getFileUrl, getFileUrls, getFileUrlsFromString, processObjectFileUrls, processArrayFileUrls } from './fileUrl'

// 导出API响应处理器
export { 
  processApiResponseFileUrls, 
  createFileUrlProcessor, 
  FILE_FIELDS_CONFIG 
} from './apiProcessor'

/**
 * 加载状态管理Mixin
 * 为所有页面提供统一的加载状态管理
 */
// 定义加载状态接口
interface LoadingState {
  loading: boolean;
  loadingTimer: number | null;
}

// 定义方法接口
interface LoadingMethods {
  startLoading(timeout?: number): void;
  endLoading(): void;
  withLoading<T>(asyncFunc: () => Promise<T>, timeout?: number): Promise<T>;
}

export const loadingMixin = {
  data(): LoadingState {
    return {
      loading: false,
      loadingTimer: null
    }
  },
  
  onUnload(this: LoadingState) {
    // 页面卸载时清除定时器
    if (this.loadingTimer) {
      clearTimeout(this.loadingTimer)
      this.loadingTimer = null
    }
  },
  
  methods: {
    /**
     * 开始加载
     * @param timeout 可选的超时时间，默认2000ms
     */
    startLoading(this: LoadingState, timeout: number = 2000) {
      // 清除之前的定时器
      if (this.loadingTimer) {
        clearTimeout(this.loadingTimer)
        this.loadingTimer = null
      }
      
      this.loading = true
      
      // 设置超时，防止加载状态一直显示
      this.loadingTimer = setTimeout(() => {
        if (this.loading) {
          this.loading = false
          this.loadingTimer = null
        }
      }, timeout)
    },
    
    /**
     * 结束加载
     */
    endLoading(this: LoadingState) {
      // 清除定时器
      if (this.loadingTimer) {
        clearTimeout(this.loadingTimer)
        this.loadingTimer = null
      }
      
      this.loading = false
    },
    
    /**
     * 带加载状态的异步操作
     * @param asyncFunc 异步函数
     * @param timeout 超时时间
     * @returns 异步操作的结果
     */
    async withLoading<T>(this: LoadingState & LoadingMethods, asyncFunc: () => Promise<T>, timeout: number = 2000): Promise<T> {
      this.startLoading(timeout)
      
      try {
        const result = await asyncFunc()
        return result
      } catch (error) {
        throw error
      } finally {
        this.endLoading()
      }
    }
  }
} as const
