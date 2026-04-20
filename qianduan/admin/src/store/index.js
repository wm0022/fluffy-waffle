import Vue from 'vue'
import Vuex from 'vuex'
import Cookies from 'js-cookie'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: Cookies.get('token') || '',
    userInfo: JSON.parse(Cookies.get('userInfo') || '{}'),
    permissions: JSON.parse(Cookies.get('permissions') || '[]'),
    sidebarOpened: Cookies.get('sidebarOpened') !== 'false'
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      Cookies.set('token', token)
      localStorage.setItem('token', token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      Cookies.set('userInfo', JSON.stringify(userInfo))
    },
    SET_PERMISSIONS(state, permissions) {
      state.permissions = permissions || []
      Cookies.set('permissions', JSON.stringify(state.permissions))
    },
    TOGGLE_SIDEBAR(state) {
      state.sidebarOpened = !state.sidebarOpened
      Cookies.set('sidebarOpened', state.sidebarOpened)
    },
    CLOSE_SIDEBAR(state) {
      state.sidebarOpened = false
      Cookies.set('sidebarOpened', false)
    },
    LOGOUT(state) {
      state.token = ''
      state.userInfo = {}
      state.permissions = []
      Cookies.remove('token')
      Cookies.remove('userInfo')
      Cookies.remove('permissions')
      localStorage.removeItem('token')
    }
  },
  actions: {
    login({ commit }, { token, userInfo, permissions }) {
      commit('SET_TOKEN', token)
      commit('SET_USER_INFO', userInfo)
      commit('SET_PERMISSIONS', permissions)
    },
    logout({ commit }) {
      commit('LOGOUT')
    }
  },
  getters: {
    isLogin: state => !!state.token,
    userInfo: state => state.userInfo,
    permissions: state => state.permissions,
    hasPermission: state => (permission) => (state.permissions || []).includes(permission)
  }
})
