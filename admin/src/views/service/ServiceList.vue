<template>
  <div class="service-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="服务名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入服务名称" clearable @keyup.enter="handleSearch" />
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
            新增服务
          </n-button>
        </div>
      </div>
      
      <!-- 桌面端表格 -->
      <template v-if="!isMobile">
        <n-data-table
          :columns="columns"
          :data="serviceList"
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
          <div v-if="serviceList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:services" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
              v-for="service in serviceList"
              :key="service.id"
              class="mobile-card"
              hoverable
            >
              <div class="card-header">
                <div class="service-info">
                  <h3 class="service-name">{{ service.name }}</h3>
                </div>
                <div class="service-cover">
                  <n-image
                    v-if="service.cover"
                    :src="service.cover"
                    width="80"
                    height="60"
                    object-fit="cover"
                    preview-disabled
                  />
                  <div v-else class="no-cover">
                    <Icon icon="mdi:services" :width="32" />
                  </div>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <span class="label">联系人：</span>
                  <span>{{ service.contactName || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">联系电话：</span>
                  <span>{{ service.phone || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">服务地址：</span>
                  <span>{{ service.address || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">状态：</span>
                  <span :class="(service.status === true || service.status === 1) ? 'status-available' : 'status-unavailable'">
                    {{ (service.status === true || service.status === 1) ? '上线' : '下线' }}
                  </span>
                </div>
                <div class="info-item">
                  <span class="label">价格：</span>
                  <span class="price">{{ service.price ? '¥' + service.price : '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">简介：</span>
                  <span class="service-desc">{{ service.description || '-' }}</span>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(service)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(service.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个服务吗？
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
      preset="card" 
      :title="dialogTitle" 
      style="width: 90%; max-width: 900px"
      :mask-closable="false"
    >
      <n-form 
        ref="formRef" 
        :model="serviceForm" 
        :rules="formRules" 
        :label-placement="isMobile ? 'top' : 'left'"
        :label-width="isMobile ? 'auto' : '120'"
      >
        <n-form-item label="服务名称" path="name">
          <n-input v-model:value="serviceForm.name" placeholder="请输入服务名称" />
        </n-form-item>
        
        <n-form-item label="价格" path="price">
          <n-input-number 
            v-model:value="serviceForm.price" 
            placeholder="请输入价格" 
            :min="0"
            :show-button="false"
            style="width: 100%"
          >
            <template #prefix>￥</template>
          </n-input-number>
        </n-form-item>

        <n-form-item label="封面图片">
          <n-upload
            v-model:file-list="coverFileList"
            list-type="image-card"
            :custom-request="handleCoverUpload"
            accept="image/*"
            :max="1"
          >
            点击上传
          </n-upload>
        </n-form-item>

        <n-form-item label="详情图片">
          <n-upload
            v-model:file-list="imageFileList"
            list-type="image-card"
            :custom-request="handleImageUpload"
            accept="image/*"
            multiple
            :max="9"
          >
            点击上传
          </n-upload>
        </n-form-item>

        <n-form-item label="可用状态" path="status">
          <n-switch v-model:value="serviceForm.status" />
        </n-form-item>

        <n-form-item label="服务简介" path="description">
          <n-input v-model:value="serviceForm.description" type="textarea" :rows="3" placeholder="请输入服务简介" />
        </n-form-item>

        <div style="display: flex; gap: 16px;">
          <n-form-item label="联系人" path="contactName" style="flex: 1;">
            <n-input v-model:value="serviceForm.contactName" placeholder="姓名" />
          </n-form-item>
          <n-form-item label="联系电话" path="phone" style="flex: 1;">
            <n-input v-model:value="serviceForm.phone" placeholder="电话" />
          </n-form-item>
        </div>

        <n-form-item label="服务地址" path="address">
          <n-input v-model:value="serviceForm.address" placeholder="请输入服务地址" />
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
          <div v-if="serviceForm.cover" style="margin-top: 12px;">
            <n-image
                :src="getImageUrl(serviceForm.thumbCover || serviceForm.cover)"
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
              :file-list="imageFileList"
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
<<<<<<< HEAD
  import { ref, reactive, onMounted, onUnmounted, h } from 'vue'
  import { NButton, NForm, NFormItem, NInput, NSelect, NInputNumber, NModal, NDataTable, NPagination, NSpace, NSpin, useMessage, NTag, NPopconfirm, NUpload, NImage, NCard } from 'naive-ui'
  import { Icon } from '@iconify/vue'
  import { deleteService, updateService, addService, getServiceList, uploadFile } from '@/api/index'
  import { getImageUrl } from '@/utils/image'
  
  const message = useMessage()
  
  const isMobile = ref(false)
  const loading = ref(false)
  const serviceList = ref([])
  const dialogVisible = ref(false)
  const dialogLoading = ref(false)
  const dialogTitle = ref('新增服务')
  const formRef = ref(null)
  
  const searchForm = reactive({
    keyword: ''
  })
  
  const serviceForm = reactive({
=======
import { ref, reactive, onMounted, onUnmounted, h } from 'vue'
import { 
  NButton, NForm, NFormItem, NInput, NSelect, NInputNumber, 
  NModal, NDataTable, NPagination, NSpace, NSpin, useMessage, 
  NTag, NPopconfirm, NUpload, NImage, NSwitch 
} from 'naive-ui'
import { Icon } from '@iconify/vue'
import { deleteService, updateService, addService, getServiceList, getServiceById, uploadFile } from '@/api/index'
import { getImageUrl } from '@/utils/image'
import { useUserStore } from '@/store/user'
import config from '@/config'

const message = useMessage()
const userStore = useUserStore()

const isMobile = ref(false)
const loading = ref(false)
const serviceList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增服务')
const formRef = ref(null)
const coverFileList = ref([])
const imageFileList = ref([])

const searchForm = reactive({
  keyword: ''
})

const serviceForm = reactive({
  id: null,
  name: '',
  price: 0,
  contactName: '',
  phone: '',
  address: '',
  description: '',
  status: true,
  image: '',
  thumbImage: '',
  userId: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  price: [{ required: true, type: 'number', message: '请输入价格', trigger: 'blur' }],
  address: [{ required: true, message: '请输入服务地址', trigger: 'blur' }]
}

const handleCoverUpload = async ({ file, fileList }) => {
  try {
    const res = await uploadFile(file.file)
    
    if (res.code === 200) {
      const url = res.data.url || res.data
      const originUrl = res.data.originUrl || url
      const thumbUrl = res.data.thumbUrl || url
      
      const index = coverFileList.value.findIndex(f => f.id === file.id)
      if (index !== -1) {
        coverFileList.value[index].status = 'finished'
        coverFileList.value[index].url = getImageUrl(thumbUrl || originUrl)
        coverFileList.value[index].originUrl = originUrl
        coverFileList.value[index].thumbUrl = thumbUrl
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
        imageFileList.value[index].url = getImageUrl(thumbUrl || originUrl)
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
  { title: 'ID', key: 'id', width: 80, fixed: 'left' },
  { title: '服务名称', key: 'name', width: 180, ellipsis: { tooltip: true }, fixed: 'left' },
  {
    title: '图片',
    key: 'image',
    width: 100,
    render: (row) => {
      const images = (row.image || '').split(',').filter(u => u)
      const thumbs = (row.thumbImage || '').split(',').filter(u => u)
      const displayUrl = thumbs[0] || images[0]
      const previewUrl = images[0] || displayUrl
      
      if (!displayUrl) return '-'
      
      return h(NImage, {
        width: 60,
        height: 45,
        src: getImageUrl(displayUrl),
        previewSrc: getImageUrl(previewUrl),
        objectFit: 'cover',
        style: { borderRadius: '4px' }
      })
    }
  },
  { title: '价格', key: 'price', width: 120, render: (row) => row.price ? `¥${row.price}` : '-' },
  { title: '联系人', key: 'contactName', width: 120 },
  { title: '联系电话', key: 'phone', width: 150 },
  { title: '服务地址', key: 'address', width: 250, ellipsis: { tooltip: true } },
  { title: '简介', key: 'description', width: 200, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      // status是Boolean类型
      const isActive = row.status === true || row.status === 1
      return h(
        'span',
        { class: isActive ? 'status-available' : 'status-unavailable' },
        isActive ? '上线' : '下线'
      )
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) => {
      return h('div', { style: 'display: flex; gap: 8px;' }, [
        h(NButton, {
          size: 'small',
          onClick: () => handleEdit(row)
        }, { default: () => '编辑' }),
        h(NPopconfirm, {
          onPositiveClick: () => handleDelete(row.id)
        }, {
          trigger: () => h(NButton, {
            size: 'small',
            type: 'error',
            quaternary: true
          }, { default: () => '删除' }),
          default: () => '确定要删除这个服务吗？'
        })
      ])
    }
  }
]

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getServiceList({
      current: pagination.page,
      size: pagination.pageSize,
      keyword: searchForm.keyword
    })
    if (res.code === 200) {
      serviceList.value = res.data || []
      // 兼容多种API返回格式
      pagination.itemCount = res.pagination?.totalItems || res.data?.total || res.total || 0
    }
  } catch (error) {
    console.error('加载服务列表失败:', error)
    message.error('加载服务列表失败')
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
  dialogTitle.value = '新增服务'
  Object.assign(serviceForm, {
>>>>>>> d6e8090b7be17a369ce2236d95c3fdfc0c48929c
    id: null,
    name: '',
    price: 0,
    contactName: '',
    phone: '',
    address: '',
    description: '',
<<<<<<< HEAD
    cover: '',
    thumbCover: '',
    image: '',
    thumbImage: '',
    status: true
  })

  const coverFileList = ref([])
  const imageFileList = ref([])
  
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
    name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
    contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
  }
  
  const columns = [
    { title: 'ID', key: 'id', width: 80, fixed: 'left' },
    { title: '服务名称', key: 'name', width: 180, ellipsis: { tooltip: true }, fixed: 'left' },
    { title: '价格', key: 'price', width: 120, render: (row) => row.price ? `¥${row.price}` : '-' },
    { title: '联系人', key: 'contactName', width: 120 },
    { title: '联系电话', key: 'phone', width: 150 },
    { title: '服务地址', key: 'address', width: 250, ellipsis: { tooltip: true } },
    { title: '简介', key: 'description', width: 200, ellipsis: { tooltip: true } },
    {
      title: '状态',
      key: 'status',
      width: 100,
      render: (row) => {
        const isActive = row.status !== false
        return h(NTag, { 
          type: isActive ? 'success' : 'default', 
          size: 'small' 
        }, { 
          default: () => isActive ? '上线' : '下线' 
=======
    status: true,
    image: '',
    thumbImage: '',
    userId: userStore.userInfo?.id || null
  })
  coverFileList.value = []
  imageFileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getServiceById(row.id)
    if (res.code === 200 && res.data) {
        dialogTitle.value = '编辑服务'
        const data = res.data
        
        Object.assign(serviceForm, {
          id: data.id,
          name: data.name || '',
          price: data.price || 0,
          contactName: data.contactName || '',
          phone: data.phone || '',
          address: data.address || '',
          description: data.description || '',
          status: data.status === 1 || data.status === true,
          image: data.image || '',
          thumbImage: data.thumbImage || '',
          userId: data.userId || data.user_id
>>>>>>> d6e8090b7be17a369ce2236d95c3fdfc0c48929c
        })

        const urls = (data.image || '').split(',').filter(u => u.trim())
        const thumbUrls = (data.thumbImage || '').split(',').filter(u => u.trim())
        
        // 封面图 (第一张)
        if (urls.length > 0) {
          const coverUrl = urls[0]
          const coverThumb = thumbUrls[0] || coverUrl
          coverFileList.value = [{
            id: 'cover-img',
            name: 'cover.jpg',
            status: 'finished',
            url: getImageUrl(coverThumb || coverUrl),
            originUrl: coverUrl,
            thumbUrl: coverThumb
          }]
        } else {
          coverFileList.value = []
        }

        // 详情图 (第二张开始)
        const detailUrls = urls.slice(1)
        const detailThumbUrls = thumbUrls.slice(1)
        
        imageFileList.value = detailUrls.map((url, index) => ({
            id: `img-${index}`,
            name: `image-${index}.jpg`,
            status: 'finished',
            url: getImageUrl(detailThumbUrls[index] || url),
            originUrl: url,
            thumbUrl: detailThumbUrls[index] || url
        }))
        
        dialogVisible.value = true
    }
  } catch (e) {
      console.error(e)
      message.error('获取详情失败')
  }
<<<<<<< HEAD
  
  const loadData = async () => {
  try {
    loading.value = true
    const res = await getServicePage(pagination.page, pagination.pageSize, searchForm.keyword)
    console.log('分页响应数据:', res) // 添加调试日志
    if (res.code === 200) {
      serviceList.value = res.data || []
      // 设置总数据量
      pagination.itemCount = res.pagination?.totalItems || 0
      // 自动计算总页数
      pagination.pageCount = Math.ceil(pagination.itemCount / pagination.pageSize) || 1
      console.log('设置总数据量:', pagination.itemCount) // 添加调试日志
      console.log('自动计算总页数:', pagination.pageCount) // 添加调试日志
    }
  } catch (error) {
    console.error('加载服务列表失败:', error)
    message.error('加载服务列表失败')
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
    dialogTitle.value = '新增服务'
    Object.assign(serviceForm, {
      id: null,
      name: '',
      price: 0,
      contactName: '',
      phone: '',
      address: '',
      description: '',
      cover: '',
      thumbCover: '',
      image: '',
      thumbImage: '',
      status: true
    })
    coverFileList.value = []
    imageFileList.value = []
    dialogVisible.value = true
  }

  const handleEdit = (service) => {
    dialogTitle.value = '编辑服务'

    // 分离封面图片和详情图片
    const imageUrls = service.image ? service.image.split(',').filter(url => url.trim()) : []
    const thumbUrls = service.thumbImage ? service.thumbImage.split(',').filter(url => url.trim()) : []

    const coverImage = imageUrls.length > 0 ? imageUrls[0] : ''
    const coverThumb = thumbUrls.length > 0 ? thumbUrls[0] : ''
    const detailImages = imageUrls.slice(1)
    const detailThumbs = thumbUrls.slice(1)

    Object.assign(serviceForm, {
      id: service.id,
      name: service.name || '',
      price: service.price || 0,
      contactName: service.contactName || '',
      phone: service.phone || '',
      address: service.address || '',
      description: service.description || '',
      cover: coverImage,
      thumbCover: coverThumb,
      image: detailImages.join(','),
      thumbImage: detailThumbs.join(','),
      status: service.status !== false
    })

    // 设置封面图片文件列表
    coverFileList.value = []
    if (coverImage) {
      coverFileList.value = [{
        id: 'cover',
        name: 'cover.jpg',
        status: 'finished',
        url: coverThumb || coverImage
      }]
    }

    // 设置详情图片文件列表
    imageFileList.value = []
    if (detailImages.length > 0) {
      imageFileList.value = detailImages.map((url, index) => ({
        id: `image-${index}`,
        name: `image-${index}.jpg`,
        status: 'finished',
        url: detailThumbs[index] || url
      }))
    }

    dialogVisible.value = true
  }
  
  const handleDialogSave = async () => {
    try {
      dialogLoading.value = true

      // 合并封面和详情图片为逗号分隔的字符串
      const allImages = []
      const allThumbs = []

      // 先添加封面图片
      if (serviceForm.cover) {
        allImages.push(serviceForm.cover)
        allThumbs.push(serviceForm.thumbCover || serviceForm.cover)
      }

      // 再添加详情图片
      if (serviceForm.image) {
        const detailImages = serviceForm.image.split(',').filter(url => url.trim())
        const detailThumbs = serviceForm.thumbImage ? serviceForm.thumbImage.split(',').filter(url => url.trim()) : []
        allImages.push(...detailImages)
        allThumbs.push(...detailThumbs)
      }

      const data = {
        ...serviceForm,
        image: allImages.join(','),
        thumbImage: allThumbs.join(',')
      }

      // 删除前端专用字段
      delete data.cover
      delete data.thumbCover

      let res
      if (data.id) {
        res = await updateService(data)
      } else {
        res = await addService(data)
      }

      if (res.code === 200) {
        message.success(serviceForm.id ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadData()
      }
    } catch (error) {
      console.error('保存失败:', error)
      message.error('保存失败')
    } finally {
      dialogLoading.value = false
=======
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
    
    // 处理封面图
    const coverFile = coverFileList.value.find(f => f.status === 'finished')
    let coverUrl = ''
    let coverThumb = ''
    
    if (coverFile) {
      if (coverFile.originUrl) coverUrl = coverFile.originUrl
      else if (coverFile.url && coverFile.url.startsWith('http')) coverUrl = coverFile.url.replace(config.fileBaseURL, '')
      else coverUrl = coverFile.url
      
      if (coverFile.thumbUrl) coverThumb = coverFile.thumbUrl
      else if (coverFile.originUrl) coverThumb = coverFile.originUrl
      else if (coverFile.url && coverFile.url.startsWith('http')) coverThumb = coverFile.url.replace(config.fileBaseURL, '')
      else coverThumb = coverFile.url
    }

    // 处理详情图
    const detailImages = imageFileList.value
      .filter(f => f.status === 'finished')
      .map(f => {
         if (f.originUrl) return f.originUrl
         if (f.url && f.url.startsWith('http')) return f.url.replace(config.fileBaseURL, '')
         return f.url
      })
      
    const detailThumbImages = imageFileList.value
      .filter(f => f.status === 'finished')
      .map(f => {
         if (f.thumbUrl) return f.thumbUrl
         if (f.originUrl) return f.originUrl
         if (f.url && f.url.startsWith('http')) return f.url.replace(config.fileBaseURL, '')
         return f.url
      })

    // 组合图片 (封面 + 详情)
    const allImages = []
    if (coverUrl) allImages.push(coverUrl)
    if (detailImages.length > 0) allImages.push(...detailImages)
    
    const allThumbImages = []
    if (coverThumb) allThumbImages.push(coverThumb)
    if (detailThumbImages.length > 0) allThumbImages.push(...detailThumbImages)

    const data = {
      ...serviceForm,
      status: serviceForm.status, // 直接传递Boolean值
      image: allImages.join(','),
      thumbImage: allThumbImages.join(','),
      user_id: serviceForm.userId
    }

    let res
    if (data.id) {
      res = await updateService(data)
    } else {
      res = await addService(data)
>>>>>>> d6e8090b7be17a369ce2236d95c3fdfc0c48929c
    }
  }

const handleDelete = async (id) => {
  try {
    const res = await deleteService(id)
    if (res.code === 200) {
      message.success('删除成功')
      loadData()
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

// 处理封面图片上传
const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl
      const thumbUrl = res.data.thumbUrl

      serviceForm.cover = originUrl
      serviceForm.thumbCover = thumbUrl

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

// 处理封面文件列表变化
const handleCoverFileListChange = (files) => {
  coverFileList.value = files
  if (files.length === 0) {
    serviceForm.cover = ''
    serviceForm.thumbCover = ''
  }
}

// 处理详情图片上传
const handleImageUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl
      const thumbUrl = res.data.thumbUrl

      // 将新上传的图片添加到现有图片列表
      const currentImages = serviceForm.image ? serviceForm.image.split(',').filter(url => url.trim()) : []
      const currentThumbs = serviceForm.thumbImage ? serviceForm.thumbImage.split(',').filter(url => url.trim()) : []

      currentImages.push(originUrl)
      currentThumbs.push(thumbUrl)

      serviceForm.image = currentImages.join(',')
      serviceForm.thumbImage = currentThumbs.join(',')

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

// 处理详情图片文件列表变化
const handleImageFileListChange = (files) => {
  imageFileList.value = files

  // 从文件列表中提取已上传的图片URL
  const uploadedFiles = files.filter(f => f.status === 'finished' && f.url)
  const imageUrls = uploadedFiles.map(f => f.url)

  // 更新表单中的图片字段
  if (imageUrls.length > 0) {
    serviceForm.image = imageUrls.join(',')
  } else {
    serviceForm.image = ''
    serviceForm.thumbImage = ''
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
.service-management {
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
      
      .service-info {
        flex: 1;
        margin-right: 12px;
        
        .service-name {
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 4px 0;
        }
      }
      
      .service-cover {
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
        
        .service-desc {
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