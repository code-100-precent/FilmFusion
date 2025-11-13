<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2 class="login-title">拍在雅安</h2>
        <p class="login-subtitle">管理后台</p>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="email">
          <el-input
            v-model="loginForm.email"
            placeholder="请输入邮箱"
            size="large"
            prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-hint">
        <el-link type="primary" @click="showRegister = true">还没有账号？立即注册</el-link>
      </div>
    </div>

    <!-- 注册对话框 -->
    <el-dialog
      v-model="showRegister"
      title="管理员注册"
      width="400px"
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名（2-32个字符）" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请输入密码（6-20个字符）" 
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入密码" 
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="registerLoading">
          注册
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)
const registerLoading = ref(false)
const showRegister = ref(false)

const loginForm = reactive({
  email: 'admin@example.com',
  password: '123456'
})

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 32, message: '用户名长度必须在2-32个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}


const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      try {
        // 模拟登录成功，无需调用真实API
        const mockToken = 'mock_token_' + Date.now()
        userStore.setToken(mockToken)
        
        // 保存模拟的用户信息
        userStore.setUserInfo({
          id: '1',
          username: '管理员',
          email: loginForm.email,
          avatar: '',
          role: 'admin'
        })
        
        ElMessage.success('登录成功')
        // 延迟一小段时间再跳转，让用户看到成功提示
        setTimeout(() => {
          router.push('/')
        }, 1000)
      } catch (error) {
        ElMessage.error('登录失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate((valid) => {
    if (valid) {
      registerLoading.value = true
      try {
        // 模拟注册成功，无需调用真实API
        ElMessage.success('注册成功')
        showRegister.value = false
        // 自动填充登录表单
        loginForm.email = registerForm.email
        // 清空注册表单
        registerForm.username = ''
        registerForm.email = ''
        registerForm.password = ''
        registerForm.confirmPassword = ''
      } catch (error) {
        ElMessage.error('注册失败，请稍后重试')
      } finally {
        registerLoading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #9c88ff 0%, #b8abff 50%, #d9ceff 100%);
  overflow: hidden;
}

.login-box {
  width: 420px;
  padding: 48px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(156, 136, 255, 0.3);
  animation: fadeInScale 0.5s ease-out;
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
  
  .login-title {
    color: #333;
    font-size: 28px;
    font-weight: 600;
    margin-bottom: 8px;
    background: linear-gradient(135deg, #9c88ff 0%, #b8abff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .login-subtitle {
    color: #999;
    font-size: 14px;
  }
}

.login-form {
  .login-button {
    width: 100%;
    height: 44px;
    font-size: 16px;
    font-weight: 500;
    background: linear-gradient(135deg, #9c88ff 0%, #b8abff 100%);
    border: none;
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(156, 136, 255, 0.4);
    }
  }
}

.login-hint {
  text-align: center;
  margin-top: 24px;
  font-size: 12px;
  color: #999;
  
  .hint-text {
    font-weight: 600;
    color: #9c88ff;
  }
}

:deep(.el-input__wrapper) {
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 0 0 1px rgba(156, 136, 255, 0.5);
  }
  
  &.is-focus {
    box-shadow: 0 0 0 1px #9c88ff;
  }
}
</style>
