# 管理端图片直传 COS 改造方案

## Context

**为什么改**：管理端上传图片慢。当前链路是「浏览器 → 后端 `/api/file` → 腾讯云 COS」，文件要经后端中转，且后端在同一个请求内同步用 Thumbnailator 生成缩略图再上传一次，端到端耗时被叠加放大。

**改成什么**：浏览器直接 PUT 到 COS，后端只负责签发一次性上传凭证，不再经手文件字节。压缩能力保留，改由前端 canvas 完成（顺带把原图也压小，这是提速的主要来源）。

**边界**：本次只改调用通用 `uploadFile` 的 11 个业务页面；头像 `POST /user/avatar` 暂不动（该接口管理端和 Web 端共用）。

---

## 目标流程

```
1. 前端 canvas 压缩   → originBlob(jpeg, 最大边1920, q0.85) + thumbBlob(jpeg, 最大边200, q0.8)
2. POST /api/file/presign  → 后端生成唯一 key + 两组 PUT 预签名 URL（10 分钟有效）
3. 前端 axios PUT 二进制直传 COS（origin、thumb 各一次）
4. POST /api/file/confirm  → 后端校验对象已落 COS，返回 { originUrl, thumbUrl }
5. 前端把 originUrl / thumbUrl 写进表单，后续随业务 save 接口落库（与现状一致）
```

第 4 步即「管理端上传完之后返回路径给后端」，同时顺带做一次服务端存在性校验，避免前端伪造 URL 入库。

---

## 后端改动

### 1. 新增 DTO / VO

- `model/dto/PresignRequestDTO.java`：`filename`、`contentType`、`needThumb`(Boolean)
- `model/dto/ConfirmFileDTO.java`：`originKey`、`thumbKey`
- `model/vo/PresignVO.java`：`originKey`、`originUploadUrl`、`originUrl`、`thumbKey`、`thumbUploadUrl`、`thumbUrl`

字段命名沿用 `model/vo/FileVO.java` 的 `originUrl` / `thumbUrl`，保证前端契约不变。

### 2. `FileStorageAdapter` 新增 1 个方法

```java
/** 生成 PUT 预签名上传地址，key 为 COS 对象键 */
String generatePresignedPutUrl(String key, int expireSeconds);
```

- **`OssStorageService`** 实现（已实测确认 `cos_api-5.6.227` 支持）：
  ```java
  GeneratePresignedUrlRequest req =
      new GeneratePresignedUrlRequest(properties.getCosBucket(), key, HttpMethodName.PUT);
  req.setExpiration(new Date(System.currentTimeMillis() + expireSeconds * 1000L));
  return cosClient.generatePresignedUrl(req).toString();
  ```
  复用类内已有的私有 `COSClient` 实例，不新建客户端。

  **不签名 Content-Type**：签名它会导致前端请求头必须逐字节一致，属于无谓的失败面；改为前端显式设置正确 Content-Type，COS 会按客户端发送的值存储。

- **`LocalStorageService`** 实现：抛 `UnsupportedOperationException("本地存储不支持前端直传，请将 code100.storage.type 设为 cos")`。当前 `application.yml` 已是 `type: cos`，仅作兜底。

`exists(String path)` 接口方法已存在且 `OssStorageService` 已实现（内部 `extractKey` 能同时吃 key 和完整 URL），confirm 直接复用，无需新增。

### 3. `FileService` / `FileServiceImpl` 新增 2 个方法

- `PresignVO presign(PresignRequestDTO dto)`
  - 生成 baseName：`System.currentTimeMillis() + "_" + 文件名安全化`（与 `OssStorageService.buildBaseName` 同规则）
  - key：`origin/{baseName}`、`thumb/{baseName}`（与现有 COS 目录结构一致）
  - `needThumb` 为 false 时只返回 origin 一组
  - 每条预签名 URL 有效期 600 秒
  - `originUrl` / `thumbUrl` 用 `fileStorageAdapter.getUrl(key)` 生成

- `FileVO confirm(ConfirmFileDTO dto)`
  - 校验 `originKey` 必须在 `origin/` 前缀下、`thumbKey` 必须在 `thumb/` 前缀下（防路径遍历）
  - `fileStorageAdapter.exists(originKey)` 为 false 则抛 `BusinessException`（提示上传未完成）
  - 返回 `new FileVO(getUrl(originKey), thumbKey != null ? getUrl(thumbKey) : null)`

### 4. `FileController` 新增 2 个端点

```java
@PostMapping("/presign")  // ApiResponse<PresignVO>
@PostMapping("/confirm")  // ApiResponse<FileVO>
```

沿用现有 `ApiResponse` 包装与 `@Slf4j` / `@Validated` 约定。

**保留现有 `POST /api/file` 不动** —— 头像等路径仍依赖 `FileStorageAdapter.upload(...)`，且 Web 端可能仍在使用。

---

## 前端改动

### 1. 新建 `admin/src/utils/imageCompress.js`

导出 `compressImage(file)` → `{ originBlob, originContentType, thumbBlob }`

压缩规则（**按类型分流，保留现有行为**）：

| 输入类型 | 原图 | 缩略图 |
|---|---|---|
| jpg / jpeg / png / webp | canvas 压缩，最大边 1920、q0.85、输出 jpeg | canvas 生成，最大边 200、q0.8、输出 jpeg |
| **gif** | **不压缩，原样直传**（保留动图） | canvas 生成静态首帧缩略图 |
| 非图片（pdf/docx） | 不处理，原样直传 | 不生成，`thumbUrl` 为空串 |

实现要点：
- 用 `<img>` + `URL.createObjectURL` 解码，浏览器会自动按 EXIF 方向渲染，canvas 因此拿到方向正确的像素，无需手工处理 orientation；用完 `revokeObjectURL`
- `canvas.toBlob(cb, 'image/jpeg', quality)` 取 Blob
- 最大边按 `Math.min(1, maxEdge / Math.max(w, h))` 等比缩放
- gif 判定用 `file.type === 'image/gif'` 或扩展名

### 2. `admin/src/api/index.js` 新增 2 个函数

```js
export const presignUpload = (data) => request({ url: '/file/presign', method: 'post', data })
export const confirmUpload = (data) => request({ url: '/file/confirm', method: 'post', data })
```

### 3. 新建 `admin/src/utils/upload.js`

导出 `uploadImageDirect(file)`，**返回结构与现有 `uploadFile` 完全一致**（`{ code, data: { originUrl, thumbUrl } }`），这样 15 处调用点只换函数名。

```js
export async function uploadImageDirect(file) {
  const packed = await compressImage(file)            // 非图片/失败时降级为原样
  const presignRes = await presignUpload({ filename: ..., contentType: ..., needThumb: !!packed.thumbBlob })
  if (presignRes.code !== 200) return presignRes
  const p = presignRes.data
  await putToCos(p.originUploadUrl, packed.originBlob, packed.originContentType)
  if (packed.thumbBlob) await putToCos(p.thumbUploadUrl, packed.thumbBlob, 'image/jpeg')
  return confirmUpload({ originKey: p.originKey, thumbKey: packed.thumbBlob ? p.thumbKey : null })
}
```

`putToCos` 用**裸 axios**（不经过 `utils/request.js` 的实例）——COS 地址是外域绝对 URL，不能带 `baseURL` 和 `Authorization` 头；上传进度可用 `onUploadProgress`，但本次不接（现有转圈遮罩已够用）。

### 4. 替换 15 处调用点

把各页面的 `await uploadFile(file.file)` 改为 `await uploadImageDirect(file.file)`，并把 import 从 `uploadFile` 换成 `uploadImageDirect`。

涉及文件与行号：

| 文件 | 行 |
|---|---|
| `admin/src/views/article/ArticleList.vue` | 534、572 |
| `admin/src/views/banner/BannerList.vue` | 554 |
| `admin/src/views/drama/DramaList.vue` | 812、852 |
| `admin/src/views/hotel/HotelList.vue` | 663、726 |
| `admin/src/views/location/LocationList.vue` | 403、445 |
| `admin/src/views/policy/PolicyList.vue` | 605 |
| `admin/src/views/report/ReportList.vue` | 489 |
| `admin/src/views/service/ServiceList.vue` | 329、370 |
| `admin/src/views/tour/TourList.vue` | 587 |
| `admin/src/views/tourroute/TourRouteList.vue` | 359 |

**不改**：`admin/src/views/profile/Profile.vue`（走 `uploadAvatar`，本次范围外）。

各页面现有的 `beforeUpload` 校验（格式白名单 + 2MB / 非图片 3MB）**原样保留**，校验发生在压缩之前。

---

## 前置条件（需你在腾讯云控制台完成）

浏览器直传必须给 COS 桶 `filmingfilmsinyaan-1389816353`（ap-guangzhou）配 **CORS 规则**，否则预检 `OPTIONS` 会失败：

- 来源 Origin：`http://localhost:5173`、`https://filmingfilmsinyaan.com`
- 允许方法：`PUT`、`GET`、`HEAD`、`OPTIONS`
- Allow-Headers：`*`（至少含 `Content-Type`）
- Expose-Headers：`ETag`
- 缓存时间：600 秒

另外确认桶的可读权限保持不变（`getUrl` 返回的是不带签名的公开地址，现有逻辑已依赖桶可公读）。

---

## 风险与取舍

1. **原图会被压缩**：COS 里 `origin/` 不再是无损原件，最大边限制在 1920。Web 端详情页展示的即这张图，需确认清晰度可接受（参数可调）。
2. **PNG 透明背景**：canvas 输出 jpeg 会让透明区域变黑。若场地/戏剧封面存在透明 PNG，需改为「PNG 输出 PNG」或给 canvas 填白底。**实施时按「透明 PNG 保留 PNG 输出」处理**以规避。
3. **EXIF 全丢**：拍摄参数不再保留，对当前业务无影响。
4. **上传体积上限**：仍受前端 2MB 校验约束，压缩后实际远低于此。

---

## 验证方式

1. `cd d:\FilmFusion` 启动后端，`cd d:\FilmFusion\admin && npm run dev` 启动管理端
2. **正常图片**：每个模块分别传一张 jpg（含 >1920px 的大图），确认
   - 转圈遮罩出现并在上传完成后消失
   - 图片正常回显，不是裂图
   - 浏览器 Network 里能看到 `PUT https://filmingfilmsinyaan-1389816353.cos.ap-guangzhou.myqcloud.com/...` 返回 200
3. **大图提速对照**：传一张接近 2MB 的图，对比改造前耗时，确认明显下降
4. **GIF**：传一张动图，确认原图仍是动的（未被静态化）
5. **非图片**：报备模块传一个 pdf，确认能正常上传、`thumbUrl` 为空且前端不报错
6. **失败路径**：故意断网或改坏预签名地址，确认 `finally` 里遮罩正常关闭、有错误提示
7. **落库回归**：每个模块上传后点保存，再刷新列表，确认图片 URL 正确持久化
8. 确认头像功能未受影响（走原后端上传链路）