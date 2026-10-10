import axios from 'axios'
import { presignUpload, confirmUpload } from '@/api'
import { compressImage } from '@/utils/imageCompress'

/**
 * 直传 COS
 *
 * 这里必须用原生 axios，不能走 utils/request 的实例：
 * 预签名地址是 COS 的绝对地址，既不能带 baseURL，也不能带后端 Authorization 头。
 */
function putToCos(uploadUrl, blob, contentType) {
  return axios.put(uploadUrl, blob, {
    headers: { 'Content-Type': contentType },
    timeout: 120000
  })
}

/**
 * 管理端直传图片到 COS
 *
 * 流程：浏览器生成压缩图 → 后端签发预签名地址 → 直传 COS（原图 + 压缩图）→ 回传后端确认
 * 原图走 origin 通道存入原图字段，压缩图走 thumb 通道存入压缩图字段。
 * 压缩图无法生成（GIF、压缩后体积反而变大等）时只传原图，
 * 此时 thumbUrl 为 null，各页面用 `thumbUrl || originUrl` 兜底。
 * 返回值与 uploadFile 保持一致（{ code, data: { originUrl, thumbUrl } }），
 * 便于各页面无缝替换。
 *
 * @param {File} file 原始文件（图片或普通附件）
 */
export async function uploadImageDirect(file) {
  const packed = await compressImage(file)

  const originBlob = packed ? packed.originBlob : file
  const originContentType = packed ? packed.originContentType : (file.type || 'application/octet-stream')
  const originFilename = packed ? packed.originFilename : file.name
  const compressBlob = packed ? packed.compressBlob : null
  const compressContentType = packed ? packed.compressContentType : null

  const credential = (await presignUpload({
    filename: originFilename,
    contentType: originContentType,
    needThumb: !!compressBlob
  })).data

  await putToCos(credential.originUploadUrl, originBlob, originContentType)
  if (compressBlob) {
    await putToCos(credential.thumbUploadUrl, compressBlob, compressContentType)
  }

  return confirmUpload({
    originKey: credential.originKey,
    thumbKey: compressBlob ? credential.thumbKey : null
  })
}