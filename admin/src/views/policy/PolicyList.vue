<template>
  <div class="policy-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="政策标题">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入政策标题" clearable @keyup.enter="handleSearch" />
          </n-form-item>
          <n-form-item label="政策类型">
            <n-select v-model:value="searchForm.type" :options="typeOptions" clearable placeholder="请选择政策类型" />
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
            新增政策
          </n-button>
        </div>
      </div>
      
      <n-data-table
        :columns="columns"
        :data="policyList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        :scroll-x="1500"
      />
      
      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-list">
        <n-spin :show="loading">
          <div v-if="policyList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:file-document-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
              v-for="policy in policyList"
              :key="policy.id"
              class="mobile-card"
              hoverable
            >
              <div class="card-header">
                <div class="policy-info">
                  <h3 class="policy-name">{{ policy.title }}</h3>
                  <div class="policy-meta">
                    <n-tag :type="policy.type === '省级' ? 'info' : 'warning'" size="small">
                      {{ policy.type || '-' }}
                    </n-tag>
                    <span class="policy-date">{{ formatDate(policy.issueTime) }}</span>
                  </div>
                </div>
                <div class="policy-cover">
                  <n-image
                    v-if="policy.cover"
                    :src="getImageUrl(policy.cover)"
                    width="80"
                    height="60"
                    object-fit="cover"
                    preview-disabled
                  />
                  <div v-else class="no-cover">
                    <Icon icon="mdi:file-document" :width="32" />
                  </div>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <span class="label">发布单位：</span>
                  <span>{{ policy.issueUnit || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">内容预览：</span>
                  <p class="policy-content-preview">{{ policy.content || '-' }}</p>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(policy)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(policy.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个政策吗？
                </n-popconfirm>
              </div>
            </n-card>
          </div>
          
          <!-- 移动端分页 -->
          <div class="mobile-pagination">
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
    
    <n-modal v-model:show="dialogVisible" preset="dialog" :title="dialogTitle" style="width: 900px">
      <n-form ref="formRef" :model="policyForm" :rules="formRules" label-placement="left" label-width="100">
        <n-form-item label="政策标题" path="title">
          <n-input v-model:value="policyForm.title" placeholder="请输入政策标题" />
        </n-form-item>
        <n-form-item label="政策类型" path="type">
          <n-select v-model:value="policyForm.type" :options="typeOptions" />
        </n-form-item>
        <n-form-item label="发布单位" path="issueUnit">
          <n-input v-model:value="policyForm.issueUnit" placeholder="请输入发布单位" />
        </n-form-item>
        <n-form-item label="发布日期" path="issueDate">
          <n-date-picker v-model:value="policyForm.issueDate" type="date" clearable />
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
          <div v-if="policyForm.cover" style="margin-top: 12px;">
            <n-image
              :src="getImageUrl(policyForm.thumbCover || policyForm.cover)"
              width="200"
              height="120"
              object-fit="cover"
            />
          </div>
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-select v-model:value="policyForm.status" :options="statusOptions" />
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
  NSelect,
  NDatePicker,
  NUpload,
  useMessage,
  NImage,
  NTag
} from 'naive-ui'
import { getPolicyPage, addPolicy, updatePolicy, deletePolicy, getPolicyById, uploadFile } from '@/api'
import { getImageUrl } from '@/utils/image'
import dayjs from 'dayjs'

const message = useMessage()

const loading = ref(false)
const policyList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增政策')
const formRef = ref(null)

const searchForm = reactive({
  keyword: '',
  type: null
})

const policyForm = reactive({
  id: null,
  title: '',
  type: '省级',
  issueUnit: '',
  issueDate: null,
  content: '',
  cover: '',
  image: '',
  thumbCover: '',
  thumbImage: '',
  status: 1
})

const coverFileList = ref([])

const typeOptions = [
  { label: '省级', value: '省级' },
  { label: '市级', value: '市级' }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const typeOptions = [
  { label: '省级', value: '省级' },
  { label: '市级', value: '市级' }
]

const statusOptions = [
  { label: '发布', value: 1 },
  { label: '草稿', value: 0 }
]

const formRules = {
  title: [{ required: true, message: '请输入政策标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择政策类型', trigger: 'change' }],
  issueUnit: [{ required: true, message: '请输入发布单位', trigger: 'blur' }],
  content: [{ required: true, message: '请输入政策内容', trigger: 'blur' }]
}

const columns = [
  { title: 'ID', key: 'id', width: 80, fixed: 'left' },
  { title: '政策标题', key: 'title', width: 200, ellipsis: { tooltip: true }, fixed: 'left' },
  {
    title: '封面',
    key: 'cover',
    width: 100,
    render: (row) => {
      // 优先使用缩略封面，如果没有则使用封面、缩略图或原图
      const coverUrl = row.thumbCover || row.cover || row.thumbImage || row.image
      if (!coverUrl) return '-'
      
      const originalUrl = row.cover || row.image || coverUrl
      
      return h(NImage, {
        width: 60,
        height: 45,
        src: getImageUrl(coverUrl),
        objectFit: 'cover',
        style: { borderRadius: '4px' },
        previewDisabled: false,
        srcset: [
          {
            src: getImageUrl(originalUrl),
            alt: '政策封面'
          }
        ]
      })
    }
  },
  { title: '政策类型', key: 'type', width: 120 },
  { title: '发布单位', key: 'issueUnit', width: 150, ellipsis: { tooltip: true } },
  { title: '政策内容', key: 'content', width: 300, ellipsis: { tooltip: true } },
  {
    title: '发布日期',
    key: 'issueDate',
    width: 120,
    render: (row) => row.issueDate ? dayjs(row.issueDate).format('YYYY-MM-DD') : '-'
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const isActive = row.status === 1
      return h(NTag, { 
        type: isActive ? 'success' : 'default', 
        size: 'small' 
      }, { 
        default: () => isActive ? '发布' : '草稿' 
      })
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
    const res = await getPolicyPage(pagination.page, pagination.pageSize, searchForm.keyword, searchForm.type)
    
    console.log('完整响应数据:', res)
    console.log('res.code:', res.code)
    console.log('res.data:', res.data)
    console.log('res.pagination:', res.pagination)
    
    if (res.code === 200) {
      policyList.value = res.data || []
      pagination.itemCount = res.pagination?.totalItems || 0
    }
  } catch (error) {
    console.error('加载政策列表失败:', error)
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
  searchForm.type = null
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
  dialogTitle.value = '新增政策'
  Object.assign(policyForm, {
    id: null,
    title: '',
    type: '省级',
    issueUnit: '',
    issueDate: null,
    content: '',
    cover: '',
    image: '',
    thumbCover: '',
    thumbImage: '',
    status: 1
  })
  coverFileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getPolicyById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑政策'
      // 后端现在返回 cover 和 thumbCover 字段（已添加），直接使用
      Object.assign(policyForm, {
        id: res.data.id,
        title: res.data.title || '',
        type: res.data.type || '省级',
        issueUnit: res.data.issueUnit || '',
        issueTime: res.data.issueTime || null,
        content: res.data.content || '',
        cover: res.data.cover || res.data.image || '',
        image: res.data.image || '',
        thumbCover: res.data.thumbCover || res.data.thumbImage || '',
        thumbImage: res.data.thumbImage || '',
        status: res.data.status || 1
      })
      
      // 设置封面图片文件列表
      if (res.data.cover || res.data.thumbCover) {
        coverFileList.value = [{
          id: 'cover',
          name: 'cover.jpg',
          status: 'finished',
          url: res.data.thumbCover || res.data.cover
        }]
      } else {
        coverFileList.value = []
      }
      
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取政策详情失败:', error)
    message.error('获取政策详情失败')
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
    // DTO只支持 image 和 thumbImage，将 cover 映射到 image，thumbCover 映射到 thumbImage
    const data = {
      title: policyForm.title,
      type: policyForm.type,
      issueUnit: policyForm.issueUnit,
      issueDate: policyForm.issueDate,
      content: policyForm.content,
      image: policyForm.cover || policyForm.image, // 封面图片映射到 image
      thumbImage: policyForm.thumbCover || policyForm.thumbImage // 缩略封面映射到 thumbImage
    }
    
    let res
    if (policyForm.id) {
      res = await updatePolicy({ ...data, id: policyForm.id })
    } else {
      res = await addPolicy(data)
    }
    
    if (res.code === 200) {
      message.success(policyForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    dialogLoading.value = false
  }
}

// 处理封面图片上传
const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl
      
      policyForm.cover = originUrl
      policyForm.thumbCover = thumbUrl
      
      // 更新文件列表中的URL，用于预览显示
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

// 处理封面文件列表变化
const handleCoverFileListChange = (files) => {
  coverFileList.value = files
  if (files.length === 0) {
    policyForm.cover = ''
    policyForm.thumbCover = ''
  }
}

const handleDelete = async (id) => {
  try {
    const res = await deletePolicy(id)
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
.policy-management {
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
}

.search-form {
  flex: 1;
}

.action-buttons {
  display: flex;
  align-items: center;
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

