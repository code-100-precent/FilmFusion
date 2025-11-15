import App from './App'
import './utils/http' // 导入 HTTP 工具，确保拦截器被注册

import Vue from 'vue'
import './uni.promisify.adaptor'
import store from './store/index.js'
Vue.config.productionTip = false

App.mpType = 'app'
const app = new Vue({
	store,
	...App
})

// 初始化用户信息（从本地存储恢复）
store.dispatch('initUserInfo')

app.$mount()
