/**
 * API响应处理器 - 处理API返回数据中的文件URL
 */
import { processObjectFileUrls, processArrayFileUrls } from './fileUrl'

/**
 * 处理API响应数据中的文件URL
 * @param response API响应数据
 * @param fileFields 需要处理的文件字段名数组
 * @returns 处理后的响应数据
 */
export const processApiResponseFileUrls = <T>(
  response: any,
  fileFields: string[]
): T => {
  if (!response) {
    return response
  }

  // 处理分页响应
  if (response.data && Array.isArray(response.data)) {
    // 分页响应格式: { code, message, data: [], pagination }
    response.data = processArrayFileUrls(response.data, fileFields)
  } else if (response.data && typeof response.data === 'object') {
    // 检查是否有嵌套的 records 或 list 数组 (针对Page对象)
    if (Array.isArray(response.data.records)) {
      response.data.records = processArrayFileUrls(response.data.records, fileFields)
    } else if (Array.isArray(response.data.list)) {
      response.data.list = processArrayFileUrls(response.data.list, fileFields)
    } else {
      // 单个对象响应格式: { code, message, data: {} }
      response.data = processObjectFileUrls(response.data, fileFields)
    }
  } else if (response.records && Array.isArray(response.records)) {
    // 游标分页响应格式: { records: [], nextCursor: ... }
    response.records = processArrayFileUrls(response.records, fileFields)
  } else if (Array.isArray(response)) {
    // 直接是数组响应
    response = processArrayFileUrls(response, fileFields)
  } else if (typeof response === 'object') {
    // 直接是对象响应
    response = processObjectFileUrls(response, fileFields)
  }

  return response
}

/**
 * 为特定API创建响应处理器
 * @param fileFields 需要处理的文件字段名数组
 * @returns 处理函数
 */
export const createFileUrlProcessor = (fileFields: string[]) => {
  return <T>(response: any): T => {
    return processApiResponseFileUrls<T>(response, fileFields)
  }
}

// 常用模块的文件字段配置
export const FILE_FIELDS_CONFIG = {
  // 用户模块
  user: ['avatar', 'avatarUrl'],
  
  // 影视作品模块
  drama: ['poster', 'posterUrl', 'images', 'thumbs'],
  
  // 场地模块
  location: ['cover', 'image', 'thumbImage', 'images', 'thumbs'],
  
  // 酒店模块
  hotel: ['cover', 'image', 'thumbImage', 'images', 'thumbs'],
  
  // 旅游线路模块
  tour: ['image', 'thumbImage', 'images', 'thumbs'],
  
  // 新闻/文章模块
  article: ['image', 'thumbImage', 'images', 'thumbs'],
  
  // 政策模块
  policy: ['image', 'thumbImage', 'images', 'thumbs', 'fileUrl', 'attachmentUrl'],
  
  // 服务模块
  service: ['image', 'thumbImage', 'images', 'thumbs'],
  
  // 反馈模块
  feedback: ['images', 'thumbs', 'attachments'],
  
  // 通用模块
  common: ['image', 'imageUrl', 'thumbImage', 'images', 'thumbs', 'fileUrl', 'attachmentUrl']
}