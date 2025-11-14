<template>
  <view class="tabbar">
    <view
      v-for="item in normalizedItems"
      :key="item.key"
      class="tabbar-item"
      :class="{ active: currentActive === item.key }"
      @click="handleSwitch(item)"
    >
      <view class="icon-wrap" :class="{ bounce: currentActive === item.key }">
        <uni-icons :type="item.icon" :color="currentActive === item.key ? activeColor : inactiveColor" size="22" />
      </view>
      <text class="tabbar-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script>
const DEFAULT_ITEMS = [
  { key: 'index', path: '/pages/index/index', text: '首页', icon: 'home' },
  { key: 'scenes', path: '/pages/scenes/scenes', text: '场地', icon: 'location' },
  { key: 'services', path: '/pages/services/services', text: '服务', icon: 'phone' },
  { key: 'news', path: '/pages/news/news', text: '资讯', icon: 'chatbubble' },
  { key: 'profile', path: '/pages/profile/profile', text: '我的', icon: 'person' }
]

export default {
  name: 'TabBar',
  props: {
    current: {
      type: String,
      default: 'index'
    },
    active: {
      type: String,
      default: ''
    },
    items: {
      type: Array,
      default: () => []
    },
    activeColor: {
      type: String,
      default: '#4F46E5'
    },
    inactiveColor: {
      type: String,
      default: '#A9A7B7'
    }
  },
  computed: {
    normalizedItems() {
      return this.items && this.items.length ? this.items : DEFAULT_ITEMS
    },
    currentActive() {
      return this.active || this.current
    }
  },
  methods: {
    handleSwitch(item) {
      if (this.currentActive === item.key) return
      this.$emit('change', item.key)
      // 使用reLaunch确保页面切换，因为已移除原生tabBar
      uni.reLaunch({ url: item.path })
    }
  }
}
</script>

<style scoped lang="scss">
.tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(24rpx) saturate(180%);
  -webkit-backdrop-filter: blur(24rpx) saturate(180%);
  border: none;
  border-top: 1rpx solid rgba(234, 236, 244, 0.8);
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 999;
  padding: 13rpx 32rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  margin: 0;
  box-sizing: border-box;
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0;
  position: relative;
  color: #8a8fa0;
  min-height: 64rpx;
}

.icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56rpx;
  height: 56rpx;
  border-radius: 16rpx;
  background: rgba(79, 70, 229, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}

.icon-wrap::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0);
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.2) 0%, transparent 70%);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.icon-wrap.bounce {
  transform: translateY(-4rpx) scale(1.05);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
  box-shadow: 0 4rpx 16rpx rgba(99, 102, 241, 0.2);
}

.icon-wrap.bounce::before {
  transform: translate(-50%, -50%) scale(1);
}

.tabbar-text {
  font-size: 22rpx;
  margin-top: 8rpx;
  font-weight: 500;
  line-height: 1;
  flex-shrink: 0;
}

.tabbar-item.active .tabbar-text {
  color: #4f46e5;
}

/* 去掉底部栏选中状态的紫色横线
.tabbar-item.active::after {
  content: '';
  position: absolute;
  bottom: 8rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  border-radius: 999rpx;
  background: linear-gradient(90deg, #6366f1 0%, #8b5cf6 100%);
  box-shadow: 0 2rpx 8rpx rgba(99, 102, 241, 0.4);
  animation: slideIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
*/

/* 去掉底部栏选中状态的动画
@keyframes slideIn {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    width: 40rpx;
    opacity: 1;
  }
}
*/
</style>
