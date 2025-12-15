<template>
  <div class="location-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="场地名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入场地名称" clearable @keyup.enter="handleSearch" />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleSearch">
              <template #icon>
                <Icon icon="mdi:magnify" />
              </template>
              搜索
            </n-button>
            <n-button @click="handleReset" style="margin-left: 12px">重置</n-button>
          </n-form-item>
        </n-form>
        <div class="action-buttons">
          <n-button type="primary" @click="handleAdd">
            <template #icon>
              <Icon icon="mdi:plus" />
            </template>
            新增场地
          </n-button>
        </div>
      </div>

      <!-- 桌面端表格 -->
      <template v-if="!isMobile">
        <n-data-table
            :columns="columns"
            :data="locationList"
            :loading="loading"
            :row-key="row => row.id"
            :scroll-x="1500"
        />

        <!-- 独立分页组件 -->
        <div class="pagination-container" v-if="pagination.itemCount > 0">
          <n-pagination
              v-model:page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :page-count="Math.ceil(pagination.itemCount / pagination.pageSize)"
              :item-count="pagination.itemCount"
              :page-sizes="pagination.pageSizes"
              show-size-picker
              show-quick-jumper
              @update:page="handlePageChange"
              @update:page-size="handlePageSizeChange"
          />
        </div>
      </template>

      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-list">
        <n-spin :show="loading">
          <div v-if="locationList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:map-marker-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
                v-for="location in locationList"
                :key="location.id"
                class="mobile-card"
                hoverable
            >
              <div class="card-header">
                <div class="location-info">
                  <h3 class="location-name">{{ location.name }}</h3>
                  <p class="location-type">{{ getTypeLabel(location.type) }}</p>
                </div>
                <div class="location-cover">
                  <n-image
                      v-if="location.cover"
                      :src="location.cover"
                      width="80"
                      height="60"
                      object-fit="cover"
                      preview-disabled
                  />
                  <div v-else class="no-cover">
                    <Icon icon="mdi:image" :width="32" />
                  </div>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <span class="label">地址：</span>
                  <span>{{ location.address || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">联系人：</span>
                  <span>{{ location.locationPrincipalName || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">联系电话：</span>
                  <span>{{ location.locationPrincipalPhone || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">价格：</span>
                  <span class="price">{{ location.price ? '¥' + location.price : '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">状态：</span>
                  <span :class="(location.status === true || location.status === 1) ? 'status-available' : 'status-unavailable'">
                    {{ (location.status === true || location.status === 1) ? '可用' : '不可用' }}
                  </span>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(location)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(location.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个场地吗？
                </n-popconfirm>
              </div>
            </n-card>
          </div>

          <!-- 移动端分页 -->
          <div class="mobile-pagination" v-if="pagination.itemCount > 0">
            <n-pagination
                :page="pagination.page"
                :page-size="pagination.pageSize"
                :item-count="pagination.itemCount"
                :page-sizes="[10, 20, 50]"
                show-size-picker
                @update:page="handlePageChange"
                @update:page-size="handlePageSizeChange"
            />
          </div>
        </n-spin>
      </div>
    </n-card>

    <n-modal
        v-model:show="dialogVisible"
        preset="card"
        :title="dialogTitle"
        style="width: 90%; max-width: 900px"
        :mask-closable="false"
    >
      <n-form
          ref="formRef"
          :model="locationForm"
          :rules="formRules"
          :label-placement="isMobile ? 'top' : 'left'"
          :label-width="isMobile ? 'auto' : '120'"
      >
        <n-form-item label="场地名称" path="name">
          <n-input v-model:value="locationForm.name" placeholder="请输入场地名称" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="locationForm.type" :options="typeOptions" placeholder="请选择场地类型" />
        </n-form-item>
        <n-form-item label="地址" path="address">
          <n-input v-model:value="locationForm.address" placeholder="请输入详细地址" />
        </n-form-item>
        <n-form-item label="价格" path="price">
          <n-input-number
              v-model:value="locationForm.price"
              placeholder="请输入价格"
              :min="0"
              :show-button="false"
          >
            <template #prefix>￥</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="封面图片">
          <n-upload
              v-model:file-list="coverFileList"
              list-type="image-card"
              :max="1"
              :custom-request="handleCoverUpload"
              accept="image/*"
          >
            点击上传
          </n-upload>
        </n-form-item>

        <n-form-item label="场地图片">
          <n-upload
              v-model:file-list="imageFileList"
              list-type="image-card"
              :custom-request="handleImageUpload"
              accept="image/*"
              multiple
          >
            点击上传
          </n-upload>
          <div v-if="imageFileList.length > 0" style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap;">
            <!-- 预览区域 -->
          </div>
        </n-form-item>

        <n-form-item label="可用状态" path="status">
          <n-switch v-model:value="locationForm.status" />
        </n-form-item>
        <n-form-item label="场地介绍" path="locationDescription">
          <n-input v-model:value="locationForm.locationDescription" type="textarea" :rows="3" placeholder="请输入场地介绍" />
        </n-form-item>
        <div style="display: flex; gap: 16px;">
          <n-form-item label="场地联系人" path="locationPrincipalName" style="flex: 1;">
            <n-input v-model:value="locationForm.locationPrincipalName" placeholder="姓名" />
          </n-form-item>
          <n-form-item label="联系电话" path="locationPrincipalPhone" style="flex: 1;">
            <n-input v-model:value="locationForm.locationPrincipalPhone" placeholder="电话" />
          </n-form-item>
        </div>
        <div style="display: flex; gap: 16px;">
          <n-form-item label="政府联系人" path="govPrincipalName" style="flex: 1;">
            <n-input v-model:value="locationForm.govPrincipalName" placeholder="姓名" />
          </n-form-item>
          <n-form-item label="联系电话" path="govPrincipalPhone" style="flex: 1;">
            <n-input v-model:value="locationForm.govPrincipalPhone" placeholder="电话" />
          </n-form-item>
        </div>
        <div style="display: flex; gap: 16px;">
          <n-form-item label="经度" path="longitude" style="flex: 1;">
            <n-input v-model:value="locationForm.longitude" placeholder="经度" />
          </n-form-item>
          <n-form-item label="纬度" path="latitude" style="flex: 1;">
            <n-input v-model:value="locationForm.latitude" placeholder="纬度" />
          </n-form-item>
        </div>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px;">
          <n-button @click="dialogVisible = false">取消</n-button>
          <n-button type="primary" @click="handleDialogSave" :loading="dialogLoading">保存</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, h, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NButton,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NSelect,
  NSwitch,
  NDataTable,
  NPopconfirm,
  NModal,
  NImage,
  NSpin,
  NPagination,
  useMessage,
  useDialog
} from 'naive-ui'
import { getLocationPage, addLocation, updateLocation, deleteLocation, getLocationById, uploadFile } from '@/api'
import { getImageUrl } from '@/utils/image'
import { useUserStore } from '@/store/user'
import config from '@/config'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const isMobile = ref(false)
const loading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const locationList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增场地')
const formRef = ref(null)
const coverFileList = ref([])
const imageFileList = ref([])

const searchForm = reactive({
  keyword: ''
})

const locationForm = reactive({
  id: null,
  name: '',
  type: '',
  address: '',
  price: 0,
  status: true,
  locationDescription: '',
  locationPrincipalName: '',
  locationPrincipalPhone: '',
  govPrincipalName: '',
  govPrincipalPhone: '',
  longitude: '',
  latitude: '',
  cover: '',
  image: '',
  thumbCover: '',
  thumbImage: '',
  userId: null
})

const typeOptions = [
  { label: '自然风光', value: 'natural' },
  { label: '历史建筑', value: 'historical' },
  { label: '现代建筑', value: 'modern' },
  { label: '文化场所', value: 'cultural' },
  { label: '商业场所', value: 'commercial' },
  { label: '公园景点', value: 'park' },
  { label: '其他', value: 'other' }
]

const getTypeLabel = (type) => {
  const option = typeOptions.find(opt => opt.value === type)
  return option ? option.label : type
}

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  name: [
    { required: true, message: '请输入场地名称', trigger: 'blur' },
    { min: 1, max: 100, message: '场地名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择场地类型', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入地址', trigger: 'blur' },
    { min: 1, max: 200, message: '地址长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  locationPrincipalPhone: [
    { pattern: /(^1[3-9]\d{9}$)|(^0\d{2,3}-\d{7,8}$)/, message: '请输入正确的手机号或座机号', trigger: 'blur' }
  ],
  govPrincipalPhone: [
    { pattern: /(^1[3-9]\d{9}$)|(^0\d{2,3}-\d{7,8}$)/, message: '请输入正确的手机号或座机号', trigger: 'blur' }
  ],
  longitude: [
    { required: true, message: '请输入经度', trigger: 'blur' },
    {
      validator: (rule, value) => {
        if (!value) return true
        // 格式：数字° E 或 W
        const pattern = /^\d+(\.\d+)?° [EW]$/
        if (!pattern.test(value)) {
          return new Error('格式错误')
        }
        return true
      },
      trigger: 'blur'
    }
  ],
  latitude: [
    { required: true, message: '请输入纬度', trigger: 'blur' },
    {
      validator: (rule, value) => {
        if (!value) return true
        // 格式：数字° N 或 S
        const pattern = /^\d+(\.\d+)?° [NS]$/
        if (!pattern.test(value)) {
          return new Error('格式错误')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

const handleCoverUpload = async ({ file, fileList }) => {
  try {
    const res = await uploadFile(file.file)

    if (res.code === 200) {
      const url = res.data.url || res.data
      const originUrl = res.data.originUrl || url
      const thumbUrl = res.data.thumbUrl || url

      // 更新文件列表状态和URL
      const index = coverFileList.value.findIndex(f => f.id === file.id)
      if (index !== -1) {
        coverFileList.value[index].status = 'finished'
        coverFileList.value[index].url = getImageUrl(thumbUrl || originUrl) // 显示用完整路径
        coverFileList.value[index].originUrl = originUrl // 保存相对路径
        coverFileList.value[index].thumbUrl = thumbUrl   // 保存相对路径
      }

      message.success('封面上传成功')
      return url
    } else {
      message.error('封面上传失败')
      const index = coverFileList.value.findIndex(f => f.id === file.id)
      if (index !== -1) {
        coverFileList.value.splice(index, 1)
      }
      return false
    }
  } catch (error) {
    console.error('上传失败:', error)
    message.error('封面上传失败')
    const index = coverFileList.value.findIndex(f => f.id === file.id)
    if (index !== -1) {
      coverFileList.value.splice(index, 1)
    }
    return false
  }
}

const handleImageUpload = async ({ file, fileList }) => {
  try {
    const res = await uploadFile(file.file)

    if (res.code === 200) {
      const url = res.data.url || res.data
      const originUrl = res.data.originUrl || url
      const thumbUrl = res.data.thumbUrl || url

      const index = imageFileList.value.findIndex(f => f.id === file.id)
      if (index !== -1) {
        imageFileList.value[index].status = 'finished'
        imageFileList.value[index].url = getImageUrl(thumbUrl || originUrl) // 显示用完整路径
        imageFileList.value[index].originUrl = originUrl
        imageFileList.value[index].thumbUrl = thumbUrl
      }

      message.success('图片上传成功')
      return url
    } else {
      message.error('图片上传失败')
      const index = imageFileList.value.findIndex(f => f.id === file.id)
      if (index !== -1) {
        imageFileList.value.splice(index, 1)
      }
      return false
    }
  } catch (error) {
    console.error('上传失败:', error)
    message.error('图片上传失败')
    const index = imageFileList.value.findIndex(f => f.id === file.id)
    if (index !== -1) {
      imageFileList.value.splice(index, 1)
    }
    return false
  }
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '场地名称', key: 'name', width: 150, ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'type',
    width: 100,
    render: (row) => getTypeLabel(row.type)
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) => {
      // status是Boolean类型
      const isActive = row.status === true || row.status === 1
      return h(
          'span',
          { class: isActive ? 'status-available' : 'status-unavailable' },
          isActive ? '可用' : '不可用'
      )
    }
  },
  {
    title: '封面',
    key: 'cover',
    width: 100,
    render: (row) => {
      // 辅助函数：获取逗号分隔字符串中的第一个有效URL
      const getFirstUrl = (str) => {
        if (!str) return ''
        if (typeof str !== 'string') {
          if (Array.isArray(str) && str.length > 0) return str[0]
          return ''
        }
        const urls = str.split(',').filter(u => u && u.trim())
        return urls.length > 0 ? urls[0] : ''
      }

      // 兼容驼峰和下划线命名
      const thumbImage = getFirstUrl(row.thumbImage || row.thumb_image)
      const image = getFirstUrl(row.image)
      const cover = getFirstUrl(row.cover)
      const thumbCover = getFirstUrl(row.thumbCover || row.thumb_cover)

      // 显示用的图片（优先缩略图）
      const displayUrl = thumbCover || thumbImage || cover || image

      // 预览用的图片（优先原图）
      const previewUrl = cover || image || displayUrl

      if (!displayUrl) return '-'

      return h(NImage, {
        width: 60,
        height: 45,
        src: getImageUrl(displayUrl),
        previewSrc: getImageUrl(previewUrl),
        objectFit: 'cover',
        previewDisabled: false,
        showToolbar: true,
        fallbackSrc: '/placeholder.jpg'
      })
    }
  },
  { title: '地址', key: 'address', width: 200, ellipsis: { tooltip: true } },
  { title: '联系人', key: 'locationPrincipalName', width: 120 },
  { title: '联系电话', key: 'locationPrincipalPhone', width: 130 },
  { title: '价格', key: 'price', width: 100, render: (row) => row.price ? '¥' + row.price : '-' },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) => {
      return h('div', { style: 'display: flex; gap: 8px;' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
        h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row.id) },
            {
              trigger: () => h(NButton, { size: 'small', type: 'error', quaternary: true }, { default: () => '删除' })
            }
        )
      ])
    }
  }
]

// 获取文件信息的辅助函数
const getFileInfo = (file) => {
  let info = { url: '', thumb: '' }

  // 1. 优先从映射中获取 (upload时存入)
  if (typeof fileMapping !== 'undefined' && fileMapping[file.id]) {
    info = { ...fileMapping[file.id] }
  }
  // 2. 其次从 file 对象中获取 (回显时设置)
  else if (file.originUrl) {
    info = {
      url: file.originUrl,
      thumb: file.thumbUrl || file.originUrl
    }
  }
  // 3. 兜底：尝试解析 url
  else {
    info.url = file.url || ''
    info.thumb = file.thumbUrl || info.url
  }

  // 统一处理去除域名
  if (info.url && info.url.startsWith('http')) {
    info.url = info.url.replace(config.fileBaseURL, '')
  }
  if (info.thumb && info.thumb.startsWith('http')) {
    info.thumb = info.thumb.replace(config.fileBaseURL, '')
  }

  return info
}

const handleDialogSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    const longPattern = /^\d+(\.\d+)?° [EW]$/
    const latPattern = /^\d+(\.\d+)?° [NS]$/
    const isLongValid = !locationForm.longitude || longPattern.test(locationForm.longitude)
    const isLatValid = !locationForm.latitude || latPattern.test(locationForm.latitude)

    if (!isLongValid || !isLatValid) {
      dialog.warning({
        title: '格式错误',
        content: '经纬度格式必须为：数字° E/W 和 数字° N/S\n例如: 104.06° E, 29.9861° N',
        positiveText: '确定'
      })
    }
    return
  }
  
  try {
    dialogLoading.value = true
    // 组合图片字段：封面 + 详情图
    // 优先使用 fileList 中的 originUrl (相对路径)，如果没有则尝试从 url 解析
    
    // 1. 获取封面信息
    let coverOrigin = locationForm.cover
    let coverThumb = locationForm.thumbCover
    const coverFile = coverFileList.value.find(f => f.status === 'finished')
    
    if (coverFile) {
      if (coverFile.originUrl) coverOrigin = coverFile.originUrl
      else if (coverFile.url && coverFile.url.startsWith('http')) coverOrigin = coverFile.url.replace(config.fileBaseURL, '')
      else coverOrigin = coverFile.url
      
      if (coverFile.thumbUrl) coverThumb = coverFile.thumbUrl
      else coverThumb = coverOrigin // fallback
    } else if (coverOrigin && coverOrigin.startsWith('http')) {
       coverOrigin = coverOrigin.replace(config.fileBaseURL, '')
    }
    
    // 2. 获取详情图信息
    const detailOrigins = imageFileList.value
        .filter(f => f.status === 'finished')
        .map(f => {
          let url = f.url
          if (f.originUrl) url = f.originUrl
          else if (f.url && f.url.startsWith('http')) url = f.url.replace(config.fileBaseURL, '')
          
          // 强制转换为原图路径
          if (url && url.includes('/files/thumb/')) {
            return url.replace('/files/thumb/', '/files/origin/')
          }
          return url
        })
        
    const detailThumbs = imageFileList.value
        .filter(f => f.status === 'finished')
        .map(f => {
          if (f.thumbUrl) return f.thumbUrl
          if (f.originUrl) return f.originUrl // 如果没有缩略图，用原图
          if (f.url && f.url.startsWith('http')) return f.url.replace(config.fileBaseURL, '')
          return f.url
        })

    const allImages = []
    // 封面图强制使用原图
    if (coverOrigin) {
      if (coverOrigin.includes('/files/thumb/')) {
        allImages.push(coverOrigin.replace('/files/thumb/', '/files/origin/'))
      } else {
        allImages.push(coverOrigin)
      }
    }
    if (detailOrigins.length > 0) allImages.push(...detailOrigins)

    const finalImageStr = allImages.join(',')

    const allThumbImages = []
    
    // 处理封面缩略图
    if (coverOrigin) {
        allThumbImages.push(coverThumb || coverOrigin)
    }
    
    // 处理详情缩略图
    if (detailOrigins.length > 0) {
        allThumbImages.push(...detailThumbs)
    }

    const finalThumbImageStr = allThumbImages.join(',')

    const data = {
      ...locationForm,
      status: locationForm.status, // 直接传递Boolean值
      image: finalImageStr,
      thumbImage: finalThumbImageStr,
      user_id: locationForm.userId
    }
    let res
    if (locationForm.id) {
      res = await updateLocation(data)
    } else {
      res = await addLocation(data)
    }
    
    if (res.code === 200) {
      message.success(locationForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
  } finally {
    dialogLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    const res = await deleteLocation(id)
    if (res.code === 200) {
      message.success('删除成功')
      loadData()
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getLocationPage(pagination.page, pagination.pageSize, searchForm.keyword)
    if (res.code === 200) {
      locationList.value = res.data?.records || res.data || []
      // 兼容多种API返回格式
      pagination.itemCount = res.data?.total || res.total || res.pagination?.totalItems || res.pagination?.total || 0
    }
  } catch (error) {
    console.error('加载场地列表失败:', error)
    message.error('加载场地列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  pagination.page = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增场地'
  Object.assign(locationForm, {
    id: null,
    name: '',
    type: '',
    address: '',
    price: 0,
    status: true,
    locationDescription: '',
    locationPrincipalName: '',
    locationPrincipalPhone: '',
    govPrincipalName: '',
    govPrincipalPhone: '',
    longitude: '',
    latitude: '',
    cover: '',
    image: '',
    thumbCover: '',
    thumbImage: '',
    userId: userStore.userInfo?.id || null
  })
  coverFileList.value = []
  imageFileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getLocationById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑场地'

      // 解析图片字段：第一个为封面，后面的是详情图
      const allImages = (res.data.image || '').split(',').filter(url => url.trim())
      const coverUrl = allImages[0] || ''
      const detailUrls = allImages.slice(1)

      // 解析缩略图字段
      const allThumbImages = (res.data.thumbImage || '').split(',').filter(url => url.trim())
      const thumbCoverUrl = allThumbImages[0] || coverUrl
      const detailThumbUrls = allThumbImages.slice(1)

      Object.assign(locationForm, {
        id: res.data.id,
        name: res.data.name || '',
        type: res.data.type || '',
        address: res.data.address || '',
        price: res.data.price || 0,
        status: res.data.status === 1 || res.data.status === true,
        locationDescription: res.data.locationDescription || '',
        locationPrincipalName: res.data.locationPrincipalName || '',
        locationPrincipalPhone: res.data.locationPrincipalPhone || '',
        govPrincipalName: res.data.govPrincipalName || '',
        govPrincipalPhone: res.data.govPrincipalPhone || '',
        longitude: res.data.longitude || '',
        latitude: res.data.latitude || '',
        cover: coverUrl,
        image: detailUrls.join(','),
        thumbCover: thumbCoverUrl,
        thumbImage: '',
        userId: res.data.userId || res.data.user_id
      })
      // 设置封面图片文件列表
      if (coverUrl) {
        coverFileList.value = [{
          id: 'cover',
          name: 'cover.jpg',
          status: 'finished',
          url: getImageUrl(thumbCoverUrl || coverUrl),
          originUrl: coverUrl,
          thumbUrl: thumbCoverUrl || coverUrl
        }]
      } else {
        coverFileList.value = []
      }

      // 设置详情图片文件列表
      imageFileList.value = detailUrls.map((url, index) => {
        const thumbUrl = detailThumbUrls[index] || url
        return {
          id: `img-${index}`,
          name: `image-${index}.jpg`,
          status: 'finished',
          url: getImageUrl(thumbUrl),
          originUrl: url,
          thumbUrl: thumbUrl
        }
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取场地详情失败:', error)
    message.error('获取场地详情失败')
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped lang="scss">
.location-management {
  animation: fadeIn 0.3s ease;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.management-card {
  :deep(.n-card__content) {
    padding: 16px;
  }
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
}

.search-form {
  flex: 1;
  min-width: 300px;
}

.action-buttons {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.status-available {
  color: #18a058;
  font-weight: 500;
}

.status-unavailable {
  color: #d03050;
  font-weight: 500;
}

// 移动端卡片列表
.mobile-list {
  .empty-state {
    text-align: center;
    padding: 60px 20px;
  }

  .card-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .mobile-card {
    :deep(.n-card__content) {
      padding: 16px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;

      .location-info {
        flex: 1;
        margin-right: 12px;

        .location-name {
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 4px 0;
        }

        .location-type {
          font-size: 14px;
          color: #6b7280;
          margin: 0;
        }
      }

      .location-cover {
        flex-shrink: 0;

        .no-cover {
          width: 80px;
          height: 60px;
          background: #f3f4f6;
          border-radius: 6px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #9ca3af;
        }
      }
    }

    .card-content {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-bottom: 12px;
      padding: 12px;
      background: #f9fafb;
      border-radius: 8px;

      .info-item {
        display: flex;
        font-size: 13px;

        .label {
          color: #6b7280;
          min-width: 80px;
          flex-shrink: 0;
        }

        .price {
          color: #f56c6c;
          font-weight: 500;
        }
      }
    }

    .card-actions {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #e5e7eb;
    }
  }

  .mobile-pagination {
    margin-top: 16px;
    padding: 12px;
    background: #ffffff;
    border-radius: 8px;

    :deep(.n-pagination) {
      justify-content: center;
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  .search-header {
    flex-direction: column;
    gap: 12px;

    .search-form {
      width: 100%;
      min-width: auto;

      :deep(.n-form-item) {
        margin-bottom: 12px;

        .n-form-item-label {
          width: auto !important;
          margin-bottom: 4px;
        }
      }
    }

    .action-buttons {
      width: 100%;

      button {
        flex: 1;
      }
    }
  }

  .management-card {
    :deep(.n-card__content) {
      padding: 12px;
    }
  }

  // 移动端表单优化
  :deep(.n-modal) {
    .n-dialog {
      margin: 20px auto;
    }

    .n-form-item {
      margin-bottom: 16px;

      .n-form-item-label {
        font-weight: 500;
        margin-bottom: 8px;
      }

      .n-input,
      .n-select {
        width: 100%;
      }
    }

    .n-dialog__action {
      padding: 12px 16px;

      .n-button {
        flex: 1;
        margin: 0 4px;
      }
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
