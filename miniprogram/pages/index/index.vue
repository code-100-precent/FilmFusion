<template>
	<view class="index-container">
		<!-- 导航栏 -->
		<NavBar title="雅安影视服务" :show-back="false" class="index-navbar"></NavBar>
			<view class="content">
				<!-- Banner 轮播图区域 -->
				<view class="banner-section" :class="{ 'banner-section--scrolled': scrollTop > 50 }">
					<swiper 
						class="banner-swiper" 
						:indicator-dots="true" 
						:autoplay="true" 
						:interval="4000" 
						:duration="600"
						:circular="true"
						indicator-color="rgba(255,255,255,0.4)"
						indicator-active-color="#fff"
					>
						<swiper-item v-for="(item, index) in bannerList" :key="index">
							<view class="banner-item" :style="{ background: item.background }">
								<view class="banner-content" :class="`banner-content--${index}`">
									<text class="banner-title">{{ item.title }}</text>
									<text class="banner-subtitle">{{ item.subtitle }}</text>
									<view class="banner-badge" v-if="item.badge">{{ item.badge }}</view>
								</view>
								<view class="banner-decoration"></view>
							</view>
						</swiper-item>
					</swiper>
				</view>
				
				<!-- 功能入口 -->
				<view class="function-section" :class="{ 'fade-in': !loading }">
					<view class="section-title-wrapper">
						<text class="section-title">快速入口</text>
						<text class="section-subtitle">一站式影视服务</text>
					</view>
					<view class="function-grid">
						<view 
							v-for="(item, index) in functionList" 
							:key="index"
							class="function-item"
							:style="{ animationDelay: `${index * 0.1}s` }"
							@click="handleFunctionClick(item)"
						>
							<view class="function-icon-wrapper">
								<view class="function-icon" :style="{ backgroundColor: item.bgColor }">
									<uni-icons :type="item.icon" :color="item.color" size="28"></uni-icons>
								</view>
								<view class="function-icon-glow" :style="{ background: item.bgColor }"></view>
							</view>
							<text class="function-text">{{ item.text }}</text>
						</view>
					</view>
				</view>
				
				<!-- 推荐路线 -->
				<view class="list-section" :class="{ 'fade-in': !loading }">
					<view class="section-header">
						<view class="section-title-wrapper">
							<text class="section-title">推荐路线</text>
							<text class="section-desc">精选拍摄路线推荐</text>
						</view>
						<text v-if="routeList.length > 0" class="section-more" @click="goToRouteList">
							查看全部
							<uni-icons type="right" size="14" color="#6366f1"></uni-icons>
						</text>
					</view>
					<view v-if="loading" class="loading-wrapper">
						<Loading />
					</view>
					<view v-else-if="routeList.length === 0" class="empty-wrapper">
						<Empty text="暂无推荐路线"></Empty>
					</view>
					<view v-else class="route-cards">
						<view 
							class="route-card"
							v-for="(route, index) in routeList" 
							:key="route.routeId || index"
							:style="{ animationDelay: `${index * 0.15}s` }"
							@click="goToRouteDetail(route)"
						>
							<view class="route-card-header">
								<text class="route-name">{{ route.routeName }}</text>
								<view class="route-badge">{{ route.suitableIdentity || '通用' }}</view>
							</view>
							<text class="route-desc">{{ route.routeDesc || '暂无描述' }}</text>
							<view class="route-card-footer">
								<view class="route-meta-item">
									<uni-icons type="clock" size="14" color="#6366f1"></uni-icons>
									<text class="route-duration">{{ route.duration || 0 }}分钟</text>
								</view>
								<view class="route-meta-item">
									<uni-icons type="location" size="14" color="#8b5cf6"></uni-icons>
									<text class="route-count">{{ route.poisCount || 0 }}个地点</text>
								</view>
							</view>
						</view>
					</view>
				</view>
				
				<!-- 拍摄故事 -->
				<view class="list-section" :class="{ 'fade-in': !loading }">
					<view class="section-header">
						<view class="section-title-wrapper">
							<text class="section-title">拍摄故事</text>
							<text class="section-desc">真实拍摄体验分享</text>
						</view>
						<text v-if="storyList.length > 0" class="section-more" @click="goToStoryList">
							查看全部
							<uni-icons type="right" size="14" color="#6366f1"></uni-icons>
						</text>
					</view>
					<view v-if="loading" class="loading-wrapper">
						<Loading />
					</view>
					<view v-else-if="storyList.length === 0" class="empty-wrapper">
						<Empty text="暂无拍摄故事"></Empty>
					</view>
					<view v-else class="story-list">
						<view 
							class="story-item" 
							v-for="(story, index) in storyList" 
							:key="story.storyId || index"
							:style="{ animationDelay: `${index * 0.1}s` }"
							@click="goToStoryDetail(story.storyId)"
						>
							<view class="story-content">
								<text class="story-title">{{ story.title }}</text>
								<text class="story-summary">{{ (story.content || '').substring(0, 80) }}{{ story.content && story.content.length > 80 ? '...' : '' }}</text>
								<view v-if="story.emotionTags && story.emotionTags.length > 0" class="story-tags">
									<text 
										v-for="(tag, tagIndex) in story.emotionTags" 
										:key="tagIndex"
										class="story-tag"
									>
										{{ tag.tagName }}
									</text>
								</view>
							</view>
							<view class="story-arrow">
								<uni-icons type="right" size="16" color="#9ca3af"></uni-icons>
							</view>
						</view>
					</view>
				</view>
				
				<!-- 底部间距 -->
				<view class="bottom-spacer"></view>
			</view>
		
		<!-- 底部导航栏 -->
		<TabBar :current="currentPath"></TabBar>
	</view>
</template>

<script>
	import NavBar from '../../components/NavBar/NavBar.vue'
	import TabBar from '../../components/TabBar/TabBar.vue'
	import Empty from '../../components/Empty/Empty.vue'
	import Loading from '../../components/Loading/Loading.vue'
	import { getRouteList, getStoryList } from '../../services/api.ts'
	
	export default {
		components: {
			NavBar,
			TabBar,
			Empty,
			Loading
		},
		data() {
			return {
				navbarHeight: 0,
				currentPath: '/pages/index/index',
				loading: false,
				scrollTop: 0,
				bannerList: [
					{ 
						title: '雅安影视取景服务', 
						subtitle: '专业影视拍摄一站式服务平台',
						badge: '官方认证',
						background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' 
					},
					{ 
						title: '发现更多精彩取景点', 
						subtitle: '探索雅安最美拍摄场景',
						badge: '热门推荐',
						background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' 
					},
					{ 
						title: '探索雅安影视资源', 
						subtitle: '文化、自然、建筑全方位覆盖',
						badge: '资源丰富',
						background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' 
					}
				],
				functionList: [
					{ icon: 'location', text: '取景手册', color: '#6366f1', bgColor: '#eef2ff', path: '/pages/scenes/scenes' },
					{ icon: 'calendar', text: '在线报备', color: '#8b5cf6', bgColor: '#f3e8ff', path: '/pages/filing/filing' },
					{ icon: 'phone', text: '协拍服务', color: '#ec4899', bgColor: '#fce7f3', path: '/pages/services/services' },
					{ icon: 'chatbubble', text: '影视资讯', color: '#06b6d4', bgColor: '#cffafe', path: '/pages/news/news' }
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
				this.navbarHeight = (systemInfo.statusBarHeight || 0) + 88
			},
			onScroll(e) {
				if (e && e.detail) {
					this.scrollTop = e.detail.scrollTop || 0
					// 通知NavBar滚动状态
					if (typeof uni !== 'undefined' && uni.$emit) {
						uni.$emit('pageScroll', this.scrollTop)
					}
				}
			},
			async loadData() {
				this.loading = true
				try {
					const [routeRes, storyRes] = await Promise.all([
						getRouteList(),
						getStoryList()
					])
					this.routeList = (routeRes.data || []).slice(0, 2)
					this.storyList = (storyRes.data || []).slice(0, 3)
				} catch (error) {
					console.error('加载数据失败', error)
					uni.showToast({
						title: (error && error.message) || '加载数据失败，请稍后重试',
						icon: 'none',
						duration: 2000
					})
				} finally {
					this.loading = false
				}
			},
			handleFunctionClick(item) {
				if (!item.path) {
					return
				}
				
				if (item.path === '/pages/index/index' || item.path === '/pages/profile/profile') {
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
			
			goToStoryDetail(storyId: number) {
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
		background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
		position: relative;
		overflow: hidden;
	}
	
	.index-navbar {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		z-index: 1000;
	}
	
	.content-scroll {
		height: 100vh;
		box-sizing: border-box;
	}
	
	.content {
		padding: 0 16rpx;
		padding-bottom: calc(96rpx + env(safe-area-inset-bottom));
	}
	
	/* Banner 轮播图 */
	.banner-section {
		width: calc(100% - 64rpx);
		height: 420rpx;
		margin: 32rpx auto 40rpx;
		border-radius: 36rpx;
		overflow: hidden;
		box-shadow: 0 24rpx 72rpx rgba(0, 0, 0, 0.15);
		transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.4s ease;
		position: relative;
	}
	
	
	.banner-section--scrolled {
		transform: scale(0.98);
		box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.1);
	}
	
	.banner-swiper {
		width: 100%;
		height: 100%;
		border-radius: 32rpx;
		overflow: hidden;
	}
	
	.banner-item {
		width: 100%;
		height: 100%;
		position: relative;
		overflow: hidden;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.banner-decoration {
		position: absolute;
		top: -50%;
		right: -20%;
		width: 600rpx;
		height: 600rpx;
		background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, transparent 70%);
		border-radius: 50%;
		animation: float 6s ease-in-out infinite;
	}
	
	.banner-content {
		position: relative;
		z-index: 2;
		text-align: center;
		padding: 0 60rpx;
		animation: fadeInUp 0.8s ease-out;
	}
	
	.banner-title {
		display: block;
		color: #fff;
		font-size: 48rpx;
		font-weight: 700;
		margin-bottom: 16rpx;
		text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
		letter-spacing: 2rpx;
	}
	
	.banner-subtitle {
		display: block;
		color: rgba(255, 255, 255, 0.9);
		font-size: 28rpx;
		margin-bottom: 24rpx;
		text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
	}
	
	.banner-badge {
		display: inline-block;
		padding: 12rpx 24rpx;
		background: rgba(255, 255, 255, 0.25);
		backdrop-filter: blur(10rpx);
		border-radius: 999rpx;
		color: #fff;
		font-size: 24rpx;
		border: 1rpx solid rgba(255, 255, 255, 0.3);
	}
	
	/* 功能入口 */
	.function-section {
		background: #fff;
		padding: 48rpx 40rpx;
		margin-bottom: 32rpx;
		border-radius: 32rpx;
		box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
		opacity: 0;
		transform: translateY(30rpx);
		transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
	}
	
	.function-section.fade-in {
		opacity: 1;
		transform: translateY(0);
	}
	
	.section-title-wrapper {
		margin-bottom: 32rpx;
	}
	
	.section-title {
		display: block;
		font-size: 36rpx;
		font-weight: 700;
		color: #1f2937;
		margin-bottom: 8rpx;
		letter-spacing: 1rpx;
	}
	
	.section-subtitle {
		display: block;
		font-size: 24rpx;
		color: #6b7280;
	}
	
	.section-desc {
		display: block;
		font-size: 24rpx;
		color: #9ca3af;
		margin-top: 6rpx;
	}
	
	.function-grid {
		display: flex;
		flex-wrap: wrap;
		justify-content: space-between;
		gap: 24rpx 0;
	}
	
	.function-item {
		width: 22%;
		display: flex;
		flex-direction: column;
		align-items: center;
		opacity: 0;
		transform: translateY(20rpx);
		animation: fadeInUp 0.6s ease-out forwards;
	}
	
	.function-icon-wrapper {
		position: relative;
		margin-bottom: 16rpx;
	}
	
	.function-icon {
		width: 112rpx;
		height: 112rpx;
		border-radius: 28rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
		position: relative;
		z-index: 2;
	}
	
	.function-icon-glow {
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		width: 112rpx;
		height: 112rpx;
		border-radius: 28rpx;
		opacity: 0.3;
		filter: blur(20rpx);
		transition: all 0.3s ease;
	}
	
	.function-item:active .function-icon {
		transform: scale(0.92) translateY(-4rpx);
		box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.15);
	}
	
	.function-item:active .function-icon-glow {
		opacity: 0.5;
		transform: translate(-50%, -50%) scale(1.1);
	}
	
	.function-text {
		font-size: 26rpx;
		color: #374151;
		font-weight: 500;
	}
	
	/* 列表区域 */
	.list-section {
		background: #fff;
		padding: 48rpx 40rpx;
		border-radius: 32rpx;
		margin-bottom: 32rpx;
		box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
		opacity: 0;
		transform: translateY(30rpx);
		transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
	}
	
	.list-section.fade-in {
		opacity: 1;
		transform: translateY(0);
	}
	
	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 32rpx;
	}
	
	.section-more {
		display: flex;
		align-items: center;
		gap: 4rpx;
		font-size: 26rpx;
		color: #6366f1;
		font-weight: 500;
		padding: 8rpx 0;
	}
	
	.loading-wrapper {
		padding: 80rpx 0;
		display: flex;
		justify-content: center;
		align-items: center;
	}
	
	.empty-wrapper {
		padding: 100rpx 0;
	}
	
	/* 路线卡片 */
	.route-cards {
		display: flex;
		flex-direction: column;
		gap: 24rpx;
	}
	
	.route-card {
		background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
		border-radius: 28rpx;
		padding: 40rpx;
		border: 1rpx solid rgba(226, 232, 240, 0.6);
		opacity: 0;
		transform: translateX(-30rpx);
		animation: slideInRight 0.6s ease-out forwards;
		transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
		box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
		position: relative;
		overflow: hidden;
	}
	
	.route-card::before {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 4rpx;
		background: linear-gradient(90deg, #6366f1 0%, #8b5cf6 100%);
		opacity: 0;
		transition: opacity 0.3s ease;
	}
	
	.route-card:active {
		transform: translateX(0) translateY(-8rpx);
		box-shadow: 0 20rpx 60rpx rgba(99, 102, 241, 0.25);
		border-color: rgba(99, 102, 241, 0.4);
	}
	
	.route-card:active::before {
		opacity: 1;
	}
	
	.route-card-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
	}
	
	.route-name {
		font-size: 32rpx;
		font-weight: 700;
		color: #1f2937;
		flex: 1;
	}
	
	.route-badge {
		padding: 8rpx 20rpx;
		background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
		color: #fff;
		font-size: 22rpx;
		border-radius: 999rpx;
		font-weight: 500;
	}
	
	.route-desc {
		display: block;
		font-size: 26rpx;
		color: #6b7280;
		line-height: 1.7;
		margin-bottom: 24rpx;
	}
	
	.route-card-footer {
		display: flex;
		gap: 32rpx;
	}
	
	.route-meta-item {
		display: flex;
		align-items: center;
		gap: 8rpx;
		font-size: 24rpx;
		color: #6b7280;
	}
	
	/* 故事列表 */
	.story-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
	
	.story-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 36rpx;
		background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
		border-radius: 28rpx;
		border: 1rpx solid rgba(226, 232, 240, 0.6);
		opacity: 0;
		transform: translateX(-30rpx);
		animation: slideInRight 0.6s ease-out forwards;
		transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
		box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
		position: relative;
		overflow: hidden;
	}
	
	.story-item::before {
		content: '';
		position: absolute;
		left: 0;
		top: 0;
		bottom: 0;
		width: 4rpx;
		background: linear-gradient(180deg, #6366f1 0%, #8b5cf6 100%);
		opacity: 0;
		transition: opacity 0.3s ease;
	}
	
	.story-item:active {
		transform: translateX(0) translateY(-6rpx);
		box-shadow: 0 16rpx 48rpx rgba(99, 102, 241, 0.2);
		border-color: rgba(99, 102, 241, 0.3);
	}
	
	.story-item:active::before {
		opacity: 1;
	}
	
	.story-content {
		flex: 1;
	}
	
	.story-title {
		display: block;
		font-size: 30rpx;
		font-weight: 600;
		color: #1f2937;
		margin-bottom: 12rpx;
	}
	
	.story-summary {
		display: block;
		font-size: 26rpx;
		color: #6b7280;
		line-height: 1.7;
		margin-bottom: 16rpx;
	}
	
	.story-tags {
		display: flex;
		gap: 12rpx;
		flex-wrap: wrap;
	}
	
	.story-tag {
		padding: 8rpx 18rpx;
		background: rgba(99, 102, 241, 0.1);
		color: #6366f1;
		font-size: 22rpx;
		border-radius: 999rpx;
		font-weight: 500;
	}
	
	.story-arrow {
		margin-left: 20rpx;
		opacity: 0.5;
		transition: all 0.3s ease;
	}
	
	.story-item:active .story-arrow {
		opacity: 1;
		transform: translateX(4rpx);
	}
	
	.bottom-spacer {
		height: 40rpx;
	}
	
	/* 动画 */
	@keyframes fadeInUp {
		from {
			opacity: 0;
			transform: translateY(30rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
	
	@keyframes slideInRight {
		from {
			opacity: 0;
			transform: translateX(-30rpx);
		}
		to {
			opacity: 1;
			transform: translateX(0);
		}
	}
	
	@keyframes float {
		0%, 100% {
			transform: translate(0, 0) rotate(0deg);
		}
		50% {
			transform: translate(30rpx, -30rpx) rotate(180deg);
		}
	}
</style>
