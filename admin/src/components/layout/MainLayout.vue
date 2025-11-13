<template>
  <div class="main-layout">
    <el-container class="layout-container">
      <el-aside width="220px" class="sidebar">
        <div class="logo">
          <h3>拍在雅安</h3>
          <p class="logo-subtitle">管理后台</p>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          class="sidebar-menu"
          :collapse="false"
        >
          <!-- 雅安相关管理菜单 -->
          <el-sub-menu index="/yaan">
            <template #title>
              <el-icon><Location /></el-icon>
              <span>雅安影视管理</span>
            </template>
            <el-menu-item index="/article">
              <el-icon><Document /></el-icon>
              <span>文章管理</span>
            </el-menu-item>
            <el-menu-item index="/drama">
              <el-icon><VideoPlay /></el-icon>
              <span>电视剧管理</span>
            </el-menu-item>
            <el-menu-item index="/image">
              <el-icon><Picture /></el-icon>
              <span>图片管理</span>
            </el-menu-item>
            <el-menu-item index="/location">
              <el-icon><MapLocation /></el-icon>
              <span>场地管理</span>
            </el-menu-item>
            <el-menu-item index="/service">
              <el-icon><Service /></el-icon>
              <span>服务管理</span>
            </el-menu-item>
            <el-menu-item index="/report">
              <el-icon><DataAnalysis /></el-icon>
              <span>拍摄报告管理</span>
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 个人中心 -->
          <el-menu-item index="/profile">
            <el-icon><UserFilled /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container class="main-container">
        <el-header class="header">
          <div class="header-title">
            {{ route.meta.title || '管理后台' }}
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-info">
                <el-icon><User /></el-icon>
                <span>管理员</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon class="mr-2"><UserFilled /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" class="logout-item">
                    <el-icon class="mr-2"><CircleClose /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main class="main-content">
          <div class="page-wrapper">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ArrowDown, CircleClose, UserFilled } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { adminLogout } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await adminLogout()
        userStore.logout()
        router.push('/login')
      } catch (error) {
        // 即使退出失败也清除本地token
        userStore.logout()
        router.push('/login')
      }
    }).catch(() => {})
  }
}
</script>

<style scoped lang="scss">
.main-layout {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.layout-container {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #f9f7ff 0%, #f3f0ff 100%);
  border-right: 1px solid #ebe6ff;
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  
  .logo {
    height: 70px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid #ebe6ff;
    background: linear-gradient(135deg, #9c88ff 0%, #b8abff 100%);
    padding: 0 20px;
    
    h3 {
      color: white;
      font-size: 20px;
      font-weight: 600;
      margin-bottom: 4px;
      letter-spacing: 1px;
    }
    
    .logo-subtitle {
      color: rgba(255, 255, 255, 0.9);
      font-size: 12px;
      font-weight: 400;
    }
  }
  
  .sidebar-menu {
    flex: 1;
    border-right: none;
    background-color: transparent;
    overflow-y: auto;
    padding: 10px 0;
    
    :deep(.el-menu-item) {
      height: 50px;
      line-height: 50px;
      margin: 4px 12px;
      border-radius: 8px;
      color: #606266;
      transition: all 0.3s ease;
      font-size: 14px;
      
      .el-icon {
        font-size: 18px;
        margin-right: 8px;
        color: #9c88ff;
      }
      
      &:hover {
        background-color: #f3f0ff;
        color: #9c88ff;
        
        .el-icon {
          color: #9c88ff;
        }
      }
      
      &.is-active {
        background: linear-gradient(135deg, #9c88ff 0%, #b8abff 100%);
        color: white;
        box-shadow: 0 4px 12px rgba(156, 136, 255, 0.3);
        
        .el-icon {
          color: white;
        }
      }
    }
  }
}

.main-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  background: white;
  border-bottom: 1px solid #ebe6ff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 2px 8px rgba(156, 136, 255, 0.08);
  
  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    letter-spacing: 0.5px;
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 8px 16px;
      border-radius: 8px;
      transition: all 0.3s ease;
      color: #606266;
      
      .el-icon {
        font-size: 18px;
      }
      
      &:hover {
        background-color: #f9f7ff;
        color: #9c88ff;
      }
    }
  }
}

.main-content {
  background-color: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
  height: calc(100vh - 60px);
}

.page-wrapper {
  animation: fadeIn 0.3s ease-in-out;
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

.logout-item {
  color: #f56c6c;
  
  &:hover {
    background-color: #fef0f0;
  }
}

.mr-2 {
  margin-right: 8px;
}
</style>
