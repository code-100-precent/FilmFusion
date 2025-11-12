<template>
  <PageShell title="个人中心" :show-back="false" :show-tab="true" tab-key="profile">
    <view class="profile-card">
      <view class="profile-card__main">
				<view class="avatar">
          <image v-if="profile.avatar" :src="profile.avatar" mode="aspectFill" />
          <uni-icons v-else type="contact-filled" color="#fff" size="56" />
        </view>
        <view class="info">
          <view class="name">
            {{ profile.name || '未登录访客' }}
				</view>
          <view class="meta">
            {{ profile.identity || loginHint }}
				</view>
			</view>
		</view>
      <button
        v-if="!isAuthenticated"
        class="profile-card__btn"
        @click="goLogin"
      >
        立即登录
      </button>
			</view>
			
    <view class="stat-group" v-if="isAuthenticated">
      <view class="stat-item">
        <view class="stat-item__value">{{ profile.visitorId || '--' }}</view>
        <view class="stat-item__label">访客编号</view>
				</view>
      <view class="stat-item">
        <view class="stat-item__value">{{ profile.phone || '--' }}</view>
        <view class="stat-item__label">绑定手机号</view>
			</view>
      <view class="stat-item">
        <view class="stat-item__value">{{ formatTime(profile.lastLoginTime) }}</view>
        <view class="stat-item__label">最近登录</view>
				</view>
			</view>
			
    <view class="menu">
      <view
        v-for="item in menus"
        :key="item.key"
        class="menu-item"
        @click="handleMenu(item)"
      >
        <view class="menu-item__left">
          <view class="menu-item__icon">
            <uni-icons :type="item.icon" size="20" color="#4f46e5" />
				</view>
          <view class="menu-item__info">
            <view class="menu-item__title">{{ item.title }}</view>
            <view class="menu-item__desc">{{ item.desc }}</view>
			</view>
				</view>
        <uni-icons type="right" size="16" color="#cbd5f5" />
			</view>
		</view>
		
    <view class="logout">
      <button
        class="logout__btn"
        :disabled="!isAuthenticated || loading"
        @click="logout"
      >
        {{ isAuthenticated ? '退出登录' : '未登录' }}
      </button>
		</view>
  </PageShell>
</template>

<script>
import PageShell from '../../components/layout/PageShell.vue'
import { getVisitorInfo, visitorLogout } from '../../services/api'

const MENUS = [
  {
    key: 'collect',
    title: '我的收藏',
    desc: '取景点收藏与关注记录',
    icon: 'heart',
    path: ''
  },
  {
    key: 'history',
    title: '浏览足迹',
    desc: '近期查看的取景点和资讯',
    icon: 'clock',
    path: ''
  },
  {
    key: 'feedback',
    title: '意见反馈',
    desc: '提交问题或需求建议',
    icon: 'chat',
    path: ''
  },
  {
    key: 'guide',
    title: '使用指南',
    desc: '了解服务流程与报备说明',
    icon: 'document',
    path: ''
  }
]
	
	export default {
		components: {
    PageShell
		},
		data() {
			return {
      profile: {
        name: '',
        identity: '',
					visitorId: '',
					phone: '',
        avatar: '',
        lastLoginTime: ''
      },
      loading: false,
      menus: MENUS
    }
  },
  computed: {
    isAuthenticated() {
      return !!uni.getStorageSync('token')
    },
    loginHint() {
      return '登录解锁收藏、浏览记录等功能'
    }
		},
		methods: {
    goLogin() {
      uni.navigateTo({ url: '/pages/login/login' })
    },
    handleMenu(item) {
      if (!this.isAuthenticated && item.key !== 'guide') {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }

      if (!item.path) {
        uni.showToast({ title: '功能开发中，敬请期待', icon: 'none' })
        return
      }

      uni.navigateTo({ url: item.path })
    },
    async loadProfile() {
      if (!this.isAuthenticated) {
        this.profile = {
          name: '',
          identity: '',
						visitorId: '',
						phone: '',
          avatar: '',
          lastLoginTime: ''
					}
					return
				}
				
				try {
					this.loading = true
        const { data } = await getVisitorInfo()
        if (data) {
          this.profile = {
            name: data.name || '',
            identity: data.identity || '',
            visitorId: data.visitorId || '',
            phone: data.phone || '',
            avatar: data.avatar || '',
            lastLoginTime: data.lastLoginTime || ''
          }
          uni.setStorageSync('visitorInfo', this.profile)
					}
				} catch (error) {
        console.warn('加载用户信息失败', error)
        this.profile = {
          name: '',
          identity: '',
						visitorId: '',
						phone: '',
          avatar: '',
          lastLoginTime: ''
					}
				} finally {
					this.loading = false
				}
			},
    logout() {
      if (!this.isAuthenticated) return
				uni.showModal({
        title: '退出确认',
        content: '确定要退出当前账号吗？',
					success: async (res) => {
          if (!res.confirm) return
							try {
            this.loading = true
								await visitorLogout()
							} catch (error) {
            console.warn('退出登录失败', error)
							} finally {
            this.loading = false
								uni.removeStorageSync('token')
								uni.removeStorageSync('visitorInfo')
            this.profile = {
              name: '',
              identity: '',
									visitorId: '',
									phone: '',
              avatar: '',
              lastLoginTime: ''
            }
            uni.showToast({ title: '已退出', icon: 'success' })
								setTimeout(() => {
              this.goLogin()
            }, 800)
          }
        }
      })
    },
    formatTime(value) {
      if (!value) return '--'
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return value
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${date.getFullYear()}-${month}-${day}`
    }
  },
  onShow() {
    this.loadProfile()
		}
	}
</script>

<style scoped lang="scss">
.profile-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #818cf8, #a855f7);
  border-radius: 32rpx;
  padding: 42rpx 36rpx;
  margin-bottom: 32rpx;
  color: #fff;
  box-shadow: 0 18rpx 48rpx rgba(99, 102, 241, 0.32);
}

.profile-card__main {
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.profile-card__btn {
  height: 72rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  font-size: 26rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.3);
	}
	
	.avatar {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
  background: rgba(255, 255, 255, 0.24);
		display: flex;
		align-items: center;
		justify-content: center;
  overflow: hidden;
	}
	
.avatar image {
		width: 100%;
		height: 100%;
	}
	
.info {
		display: flex;
		flex-direction: column;
  gap: 8rpx;
	}
	
.name {
		font-size: 36rpx;
  font-weight: 700;
	}
	
.meta {
		font-size: 26rpx;
  opacity: 0.85;
}

.stat-group {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 32rpx;
  border: 1rpx solid rgba(226, 232, 240, 0.64);
  box-shadow: 0 14rpx 32rpx rgba(15, 23, 42, 0.08);
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  align-items: center;
  color: #1f2937;
}

.stat-item__value {
  font-size: 28rpx;
  font-weight: 600;
}

.stat-item__label {
  font-size: 22rpx;
  color: #6b7280;
}

.menu {
  background: #fff;
		border-radius: 24rpx;
		overflow: hidden;
  border: 1rpx solid rgba(226, 232, 240, 0.64);
  box-shadow: 0 18rpx 38rpx rgba(15, 23, 42, 0.08);
	}
	
	.menu-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
  padding: 28rpx 32rpx;
  gap: 16rpx;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.8);
	}
	
	.menu-item:last-child {
		border-bottom: none;
	}
	
.menu-item__left {
		display: flex;
  gap: 22rpx;
		align-items: center;
	}
	
.menu-item__icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 24rpx;
  background: rgba(99, 102, 241, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-item__info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.menu-item__title {
		font-size: 30rpx;
  font-weight: 600;
  color: #111827;
}

.menu-item__desc {
  font-size: 24rpx;
  color: #6b7280;
}

.logout {
  margin-top: 40rpx;
}

.logout__btn {
		width: 100%;
		height: 88rpx;
  border-radius: 999rpx;
  background: rgba(239, 68, 68, 0.12);
  color: #b91c1c;
  font-size: 30rpx;
  border: 1rpx solid rgba(239, 68, 68, 0.24);
}

.logout__btn:disabled {
  opacity: 0.6;
	}
</style>

