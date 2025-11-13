<template>
	<view class="empty-container">
		<view v-if="image || showImage" class="empty-icon-wrapper">
			<uni-icons :type="image || 'info'" color="#ccc" size="80"></uni-icons>
		</view>
		<text class="empty-text">{{ text || '暂无数据' }}</text>
		<view v-if="showButton" class="empty-button">
			<slot name="button">
				<button class="btn" @click="handleClick">{{ buttonText || '重新加载' }}</button>
			</slot>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'Empty',
		props: {
			text: {
				type: String,
				default: '暂无数据'
			},
			image: {
				type: String,
				default: ''
			},
			showImage: {
				type: Boolean,
				default: true
			},
			showButton: {
				type: Boolean,
				default: false
			},
			buttonText: {
				type: String,
				default: '重新加载'
			}
		},
		methods: {
			handleClick() {
				this.$emit('click')
			}
		}
	}
</script>

<style lang="scss" scoped>
	.empty-container {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 40rpx;
		min-height: 400rpx;
	}
	
	.empty-icon-wrapper {
		margin-bottom: 40rpx;
		opacity: 0.6;
	}
	
	.empty-text {
		font-size: 28rpx;
		color: #999;
		margin-bottom: 40rpx;
	}
	
	.empty-button {
		width: 100%;
		display: flex;
		justify-content: center;
	}
	
	.btn {
		padding: 20rpx 60rpx;
		background-color: #007aff;
		color: #fff;
		border-radius: 10rpx;
		font-size: 28rpx;
		border: none;
	}
</style>
