/**
 * 图片压缩工具
 *
 * 用于管理端直传前在浏览器侧生成压缩图：
 * - 原图：不做任何处理，直接上传用户选择的原始文件，保证 C 端展示清晰度
 * - 普通图片：压缩图等比缩到最大边 1920、输出 jpeg
 * - PNG / WEBP：压缩图保留原格式，以保留透明通道
 * - GIF：canvas 无法输出动图，且静态帧格式无法与 .gif 扩展名对齐，跳过压缩图
 * - 非图片：返回 null，由调用方按原文件直传
 *
 * 压缩图的对象键沿用原图文件名，因此输出格式必须与扩展名保持一致；
 * 无法对齐格式或压缩后体积反而变大时，一律跳过压缩图，由调用方用原图 url 兜底。
 */

/** 压缩图最大边（像素） */
export const COMPRESS_MAX_EDGE = 1920
/** 压缩图输出质量 */
export const COMPRESS_QUALITY = 0.85

/**
 * 解码图片，优先使用 createImageBitmap（会按 EXIF 方向自动校正）
 */
async function decodeImage(file) {
  if (typeof createImageBitmap === 'function') {
    try {
      const bitmap = await createImageBitmap(file, { imageOrientation: 'from-image' })
      return {
        source: bitmap,
        width: bitmap.width,
        height: bitmap.height,
        release: () => bitmap.close()
      }
    } catch (e) {
      // 部分浏览器不支持 imageOrientation 选项，回退到 Image 解码
    }
  }

  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      resolve({
        source: img,
        width: img.naturalWidth,
        height: img.naturalHeight,
        release: () => {}
      })
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('图片解码失败'))
    }
    img.src = url
  })
}

/**
 * 等比缩放尺寸，不放大
 */
function scaleSize(width, height, maxEdge) {
  const ratio = Math.min(1, maxEdge / Math.max(width, height))
  return {
    width: Math.max(1, Math.round(width * ratio)),
    height: Math.max(1, Math.round(height * ratio))
  }
}

/**
 * 绘制到 canvas，输出 jpeg 时先铺白底，避免透明区域变黑
 */
function drawToCanvas(image, width, height, opaqueBackground) {
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  if (opaqueBackground) {
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, width, height)
  }
  ctx.drawImage(image, 0, 0, width, height)
  return canvas
}

function toBlob(canvas, type, quality) {
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => (blob ? resolve(blob) : reject(new Error('图片压缩失败'))),
      type,
      quality
    )
  })
}

/**
 * 压缩图输出格式
 *
 * 压缩图的对象键沿用原图文件名，因此输出格式需与扩展名保持一致。
 * 返回 null 表示无法生成格式对齐的压缩图（GIF 动图、未知格式），此时跳过压缩图。
 */
function resolveCompressType(fileType) {
  if (fileType === 'image/jpeg') return 'image/jpeg'
  if (fileType === 'image/png') return 'image/png'
  if (fileType === 'image/webp') return 'image/webp'
  return null
}

/**
 * 统一组装返回值，跳过压缩图时 compressBlob / compressContentType 为 null
 */
function buildResult(file, compressBlob, compressContentType) {
  return {
    originBlob: file,
    originContentType: file.type,
    originFilename: file.name,
    compressBlob,
    compressContentType
  }
}

/**
 * 生成压缩图
 *
 * 原图始终返回用户选择的原始文件，不做任何压缩处理；
 * 压缩图用于落库备用，不作为 C 端主要展示源。
 *
 * @param {File} file 原始文件
 * @returns {Promise<null | {
 *   originBlob: Blob, originContentType: string, originFilename: string,
 *   compressBlob: Blob|null, compressContentType: string|null
 * }>} 非图片返回 null；无法生成压缩图时 compressBlob 为 null
 */
export async function compressImage(file) {
  if (!file || !file.type || !file.type.startsWith('image/')) {
    return null
  }

  const compressType = resolveCompressType(file.type)
  if (!compressType) {
    return buildResult(file, null, null)
  }

  const decoded = await decodeImage(file)
  try {
    const { width, height } = scaleSize(decoded.width, decoded.height, COMPRESS_MAX_EDGE)
    const compressBlob = await toBlob(
      drawToCanvas(decoded.source, width, height, compressType === 'image/jpeg'),
      compressType,
      COMPRESS_QUALITY
    )

    // 浏览器未按目标格式输出（如老浏览器不支持 webp），或压缩后体积反而变大（常见于 PNG），
    // 都跳过压缩图，避免对象键扩展名与内容格式不一致，或存下一份更占空间的“压缩图”
    if (compressBlob.type !== compressType || compressBlob.size >= file.size) {
      return buildResult(file, null, null)
    }

    return buildResult(file, compressBlob, compressType)
  } finally {
    decoded.release()
  }
}