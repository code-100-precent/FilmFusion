<template>
  <div class="drama-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="电视剧名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入电视剧名称" clearable @keyup.enter="handleSearch" />
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
      
      <!-- 桌面端表格 -->
      <n-data-table
        v-if="!isMobile"
        :columns="columns"
        :data="dramaList"
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
          <div v-if="dramaList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:film-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
              v-for="drama in dramaList"
              :key="drama.id"
              class="mobile-card"
              hoverable
            >
              <div class="card-header">
                <div class="drama-info">
                  <h3 class="drama-name">{{ drama.name }}</h3>
                  <p class="drama-company">{{ drama.prodCompany }}</p>
                </div>
                <div class="drama-cover">
                  <n-image
                    v-if="drama.cover"
                    :src="drama.cover"
                    width="80"
                    height="60"
                    object-fit="cover"
                    preview-disabled
                  />
                  <div v-else class="no-cover">
                    <Icon icon="mdi:film" :width="32" />
                  </div>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <span class="label">备案号：</span>
                  <span>{{ drama.filingNum || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">拍摄地：</span>
                  <span>{{ drama.shootLocation || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">演员：</span>
                  <span class="cast-info">{{ drama.cast || '-' }}</span>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(drama)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(drama.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个电视剧吗？
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
        :model="dramaForm" 
        :rules="formRules" 
        :label-placement="isMobile ? 'top' : 'left'"
        :label-width="isMobile ? 'auto' : '120'"
      >
        <n-form-item label="电视剧名称" path="name">
          <n-input v-model:value="dramaForm.name" placeholder="请输入电视剧名称" />
        </n-form-item>
        <n-form-item label="备案号" path="filingNum">
          <n-input v-model:value="dramaForm.filingNum" placeholder="请输入备案号" />
        </n-form-item>
        <n-form-item label="出品公司" path="prodCompany">
          <n-input v-model:value="dramaForm.prodCompany" placeholder="请输入出品公司" />
        </n-form-item>
        <n-form-item label="公司简介" path="crewDescription">
          <n-input v-model:value="dramaForm.crewDescription" type="textarea" :rows="3" placeholder="请输入公司简介" />
        </n-form-item>
        <n-form-item label="电视剧简介" path="dramaDescription">
          <n-input v-model:value="dramaForm.dramaDescription" type="textarea" :rows="4" placeholder="请输入电视剧简介" />
        </n-form-item>
        <!-- 这里应该添加更多表单字段 -->
      </n-form>
      <template #footer>
        <n-space>
          <n-button @click="dialogVisible = false">取消</n-button>
          <n-button type="primary" @click="handleDialogSave">保存</n-button>
        </n-space>
      </template>
    </n-modal>

<template>
  <div class="drama-management">
    <n-card class="management-card">
      <div class="search-header">
        <n-form :model="searchForm" inline class="search-form">
          <n-form-item label="电视剧名称">
            <n-input v-model:value="searchForm.keyword" placeholder="请输入电视剧名称" clearable @keyup.enter="handleSearch" />
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
      
      <!-- 桌面端表格 -->
      <n-data-table
        v-if="!isMobile"
        :columns="columns"
        :data="dramaList"
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
          <div v-if="dramaList.length === 0 && !loading" class="empty-state">
            <Icon icon="mdi:film-off" :width="48" style="color: #d1d5db; margin-bottom: 16px;" />
            <p style="color: #9ca3af;">暂无数据</p>
          </div>
          <div v-else class="card-list">
            <n-card
              v-for="drama in dramaList"
              :key="drama.id"
              class="mobile-card"
              hoverable
            >
              <div class="card-header">
                <div class="drama-info">
                  <h3 class="drama-name">{{ drama.name }}</h3>
                  <p class="drama-company">{{ drama.prodCompany }}</p>
                </div>
                <div class="drama-cover">
                  <n-image
                    v-if="drama.cover"
                    :src="drama.cover"
                    width="80"
                    height="60"
                    object-fit="cover"
                    preview-disabled
                  />
                  <div v-else class="no-cover">
                    <Icon icon="mdi:film" :width="32" />
                  </div>
                </div>
              </div>
              <div class="card-content">
                <div class="info-item">
                  <span class="label">备案号：</span>
                  <span>{{ drama.filingNum || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">拍摄地：</span>
                  <span>{{ drama.shootLocation || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">演员：</span>
                  <span class="cast-info">{{ drama.cast || '-' }}</span>
                </div>
              </div>
              <div class="card-actions">
                <n-button size="small" @click="handleEdit(drama)" block style="margin-bottom: 8px">
                  编辑
                </n-button>
                <n-popconfirm @positive-click="handleDelete(drama.id)">
                  <template #trigger>
                    <n-button size="small" type="error" quaternary block>
                      删除
                    </n-button>
                  </template>
                  确定要删除这个电视剧吗？
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
        :model="dramaForm" 
        :rules="formRules" 
        :label-placement="isMobile ? 'top' : 'left'"
        :label-width="isMobile ? 'auto' : '120'"
      >
        <n-form-item label="电视剧名称" path="name">
          <n-input v-model:value="dramaForm.name" placeholder="请输入电视剧名称" />
        </n-form-item>
        <n-form-item label="备案号" path="filingNum">
          <n-input v-model:value="dramaForm.filingNum" placeholder="请输入备案号" />
        </n-form-item>
        <n-form-item label="出品公司" path="prodCompany">
          <n-input v-model:value="dramaForm.prodCompany" placeholder="请输入出品公司" />
        </n-form-item>
        <n-form-item label="公司简介" path="crewDescription">
          <n-input v-model:value="dramaForm.crewDescription" type="textarea" :rows="3" placeholder="请输入公司简介" />
        </n-form-item>
        <n-form-item label="电视剧简介" path="dramaDescription">
          <n-input v-model:value="dramaForm.dramaDescription" type="textarea" :rows="4" placeholder="请输入电视剧简介" />
        </n-form-item>
        <n-form-item label="演员名单" path="cast">
          <n-input v-model:value="dramaForm.cast" type="textarea" :rows="3" placeholder="请输入演员名单，多个演员用逗号分隔" />
        </n-form-item>
        <n-form-item label="拍摄地" path="shootLocation">
          <n-input v-model:value="dramaForm.shootLocation" placeholder="请输入拍摄地" />
        </n-form-item>
        <n-form-item label="协拍服务" path="service">
          <n-input v-model:value="dramaForm.service" type="textarea" :rows="3" placeholder="请输入协拍服务描述" />
        </n-form-item>
        <n-form-item label="封面图片" path="cover">
          <n-input v-model:value="dramaForm.cover" placeholder="请输入封面图片URL" />
        </n-form-item>
        <n-form-item label="缩略封面" path="thumbCover">
          <n-input v-model:value="dramaForm.thumbCover" placeholder="请输入缩略封面URL" />
        </n-form-item>
        <n-form-item label="详情图片" path="image">
          <n-input v-model:value="dramaForm.image" placeholder="请输入详情图片URL" />
        </n-form-item>
        <n-form-item label="缩略详情图" path="thumbImage">
          <n-input v-model:value="dramaForm.thumbImage" placeholder="请输入缩略详情图URL" />
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
  NDataTable,
  NPopconfirm,
  NModal,
  NImage,
  NSpin,
  NPagination,
  useMessage
} from 'naive-ui'
import { getDramaPage, addDrama, updateDrama, deleteDrama, getDramaById } from '@/api'
import dayjs from 'dayjs'

const message = useMessage()

const isMobile = ref(false)
const loading = ref(false)

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const dramaList = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('新增电视剧')
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const dramaForm = reactive({
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

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const formRules = {
  name: [
    { required: true, message: '请输入电视剧名称', trigger: 'blur' },
    { min: 1, max: 100, message: '电视剧名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  prodCompany: [
    { required: true, message: '请输入出品公司', trigger: 'blur' },
    { min: 1, max: 100, message: '出品公司长度在 1 到 100 个字符', trigger: 'blur' }
  ]
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  {
      title: '封面',
      key: 'cover',
      width: 100,
      render: (row) => {
        // 优先使用缩略图URL（thumbUrl属性），如果没有则使用原图URL
        const thumbnailUrl = row.thumbUrl;
        // 获取原图URL用于预览
        const originalUrl = row.cover || '';
        
        return h(NImage, {
          width: 60,
          height: 45,
          src: thumbnailUrl || originalUrl, // 显示压缩后的图片
          objectFit: 'cover',
          previewDisabled: false, // 启用预览功能
          showToolbar: false,
          // 配置预览功能，点击时显示原图
          srcset: [
            {
              src: originalUrl,
              alt: '电视剧封面'
            }
          ],
          fallbackSrc: '/placeholder.jpg'
        })
      }
    },
  {
    title: '缩略封面',
    key: 'thumbCover',
    width: 100,
    render: (row) => {
      return row.thumbCover ? h(NImage, {
        width: 60,
        height: 45,
        src: row.thumbCover,
        objectFit: 'cover',
        previewDisabled: false
      }) : '-'
    }
  },
  {
    title: '详情图片',
    key: 'image',
    width: 100,
    render: (row) => {
      return row.image ? h(NImage, {
        width: 60,
        height: 45,
        src: row.image,
        objectFit: 'cover',
        previewDisabled: false
      }) : '-'
    }
  },
  {
    title: '缩略详情图',
    key: 'thumbImage',
    width: 100,
    render: (row) => {
      return row.thumbImage ? h(NImage, {
        width: 60,
        height: 45,
        src: row.thumbImage,
        objectFit: 'cover',
        previewDisabled: false
      }) : '-'
    }
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
      
      .drama-info {
        flex: 1;
        margin-right: 12px;
        
        .drama-name {
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 4px 0;
        }
        
        .drama-company {
          font-size: 14px;
          color: #6b7280;
          margin: 0;
        }
      }
      
      .drama-cover {
        flex-shrink: 0;
        
        .no-cover {
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
        
        .cast-info {
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
</style></div>
  </div>
  </div>
</template>
