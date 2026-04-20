import Vue from 'vue'
import Vuex from 'vuex'
import Cookies from 'js-cookie'

Vue.use(Vuex)

// 客户端使用独立前缀的 Cookie Key，避免与管理端(8081)跨端口覆盖冲突
const TOKEN_KEY = 'user_token'
const USER_INFO_KEY = 'user_userInfo'
const SIDEBAR_KEY = 'user_sidebarOpened'

export default new Vuex.Store({
  state: {
    token: Cookies.get(TOKEN_KEY) || '',
    userInfo: JSON.parse(Cookies.get(USER_INFO_KEY) || '{}'),
    sidebarOpened: Cookies.get(SIDEBAR_KEY) !== 'false'
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
      Cookies.remove(TOKEN_KEY)
      Cookies.remove(USER_INFO_KEY)
    }
  },
  actions: {
    login({ commit }, { token, userInfo }) {
      commit('SET_TOKEN', token)
      commit('SET_USER_INFO', userInfo)
    },
    logout({ commit }) {
      commit('LOGOUT')
    }
  },
  getters: {
    isLogin: state => !!state.token,
    userInfo: state => state.userInfo
  }
})
