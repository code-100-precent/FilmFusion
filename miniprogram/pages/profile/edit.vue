<template>
  <view class="edit-page">
    <NavBar title="个人信息" :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y>
      <view class="form-card">
        <view class="form-item">
          <view class="form-label">
            <uni-icons type="person" size="18" color="#6366f1"></uni-icons>
            <text>用户名</text>
          </view>
          <input
            v-model="form.username"
            class="form-input"
            type="text"
            placeholder="请输入用户名"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <view class="form-label">
            <uni-icons type="phone" size="18" color="#6366f1"></uni-icons>
            <text>手机号</text>
          </view>
          <input
            v-model="form.phoneNumber"
            class="form-input"
            type="number"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </view>

        <button
          class="submit-btn"
          :class="{ 'submit-btn--disabled': !canSubmit || loading }"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ loading ? '保存中...' : '保存' }}
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import { getCurrentUserInfo, updateUserInfo } from '../../services/api'
import { mapGetters, mapActions } from 'vuex'

export default {
  components: {
    NavBar
  },
  data() {
    return {
      form: {
        username: '',
        phoneNumber: ''
      },
      loading: false
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    canSubmit() {
      return this.form.username.trim().length >= 3 && 
             this.form.username.trim().length <= 20 &&
             /^1\d{10}$/.test(this.form.phoneNumber)
    }
  },
  onLoad() {
    this.loadUserInfo()
  },
  methods: {
    ...mapActions(['setUserInfo']),
    async loadUserInfo() {
      try {
        const res = await getCurrentUserInfo()
        if (res.code === 200 && res.data) {
          this.form.username = res.data.username || ''
          this.form.phoneNumber = res.data.phoneNumber || ''
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
      }
    },
    async handleSubmit() {
      if (!this.canSubmit) {
        uni.showToast({
          title: '请填写完整信息',
          icon: 'none'
        })
        return
      }

      this.loading = true
      try {
        const res = await updateUserInfo({
          username: this.form.username.trim(),
          phoneNumber: this.form.phoneNumber
        })

        if (res.code === 200 && res.data) {
          this.setUserInfo(res.data)
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1000)
        } else {
          uni.showToast({
            title: res.message || '保存失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('保存失败:', error)
        uni.showToast({
          title: error.message || '保存失败，请稍后重试',
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
.edit-page {
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

