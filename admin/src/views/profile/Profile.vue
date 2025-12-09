<template>
  <div class="profile-page">
    <n-spin :show="loading">
      <n-card class="profile-card">
        <!-- 头部信息卡片 -->
        <div class="profile-header">
          <div class="avatar-wrapper">
            <n-avatar 
              :size="120" 
              :src="avatarUrl" 
              round
              class="avatar"
            >
              <template #fallback>
                <Icon icon="mdi:account" :width="110" :height="110" />
              </template>
            </n-avatar>
            <div class="avatar-overlay" @click="handleUploadAvatar">
              <Icon icon="mdi:camera" :width="24" />
              <span>更换头像</span>
            </div>
            <input 
              ref="avatarInput" 
              type="file" 
              accept="image/*" 
              style="display: none" 
              @change="handleAvatarChange"
            />
          </div>
          <div class="user-info">
            <h2 class="username">{{ userInfo.username || '管理员' }}</h2>
            <div class="user-meta">
              <n-tag :type="userInfo.role === 'ADMIN' ? 'success' : 'default'" size="large">
                <template #icon>
                  <Icon icon="mdi:shield-account" />
                </template>
                {{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </n-tag>
              <span class="phone-number">
                <Icon icon="mdi:phone" />
                {{ userInfo.phoneNumber || '未绑定' }}
              </span>
            </div>
          </div>
        </div>

        <n-divider />

        <!-- 内容区域 -->
        <div class="profile-content">
          <!-- 基本信息 -->
          <n-card title="基本信息" class="section-card">
            <template #header-extra>
              <Icon icon="mdi:account-edit" />
            </template>
            <n-descriptions :column="1" bordered>
              <n-descriptions-item label="用户名">
                <n-text strong>{{ userInfo.username || '-' }}</n-text>
              </n-descriptions-item>
              <n-descriptions-item label="手机号">
                <n-text>{{ userInfo.phoneNumber || '未绑定' }}</n-text>
              </n-descriptions-item>
              <n-descriptions-item label="角色">
                <n-tag :type="userInfo.role === 'ADMIN' ? 'success' : 'default'">
                  {{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </n-tag>
              </n-descriptions-item>
              <n-descriptions-item label="账户状态">
                <n-tag :type="userInfo.enabled ? 'success' : 'error'">
                  {{ userInfo.enabled ? '已启用' : '已禁用' }}
                </n-tag>
              </n-descriptions-item>
              <n-descriptions-item label="创建时间">
                <n-text>{{ userInfo.createdAt || '-' }}</n-text>
              </n-descriptions-item>
              <n-descriptions-item label="更新时间">
                <n-text>{{ userInfo.updatedAt || '-' }}</n-text>
              </n-descriptions-item>
            </n-descriptions>
          </n-card>

          <!-- 修改密码 -->
          <n-card title="修改密码" class="section-card">
            <template #header-extra>
              <Icon icon="mdi:lock-reset" />
            </template>
            <n-form 
              ref="passwordFormRef" 
              :model="passwordForm" 
              :rules="passwordRules" 
              label-placement="left"
              label-width="100"
              size="large"
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
                    <Icon icon="mdi:lock-outline" />
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
                    <Icon icon="mdi:lock-plus-outline" />
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
                    <Icon icon="mdi:lock-check-outline" />
                  </template>
                </n-input>
              </n-form-item>
              <n-form-item>
                <n-button 
                  type="primary" 
                  @click="handleChangePassword" 
                  :loading="passwordLoading"
                  size="large"
                  block
                >
                  <template #icon>
                    <Icon icon="mdi:check-circle" />
                  </template>
                  修改密码
                </n-button>
              </n-form-item>
            </n-form>
          </n-card>
        </div>
      </n-card>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NForm,
  NFormItem,
  NInput,
  NButton,
  NAvatar,
  NTag,
  NDivider,
  NDescriptions,
  NDescriptionsItem,
  NText,
  NSpin,
  useMessage
} from 'naive-ui'
import { useUserStore } from '@/store/user'
import { getAdminInfo, changePassword, uploadAvatar } from '@/api'
import { getImageUrl } from '@/utils/image'

const userStore = useUserStore()
const message = useMessage()

const userInfo = ref({})
const loading = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const avatarInput = ref(null)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 计算头像URL
const avatarUrl = computed(() => {
  if (!userInfo.value || !userInfo.value.avatar) {
    return ''
  }
  return getImageUrl(userInfo.value.avatar)
})

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
    console.log('获取用户信息响应:', res)
    if (res.code === 200 && res.data) {
      userInfo.value = res.data
      console.log('用户信息:', userInfo.value)
      console.log('头像字段值:', userInfo.value.avatar)
      console.log('计算后的头像URL:', avatarUrl.value)
      userStore.setUserInfo(res.data)
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
      const avatarUrl = res.data.originUrl || res.data.thumbUrl || res.data.url || res.data.path
      if (avatarUrl) {
        // 更新用户信息
        userInfo.value.avatar = avatarUrl
        userStore.setUserInfo({ ...userInfo.value, avatar: avatarUrl })
        message.success('头像更新成功')
        
        // 重新加载用户信息以确保数据同步
        await loadUserInfo()
      } else {
        message.error('上传成功但未获取到头像URL')
      }
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
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
  animation: fadeIn 0.3s ease;
}

.profile-card {
  :deep(.n-card__content) {
    padding: 32px;
  }
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 24px 0;

  .avatar-wrapper {
    position: relative;
    flex-shrink: 0;

    .avatar {
      border: 4px solid #e5e7eb;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;
    }

    .avatar-overlay {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 40px;
      height: 40px;
      background: #18a058;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      cursor: pointer;
      box-shadow: 0 2px 8px rgba(24, 160, 88, 0.3);
      transition: all 0.3s ease;
      overflow: hidden;

      span {
        display: none;
      }

      &:hover {
        width: auto;
        padding: 0 12px;
        border-radius: 20px;

        span {
          display: inline;
          margin-left: 4px;
          font-size: 12px;
          white-space: nowrap;
        }
      }
    }
  }

  .user-info {
    flex: 1;

    .username {
      margin: 0 0 16px 0;
      color: #1f2937;
      font-size: 28px;
      font-weight: 600;
    }

    .user-meta {
      display: flex;
      align-items: center;
      gap: 16px;
      flex-wrap: wrap;

      .phone-number {
        display: flex;
        align-items: center;
        gap: 6px;
        color: #6b7280;
        font-size: 14px;

        .iconify {
          color: #9ca3af;
        }
      }
    }
  }
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  :deep(.n-card__content) {
    padding: 24px;
  }

  :deep(.n-card-header) {
    padding: 16px 24px;
    border-bottom: 1px solid #e5e7eb;

    .n-card-header__main {
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;
    }

    .n-card-header__extra {
      color: #6b7280;
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  .profile-page {
    padding: 16px;
  }

  .profile-card {
    :deep(.n-card__content) {
      padding: 20px;
    }
  }

  .profile-header {
    flex-direction: column;
    text-align: center;
    gap: 20px;

    .user-info {
      .username {
        font-size: 24px;
      }

      .user-meta {
        justify-content: center;
        flex-direction: column;
        gap: 12px;
      }
    }
  }

  .section-card {
    :deep(.n-card__content) {
      padding: 16px;
    }
  }

  :deep(.n-form-item) {
    margin-bottom: 20px;
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

