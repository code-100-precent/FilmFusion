import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
// import { getAdminInfo } from '@/api'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', transition: 'fade' }
  },
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    redirect: '/profile',
    children: [
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
        meta: { title: '个人中心', icon: 'UserFilled', transition: 'slide' }
      },
        {
          path: 'article',
          name: 'article',
          component: () => import('../views/article/ArticleList.vue'),
          meta: {
            title: '文章管理'
          }
        },
        {
          path: 'drama',
          name: 'drama',
          component: () => import('../views/drama/DramaList.vue'),
          meta: {
            title: '电视剧管理'
          }
        },
        {
          path: 'image',
          name: 'image',
          component: () => import('../views/image/ImageList.vue'),
          meta: {
            title: '图片管理'
          }
        },
        {
          path: 'location',
          name: 'location',
          component: () => import('../views/location/LocationList.vue'),
          meta: {
            title: '场地管理'
          }
        },
        {
          path: 'service',
          name: 'service',
          component: () => import('../views/service/ServiceList.vue'),
          meta: {
            title: '服务管理'
          }
        },
        {
          path: 'report',
          name: 'report',
          component: () => import('../views/report/ReportList.vue'),
          meta: {
            title: '拍摄报告管理'
          }
        }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  if (to.path === '/login') {
    if (userStore.token) {
      next('/')
    } else {
      next()
    }
  } else {
    if (!userStore.token) {
      next('/login')
    } else {
      // 模拟验证token有效
      try {
        // 延迟一小段时间模拟网络请求
        await new Promise(resolve => setTimeout(resolve, 300))
        
        // 模拟用户信息
        const mockUserInfo = {
          id: 1,
          username: '管理员',
          email: 'admin@example.com',
          avatar: '',
          role: 'ADMIN'
        }
        
        // 更新用户信息
        userStore.setUserInfo(mockUserInfo)
        next()
      } catch (error) {
        console.error('验证失败:', error)
        userStore.logout()
        next('/login')
      }
    }
  }
})

export default router

