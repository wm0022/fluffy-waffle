import Vue from 'vue'
import store from '@/store'

/**
 * v-permission 指令
 * 按钮级权限控制，根据权限标识决定 DOM 元素的显示/隐藏
 *
 * 使用方式:
 *   <el-button v-permission="'book:add'">新增</el-button>
 *   <el-button v-permission="['book:delete', 'admin']">删除</el-button>
 *
 * 原理: 从 Vuex Store 的 permissions 数组中检查是否包含指定权限标识
 */
Vue.directive('permission', {
  inserted(el, binding) {
    const perms = store.getters.permissions || []
    const requiredPerms = binding.value

    // 支持字符串或数组形式传入
    let hasPerm = false
    if (Array.isArray(requiredPerms)) {
      // 数组：满足任一权限即可（OR 逻辑）
      hasPerm = requiredPerms.some(p => perms.includes(p))
    } else if (typeof requiredPerms === 'string') {
      // 字符串：直接匹配
      hasPerm = perms.includes(requiredPerms)
    }

    if (!hasPerm) {
      // 无权限则从 DOM 中移除该元素
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
})
