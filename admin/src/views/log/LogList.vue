<template>
  <div class="log-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="关键字搜索">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入关键字" clearable @keyup.enter="handleSearch" />
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
      
      <n-data-table
        :columns="columns"
        :data="logList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, h } from 'vue'
import { getLogPage } from '@/api'
import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NSpace } from 'naive-ui'
import { Icon } from '@iconify/vue'

export default {
  name: 'LogList',
  components: {
    NButton,
    NCard,
    NDataTable,
    NForm,
    NFormItem,
    NInput,
    Icon
  },
  setup() {
    const loading = ref(false)
    const logList = ref([])
    const searchForm = reactive({
      keyword: ''
    })
    
    const pagination = reactive({
      page: 1,
      pageSize: 10,
      pageCount: 1,
      itemCount: 0,
      showSizePicker: true,
      pageSizes: [10, 20, 50, 100],
      showQuickJumper: true,
      // 添加以下属性以确保Naive UI正确计算分页
      prefix: ({ itemCount }) => `共 ${itemCount} 条`
    })
    
    const columns = [
      {
        title: 'ID',
        key: 'id',
        width: 80
      },
      {
        title: '操作类型',
        key: 'type',
        width: 120
      },
      {
        title: '描述',
        key: 'description',
        width: 300
      },
      {
        title: '操作人',
        key: 'operator',
        width: 150
      },
      {
        title: '是否成功',
        key: 'success',
        width: 100,
        render(row) {
          return row.success ? '成功' : '失败'
        }
      },
      {
        title: '操作时间',
        key: 'timestamp',
        width: 200,
        render(row) {
          return new Date(row.timestamp).toLocaleString('zh-CN')
        }
      },
      {
        title: '操作',
        key: 'actions',
        width: 150,
        render(row) {
          return h(NSpace, null, {
            default: () => [
              h(NButton, {
                size: 'small',
                type: 'primary',
                onClick: () => handleEdit(row)
              }, { default: () => '编辑' }),
              h(NButton, {
                size: 'small',
                type: 'error',
                onClick: () => handleDelete(row.id)
              }, { default: () => '删除' })
            ]
          })
        }
      }
    ]
    
    const loadData = async () => {
      loading.value = true
      try {
        const res = await getLogPage(pagination.page, pagination.pageSize, searchForm.keyword)
        console.log('分页响应数据:', res) // 添加调试日志
        if (res.code === 200) {
          logList.value = res.data || []
          // 确保正确设置总数据量和总页数
          pagination.itemCount = res.pagination?.totalItems || 0
          pagination.pageCount = res.pagination?.totalPages || 1
          console.log('设置总数据量:', pagination.itemCount) // 添加调试日志
          console.log('设置总页数:', pagination.pageCount) // 添加调试日志
        }
      } catch (error) {
        console.error('获取日志列表失败:', error)
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
    
    const handleEdit = (log) => {
      // 这里可以实现编辑功能，比如打开编辑对话框
      console.log('编辑日志:', log)
      // 由于日志通常是只读的，这里只是展示功能
      window.alert('日志编辑功能暂未实现，通常日志为只读数据')
    }
    
    const handleDelete = (id) => {
      console.log('删除日志:', id)
      // 这里可以实现删除功能，通常需要确认对话框
      window.alert(`确定要删除ID为${id}的日志记录吗？`)
    }
    
    onMounted(() => {
      loadData()
    })
    
    return {
      searchForm,
      loading,
      logList,
      pagination,
      columns,
      handleSearch,
      handleReset,
      handlePageChange,
      handlePageSizeChange,
      handleEdit,
      handleDelete
    }
  }
}
</script>

<style scoped>
.log-management {
  padding: 20px;
}

.management-card {
  margin-bottom: 20px;
}

.search-header {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  align-items: center;
}
</style>