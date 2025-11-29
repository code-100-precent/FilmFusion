<template>
	<view class="loading-container">
		<view class="dots-container">
			<view class="dot dot1"></view>
			<view class="dot dot2"></view>
			<view class="dot dot3"></view>
		</view>
		<text class="loading-text">{{ text }}</text>
	</view>
</template>

<script>
	export default {
		name: 'Loading',
		props: {
			text: {
				type: String,
				default: '正在加载中'
			}
		}
	}
</script>

<style lang="scss" scoped>
	.loading-container {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 20rpx;
	}
	
	.dots-container {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 16rpx;
	}
	
	.dot {
		width: 16rpx;
		height: 16rpx;
		border-radius: 50%;
		background-color: #6366f1; /* 使用主题色 */
		margin: 0 6rpx;
		animation: bounce 1.4s infinite ease-in-out both;
	}
	
	.dot1 {
		animation-delay: -0.32s;
	}
	
	.dot2 {
		animation-delay: -0.16s;
	}
	
	@keyframes bounce {
		0%, 80%, 100% { 
			transform: scale(0);
			opacity: 0.5;
		}
		40% { 
			transform: scale(1);
			opacity: 1;
		}
	}
	
	.loading-text {
		font-size: 26rpx;
		color: #9ca3af;
		letter-spacing: 2rpx;
	}
</style>
