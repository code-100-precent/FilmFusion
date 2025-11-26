<template>
  <view class="filing-page">
    <NavBar :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y>
      <!-- 页面标题 -->
      <view class="page-title">
        <text class="title-text">剧组报备</text>
      </view>
      <!-- 说明卡片 -->
      <view class="intro-card">
        <view class="intro-title">在线剧组报备</view>
        <view class="intro-desc">提交剧组信息、拍摄计划与资料，工作人员将在1个工作日内联系确认</view>
      </view>

      <!-- 报备表单 -->
      <view class="form-card">
        <view class="form-section">
          <view class="section-title">基本信息</view>
          <view class="form-item">
            <text class="label required">影视剧名称</text>
            <input
              v-model="form.name"
              class="input"
              type="text"
              placeholder="请输入影视剧名称"
              maxlength="50"
            />
          </view>
          <view class="form-row">
            <view class="form-item form-item--half">
              <text class="label required">类型</text>
              <picker
                mode="selector"
                :range="typeOptions"
                :value="typeIndex"
                @change="onTypeChange"
              >
                <view class="picker">{{ form.type || '请选择类型' }}</view>
              </picker>
            </view>
            <view class="form-item form-item--half">
              <text class="label required">题材</text>
              <picker
                mode="selector"
                :range="genreOptions"
                :value="genreIndex"
                @change="onGenreChange"
              >
                <view class="picker">{{ form.genre || '请选择题材' }}</view>
              </picker>
            </view>
          </view>
          <view class="form-row">
            <view class="form-item form-item--half">
              <text class="label required">集数</text>
              <input
                v-model.number="form.episodes"
                class="input"
                type="number"
                placeholder="请输入集数"
              />
            </view>
            <view class="form-item form-item--half">
              <text class="label required">投资金额（万元）</text>
              <input
                v-model.number="form.investAmount"
                class="input"
                type="digit"
                placeholder="请输入投资金额"
              />
            </view>
          </view>
        </view>

        <view class="form-section">
          <view class="section-title">拍摄计划</view>
          <view class="form-row">
            <view class="form-item form-item--half">
              <text class="label required">拍摄开始日期</text>
              <picker mode="date" :value="form.startDate" @change="onDateChange($event, 'startDate')">
                <view class="picker">{{ form.startDate || '请选择日期' }}</view>
              </picker>
            </view>
            <view class="form-item form-item--half">
              <text class="label required">拍摄结束日期</text>
              <picker mode="date" :value="form.endDate" @change="onDateChange($event, 'endDate')">
                <view class="picker">{{ form.endDate || '请选择日期' }}</view>
              </picker>
            </view>
          </view>
          <view class="form-item">
            <text class="label required">剧组规模</text>
            <picker
              mode="selector"
              :range="crewScaleOptions"
              :value="crewScaleIndex"
              @change="onCrewScaleChange"
            >
              <view class="picker">{{ form.crewScale || '请选择剧组规模' }}</view>
            </picker>
          </view>
        </view>

        <view class="form-section">
          <view class="section-title">出品信息</view>
          <view class="form-item">
            <text class="label required">主创人员</text>
            <input
              v-model="form.mainCreators"
              class="input"
              type="text"
              placeholder="请输入主创人员（导演、编剧等）"
              maxlength="255"
            />
          </view>
          <view class="form-item">
            <text class="label required">第一出品单位</text>
            <input
              v-model="form.leadProducer"
              class="input"
              type="text"
              placeholder="请输入第一出品单位"
              maxlength="100"
            />
          </view>
          <view class="form-item">
            <text class="label required">制片单位</text>
            <input
              v-model="form.producerUnit"
              class="input"
              type="text"
              placeholder="请输入制片单位"
              maxlength="100"
            />
          </view>
        </view>

        <view class="form-section">
          <view class="section-title">联系信息</view>
          <view class="form-item">
            <text class="label required">联系人</text>
            <input
              v-model="form.contact"
              class="input"
              type="text"
              placeholder="请输入联系人姓名"
              maxlength="50"
            />
          </view>
          <view class="form-item">
            <text class="label required">联系电话</text>
            <input
              v-model="form.phoneNumber"
              class="input"
              type="number"
              placeholder="请输入联系电话"
              maxlength="20"
            />
          </view>
          <view class="form-item">
            <text class="label required">剧组职务</text>
            <input
              v-model="form.crewPosition"
              class="input"
              type="text"
              placeholder="请输入剧组职务（如：导演、制片人等）"
              maxlength="50"
            />
          </view>
        </view>
      </view>

      <view class="form-card">
        <view class="form-section">
          <view class="section-title">相关材料</view>
          
          <view class="form-item">
            <text class="label required">影视拍摄许可证</text>
            <view class="upload-area" @click="uploadFile('permit')">
              <view v-if="!form.files.permit" class="upload-placeholder">
                <uni-icons type="upload" size="24" color="#6366f1"></uni-icons>
                <text>点击上传</text>
              </view>
              <view v-else class="file-preview">
                <view class="file-info">
                  <uni-icons type="paperclip" size="20" color="#6366f1"></uni-icons>
                  <text class="file-name">已上传文件</text>
                </view>
                <uni-icons type="close" size="20" color="#ef4444" @click.stop="removeFile('permit')"></uni-icons>
              </view>
            </view>
          </view>
          
          <view class="form-item">
            <text class="label required">立项审批文件</text>
            <view class="upload-area" @click="uploadFile('approval')">
              <view v-if="!form.files.approval" class="upload-placeholder">
                <uni-icons type="upload" size="24" color="#6366f1"></uni-icons>
                <text>点击上传</text>
              </view>
              <view v-else class="file-preview">
                <view class="file-info">
                  <uni-icons type="paperclip" size="20" color="#6366f1"></uni-icons>
                  <text class="file-name">已上传文件</text>
                </view>
                <uni-icons type="close" size="20" color="#ef4444" @click.stop="removeFile('approval')"></uni-icons>
              </view>
            </view>
          </view>
          
          <view class="form-item">
            <text class="label">协拍服务申请表</text>
            <view class="upload-area" @click="uploadFile('application')">
              <view v-if="!form.files.application" class="upload-placeholder">
                <uni-icons type="upload" size="24" color="#6366f1"></uni-icons>
                <text>点击上传</text>
              </view>
              <view v-else class="file-preview">
                <view class="file-info">
                  <uni-icons type="paperclip" size="20" color="#6366f1"></uni-icons>
                  <text class="file-name">已上传文件</text>
                </view>
                <uni-icons type="close" size="20" color="#ef4444" @click.stop="removeFile('application')"></uni-icons>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button
          class="submit-btn"
          :class="{ 'submit-btn--disabled': !canSubmit || submitting }"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '提交报备' }}
        </button>
        <text class="submit-tip">提交后，工作人员将在1个工作日内联系您</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import { createReport } from '../../services/api'
import { mapGetters } from 'vuex'

export default {
  components: {
    NavBar
  },
  data() {
    return {
      submitting: false,
      typeOptions: ['电视剧', '电影', '网络剧', '纪录片', '其他'],
      genreOptions: ['历史', '现代', '古装', '悬疑', '爱情', '喜剧', '动作', '科幻', '文化', '其他'],
      crewScaleOptions: ['小型', '中型', '大型'],
      typeIndex: -1,
      genreIndex: -1,
      crewScaleIndex: -1,
      form: {
        name: '',
        type: '',
        genre: '',
        episodes: null,
        investAmount: null,
        mainCreators: '',
        leadProducer: '',
        producerUnit: '',
        startDate: '',
        endDate: '',
        crewScale: '',
        contact: '',
        phoneNumber: '',
        crewPosition: '',
        files: {
          permit: '',
          approval: '',
          application: ''
        }
      }
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn']),
    canSubmit() {
      return (
        this.form.name &&
        this.form.type &&
        this.form.genre &&
        this.form.episodes &&
        this.form.investAmount &&
        this.form.mainCreators &&
        this.form.leadProducer &&
        this.form.producerUnit &&
        this.form.startDate &&
        this.form.endDate &&
        this.form.crewScale &&
        this.form.contact &&
        this.form.phoneNumber &&
        this.form.phoneNumber &&
        this.form.crewPosition &&
        this.form.files.permit &&
        this.form.files.approval
      )
    }
  },
  onLoad() {
    if (!this.isLoggedIn) {
      uni.showModal({
        title: '提示',
        content: '请先登录',
        showCancel: false,
        success: () => {
          uni.navigateTo({
            url: '/pages/login/login'
          })
        }
      })
    }
  },
  methods: {
    onTypeChange(e) {
      this.typeIndex = e.detail.value
      this.form.type = this.typeOptions[e.detail.value]
    },
    onGenreChange(e) {
      this.genreIndex = e.detail.value
      this.form.genre = this.genreOptions[e.detail.value]
    },
    onCrewScaleChange(e) {
      this.crewScaleIndex = e.detail.value
      this.form.crewScale = this.crewScaleOptions[e.detail.value]
    },
    onDateChange(e, field) {
      this.form[field] = e.detail.value
    },
    uploadFile(type) {
      // Mock upload
      uni.chooseImage({
        count: 1,
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0];
          // In a real app, upload to server here.
          // For now, just use the temp path.
          this.form.files[type] = tempFilePath;
          uni.showToast({
            title: '上传成功',
            icon: 'success'
          });
        }
      });
    },
    removeFile(type) {
      this.form.files[type] = '';
    },
    async handleSubmit() {
      if (!this.isLoggedIn) {
        uni.showModal({
          title: '提示',
          content: '请先登录',
          showCancel: false,
          success: () => {
            uni.navigateTo({
              url: '/pages/login/login'
            })
          }
        })
        return
      }

      if (!this.canSubmit) {
        uni.showToast({
          title: '请填写完整的报备信息',
          icon: 'none'
        })
        return
      }

      if (new Date(this.form.startDate) > new Date(this.form.endDate)) {
        uni.showToast({
          title: '结束日期不能早于开始日期',
          icon: 'none'
        })
        return
      }

      this.submitting = true
      try {
        const res = await createReport({
          name: this.form.name,
          type: this.form.type,
          genre: this.form.genre,
          episodes: this.form.episodes,
          investAmount: this.form.investAmount,
          mainCreators: this.form.mainCreators,
          leadProducer: this.form.leadProducer,
          producerUnit: this.form.producerUnit,
          startDate: this.form.startDate,
          endDate: this.form.endDate,
          crewScale: this.form.crewScale,
          contact: this.form.contact,
          phoneNumber: this.form.phoneNumber,
          crewPosition: this.form.crewPosition
        })

        if (res.code === 200) {
          uni.showModal({
            title: '提交成功',
            content: '您的报备信息已提交，工作人员将在1个工作日内联系您',
            showCancel: false,
            success: () => {
              this.resetForm()
              uni.switchTab({
                url: '/pages/index/index'
              })
            }
          })
        } else {
          uni.showToast({
            title: res.message || '提交失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('提交报备失败:', error)
        uni.showToast({
          title: error.message || '提交失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.submitting = false
      }
    },
    resetForm() {
      this.form = {
        name: '',
        type: '',
        genre: '',
        episodes: null,
        investAmount: null,
        mainCreators: '',
        leadProducer: '',
        producerUnit: '',
        startDate: '',
        endDate: '',
        crewScale: '',
        contact: '',
        phoneNumber: '',
        phoneNumber: '',
        crewPosition: '',
        files: {
          permit: '',
          approval: '',
          application: ''
        }
      }
      this.typeIndex = -1
      this.genreIndex = -1
      this.crewScaleIndex = -1
    }
  }
}
</script>

<style lang="scss" scoped>
.filing-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  height: calc(100vh - 132rpx - 100rpx);
  padding: 24rpx;
  padding-bottom: calc(80rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  width: 100%;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  /* 兼容火狐浏览器 */
  scrollbar-width: none;
  /* 兼容IE浏览器 */
  -ms-overflow-style: none;
}

/* 页面标题样式 */
.page-title {
  padding: 32rpx 0 8rpx 0;
  width: 100%;
}

.title-text {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.intro-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  color: #fff;
  width: 100%;
  box-sizing: border-box;
}

.intro-title {
  font-size: 36rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}

.intro-desc {
  font-size: 26rpx;
  opacity: 0.9;
  line-height: 1.6;
}

.form-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.form-section {
  margin-bottom: 32rpx;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #f3f4f6;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-row {
  display: flex;
  gap: 24rpx;
}

.form-item--half {
  flex: 1;
}

.label {
  display: block;
  font-size: 28rpx;
  color: #374151;
  font-weight: 500;
  margin-bottom: 16rpx;
}

.label.required::after {
  content: ' *';
  color: #ef4444;
}

.input,
.picker {
  width: 100%;
  height: 80rpx;
  background: #f9fafb;
  border: 2rpx solid #e5e7eb;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1f2937;
  box-sizing: border-box;
  margin: 0;
}

.input:focus {
  border-color: #6366f1;
  background: #fff;
}

.picker {
  display: flex;
  align-items: center;
  color: #1f2937;
}

.submit-section {
  padding: 24rpx 0;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 16rpx;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
  margin-bottom: 20rpx;
  box-sizing: border-box;
}

.submit-btn--disabled {
  opacity: 0.6;
  background: #d1d5db;
}

.submit-tip {
  display: block;
  text-align: center;
  font-size: 24rpx;
  color: #9ca3af;
}
.upload-area {
  width: 100%;
  height: 80rpx;
  background: #f9fafb;
  border: 2rpx dashed #cbd5e1;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.upload-area:active {
  background: #f3f4f6;
  border-color: #6366f1;
}

.upload-placeholder {
  display: flex;
  align-items: center;
  gap: 12rpx;
  color: #6366f1;
  font-size: 28rpx;
}

.file-preview {
  width: 100%;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.file-name {
  font-size: 26rpx;
  color: #374151;
}
</style>
