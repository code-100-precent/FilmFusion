/**
 * 统一错误处理工具
 * 提供友好的错误提示和日志记录
 */

// 错误码映射表 - 前端统一处理错误文案
export const ERROR_MESSAGES: Record<number, string> = {
  // 通用/系统错误
  400: '请求参数有误',
  401: '登录已过期，请重新登录',
  402: '请求参数不合法',
  403: '您没有权限执行此操作',
  404: '找不到相关内容',
  409: '该记录已存在',
  410: '内容已过期或失效',
  413: '文件太大，请选择小一点的文件',
  415: '不支持该文件类型',
  429: '操作太频繁，请稍后再试',
  500: '系统繁忙，请稍后再试',
  501: '数据服务异常',
  502: '服务暂不可用',
  503: '网络服务异常',
  
  // 业务逻辑错误
  550: '业务处理失败',
  551: '余额不足',
  552: '商品已售罄',
  
  // 文件上传错误
  1001: '文件格式不正确',
  1002: '文件大小超出限制',
  1003: '上传失败，请重试'
}

// 网络错误类型
export enum NetworkErrorType {
  TIMEOUT = 'timeout',
  NETWORK_FAIL = 'fail',
  ABORT = 'abort'
}

/**
 * 错误处理配置
 */
interface ErrorHandlerOptions {
  showToast?: boolean // 是否显示Toast提示，默认true
  logError?: boolean // 是否记录错误日志，默认true
  duration?: number // Toast显示时长，默认2000ms
}

/**
 * 处理API错误
 * @param error 错误对象
 * @param context 错误上下文（用于日志）
 * @param options 配置选项
 */
export const handleApiError = (
  error: any,
  context: string = '操作',
  options: ErrorHandlerOptions = {}
) => {
  const {
    showToast = true,
    logError = true,
    duration = 2000
  } = options

  let errorMsg = '操作失败，请稍后重试'
  let errorCode = 0

  // 解析错误信息
  if (error) {
    if (typeof error === 'object') {
      errorCode = error.code || error.statusCode || 0
      
      // 优先使用映射表中的友好提示
      if (errorCode && ERROR_MESSAGES[errorCode]) {
        errorMsg = ERROR_MESSAGES[errorCode]
      } else if (error.message) {
        // 如果映射表中没有，使用默认提示（不使用后端message）
        errorMsg = `${context}失败，请稍后重试`
      }
    } else if (typeof error === 'string') {
      errorMsg = error
    }
  }

  // 记录错误日志
  if (logError) {
    console.error(`[${context}错误]`, {
      code: errorCode,
      message: error?.message || error,
      originalError: error
    })
  }

  // 显示Toast提示
  if (showToast) {
    uni.showToast({
      icon: 'none',
      title: errorMsg,
      duration
    })
  }

  return {
    code: errorCode,
    message: errorMsg
  }
}

/**
 * 处理网络错误
 * @param error 错误对象
 * @param context 错误上下文
 * @param options 配置选项
 */
export const handleNetworkError = (
  error: any,
  context: string = '网络请求',
  options: ErrorHandlerOptions = {}
) => {
  const {
    showToast = true,
    logError = true,
    duration = 2000
  } = options

  let errorMsg = '网络连接失败，请检查网络'

  // 判断网络错误类型
  if (error?.errMsg) {
    if (error.errMsg.includes('timeout')) {
      errorMsg = '请求超时，请重试'
    } else if (error.errMsg.includes('fail')) {
      errorMsg = '网络连接失败，请检查网络'
    } else if (error.errMsg.includes('abort')) {
      errorMsg = '请求已取消'
    }
  }

  // 记录错误日志
  if (logError) {
    console.error(`[${context}网络错误]`, error)
  }

  // 显示Toast提示
  if (showToast) {
    uni.showToast({
      icon: 'none',
      title: errorMsg,
      duration
    })
  }

  return {
    code: 1000,
    message: errorMsg
  }
}

/**
 * 处理文件上传错误
 * @param error 错误对象
 * @param options 配置选项
 */
export const handleUploadError = (
  error: any,
  options: ErrorHandlerOptions = {}
) => {
  const {
    showToast = true,
    logError = true,
    duration = 2000
  } = options

  let errorMsg = '上传失败，请重试'
  let errorCode = 0

  if (error) {
    errorCode = error.code || error.statusCode || 0
    
    // 文件上传特定错误
    if (errorCode === 413) {
      errorMsg = '文件太大，请选择小一点的文件'
    } else if (errorCode === 415) {
      errorMsg = '不支持该文件类型'
    } else if (errorCode === 401) {
      errorMsg = '登录已过期，请重新登录'
    } else if (ERROR_MESSAGES[errorCode]) {
      errorMsg = ERROR_MESSAGES[errorCode]
    }
  }

  // 记录错误日志
  if (logError) {
    console.error('[文件上传错误]', {
      code: errorCode,
      message: error?.message || error,
      originalError: error
    })
  }

  // 显示Toast提示
  if (showToast) {
    uni.showToast({
      icon: 'none',
      title: errorMsg,
      duration
    })
  }

  return {
    code: errorCode,
    message: errorMsg
  }
}

/**
 * 处理表单验证错误
 * @param fieldName 字段名称
 * @param errorType 错误类型
 */
export const handleValidationError = (
  fieldName: string,
  errorType: 'required' | 'format' | 'length' | 'custom',
  customMsg?: string
) => {
  let errorMsg = ''

  switch (errorType) {
    case 'required':
      errorMsg = `请填写${fieldName}`
      break
    case 'format':
      errorMsg = `${fieldName}格式不正确`
      break
    case 'length':
      errorMsg = `${fieldName}长度不符合要求`
      break
    case 'custom':
      errorMsg = customMsg || `${fieldName}验证失败`
      break
  }

  uni.showToast({
    icon: 'none',
    title: errorMsg,
    duration: 2000
  })

  return errorMsg
}

/**
 * 统一的成功提示
 * @param message 提示信息
 * @param duration 显示时长
 */
export const showSuccess = (message: string, duration: number = 1500) => {
  uni.showToast({
    icon: 'success',
    title: message,
    duration
  })
}

/**
 * 统一的警告提示
 * @param message 提示信息
 * @param duration 显示时长
 */
export const showWarning = (message: string, duration: number = 2000) => {
  uni.showToast({
    icon: 'none',
    title: message,
    duration
  })
}

/**
 * 统一的确认对话框
 * @param title 标题
 * @param content 内容
 * @param confirmText 确认按钮文字
 * @param cancelText 取消按钮文字
 */
export const showConfirm = (
  title: string,
  content: string,
  confirmText: string = '确定',
  cancelText: string = '取消'
): Promise<boolean> => {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      content,
      confirmText,
      cancelText,
      success: (res) => {
        resolve(res.confirm)
      },
      fail: () => {
        resolve(false)
      }
    })
  })
}

export default {
  ERROR_MESSAGES,
  handleApiError,
  handleNetworkError,
  handleUploadError,
  handleValidationError,
  showSuccess,
  showWarning,
  showConfirm
}
