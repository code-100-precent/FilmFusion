<template>
  <view class="page-shell" :style="{ background: background }">
    <NavBar
      v-if="showNav"
      :title="title"
      :show-back="showBack"
      :background="navBackground"
      :shadow="navShadow"
      @back="$emit('back')"
    >
      <template #right>
        <slot name="nav-right" />
      </template>
    </NavBar>

    <scroll-view
      v-if="scrollable"
      scroll-y
      class="page-shell__scroll"
      :style="scrollStyle"
      :refresher-enabled="false"
      :lower-threshold="lowerThreshold"
      @scrolltolower="$emit('scrollToLower')"
      @scroll="onScroll"
    >
      <view class="page-shell__slot" :style="slotStyle">
        <slot />
      </view>
    </scroll-view>

    <view v-else class="page-shell__static" :style="slotStyle">
      <slot />
    </view>

    <TabBar
      v-if="showTab"
      :active="tabKey"
      :items="tabItems"
      @change="$emit('tabChange', $event)"
    />
  </view>
</template>

<script>
import NavBar from '../NavBar/NavBar.vue'
import TabBar from '../TabBar/TabBar.vue'

export default {
  name: 'PageShell',
  components: {
    NavBar,
    TabBar
  },
  props: {
    title: {
      type: String,
      default: ''
    },
    showBack: {
      type: Boolean,
      default: true
    },
    showNav: {
      type: Boolean,
      default: true
    },
    showTab: {
      type: Boolean,
      default: false
    },
    tabKey: {
      type: String,
      default: ''
    },
    tabItems: {
      type: Array,
      default: () => []
    },
    paddingTop: {
      type: String,
      default: '40rpx'
    },
    paddingX: {
      type: String,
      default: '32rpx'
    },
    paddingBottom: {
      type: String,
      default: '40rpx'
    },
    background: {
      type: String,
      default: 'linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%)'
    },
    navBackground: {
      type: String,
      default: 'rgba(255,255,255,0.9)'
    },
    navShadow: {
      type: Boolean,
      default: true
    },
    scrollable: {
      type: Boolean,
      default: true
    },
    lowerThreshold: {
      type: Number,
      default: 120
    }
  },
  computed: {
    bottomPadding() {
      return this.showTab
        ? `calc(${this.paddingBottom} + 96rpx + env(safe-area-inset-bottom))`
        : this.paddingBottom
    },
    slotStyle() {
      return {
        padding: `${this.paddingTop} ${this.paddingX} ${this.bottomPadding}`,
        background: this.background
      }
    },
    scrollStyle() {
      return {
        background: this.background
      }
    }
  },
  methods: {
    onScroll(e) {
      // 通知NavBar滚动状态
      if (e && e.detail && typeof uni !== 'undefined' && uni.$emit) {
        uni.$emit('pageScroll', e.detail.scrollTop || 0)
      }
    }
  }
}
</script>

<style scoped lang="scss">
.page-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  box-sizing: border-box;
}

.page-shell__scroll {
  flex: 1;
  height: 100%;
  transition: opacity 0.3s ease;
}

.page-shell__slot,
.page-shell__static {
  box-sizing: border-box;
  min-height: 100%;
  transition: opacity 0.3s ease;
}
</style>

