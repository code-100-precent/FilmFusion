<template>
	<view class="login-container">
		<!-- 背景装饰 -->
		<view class="bg-decoration">
			<view class="decoration-circle circle-1"></view>
			<view class="decoration-circle circle-2"></view>
			<view class="decoration-circle circle-3"></view>
		</view>
		
		<view class="login-content">
			<view class="login-header">
				<view class="logo-wrapper">
					<view class="logo">
						<uni-icons type="contact-filled" color="#9D8DF1" size="56"></uni-icons>
					</view>
					<view class="logo-ring"></view>
				</view>
				<text class="title">欢迎回来</text>
				<text class="subtitle">登录以继续使用校园导览</text>
			</view>
			
			<view class="login-form">
				<view class="form-item">
					<view class="input-wrapper">
						<uni-icons type="phone" color="#9D8DF1" size="20"></uni-icons>
						<input 
							class="input" 
							type="number" 
							placeholder="请输入手机号"
							v-model="formData.phone"
							placeholder-style="color: #B8A9E8"
							maxlength="11"
						/>
					</view>
				</view>
				
				<view class="form-item">
					<view class="input-wrapper">
						<uni-icons type="locked" color="#9D8DF1" size="20"></uni-icons>
						<input 
							class="input" 
							type="password" 
							placeholder="请输入密码"
							v-model="formData.password"
							placeholder-style="color: #B8A9E8"
						/>
					</view>
				</view>
				
				<view class="form-item">
					<view class="identity-wrapper">
						<text class="identity-label">身份类型</text>
						<view class="identity-options">
							<view 
								v-for="(item, index) in identityOptions" 
								:key="index"
								class="identity-item"
								:class="{ active: formData.identity === item.value }"
								@click="formData.identity = item.value"
							>
								{{ item.label }}
							</view>
						</view>
					</view>
				</view>
				
				<button class="login-btn" @click="handleLogin" :loading="loading" :disabled="loading || !canLogin">
					<text v-if="!loading">登录</text>
					<text v-else>登录中...</text>
				</button>
				
				<view class="login-footer">
					<text class="footer-text">还没有账号？</text>
					<text class="register-text" @click="handleRegister">立即注册</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { visitorLogin } from '../../services/api.ts'
	
	export default {
		name: 'Login',
		data() {
			return {
				loading: false,
				identityOptions: [
					{ label: '新生', value: '新生' },
					{ label: '家长', value: '家长' },
					{ label: '其他', value: '其他' }
				],
				formData: {
					phone: '',
					password: '',
					identity: '新生'
				}
			}
		},
		computed: {
			canLogin() {
				return this.formData.phone.length === 11 && this.formData.password.length >= 6 && this.formData.identity
			}
		},
		methods: {
			// 登录
			async handleLogin() {
				if (!this.formData.phone) {
					uni.showToast({
						title: '请输入手机号',
						icon: 'none'
					})
					return
				}
				
				if (this.formData.phone.length !== 11) {
					uni.showToast({
						title: '请输入正确的手机号',
						icon: 'none'
					})
					return
				}
				
				if (!this.formData.password) {
					uni.showToast({
						title: '请输入密码',
						icon: 'none'
					})
					return
				}
				
				if (this.formData.password.length < 6) {
					uni.showToast({
						title: '密码长度至少6位',
						icon: 'none'
					})
					return
				}
				
				if (!this.formData.identity) {
					uni.showToast({
						title: '请选择身份类型',
						icon: 'none'
					})
					return
				}
				
				this.loading = true
				
				try {
					console.log('开始登录请求:', {
						phone: this.formData.phone,
						password: '***',
						identity: this.formData.identity
					})
					
					const res = await visitorLogin({
						phone: this.formData.phone,
						password: this.formData.password
					})
					
					console.log('登录响应:', res)
					
					// 保存登录信息
					uni.setStorageSync('token', res.data.token)
					uni.setStorageSync('visitorId', res.data.visitorId)
					uni.setStorageSync('identity', res.data.identity)
					
					this.loading = false
					uni.showToast({
						title: res.message || '登录成功',
						icon: 'success'
					})
					
					// 跳转到首页
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/index/index'
						})
					}, 1500)
				} catch (error) {
					this.loading = false
					uni.showToast({
						title: error.message || '登录失败',
						icon: 'none'
					})
				}
			},
			
			handleRegister() {
				uni.navigateTo({
					url: '/pages/register/register'
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.login-container {
		height: 100vh;
		background: linear-gradient(180deg, #F8F6FF 0%, #E8E1FF 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 30rpx;
		position: relative;
		overflow: hidden;
		box-sizing: border-box;
	}
	
	.bg-decoration {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		overflow: hidden;
		z-index: 0;
	}
	
	.decoration-circle {
		position: absolute;
		border-radius: 50%;
		background: rgba(157, 141, 241, 0.1);
	}
	
	.circle-1 {
		width: 350rpx;
		height: 350rpx;
		top: -80rpx;
		right: -80rpx;
		background: rgba(157, 141, 241, 0.08);
	}
	
	.circle-2 {
		width: 250rpx;
		height: 250rpx;
		bottom: -40rpx;
		left: -40rpx;
		background: rgba(184, 169, 232, 0.1);
	}
	
	.circle-3 {
		width: 180rpx;
		height: 180rpx;
		top: 25%;
		left: 10%;
		background: rgba(157, 141, 241, 0.06);
	}
	
	.login-content {
		width: 100%;
		max-width: 600rpx;
		background-color: #fff;
		border-radius: 32rpx;
		padding: 60rpx 50rpx 50rpx;
		box-shadow: 0 8rpx 40rpx rgba(157, 141, 241, 0.15);
		position: relative;
		z-index: 1;
	}
	
	.login-header {
		text-align: center;
		margin-bottom: 50rpx;
	}
	
	.logo-wrapper {
		position: relative;
		display: inline-block;
		margin-bottom: 28rpx;
	}
	
	.logo {
		width: 120rpx;
		height: 120rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #F8F6FF 0%, #E8E1FF 100%);
		border-radius: 50%;
		position: relative;
		z-index: 2;
		border: 2rpx solid rgba(157, 141, 241, 0.2);
	}
	
	.logo-ring {
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		width: 140rpx;
		height: 140rpx;
		border-radius: 50%;
		border: 2rpx solid rgba(157, 141, 241, 0.15);
		animation: pulse 2s ease-in-out infinite;
	}
	
	@keyframes pulse {
		0%, 100% {
			transform: translate(-50%, -50%) scale(1);
			opacity: 1;
		}
		50% {
			transform: translate(-50%, -50%) scale(1.1);
			opacity: 0.7;
		}
	}
	
	.title {
		display: block;
		font-size: 44rpx;
		font-weight: 600;
		color: #6B5B95;
		margin-bottom: 12rpx;
		letter-spacing: 1rpx;
	}
	
	.subtitle {
		display: block;
		font-size: 24rpx;
		color: #B8A9E8;
		font-weight: 400;
	}
	
	.login-form {
		.form-item {
			margin-bottom: 28rpx;
		}
		
		.input-wrapper {
			display: flex;
			align-items: center;
			background-color: #F8F6FF;
			border-radius: 16rpx;
			padding: 0 28rpx;
			height: 88rpx;
			border: 2rpx solid transparent;
			transition: all 0.3s;
			
			&:focus-within {
				border-color: #9D8DF1;
				background-color: #fff;
				box-shadow: 0 4rpx 16rpx rgba(157, 141, 241, 0.15);
			}
		}
		
		.input-wrapper uni-icons {
			margin-right: 20rpx;
		}
		
		.input {
			flex: 1;
			height: 100%;
			font-size: 28rpx;
			color: #6B5B95;
		}
		
		.identity-wrapper {
			width: 100%;
		}
		
		.identity-label {
			display: block;
			font-size: 26rpx;
			color: #6B5B95;
			margin-bottom: 16rpx;
			font-weight: 500;
		}
		
		.identity-options {
			display: flex;
			gap: 16rpx;
		}
		
		.identity-item {
			flex: 1;
			height: 72rpx;
			background-color: #F8F6FF;
			border-radius: 12rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 26rpx;
			color: #B8A9E8;
			border: 2rpx solid transparent;
			transition: all 0.3s;
			
			&.active {
				background: linear-gradient(135deg, #9D8DF1 0%, #B8A9E8 100%);
				color: #fff;
				border-color: #9D8DF1;
			}
		}
	}
	
	.login-btn {
		width: 100%;
		height: 88rpx;
		background: linear-gradient(135deg, #9D8DF1 0%, #B8A9E8 100%);
		color: #fff;
		border-radius: 16rpx;
		font-size: 32rpx;
		font-weight: 500;
		border: none;
		box-shadow: 0 8rpx 24rpx rgba(157, 141, 241, 0.3);
		transition: all 0.3s;
		margin-top: 20rpx;
		
		&:active {
			transform: translateY(2rpx);
			box-shadow: 0 4rpx 16rpx rgba(157, 141, 241, 0.25);
		}
		
		&[disabled] {
			opacity: 0.7;
		}
	}
	
	.login-footer {
		margin-top: 32rpx;
		text-align: center;
		display: flex;
		justify-content: center;
		align-items: center;
	}
	
	.footer-text {
		font-size: 24rpx;
		color: #B8A9E8;
		margin-right: 8rpx;
	}
	
	.register-text {
		font-size: 24rpx;
		color: #9D8DF1;
		font-weight: 500;
	}
</style>


