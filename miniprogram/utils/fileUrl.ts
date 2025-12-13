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
export const getFileUrl = (url: string | string[]): string => {
  // 如果是数组，取第一个非空元素
  if (Array.isArray(url)) {
    if (url.length === 0) return ''
    return getFileUrl(url[0])
  }

  // 如果URL为空或无效，返回空字符串
  if (!url || typeof url !== 'string') {
    return ''
  }

  // 清理字符串：去除首尾空格
  let cleanedUrl = url.trim()
  
  // 尝试检测是否为JSON字符串（兼容历史脏数据）
  if (cleanedUrl.startsWith('{') && (cleanedUrl.includes('"originUrl"') || cleanedUrl.includes('"url"'))) {
    try {
      const parsed = JSON.parse(cleanedUrl)
      if (parsed.originUrl) return getFileUrl(parsed.originUrl)
      if (parsed.url) return getFileUrl(parsed.url)
      if (parsed.path) return getFileUrl(parsed.path)
      if (parsed.filename) return getFileUrl(parsed.filename)
    } catch (e) {
      // 解析失败则继续按普通字符串处理
    }
  }
  
  // 去除可能存在的反引号 (处理 `url` 这种格式)
  cleanedUrl = cleanedUrl.replace(/`/g, '').trim()
  
  // 如果包含逗号，说明是多张图片，取第一张
  if (cleanedUrl.includes(',')) {
    const parts = cleanedUrl.split(',')
    // 递归处理第一部分，确保去除了逗号后的部分也能经过完整的清理流程
    return getFileUrl(parts[0])
  }
  
  // 如果已经是完整的URL（以http开头），直接返回
  if (cleanedUrl.startsWith('http://') || cleanedUrl.startsWith('https://')) {
    return cleanedUrl
  }
  
  // 统一路径分隔符，将 \ 替换为 /
  let normalizedUrl = cleanedUrl.replace(/\\/g, '/')
  
  // 如果URL以/开头，去掉第一个/避免双斜杠
  normalizedUrl = normalizedUrl.startsWith('/') ? normalizedUrl.substring(1) : normalizedUrl
  
  // 如果URL以files开头，需要添加api前缀，因为后端配置的文件访问路径是/api/files/**
  const finalUrl = normalizedUrl.startsWith('files/') ? `api/${normalizedUrl}` : normalizedUrl
  
  // 拼接服务器基础地址
  const result = `${FILE_BASE_URL}/${finalUrl}`
  
  return result
}

/**
 * 从逗号分隔的字符串中获取文件URL数组
 * @param urls 逗号分隔的文件URL字符串
 * @returns 处理后的完整URL数组
 */
export const getFileUrlsFromString = (urls: string): string[] => {
  if (!urls || typeof urls !== 'string') {
    return []
  }
  
  // 按逗号分割URL，并去除空白
  const urlArray = urls.split(',').map(url => url.trim()).filter(url => url)
  
  // 处理每个URL
  const result = urlArray.map(url => getFileUrl(url))
  
  return result
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
  
  const result = urls.map(url => getFileUrl(url))
  
  return result
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
      // 检查是否是逗号分隔的多图片路径
      if (value.includes(',') && (field as string).includes('image')) {
        // 如果是图片字段且包含逗号，则分割处理
        const processedValue = getFileUrlsFromString(value) as T[keyof T]
        result[field] = processedValue
      } else {
        // 单个URL直接处理
        const processedValue = getFileUrl(value) as T[keyof T]
        result[field] = processedValue
      }
    } else if (Array.isArray(value)) {
      const processedValue = getFileUrls(value) as T[keyof T]
      result[field] = processedValue
    }
  })
  
  return result
}

/**
 * 批量处理数组中的文件URL
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
  
  const result = array.map(item => processObjectFileUrls(item, fileFields))
  
  return result
}