<template>
  <div class="image-management animate-fade-in">
    <el-card class="card animate-slide-in-up">
      <template #header>
        <div class="card-header">
          <span class="card-title">图片管理</span>
          <el-button type="primary" @click="handleAdd" class="btn-transition">
            <el-icon><Plus /></el-icon>
            新增图片
          </el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="所属模块">
            <el-select v-model="searchForm.module" placeholder="请选择模块">
              <el-option label="全部" value="" />
              <el-option label="文章" value="article" />
              <el-option label="电视剧" value="drama" />
              <el-option label="场地" value="location" />
              <el-option label="服务" value="service" />
            </el-select>
          </el-form-item>
          <el-form-item label="记录ID">
            <el-input v-model="searchForm.recordId" placeholder="请输入记录ID" />
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
        :data="imageList" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="image_id" label="图片ID" width="80" />
        <el-table-column prop="module" label="所属模块" width="100">
          <template #default="scope">
            {{ getModuleName(scope.row.module) }}
          </template>
        </el-table-column>
        <el-table-column prop="record_id" label="记录ID" width="100" />
        <el-table-column prop="url" label="图片URL" min-width="250" show-overflow-tooltip />
        <el-table-column label="图片预览" width="120">
          <template #default="scope">
            <el-image 
              :src="scope.row.url" 
              :preview-src-list="[scope.row.url]" 
              fit="cover" 
              width="60" 
              height="60"
              class="image-preview"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-dropdown trigger="hover" placement="bottom">
              <el-button size="small" type="text">
                ...
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleDelete(scope.row.image_id)">删除</el-dropdown-item>
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
    
    <!-- 图片添加对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="imageFormRef"
        :model="imageForm"
        :rules="formRules"
        label-width="100px"
        class="image-form"
      >
        <el-form-item label="所属模块" prop="module">
          <el-select v-model="imageForm.module" placeholder="请选择所属模块">
            <el-option label="文章" value="article" />
            <el-option label="电视剧" value="drama" />
            <el-option label="场地" value="location" />
            <el-option label="服务" value="service" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录ID" prop="record_id">
          <el-input v-model="imageForm.record_id" placeholder="请输入关联记录ID" />
        </el-form-item>
        <el-form-item label="图片上传" prop="file">
          <el-upload
            class="upload-demo"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            multiple
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖放文件到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                请上传PNG、JPG、JPEG格式的图片，单个文件大小不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="uploadedFiles.length > 0">
          <div class="uploaded-files">
            <el-tag 
              v-for="(file, index) in uploadedFiles" 
              :key="index"
              closable
              @close="removeFile(index)"
              class="file-tag"
            >
              {{ file.name }}
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDialogSave" :loading="dialogLoading">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'

// 模拟数据
const imageList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('')
const imageFormRef = ref(null)
const selectedRows = ref([])
const uploadedFiles = ref([])

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  module: '',
  recordId: ''
})

// 图片表单
const imageForm = reactive({
  image_id: '',
  module: '',
  record_id: '',
  url: '',
  file: null
})

// 表单验证规则
const formRules = {
  module: [
    { required: true, message: '请选择所属模块', trigger: 'change' }
  ],
  record_id: [
    { required: true, message: '请输入记录ID', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请上传图片', trigger: 'change' }
  ]
}

// 模块名称映射
const moduleMap = {
  'article': '文章',
  'drama': '电视剧',
  'location': '场地',
  'service': '服务'
}

// 初始加载数据
onMounted(() => {
  loadImageList()
})

// 获取模块名称
const getModuleName = (module) => {
  return moduleMap[module] || module
}

// 加载图片列表
const loadImageList = async () => {
  try {
    loading.value = true
    // 这里应该调用API接口，暂时使用模拟数据
    // const res = await getImagePage({
    //   page: pagination.currentPage,
    //   pageSize: pagination.pageSize,
    //   module: searchForm.module,
    //   recordId: searchForm.recordId
    // })
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    imageList.value = [
      {
        image_id: 1,
        module: 'article',
        record_id: 1,
        url: 'https://picsum.photos/200/300?random=1'
      },
      {
        image_id: 2,
        module: 'drama',
        record_id: 1,
        url: 'https://picsum.photos/200/300?random=2'
      },
      {
        image_id: 3,
        module: 'location',
        record_id: 1,
        url: 'https://picsum.photos/200/300?random=3'
      },
      {
        image_id: 4,
        module: 'service',
        record_id: 1,
        url: 'https://picsum.photos/200/300?random=4'
      },
      {
        image_id: 5,
        module: 'article',
        record_id: 2,
        url: 'https://picsum.photos/200/300?random=5'
      }
    ]
    pagination.total = imageList.value.length
  } catch (error) {
    ElMessage.error('加载图片列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadImageList()
}

// 处理重置
const handleReset = () => {
  searchForm.module = ''
  searchForm.recordId = ''
  pagination.currentPage = 1
  loadImageList()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadImageList()
}

// 处理页码变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  loadImageList()
}

// 处理选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理新增
const handleAdd = () => {
  dialogTitle.value = '新增图片'
  resetForm()
  dialogVisible.value = true
}

// 处理删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个图片吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里应该调用API接口
    // const res = await deleteImage(id)
    
    // 模拟删除
    await new Promise(resolve => setTimeout(resolve, 300))
    ElMessage.success('删除成功')
    loadImageList()
  } catch (error) {
    // 用户取消删除时不显示错误信息
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 处理文件变化
const handleFileChange = (file, fileList) => {
  uploadedFiles.value = fileList
  imageForm.file = file
}

// 移除文件
const removeFile = (index) => {
  uploadedFiles.value.splice(index, 1)
  if (uploadedFiles.value.length === 0) {
    imageForm.file = null
  }
}

// 重置表单
const resetForm = () => {
  if (imageFormRef.value) {
    imageFormRef.value.resetFields()
  }
  uploadedFiles.value = []
  Object.assign(imageForm, {
    image_id: '',
    module: '',
    record_id: '',
    url: '',
    file: null
  })
}

// 处理对话框保存
const handleDialogSave = async () => {
  if (!imageFormRef.value) return
  
  await imageFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        dialogLoading.value = true
        
        // 这里应该调用API接口上传文件
        // const formData = new FormData()
        // formData.append('file', imageForm.file.raw)
        // formData.append('module', imageForm.module)
        // formData.append('record_id', imageForm.record_id)
        // const res = await uploadImage(formData)
        
        // 模拟上传
        await new Promise(resolve => setTimeout(resolve, 1500))
        ElMessage.success('上传成功')
        
        dialogVisible.value = false
        loadImageList()
      } catch (error) {
        ElMessage.error('上传失败')
      } finally {
        dialogLoading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.image-management {
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

.image-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
  
  :deep(.el-upload-dragger) {
    width: 100%;
  }
}

.image-preview {
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
}

.uploaded-files {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.file-tag {
  margin-bottom: 8px;
}

.mr-2 {
  margin-right: 8px;
}
</style>