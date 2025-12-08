<template>
  <div class="banner-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="标题">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入标题关键词" clearable @keyup.enter="handleSearch" />
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
            新增Banner
          </n-button>
        </div>
      </div>
      
      <!-- 桌面端表格 -->
      <n-data-table
        v-if="!isMobile"
        :columns="columns"
        :data="bannerList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        :scroll-x="1200"
      />
      
      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-list">
        <n-spin :show="loading">
          <div v-if="bannerList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:image-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
              v-for="banner in bannerList"
              :key="banner.id"
              class="mobile-card"
              hoverable
            >
              <div class="card-header">
                <div class="banner-info">
                  <h3 class="banner-title">{{ banner.imageName || banner.title }}</h3>
                  <p class="banner-link">{{ banner.targetModule || banner.link }}</p>
                </div>
                <div class="banner-image">
                  <n-image
                    v-if="banner.imageUrl"
                    :src="banner.imageUrl"
                    width="80"
                    height="60"
                    object-fit="cover"
                    preview-disabled
                  />
                  <div v-else class="no-image">
                    <Icon icon="mdi:image" :width="32" />
                  </div>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <span class="label">状态：</span>
                  <n-tag :type="(banner.status === true || banner.status === 1) ? 'success' : 'error'">
                    {{ (banner.status === true || banner.status === 1) ? '启用' : '禁用' }}
                  </n-tag>
                </div>
                <div class="info-item">
                  <span class="label">排序：</span>
                  <span>{{ banner.sort || banner.sortOrder }}</span>
                </div>
                <div class="info-item">
                  <span class="label">描述：</span>
                  <span>{{ banner.description || '-' }}</span>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(banner)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(banner.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个Banner吗？
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
      style="width: 90%; max-width: 800px"
      :mask-closable="false"
    >
      <n-form 
        ref="formRef" 
        :model="bannerForm" 
        :rules="formRules" 
        :label-placement="isMobile ? 'top' : 'left'"
        :label-width="isMobile ? 'auto' : '100'"
      >
        <n-form-item label="标题" path="title">
          <n-input v-model:value="bannerForm.title" placeholder="请输入Banner标题" />
        </n-form-item>
        <n-form-item label="链接" path="link">
          <n-input v-model:value="bannerForm.link" placeholder="请输入跳转链接" />
        </n-form-item>
        <n-form-item label="图片" path="imageUrl">
          <n-upload
            :max="1"
            :default-file-list="fileList"
            @update:file-list="handleFileListChange"
            @finish="handleUploadFinish"
            :custom-request="handleUpload"
          >
            <n-button>上传图片</n-button>
          </n-upload>
          <div v-if="bannerForm.imageUrl" style="margin-top: 12px;">
            <n-image
              :src="getImageUrl(bannerForm.imageUrl)"
              width="200"
              height="120"
              object-fit="cover"
            />
          </div>
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input v-model:value="bannerForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </n-form-item>
        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="bannerForm.sortOrder" placeholder="请输入排序值" :min="0" style="width: 100%;" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-switch v-model:value="statusSwitch" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
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
  useMessage,
  useDialog
} from 'naive-ui'
import request from '@/utils/request'
import { uploadFile } from '@/api'
import { getImageUrl } from '@/utils/image'
// Banner相关API函数
const getBannerPage = (current = 1, size = 10, keyword = '') => {
  return request({
    url: '/banner/page',
    method: 'get',
    params: {
      current,
      size,
      keyword
    }
  })
}

const getBannerById = (id) => {
  return request({
    url: `/banner/${id}`,
    method: 'get'
  })
}

const createBanner = (data) => {
  return request({
    url: '/banner',
    method: 'post',
    data
  })
}

const updateBanner = (id, data) => {
  return request({
    url: `/banner/update/${id}`,
    method: 'put',
    data
  })
}

const deleteBanner = (id) => {
  return request({
    url: `/banner/delete/${id}`,
    method: 'delete'
  })
}
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()

const isMobile = ref(false)
const loading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const bannerList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增Banner')
const formRef = ref(null)
const fileList = ref([])

const searchForm = reactive({
  keyword: ''
})

const bannerForm = reactive({
  id: null,
  title: '',
  link: '',
  imageUrl: '',
  description: '',
  sortOrder: 0,
  status: 1
})

const statusSwitch = computed({
  get: () => bannerForm.status === 1,
  set: (val) => {
    bannerForm.status = val ? 1 : 0
  }
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  title: [
    { required: true, message: '请输入Banner标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  imageUrl: [
    { required: true, message: '请上传图片', trigger: 'blur' }
  ]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  {
      title: '图片',
      key: 'imageUrl',
      width: 120,
      render: (row) => {
        // 优先使用缩略图URL（thumbUrl属性），如果没有则使用原图URL
        const thumbnailUrl = row.thumbUrl;
        // 获取原图URL用于预览
        const originalUrl = row.imageUrl || '';
        
        return h(NImage, {
          width: 80,
          height: 50,
          src: getImageUrl(thumbnailUrl || originalUrl), // 显示压缩后的图片
          objectFit: 'cover',
          previewDisabled: false, // 启用预览功能
          showToolbar: false,
          fallbackSrc: '/placeholder.jpg',
          // 配置预览功能，点击时显示原图
          srcset: [
            {
              src: getImageUrl(originalUrl),
              alt: 'Banner图片'
            }
          ]
        })
      }
    },
  { title: '标题', key: 'title', width: 200, ellipsis: { tooltip: true }, render: (row) => row.imageName || row.title || '-' },
  { title: '链接', key: 'link', width: 200, ellipsis: { tooltip: true }, render: (row) => row.targetModule || row.link || '-' },
  { title: '排序', key: 'sortOrder', width: 80, render: (row) => row.sort || row.sortOrder || 0 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const status = row.status === true || row.status === 1
      return h(NTag, {
        type: status ? 'success' : 'error'
      }, {
        default: () => status ? '启用' : '禁用'
      })
    }
  },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: (row) => {
      if (Array.isArray(row.createdAt)) {
        return dayjs(row.createdAt[0] + '-' + String(row.createdAt[1]).padStart(2, '0') + '-' + String(row.createdAt[2]).padStart(2, '0')).format('YYYY-MM-DD HH:mm:ss')
      }
      return row.createdAt ? dayjs(row.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'
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
    const res = await getBannerPage(pagination.page, pagination.pageSize, searchForm.keyword)
    if (res.code === 200) {
      bannerList.value = res.data || []
      pagination.itemCount = res.total || 0
    }
  } catch (error) {
    console.error('加载Banner列表失败:', error)
    message.error('加载Banner列表失败')
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
  dialogTitle.value = '新增Banner'
  Object.assign(bannerForm, {
    id: null,
    title: '',
    link: '',
    imageUrl: '',
    description: '',
    sortOrder: 0,
    status: 1
  })
  fileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getBannerById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑Banner'
      // 映射后端字段到前端字段
      Object.assign(bannerForm, {
        id: res.data.id,
        title: res.data.imageName || res.data.title || '', // imageName映射到title
        link: res.data.targetModule || res.data.link || '', // targetModule映射到link
        imageUrl: res.data.imageUrl || '',
        description: res.data.description || '',
        sortOrder: res.data.sort || res.data.sortOrder || 0, // sort映射到sortOrder
        status: res.data.status === true ? 1 : (res.data.status === false ? 0 : (res.data.status || 1)) // Boolean转换为数字
      })
      
      // 设置文件列表
      if (res.data.imageUrl) {
        fileList.value = [{
          id: '1',
          name: 'banner.jpg',
          status: 'finished',
          url: res.data.imageUrl
        }]
      } else {
        fileList.value = []
      }
      
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取Banner详情失败:', error)
    message.error('获取Banner详情失败')
  }
}

const handleFileListChange = (files) => {
  fileList.value = files
}

const handleUploadFinish = ({ file, event }) => {
  if (event?.target?.response) {
    try {
      const res = JSON.parse(event.target.response)
      if (res.code === 200) {
        bannerForm.imageUrl = res.data.url
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
      bannerForm.imageUrl = res.data.originUrl || res.data.url
      onFinish()
      message.success('图片上传成功')
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
    // 映射前端字段到后端DTO字段
    const data = {
      imageName: bannerForm.title || bannerForm.imageName, // title映射到imageName
      imageUrl: bannerForm.imageUrl,
      targetModule: bannerForm.link || bannerForm.targetModule, // link映射到targetModule
      sort: bannerForm.sortOrder || bannerForm.sort || 0, // sortOrder映射到sort
      status: bannerForm.status === 1 ? true : (bannerForm.status === 0 ? false : bannerForm.status) // 转换为Boolean
    }
    
    let res
    if (bannerForm.id) {
      res = await updateBanner(bannerForm.id, data)
    } else {
      res = await createBanner(data)
    }
    
    if (res.code === 200) {
      message.success(bannerForm.id ? '更新成功' : '创建成功')
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
    const res = await deleteBanner(id)
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
.banner-management {
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
      
      .banner-info {
          flex: 1;
          margin-right: 12px;
          
          .banner-title {
            font-size: 16px;
            font-weight: 600;
            color: #1f2937;
            margin: 0 0 4px 0;
          }
          
          .banner-link {
            font-size: 14px;
            color: #6b7280;
            margin: 0;
            word-break: break-all;
          }
        }
        
        .banner-image {
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
          
          // 针对图片添加点击放大功能
          .n-image {
            cursor: zoom-in;
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