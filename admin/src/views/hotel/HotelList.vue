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
      <n-data-table
        v-if="!isMobile"
        :columns="columns"
        :data="hotelList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        :scroll-x="1400"
      />
      
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
                <div class="hotel-image">
                  <n-image
                    v-if="hotel.imageUrl"
                    :src="hotel.imageUrl"
                    width="80"
                    height="60"
                    object-fit="cover"
                    preview-disabled
                  />
                  <div v-else class="no-image">
                    <Icon icon="mdi:hotel" :width="32" />
                  </div>
                </div>
              </div>
              
              <!-- 正文图片预览 -->
              <div v-if="hotel.contentImages && hotel.contentImages.length > 0" class="content-images-preview">
                <div class="image-label">正文图片：</div>
                <div class="image-grid">
                  <n-image
                    v-for="(img, index) in hotel.contentImages.slice(0, 3)"
                    :key="index"
                    :src="img"
                    width="50"
                    height="40"
                    object-fit="cover"
                    preview-disabled
                    style="margin-right: 4px; margin-bottom: 4px;"
                  />
                  <div v-if="hotel.contentImages.length > 3" class="more-images">
                    +{{ hotel.contentImages.length - 3 }}
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
        <n-form-item label="封面图片" path="cover">
          <n-upload
            :max="1"
            :default-file-list="fileList"
            @update:file-list="handleFileListChange"
            @finish="handleUploadFinish"
            :custom-request="handleUpload"
          >
            <n-button>上传封面图片</n-button>
          </n-upload>
          <div v-if="hotelForm.imageUrl" style="margin-top: 12px;">
            <n-image
              :src="hotelForm.imageUrl"
              width="200"
              height="120"
              object-fit="cover"
              fallback-src="/placeholder.jpg"
            />
          </div>
        </n-form-item>
        
        <!-- 添加正文图片上传组件 -->
        <n-form-item label="正文图片" path="images">
          <n-upload
            :max="9"
            :default-file-list="contentFileList"
            @update:file-list="handleContentFileListChange"
            @finish="handleContentUploadFinish"
            :custom-request="handleContentUpload"
          >
            <n-button>上传正文图片（最多9张）</n-button>
          </n-upload>
          <div v-if="contentFileList.length > 0" style="margin-top: 12px; display: flex; flex-wrap: wrap; gap: 8px;">
            <div v-for="(file, index) in contentFileList" :key="index" style="position: relative;">
              <n-image
                :src="file.url || file.thumbnailUrl"
                width="100"
                height="80"
                object-fit="cover"
                fallback-src="/placeholder.jpg"
              />
              <n-button 
                size="tiny" 
                type="error" 
                quaternary 
                circle 
                style="position: absolute; top: -8px; right: -8px;"
                @click="contentFileList.splice(index, 1)"
              >
                <template #icon>
                  <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                </template>
              </n-button>
            </div>
          </div>
        </n-form-item>
        
        <!-- 添加thumb_image图片显示区域 -->
        <n-form-item label="缩略图（thumb_image）">
          <div v-if="thumbImageList.length > 0" style="margin-top: 12px; display: flex; flex-wrap: wrap; gap: 8px;">
            <div v-for="(file, index) in thumbImageList" :key="index" style="position: relative;">
              <n-image
                :src="file.url"
                width="100"
                height="80"
                object-fit="cover"
                fallback-src="/placeholder.jpg"
                :preview-disabled="false"
              />
              <n-button 
                size="tiny" 
                type="error" 
                quaternary 
                circle 
                style="position: absolute; top: -8px; right: -8px;"
                @click="thumbImageList.splice(index, 1)"
              >
                <template #icon>
                  <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                </template>
              </n-button>
            </div>
          </div>
          <div v-else style="margin-top: 12px; color: #9ca3af;">
            暂无缩略图
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
import { getHotelPage, createHotel, updateHotel, deleteHotel, getHotelById } from '@/api'
import dayjs from 'dayjs'

const message = useMessage()

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
const fileList = ref([])
// 添加正文图片的文件列表
const contentFileList = ref([])
// 添加缩略图列表
const thumbImageList = ref([])

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
  user_id: '',
  cover: '',
  image: '',
  thumb_cover: '',
  thumb_image: '',
  deleted: 0,
  created_at: '',
  updated_at: '',
  imageUrl: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

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



const columns = [
  { title: 'ID', key: 'id', width: 80 },
  {
      title: '封面图片',
      key: 'imageUrl',
      width: 100,
      render: (row) => {
        // 优先使用缩略图URL（thumbUrl属性），如果没有则使用原图URL
        const imgUrl = row.thumbUrl || row.imageUrl;
        // 获取原图URL用于预览
        const originalUrl = row.imageUrl || imgUrl;
        
        return h(NImage, {
          width: 60,
          height: 45,
          src: imgUrl, // 显示压缩后的图片
          objectFit: 'cover',
          previewDisabled: false, // 启用预览功能
          fallbackSrc: '/placeholder.jpg',
          // 配置预览功能，点击时显示原图
          srcset: [
            {
              src: originalUrl, // 预览时显示未压缩的原图
              alt: '酒店封面图片'
            }]
          })
      }
    },
    {
      title: '正文图片',
      key: 'contentImages',
      width: 150,
      render: (row) => {
        if (!row.contentImages || row.contentImages.length === 0) {
          return '-'  
        }
        return h('div', { style: 'display: flex; gap: 4px;' },
          row.contentImages.slice(0, 3).map((img, index) => {
            // 检查是否有对应的缩略图URL数组
            const imgUrl = (row.contentThumbUrls && row.contentThumbUrls[index]) || img;
            // 获取原图URL用于预览
            const originalUrl = img; // 假设contentImages中存储的是原图URL
            
            return h(NImage, {
              width: 40,
              height: 30,
              src: imgUrl, // 显示压缩后的图片
              objectFit: 'cover',
              previewDisabled: false, // 启用预览功能
              fallbackSrc: '/placeholder.jpg',
              style: { borderRadius: '2px' },
              // 配置预览功能，点击时显示原图
              srcset: [
                {
                  src: originalUrl, // 预览时显示未压缩的原图
                  alt: `正文图片${index + 1}`
                }
              ]
            })
          })
        )
      }
    },
  { title: '酒店名称', key: 'name', width: 200, ellipsis: { tooltip: true } },
  { title: '地址', key: 'address', width: 250, ellipsis: { tooltip: true } },
  { title: '负责人', key: 'manager_name', width: 100 },
  { title: '负责人电话', key: 'manager_phone', width: 120 },
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
    const res = await getHotelPage(pagination.page, pagination.pageSize, searchForm.keyword)
    
    // 添加调试日志，打印原始响应数据
    console.log('原始响应数据:', res)
    
    // 处理响应数据，确保兼容性
    let dataList = []
    let totalCount = 0
    
    // 检查响应结构，支持直接返回数据对象的情况
    if (res) {
      // 优先使用res.data，如果不存在则直接使用res
      dataList = res.data || res || []
      // 尝试从不同可能的位置获取总数
      totalCount = res.pagination?.totalItems || res.total || res.pagination?.total || 0
    }
    
    // 打印处理前的数据列表
    console.log('处理前的数据列表:', dataList)
    
    // 处理酒店数据，转换后端字段到前端需要的格式
    hotelList.value = dataList.map(hotel => ({
      ...hotel,
      // 映射图片资源，严格区分封面和正文图片
      imageUrl: hotel.cover || hotel.thumb_cover || '', // 封面图片只使用cover或thumb_cover
      contentImages: hotel.image && typeof hotel.image === 'string' ? hotel.image.split(',') : [], // 正文图片转换为数组
      // 添加字段映射，处理驼峰命名和下划线命名的兼容性
      manager_name: hotel.manager_name || hotel.managerName || '',
      manager_phone: hotel.manager_phone || hotel.managerPhone || ''
    }))
    
    // 打印处理后的数据列表
    console.log('处理后的数据列表:', hotelList.value)

    pagination.itemCount = totalCount
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

const handleAdd = () => {
  dialogTitle.value = '新增酒店'
  Object.assign(hotelForm, {
    id: null,
    name: '',
    address: '',
    manager_name: '',
    manager_phone: '',
    longitude: '',
    latitude: '',
    description: '',
    cover: '',
    image: '',
    thumb_cover: '',
    thumb_image: '',
    user_id: '',
    deleted: 0,
    imageUrl: ''
  })
  fileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑酒店'
  dialogVisible.value = true
  try {
    dialogLoading.value = true
    const res = await getHotelById(row.id)
    console.log('原始响应数据:', res)
    const hotel = res.data || res
    console.log('处理前的酒店数据:', hotel)
    
    // 重置表单数据
    Object.assign(hotelForm, {
      ...hotel,
      manager_name: hotel.manager_name || hotel.managerName || '',
      manager_phone: hotel.manager_phone || hotel.managerPhone || ''
    })
    
    // 设置封面图片
    if (hotel.cover || hotel.imageUrl || hotel.image) {
      hotelForm.imageUrl = hotel.cover || hotel.imageUrl || hotel.image
      fileList.value = [{
        url: hotelForm.imageUrl,
        thumbnailUrl: hotelForm.imageUrl,
        name: '封面图片'
      }]
    } else {
      fileList.value = []
    }
    
    // 设置正文图片
    contentFileList.value = []
    if (hotel.image && typeof hotel.image === 'string') {
      const imageUrls = hotel.image.split(',')
      contentFileList.value = imageUrls.map(url => ({
        url: url,
        thumbnailUrl: url,
        name: '正文图片'
      }))
    }
    
    // 设置缩略图(thumb_image)
    thumbImageList.value = []
    if (hotel.thumb_image && typeof hotel.thumb_image === 'string') {
      const thumbImageUrls = hotel.thumb_image.split(',')
      thumbImageList.value = thumbImageUrls.map(url => ({
        url: url,
        name: '缩略图'
      }))
    }
    
    console.log('处理后的数据:', hotelForm)
  } catch (error) {
    console.error('获取酒店详情失败:', error)
    message.error('获取酒店详情失败')
  } finally {
    dialogLoading.value = false
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
        hotelForm.imageUrl = res.data.url
      } else {
        message.error('上传失败：' + res.message)
      }
    } catch (error) {
      message.error('上传响应解析失败')
    }
  }
}

const handleUpload = ({ file, onFinish, onError }) => {
  const formData = new FormData()
  formData.append('file', file.file)
  
  // 这里应该调用上传API，暂时模拟
  setTimeout(() => {
    hotelForm.imageUrl = URL.createObjectURL(file.file)
    onFinish()
  }, 1000)
}

const handleContentFileListChange = (fileList) => {
  contentFileList.value = fileList
}

const handleContentUploadFinish = () => {
  message.success('正文图片上传成功')
}

const handleContentUpload = ({ file, onFinish, onError }) => {
  const formData = new FormData()
  formData.append('file', file.file)
  
  // 模拟上传过程
  setTimeout(() => {
    const fileUrl = URL.createObjectURL(file.file)
    file.url = fileUrl
    file.thumbnailUrl = fileUrl
    onFinish()
  }, 1000)
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
    // 收集正文图片URL
    const contentImageUrls = contentFileList.value
      .filter(file => file.url)
      .map(file => file.url)
      .join(',') // 使用逗号分隔多张图片URL
    
    // 收集缩略图URL
    const thumbImageUrls = thumbImageList.value
      .filter(file => file.url)
      .map(file => file.url)
      .join(',') // 使用逗号分隔多张图片URL
    
    const data = {
      name: hotelForm.name,
      address: hotelForm.address,
      manager_phone: hotelForm.manager_phone || hotelForm.phone || '',
      manager_name: hotelForm.manager_name || '',
      description: hotelForm.description,
      // 严格按照SQL字段结构使用图片字段
      cover: hotelForm.cover || hotelForm.imageUrl || '',
      image: hotelForm.image || contentImageUrls || '',
      thumb_cover: hotelForm.thumb_cover || '',
      thumb_image: thumbImageUrls || hotelForm.thumb_image || '',
      longitude: hotelForm.longitude || '',
      latitude: hotelForm.latitude || '',
      user_id: hotelForm.user_id || '',
      deleted: hotelForm.deleted || 0
    }
    
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
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}
const resetForm = () => {
  // 重置表单数据
  hotelForm.id = ''
  hotelForm.name = ''
  hotelForm.address = ''
  hotelForm.manager_name = ''
  hotelForm.manager_phone = ''
  hotelForm.description = ''
  hotelForm.imageUrl = ''
  hotelForm.cover = ''
  hotelForm.image = ''
  hotelForm.thumb_cover = ''
  hotelForm.thumb_image = ''
  hotelForm.longitude = ''
  hotelForm.latitude = ''
  hotelForm.user_id = ''
  hotelForm.deleted = 0
  
  // 清空文件列表
  fileList.value = []
  contentFileList.value = []
  
  // 重置表单验证
  if (formRef.value) {
    formRef.value.reset()
  }
}
</script>

<style scoped lang="scss">
.hotel-management {
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
      
      .hotel-info {
        flex: 1;
        margin-right: 12px;
        
        .hotel-name {
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 4px 0;
        }
        
        .hotel-address {
          font-size: 14px;
          color: #6b7280;
          margin: 0;
          word-break: break-all;
        }
      }
      
      .hotel-image {
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
        
        .facilities-info {
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

.card-content {
  margin-top: 12px;
}

.content-images-preview {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.image-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.image-grid {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.more-images {
  width: 50px;
  height: 40px;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  border-radius: 2px;
  margin-left: 4px;
}

.info-item {
  display: flex;
  font-size: 13px;
  
  .label {
    color: #6b7280;
    min-width: 60px;
    flex-shrink: 0;
  }
  
  .facilities-info {
    color: #374151;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>