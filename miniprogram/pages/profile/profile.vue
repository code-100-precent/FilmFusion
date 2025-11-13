<template>
  <view class="profile-page">
    <NavBar title="个人中心" :show-back="false"></NavBar>

    <scroll-view class="content" scroll-y>
      <!-- 用户信息卡片 -->
      <view class="user-card">
        <view v-if="isLoggedIn" class="user-info">
          <view class="avatar-wrapper">
            <image
              v-if="userInfo.avatar"
              class="avatar"
              :src="userInfo.avatar"
              mode="aspectFill"
            ></image>
            <view v-else class="avatar avatar--default">
              <uni-icons type="contact-filled" size="60" color="#9ca3af"></uni-icons>
            </view>
          </view>
          <view class="user-details">
            <text class="username">{{ userInfo.username || '未设置' }}</text>
            <text class="user-phone">{{ formatPhone(userInfo.phoneNumber) }}</text>
            <view class="user-role">
              <text class="role-badge" :class="roleClass">
                {{ roleText }}
              </text>
            </view>
          </view>
        </view>
        <view v-else class="login-prompt">
          <view class="avatar-wrapper">
            <view class="avatar avatar--default">
              <uni-icons type="contact-filled" size="60" color="#9ca3af"></uni-icons>
            </view>
          </view>
          <view class="login-text">
            <text class="login-title">未登录</text>
            <text class="login-desc">登录后享受更多服务</text>
          </view>
          <button class="login-btn" @click="goToLogin">立即登录</button>
        </view>
      </view>

      <!-- 功能菜单 -->
      <view v-if="isLoggedIn" class="menu-section">
        <view class="menu-group">
          <view class="menu-item" @click="goToMyReports">
            <view class="menu-icon" style="background: #eef2ff;">
              <uni-icons type="list" size="24" color="#6366f1"></uni-icons>
            </view>
            <text class="menu-text">我的报备</text>
            <uni-icons type="right" size="16" color="#d1d5db"></uni-icons>
          </view>
          <view class="menu-item" @click="goToMyFeedback">
            <view class="menu-icon" style="background: #f3e8ff;">
              <uni-icons type="chatbubble" size="24" color="#8b5cf6"></uni-icons>
            </view>
            <text class="menu-text">我的反馈</text>
            <uni-icons type="right" size="16" color="#d1d5db"></uni-icons>
          </view>
        </view>

        <view class="menu-group">
          <view class="menu-item" @click="goToEditProfile">
            <view class="menu-icon" style="background: #fce7f3;">
              <uni-icons type="person" size="24" color="#ec4899"></uni-icons>
            </view>
            <text class="menu-text">个人信息</text>
            <uni-icons type="right" size="16" color="#d1d5db"></uni-icons>
          </view>
          <view class="menu-item" @click="goToChangePassword">
            <view class="menu-icon" style="background: #cffafe;">
              <uni-icons type="locked" size="24" color="#06b6d4"></uni-icons>
            </view>
            <text class="menu-text">修改密码</text>
            <uni-icons type="right" size="16" color="#d1d5db"></uni-icons>
          </view>
        </view>

        <view class="menu-group">
          <view class="menu-item" @click="goToFeedback">
            <view class="menu-icon" style="background: #f3e8ff;">
              <uni-icons type="chatboxes" size="24" color="#8b5cf6"></uni-icons>
            </view>
            <text class="menu-text">意见反馈</text>
            <uni-icons type="right" size="16" color="#d1d5db"></uni-icons>
          </view>
        </view>

        <view class="menu-group">
          <view class="menu-item" @click="goToHelp">
            <view class="menu-icon" style="background: #ecfdf5;">
              <uni-icons type="help" size="24" color="#10b981"></uni-icons>
            </view>
            <text class="menu-text">帮助中心</text>
            <uni-icons type="right" size="16" color="#d1d5db"></uni-icons>
          </view>
          <view class="menu-item" @click="goToAbout">
            <view class="menu-icon" style="background: #e5e7eb;">
              <uni-icons type="info" size="24" color="#6b7280"></uni-icons>
            </view>
            <text class="menu-text">关于我们</text>
            <uni-icons type="right" size="16" color="#d1d5db"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 退出登录 -->
      <view v-if="isLoggedIn" class="logout-section">
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </view>
    </scroll-view>

    <!-- 底部导航栏 -->
    <TabBar :current="'profile'"></TabBar>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
import TabBar from '../../components/TabBar/TabBar.vue'
import { getCurrentUserInfo, userLogout } from '../../services/api'
import { mapGetters, mapActions } from 'vuex'

export default {
  components: {
    NavBar,
    TabBar
  },
  data() {
    return {
      userInfo: {},
      loading: false
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn']),
    roleText() {
      return this.getRoleText(this.userInfo.role)
    },
    roleClass() {
      return this.getRoleClass(this.userInfo.role)
    }
  },
  onLoad() {
    if (this.isLoggedIn) {
      this.loadUserInfo()
    }
  },
  onShow() {
    if (this.isLoggedIn) {
      this.loadUserInfo()
    }
  },
  methods: {
    ...mapActions(['logout']),
    async loadUserInfo() {
      if (!this.isLoggedIn) return

      this.loading = true
      try {
        const res = await getCurrentUserInfo()
        if (res.code === 200 && res.data) {
          this.userInfo = res.data
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
      } finally {
        this.loading = false
      }
    },
    goToLogin() {
      uni.navigateTo({
        url: '/pages/login/login'
      })
    },
    goToMyReports() {
      uni.navigateTo({
        url: '/pages/profile/reports'
      })
    },
    goToMyFeedback() {
      uni.navigateTo({
        url: '/pages/profile/feedback-list'
      })
    },
    goToEditProfile() {
      uni.navigateTo({
        url: '/pages/profile/edit'
      })
    },
    goToChangePassword() {
      uni.navigateTo({
        url: '/pages/profile/password'
      })
    },
    goToFeedback() {
      uni.navigateTo({
        url: '/pages/feedback/feedback'
      })
    },
    goToHelp() {
      uni.navigateTo({
        url: '/pages/profile/help'
      })
    },
    goToAbout() {
      uni.navigateTo({
        url: '/pages/profile/about'
      })
    },
    async handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await userLogout()
            } catch (error) {
              console.error('退出登录失败:', error)
            } finally {
              // 无论后端是否成功，都清除本地token
              this.logout()
              uni.showToast({
                title: '已退出登录',
                icon: 'success'
              })
            }
          }
        }
      })
    },
    formatPhone(phone) {
      if (!phone) return '未绑定手机号'
      if (phone.length === 11) {
        return phone.substring(0, 3) + '****' + phone.substring(7)
      }
      return phone
    },
    getRoleText(role) {
      const roleMap = {
        USER: '普通用户',
        ADMIN: '管理员',
        SUPER_ADMIN: '超级管理员'
      }
      return roleMap[role] || '用户'
    },
    getRoleClass(role) {
      if (role === 'ADMIN' || role === 'SUPER_ADMIN') {
        return 'role-badge--admin'
      }
      return 'role-badge--user'
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  padding: 24rpx 32rpx;
  padding-top: 40rpx;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  width: 100%;
}

.user-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  width: 100%;
  box-sizing: border-box;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.avatar-wrapper {
  width: 100rpx;
  height: 100rpx;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 2rpx solid #e5e7eb;
  background: #f9fafb;
}

.avatar--default {
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.username {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
}

.user-phone {
  font-size: 24rpx;
  color: #6b7280;
}

.user-role {
  margin-top: 8rpx;
}

.role-badge {
  display: inline-block;
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  font-weight: 500;
}

.role-badge--user {
  background: #f3f4f6;
  color: #6b7280;
}

.role-badge--admin {
  background: #eef2ff;
  color: #6366f1;
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.login-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.login-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
}

.login-desc {
  font-size: 24rpx;
  color: #6b7280;
}

.login-btn {
  width: 200rpx;
  height: 72rpx;
  background: #6366f1;
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
  border-radius: 12rpx;
  border: none;
}

.menu-section {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.menu-group {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  width: 100%;
  box-sizing: border-box;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f3f4f6;
  transition: all 0.2s;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: #f9fafb;
}

.menu-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.menu-text {
  flex: 1;
  font-size: 28rpx;
  color: #1f2937;
  font-weight: 500;
}

.logout-section {
  margin-top: 24rpx;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  background: #fff;
  color: #ef4444;
  font-size: 28rpx;
  font-weight: 500;
  border-radius: 12rpx;
  border: 1rpx solid #fee2e2;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  box-sizing: border-box;
}
</style>
