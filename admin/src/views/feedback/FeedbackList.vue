<template>
  <div class="feedback-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="反馈内容">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入反馈内容关键词" clearable @keyup.enter="handleSearch" />
          </n-form-item>
          <n-form-item label="状态">
            <n-select 
              v-model:value="searchForm.status" 
              :options="statusOptions" 
              placeholder="请选择状态" 
              clearable 
              style="min-width: 120px;"
            />
          </n-form-item>
          <n-form-item label="类型">
            <n-select 
              v-model:value="searchForm.type" 
              :options="typeOptions" 
              placeholder="请选择类型" 
              clearable 
              style="min-width: 120px;"
            />
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
      </div>
      
      <!-- 桌面端表格 -->
      <n-data-table
        v-if="!isMobile"
        :columns="columns"
        :data="feedbackList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        :scroll-x="1200"
      />
      
      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-list">
        <n-spin :show="loading">
          <div v-if="feedbackList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:message-text-outline" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
              v-for="feedback in feedbackList"
              :key="feedback.id"
              class="mobile-card"
              hoverable
            >
              <div class="card-header">
                <div class="feedback-info">
                  <div class="feedback-meta">
                    <n-tag :type="getTypeTagType(feedback.type)" size="small">
                      {{ getTypeLabel(feedback.type) }}
                    </n-tag>
                    <n-tag :type="getStatusTagType(feedback.status)" size="small">
                      {{ getStatusLabel(feedback.status) }}
                    </n-tag>
                  </div>
                  <div class="feedback-time">
                    {{ formatDate(feedback.createdAt) }}
                  </div>
                </div>
              </div>
              <div class="card-content">
                <div class="content-item">
                  <span class="label">反馈内容：</span>
                  <p class="feedback-content">{{ feedback.content }}</p>
                </div>
                <div class="info-item">
                  <span class="label">用户ID：</span>
                  <span>{{ feedback.userId || '-' }}</span>
                </div>
              </div>
              <div class="card-actions">
                <n-button 
                  size="small" 
                  @click="handleUpdateStatus(feedback)" 
                  block 
                  style="margin-bottom: 8px"
                  :type="feedback.status === '已解决' ? 'warning' : 'success'"
                >
                  {{ feedback.status === '已解决' ? '重新处理' : '标记为已解决' }}
                </n-button>
                <n-popconfirm @positive-click="handleDelete(feedback.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个反馈吗？
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

    <!-- 状态更新对话框 -->
    <n-modal 
      v-model:show="statusDialogVisible" 
      preset="dialog" 
      title="更新反馈状态" 
      style="width: 90%; max-width: 500px"
      :mask-closable="false"
    >
      <n-form ref="statusFormRef" :model="statusForm" :rules="statusFormRules" label-placement="left" label-width="80">
        <n-form-item label="当前状态">
          <n-tag :type="getStatusTagType(currentFeedback?.status)" size="small">
            {{ getStatusLabel(currentFeedback?.status) }}
          </n-tag>
        </n-form-item>
        <n-form-item label="新状态" path="status">
          <n-select v-model:value="statusForm.status" :options="statusOptionsForUpdate" placeholder="请选择新状态" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="statusDialogVisible = false">取消</n-button>
        <n-button type="primary" @click="handleStatusSave" :loading="statusLoading">确定</n-button>
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
  NTag,
  NDataTable,
  NPopconfirm,
  NModal,
  NSpin,
  NPagination,
  useMessage
} from 'naive-ui'
import { getFeedbackPage, updateFeedbackStatus, deleteFeedback, getFeedbackById } from '@/api'
import dayjs from 'dayjs'

const message = useMessage()

const isMobile = ref(false)
const loading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const feedbackList = ref([])
const statusDialogVisible = ref(false)
const statusLoading = ref(false)
const currentFeedback = ref(null)
const formRef = ref(null)
const statusFormRef = ref(null)

const searchForm = reactive({
  keyword: '',
  status: '',
  type: ''
})

const statusForm = reactive({
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const statusOptions = [
  { label: '未处理', value: '未处理' },
  { label: '处理中', value: '处理中' },
  { label: '已解决', value: '已解决' }
]

const statusOptionsForUpdate = [
  { label: '未处理', value: '未处理' },
  { label: '处理中', value: '处理中' },
  { label: '已解决', value: '已解决' }
]

const typeOptions = [
  { label: '建议', value: '建议' },
  { label: '投诉', value: '投诉' },
  { label: '咨询', value: '咨询' },
  { label: '其他', value: '其他' }
]

const statusFormRules = {
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户ID', key: 'userId', width: 100 },
  {
    title: '反馈类型',
    key: 'type',
    width: 100,
    render: (row) => {
      return h(NTag, { 
        type: getTypeTagType(row.type), 
        size: 'small' 
      }, { 
        default: () => getTypeLabel(row.type) 
      })
    }
  },
  {
    title: '反馈内容',
    key: 'content',
    width: 300,
    ellipsis: { 
      tooltip: true,
      line: 2
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 120,
    render: (row) => {
      return h(NTag, { 
        type: getStatusTagType(row.status), 
        size: 'small' 
      }, { 
        default: () => getStatusLabel(row.status) 
      })
    }
  },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: (row) => {
      if (Array.isArray(row.createdAt)) {
        return dayjs(row.createdAt[0] + '-' + String(row.createdAt[1]).padStart(2, '0') + '-' + String(row.createdAt[2]).padStart(2, '0')).format('YYYY-MM-DD HH:mm:ss')
      }
      return row.createdAt ? dayjs(row.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: (row) => {
      return h('div', { style: 'display: flex; gap: 8px; flex-wrap: wrap;' }, [
        h(NButton, { 
          size: 'small', 
          type: row.status === '已解决' ? 'warning' : 'success',
          onClick: () => handleUpdateStatus(row) 
        }, { 
          default: () => row.status === '已解决' ? '重新处理' : '标记解决' 
        }),
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

const formatDate = (date) => {
  if (!date) return '-'
  if (Array.isArray(date)) {
    return dayjs(date[0] + '-' + String(date[1]).padStart(2, '0') + '-' + String(date[2]).padStart(2, '0')).format('YYYY-MM-DD HH:mm:ss')
  }
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const getTypeLabel = (type) => {
  // 类型值已经是中文，直接返回
  return type || '未知'
}

const getTypeTagType = (type) => {
  const typeMap = {
    '建议': 'info',
    '投诉': 'error',
    '咨询': 'warning',
    '其他': 'default'
  }
  return typeMap[type] || 'default'
}

const getStatusLabel = (status) => {
  // 状态值已经是中文，直接返回
  return status || '未知'
}

const getStatusTagType = (status) => {
  const statusMap = {
    '未处理': 'warning',
    '处理中': 'info',
    '已解决': 'success'
  }
  return statusMap[status] || 'default'
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getFeedbackPage(pagination.page, pagination.pageSize, searchForm.keyword, searchForm.status)
    if (res.code === 200) {
      let list = res.data || []
      // 如果选择了类型，在前端过滤
      if (searchForm.type) {
        list = list.filter(item => item.type === searchForm.type)
      }
      feedbackList.value = list
      pagination.itemCount = res.pagination?.totalItems || 0
    }
  } catch (error) {
    console.error('加载反馈列表失败:', error)
    message.error('加载反馈列表失败')
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
  searchForm.status = ''
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

const handleUpdateStatus = (feedback) => {
  currentFeedback.value = feedback
  // 如果当前是已解决，则切换到处理中；否则切换到已解决
  statusForm.status = feedback.status === '已解决' ? '处理中' : '已解决'
  statusDialogVisible.value = true
}

const handleStatusSave = async () => {
  if (!statusFormRef.value) return
  try {
    await statusFormRef.value.validate()
  } catch (error) {
    return
  }
  
  try {
    statusLoading.value = true
    const res = await updateFeedbackStatus(currentFeedback.value.id, statusForm.status)
    
    if (res.code === 200) {
      message.success('状态更新成功')
      statusDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    message.error('更新状态失败')
  } finally {
    statusLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    const res = await deleteFeedback(id)
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
.feedback-management {
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
      margin-bottom: 12px;
      
      .feedback-info {
        .feedback-meta {
          display: flex;
          gap: 8px;
          margin-bottom: 8px;
        }
        
        .feedback-time {
          font-size: 12px;
          color: #9ca3af;
        }
      }
    }
    
    .card-content {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 12px;
      padding: 12px;
      background: #f9fafb;
      border-radius: 8px;
      
      .content-item {
        .feedback-content {
          margin: 8px 0 0 0;
          color: #374151;
          line-height: 1.5;
        }
      }
      
      .info-item {
        display: flex;
        font-size: 13px;
        
        .label {
          color: #6b7280;
          min-width: 60px;
          flex-shrink: 0;
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
      .n-select {
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