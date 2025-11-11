<template>
	<view class="page">
		
		<view class="hero">
			<view class="hero-title">一站式协拍对接</view>
			<view class="hero-sub">景区、酒店民宿、车辆器材快速联系</view>
		</view>
		<scroll-view scroll-y class="content" :style="{ paddingTop: '0px' }">
			<view class="group">
				<view class="title">景区</view>
				<view class="list">
					<view class="item" v-for="(s, i) in services.attractions" :key="i">
						<view class="left">
							<view class="name">{{ s.name }}</view>
							<view class="meta">{{ s.addr }}</view>
						</view>
						<view class="right">
							<button class="btn" size="mini" @click="call(s.phone)">电话</button>
						</view>
					</view>
				</view>
			</view>

			<view class="group">
				<view class="title">酒店/民宿</view>
				<view class="list">
					<view class="item" v-for="(s, i) in services.stays" :key="i">
						<view class="left">
							<view class="name">{{ s.name }}</view>
							<view class="meta">{{ s.addr }}</view>
						</view>
						<view class="right">
							<button class="btn" size="mini" @click="call(s.phone)">电话</button>
						</view>
					</view>
				</view>
			</view>

			<view class="group">
				<view class="title">车辆/器材</view>
				<view class="list">
					<view class="item" v-for="(s, i) in services.support" :key="i">
						<view class="left">
							<view class="name">{{ s.name }}</view>
							<view class="meta">{{ s.desc }}</view>
						</view>
						<view class="right">
							<button class="btn" size="mini" @click="call(s.phone)">电话</button>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>

		<TabBar :active="'services'" />
	</view>
</template>

<script>
export default {
	data() {
		return {
			services: {
				attractions: [{ name: '青衣江景区服务中心', addr: '雨城区滨江路88号', phone: '0835-0000001' }],
				stays: [{ name: '雅安影视合作酒店', addr: '雨城区中环路168号', phone: '0835-0000002' }],
				support: [{ name: '拍摄车辆与器材租赁', desc: '通告车/灯光/支架', phone: '0835-0000003' }]
			}
		}
	},
	methods: {
		call(phone) { uni.makePhoneCall({ phoneNumber: phone }) }
	}
}
</script>

<style lang="scss" scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: linear-gradient(180deg,#F7F8FC 0%,#FFFFFF 100%); }
.hero { margin: 16rpx 28rpx 0; padding: 26rpx 28rpx; background: linear-gradient(135deg,#EEF2FF 0%, #E9ECFF 100%); border-radius: 20rpx; border:1rpx solid #EEF0F7; box-shadow: 0 6rpx 20rpx rgba(79,70,229,0.06);}
.hero-title { font-size: 34rpx; font-weight: 800; color:#2E2F33; }
.hero-sub { margin-top: 8rpx; color:#6B7280; font-size: 24rpx; }
.content { flex: 1; padding: 20rpx 28rpx 140rpx; }
.group { margin-bottom: 32rpx; }
.title { font-size: 30rpx; font-weight: 700; margin: 14rpx 0; color:#2E2F33; position:relative; padding-left: 18rpx; }
.title::before { content:''; position:absolute; left:0; top:50%; transform:translateY(-50%); width:8rpx; height:26rpx; border-radius:6rpx; background:#5b75ff; }
.list { background: #fff; border-radius: 18rpx; overflow: hidden; box-shadow: 0 8rpx 24rpx rgba(91,117,255,0.08); border: 1rpx solid #EEF0F7; }
.item { display: flex; align-items: center; justify-content: space-between; padding: 26rpx; border-bottom: 1px solid #F1F3F8; }
.item:last-child { border-bottom: none; }
.left .name { font-size: 28rpx; font-weight: 700; color:#2E2F33; }
.left .meta { font-size: 24rpx; color: #6B7280; margin-top: 6rpx; }
.btn { background: linear-gradient(135deg,#5b75ff,#8f9bff); color: #fff; border-radius: 999rpx; padding: 0 28rpx; }
</style>


