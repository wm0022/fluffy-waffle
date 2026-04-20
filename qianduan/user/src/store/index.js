import Vue from 'vue'
import Vuex from 'vuex'
import Cookies from 'js-cookie'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: Cookies.get('token') || '',
    userInfo: JSON.parse(Cookies.get('userInfo') || '{}'),
    sidebarOpened: Cookies.get('sidebarOpened') !== 'false'
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      Cookies.set('token', token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      Cookies.set('userInfo', JSON.stringify(userInfo))
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
      Cookies.remove('token')
      Cookies.remove('userInfo')
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
