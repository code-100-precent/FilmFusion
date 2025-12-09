<template>
  <div class="location-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="关键词">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入场地名称或关键词" clearable @keyup.enter="handleSearch" />
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
            新增场地
          </n-button>
        </div>
      </div>
      
      <n-data-table
        :columns="columns"
        :data="locationList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        :scroll-x="2400"
      />
    </n-card>
    
    <n-modal v-model:show="dialogVisible" preset="dialog" :title="dialogTitle" style="width: 90%; max-width: 800px">
      <n-form ref="formRef" :model="locationForm" :rules="formRules" label-placement="left" label-width="100">
        <n-form-item label="场地名称" path="name">
          <n-input v-model:value="locationForm.name" placeholder="请输入场地名称" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="locationForm.type" :options="typeOptions" placeholder="请选择类型" />
        </n-form-item>
        <n-form-item label="价格" path="price">
          <n-input-number v-model:value="locationForm.price" placeholder="请输入价格" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-select v-model:value="locationForm.status" :options="statusOptions" />
        </n-form-item>
        <n-form-item label="场地介绍" path="locationDescription">
          <n-input v-model:value="locationForm.locationDescription" type="textarea" :rows="5" placeholder="请输入场地介绍" />
        </n-form-item>
        <n-form-item label="联系人" path="contactName">
          <n-input v-model:value="locationForm.contactName" placeholder="请输入联系人" />
        </n-form-item>
        <n-form-item label="联系电话" path="contactPhone">
          <n-input v-model:value="locationForm.contactPhone" placeholder="请输入联系电话" />
        </n-form-item>
        <n-form-item label="地址" path="address">
          <n-input v-model:value="locationForm.address" placeholder="请输入地址" />
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
  NImage,
  useMessage
} from 'naive-ui'
import { getLocationPage, addLocation, updateLocation, deleteLocation, getLocationById } from '@/api'
import { getImageUrl } from '@/utils/image'
import dayjs from 'dayjs'

const message = useMessage()

const loading = ref(false)
const locationList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增场地')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const locationForm = reactive({
  id: null,
  name: '',
  type: '',
  price: 0,
  status: 1,
  locationDescription: '',
  contactPhone: '',
  contactName: '',
  address: ''
})

const typeOptions = [
  { label: '自然景观', value: 'NATURAL' },
  { label: '历史建筑', value: 'HISTORICAL' },
  { label: '现代建筑', value: 'MODERN' },
  { label: '其他', value: 'OTHER' }
]

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
  name: [{ required: true, message: '请输入场地名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  locationDescription: [{ required: true, message: '请输入场地介绍', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '场地名称', key: 'name', width: 150, ellipsis: { tooltip: true } },
  { title: '类型', key: 'type', width: 100 },
  {
    title: '封面',
    key: 'cover',
    width: 100,
    render: (row) => {
      // 优先使用缩略图，如果没有则使用原图
      const coverUrl = row.thumbImage || row.image || row.cover || row.thumbCover
      if (!coverUrl) return '-'
      
      const originalUrl = row.image || row.cover || coverUrl
      
      return h(NImage, {
        width: 60,
        height: 45,
        src: getImageUrl(coverUrl),
        objectFit: 'cover',
        previewDisabled: false,
        showToolbar: false,
        srcset: [
          {
            src: getImageUrl(originalUrl),
            alt: '场地封面'
          }
        ]
      })
    }
  },
  { title: '地址', key: 'address', width: 200, ellipsis: { tooltip: true } },
  { title: '联系人', key: 'locationPrincipalName', width: 120 },
  { title: '联系电话', key: 'locationPrincipalPhone', width: 130 },
  { title: '价格', key: 'price', width: 100, render: (row) => row.price ? '¥' + row.price : '-' },
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

const handleDialogSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  
  try {
    dialogLoading.value = true
    const data = { ...locationForm }
    
    let res
    if (locationForm.id) {
      res = await updateLocation(locationForm.id, data)
    } else {
      res = await addLocation(data)
    }
    
    if (res.code === 200) {
      message.success(locationForm.id ? '更新成功' : '创建成功')
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
    const res = await deleteLocation(id)
    if (res.code === 200) {
      message.success('删除成功')
      loadData()
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getLocationPage(pagination.page, pagination.pageSize, searchForm.keyword)
    if (res.code === 200) {
      locationList.value = res.data || []
      pagination.itemCount = res.pagination?.totalItems || 0
    }
  } catch (error) {
    console.error('加载场地列表失败:', error)
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
  dialogTitle.value = '新增场地'
  Object.assign(locationForm, {
    id: null,
    name: '',
    type: '',
    price: 0,
    status: 1,
    locationDescription: '',
    contactPhone: '',
    contactName: '',
    address: ''
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getLocationById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑场地'
      Object.assign(locationForm, {
        id: res.data.id,
        name: res.data.name,
        type: res.data.type,
        price: res.data.price,
        status: res.data.status,
        locationDescription: res.data.locationDescription,
        contactPhone: res.data.contactPhone,
        contactName: res.data.contactName,
        address: res.data.address
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取场地详情失败:', error)
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
      name: locationForm.name,
      type: locationForm.type,
      price: locationForm.price,
      status: locationForm.status,
      locationDescription: locationForm.locationDescription,
      contactPhone: locationForm.contactPhone,
      contactName: locationForm.contactName,
      address: locationForm.address
    }
    
    let res
    if (locationForm.id) {
      res = await updateLocation({ ...data, id: locationForm.id })
    } else {
      res = await addLocation(data)
    }
    
    if (res.code === 200) {
      message.success(locationForm.id ? '更新成功' : '创建成功')
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
    const res = await deleteLocation(id)
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
.location-management {
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
