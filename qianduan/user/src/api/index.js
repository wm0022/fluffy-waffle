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
    login: (username, password, captchaKey, captchaCode) => request({
      url: `/customer/auth/login`,
      method: 'post',
      params: { username, password, captchaKey, captchaCode }
    }),
    register: (username, password, email, phone) => request({
      url: `/customer/auth/register`,
      method: 'post',
      params: { username, password, email, phone }
    }),
    logout: () => request({
      url: `/customer/auth/logout`,
      method: 'post'
    }),
    getInfo: () => request({
      url: `/customer/auth/info`,
      method: 'get'
    }),
    changePassword: (data) => request({
      url: `/customer/auth/change-password`,
      method: 'post',
      data
    })
  },
  book: {
    pageList: (params) => request({
      url: `/book/page`,
      method: 'get',
      params
    }),
    getHot: (limit) => request({
      url: `/book/hot`,
      method: 'get',
      params: { limit }
    }),
    getNew: (limit) => request({
      url: `/book/new`,
      method: 'get',
      params: { limit }
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
  cart: {
    add: (bookId, quantity) => request({
      url: `/cart/add`,
      method: 'post',
      data: { bookId, quantity }
    }),
    list: () => request({
      url: `/cart/list`,
      method: 'get'
    }),
    remove: (bookId) => request({
      url: `/cart/remove`,
      method: 'delete',
      data: { bookId }
    }),
    clear: () => request({
      url: `/cart/clear`,
      method: 'delete'
    })
  },
  order: {
    create: (data) => request({
      url: `/order/create`,
      method: 'post',
      data
    }),
    list: (params) => request({
      url: `/order/list`,
      method: 'get',
      params
    }),
    getItems: (orderId) => request({
      url: `/order/items/${orderId}`,
      method: 'get'
    }),
    pay: (orderNo) => request({
      url: `/order/pay`,
      method: 'post',
      data: { orderNo }
    }),
    applyRefund: (orderNo, reason) => request({
      url: `/order/refund/apply`,
      method: 'post',
      data: { orderNo, reason }
    }),
    getRefundList: (params) => request({
      url: `/order/refund/list`,
      method: 'get',
      params
    })
  },
  donation: {
    submit: (data) => request({
      url: `/donation/submit`,
      method: 'post',
      data
    }),
    myList: () => request({
      url: `/donation/myList`,
      method: 'get'
    }),
    count: () => request({
      url: `/donation/count`,
      method: 'get'
    })
  },
  upload: {
    bookCover: (file) => {
      const formData = new FormData()
      formData.append('file', file)
      return service.post('/upload/bookCover', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      // 注意：响应拦截器已经解包返回 res.data（即 { url, filename, originalName }）
      // 此处无需再 .then(res => res.data)，否则会二次解包得到 undefined
    }
  },
  member: {
    getInfo: () => request({
      url: `/member/info`,
      method: 'get'
    })
  },
  user: {
    getById: (id) => request({
      url: `/sysUser/${id}`,
      method: 'get'
    }),
    update: (data) => request({
      url: `/sysUser`,
      method: 'put',
      data
    })
  },
  review: {
    add: (data) => request({
      url: `/book-review`,
      method: 'post',
      data
    }),
    getByBookId: (bookId) => request({
      url: `/book-review/book/${bookId}`,
      method: 'get'
    }),
    getCount: (params) => request({
      url: `/book-review/count`,
      method: 'get',
      params
    }),
    checkExists: (orderItemId) => request({
      url: `/book-review/check`,
      method: 'get',
      params: { orderItemId }
    })
  },
  charity: {
    list: (params) => request({
      url: `/book/charity`,
      method: 'get',
      params
    })
  },
  customer: {
    getInfo: () => request({
      url: `/customer/auth/info`,
      method: 'get'
    }),
    updateProfile: (data) => request({
      url: `/customer`,
      method: 'put',
      data
    })
  },
  captcha: {
    getImage: () => request({
      url: `/captcha/image`,
      method: 'get'
    })
  }
}
