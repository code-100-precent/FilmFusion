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
            title: '报备管理'
          }
        },
        {
          path: 'feedback',
          name: 'feedback',
          component: () => import('../views/feedback/FeedbackList.vue'),
          meta: {
            title: '反馈管理'
          }
        },
        {
          path: 'user',
          name: 'user',
          component: () => import('../views/user/UserList.vue'),
          meta: {
            title: '用户管理'
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
      // 验证token有效并获取用户信息
      try {
        const { getAdminInfo } = await import('@/api')
        const res = await getAdminInfo()
        
        if (res.code === 200 && res.data) {
          // 检查用户角色是否为管理员
          if (res.data.role !== 'ADMIN') {
            userStore.logout()
            next('/login')
            return
          }
          
          // 更新用户信息
          userStore.setUserInfo(res.data)
          next()
        } else {
          throw new Error('获取用户信息失败')
        }
      } catch (error) {
        console.error('验证失败:', error)
        userStore.logout()
        next('/login')
      }
    }
  }
})

export default router

