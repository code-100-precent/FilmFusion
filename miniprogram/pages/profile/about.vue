<template>
  <view class="about-page">
    <NavBar title="关于我们" :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y>
      <view class="about-section">
        <view class="logo-section">
          <view class="logo">
            <uni-icons type="film" size="60" color="#6366f1"></uni-icons>
          </view>
          <text class="app-name">雅安影视服务</text>
          <text class="app-version">版本 1.0.0</text>
        </view>
      </view>

      <view class="about-section">
        <view class="section-title">平台简介</view>
        <text class="section-content">
          雅安影视服务是专业的影视拍摄一站式服务平台，致力于为影视制作团队提供全方位的服务支持。
          平台整合了雅安市优质的拍摄场地资源、协拍服务资源，为来雅拍摄的剧组提供便捷、高效的服务。
          我们致力于推动雅安市影视产业发展，打造西南地区重要的影视拍摄基地和影视产业集聚区。
        </text>
      </view>

      <view class="about-section">
        <view class="section-title">核心功能</view>
        <view class="feature-list">
          <view class="feature-item">
            <uni-icons type="location" size="24" color="#6366f1"></uni-icons>
            <view class="feature-content">
              <text class="feature-title">拍摄场地</text>
              <text class="feature-desc">提供丰富的拍摄场地资源，包括自然风光、古镇建筑、现代场景等，满足各类影视拍摄需求</text>
            </view>
          </view>
          <view class="feature-item">
            <uni-icons type="settings" size="24" color="#6366f1"></uni-icons>
            <view class="feature-content">
              <text class="feature-title">协拍服务</text>
              <text class="feature-desc">整合设备租赁、人员服务、后期制作等各类协拍服务，为剧组提供一站式解决方案</text>
            </view>
          </view>
          <view class="feature-item">
            <uni-icons type="list" size="24" color="#6366f1"></uni-icons>
            <view class="feature-content">
              <text class="feature-title">剧组报备</text>
              <text class="feature-desc">便捷的在线报备系统，简化报备流程，提高报备效率，确保拍摄活动规范有序</text>
            </view>
          </view>
          <view class="feature-item">
            <uni-icons type="info" size="24" color="#6366f1"></uni-icons>
            <view class="feature-content">
              <text class="feature-title">影视资讯</text>
              <text class="feature-desc">及时发布影视行业动态、政策信息、拍摄基地新闻等，帮助用户了解行业最新动态</text>
            </view>
          </view>
        </view>
      </view>

      <view class="about-section">
        <view class="section-title">服务优势</view>
        <view class="advantage-list">
          <view class="advantage-item">
            <text class="advantage-title">资源丰富</text>
            <text class="advantage-desc">整合雅安市优质拍摄资源，涵盖各类场景和服务</text>
          </view>
          <view class="advantage-item">
            <text class="advantage-title">服务专业</text>
            <text class="advantage-desc">专业的服务团队，为剧组提供全方位支持</text>
          </view>
          <view class="advantage-item">
            <text class="advantage-title">流程便捷</text>
            <text class="advantage-desc">在线报备、在线预约，简化操作流程</text>
          </view>
          <view class="advantage-item">
            <text class="advantage-title">政策支持</text>
            <text class="advantage-desc">享受雅安市影视产业扶持政策，降低拍摄成本</text>
          </view>
        </view>
      </view>

      <view class="about-section">
        <view class="section-title">发展愿景</view>
        <text class="section-content">
          我们将继续完善平台功能，拓展服务范围，提升服务质量，致力于将雅安打造成为
          西南地区重要的影视拍摄基地和影视产业集聚区。我们期待与更多影视制作团队合作，
          共同推动中国影视产业的发展。
        </text>
      </view>

      <view class="about-section">
        <view class="section-title">版权信息</view>
        <text class="section-content">
          © 2024 雅安影视服务 版权所有
        </text>
        <text class="section-content" style="margin-top: 16rpx;">
          本平台所有内容，包括但不限于文字、图片、音频、视频等，均受版权保护。
          未经授权，不得以任何形式复制、传播或使用。
        </text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'

export default {
  components: {
    NavBar
  }
}
</script>

<style lang="scss" scoped>
.about-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  width: 100%;
  height: calc(100vh - 132rpx);
  padding: 24rpx 32rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.about-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0;
}

.logo {
  width: 120rpx;
  height: 120rpx;
  background: #eef2ff;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.app-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12rpx;
}

.app-version {
  font-size: 26rpx;
  color: #9ca3af;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #e5e7eb;
}

.section-content {
  display: block;
  font-size: 28rpx;
  line-height: 1.8;
  color: #374151;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.feature-item {
  display: flex;
  gap: 16rpx;
}

.feature-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.feature-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
}

.feature-desc {
  font-size: 26rpx;
  line-height: 1.6;
  color: #6b7280;
}

.advantage-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.advantage-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.advantage-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #6366f1;
}

.advantage-desc {
  font-size: 26rpx;
  line-height: 1.6;
  color: #6b7280;
}
</style>

