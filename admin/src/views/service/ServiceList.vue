<template>
  <div class="service-management animate-fade-in">
    <el-card class="card animate-slide-in-up">
      <template #header>
        <div class="card-header">
          <span class="card-title">服务管理</span>
          <el-button type="primary" @click="handleAdd" class="btn-transition">
            <el-icon><Plus /></el-icon>
            新增服务
          </el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="服务名称">
            <el-input v-model="searchForm.serviceName" placeholder="请输入服务名称" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value="" />
              <el-option label="可用" :value="1" />
              <el-option label="不可用" :value="0" />
            </el-select>
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
        :data="serviceList" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="service_id" label="服务ID" width="80" />
        <el-table-column prop="service_name" label="服务名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '可用' : '不可用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="service_address" label="服务地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contact_name" label="联系人" width="100" />
        <el-table-column prop="contact_phone" label="联系电话" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-dropdown trigger="hover" placement="bottom">
              <el-button size="small" type="text">
                ...
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleView(scope.row)">查看</el-dropdown-item>
                  <el-dropdown-item @click="handleEdit(scope.row)">编辑</el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(scope.row.service_id)" danger>删除</el-dropdown-item>
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
    
    <!-- 服务详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      destroy-on-close
    >
      <el-form
        ref="serviceFormRef"
        :model="serviceForm"
        :rules="formRules"
        label-width="100px"
        class="service-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务名称" prop="service_name">
              <el-input v-model="serviceForm.service_name" placeholder="请输入服务名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input v-model="serviceForm.price" placeholder="请输入价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="serviceForm.status">
                <el-radio :label="1">可用</el-radio>
                <el-radio :label="0">不可用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contact_name">
              <el-input v-model="serviceForm.contact_name" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contact_phone">
              <el-input v-model="serviceForm.contact_phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务地址" prop="service_address">
              <el-input v-model="serviceForm.service_address" placeholder="请输入服务地址" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="服务描述" prop="service_description">
              <el-input
                v-model="serviceForm.service_description"
                type="textarea"
                :rows="4"
                placeholder="请输入服务描述"
              />
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
import { Plus } from '@element-plus/icons-vue'

// 模拟数据
const serviceList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('')
const currentAction = ref('')
const serviceFormRef = ref(null)
const selectedRows = ref([])

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  serviceName: '',
  status: ''
})

// 服务表单
const serviceForm = reactive({
  service_id: '',
  service_name: '',
  service_description: '',
  price: '',
  status: 1,
  service_address: '',
  contact_phone: '',
  contact_name: ''
})

// 表单验证规则
const formRules = {
  service_name: [
    { required: true, message: '请输入服务名称', trigger: 'blur' },
    { max: 20, message: '服务名称长度不能超过20个字符', trigger: 'blur' }
  ],
  service_description: [
    { required: true, message: '请输入服务描述', trigger: 'blur' },
    { max: 200, message: '服务描述长度不能超过200个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { max: 20, message: '价格长度不能超过20个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  service_address: [
    { required: true, message: '请输入服务地址', trigger: 'blur' },
    { max: 50, message: '服务地址长度不能超过50个字符', trigger: 'blur' }
  ],
  contact_phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { max: 20, message: '联系电话长度不能超过20个字符', trigger: 'blur' }
  ],
  contact_name: [
    { required: true, message: '请输入联系人', trigger: 'blur' },
    { max: 10, message: '联系人长度不能超过10个字符', trigger: 'blur' }
  ]
}

// 初始加载数据
onMounted(() => {
  loadServiceList()
})

// 加载服务列表
const loadServiceList = async () => {
  try {
    loading.value = true
    // 这里应该调用API接口，暂时使用模拟数据
    // const res = await getServicePage({
    //   page: pagination.currentPage,
    //   pageSize: pagination.pageSize,
    //   ...searchForm
    // })
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    serviceList.value = [
      {
        service_id: 1,
        service_name: '摄影器材租赁',
        service_description: '提供各种专业摄影器材的租赁服务，包括相机、镜头、灯光等',
        price: '3000/天',
        status: 1,
        service_address: '雅安市雨城区和平路123号',
        contact_phone: '13800138000',
        contact_name: '张经理'
      },
      {
        service_id: 2,
        service_name: '场务支持',
        service_description: '提供专业的电影拍摄场务支持服务，包括场地布置、道具准备等',
        price: '5000/天',
        status: 1,
        service_address: '雅安市雨城区创业路456号',
        contact_phone: '13900139000',
        contact_name: '李主任'
      },
      {
        service_id: 3,
        service_name: '群众演员',
        service_description: '提供各类影视剧拍摄所需的群众演员服务',
        price: '200/人/天',
        status: 0,
        service_address: '雅安市雨城区文化街789号',
        contact_phone: '13700137000',
        contact_name: '王总'
      }
    ]
    pagination.total = serviceList.value.length
  } catch (error) {
    ElMessage.error('加载服务列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadServiceList()
}

// 处理重置
const handleReset = () => {
  searchForm.serviceName = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadServiceList()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadServiceList()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  loadServiceList()
}

// 处理选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理新增
const handleAdd = () => {
  dialogTitle.value = '新增服务'
  currentAction.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑服务'
  currentAction.value = 'edit'
  Object.assign(serviceForm, row)
  dialogVisible.value = true
}

// 处理查看
const handleView = (row) => {
  dialogTitle.value = '查看服务'
  currentAction.value = 'view'
  Object.assign(serviceForm, row)
  dialogVisible.value = true
}

// 处理删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里应该调用API接口
    // const res = await deleteService(id)
    
    // 模拟删除
    await new Promise(resolve => setTimeout(resolve, 300))
    ElMessage.success('删除成功')
    loadServiceList()
  } catch (error) {
    // 用户取消删除时不显示错误信息
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  if (serviceFormRef.value) {
    serviceFormRef.value.resetFields()
  }
  Object.assign(serviceForm, {
    service_id: '',
    service_name: '',
    service_description: '',
    price: '',
    status: 1,
    service_address: '',
    contact_phone: '',
    contact_name: ''
  })
}

// 处理对话框保存
const handleDialogSave = async () => {
  if (!serviceFormRef.value) return
  
  await serviceFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        dialogLoading.value = true
        
        if (currentAction.value === 'add') {
          // 这里应该调用API接口
          // const res = await addService(serviceForm)
          
          // 模拟添加
          await new Promise(resolve => setTimeout(resolve, 500))
          ElMessage.success('新增成功')
        } else if (currentAction.value === 'edit') {
          // 这里应该调用API接口
          // const res = await updateService(serviceForm)
          
          // 模拟更新
          await new Promise(resolve => setTimeout(resolve, 500))
          ElMessage.success('更新成功')
        }
        
        dialogVisible.value = false
        loadServiceList()
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
.service-management {
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

.service-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

.mr-2 {
  margin-right: 8px;
}
</style>