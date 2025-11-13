<template>
  <div class="profile-page">
    <n-grid :cols="2" :x-gap="24" :y-gap="24" responsive="screen">
      <n-gi :span="2">
        <n-card title="个人信息" class="profile-card">
          <template #header-extra>
            <n-button
              v-if="!isEditing"
              type="primary"
              @click="handleEdit"
            >
              <template #icon>
                <Icon icon="mdi:pencil-outline" />
              </template>
              编辑
            </n-button>
            <div v-else class="edit-actions">
              <n-button @click="handleCancel">取消</n-button>
              <n-button type="primary" @click="handleSave" :loading="loading">保存</n-button>
            </div>
          </template>
          
          <div class="profile-content">
            <div class="avatar-section">
              <n-upload
                :file-list="avatarFileList"
                :max="1"
                :show-file-list="false"
                :custom-request="handleAvatarUpload"
                accept="image/*"
                :disabled="!isEditing"
              >
                <n-avatar
                  :size="80"
                  :src="formData.avatar"
                  class="avatar"
                >
                  <Icon icon="mdi:account" :width="40" />
                </n-avatar>
              </n-upload>
              <div class="user-info">
                <h3 class="username">{{ formData.username || '管理员' }}</h3>
                <n-tag :type="formData.role === 'ADMIN' ? 'success' : 'default'" size="small">
                  {{ formData.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </n-tag>
              </div>
            </div>
            
            <n-form
              ref="formRef"
              :model="formData"
              :rules="formRules"
              label-placement="left"
              label-width="100"
              :disabled="!isEditing"
            >
              <n-grid :cols="2" :x-gap="24">
                <n-gi>
                  <n-form-item label="用户名" path="username">
                    <n-input v-model:value="formData.username" placeholder="请输入用户名" />
                  </n-form-item>
                </n-gi>
                <n-gi>
                  <n-form-item label="手机号">
                    <n-input v-model:value="formData.phoneNumber" disabled placeholder="手机号不可修改" />
                  </n-form-item>
                </n-gi>
              </n-grid>
            </n-form>
          </div>
        </n-card>
      </n-gi>
      
      <n-gi>
        <n-card title="修改密码" class="password-card">
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
              <n-button type="primary" @click="handleChangePassword" :loading="loading">
                修改密码
              </n-button>
              <n-button @click="handleResetPassword" style="margin-left: 12px">重置</n-button>
            </n-form-item>
          </n-form>
        </n-card>
      </n-gi>
      
      <n-gi>
        <n-card title="账户信息" class="info-card">
          <n-descriptions :column="1" bordered>
            <n-descriptions-item label="用户ID">
              {{ formData.id || '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="角色">
              <n-tag :type="formData.role === 'ADMIN' ? 'success' : 'default'" size="small">
                {{ formData.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="状态">
              <n-tag :type="formData.enabled ? 'success' : 'error'" size="small">
                {{ formData.enabled ? '启用' : '禁用' }}
              </n-tag>
            </n-descriptions-item>
          </n-descriptions>
        </n-card>
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NGrid,
  NGi,
  NButton,
  NUpload,
  NAvatar,
  NTag,
  NForm,
  NFormItem,
  NInput,
  NDescriptions,
  NDescriptionsItem,
  useMessage
} from 'naive-ui'
import { getAdminInfo, updateAdminInfo, changePassword, uploadAvatar } from '@/api'

const userStore = useUserStore()
const message = useMessage()
const formRef = ref(null)
const passwordFormRef = ref(null)
const isEditing = ref(false)
const loading = ref(false)
const avatarFileList = ref([])

const formData = reactive({
  id: null,
  username: '',
  phoneNumber: '',
  avatar: '',
  role: '',
  enabled: true
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 32, message: '用户名长度在 2 到 32 个字符', trigger: 'blur' }
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
        if (value !== passwordForm.newPassword) {
          return new Error('两次输入密码不一致')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

onMounted(async () => {
  await loadAdminInfo()
})

const loadAdminInfo = async () => {
  try {
    loading.value = true
    const res = await getAdminInfo()
    
    if (res.code === 200 && res.data) {
      Object.assign(formData, {
        id: res.data.id,
        username: res.data.username || '',
        phoneNumber: res.data.phoneNumber || '',
        avatar: res.data.avatar || '',
        role: res.data.role || '',
        enabled: res.data.enabled !== false
      })
      userStore.setUserInfo(res.data)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
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
      phoneNumber: userStore.userInfo.phoneNumber || '',
      avatar: userStore.userInfo.avatar || '',
      role: userStore.userInfo.role || '',
      enabled: userStore.userInfo.enabled !== false
    })
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  
  try {
    loading.value = true
    const res = await updateAdminInfo({
      username: formData.username
    })
    
    if (res.code === 200) {
      message.success('保存成功')
      await loadAdminInfo()
      isEditing.value = false
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAvatarUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadAvatar(file.file)
    if (res.code === 200 && res.data) {
      formData.avatar = res.data
      message.success('头像上传成功')
      onFinish()
      await loadAdminInfo()
    } else {
      onError()
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    onError()
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
  } catch (error) {
    return
  }
  
  try {
    loading.value = true
    const res = await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    
    if (res.code === 200) {
      message.success('密码修改成功')
      handleResetPassword()
    }
  } catch (error) {
    console.error('密码修改失败:', error)
  } finally {
    loading.value = false
  }
}

const handleResetPassword = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  if (passwordFormRef.value) {
    passwordFormRef.value.restoreValidation()
  }
}
</script>

<style scoped lang="scss">
.profile-page {
  animation: fadeIn 0.3s ease;
}

.profile-card,
.password-card,
.info-card {
  height: 100%;
}

.profile-content {
  .avatar-section {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 24px;
    padding-bottom: 24px;
    border-bottom: 1px solid #e5e7eb;
    
    .avatar {
      cursor: pointer;
      transition: transform 0.2s;
      
      &:hover {
        transform: scale(1.05);
      }
    }
    
    .user-info {
      flex: 1;
      
      .username {
        font-size: 20px;
        font-weight: 600;
        color: #1f2937;
        margin-bottom: 8px;
      }
    }
  }
}

.edit-actions {
  display: flex;
  gap: 12px;
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
