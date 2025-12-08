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
                      v-if="hotel.thumbImage || hotel.imageUrl"
                      :src="getImageUrl(hotel.thumbImage || hotel.imageUrl)"
                      :preview-src="getImageUrl(hotel.imageUrl)"
                      width="80"
                      height="60"
                      object-fit="cover"
                      preview-disabled
                      class="hotel-image-content"
                  />
                  <div v-else class="no-image">
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
        <!-- 封面图片上传 -->
        <n-form-item label="封面图片" path="image">
          <n-upload
              :multiple="false"
              :file-list-style="{ maxHeight: '180px' }"
              :file-list="coverFileList"
              @update:file-list="handleCoverFileListChange"
              :custom-request="handleCoverUpload"
              accept="image/*"
          >
            <div v-if="!hotelForm.image" class="upload-trigger">
              <Icon icon="mdi:upload" />
              <span>点击上传封面图片</span>
            </div>
            <div v-if="hotelForm.image">
              <n-image
                  :src="getImageUrl(hotelForm.thumbImage || hotelForm.image)"
                  :preview-src="getImageUrl(hotelForm.image)"
                  :preview-disabled="false"
                  class="form-image-preview"
                  style="max-height: 120px; max-width: 100%; width: auto;"
                  @click.stop
              />
              <!-- 修改封面：点击后选择新图并上传 -->
              <n-upload
                  :show-file-list="false"
                  :multiple="false"
                  accept="image/*"
                  :custom-request="handleReplaceCoverUpload"
              >
                <n-button type="primary" text>
                  <Icon icon="mdi:pencil-outline" />
                  修改封面
                </n-button>
              </n-upload>
            </div>
          </n-upload>
        </n-form-item>

        <!-- 缩略图上传 -->
        <n-form-item label="缩略图" path="thumbImage">
          <n-upload
              :multiple="false"
              :file-list="thumbFileList"
              @update:file-list="handleThumbFileListChange"
              :custom-request="handleThumbUpload"
              accept="image/*"
          >
            <n-button>上传缩略图</n-button>
          </n-upload>
          <div v-if="hotelForm.thumbImage" style="margin-top: 12px;">
            <n-image
                :src="getImageUrl(hotelForm.thumbImage)"
                width="200"
                height="120"
                object-fit="cover"
                class="form-image-preview"
                style="max-width: 200px; max-height: 120px;"
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
import request from '@/utils/request'
import { getImageUrl } from '@/utils/image'

// 酒店相关API函数（使用你提供的）
const getHotelPage = (currentPage = 1, pageSize = 10, keyword = '') => {
  return request({
    url: '/hotel/admin/page',
    method: 'get',
    params: {
      currentPage,  // ✅ 修正参数名，匹配后端期望
      pageSize,     // ✅ 修正参数名
      keyword
    }
  })

  // 替换封面图（覆盖 image + thumbImage）
  const handleReplaceCoverUpload = async ({ file, onFinish, onError }) => {
    try {
      // 准备要发送的数据，包括旧图片的URL
      const formData = new FormData();
      formData.append('file', file.file);
      formData.append('oldImageUrl', hotelForm.image); // 假设服务器端会根据这个参数删除旧图片

      // 调用你的通用上传接口
      const res = await uploadFile(formData);

      if (res.code === 200 && res.data) {
        const originUrl = res.data.originUrl || res.data.url;
        const thumbUrl = res.data.thumbUrl || originUrl; // 如果后端没返回缩略图，用原图兜底

        // ✅ 直接覆盖表单中的封面和缩略图
        hotelForm.image = originUrl;
        hotelForm.thumbImage = thumbUrl;

        // 更新文件列表（用于回显或后续校验）
        coverFileList.value = [{
          id: 'cover',
          name: file.name,
          status: 'finished',
          url: originUrl
        }];

        onFinish();
        message.success('封面已更新');
      } else {
        onError();
        message.error('上传失败：' + (res.message || '未知错误'));
      }
    } catch (error) {
      console.error('封面上传失败:', error);
      onError();
      message.error('上传失败，请重试');
    }
  }
}

const getHotelById = (id) => {
  return request({
    url: `/hotel/${id}`,
    method: 'get'
  })
}

const createHotel = (data) => {
  return request({
    url: '/hotel/admin/create',
    method: 'post',
    data
  })
}

const updateHotel = (id, data) => {
  return request({
    url: `/hotel/admin/update/${id}`,
    method: 'put',
    data
  })
}

const deleteHotel = (id) => {
  return request({
    url: `/hotel/admin/delete/${id}`,
    method: 'delete'
  })
}

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
  image: '',        // 对应数据库 image 字段
  thumbImage: ''    // 对应数据库 thumb_image 字段
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
const thumbFileList = ref([])

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
    render(row) {
      return h('span', row.name)
    }
  },
  {
    title: '地址',
    key: 'address',
    render(row) {
      return h('span', { style: { maxWidth: '200px', display: 'inline-block', wordBreak: 'break-all' } }, row.address)
    }
  },
  {
    title: '负责人',
    key: 'manager_name',
    render(row) {
      return h('span', row.manager_name)
    }
  },
  {
    title: '联系电话',
    key: 'manager_phone',
    render(row) {
      return h('span', row.manager_phone)
    }
  },
  {
    title: '封面图',
    key: 'imageUrl',
    width: 120,
    render(row) {
      if (row.thumbImage) {
        return h(NImage, {
          src: getImageUrl(row.thumbImage),
          previewSrc: getImageUrl(row.imageUrl), // 点击预览用原图
          width: 80,
          height: 60,
          objectFit: 'cover',
          previewDisabled: false // 允许预览
        })
      } else if (row.imageUrl) {
        // 如果没有缩略图，退化到原图
        return h(NImage, {
          src: getImageUrl(row.imageUrl),
          width: 80,
          height: 60,
          objectFit: 'cover',
          previewDisabled: false
        })
      }
      return h('span', '无')
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render(row) {
      return [
        h(NButton, {
          size: 'small',
          type: 'primary',
          style: { marginRight: '8px' },
          onClick: () => handleEdit(row)
        }, { default: () => '编辑' }),
        h(NButton, {
          size: 'small',
          type: 'error',
          onClick: () => handleDelete(row.id)
        }, { default: () => '删除' })
      ]
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

// ✅ 修正后的 loadData 方法 - 匹配后端实际返回格式
const loadData = async () => {
  try {
    loading.value = true
    const res = await getHotelPage(pagination.page, pagination.pageSize, searchForm.keyword)

    console.log('✅ API响应:', res)

    if (res.code !== 200) {
      message.error(res.message || '加载失败')
      return
    }

    // ✅ 修正：后端实际返回格式
    const hotelListData = res.data || []  // 直接是酒店数组
    const paginationInfo = res.pagination || {}

    // ✅ 修正：映射后端字段（驼峰转下划线）
    hotelList.value = hotelListData.map(hotel => ({
      ...hotel,
      manager_name: hotel.managerName || '',      // 驼峰转下划线
      manager_phone: hotel.managerPhone || '',
      imageUrl: hotel.image || '',                // image 字段是封面图
      longitude: hotel.longitude || '',
      latitude: hotel.latitude || ''
    }))

    // ✅ 修正：总数字段名
    pagination.itemCount = paginationInfo.totalItems || 0

    console.log('✅ 处理后列表:', hotelList.value)
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
    description: '',
    longitude: '',
    latitude: '',
    image: '',
    thumbImage: ''
  })
  coverFileList.value = []
  thumbFileList.value = []
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

    Object.assign(hotelForm, {
      id: hotel.id,
      name: hotel.name,
      address: hotel.address,
      manager_name: hotel.managerName || '',
      manager_phone: hotel.managerPhone || '',
      description: hotel.description,
      longitude: hotel.longitude,
      latitude: hotel.latitude,
      image: hotel.image || '',
      thumbImage: hotel.thumbImage || ''
    })

    // 设置封面图文件列表
    if (hotel.image) {
      coverFileList.value = [{
        id: 'cover',
        name: '封面图.jpg',
        status: 'finished',
        url: hotel.image
      }]
    } else {
      coverFileList.value = []
    }

    // 设置缩略图文件列表
    if (hotel.thumbImage) {
      thumbFileList.value = [{
        id: 'thumb',
        name: '缩略图.jpg',
        status: 'finished',
        url: hotel.thumbImage
      }]
    } else {
      thumbFileList.value = []
    }
  } catch (error) {
    console.error('获取酒店详情失败:', error)
    message.error('获取酒店详情失败')
  } finally {
    dialogLoading.value = false
  }
}

// 封面图上传相关
const handleCoverFileListChange = (files) => {
  coverFileList.value = files
  if (files.length > 0) {
    hotelForm.image = files[0].url || ''
  } else {
    hotelForm.image = ''
  }
}

const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl

      hotelForm.image = originUrl
      hotelForm.thumbImage = thumbUrl

      const updatedFile = {
        ...file,
        status: 'finished',
        url: originUrl
      }
      coverFileList.value = [updatedFile]

      onFinish()
      message.success('封面图上传成功')
    } else {
      onError()
      message.error('上传失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('上传失败:', error)
    onError()
    message.error('上传失败')
  }
}

// 缩略图上传相关
const handleThumbFileListChange = (files) => {
  thumbFileList.value = files
  if (files.length > 0) {
    hotelForm.thumbImage = files[0].url || ''
  } else {
    hotelForm.thumbImage = ''
  }
}

const handleThumbUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const thumbUrl = res.data.thumbUrl || res.data.originUrl || res.data.url

      hotelForm.thumbImage = thumbUrl

      const updatedFile = {
        ...file,
        status: 'finished',
        url: thumbUrl
      }
      thumbFileList.value = [updatedFile]

      onFinish()
      message.success('缩略图上传成功')
    } else {
      onError()
      message.error('上传失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('上传失败:', error)
    onError()
    message.error('上传失败')
  }
}

// 清除封面图
const clearCover = () => {
  hotelForm.image = ''
  coverFileList.value = []
}

// 清除缩略图
const clearThumbCover = () => {
  hotelForm.thumbImage = ''
  thumbFileList.value = []
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

    // 使用后端 DTO 字段名（驼峰）
    const data = {
      name: hotelForm.name,
      address: hotelForm.address,
      managerName: hotelForm.manager_name,
      managerPhone: hotelForm.manager_phone,
      description: hotelForm.description,
      image: hotelForm.image,
      thumbImage: hotelForm.thumbImage,
      longitude: hotelForm.longitude,
      latitude: hotelForm.latitude
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
    thumbImage: ''
  })
  coverFileList.value = []
  thumbFileList.value = []

  if (formRef.value) {
    formRef.value.restoreValidation()
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
        width: 80px;
        height: 60px;
        overflow: hidden;
        border-radius: 6px;

        .hotel-image-content {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .no-image {
          width: 100%;
          height: 100%;
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

// 表格中的图片样式
:deep(.n-data-table-td) .hotel-image-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

:deep(.n-data-table-td) .hotel-image-cell img {
  object-fit: cover;
  border-radius: 4px;
}

// 表单中的图片预览样式
.form-image-preview {
  object-fit: cover;
  border-radius: 6px;
  max-width: 100%;
  height: auto;
  max-height: 200px;
}

.image-preview {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
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

.info-item {
  display: flex;
  font-size: 13px;

  .label {
    color: #6b7280;
    min-width: 60px;
    flex-shrink: 0;
  }
}
</style>



