import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import router from '@/router'

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '/api',
  timeout: 15000
})

service.interceptors.request.use(
  config => {
    const token = store.state.token
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error(error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      Message.error(res.message || '请求失败')
      // 仅对需要登录权限的接口进行401/403跳转（排除公开接口）
      if ((res.code === 401 || res.code === 403) && !isPublicApi(response.config.url)) {
        store.dispatch('logout')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error(error)
    Message.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

/**
 * 判断是否为公开接口（无需登录即可访问）
 */
function isPublicApi(url) {
  if (!url) return false
  // 公开接口列表（与后端白名单保持一致）
  const publicApis = [
    '/book/',
    '/book/page',
    '/book/hot',
    '/book/new',
    '/book/charity',
    '/book/count',
    '/book/isbn/',
    '/donation/'
  ]
  return publicApis.some(api => url.includes(api))
}

export default service
