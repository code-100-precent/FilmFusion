<template>
  <div class="policy-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="政策标题">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入政策标题" clearable @keyup.enter="handleSearch" />
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

      <!-- 桌面端表格 -->
      <n-data-table
          v-if="!isMobile"
          :columns="columns"
          :data="policyList"
          :loading="loading"
          :pagination="{
            page: currentPage,
            pageSize: pageSize,
            itemCount: totalItems,
            showSizePicker: true,
            pageSizes: [5, 10, 20, 50],
            onUpdatePage: handlePageChange,
            onUpdatePageSize: handlePageSizeChange,
            prefix: (info) => `共 ${info.itemCount} 条`
          }"
          :row-key="row => row.id"
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
                      v-if="policy.image"
                      :src="getImageUrl(policy.thumbImage || policy.image)"
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
                  <p class="policy-content-preview">{{ policy.content?.substring(0, 50) + (policy.content?.length > 50 ? '...' : '') || '-' }}</p>
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
                :page="currentPage"
                :page-size="pageSize"
                :item-count="totalItems"
                :page-sizes="[5, 10, 20, 50]"
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
        style="width: 90%; max-width: 900px"
        :mask-closable="false"
    >
      <n-form
          ref="formRef"
          :model="policyForm"
          :rules="formRules"
          :label-placement="isMobile ? 'top' : 'left'"
          :label-width="isMobile ? 'auto' : '120'"
      >
        <n-form-item label="政策标题" path="title">
          <n-input v-model:value="policyForm.title" placeholder="请输入政策标题" />
        </n-form-item>
        <n-form-item label="政策类型" path="type">
          <n-select v-model:value="policyForm.type" :options="typeOptions" placeholder="请选择政策类型" />
        </n-form-item>
        <n-form-item label="发布单位" path="issueUnit">
          <n-input v-model:value="policyForm.issueUnit" placeholder="请输入发布单位" />
        </n-form-item>
        <n-form-item label="发布时间" path="issueTime">
          <n-date-picker
              v-model:value="policyForm.issueTime"
              type="datetime"
              placeholder="请选择发布时间"
              format="yyyy-MM-dd HH:mm:ss"
              value-format="yyyy-MM-dd HH:mm:ss"
              clearable
          />
        </n-form-item>
        <n-form-item label="政策内容" path="content">
          <n-input
              v-model:value="policyForm.content"
              type="textarea"
              placeholder="请输入政策内容"
              :autosize="{ minRows: 3, maxRows: 6 }"
          />
        </n-form-item>
        <n-form-item label="封面图片" path="image">
          <n-upload
              :max="1"
              :file-list="imageFileList"
              @update:file-list="handleImageFileListChange"
              :custom-request="handleImageUpload"
              accept="image/*"
          >
            <n-button>上传封面图片</n-button>
          </n-upload>
          <div v-if="policyForm.image" style="margin-top: 12px;">
            <n-image
                :src="getImageUrl(policyForm.thumbImage || policyForm.image)"
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
import { ref, reactive, h, onMounted, onUnmounted, computed } from 'vue'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NButton,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NDataTable,
  NPopconfirm,
  NModal,
  NImage,
  NDatePicker,
  NUpload,
  useMessage,
  NTag,
  NSpin,
  NPagination
} from 'naive-ui'
import dayjs from 'dayjs'

const message = useMessage()

const isMobile = ref(false)
const loading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const policyList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增政策')
const formRef = ref(null)

// 简化：只保留关键词搜索
const searchForm = reactive({
  keyword: ''
})

const policyForm = reactive({
  id: null,
  title: '',
  type: '省级',
  issueUnit: '',
  issueTime: null,
  content: '',
  image: '',
  thumbImage: '',
  status: 1
})

const imageFileList = ref([])

// 政策类型选项（保留用于编辑）
const typeOptions = [
  { label: '省级', value: '省级' },
  { label: '市级', value: '市级' }
]

const statusOptions = [
  { label: '发布', value: 1 },
  { label: '草稿', value: 0 }
]

// 分页相关状态
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

const formRules = {
  title: [
    { required: true, message: '请输入政策标题', trigger: 'blur' },
    { min: 1, max: 200, message: '政策标题长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择政策类型', trigger: 'change' }
  ],
  issueUnit: [
    { required: true, message: '请输入发布单位', trigger: 'blur' },
    { min: 1, max: 100, message: '发布单位长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入政策内容', trigger: 'blur' },
    { min: 10, max: 5000, message: '政策内容长度在 10 到 5000 个字符', trigger: 'blur' }
  ]
}

// 从localStorage获取token
const getToken = () => {
  return localStorage.getItem('token') || sessionStorage.getItem('token')
}

// 简化API调用，只传递关键词
const getPolicyPage = (current = 1, size = 10, keyword = '') => {
  const token = getToken()

  // 构建URL参数，只包含关键词搜索
  const params = new URLSearchParams({
    current: current.toString(),
    size: size.toString(),
    keyword: keyword
  })

  const url = `/api/policy/admin/page?${params.toString()}`

  console.log('请求URL:', url)

  return fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        return response.json()
      })
      .then(data => {
        console.log('API响应数据:', data)
        return data
      })
      .catch(error => {
        console.error('API请求错误:', error)
        return {
          code: 500,
          message: error.message || '网络请求失败'
        }
      })
}

const getPolicyById = (id) => {
  const token = getToken()
  return fetch(`/api/policy/${id}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        return response.json()
      })
      .catch(error => {
        console.error('API请求错误:', error)
        return {
          code: 500,
          message: error.message || '网络请求失败'
        }
      })
}

const addPolicy = (data) => {
  const token = getToken()
  return fetch('/api/policy/admin/create', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        return response.json()
      })
      .catch(error => {
        console.error('API请求错误:', error)
        return {
          code: 500,
          message: error.message || '网络请求失败'
        }
      })
}

const updatePolicy = (id, data) => {
  const token = getToken()
  return fetch(`/api/policy/admin/update/${id}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        return response.json()
      })
      .catch(error => {
        console.error('API请求错误:', error)
        return {
          code: 500,
          message: error.message || '网络请求失败'
        }
      })
}

const deletePolicy = (id) => {
  const token = getToken()
  return fetch(`/api/policy/admin/delete/${id}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        return response.json()
      })
      .catch(error => {
        console.error('API请求错误:', error)
        return {
          code: 500,
          message: error.message || '网络请求失败'
        }
      })
}

const uploadFile = (file) => {
  // 模拟文件上传API
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        data: {
          originUrl: `https://example.com/uploads/${file.name}`,
          thumbUrl: `https://example.com/uploads/thumb_${file.name}`
        }
      })
    }, 1000)
  })
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `${window.location.origin}/uploads/${url}`
}

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 80, fixed: 'left' },
  { title: '政策标题', key: 'title', width: 200, ellipsis: { tooltip: true }, fixed: 'left' },
  {
    title: '封面',
    key: 'image',
    width: 100,
    render: (row) => {
      if (!row.image) return '-'

      return h(NImage, {
        width: 60,
        height: 45,
        src: getImageUrl(row.thumbImage || row.image),
        objectFit: 'cover',
        style: { borderRadius: '4px' },
        previewDisabled: true
      })
    }
  },
  { title: '政策类型', key: 'type', width: 120 },
  { title: '发布单位', key: 'issueUnit', width: 150, ellipsis: { tooltip: true } },
  {
    title: '政策内容',
    key: 'content',
    width: 300,
    ellipsis: { tooltip: true },
    render: (row) => {
      return row.content?.substring(0, 30) + (row.content?.length > 30 ? '...' : '') || '-'
    }
  },
  {
    title: '发布时间',
    key: 'issueTime',
    width: 160,
    render: (row) => formatDate(row.issueTime)
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
])

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

const formatDate = (date) => {
  if (!date) return '-'
  if (typeof date === 'string' || typeof date === 'number') {
    return dayjs(date).format('YYYY-MM-dd HH:mm:ss')
  }
  return '-'
}

const loadData = async () => {
  try {
    loading.value = true
    console.log(`加载第 ${currentPage.value} 页，每页 ${pageSize.value} 条，关键词: ${searchForm.keyword}`)

    const res = await getPolicyPage(currentPage.value, pageSize.value, searchForm.keyword)

    console.log('API响应:', res)

    if (res.code === 200) {
      policyList.value = res.data || []

      // 设置总数 - 尝试多种可能的字段名
      if (res.pagination && res.pagination.totalItems !== undefined) {
        totalItems.value = res.pagination.totalItems
      } else if (res.pagination && res.pagination.total !== undefined) {
        totalItems.value = res.pagination.total
      } else if (res.total !== undefined) {
        totalItems.value = res.total
      } else if (Array.isArray(res.data)) {
        totalItems.value = res.data.length
        console.warn('警告：未找到总数，使用当前页数据长度。请检查后端API是否返回总数信息。')
      }

      console.log(`成功加载数据，总数: ${totalItems.value}，当前页数据条数: ${policyList.value.length}`)
    } else if (res.code === 401 || res.code === 403) {
      message.error('未登录或权限不足，请重新登录')
    } else {
      message.error(res.message || '加载政策列表失败')
    }
  } catch (error) {
    console.error('加载政策列表失败:', error)
    message.error('加载政策列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  currentPage.value = 1
  loadData()
}

const handlePageChange = (page) => {
  console.log('页码变化到:', page)
  currentPage.value = page
  loadData()
}

const handlePageSizeChange = (newPageSize) => {
  console.log('每页大小变化到:', newPageSize)
  pageSize.value = newPageSize
  currentPage.value = 1  // 页大小改变时回到第一页
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增政策'
  Object.assign(policyForm, {
    id: null,
    title: '',
    type: '省级',
    issueUnit: '',
    issueTime: null,
    content: '',
    image: '',
    thumbImage: '',
    status: 1
  })
  imageFileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getPolicyById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑政策'
      Object.assign(policyForm, {
        id: res.data.id,
        title: res.data.title || '',
        type: res.data.type || '省级',
        issueUnit: res.data.issueUnit || '',
        issueTime: res.data.issueTime || null,
        content: res.data.content || '',
        image: res.data.image || '',
        thumbImage: res.data.thumbImage || '',
        status: res.data.status || 1
      })

      // 设置封面图片文件列表
      if (res.data.image || res.data.thumbImage) {
        imageFileList.value = [{
          id: 'image',
          name: 'image.jpg',
          status: 'finished',
          url: res.data.thumbImage || res.data.image
        }]
      } else {
        imageFileList.value = []
      }

      dialogVisible.value = true
    } else if (res.code === 401 || res.code === 403) {
      message.error('未登录或权限不足，请重新登录')
    } else {
      message.error(res.message || '获取政策详情失败')
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
    const data = {
      title: policyForm.title,
      type: policyForm.type,
      issueUnit: policyForm.issueUnit,
      issueTime: policyForm.issueTime,
      content: policyForm.content,
      image: policyForm.image,
      thumbImage: policyForm.thumbImage,
      status: policyForm.status
    }

    let res
    if (policyForm.id) {
      res = await updatePolicy(policyForm.id, data)
    } else {
      res = await addPolicy(data)
    }

    if (res.code === 200) {
      message.success(policyForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } else if (res.code === 401 || res.code === 403) {
      message.error('未登录或权限不足，请重新登录')
    } else {
      message.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
  } finally {
    dialogLoading.value = false
  }
}

// 处理封面图片上传
const handleImageUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl
      const thumbUrl = res.data.thumbUrl

      policyForm.image = originUrl
      policyForm.thumbImage = thumbUrl

      // 更新文件列表中的URL，用于预览显示
      const fileIndex = imageFileList.value.findIndex(f => f.id === file.id || f.name === file.name)
      if (fileIndex !== -1) {
        imageFileList.value[fileIndex].url = thumbUrl
        imageFileList.value[fileIndex].status = 'finished'
      } else {
        imageFileList.value.push({
          id: file.id || 'image',
          name: file.name || 'image.jpg',
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
const handleImageFileListChange = (files) => {
  imageFileList.value = files
  if (files.length === 0) {
    policyForm.image = ''
    policyForm.thumbImage = ''
  }
}

const handleDelete = async (id) => {
  try {
    const res = await deletePolicy(id)
    if (res.code === 200) {
      message.success('删除成功')
      loadData()
    } else if (res.code === 401 || res.code === 403) {
      message.error('未登录或权限不足，请重新登录')
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
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

// 移动端卡片列表
.mobile-list {
  .empty-state {
    text-align: center;
    padding: 60px 20px;
  }

  .card-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .mobile-card {
    :deep(.n-card__content) {
      padding: 16px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;

      .policy-info {
        flex: 1;
        margin-right: 12px;

        .policy-name {
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 4px 0;
          line-height: 1.4;
        }

        .policy-meta {
          display: flex;
          flex-direction: column;
          gap: 6px;
          align-items: flex-start;

          .policy-date {
            font-size: 12px;
            color: #6b7280;
          }
        }
      }

      .policy-cover {
        flex-shrink: 0;

        .no-cover {
          width: 80px;
          height: 60px;
          background: #f3f4f6;
          border-radius: 6px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #9ca3af;
        }
      }
    }

    .card-content {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-bottom: 12px;
      padding: 12px;
      background: #f9fafb;
      border-radius: 8px;

      .info-item {
        display: flex;
        font-size: 13px;

        .label {
          color: #6b7280;
          min-width: 80px;
          flex-shrink: 0;
        }

        .policy-content-preview {
          color: #374151;
          line-height: 1.4;
          max-height: 3.6em;
          overflow: hidden;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
      }
    }

    .card-actions {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #e5e7eb;
    }
  }

  .mobile-pagination {
    margin-top: 16px;
    padding: 12px;
    background: #ffffff;
    border-radius: 8px;

    :deep(.n-pagination) {
      justify-content: center;
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  .search-header {
    flex-direction: column;
    gap: 12px;

    .search-form {
      width: 100%;
      min-width: auto;

      :deep(.n-form-item) {
        margin-bottom: 12px;

        .n-form-item-label {
          width: auto !important;
          margin-bottom: 4px;
        }
      }
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

  // 移动端表单优化
  :deep(.n-modal) {
    .n-dialog {
      margin: 20px auto;
    }

    .n-form-item {
      margin-bottom: 16px;

      .n-form-item-label {
        font-weight: 500;
        margin-bottom: 8px;
      }

      .n-input,
      .n-select,
      .n-date-picker {
        width: 100%;
      }
    }

    .n-dialog__action {
      padding: 12px 16px;

      .n-button {
        flex: 1;
        margin: 0 4px;
      }
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



