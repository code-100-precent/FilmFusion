<template>
  <view class="login-page">
    <view class="login-card">
      <view class="login-card__header">
        <view class="brand-icon">
          <uni-icons type="contact-filled" color="#6366f1" size="60" />
		</view>
        <view class="title">欢迎回来</view>
        <view class="subtitle">登录影务服务平台，管理报备与协拍需求</view>
			</view>
			
      <view class="form">
        <view class="field">
          <text class="label">手机号</text>
						<input 
            v-model="form.phone"
							class="input" 
							type="number" 
							maxlength="11"
            placeholder="请输入 11 位手机号"
						/>
				</view>
				
        <view class="field">
          <text class="label">密码</text>
						<input 
            v-model="form.password"
							class="input" 
							type="password" 
							placeholder="请输入密码"
            maxlength="20"
						/>
				</view>
				
        <view class="identity">
          <text class="label">身份类型</text>
						<view class="identity-options">
							<view 
              v-for="option in identityOptions"
              :key="option.value"
              class="identity-option"
              :class="{ active: form.identity === option.value }"
              @click="form.identity = option.value"
            >
              {{ option.label }}
						</view>
					</view>
				</view>
				
        <button class="submit-btn" :loading="loading" :disabled="!canSubmit" @click="submit">
          {{ loading ? '登录中...' : '登录' }}
				</button>
      </view>

      <view class="extra">
        <text>首次使用？</text>
        <text class="link" @click="goRegister">创建账号</text>
			</view>
		</view>
	</view>
</template>

<script>
import { visitorLogin } from '../../services/api'

const IDENTITY_OPTIONS = [
  { label: '剧组', value: '剧组' },
  { label: '制作人', value: '制作人' },
  { label: '其他', value: '其他' }
]
	
	export default {
		data() {
			return {
      form: {
					phone: '',
					password: '',
        identity: '剧组'
      },
      identityOptions: IDENTITY_OPTIONS,
      loading: false
			}
		},
		computed: {
    canSubmit() {
      return /^1\d{10}$/.test(this.form.phone) && this.form.password.length >= 6 && !!this.form.identity
			}
		},
		methods: {
    async submit() {
      if (!this.canSubmit) {
        uni.showToast({ title: '请完善登录信息', icon: 'none' })
					return
				}
				
				this.loading = true
      try {
        const { data } = await visitorLogin({
          phone: this.form.phone,
          password: this.form.password
        })

        if (data && data.token) {
          uni.setStorageSync('token', data.token)
          uni.setStorageSync('visitorInfo', data)
        }

        uni.showToast({ title: '登录成功', icon: 'success' })
					setTimeout(() => {
          uni.reLaunch({ url: '/pages/scenes/scenes' })
        }, 800)
				} catch (error) {
        uni.showToast({ title: (error && error.message) || '登录失败', icon: 'none' })
      } finally {
					this.loading = false
      }
    },
    goRegister() {
      uni.navigateTo({ url: '/pages/register/register' })
			}
		}
	}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #eef2ff 0%, #ede9fe 100%);
		display: flex;
		align-items: center;
		justify-content: center;
  padding: 60rpx 40rpx;
		box-sizing: border-box;
	}
	
.login-card {
		width: 100%;
  max-width: 640rpx;
  background: #fff;
  border-radius: 36rpx;
  padding: 48rpx 44rpx 40rpx;
  box-shadow: 0 28rpx 68rpx rgba(99, 102, 241, 0.25);
  display: flex;
  flex-direction: column;
  gap: 42rpx;
}

.login-card__header {
		text-align: center;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  color: #1f2937;
}

.brand-icon {
		width: 120rpx;
		height: 120rpx;
  margin: 0 auto;
  border-radius: 50%;
  background: rgba(99, 102, 241, 0.12);
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.title {
		font-size: 44rpx;
  font-weight: 700;
	}
	
	.subtitle {
  font-size: 26rpx;
  color: #6b7280;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.field {
			display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.label {
  font-size: 26rpx;
  color: #4b5563;
		}
		
		.input {
  width: 100%;
  height: 88rpx;
  border-radius: 18rpx;
  background: #f9fafb;
  border: 1rpx solid rgba(226, 232, 240, 0.6);
  padding: 0 24rpx;
			font-size: 28rpx;
  color: #1f2937;
}

.identity {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
		}
		
		.identity-options {
			display: flex;
			gap: 16rpx;
		}
		
.identity-option {
			flex: 1;
  height: 80rpx;
  border-radius: 18rpx;
  background: #f5f3ff;
  color: #6655d4;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 26rpx;
  border: 1rpx solid transparent;
}
			
.identity-option.active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
				color: #fff;
  border-color: rgba(99, 102, 241, 0.6);
  box-shadow: 0 12rpx 28rpx rgba(99, 102, 241, 0.25);
}

.submit-btn {
  margin-top: 12rpx;
  height: 92rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
		color: #fff;
		font-size: 32rpx;
  font-weight: 600;
		border: none;
  letter-spacing: 2rpx;
  box-shadow: 0 22rpx 38rpx rgba(99, 102, 241, 0.32);
}

.submit-btn:disabled {
  opacity: 0.6;
}

.extra {
		display: flex;
		justify-content: center;
  gap: 10rpx;
		font-size: 24rpx;
  color: #6b7280;
}

.link {
  color: #4f46e5;
  font-weight: 600;
	}
</style>

