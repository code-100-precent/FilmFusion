<template>
  <view class="password-page">
    <NavBar title="修改密码" :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y>
      <view class="form-card">
        <view class="form-item">
          <view class="form-label">
            <uni-icons type="locked" size="18" color="#6366f1"></uni-icons>
            <text>原密码</text>
          </view>
          <input
            v-model="form.oldPassword"
            class="form-input"
            type="password"
            placeholder="请输入原密码"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <view class="form-label">
            <uni-icons type="locked" size="18" color="#6366f1"></uni-icons>
            <text>新密码</text>
          </view>
          <input
            v-model="form.newPassword"
            class="form-input"
            type="password"
            placeholder="请输入新密码（至少6位）"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <view class="form-label">
            <uni-icons type="locked" size="18" color="#6366f1"></uni-icons>
            <text>确认新密码</text>
          </view>
          <input
            v-model="form.confirmPassword"
            class="form-input"
            type="password"
            placeholder="请再次输入新密码"
            maxlength="20"
          />
        </view>

        <button
          class="submit-btn"
          :class="{ 'submit-btn--disabled': !canSubmit || loading }"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ loading ? '修改中...' : '确认修改' }}
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import { changePassword } from '../../services/api'

export default {
  components: {
    NavBar
  },
  data() {
    return {
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      loading: false
    }
  },
  computed: {
    canSubmit() {
      return this.form.oldPassword.length >= 6 &&
             this.form.newPassword.length >= 6 &&
             this.form.newPassword === this.form.confirmPassword
    }
  },
  methods: {
    async handleSubmit() {
      if (!this.canSubmit) {
        uni.showToast({
          title: '请填写完整信息',
          icon: 'none'
        })
        return
      }

      if (this.form.newPassword !== this.form.confirmPassword) {
        uni.showToast({
          title: '两次输入的密码不一致',
          icon: 'none'
        })
        return
      }

      this.loading = true
      try {
        const res = await changePassword({
          oldPassword: this.form.oldPassword,
          newPassword: this.form.newPassword
        })

        if (res.code === 200) {
          uni.showToast({
            title: '修改成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1000)
        } else {
          uni.showToast({
            title: res.message || '修改失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        uni.showToast({
          title: error.message || '修改失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.password-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  height: calc(100vh - 132rpx);
  padding: 24rpx 32rpx;
  padding-top: 40rpx;
  padding-bottom: 40rpx;
  box-sizing: border-box;
  width: 100%;
}

.form-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  width: 100%;
  box-sizing: border-box;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 40rpx;
}

.form-item:last-of-type {
  margin-bottom: 0;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
  color: #374151;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 88rpx;
  background: #f9fafb;
  border: 1rpx solid #e5e7eb;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1f2937;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #6366f1;
  background: #fff;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: #6366f1;
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 12rpx;
  border: none;
  margin-top: 40rpx;
  box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.3);
  transition: all 0.2s;
}

.submit-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 2rpx 8rpx rgba(99, 102, 241, 0.2);
}

.submit-btn--disabled {
  opacity: 0.6;
  background: #d1d5db;
}
</style>

