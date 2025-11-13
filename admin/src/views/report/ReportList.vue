<template>
  <div class="report-management animate-fade-in">
    <el-card class="card animate-slide-in-up">
      <template #header>
        <div class="card-header">
          <span class="card-title">电视剧拍摄报告管理</span>
          <el-button type="primary" @click="handleAdd" class="btn-transition">
            <el-icon><Plus /></el-icon>
            新增报告
          </el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="剧名">
            <el-input v-model="searchForm.tvName" placeholder="请输入剧名" />
          </el-form-item>
          <el-form-item label="剧种">
            <el-input v-model="searchForm.tvType" placeholder="请输入剧种" />
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
        :data="reportList" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="tv_id" label="报告ID" width="80" />
        <el-table-column prop="tv_name" label="剧名" min-width="150" show-overflow-tooltip />
        <el-table-column prop="tv_type" label="剧种" width="120" />
        <el-table-column prop="tv_genre" label="题材" width="120" />
        <el-table-column prop="tv_episodes" label="集数" width="80" />
        <el-table-column prop="invest_amount" label="投资金额" width="120" />
        <el-table-column prop="start_date" label="开机日期" width="120">
          <template #default="scope">
            {{ formatDate(scope.row.start_date) }}
          </template>
        </el-table-column>
        <el-table-column prop="end_date" label="关机日期" width="120">
          <template #default="scope">
            {{ formatDate(scope.row.end_date) }}
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
                  <el-dropdown-item @click="handleDelete(scope.row.tv_id)" danger>删除</el-dropdown-item>
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
    
    <!-- 报告详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      destroy-on-close
      fullscreen
    >
      <el-form
        ref="reportFormRef"
        :model="reportForm"
        :rules="formRules"
        label-width="120px"
        class="report-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="剧名" prop="tv_name">
              <el-input v-model="reportForm.tv_name" placeholder="请输入剧名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="剧种" prop="tv_type">
              <el-input v-model="reportForm.tv_type" placeholder="请输入剧种" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="题材" prop="tv_genre">
              <el-input v-model="reportForm.tv_genre" placeholder="请输入题材" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="集数" prop="tv_episodes">
              <el-input v-model="reportForm.tv_episodes" type="number" placeholder="请输入集数" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="投资金额" prop="invest_amount">
              <el-input v-model="reportForm.invest_amount" placeholder="请输入投资金额" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开机日期" prop="start_date">
              <el-date-picker
                v-model="reportForm.start_date"
                type="date"
                placeholder="请选择开机日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关机日期" prop="end_date">
              <el-date-picker
                v-model="reportForm.end_date"
                type="date"
                placeholder="请选择关机日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="剧组规模" prop="crew_scale">
              <el-input v-model="reportForm.crew_scale" placeholder="请输入剧组规模" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主要创作人员" prop="main_creators">
              <el-input v-model="reportForm.main_creators" placeholder="请输入主要创作人员" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主要制作人" prop="lead_producer">
              <el-input v-model="reportForm.lead_producer" placeholder="请输入主要制作人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="制作单位" prop="producer_unit">
              <el-input v-model="reportForm.producer_unit" placeholder="请输入制作单位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contact">
              <el-input v-model="reportForm.contact" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone_number">
              <el-input v-model="reportForm.phone_number" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="剧组职务" prop="crew_position">
              <el-input v-model="reportForm.crew_position" placeholder="请输入剧组职务" />
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
const reportList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('')
const currentAction = ref('')
const reportFormRef = ref(null)
const selectedRows = ref([])

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  tvName: '',
  tvType: ''
})

// 报告表单
const reportForm = reactive({
  tv_id: '',
  tv_name: '',
  tv_type: '',
  tv_genre: '',
  tv_episodes: '',
  invest_amount: '',
  main_creators: '',
  lead_producer: '',
  producer_unit: '',
  start_date: '',
  end_date: '',
  crew_scale: '',
  contact: '',
  phone_number: '',
  crew_position: ''
})

// 表单验证规则
const formRules = {
  tv_name: [
    { required: true, message: '请输入剧名', trigger: 'blur' },
    { max: 50, message: '剧名长度不能超过50个字符', trigger: 'blur' }
  ],
  tv_type: [
    { required: true, message: '请输入剧种', trigger: 'blur' },
    { max: 20, message: '剧种长度不能超过20个字符', trigger: 'blur' }
  ],
  tv_genre: [
    { required: true, message: '请输入题材', trigger: 'blur' },
    { max: 20, message: '题材长度不能超过20个字符', trigger: 'blur' }
  ],
  tv_episodes: [
    { required: true, message: '请输入集数', trigger: 'blur' }
  ],
  invest_amount: [
    { required: true, message: '请输入投资金额', trigger: 'blur' },
    { max: 20, message: '投资金额长度不能超过20个字符', trigger: 'blur' }
  ],
  main_creators: [
    { required: true, message: '请输入主要创作人员', trigger: 'blur' },
    { max: 100, message: '主要创作人员长度不能超过100个字符', trigger: 'blur' }
  ],
  lead_producer: [
    { required: true, message: '请输入主要制作人', trigger: 'blur' },
    { max: 50, message: '主要制作人长度不能超过50个字符', trigger: 'blur' }
  ],
  producer_unit: [
    { required: true, message: '请输入制作单位', trigger: 'blur' },
    { max: 100, message: '制作单位长度不能超过100个字符', trigger: 'blur' }
  ],
  start_date: [
    { required: true, message: '请选择开机日期', trigger: 'change' }
  ],
  end_date: [
    { required: true, message: '请选择关机日期', trigger: 'change' }
  ],
  crew_scale: [
    { required: true, message: '请输入剧组规模', trigger: 'blur' },
    { max: 50, message: '剧组规模长度不能超过50个字符', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系人', trigger: 'blur' },
    { max: 20, message: '联系人长度不能超过20个字符', trigger: 'blur' }
  ],
  phone_number: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { max: 20, message: '联系电话长度不能超过20个字符', trigger: 'blur' }
  ],
  crew_position: [
    { required: true, message: '请输入剧组职务', trigger: 'blur' },
    { max: 20, message: '剧组职务长度不能超过20个字符', trigger: 'blur' }
  ]
}

// 初始加载数据
onMounted(() => {
  loadReportList()
})

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 加载报告列表
const loadReportList = async () => {
  try {
    loading.value = true
    // 这里应该调用API接口，暂时使用模拟数据
    // const res = await getReportPage({
    //   page: pagination.currentPage,
    //   pageSize: pagination.pageSize,
    //   tvName: searchForm.tvName,
    //   tvType: searchForm.tvType
    // })
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    reportList.value = [
      {
        tv_id: 1,
        tv_name: '川西故事',
        tv_type: '电视剧',
        tv_genre: '都市情感',
        tv_episodes: 30,
        invest_amount: '5000万',
        main_creators: '张艺谋、李安',
        lead_producer: '张伟',
        producer_unit: '北京电影制片厂',
        start_date: '2023-01-15',
        end_date: '2023-06-30',
        crew_scale: '100人',
        contact: '李明',
        phone_number: '13800138000',
        crew_position: '副导演'
      },
      {
        tv_id: 2,
        tv_name: '雅安往事',
        tv_type: '纪录片',
        tv_genre: '历史人文',
        tv_episodes: 8,
        invest_amount: '800万',
        main_creators: '陈凯歌',
        lead_producer: '王丽',
        producer_unit: '中央电视台',
        start_date: '2023-03-10',
        end_date: '2023-05-20',
        crew_scale: '50人',
        contact: '赵刚',
        phone_number: '13900139000',
        crew_position: '制片主任'
      },
      {
        tv_id: 3,
        tv_name: '熊猫传奇',
        tv_type: '动画片',
        tv_genre: '科幻冒险',
        tv_episodes: 26,
        invest_amount: '2000万',
        main_creators: '宫崎骏、久石让',
        lead_producer: '刘伟',
        producer_unit: '上海动画制片厂',
        start_date: '2023-02-01',
        end_date: '2023-10-31',
        crew_scale: '80人',
        contact: '钱小红',
        phone_number: '13700137000',
        crew_position: '执行导演'
      }
    ]
    pagination.total = reportList.value.length
  } catch (error) {
    ElMessage.error('加载报告列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadReportList()
}

// 处理重置
const handleReset = () => {
  searchForm.tvName = ''
  searchForm.tvType = ''
  pagination.currentPage = 1
  loadReportList()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadReportList()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  loadReportList()
}

// 处理选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理新增
const handleAdd = () => {
  dialogTitle.value = '新增报告'
  currentAction.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑报告'
  currentAction.value = 'edit'
  Object.assign(reportForm, row)
  dialogVisible.value = true
}

// 处理查看
const handleView = (row) => {
  dialogTitle.value = '查看报告'
  currentAction.value = 'view'
  Object.assign(reportForm, row)
  dialogVisible.value = true
}

// 处理删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个报告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里应该调用API接口
    // const res = await deleteReport(id)
    
    // 模拟删除
    await new Promise(resolve => setTimeout(resolve, 300))
    ElMessage.success('删除成功')
    loadReportList()
  } catch (error) {
    // 用户取消删除时不显示错误信息
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  if (reportFormRef.value) {
    reportFormRef.value.resetFields()
  }
  Object.assign(reportForm, {
    tv_id: '',
    tv_name: '',
    tv_type: '',
    tv_genre: '',
    tv_episodes: '',
    invest_amount: '',
    main_creators: '',
    lead_producer: '',
    producer_unit: '',
    start_date: '',
    end_date: '',
    crew_scale: '',
    contact: '',
    phone_number: '',
    crew_position: ''
  })
}

// 处理对话框保存
const handleDialogSave = async () => {
  if (!reportFormRef.value) return
  
  await reportFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        dialogLoading.value = true
        
        if (currentAction.value === 'add') {
          // 这里应该调用API接口
          // const res = await addReport(reportForm)
          
          // 模拟添加
          await new Promise(resolve => setTimeout(resolve, 500))
          ElMessage.success('新增成功')
        } else if (currentAction.value === 'edit') {
          // 这里应该调用API接口
          // const res = await updateReport(reportForm)
          
          // 模拟更新
          await new Promise(resolve => setTimeout(resolve, 500))
          ElMessage.success('更新成功')
        }
        
        dialogVisible.value = false
        loadReportList()
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
.report-management {
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

.report-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

.mr-2 {
  margin-right: 8px;
}
</style>