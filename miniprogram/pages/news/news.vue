<template>
	<view class="page">
		
		<view class="hero">
			<view class="hero-title">实时影讯/政策</view>
			<view class="hero-sub">雅安影视拍摄相关资讯、政策与活动</view>
		</view>
		<scroll-view scroll-y class="content" :style="{ paddingTop: '0px' }" @refresherrefresh="onRefresh" :refresher-enabled="true" :refresher-triggered="loading">
			<view class="news-card" v-for="(n, i) in list" :key="i" @click="open(n.link)">
				<image class="cover" :src="n.cover" mode="aspectFill" />
				<view class="info">
					<view class="title">{{ n.title }}</view>
					<view class="meta">
						<text>{{ n.source }}</text>
						<text>{{ n.time }}</text>
					</view>
				</view>
			</view>
			<view class="foot">已经到底啦</view>
		</scroll-view>
		<TabBar :active="'news'" />
	</view>
</template>

<script>
export default {
	data() {
		return {
			loading: false,
			list: [
				{ title: '雅安出台影视拍摄扶持政策', source: '雅安发布', time: '刚刚', cover: 'https://via.placeholder.com/160x100?text=政策', link: '' },
				{ title: '多部作品在雅安开机拍摄', source: '影讯', time: '1小时前', cover: 'https://via.placeholder.com/160x100?text=影讯', link: '' }
			]
		}
	},
	methods: {
		onRefresh() {
			this.loading = true
			setTimeout(() => { this.loading = false }, 800)
		},
		open(link) { if (link) uni.navigateTo({ url: link }) },
	}
}
</script>

<style lang="scss" scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: linear-gradient(180deg,#F7F8FC 0%,#FFFFFF 100%); }
.hero { margin: 16rpx 28rpx 0; padding: 26rpx 28rpx; background: linear-gradient(135deg,#EEF2FF 0%, #E9ECFF 100%); border-radius: 20rpx; border:1rpx solid #EEF0F7; box-shadow: 0 6rpx 20rpx rgba(79,70,229,0.06);}
.hero-title { font-size: 34rpx; font-weight: 800; color:#2E2F33; }
.hero-sub { margin-top: 8rpx; color:#6B7280; font-size: 24rpx; }
.content { flex: 1; padding: 20rpx 28rpx 140rpx; }
.news-card { display: flex; background: #fff; border-radius: 18rpx; overflow: hidden; margin-bottom: 18rpx; box-shadow: 0 8rpx 24rpx rgba(91,117,255,0.08); border: 1rpx solid #EEF0F7; }
.cover { width: 260rpx; height: 180rpx; background: #f5f7fb; }
.info { padding: 18rpx; display: flex; flex-direction: column; justify-content: space-between; flex: 1; }
.title { font-size: 28rpx; font-weight: 700; line-height: 1.4; color:#2E2F33; }
.meta { display: flex; justify-content: space-between; color: #6B7280; font-size: 22rpx; }
.foot { text-align: center; color: #9aa; padding: 28rpx 0; font-size: 24rpx; }
</style>


