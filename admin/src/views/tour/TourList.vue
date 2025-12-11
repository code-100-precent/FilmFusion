<template>
  <div class="tour-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="体验游名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入体验游名称" clearable @keyup.enter="handleSearch" />
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
            新增体验游
          </n-button>
        </div>
      </div>

      <!-- 桌面端表格 -->
      <template v-if="!isMobile">
        <n-data-table
            :columns="columns"
            :data="tourList"
            :loading="loading"
            :row-key="row => row.id"
            :scroll-x="1800"
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
          <div v-if="tourList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:map-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
                v-for="tour in tourList"
                :key="tour.id"
                class="mobile-card"
                hoverable
            >
              <div class="card-header">
                <div class="tour-info">
                  <h3 class="tour-name">{{ tour.name }}</h3>
                  <p class="tour-theme">{{ tour.theme }}</p>
                </div>
                <div class="tour-cover">
                  <n-image
                      v-if="tour.cover"
                      :src="getImageUrl(tour.thumbCover || tour.cover)"
                      :preview-src="getImageUrl(tour.cover)"
                      width="80"
                      height="60"
                      object-fit="cover"
                      preview-disabled
                  />
                  <div v-else class="no-cover">
                    <Icon icon="mdi:map" :width="32" />
                  </div>
                </div>
              </div>

              <div class="card-content">
                <div class="info-item">
                  <span class="label">特点：</span>
                  <span class="text-ellipsis">{{ tour.features || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">交通：</span>
                  <span class="text-ellipsis">{{ tour.transport || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">状态：</span>
                  <n-tag :type="tour.deleted === 0 ? 'success' : 'error'" size="small">
                    {{ tour.deleted === 0 ? '启用' : '已删除' }}
                  </n-tag>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(tour)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(tour.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个体验游吗？
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
        preset="dialog"
        :title="dialogTitle"
        style="width: 90%; max-width: 900px"
        :mask-closable="false"
    >
      <n-form
          ref="formRef"
          :model="tourForm"
          :rules="formRules"
          :label-placement="isMobile ? 'top' : 'left'"
          :label-width="isMobile ? 'auto' : '120'"
      >
        <n-form-item label="体验游名称" path="name">
          <n-input v-model:value="tourForm.name" placeholder="请输入体验游名称" />
        </n-form-item>
        <n-form-item label="主题" path="theme">
          <n-input v-model:value="tourForm.theme" placeholder="请输入主题" />
        </n-form-item>
        <n-form-item label="介绍" path="description">
          <n-input v-model:value="tourForm.description" type="textarea" :rows="4" placeholder="请输入介绍" />
        </n-form-item>
        <n-form-item label="特点" path="features">
          <n-input v-model:value="tourForm.features" type="textarea" :rows="3" placeholder="请输入特点，多项用逗号分隔" />
        </n-form-item>
        
        <div class="form-row">
          <n-form-item label="交通方式" path="transport">
            <n-input v-model:value="tourForm.transport" placeholder="请输入交通方式" />
          </n-form-item>
          <n-form-item label="周边旅馆" path="hotel">
            <n-input v-model:value="tourForm.hotel" placeholder="请输入周边旅馆信息" />
          </n-form-item>
        </div>

        <div class="form-row">
          <n-form-item label="美食推荐" path="food">
            <n-input v-model:value="tourForm.food" placeholder="请输入美食推荐" />
          </n-form-item>
          <n-form-item label="景点选择" path="locationId">
            <n-select
                v-model:value="tourForm.locationId"
                :options="locationOptions"
                placeholder="请选择景点"
                filterable
                clearable
                multiple
            />
          </n-form-item>
        </div>

        <div class="form-row">
          <n-form-item label="关联影视选择" path="dramaId">
            <n-select
                v-model:value="tourForm.dramaId"
                :options="dramaOptions"
                placeholder="请选择关联影视"
                filterable
                clearable
                multiple
            />
          </n-form-item>
          <n-form-item label="状态" path="deleted">
            <n-switch v-model:value="statusSwitch" :checked-value="0" :unchecked-value="1">
              <template #checked>启用</template>
              <template #unchecked>禁用</template>
            </n-switch>
          </n-form-item>
        </div>

        <div class="form-row">
          <n-form-item label="经度" path="longitude">
            <n-input v-model:value="tourForm.longitude" placeholder="请输入经度" />
          </n-form-item>
          <n-form-item label="纬度" path="latitude">
            <n-input v-model:value="tourForm.latitude" placeholder="请输入纬度" />
          </n-form-item>
        </div>

        <!-- 封面图片上传 -->
        <n-form-item label="封面图片" path="cover">
          <n-upload
              :max="1"
              :file-list="coverFileList"
              @update:file-list="handleCoverFileListChange"
              :custom-request="handleCoverUpload"
              accept="image/*"
              list-type="image-card"
          >
            点击上传封面
          </n-upload>
        </n-form-item>

        <!-- 详情图片上传 -->
        <n-form-item label="详情图片" path="detailImages">
          <n-upload
              v-model:file-list="detailFileList"
              @update:file-list="handleDetailFileListChange"
              :custom-request="handleDetailUpload"
              accept="image/*"
              list-type="image-card"
              multiple
          >
            点击上传详情图
          </n-upload>
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="dialogVisible = false">取消</n-button>
        <n-button type="primary" @click="handleDialogSave" :loading="dialogLoading">保存</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, h, onMounted, onUnmounted, computed } from 'vue'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NButton,
  NForm,
  NFormItem,
  NInput,
  NDataTable,
  NPopconfirm,
  NModal,
  NImage,
  NSpin,
  NPagination,
  NTag,
  NSwitch,
  NUpload,
  useMessage,
  NSelect
} from 'naive-ui'
import { getTourPage, createTour, updateTour, deleteTour, getTourById, uploadFile, getLocationList, getDramaList } from '@/api'
import { getImageUrl } from '@/utils/image'
import dayjs from 'dayjs'

const message = useMessage()

const isMobile = ref(false)
const loading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const tourList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增体验游')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const tourForm = reactive({
  id: null,
  name: '',
  description: '',
  theme: '',
  features: '',
  transport: '',
  hotel: '',
  food: '',
  deleted: 0,
  image: '',          // 对应 image (封面图 + 详情图)
  thumb_image: '',    // 对应 thumb_image (缩略图)
  longitude: '',
  latitude: '',
  locationId: [],
  dramaId: [],
  // 辅助字段
  cover: '',
  thumbCover: ''
})

const statusSwitch = computed({
  get: () => tourForm.deleted,
  set: (val) => {
    tourForm.deleted = val
  }
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

// 文件列表
const coverFileList = ref([])
const detailFileList = ref([])
// 存储上传文件的详细信息
const fileMapping = reactive({})

// 下拉选项
const locationOptions = ref([])
const dramaOptions = ref([])

// 加载选项数据
const loadOptions = async () => {
  try {
    // 并行请求
    const [locRes, dramaRes] = await Promise.all([
      getLocationList({ current: 1, size: 1000 }), // 获取足够多的景点
      getDramaList({ current: 1, size: 1000 })
    ])

    if (locRes.data) {
      // 兼容分页结构
      const records = Array.isArray(locRes.data) ? locRes.data : (locRes.data.records || [])
      locationOptions.value = records.map(item => ({
        label: item.name,
        value: item.id
      }))
    }

    if (dramaRes.data) {
      const list = Array.isArray(dramaRes.data) ? dramaRes.data : (dramaRes.data.records || [])
      dramaOptions.value = list.map(item => ({
        label: item.name,
        value: item.id
      }))
    }
  } catch (error) {
    console.error('加载选项失败:', error)
  }
}


const formRules = {
  name: [
    { required: true, message: '请输入体验游名称', trigger: 'blur' },
    { min: 1, max: 255, message: '名称长度在 1 到 255 个字符', trigger: 'blur' }
  ],
  theme: [
    { required: true, message: '请输入主题', trigger: 'blur' },
    { min: 1, max: 50, message: '主题长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入介绍', trigger: 'blur' }
  ],
  longitude: [
    { required: true, message: '请输入经度', trigger: 'blur' }
  ],
  latitude: [
    { required: true, message: '请输入纬度', trigger: 'blur' }
  ],
  locationId: [
    { type: 'array', required: true, message: '请选择景点', trigger: ['blur', 'change'] }
  ],
  dramaId: [
    { type: 'array', required: true, message: '请选择关联影视', trigger: ['blur', 'change'] }
  ]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  {
    title: '封面',
    key: 'cover',
    width: 100,
    render: (row) => {
      // 优先显示缩略图，预览显示原图
      const displayUrl = row.thumbCover || row.thumbImage || row.cover || row.image;
      const previewUrl = row.cover || row.image;

      if (displayUrl) {
        return h(NImage, {
          width: 60,
          height: 45,
          src: getImageUrl(displayUrl),
          objectFit: 'cover',
          previewDisabled: false,
          fallbackSrc: '/placeholder.jpg',
          previewSrc: getImageUrl(previewUrl)
        });
      }
      return h('span', '无')
    }
  },
  { title: '体验游名称', key: 'name', width: 200, ellipsis: { tooltip: true } },
  { title: '主题', key: 'theme', width: 120, ellipsis: { tooltip: true } },
  { title: '介绍', key: 'description', width: 250, ellipsis: { tooltip: true } },
  { title: '特点', key: 'features', width: 200, ellipsis: { tooltip: true } },
  { title: '交通方式', key: 'transport', width: 150, ellipsis: { tooltip: true } },
  { title: '周边旅馆', key: 'hotel', width: 150, ellipsis: { tooltip: true } },
  { title: '美食推荐', key: 'food', width: 150, ellipsis: { tooltip: true } },
  { title: '经度', key: 'longitude', width: 120 },
  { title: '纬度', key: 'latitude', width: 120 },
  { title: '景点ID', key: 'locationId', width: 120 },
  { title: '关联影视ID', key: 'dramaId', width: 120 },
  {
    title: '状态',
    key: 'deleted',
    width: 100,
    render: (row) => {
      return h(NTag, {
        type: row.deleted === 0 ? 'success' : 'error'
      }, {
        default: () => row.deleted === 0 ? '启用' : '已删除'
      })
    }
  },
  {
    title: '创建时间',
    key: 'created_at',
    width: 180,
    render: (row) => {
      if (Array.isArray(row.created_at)) {
        return dayjs(row.created_at[0] + '-' + String(row.created_at[1]).padStart(2, '0') + '-' + String(row.created_at[2]).padStart(2, '0')).format('YYYY-MM-DD HH:mm:ss')
      }
      return row.created_at ? dayjs(row.created_at).format('YYYY-MM-DD HH:mm:ss') : '-'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render: (row) => {
      return h('div', { style: 'display: flex; gap: 8px;' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
        h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row.id) },
            {
              trigger: () => h(NButton, { size: 'small', type: 'error', quaternary: true }, { default: () => '删除' }),
              default: () => '确定要删除这个体验游吗？'
            }
        )
      ])
    }
  }
]

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadOptions()
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// 辅助函数：解析图片字符串
const parseImages = (imageStr) => {
  if (!imageStr) return []
  if (typeof imageStr !== 'string') return []
  return imageStr.split(',').filter(url => url && url.trim())
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getTourPage(pagination.page, pagination.pageSize, searchForm.keyword)

    if (res.code === 200) {
      const listData = res.data?.list || res.data?.records || res.data || []
      const totalItems = res.data?.total || res.data?.totalItems || res.pagination?.totalItems || res.pagination?.total || 0

      tourList.value = listData.map(tour => {
        // 解析图片字段
        const images = parseImages(tour.image)
        const thumbImages = parseImages(tour.thumb_image || tour.thumbImage)
        
        const cover = images.length > 0 ? images[0] : ''
        const thumbCover = thumbImages.length > 0 ? thumbImages[0] : ''
        
        return {
          ...tour,
          image: tour.image || '',
          thumb_image: tour.thumb_image || tour.thumbImage || '',
          cover,
          thumbCover
        }
      })

      pagination.itemCount = totalItems
    } else {
      message.error(`获取体验游失败: ${res.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('加载体验游列表失败:', error)
    message.error('加载体验游列表失败')
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

const resetForm = () => {
  Object.assign(tourForm, {
    id: null,
    name: '',
    description: '',
    theme: '',
    features: '',
    transport: '',
    hotel: '',
    food: '',
    deleted: 0,
    image: '',
    thumb_image: '',
    longitude: '',
    latitude: '',
    locationId: [],
    dramaId: [],
    cover: '',
    thumbCover: ''
  })
  coverFileList.value = []
  detailFileList.value = []
  
  // 清空映射
  for (const key in fileMapping) {
    delete fileMapping[key]
  }

  if (formRef.value) {
    formRef.value.restoreValidation()
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增体验游'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑体验游'
  dialogVisible.value = true
  try {
    dialogLoading.value = true
    const res = await getTourById(row.id)
    if (res.code === 200 && res.data) {
      const tour = res.data
      Object.assign(tourForm, {
        id: tour.id,
        name: tour.name || '',
        description: tour.description || '',
        theme: tour.theme || '',
        features: tour.features || '',
        transport: tour.transport || '',
        hotel: tour.hotel || '',
        food: tour.food || '',
        deleted: tour.deleted || 0,
        image: tour.image || '',
        thumb_image: tour.thumb_image || tour.thumbImage || '',
        longitude: tour.longitude || '',
        latitude: tour.latitude || '',
        locationId: tour.locationId ? String(tour.locationId).split(',').map(Number) : [],
        dramaId: tour.dramaId ? String(tour.dramaId).split(',').map(Number) : []
      })

      // 初始化文件列表
      const images = parseImages(tourForm.image)
      const thumbImages = parseImages(tourForm.thumb_image)

      // 封面图：取第一张
      if (images.length > 0) {
        const coverUrl = images[0]
        const coverThumbUrl = thumbImages.length > 0 ? thumbImages[0] : coverUrl
        
        coverFileList.value = [{
          id: 'cover',
          name: '封面图',
          status: 'finished',
          url: getImageUrl(coverThumbUrl),
          originUrl: coverUrl,
          thumbUrl: coverThumbUrl
        }]
        
        tourForm.cover = coverUrl
        tourForm.thumbCover = coverThumbUrl
        
        // 记录到映射
        fileMapping['cover'] = { originUrl: coverUrl, thumbUrl: coverThumbUrl }
      } else {
        coverFileList.value = []
        tourForm.cover = ''
        tourForm.thumbCover = ''
      }

      // 详情图：取剩余的
      if (images.length > 1) {
        const detailUrls = images.slice(1)
        const detailThumbUrls = thumbImages.length > 1 ? thumbImages.slice(1) : detailUrls
        
        detailFileList.value = detailUrls.map((url, index) => {
          const id = `detail-${index}`
          const thumb = detailThumbUrls[index] || url
          
          // 记录到映射
          fileMapping[id] = { originUrl: url, thumbUrl: thumb }
          
          return {
            id: id,
            name: `详情图-${index + 1}`,
            status: 'finished',
            url: getImageUrl(thumb),
            originUrl: url,
            thumbUrl: thumb
          }
        })
      } else {
        detailFileList.value = []
      }
    }
  } catch (error) {
    console.error('获取体验游详情失败:', error)
    message.error('获取体验游详情失败')
  } finally {
    dialogLoading.value = false
  }
}

// 获取文件信息的辅助函数
const getFileInfo = (file) => {
  if (fileMapping[file.id]) {
    return fileMapping[file.id]
  }
  return {
    originUrl: file.originUrl || file.url,
    thumbUrl: file.thumbUrl || file.url
  }
}

const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl
      
      fileMapping[file.id] = { originUrl, thumbUrl }

      const fileIndex = coverFileList.value.findIndex(f => f.id === file.id)
      if (fileIndex !== -1) {
        const fileItem = coverFileList.value[fileIndex]
        fileItem.url = getImageUrl(thumbUrl)
        fileItem.originUrl = originUrl
        fileItem.thumbUrl = thumbUrl
        fileItem.status = 'finished'
      } else {
        coverFileList.value = [{
          id: file.id,
          name: file.name,
          status: 'finished',
          url: getImageUrl(thumbUrl),
          originUrl: originUrl,
          thumbUrl: thumbUrl
        }]
      }
      
      tourForm.cover = originUrl
      tourForm.thumbCover = thumbUrl
      
      // 强制更新
      coverFileList.value = [...coverFileList.value]
      
      onFinish()
      message.success('封面图上传成功')
    } else {
      onError()
      message.error('上传失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('上传图片失败:', error)
    onError()
    message.error('上传失败')
  }
}

const handleCoverFileListChange = (files) => {
  coverFileList.value = files
  if (files.length === 0) {
    tourForm.cover = ''
    tourForm.thumbCover = ''
  }
}

const handleDetailUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl
      
      fileMapping[file.id] = { originUrl, thumbUrl }

      const index = detailFileList.value.findIndex(f => f.id === file.id)
      if (index !== -1) {
        const fileItem = detailFileList.value[index]
        fileItem.url = getImageUrl(thumbUrl)
        fileItem.originUrl = originUrl
        fileItem.thumbUrl = thumbUrl
        fileItem.status = 'finished'
        detailFileList.value = [...detailFileList.value]
      } else {
         const newItem = {
          id: file.id,
          name: file.name,
          status: 'finished',
          url: getImageUrl(thumbUrl),
          originUrl: originUrl,
          thumbUrl: thumbUrl
        }
        detailFileList.value = [...detailFileList.value, newItem]
      }
      onFinish()
    } else {
      onError()
      message.error('上传失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('上传图片失败:', error)
    onError()
    message.error('上传失败')
  }
}

const handleDetailFileListChange = (files) => {
  detailFileList.value = files
}

const handleDialogSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  try {
    dialogLoading.value = true
    
    // 合并图片
    const allImages = []
    const allThumbImages = []
    
    // 1. 封面图
    if (coverFileList.value.length > 0) {
      const coverFile = coverFileList.value[0]
      const info = getFileInfo(coverFile)
      if (info.originUrl) {
        allImages.push(info.originUrl)
        allThumbImages.push(info.thumbUrl || info.originUrl)
      }
    }
    
    // 2. 详情图
    if (detailFileList.value.length > 0) {
      detailFileList.value.forEach(file => {
        const info = getFileInfo(file)
        if (info.originUrl) {
          allImages.push(info.originUrl)
          allThumbImages.push(info.thumbUrl || info.originUrl)
        }
      })
    }

    const data = {
      name: tourForm.name,
      description: tourForm.description,
      theme: tourForm.theme,
      features: tourForm.features,
      transport: tourForm.transport,
      hotel: tourForm.hotel,
      food: tourForm.food,
      deleted: tourForm.deleted,
      longitude: tourForm.longitude,
      latitude: tourForm.latitude,
      locationId: Array.isArray(tourForm.locationId) ? tourForm.locationId.join(',') : '',
      dramaId: Array.isArray(tourForm.dramaId) ? tourForm.dramaId.join(',') : '',
      image: allImages.join(','),
      thumb_image: allThumbImages.join(',')
    }

    if (tourForm.id) {
      const res = await updateTour(tourForm.id, data)
      if (res.code === 200) {
        message.success('更新成功')
        dialogVisible.value = false
        loadData()
      } else {
        message.error(res.message || '更新失败')
      }
    } else {
      const res = await createTour(data)
      if (res.code === 200) {
        message.success('创建成功')
        dialogVisible.value = false
        loadData()
      } else {
        message.error(res.message || '创建失败')
      }
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
    const res = await deleteTour(id)
    if (res.code === 200) {
      message.success('删除成功')
      loadData()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}
</script>

<style scoped>
.tour-management {
  padding: 16px;
}

.management-card {
  min-height: calc(100vh - 100px);
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 16px;
}

.search-form {
  flex: 1;
  min-width: 300px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-row .n-form-item {
  flex: 1;
}

/* Mobile Responsive Styles */
@media (max-width: 768px) {
  .tour-management {
    padding: 8px;
  }
  
  .management-card {
    border-radius: 8px;
  }

  .search-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-form {
    width: 100%;
  }
  
  .action-buttons {
    width: 100%;
  }
  
  .action-buttons button {
    width: 100%;
  }

  .mobile-card {
    margin-bottom: 12px;
    border-radius: 8px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
  }
  
  .tour-info {
    flex: 1;
    margin-right: 12px;
  }
  
  .tour-name {
    margin: 0 0 4px 0;
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
  }
  
  .tour-theme {
    margin: 0;
    font-size: 13px;
    color: #6b7280;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .tour-cover {
    width: 80px;
    height: 60px;
    border-radius: 4px;
    overflow: hidden;
    flex-shrink: 0;
  }
  
  .no-cover {
    width: 100%;
    height: 100%;
    background-color: #f3f4f6;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #9ca3af;
  }
  
  .card-content {
    margin-bottom: 16px;
    padding: 12px;
    background-color: #f9fafb;
    border-radius: 6px;
  }
  
  .info-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 13px;
  }
  
  .info-item:last-child {
    margin-bottom: 0;
  }
  
  .info-item .label {
    color: #6b7280;
    flex-shrink: 0;
  }

  .text-ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 70%;
  }
  
  .card-actions {
    display: flex;
    gap: 8px;
  }
  
  .mobile-pagination {
    display: flex;
    justify-content: center;
    margin-top: 16px;
    padding-bottom: 16px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>