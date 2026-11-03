import  useUserStore  from '@/stores/modules/userStore.js' // 引入Pinia

export default {
    mounted(el, binding) {
        // 1. 获取用户当前的权限集合
        const userStore = useUserStore()
        const permissions = userStore.permissions
        // 2. 获取指令绑定的值（比如 v-has-perm="['system:user:add']"）
        const requiredPerms = binding.value

        // 3. 超级管理员直接放行（如果后端没给 admin 返回权限，前端得硬编码防一下）
        if (permissions.includes('*:*:*')) return

        // 4. 如果权限集合里没有要求的权限，直接把按钮从页面上删掉！
        const hasPerm = permissions.some(p => requiredPerms.includes(p))

        if (!hasPerm) {
            // 把当前元素的父节点干掉
            el.parentNode && el.parentNode.removeChild(el)
        }
    }
}