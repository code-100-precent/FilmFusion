<template>
  <div class="feedback-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="关键词">
            <n-input
              v-model:value="searchForm.keyword"
              placeholder="请输入关键词"
              clearable
              @keyup.enter="handleSearch"
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
            <n-button @click="handleRefresh" style="margin-left: 12px">
              <template #icon>
                <Icon icon="mdi:refresh" />
              </template>
              刷新
            </n-button>
          </n-form-item>
        </n-form>
      </div>
      
      <n-data-table
        :columns="columns"
        :data="feedbackList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>
    
    <!-- 处理反馈对话框 -->
    <n-modal v-model:show="dialogVisible" preset="dialog" title="处理反馈" :mask-closable="false" style="width: 90%; max-width: 600px">
      <n-form label-placement="left" label-width="100px">
        <n-form-item label="反馈类型">
          <n-input :value="currentFeedback?.type" disabled />
        </n-form-item>
        <n-form-item label="反馈内容">
          <n-input :value="currentFeedback?.content" type="textarea" :rows="5" disabled />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="feedbackForm.status" :options="statusOptions" placeholder="请选择状态" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="dialogVisible = false">取消</n-button>
        <n-button type="primary" @click="handleSaveStatus" :loading="dialogLoading">保存</n-button>
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
  NTag,
  NPopconfirm,
  NModal,
  NSelect,
  useMessage,
  useDialog
} from 'naive-ui'
import { getFeedbackPage, deleteFeedback, updateFeedbackStatus, getFeedbackById } from '@/api'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const feedbackList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const currentFeedback = ref(null)
const feedbackForm = reactive({
  id: null,
  status: '',
  type: ''
})
const searchForm = reactive({
  keyword: ''
})

const statusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已解决', value: 'RESOLVED' },
  { label: '已拒绝', value: 'REJECTED' }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  onChange: (page) => {
    pagination.page = page
    loadData()
  },
  onUpdatePageSize: (pageSize) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    loadData()
  }
})

const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '反馈类型',
    key: 'type',
    width: 120,
    render: (row) => {
      const typeMap = {
        'SUGGESTION': { label: '建议', type: 'info' },
        'BUG': { label: 'Bug', type: 'error' },
        'FEATURE': { label: '功能', type: 'success' },
        'OTHER': { label: '其他', type: 'default' }
      }
      const typeInfo = typeMap[row.type] || { label: row.type, type: 'default' }
      return h(NTag, { type: typeInfo.type, size: 'small' }, { default: () => typeInfo.label })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const statusMap = {
        'PENDING': { label: '待处理', type: 'warning' },
        'PROCESSING': { label: '处理中', type: 'info' },
        'RESOLVED': { label: '已解决', type: 'success' },
        'REJECTED': { label: '已拒绝', type: 'error' }
      }
      const statusInfo = statusMap[row.status] || { label: row.status, type: 'default' }
      return h(NTag, { type: statusInfo.type, size: 'small' }, { default: () => statusInfo.label })
    }
  },
  {
    title: '反馈内容',
    key: 'content',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: (row) => {
      if (!row.createdAt) return '-'
      // 处理数组格式的日期 [2024, 12, 15, 10, 0, 0]
      if (Array.isArray(row.createdAt)) {
        if (row.createdAt.length >= 6) {
          const dateStr = `${row.createdAt[0]}-${String(row.createdAt[1]).padStart(2, '0')}-${String(row.createdAt[2]).padStart(2, '0')} ${String(row.createdAt[3]).padStart(2, '0')}:${String(row.createdAt[4]).padStart(2, '0')}:${String(row.createdAt[5]).padStart(2, '0')}`
          return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
        }
        return '-'
      }
      return dayjs(row.createdAt).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: (row) => {
      return h('div', { style: 'display: flex; gap: 8px;' }, [
        h(NButton, {
          size: 'small',
          type: 'info',
          onClick: () => handleProcess(row)
        }, { default: () => '处理' }),
        h(
          NPopconfirm,
          {
            onPositiveClick: () => handleDelete(row.id)
          },
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
    const res = await getFeedbackPage(
      pagination.page,
      pagination.pageSize,
      searchForm.keyword
    )
    
    if (res.code === 200) {
      feedbackList.value = res.data || []
      pagination.itemCount = res.pagination?.totalItems || 0
    }
  } catch (error) {
    console.error('加载反馈列表失败:', error)
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

const handleRefresh = () => {
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

const handleProcess = async (row) => {
  try {
    const res = await getFeedbackById(row.id)
    if (res.code === 200 && res.data) {
      currentFeedback.value = res.data
      feedbackForm.id = res.data.id
      feedbackForm.status = res.data.status
      feedbackForm.type = res.data.type
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取反馈详情失败:', error)
    message.error('获取反馈详情失败')
  }
}

const handleSaveStatus = async () => {
  if (!feedbackForm.status) {
    message.warning('请选择状态')
    return
  }
  
  try {
    dialogLoading.value = true
    const res = await updateFeedbackStatus(feedbackForm.id, feedbackForm.status)
    if (res.code === 200) {
      message.success('更新成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('更新失败:', error)
    message.error('更新失败')
  } finally {
    dialogLoading.value = false
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

