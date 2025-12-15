<template>
	<view v-if="show" class="splash-screen" :style="splashStyle">
		<view class="splash-content">
			<image 
				class="splash-logo" 
				src="/static/splash-logo.png"
				mode="aspectFit"
			></image>
			<text class="splash-text">雅安影视服务一站式平台</text>
		</view>
	</view>
</template>

<script>
export default {
	name: 'SplashScreen',
	props: {
		show: {
			type: Boolean,
			default: true
		},
		duration: {
			type: Number,
			default: 3000
		}
	},
	computed: {
		splashStyle() {
			return {
				background: 'linear-gradient(to top, #ffffff 0%, #20b2aa 100%)'
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.splash-screen {
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	background: linear-gradient(to top, #ffffff 0%, #20b2aa 100%);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	z-index: 9999;
	animation: fadeIn 0.5s ease-in-out;
}

.splash-content {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 40rpx;
	animation: slideUp 1s ease-out 0.2s both;
}

.splash-logo {
	width: 200rpx;
	height: 200rpx;
	animation: bounce 2s ease-in-out infinite;
	border-radius: 24rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.splash-text {
	font-size: 36rpx;
	font-weight: 600;
	color: #1f2937;
	text-align: center;
	animation: fadeIn 1s ease-in-out 1.2s both;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.05);
}

/* 动画关键帧 */
@keyframes fadeIn {
	from {
		opacity: 0;
	}
	to {
		opacity: 1;
	}
}

@keyframes fadeOut {
	from {
		opacity: 1;
	}
	to {
		opacity: 0;
	}
}

@keyframes slideUp {
	from {
		transform: translateY(60rpx);
		opacity: 0;
	}
	to {
		transform: translateY(0);
		opacity: 1;
	}
}

@keyframes bounce {
	0%, 20%, 53%, 80%, 100% {
		transform: translate3d(0, 0, 0);
	}
	40%, 43% {
		transform: translate3d(0, -20rpx, 0);
	}
	70% {
		transform: translate3d(0, -10rpx, 0);
	}
	90% {
		transform: translate3d(0, -4rpx, 0);
	}
}
</style>