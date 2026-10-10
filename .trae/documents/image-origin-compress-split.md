# 图片「原图 / 压缩图」字段语义修正

## Context

C 端用户反馈上传后的图片不清晰。排查后确认根因不是压缩参数问题，而是**字段语义与实现长期不一致**。

数据库每个业务实体本来就有两列，注释也是对的，例如 `Drama`（[Drama.java](file:///d:/FilmFusion/src/main/java/cn/cxdproject/coder/model/entity/Drama.java#L99-L109)）：

| 字段 | 注释本意 | 当前实际存的内容 |
|---|---|---|
| `image` | 原图 url | **压缩到 1920px 的图** |
| `thumb_image` | 压缩后图片 url | **200×200 缩略图** |

更关键的是小程序的展示逻辑：绝大多数页面**优先读 `thumbImage`**，导致用户看到的永远是 200×200 的图——这才是"不清晰"的直接原因。

| 上传来源 | `image` 实际内容 | `thumb_image` 实际内容 | 小程序读到 |
|---|---|---|---|
| 管理端（预签名直传） | 1920 压缩图 | 200px 缩略图 | `thumbImage` → 200px，极模糊 |
| 小程序（`POST /api/file`） | 原图 | 200px 缩略图 | `thumbImage` → 200px，极模糊 |

**目标**：`image` 存真正的未压缩原图，`thumb_image` 存压缩图，**前端展示统一读原图**，压缩图仅落库备用。

---

## 改造方案

### A. 后端：老上传链路生成「压缩图」而非缩略图

小程序走 `POST /api/file`，由后端用 Thumbnailator 生成缩略图，参数需要从「200px 缩略图」改为「1920 压缩图」。

1. **`src/main/java/cn/cxdproject/coder/common/storage/OssStorageService.java`**
   - `upload(MultipartFile)`（L47-L72）与 `upload(String prefix, MultipartFile)`（L74-L100）：方法内变量名 `thumbnailUrl` 语义不变（仍对应 `thumb_image` 字段），无需改结构。
   - `uploadThumbnail`（L271-L289）：`size(200,200)` → `size(1920,1920)`，`outputQuality(0.8f)` → `outputQuality(0.85f)`。`keepAspectRatio(true)` + `outputFormat("jpg")` 保持不变。
   - `isWebp` 跳过逻辑保留：Thumbnailator 无法解码 WEBP，这类文件 `thumb_image` 仍为空。

2. **`LocalStorageService.java`**
   - `generateLocalThumbnail`（L325-L331）：同样改为 `size(1920,1920)` + `outputQuality(0.85f)`。

3. **`FileServiceImpl.java`**
   - 预签名链路（L45-L75）逻辑**无需改动**，后端只签发地址、不关心内容。
   - 仅把 `THUMB_PREFIX` 的注释由"缩略图对象目录前缀"改为"压缩图对象目录前缀"，避免后续再次产生语义漂移。
   - 对象键前缀继续用 `origin/` 和 `thumb/`，与数据库字段名 `thumb_image` 保持一致，避免改动 `confirm()` 的目录校验和线上已有 URL。

### B. 管理端：直传原始文件 + 生成压缩图

4. **`admin/src/utils/imageCompress.js`**
   - 常量调整：`THUMB_MAX_EDGE = 200` → `COMPRESS_MAX_EDGE = 1920`；`THUMB_QUALITY = 0.8` → `COMPRESS_QUALITY = 0.85`（与后端压缩图参数对齐，统一走 `ORIGIN_MAX_EDGE`/`ORIGIN_QUALITY` 即可，无需新增常量）。
   - `compressImage(file)` 返回值语义变更（保持三个字段名不变，减少调用方改动）：
     - `originBlob` ← **原始 `file` 对象**（不再经过 canvas），`originContentType` ← `file.type`，`originFilename` ← `file.name`
     - `thumbBlob` ← 压缩图（1920 / q0.85）
   - `buildThumbnail`（L148-L152）重命名/改写为 `buildCompressed`，尺寸取 1920，其余逻辑复用。
   - 分格式行为：
     - **GIF**：`originBlob = file`（保留动图）；压缩图只能是 canvas 出的静态首帧 JPEG，与现状一致。
     - **PNG**：`originBlob = file`；压缩图仍走 `keepPng` 分支输出 PNG（canvas 重编码对无损格式无体积收益，但尺寸可缩到 1920）。
     - **非图片**：返回 `null`，调用方按原文件直传。
   - `decodeImage` 里的 EXIF 方向校正（`imageOrientation: 'from-image'`）现在只服务于压缩图；原图直接上传，方向由原文件保留，行为正确。

5. **`admin/src/utils/upload.js`**
   - `uploadImageDirect`（L27-L50）：两次 PUT 的结构不变，无需改代码。`originBlob` 自动变为原图。
   - 确认 `originContentType` / `originFilename` 取的是原始文件的值（改造后在 `imageCompress.js` 中已保证），这样 COS 上的原图扩展名和 Content-Type 正确。
   - 管理端各列表页**不需要改动**：它们读取 `thumbUrl` 做列表展示、`originUrl` 做预览，改造后自动变成「列表用压缩图、预览用原图」。

### C. 小程序：展示统一改为读原图

6. 把各页面的图片取值优先级统一为 **`image` 优先，`thumbImage` 兜底**。当前实现不一致，代表位置：

| 文件 | 位置 | 现状 |
|---|---|---|
| `pages/index/index.vue` | L649-L651 | 优先 `thumbImage` |
| `pages/index/index.vue` | L132-L133 | `cover \|\| thumbImage` |
| `pages/news/news.vue` | L191-L193 | 优先 `thumbImage` |
| `pages/services/module-list.vue` | L39-L40 | `thumbImage \|\| image` |
| `pages/services/services.vue` | L66-L67 | `thumbImage \|\| image` |
| `pages/films/detail.vue` | L296-L297 | `cover \|\| image \|\| thumbCover \|\| thumbImage` |
| `pages/tourroute/detail.vue` | L287-L288 | 同上 |
| `pages/article/detail.vue` | L104-L106 | 兜底才用 `thumbImage` |
| `pages/shoot/detail.vue` | L114、L133 | `image \|\| ... \|\| thumbImage`（已正确） |
| `pages/location/detail.vue` | L192、L220 | `image \|\| ... `（已正确） |
| `pages/hotel/detail.vue` | L129、L153 | `image \|\| cover \|\| thumbImage`（已正确） |

改造方式：统一成 `image || thumbImage || cover`（列表卡片可保留 `cover` 作为业务封面字段优先，但把 `thumbImage` 降到 `image` 之后）。`pages/scenes/scenes.vue` L64-L65 同理。

7. **`miniprogram/utils/apiProcessor.ts`** L64-L89 与 **`services/backend-api.ts`** 的 `fileFields` 数组本身不需要改（`thumbImage` 仍需处理 URL 拼接作为兜底），只调整页面展示优先级。

8. 小程序上传走 `POST /api/file`（[backend-api.ts](file:///d:/FilmFusion/miniprogram/services/backend-api.ts#L369-L387)），后端改动后返回的 `originUrl` 是原图，落在 `image` 字段；小程序无需改上传逻辑。

### D. 历史数据对齐（建议）

已有数据的 `image` 是 1920 压缩图、`thumb_image` 是 200px 缩略图。旧原图**已丢失、无法恢复**，但可以让两字段语义与新逻辑一致：

```sql
-- 让 thumb_image 变成"压缩图"语义；image 保持不变（小程序读它，清晰度立刻提升）
UPDATE fi_dramas   SET thumb_image = image WHERE image IS NOT NULL;
-- 其余业务表按 @TableName 逐个确认后同样处理：
-- fi_articles / fi_locations / fi_hotels / fi_policies / fi_shoots / fi_attractions
-- fi_users 对应 avatar / thumb_avatar
-- fi_reports 对应 shoot_permit / thumb_shoot_permit 等多组字段
```

执行前建议先 `SELECT` 抽样核对，并备份。

---

## 已知影响与需要确认的点

1. **5MB 限制**：管理端 `beforeUpload` 校验图片不超过 5MB。改成上传原始文件后，接近上限的原图会更容易被拦。若需要放宽，需同时调整前端校验与 `spring.servlet.multipart.max-file-size`。
2. **管理端列表变重**：去掉 200px 缩略图后，列表读取的是 1920 压缩图（约 200–500KB/张），表格加载会比之前慢。这是本次决策的直接代价。
3. **WEBP 无压缩图**：后端 Thumbnailator 不支持，`thumb_image` 为空；管理端（浏览器 canvas）不受影响，能正常生成。
4. **GIF 的压缩图是静态首帧**：canvas 无法输出动图，与原实现行为一致。

---

## 验证方式

1. **后端（本地存储）**：将存储切到本地模式，通过 `POST /api/file` 上传一张 >1920px 的 JPEG，检查 `origin/` 下是原始字节（体积不变）、`thumb/` 下是 1920 / q0.85 的 JPEG（体积明显更小）。
2. **管理端**：上传一张大图，浏览器 Network 中确认两次 PUT 分别发送原始文件与压缩图；检查 `confirm` 返回的 `originUrl` 指向的 COS 对象体积等于原文件。
3. **数据库**：确认 `image` 存原图路径、`thumb_image` 存压缩图路径。
4. **小程序**：上传后进入详情页与首页列表，肉眼确认清晰度相比改造前明显提升；用开发者工具确认请求的图片体积对应原图。
5. **回归**：GIF 上传后动图仍可播放；非图片附件上传流程不受影响；WEBP 上传不报错。