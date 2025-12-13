<template>
  <div class="hotel-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="酒店名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入酒店名称" clearable @keyup.enter="handleSearch" />
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
            新增酒店
          </n-button>
        </div>
      </div>

      <!-- 桌面端表格 -->
      <template v-if="!isMobile">
        <n-data-table
            :columns="columns"
            :data="hotelList"
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
          <div v-if="hotelList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:hotel-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
                v-for="hotel in hotelList"
                :key="hotel.id"
                class="mobile-card"
                hoverable
            >
              <div class="card-header">
                <div class="hotel-info">
                  <h3 class="hotel-name">{{ hotel.name }}</h3>
                  <p class="hotel-address">{{ hotel.address }}</p>
                </div>
                <div class="hotel-cover">
                  <n-image
                      v-if="hotel.cover"
                      :src="getImageUrl(hotel.thumbCover || hotel.cover)"
                      :preview-src="getImageUrl(hotel.cover)"
                      width="80"
                      height="60"
                      object-fit="cover"
                      preview-disabled
                  />
                  <div v-else class="no-cover">
                    <Icon icon="mdi:hotel" :width="32" />
                  </div>
                </div>
              </div>

              <div class="card-content">
                <div class="info-item">
                  <span class="label">负责人：</span>
                  <span>{{ hotel.manager_name || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">负责人电话：</span>
                  <span>{{ hotel.manager_phone || '-' }}</span>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(hotel)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(hotel.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个酒店吗？
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
          :model="hotelForm"
          :rules="formRules"
          :label-placement="isMobile ? 'top' : 'left'"
          :label-width="isMobile ? 'auto' : '120'"
      >
        <n-form-item label="酒店名称" path="name">
          <n-input v-model:value="hotelForm.name" placeholder="请输入酒店名称" />
        </n-form-item>
        <n-form-item label="地址" path="address">
          <n-input v-model:value="hotelForm.address" placeholder="请输入酒店地址" />
        </n-form-item>

        <n-form-item label="负责人姓名" path="manager_name">
          <n-input v-model:value="hotelForm.manager_name" placeholder="请输入负责人姓名" />
        </n-form-item>
        <n-form-item label="负责人电话" path="manager_phone">
          <n-input v-model:value="hotelForm.manager_phone" placeholder="请输入负责人电话" />
        </n-form-item>
        <n-form-item label="经度" path="longitude">
          <n-input v-model:value="hotelForm.longitude" placeholder="请输入经度" />
        </n-form-item>
        <n-form-item label="纬度" path="latitude">
          <n-input v-model:value="hotelForm.latitude" placeholder="请输入纬度" />
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input v-model:value="hotelForm.description" type="textarea" :rows="4" placeholder="请输入酒店描述" />
        </n-form-item>

        <!-- 封面图片上传 -->
        <n-form-item label="封面图片" path="cover">
          <n-upload
              :max="1"
              :file-list="coverFileList"
              @update:file-list="handleCoverFileListChange"
              @before-upload="beforeUpload"
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
              @before-upload="beforeUpload"
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
import { ref, reactive, h, onMounted, onUnmounted, nextTick } from 'vue'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NButton,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NDataTable,
  NPopconfirm,
  NModal,
  NImage,
  NSpin,
  NPagination,
  NUpload,
  useMessage,
  useDialog
} from 'naive-ui'
import {
  getHotelPage,
  createHotel,
  updateHotel,
  deleteHotel,
  getHotelById,
  uploadFile
} from '@/api'
import { getImageUrl } from '@/utils/image'
import config from '@/config'
import { useUserStore } from '@/store/user'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const isMobile = ref(false)
const loading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const hotelList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增酒店')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const hotelForm = reactive({
  id: null,
  name: '',
  address: '',
  manager_name: '',
  manager_phone: '',
  description: '',
  longitude: '',
  latitude: '',
  image: '',           // 对应 image (封面图 + 详情图)
  thumbImage: '',      // 对应 thumb_image (缩略图)
  userId: null,
  // 辅助字段，不提交给后端，用于内部逻辑
  cover: '',
  thumbCover: ''
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
// 存储上传文件的详细信息 (id -> { originUrl, thumbUrl })，解决 Naive UI 文件列表可能丢失自定义属性的问题
const fileMapping = reactive({})

const formRules = {
  name: [
    { required: true, message: '请输入酒店名称', trigger: 'blur' },
    { min: 1, max: 100, message: '酒店名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入酒店地址', trigger: 'blur' }
  ],
  manager_name: [
    { required: true, message: '请输入负责人姓名', trigger: 'blur' }
  ],
  manager_phone: [
    { required: true, message: '请输入负责人电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  longitude: [
    { required: true, message: '请输入经度', trigger: 'blur' }
  ],
  latitude: [
    { required: true, message: '请输入纬度', trigger: 'blur' }
  ]
}

// 生成表格列
const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '酒店名称',
    key: 'name',
    width: 150,
    render(row) {
      return h('span', row.name)
    }
  },
  {
    title: '地址',
    key: 'address',
    width: 200,
    render(row) {
      return h('span', { style: { maxWidth: '200px', display: 'inline-block', wordBreak: 'break-all' } }, row.address)
    }
  },
  {
    title: '负责人',
    key: 'manager_name',
    width: 100,
    render(row) {
      return h('span', row.manager_name)
    }
  },
  {
    title: '联系电话',
    key: 'manager_phone',
    width: 120,
    render(row) {
      return h('span', row.manager_phone)
    }
  },
  {
    title: '封面图',
    key: 'cover',
    width: 120,
    render(row) {
      // 优先显示缩略图，预览显示原图
      const displayUrl = row.thumbCover || row.thumbImage || row.cover || row.image;
      const previewUrl = row.cover || row.image;

      if (displayUrl) {
        return h(NImage, {
          src: getImageUrl(displayUrl),
          previewSrc: getImageUrl(previewUrl),
          width: 80,
          height: 60,
          objectFit: 'cover',
          previewDisabled: false,
          fallbackSrc: '/placeholder.jpg'
        })
      }
      return h('span', '无')
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render(row) {
      return h('div', { style: 'display: flex; gap: 8px;' }, [
        h(NButton, {
          size: 'small',
          onClick: () => handleEdit(row)
        }, { default: () => '编辑' }),
        h(NPopconfirm, {
          onPositiveClick: () => handleDelete(row.id),
          placement: 'left'
        }, {
          trigger: () => h(NButton, {
            size: 'small',
            type: 'error',
            tertiary: true
          }, { default: () => '删除' }),
          default: () => '确定要删除这个酒店吗？'
        })
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

// 辅助函数：解析图片字符串
const parseImages = (imageStr) => {
  if (!imageStr) return []
  if (typeof imageStr !== 'string') return []
  return imageStr.split(',').filter(url => url && url.trim())
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getHotelPage(pagination.page, pagination.pageSize, searchForm.keyword)

    if (res.code !== 200) {
      message.error(res.message || '加载失败')
      return
    }

    const listData = res.data?.list || res.data?.records || res.data || []
    // 兼容多种API返回格式，特别是 PageResponse
    const totalItems = res.data?.total || res.data?.totalItems || res.pagination?.totalItems || res.pagination?.total || 0

    hotelList.value = listData.map(hotel => {
      // 解析图片字段
      // 假设 image 字段包含所有图片 (cover + details)，逗号分隔
      const images = parseImages(hotel.image)
      const thumbImages = parseImages(hotel.thumbImage || hotel.thumb_image)

      const cover = images.length > 0 ? images[0] : ''
      const thumbCover = thumbImages.length > 0 ? thumbImages[0] : ''

      return {
        ...hotel,
        manager_name: hotel.managerName || hotel.manager_name || '',
        manager_phone: hotel.managerPhone || hotel.manager_phone || '',
        image: hotel.image || '',
        thumbImage: hotel.thumbImage || hotel.thumb_image || '',
        cover, // 供列表显示使用
        thumbCover
      }
    })

    pagination.itemCount = totalItems

  } catch (error) {
    console.error('加载酒店列表失败:', error)
    message.error('加载酒店列表失败')
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
  Object.assign(hotelForm, {
    id: null,
    name: '',
    address: '',
    manager_name: '',
    manager_phone: '',
    description: '',
    longitude: '',
    latitude: '',
    image: '',
    thumbImage: '',
    userId: userStore.userInfo?.id || null,
    cover: '',
    thumbCover: ''
  })
  coverFileList.value = []
  detailFileList.value = []

  if (formRef.value) {
    formRef.value.restoreValidation()
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增酒店'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑酒店'
  dialogVisible.value = true
  try {
    dialogLoading.value = true
    const res = await getHotelById(row.id)

    if (res.code !== 200) {
      throw new Error(res.message || '获取详情失败')
    }

    const hotel = res.data

    // 映射 API 数据到表单
    Object.assign(hotelForm, {
      id: hotel.id,
      name: hotel.name,
      address: hotel.address,
      manager_name: hotel.managerName || hotel.manager_name || '',
      manager_phone: hotel.managerPhone || hotel.manager_phone || '',
      description: hotel.description,
      longitude: hotel.longitude,
      latitude: hotel.latitude,
      image: hotel.image || '',
      thumbImage: hotel.thumbImage || hotel.thumb_image || '',
      userId: hotel.userId || hotel.user_id
    })

    // 初始化文件列表
    const images = parseImages(hotelForm.image)
    const thumbImages = parseImages(hotelForm.thumbImage)

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

      hotelForm.cover = coverUrl
      hotelForm.thumbCover = coverThumbUrl
    } else {
      coverFileList.value = []
      hotelForm.cover = ''
      hotelForm.thumbCover = ''
    }

    // 详情图：取剩余的
    if (images.length > 1) {
      const detailUrls = images.slice(1)
      const detailThumbUrls = thumbImages.length > 1 ? thumbImages.slice(1) : detailUrls

      detailFileList.value = detailUrls.map((url, index) => ({
        id: `detail-${index}`,
        name: `详情图-${index + 1}`,
        status: 'finished',
        url: getImageUrl(detailThumbUrls[index] || url),
        originUrl: url,
        thumbUrl: detailThumbUrls[index] || url
      }))
    } else {
      detailFileList.value = []
    }

  } catch (error) {
    console.error('获取酒店详情失败:', error)
    message.error('获取酒店详情失败')
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

// --- 图片上传逻辑 ---

<<<<<<< HEAD
<<<<<<< HEAD
const handleCoverUpload = async ({ file, onFinish, onError }) => {
=======
=======
const beforeUpload = (data) => {
  if (data.file.file?.size > 5 * 1024 * 1024) {
    dialog.warning({
      title: '提示',
      content: '图片过大，请重新上传',
      positiveText: '确定'
    })
    return false
  }
  return true
}

>>>>>>> b7326086e0e75703cb5af249ab6dc25ee0902864
const handleCoverUpload = async ({file, onFinish, onError}) => {
>>>>>>> d19d7fd1954f1e828eae1b79e38d10b2d057ee79
  try {
    const res = await uploadFile(file.file);

    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url;
      const thumbUrl = res.data.thumbUrl || originUrl;

      // 记录到映射表
      fileMapping[file.id] = {originUrl, thumbUrl}

      // 更新文件列表中的URL，用于预览显示
      // 注意：这里我们查找并更新现有文件对象，而不是替换它
      const fileIndex = coverFileList.value.findIndex(f => f.id === file.id)
      if (fileIndex !== -1) {
        const fileItem = coverFileList.value[fileIndex]
        fileItem.url = getImageUrl(thumbUrl)
        fileItem.originUrl = originUrl
        fileItem.thumbUrl = thumbUrl
        fileItem.status = 'finished' // 显式设置，虽然 onFinish 也会设置
      } else {
        // 如果没找到（理论上不应该），则创建一个新的
        coverFileList.value = [{
          id: file.id,
          name: file.name,
          status: 'finished',
          url: getImageUrl(thumbUrl),
          originUrl: originUrl,
          thumbUrl: thumbUrl
        }]
      }

      // 更新表单辅助字段
      hotelForm.cover = originUrl
      hotelForm.thumbCover = thumbUrl

      // 强制更新 coverFileList 以确保视图刷新
      coverFileList.value = [...coverFileList.value]

      onFinish();
      message.success('封面图上传成功');
    } else {
      onError();
      message.error('上传失败：' + (res.message || '未知错误'));
    }
  } catch (error) {
    console.error('上传失败:', error);
    onError();
    message.error('上传失败');
  }
};

const handleCoverFileListChange = (newList) => {
  coverFileList.value = newList
  if (newList.length === 0) {
    hotelForm.cover = ''
    hotelForm.thumbCover = ''
  }
}

<<<<<<< HEAD
const handleDetailUpload = async ({ file, onFinish, onError }) => {
=======
const handleDetailUpload = async ({file, onFinish, onError}) => {
>>>>>>> d19d7fd1954f1e828eae1b79e38d10b2d057ee79
  try {
    const res = await uploadFile(file.file);

    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url;
      const thumbUrl = res.data.thumbUrl || originUrl;

      // 记录到映射表
      fileMapping[file.id] = {originUrl, thumbUrl}

      // 查找并更新文件属性
      const index = detailFileList.value.findIndex(f => f.id === file.id)
      if (index !== -1) {
        const fileItem = detailFileList.value[index]
        fileItem.url = getImageUrl(thumbUrl)
        fileItem.originUrl = originUrl
        fileItem.thumbUrl = thumbUrl
        fileItem.status = 'finished'
        // 强制触发更新
        detailFileList.value = [...detailFileList.value]
      } else {
        // 如果文件列表中找不到（可能是 v-model 更新延迟），手动添加
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

      onFinish();
    } else {
      onError();
      message.error('上传失败：' + (res.message || '未知错误'));
    }
  } catch (error) {
    console.error('上传失败:', error);
    onError();
    message.error('上传失败');
  }
};

const handleDetailFileListChange = (newList) => {
  detailFileList.value = newList
}

// --- 图片上传逻辑结束 ---

const handleDialogSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  try {
    dialogLoading.value = true
<<<<<<< HEAD
    
=======

>>>>>>> d19d7fd1954f1e828eae1b79e38d10b2d057ee79
    // 辅助函数：获取文件信息
    const getFileInfo = (file) => {
      // 1. 优先检查 fileMapping (本次会话上传的文件)
      if (fileMapping[file.id]) {
        return {
          url: fileMapping[file.id].originUrl,
          thumb: fileMapping[file.id].thumbUrl
        }
      }
      // 2. 检查文件对象自带属性 (回显的文件)
      if (file.originUrl) {
        return {
          url: file.originUrl,
          thumb: file.thumbUrl || file.originUrl
        }
      }
      // 3. 兜底：尝试解析 url
      let url = file.url || ''
      let thumb = file.thumbUrl || url

      if (url && url.startsWith('http')) {
        url = url.replace(config.fileBaseURL, '')
      }
      if (thumb && thumb.startsWith('http')) {
        thumb = thumb.replace(config.fileBaseURL, '')
      }

      return {url, thumb}
    }

    // 1. 提取封面图
    let coverUrl = ''
    let coverThumbUrl = ''

    // 优先从文件列表获取
    // 只要有 url 就可以，不强制 status === 'finished'，防止状态同步问题
    const coverFile = coverFileList.value.find(f => f.url || f.originUrl || fileMapping[f.id])
    if (coverFile) {
      const info = getFileInfo(coverFile)
      coverUrl = info.url
      coverThumbUrl = info.thumb
    } else {
      // 兜底：使用表单数据
      if (hotelForm.cover) {
        coverUrl = hotelForm.cover
        if (coverUrl.startsWith('http')) {
          coverUrl = coverUrl.replace(config.fileBaseURL, '')
        }
      }
      if (hotelForm.thumbCover) {
        coverThumbUrl = hotelForm.thumbCover
        if (coverThumbUrl.startsWith('http')) {
          coverThumbUrl = coverThumbUrl.replace(config.fileBaseURL, '')
        }
      }
    }

    // 2. 提取详情图
    const detailUrls = []
    const detailThumbUrls = []

    // 同样放宽条件，只要有 url 即可
    const validDetails = detailFileList.value.filter(f => f.url || f.originUrl || fileMapping[f.id])

    validDetails.forEach(file => {
      const info = getFileInfo(file)

      if (info.url) {
        detailUrls.push(info.url)
        detailThumbUrls.push(info.thumb || info.url)
      }
    })

    // 3. 合并
    const allImages = []
    const allThumbImages = []

    if (coverUrl) {
      allImages.push(coverUrl)
      allThumbImages.push(coverThumbUrl || coverUrl)
    }

    if (detailUrls.length > 0) {
      allImages.push(...detailUrls)
      allThumbImages.push(...detailThumbUrls)
    }

    const finalImageStr = allImages.join(',')
    const finalThumbImageStr = allThumbImages.join(',')

    console.log('Saving Hotel:', {
      coverUrl,
      detailUrls,
      finalImageStr
    })
<<<<<<< HEAD
    
=======

>>>>>>> d19d7fd1954f1e828eae1b79e38d10b2d057ee79
    const data = {
      name: hotelForm.name,
      address: hotelForm.address,
      managerName: hotelForm.manager_name,
      managerPhone: hotelForm.manager_phone,
      description: hotelForm.description,
      longitude: hotelForm.longitude,
      latitude: hotelForm.latitude,
      image: finalImageStr,
      thumbImage: finalThumbImageStr,
      userId: hotelForm.userId
    }

    // 兼容后端字段名
    data.manager_name = hotelForm.manager_name
    data.manager_phone = hotelForm.manager_phone

    let res
    if (hotelForm.id) {
      res = await updateHotel(hotelForm.id, data)
    } else {
      res = await createHotel(data)
    }

    if (res.code === 200) {
      message.success(hotelForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } else {
      message.error(res.message || '保存失败')
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
    const res = await deleteHotel(id)
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
.management-card {
  min-height: calc(100vh - 100px);
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: nowrap;
  gap: 16px;
}

.search-form {
  flex: 1;
  min-width: 0;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* 移动端样式 */
.mobile-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-card {
  margin-bottom: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.hotel-info {
  flex: 1;
  margin-right: 12px;
}

.hotel-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: bold;
  color: #1f2937;
}

.hotel-address {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hotel-cover {
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
  background-color: #f9fafb;
  padding: 8px;
  border-radius: 4px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  font-size: 13px;
  margin-bottom: 4px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  color: #6b7280;
  width: 70px;
  flex-shrink: 0;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.empty-state {
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

@media (max-width: 640px) {
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
    flex: 1;
  }
}
</style>
