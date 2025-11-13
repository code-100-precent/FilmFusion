<template>
  <div class="drama-management animate-fade-in">
    <el-card class="card animate-slide-in-up">
      <template #header>
        <div class="card-header">
          <span class="card-title">电视剧管理</span>
          <el-button type="primary" @click="handleAdd" class="btn-transition">
            <el-icon><Plus /></el-icon>
            新增电视剧
          </el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="电视剧名称">
            <el-input v-model="searchForm.dramaName" placeholder="请输入电视剧名称" />
          </el-form-item>
          <el-form-item label="备案号">
            <el-input v-model="searchForm.filingNum" placeholder="请输入备案号" />
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
        :data="dramaList" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="drama_id" label="电视剧ID" width="80" />
        <el-table-column prop="drama_name" label="电视剧名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="filing_num" label="备案号" width="150" />
        <el-table-column prop="prod_company" label="出品公司" width="200" show-overflow-tooltip />
        <el-table-column prop="shoot_location" label="拍摄地" width="150" />
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
                  <el-dropdown-item @click="handleDelete(scope.row.drama_id)" danger>删除</el-dropdown-item>
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
    
    <!-- 电视剧详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      destroy-on-close
    >
      <el-form
        ref="dramaFormRef"
        :model="dramaForm"
        :rules="formRules"
        label-width="100px"
        class="drama-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电视剧名称" prop="drama_name">
              <el-input v-model="dramaForm.drama_name" placeholder="请输入电视剧名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备案号" prop="filing_num">
              <el-input v-model="dramaForm.filing_num" placeholder="请输入备案号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出品公司" prop="prod_company">
              <el-input v-model="dramaForm.prod_company" placeholder="请输入出品公司" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="演员" prop="cast">
              <el-input v-model="dramaForm.cast" placeholder="请输入演员" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拍摄地" prop="shoot_location">
              <el-input v-model="dramaForm.shoot_location" placeholder="请输入拍摄地" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拍摄地ID" prop="location_id">
              <el-input v-model="dramaForm.location_id" placeholder="请输入拍摄地ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="公司简介" prop="crew_description">
              <el-input
                v-model="dramaForm.crew_description"
                type="textarea"
                :rows="3"
                placeholder="请输入公司简介"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="电视剧简介" prop="drama_description">
              <el-input
                v-model="dramaForm.drama_description"
                type="textarea"
                :rows="3"
                placeholder="请输入电视剧简介"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="协拍服务" prop="service">
              <el-input v-model="dramaForm.service" placeholder="请输入协拍服务" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="协拍服务ID" prop="service_id">
              <el-input v-model="dramaForm.service_id" placeholder="请输入协拍服务ID" />
            </el-form-item>
          </el-col>
        </el-row>
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
const dramaList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('')
const currentAction = ref('')
const dramaFormRef = ref(null)
const selectedRows = ref([])

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  dramaName: '',
  filingNum: ''
})

// 电视剧表单
const dramaForm = reactive({
  drama_id: '',
  drama_name: '',
  filing_num: '',
  prod_company: '',
  crew_description: '',
  drama_description: '',
  cast: '',
  shoot_location: '',
  location_id: '',
  service: '',
  service_id: ''
})

// 表单验证规则
const formRules = {
  drama_name: [
    { required: true, message: '请输入电视剧名称', trigger: 'blur' },
    { max: 20, message: '电视剧名称长度不能超过20个字符', trigger: 'blur' }
  ],
  filing_num: [
    { required: true, message: '请输入备案号', trigger: 'blur' },
    { max: 20, message: '备案号长度不能超过20个字符', trigger: 'blur' }
  ],
  prod_company: [
    { required: true, message: '请输入出品公司', trigger: 'blur' },
    { max: 100, message: '出品公司长度不能超过100个字符', trigger: 'blur' }
  ],
  crew_description: [
    { required: true, message: '请输入公司简介', trigger: 'blur' },
    { max: 200, message: '公司简介长度不能超过200个字符', trigger: 'blur' }
  ],
  drama_description: [
    { required: true, message: '请输入电视剧简介', trigger: 'blur' },
    { max: 100, message: '电视剧简介长度不能超过100个字符', trigger: 'blur' }
  ],
  cast: [
    { required: true, message: '请输入演员', trigger: 'blur' },
    { max: 100, message: '演员长度不能超过100个字符', trigger: 'blur' }
  ],
  shoot_location: [
    { required: true, message: '请输入拍摄地', trigger: 'blur' },
    { max: 100, message: '拍摄地长度不能超过100个字符', trigger: 'blur' }
  ],
  location_id: [
    { required: true, message: '请输入拍摄地ID', trigger: 'blur' },
    { max: 50, message: '拍摄地ID长度不能超过50个字符', trigger: 'blur' }
  ],
  service: [
    { required: true, message: '请输入协拍服务', trigger: 'blur' },
    { max: 100, message: '协拍服务长度不能超过100个字符', trigger: 'blur' }
  ],
  service_id: [
    { required: true, message: '请输入协拍服务ID', trigger: 'blur' },
    { max: 100, message: '协拍服务ID长度不能超过100个字符', trigger: 'blur' }
  ]
}

// 初始加载数据
onMounted(() => {
  loadDramaList()
})

// 加载电视剧列表
const loadDramaList = async () => {
  try {
    loading.value = true
    // 这里应该调用API接口，暂时使用模拟数据
    // const res = await getDramaPage({
    //   page: pagination.currentPage,
    //   pageSize: pagination.pageSize,
    //   ...searchForm
    // })
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    dramaList.value = [
      {
        drama_id: 1,
        drama_name: '雅安故事',
        filing_num: '2023001',
        prod_company: '雅安影视传媒有限公司',
        crew_description: '专业的影视制作公司...',
        drama_description: '讲述雅安风景和人文故事的电视剧',
        cast: '张三, 李四, 王五',
        shoot_location: '碧峰峡景区',
        location_id: 'loc001',
        service: '场地协调, 道具租赁',
        service_id: 'serv001'
      },
      {
        drama_id: 2,
        drama_name: '青衣江之恋',
        filing_num: '2023002',
        prod_company: '四川影视集团',
        crew_description: '大型影视制作集团...',
        drama_description: '爱情题材电视剧',
        cast: '赵六, 钱七',
        shoot_location: '青衣江边',
        location_id: 'loc002',
        service: '交通协调, 住宿安排',
        service_id: 'serv002'
      }
    ]
    pagination.total = dramaList.value.length
  } catch (error) {
    ElMessage.error('加载电视剧列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadDramaList()
}

// 处理重置
const handleReset = () => {
  searchForm.dramaName = ''
  searchForm.filingNum = ''
  pagination.currentPage = 1
  loadDramaList()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadDramaList()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  loadDramaList()
}

// 处理选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理新增
const handleAdd = () => {
  dialogTitle.value = '新增电视剧'
  currentAction.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑电视剧'
  currentAction.value = 'edit'
  Object.assign(dramaForm, row)
  dialogVisible.value = true
}

// 处理查看
const handleView = (row) => {
  dialogTitle.value = '查看电视剧'
  currentAction.value = 'view'
  Object.assign(dramaForm, row)
  dialogVisible.value = true
}

// 处理删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这部电视剧吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里应该调用API接口
    // const res = await deleteDrama(id)
    
    // 模拟删除
    await new Promise(resolve => setTimeout(resolve, 300))
    ElMessage.success('删除成功')
    loadDramaList()
  } catch (error) {
    // 用户取消删除时不显示错误信息
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  if (dramaFormRef.value) {
    dramaFormRef.value.resetFields()
  }
  Object.assign(dramaForm, {
    drama_id: '',
    drama_name: '',
    filing_num: '',
    prod_company: '',
    crew_description: '',
    drama_description: '',
    cast: '',
    shoot_location: '',
    location_id: '',
    service: '',
    service_id: ''
  })
}

// 处理对话框保存
const handleDialogSave = async () => {
  if (!dramaFormRef.value) return
  
  await dramaFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        dialogLoading.value = true
        
        if (currentAction.value === 'add') {
          // 这里应该调用API接口
          // const res = await addDrama(dramaForm)
          
          // 模拟添加
          await new Promise(resolve => setTimeout(resolve, 500))
          ElMessage.success('新增成功')
        } else if (currentAction.value === 'edit') {
          // 这里应该调用API接口
          // const res = await updateDrama(dramaForm)
          
          // 模拟更新
          await new Promise(resolve => setTimeout(resolve, 500))
          ElMessage.success('更新成功')
        }
        
        dialogVisible.value = false
        loadDramaList()
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
.drama-management {
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

.drama-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

.mr-2 {
  margin-right: 8px;
}
</style>