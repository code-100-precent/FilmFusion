<template>
  <div class="user-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="关键词">
            <n-input 
              v-model:value="searchForm.keyword" 
              placeholder="请输入用户名或手机号" 
              clearable 
              @keyup.enter="handleSearch" 
            />
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
            新增用户
          </n-button>
        </div>
      </div>
      
      <!-- 桌面端表格 -->
      <n-data-table
        v-if="!isMobile"
        :columns="columns"
        :data="userList"
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
          <div v-if="userList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:account-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
              v-for="user in userList"
              :key="user.id"
              class="mobile-card"
              hoverable
            >
              <div class="card-header">
                <div class="user-avatar-name">
                  <n-avatar :src="getAvatarUrl(user.avatar)" size="medium" round>
                    <Icon icon="mdi:account" :width="24" />
                  </n-avatar>
                  <div class="user-info">
                    <div class="user-name">{{ user.username }}</div>
                    <div class="user-phone">{{ user.phoneNumber }}</div>
                  </div>
                </div>
                <div class="user-tags">
                  <n-tag :type="user.role === 'ADMIN' ? 'success' : 'default'" size="small">
                    {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
                  </n-tag>
                  <n-tag :type="user.enabled ? 'success' : 'error'" size="small">
                    {{ user.enabled ? '启用' : '禁用' }}
                  </n-tag>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <Icon icon="mdi:identifier" :width="16" />
                  <span>ID: {{ user.id }}</span>
                </div>
                <div class="info-item">
                  <Icon icon="mdi:calendar-clock" :width="16" />
                  <span>{{ formatDate(user.createdAt) }}</span>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(user)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(user.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个用户吗？
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
      style="width: 90%; max-width: 600px"
      :mask-closable="false"
    >
      <n-form 
        ref="formRef" 
        :model="userForm" 
        :rules="formRules" 
        :label-placement="isMobile ? 'top' : 'left'"
        :label-width="isMobile ? 'auto' : '100'"
      >
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="userForm.username" placeholder="请输入用户名" />
        </n-form-item>
        <n-form-item label="手机号" path="phoneNumber">
          <n-input v-model:value="userForm.phoneNumber" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item v-if="!userForm.id" label="密码" path="password">
          <n-input 
            v-model:value="userForm.password" 
            type="password" 
            placeholder="请输入密码（6-20个字符）"
            show-password-on="click"
          />
        </n-form-item>
        <n-form-item label="角色" path="role">
          <n-select v-model:value="userForm.role" :options="roleOptions" placeholder="请选择角色" />
        </n-form-item>
        <n-form-item label="状态" path="enabled">
          <n-switch v-model:value="userForm.enabled">
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
import { ref, reactive, h, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import {
  NCard,
  NButton,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NSwitch,
  NDataTable,
  NPopconfirm,
  NModal,
  NTag,
  NAvatar,
  NSpin,
  NPagination,
  useMessage
} from 'naive-ui'
import { getUserPage, createUser, updateUser, deleteUser, getUserById } from '@/api'
import dayjs from 'dayjs'

const message = useMessage()

const isMobile = ref(false)
const loading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  if (Array.isArray(date)) {
    return dayjs(date[0] + '-' + String(date[1]).padStart(2, '0') + '-' + String(date[2]).padStart(2, '0')).format('YYYY-MM-DD HH:mm:ss')
  }
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}
const userList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const userForm = reactive({
  id: null,
  username: '',
  phoneNumber: '',
  password: '',
  role: 'USER',
  enabled: true
})

const roleOptions = [
  { label: '普通用户', value: 'USER' },
  { label: '管理员', value: 'ADMIN' }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 32, message: '用户名长度在 2 到 32 个字符', trigger: 'blur' }
  ],
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 处理头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  // 如果是相对路径，拼接baseURL
  if (avatar.startsWith('/')) {
    return 'http://localhost:8080' + avatar
  }
  return avatar
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  {
    title: '头像',
    key: 'avatar',
    width: 80,
    render: (row) => {
      return h(NAvatar, {
        size: 'small',
        src: getAvatarUrl(row.avatar),
        round: true
      }, {
        default: () => h(Icon, { icon: 'mdi:account', width: 20 })
      })
    }
  },
  { title: '用户名', key: 'username', width: 150, ellipsis: { tooltip: true } },
  { title: '手机号', key: 'phoneNumber', width: 130 },
  {
    title: '角色',
    key: 'role',
    width: 120,
    render: (row) => {
      const roleMap = {
        'ADMIN': { label: '管理员', type: 'success' },
        'USER': { label: '普通用户', type: 'default' }
      }
      const roleInfo = roleMap[row.role] || { label: row.role, type: 'default' }
      return h(NTag, { type: roleInfo.type, size: 'small' }, { default: () => roleInfo.label })
    }
  },
  {
    title: '状态',
    key: 'enabled',
    width: 100,
    render: (row) => {
      return h(NTag, { 
        type: row.enabled ? 'success' : 'error', 
        size: 'small' 
      }, { 
        default: () => row.enabled ? '启用' : '禁用' 
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
    const res = await getUserPage(pagination.page, pagination.pageSize, searchForm.keyword)
    if (res.code === 200) {
      userList.value = res.data || []
      pagination.itemCount = res.pagination?.totalItems || 0
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    message.error('加载用户列表失败')
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
  dialogTitle.value = '新增用户'
  Object.assign(userForm, {
    id: null,
    username: '',
    phoneNumber: '',
    password: '',
    role: 'USER',
    enabled: true
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getUserById(row.id)
    if (res.code === 200 && res.data) {
      dialogTitle.value = '编辑用户'
      Object.assign(userForm, {
        id: res.data.id,
        username: res.data.username,
        phoneNumber: res.data.phoneNumber,
        password: '',
        role: res.data.role,
        enabled: res.data.enabled !== false
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    message.error('获取用户详情失败')
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
      username: userForm.username,
      phoneNumber: userForm.phoneNumber,
      role: userForm.role,
      enabled: userForm.enabled
    }
    
    // 如果是新增，需要密码；如果是编辑，密码可选
    if (!userForm.id) {
      data.password = userForm.password
    } else if (userForm.password) {
      data.password = userForm.password
    }
    
    let res
    if (userForm.id) {
      res = await updateUser(userForm.id, data)
    } else {
      res = await createUser(data)
    }
    
    if (res.code === 200) {
      message.success(userForm.id ? '更新成功' : '创建成功')
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
    const res = await deleteUser(id)
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
.user-management {
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
      
      .user-avatar-name {
        display: flex;
        align-items: center;
        gap: 12px;
        flex: 1;
        
        .user-info {
          flex: 1;
          
          .user-name {
            font-size: 16px;
            font-weight: 600;
            color: #1f2937;
            margin-bottom: 4px;
          }
          
          .user-phone {
            font-size: 14px;
            color: #6b7280;
          }
        }
      }
      
      .user-tags {
        display: flex;
        flex-direction: column;
        gap: 6px;
        align-items: flex-end;
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
        align-items: center;
        gap: 8px;
        font-size: 13px;
        color: #6b7280;
        
        span {
          color: #374151;
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

