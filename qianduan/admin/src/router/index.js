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
        name: 'Member',
        component: () => import('@/views/admin/member/MemberList.vue'),
        meta: { title: '会员管理', requireAdmin: true }
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
        component: () => import('@/views/admin/donation/PersonManage.vue'),
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

  // 2. 权限校验：标记了 requireAdmin 的路由，只有 userType=1 的管理员可访问
  if (to.meta.requireAdmin && !store.getters.isAdmin) {
    console.warn('[权限拦截] 非管理员用户试图访问受限页面:', to.path)
    return next('/403')
  }

  next()
})

export default router
