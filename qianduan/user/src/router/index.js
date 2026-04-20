import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/customer/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/home',
    redirect: '/customer/home'
  },
  {
    path: '/customer',
    component: () => import('@/layout/CustomerLayout.vue'),
    redirect: '/customer/home',
    children: [
      {
        path: 'home',
        name: 'CustomerHome',
        component: () => import('@/views/customer/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'book/:id',
        name: 'BookDetail',
        component: () => import('@/views/customer/BookDetail.vue'),
        meta: { title: '图书详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/customer/Cart.vue'),
        meta: { title: '购物车' }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/customer/Checkout.vue'),
        meta: { title: '订单确认' }
      },
      {
        path: 'pay',
        name: 'Pay',
        component: () => import('@/views/customer/Pay.vue'),
        meta: { title: '支付订单' }
      },
      {
        path: 'order',
        name: 'CustomerOrder',
        component: () => import('@/views/customer/OrderList.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: 'member',
        name: 'MemberCenter',
        component: () => import('@/views/customer/MemberCenter.vue'),
        meta: { title: '会员中心' }
      },
      {
        path: 'donation',
        name: 'Donation',
        component: () => import('@/views/customer/Donation.vue'),
        meta: { title: '图书捐赠' }
      },
      {
        path: 'charity',
        name: 'Charity',
        component: () => import('@/views/customer/Charity.vue'),
        meta: { title: '公益专区' }
      },
      {
        path: 'password',
        name: 'ChangePassword',
        component: () => import('@/views/customer/ChangePassword.vue'),
        meta: { title: '修改密码' }
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
  if (to.meta.title) {
    document.title = to.meta.title + ' - 圣惟书店管理系统'
  }
  next()
})

export default router
