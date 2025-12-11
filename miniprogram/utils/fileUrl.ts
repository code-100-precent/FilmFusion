/**
 * 文件URL处理工具
 */
import { FILE_BASE_URL } from './config'

/**
 * 处理文件URL
 * 如果URL不是以http开头，则拼接服务器基础地址
 * @param url 文件URL
 * @returns 处理后的完整URL
 */
export const getFileUrl = (url: string): string => {
  // 如果URL为空或无效，返回空字符串
  if (!url || typeof url !== 'string') {
    return ''
  }
  
  // 如果已经是完整的URL（以http开头），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 如果URL以/开头，去掉第一个/避免双斜杠
  const normalizedUrl = url.startsWith('/') ? url.substring(1) : url
  
  // 如果URL以files开头，需要添加api前缀，因为后端配置的文件访问路径是/api/files/**
  const finalUrl = normalizedUrl.startsWith('files/') ? `api/${normalizedUrl}` : normalizedUrl
  
  // 拼接服务器基础地址
  return `${FILE_BASE_URL}/${finalUrl}`
}

/**
 * 批量处理文件URL
 * @param urls 文件URL数组
 * @returns 处理后的完整URL数组
 */
export const getFileUrls = (urls: string[]): string[] => {
  if (!Array.isArray(urls)) {
    return []
  }
  
  return urls.map(url => getFileUrl(url))
}

/**
 * 处理对象中的文件URL字段
 * @param obj 包含文件URL的对象
 * @param fileFields 需要处理的文件字段名数组
 * @returns 处理后的对象
 */
export const processObjectFileUrls = <T extends Record<string, any>>(
  obj: T,
  fileFields: (keyof T)[]
): T => {
  if (!obj || typeof obj !== 'object') {
    return obj
  }
  
  const result = { ...obj }
  
  fileFields.forEach(field => {
    const value = result[field]
    if (typeof value === 'string') {
      result[field] = getFileUrl(value) as T[keyof T]
    } else if (Array.isArray(value)) {
      result[field] = getFileUrls(value) as T[keyof T]
    }
  })
  
  return result
}

/**
 * 处理数组中每个对象的文件URL字段
 * @param array 包含文件URL的对象数组
 * @param fileFields 需要处理的文件字段名数组
 * @returns 处理后的数组
 */
export const processArrayFileUrls = <T extends Record<string, any>>(
  array: T[],
  fileFields: (keyof T)[]
): T[] => {
  if (!Array.isArray(array)) {
    return []
  }
  
  return array.map(item => processObjectFileUrls(item, fileFields))
}