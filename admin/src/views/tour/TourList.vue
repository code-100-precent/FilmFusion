<template>
  <div class="tour-management">
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

      <!-- 桌面端表格 -->
      <template v-if="!isMobile">
        <n-data-table
            :columns="columns"
            :data="tourList"
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
          <div v-if="tourList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:routes" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
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
                  <h3 class="tour-title">{{ tour.name }}</h3>
                  <p class="tour-desc">{{ tour.description }}</p>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <span class="label">行程天数：</span>
                  <span>{{ tour.days?.length || 0 }} 天</span>
                </div>
                <div class="info-item">
                  <span class="label">景点数量：</span>
                  <span>{{ getTotalAttractions(tour) }} 个</span>
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
                  确定要删除这条线路吗？
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

    <!-- 编辑/新增对话框 -->
    <n-modal
        v-model:show="dialogVisible"
        preset="dialog"
        :title="dialogTitle"
        style="width: 95%; max-width: 1200px"
        :mask-closable="false"
    >
      <n-scrollbar style="max-height: 70vh">
        <n-form
            ref="formRef"
            :model="tourForm"
            :rules="formRules"
            :label-placement="isMobile ? 'top' : 'left'"
            :label-width="isMobile ? 'auto' : '100'"
        >
          <!-- 基本信息 -->
          <n-divider title-placement="left">基本信息</n-divider>
          <n-form-item label="线路名称" path="name">
            <n-input v-model:value="tourForm.name" placeholder="请输入线路名称" />
          </n-form-item>
          <n-form-item label="线路介绍" path="description">
            <n-input v-model:value="tourForm.description" type="textarea" :rows="4" placeholder="请输入线路介绍" />
          </n-form-item>

          <!-- 行程列表 -->
          <n-divider title-placement="left">
            <span>行程安排</span>
            <n-button size="small" type="primary" @click="addDay" style="margin-left: 12px">
              <template #icon>
                <Icon icon="mdi:plus" />
              </template>
              添加行程
            </n-button>
          </n-divider>

          <div v-if="tourForm.days.length === 0" style="text-align: center; padding: 24px; color: #999;">
            暂无行程，点击上方"添加行程"按钮添加
          </div>

          <n-collapse v-else accordion>
            <n-collapse-item v-for="(day, dayIndex) in tourForm.days" :key="dayIndex" :title="`${day.day} - ${day.name || '未命名'}`">
              <template #header-extra>
                <n-button size="small" type="error" quaternary @click.stop="removeDay(dayIndex)">
                  <template #icon>
                    <Icon icon="mdi:delete" />
                  </template>
                  删除
                </n-button>
              </template>

              <n-form-item label="天数标识" :path="`days[${dayIndex}].day`">
                <n-input v-model:value="day.day" placeholder="例如：Day1" />
              </n-form-item>
              <n-form-item label="专题名称" :path="`days[${dayIndex}].name`">
                <n-input v-model:value="day.name" placeholder="请输入当天专题名称" />
              </n-form-item>

              <!-- 景点列表 -->
              <n-divider title-placement="left">
                <span>景点列表</span>
                <n-button size="small" @click="addAttraction(dayIndex)" style="margin-left: 8px">
                  <template #icon>
                    <Icon icon="mdi:plus" />
                  </template>
                  添加景点
                </n-button>
              </n-divider>

              <div v-if="day.attractions.length === 0" style="text-align: center; padding: 16px; color: #999; background: #fafafa; border-radius: 4px;">
                暂无景点，点击"添加景点"按钮添加
              </div>

              <n-card v-for="(attr, attrIndex) in day.attractions" :key="attrIndex" style="margin-bottom: 12px" size="small">
                <template #header>
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>景点 {{ attrIndex + 1 }}</span>
                    <n-button size="small" type="error" quaternary @click="removeAttraction(dayIndex, attrIndex)">
                      <template #icon>
                        <Icon icon="mdi:delete" />
                      </template>
                      删除
                    </n-button>
                  </div>
                </template>

                <n-form-item label="景点名称" :path="`days[${dayIndex}].attractions[${attrIndex}].name`">
                  <n-input v-model:value="attr.name" placeholder="请输入景点名称" />
                </n-form-item>
                <n-form-item label="景点亮点" :path="`days[${dayIndex}].attractions[${attrIndex}].highlights`">
                  <n-input v-model:value="attr.highlights" type="textarea" :rows="3" placeholder="请输入景点亮点" />
                </n-form-item>

                <div class="form-row">
                  <n-form-item label="关联景点" :path="`days[${dayIndex}].attractions[${attrIndex}].locationId`">
                    <n-select
                        v-model:value="attr.locationIds"
                        :options="locationOptions"
                        placeholder="请选择关联景点"
                        filterable
                        multiple
                        clearable
                    />
                  </n-form-item>
                  <n-form-item label="关联影视" :path="`days[${dayIndex}].attractions[${attrIndex}].dramaId`">
                    <n-select
                        v-model:value="attr.dramaIds"
                        :options="dramaOptions"
                        placeholder="请选择关联影视"
                        filterable
                        multiple
                        clearable
                    />
                  </n-form-item>
                </div>

                <n-form-item label="关联酒店" :path="`days[${dayIndex}].attractions[${attrIndex}].hotelId`">
                  <n-select
                      v-model:value="attr.hotelIds"
                      :options="hotelOptions"
                      placeholder="请选择关联酒店"
                      filterable
                      multiple
                      clearable
                  />
                </n-form-item>

                <!-- 景点图片上传 -->
                <n-form-item label="景点图片">
                  <n-upload
                      v-model:file-list="attr.fileList"
                      @update:file-list="(files) => handleAttractionFileListChange(dayIndex, attrIndex, files)"
                      :custom-request="(options) => handleAttractionUpload(dayIndex, attrIndex, options)"
                      @before-upload="beforeUpload"
                      accept="image/*"
                      list-type="image-card"
                      multiple
                      :max="5"
                  >
                    点击上传图片
                  </n-upload>
                </n-form-item>
              </n-card>
            </n-collapse-item>
          </n-collapse>
        </n-form>
      </n-scrollbar>
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
  useMessage,
  useDialog,
  NUpload,
  NPagination,
  NSpin,
  NDivider,
  NCollapse,
  NCollapseItem,
  NScrollbar,
  NSelect
} from 'naive-ui'
import { getTourPage, createTour, updateTour, deleteTour, getTourById, uploadFile, getLocationList, getDramaList, getHotelPage } from '@/api'
import { getImageUrl } from '@/utils/image'
import config from '@/config'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()

const isMobile = ref(false)
const loading = ref(false)
const tourList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增线路')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const tourForm = reactive({
  id: null,
  name: '',
  description: '',
  days: []
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  name: [{ required: true, message: '请输入线路名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入线路介绍', trigger: 'blur' }]
}

// 下拉选项数据
const locationOptions = ref([])
const dramaOptions = ref([])
const hotelOptions = ref([])

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const formatDate = (date) => {
  if (!date) return '-'
  if (Array.isArray(date)) {
    return dayjs(date[0] + '-' + String(date[1]).padStart(2, '0') + '-' + String(date[2]).padStart(2, '0')).format('YYYY-MM-DD HH:mm')
  }
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getTotalAttractions = (tour) => {
  if (!tour.days || !Array.isArray(tour.days)) return 0
  return tour.days.reduce((total, day) => {
    return total + (day.attractions?.length || 0)
  }, 0)
}

const columns = [
  { title: 'ID', key: 'id', width: 80, fixed: 'left' },
  { title: '线路名称', key: 'name', width: 200, ellipsis: { tooltip: true }, fixed: 'left' },
  { title: '线路介绍', key: 'description', width: 300, ellipsis: { tooltip: true } },
  {
    title: '行程天数',
    key: 'days',
    width: 100,
    render: (row) => (row.days?.length || 0) + ' 天'
  },
  {
    title: '景点数量',
    key: 'attractions',
    width: 100,
    render: (row) => getTotalAttractions(row) + ' 个'
  },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: (row) => formatDate(row.createdAt || row.created_at)
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
              trigger: () => h(NButton, { size: 'small', type: 'error', quaternary: true }, { default: () => '删除' }),
              default: () => '确定要删除这条线路吗？'
            }
        )
      ])
    }
  }
]

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadOptions()
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// 加载下拉选项数据
const loadOptions = async () => {
  try {
    // 加载景点列表
    const locRes = await getLocationList({ current: 1, size: 1000 })
    if (locRes.code === 200 && locRes.data) {
      const records = Array.isArray(locRes.data) ? locRes.data : (locRes.data.records || [])
      locationOptions.value = records.map(item => ({
        label: item.name,
        value: item.id
      }))
    }

    // 加载影视列表
    const dramaRes = await getDramaList({ current: 1, size: 1000 })
    if (dramaRes.code === 200 && dramaRes.data) {
      const records = Array.isArray(dramaRes.data) ? dramaRes.data : (dramaRes.data.records || [])
      dramaOptions.value = records.map(item => ({
        label: item.name,
        value: item.id
      }))
    }

    // 加载酒店列表
    const hotelRes = await getHotelPage(1, 1000)
    if (hotelRes.code === 200 && hotelRes.data) {
      const records = Array.isArray(hotelRes.data) ? hotelRes.data : (hotelRes.data.records || [])
      hotelOptions.value = records.map(item => ({
        label: item.name,
        value: item.id
      }))
    }
  } catch (error) {
    console.error('加载下拉选项失败:', error)
  }
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getTourPage(pagination.page, pagination.pageSize, searchForm.keyword)
    if (res.code === 200) {
      tourList.value = res.data?.records || res.data || []
      pagination.itemCount = res.data?.total || res.total || res.pagination?.totalItems || 0
    } else {
      message.error(res.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载线路列表失败:', error)
    message.error('加载数据失败')
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
  Object.assign(tourForm, {
    id: null,
    name: '',
    description: '',
    days: []
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    dialogLoading.value = true
    const res = await getTourById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑线路'
      const data = res.data

      Object.assign(tourForm, {
        id: data.id,
        name: data.name,
        description: data.description,
        days: (data.days || []).map(day => ({
          id: day.id,
          name: day.name,
          day: day.day,
          attractions: (day.attractions || []).map(attr => {
            // 解析图片
            const images = (attr.image || '').split(',').filter(url => url.trim())
            const thumbImages = (attr.thumbImage || attr.thumb_image || '').split(',').filter(url => url.trim())

            const fileList = images.map((url, index) => ({
              id: `attr-${attr.id || Date.now()}-${index}`,
              name: `image-${index}.jpg`,
              status: 'finished',
              url: getImageUrl(thumbImages[index] || url),
              originUrl: url,
              thumbUrl: thumbImages[index] || url
            }))

            // 将逗号分隔的 ID 字符串转换为数组
            const parseIds = (idStr) => {
              if (!idStr) return []
              return idStr.split(',').map(id => {
                const parsed = parseInt(id.trim())
                return isNaN(parsed) ? null : parsed
              }).filter(id => id !== null)
            }

            return {
              id: attr.id,
              name: attr.name,
              highlights: attr.highlights,
              locationId: attr.locationId || attr.location_id || '',
              dramaId: attr.dramaId || attr.drama_id || '',
              hotelId: attr.hotelId || attr.hotel_id || '',
              locationIds: parseIds(attr.locationId || attr.location_id),
              dramaIds: parseIds(attr.dramaId || attr.drama_id),
              hotelIds: parseIds(attr.hotelId || attr.hotel_id),
              image: attr.image,
              thumbImage: attr.thumbImage || attr.thumb_image,
              fileList: fileList
            }
          })
        }))
      })

      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取线路详情失败:', error)
    message.error('获取线路详情失败')
  } finally {
    dialogLoading.value = false
  }
}

const beforeUpload = (data) => {
  const isImage = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'].includes(data.file.file?.type)
  if (!isImage) {
    message.error('只能上传 PNG/JPG/GIF/WEBP 格式的图片文件，请重新上传')
    return false
  }
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

const handleAttractionUpload = async (dayIndex, attrIndex, {file, onFinish, onError}) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || originUrl

      file.url = getImageUrl(thumbUrl)
      file.originUrl = originUrl
      file.thumbUrl = thumbUrl
      file.status = 'finished'

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

const handleAttractionFileListChange = (dayIndex, attrIndex, files) => {
  tourForm.days[dayIndex].attractions[attrIndex].fileList = files
}

const addDay = () => {
  const dayCount = tourForm.days.length + 1
  tourForm.days.push({
    name: '',
    day: `Day${dayCount}`,
    attractions: []
  })
}

const removeDay = (index) => {
  tourForm.days.splice(index, 1)
}

const addAttraction = (dayIndex) => {
  tourForm.days[dayIndex].attractions.push({
    name: '',
    highlights: '',
    locationId: '',
    dramaId: '',
    hotelId: '',
    locationIds: [],
    dramaIds: [],
    hotelIds: [],
    image: '',
    thumbImage: '',
    fileList: []
  })
}

const removeAttraction = (dayIndex, attrIndex) => {
  tourForm.days[dayIndex].attractions.splice(attrIndex, 1)
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

    // 构建提交数据
    const data = {
      name: tourForm.name,
      description: tourForm.description,
      days: tourForm.days.map(day => ({
        name: day.name,
        day: day.day,
        attractions: day.attractions.map(attr => {
          // 提取图片URL
          const images = (attr.fileList || [])
              .filter(f => f.status === 'finished')
              .map(f => {
                let url = f.originUrl || f.url
                if (url && url.startsWith('http')) {
                  url = url.replace(config.fileBaseURL, '')
                }
                // 强制转换为原图路径
                if (url && url.includes('/files/thumb/')) {
                  url = url.replace('/files/thumb/', '/files/origin/')
                }
                return url
              })

          const thumbImages = (attr.fileList || [])
              .filter(f => f.status === 'finished')
              .map(f => {
                let url = f.thumbUrl || f.url
                if (url && url.startsWith('http')) {
                  url = url.replace(config.fileBaseURL, '')
                }
                return url
              })

          // 将 ID 数组转换为逗号分隔的字符串
          const locationIdStr = (attr.locationIds && attr.locationIds.length > 0)
              ? attr.locationIds.join(',')
              : ''
          const dramaIdStr = (attr.dramaIds && attr.dramaIds.length > 0)
              ? attr.dramaIds.join(',')
              : ''
          const hotelIdStr = (attr.hotelIds && attr.hotelIds.length > 0)
              ? attr.hotelIds.join(',')
              : ''

          return {
            name: attr.name,
            highlights: attr.highlights,
            locationId: locationIdStr,
            dramaId: dramaIdStr,
            hotelId: hotelIdStr,
            image: images.join(','),
            thumbImage: thumbImages.join(',')
          }
        })
      }))
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
    const res = await deleteTour(id)
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

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.form-row {
  display: flex;
  gap: 16px;

  .n-form-item {
    flex: 1;
  }
}

// 移动端适配
.mobile-list {
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 48px 0;
  }

  .card-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .mobile-card {
    :deep(.n-card__content) {
      padding: 12px;
    }
  }

  .card-header {
    margin-bottom: 12px;

    .tour-info {
      .tour-title {
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 4px 0;
        line-height: 1.4;
      }

      .tour-desc {
        font-size: 13px;
        color: #6b7280;
        margin: 0;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }
  }

  .card-content {
    margin-bottom: 12px;
    font-size: 13px;
    color: #4b5563;

    .info-item {
      display: flex;
      margin-bottom: 4px;

      .label {
        color: #9ca3af;
        min-width: 80px;
      }
    }
  }

  .mobile-pagination {
    margin-top: 16px;
    display: flex;
    justify-content: center;
  }
}

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

  .form-row {
    flex-direction: column;
    gap: 0;
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
