<template>
  <div class="policy-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="政策标题">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入政策标题" clearable @keyup.enter="handleSearch" />
          </n-form-item>
          <n-form-item label="政策类型">
            <n-select v-model:value="searchForm.type" :options="typeOptions" placeholder="请选择政策类型" clearable />
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
                    :src="policy.cover"
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
        <n-form-item label="封面" path="cover">
          <n-input v-model:value="policyForm.cover" placeholder="请输入封面图片地址" />
        </n-form-item>
        <n-form-item label="图片" path="image">
          <n-input v-model:value="policyForm.image" placeholder="请输入图片地址" />
        </n-form-item>
        <n-form-item label="封面图片" path="cover">
          <n-input v-model:value="policyForm.cover" placeholder="请输入封面图片URL" />
        </n-form-item>
        <n-form-item label="缩略封面" path="thumbCover">
          <n-input v-model:value="policyForm.thumbCover" placeholder="请输入缩略封面URL" />
        </n-form-item>
        <n-form-item label="详情图片" path="image">
          <n-input v-model:value="policyForm.image" placeholder="请输入详情图片URL" />
        </n-form-item>
        <n-form-item label="缩略详情图" path="thumbImage">
          <n-input v-model:value="policyForm.thumbImage" placeholder="请输入缩略详情图URL" />
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
import { ref, reactive, h, onMounted, onUnmounted } from 'vue'
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
  useMessage,
  NImage,
  NTag
} from 'naive-ui'
import { getPolicyPage, addPolicy, updatePolicy, deletePolicy, getPolicyById } from '@/api'
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

const searchForm = reactive({
  keyword: '',
  type: ''
})

const policyForm = reactive({
  id: null,
  title: '',
  type: '省级',
  issueUnit: '',
  issueTime: null,
  content: '',
  cover: '',
  image: '',
  thumbCover: '',
  thumbImage: '',
  status: 1
})

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

const columns = [
  { title: 'ID', key: 'id', width: 80, fixed: 'left' },
  { title: '政策标题', key: 'title', width: 200, ellipsis: { tooltip: true }, fixed: 'left' },
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
  { title: '政策类型', key: 'type', width: 120 },
  { title: '发布单位', key: 'issueUnit', width: 150, ellipsis: { tooltip: true } },
  { title: '政策内容', key: 'content', width: 300, ellipsis: { tooltip: true } },
  {
    title: '政策标题',
    key: 'title',
    width: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '政策类型',
    key: 'type',
    width: 120,
    render: (row) => {
      return h(NTag, { 
        type: row.type === '省级' ? 'info' : 'warning', 
        size: 'small' 
      }, { 
        default: () => row.type || '-' 
      })
    }
  },
  { title: '发布单位', key: 'issueUnit', width: 150, ellipsis: { tooltip: true } },
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
  checkMobile()
  window.addEventListener('resize', checkMobile)
  // 这里需要根据实际的Policy API来调整
  // loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

const formatDate = (date) => {
  if (!date) return '-'
  if (Array.isArray(date)) {
    return dayjs(date[0] + '-' + String(date[1]).padStart(2, '0') + '-' + String(date[2]).padStart(2, '0')).format('YYYY-MM-DD HH:mm:ss')
  }
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

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
    message.error('加载政策列表失败')
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
  searchForm.type = ''
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
    issueTime: null,
    content: '',
    cover: '',
    image: '',
    thumbCover: '',
    thumbImage: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  // 这里需要根据实际的Policy API来调整
  dialogTitle.value = '编辑政策'
  Object.assign(policyForm, {
    id: row.id,
    title: row.title || '',
    type: row.type || '省级',
    issueUnit: row.issueUnit || '',
    issueTime: row.issueTime || null,
    content: row.content || '',
    cover: row.cover || '',
    image: row.image || '',
    thumbCover: row.thumbCover || '',
    thumbImage: row.thumbImage || ''
  })
  dialogVisible.value = true
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
      cover: policyForm.cover,
      image: policyForm.image,
      thumbCover: policyForm.thumbCover,
      thumbImage: policyForm.thumbImage,
      status: policyForm.status
    }
    
    let res
    if (policyForm.id) {
      res = await updatePolicy(policyForm.id, data)
    } else {
      res = await createPolicy(data)
    }
    
    if (res.code === 200) {
      message.success(policyForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
    
    message.success('政策管理功能待实现')
    dialogVisible.value = false
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
  } finally {
    dialogLoading.value = false
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