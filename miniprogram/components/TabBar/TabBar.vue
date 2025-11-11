<template>
	<view class="tabbar">
		<view 
			v-for="(item, index) in tabList" 
			:key="index"
			class="tabbar-item"
			:class="{ active: active === item.key }"
			@click="switchTab(item)"
		>
			<view class="icon-wrap" :class="{ bounce: active === item.key }">
				<uni-icons :type="item.icon" :color="active === item.key ? '#5b75ff' : '#B8A9E8'" size="22"></uni-icons>
			</view>
			<text class="tabbar-text">{{ item.text }}</text>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'TabBar',
		props: {
			active: { type: String, default: 'scenes' }
		},
		emits: ['change'],
		data() {
			return {
				tabList: [
					{ key: 'scenes', path: '/pages/scenes/scenes', text: '场景', icon: 'home' },
					{ key: 'services', path: '/pages/services/services', text: '协拍', icon: 'map' },
					{ key: 'news', path: '/pages/news/news', text: '资讯', icon: 'chat' },
					{ key: 'filing', path: '/pages/filing/filing', text: '报备', icon: 'paperplane' }
				]
			}
		},
		methods: {
			switchTab(item) {
				if (this.active === item.key) return
				this.$emit('change', item.key)
				// custom tabbar uses reLaunch to mimic native tabbar navigation
				uni.reLaunch({ url: item.path })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.tabbar {
		position: fixed;
		bottom: 0rpx;
		left: 8rpx;
		right: 8rpx;
		height: 68rpx;
		background: rgba(255,255,255,0.85);
		backdrop-filter: blur(12rpx);
		border: 0.2rpx solid rgba(239, 241, 249, 0.9);
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: space-around;
		z-index: 999;
		padding: 0 2rpx;
		padding-bottom: env(safe-area-inset-bottom);
		box-shadow: 0 8rpx 28rpx rgba(20, 20, 40, 0.08);
	}
	
	.tabbar-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 10rpx 0;
		position: relative;
	}
	.icon-wrap { transition: transform .25s ease; }
	.icon-wrap.bounce { transform: translateY(-6rpx) scale(1.08); }
	
	.tabbar-text {
		font-size: 18rpx;
		color: #8A8FA0;
		margin-top: 4rpx;
	}
	
	.tabbar-item.active .tabbar-text { color: #4F46E5; font-weight: 700; }
	.tabbar-item.active::after {
		content:'';
		position: absolute;
		bottom: 8rpx;
		width: 36rpx;
		height: 6rpx;
		border-radius: 999rpx;
		background: linear-gradient(90deg,#5b75ff,#8f9bff);
	}
</style>
