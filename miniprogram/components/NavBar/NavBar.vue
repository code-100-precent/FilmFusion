<template>
  <view
    class="navbar"
    :class="{ 
      'navbar--shadow': shadow,
      'navbar--scrolled': isScrolled
    }"
    :style="{
      background: background
    }"
  >
    <view class="navbar__status-bar"></view>
    <view class="navbar__content">
      <view class="navbar__curve navbar__curve--top"></view>
      <view v-if="showBack" class="navbar__left" @click="handleBack">
        <uni-icons type="left" color="#4F46E5" size="20" />
      </view>
      <view class="navbar__title">
        <slot>{{ title }}</slot>
			</view>
      <view class="navbar__right">
        <slot name="right" />
			</view>
      <view class="navbar__curve navbar__curve--bottom"></view>
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
      isScrolled: false
			}
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
		backdrop-filter: blur(20rpx) saturate(180%);
		-webkit-backdrop-filter: blur(20rpx) saturate(180%);
		box-sizing: border-box;
		transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
		overflow: hidden;
		margin: 0;
		/* 总高度 = 状态栏高度 + 导航栏内容高度 */
		padding-top: env(safe-area-inset-top, 44rpx);
	}

	.navbar__status-bar {
		height: env(safe-area-inset-top, 44rpx);
		background: transparent;
	}

	.navbar--shadow {
		box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
	}

	.navbar--scrolled {
		box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.12);
	}

	.navbar__content {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 88rpx; /* 固定导航栏内容高度 */
		padding: 0 40rpx;
		box-sizing: border-box;
		position: relative;
		margin: 0;
	}
	
	.navbar__curve {
		position: absolute;
		left: 0;
		right: 0;
		height: 24rpx;
		pointer-events: none;
		opacity: 0;
		transition: opacity 0.4s cubic-bezier(0.4, 0, 0.2, 1);
	}

	.navbar__curve--top {
		top: -24rpx;
		background: radial-gradient(ellipse at center, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
	}

	.navbar__curve--bottom {
		bottom: -24rpx;
		background: radial-gradient(ellipse at center, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
	}

	.navbar--scrolled .navbar__curve {
		opacity: 1;
	}

	.navbar__left,
	.navbar__right {
		width: 80rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
		z-index: 2;
	}
	
	.navbar__left {
		height: 80rpx;
		border-radius: 20rpx;
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	}

	.navbar__left:active {
		background: rgba(99, 102, 241, 0.1);
		transform: scale(0.95);
	}
	
	.navbar__title {
		flex: 1;
		text-align: center;
		font-size: 34rpx;
		font-weight: 700;
		color: #1f2937;
		letter-spacing: 0.5rpx;
		background: linear-gradient(135deg, #1f2937 0%, #4b5563 100%);
		-webkit-background-clip: text;
		-webkit-text-fill-color: transparent;
		background-clip: text;
		position: relative;
		z-index: 2;
	}
</style>

