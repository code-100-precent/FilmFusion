<template>
  <view
		class="navbar"
		:style="{
			paddingTop: (statusBarHeight / 2) + 'px'
		}"
	>
		<view class="navbar__content">
			<view v-if="showBack" class="navbar__left" @click="handleBack">
				<uni-icons type="left" color="#1f2937" size="18" />
			</view>
			<view class="navbar__title">
				<slot>{{ title }}</slot>
			</view>
			<view class="navbar__right">
				<slot name="right" />
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
    },
    background: {
      type: String,
      default: 'rgba(255, 255, 255, 0.92)'
    },
    shadow: {
				type: Boolean,
				default: true
			}
		},
		data() {
			return {
				isScrolled: false,
				statusBarHeight: 0,
				navbarHeight: 0
			}
		},
		mounted() {
			// 获取系统信息
			const systemInfo = uni.getSystemInfoSync()
			// 状态栏高度，单位转换为rpx（1px = 2rpx）
			this.statusBarHeight = (systemInfo.statusBarHeight || 22) * 2
			this.navbarHeight = this.statusBarHeight + 88
		},
		methods: {
			handleBack() {
      this.$emit('back')
				const pages = getCurrentPages()
				if (pages.length > 1) {
					uni.navigateBack()
				} else {
        uni.reLaunch({ url: '/pages/scenes/scenes' })
				}
			},
			updateScrollState(scrollTop) {
				this.isScrolled = (scrollTop || 0) > 20
			}
		},
		created() {
			// 使用全局事件总线监听滚动
			if (typeof uni !== 'undefined' && uni.$on) {
				uni.$on('pageScroll', this.updateScrollState)
			}
		},
		beforeDestroy() {
			if (typeof uni !== 'undefined' && uni.$off) {
				uni.$off('pageScroll', this.updateScrollState)
			}
		}
	}
</script>

<style scoped lang="scss">
	.navbar {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		z-index: 998;
		padding: 0;
		margin: 0;
	}

	.navbar__content {
		display: flex;
		align-items: center;
		height: 88rpx;
		padding: 0 32rpx 0 20rpx;
		box-sizing: border-box;
	}
	
	.navbar__left {
		width: 60rpx;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 50%;
		transition: all 0.3s;
		margin-right: 24rpx;
	}

	.navbar__left:active {
		background: rgba(0, 0, 0, 0.05);
	}
	
	.navbar__title {
		flex: 1;
		text-align: left;
		font-size: 20rpx;
		font-weight: 600;
		color: #1f2937;
		letter-spacing: 0.5rpx;
	}
	
	.navbar__right {
		width: 80rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}
</style>

