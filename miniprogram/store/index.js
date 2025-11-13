import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    // 用户信息
    userInfo: null,
    token: null,
    // 是否已登录
    isLoggedIn: false
  },
  mutations: {
    // 设置用户信息
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      state.isLoggedIn = !!userInfo
    },
    // 设置token
    SET_TOKEN(state, token) {
      state.token = token
      if (token) {
        uni.setStorageSync('token', token)
      } else {
        uni.removeStorageSync('token')
      }
    },
    // 清除用户信息
    CLEAR_USER_INFO(state) {
      state.userInfo = null
      state.token = null
      state.isLoggedIn = false
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
    }
  },
  actions: {
    // 登录
    login({ commit }, { token, userInfo }) {
      commit('SET_TOKEN', token)
      commit('SET_USER_INFO', userInfo)
      uni.setStorageSync('userInfo', userInfo)
    },
    // 登出
    logout({ commit }) {
      commit('CLEAR_USER_INFO')
      uni.reLaunch({
        url: '/pages/login/login'
      })
    },
    // 初始化用户信息（从本地存储恢复）
    initUserInfo({ commit }) {
      const token = uni.getStorageSync('token')
      const userInfo = uni.getStorageSync('userInfo')
      if (token && userInfo) {
        commit('SET_TOKEN', token)
        commit('SET_USER_INFO', userInfo)
      }
    },
    // 更新用户信息
    updateUserInfo({ commit }, userInfo) {
      commit('SET_USER_INFO', userInfo)
      uni.setStorageSync('userInfo', userInfo)
    },
    // 设置用户信息（别名）
    setUserInfo({ commit }, userInfo) {
      commit('SET_USER_INFO', userInfo)
      uni.setStorageSync('userInfo', userInfo)
    }
  },
  getters: {
    // 是否已登录
    isLoggedIn: state => state.isLoggedIn,
    // 用户信息
    userInfo: state => state.userInfo,
    // 是否是管理员
    isAdmin: state => {
      return state.userInfo && (state.userInfo.role === 'ADMIN' || state.userInfo.role === 'SUPER_ADMIN')
    }
  }
})
