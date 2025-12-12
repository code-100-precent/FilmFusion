<template>
  <view class="filing-page">
    <NavBar :show-back="true"></NavBar>

    <scroll-view class="content" scroll-y>
      <!-- 说明卡片 -->
      <view class="intro-card">
        <view class="intro-title">在线剧组报备</view>
        <view class="intro-desc">提交剧组信息、拍摄计划与资料，工作人员将在1个工作日内联系确认</view>
      </view>

      <!-- 报备表单 -->
      <view class="form-card">
        <view class="form-section">
          <view class="section-title">基本信息</view>
          <view class="form-item">
            <text class="label required">影视剧名称</text>
            <input
              v-model="form.name"
              class="input"
              type="text"
              placeholder="请输入影视剧名称"
              maxlength="50"
            />
          </view>
          <view class="form-row">
            <view class="form-item form-item--half">
              <text class="label required">类型</text>
              <picker
                mode="selector"
                :range="typeOptions"
                :value="typeIndex"
                @change="onTypeChange"
              >
                <view class="picker">{{ form.type || '请选择类型' }}</view>
              </picker>
            </view>
            <view class="form-item form-item--half">
              <text class="label required">题材</text>
              <picker
                mode="selector"
                :range="genreOptions"
                :value="genreIndex"
                @change="onGenreChange"
              >
                <view class="picker">{{ form.genre || '请选择题材' }}</view>
              </picker>
            </view>
          </view>
          <view class="form-row">
            <view class="form-item form-item--half">
              <text class="label required">集数</text>
              <input
                v-model.number="form.episodes"
                class="input"
                type="number"
                placeholder="请输入集数"
              />
            </view>
            <view class="form-item form-item--half">
              <text class="label required">投资金额（万元）</text>
              <input
                v-model.number="form.investAmount"
                class="input"
                type="digit"
                placeholder="请输入投资金额"
              />
            </view>
          </view>
        </view>

        <view class="form-section">
          <view class="section-title">拍摄计划</view>
          <view class="form-row">
            <view class="form-item form-item--half">
              <text class="label required">拍摄开始日期</text>
              <picker mode="date" :value="form.startDate" @change="onDateChange($event, 'startDate')">
                <view class="picker">{{ form.startDate || '请选择日期' }}</view>
              </picker>
            </view>
            <view class="form-item form-item--half">
              <text class="label required">拍摄结束日期</text>
              <picker mode="date" :value="form.endDate" @change="onDateChange($event, 'endDate')">
                <view class="picker">{{ form.endDate || '请选择日期' }}</view>
              </picker>
            </view>
          </view>
          <view class="form-item">
            <text class="label required">剧组规模</text>
            <picker
              mode="selector"
              :range="crewScaleOptions"
              :value="crewScaleIndex"
              @change="onCrewScaleChange"
            >
              <view class="picker">{{ form.crewScale || '请选择剧组规模' }}</view>
            </picker>
          </view>
        </view>

        <view class="form-section">
          <view class="section-title">出品信息</view>
          <view class="form-item">
            <text class="label required">主创人员</text>
            <input
              v-model="form.mainCreators"
              class="input"
              type="text"
              placeholder="请输入主创人员（导演、编剧等）"
              maxlength="255"
            />
          </view>
          <view class="form-item">
            <text class="label required">第一出品单位</text>
            <input
              v-model="form.leadProducer"
              class="input"
              type="text"
              placeholder="请输入第一出品单位"
              maxlength="100"
            />
          </view>
          <view class="form-item">
            <text class="label required">制片单位</text>
            <input
              v-model="form.producerUnit"
              class="input"
              type="text"
              placeholder="请输入制片单位"
              maxlength="100"
            />
          </view>
        </view>

        <view class="form-section">
          <view class="section-title">联系信息</view>
          <view class="form-item">
            <text class="label required">联系人</text>
            <input
              v-model="form.contact"
              class="input"
              type="text"
              placeholder="请输入联系人姓名"
              maxlength="50"
            />
          </view>
          <view class="form-item">
            <text class="label required">联系电话</text>
            <input
              v-model="form.phoneNumber"
              class="input"
              type="number"
              placeholder="请输入联系电话"
              maxlength="20"
            />
          </view>
          <view class="form-item">
            <text class="label required">剧组职务</text>
            <input
              v-model="form.crewPosition"
              class="input"
              type="text"
              placeholder="请输入剧组职务（如：导演、制片人等）"
              maxlength="50"
            />
          </view>
        </view>
      </view>

      <view class="form-card">
        <view class="form-section">
          <view class="section-title">相关材料</view>
          
          <view class="form-item">
            <text class="label required">影视拍摄许可证</text>
            <view class="upload-area" @click="uploadFile('permit')">
              <view v-if="!form.shootPermit" class="upload-placeholder">
                <uni-icons type="upload" size="24" color="#6366f1"></uni-icons>
                <text>点击上传</text>
              </view>
              <view v-else class="file-preview">
                <view class="file-info" @click.stop="previewFile(form.shootPermit, '影视拍摄许可证')">
                  <uni-icons type="paperclip" size="20" color="#6366f1"></uni-icons>
                  <text class="file-name">已上传文件</text>
                </view>
                <view class="file-actions">
                  <view @click.stop="previewFile(form.shootPermit, '影视拍摄许可证')" style="padding: 8rpx;">
                    <uni-icons type="eye" size="20" color="#6366f1"></uni-icons>
                  </view>
                  <view @click.stop="removeFile('permit')" style="padding: 8rpx;">
                    <uni-icons type="close" size="20" color="#ef4444"></uni-icons>
                  </view>
                </view>
              </view>
            </view>
          </view>
          
          <view class="form-item">
            <text class="label required">立项审批文件</text>
            <view class="upload-area" @click="uploadFile('approval')">
              <view v-if="!form.approvalFile" class="upload-placeholder">
                <uni-icons type="upload" size="24" color="#6366f1"></uni-icons>
                <text>点击上传</text>
              </view>
              <view v-else class="file-preview">
                <view class="file-info" @click.stop="previewFile(form.approvalFile, '立项审批文件')">
                  <uni-icons type="paperclip" size="20" color="#6366f1"></uni-icons>
                  <text class="file-name">已上传文件</text>
                </view>
                <view class="file-actions">
                  <view @click.stop="previewFile(form.approvalFile, '立项审批文件')" style="padding: 8rpx;">
                    <uni-icons type="eye" size="20" color="#6366f1"></uni-icons>
                  </view>
                  <view @click.stop="removeFile('approval')" style="padding: 8rpx;">
                    <uni-icons type="close" size="20" color="#ef4444"></uni-icons>
                  </view>
                </view>
              </view>
            </view>
          </view>
          
          <view class="form-item">
            <text class="label">协拍服务申请表</text>
            <view class="upload-area" @click="uploadFile('application')">
              <view v-if="!form.shootApply" class="upload-placeholder">
                <uni-icons type="upload" size="24" color="#6366f1"></uni-icons>
                <text>点击上传</text>
              </view>
              <view v-else class="file-preview">
                <view class="file-info" @click.stop="previewFile(form.shootApply, '协拍服务申请表')">
                  <uni-icons type="paperclip" size="20" color="#6366f1"></uni-icons>
                  <text class="file-name">已上传文件</text>
                </view>
                <view class="file-actions">
                  <view @click.stop="previewFile(form.shootApply, '协拍服务申请表')" style="padding: 8rpx;">
                    <uni-icons type="eye" size="20" color="#6366f1"></uni-icons>
                  </view>
                  <view @click.stop="removeFile('application')" style="padding: 8rpx;">
                    <uni-icons type="close" size="20" color="#ef4444"></uni-icons>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button
          class="submit-btn"
          :class="{ 'submit-btn--disabled': !canSubmit || submitting }"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : (reportId ? '更新报备' : '提交报备') }}
        </button>
        <text class="submit-tip">提交后，工作人员将在1个工作日内联系您</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import NavBar from '../../components/NavBar/NavBar.vue'
// 使用真实后端API
import { createReport, updateReport, getReportById, uploadFile as apiUploadFile } from '../../services/backend-api'
import { mapGetters } from 'vuex'
// 导入文件URL处理函数
import { getFileUrl } from '../../utils'

export default {
  components: {
    NavBar
  },
  data() {
    return {
      submitting: false,
      reportId: '',
      typeOptions: ['电视剧', '电影', '网络剧', '纪录片', '其他'],
      genreOptions: ['历史', '现代', '古装', '悬疑', '爱情', '喜剧', '动作', '科幻', '文化', '其他'],
      crewScaleOptions: ['小型', '中型', '大型'],
      typeIndex: -1,
      genreIndex: -1,
      crewScaleIndex: -1,
      form: {
        name: '',
        type: '',
        genre: '',
        episodes: null,
        investAmount: null,
        mainCreators: '',
        leadProducer: '',
        producerUnit: '',
        startDate: '',
        endDate: '',
        crewScale: '',
        contact: '',
        phoneNumber: '',
        crewPosition: '',
        // 使用后端期望的字段名
        shootPermit: '',
        thumbShootPermit: '',
        approvalFile: '',
        thumbApprovalFile: '',
        shootApply: '',
        thumbShootApply: ''
      }
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn']),
    canSubmit() {
      return (
        this.form.name &&
        this.form.type &&
        this.form.genre &&
        this.form.episodes &&
        this.form.investAmount &&
        this.form.mainCreators &&
        this.form.leadProducer &&
        this.form.producerUnit &&
        this.form.startDate &&
        this.form.endDate &&
        this.form.crewScale &&
        this.form.contact &&
        this.form.phoneNumber &&
        this.form.crewPosition &&
        this.form.shootPermit &&
        this.form.approvalFile
      )
    }
  },
  onLoad(options) {
    if (!this.isLoggedIn) {
      uni.showModal({
        title: '提示',
        content: '请先登录',
        showCancel: false,
        success: () => {
          uni.navigateTo({
            url: '/pages/login/login'
          })
        }
      })
      return
    }
    
    // 如果传入了报备ID，则加载报备详情
    if (options.id) {
      this.reportId = options.id
      this.loadReportDetail()
    }
  },
  
  methods: {
    async loadReportDetail() {
      try {
        uni.showLoading({ title: '加载中...' })
        const res = await getReportById(this.reportId)
        
        if (res.code === 200 && res.data) {
          const reportData = res.data
          this.form = {
            name: reportData.name || '',
            type: reportData.type || '',
            genre: reportData.genre || '',
            episodes: reportData.episodes || null,
            investAmount: reportData.investAmount || null,
            mainCreators: reportData.mainCreators || '',
            leadProducer: reportData.leadProducer || '',
            producerUnit: reportData.producerUnit || '',
            startDate: reportData.startDate || '',
            endDate: reportData.endDate || '',
            crewScale: reportData.crewScale || '',
            contact: reportData.contact || '',
            phoneNumber: reportData.phoneNumber || '',
            crewPosition: reportData.crewPosition || '',
            // 使用后端返回的字段名
            shootPermit: reportData.shootPermit || '',
            thumbShootPermit: reportData.thumbShootPermit || '',
            approvalFile: reportData.approvalFile || '',
            thumbApprovalFile: reportData.thumbApprovalFile || '',
            shootApply: reportData.shootApply || '',
            thumbShootApply: reportData.thumbShootApply || ''
          }
          
          // 设置选项索引
          this.typeIndex = this.typeOptions.indexOf(this.form.type)
          this.genreIndex = this.genreOptions.indexOf(this.form.genre)
          this.crewScaleIndex = this.crewScaleOptions.indexOf(this.form.crewScale)
        } else {
          uni.showToast({ title: '加载失败', icon: 'none' })
          uni.navigateBack()
        }
      } catch (error) {
        console.error('加载报备详情失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
        uni.navigateBack()
      } finally {
        uni.hideLoading()
      }
    },
    onTypeChange(e) {
      this.typeIndex = e.detail.value
      this.form.type = this.typeOptions[e.detail.value]
    },
    onGenreChange(e) {
      this.genreIndex = e.detail.value
      this.form.genre = this.genreOptions[e.detail.value]
    },
    onCrewScaleChange(e) {
      this.crewScaleIndex = e.detail.value
      this.form.crewScale = this.crewScaleOptions[e.detail.value]
    },
    
    onDateChange(e, field) {
      this.form[field] = e.detail.value
    },
    
    async uploadFile(field) {
      console.log('开始处理文件上传，字段:', field)
      
      if (!this.isLoggedIn) {
        console.log('用户未登录')
        uni.showModal({
          title: '提示',
          content: '请先登录',
          showCancel: false,
          success: () => {
            uni.navigateTo({
              url: '/pages/login/login'
            })
          }
        })
        return
      }
      
      // 显示选择方式弹窗
      uni.showActionSheet({
        itemList: ['从相册选择图片', '从文件选择'],
        success: async (res) => {
          console.log('选择了上传方式:', res.tapIndex)
          
          try {
            if (res.tapIndex === 0) {
              // 从相册选择图片
              console.log('1. 开始从相册选择图片...')
              const imageResult = await uni.chooseImage({
                count: 1,
                sizeType: ['compressed'], // 选择压缩图片
                sourceType: ['album', 'camera'], // 可以从相册或相机选择
                extension: ['jpg', 'jpeg', 'png']
              })
              
              console.log('2. 图片选择结果:', JSON.stringify(imageResult))
              
              if (!imageResult.tempFilePaths || imageResult.tempFilePaths.length === 0) {
                console.log('未选择图片')
                return
              }
              
              // 使用选择的第一张图片
              const tempFilePath = imageResult.tempFilePaths[0]
              const fileName = `image_${Date.now()}.${tempFilePath.split('.').pop()}`
              
              console.log('3. 准备上传图片:', fileName)
              
              // 调用上传方法
              await this.processFileUpload(tempFilePath, fileName, field)
              
            } else {
              // 从文件选择
              console.log('1. 开始选择文件...')
              const fileResult = await uni.chooseMessageFile({
                count: 1,
                type: 'file',
                extension: ['pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png'],
                showFileExtension: true
              })
              
              console.log('2. 文件选择结果:', JSON.stringify(fileResult))
              
              // 兼容两种返回格式：tempFilePaths数组 或 tempFiles数组
              let filePath = null
              let fileName = null
              
              // 检查是否有tempFilePaths数组
              if (fileResult && fileResult.tempFilePaths && fileResult.tempFilePaths.length > 0) {
                filePath = fileResult.tempFilePaths[0]
                fileName = `file_${Date.now()}.${filePath.split('.').pop()}`
                console.log('3. 选择的文件路径:', filePath)
              } else if (fileResult && fileResult.tempFiles && fileResult.tempFiles.length > 0) {
                // 从tempFiles数组中提取路径和文件名
                filePath = fileResult.tempFiles[0].path
                fileName = fileResult.tempFiles[0].name || `file_${Date.now()}.${filePath.split('.').pop()}`
                console.log('3. 从tempFiles中提取的文件路径:', filePath)
                console.log('3.1 文件名称:', fileName)
                console.log('3.2 文件大小:', fileResult.tempFiles[0].size)
              }
              
              if (filePath) {
                // 调用上传方法
                await this.processFileUpload(filePath, fileName, field)
              }
            }
          } catch (error) {
            console.error('文件选择或上传失败:', error)
            uni.showToast({
              title: '上传失败，请重试',
              icon: 'none'
            })
          }
        },
        fail: (err) => {
          console.log('取消选择文件')
        }
      })
    },
    
    // 处理文件上传的通用方法
    async processFileUpload(filePath, fileName, field) {
      console.log('开始处理文件上传:', filePath, fileName, field)
      
      try {
        uni.showLoading({ title: '上传中...' })
        
        console.log('4. 开始调用上传API...')
        console.log('4.1 上传目标URL:', 'http://162.14.106.139:8080/api/file')
        
        // 记录开始时间用于监控响应延迟
        const startTime = Date.now()
        // 调用后端API上传文件
        const response = await apiUploadFile(filePath)
        const endTime = Date.now()
        
        console.log(`5. API调用完成，响应 (耗时: ${endTime - startTime}ms):`, JSON.stringify(response))
        
        // 处理API响应
        if (response) {
          // 支持code为200或0的成功状态
          const isSuccess = response.code === 200 || response.code === 0
          
          if (isSuccess) {
            // 确保文件URL是字符串格式
            let fileUrl = response.data
            if (typeof fileUrl === 'object') {
              fileUrl = fileUrl.url || fileUrl.filename || fileUrl.path || JSON.stringify(fileUrl)
            }
            fileUrl = String(fileUrl)
            
            // 根据field类型设置对应的后端字段
            if (field === 'permit') {
              this.form.shootPermit = fileUrl
              this.form.thumbShootPermit = fileUrl
            } else if (field === 'approval') {
              this.form.approvalFile = fileUrl
              this.form.thumbApprovalFile = fileUrl
            } else if (field === 'application') {
              this.form.shootApply = fileUrl
              this.form.thumbShootApply = fileUrl
            }
            
            console.log(`6. 文件上传成功，文件名:`, fileUrl)
            console.log(`6.1 文件存储路径:`, fileUrl)
            uni.showToast({ 
              title: '上传成功', 
              icon: 'success' 
            })
          } else {
            const errorMsg = response.message || response.msg || '上传失败，请重试'
            console.error('7. API返回错误:', errorMsg, '完整响应:', JSON.stringify(response))
            uni.showToast({ 
              title: errorMsg, 
              icon: 'none',
              duration: 3000
            })
          }
        } else {
          console.error('7. API未返回响应')
          uni.showToast({ 
            title: '服务器未响应', 
            icon: 'none',
            duration: 3000
          })
        }
      } catch (apiError) {
        console.error('7. API调用异常:', apiError, '错误堆栈:', apiError?.stack || '无堆栈')
        
        // 详细的错误类型判断
        let errorTitle = '上传请求失败'
        if (apiError?.errMsg?.includes('request:fail')) {
          errorTitle = '网络请求失败，请检查网络连接'
        } else if (apiError?.errMsg?.includes('timeout')) {
          errorTitle = '请求超时，请重试'
        } else if (apiError?.code === 401) {
          errorTitle = '登录已过期，请重新登录'
        } else if (apiError?.code === 500) {
          errorTitle = '服务器错误，请稍后重试'
        }
        
        uni.showToast({ 
          title: errorTitle, 
          icon: 'none',
          duration: 3000
        })
      } finally {
        // 安全地隐藏loading，避免"toast can't be found"错误
        try {
          uni.hideLoading()
        } catch (e) {
          console.warn('隐藏loading时出错:', e)
        }
      }
    },
    
    removeFile(field) {
      // 显示确认对话框
      uni.showModal({
        title: '确认删除',
        content: '确定要移除该文件吗？',
        success: (res) => {
          if (res.confirm) {
            // 根据field类型清空对应的后端字段
            if (field === 'permit') {
              this.form.shootPermit = ''
              this.form.thumbShootPermit = ''
            } else if (field === 'approval') {
              this.form.approvalFile = ''
              this.form.thumbApprovalFile = ''
            } else if (field === 'application') {
              this.form.shootApply = ''
              this.form.thumbShootApply = ''
            }
            uni.showToast({ 
              title: '文件已移除', 
              icon: 'success',
              duration: 1500
            })
          }
        }
      })
    },
    
    // 预览文件
    previewFile(fileUrl, fileName) {
      console.log('预览文件被点击:', fileUrl, fileName)
      
      if (!fileUrl) {
        uni.showToast({
          title: '文件不存在',
          icon: 'none'
        })
        return
      }
      
      // 处理JSON格式的文件URL
      let actualFileUrl = fileUrl
      try {
        // 尝试解析JSON格式的URL
        if (fileUrl.includes('originUrl')) {
          // 处理可能被反引号包裹的JSON字符串
          let jsonString = fileUrl
          
          // 去除可能的前缀和后缀
          if (fileUrl.startsWith('`')) {
            jsonString = fileUrl.replace(/^`+/, '').replace(/`+$/, '') // 去掉首尾的所有反引号
          }
          
          // 处理可能包含的URL前缀
          if (jsonString.includes('http://') || jsonString.includes('https://')) {
            // 提取JSON部分
            const jsonMatch = jsonString.match(/\{.*"originUrl".*\}/)
            if (jsonMatch) {
              jsonString = jsonMatch[0]
            }
          }
          
          console.log('处理前的JSON字符串:', jsonString)
          
          const urlObj = JSON.parse(jsonString)
          actualFileUrl = urlObj.originUrl || urlObj.thumbUrl || fileUrl
          console.log('解析后的实际文件URL:', actualFileUrl)
        }
      } catch (e) {
        console.log('JSON解析失败，使用原始URL，错误信息:', e)
        // 如果解析失败，尝试从字符串中提取originUrl
        try {
          const originUrlMatch = fileUrl.match(/"originUrl"\s*:\s*"([^"]+)"/)
          if (originUrlMatch && originUrlMatch[1]) {
            actualFileUrl = originUrlMatch[1]
            console.log('通过正则提取的originUrl:', actualFileUrl)
          }
        } catch (regexError) {
          console.log('正则提取也失败，使用原始URL:', regexError)
        }
      }
      
      // 处理文件URL
      const processedUrl = getFileUrl(actualFileUrl)
      console.log('处理后的URL:', processedUrl)
      
      // 获取文件扩展名
      const fileExt = actualFileUrl.split('.').pop().toLowerCase()
      console.log('文件扩展名:', fileExt)
      
      // 判断是否为图片文件
      const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
      
      if (imageExts.includes(fileExt)) {
        console.log('是图片文件，开始预览')
        // 图片文件直接预览
        uni.previewImage({
          urls: [processedUrl],
          current: processedUrl,
          success: () => {
            console.log('图片预览成功')
          },
          fail: (err) => {
            console.error('图片预览失败:', err)
            uni.showToast({
              title: '预览失败',
              icon: 'none'
            })
          }
        })
      } else {
        console.log('非图片文件，直接下载')
        // 非图片文件直接下载，传递原始URL以便downloadFile方法处理
        this.downloadFile(fileUrl, fileName)
      }
    },
    
    // 下载文件
    downloadFile(fileUrl, fileName) {
      if (!fileUrl) {
        uni.showToast({
          title: '文件不存在',
          icon: 'none'
        })
        return
      }
      
      // 处理JSON格式的文件URL
      let actualFileUrl = fileUrl
      try {
        // 尝试解析JSON格式的URL
        if (fileUrl.includes('originUrl')) {
          // 处理可能被反引号包裹的JSON字符串
          let jsonString = fileUrl
          
          // 去除可能的前缀和后缀
          if (fileUrl.startsWith('`')) {
            jsonString = fileUrl.replace(/^`+/, '').replace(/`+$/, '') // 去掉首尾的所有反引号
          }
          
          // 处理可能包含的URL前缀
          if (jsonString.includes('http://') || jsonString.includes('https://')) {
            // 提取JSON部分
            const jsonMatch = jsonString.match(/\{.*"originUrl".*\}/)
            if (jsonMatch) {
              jsonString = jsonMatch[0]
            }
          }
          
          console.log('下载文件 - 处理前的JSON字符串:', jsonString)
          
          const urlObj = JSON.parse(jsonString)
          actualFileUrl = urlObj.originUrl || urlObj.thumbUrl || fileUrl
          console.log('下载文件 - 解析后的实际文件URL:', actualFileUrl)
        }
      } catch (e) {
        console.log('下载文件 - JSON解析失败，使用原始URL，错误信息:', e)
        // 如果解析失败，尝试从字符串中提取originUrl
        try {
          const originUrlMatch = fileUrl.match(/"originUrl"\s*:\s*"([^"]+)"/)
          if (originUrlMatch && originUrlMatch[1]) {
            actualFileUrl = originUrlMatch[1]
            console.log('下载文件 - 通过正则提取的originUrl:', actualFileUrl)
          }
        } catch (regexError) {
          console.log('下载文件 - 正则提取也失败，使用原始URL:', regexError)
        }
      }
      
      // 处理文件URL
      const processedUrl = getFileUrl(actualFileUrl)
      console.log('下载文件 - 最终处理后的URL:', processedUrl)
      
      // 显示下载提示
      uni.showLoading({
        title: '下载中...'
      })
      
      // 添加下载进度监听
      const downloadTask = uni.downloadFile({
        url: processedUrl,
        success: (res) => {
          uni.hideLoading()
          if (res.statusCode === 200) {
            // 保存文件到本地
            uni.saveFile({
              tempFilePath: res.tempFilePath,
              success: (saveRes) => {
                uni.showToast({
                  title: '下载成功',
                  icon: 'success'
                })
                console.log('文件保存路径:', saveRes.savedFilePath)
                
                // 提示用户文件已保存，并提供打开选项
                uni.showModal({
                  title: '下载完成',
                  content: '文件已保存到本地，是否立即打开？',
                  confirmText: '打开',
                  cancelText: '稍后',
                  success: (modalRes) => {
                    if (modalRes.confirm) {
                      // 尝试打开文件
                      uni.openDocument({
                        filePath: saveRes.savedFilePath,
                        showMenu: true,
                        success: () => {
                          console.log('文件打开成功')
                        },
                        fail: (err) => {
                          console.error('文件打开失败:', err)
                          uni.showToast({
                            title: '无法打开该文件类型',
                            icon: 'none'
                          })
                        }
                      })
                    }
                  }
                })
              },
              fail: (err) => {
                console.error('文件保存失败:', err)
                uni.showToast({
                  title: '保存失败',
                  icon: 'none'
                })
              }
            })
          } else {
            console.error('下载失败，状态码:', res.statusCode)
            uni.showToast({
              title: '下载失败，请重试',
              icon: 'none'
            })
          }
        },
        fail: (err) => {
          uni.hideLoading()
          console.error('文件下载失败:', err)
          
          // 根据错误类型提供不同的提示
          let errorMsg = '下载失败'
          if (err.errMsg && err.errMsg.includes('fail')) {
            if (err.errMsg.includes('timeout')) {
              errorMsg = '下载超时，请检查网络连接'
            } else if (err.errMsg.includes('size')) {
              errorMsg = '文件过大，无法下载'
            } else {
              errorMsg = '网络错误，请检查网络连接'
            }
          }
          
          uni.showToast({
            title: errorMsg,
            icon: 'none',
            duration: 3000
          })
        }
      })
      
      // 监听下载进度
      downloadTask.onProgressUpdate((res) => {
        console.log('下载进度', res.progress)
        console.log('已经下载的数据长度', res.totalBytesWritten)
        console.log('预期需要下载的数据总长度', res.totalBytesExpectedToWrite)
      })
    },
    
    async handleSubmit() {
      if (!this.isLoggedIn) {
        uni.showModal({
          title: '提示',
          content: '请先登录',
          showCancel: false,
          success: () => {
            uni.navigateTo({
              url: '/pages/login/login'
            })
          }
        })
        return
      }

      if (!this.canSubmit) {
        uni.showToast({
          title: '请完善所有必填信息',
          icon: 'none'
        })
        return
      }

      if (new Date(this.form.startDate) > new Date(this.form.endDate)) {
        uni.showToast({
          title: '结束日期不能早于开始日期',
          icon: 'none'
        })
        return
      }

      // 验证文件上传
      if (!this.form.shootPermit || !this.form.approvalFile) {
        uni.showToast({ title: '请上传必要的文件材料', icon: 'none' })
        return
      }

      this.submitting = true
      try {
        // 准备提交数据，确保数据类型与后端匹配
      const reportData = {
        name: this.form.name,
        type: this.form.type,
        genre: this.form.genre,
        episodes: parseInt(this.form.episodes) || 0,
        investAmount: parseFloat(this.form.investAmount) || 0,
        mainCreators: this.form.mainCreators,
        leadProducer: this.form.leadProducer,
        producerUnit: this.form.producerUnit,
        startDate: this.form.startDate,
        endDate: this.form.endDate,
        crewScale: this.form.crewScale,
        contact: this.form.contact,
        phoneNumber: this.form.phoneNumber,
        crewPosition: this.form.crewPosition,
        status: this.reportId ? undefined : 'PENDING', // 新增报备设置为'PENDING'，编辑时保持原状态
        // 确保文件字段都是字符串类型
        shootPermit: this.ensureString(this.form.shootPermit),
        thumbShootPermit: this.ensureString(this.form.thumbShootPermit),
        approvalFile: this.ensureString(this.form.approvalFile),
        thumbApprovalFile: this.ensureString(this.form.thumbApprovalFile),
        shootApply: this.ensureString(this.form.shootApply),
        thumbShootApply: this.ensureString(this.form.thumbShootApply)
      }
        
        console.log('提交数据:', reportData)
        
        // 根据是否有reportId决定是创建还是更新报备
        let res
        if (this.reportId) {
          // 更新报备
          res = await updateReport(this.reportId, reportData)
        } else {
          // 创建报备
          res = await createReport(reportData)
        }

        if (res.code === 200 || res.code === 0) {
          const isUpdate = !!this.reportId
          uni.showModal({
            title: isUpdate ? '更新成功' : '提交成功',
            content: isUpdate ? '您的报备信息已更新' : '您的报备信息已提交，工作人员将在1个工作日内联系您',
            showCancel: false,
            success: () => {
              if (isUpdate) {
                // 更新成功后返回上一页
                uni.navigateBack()
              } else {
                // 创建成功后重置表单并跳转
                this.resetForm()
                uni.navigateTo({
                  url: '/pages/index/index'
                })
              }
            }
          })
        } else {
          uni.showToast({
            title: res.message || res.msg || '操作失败',
            icon: 'none',
            duration: 2000
          })
        }
      } catch (error) {
        console.error('提交报备失败:', error)
        uni.showToast({
          title: error.message || '提交失败，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      } finally {
        this.submitting = false
      }
    },
    // 确保值为字符串类型
    ensureString(value) {
      if (value === null || value === undefined) {
        return ''
      }
      if (typeof value === 'string') {
        return value
      }
      if (typeof value === 'object') {
        return value.url || value.filename || value.path || JSON.stringify(value)
      }
      return String(value)
    },
    resetForm() {
      this.form = {
        name: '',
        type: '',
        genre: '',
        episodes: null,
        investAmount: null,
        mainCreators: '',
        leadProducer: '',
        producerUnit: '',
        startDate: '',
        endDate: '',
        crewScale: '',
        contact: '',
        phoneNumber: '',
        crewPosition: '',
        // 使用后端期望的字段名
        shootPermit: '',
        thumbShootPermit: '',
        approvalFile: '',
        thumbApprovalFile: '',
        shootApply: '',
        thumbShootApply: ''
      }
      this.typeIndex = -1
      this.genreIndex = -1
      this.crewScaleIndex = -1
    }
  }
}
</script>

<style lang="scss" scoped>
.filing-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 132rpx;
  box-sizing: border-box;
}

.content {
  height: calc(100vh - 132rpx - 100rpx);
  padding: 24rpx;
  padding-bottom: calc(80rpx + env(safe-area-inset-bottom));
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

/* 页面标题样式 */
.page-title {
  padding: 32rpx 0 8rpx 0;
  width: 100%;
}

.title-text {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.intro-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  color: #fff;
  width: 100%;
  box-sizing: border-box;
}

.intro-title {
  font-size: 36rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}

.intro-desc {
  font-size: 26rpx;
  opacity: 0.9;
  line-height: 1.6;
}

.form-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.form-section {
  margin-bottom: 32rpx;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #f3f4f6;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-row {
  display: flex;
  gap: 24rpx;
}

.form-item--half {
  flex: 1;
}

.label {
  display: block;
  font-size: 28rpx;
  color: #374151;
  font-weight: 500;
  margin-bottom: 16rpx;
}

.label.required::after {
  content: ' *';
  color: #ef4444;
}

.input,
.picker {
  width: 100%;
  height: 80rpx;
  background: #f9fafb;
  border: 2rpx solid #e5e7eb;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1f2937;
  box-sizing: border-box;
  margin: 0;
}

.input:focus {
  border-color: #6366f1;
  background: #fff;
}

.picker {
  display: flex;
  align-items: center;
  color: #1f2937;
}

.submit-section {
  padding: 24rpx 0;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 16rpx;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
  margin-bottom: 20rpx;
  box-sizing: border-box;
}

.submit-btn--disabled {
  opacity: 0.6;
  background: #d1d5db;
}

.submit-tip {
  display: block;
  text-align: center;
  font-size: 24rpx;
  color: #9ca3af;
}
.upload-area {
  width: 100%;
  height: 80rpx;
  background: #f9fafb;
  border: 2rpx dashed #cbd5e1;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.upload-area:active {
  background: #f3f4f6;
  border-color: #6366f1;
}

.upload-placeholder {
  display: flex;
  align-items: center;
  gap: 12rpx;
  color: #6366f1;
  font-size: 28rpx;
}

.file-preview {
  width: 100%;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  cursor: pointer;
}

.file-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.file-name {
  font-size: 26rpx;
  color: #374151;
}
</style>
