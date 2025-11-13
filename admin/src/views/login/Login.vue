<template>
  <div class="login-container">
    <n-card class="login-card" :bordered="false">
      <div class="login-header">
        <Icon icon="mdi:film" :width="48" :height="48" class="logo-icon" />
        <h2 class="login-title">拍在雅安</h2>
        <p class="login-subtitle">管理后台</p>
      </div>
      
      <n-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        size="large"
        @submit.prevent="handleLogin"
      >
        <n-form-item path="username" label="用户名">
          <n-input
            v-model:value="loginForm.username"
            placeholder="请输入用户名"
            :input-props="{ autocomplete: 'username' }"
            clearable
          >
            <template #prefix>
              <Icon icon="mdi:account-outline" />
            </template>
          </n-input>
        </n-form-item>
        
        <n-form-item path="password" label="密码">
          <n-input
            v-model:value="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :input-props="{ autocomplete: 'current-password' }"
            show-password-on="click"
            clearable
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <Icon icon="mdi:lock-outline" />
            </template>
          </n-input>
        </n-form-item>
        
        <n-form-item>
          <n-button
            type="primary"
            block
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-button"
          >
            登录
          </n-button>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NForm,
  NFormItem,
  NInput,
  NButton,
  useMessage
} from 'naive-ui'
import { adminLogin } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  
  loading.value = true
  try {
    const res = await adminLogin(loginForm.username, loginForm.password)
    
    if (res.code === 200 && res.data) {
      // 检查用户角色是否为管理员
      if (res.data.userInfo && res.data.userInfo.role !== 'ADMIN') {
        message.error('您不是管理员，无法登录管理后台')
        return
      }
      
      // 保存 token
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data.userInfo)
      
      message.success('登录成功')
      
      // 跳转到首页
      setTimeout(() => {
        router.push('/')
      }, 500)
    } else {
      message.error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    // 错误消息由 request.js 的拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-card {
  width: 420px;
  padding: 48px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  
  :deep(.n-card__content) {
    padding: 0;
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
  
  .logo-icon {
    color: #1f2937;
    margin-bottom: 16px;
  }
  
  .login-title {
    color: #1f2937;
    font-size: 28px;
    font-weight: 600;
    margin-bottom: 8px;
    letter-spacing: 0.5px;
  }
  
  .login-subtitle {
    color: #6b7280;
    font-size: 14px;
    margin: 0;
  }
}

.login-button {
  margin-top: 8px;
  font-weight: 500;
  height: 44px;
}

:deep(.n-input) {
  .n-input__prefix {
    color: #9ca3af;
  }
}
</style>
