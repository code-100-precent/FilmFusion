<template>
	<view class="error-container">
		<view class="error-content">
			<text class="error-code">404</text>
			<text class="error-text">页面不存在</text>
			<text class="error-desc">抱歉，您访问的页面不存在</text>
			<button class="back-btn" @click="handleBack">返回首页</button>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'Error404',
		methods: {
			handleBack() {
				uni.reLaunch({
					url: '/pages/index/index'
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.error-container {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #f5f5f5;
		padding: 40rpx;
	}
	
	.error-content {
		text-align: center;
	}
	
	.error-code {
		display: block;
		font-size: 120rpx;
		font-weight: bold;
		color: #ddd;
		margin-bottom: 20rpx;
	}
	
	.error-text {
		display: block;
		font-size: 40rpx;
		font-weight: 500;
		color: #333;
		margin-bottom: 20rpx;
	}
	
	.error-desc {
		display: block;
		font-size: 28rpx;
		color: #999;
		margin-bottom: 60rpx;
	}
	
	.back-btn {
		width: 300rpx;
		height: 80rpx;
		background-color: #007aff;
		color: #fff;
		border-radius: 10rpx;
		font-size: 28rpx;
		border: none;
	}
</style>

