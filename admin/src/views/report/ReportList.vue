<template>
  <div class="report-management">
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
            新增报备
          </n-button>
        </div>
      </div>
      
      <n-data-table
        :columns="columns"
        :data="reportList"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>
    
    <n-modal v-model:show="dialogVisible" preset="dialog" :title="dialogTitle" style="width: 90%; max-width: 900px">
      <n-form ref="formRef" :model="reportForm" :rules="formRules" label-placement="left" label-width="100">
        <n-grid :cols="2" :x-gap="24" responsive="screen" :s-cols="1">
          <n-gi>
            <n-form-item label="剧名" path="name">
              <n-input v-model:value="reportForm.name" placeholder="请输入剧名" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="类型" path="type">
              <n-select v-model:value="reportForm.type" :options="typeOptions" placeholder="请选择类型" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="题材" path="genre">
              <n-input v-model:value="reportForm.genre" placeholder="请输入题材" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="集数" path="episodes">
              <n-input-number v-model:value="reportForm.episodes" placeholder="请输入集数" :min="1" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="投资金额" path="investAmount">
              <n-input-number v-model:value="reportForm.investAmount" placeholder="请输入投资金额" :min="0" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="主创人员" path="mainCreators">
              <n-input v-model:value="reportForm.mainCreators" placeholder="请输入主创人员" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="第一出品单位" path="leadProducer">
              <n-input v-model:value="reportForm.leadProducer" placeholder="请输入第一出品单位" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="制片单位" path="producerUnit">
              <n-input v-model:value="reportForm.producerUnit" placeholder="请输入制片单位" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="拍摄开始日期" path="startDate">
              <n-date-picker v-model:value="reportForm.startDate" type="date" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="拍摄结束日期" path="endDate">
              <n-date-picker v-model:value="reportForm.endDate" type="date" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="剧组规模" path="crewScale">
              <n-input v-model:value="reportForm.crewScale" placeholder="请输入剧组规模" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="联系人" path="contact">
              <n-input v-model:value="reportForm.contact" placeholder="请输入联系人" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="联系电话" path="phoneNumber">
              <n-input v-model:value="reportForm.phoneNumber" placeholder="请输入联系电话" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="剧组职务" path="crewPosition">
              <n-input v-model:value="reportForm.crewPosition" placeholder="请输入剧组职务" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="拍摄许可" path="shootPermit">
              <n-upload
                :custom-request="handleShootPermitUpload"
                :max="1"
                :file-list="shootPermitFileList"
                @update:file-list="(v) => shootPermitFileList = v"
                @before-upload="beforeUpload"
              >
                <n-button>上传文件</n-button>
              </n-upload>
              <div v-if="reportForm.thumbShootPermit" style="margin-top: 8px;">
                 <n-image :src="getImageUrl(reportForm.thumbShootPermit)" width="100" height="100" object-fit="cover" />
              </div>
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="立项审批" path="approvalFile">
              <n-upload
                :custom-request="handleApprovalFileUpload"
                :max="1"
                :file-list="approvalFileList"
                @update:file-list="(v) => approvalFileList = v"
                @before-upload="beforeUpload"
              >
                <n-button>上传文件</n-button>
              </n-upload>
              <div v-if="reportForm.thumbApprovalFile" style="margin-top: 8px;">
                 <n-image :src="getImageUrl(reportForm.thumbApprovalFile)" width="100" height="100" object-fit="cover" />
              </div>
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="协拍服务许可" path="shootApply">
              <n-upload
                :custom-request="handleShootApplyUpload"
                :max="1"
                :file-list="shootApplyFileList"
                @update:file-list="(v) => shootApplyFileList = v"
                @before-upload="beforeUpload"
              >
                <n-button>上传文件</n-button>
              </n-upload>
              <div v-if="reportForm.thumbShootApply" style="margin-top: 8px;">
                 <n-image :src="getImageUrl(reportForm.thumbShootApply)" width="100" height="100" object-fit="cover" />
              </div>
            </n-form-item>
          </n-gi>
        </n-grid>
      </n-form>
      <template #action>
        <n-button @click="dialogVisible = false">取消</n-button>
        <n-button type="primary" @click="handleDialogSave" :loading="dialogLoading">保存</n-button>
      </template>
    </n-modal>

    <!-- 审核弹窗 -->
    <n-modal v-model:show="auditDialogVisible" preset="dialog" title="审核报备" style="width: 400px">
      <n-form :model="auditForm" label-placement="left" label-width="80">
        <n-form-item label="当前状态">
          <n-tag :type="auditForm.currentStatus === 'APPROVED' ? 'success' : auditForm.currentStatus === 'REJECTED' ? 'error' : 'warning'">
            {{ statusOptions.find(o => o.value === auditForm.currentStatus)?.label || auditForm.currentStatus }}
          </n-tag>
        </n-form-item>
        <n-form-item label="审核状态">
          <n-select v-model:value="auditForm.status" :options="statusOptions" placeholder="请选择状态" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="auditDialogVisible = false">取消</n-button>
        <n-button type="primary" @click="handleAuditSave" :loading="auditLoading">确定</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, h, onMounted, watch } from 'vue'
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
  NTag,
  NPopconfirm,
  NModal,
  NGrid,
  NGi,
  NDatePicker,
  NUpload,
  NImage,
  useMessage,
  useDialog
} from 'naive-ui'
import { getReportPage, addReport, updateReport, deleteReport, getReportById, uploadFile, updateReportStatus } from '@/api'
import dayjs from 'dayjs'
import config from '@/config'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const reportList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增报备')
const formRef = ref(null)

const auditDialogVisible = ref(false)
const auditLoading = ref(false)
const auditForm = reactive({
  id: null,
  status: null,
  currentStatus: null
})

// 文件列表状态
const shootPermitFileList = ref([])
const approvalFileList = ref([])
const shootApplyFileList = ref([])

watch(shootPermitFileList, (newVal) => {
  if (newVal.length === 0) {
    reportForm.shootPermit = ''
    reportForm.thumbShootPermit = ''
  }
})

watch(approvalFileList, (newVal) => {
  if (newVal.length === 0) {
    reportForm.approvalFile = ''
    reportForm.thumbApprovalFile = ''
  }
})

watch(shootApplyFileList, (newVal) => {
  if (newVal.length === 0) {
    reportForm.shootApply = ''
    reportForm.thumbShootApply = ''
  }
})

const searchForm = reactive({
  keyword: ''
})

const reportForm = reactive({
  id: null,
  name: '',
  type: '',
  genre: '',
  episodes: 0,
  investAmount: 0,
  mainCreators: '',
  leadProducer: '',
  producerUnit: '',
  startDate: null,
  endDate: null,
  crewScale: '',
  contact: '',
  phoneNumber: '',
  crewPosition: '',
  shootPermit: '',
  thumbShootPermit: '',
  approvalFile: '',
  thumbApprovalFile: '',
  shootApply: '',
  thumbShootApply: '',
  status: 'PENDING'
})

const typeOptions = [
  { label: '电视剧', value: 'TV_SERIES' },
  { label: '电影', value: 'MOVIE' },
  { label: '网络剧', value: 'WEB_SERIES' },
  { label: '其他', value: 'OTHER' }
]

const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '处理中', value: 'PROCESSING' }
]

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
  name: [{ required: true, message: '请输入剧名', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  genre: [{ required: true, message: '请输入题材', trigger: 'blur' }],
  episodes: [{ required: true, type: 'number', message: '请输入集数', trigger: ['blur', 'change'] }],
  investAmount: [{ required: true, type: 'number', message: '请输入投资金额', trigger: ['blur', 'change'] }],
  mainCreators: [{ required: true, message: '请输入主创人员', trigger: 'blur' }],
  leadProducer: [{ required: true, message: '请输入第一出品单位', trigger: 'blur' }],
  producerUnit: [{ required: true, message: '请输入制片单位', trigger: 'blur' }],
  startDate: [{ required: true, type: 'number', message: '请选择拍摄开始日期', trigger: ['blur', 'change'] }],
  endDate: [{ required: true, type: 'number', message: '请选择拍摄结束日期', trigger: ['blur', 'change'] }],
  crewScale: [{ required: true, message: '请输入剧组规模', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phoneNumber: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  crewPosition: [{ required: true, message: '请输入剧组职务', trigger: 'blur' }]
}

const beforeUpload = (data) => {
  const isImage = data.file.file?.type.startsWith('image/')
  const limit = isImage ? 5 * 1024 * 1024 : 15 * 1024 * 1024
  
  if (data.file.file?.size > limit) {
    dialog.warning({
      title: '提示',
      content: isImage ? '图片过大，请重新上传' : '文件过大，请重新上传',
      positiveText: '确定'
    })
    return false
  }
  return true
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '剧名', key: 'name', ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'type',
    width: 120,
    render: (row) => {
      const typeMap = {
        'TV_SERIES': '电视剧',
        'MOVIE': '电影',
        'WEB_SERIES': '网络剧',
        'OTHER': '其他'
      }
      return typeMap[row.type] || row.type
    }
  },
  { title: '集数', key: 'episodes', width: 100 },
  { title: '投资金额', key: 'investAmount', width: 120, render: (row) => `¥${row.investAmount || 0}` },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已驳回',
        'PROCESSING': '处理中'
      }
      return h(
        NTag,
        {
          type: row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'error' : 'warning',
          bordered: false
        },
        { default: () => statusMap[row.status] || row.status }
      )
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
        h(NButton, { size: 'small', type: 'info', onClick: () => handleAudit(row) }, { default: () => '审核' }),
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
    const res = await getReportPage(pagination.page, pagination.pageSize, searchForm.keyword)
    console.log('分页响应数据:', res) // 添加调试日志
    if (res.code === 200) {
      reportList.value = res.data || []
      // 设置总数据量
      pagination.itemCount = res.pagination?.totalItems || 0
      // 自动计算总页数
      pagination.pageCount = Math.ceil(pagination.itemCount / pagination.pageSize) || 1
      console.log('设置总数据量:', pagination.itemCount) // 添加调试日志
      console.log('自动计算总页数:', pagination.pageCount) // 添加调试日志
    }
  } catch (error) {
    console.error('加载报备列表失败:', error)
    message.error('加载报备列表失败')
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

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return config.fileBaseURL + url
}

const handleUpload = async ({ file, onFinish, onError, field, thumbField, fileListRef }) => {
  try {
    const res = await uploadFile(file.file)
    if (res.code === 200 && res.data) {
      const originUrl = res.data.originUrl || res.data.url
      const thumbUrl = res.data.thumbUrl || '' // If doc, this might be empty

      reportForm[field] = originUrl
      reportForm[thumbField] = thumbUrl

      fileListRef.value = [{
        id: file.id,
        name: file.name,
        status: 'finished',
        url: getImageUrl(originUrl),
        originUrl: originUrl,
        thumbUrl: thumbUrl
      }]

      onFinish()
      message.success('上传成功')
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

const handleShootPermitUpload = (options) => handleUpload({ ...options, field: 'shootPermit', thumbField: 'thumbShootPermit', fileListRef: shootPermitFileList })
const handleApprovalFileUpload = (options) => handleUpload({ ...options, field: 'approvalFile', thumbField: 'thumbApprovalFile', fileListRef: approvalFileList })
const handleShootApplyUpload = (options) => handleUpload({ ...options, field: 'shootApply', thumbField: 'thumbShootApply', fileListRef: shootApplyFileList })

const handleAdd = () => {
  dialogTitle.value = '新增报备'
  Object.assign(reportForm, {
    id: null,
    name: '',
    type: '',
    genre: '',
    episodes: 0,
    investAmount: 0,
    mainCreators: '',
    leadProducer: '',
    producerUnit: '',
    startDate: null,
    endDate: null,
    crewScale: '',
    contact: '',
    phoneNumber: '',
    crewPosition: '',
    shootPermit: '',
    thumbShootPermit: '',
    approvalFile: '',
    thumbApprovalFile: '',
    shootApply: '',
    thumbShootApply: ''
  })
  shootPermitFileList.value = []
  approvalFileList.value = []
  shootApplyFileList.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getReportById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑报备'
      Object.assign(reportForm, {
        id: res.data.id,
        name: res.data.name,
        type: res.data.type,
        genre: res.data.genre,
        episodes: res.data.episodes,
        investAmount: res.data.investAmount,
        mainCreators: res.data.mainCreators,
        leadProducer: res.data.leadProducer,
        producerUnit: res.data.producerUnit,
        startDate: res.data.startDate ? dayjs(res.data.startDate).valueOf() : null,
        endDate: res.data.endDate ? dayjs(res.data.endDate).valueOf() : null,
        crewScale: res.data.crewScale,
        contact: res.data.contact,
        phoneNumber: res.data.phoneNumber,
        crewPosition: res.data.crewPosition,
        shootPermit: res.data.shootPermit,
        thumbShootPermit: res.data.thumbShootPermit,
        approvalFile: res.data.approvalFile,
        thumbApprovalFile: res.data.thumbApprovalFile,
        shootApply: res.data.shootApply,
        thumbShootApply: res.data.thumbShootApply
      })

      // Set file lists
      const setFileList = (url, thumbUrl, listRef) => {
        if (url) {
          listRef.value = [{
            id: 'existing',
            name: '已上传文件',
            status: 'finished',
            url: getImageUrl(url),
            originUrl: url,
            thumbUrl: thumbUrl
          }]
        } else {
          listRef.value = []
        }
      }

      setFileList(res.data.shootPermit, res.data.thumbShootPermit, shootPermitFileList)
      setFileList(res.data.approvalFile, res.data.thumbApprovalFile, approvalFileList)
      setFileList(res.data.shootApply, res.data.thumbShootApply, shootApplyFileList)

      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    message.error('获取详情失败')
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
      name: reportForm.name,
      type: reportForm.type,
      genre: reportForm.genre,
      episodes: reportForm.episodes,
      investAmount: reportForm.investAmount,
      mainCreators: reportForm.mainCreators,
      leadProducer: reportForm.leadProducer,
      producerUnit: reportForm.producerUnit,
      startDate: reportForm.startDate ? dayjs(reportForm.startDate).format('YYYY-MM-DD') : null,
      endDate: reportForm.endDate ? dayjs(reportForm.endDate).format('YYYY-MM-DD') : null,
      crewScale: reportForm.crewScale,
      contact: reportForm.contact,
      phoneNumber: reportForm.phoneNumber,
      crewPosition: reportForm.crewPosition,
      shootPermit: reportForm.shootPermit,
      thumbShootPermit: reportForm.thumbShootPermit,
      approvalFile: reportForm.approvalFile,
      thumbApprovalFile: reportForm.thumbApprovalFile,
      shootApply: reportForm.shootApply,
      thumbShootApply: reportForm.thumbShootApply,
      status: reportForm.id ? undefined : 'PENDING'
    }
    
    let res
    if (reportForm.id) {
      res = await updateReport({ ...data, id: reportForm.id })
    } else {
      res = await addReport(data)
    }
    
    if (res.code === 200) {
      message.success(reportForm.id ? '更新成功' : '创建成功')
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

const handleAudit = (row) => {
  auditForm.id = row.id
  auditForm.currentStatus = row.status
  auditForm.status = row.status
  auditDialogVisible.value = true
}

const handleAuditSave = async () => {
  if (!auditForm.id || !auditForm.status) {
    message.warning('请选择状态')
    return
  }

  try {
    auditLoading.value = true
    const res = await updateReportStatus({
      id: auditForm.id,
      status: auditForm.status
    })

    if (res.code === 200) {
      message.success('审核成功')
      auditDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('审核失败:', error)
    message.error('审核失败')
  } finally {
    auditLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    const res = await deleteReport(id)
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
.report-management {
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
