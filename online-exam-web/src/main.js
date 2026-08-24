import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css' // 引入样式
import router from './router'
import App from './App.vue'

const app = createApp(App)

app.use(router)
app.use(ElementPlus) // 安装 UI 库
app.mount('#app')