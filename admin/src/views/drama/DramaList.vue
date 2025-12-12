<template>
  <div class="drama-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="电视剧名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入电视剧名称" clearable @keyup.enter="handleSearch" />
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
            新增电视剧
          </n-button>
        </div>
      </div>

      <!-- 桌面端表格 -->
      <template v-if="!isMobile">
        <n-data-table
            :columns="columns"
            :data="dramaList"
            :loading="loading"
            :row-key="row => row.id"
            :scroll-x="1400"
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
          <div v-if="dramaList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:film-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
                v-for="drama in dramaList"
                :key="drama.id"
                class="mobile-card"
                hoverable
            >
              <div class="card-header">
                <div class="drama-info">
                  <h3 class="drama-name">{{ drama.name }}</h3>
                  <p class="drama-company">{{ drama.prodCompany }}</p>
                </div>
                <div class="drama-cover">
                  <n-image
                      v-if="drama.cover"
                      :src="drama.cover"
                      width="80"
                      height="60"
                      object-fit="cover"
                      preview-disabled
                  />
                  <div v-else class="no-cover">
                    <Icon icon="mdi:film" :width="32" />
                  </div>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <span class="label">备案号：</span>
                  <span>{{ drama.filingNum || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">拍摄地：</span>
                  <span>{{ drama.shootLocation || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">演员：</span>
                  <span class="cast-info">{{ drama.cast || '-' }}</span>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(drama)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(drama.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个电视剧吗？
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
          :model="dramaForm"
          :rules="formRules"
          :label-placement="isMobile ? 'top' : 'left'"
          :label-width="isMobile ? 'auto' : '120'"
      >
        <n-form-item label="电视剧名称" path="name">
          <n-input v-model:value="dramaForm.name" placeholder="请输入电视剧名称" />
        </n-form-item>
        <n-form-item label="备案号" path="filingNum">
          <n-input v-model:value="dramaForm.filingNum" placeholder="请输入备案号" />
        </n-form-item>
        <n-form-item label="出品公司" path="prodCompany">
          <n-input v-model:value="dramaForm.prodCompany" placeholder="请输入出品公司" />
        </n-form-item>
        <n-form-item label="公司简介" path="crewDescription">
          <n-input v-model:value="dramaForm.crewDescription" type="textarea" :rows="3" placeholder="请输入公司简介" />
        </n-form-item>
        <n-form-item label="电视剧简介" path="dramaDescription">
          <n-input v-model:value="dramaForm.dramaDescription" type="textarea" :rows="4" placeholder="请输入电视剧简介" />
        </n-form-item>
        <n-form-item label="演员" path="cast">
          <n-input v-model:value="dramaForm.cast" placeholder="请输入演员信息" />
        </n-form-item>
        <n-form-item label="拍摄场地" path="locationId">
          <n-select v-model:value="dramaForm.locationId" :options="locationOptions" placeholder="请选择拍摄场地" filterable clearable multiple @update:value="handleLocationUpdate" />
        </n-form-item>
        <n-form-item label="拍摄地描述" path="shootLocation">
          <n-input v-model:value="dramaForm.shootLocation" placeholder="请输入拍摄地描述" />
        </n-form-item>
        <n-form-item label="协拍服务" path="serviceId">
          <n-select v-model:value="dramaForm.serviceId" :options="serviceOptions" placeholder="请选择协拍服务" filterable clearable multiple @update:value="handleServiceUpdate" />
        </n-form-item>
        <n-form-item label="服务描述" path="service">
          <n-input v-model:value="dramaForm.service" type="textarea" :rows="2" placeholder="请输入服务描述" />
        </n-form-item>
        <n-form-item label="封面图片" path="cover">
          <n-upload
              :max="1"
              :file-list="coverFileList"
              @update:file-list="handleCoverFileListChange"
              :custom-request="handleCoverUpload"
              accept="image/*"
          >
            <n-button>上传封面图片</n-button>
          </n-upload>
          <div v-if="dramaForm.cover" style="margin-top: 12px;">
            <n-image
                :src="getImageUrl(dramaForm.thumbCover || dramaForm.cover)"
                width="200"
                height="120"
                object-fit="cover"
            />
          </div>
        </n-form-item>
        <n-form-item label="详情图片" path="image">
          <n-upload
              :max="10"
              :file-list="imageFileList"
              @update:file-list="handleImageFileListChange"
              :custom-request="handleImageUpload"
              accept="image/*"
              multiple
          >
            <n-button>上传详情图片（可多张）</n-button>
          </n-upload>
          <div v-if="imageFileList.length > 0" style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap;">
            <n-image
                v-for="(file, index) in imageFileList"
                :key="index"
                :src="file.url"
                width="100"
                height="75"
                object-fit="cover"
                style="border-radius: 4px;"
            />
          </div>
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
import { ref, reactive, h, onMounted, onUnmounted } from 'vue'
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
  NUpload,
  NSelect,
  useMessage
} from 'naive-ui'
import { useUserStore } from '@/store/user'
import { 
  getDramaPage, 
  addDrama, 
  updateDrama, 
  deleteDrama, 
  getDramaById, 
  uploadFile,
  getLocationList,
  getServiceList
} from '@/api'
import { getImageUrl } from '@/utils/image'
import config from '@/config'
import dayjs from 'dayjs'

const message = useMessage()
const userStore = useUserStore()

const isMobile = ref(false)
const loading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const dramaList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增电视剧')
const formRef = ref(null)
const coverFileList = ref([])
const imageFileList = ref([])
const locationOptions = ref([])
const serviceOptions = ref([])

const fetchOptions = async () => {
  try {
    // 获取场地列表
    const locRes = await getLocationList({ current: 1, size: 100 })
    if (locRes.code === 200) {
      const list = locRes.data?.records || locRes.data || []
      locationOptions.value = list.map(item => ({
        label: item.name,
        value: item.id
      }))
    }

    // 获取服务列表 (分页接口，取前100条)
    const servRes = await getServiceList({ current: 1, size: 100 })
    if (servRes.code === 200) {
      const list = servRes.data?.records || servRes.data || []
      serviceOptions.value = list.map(item => ({
        label: item.name,
        value: item.id
      }))
    }
  } catch (error) {
    console.error('获取选项数据失败:', error)
  }
}

const searchForm = reactive({
  keyword: ''
})

const dramaForm = reactive({
  id: null,
  name: '',
  filingNum: '',
  prodCompany: '',
  crewDescription: '',
  dramaDescription: '',
  cast: '',
  shootLocation: '',
  locationId: [],
  service: '',
  serviceId: [],
  userId: null,
  cover: '',
  image: '',
  thumbCover: '',
  thumbImage: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  showQuickJumper: true,
  // 添加以下属性以确保Naive UI正确计算分页
  pageCount: 1,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`
})

const formRules = {
  name: [
    { required: true, message: '请输入电视剧名称', trigger: 'blur' },
    { min: 1, max: 100, message: '电视剧名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  prodCompany: [
    { required: true, message: '请输入出品公司', trigger: 'blur' },
    { min: 1, max: 100, message: '出品公司长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  locationId: [
    { type: 'array', required: true, message: '请选择拍摄场地', trigger: ['blur', 'change'] }
  ],
  serviceId: [
    { type: 'array', required: true, message: '请选择协拍服务', trigger: ['blur', 'change'] }
  ]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '电视剧名称', key: 'name', width: 200, ellipsis: { tooltip: true } },
  { title: '备案号', key: 'filingNum', width: 150, ellipsis: { tooltip: true } },
  { title: '出品公司', key: 'prodCompany', width: 200, ellipsis: { tooltip: true } },
  {
    title: '封面',
    key: 'cover',
    width: 100,
    render: (row) => {
      // 调试日志：检查特定ID的数据 (ID 20)
      if (row.id == 20) {
        console.log('--- Debug ID 20 ---')
        console.log('Row Data:', row)
        console.log('Fields:', {
          thumbImage: row.thumbImage,
          thumb_image: row.thumb_image,
          image: row.image,
          cover: row.cover,
          thumbCover: row.thumbCover,
          thumb_cover: row.thumb_cover
        })
      }

      // 辅助函数：获取逗号分隔字符串中的第一个有效URL
      const getFirstUrl = (str) => {
        if (!str) return ''
        // 确保 str 是字符串
        if (typeof str !== 'string') {
          // 如果是数组，尝试取第一个
          if (Array.isArray(str) && str.length > 0) {
             return str[0]
          }
          return ''
        }
        // 过滤掉空字符串，防止出现 ",url" 这种情况导致取到空值
        const urls = str.split(',').filter(u => u && u.trim())
        return urls.length > 0 ? urls[0] : ''
      }
      
      // 兼容驼峰和下划线命名，并确保取到的是第一张图片
      const thumbImage = getFirstUrl(row.thumbImage || row.thumb_image)
      const image = getFirstUrl(row.image)
      const cover = getFirstUrl(row.cover)
      const thumbCover = getFirstUrl(row.thumbCover || row.thumb_cover)
      
      // 优先级：封面缩略 -> 封面原图 -> 详情缩略 -> 详情原图
      // 显示用的图片（优先缩略图）
      const displayUrl = thumbCover || thumbImage || cover || image
      
      // 预览用的图片（优先原图）
      const previewUrl = cover || image || displayUrl
      
      if (row.id == 20) {
         console.log('Display URL (Thumb):', displayUrl)
         console.log('Preview URL (Original):', previewUrl)
      }

      if (!displayUrl) return '-'

      return h(NImage, {
        width: 60,
        height: 45,
        src: getImageUrl(displayUrl),
        previewSrc: getImageUrl(previewUrl), // 点击预览时显示原图
        objectFit: 'cover',
        previewDisabled: false,
        showToolbar: true,
        fallbackSrc: '/placeholder.jpg'
      })
    }
  },
  { title: '演员名单', key: 'cast', width: 200, ellipsis: { tooltip: true } },
  { title: '拍摄地', key: 'shootLocation', width: 150, ellipsis: { tooltip: true } },
  { title: '协拍服务', key: 'service', width: 150, ellipsis: { tooltip: true } },
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
              trigger: () => h(NButton, { size: 'small', type: 'error', quaternary: true }, { default: () => '删除' })
            }
        )
      ])
    }
  }
]

const handleDialogSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  try {
    dialogLoading.value = true
    
    // 组合图片字段：封面 + 详情图
    // 优先使用 fileList 中的 originUrl (相对路径)，如果没有则尝试从 url 解析
    const detailOrigins = imageFileList.value
        .filter(f => f.status === 'finished')
        .map(f => {
          if (f.originUrl) return f.originUrl
          if (f.url && f.url.startsWith('http')) return f.url.replace(config.fileBaseURL, '')
          return f.url
        })
        
    const allImages = []
    
    // 获取封面原图（优先从文件列表获取）
    let coverOrigin = dramaForm.cover
    const coverFile = coverFileList.value.find(f => f.status === 'finished')
    if (coverFile) {
      if (coverFile.originUrl) {
        coverOrigin = coverFile.originUrl
      } else if (coverFile.url && coverFile.url.startsWith('http')) {
        coverOrigin = coverFile.url.replace(config.fileBaseURL, '')
      } else {
        coverOrigin = coverFile.url
      }
    } else if (coverOrigin && coverOrigin.startsWith('http')) {
      // 兜底：如果 dramaForm.cover 是完整路径
      coverOrigin = coverOrigin.replace(config.fileBaseURL, '')
    }

    if (coverOrigin) allImages.push(coverOrigin)
    if (detailOrigins.length > 0) allImages.push(...detailOrigins)
    
    const finalImageStr = allImages.join(',')
    
    // 组合缩略图字段
    // 优先使用 fileList 中的 thumbUrl (相对路径)，如果没有则回退到 originUrl 或 url
    const detailThumbs = imageFileList.value
        .filter(f => f.status === 'finished')
        .map(f => {
          if (f.thumbUrl) return f.thumbUrl
          if (f.originUrl) return f.originUrl
          if (f.url && f.url.startsWith('http')) return f.url.replace(config.fileBaseURL, '')
          return f.url
        })

    const allThumbImages = []
    
    // 获取封面缩略图（优先从文件列表获取）
    let coverThumb = dramaForm.thumbCover || dramaForm.cover
    if (coverFile) {
      if (coverFile.thumbUrl) {
        coverThumb = coverFile.thumbUrl
      } else if (coverFile.originUrl) {
        coverThumb = coverFile.originUrl
      }
    }
    
    // 兜底处理
    if (coverThumb && coverThumb.startsWith('http')) {
       coverThumb = coverThumb.replace(config.fileBaseURL, '')
    }

    if (coverThumb) allThumbImages.push(coverThumb)
    if (detailThumbs.length > 0) allThumbImages.push(...detailThumbs)
    
    const finalThumbImageStr = allThumbImages.join(',')

    const data = {
      ...dramaForm,
      image: finalImageStr,
      thumbImage: finalThumbImageStr,
      // 映射表单字段到后端字段
      locationId: Array.isArray(dramaForm.locationId) ? dramaForm.locationId.join(',') : dramaForm.locationId,
      serviceId: Array.isArray(dramaForm.serviceId) ? dramaForm.serviceId.join(',') : dramaForm.serviceId,
      location_id: Array.isArray(dramaForm.locationId) ? dramaForm.locationId.join(',') : dramaForm.locationId,
      service_id: Array.isArray(dramaForm.serviceId) ? dramaForm.serviceId.join(',') : dramaForm.serviceId,
      user_id: dramaForm.userId
    }

    let res
    if (dramaForm.id) {
      res = await updateDrama(data)
    } else {
      res = await addDrama(data)
    }

    if (res.code === 200) {
      message.success(dramaForm.id ? '更新成功' : '创建成功')
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
    const res = await deleteDrama(id)
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
    const res = await getDramaPage(pagination.page, pagination.pageSize, searchForm.keyword)
    console.log('分页响应数据:', res) // 添加调试日志
    if (res.code === 200) {
      dramaList.value = res.data?.records || res.data || []
      // 兼容多种API返回格式
      pagination.itemCount = res.data?.total || res.total || res.pagination?.totalItems || res.pagination?.total || 0
    }
  } catch (error) {
    console.error('加载电视剧列表失败:', error)
    message.error('加载电视剧列表失败')
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

const handleLocationUpdate = (values, options) => {
  if (options && options.length > 0) {
    dramaForm.shootLocation = options.map(o => o.label).join(',')
  } else {
    dramaForm.shootLocation = ''
  }
}

const handleServiceUpdate = (values, options) => {
  if (options && options.length > 0) {
    dramaForm.service = options.map(o => o.label).join(',')
  } else {
    dramaForm.service = ''
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增电视剧'
  Object.assign(dramaForm, {
    id: null,
    name: '',
    filingNum: '',
    prodCompany: '',
    crewDescription: '',
    dramaDescription: '',
    cast: '',
    shootLocation: '',
    locationId: [],
    service: '',
    serviceId: [],
    userId: userStore.userInfo?.id || null,
    cover: '',
    image: '',
    thumbCover: '',
    thumbImage: ''
  })
  coverFileList.value = []
  imageFileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getDramaById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑电视剧'
      
      // 解析图片字段：第一个为封面，后面的是详情图
      const allImages = (res.data.image || '').split(',').filter(url => url.trim())
      const coverUrl = allImages[0] || ''
      const detailUrls = allImages.slice(1)
      
      // 解析缩略图字段
      const allThumbImages = (res.data.thumbImage || '').split(',').filter(url => url.trim())
      const thumbCoverUrl = allThumbImages[0] || coverUrl
      const detailThumbUrls = allThumbImages.slice(1)

      Object.assign(dramaForm, {
        id: res.data.id,
        name: res.data.name || '',
        filingNum: res.data.filingNum || '',
        prodCompany: res.data.prodCompany || '',
        crewDescription: res.data.crewDescription || '',
        dramaDescription: res.data.dramaDescription || '',
        cast: res.data.cast || '',
        shootLocation: res.data.shootLocation || '',
        locationId: String(res.data.locationId || res.data.location_id || '').split(',').filter(id => id && id.trim()).map(Number),
        service: res.data.service || '',
        serviceId: String(res.data.serviceId || res.data.service_id || '').split(',').filter(id => id && id.trim()).map(Number),
        userId: res.data.userId || res.data.user_id,
        cover: coverUrl,
        image: detailUrls.join(','), // 详情图片字符串
        thumbCover: thumbCoverUrl,
        thumbImage: '' 
      })

      // 设置封面图片文件列表
      if (coverUrl) {
        coverFileList.value = [{
          id: 'cover',
          name: 'cover.jpg',
          status: 'finished',
          url: getImageUrl(thumbCoverUrl || coverUrl),
          originUrl: coverUrl,        // 封面原图相对路径
          thumbUrl: thumbCoverUrl || coverUrl // 封面缩略图相对路径
        }]
      } else {
        coverFileList.value = []
      }

      // 设置详情图片文件列表
      // 同时保存原图和缩略图的相对路径到文件对象上
      imageFileList.value = detailUrls.map((url, index) => ({
        id: `image-${index}`,
        name: `image-${index}.jpg`,
        status: 'finished',
        url: getImageUrl(url), // 显示用完整路径
        originUrl: url,        // 原图相对路径
        thumbUrl: detailThumbUrls[index] || url // 缩略图相对路径
      }))

      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取电视剧详情失败:', error)
    message.error('获取电视剧详情失败')
  }
}

// 处理封面图片上传
const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl
      
      dramaForm.cover = originUrl
      dramaForm.thumbCover = thumbUrl
      
      // 更新文件列表中的URL，以便显示预览
      // 使用 getImageUrl 转换为完整 URL
      const fullUrl = getImageUrl(originUrl)
      file.url = fullUrl
      file.originUrl = originUrl
      file.thumbUrl = thumbUrl
      
      const fileIndex = coverFileList.value.findIndex(f => f.id === file.id)
      if (fileIndex !== -1) {
        coverFileList.value[fileIndex].url = fullUrl
        coverFileList.value[fileIndex].originUrl = originUrl
        coverFileList.value[fileIndex].thumbUrl = thumbUrl
      }

      onFinish()
      message.success('封面图片上传成功')
    } else {
      onError()
      message.error('上传失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('上传封面图片失败:', error)
    onError()
    message.error('上传失败')
  }
}

// 处理详情图片上传
const handleImageUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl // 获取缩略图路径
      
      // 更新文件列表中的URL，以便显示预览
      // 使用 getImageUrl 转换为完整 URL
      const fullUrl = getImageUrl(originUrl)
      file.url = fullUrl
      file.originUrl = originUrl // 保存原图相对路径
      file.thumbUrl = thumbUrl   // 保存缩略图相对路径
      
      const fileIndex = imageFileList.value.findIndex(f => f.id === file.id)
      if (fileIndex !== -1) {
        imageFileList.value[fileIndex].url = fullUrl
        imageFileList.value[fileIndex].originUrl = originUrl
        imageFileList.value[fileIndex].thumbUrl = thumbUrl
      }

      // 添加到图片列表 (保持相对路径)
      // 注意：这里只是为了更新 form 数据以便校验，最终保存时会从 imageFileList 重新读取
      const existingImages = dramaForm.image ? dramaForm.image.split(',').filter(url => url.trim()) : []
      existingImages.push(originUrl)
      dramaForm.image = existingImages.join(',')
      
      onFinish()
      message.success('详情图片上传成功')
    } else {
      onError()
      message.error('上传失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('上传详情图片失败:', error)
    onError()
    message.error('上传失败')
  }
}

// 处理封面文件列表变化
const handleCoverFileListChange = (files) => {
  coverFileList.value = files
  if (files.length === 0) {
    dramaForm.cover = ''
    dramaForm.thumbCover = ''
  }
}

// 处理详情图片文件列表变化
const handleImageFileListChange = (files) => {
  imageFileList.value = files
  // 更新image字段为逗号分隔的URL字符串
  const imageUrls = files
      .filter(file => file.status === 'finished' && file.url)
      .map(file => {
        // 如果是完整URL，移除基础URL部分以存储相对路径
        if (file.url.startsWith('http')) {
          return file.url.replace(config.fileBaseURL, '')
        }
        return file.url
      })
  dramaForm.image = imageUrls.join(',')
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadData()
  fetchOptions()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped lang="scss">
.drama-management {
  animation: fadeIn 0.3s ease;
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

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
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

      .drama-info {
        flex: 1;
        margin-right: 12px;

        .drama-name {
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 4px 0;
        }

        .drama-company {
          font-size: 14px;
          color: #6b7280;
          margin: 0;
        }
      }

      .drama-cover {
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
          min-width: 60px;
          flex-shrink: 0;
        }

        .cast-info {
          color: #374151;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
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