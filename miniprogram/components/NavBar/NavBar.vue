<template>
	<view class="navbar" :style="{ height: navbarHeight + 'px', paddingTop: statusBarHeight + 'px' }">
		<view class="navbar-content">
			<!-- 左侧返回按钮 -->
			<view v-if="showBack" class="navbar-left" @click="handleBack">
				<uni-icons type="left" color="#6B5B95" size="18"></uni-icons>
			</view>
			
			<!-- 标题 -->
			<view class="navbar-title">{{ title }}</view>
			
			<!-- 右侧插槽 -->
			<view class="navbar-right">
				<slot name="right"></slot>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'NavBar',
		props: {
			title: {
				type: String,
				default: ''
			},
			showBack: {
				type: Boolean,
				default: true
			}
		},
		data() {
			return {
				statusBarHeight: 0,
				navbarHeight: 36
			}
		},
		mounted() {
			// 获取系统信息
			try {
				const windowInfo = uni.getWindowInfo()
				this.statusBarHeight = windowInfo.statusBarHeight || 0
				// navbarHeight = statusBarHeight + 导航栏内容高度(36px)
				this.navbarHeight = this.statusBarHeight + 36
			} catch (e) {
				// 兼容旧版本
				const systemInfo = uni.getSystemInfoSync()
				this.statusBarHeight = systemInfo.statusBarHeight || 0
				this.navbarHeight = this.statusBarHeight + 36
			}
		},
		methods: {
			handleBack() {
				// 检查是否可以返回
				const pages = getCurrentPages()
				if (pages.length > 1) {
					uni.navigateBack()
				} else {
					// 如果没有上一页，跳转到首页
					uni.reLaunch({
						url: '/pages/index/index'
					})
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.navbar {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		z-index: 999;
		background-color: transparent;
		backdrop-filter: blur(10rpx);
	}
	
	.navbar-content {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 36px;
		padding: 0 30rpx;
		box-sizing: border-box;
	}
	
	.navbar-left {
		width: 60rpx;
		height: 36px;
		display: flex;
		align-items: center;
		justify-content: center;
		cursor: pointer;
	}
	
	
	.navbar-title {
		flex: 1;
		text-align: center;
		font-size: 28rpx;
		font-weight: 600;
		color: #6B5B95;
	}
	
	.navbar-right {
		width: 60rpx;
		height: 36px;
		display: flex;
		align-items: center;
		justify-content: center;
	}
</style>

