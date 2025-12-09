import config from '@/config'

/**
 * 获取图片完整URL
 * @param {string} imagePath - 图片相对路径，例如: "/files/thumb/xxx.jpg"
 * @returns {string} 完整的图片URL
 */
export function getImageUrl(imagePath) {
  if (!imagePath) return ''
  
  // 如果已经是完整URL，直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }
  
  // 如果是相对路径，拼接文件服务基础URL
  // 确保路径以 / 开头
  const path = imagePath.startsWith('/') ? imagePath : '/' + imagePath
  
  return config.fileBaseURL + path
}

