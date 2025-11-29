<template>
  <view class="layout-container">
    <!-- 自定义导航栏 -->
    <slot name="navbar"></slot>
    
    <!-- 主要内容区域 -->
    <view class="content-container">
      <!-- 加载中状态 -->
      <view v-if="loading" class="loading-overlay">
        <view class="loading-content">
          <Loading :text="loadingText"></Loading>
        </view>
      </view>
      
      <!-- 页面内容 -->
      <slot></slot>
    </view>
    
    <!-- 底部导航栏 -->
    <slot name="tabbar"></slot>
  </view>
</template>

<script>
import Loading from '../Loading/Loading.vue'

export default {
  name: 'Layout',
  components: {
    Loading
  },
  props: {
    loading: {
      type: Boolean,
      default: false
    },
    loadingText: {
      type: String,
      default: '正在加载中'
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  min-height: 100vh;
  width: 100%;
  position: relative;
}

.content-container {
  position: relative;
  min-height: calc(100vh - 132rpx - 100rpx); /* 进一步调整，减去顶部导航栏和底部导航栏高度 */
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(4rpx);
}

.loading-content {
  padding: 40rpx;
  border-radius: 20rpx;
  background-color: rgba(255, 255, 255, 0.95);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}
</style>