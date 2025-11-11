<template>
	<view class="page">
		
		<view class="hero">
			<view class="hero-title">雅安取景手册</view>
			<view class="hero-sub">精选文化、山水、建筑、年代等高质点位</view>
		</view>
		<scroll-view scroll-y class="content" :style="{ paddingTop: '0px' }">
			<view class="section">
				<view class="section-title">雅州文化</view>
				<view class="card-list">
					<view class="card" v-for="(item, idx) in scenes.culture" :key="idx">
						<image class="thumb" :src="item.cover" mode="aspectFill" />
						<view class="meta">
							<text class="name">{{ item.name }}</text>
							<text class="desc">{{ item.desc }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="section">
				<view class="section-title">生态山水</view>
				<view class="card-list">
					<view class="card" v-for="(item, idx) in scenes.nature" :key="idx">
						<image class="thumb" :src="item.cover" mode="aspectFill" />
						<view class="meta">
							<text class="name">{{ item.name }}</text>
							<text class="desc">{{ item.desc }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="section">
				<view class="section-title">簃多时人落建筑</view>
				<view class="card-list">
					<view class="card" v-for="(item, idx) in scenes.arch" :key="idx">
						<image class="thumb" :src="item.cover" mode="aspectFill" />
						<view class="meta">
							<text class="name">{{ item.name }}</text>
							<text class="desc">{{ item.desc }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="section">
				<view class="section-title">年代记忆</view>
				<view class="card-list">
					<view class="card" v-for="(item, idx) in scenes.memory" :key="idx">
						<image class="thumb" :src="item.cover" mode="aspectFill" />
						<view class="meta">
							<text class="name">{{ item.name }}</text>
							<text class="desc">{{ item.desc }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="section">
				<view class="section-title">特色资源</view>
				<view class="tag-list">
					<text class="tag" v-for="(t, i) in scenes.resources" :key="i">{{ t }}</text>
				</view>
			</view>
		</scroll-view>

		<TabBar :active="'scenes'" />
	</view>
</template>

<script>
export default {
	data() {
		return {
			scenes: {
				culture: [
					{ name: '雅州古街', desc: '古风街巷、人文底蕴', cover: 'https://via.placeholder.com/160x100?text=雅州古街' }
				],
				nature: [
					{ name: '雨城峡谷', desc: '峡谷飞瀑、生态山水', cover: 'https://via.placeholder.com/160x100?text=雨城峡谷' }
				],
				arch: [
					{ name: '民居老院', desc: '川西民居、木结构', cover: 'https://via.placeholder.com/160x100?text=民居老院' }
				],
				memory: [
					{ name: '年代厂区', desc: '工业遗址、年代质感', cover: 'https://via.placeholder.com/160x100?text=年代厂区' }
				],
				resources: ['茶马古道', '藏羌风情', '雨城烟火', '雅女文化']
			}
		}
	},
	
}
</script>

<style lang="scss" scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: linear-gradient(180deg,#F7F8FC 0%,#FFFFFF 100%); }
.hero { margin: 16rpx 28rpx 0; padding: 26rpx 28rpx; background: linear-gradient(135deg,#EEF2FF 0%, #E9ECFF 100%); border-radius: 20rpx; border:1rpx solid #EEF0F7; box-shadow: 0 6rpx 20rpx rgba(79,70,229,0.06);}
.hero-title { font-size: 34rpx; font-weight: 800; color:#2E2F33; }
.hero-sub { margin-top: 8rpx; color:#6B7280; font-size: 24rpx; }
.content { flex: 1; padding: 20rpx 28rpx 140rpx; }
.section { margin-bottom: 36rpx; }
.section-title { font-size: 32rpx; font-weight: 700; margin: 18rpx 0; color:#2E2F33; position: relative; padding-left: 18rpx; }
.section-title::before { content:''; position:absolute; left:0; top:50%; transform:translateY(-50%); width:8rpx; height:26rpx; border-radius:6rpx; background:#5b75ff; }
.card-list { display: grid; grid-template-columns: 1fr 1fr; gap: 18rpx; }
.card { background: #fff; border-radius: 18rpx; overflow: hidden; box-shadow: 0 8rpx 24rpx rgba(91,117,255,0.08); border: 1rpx solid #EEF0F7; }
.thumb { width: 100%; height: 200rpx; background: #f5f7fb; }
.meta { padding: 18rpx; }
.name { font-size: 28rpx; font-weight: 700; color:#2E2F33; }
.desc { font-size: 24rpx; color: #6B7280; margin-top: 6rpx; display: block; line-height: 1.5; }
.tag-list { display: flex; flex-wrap: wrap; gap: 12rpx; }
.tag { padding: 10rpx 18rpx; background: #EEF2FF; color: #4F46E5; border-radius: 999rpx; font-size: 22rpx; }
</style>


