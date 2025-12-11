<template>
  <div class="tour-route-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="线路名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入线路名称" clearable @keyup.enter="handleSearch" />
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
            新增线路
          </n-button>
        </div>
      </div>
      
      <n-data-table
        :columns="columns"
        :data="tourRouteList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        :scroll-x="2000"
      />
    </n-card>
    
    <n-modal v-model:show="dialogVisible" preset="dialog" :title="dialogTitle" style="width: 900px">
      <n-form ref="formRef" :model="tourRouteForm" :rules="formRules" label-placement="left" label-width="100">
        <n-form-item label="线路名称" path="name">
          <n-input v-model:value="tourRouteForm.name" placeholder="请输入线路名称" />
        </n-form-item>
        <n-form-item label="线路描述" path="description">
          <n-input v-model:value="tourRouteForm.description" type="textarea" :rows="4" placeholder="请输入线路描述" />
        </n-form-item>
        <n-form-item label="主题标签" path="theme">
          <n-input v-model:value="tourRouteForm.theme" placeholder="如：熊猫文化、茶文化等" />
        </n-form-item>
        <n-form-item label="特色描述" path="features">
          <n-input v-model:value="tourRouteForm.features" type="textarea" :rows="3" placeholder="请输入线路特色" />
        </n-form-item>
        <n-form-item label="交通方式" path="transportInfo">
          <n-input v-model:value="tourRouteForm.transportInfo" type="textarea" :rows="3" placeholder="请输入交通方式信息" />
        </n-form-item>
        <n-form-item label="住宿推荐" path="accommodation">
          <n-input v-model:value="tourRouteForm.accommodation" type="textarea" :rows="3" placeholder="请输入住宿推荐" />
        </n-form-item>
        <n-form-item label="美食推荐" path="foodRecommendation">
          <n-input v-model:value="tourRouteForm.foodRecommendation" type="textarea" :rows="3" placeholder="请输入美食推荐" />
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
          <div v-if="tourRouteForm.cover" style="margin-top: 12px;">
            <n-image
              :src="getImageUrl(tourRouteForm.thumbCover || tourRouteForm.cover)"
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
        <n-form-item label="状态" path="status">
          <n-select v-model:value="tourRouteForm.status" :options="statusOptions" />
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
import { ref, reactive, h, onMounted } from 'vue'
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
  NSelect,
  NUpload,
  useMessage,
  NImage,
  NTag
} from 'naive-ui'
import { getTourRoutePage, addTourRoute, updateTourRoute, deleteTourRoute, getTourById, uploadFile } from '@/api'
import { getImageUrl } from '@/utils/image'

const message = useMessage()

const loading = ref(false)
const tourRouteList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增线路')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const tourRouteForm = reactive({
  id: null,
  name: '',
  description: '',
  theme: '',
  features: '',
  transportInfo: '',
  accommodation: '',
  foodRecommendation: '',
  cover: '',
  image: '',
  thumbCover: '',
  thumbImage: '',
  status: 1
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

const statusOptions = [
  { label: '上线', value: 1 },
  { label: '下线', value: 0 }
]

const formRules = {
  name: [{ required: true, message: '请输入线路名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入线路描述', trigger: 'blur' }],
  theme: [{ required: true, message: '请输入主题标签', trigger: 'blur' }],
  features: [{ required: true, message: '请输入特色描述', trigger: 'blur' }]
}

const columns = [
  { title: 'ID', key: 'id', width: 80, fixed: 'left' },
  { title: '线路名称', key: 'name', width: 180, ellipsis: { tooltip: true }, fixed: 'left' },
  {
    title: '封面',
    key: 'cover',
    width: 100,
    render: (row) => {
      if (!row.cover) return '-'
      return h(NImage, {
        width: 60,
        height: 45,
        src: getImageUrl(row.cover),
        objectFit: 'cover',
        style: { borderRadius: '4px' }
      })
    }
  },
  { title: '主题', key: 'theme', width: 120 },
  { title: '线路描述', key: 'description', width: 250, ellipsis: { tooltip: true } },
  { title: '特色', key: 'features', width: 200, ellipsis: { tooltip: true } },
  { title: '交通方式', key: 'transportInfo', width: 200, ellipsis: { tooltip: true } },
  { title: '住宿推荐', key: 'accommodation', width: 200, ellipsis: { tooltip: true } },
  { title: '美食推荐', key: 'foodRecommendation', width: 200, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const isActive = row.status === 1
      return h(NTag, { 
        type: isActive ? 'success' : 'default', 
        size: 'small' 
      }, { 
        default: () => isActive ? '上线' : '下线' 
      })
    }
  },
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

onMounted(() => {
  loadData()
})

const loadData = async () => {
  try {
    loading.value = true
    
    console.log('开始加载旅游线路数据，参数:', {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword
    })
    
    // 直接调用后端API获取真实数据
    const res = await getTourRoutePage(pagination.page, pagination.pageSize, searchForm.keyword)
    console.log('后端API响应数据:', JSON.stringify(res))
    
    if (res.code === 200) {
      // PageResponse的数据结构：data是数组，pagination包含分页信息
      tourRouteList.value = res.data || []
      if (res.pagination) {
        pagination.itemCount = res.pagination.totalItems || 0
        // 自动计算总页数
        pagination.pageCount = Math.ceil(pagination.itemCount / pagination.pageSize) || 1
        pagination.page = res.pagination.currentPage || 1
        pagination.pageSize = res.pagination.pageSize || 10
      }
      console.log('数据加载成功，获取到', tourRouteList.value.length, '条记录')
    } else {
      console.error('API返回非成功状态:', res.code, res.message)
      message.error(res.message || '加载失败')
    }
  } catch (error) {
    console.error('加载线路列表失败:', error)
    console.error('错误详情:', error.message, error.response)
    message.error('加载线路列表失败：' + (error.message || '未知错误'))
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
  dialogTitle.value = '新增线路'
  Object.assign(tourRouteForm, {
    id: null,
    name: '',
    description: '',
    theme: '',
    features: '',
    transportInfo: '',
    accommodation: '',
    foodRecommendation: '',
    cover: '',
    image: '',
    thumbCover: '',
    thumbImage: '',
    status: 1
  })
  coverFileList.value = []
  imageFileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getTourById(row.id)
    console.log('获取线路详情响应:', res)
    if (res.code === 200) {
      if (res.data) {
        dialogTitle.value = '编辑线路'

        // 分离封面图片和详情图片
        const imageUrls = res.data.image ? res.data.image.split(',').filter(url => url.trim()) : []
        const thumbUrls = res.data.thumbImage ? res.data.thumbImage.split(',').filter(url => url.trim()) : []

        const coverImage = imageUrls.length > 0 ? imageUrls[0] : (res.data.cover || '')
        const coverThumb = thumbUrls.length > 0 ? thumbUrls[0] : (res.data.thumbCover || '')
        const detailImages = imageUrls.slice(1)
        const detailThumbs = thumbUrls.slice(1)

        Object.assign(tourRouteForm, {
          ...res.data,
          cover: coverImage,
          thumbCover: coverThumb,
          image: detailImages.join(','),
          thumbImage: detailThumbs.join(',')
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
      } else {
        console.error('获取线路详情失败: 数据为空', res)
        message.error('获取详情失败：数据为空')
      }
    } else {
      // 处理业务逻辑错误
      console.error('获取线路详情失败: 业务错误', res.code, res.message)
      message.error(res.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取线路详情失败:', error)
    console.error('错误详情:', error.response || error.message)
    const errorMsg = error.response?.data?.message || error.message || '未知错误'
    message.error('获取线路详情失败：' + errorMsg)
  }
}

// 处理封面图片上传
const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl
      
      tourRouteForm.cover = originUrl
      tourRouteForm.thumbCover = thumbUrl
      
      // 更新文件列表中的URL，用于预览显示
      const fileIndex = coverFileList.value.findIndex(f => f.id === file.id || f.name === file.name)
      if (fileIndex !== -1) {
        coverFileList.value[fileIndex].url = thumbUrl
        coverFileList.value[fileIndex].status = 'finished'
      } else {
        coverFileList.value.push({
          id: file.id || 'cover',
          name: file.name || 'cover.jpg',
          status: 'finished',
          url: thumbUrl
        })
      }
      
      onFinish({
        url: thumbUrl
      })
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
    tourRouteForm.cover = ''
    tourRouteForm.thumbCover = ''
  }
}

// 处理详情图片上传
const handleImageUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl

      // 将新上传的图片添加到现有图片列表
      const currentImages = tourRouteForm.image ? tourRouteForm.image.split(',').filter(url => url.trim()) : []
      const currentThumbs = tourRouteForm.thumbImage ? tourRouteForm.thumbImage.split(',').filter(url => url.trim()) : []

      currentImages.push(originUrl)
      currentThumbs.push(thumbUrl)

      tourRouteForm.image = currentImages.join(',')
      tourRouteForm.thumbImage = currentThumbs.join(',')

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
    tourRouteForm.image = imageUrls.join(',')
  } else {
    tourRouteForm.image = ''
    tourRouteForm.thumbImage = ''
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

    // 合并封面和详情图片为逗号分隔的字符串
    const allImages = []
    const allThumbs = []

    // 先添加封面图片
    if (tourRouteForm.cover) {
      allImages.push(tourRouteForm.cover)
      allThumbs.push(tourRouteForm.thumbCover || tourRouteForm.cover)
    }

    // 再添加详情图片
    if (tourRouteForm.image) {
      const detailImages = tourRouteForm.image.split(',').filter(url => url.trim())
      const detailThumbs = tourRouteForm.thumbImage ? tourRouteForm.thumbImage.split(',').filter(url => url.trim()) : []
      allImages.push(...detailImages)
      allThumbs.push(...detailThumbs)
    }

    const data = {
      name: tourRouteForm.name,
      description: tourRouteForm.description,
      theme: tourRouteForm.theme,
      features: tourRouteForm.features,
      transportInfo: tourRouteForm.transportInfo,
      accommodation: tourRouteForm.accommodation,
      foodRecommendation: tourRouteForm.foodRecommendation,
      cover: tourRouteForm.cover,
      image: allImages.join(','),
      thumbCover: tourRouteForm.thumbCover,
      thumbImage: allThumbs.join(','),
      status: tourRouteForm.status
    }
    
    let res
    if (tourRouteForm.id) {
      res = await updateTourRoute({ ...data, id: tourRouteForm.id })
    } else {
      res = await addTourRoute(data)
    }
    
    if (res.code === 200) {
      message.success(tourRouteForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    dialogLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    const res = await deleteTourRoute(id)
    if (res.code === 200) {
      message.success('删除成功')
      loadData()
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}
</script>

<style scoped lang="scss">
.tour-route-management {
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
}

.search-form {
  flex: 1;
}

.action-buttons {
  display: flex;
  align-items: center;
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

