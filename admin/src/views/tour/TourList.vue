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
      <n-data-table
          v-if="!isMobile"
          :columns="columns"
          :data="tourList"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
          :scroll-x="1600"
      />

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
                  <p class="tour-description">{{ tour.description }}</p>
                </div>
                <div class="tour-image">
                  <n-image
                      v-if="tour.cover"
                      :src="getImageUrl(tour.thumb_cover || tour.cover)"
                      width="80"
                      height="60"
                      object-fit="cover"
                      preview-disabled
                  />
                  <div v-else class="no-image">
                    <Icon icon="mdi:map" :width="32" />
                  </div>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <span class="label">主题：</span>
                  <span>{{ tour.theme || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">交通：</span>
                  <span>{{ tour.transport || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">状态：</span>
                  <n-tag :type="tour.deleted === 0 ? 'success' : 'error'">
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
          <div class="mobile-pagination">
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
        <n-form-item label="交通方式" path="transport">
          <n-input v-model:value="tourForm.transport" placeholder="请输入交通方式" />
        </n-form-item>
        <n-form-item label="周边旅馆" path="hotel">
          <n-input v-model:value="tourForm.hotel" placeholder="请输入周边旅馆信息" />
        </n-form-item>
        <n-form-item label="美食推荐" path="food">
          <n-input v-model:value="tourForm.food" placeholder="请输入美食推荐" />
        </n-form-item>
        <n-form-item label="经度" path="longitude">
          <n-input v-model:value="tourForm.longitude" placeholder="请输入经度" />
        </n-form-item>
        <n-form-item label="纬度" path="latitude">
          <n-input v-model:value="tourForm.latitude" placeholder="请输入纬度" />
        </n-form-item>
        <n-form-item label="景点ID" path="location_id">
          <n-input v-model:value="tourForm.location_id" placeholder="请输入景点ID" />
        </n-form-item>
        <n-form-item label="关联影视ID" path="drama_id">
          <n-input v-model:value="tourForm.drama_id" placeholder="请输入关联影视ID" />
        </n-form-item>
        <n-form-item label="状态" path="deleted">
          <n-switch v-model:value="statusSwitch" :checked-value="0" :unchecked-value="1">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>
        <n-form-item label="封面图片" path="cover">
          <n-upload
              :max="1"
              :default-file-list="coverFileList"
              @update:file-list="handleCoverFileListChange"
              @finish="handleUploadFinish"
              :custom-request="handleUpload"
          >
            <n-button>上传封面图片</n-button>
          </n-upload>
          <div v-if="tourForm.cover" style="margin-top: 12px;">
            <n-image
                :src="getImageUrl(tourForm.thumb_cover || tourForm.cover)"
                width="200"
                height="120"
                object-fit="cover"
            />
          </div>
        </n-form-item>
        <n-form-item label="详情图片" path="image">
          <n-upload
              :max="10"
              multiple
              :default-file-list="imageFileList"
              @update:file-list="handleImageFileListChange"
              :custom-request="handleImageUpload"
              accept="image/*"
          >
            <n-button>上传详情图片（最多10张）</n-button>
          </n-upload>
          <div v-if="imageFileList.length > 0" style="margin-top: 12px; display: flex; flex-wrap: wrap; gap: 8px;">
            <n-image
                v-for="(file, index) in imageFileList"
                :key="index"
                :src="file.url"
                width="100"
                height="100"
                object-fit="cover"
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
import { ref, reactive, h, onMounted, onUnmounted, computed, watch } from 'vue'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NButton,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NDataTable,
  NPopconfirm,
  NModal,
  NImage,
  NSpin,
  NPagination,
  NTag,
  NSwitch,
  NUpload,
  useMessage
} from 'naive-ui'
import { getTourPage, createTour, updateTour, deleteTour, getTourById, uploadFile } from '@/api'
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
const coverFileList = ref([])
const imageFileList = ref([])

const searchForm = reactive({
  keyword: ''
})

const tourForm = reactive({
  id: null,
  name: '',
  description: '',
  theme: '',
  features: '',
  cover: '',
  transport: '',
  hotel: '',
  food: '',
  deleted: 0,
  image: '',
  thumb_cover: '',
  thumb_image: '',
  longitude: '',
  latitude: '',
  location_id: '',
  drama_id: ''
})

const statusSwitch = computed({
  get: () => tourForm.deleted === 0,
  set: (val) => {
    tourForm.deleted = val ? 0 : 1
  }
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  showQuickJumper: true
})

const formRules = {
  name: [
    { required: true, message: '请输入体验游名称', trigger: 'blur' },
    { min: 1, max: 255, message: '体验游名称长度在 1 到 255 个字符', trigger: 'blur' }
  ],
  theme: [
    { required: true, message: '请输入主题', trigger: 'blur' },
    { min: 1, max: 50, message: '主题长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入介绍', trigger: 'blur' },
    { min: 1, max: 2550, message: '介绍长度在 1 到 2550 个字符', trigger: 'blur' }
  ],
  longitude: [
    { required: true, message: '请输入经度', trigger: 'blur' }
  ],
  latitude: [
    { required: true, message: '请输入纬度', trigger: 'blur' }
  ],
  location_id: [
    { required: true, message: '请输入景点ID', trigger: 'blur' }
  ],
  drama_id: [
    { required: true, message: '请输入关联影视ID', trigger: 'blur' }
  ]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  {
    title: '封面',
    key: 'cover',
    width: 100,
    render: (row) => {
      const imgUrl = row.thumb_cover || row.cover;
      const originalUrl = row.cover || imgUrl;

      return h(NImage, {
        width: 60,
        height: 45,
        src: getImageUrl(imgUrl), // 显示压缩后的图片
        objectFit: 'cover',
        previewDisabled: false, // 启用预览功能
        fallbackSrc: '/placeholder.jpg',
        // 配置预览功能，点击时显示原图
        previewSrc: getImageUrl(originalUrl)
      });
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
  { title: '景点ID', key: 'location_id', width: 120 },
  { title: '关联影视ID', key: 'drama_id', width: 120 },
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
    title: '更新时间',
    key: 'updated_at',
    width: 180,
    render: (row) => {
      if (Array.isArray(row.updated_at)) {
        return dayjs(row.updated_at[0] + '-' + String(row.updated_at[1]).padStart(2, '0') + '-' + String(row.updated_at[2]).padStart(2, '0')).format('YYYY-MM-DD HH:mm:ss')
      }
      return row.updated_at ? dayjs(row.updated_at).format('YYYY-MM-DD HH:mm:ss') : '-'
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
              trigger: () => h(NButton, { size: 'small', type: 'error', quaternary: true }, { default: () => '删除' })
            }
        )
      ])
    }
  }
]

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

const loadData = async () => {
  try {
    loading.value = true
    console.log('开始加载体验游数据:', {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword
    })

    // 直接调用后端API获取真实数据
    const res = await getTourPage(pagination.page, pagination.pageSize, searchForm.keyword)
    console.log('后端API响应数据:', res)

    if (res.code === 200) {
      // PageResponse的数据结构：data是数组，pagination包含分页信息
      tourList.value = res.data || []
      if (res.pagination) {
        pagination.itemCount = res.pagination.totalItems || 0
        // 自动计算总页数
        pagination.pageCount = Math.ceil(pagination.itemCount / pagination.pageSize) || 1
        pagination.page = res.pagination.currentPage || 1
        pagination.pageSize = res.pagination.pageSize || 10
      }
    } else {
      console.warn('API返回非成功状态:', res.code)
      message.error(`获取体验游失败: ${res.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('加载体验游列表失败:', error)
    console.error('错误详情:', error.response || error.message)
    message.error('加载体验游列表失败')
  } finally {
    loading.value = false
    console.log('加载完成，当前tourList数据量:', tourList.value.length)
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
  dialogTitle.value = '新增体验游'
  Object.assign(tourForm, {
    id: null,
    name: '',
    description: '',
    theme: '',
    features: '',
    cover: '',
    transport: '',
    hotel: '',
    food: '',
    deleted: 0,
    image: '',
    thumb_cover: '',
    thumb_image: '',
    longitude: '',
    latitude: '',
    location_id: '',
    drama_id: ''
  })
  coverFileList.value = []
  imageFileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getTourById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑体验游'
      Object.assign(tourForm, {
        id: res.data.id,
        name: res.data.name || '',
        description: res.data.description || '',
        theme: res.data.theme || '',
        features: res.data.features || '',
        cover: res.data.cover || '',
        transport: res.data.transport || '',
        hotel: res.data.hotel || '',
        food: res.data.food || '',
        deleted: res.data.deleted || 0,
        image: res.data.image || '',
        thumb_cover: res.data.thumb_cover || '',
        thumb_image: res.data.thumb_image || '',
        longitude: res.data.longitude || '',
        latitude: res.data.latitude || '',
        location_id: res.data.location_id || '',
        drama_id: res.data.drama_id || ''
      })

      // 设置封面图片文件列表
      coverFileList.value = []
      if (res.data.cover) {
        coverFileList.value = [{
          id: 'cover',
          name: 'cover.jpg',
          status: 'finished',
          url: res.data.thumb_cover || res.data.cover
        }]
      }

      // 设置详情图片文件列表
      imageFileList.value = []
      if (res.data.image) {
        const imageUrls = res.data.image.split(',').filter(url => url.trim())
        const thumbUrls = res.data.thumb_image ? res.data.thumb_image.split(',').filter(url => url.trim()) : []

        imageFileList.value = imageUrls.map((url, index) => ({
          id: `image-${index}`,
          name: `image-${index}.jpg`,
          status: 'finished',
          url: thumbUrls[index] || url.trim()
        }))
      }

      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取体验游详情失败:', error)
    message.error('获取体验游详情失败')
  }
}

const handleCoverFileListChange = (files) => {
  coverFileList.value = files
}

const handleImageFileListChange = (files) => {
  imageFileList.value = files

  // 从文件列表中提取已上传的图片URL
  const uploadedFiles = files.filter(f => f.status === 'finished' && f.url)
  const imageUrls = uploadedFiles.map(f => f.url)

  // 更新表单中的图片字段
  if (imageUrls.length > 0) {
    tourForm.image = imageUrls.join(',')
  } else {
    tourForm.image = ''
    tourForm.thumb_image = ''
  }
}

const handleUploadFinish = ({ file, event }) => {
  if (event?.target?.response) {
    try {
      const res = JSON.parse(event.target.response)
      if (res.code === 200) {
        // 根据上传的文件类型更新对应字段
        if (file.id === 'cover') {
          tourForm.cover = res.data.url
          tourForm.thumb_cover = res.data.thumbUrl || res.data.url
        }
      } else {
        message.error('上传失败：' + res.message)
      }
    } catch (error) {
      message.error('上传响应解析失败')
    }
  }
}

const handleUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const imageUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || imageUrl

      tourForm.cover = imageUrl
      tourForm.thumb_cover = thumbUrl

      onFinish()
      message.success('封面图片上传成功')
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

// 详情图片上传处理
const handleImageUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const imageUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || imageUrl

      // 将新上传的图片添加到现有图片列表
      const currentImages = tourForm.image ? tourForm.image.split(',').filter(url => url.trim()) : []
      const currentThumbs = tourForm.thumb_image ? tourForm.thumb_image.split(',').filter(url => url.trim()) : []

      currentImages.push(imageUrl)
      currentThumbs.push(thumbUrl)

      tourForm.image = currentImages.join(',')
      tourForm.thumb_image = currentThumbs.join(',')

      onFinish()
      message.success('详情图片上传成功')
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

const handleDialogSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  try {
    dialogLoading.value = true
    const data = {
      name: tourForm.name,
      description: tourForm.description,
      theme: tourForm.theme,
      features: tourForm.features,
      cover: tourForm.cover,
      transport: tourForm.transport,
      hotel: tourForm.hotel,
      food: tourForm.food,
      deleted: tourForm.deleted,
      image: tourForm.image,
      thumb_cover: tourForm.thumb_cover,
      thumb_image: tourForm.thumb_image,
      longitude: tourForm.longitude,
      latitude: tourForm.latitude,
      location_id: tourForm.location_id,
      drama_id: tourForm.drama_id
    }

    let res
    if (tourForm.id) {
      res = await updateTour(tourForm.id, data)
    } else {
      res = await createTour(data)
    }

    if (res.code === 200) {
      message.success(tourForm.id ? '更新成功' : '创建成功')
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
    const res = await deleteTour(id)
    if (res.code === 200) {
      message.success('删除成功')
      loadData()
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}
</script>

<style scoped lang="scss">
.tour-management {
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

      .tour-info {
        flex: 1;
        margin-right: 12px;

        .tour-name {
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 4px 0;
        }

        .tour-description {
          font-size: 14px;
          color: #6b7280;
          margin: 0;
          word-break: break-all;
        }
      }

      .tour-image {
        flex-shrink: 0;

        .no-image {
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

        .price {
          color: #dc2626;
          font-weight: 600;
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
      .n-select,
      .n-input-number {
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



