<template>
  <div class="drama-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="关键词">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入剧名或关键词" clearable @keyup.enter="handleSearch" />
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
      
      <n-data-table
        :columns="columns"
        :data="dramaList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        :scroll-x="2000"
      />
    </n-card>
    
    <n-modal v-model:show="dialogVisible" preset="dialog" :title="dialogTitle" style="width: 90%; max-width: 800px">
      <n-form ref="formRef" :model="dramaForm" :rules="formRules" label-placement="left" label-width="100">
        <n-form-item label="剧名" path="name">
          <n-input v-model:value="dramaForm.name" placeholder="请输入剧名" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="dramaForm.type" :options="typeOptions" placeholder="请选择类型" />
        </n-form-item>
        <n-form-item label="集数" path="episodes">
          <n-input-number v-model:value="dramaForm.episodes" placeholder="请输入集数" :min="1" style="width: 100%" />
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input v-model:value="dramaForm.description" type="textarea" :rows="5" placeholder="请输入描述" />
        </n-form-item>
        <n-form-item label="演员" path="cast">
          <n-input v-model:value="dramaForm.cast" placeholder="请输入演员信息" />
        </n-form-item>
        <n-form-item label="拍摄地" path="shootLocation">
          <n-input v-model:value="dramaForm.shootLocation" placeholder="请输入拍摄地" />
        </n-form-item>
        <n-form-item label="服务" path="service">
          <n-input v-model:value="dramaForm.service" type="textarea" :rows="2" placeholder="请输入服务信息" />
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
              :src="dramaForm.thumbCover || dramaForm.cover"
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
import { ref, reactive, h, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NButton,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NSelect,
  NDataTable,
  NPopconfirm,
  NModal,
  NImage,
  NSpin,
  NPagination,
  NUpload,
  useMessage
} from 'naive-ui'
import { getDramaPage, addDrama, updateDrama, deleteDrama, getDramaById, uploadFile } from '@/api'
import { getImageUrl } from '@/utils/image'
import dayjs from 'dayjs'

const message = useMessage()

const loading = ref(false)
const dramaList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增电视剧')
const formRef = ref(null)
const coverFileList = ref([])
const imageFileList = ref([])

const searchForm = reactive({
  keyword: ''
})

const dramaForm = reactive({
  id: null,
  name: '',
  type: '',
  episodes: 0,
  description: ''
})

const typeOptions = [
  { label: '古装剧', value: 'COSTUME' },
  { label: '现代剧', value: 'MODERN' },
  { label: '年代剧', value: 'PERIOD' },
  { label: '其他', value: 'OTHER' }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  name: [{ required: true, message: '请输入剧名', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  episodes: [{ required: true, message: '请输入集数', trigger: 'blur' }]
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
      // 优先使用缩略封面，如果没有则使用封面，最后使用详情图片
      const coverUrl = row.thumbCover || row.cover || row.thumbImage || row.image
      if (!coverUrl) return '-'
      
      // 原图用于预览
      const originalUrl = row.cover || row.image || coverUrl
      
      return h(NImage, {
        width: 60,
        height: 45,
        src: getImageUrl(coverUrl),
        objectFit: 'cover',
        previewDisabled: false,
        showToolbar: false,
        // 配置预览功能，点击时显示原图
        srcset: [
          {
            src: getImageUrl(originalUrl),
            alt: '电视剧封面'
          }
        ],
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
    // 将 cover 映射到 image，thumbCover 映射到 thumbImage（后端只支持 image 和 thumbImage）
    const data = {
      ...dramaForm,
      image: dramaForm.cover || dramaForm.image, // 优先使用 cover，如果没有则使用 image
      thumbImage: dramaForm.thumbCover || dramaForm.thumbImage // 优先使用 thumbCover，如果没有则使用 thumbImage
    }
    
    let res
    if (dramaForm.id) {
      res = await updateDrama(dramaForm.id, data)
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
    if (res.code === 200) {
      dramaList.value = res.data || []
      pagination.itemCount = res.pagination?.totalItems || 0
    }
  } catch (error) {
    console.error('加载电视剧列表失败:', error)
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
    service: '',
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
      // 后端返回 cover 和 thumbCover（已添加），直接使用
      // 后端现在返回 cover 和 thumbCover 字段（已添加），直接使用
      Object.assign(dramaForm, {
        id: res.data.id,
        name: res.data.name || '',
        filingNum: res.data.filingNum || '',
        prodCompany: res.data.prodCompany || '',
        crewDescription: res.data.crewDescription || '',
        dramaDescription: res.data.dramaDescription || '',
        cast: res.data.cast || '',
        shootLocation: res.data.shootLocation || '',
        service: res.data.service || '',
        cover: res.data.cover || res.data.image || '', // 优先使用 cover，如果没有则使用 image
        image: res.data.image || '', // 详情图片
        thumbCover: res.data.thumbCover || res.data.thumbImage || '', // 优先使用 thumbCover，如果没有则使用 thumbImage
        thumbImage: res.data.thumbImage || '' // 缩略详情图
      })
      
      // 设置封面图片文件列表
      if (res.data.cover) {
        coverFileList.value = [{
          id: 'cover',
          name: 'cover.jpg',
          status: 'finished',
          url: res.data.thumbCover || res.data.cover
        }]
      } else {
        coverFileList.value = []
      }
      
      // 设置详情图片文件列表（支持多张）
      if (res.data.image) {
        const imageUrls = res.data.image.split(',').filter(url => url.trim())
        imageFileList.value = imageUrls.map((url, index) => ({
          id: `image-${index}`,
          name: `image-${index}.jpg`,
          status: 'finished',
          url: url.trim()
        }))
      } else {
        imageFileList.value = []
      }
      
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
      dramaForm.cover = res.data.originUrl || res.data.url
      dramaForm.thumbCover = res.data.thumbUrl || res.data.originUrl || res.data.url
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
      const imageUrl = res.data.originUrl || res.data.url
      // 添加到图片列表
      const existingImages = dramaForm.image ? dramaForm.image.split(',').filter(url => url.trim()) : []
      existingImages.push(imageUrl)
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
    .map(file => file.url)
  dramaForm.image = imageUrls.join(',')
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

// 移动端适配
@media (max-width: 768px) {
  .search-header {
    flex-direction: column;
    
    .search-form {
      width: 100%;
      min-width: auto;
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
  
  :deep(.n-data-table) {
    .n-data-table-wrapper {
      overflow-x: auto;
    }
  }
}

.action-bar {
  margin-bottom: 16px;
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
