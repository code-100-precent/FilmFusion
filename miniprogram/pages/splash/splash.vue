<template>
	<view class="splash-screen" :class="{ 'slide-out': isSlideOut }">
		<image 
			class="splash-image" 
			:src="splashImage"
		></image>
	</view>
</template>

<script>
import { getFileUrl } from '../../utils'

export default {
	data() {
		return {
			isSlideOut: false
		}
	},
	computed: {
		splashImage() {
			return getFileUrl('files/origin/1765768530788_compressed-图片文字修改_compressed.jpg')
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