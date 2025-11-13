<template>
  <div class="profile-page animate-fade-in">
    <div class="profile-container">
      <!-- 用户信息卡片 -->
      <el-card class="profile-card card animate-slide-in-up">
        <template #header>
          <div class="card-header">
            <span class="card-title">个人信息</span>
            <el-button 
              type="primary" 
              @click="handleEdit"
              class="btn-transition"
              v-if="!isEditing"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <div v-else>
              <el-button @click="handleCancel" class="mr-2">取消</el-button>
              <el-button type="primary" @click="handleSave">保存</el-button>
            </div>
          </div>
        </template>
        
        <div class="profile-content">
          <!-- 头像区域 -->
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="80" class="avatar" :src="formData.avatar">
                <el-icon :size="40"><User /></el-icon>
              </el-avatar>
              <div class="avatar-overlay" v-if="isEditing">
                <el-upload
                  :action="uploadAction"
                  :on-success="handleAvatarSuccess"
                  :show-file-list="false"
                >
                  <el-button type="primary" circle>
                    <el-icon><Camera /></el-icon>
                  </el-button>
                </el-upload>
              </div>
            </div>
            <div class="user-info">
              <h3 class="username">{{ formData.username || '管理员' }}</h3>
              <p class="user-role">{{ formData.role === 'ADMIN' ? '管理员' : '普通用户' }}</p>
            </div>
          </div>
          
          <!-- 信息表单 -->
          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="100px"
            class="info-form"
            v-loading="loading"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户名" prop="username">
                  <el-input 
                    v-model="formData.username" 
                    :disabled="!isEditing"
                    class="animate-slide-in-up"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input 
                    v-model="formData.email" 
                    :disabled="!isEditing"
                    placeholder="请输入邮箱"
                    class="animate-slide-in-up"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="角色">
                  <el-input 
                    v-model="formData.identity" 
                    disabled
                    class="animate-slide-in-up"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </el-card>
      
      <!-- 修改密码卡片 -->
      <el-card class="password-card card animate-slide-in-up">
        <template #header>
          <div class="card-header">
            <span class="card-title">修改密码</span>
          </div>
        </template>
        
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
          class="password-form"
        >
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入当前密码"
              show-password
              class="animate-slide-in-up"
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
              class="animate-slide-in-up"
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
              class="animate-slide-in-up"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleChangePassword" class="btn-transition">
              修改密码
            </el-button>
            <el-button @click="handleResetPassword">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 操作日志卡片 -->
      <el-card class="logs-card card animate-slide-in-up">
        <template #header>
          <div class="card-header">
            <span class="card-title">最近操作</span>
          </div>
        </template>
        
        <el-timeline class="timeline-compact">
          <el-timeline-item
            v-for="(log, index) in operationLogs"
            :key="index"
            :timestamp="log.time"
            placement="top"
            class="animate-slide-in-left"
          >
            <div class="log-content">
              <h4>
                {{ log.action }}
                <el-tag v-if="log.success === false" type="danger" size="small" style="margin-left: 8px">失败</el-tag>
                <el-tag v-else type="success" size="small" style="margin-left: 8px">成功</el-tag>
              </h4>
              <p v-if="log.detail">{{ log.detail }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
        <div v-if="operationLogs.length === 0" class="empty-logs">
          <el-empty description="暂无操作记录" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Edit, Camera } from '@element-plus/icons-vue'
import EnhancedTextarea from '@/components/EnhancedTextarea.vue'
// import { getAdminInfo, updateAdminInfo, changePassword, uploadAvatar, getRecentOperationLogs } from '@/api'

const userStore = useUserStore()
const formRef = ref(null)
const passwordFormRef = ref(null)
const isEditing = ref(false)
const loading = ref(false)

const formData = reactive({
  id: null,
  username: '',
  email: '',
  avatar: '',
  role: '',
  identity: '管理员'
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const operationLogs = ref([])

const uploadAction = '/api/admin/upload-avatar'

onMounted(async () => {
  await loadAdminInfo()
  await loadOperationLogs()
})

const loadOperationLogs = async () => {
  try {
    // const res = await getRecentOperationLogs(10)
    // if (res.code === 200 && res.data) {
    //   operationLogs.value = res.data.map(log => ({
    //     action: log.description || '未知操作',
    //     detail: log.type ? `操作类型: ${typeof log.type === 'string' ? log.type : log.type.description || log.type}` : '',
    //     time: log.timestamp ? formatDateTime(log.timestamp) : '',
    //     success: log.success !== undefined ? log.success : true
    //   }))
    // }
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    operationLogs.value = [
      {
        action: '登录系统',
        detail: '操作类型: 登录',
        time: formatDateTime(new Date().getTime() - 3600000),
        success: true
      },
      {
        action: '修改个人信息',
        detail: '操作类型: 更新',
        time: formatDateTime(new Date().getTime() - 7200000),
        success: true
      },
      {
        action: '查看电视剧列表',
        detail: '操作类型: 查询',
        time: formatDateTime(new Date().getTime() - 10800000),
        success: true
      }
    ]
  } catch (error) {
    console.error('加载操作日志失败:', error)
  }
}

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

const loadAdminInfo = async () => {
  try {
    loading.value = true
    // const res = await getAdminInfo()
    // if (res.code === 200 && res.data) {
    //   Object.assign(formData, {
    //     id: res.data.id,
    //     username: res.data.username || '',
    //     email: res.data.email || '',
    //     avatar: res.data.avatar || '',
    //     role: res.data.role || '',
    //     identity: res.data.role === 'ADMIN' ? '管理员' : '普通用户'
    //   })
    //   // 更新store
    //   userStore.setUserInfo(res.data)
    // }
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    const mockUserInfo = {
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      avatar: '',
      role: 'ADMIN',
      identity: '管理员'
    }
    Object.assign(formData, mockUserInfo)
    // 更新store
    userStore.setUserInfo(mockUserInfo)
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  isEditing.value = true
}

const handleCancel = () => {
  isEditing.value = false
  // 恢复原始数据
  if (userStore.userInfo) {
    Object.assign(formData, {
      id: userStore.userInfo.id,
      username: userStore.userInfo.username || '',
      email: userStore.userInfo.email || '',
      avatar: userStore.userInfo.avatar || '',
      role: userStore.userInfo.role || '',
      identity: userStore.userInfo.role === 'ADMIN' ? '管理员' : '普通用户'
    })
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        const updateData = {
          id: formData.id,
          username: formData.username,
          email: formData.email,
          avatar: formData.avatar
        }
        // const res = await updateAdminInfo(updateData)
        // if (res.code === 200) {
        //   ElMessage.success('保存成功')
        //   await loadAdminInfo()
        //   isEditing.value = false
        // } else {
        //   ElMessage.error(res.message || '保存失败')
        // }
        
        // 模拟保存
        await new Promise(resolve => setTimeout(resolve, 500))
        ElMessage.success('保存成功')
        // 更新store中的数据
        const updatedUserInfo = {
          ...userStore.userInfo,
          ...updateData
        }
        userStore.setUserInfo(updatedUserInfo)
        isEditing.value = false
      } catch (error) {
        ElMessage.error(error.message || '保存失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleAvatarSuccess = async (response) => {
  // 模拟头像上传成功
  formData.avatar = 'https://randomuser.me/api/portraits/men/32.jpg'
  ElMessage.success('头像上传成功')
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        // const res = await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
        // if (res.code === 200) {
        //   ElMessage.success('密码修改成功')
        //   handleResetPassword()
        // } else {
        //   ElMessage.error(res.message || '密码修改失败')
        // }
        
        // 模拟密码修改
        await new Promise(resolve => setTimeout(resolve, 500))
        ElMessage.success('密码修改成功')
        handleResetPassword()
      } catch (error) {
        ElMessage.error(error.message || '密码修改失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleResetPassword = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}
</script>

<style scoped lang="scss">
.profile-page {
  padding: 0;
}

.profile-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  
  .profile-card {
    grid-column: 1 / -1;
  }
  
  .password-card,
  .logs-card {
    grid-column: span 1;
  }
}

@media (max-width: 1200px) {
  .profile-container {
    grid-template-columns: 1fr;
  }
}

.profile-card,
.password-card,
.logs-card {
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
}

.profile-content {
  padding: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebe6ff;
}

.avatar-wrapper {
  position: relative;
  
  .avatar {
    width: 80px !important;
    height: 80px !important;
    border: 3px solid #ebe6ff;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #9c88ff;
      transform: scale(1.05);
    }
  }
  
  .avatar-overlay {
    position: absolute;
    bottom: 0;
    right: 0;
    background: white;
    border-radius: 50%;
    padding: 2px;
    box-shadow: 0 2px 6px rgba(156, 136, 255, 0.3);
    transition: all 0.3s ease;
    
    &:hover {
      transform: scale(1.1);
    }
  }
}

.user-info {
  flex: 1;
  
  .username {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 6px;
    background: linear-gradient(135deg, #9c88ff 0%, #b8abff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .user-role {
    font-size: 13px;
    color: #909399;
    margin: 0;
  }
}

.info-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
  
  :deep(.el-input) {
    transition: all 0.3s ease;
  }
}

.password-form {
  padding: 20px;
  
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

.logs-card {
  :deep(.el-timeline) {
    padding-left: 0;
  }
  
  :deep(.el-timeline-item__timestamp) {
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
  }
}

.timeline-compact {
  padding: 0;
  
  :deep(.el-timeline-item) {
    padding-bottom: 12px;
  }
  
  :deep(.el-timeline-item__tail) {
    left: 6px;
  }
  
  :deep(.el-timeline-item__node) {
    width: 12px;
    height: 12px;
    left: 0;
  }
}

.log-content {
  padding: 8px 12px;
  background: #f9f7ff;
  border-radius: 6px;
  margin-bottom: 4px;
  transition: all 0.3s ease;
  
  &:hover {
    background: #f3f0ff;
    transform: translateX(5px);
  }
  
  h4 {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 2px;
  }
  
  p {
    font-size: 12px;
    color: #909399;
    margin: 0;
    line-height: 1.4;
  }
}

.empty-logs {
  padding: 20px 0;
}

.mr-2 {
  margin-right: 8px;
}
</style>

