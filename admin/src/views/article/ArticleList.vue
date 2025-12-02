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
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        :scroll-x="1500"
      />
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
          <n-input v-model:value="articleForm.cover" placeholder="请输入封面图片URL" />
        </n-form-item>
        <n-form-item label="缩略封面" path="thumbCover">
          <n-input v-model:value="articleForm.thumbCover" placeholder="请输入缩略封面URL" />
        </n-form-item>
        <n-form-item label="详情图片" path="image">
          <n-input v-model:value="articleForm.image" placeholder="请输入详情图片URL" />
        </n-form-item>
        <n-form-item label="缩略详情图" path="thumbImage">
          <n-input v-model:value="articleForm.thumbImage" placeholder="请输入缩略详情图URL" />
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
  NImage
} from 'naive-ui'
import { getArticlePage, addArticle, updateArticle, deleteArticle, getArticleById } from '@/api'
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

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
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
      if (!row.cover) return '-'
      return h(NImage, {
        width: 60,
        height: 45,
        src: row.cover,
        objectFit: 'cover',
        style: { borderRadius: '4px' }
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
    if (res.code === 200) {
      // PageResponse 直接包含 data 和 pagination
      articleList.value = res.data || []
      pagination.itemCount = res.pagination?.totalItems || 0
    }
  } catch (error) {
    console.error('加载文章列表失败:', error)
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
    content: '',
    cover: '',
    image: '',
    thumbCover: '',
    thumbImage: ''
  })
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
        cover: res.data.cover,
        image: res.data.image,
        thumbCover: res.data.thumbCover,
        thumbImage: res.data.thumbImage
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
  }
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
      cover: articleForm.cover,
      image: articleForm.image,
      thumbCover: articleForm.thumbCover,
      thumbImage: articleForm.thumbImage
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
