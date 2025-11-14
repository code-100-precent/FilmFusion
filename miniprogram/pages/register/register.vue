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
import { userRegister } from '../../services/api'
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
        const res = await userRegister({
          username: this.form.username.trim(),
          phoneNumber: this.form.phoneNumber,
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx 32rpx;
  position: relative;
  overflow: hidden;
}

.register-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="20" cy="20" r="2" fill="rgba(255,255,255,0.1)"/><circle cx="80" cy="40" r="1.5" fill="rgba(255,255,255,0.08)"/><circle cx="40" cy="70" r="1" fill="rgba(255,255,255,0.06)"/><circle cx="90" cy="80" r="2.5" fill="rgba(255,255,255,0.09)"/><circle cx="10" cy="60" r="1" fill="rgba(255,255,255,0.05)"/></svg>') repeat;
  opacity: 0.3;
  z-index: 0;
}

.register-container {
  width: 100%;
  max-width: 600rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-radius: 32rpx;
  padding: 48rpx 40rpx 36rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
  border: 1rpx solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 1;
}

.header {
  text-align: center;
  margin-bottom: 40rpx;
}

.title {
  display: block;
  font-size: 42rpx;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 12rpx;
  letter-spacing: -0.5rpx;
}

.subtitle {
  display: block;
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 400;
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: #374151;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 80rpx;
  background: rgba(249, 250, 251, 0.8);
  border: 1.5rpx solid #e5e7eb;
  border-radius: 16rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #1f2937;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #6366f1;
  background: #fff;
  box-shadow: 0 0 0 3rpx rgba(99, 102, 241, 0.1);
}

.register-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
  border-radius: 16rpx;
  border: none;
  margin-top: 8rpx;
  transition: all 0.2s;
  box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.3);
}

.register-btn:active {
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 16rpx rgba(99, 102, 241, 0.4);
}

.register-btn--disabled {
  opacity: 0.5;
  background: #d1d5db;
  box-shadow: none;
}

.login-link {
  text-align: center;
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 24rpx;
}

.link-text {
  color: #6366f1;
  font-weight: 600;
  margin-left: 6rpx;
}
</style>
