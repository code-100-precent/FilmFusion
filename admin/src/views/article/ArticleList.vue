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

      <n-data-table
          :columns="columns"
          :data="articleList"
          :loading="loading"
          :row-key="row => row.id"
          :scroll-x="1500"
      />

      <!-- 独立分页组件 -->
      <div class="pagination-container" v-if="pagination.totalItems > 0">
        <n-pagination
            v-model:page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :page-count="Math.ceil(pagination.totalItems / pagination.pageSize)"
            :item-count="pagination.totalItems"
            :page-sizes="pagination.pageSizes"
            show-size-picker
            show-quick-jumper
            @update:page="handlePageChange"
            @update:page-size="handlePageSizeChange"
        />
      </div>
    </n-card>

    <n-modal v-model:show="dialogVisible" preset="dialog" :title="dialogTitle" style="width: 90%; max-width: 800px">
      <n-form ref="formRef" :model="articleForm" :rules="formRules" label-placement="left" label-width="100">
        <n-form-item label="文章标题" path="title">
          <n-input v-model:value="articleForm.title" placeholder="请输入文章标题" />
        </n-form-item>
        <n-form-item label="发布单位" path="issueUnit">
          <n-input v-model:value="articleForm.issueUnit" placeholder="请输入发布单位" />
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
          >
            <n-button>上传封面图片</n-button>
          </n-upload>
          <div v-if="articleForm.cover" style="margin-top: 12px;">
            <n-image
                :src="getImageUrl(articleForm.thumbCover || articleForm.cover)"
                width="200"
                height="120"
                object-fit="cover"
            />
          </div>
        </n-form-item>
        <n-form-item label="详情图片" path="image">
          <n-upload
              :max="10"
              :file-list="imageFileList"
              @update:file-list="handleImageFileListChange"
              :custom-request="handleImageUpload"
              accept="image/*"
              multiple
          >
            <n-button>上传详情图片（可多张）</n-button>
          </n-upload>
          <div v-if="imageFileList.length > 0" style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap;">
            <n-image
                v-for="(file, index) in imageFileList"
                :key="index"
                :src="getImageUrl(file.url)"
                width="100"
                height="75"
                object-fit="cover"
                style="border-radius: 4px;"
            />
          </div>
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
import { ref, reactive, h, onMounted } from 'vue'
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
  NDatePicker,
  useMessage,
  useDialog,
  NImage,
  NUpload,
  NPagination
} from 'naive-ui'
import { getArticlePage, addArticle, updateArticle, deleteArticle, getArticleById, uploadFile } from '@/api'
import { getImageUrl } from '@/utils/image'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()

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
  content: '',
  cover: '',
  image: '',
  thumbCover: '',
  thumbImage: ''
})

const coverFileList = ref([])
const imageFileList = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 10,
  totalItems: 0,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  issueUnit: [{ required: true, message: '请输入发布单位', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

const columns = [
  { title: 'ID', key: 'id', width: 80, fixed: 'left' },
  { title: '文章标题', key: 'title', width: 200, ellipsis: { tooltip: true }, fixed: 'left' },
  {
    title: '封面',
    key: 'cover',
    width: 100,
    render: (row) => {
      // 尝试多种可能的封面字段
      let coverUrl = null

      // 按优先级尝试不同的字段
      if (row.thumbImage) {
        coverUrl = row.thumbImage
      } else if (row.image) {
        coverUrl = row.image
      } else if (row.thumbCover) {
        coverUrl = row.thumbCover
      } else if (row.cover) {
        coverUrl = row.cover
      }

      // 如果是数组，取第一个元素
      if (Array.isArray(coverUrl)) {
        coverUrl = coverUrl[0]
      }

      // 如果是逗号分隔的字符串，取第一个
      if (typeof coverUrl === 'string' && coverUrl.includes(',')) {
        coverUrl = coverUrl.split(',')[0].trim()
      }

      // 检查 URL 是否有效
      if (!coverUrl || coverUrl === '') {
        return '-'
      }

      // 确保 URL 是有效的
      const imageUrl = getImageUrl(coverUrl)
      if (!imageUrl) {
        return '-'
      }

      // 调试日志
      console.log('封面图片URL:', imageUrl, '原始数据:', row)

      return h(NImage, {
        width: 60,
        height: 45,
        src: imageUrl,
        objectFit: 'cover',
        style: { borderRadius: '4px' },
        previewDisabled: false,
        // 添加错误处理
        onError: (e) => {
          console.error('封面图片加载失败:', e)
        }
      })
    }
  },
  { title: '发布单位', key: 'issueUnit', width: 150, ellipsis: { tooltip: true } },
  { title: '文章内容', key: 'content', width: 300, ellipsis: { tooltip: true } },
  {
    title: '发布时间',
    key: 'issueTime',
    width: 180,
    render: (row) => {
      if (Array.isArray(row.issueTime)) {
        return dayjs(row.issueTime[0] + '-' + String(row.issueTime[1]).padStart(2, '0') + '-' + String(row.issueTime[2]).padStart(2, '0')).format('YYYY-MM-DD HH:mm:ss')
      }
      return row.issueTime ? dayjs(row.issueTime).format('YYYY-MM-DD HH:mm:ss') : '-'
    }
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
  loadData()
})

const loadData = async () => {
  try {
    loading.value = true
    const res = await getArticlePage(pagination.page, pagination.pageSize, searchForm.keyword)
    console.log('API响应:', res)

    if (res.code === 200) {
      articleList.value = res.data || []
      pagination.totalItems = res.pagination?.totalItems || 0
      console.log('设置的总数:', pagination.totalItems, '当前页:', pagination.page, '每页:', pagination.pageSize)

      // 调试：输出第一页和当前页的数据结构
      if (pagination.page === 1) {
        console.log('第一页数据结构示例:', res.data[0])
      } else {
        console.log(`第${pagination.page}页数据结构示例:`, res.data[0])
      }
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
  console.log('页码变化:', page)
  pagination.page = page
  loadData()
}

const handlePageSizeChange = (pageSize) => {
  console.log('页面大小变化:', pageSize)
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
    content: '',
    cover: '',
    image: '',
    thumbCover: '',
    thumbImage: ''
  })
  coverFileList.value = []
  imageFileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getArticleById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑文章'
      Object.assign(articleForm, {
        id: res.data.id,
        title: res.data.title,
        issueUnit: res.data.issueUnit,
        content: res.data.content,
        cover: res.data.image || res.data.cover,
        image: res.data.image,
        thumbCover: res.data.thumbImage || res.data.thumbCover,
        thumbImage: res.data.thumbImage
      })

      if (res.data.image || res.data.thumbImage) {
        coverFileList.value = [{
          id: 'cover',
          name: 'cover.jpg',
          status: 'finished',
          url: res.data.thumbImage || res.data.image
        }]
      } else {
        coverFileList.value = []
      }

      if (res.data.image) {
        const imageUrls = res.data.image.split(',').filter(url => url.trim())
        imageFileList.value = imageUrls.map((url, index) => ({
          id: `image-${index}`,
          name: `image-${index}.jpg`,
          status: 'finished',
          url: url.trim()
        }))
      } else {
        imageFileList.value = []
      }

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

      const fileIndex = coverFileList.value.findIndex(f => f.id === file.id || f.name === file.name)
      if (fileIndex !== -1) {
        coverFileList.value[fileIndex].url = thumbUrl
        coverFileList.value[fileIndex].status = 'finished'
      } else {
        coverFileList.value.push({
          id: file.id || 'cover',
          name: file.name || 'cover.jpg',
          status: 'finished',
          url: thumbUrl
        })
      }

      onFinish({
        url: thumbUrl
      })
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

const handleImageUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl

      const existingImages = articleForm.image ? articleForm.image.split(',').filter(url => url.trim()) : []
      existingImages.push(originUrl)
      articleForm.image = existingImages.join(',')

      const fileIndex = imageFileList.value.findIndex(f => f.id === file.id || f.name === file.name)
      if (fileIndex !== -1) {
        imageFileList.value[fileIndex].url = thumbUrl
        imageFileList.value[fileIndex].status = 'finished'
      } else {
        imageFileList.value.push({
          id: file.id || `image-${Date.now()}`,
          name: file.name || 'image.jpg',
          status: 'finished',
          url: thumbUrl
        })
      }

      onFinish({
        url: thumbUrl
      })
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

const handleCoverFileListChange = (files) => {
  coverFileList.value = files
  if (files.length === 0) {
    articleForm.cover = ''
    articleForm.thumbCover = ''
  }
}

const handleImageFileListChange = (files) => {
  imageFileList.value = files
  const imageUrls = files
      .filter(file => file.status === 'finished' && file.url)
      .map(file => file.url)
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
    const data = {
      title: articleForm.title,
      issueUnit: articleForm.issueUnit,
      content: articleForm.content,
      image: articleForm.cover || articleForm.image,
      thumbImage: articleForm.thumbCover || articleForm.thumbImage
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

  :deep(.n-data-table) {
    .n-data-table-wrapper {
      overflow-x: auto;
    }
  }

  .pagination-container {
    flex-wrap: wrap;
  }
}

.action-bar {
  margin-bottom: 16px;
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