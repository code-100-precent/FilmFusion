<template>
  <view class="help-page">
    <NavBar title="帮助中心" :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y>
      <view class="help-section">
        <view class="section-title">常见问题</view>
        
        <view class="help-item">
          <view class="help-question">
            <uni-icons type="help" size="20" color="#6366f1"></uni-icons>
            <text>如何报备影视剧拍摄？</text>
          </view>
          <view class="help-answer">
            <text>在个人中心点击"剧组报备"，填写完整的影视剧信息，包括剧名、类型、集数、投资金额、主创人员、制片单位、拍摄时间、剧组规模、联系方式等信息，提交后等待审核。</text>
          </view>
        </view>

        <view class="help-item">
          <view class="help-question">
            <uni-icons type="help" size="20" color="#6366f1"></uni-icons>
            <text>如何查找拍摄场地？</text>
          </view>
          <view class="help-answer">
            <text>在首页或"拍摄场地"页面，可以通过搜索功能查找场地。支持按场地名称、类型、地址等关键词搜索。点击场地卡片可查看详细信息，包括联系方式、价格等。</text>
          </view>
        </view>

        <view class="help-item">
          <view class="help-question">
            <uni-icons type="help" size="20" color="#6366f1"></uni-icons>
            <text>如何联系协拍服务？</text>
          </view>
          <view class="help-answer">
            <text>在"协拍服务"页面浏览可用的服务项目，点击服务卡片查看详细信息，包括服务描述、联系方式、价格等。您可以直接联系服务提供方进行咨询和预约。</text>
          </view>
        </view>

        <view class="help-item">
          <view class="help-question">
            <uni-icons type="help" size="20" color="#6366f1"></uni-icons>
            <text>如何修改个人信息？</text>
          </view>
          <view class="help-answer">
            <text>在个人中心点击"个人信息"，可以修改用户名。注意：手机号不可修改，如需更换手机号，请联系客服处理。</text>
          </view>
        </view>

        <view class="help-item">
          <view class="help-question">
            <uni-icons type="help" size="20" color="#6366f1"></uni-icons>
            <text>如何查看我的报备记录？</text>
          </view>
          <view class="help-answer">
            <text>在个人中心点击"我的报备"，可以查看您提交的所有报备记录，包括报备状态、提交时间等信息。</text>
          </view>
        </view>

        <view class="help-item">
          <view class="help-question">
            <uni-icons type="help" size="20" color="#6366f1"></uni-icons>
            <text>如何提交意见反馈？</text>
          </view>
          <view class="help-answer">
            <text>在个人中心点击"意见反馈"，选择反馈类型（功能建议、问题反馈、使用咨询、其他），填写详细的反馈内容（至少10个字符），提交后我们会在1-3个工作日内处理。</text>
          </view>
        </view>
      </view>

      <view class="help-section">
        <view class="section-title">使用指南</view>
        
        <view class="guide-item">
          <text class="guide-title">1. 注册登录</text>
          <text class="guide-content">首次使用需要注册账号，填写用户名、手机号和密码。注册成功后自动登录，后续可直接使用手机号或用户名登录。</text>
        </view>

        <view class="guide-item">
          <text class="guide-title">2. 浏览资讯</text>
          <text class="guide-content">在首页"最新资讯"区域可以浏览最新的影视行业资讯，点击"更多"查看全部资讯，点击资讯卡片查看详细内容。</text>
        </view>

        <view class="guide-item">
          <text class="guide-title">3. 查找场地</text>
          <text class="guide-content">在"拍摄场地"页面，可以通过搜索功能快速查找需要的场地。支持按名称、类型、地址等关键词搜索，查看场地详情后可直接联系场地提供方。</text>
        </view>

        <view class="guide-item">
          <text class="guide-title">4. 预约服务</text>
          <text class="guide-content">在"协拍服务"页面浏览各类协拍服务，包括设备租赁、人员服务、后期制作等。查看服务详情后，可直接联系服务提供方进行预约。</text>
        </view>

        <view class="guide-item">
          <text class="guide-title">5. 提交报备</text>
          <text class="guide-content">在"剧组报备"页面填写完整的影视剧信息，确保信息准确无误。提交后系统会进行审核，审核结果会通过站内消息或短信通知您。</text>
        </view>
      </view>

      <view class="help-section">
        <view class="section-title">联系我们</view>
        <view class="contact-info">
          <view class="contact-item">
            <uni-icons type="phone" size="20" color="#6366f1"></uni-icons>
            <text>客服热线：400-888-8888</text>
          </view>
          <view class="contact-item">
            <uni-icons type="email" size="20" color="#6366f1"></uni-icons>
            <text>邮箱：service@yaanfilm.com</text>
          </view>
          <view class="contact-item">
            <uni-icons type="location" size="20" color="#6366f1"></uni-icons>
            <text>地址：四川省雅安市雨城区</text>
          </view>
          <view class="contact-item">
            <uni-icons type="clock" size="20" color="#6366f1"></uni-icons>
            <text>服务时间：周一至周五 9:00-18:00</text>
          </view>
        </view>
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
.help-page {
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

.help-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #e5e7eb;
}

.help-item {
  margin-bottom: 32rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.help-question {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16rpx;
}

.help-answer {
  padding-left: 32rpx;
  font-size: 26rpx;
  line-height: 1.8;
  color: #6b7280;
}

.guide-item {
  margin-bottom: 32rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.guide-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12rpx;
}

.guide-content {
  display: block;
  font-size: 26rpx;
  line-height: 1.8;
  color: #6b7280;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
  color: #374151;
}
</style>

