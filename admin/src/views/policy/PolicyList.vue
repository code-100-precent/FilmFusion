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
      />
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
        <n-form-item label="政策内容" path="content">
          <n-input v-model:value="policyForm.content" type="textarea" :rows="8" placeholder="请输入政策内容" />
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
  useMessage
} from 'naive-ui'
import { getPolicyPage, addPolicy, updatePolicy, deletePolicy, getPolicyById } from '@/api'
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
  status: 1
})

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
  { title: 'ID', key: 'id', width: 80 },
  { title: '政策标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '政策类型', key: 'type', width: 100 },
  { title: '发布单位', key: 'issueUnit', width: 150 },
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
    render: (row) => row.status === 1 ? '发布' : '草稿'
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
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
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getPolicyById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑政策'
      Object.assign(policyForm, res.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取政策详情失败:', error)
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
      issueDate: policyForm.issueDate,
      content: policyForm.content,
      status: policyForm.status
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

