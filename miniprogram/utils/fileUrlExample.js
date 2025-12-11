/**
 * 文件URL处理功能使用示例
 * 
 * 本文件展示了如何在项目中使用文件URL处理功能
 */

// 1. 基本使用方式 - 在组件中导入并使用getFileUrl函数
import { getFileUrl } from '@/utils'

export default {
  data() {
    return {
      userAvatar: 'uploads/avatar/123.jpg',
      posterImage: 'uploads/poster/456.jpg'
    }
  },
  computed: {
    // 处理用户头像URL
    processedAvatarUrl() {
      return getFileUrl(this.userAvatar)
    },
    // 处理海报URL
    processedPosterUrl() {
      return getFileUrl(this.posterImage)
    }
  }
}

// 2. 在API层面处理文件URL - 使用httpWithFileUrl函数
import { httpWithFileUrl } from '@/utils/http'
import { FILE_FIELDS_CONFIG } from '@/utils/apiProcessor'

// 示例：获取用户列表，自动处理头像URL
export const getUserList = () => {
  return httpWithFileUrl({
    url: '/user/list',
    method: 'GET'
  }, FILE_FIELDS_CONFIG.user) // 使用预定义的文件字段配置
}

// 示例：获取影视作品列表，自动处理海报URL
export const getDramaList = () => {
  return httpWithFileUrl({
    url: '/drama/list',
    method: 'GET'
  }, FILE_FIELDS_CONFIG.drama) // 使用预定义的文件字段配置
}

// 3. 批量处理对象数组中的文件URL
import { processArrayFileUrls } from '@/utils'

export const processDramaList = (dramaList) => {
  return processArrayFileUrls(dramaList, ['poster', 'images', 'thumbs'])
}

// 4. 处理单个对象中的文件URL
import { processObjectFileUrls } from '@/utils'

export const processUserInfo = (userInfo) => {
  return processObjectFileUrls(userInfo, ['avatar', 'avatarUrl'])
}

// 5. 在模板中使用处理后的URL
/*
<template>
  <view>
    <!-- 使用计算属性处理后的URL -->
    <image :src="processedAvatarUrl" mode="aspectFill"></image>
    <image :src="processedPosterUrl" mode="aspectFill"></image>
    
    <!-- 或者直接在模板中调用函数 -->
    <image :src="getFileUrl(userAvatar)" mode="aspectFill"></image>
    <image :src="getFileUrl(posterImage)" mode="aspectFill"></image>
  </view>
</template>
*/

// 6. 自定义文件URL处理
export const customFileUrlProcessor = (url, baseUrl = 'http://162.14.106.139:8080') => {
  if (!url || typeof url !== 'string') {
    return ''
  }
  
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  const normalizedUrl = url.startsWith('/') ? url.substring(1) : url
  return `${baseUrl}/${normalizedUrl}`
}

// 7. 处理不同类型的文件URL
export const processDifferentFileTypes = (data) => {
  const result = { ...data }
  
  // 处理图片URL
  if (result.image) {
    result.image = getFileUrl(result.image)
  }
  
  // 处理文档URL
  if (result.document) {
    result.document = getFileUrl(result.document)
  }
  
  // 处理视频URL
  if (result.video) {
    result.video = getFileUrl(result.video)
  }
  
  return result
}