<template>
  <div class="profile-page">
    <n-grid :cols="24" :x-gap="24" :y-gap="24" responsive="screen">
      <!-- 个人信息卡片 -->
      <n-gi :span="24">
        <n-card class="profile-card" :bordered="false">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <Icon icon="mdi:account-circle" :width="24" style="margin-right: 8px; color: #18a058;" />
                <span>个人信息</span>
              </div>
              <div class="header-actions">
                <n-button
                  v-if="!isEditing"
                  type="primary"
                  @click="handleEdit"
                  size="medium"
                >
                  <template #icon>
                    <Icon icon="mdi:pencil-outline" />
                  </template>
                  编辑信息
                </n-button>
                <div v-else class="edit-actions">
                  <n-button @click="handleCancel" size="medium">取消</n-button>
                  <n-button type="primary" @click="handleSave" :loading="loading" size="medium">
                    <template #icon>
                      <Icon icon="mdi:check" />
                    </template>
                    保存
                  </n-button>
                </div>
              </div>
            </div>
          </template>
          
          <div class="profile-content">
            <!-- 头像和基本信息 -->
            <div class="avatar-section">
              <div class="avatar-wrapper">
                <n-upload
                  :file-list="avatarFileList"
                  :max="1"
                  :show-file-list="false"
                  :custom-request="handleAvatarUpload"
                  accept="image/*"
                  :disabled="!isEditing"
                >
                  <template #trigger>
                    <div class="avatar-container">
                      <n-avatar
                        :size="100"
                        :src="formData.avatar"
                        class="avatar"
                        :fallback-src="null"
                      >
                        <Icon icon="mdi:account" :width="50" />
                      </n-avatar>
                      <div v-if="isEditing" class="avatar-overlay">
                        <Icon icon="mdi:camera" :width="24" />
                        <span>更换头像</span>
                      </div>
                    </div>
                  </template>
                </n-upload>
              </div>
              <div class="user-info">
                <h2 class="username">{{ formData.username || '管理员' }}</h2>
                <div class="user-tags">
                  <n-tag :type="formData.role === 'ADMIN' ? 'success' : 'default'" size="medium" round>
                    <template #icon>
                      <Icon icon="mdi:shield-account" />
                    </template>
                    {{ formData.role === 'ADMIN' ? '管理员' : '普通用户' }}
                  </n-tag>
                  <n-tag :type="formData.enabled ? 'success' : 'error'" size="medium" round>
                    <template #icon>
                      <Icon :icon="formData.enabled ? 'mdi:check-circle' : 'mdi:close-circle'" />
                    </template>
                    {{ formData.enabled ? '已启用' : '已禁用' }}
                  </n-tag>
                </div>
                <div class="user-meta">
                  <div class="meta-item">
                    <Icon icon="mdi:phone" :width="16" />
                    <span>{{ formData.phoneNumber || '未设置' }}</span>
                  </div>
                  <div class="meta-item" v-if="formData.id">
                    <Icon icon="mdi:identifier" :width="16" />
                    <span>ID: {{ formData.id }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 编辑表单 -->
            <n-divider />
            <n-form
              ref="formRef"
              :model="formData"
              :rules="formRules"
              label-placement="left"
              label-width="120"
              :disabled="!isEditing"
              class="profile-form"
            >
              <n-grid :cols="2" :x-gap="24" responsive="screen">
                <n-gi :span="2" :s-span="1">
                  <n-form-item label="用户名" path="username">
                    <n-input 
                      v-model:value="formData.username" 
                      placeholder="请输入用户名"
                      :disabled="!isEditing"
                      clearable
                    >
                      <template #prefix>
                        <Icon icon="mdi:account" />
                      </template>
                    </n-input>
                  </n-form-item>
                </n-gi>
                <n-gi :span="2" :s-span="1">
                  <n-form-item label="手机号">
                    <n-input 
                      v-model:value="formData.phoneNumber" 
                      disabled 
                      placeholder="手机号不可修改"
                    >
                      <template #prefix>
                        <Icon icon="mdi:phone" />
                      </template>
                    </n-input>
                  </n-form-item>
                </n-gi>
              </n-grid>
            </n-form>
          </div>
        </n-card>
      </n-gi>
      
      <!-- 修改密码卡片 -->
      <n-gi :span="12" :s-span="24">
        <n-card class="password-card" :bordered="false">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <Icon icon="mdi:lock-reset" :width="24" style="margin-right: 8px; color: #2080f0;" />
                <span>修改密码</span>
              </div>
            </div>
          </template>
          <n-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-placement="left"
            label-width="100"
            class="password-form"
          >
            <n-form-item label="当前密码" path="oldPassword">
              <n-input
                v-model:value="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password-on="click"
                clearable
              >
                <template #prefix>
                  <Icon icon="mdi:lock" />
                </template>
              </n-input>
            </n-form-item>
            <n-form-item label="新密码" path="newPassword">
              <n-input
                v-model:value="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（6-20个字符）"
                show-password-on="click"
                clearable
              >
                <template #prefix>
                  <Icon icon="mdi:lock-outline" />
                </template>
              </n-input>
            </n-form-item>
            <n-form-item label="确认密码" path="confirmPassword">
              <n-input
                v-model:value="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password-on="click"
                clearable
              >
                <template #prefix>
                  <Icon icon="mdi:lock-check" />
                </template>
              </n-input>
            </n-form-item>
            <n-form-item>
              <div class="form-actions">
                <n-button type="primary" @click="handleChangePassword" :loading="loading" block>
                  <template #icon>
                    <Icon icon="mdi:check-circle" />
                  </template>
                  修改密码
                </n-button>
                <n-button @click="handleResetPassword" block style="margin-top: 12px">
                  <template #icon>
                    <Icon icon="mdi:refresh" />
                  </template>
                  重置
                </n-button>
              </div>
            </n-form-item>
          </n-form>
        </n-card>
      </n-gi>
      
      <!-- 账户信息卡片 -->
      <n-gi :span="12" :s-span="24">
        <n-card class="info-card" :bordered="false">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <Icon icon="mdi:information" :width="24" style="margin-right: 8px; color: #f0a020;" />
                <span>账户信息</span>
              </div>
            </div>
          </template>
          <n-descriptions :column="1" bordered size="medium" class="info-descriptions">
            <n-descriptions-item label="用户ID">
              <div class="info-value">
                <Icon icon="mdi:identifier" :width="16" style="margin-right: 8px;" />
                {{ formData.id || '-' }}
              </div>
            </n-descriptions-item>
            <n-descriptions-item label="角色">
              <n-tag :type="formData.role === 'ADMIN' ? 'success' : 'default'" size="medium" round>
                <template #icon>
                  <Icon icon="mdi:shield-account" />
                </template>
                {{ formData.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="账户状态">
              <n-tag :type="formData.enabled ? 'success' : 'error'" size="medium" round>
                <template #icon>
                  <Icon :icon="formData.enabled ? 'mdi:check-circle' : 'mdi:close-circle'" />
                </template>
                {{ formData.enabled ? '已启用' : '已禁用' }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="手机号">
              <div class="info-value">
                <Icon icon="mdi:phone" :width="16" style="margin-right: 8px;" />
                {{ formData.phoneNumber || '未设置' }}
              </div>
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
  NDivider,
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
  padding: 0;
}

.profile-card,
.password-card,
.info-card {
  height: 100%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-title {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
  }
  
  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.edit-actions {
  display: flex;
  gap: 12px;
}

.profile-content {
  .avatar-section {
    display: flex;
    align-items: flex-start;
    gap: 32px;
    margin-bottom: 24px;
    padding: 24px;
    background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
    border-radius: 12px;
    
    .avatar-wrapper {
      flex-shrink: 0;
    }
    
    .avatar-container {
      position: relative;
      cursor: pointer;
      
      .avatar {
        border: 4px solid #ffffff;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transition: all 0.3s ease;
        
        &:hover {
          transform: scale(1.05);
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
        }
      }
      
      .avatar-overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: rgba(0, 0, 0, 0.6);
        color: white;
        padding: 8px;
        border-radius: 0 0 50px 50px;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        font-size: 12px;
        opacity: 0;
        transition: opacity 0.3s ease;
        
        span {
          font-size: 12px;
        }
      }
      
      &:hover .avatar-overlay {
        opacity: 1;
      }
    }
    
    .user-info {
      flex: 1;
      
      .username {
        font-size: 28px;
        font-weight: 700;
        color: #1f2937;
        margin-bottom: 16px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
      
      .user-tags {
        display: flex;
        gap: 12px;
        margin-bottom: 16px;
        flex-wrap: wrap;
      }
      
      .user-meta {
        display: flex;
        flex-direction: column;
        gap: 12px;
        
        .meta-item {
          display: flex;
          align-items: center;
          gap: 8px;
          color: #6b7280;
          font-size: 14px;
          
          span {
            color: #374151;
          }
        }
      }
    }
  }
  
  .profile-form {
    padding: 0 24px;
  }
}

.password-form {
  padding: 8px 0;
}

.form-actions {
  width: 100%;
}

.info-descriptions {
  :deep(.n-descriptions-item__label) {
    font-weight: 500;
    color: #374151;
  }
  
  .info-value {
    display: flex;
    align-items: center;
    color: #6b7280;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile-content {
    .avatar-section {
      flex-direction: column;
      align-items: center;
      text-align: center;
      
      .user-info {
        width: 100%;
        
        .user-tags {
          justify-content: center;
        }
        
        .user-meta {
          align-items: center;
        }
      }
    }
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    
    .header-actions {
      width: 100%;
      
      .edit-actions {
        width: 100%;
        
        button {
          flex: 1;
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
