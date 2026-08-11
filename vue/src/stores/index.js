//导入Pinia的创建函数
import { createPinia } from 'pinia'

/**
 * 创建一个全局的Pinia仓库
 * 整个应用共享的状态管理中心
 */

const store = createPinia();

//导出仓库实例
export default store

