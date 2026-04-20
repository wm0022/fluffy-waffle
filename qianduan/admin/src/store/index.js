import Vue from 'vue'
import Vuex from 'vuex'
import Cookies from 'js-cookie'

Vue.use(Vuex)

// 管理端使用独立前缀的 Cookie Key，避免与客户端(8082)跨端口覆盖冲突
const TOKEN_KEY = 'admin_token'
const USER_INFO_KEY = 'admin_userInfo'
const SIDEBAR_KEY = 'admin_sidebarOpened'
const MENUS_KEY = 'admin_menus'
const PERMS_KEY = 'admin_perms'

export default new Vuex.Store({
  state: {
    token: Cookies.get(TOKEN_KEY) || '',
    userInfo: JSON.parse(Cookies.get(USER_INFO_KEY) || '{}'),
    sidebarOpened: Cookies.get(SIDEBAR_KEY) !== 'false',
    // 权限菜单列表（从后端 /menu/my-menus 获取，RBAC 过滤）
    menus: JSON.parse(Cookies.get(MENUS_KEY) || '[]'),
    // 权限标识集合（从后端 /menu/my-perms 获取，用于按钮级控制）
    perms: JSON.parse(Cookies.get(PERMS_KEY) || '[]')
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      Cookies.set(TOKEN_KEY, token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      Cookies.set(USER_INFO_KEY, JSON.stringify(userInfo))
    },
    SET_MENUS(state, menus) {
      state.menus = menus
      Cookies.set(MENUS_KEY, JSON.stringify(menus))
    },
    SET_PERMS(state, perms) {
      state.perms = perms
      Cookies.set(PERMS_KEY, JSON.stringify(perms))
    },
    TOGGLE_SIDEBAR(state) {
      state.sidebarOpened = !state.sidebarOpened
      Cookies.set(SIDEBAR_KEY, state.sidebarOpened)
    },
    CLOSE_SIDEBAR(state) {
      state.sidebarOpened = false
      Cookies.set(SIDEBAR_KEY, false)
    },
    LOGOUT(state) {
      state.token = ''
      state.userInfo = {}
      state.menus = []
      state.perms = []
      Cookies.remove(TOKEN_KEY)
      Cookies.remove(USER_INFO_KEY)
      Cookies.remove(MENUS_KEY)
      Cookies.remove(PERMS_KEY)
    }
  },
  actions: {
    login({ commit }, { token, userInfo }) {
      commit('SET_TOKEN', token)
      commit('SET_USER_INFO', userInfo)
    },
    logout({ commit }) {
      commit('LOGOUT')
    },
    setMenus({ commit }, menus) {
      commit('SET_MENUS', menus)
    },
    setPerms({ commit }, perms) {
      commit('SET_PERMS', perms)
    }
  },
  getters: {
    isLogin: state => !!state.token,
    userInfo: state => state.userInfo,
    menus: state => state.menus,
    /**
     * 判断当前用户是否为管理员（userType=1）
     */
    isAdmin: state => state.userInfo.userType === 1,
    /**
     * 获取当前用户所有权限标识（从后端 /menu/my-perms 获取）
     * 包含菜单级 + 按钮级的全部 perms
     */
    permissions: state => state.perms || [],
    /**
     * 检查是否有指定权限（用于 v-if 按钮控制）
     * 用法: $store.getters.hasPermission('book:delete')
     */
    hasPermission: (state, getters) => (perm) => {
      return (getters.permissions || []).includes(perm)
    }
  }
})
