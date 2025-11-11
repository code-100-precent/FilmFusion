<template>
	<view class="profile-container">
		<!-- 用户信息卡片 -->
		<view class="user-card">
			<view class="user-info">
				<view class="avatar">
					<uni-icons v-if="!userInfo.avatar" type="contact-filled" color="#fff" size="48"></uni-icons>
					<image v-else class="avatar-image" :src="userInfo.avatar" mode="aspectFill"></image>
				</view>
				<view class="user-detail">
					<text class="username">{{ userInfo.username || '未登录' }}</text>
					<text class="user-desc">{{ userInfo.desc || '点击登录' }}</text>
				</view>
			</view>
		</view>
		
		<!-- 功能列表 -->
		<view class="menu-list">
			<view class="menu-item" @click="handleMenuClick('account')">
				<view class="menu-left">
					<uni-icons type="person" color="#9D8DF1" size="20"></uni-icons>
					<text class="menu-text">账号信息</text>
				</view>
				<uni-icons type="right" color="#D4C5F7" size="16"></uni-icons>
			</view>
			
			<view class="menu-item" @click="handleMenuClick('collect')">
				<view class="menu-left">
					<uni-icons type="heart" color="#FF6B6B" size="20"></uni-icons>
					<text class="menu-text">我的收藏</text>
				</view>
				<uni-icons type="right" color="#D4C5F7" size="16"></uni-icons>
			</view>
			
			<view class="menu-item" @click="handleMenuClick('route')">
				<view class="menu-left">
					<uni-icons type="map" color="#9D8DF1" size="20"></uni-icons>
					<text class="menu-text">路线列表</text>
				</view>
				<uni-icons type="right" color="#D4C5F7" size="16"></uni-icons>
			</view>
			
			<view class="menu-item" @click="handleMenuClick('settings')">
				<view class="menu-left">
					<uni-icons type="gear" color="#9D8DF1" size="20"></uni-icons>
					<text class="menu-text">设置</text>
				</view>
				<uni-icons type="right" color="#D4C5F7" size="16"></uni-icons>
			</view>
			
			<view class="menu-item" @click="handleMenuClick('about')">
				<view class="menu-left">
					<uni-icons type="info" color="#9D8DF1" size="20"></uni-icons>
					<text class="menu-text">关于我们</text>
				</view>
				<uni-icons type="right" color="#D4C5F7" size="16"></uni-icons>
			</view>
			
			<view class="menu-item" @click="handleMenuClick('feedback')">
				<view class="menu-left">
					<uni-icons type="chat" color="#9D8DF1" size="20"></uni-icons>
					<text class="menu-text">意见反馈</text>
				</view>
				<uni-icons type="right" color="#D4C5F7" size="16"></uni-icons>
			</view>
		</view>
		
		<!-- 退出登录 -->
		<view class="logout-section">
			<button class="logout-btn" @click="handleLogout">退出登录</button>
		</view>
		
		<!-- 底部导航栏 -->
		<TabBar :current="currentPath"></TabBar>
	</view>
</template>

<script>
	import TabBar from '../../components/TabBar/TabBar.vue'
	import { getVisitorInfo, visitorLogout } from '../../services/api.ts'
	
	export default {
		name: 'Profile',
		components: {
			TabBar
		},
		data() {
			return {
				currentPath: '/pages/profile/profile',
				userInfo: {
					username: '',
					avatar: '',
					desc: '',
					visitorId: '',
					phone: '',
					identity: ''
				},
				loading: false
			}
		},
		onLoad() {
			this.loadUserInfo()
		},
		methods: {
			async loadUserInfo() {
				const token = uni.getStorageSync('token')
				if (!token) {
					this.userInfo = {
						username: '未登录',
						avatar: '',
						desc: '点击登录',
						visitorId: '',
						phone: '',
						identity: ''
					}
					return
				}
				
				try {
					this.loading = true
					const res = await getVisitorInfo()
					if (res.code === 200 && res.data) {
						this.userInfo = {
							username: res.data.name || `访客${res.data.visitorId.slice(-4)}`,
							avatar: res.data.avatar || '',
							desc: res.data.identity || '校园访客',
							visitorId: res.data.visitorId || '',
							phone: res.data.phone || '',
							identity: res.data.identity || ''
						}
						// 更新本地存储
						uni.setStorageSync('visitorId', res.data.visitorId)
						uni.setStorageSync('identity', res.data.identity)
					}
				} catch (error) {
					console.error('加载用户信息失败:', error)
					this.userInfo = {
						username: '未登录',
						avatar: '',
						desc: '点击登录',
						visitorId: '',
						phone: '',
						identity: ''
					}
				} finally {
					this.loading = false
				}
			},
			handleMenuClick(type) {
				switch(type) {
					case 'account':
						uni.navigateTo({
							url: '/pages/account-info/account-info'
						})
						break
					case 'collect':
						uni.navigateTo({
							url: '/pages/collect-list/collect-list'
						})
						break
					case 'route':
						uni.navigateTo({
							url: '/pages/route-list/route-list'
						})
						break
					case 'settings':
						uni.navigateTo({
							url: '/pages/settings/settings'
						})
						break
					case 'about':
						uni.navigateTo({
							url: '/pages/about/about'
						})
						break
					case 'feedback':
						uni.navigateTo({
							url: '/pages/feedback/feedback'
						})
						break
				}
			},
			handleLogout() {
				uni.showModal({
					title: '提示',
					content: '确定要退出登录吗？',
					success: async (res) => {
						if (res.confirm) {
							try {
								await visitorLogout()
							} catch (error) {
								console.error('退出登录失败:', error)
							} finally {
								// 清除登录信息
								uni.removeStorageSync('token')
								uni.removeStorageSync('visitorId')
								uni.removeStorageSync('identity')
								uni.removeStorageSync('visitorInfo')
								
								// 重置用户信息
								this.userInfo = {
									username: '未登录',
									avatar: '',
									desc: '点击登录',
									visitorId: '',
									phone: '',
									identity: ''
								}
								
								uni.showToast({
									title: '已退出',
									icon: 'success'
								})
								
								// 跳转到登录页
								setTimeout(() => {
									uni.reLaunch({
										url: '/pages/login/login'
									})
								}, 1500)
							}
						}
					}
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.profile-container {
		min-height: 100vh;
		background-color: #F8F6FF;
		padding-bottom: 200rpx;
		position: relative;
	}
	
	.user-card {
		background: linear-gradient(135deg, #9D8DF1 0%, #B8A9E8 100%);
		padding: 80rpx 40rpx 60rpx;
		margin-bottom: 30rpx;
	}
	
	.user-info {
		display: flex;
		align-items: center;
	}
	
	.avatar {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
		background-color: rgba(255, 255, 255, 0.2);
		margin-right: 30rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.avatar-image {
		width: 100%;
		height: 100%;
		border-radius: 50%;
	}
	
	.user-detail {
		flex: 1;
		display: flex;
		flex-direction: column;
	}
	
	.username {
		font-size: 36rpx;
		font-weight: bold;
		color: #fff;
		margin-bottom: 10rpx;
	}
	
	.user-desc {
		font-size: 26rpx;
		color: rgba(255, 255, 255, 0.8);
	}
	
	.menu-list {
		background-color: #fff;
		margin-bottom: 30rpx;
		border-radius: 24rpx;
		overflow: hidden;
		box-shadow: 0 2rpx 16rpx rgba(157, 141, 241, 0.08);
	}
	
	.menu-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 32rpx 40rpx;
		border-bottom: 1rpx solid #F8F6FF;
		transition: background-color 0.2s;
		
		&:active {
			background-color: #F8F6FF;
		}
	}
	
	.menu-item:last-child {
		border-bottom: none;
	}
	
	.menu-left {
		display: flex;
		align-items: center;
	}
	
	.menu-left uni-icons {
		margin-right: 20rpx;
	}
	
	.menu-text {
		font-size: 30rpx;
		color: #6B5B95;
		font-weight: 500;
	}
	
	.logout-section {
		padding: 0 40rpx;
		margin-top: 60rpx;
	}
	
	.logout-btn {
		width: 100%;
		height: 88rpx;
		background-color: #fff;
		color: #ff3b30;
		border-radius: 10rpx;
		font-size: 32rpx;
		border: none;
	}
</style>

