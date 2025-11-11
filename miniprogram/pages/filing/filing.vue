<template>
	<view class="page">
		
		<view class="hero">
			<view class="hero-title">在线剧组报备</view>
			<view class="hero-sub">提交许可证件与拍摄信息，快速对接服务</view>
		</view>
		<scroll-view scroll-y class="content" :style="{ paddingTop: '0px' }">
			<view class="form">
				<view class="form-item">
					<text class="label">剧组名称</text>
					<input class="input" v-model="form.name" placeholder="请输入剧组名称" />
				</view>
				<view class="form-item">
					<text class="label">联系人</text>
					<input class="input" v-model="form.contact" placeholder="联系人姓名" />
				</view>
				<view class="form-item">
					<text class="label">联系电话</text>
					<input class="input" v-model="form.phone" placeholder="手机号码" type="number" />
				</view>
				<view class="form-item">
					<text class="label">拍摄时间</text>
					<input class="input" v-model="form.schedule" placeholder="如 2025-12-01 至 2025-12-10" />
				</view>
				<view class="form-item">
					<text class="label">拍摄地点</text>
					<input class="input" v-model="form.location" placeholder="拟拍摄地点" />
				</view>
				<view class="form-item">
					<text class="label">材料上传</text>
					<uni-file-picker v-model="files" fileMediatype="all" mode="grid" :limit="6" />
					<view class="hint">支持：许可证、立项批复、服务申报表等</view>
				</view>
				<view class="form-item">
					<text class="label">备注</text>
					<textarea class="textarea" v-model="form.remark" placeholder="补充说明（选填）" auto-height />
				</view>
				<button class="submit" :loading="submitting" @click="submit">提交报备</button>
			</view>
		</scroll-view>

		<TabBar :active="'filing'" />
	</view>
</template>

<script>
export default {
	data() {
		return {
			form: { name: '', contact: '', phone: '', schedule: '', location: '', remark: '' },
			files: [],
			submitting: false
		}
	},
	methods: {
		async submit() {
			if (!this.form.name || !this.form.contact || !this.form.phone) {
				return uni.showToast({ title: '请完善必填信息', icon: 'none' })
			}
			this.submitting = true
			setTimeout(() => {
				this.submitting = false
				uni.showToast({ title: '提交成功', icon: 'success' })
			}, 800)
		}
	}
}
</script>

<style lang="scss" scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: linear-gradient(180deg,#F7F8FC 0%,#FFFFFF 100%); }
.hero { margin: 16rpx 28rpx 0; padding: 26rpx 28rpx; background: linear-gradient(135deg,#EEF2FF 0%, #E9ECFF 100%); border-radius: 20rpx; border:1rpx solid #EEF0F7; box-shadow: 0 6rpx 20rpx rgba(79,70,229,0.06);}
.hero-title { font-size: 34rpx; font-weight: 800; color:#2E2F33; }
.hero-sub { margin-top: 8rpx; color:#6B7280; font-size: 24rpx; }
.content { flex: 1; padding: 20rpx 28rpx 140rpx; }
.form { background: #fff; border-radius: 18rpx; padding: 28rpx; box-shadow: 0 8rpx 24rpx rgba(91,117,255,0.08); border: 1rpx solid #EEF0F7; }
.form-item { margin-bottom: 24rpx; }
.label { display: block; font-size: 26rpx; color: #6B7280; margin-bottom: 10rpx; }
.input { background: #F7F8FA; border-radius: 12rpx; padding: 22rpx 20rpx; font-size: 28rpx; }
.textarea { background: #F7F8FA; border-radius: 12rpx; padding: 20rpx; min-height: 160rpx; font-size: 28rpx; }
.hint { color: #9CA3AF; font-size: 22rpx; margin-top: 8rpx; }
.submit { margin-top: 16rpx; background: linear-gradient(135deg,#5b75ff,#8f9bff); color:#fff; border-radius: 999rpx; }
</style>


