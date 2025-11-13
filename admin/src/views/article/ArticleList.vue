<template>
  <div class="article-management animate-fade-in">
    <el-card class="card animate-slide-in-up">
      <template #header>
        <div class="card-header">
          <span class="card-title">文章管理</span>
          <el-button type="primary" @click="handleAdd" class="btn-transition">
            <el-icon><Plus /></el-icon>
            新增文章
          </el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="文章标题">
            <el-input v-model="searchForm.articleTitle" placeholder="请输入文章标题" />
          </el-form-item>
          <el-form-item label="发布单位">
            <el-input v-model="searchForm.issueUnit" placeholder="请输入发布单位" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" class="mr-2">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 数据表格 -->
      <el-table 
        v-loading="loading" 
        :data="articleList" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="article_id" label="文章ID" width="80" />
        <el-table-column prop="article_title" label="文章标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="issue_unit" label="发布单位" width="150" />
        <el-table-column prop="issue_time" label="发布时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.issue_time) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-dropdown>
              <el-button size="small" type="text">
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleView(scope.row)">查看</el-dropdown-item>
                  <el-dropdown-item @click="handleEdit(scope.row)">编辑</el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(scope.row.article_id)" danger>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 文章详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      destroy-on-close
    >
      <el-form
        ref="articleFormRef"
        :model="articleForm"
        :rules="formRules"
        label-width="100px"
        class="article-form"
      >
        <el-form-item label="文章标题" prop="article_title">
          <el-input v-model="articleForm.article_title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="发布单位" prop="issue_unit">
          <el-input v-model="articleForm.issue_unit" placeholder="请输入发布单位" />
        </el-form-item>
        <el-form-item label="发布时间" prop="issue_time">
          <el-date-picker
            v-model="articleForm.issue_time"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="文章内容" prop="content">
          <el-input
            v-model="articleForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入文章内容"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDialogSave" :loading="dialogLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, More } from '@element-plus/icons-vue'

// 模拟数据
const articleList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('')
const currentAction = ref('')
const articleFormRef = ref(null)
const selectedRows = ref([])

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  articleTitle: '',
  issueUnit: ''
})

// 文章表单
const articleForm = reactive({
  article_id: '',
  article_title: '',
  issue_unit: '',
  issue_time: '',
  content: ''
})

// 表单验证规则
const formRules = {
  article_title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { max: 50, message: '标题长度不能超过50个字符', trigger: 'blur' }
  ],
  issue_unit: [
    { required: true, message: '请输入发布单位', trigger: 'blur' },
    { max: 50, message: '发布单位长度不能超过50个字符', trigger: 'blur' }
  ],
  issue_time: [
    { required: true, message: '请选择发布时间', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' }
  ]
}

// 初始加载数据
onMounted(() => {
  loadArticleList()
})

// 加载文章列表
const loadArticleList = async () => {
  try {
    loading.value = true
    // 这里应该调用API接口，暂时使用模拟数据
    // const res = await getArticlePage({
    //   page: pagination.currentPage,
    //   pageSize: pagination.pageSize,
    //   ...searchForm
    // })
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    articleList.value = [
      {
        article_id: 1,
        article_title: '雅安市影视产业发展情况报告',
        issue_unit: '雅安市文化旅游局',
        issue_time: '2023-06-15 09:30:00',
        content: '雅安市近年来大力发展影视产业...'
      },
      {
        article_id: 2,
        article_title: '雅安影视城建设进度通报',
        issue_unit: '雅安市政府',
        issue_time: '2023-07-20 14:20:00',
        content: '雅安影视城一期工程已完成80%...'
      }
    ]
    pagination.total = articleList.value.length
  } catch (error) {
    ElMessage.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadArticleList()
}

// 处理重置
const handleReset = () => {
  searchForm.articleTitle = ''
  searchForm.issueUnit = ''
  pagination.currentPage = 1
  loadArticleList()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadArticleList()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  loadArticleList()
}

// 处理选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理新增
const handleAdd = () => {
  dialogTitle.value = '新增文章'
  currentAction.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑文章'
  currentAction.value = 'edit'
  Object.assign(articleForm, row)
  dialogVisible.value = true
}

// 处理查看
const handleView = (row) => {
  dialogTitle.value = '查看文章'
  currentAction.value = 'view'
  Object.assign(articleForm, row)
  dialogVisible.value = true
}

// 处理删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里应该调用API接口
    // const res = await deleteArticle(id)
    
    // 模拟删除
    await new Promise(resolve => setTimeout(resolve, 300))
    ElMessage.success('删除成功')
    loadArticleList()
  } catch (error) {
    // 用户取消删除时不显示错误信息
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  if (articleFormRef.value) {
    articleFormRef.value.resetFields()
  }
  Object.assign(articleForm, {
    article_id: '',
    article_title: '',
    issue_unit: '',
    issue_time: new Date(),
    content: ''
  })
}

// 处理对话框保存
const handleDialogSave = async () => {
  if (!articleFormRef.value) return
  
  await articleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        dialogLoading.value = true
        
        if (currentAction.value === 'add') {
          // 这里应该调用API接口
          // const res = await addArticle(articleForm)
          
          // 模拟添加
          await new Promise(resolve => setTimeout(resolve, 500))
          ElMessage.success('新增成功')
        } else if (currentAction.value === 'edit') {
          // 这里应该调用API接口
          // const res = await updateArticle(articleForm)
          
          // 模拟更新
          await new Promise(resolve => setTimeout(resolve, 500))
          ElMessage.success('更新成功')
        }
        
        dialogVisible.value = false
        loadArticleList()
      } catch (error) {
        ElMessage.error(currentAction.value === 'add' ? '新增失败' : '更新失败')
      } finally {
        dialogLoading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.article-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebe6ff;
  background: linear-gradient(135deg, #f9f7ff 0%, #f3f0ff 100%);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #9c88ff 0%, #b8abff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.search-section {
  padding: 16px 20px;
  border-bottom: 1px solid #ebe6ff;
}

.search-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.article-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

.mr-2 {
  margin-right: 8px;
}
</style>