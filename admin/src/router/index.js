import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getAdminInfo } from '@/api'

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
      // 验证token是否有效
      try {
        const res = await getAdminInfo()
        if (res.code === 200 && res.data) {
          // 更新用户信息
          userStore.setUserInfo(res.data)
          next()
        } else {
          // token无效，清除并跳转登录
          userStore.logout()
          next('/login')
        }
      } catch (error) {
        // token验证失败，清除并跳转登录
        userStore.logout()
        next('/login')
      }
    }
  }
})

export default router

