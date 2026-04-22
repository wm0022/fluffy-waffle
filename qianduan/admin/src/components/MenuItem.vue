<template>
  <!-- 有子菜单 → 使用 el-sub-menu -->
  <el-submenu v-if="menu.children && menu.children.length > 0" :index="String(menu.menuId)">
    <template slot="title">
      <i :class="resolvedIcon"></i>
      <span slot="title">{{ menu.menuName }}</span>
    </template>
    <menu-item v-for="child in menu.children" :key="child.menuId" :menu="child" />
  </el-submenu>

  <!-- 无子菜单 → 使用 el-menu-item -->
  <el-menu-item v-else :index="resolvePath(menu.path)">
    <i :class="resolvedIcon"></i>
    <span slot="title">{{ menu.menuName }}</span>
  </el-menu-item>
</template>

<script>
export default {
  name: 'MenuItem',
  props: {
    menu: {
      type: Object,
      required: true,
      default: () => ({})
    }
  },
  methods: {
    /**
     * 智能解析菜单路径：
     * - 如果 path 已包含 /admin/ 前缀（如 "admin/inventory"），直接补全为 /admin/inventory
     * - 如果 path 不含前缀（如 "inventory"），拼接为 /admin/inventory
     * - 统一返回以 / 开头的完整路径供 el-menu-item 的 index 使用（router 模式）
     */
    resolvePath(path) {
      if (!path) return ''
      // 已包含 admin/ 前缀，确保以 / 开头即可
      if (path.startsWith('admin/') || path.includes('/admin/')) {
        return '/' + path.replace(/^\//, '')
      }
      // 纯路径名，补全 /admin/ 前缀
      return '/admin/' + path.replace(/^\/+/, '')
    },
    /**
     * 菜单名称到图标的映射表（与原始静态菜单保持一致）
     * 优先使用后端返回的 icon 字段，若为空则按名称匹配
     */
    getIconByMenuName(menuName, fallbackIcon) {
      const name = (menuName || '').trim()
      const iconMap = {
        '角色管理': 'el-icon-s-custom',
        '用户管理': 'el-icon-user',
        '员工管理': 'el-icon-user-solid',
        '会员管理': 'el-icon-user',
        '顾客管理': 'el-icon-user',
        '图书管理': 'el-icon-reading',
        '库存管理': 'el-icon-office-building',
        '订单管理': 'el-icon-shopping-cart-2',
        '爱心赠书人士': 'el-icon-s-promotion',
        '捐赠审核': 'el-icon-document',
        '捐赠管理': 'el-icon-document',
        '评价管理': 'el-icon-chat-dot-round',
        '修改密码': 'el-icon-lock',
        '首页': 'el-icon-house'
      }
      return iconMap[name] || fallbackIcon || 'el-icon-document'
    }
  },
  computed: {
    resolvedIcon() {
      // 优先使用后端配置的 icon，否则按菜单名称映射，最终 fallback
      if (this.menu.icon && this.menu.icon.trim()) return this.menu.icon.trim()
      return this.getIconByMenuName(this.menu.menuName)
    }
  }
}
</script>
