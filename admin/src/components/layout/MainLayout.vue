<template>
  <n-layout has-sider class="main-layout">
    <!-- 移动端遮罩层 -->
    <div 
      v-if="isMobile && !collapsed" 
      class="mobile-overlay"
      @click="collapsed = true"
    ></div>
    
    <n-layout-sider
      bordered
      :width="240"
      v-model:collapsed="collapsed"
      :collapsed-width="64"
      collapse-mode="width"
      :native-scrollbar="false"
      class="sidebar"
      :class="{ 'mobile-sidebar': isMobile }"
      :show-trigger="!isMobile"
      :collapsible="true"
    >
      <div class="logo" :class="{ collapsed }">
        <div class="logo-content">
          <Icon icon="mdi:film" :width="24" :height="24" />
          <span v-if="!collapsed" class="logo-text">拍在雅安</span>
        </div>
        <span v-if="!collapsed" class="logo-subtitle">管理后台</span>
      </div>
      
      <n-menu
        :value="activeKey"
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="20"
        :options="menuOptions"
        :render-label="renderMenuLabel"
        :render-icon="renderMenuIcon"
        @update:value="handleMenuSelect"
      />
    </n-layout-sider>
    
    <n-layout>
      <n-layout-header bordered class="header">
        <div class="header-left">
          <n-button
            quaternary
            circle
            @click="collapsed = !collapsed"
            class="collapse-btn"
          >
            <template #icon>
              <Icon :icon="collapsed ? 'mdi:menu' : 'mdi:menu-open'" />
            </template>
          </n-button>
          <n-breadcrumb class="breadcrumb">
            <n-breadcrumb-item>{{ currentTitle }}</n-breadcrumb-item>
          </n-breadcrumb>
        </div>
        
        <div class="header-right">
          <n-dropdown :options="userOptions" @select="handleUserSelect">
            <n-button quaternary class="user-btn">
              <template #icon>
                <Icon icon="mdi:account-circle" />
              </template>
              <span class="user-name">{{ userInfo?.username || '管理员' }}</span>
              <Icon icon="mdi:chevron-down" class="chevron-icon" />
            </n-button>
          </n-dropdown>
        </div>
      </n-layout-header>
      
      <n-layout-content class="main-content" :native-scrollbar="false">
        <div class="page-wrapper">
          <router-view v-slot="{ Component, route }">
            <transition
              name="page"
              mode="out-in"
            >
              <component :is="Component" :key="route.path" />
            </transition>
          </router-view>
        </div>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, computed, h, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Icon } from '@iconify/vue'
import {
  NLayout,
  NLayoutSider,
  NLayoutHeader,
  NLayoutContent,
  NMenu,
  NButton,
  NBreadcrumb,
  NBreadcrumbItem,
  NDropdown,
  useMessage,
  useDialog
} from 'naive-ui'
import { adminLogout } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const dialog = useDialog()
const collapsed = ref(false)
const isMobile = ref(false)

// 检测移动端
const checkMobile = () => {
  const mobile = window.innerWidth <= 768
  isMobile.value = mobile
  if (mobile) {
    collapsed.value = true
  }
}

// 移动端点击菜单后自动关闭侧边栏
const handleMenuSelect = (key) => {
  router.push(key)
  if (isMobile.value) {
    collapsed.value = true
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

const menuOptions = [
  {
    label: '用户管理',
    key: '/user',
    icon: 'mdi:account-group-outline'
  },
  {
    label: '文章管理',
    key: '/article',
    icon: 'mdi:file-document-outline'
  },
  {
    label: '电视剧管理',
    key: '/drama',
    icon: 'mdi:television'
  },
  {
    label: '场地管理',
    key: '/location',
    icon: 'mdi:map-marker-outline'
  },
  {
    label: '服务管理',
    key: '/service',
    icon: 'mdi:movie-open-outline'
  },
  {
    label: '报备管理',
    key: '/report',
    icon: 'mdi:clipboard-text-outline'
  },
  {
    label: '反馈管理',
    key: '/feedback',
    icon: 'mdi:message-text-outline'
  },
  {
    label: 'Banner管理',
    key: '/banner',
    icon: 'mdi:image-outline'
  },
  {
    label: '住宿管理',
    key: '/hotel',
    icon: 'mdi:home-outline'
  },
  {
    label: '旅游管理',
    key: '/tour',
    icon: 'mdi:map-outline'
  },
  {
    label: '旅游线路管理',
    key: '/tourroute',
    icon: 'mdi:routes'
  },
  {
    label: '政策管理',
    key: '/policy',
    icon: 'mdi:file-document-multiple-outline'
  },
  {
    label: '个人中心',
    key: '/profile',
    icon: 'mdi:account-circle-outline'
  }
]

const userOptions = [
  {
    label: '个人中心',
    key: 'profile',
    icon: () => h(Icon, { icon: 'mdi:account-circle-outline' })
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: () => h(Icon, { icon: 'mdi:logout' })
  }
]

const activeKey = computed(() => route.path)
const currentTitle = computed(() => {
  const item = menuOptions.find(item => item.key === route.path)
  return item ? item.label : '管理后台'
})

const userInfo = computed(() => userStore.userInfo)

const renderMenuIcon = (option) => {
  return h(Icon, {
    icon: option.icon,
    width: 20,
    height: 20
  })
}

const renderMenuLabel = (option) => {
  return option.label
}

const handleUserSelect = (key) => {
  if (key === 'profile') {
    router.push('/profile')
  } else if (key === 'logout') {
    dialog.warning({
      title: '确认退出',
      content: '确定要退出登录吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          await adminLogout()
          message.success('退出成功')
        } catch (error) {
          console.error('退出登录失败:', error)
        } finally {
          userStore.logout()
          router.push('/login')
        }
      }
    })
  }
}
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
  display: flex;
  position: relative;
}

// 移动端遮罩层
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  animation: fadeIn 0.3s ease;
}

.sidebar {
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &.mobile-sidebar {
    position: fixed !important;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    height: 100vh;
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
    
    :deep(.n-layout-sider--collapsed) {
      transform: translateX(-100%);
    }
  }
  
  // 确保折叠状态正确应用
  :deep(.n-layout-sider) {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
  
  .logo {
    height: 64px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid #e5e7eb;
    padding: 12px;
    transition: all 0.3s;
    
    &.collapsed {
      .logo-content {
        justify-content: center;
      }
    }
    
    .logo-content {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
      
      .logo-text {
        font-size: 18px;
        font-weight: 600;
        color: #1f2937;
        letter-spacing: 0.5px;
      }
    }
    
    .logo-subtitle {
      font-size: 12px;
      color: #6b7280;
      font-weight: 400;
    }
  }
}

.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 100;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;
    min-width: 0;
    
    .collapse-btn {
      color: #6b7280;
      flex-shrink: 0;
      
      &:hover {
        color: #1f2937;
      }
    }
    
    .breadcrumb {
      flex: 1;
      min-width: 0;
      
      :deep(.n-breadcrumb-item) {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 14px;
      }
    }
  }
  
  .header-right {
    flex-shrink: 0;
    
    .user-btn {
      color: #374151;
      font-weight: 500;
      padding: 4px 12px;
      
      .user-name {
        margin: 0 4px;
      }
      
      .chevron-icon {
        margin-left: 4px;
        font-size: 16px;
      }
    }
  }
}

.main-content {
  background: #f9fafb;
  padding: 16px;
  overflow-y: auto;
  min-height: calc(100vh - 64px);
  
  .page-wrapper {
    max-width: 100%;
    margin: 0;
  }
}

// 移动端适配
@media (max-width: 768px) {
  .header {
    padding: 0 12px;
    height: 56px;
    
    .header-left {
      gap: 8px;
      
      .breadcrumb {
        :deep(.n-breadcrumb-item) {
          font-size: 14px;
        }
      }
    }
    
    .header-right {
      .user-btn {
        padding: 4px 8px;
        
        .user-name {
          display: none;
        }
        
        .chevron-icon {
          display: none;
        }
      }
    }
  }
  
  .main-content {
    padding: 12px;
    min-height: calc(100vh - 56px);
  }
  
  .sidebar {
    width: 260px !important;
    
    .logo {
      height: 56px;
      padding: 10px;
    }
  }
}

// 小屏幕适配
@media (max-width: 480px) {
  .header {
    padding: 0 8px;
    height: 52px;
    
    .header-left {
      .breadcrumb {
        :deep(.n-breadcrumb-item) {
          font-size: 13px;
        }
      }
    }
  }
  
  .main-content {
    padding: 8px;
    min-height: calc(100vh - 52px);
  }
  
  .sidebar {
    width: 240px !important;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

// 页面过渡动画
.page-enter-active,
.page-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
