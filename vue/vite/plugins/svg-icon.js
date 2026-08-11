//导入插件的创建函数
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
//导入path模块用于处理文件路径
import path from 'path'

/**
 * 创建svg图标插件的配置函数
 */
export default function createSvgIcons() {
    return createSvgIconsPlugin({
        // 指定图标文件夹
        iconDirs: [path.resolve(process.cwd(), 'src/assets/icons/svg')],
        // 指定图标Id格式
        symbolId: 'icon-[name]',

    })
}

