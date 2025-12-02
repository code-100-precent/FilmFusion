<template>
  <div class="location-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="场地名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入场地名称" clearable @keyup.enter="handleSearch" />
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
      
      <!-- 桌面端表格 -->
      <n-data-table
        v-if="!isMobile"
        :columns="columns"
        :data="locationList"
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
          <div v-if="locationList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:map-marker-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
              v-for="location in locationList"
              :key="location.id"
              class="mobile-card"
              hoverable
            >
              <div class="card-header">
                <div class="location-info">
                  <h3 class="location-name">{{ location.name }}</h3>
                  <p class="location-type">{{ location.type }}</p>
                </div>
                <div class="location-cover">
                  <n-image
                    v-if="location.cover"
                    :src="location.cover"
                    width="80"
                    height="60"
                    object-fit="cover"
                    preview-disabled
                  />
                  <div v-else class="no-cover">
                    <Icon icon="mdi:image" :width="32" />
                  </div>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <span class="label">地址：</span>
                  <span>{{ location.address || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">联系人：</span>
                  <span>{{ location.locationPrincipalName || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">联系电话：</span>
                  <span>{{ location.locationPrincipalPhone || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">价格：</span>
                  <span class="price">{{ location.price ? '¥' + location.price : '-' }}</span>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(location)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(location.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个场地吗？
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
        :model="locationForm" 
        :rules="formRules" 
        :label-placement="isMobile ? 'top' : 'left'"
        :label-width="isMobile ? 'auto' : '120'"
      >
        <n-form-item label="场地名称" path="name">
          <n-input v-model:value="locationForm.name" placeholder="请输入场地名称" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="locationForm.type" :options="typeOptions" placeholder="请选择场地类型" />
        </n-form-item>
        <n-form-item label="地址" path="address">
          <n-input v-model:value="locationForm.address" placeholder="请输入详细地址" />
        </n-form-item>
        <n-form-item label="价格" path="price">
          <n-input-number 
            v-model:value="locationForm.price" 
            placeholder="请输入价格（元）" 
            :precision="2"
            :min="0"
            style="width: 100%"
          />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-switch v-model:value="locationForm.status">
            <template #checked>可用</template>
            <template #unchecked>不可用</template>
          </n-switch>
        </n-form-item>
        <n-form-item label="场地介绍" path="locationDescription">
          <n-input v-model:value="locationForm.locationDescription" type="textarea" :rows="4" placeholder="请输入场地介绍" />
        </n-form-item>
        <n-form-item label="场地联系人" path="locationPrincipalName">
          <n-input v-model:value="locationForm.locationPrincipalName" placeholder="请输入场地联系人姓名" />
        </n-form-item>
        <n-form-item label="场地联系电话" path="locationPrincipalPhone">
          <n-input v-model:value="locationForm.locationPrincipalPhone" placeholder="请输入场地联系电话" />
        </n-form-item>
        <n-form-item label="政府联系人" path="govPrincipalName">
          <n-input v-model:value="locationForm.govPrincipalName" placeholder="请输入政府联系人姓名" />
        </n-form-item>
        <n-form-item label="政府联系电话" path="govPrincipalPhone">
          <n-input v-model:value="locationForm.govPrincipalPhone" placeholder="请输入政府联系电话" />
        </n-form-item>
        <n-form-item label="经度" path="longitude">
          <n-input v-model:value="locationForm.longitude" placeholder="请输入经度" />
        </n-form-item>
        <n-form-item label="纬度" path="latitude">
          <n-input v-model:value="locationForm.latitude" placeholder="请输入纬度" />
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
  NInputNumber,
  NSelect,
  NSwitch,
  NDataTable,
  NPopconfirm,
  NModal,
  NImage,
  NSpin,
  NPagination,
  useMessage
} from 'naive-ui'
import { getLocationPage, addLocation, updateLocation, deleteLocation, getLocationById } from '@/api'
import dayjs from 'dayjs'

const message = useMessage()

const isMobile = ref(false)
const loading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

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
  address: '',
  price: 0,
  status: true,
  locationDescription: '',
  locationPrincipalName: '',
  locationPrincipalPhone: '',
  govPrincipalName: '',
  govPrincipalPhone: '',
  longitude: '',
  latitude: ''
})

const typeOptions = [
  { label: '自然风光', value: 'natural' },
  { label: '历史建筑', value: 'historical' },
  { label: '现代建筑', value: 'modern' },
  { label: '文化场所', value: 'cultural' },
  { label: '商业场所', value: 'commercial' },
  { label: '其他', value: 'other' }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  name: [
    { required: true, message: '请输入场地名称', trigger: 'blur' },
    { min: 1, max: 100, message: '场地名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择场地类型', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入地址', trigger: 'blur' },
    { min: 1, max: 200, message: '地址长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  locationPrincipalPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  govPrincipalPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  {
      title: '封面',
      key: 'cover',
      width: 100,
      render: (row) => {
        // 优先使用缩略图URL（thumbUrl属性），如果没有则使用原图URL
        const thumbnailUrl = row.thumbUrl;
        // 获取原图URL用于预览
        const originalUrl = row.cover || '';
        
        return h(NImage, {
          width: 60,
          height: 45,
          src: thumbnailUrl || originalUrl, // 显示压缩后的图片
          objectFit: 'cover',
          previewDisabled: false, // 启用预览功能
          showToolbar: false,
          // 配置预览功能，点击时显示原图
          srcset: [
            {
              src: originalUrl,
              alt: '场地封面'
            }
          ]
        })
      }
    },
  { title: '场地名称', key: 'name', width: 180, ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'type',
    width: 120,
    render: (row) => {
      const typeMap = {
        'natural': '自然风光',
        'historical': '历史建筑',
        'modern': '现代建筑',
        'cultural': '文化场所',
        'commercial': '商业场所',
        'other': '其他'
      }
      return typeMap[row.type] || row.type
    }
  },
  { title: '地址', key: 'address', width: 200, ellipsis: { tooltip: true } },
  {
    title: '价格',
    key: 'price',
    width: 100,
    render: (row) => {
      return row.price ? '¥' + row.price : '-'
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      return h('span', {
        class: row.status ? 'status-available' : 'status-unavailable'
      }, row.status ? '可用' : '不可用')
    }
  },
  { title: '场地联系人', key: 'locationPrincipalName', width: 120 },
  { title: '场地联系电话', key: 'locationPrincipalPhone', width: 130 },
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
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

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
    message.error('加载场地列表失败')
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
    address: '',
    price: 0,
    status: true,
    locationDescription: '',
    locationPrincipalName: '',
    locationPrincipalPhone: '',
    govPrincipalName: '',
    govPrincipalPhone: '',
    longitude: '',
    latitude: ''
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
        name: res.data.name || '',
        type: res.data.type || '',
        address: res.data.address || '',
        price: res.data.price || 0,
        status: res.data.status !== false,
        locationDescription: res.data.locationDescription || '',
        locationPrincipalName: res.data.locationPrincipalName || '',
        locationPrincipalPhone: res.data.locationPrincipalPhone || '',
        govPrincipalName: res.data.govPrincipalName || '',
        govPrincipalPhone: res.data.govPrincipalPhone || '',
        longitude: res.data.longitude || '',
        latitude: res.data.latitude || ''
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取场地详情失败:', error)
    message.error('获取场地详情失败')
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
      address: locationForm.address,
      price: locationForm.price,
      status: locationForm.status,
      locationDescription: locationForm.locationDescription,
      locationPrincipalName: locationForm.locationPrincipalName,
      locationPrincipalPhone: locationForm.locationPrincipalPhone,
      govPrincipalName: locationForm.govPrincipalName,
      govPrincipalPhone: locationForm.govPrincipalPhone,
      longitude: locationForm.longitude,
      latitude: locationForm.latitude
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

.status-available {
  color: #18a058;
  font-weight: 500;
}

.status-unavailable {
  color: #d03050;
  font-weight: 500;
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
      
      .location-info {
        flex: 1;
        margin-right: 12px;
        
        .location-name {
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 4px 0;
        }
        
        .location-type {
          font-size: 14px;
          color: #6b7280;
          margin: 0;
        }
      }
      
      .location-cover {
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
        
        .price {
          color: #f56c6c;
          font-weight: 500;
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