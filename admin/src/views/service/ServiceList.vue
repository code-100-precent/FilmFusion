<template>
  <div class="service-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="服务名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入服务名称" clearable @keyup.enter="handleSearch" />
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
            新增服务
          </n-button>
        </div>
      </div>
      
      <n-data-table
        :columns="columns"
        :data="serviceList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>
    
    <n-modal v-model:show="dialogVisible" preset="dialog" :title="dialogTitle" style="width: 90%; max-width: 800px">
      <n-form ref="formRef" :model="serviceForm" :rules="formRules" label-placement="left" label-width="100">
        <n-form-item label="服务名称" path="name">
          <n-input v-model:value="serviceForm.name" placeholder="请输入服务名称" />
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input v-model:value="serviceForm.description" type="textarea" :rows="5" placeholder="请输入服务描述" />
        </n-form-item>
        <n-form-item label="价格" path="price">
          <n-input-number v-model:value="serviceForm.price" placeholder="请输入价格" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-select v-model:value="serviceForm.status" :options="statusOptions" />
        </n-form-item>
        <n-form-item label="服务地址" path="address">
          <n-input v-model:value="serviceForm.address" placeholder="请输入服务地址" />
        </n-form-item>
        <n-form-item label="联系电话" path="phone">
          <n-input v-model:value="serviceForm.phone" placeholder="请输入联系电话" />
        </n-form-item>
        <n-form-item label="联系人" path="contactName">
          <n-input v-model:value="serviceForm.contactName" placeholder="请输入联系人" />
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
  NInputNumber,
  NSelect,
  NDataTable,
  NTag,
  NPopconfirm,
  NModal,
  useMessage
} from 'naive-ui'
import { getServicePage, addService, updateService, deleteService, getServiceById } from '@/api'

const message = useMessage()

const loading = ref(false)
const serviceList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增服务')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const serviceForm = reactive({
  id: null,
  name: '',
  description: '',
  price: 0,
  status: 1,
  address: '',
  phone: '',
  contactName: ''
})

const statusOptions = [
  { label: '可用', value: 1 },
  { label: '不可用', value: 0 }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入服务描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  address: [{ required: true, message: '请输入服务地址', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '服务名称', key: 'name', ellipsis: { tooltip: true } },
  { title: '价格', key: 'price', width: 120, render: (row) => `¥${row.price || 0}` },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const statusMap = {
        1: { label: '可用', type: 'success' },
        0: { label: '不可用', type: 'error' }
      }
      const statusInfo = statusMap[row.status] || { label: row.status, type: 'default' }
      return h(NTag, { type: statusInfo.type, size: 'small' }, { default: () => statusInfo.label })
    }
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
    const res = await getServicePage(pagination.page, pagination.pageSize, searchForm.keyword)
    if (res.code === 200) {
      serviceList.value = res.data || []
      pagination.itemCount = res.pagination?.totalItems || 0
    }
  } catch (error) {
    console.error('加载服务列表失败:', error)
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
  dialogTitle.value = '新增服务'
  Object.assign(serviceForm, {
    id: null,
    name: '',
    description: '',
    price: 0,
    status: 1,
    address: '',
    phone: '',
    contactName: ''
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getServiceById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑服务'
      Object.assign(serviceForm, {
        id: res.data.id,
        name: res.data.name,
        description: res.data.description,
        price: res.data.price,
        status: res.data.status,
        address: res.data.address,
        phone: res.data.phone,
        contactName: res.data.contactName
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取服务详情失败:', error)
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
      name: serviceForm.name,
      description: serviceForm.description,
      price: serviceForm.price,
      status: serviceForm.status,
      address: serviceForm.address,
      phone: serviceForm.phone,
      contactName: serviceForm.contactName
    }
    
    let res
    if (serviceForm.id) {
      res = await updateService({ ...data, id: serviceForm.id })
    } else {
      res = await addService(data)
    }
    
    if (res.code === 200) {
      message.success(serviceForm.id ? '更新成功' : '创建成功')
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
    const res = await deleteService(id)
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
.service-management {
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
