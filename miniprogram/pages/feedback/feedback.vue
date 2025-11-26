<template>
  <view class="feedback-page">
    <NavBar title="意见反馈" :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y>
      <view class="form-card">
        <view class="form-item">
          <view class="form-label">
            <uni-icons type="list" size="18" color="#6366f1"></uni-icons>
            <text>反馈类型</text>
          </view>
          <picker
            mode="selector"
            :range="typeOptions"
            range-key="label"
            :value="typeIndex"
            @change="onTypeChange"
          >
            <view class="picker-view">
              <text :class="{ 'picker-placeholder': typeIndex === -1 }">
                {{ typeIndex === -1 ? '请选择反馈类型' : typeOptions[typeIndex].label }}
              </text>
              <uni-icons type="right" size="16" color="#9ca3af"></uni-icons>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <view class="form-label">
            <uni-icons type="chatboxes" size="18" color="#6366f1"></uni-icons>
            <text>反馈内容</text>
          </view>
          <textarea
            v-model="form.content"
            class="form-textarea"
            placeholder="请详细描述您的问题或建议..."
            maxlength="500"
            :show-confirm-bar="false"
          ></textarea>
          <view class="char-count">{{ form.content.length }}/500</view>
        </view>

        <button
          class="submit-btn"
          :class="{ 'submit-btn--disabled': !canSubmit || loading }"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ loading ? '提交中...' : '提交反馈' }}
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import { createFeedback } from '../../services/backend-api'
import { mapGetters } from 'vuex'

export default {
  components: {
    NavBar
  },
  data() {
    return {
      form: {
        type: '',
        content: ''
      },
      typeIndex: -1,
      typeOptions: [
        { label: '功能建议', value: 'FEATURE' },
        { label: '问题反馈', value: 'BUG' },
        { label: '使用咨询', value: 'QUESTION' },
        { label: '其他', value: 'OTHER' }
      ],
      loading: false
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn']),
    canSubmit() {
      return this.typeIndex !== -1 && this.form.content.trim().length >= 10
    }
  },
  onLoad() {
    if (!this.isLoggedIn) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  },
  methods: {
    onTypeChange(e) {
      this.typeIndex = parseInt(e.detail.value)
      this.form.type = this.typeOptions[this.typeIndex].value
    },
    async handleSubmit() {
      if (!this.canSubmit) {
        uni.showToast({
          title: '请填写完整的反馈信息',
          icon: 'none'
        })
        return
      }

      this.loading = true
      try {
        const res = await createFeedback({
          type: this.form.type,
          content: this.form.content.trim()
        })

        if (res.code === 200) {
          uni.showToast({
            title: '反馈提交成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.message || '提交失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('提交反馈失败:', error)
        uni.showToast({
          title: error.message || '提交失败，请稍后重试',
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
.feedback-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  width: 100%;
  height: calc(100vh - 132rpx);
  padding: 24rpx 32rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  /* 兼容火狐浏览器 */
  scrollbar-width: none;
  /* 兼容IE浏览器 */
  -ms-overflow-style: none;
}

.form-card {
  width: 100%;
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 32rpx;
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

.picker-view {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 88rpx;
  background: #f9fafb;
  border: 1rpx solid #e5e7eb;
  border-radius: 12rpx;
  padding: 0 24rpx;
  box-sizing: border-box;
  font-size: 28rpx;
  color: #1f2937;
}

.picker-placeholder {
  color: #9ca3af;
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  background: #f9fafb;
  border: 1rpx solid #e5e7eb;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  color: #1f2937;
  box-sizing: border-box;
  line-height: 1.6;
}

.char-count {
  font-size: 24rpx;
  color: #9ca3af;
  text-align: right;
  margin-top: 8rpx;
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

