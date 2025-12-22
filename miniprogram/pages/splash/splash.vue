<template>
	<view class="splash-screen" :class="{ 'slide-out': isSlideOut }">
		<!-- 使用本地图片，无需加载状态 -->
		<image 
			class="splash-image" 
			:src="splashImage"
			mode="aspectFill"
		></image>
	</view>
</template>

<script>
export default {
	data() {
		return {
			isSlideOut: false
		}
	},
	computed: {
		splashImage() {
			// 使用本地static目录下的图片，加载速度快
			return '/static/拍在雅安.png'
		}
	},
	onLoad() {
		// 3秒后播放淡出动画，然后跳转
		setTimeout(() => {
			this.isSlideOut = true
			// 动画结束后跳转
			setTimeout(() => {
				uni.redirectTo({
					url: '/pages/index/index'
				})
			}, 600) // 等待动画完成
		}, 3000)
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
	z-index: 9999;
	animation: fadeIn 0.5s ease-in-out;
	background-color: #000000;
}

.splash-image {
	width: 100vw;
	height: 100vh;
	object-fit: cover;
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

@keyframes slideOut {
	from {
		opacity: 1;
	}
	to {
		opacity: 0;
	}
}

.slide-out {
	animation: slideOut 0.6s ease-in-out forwards;
}
</style>