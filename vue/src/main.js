import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
//注册中文语言包
import zhCn from 'element-plus/es/locale/lang/zh-cn'
//引入路由守卫
import './permission'
//导入pinia
import store from './stores'


const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.use(store)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.use(ElementPlus, {
    //本地化配置 设置中文
    locale: zhCn,
})




app.mount('#app')
