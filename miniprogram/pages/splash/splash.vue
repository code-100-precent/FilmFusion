<template>
	<view class="splash-screen" :class="{ 'slide-out': isSlideOut }">
		<image 
			class="splash-image" 
			src="https://xy-work.oss-cn-beijing.aliyuncs.com/uploads/%E5%9B%BE%E7%89%87%E6%96%87%E5%AD%97%E4%BF%AE%E6%94%B9.png"
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