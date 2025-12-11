<template>
  <view class="test-page">
    <NavBar title="文件预览测试" :show-back="true"></NavBar>
    
    <view class="content">
      <view class="test-section">
        <text class="section-title">测试预览功能</text>
        
        <view class="test-item">
          <text class="test-label">测试图片预览：</text>
          <button class="test-btn" @click="testImagePreview">测试图片预览</button>
        </view>
        
        <view class="test-item">
          <text class="test-label">测试文档预览：</text>
          <button class="test-btn" @click="testDocumentPreview">测试文档预览</button>
        </view>
        
        <view class="test-item">
          <text class="test-label">测试文件下载：</text>
          <button class="test-btn" @click="testFileDownload">测试文件下载</button>
        </view>
        
        <view class="test-item">
          <text class="test-label">检查报备数据：</text>
          <button class="test-btn" @click="checkReportData">检查报备数据</button>
        </view>
      </view>
      
      <view class="result-section" v-if="testResult">
        <text class="section-title">测试结果</text>
        <text class="result-text">{{ testResult }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import NavBar from '@/components/NavBar/NavBar.vue'
import { getFileUrl } from '../../utils'
import { getMyReportPage } from '../../services/backend-api'

export default {
  components: {
    NavBar
  },
  data() {
    return {
      testResult: ''
    }
  },
  methods: {
    testImagePreview() {
      // 测试图片预览功能
      const testImageUrl = '/uploads/test-image.jpg' // 替换为实际存在的图片路径
      
      // 处理文件URL
      const processedUrl = getFileUrl(testImageUrl)
      
      this.testResult = `图片URL处理结果: ${processedUrl}`
      
      // 预览图片
      uni.previewImage({
        urls: [processedUrl],
        current: processedUrl,
        success: () => {
          this.testResult += '\n图片预览成功'
        },
        fail: (err) => {
          this.testResult += `\n图片预览失败: ${JSON.stringify(err)}`
        }
      })
    },
    
    testDocumentPreview() {
      // 测试文档预览功能
      const testDocUrl = '/uploads/test-document.pdf' // 替换为实际存在的文档路径
      
      // 处理文件URL
      const processedUrl = getFileUrl(testDocUrl)
      
      this.testResult = `文档URL处理结果: ${processedUrl}`
      
      // 下载并打开文档
      uni.downloadFile({
        url: processedUrl,
        success: (res) => {
          if (res.statusCode === 200) {
            uni.openDocument({
              filePath: res.tempFilePath,
              showMenu: true,
              success: () => {
                this.testResult += '\n文档打开成功'
              },
              fail: (err) => {
                this.testResult += `\n文档打开失败: ${JSON.stringify(err)}`
              }
            })
          }
        },
        fail: (err) => {
          this.testResult += `\n文档下载失败: ${JSON.stringify(err)}`
        }
      })
    },
    
    testFileDownload() {
      // 测试文件下载功能
      const testFileUrl = '/uploads/test-file.txt' // 替换为实际存在的文件路径
      
      // 处理文件URL
      const processedUrl = getFileUrl(testFileUrl)
      
      this.testResult = `文件URL处理结果: ${processedUrl}`
      
      // 下载文件
      uni.showLoading({
        title: '下载中...'
      })
      
      uni.downloadFile({
        url: processedUrl,
        success: (res) => {
          uni.hideLoading()
          if (res.statusCode === 200) {
            // 保存文件到本地
            uni.saveFile({
              tempFilePath: res.tempFilePath,
              success: (saveRes) => {
                this.testResult += `\n文件下载成功，保存路径: ${saveRes.savedFilePath}`
              },
              fail: (err) => {
                this.testResult += `\n文件保存失败: ${JSON.stringify(err)}`
              }
            })
          }
        },
        fail: (err) => {
          uni.hideLoading()
          this.testResult += `\n文件下载失败: ${JSON.stringify(err)}`
        }
      })
    },
    
    async checkReportData() {
      try {
        // 获取报备数据
        const response = await getMyReportPage({
          current: 1,
          size: 5
        })
        
        if (response.code === 200 && response.data && response.data.length > 0) {
          const report = response.data[0]
          this.testResult = `报备数据检查结果:\n`
          this.testResult += `报备名称: ${report.name}\n`
          this.testResult += `是否有拍摄许可证: ${report.shootPermit ? '是' : '否'}\n`
          this.testResult += `是否有审批文件: ${report.approvalFile ? '是' : '否'}\n`
          this.testResult += `是否有申请表: ${report.shootApply ? '是' : '否'}\n`
          
          if (report.shootPermit) {
            this.testResult += `拍摄许可证URL: ${getFileUrl(report.shootPermit)}\n`
          }
          if (report.approvalFile) {
            this.testResult += `审批文件URL: ${getFileUrl(report.approvalFile)}\n`
          }
          if (report.shootApply) {
            this.testResult += `申请表URL: ${getFileUrl(report.shootApply)}\n`
          }
        } else {
          this.testResult = '没有找到报备数据'
        }
      } catch (error) {
        this.testResult = `检查报备数据失败: ${JSON.stringify(error)}`
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.test-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  padding: 24rpx 32rpx;
}

.test-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24rpx;
  display: block;
}

.test-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 2rpx solid #f3f4f6;
}

.test-item:last-child {
  border-bottom: none;
}

.test-label {
  font-size: 28rpx;
  color: #374151;
  flex: 1;
}

.test-btn {
  background: #6366f1;
  color: #fff;
  font-size: 26rpx;
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
  border: none;
}

.result-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
}

.result-text {
  font-size: 26rpx;
  color: #374151;
  line-height: 40rpx;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>