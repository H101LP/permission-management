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
//导入svg图标
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/SvgIcon'
//引入全局样式
import '@/assets/styles/all.css'
//ui
import VxeUIBase from 'vxe-pc-ui'
import 'vxe-pc-ui/es/style.css'
import VxeUITable from 'vxe-table'
import 'vxe-table/es/style.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.use(store)
app.use(VxeUIBase)
app.use(VxeUITable)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.component('svg-icon', SvgIcon)

app.use(ElementPlus, {
    //本地化配置 设置中文
    locale: zhCn,
})




app.mount('#app')
