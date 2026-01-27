<template>
  <div class="module-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="模块名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入模块名称" clearable @keyup.enter="handleSearch" />
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
            新增模块
          </n-button>
        </div>
      </div>

      <!-- 桌面端表格 -->
      <template v-if="!isMobile">
        <n-data-table
            :columns="columns"
            :data="moduleList"
            :loading="loading"
            :row-key="row => row.id"
        />

        <!-- 独立分页组件 -->
        <div class="pagination-container" v-if="pagination.itemCount > 0">
          <n-pagination
              v-model:page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :page-count="Math.ceil(pagination.itemCount / pagination.pageSize)"
              :item-count="pagination.itemCount"
              :page-sizes="pagination.pageSizes"
              show-size-picker
              show-quick-jumper
              @update:page="handlePageChange"
              @update:page-size="handlePageSizeChange"
          />
        </div>
      </template>

      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-list">
        <n-spin :show="loading">
          <div v-if="moduleList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:folder-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
                v-for="module in moduleList"
                :key="module.id"
                class="mobile-card"
                hoverable
            >
              <div class="card-header">
                <h3 class="module-name">{{ module.name }}</h3>
              </div>

              <div class="card-content">
                <div class="info-item">
                  <span class="label">描述：</span>
                  <span class="text-ellipsis">{{ module.description || '-' }}</span>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(module)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(module.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个模块吗？
                </n-popconfirm>
              </div>
            </n-card>
          </div>

          <!-- 移动端分页 -->
          <div class="mobile-pagination" v-if="pagination.itemCount > 0">
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
        style="width: 90%; max-width: 600px"
        :mask-closable="false"
    >
      <n-form
          ref="formRef"
          :model="moduleForm"
          :rules="formRules"
          :label-placement="isMobile ? 'top' : 'left'"
          :label-width="isMobile ? 'auto' : '100'"
      >
        <n-form-item label="模块名称" path="name">
          <n-input v-model:value="moduleForm.name" placeholder="请输入模块名称" />
        </n-form-item>
        <n-form-item label="模块描述" path="description">
          <n-input v-model:value="moduleForm.description" type="textarea" :rows="4" placeholder="请输入模块描述" />
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
  NDataTable,
  NPopconfirm,
  NModal,
  NSpin,
  NPagination,
  useMessage
} from 'naive-ui'
import { getModulePage, createModule, updateModule, deleteModule, getModuleById } from '@/api'
import dayjs from 'dayjs'

const message = useMessage()

const isMobile = ref(false)
const loading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const moduleList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增模块')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const moduleForm = reactive({
  id: null,
  name: '',
  description: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  name: [
    { required: true, message: '请输入模块名称', trigger: 'blur' },
    { min: 1, max: 50, message: '名称长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '模块名称', key: 'name', width: 200 },
  { title: '模块描述', key: 'description', ellipsis: { tooltip: true } },
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
              trigger: () => h(NButton, { size: 'small', type: 'error', quaternary: true }, { default: () => '删除' }),
              default: () => '确定要删除这个模块吗？'
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
    const res = await getModulePage(pagination.page, pagination.pageSize, searchForm.keyword)

    if (res.code === 200) {
      const listData = Array.isArray(res.data) 
        ? res.data 
        : (res.data?.records || res.data?.list || [])
      
      const totalItems = res.pagination?.totalItems || res.data?.total || res.data?.totalItems || 0

      moduleList.value = listData
      pagination.itemCount = totalItems
    } else {
      message.error(`获取模块失败: ${res.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('加载模块列表失败:', error)
    // 如果是 404 错误，说明后端接口未启动
    if (error.httpStatus === 404) {
      message.warning('模块功能暂未启用，请先启动后端服务')
      moduleList.value = []
      pagination.itemCount = 0
    } else {
      message.error('加载模块列表失败')
    }
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

const resetForm = () => {
  Object.assign(moduleForm, {
    id: null,
    name: '',
    description: ''
  })

  if (formRef.value) {
    formRef.value.restoreValidation()
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增模块'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑模块'
  dialogVisible.value = true
  try {
    dialogLoading.value = true
    const res = await getModuleById(row.id)
    if (res.code === 200 && res.data) {
      const module = res.data
      Object.assign(moduleForm, {
        id: module.id,
        name: module.name || '',
        description: module.description || ''
      })
    }
  } catch (error) {
    console.error('获取模块详情失败:', error)
    message.error('获取模块详情失败')
  } finally {
    dialogLoading.value = false
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
      name: moduleForm.name,
      description: moduleForm.description
    }

    if (moduleForm.id) {
      const res = await updateModule(moduleForm.id, data)
      if (res.code === 200) {
        message.success('更新成功')
        dialogVisible.value = false
        loadData()
      } else {
        message.error(res.message || '更新失败')
      }
    } else {
      const res = await createModule(data)
      if (res.code === 200) {
        message.success('创建成功')
        dialogVisible.value = false
        loadData()
      } else {
        message.error(res.message || '创建失败')
      }
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
    const res = await deleteModule(id)
    if (res.code === 200) {
      message.success('删除成功')
      loadData()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}
</script>

<style scoped>
.module-management {
  padding: 16px;
}

.management-card {
  min-height: calc(100vh - 100px);
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 16px;
}

.search-form {
  flex: 1;
  min-width: 300px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* Mobile Responsive Styles */
@media (max-width: 768px) {
  .module-management {
    padding: 8px;
  }

  .management-card {
    border-radius: 8px;
  }

  .search-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-form {
    width: 100%;
  }

  .action-buttons {
    width: 100%;
  }

  .action-buttons button {
    width: 100%;
  }

  .mobile-card {
    margin-bottom: 12px;
    border-radius: 8px;
  }

  .card-header {
    margin-bottom: 12px;
  }

  .module-name {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
  }

  .card-content {
    margin-bottom: 16px;
    padding: 12px;
    background-color: #f9fafb;
    border-radius: 6px;
  }

  .info-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 13px;
  }

  .info-item:last-child {
    margin-bottom: 0;
  }

  .info-item .label {
    color: #6b7280;
    flex-shrink: 0;
  }

  .text-ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 70%;
  }

  .card-actions {
    display: flex;
    gap: 8px;
  }

  .mobile-pagination {
    display: flex;
    justify-content: center;
    margin-top: 16px;
    padding-bottom: 16px;
  }
}
</style>
