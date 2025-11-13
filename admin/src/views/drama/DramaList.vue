<template>
  <div class="drama-management">
    <n-card class="management-card">
      <n-form :model="searchForm" inline class="search-form">
        <n-form-item label="关键词">
          <n-input v-model:value="searchForm.keyword" placeholder="请输入剧名或关键词" clearable @keyup.enter="handleSearch" />
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
      
      <div class="action-bar">
        <n-button type="primary" @click="handleAdd">
          <template #icon>
            <Icon icon="mdi:plus" />
          </template>
          新增电视剧
        </n-button>
      </div>
      
      <n-data-table
        :columns="columns"
        :data="dramaList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>
    
    <n-modal v-model:show="dialogVisible" preset="dialog" :title="dialogTitle" style="width: 800px">
      <n-form ref="formRef" :model="dramaForm" :rules="formRules" label-placement="left" label-width="100">
        <n-form-item label="剧名" path="name">
          <n-input v-model:value="dramaForm.name" placeholder="请输入剧名" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="dramaForm.type" :options="typeOptions" placeholder="请选择类型" />
        </n-form-item>
        <n-form-item label="集数" path="episodes">
          <n-input-number v-model:value="dramaForm.episodes" placeholder="请输入集数" :min="1" style="width: 100%" />
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input v-model:value="dramaForm.description" type="textarea" :rows="5" placeholder="请输入描述" />
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
  NPopconfirm,
  NModal,
  useMessage
} from 'naive-ui'
import { getDramaPage, addDrama, updateDrama, deleteDrama, getDramaById } from '@/api'
import dayjs from 'dayjs'

const message = useMessage()

const loading = ref(false)
const dramaList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增电视剧')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const dramaForm = reactive({
  id: null,
  name: '',
  type: '',
  episodes: 0,
  description: ''
})

const typeOptions = [
  { label: '古装剧', value: 'COSTUME' },
  { label: '现代剧', value: 'MODERN' },
  { label: '年代剧', value: 'PERIOD' },
  { label: '其他', value: 'OTHER' }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  name: [{ required: true, message: '请输入剧名', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  episodes: [{ required: true, message: '请输入集数', trigger: 'blur' }]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '剧名', key: 'name', ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'type',
    width: 120,
    render: (row) => {
      const typeMap = {
        'COSTUME': '古装剧',
        'MODERN': '现代剧',
        'PERIOD': '年代剧',
        'OTHER': '其他'
      }
      return typeMap[row.type] || row.type
    }
  },
  { title: '集数', key: 'episodes', width: 100 },
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
  loadData()
})

const loadData = async () => {
  try {
    loading.value = true
    const res = await getDramaPage(pagination.page, pagination.pageSize, searchForm.keyword)
    if (res.code === 200) {
      dramaList.value = res.data || []
      pagination.itemCount = res.pagination?.totalItems || 0
    }
  } catch (error) {
    console.error('加载电视剧列表失败:', error)
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
  dialogTitle.value = '新增电视剧'
  Object.assign(dramaForm, {
    id: null,
    name: '',
    type: '',
    episodes: 0,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getDramaById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑电视剧'
      Object.assign(dramaForm, {
        id: res.data.id,
        name: res.data.name,
        type: res.data.type,
        episodes: res.data.episodes,
        description: res.data.description
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取电视剧详情失败:', error)
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
      name: dramaForm.name,
      type: dramaForm.type,
      episodes: dramaForm.episodes,
      description: dramaForm.description
    }
    
    let res
    if (dramaForm.id) {
      res = await updateDrama({ ...data, id: dramaForm.id })
    } else {
      res = await addDrama(data)
    }
    
    if (res.code === 200) {
      message.success(dramaForm.id ? '更新成功' : '创建成功')
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
    const res = await deleteDrama(id)
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
.drama-management {
  animation: fadeIn 0.3s ease;
}

.management-card {
  :deep(.n-card__content) {
    padding: 16px;
  }
}

.search-form {
  margin-bottom: 16px;
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
