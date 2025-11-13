<template>
	<view class="index-container">
		<!-- 导航栏 -->
		<NavBar title="首页" :show-back="false"></NavBar>
		
		<view class="content" :style="{ paddingTop: navbarHeight + 'px' }">
			<!-- Banner 区域 -->
			<view class="banner-section">
				<swiper class="banner-swiper" :indicator-dots="true" :autoplay="true" :interval="3000" :duration="500">
					<swiper-item v-for="(item, index) in bannerList" :key="index">
						<view class="banner-image" :style="{ background: item.background }">
							<text class="banner-text">{{ item.text }}</text>
						</view>
					</swiper-item>
				</swiper>
			</view>
			
			<!-- 功能入口 -->
			<view class="function-section">
				<view class="section-title">功能入口</view>
				<view class="function-grid">
					<view 
						v-for="(item, index) in functionList" 
						:key="index"
						class="function-item"
						@click="handleFunctionClick(item)"
					>
						<view class="function-icon" :style="{ backgroundColor: item.bgColor }">
							<uni-icons :type="item.icon" :color="item.color" size="24"></uni-icons>
						</view>
						<text class="function-text">{{ item.text }}</text>
					</view>
				</view>
			</view>
			
			<!-- 内容列表 -->
			<view class="list-section">
				<view class="section-header">
					<text class="section-title">推荐路线</text>
					<text class="section-more" @click="goToRouteList">查看全部</text>
				</view>
				<view class="route-cards">
					<view 
						class="route-card"
						v-for="(route, index) in routeList" 
						:key="index"
						@click="goToRouteDetail(route)"
					>
						<view class="route-card-header">
							<text class="route-name">{{ route.routeName }}</text>
							<view class="route-badge">{{ route.suitableIdentity }}</view>
						</view>
						<text class="route-desc">{{ route.routeDesc }}</text>
						<view class="route-card-footer">
							<text class="route-duration">{{ route.duration }}分钟</text>
							<text class="route-count">{{ route.poisCount }}个地点</text>
						</view>
					</view>
				</view>
			</view>
			
			<view class="list-section">
				<view class="section-header">
					<text class="section-title">校友故事</text>
					<text class="section-more" @click="goToStoryList">查看全部</text>
				</view>
				<view v-if="storyList.length === 0" class="empty-wrapper">
					<Empty text="暂无故事"></Empty>
				</view>
				<view 
					v-else 
					class="story-item" 
					v-for="(story, index) in storyList" 
					:key="story.storyId"
					@click="goToStoryDetail(story.storyId)"
				>
					<text class="story-title">{{ story.title }}</text>
					<text class="story-summary">{{ story.content.substring(0, 80) }}...</text>
					<view class="story-tags">
						<text 
							v-for="(tag, tagIndex) in story.emotionTags" 
							:key="tagIndex"
							class="story-tag"
						>
							{{ tag.tagName }}
						</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 底部导航栏 -->
		<TabBar :current="currentPath"></TabBar>
	</view>
</template>

<script>
	import NavBar from '../../components/NavBar/NavBar.vue'
	import TabBar from '../../components/TabBar/TabBar.vue'
	import Empty from '../../components/Empty/Empty.vue'
	import { getRouteList, getStoryList } from '../../services/api.ts'
	
	export default {
		components: {
			NavBar,
			TabBar,
			Empty
		},
		data() {
			return {
				navbarHeight: 0,
				currentPath: '/pages/index/index',
				bannerList: [
					{ text: '欢迎使用校园导览', background: 'linear-gradient(135deg, #9D8DF1 0%, #B8A9E8 100%)' },
					{ text: '发现更多精彩', background: 'linear-gradient(135deg, #E8E1FF 0%, #D4C5F7 100%)' },
					{ text: '探索校园生活', background: 'linear-gradient(135deg, #B8A9E8 0%, #9D8DF1 100%)' }
				],
				functionList: [
					{ icon: 'home', text: '首页', color: '#9D8DF1', bgColor: '#F8F6FF', path: '/pages/index/index' },
					{ icon: 'map', text: '地图', color: '#9D8DF1', bgColor: '#F8F6FF', path: '/pages/map/map' },
					{ icon: 'contact', text: '个人', color: '#9D8DF1', bgColor: '#F8F6FF', path: '/pages/profile/profile' },
					{ icon: 'gear', text: '设置', color: '#9D8DF1', bgColor: '#F8F6FF', path: '/pages/settings/settings' }
				],
				routeList: [],
				storyList: []
			}
		},
		onLoad() {
			this.calculateNavbarHeight()
			this.loadData()
		},
		methods: {
			calculateNavbarHeight() {
				const systemInfo = uni.getSystemInfoSync()
				this.navbarHeight = (systemInfo.statusBarHeight || 0) + 44
			},
			async loadData() {
				try {
					console.log('开始加载首页数据...')
					const [routeRes, storyRes] = await Promise.all([
						getRouteList(),
						getStoryList()
					])
					console.log('路线数据:', routeRes)
					console.log('故事数据:', storyRes)
					this.routeList = routeRes.data.slice(0, 2)
					this.storyList = storyRes.data.slice(0, 3)
				} catch (error) {
					console.error('加载数据失败', error)
					uni.showToast({
						title: error.message || '加载数据失败',
						icon: 'none'
					})
				}
			},
			handleFunctionClick(item) {
				if (!item.path) {
					return
				}
				
				if (item.path === '/pages/index/index' || item.path === '/pages/profile/profile' || item.path === '/pages/map/map') {
					uni.reLaunch({
						url: item.path
					})
				} else {
					uni.navigateTo({
						url: item.path
					})
				}
			},
			goToRouteList() {
				uni.navigateTo({
					url: '/pages/route-list/route-list'
				})
			},
			
			goToRouteDetail(route) {
				uni.navigateTo({
					url: `/pages/route/route?routeId=${route.routeId}`
				})
			},
			
			goToStoryList() {
				uni.navigateTo({
					url: '/pages/story-list/story-list'
				})
			},
			
			goToStoryDetail(storyId) {
				uni.navigateTo({
					url: `/pages/story-detail/story-detail?storyId=${storyId}`
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.index-container {
		min-height: 100vh;
		background-color: #F8F6FF;
		padding-bottom: 200rpx;
	}
	
	.content {
		padding-bottom: 200rpx;
	}
	
	.banner-section {
		width: 100%;
		height: 400rpx;
		margin-bottom: 30rpx;
	}
	
	.banner-swiper {
		width: 100%;
		height: 100%;
	}
	
	.banner-image {
		width: 100%;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.banner-text {
		color: #fff;
		font-size: 36rpx;
		font-weight: bold;
	}
	
	.function-section {
		background-color: #fff;
		padding: 40rpx;
		margin-bottom: 30rpx;
		border-radius: 24rpx;
		margin-left: 30rpx;
		margin-right: 30rpx;
		box-shadow: 0 2rpx 16rpx rgba(157, 141, 241, 0.08);
	}
	
	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 24rpx;
	}
	
	.section-title {
		font-size: 32rpx;
		font-weight: 600;
		color: #6B5B95;
		letter-spacing: 1rpx;
	}
	
	.section-more {
		font-size: 24rpx;
		color: #9D8DF1;
	}
	
	.route-cards {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
	
	.route-card {
		background: linear-gradient(135deg, #F8F6FF 0%, #E8E1FF 100%);
		border-radius: 20rpx;
		padding: 32rpx;
		border: 2rpx solid rgba(157, 141, 241, 0.2);
	}
	
	.route-card-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 16rpx;
	}
	
	.route-name {
		font-size: 30rpx;
		font-weight: 600;
		color: #6B5B95;
		flex: 1;
	}
	
	.route-badge {
		padding: 8rpx 16rpx;
		background: linear-gradient(135deg, #9D8DF1 0%, #B8A9E8 100%);
		color: #fff;
		font-size: 22rpx;
		border-radius: 12rpx;
	}
	
	.route-desc {
		display: block;
		font-size: 26rpx;
		color: #6B5B95;
		line-height: 1.6;
		margin-bottom: 20rpx;
	}
	
	.route-card-footer {
		display: flex;
		gap: 24rpx;
	}
	
	.route-duration,
	.route-count {
		font-size: 24rpx;
		color: #B8A9E8;
	}
	
	.story-item {
		padding: 24rpx;
		background-color: #fff;
		border-radius: 16rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 2rpx 12rpx rgba(157, 141, 241, 0.08);
	}
	
	.story-title {
		display: block;
		font-size: 28rpx;
		font-weight: 600;
		color: #6B5B95;
		margin-bottom: 12rpx;
	}
	
	.story-summary {
		display: block;
		font-size: 24rpx;
		color: #B8A9E8;
		line-height: 1.6;
		margin-bottom: 16rpx;
	}
	
	.story-tags {
		display: flex;
		gap: 12rpx;
		flex-wrap: wrap;
	}
	
	.story-tag {
		padding: 6rpx 16rpx;
		background-color: rgba(157, 141, 241, 0.1);
		color: #9D8DF1;
		font-size: 22rpx;
		border-radius: 12rpx;
	}
	
	.function-grid {
		display: flex;
		flex-wrap: wrap;
		justify-content: space-between;
	}
	
	.function-item {
		width: 25%;
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 30rpx;
	}
	
	.function-icon {
		width: 100rpx;
		height: 100rpx;
		border-radius: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 20rpx;
	}
	
	.function-text {
		font-size: 24rpx;
		color: #6B5B95;
	}
	
	.list-section {
		background-color: #fff;
		padding: 40rpx;
		border-radius: 24rpx;
		margin-left: 30rpx;
		margin-right: 30rpx;
		box-shadow: 0 2rpx 16rpx rgba(157, 141, 241, 0.08);
	}
	
	.empty-wrapper {
		padding: 100rpx 0;
	}
	
	.list-item {
		padding: 30rpx 0;
		border-bottom: 1rpx solid #F8F6FF;
	}
	
	.list-item:last-child {
		border-bottom: none;
	}
	
	.list-title {
		display: block;
		font-size: 30rpx;
		font-weight: 500;
		color: #6B5B95;
		margin-bottom: 10rpx;
	}
	
	.list-desc {
		display: block;
		font-size: 26rpx;
		color: #B8A9E8;
	}
</style>
