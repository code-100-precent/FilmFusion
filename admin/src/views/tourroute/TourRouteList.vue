<template>
  <div class="tour-route-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="线路名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入线路名称" clearable @keyup.enter="handleSearch" />
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
            新增线路
          </n-button>
        </div>
      </div>
      
      <n-data-table
        :columns="columns"
        :data="tourRouteList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>
    
    <n-modal v-model:show="dialogVisible" preset="dialog" :title="dialogTitle" style="width: 900px">
      <n-form ref="formRef" :model="tourRouteForm" :rules="formRules" label-placement="left" label-width="100">
        <n-form-item label="线路名称" path="name">
          <n-input v-model:value="tourRouteForm.name" placeholder="请输入线路名称" />
        </n-form-item>
        <n-form-item label="线路描述" path="description">
          <n-input v-model:value="tourRouteForm.description" type="textarea" :rows="4" placeholder="请输入线路描述" />
        </n-form-item>
        <n-form-item label="主题标签" path="theme">
          <n-input v-model:value="tourRouteForm.theme" placeholder="如：熊猫文化、茶文化等" />
        </n-form-item>
        <n-form-item label="特色描述" path="features">
          <n-input v-model:value="tourRouteForm.features" type="textarea" :rows="3" placeholder="请输入线路特色" />
        </n-form-item>
        <n-form-item label="交通方式" path="transportInfo">
          <n-input v-model:value="tourRouteForm.transportInfo" type="textarea" :rows="3" placeholder="请输入交通方式信息" />
        </n-form-item>
        <n-form-item label="住宿推荐" path="accommodation">
          <n-input v-model:value="tourRouteForm.accommodation" type="textarea" :rows="3" placeholder="请输入住宿推荐" />
        </n-form-item>
        <n-form-item label="美食推荐" path="foodRecommendation">
          <n-input v-model:value="tourRouteForm.foodRecommendation" type="textarea" :rows="3" placeholder="请输入美食推荐" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-select v-model:value="tourRouteForm.status" :options="statusOptions" />
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
  useMessage
} from 'naive-ui'
import { getTourRoutePage, addTourRoute, updateTourRoute, deleteTourRoute, getTourRouteById } from '@/api'

const message = useMessage()

const loading = ref(false)
const tourRouteList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增线路')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const tourRouteForm = reactive({
  id: null,
  name: '',
  description: '',
  theme: '',
  features: '',
  transportInfo: '',
  accommodation: '',
  foodRecommendation: '',
  status: 1
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const statusOptions = [
  { label: '上线', value: 1 },
  { label: '下线', value: 0 }
]

const formRules = {
  name: [{ required: true, message: '请输入线路名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入线路描述', trigger: 'blur' }],
  theme: [{ required: true, message: '请输入主题标签', trigger: 'blur' }],
  features: [{ required: true, message: '请输入特色描述', trigger: 'blur' }]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '线路名称', key: 'name', ellipsis: { tooltip: true } },
  { title: '主题', key: 'theme', width: 120 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => row.status === 1 ? '上线' : '下线'
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
    const res = await getTourRoutePage(pagination.page, pagination.pageSize, searchForm.keyword)
    if (res.code === 200) {
      tourRouteList.value = res.data || []
      pagination.itemCount = res.pagination?.totalItems || 0
    }
  } catch (error) {
    console.error('加载线路列表失败:', error)
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
  dialogTitle.value = '新增线路'
  Object.assign(tourRouteForm, {
    id: null,
    name: '',
    description: '',
    theme: '',
    features: '',
    transportInfo: '',
    accommodation: '',
    foodRecommendation: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getTourRouteById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑线路'
      Object.assign(tourRouteForm, res.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取线路详情失败:', error)
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
      name: tourRouteForm.name,
      description: tourRouteForm.description,
      theme: tourRouteForm.theme,
      features: tourRouteForm.features,
      transportInfo: tourRouteForm.transportInfo,
      accommodation: tourRouteForm.accommodation,
      foodRecommendation: tourRouteForm.foodRecommendation,
      status: tourRouteForm.status
    }
    
    let res
    if (tourRouteForm.id) {
      res = await updateTourRoute({ ...data, id: tourRouteForm.id })
    } else {
      res = await addTourRoute(data)
    }
    
    if (res.code === 200) {
      message.success(tourRouteForm.id ? '更新成功' : '创建成功')
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
    const res = await deleteTourRoute(id)
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
.tour-route-management {
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

