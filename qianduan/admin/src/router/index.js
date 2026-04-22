import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/admin/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/components/Forbidden.vue'),
    meta: { title: '无权限', public: true }
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/admin/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/admin/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'book',
        name: 'Book',
        component: () => import('@/views/admin/book/BookList.vue'),
        meta: { title: '图书管理', requireAdmin: true }
      },
      {
        path: 'book/add',
        name: 'BookAdd',
        component: () => import('@/views/admin/book/BookAdd.vue'),
        meta: { title: '添加图书', requireAdmin: true }
      },
      {
        path: 'book/edit/:id',
        name: 'BookEdit',
        component: () => import('@/views/admin/book/BookEdit.vue'),
        meta: { title: '编辑图书', requireAdmin: true }
      },
      {
        path: 'inventory',
        name: 'Inventory',
        component: () => import('@/views/admin/inventory/InventoryList.vue'),
        meta: { title: '库存管理', requireAdmin: true }
      },
      {
        path: 'member',
        name: 'Staff',
        component: () => import('@/views/admin/member/MemberList.vue'),
        meta: { title: '员工管理', requireAdmin: true }
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('@/views/admin/customer/CustomerList.vue'),
        meta: { title: '顾客管理', requireAdmin: true }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/admin/order/OrderList.vue'),
        meta: { title: '订单管理', requireAdmin: true }
      },
      {
        path: 'donor',
        name: 'Donor',
        component: () => import('@/views/admin/donation/DonorManage.vue'),
        meta: { title: '爱心赠书人士管理', requireAdmin: true }
      },
      {
        path: 'donation-manage',
        name: 'DonationManage',
        component: () => import('@/views/admin/donation/DonationManage.vue'),
        meta: { title: '捐赠审核管理', requireAdmin: true }
      },
      {
        path: 'password',
        name: 'AdminChangePassword',
        component: () => import('@/views/admin/system/ChangePassword.vue'),
        meta: { title: '修改密码' }
      },
      {
        path: 'review',
        name: 'ReviewManage',
        component: () => import('@/views/admin/review/ReviewManage.vue'),
        meta: { title: '评价管理', requireAdmin: true }
      },
      {
        path: 'role',
        name: 'RoleManage',
        component: () => import('@/views/admin/system/RoleManage.vue'),
        meta: { title: '角色管理', requireAdmin: true }
      },
      {
        path: 'donor-manage',
        name: 'DonorManage',
        component: () => import('@/views/admin/donation/DonorManage.vue'),
        meta: { title: '爱心赠书人士管理', requireAdmin: true }
      }
    ]
  }

]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title + ' - 圣惟书店管理系统'
  }

  // 公开页面直接放行
  if (to.meta.public) {
    return next()
  }

  // 1. 未登录 → 跳转登录页（记录目标路径，登录后可回跳）
  if (!store.state.token) {
    return next('/login?redirect=' + encodeURIComponent(to.fullPath))
  }

  // 2. 权限校验：基于 RBAC 菜单权限判断
  if (to.meta.requireAdmin && !hasMenuPermission(to.path)) {
    console.warn('[权限拦截] 用户无权访问该页面:', to.path)
    return next('/403')
  }

  next()
})

/**
 * 检查当前用户是否有访问目标路由的菜单权限
 * 规则：
 *   - userType=1 超级管理员：放行所有路由
 *   - 其他角色：检查路由路径是否在已授权的菜单路径中（支持子路径匹配）
 *     例如拥有 "book" 权限的用户可以访问 /admin/book、/admin/book/add、/admin/book/edit/123
 */
function hasMenuPermission(routePath) {
  // 超级管理员直接放行
  if (store.getters.isAdmin) return true

  const menus = store.state.menus || []
  const allowedPaths = extractMenuPaths(menus)

  console.log('[权限校验] 目标路由:', routePath)
  console.log('[权限校验] 菜单数据(JSON):', JSON.stringify(menus))
  console.log('[权限校验] 提取到的路径列表:', allowedPaths)

  // 兼容两种格式：已带 /admin/ 前缀 或 纯路径名
  return allowedPaths.some(p => {
    const normalized = p.startsWith('/admin/') ? p : ('/admin/' + p).replace(/\/+/g, '/')
    return routePath === normalized || routePath.startsWith(normalized + '/')
  })
}

/**
 * 从菜单树中递归提取所有叶子节点的 path 字段
 */
function extractMenuPaths(menus) {
  const paths = []
  function traverse(items) {
    for (const item of items || []) {
      if (item.children && item.children.length > 0) {
        traverse(item.children)
      } else if (item.path) {
        paths.push(item.path)
      }
    }
  }
  traverse(menus)
  return paths
}

export default router
