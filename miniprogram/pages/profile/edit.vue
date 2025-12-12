<template>
  <view class="edit-page">
    <NavBar title="个人信息" :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y>
      <view class="form-card">
        <view class="avatar-section" @click="handleUploadAvatar">
          <view class="avatar-wrapper">
            <image
              v-if="form.avatar"
              class="avatar"
              :src="form.avatar"
              mode="aspectFill"
            ></image>
            <view v-else class="avatar avatar--default">
              <uni-icons type="contact-filled" size="60" color="#9ca3af"></uni-icons>
            </view>
            <view class="avatar-edit-icon">
              <uni-icons type="camera-filled" size="20" color="#fff"></uni-icons>
            </view>
          </view>
          <text class="avatar-tip">点击更换头像</text>
        </view>
        <view class="form-item">
          <view class="form-label">
            <uni-icons type="person" size="18" color="#6366f1"></uni-icons>
            <text>用户名</text>
          </view>
          <input
            v-model="form.username"
            class="form-input"
            type="text"
            placeholder="请输入用户名"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <view class="form-label">
            <uni-icons type="phone" size="18" color="#6366f1"></uni-icons>
            <text>手机号</text>
          </view>
          <view class="form-input form-input--disabled">
            {{ form.phoneNumber || '未设置' }}
          </view>
          <text class="form-hint">手机号不可修改</text>
        </view>

        <button
          class="submit-btn"
          :class="{ 'submit-btn--disabled': !canSubmit || loading }"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ loading ? '保存中...' : '保存' }}
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'

// 使用真实后端API
import { getUserInfo, updateUserInfo, uploadFile } from '../../services/backend-api'
import { mapGetters, mapActions } from 'vuex'


export default {
  components: {
    NavBar
  },
  data() {
    return {
      form: {
        username: '',
        phoneNumber: '',
        avatar: ''
      },
      loading: false
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    canSubmit() {
      return this.form.username.trim().length >= 3 && 
             this.form.username.trim().length <= 20
    }
  },
  onLoad() {
    this.loadUserInfo()
  },
  methods: {
    ...mapActions(['setUserInfo']),
    async loadUserInfo() {
      try {
        const res = await getUserInfo()
        if (res.code === 200 && res.data) {
          this.form.username = res.data.username || ''
          this.form.phoneNumber = res.data.phoneNumber || ''
          this.form.avatar = res.data.avatar || ''
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
      }
    },

    async handleUploadAvatar() {
      try {
        const res = await uni.chooseImage({
          count: 1,
          sizeType: ['compressed'],
          sourceType: ['album', 'camera']
        })
        
        const tempFilePath = res.tempFilePaths[0]
        
        uni.showLoading({ title: '上传中...' })
        
        try {
          // 1. 先上传文件
          const uploadRes = await uploadFile(tempFilePath)
          console.log('上传接口完整响应:', JSON.stringify(uploadRes))
          
          if (uploadRes.code === 200) {
            let avatarUrl = uploadRes.data
            console.log('上传接口返回的数据(data):', JSON.stringify(avatarUrl))
            
            // 处理返回数据可能是对象的情况
            if (typeof avatarUrl === 'object' && avatarUrl !== null) {
              // 尝试从常见字段中获取URL，优先使用 originUrl
              avatarUrl = avatarUrl.originUrl || avatarUrl.url || avatarUrl.fileUrl || avatarUrl.path || avatarUrl.link || avatarUrl.data || avatarUrl.fileName
              console.log('提取后的URL:', avatarUrl)
              
              // 处理相对路径
              if (avatarUrl && typeof avatarUrl === 'string' && !avatarUrl.startsWith('http')) {
                // 假设后端服务运行在 localhost:8080，实际项目中应从配置文件获取
                const serverUrl = 'http://162.14.106.139:8080'
                // 如果返回的路径已经包含 /api，则不需要重复拼接 /api
                // 但这里返回的是 /api/files/...，所以直接拼接到 serverUrl 即可
                // 注意：如果 serverUrl 结尾有 /，或者 avatarUrl 开头有 /，需要处理
                if (serverUrl.endsWith('/') && avatarUrl.startsWith('/')) {
                  avatarUrl = serverUrl + avatarUrl.substring(1)
                } else if (!serverUrl.endsWith('/') && !avatarUrl.startsWith('/')) {
                  avatarUrl = serverUrl + '/' + avatarUrl
                } else {
                  avatarUrl = serverUrl + avatarUrl
                }
                console.log('拼接后的完整URL:', avatarUrl)
              }
              
              // 如果还是对象，可能需要查看具体结构
              if (typeof avatarUrl === 'object') {
                console.error('无法从对象中提取URL，对象结构:', JSON.stringify(uploadRes.data))
                uni.showToast({ title: '上传返回格式错误', icon: 'none' })
                return
              }
            }
            
            if (!avatarUrl) {
              uni.showToast({ title: '上传失败：未获取到图片地址', icon: 'none' })
              return
            }
            
            // 2. 更新用户信息中的头像字段
            const updateRes = await updateUserInfo({
              avatar: avatarUrl
            })
            
            if (updateRes.code === 200) {
              this.form.avatar = avatarUrl
              // 更新Vuex中的用户信息
              this.setUserInfo({
                ...this.userInfo,
                avatar: avatarUrl
              })
              uni.showToast({
                title: '头像更新成功',
                icon: 'success'
              })
            } else {
              uni.showToast({
                title: updateRes.message || '更新头像失败',
                icon: 'none'
              })
            }
          } else {
            uni.showToast({
              title: uploadRes.message || '上传失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('上传头像失败:', error)
          uni.showToast({
            title: '上传失败，请重试',
            icon: 'none'
          })
        } finally {
          uni.hideLoading()
        }
      } catch (error) {
        console.error('选择图片失败:', error)
      }
    },
    async handleSubmit() {
      if (!this.canSubmit) {
        uni.showToast({
          title: '请填写完整信息',
          icon: 'none'
        })
        return
      }

      this.loading = true
      try {
        const res = await updateUserInfo({
          username: this.form.username.trim()
        })

        if (res.code === 200 && res.data) {
          this.setUserInfo(res.data)
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1000)
        } else {
          uni.showToast({
            title: res.message || '保存失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('保存失败:', error)
        uni.showToast({
          title: error.message || '保存失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.edit-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  height: calc(100vh - 132rpx);
  padding: 24rpx 32rpx;
  padding-top: 40rpx;
  padding-bottom: 40rpx;
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



.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 48rpx;
}

.avatar-wrapper {
  position: relative;
  width: 160rpx;
  height: 160rpx;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  border: 4rpx solid #f3f4f6;
}

.avatar--default {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
  border-radius: 50%;
  border: 4rpx solid #f3f4f6;
}

.avatar-edit-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 48rpx;
  height: 48rpx;
  background: #6366f1;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid #fff;
}

.avatar-tip {
  font-size: 24rpx;
  color: #6b7280;
}

.form-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  width: 100%;
  box-sizing: border-box;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 40rpx;
}

.form-item:last-of-type {
  margin-bottom: 0;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
  color: #374151;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 88rpx;
  background: #f9fafb;
  border: 1rpx solid #e5e7eb;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1f2937;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #6366f1;
  background: #fff;
}

.form-input--disabled {
  background: #f3f4f6;
  color: #6b7280;
  cursor: not-allowed;
}

.form-hint {
  font-size: 24rpx;
  color: #9ca3af;
  margin-top: 8rpx;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: #6366f1;
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 12rpx;
  border: none;
  margin-top: 40rpx;
  box-shadow: 0 4rpx 12rpx rgba(99, 102, 241, 0.3);
  transition: all 0.2s;
}

.submit-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 2rpx 8rpx rgba(99, 102, 241, 0.2);
}

.submit-btn--disabled {
  opacity: 0.6;
  background: #d1d5db;
}
</style>

