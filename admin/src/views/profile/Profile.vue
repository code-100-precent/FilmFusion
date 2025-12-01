<template>
  <div class="profile-page">
    <n-card class="profile-card">
      <div class="profile-header">
        <h2>个人中心</h2>
        <p>管理您的个人信息和账户设置</p>
      </div>
      
      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-container">
            <n-avatar 
              :size="100" 
              :src="userInfo.avatar" 
              round
              class="avatar"
            >
              <Icon icon="mdi:account" :width="40" />
            </n-avatar>
            <div class="avatar-actions">
              <n-button @click="handleUploadAvatar" type="primary" size="small">
                <template #icon>
                  <Icon icon="mdi:camera" />
                </template>
                更换头像
              </n-button>
            </div>
          </div>
          <input 
            ref="avatarInput" 
            type="file" 
            accept="image/*" 
            style="display: none" 
            @change="handleAvatarChange"
          />
        </div>
        
        <!-- 基本信息表单 -->
        <div class="form-section">
          <h3>基本信息</h3>
          <n-form 
            ref="formRef" 
            :model="userForm" 
            :rules="formRules" 
            label-placement="left" 
            label-width="100"
          >
            <n-form-item label="用户名" path="username">
              <n-input v-model:value="userForm.username" disabled />
            </n-form-item>
            <n-form-item label="手机号" path="phoneNumber">
              <n-input v-model:value="userForm.phoneNumber" disabled />
            </n-form-item>
            <n-form-item label="角色">
              <n-tag :type="userInfo.role === 'ADMIN' ? 'success' : 'default'">
                {{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </n-tag>
            </n-form-item>
          </n-form>
        </div>
        
        <!-- 修改密码 -->
        <div class="form-section">
          <h3>修改密码</h3>
          <n-form 
            ref="passwordFormRef" 
            :model="passwordForm" 
            :rules="passwordRules" 
            label-placement="left" 
            label-width="100"
          >
            <n-form-item label="当前密码" path="oldPassword">
              <n-input 
                v-model:value="passwordForm.oldPassword" 
                type="password" 
                placeholder="请输入当前密码"
                show-password-on="click"
              />
            </n-form-item>
            <n-form-item label="新密码" path="newPassword">
              <n-input 
                v-model:value="passwordForm.newPassword" 
                type="password" 
                placeholder="请输入新密码"
                show-password-on="click"
              />
            </n-form-item>
            <n-form-item label="确认密码" path="confirmPassword">
              <n-input 
                v-model:value="passwordForm.confirmPassword" 
                type="password" 
                placeholder="请再次输入新密码"
                show-password-on="click"
              />
            </n-form-item>
            <n-form-item>
              <n-button 
                type="primary" 
                @click="handleChangePassword" 
                :loading="passwordLoading"
                block
              >
                修改密码
              </n-button>
            </n-form-item>
          </n-form>
        </div>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NForm,
  NFormItem,
  NInput,
  NButton,
  NAvatar,
  NTag,
  useMessage
} from 'naive-ui'
import { useUserStore } from '@/store/user'
import { getAdminInfo, updateAdminInfo, changePassword, uploadAvatar } from '@/api'

const userStore = useUserStore()
const message = useMessage()

const userInfo = ref({})
const loading = ref(false)
const passwordLoading = ref(false)
const formRef = ref(null)
const passwordFormRef = ref(null)
const avatarInput = ref(null)

const userForm = reactive({
  username: '',
  phoneNumber: '',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const formRules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' }
  ],
  phoneNumber: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
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
      validator: (rule, value) => {
        return value === passwordForm.newPassword
      },
      message: '两次输入的密码不一致',
      trigger: ['blur', 'input']
    }
  ]
}

onMounted(() => {
  loadUserInfo()
})

const loadUserInfo = async () => {
  try {
    loading.value = true
    const res = await getAdminInfo()
    if (res.code === 200 && res.data) {
      userInfo.value = res.data
      Object.assign(userForm, {
        username: res.data.username || '',
        phoneNumber: res.data.phoneNumber || '',
        avatar: res.data.avatar || ''
      })
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    message.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const handleUploadAvatar = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    message.error('请选择图片文件')
    return
  }
  
  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    message.error('图片大小不能超过5MB')
    return
  }
  
  try {
    const res = await uploadAvatar(file)
    if (res.code === 200 && res.data) {
      userInfo.value.avatar = res.data.url || res.data.path
      userForm.avatar = res.data.url || res.data.path
      userStore.setUserInfo(userInfo.value)
      message.success('头像更新成功')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    message.error('上传头像失败')
  }
  
  // 清空文件选择
  event.target.value = ''
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
  } catch (error) {
    return
  }
  
  try {
    passwordLoading.value = true
    const res = await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    
    if (res.code === 200) {
      message.success('密码修改成功')
      // 重置密码表单
      Object.assign(passwordForm, {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      })
      passwordFormRef.value?.restoreValidation()
    }
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.profile-page {
  max-width: 800px;
  margin: 0 auto;
  animation: fadeIn 0.3s ease;
}

.profile-card {
  :deep(.n-card__content) {
    padding: 24px;
  }
}

.profile-header {
  text-align: center;
  margin-bottom: 32px;
  
  h2 {
    margin: 0 0 8px 0;
    color: #1f2937;
    font-size: 24px;
    font-weight: 600;
  }
  
  p {
    margin: 0;
    color: #6b7280;
    font-size: 14px;
  }
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  
  .avatar-container {
    position: relative;
    
    .avatar {
      border: 3px solid #f3f4f6;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .avatar-actions {
      position: absolute;
      bottom: -8px;
      right: -8px;
    }
  }
}

.form-section {
  h3 {
    margin: 0 0 20px 0;
    color: #1f2937;
    font-size: 18px;
    font-weight: 600;
    border-bottom: 2px solid #e5e7eb;
    padding-bottom: 8px;
  }
}

// 移动端适配
@media (max-width: 768px) {
  .profile-page {
    margin: 0 16px;
  }
  
  .profile-card {
    :deep(.n-card__content) {
      padding: 16px;
    }
  }
  
  .profile-header {
    margin-bottom: 24px;
    
    h2 {
      font-size: 20px;
    }
  }
  
  .profile-content {
    gap: 24px;
  }
  
  .avatar-section {
    gap: 12px;
    
    .avatar-container {
      .avatar {
        width: 80px !important;
        height: 80px !important;
      }
    }
  }
  
  .form-section {
    h3 {
      font-size: 16px;
      margin-bottom: 16px;
    }
    
    :deep(.n-form) {
      .n-form-item {
        margin-bottom: 16px;
        
        .n-form-item-label {
          width: 80px !important;
        }
      }
    }
  }
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