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
      default: '#20B2AA'
    },
    inactiveColor: {
      type: String,
      default: '#9CA3AF'
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
  background: #fff;
  border-top: 1rpx solid #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 999;
  padding: 12rpx 0;
  padding-bottom: calc(12rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rpx 0;
  position: relative;
  transition: all 0.3s ease;
}

.tabbar-item:active {
  transform: scale(0.95);
}

.icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.icon-wrap.bounce {
  transform: scale(1.15);
  background: rgba(32, 178, 170, 0.12);
  animation: bounceIn 0.3s ease;
}

@keyframes bounceIn {
  0% {
    transform: scale(0.9);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1.15);
  }
}

.tabbar-text {
  font-size: 20rpx;
  margin-top: 6rpx;
  font-weight: 400;
  line-height: 1;
  transition: all 0.3s ease;
  color: #9CA3AF;
}

.tabbar-item.active .tabbar-text {
  color: #20B2AA;
  font-weight: 500;
}
</style>
