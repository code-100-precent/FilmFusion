<template>
	<view v-if="show" class="loading-mask">
		<view class="loading-content">
			<view class="loading-spinner"></view>
			<text v-if="text" class="loading-text">{{ text }}</text>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'Loading',
		props: {
			show: {
				type: Boolean,
				default: false
			},
			text: {
				type: String,
				default: '加载中...'
			}
		}
	}
</script>

<style lang="scss" scoped>
	.loading-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 9999;
	}
	
	.loading-content {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		background-color: #fff;
		border-radius: 20rpx;
		padding: 60rpx 40rpx;
	}
	
	.loading-spinner {
		width: 60rpx;
		height: 60rpx;
		border: 6rpx solid #f3f3f3;
		border-top: 6rpx solid #007aff;
		border-radius: 50%;
		animation: spin 1s linear infinite;
	}
	
	@keyframes spin {
		0% { transform: rotate(0deg); }
		100% { transform: rotate(360deg); }
	}
	
	.loading-text {
		margin-top: 30rpx;
		font-size: 28rpx;
		color: #666;
	}
</style>
