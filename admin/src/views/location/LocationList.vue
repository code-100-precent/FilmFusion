<template>
  <div class="location-management animate-fade-in">
    <el-card class="card animate-slide-in-up">
      <template #header>
        <div class="card-header">
          <span class="card-title">场地管理</span>
          <el-button type="primary" @click="handleAdd" class="btn-transition">
            <el-icon><Plus /></el-icon>
            新增场地
          </el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="场地名称">
            <el-input v-model="searchForm.locationName" placeholder="请输入场地名称" />
          </el-form-item>
          <el-form-item label="场地类型">
            <el-input v-model="searchForm.locationType" placeholder="请输入场地类型" />
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
        :data="locationList" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="location_id" label="场地ID" width="80" />
        <el-table-column prop="location_name" label="场地名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="location_type" label="场地类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '可用' : '不可用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
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
                  <el-dropdown-item @click="handleDelete(scope.row.location_id)" danger>删除</el-dropdown-item>
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
    
    <!-- 场地详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      destroy-on-close
    >
      <el-form
        ref="locationFormRef"
        :model="locationForm"
        :rules="formRules"
        label-width="100px"
        class="location-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="场地名称" prop="location_name">
              <el-input v-model="locationForm.location_name" placeholder="请输入场地名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="场地类型" prop="location_type">
              <el-input v-model="locationForm.location_type" placeholder="请输入场地类型" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="locationForm.status">
                <el-radio :label="1">可用</el-radio>
                <el-radio :label="0">不可用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input v-model="locationForm.price" placeholder="请输入价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contact_name">
              <el-input v-model="locationForm.contact_name" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contact_phone">
              <el-input v-model="locationForm.contact_phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="地址" prop="address">
              <el-input v-model="locationForm.address" placeholder="请输入地址" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="场地介绍" prop="location_description">
              <el-input
                v-model="locationForm.location_description"
                type="textarea"
                :rows="4"
                placeholder="请输入场地介绍"
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
const locationList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('')
const currentAction = ref('')
const locationFormRef = ref(null)
const selectedRows = ref([])

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  locationName: '',
  locationType: '',
  status: ''
})

// 场地表单
const locationForm = reactive({
  location_id: '',
  location_name: '',
  location_type: '',
  status: 1,
  location_description: '',
  contact_phone: '',
  contact_name: '',
  address: '',
  price: ''
})

// 表单验证规则
const formRules = {
  location_name: [
    { required: true, message: '请输入场地名称', trigger: 'blur' },
    { max: 20, message: '场地名称长度不能超过20个字符', trigger: 'blur' }
  ],
  location_type: [
    { required: true, message: '请输入场地类型', trigger: 'blur' },
    { max: 10, message: '场地类型长度不能超过10个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  location_description: [
    { required: true, message: '请输入场地介绍', trigger: 'blur' },
    { max: 200, message: '场地介绍长度不能超过200个字符', trigger: 'blur' }
  ],
  contact_phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { max: 20, message: '联系电话长度不能超过20个字符', trigger: 'blur' }
  ],
  contact_name: [
    { required: true, message: '请输入联系人', trigger: 'blur' },
    { max: 10, message: '联系人长度不能超过10个字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入地址', trigger: 'blur' },
    { max: 50, message: '地址长度不能超过50个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { max: 20, message: '价格长度不能超过20个字符', trigger: 'blur' }
  ]
}

// 初始加载数据
onMounted(() => {
  loadLocationList()
})

// 加载场地列表
const loadLocationList = async () => {
  try {
    loading.value = true
    // 这里应该调用API接口，暂时使用模拟数据
    // const res = await getLocationPage({
    //   page: pagination.currentPage,
    //   pageSize: pagination.pageSize,
    //   ...searchForm
    // })
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    locationList.value = [
      {
        location_id: 1,
        location_name: '碧峰峡景区',
        location_type: '自然风景',
        status: 1,
        location_description: '著名的自然风景旅游区，适合拍摄自然风光场景',
        contact_phone: '13800138000',
        contact_name: '张经理',
        address: '雅安市雨城区碧峰峡镇',
        price: '10000/天'
      },
      {
        location_id: 2,
        location_name: '上里古镇',
        location_type: '古镇',
        status: 1,
        location_description: '保存完好的明清古镇，适合拍摄历史题材',
        contact_phone: '13900139000',
        contact_name: '李主任',
        address: '雅安市雨城区上里镇',
        price: '8000/天'
      },
      {
        location_id: 3,
        location_name: '蒙顶山',
        location_type: '山地',
        status: 0,
        location_description: '茶文化发源地，风景秀丽',
        contact_phone: '13700137000',
        contact_name: '王总',
        address: '雅安市名山区蒙顶山镇',
        price: '12000/天'
      }
    ]
    pagination.total = locationList.value.length
  } catch (error) {
    ElMessage.error('加载场地列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadLocationList()
}

// 处理重置
const handleReset = () => {
  searchForm.locationName = ''
  searchForm.locationType = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadLocationList()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadLocationList()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  loadLocationList()
}

// 处理选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理新增
const handleAdd = () => {
  dialogTitle.value = '新增场地'
  currentAction.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑场地'
  currentAction.value = 'edit'
  Object.assign(locationForm, row)
  dialogVisible.value = true
}

// 处理查看
const handleView = (row) => {
  dialogTitle.value = '查看场地'
  currentAction.value = 'view'
  Object.assign(locationForm, row)
  dialogVisible.value = true
}

// 处理删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个场地吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里应该调用API接口
    // const res = await deleteLocation(id)
    
    // 模拟删除
    await new Promise(resolve => setTimeout(resolve, 300))
    ElMessage.success('删除成功')
    loadLocationList()
  } catch (error) {
    // 用户取消删除时不显示错误信息
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  if (locationFormRef.value) {
    locationFormRef.value.resetFields()
  }
  Object.assign(locationForm, {
    location_id: '',
    location_name: '',
    location_type: '',
    status: 1,
    location_description: '',
    contact_phone: '',
    contact_name: '',
    address: '',
    price: ''
  })
}

// 处理对话框保存
const handleDialogSave = async () => {
  if (!locationFormRef.value) return
  
  await locationFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        dialogLoading.value = true
        
        if (currentAction.value === 'add') {
          // 这里应该调用API接口
          // const res = await addLocation(locationForm)
          
          // 模拟添加
          await new Promise(resolve => setTimeout(resolve, 500))
          ElMessage.success('新增成功')
        } else if (currentAction.value === 'edit') {
          // 这里应该调用API接口
          // const res = await updateLocation(locationForm)
          
          // 模拟更新
          await new Promise(resolve => setTimeout(resolve, 500))
          ElMessage.success('更新成功')
        }
        
        dialogVisible.value = false
        loadLocationList()
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
.location-management {
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

.location-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

.mr-2 {
  margin-right: 8px;
}
</style>