<template>
  <view class="login-page">
    <view class="login-container">
      <view class="header">
        <text class="title">欢迎登录</text>
        <text class="subtitle">专业影视拍摄一站式服务平台</text>
      </view>

      <view class="form-container">
        <view class="form-item">
          <view class="form-label">
            <uni-icons type="person" size="18" color="#6b7280"></uni-icons>
            <text>用户名/手机号</text>
          </view>
          <input
            v-model="form.username"
            class="form-input"
            type="text"
            placeholder="请输入用户名或手机号"
            maxlength="50"
          />
        </view>

        <view class="form-item">
          <view class="form-label">
            <uni-icons type="locked" size="18" color="#6b7280"></uni-icons>
            <text>密码</text>
          </view>
          <input
            v-model="form.password"
            class="form-input"
            type="password"
            placeholder="请输入密码"
            maxlength="20"
          />
        </view>

        <button
          class="login-btn"
          :class="{ 'login-btn--disabled': !canSubmit || loading }"
          :loading="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <view class="register-link">
          <text>还没有账号？</text>
          <text class="link-text" @click="goToRegister">立即注册</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
// 使用真实后端API，使用别名避免与vuex action冲突
import { login as apiLogin } from '../../services/backend-api'
import { mapActions } from 'vuex'

export default {
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      loading: false
    }
  },
  computed: {
    canSubmit() {
      return this.form.username.trim().length > 0 && this.form.password.length >= 6
    }
  },
  methods: {
    ...mapActions(['login']),
    async handleLogin() {
      if (!this.canSubmit) {
        uni.showToast({
          title: '请填写完整的登录信息',
          icon: 'none'
        })
        return
      }

      this.loading = true
      try {
        const res = await apiLogin({
          username: this.form.username.trim(),
          password: this.form.password
        })

        if (res.code === 200 && res.data) {
          this.login({
            token: res.data.token,
            userInfo: res.data.userInfo
          })

          uni.showToast({
            title: '登录成功',
            icon: 'success'
          })

          setTimeout(() => {
            uni.reLaunch({
              url: '/pages/index/index'
            })
          }, 1000)
        } else {
          uni.showToast({
            title: res.message || '登录失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('登录失败:', error)
        uni.showToast({
          title: error.message || '登录失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    goToRegister() {
      uni.navigateTo({
        url: '/pages/register/register'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx 32rpx;
}

.login-container {
  width: 100%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 80rpx 48rpx 60rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.header {
  text-align: center;
  margin-bottom: 64rpx;
}

.title {
  display: block;
  font-size: 48rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16rpx;
  letter-spacing: -0.5rpx;
}

.subtitle {
  display: block;
  font-size: 26rpx;
  color: #9ca3af;
  font-weight: 400;
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 10rpx;
  font-size: 28rpx;
  color: #4b5563;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 88rpx;
  background: #f9fafb;
  border: 1rpx solid #e5e7eb;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #1f2937;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #6366f1;
  background: #fff;
}

.login-btn {
  width: 100%;
  height: 88rpx;
  background: #1f2937;
  color: #fff;
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 12rpx;
  border: none;
  margin-top: 12rpx;
  transition: all 0.2s;
}

.login-btn:active {
  background: #374151;
}

.login-btn--disabled {
  opacity: 0.5;
  background: #d1d5db;
}

.register-link {
  text-align: center;
  font-size: 26rpx;
  color: #6b7280;
  margin-top: 32rpx;
}

.link-text {
  color: #1f2937;
  font-weight: 500;
  margin-left: 8rpx;
}
</style>
