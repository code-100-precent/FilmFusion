import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { Icon } from '@iconify/vue'
import naive from 'naive-ui'

// 引入字体
import 'vfonts/Lato.css'
import 'vfonts/FiraCode.css'

import App from './App.vue'
import router from './router'
import './assets/css/main.css'

const app = createApp(App)

// 全局注册图标组件
app.component('Icon', Icon)

// 全局注册 EnhancedTextarea 组件
import EnhancedTextarea from './components/EnhancedTextarea.vue'
app.component('EnhancedTextarea', EnhancedTextarea)

app.use(createPinia())
app.use(router)
app.use(naive)

app.mount('#app')

