import axios from 'axios'
import store from '@/store'

const API_BASE = '/api'

// 创建axios实例
const service = axios.create({
  baseURL: API_BASE,
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从 Vuex Store 读取 token（Store 数据来源于 Cookie）
    const token = store.state.token
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 如果返回 code 为 200，返回完整的 data 对象
    if (res.code === 200) {
      return res.data
    } else {
      return Promise.reject(new Error(res.message || 'Error'))
    }
  },
  error => {
    console.error('Response error:', error)
    return Promise.reject(error)
  }
)

// 通用request函数
const request = (config) => {
  return service(config)
}

export default {
  auth: {
    login: (username, password) => request({
      url: `/auth/login`,
      method: 'post',
      params: { username, password }
    }),
    logout: () => request({
      url: `/auth/logout`,
      method: 'post'
    }),
    getInfo: () => request({
      url: `/auth/info`,
      method: 'get'
    })
  },
  book: {
    pageList: (params) => request({
      url: `/book/page`,
      method: 'get',
      params
    }),
    getById: (id) => request({
      url: `/book/${id}`,
      method: 'get'
    }),
    getByIsbn: (isbn) => request({
      url: `/book/isbn/${isbn}`,
      method: 'get'
    }),
    add: (data) => request({
      url: `/book`,
      method: 'post',
      data
    }),
    update: (data) => request({
      url: `/book`,
      method: 'put',
      data
    }),
    delete: (id) => request({
      url: `/book/${id}`,
      method: 'delete'
    }),
    updateShelfStatus: (id, status) => request({
      url: `/book/${id}/shelf`,
      method: 'put',
      params: { shelfStatus: status }
    }),
    count: () => request({
      url: `/book/count`,
      method: 'get'
    })
  },
  order: {
    list: (params) => request({
      url: `/order/all`,
      method: 'get',
      params
    }),
    getDetail: (id) => request({
      url: `/order/detail/${id}`,
      method: 'get'
    }),
    ship: (id) => request({
      url: `/order/ship/${id}`,
      method: 'put'
    }),
    complete: (id) => request({
      url: `/order/complete/${id}`,
      method: 'put'
    }),
    cancel: (orderNo) => request({
      url: `/order/cancel/${orderNo}`,
      method: 'put'
    }),
    refundList: (params) => request({
      url: `/order/refund/list`,
      method: 'get',
      params
    }),
    handleRefund: (refundId, status, remark) => request({
      url: `/order/refund/handle`,
      method: 'post',
      data: { refundId, status, remark }
    })
  },
  donation: {
    list: (status) => request({
      url: `/donation/all`,
      method: 'get',
      params: { status }
    }),
    review: (id, status, reviewRemark) => request({
      url: `/donation/review`,
      method: 'post',
      data: { id, status, reviewRemark }
    }),
    update: (data) => request({
      url: `/donation/update`,
      method: 'post',
      data
    }),
    count: () => request({
      url: `/donation/count`,
      method: 'get'
    }),
    personList: (status) => request({
      url: `/donation/person/list`,
      method: 'get',
      params: { status }
    }),
    savePerson: (data) => request({
      url: `/donation/person/save`,
      method: 'post',
      data
    }),
    deletePerson: (id) => request({
      url: `/donation/person/${id}`,
      method: 'delete'
    }),
    reviewPerson: (id, status) => request({
      url: `/donation/person/review`,
      method: 'post',
      data: { id, status }
    })
  },
  home: {
    getTodoStats: () => request({
      url: `/home/todo-stats`,
      method: 'get'
    }),
    getSalesStats: () => request({
      url: `/home/sales-stats`,
      method: 'get'
    })
  },
  sysUser: {
    pageList: (params) => request({
      url: `/sysUser/page`,
      method: 'get',
      params
    }),
    list: () => request({
      url: `/sysUser/list`,
      method: 'get'
    }),
    getById: (id) => request({
      url: `/sysUser/${id}`,
      method: 'get'
    }),
    update: (data) => request({
      url: `/sysUser`,
      method: 'put',
      data
    }),
    delete: (id) => request({
      url: `/sysUser/${id}`,
      method: 'delete'
    }),
    changePassword: (data) => request({
      url: `/auth/change-password`,
      method: 'post',
      data
    }),
    create: (data) => request({
      url: `/sysUser/create`,
      method: 'post',
      data
    })
  },
  inventory: {
    list: () => request({
      url: `/inventory/list`,
      method: 'get'
    }),
    warning: () => request({
      url: `/inventory/warning`,
      method: 'get'
    }),
    statistics: (params) => request({
      url: `/inventory/statistics`,
      method: 'get',
      params
    }),
    restockSuggestions: () => request({
      url: `/inventory/restock-suggestions`,
      method: 'get'
    }),
    update: (data) => request({
      url: `/inventory/update`,
      method: 'put',
      data
    })
  },
  upload: {
    bookCover: (formData) => request({
      url: `/upload/bookCover`,
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
    file: (formData) => request({
      url: `/upload/file`,
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  review: {
    pageList: (params) => request({
      url: `/book-review/page`,
      method: 'get',
      params
    }),
    getById: (reviewId) => request({
      url: `/book-review/${reviewId}`,
      method: 'get'
    }),
    audit: (reviewId, data) => request({
      url: `/book-review/${reviewId}/audit`,
      method: 'post',
      data
    }),
    reply: (reviewId, data) => request({
      url: `/book-review/${reviewId}/reply`,
      method: 'post',
      data
    }),
    toggleVisibility: (reviewId) => request({
      url: `/book-review/${reviewId}/toggle-visibility`,
      method: 'put'
    }),
    count: (params) => request({
      url: `/book-review/count`,
      method: 'get',
      params
    })
  },
  role: {
    list: () => request({
      url: `/role/list`,
      method: 'get'
    }),
    pageList: (params) => request({
      url: `/role/page`,
      method: 'get',
      params
    }),
    save: (data) => request({
      url: `/role/save`,
      method: 'post',
      data
    }),
    update: (data) => request({
      url: `/role/update`,
      method: 'put',
      data
    }),
    delete: (id) => request({
      url: `/role/${id}`,
      method: 'delete'
    }),
    assignMenus: (roleId, menuIds) => request({
      url: `/role/${roleId}/menus`,
      method: 'post',
      data: { menuIds }
    }),
    getMenus: (roleId) => request({
      url: `/role/${roleId}/menus`,
      method: 'get'
    })
  },
  menu: {
    list: () => request({
      url: `/menu/list`,
      method: 'get'
    }),
    tree: () => request({
      url: `/menu/tree`,
      method: 'get'
    }),
    /**
     * 获取当前登录用户的菜单权限列表（根据角色过滤）
     */
    getMyMenus: () => request({
      url: `/menu/my-menus`,
      method: 'get'
    }),
    /**
     * 获取当前登录用户的所有权限标识（用于按钮级权限控制）
     */
    getMyPerms: () => request({
      url: `/menu/my-perms`,
      method: 'get'
    }),
    save: (data) => request({
      url: `/menu/save`,
      method: 'post',
      data
    }),
    update: (data) => request({
      url: `/menu/update`,
      method: 'put',
      data
    }),
    delete: (id) => request({
      url: `/menu/${id}`,
      method: 'delete'
    })
  },
  donor: {
    pageList: (params) => request({
      url: `/donor/page`,
      method: 'get',
      params
    }),
    list: () => request({
      url: `/donor/count`,
      method: 'get'
    }),
    save: (data) => request({
      url: `/donor`,
      method: 'post',
      data
    }),
    update: (data) => request({
      url: `/donor`,
      method: 'put',
      data
    }),
    delete: (id) => request({
      url: `/donor/${id}`,
      method: 'delete'
    })
  }
}
