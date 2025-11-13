<template>
  <n-layout class="main-layout">
    <n-layout-sider
      bordered
      :width="240"
      :collapsed="collapsed"
      :collapsed-width="64"
      collapse-mode="width"
      :native-scrollbar="false"
      class="sidebar"
    >
      <div class="logo" :class="{ collapsed }">
        <div class="logo-content">
          <Icon icon="mdi:film" :width="24" :height="24" v-if="!collapsed" />
          <Icon icon="mdi:film" :width="24" :height="24" v-else />
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
          <n-breadcrumb>
            <n-breadcrumb-item>{{ currentTitle }}</n-breadcrumb-item>
          </n-breadcrumb>
        </div>
        
        <div class="header-right">
          <n-dropdown :options="userOptions" @select="handleUserSelect">
            <n-button quaternary class="user-btn">
              <template #icon>
                <Icon icon="mdi:account-circle" />
              </template>
              <span>{{ userInfo?.username || '管理员' }}</span>
              <Icon icon="mdi:chevron-down" style="margin-left: 4px" />
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
import { ref, computed, h } from 'vue'
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

const menuOptions = [
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

const handleMenuSelect = (key) => {
  router.push(key)
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
}

.sidebar {
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  
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
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .collapse-btn {
      color: #6b7280;
      
      &:hover {
        color: #1f2937;
      }
    }
  }
  
  .header-right {
    .user-btn {
      color: #374151;
      font-weight: 500;
    }
  }
}

.main-content {
  background: #f9fafb;
  padding: 24px;
  overflow-y: auto;
  
  .page-wrapper {
    max-width: 1400px;
    margin: 0 auto;
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
