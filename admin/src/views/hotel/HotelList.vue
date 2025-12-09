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
              :custom-request="handleCoverUpload"
              accept="image/*"
          >
            <div v-if="!hotelForm.image" class="upload-trigger">
              <Icon icon="mdi:upload" />
              <span>点击上传封面图片</span>
            </div>
            <div v-if="hotelForm.image">
              <n-image
                  :src="getImageUrl(hotelForm.thumbImage)"
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

        <!-- ✅ 移除缩略图上传部分，因为会自动生成 -->
        <!-- ✅ 添加缩略图预览显示 -->
        <div v-if="hotelForm.thumbImage" style="margin-top: 12px;">
          <span style="font-size: 14px; color: #6b7280; display: block; margin-bottom: 8px;">缩略图预览：</span>
          <n-image
              :src="getImageUrl(hotelForm.thumbImage)"
              width="200"
              height="120"
              object-fit="cover"
              class="form-image-preview"
              style="max-width: 200px; max-height: 120px;"
          />
        </div>

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
  useMessage
} from 'naive-ui'
import request from '@/utils/request'
import { getImageUrl } from '@/utils/image'

// 💡 假设您的所有 API 函数 (getHotelPage, createHotel, uploadFile, etc.)
// 都是从外部文件导入或在文件顶部定义。为了让代码在单个文件中运行，
// 且便于理解，这里重新定义 uploadFile，并假设其他 API 已经在组件外部被导出。

// 请确保您的实际项目中，以下 API 函数已从 '@/utils/request' 等文件中导入
// 否则，请将它们完整地粘贴到 setup 外部（如果它们确实在同一个文件）。

// --- API 模拟/重定义 (请根据您的项目实际情况导入) ---
const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

const getHotelPage = (currentPage = 1, pageSize = 10, keyword = '') => {
  return request({
    url: '/hotel/admin/page',
    method: 'get',
    params: {
      current: currentPage,
      size: pageSize,
      keyword
    }
  })
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
// --- API 模拟/重定义 结束 ---

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
  manager_name: '',    // 对应 manager_name
  manager_phone: '',   // 对应 manager_phone
  description: '',
  longitude: '',
  latitude: '',
  image: '',           // 对应 image (封面图)
  thumbImage: ''       // 对应 thumb_image (缩略图)
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
// ✅ 删除缩略图文件列表
// const thumbFileList = ref([])

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
    // 增加 flexGrow 以确保地址列能自动换行
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
      const displayUrl = row.thumbImage || row.imageUrl;
      const previewUrl = row.imageUrl; // 预览用原图

      if (displayUrl) {
        return h(NImage, {
          src: getImageUrl(displayUrl),
          previewSrc: getImageUrl(previewUrl),
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
    fixed: 'right', // 锁定操作列
    render(row) {
      return [
        h(NButton, {
          size: 'small',
          type: 'primary',
          style: { marginRight: '8px' },
          onClick: () => handleEdit(row)
        }, { default: () => '编辑' }),
        // ✅ 修正：使用 NPopconfirm 包裹删除按钮
        h(NPopconfirm, {
          onPositiveClick: () => handleDelete(row.id),
          placement: 'left'
        }, {
          trigger: () => h(NButton, {
            size: 'small',
            type: 'error',
            tertiary: true // 使用三级按钮更柔和
          }, { default: () => '删除' }),
          default: () => '确定要删除这个酒店吗？'
        })
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

// ✅ loadData 方法 - 确保参数和映射正确
const loadData = async () => {
  try {
    loading.value = true
    // 确保传递给 API 的参数名与接口定义一致 (current, size)
    const res = await getHotelPage(pagination.page, pagination.pageSize, searchForm.keyword)

    if (res.code !== 200) {
      message.error(res.message || '加载失败')
      return
    }

    // 假设后端返回的数据结构为 { data: [/* list */], totalItems: 100 } 或 { data: { list: [], total: 100 } }
    // 您的 loadData 逻辑中已经有 item-count 字段，这里假设后端返回的分页信息包含 totalItems
    const listData = res.data.list || res.data || []
    const totalItems = res.data.total || res.totalItems || 0

    // ✅ 修正：将 API 返回的驼峰字段 (managerName) 映射到前端使用的下划线字段 (manager_name)
    hotelList.value = listData.map(hotel => ({
      ...hotel,
      manager_name: hotel.managerName || '',
      manager_phone: hotel.managerPhone || '',
      imageUrl: hotel.image || '',                // image 字段是封面图
      thumbImage: hotel.thumbImage || '',         // thumbImage 字段是缩略图
    }))

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
  // 重置表单逻辑
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
  // ✅ 删除这行：thumbFileList.value = []

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

    // ✅ 修正：将 API 返回的驼峰字段映射到表单的下划线字段
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
        url: getImageUrl(hotel.thumbImage || hotel.image)  // 显示缩略图
      }]
    } else {
      coverFileList.value = []
    }

    // ✅ 不再设置缩略图文件列表，因为不再使用
  } catch (error) {
    console.error('获取酒店详情失败:', error)
    message.error('获取酒店详情失败')
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

// --- 图片上传逻辑 ---

// ✅ 封面图上传（新增/第一次上传）
const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file);

    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl;
      const thumbUrl = res.data.thumbUrl;

      hotelForm.image = originUrl;
      hotelForm.thumbImage = thumbUrl;

      const newFile = {
        id: file.id,
        name: file.name,
        status: 'finished',
        url: getImageUrl(thumbUrl)  // 显示缩略图
      };

      coverFileList.value = [newFile];

      await nextTick();
      onFinish();
      message.success('封面图上传成功');
    } else {
      const errorFile = {
        ...file,
        status: 'error',
        error: res.message || '上传失败'
      };
      coverFileList.value = [errorFile];
      await nextTick();
      onError();
      message.error('上传失败：' + (res.message || '未知错误'));
    }
  } catch (error) {
    console.error('上传失败:', error);
    const errorFile = {
      ...file,
      status: 'error',
      error: error.message || '网络错误'
    };
    coverFileList.value = [errorFile];
    await nextTick();
    onError();
    message.error('上传失败');
  }
};

// ✅ 替换封面图（点击"修改封面"触发）
const handleReplaceCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadFile(file.file);

    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl;
      const thumbUrl = res.data.thumbUrl;

      hotelForm.image = originUrl;
      hotelForm.thumbImage = thumbUrl;

      const newFile = {
        id: file.id,
        name: file.name,
        status: 'finished',
        url: getImageUrl(thumbUrl)  // 显示缩略图
      };

      coverFileList.value = [newFile];

      await nextTick();
      onFinish();
      message.success('封面已更新');
    } else {
      const errorFile = {
        ...file,
        status: 'error',
        error: res.message || '上传失败'
      };
      coverFileList.value = [errorFile];
      await nextTick();
      onError();
      message.error('上传失败：' + (res.message || '未知错误'));
    }
  } catch (error) {
    console.error('封面上传失败:', error);
    const errorFile = {
      ...file,
      status: 'error',
      error: error.message || '网络错误'
    };
    coverFileList.value = [errorFile];
    await nextTick();
    onError();
    message.error('上传失败，请重试');
  }
};

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

    // ✅ 修正：将表单的下划线字段映射到 API 期望的驼峰字段
    const data = {
      name: hotelForm.name,
      address: hotelForm.address,
      managerName: hotelForm.manager_name,    // 驼峰
      managerPhone: hotelForm.manager_phone,  // 驼峰
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
      // 删除后回到第一页或重新加载当前页
      if (hotelList.value.length === 1 && pagination.page > 1) {
        pagination.page--
      }
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



