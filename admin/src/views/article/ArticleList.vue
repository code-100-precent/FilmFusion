<template>
  <div class="article-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="文章标题">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入文章标题" clearable @keyup.enter="handleSearch" />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleSearch">
              <template #icon>
                <Icon icon="mdi:magnify" />
              </template>
              搜索
            </n-button>
            <n-button @click="handleReset" style="margin-left: 12px">重置</n-button>
          </n-form-item>
        </n-form>
        <div class="action-buttons">
          <n-button type="primary" @click="handleAdd">
            <template #icon>
              <Icon icon="mdi:plus" />
            </template>
            新增文章
          </n-button>
        </div>
      </div>

      <!-- 桌面端表格 -->
      <template v-if="!isMobile">
        <n-data-table
            :columns="columns"
            :data="articleList"
            :loading="loading"
            :row-key="row => row.id"
            :scroll-x="1400"
        />

        <!-- 独立分页组件 -->
        <div class="pagination-container" v-if="pagination.itemCount > 0">
          <n-pagination
              v-model:page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :page-count="Math.ceil(pagination.itemCount / pagination.pageSize)"
              :item-count="pagination.itemCount"
              :page-sizes="pagination.pageSizes"
              show-size-picker
              show-quick-jumper
              @update:page="handlePageChange"
              @update:page-size="handlePageSizeChange"
          />
        </div>
      </template>

      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-list">
        <n-spin :show="loading">
          <div v-if="articleList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:file-document-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
                v-for="article in articleList"
                :key="article.id"
                class="mobile-card"
                hoverable
            >
              <div class="card-header">
                <div class="article-info">
                  <h3 class="article-title">{{ article.title }}</h3>
                  <p class="article-unit">{{ article.issueUnit }}</p>
                </div>
                <div class="article-cover">
                  <n-image
                      v-if="getCover(article, 'thumb')"
                      :src="getImageUrl(getCover(article, 'thumb'))"
                      :preview-src="getImageUrl(getCover(article, 'origin'))"
                      width="80"
                      height="60"
                      object-fit="cover"
                  />
                  <div v-else class="no-cover">
                    <Icon icon="mdi:file-image" :width="32" />
                  </div>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <span class="label">发布时间：</span>
                  <span>{{ formatDate(article.issueTime || article.issue_time) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">内容摘要：</span>
                  <span class="content-preview">{{ article.content ? article.content.substring(0, 30) + '...' : '-' }}</span>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(article)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(article.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这篇文章吗？
                </n-popconfirm>
              </div>
            </n-card>
          </div>

          <!-- 移动端分页 -->
          <div class="mobile-pagination" v-if="pagination.itemCount > 0">
            <n-pagination
                :page="pagination.page"
                :page-size="pagination.pageSize"
                :item-count="pagination.itemCount"
                :page-sizes="[10, 20, 50]"
                show-size-picker
                @update:page="handlePageChange"
                @update:page-size="handlePageSizeChange"
            />
          </div>
        </n-spin>
      </div>
    </n-card>

    <n-modal
        v-model:show="dialogVisible"
        preset="dialog"
        :title="dialogTitle"
        style="width: 90%; max-width: 800px"
        :mask-closable="false"
    >
      <n-form
          ref="formRef"
          :model="articleForm"
          :rules="formRules"
          :label-placement="isMobile ? 'top' : 'left'"
          :label-width="isMobile ? 'auto' : '100'"
      >
        <n-form-item label="文章标题" path="title">
          <n-input v-model:value="articleForm.title" placeholder="请输入文章标题" />
        </n-form-item>
        <n-form-item label="发布单位" path="issueUnit">
          <n-input v-model:value="articleForm.issueUnit" placeholder="请输入发布单位" />
        </n-form-item>
        <n-form-item label="发布时间" path="issueTime">
          <n-date-picker v-model:value="articleForm.issueTime" type="datetime" clearable style="width: 100%" />
        </n-form-item>
        <n-form-item label="文章内容" path="content">
          <n-input v-model:value="articleForm.content" type="textarea" :rows="10" placeholder="请输入文章内容" />
        </n-form-item>
        <n-form-item label="封面图片" path="cover">
          <n-upload
              :max="1"
              :file-list="coverFileList"
              @update:file-list="handleCoverFileListChange"
              :custom-request="handleCoverUpload"
              accept="image/*"
              list-type="image-card"
          >
            点击上传封面
          </n-upload>
        </n-form-item>
        <n-form-item label="详情图片" path="image">
          <n-upload
              v-model:file-list="detailFileList"
              @update:file-list="handleDetailFileListChange"
              :custom-request="handleDetailUpload"
              accept="image/*"
              list-type="image-card"
              multiple
          >
            点击上传详情图
          </n-upload>
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="dialogVisible = false">取消</n-button>
        <n-button type="primary" @click="handleDialogSave" :loading="dialogLoading">保存</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, h, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NButton,
  NForm,
  NFormItem,
  NInput,
  NDataTable,
  NPopconfirm,
  NModal,
  useMessage,
  NImage,
  NUpload,
  NPagination,
  NSpin,
  NDatePicker
} from 'naive-ui'
import { useUserStore } from '@/store/user'
import { getArticlePage, addArticle, updateArticle, deleteArticle, getArticleById, uploadFile } from '@/api'
import { getImageUrl } from '@/utils/image'
import config from '@/config'
import dayjs from 'dayjs'

const message = useMessage()
const userStore = useUserStore()

const isMobile = ref(false)
const loading = ref(false)
const articleList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增文章')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const articleForm = reactive({
  id: null,
  title: '',
  issueUnit: '',
  issueTime: null,
  content: '',
  image: '', // 存储详情图片
  thumbImage: '',
  cover: '', // 存储封面图片
  thumbCover: '',
  userId: null
})

const coverFileList = ref([])
const detailFileList = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  issueUnit: [{ required: true, message: '请输入发布单位', trigger: 'blur' }],
  issueTime: [{ required: true, message: '请选择发布时间', trigger: 'change', type: 'number' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const formatDate = (date) => {
  if (!date) return '-'
  if (Array.isArray(date)) {
    return dayjs(date[0] + '-' + String(date[1]).padStart(2, '0') + '-' + String(date[2]).padStart(2, '0')).format('YYYY-MM-DD HH:mm:ss')
  }
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const getCover = (article, type = 'thumb') => {
  const thumbStr = article.thumbImage || article.thumb_image || ''
  const originStr = article.image || ''
  
  if (type === 'thumb') {
    return thumbStr ? thumbStr.split(',')[0] : (originStr ? originStr.split(',')[0] : '')
  } else {
    return originStr ? originStr.split(',')[0] : (thumbStr ? thumbStr.split(',')[0] : '')
  }
}

const columns = [
  { title: 'ID', key: 'id', width: 80, fixed: 'left' },
  { title: '文章标题', key: 'title', width: 200, ellipsis: { tooltip: true }, fixed: 'left' },
  {
    title: '图片',
    key: 'image',
    width: 100,
    render: (row) => {
      const thumbStr = row.thumbImage || row.thumb_image || ''
      const originStr = row.image || ''
      
      // 优先展示缩略图，如果没有缩略图则展示原图
      const firstThumb = thumbStr ? thumbStr.split(',')[0] : (originStr ? originStr.split(',')[0] : '')
      // 预览时展示原图，如果没有原图则展示缩略图
      const firstOrigin = originStr ? originStr.split(',')[0] : (thumbStr ? thumbStr.split(',')[0] : '')

      if (!firstThumb) return '-'

      return h(NImage, {
        width: 60,
        height: 45,
        src: getImageUrl(firstThumb),
        previewSrc: getImageUrl(firstOrigin),
        objectFit: 'cover',
        style: { borderRadius: '4px' },
        previewDisabled: false
      })
    }
  },
  { title: '发布单位', key: 'issueUnit', width: 150, ellipsis: { tooltip: true } },
  {
    title: '发布时间',
    key: 'issueTime',
    width: 180,
    render: (row) => formatDate(row.issueTime || row.issue_time)
  },
  { title: '文章内容', key: 'content', width: 300, ellipsis: { tooltip: true } },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: (row) => formatDate(row.createdAt || row.created_at)
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) => {
      return h('div', { style: 'display: flex; gap: 8px;' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
        h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row.id) },
            {
              trigger: () => h(NButton, { size: 'small', type: 'error', quaternary: true }, { default: () => '删除' })
            }
        )
      ])
    }
  }
]

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

const loadData = async () => {
  try {
    loading.value = true
    const res = await getArticlePage(pagination.page, pagination.pageSize, searchForm.keyword)
    
    if (res.code === 200) {
      articleList.value = res.data?.records || res.data || []
      pagination.itemCount = res.data?.total || res.total || res.pagination?.totalItems || 0
    } else {
      message.error(res.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载文章列表失败:', error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  pagination.page = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增文章'
  Object.assign(articleForm, {
    id: null,
    title: '',
    issueUnit: '',
    issueTime: Date.now(),
    content: '',
    image: '',
    thumbImage: '',
    cover: '',
    thumbCover: '',
    userId: userStore.userInfo?.id || null
  })
  coverFileList.value = []
  detailFileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getArticleById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑文章'
      const data = res.data

      // 解析图片字段：第一个为封面，后面的是详情图
      const allImages = (data.image || '').split(',').filter(url => url.trim())
      const coverUrl = allImages[0] || ''
      const detailUrls = allImages.slice(1)
      
      // 解析缩略图字段
      const allThumbImages = (data.thumbImage || '').split(',').filter(url => url.trim())
      const thumbCoverUrl = allThumbImages[0] || coverUrl
      const detailThumbUrls = allThumbImages.slice(1)

      Object.assign(articleForm, {
        id: data.id,
        title: data.title,
        issueUnit: data.issueUnit || data.issue_unit,
        issueTime: data.issueTime ? new Date(data.issueTime).getTime() : (data.issue_time ? new Date(data.issue_time).getTime() : Date.now()),
        content: data.content,
        image: detailUrls.join(','), // 详情图片字符串
        thumbImage: '', // 暂时置空，保存时重新计算
        cover: coverUrl,
        thumbCover: thumbCoverUrl,
        userId: data.userId || data.user_id
      })

      // 封面
      if (coverUrl) {
        coverFileList.value = [{
          id: 'cover',
          name: 'cover.jpg',
          status: 'finished',
          url: getImageUrl(thumbCoverUrl || coverUrl),
          originUrl: coverUrl,
          thumbUrl: thumbCoverUrl || coverUrl
        }]
      } else {
        coverFileList.value = []
      }

      // 详情
      detailFileList.value = detailUrls.map((url, index) => {
        const thumb = detailThumbUrls[index] || url
        return {
          id: `detail-${index}`,
          name: `detail-${index}.jpg`,
          status: 'finished',
          url: getImageUrl(thumb),
          originUrl: url,
          thumbUrl: thumb
        }
      })

      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
  }
}

const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl
      
      articleForm.cover = originUrl
      articleForm.thumbCover = thumbUrl
      
      const fullUrl = getImageUrl(originUrl)
      file.url = fullUrl
      file.originUrl = originUrl
      file.thumbUrl = thumbUrl
      
      const fileIndex = coverFileList.value.findIndex(f => f.id === file.id)
      if (fileIndex !== -1) {
        coverFileList.value[fileIndex].url = fullUrl
        coverFileList.value[fileIndex].originUrl = originUrl
        coverFileList.value[fileIndex].thumbUrl = thumbUrl
      }

      onFinish()
      message.success('封面图片上传成功')
    } else {
      onError()
      message.error('上传失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('上传封面图片失败:', error)
    onError()
    message.error('上传失败')
  }
}

const handleDetailUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl
      
      const fullUrl = getImageUrl(originUrl)
      file.url = fullUrl
      file.originUrl = originUrl
      file.thumbUrl = thumbUrl
      
      const fileIndex = detailFileList.value.findIndex(f => f.id === file.id)
      if (fileIndex !== -1) {
        detailFileList.value[fileIndex].url = fullUrl
        detailFileList.value[fileIndex].originUrl = originUrl
        detailFileList.value[fileIndex].thumbUrl = thumbUrl
      }

      const existingImages = articleForm.image ? articleForm.image.split(',').filter(url => url.trim()) : []
      existingImages.push(originUrl)
      articleForm.image = existingImages.join(',')
      
      onFinish()
      message.success('详情图片上传成功')
    } else {
      onError()
      message.error('上传失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('上传详情图片失败:', error)
    onError()
    message.error('上传失败')
  }
}

// 处理封面文件列表变化
const handleCoverFileListChange = (files) => {
  coverFileList.value = files
  if (files.length === 0) {
    articleForm.cover = ''
    articleForm.thumbCover = ''
  }
}

// 处理详情图片文件列表变化
const handleDetailFileListChange = (files) => {
  detailFileList.value = files
  const imageUrls = files
      .filter(file => file.status === 'finished' && file.url)
      .map(file => {
        if (file.url.startsWith('http')) {
          return file.url.replace(config.fileBaseURL, '')
        }
        return file.url
      })
  articleForm.image = imageUrls.join(',')
}

const handleDialogSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  try {
    dialogLoading.value = true
    
    // 组合图片字段：封面 + 详情图
    // 优先使用 fileList 中的 originUrl (相对路径)，如果没有则尝试从 url 解析
    const detailOrigins = detailFileList.value
        .filter(f => f.status === 'finished')
        .map(f => {
          if (f.originUrl) return f.originUrl
          if (f.url && f.url.startsWith('http')) return f.url.replace(config.fileBaseURL, '')
          return f.url
        })
        
    const allImages = []
    
    // 获取封面原图（优先从文件列表获取）
    let coverOrigin = articleForm.cover
    const coverFile = coverFileList.value.find(f => f.status === 'finished')
    if (coverFile) {
      if (coverFile.originUrl) {
        coverOrigin = coverFile.originUrl
      } else if (coverFile.url && coverFile.url.startsWith('http')) {
        coverOrigin = coverFile.url.replace(config.fileBaseURL, '')
      } else {
        coverOrigin = coverFile.url
      }
    } else if (coverOrigin && coverOrigin.startsWith('http')) {
      // 兜底：如果 articleForm.cover 是完整路径
      coverOrigin = coverOrigin.replace(config.fileBaseURL, '')
    }

    if (coverOrigin) allImages.push(coverOrigin)
    if (detailOrigins.length > 0) allImages.push(...detailOrigins)
    
    const finalImageStr = allImages.join(',')
    
    // 组合缩略图字段
    // 优先使用 fileList 中的 thumbUrl (相对路径)，如果没有则回退到 originUrl 或 url
    const detailThumbs = detailFileList.value
        .filter(f => f.status === 'finished')
        .map(f => {
          if (f.thumbUrl) return f.thumbUrl
          if (f.originUrl) return f.originUrl
          if (f.url && f.url.startsWith('http')) return f.url.replace(config.fileBaseURL, '')
          return f.url
        })

    const allThumbImages = []
    
    // 获取封面缩略图（优先从文件列表获取）
    let coverThumb = articleForm.thumbCover || articleForm.cover
    if (coverFile) {
      if (coverFile.thumbUrl) {
        coverThumb = coverFile.thumbUrl
      } else if (coverFile.originUrl) {
        coverThumb = coverFile.originUrl
      }
    }
    
    // 兜底处理
    if (coverThumb && coverThumb.startsWith('http')) {
       coverThumb = coverThumb.replace(config.fileBaseURL, '')
    }

    if (coverThumb) allThumbImages.push(coverThumb)
    if (detailThumbs.length > 0) allThumbImages.push(...detailThumbs)
    
    const finalThumbImageStr = allThumbImages.join(',')
    
    const data = {
      title: articleForm.title,
      issueUnit: articleForm.issueUnit,
      issueTime: dayjs(articleForm.issueTime).format('YYYY-MM-DD HH:mm:ss'),
      content: articleForm.content,
      image: finalImageStr,
      thumbImage: finalThumbImageStr,
      userId: articleForm.userId
    }

    let res
    if (articleForm.id) {
      res = await updateArticle({ ...data, id: articleForm.id })
    } else {
      res = await addArticle(data)
    }

    if (res.code === 200) {
      message.success(articleForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
  } finally {
    dialogLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    const res = await deleteArticle(id)
    if (res.code === 200) {
      message.success('删除成功')
      loadData()
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}
</script>

<style scoped lang="scss">
.article-management {
  animation: fadeIn 0.3s ease;
}

.management-card {
  :deep(.n-card__content) {
    padding: 16px;
  }
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
}

.search-form {
  flex: 1;
  min-width: 300px;
}

.action-buttons {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
}

// 移动端适配
.mobile-list {
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 48px 0;
  }

  .card-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .mobile-card {
    :deep(.n-card__content) {
      padding: 12px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;

    .article-info {
      flex: 1;
      margin-right: 12px;

      .article-title {
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 4px 0;
        line-height: 1.4;
      }

      .article-unit {
        font-size: 13px;
        color: #6b7280;
        margin: 0;
      }
    }

    .article-cover {
      flex-shrink: 0;
      width: 80px;
      height: 60px;
      border-radius: 4px;
      overflow: hidden;
      background-color: #f3f4f6;
      
      .no-cover {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #9ca3af;
      }
    }
  }

  .card-content {
    margin-bottom: 12px;
    font-size: 13px;
    color: #4b5563;

    .info-item {
      display: flex;
      margin-bottom: 4px;

      .label {
        color: #9ca3af;
        min-width: 70px;
      }
    }
  }

  .mobile-pagination {
    margin-top: 16px;
    display: flex;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .search-header {
    flex-direction: column;

    .search-form {
      width: 100%;
      min-width: auto;
    }

    .action-buttons {
      width: 100%;

      button {
        flex: 1;
      }
    }
  }

  .management-card {
    :deep(.n-card__content) {
      padding: 12px;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
