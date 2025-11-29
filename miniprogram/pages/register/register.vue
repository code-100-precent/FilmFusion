<template>
  <view class="register-page">
    <view class="register-container">
      <view class="header">
        <text class="title">注册账号</text>
        <text class="subtitle">加入雅安影视服务平台</text>
      </view>

      <view class="form-container">
        <view class="form-item">
          <view class="form-label">
            <uni-icons type="person" size="18" color="#6b7280"></uni-icons>
            <text>用户名</text>
          </view>
          <input
            v-model="form.username"
            class="form-input"
            type="text"
            placeholder="请输入用户名（3-20个字符）"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <view class="form-label">
            <uni-icons type="phone" size="18" color="#6b7280"></uni-icons>
            <text>手机号</text>
          </view>
          <input
            v-model="form.phoneNumber"
            class="form-input"
            type="number"
            placeholder="请输入11位手机号"
            maxlength="11"
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
            placeholder="请输入密码（至少6位）"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <view class="form-label">
            <uni-icons type="locked" size="18" color="#6b7280"></uni-icons>
            <text>确认密码</text>
          </view>
          <input
            v-model="form.confirmPassword"
            class="form-input"
            type="password"
            placeholder="请再次输入密码"
            maxlength="20"
          />
        </view>

        <button
          class="register-btn"
          :class="{ 'register-btn--disabled': !canSubmit || loading }"
          :loading="loading"
          @click="handleRegister"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <view class="login-link">
          <text>已有账号？</text>
          <text class="link-text" @click="goToLogin">立即登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
// 使用真实后端API
import { register } from '../../services/backend-api'
import { mapActions } from 'vuex'

export default {
  data() {
    return {
      form: {
        username: '',
        phoneNumber: '',
        password: '',
        confirmPassword: ''
      },
      loading: false
    }
  },
  computed: {
    canSubmit() {
      return (
        this.form.username.trim().length >= 3 &&
        this.form.username.trim().length <= 20 &&
        /^1\d{10}$/.test(this.form.phoneNumber) &&
        this.form.password.length >= 6 &&
        this.form.password === this.form.confirmPassword
      )
    }
  },
  methods: {
    ...mapActions(['login']),
    async handleRegister() {
      if (!this.canSubmit) {
        uni.showToast({
          title: '请填写完整的注册信息',
          icon: 'none'
        })
        return
      }

      if (this.form.password !== this.form.confirmPassword) {
        uni.showToast({
          title: '两次输入的密码不一致',
          icon: 'none'
        })
        return
      }

      this.loading = true
      try {
        const res = await register({
          username: this.form.username.trim(),
          phone: this.form.phoneNumber,
          password: this.form.password
        })

        if (res.code === 200 && res.data) {
          this.login({
            token: res.data.token,
            userInfo: res.data.userInfo
          })

          uni.showToast({
            title: '注册成功',
            icon: 'success'
          })

          setTimeout(() => {
            uni.reLaunch({
              url: '/pages/index/index'
            })
          }, 1000)
        } else {
          uni.showToast({
            title: res.message || '注册失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('注册失败:', error)
        uni.showToast({
          title: error.message || '注册失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    goToLogin() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx 32rpx;
}

.register-container {
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

.register-btn {
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

.register-btn:active {
  background: #374151;
}

.register-btn--disabled {
  opacity: 0.5;
  background: #d1d5db;
}

.login-link {
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
