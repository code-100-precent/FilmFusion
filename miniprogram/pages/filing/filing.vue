<template>
  <PageShell
    title="在线剧组报备"
    :show-back="false"
    :show-tab="false"
  >
    <view class="intro-card">
      <view class="intro-card__title">快速对接拍摄服务</view>
      <view class="intro-card__desc">提交剧组信息、拍摄计划与资料，工作人员将在 1 个工作日内联系确认</view>
      <view class="intro-card__tips">
        <view class="tip-chip">支持分阶段补充材料</view>
        <view class="tip-chip">提供政策辅导与现场协调</view>
      </view>
    </view>

    <view class="form-card">
      <view class="form-group">
        <view class="group-title">基本信息</view>
        <view class="group-fields">
          <view class="field">
					<text class="label">剧组名称</text>
            <input v-model="form.name" class="input" placeholder="请输入剧组名称" />
				</view>
          <view class="field-row">
            <view class="field field--half">
					<text class="label">联系人</text>
              <input v-model="form.contact" class="input" placeholder="联系人姓名" />
				</view>
            <view class="field field--half">
					<text class="label">联系电话</text>
              <input v-model="form.phoneNumber" class="input" type="number" placeholder="手机号码" />
            </view>
          </view>
        </view>
      </view>

      <view class="form-group">
        <view class="group-title">拍摄计划</view>
        <view class="group-fields">
          <view class="field-row">
            <view class="field field--half">
              <text class="label">开机日期</text>
              <picker mode="date" :value="form.startDate" @change="onDateChange($event, 'startDate')">
                <view class="picker">{{ form.startDate || '选择日期' }}</view>
              </picker>
            </view>
            <view class="field field--half">
              <text class="label">收官日期</text>
              <picker mode="date" :value="form.endDate" @change="onDateChange($event, 'endDate')">
                <view class="picker">{{ form.endDate || '选择日期' }}</view>
              </picker>
            </view>
          </view>
          <view class="field">
            <text class="label">拟拍摄地点</text>
            <input v-model="form.location" class="input" placeholder="请说明主要取景地" />
          </view>
          <view class="field">
            <text class="label">剧组规模</text>
            <picker :range="crewScaleOptions" :value="crewScaleIndex" @change="onCrewScaleChange">
              <view class="picker">{{ form.crewScale || '选择规模范围' }}</view>
            </picker>
          </view>
        </view>
      </view>

      <view class="form-group">
        <view class="group-title">出品信息</view>
        <view class="group-fields">
          <view class="field">
            <text class="label">第一出品单位</text>
            <input v-model="form.leadProducer" class="input" placeholder="请输入第一出品单位" />
          </view>
          <view class="field">
            <text class="label">联合出品 / 制片单位</text>
            <input v-model="form.producerUnit" class="input" placeholder="请输入制片单位或协作公司" />
          </view>
				</view>
				</view>

      <view class="form-group">
        <view class="group-title">附件材料</view>
        <view class="group-fields">
          <view class="field">
            <text class="label">许可证、批文等资料</text>
            <uni-file-picker v-model="files" fileMediatype="all" mode="grid" :limit="6" @select="onFileSelect" />
            <view class="hint">支持上传许可证、立项批复、申报表等（最多 6 个文件）</view>
				</view>
          <view class="field">
            <text class="label">备注说明</text>
            <textarea
              v-model="form.remark"
              class="textarea"
              placeholder="可补充特殊需求、主要演员、器材等信息"
              maxlength="400"
            />
				</view>
				</view>
			</view>

      <button class="submit-btn" :loading="submitting" :disabled="submitting" @click="submit">
        {{ submitting ? '提交中...' : '提交报备' }}
      </button>
	</view>
  </PageShell>
</template>

<script>
import PageShell from '../../components/layout/PageShell.vue'
import { submitReport, uploadReportFile } from '../../services/api'

const CREW_SCALE_OPTIONS = ['小型（30 人以内）', '中型（31-80 人）', '大型（81-150 人）', '超大型（150 人以上）']

export default {
  components: {
    PageShell
  },
	data() {
		return {
      form: {
        name: '',
        contact: '',
        phoneNumber: '',
        startDate: '',
        endDate: '',
        crewScale: '',
        leadProducer: '',
        producerUnit: '',
        location: '',
        remark: '',
        fileIds: []
      },
			files: [],
      submitting: false,
      crewScaleOptions: CREW_SCALE_OPTIONS
    }
  },
  computed: {
    crewScaleIndex() {
      const index = this.crewScaleOptions.indexOf(this.form.crewScale)
      return index >= 0 ? index : 0
		}
	},
	methods: {
    onCrewScaleChange(event) {
      const index = Number(event.detail.value)
      this.form.crewScale = this.crewScaleOptions[index]
    },
    onDateChange(event, field) {
      this.form[field] = event.detail.value
    },
    onFileSelect(event) {
      if (event.tempFiles && event.tempFiles.length > 0) {
        this.files = event.tempFiles
      }
    },
    validate() {
      if (!this.form.name || !this.form.name.trim()) return '请填写剧组名称'
      if (!this.form.contact || !this.form.contact.trim()) return '请填写联系人'
      if (!/^1\d{10}$/.test(this.form.phoneNumber || '')) return '请输入正确的手机号码'
      if (!this.form.startDate) return '请选择开机日期'
      if (!this.form.endDate) return '请选择收官日期'
      if (!this.form.location || !this.form.location.trim()) return '请填写拟拍摄地点'
      if (!this.form.crewScale) return '请选择剧组规模'
      if (!this.form.leadProducer || !this.form.leadProducer.trim()) return '请填写第一出品单位'
      if (!this.form.producerUnit || !this.form.producerUnit.trim()) return '请填写制片或协作单位'
      return ''
    },
		async submit() {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showModal({
          title: '提示',
          content: '请先登录后再提交报备',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({ url: '/pages/login/login' })
            }
          }
        })
        return
      }

      const message = this.validate()
      if (message) {
        uni.showToast({ title: message, icon: 'none' })
        return
      }

			this.submitting = true
      try {
        const uploadedIds = []

        for (const file of this.files) {
          const path = file && (file.path || file.url || file.tempFilePath)
          if (!path) continue
          if (file.fileId) {
            uploadedIds.push(file.fileId)
            continue
          }
          const { data } = await uploadReportFile(path)
          if (data && data.fileId) {
            uploadedIds.push(data.fileId)
          }
        }

        this.form.fileIds = uploadedIds

        await submitReport({ ...this.form })

        uni.showToast({ title: '提交成功', icon: 'success' })
			setTimeout(() => {
          uni.navigateTo({ url: '/pages/services/services' })
        }, 1200)
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '提交失败，请稍后重试', icon: 'none' })
      } finally {
				this.submitting = false
      }
		}
	}
}
</script>

<style scoped lang="scss">
.intro-card {
  background: linear-gradient(135deg, #fef3c7 0%, #f3f4ff 100%);
  border-radius: 32rpx;
  padding: 42rpx;
  margin-bottom: 36rpx;
  display: flex;
  flex-direction: column;
  gap: 22rpx;
  border: 1rpx solid rgba(251, 191, 36, 0.24);
  box-shadow: 0 16rpx 42rpx rgba(250, 204, 21, 0.16);
}

.intro-card__title {
  font-size: 38rpx;
  font-weight: 700;
  color: #92400e;
}

.intro-card__desc {
  font-size: 26rpx;
  color: #b45309;
  line-height: 1.6;
}

.intro-card__tips {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
}

.tip-chip {
  padding: 12rpx 22rpx;
  border-radius: 999rpx;
  background: rgba(217, 119, 6, 0.12);
  color: #92400e;
  font-size: 22rpx;
}

.form-card {
  background: #fff;
  border-radius: 32rpx;
  padding: 40rpx 34rpx;
  display: flex;
  flex-direction: column;
  gap: 38rpx;
  border: 1rpx solid rgba(226, 232, 240, 0.64);
  box-shadow: 0 22rpx 44rpx rgba(30, 64, 175, 0.08);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.group-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f2937;
}

.group-fields {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.field-row {
  display: flex;
  gap: 20rpx;
  flex-wrap: wrap;
}

.field--half {
  flex: 1;
}

.label {
  font-size: 26rpx;
  color: #4b5563;
}

.input,
.picker,
.textarea {
  width: 100%;
  background: #f9fafb;
  border-radius: 18rpx;
  padding: 26rpx 24rpx;
  font-size: 28rpx;
  color: #1f2937;
  border: 1rpx solid transparent;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.picker {
  color: #6b7280;
}

.input:focus,
.textarea:focus {
  border-color: #6366f1;
  background: #fff;
}

.textarea {
  min-height: 180rpx;
  line-height: 1.6;
}

.hint {
  font-size: 24rpx;
  color: #9ca3af;
}

.submit-btn {
  width: 100%;
  height: 92rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  letter-spacing: 2rpx;
  box-shadow: 0 20rpx 40rpx rgba(99, 102, 241, 0.32);
}

.submit-btn:disabled {
  opacity: 0.7;
}
</style>

