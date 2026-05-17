<template>
  <div class="admin-layout">
    <el-container>
      <el-aside :width="sidebarOpened ? '200px' : '64px'" class="sidebar">
        <div class="logo">
          <span v-if="sidebarOpened" class="logo-text">圣惟书店管理系统</span>
          <span v-else>书</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="!sidebarOpened"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <!-- 首页始终置顶显示 -->
          <el-menu-item index="/admin/home">
            <i class="el-icon-house"></i>
            <span slot="title">首页</span>
          </el-menu-item>
          <!-- 动态菜单：仅从后端 RBAC 权限菜单列表渲染，无权限则不显示 -->
          <template v-for="menu in menus">
            <!-- 跳过后端返回中的"首页"条目，避免重复 -->
            <menu-item v-if="!isHomeMenu(menu)" :key="menu.menuId" :menu="menu" />
          </template>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <i class="el-icon-s-fold" @click="toggleSidebar"></i>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin/home' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userInfo.avatar">{{ userInfo.realName ? userInfo.realName[0] : 'U' }}</el-avatar>
                <span class="username">{{ userInfo.realName || userInfo.username }}</span>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import MenuItem from '@/components/MenuItem'

export default {
  name: 'AdminLayout',
  components: { MenuItem },
  computed: {
    ...mapGetters(['userInfo', 'menus']),
    sidebarOpened() {
      return this.$store.state.sidebarOpened
    },
    activeMenu() {
      return this.$route.path
    }
  },
  methods: {
    toggleSidebar() {
      this.$store.commit('TOGGLE_SIDEBAR')
    },
    /**
     * 判断菜单项是否为"首页"，用于过滤后端返回的重复首页条目
     */
    isHomeMenu(menu) {
      const name = (menu.menuName || '').trim()
      const path = (menu.path || '').replace(/^\/+/, '')
      return name === '首页' || path === 'admin/home' || path === 'home'
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$store.dispatch('logout')
          this.$router.push('/login')
          this.$message.success('已退出登录')
        })
      } else if (command === 'profile') {
        this.$message.info('个人中心功能开发中')
      } else if (command === 'password') {
        this.$router.push('/admin/password')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  height: 100%;
  
  .el-container {
    height: 100%;
  }
  
  .sidebar {
    background-color: #304156;
    transition: width 0.3s;
    
    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #2b3a4b;
      
      img, .logo-text {
        height: 40px;
        color: white;
        font-size: 18px;
        font-weight: bold;
      }
      
      span {
        color: #fff;
        font-size: 24px;
        font-weight: bold;
      }
    }
    
    .el-menu {
      border-right: none;
    }
  }
  
  .header {
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0,21,41,.08);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    
    .header-left {
      display: flex;
      align-items: center;
      
      i {
        font-size: 20px;
        cursor: pointer;
        margin-right: 20px;
      }
    }
    
    .header-right {
      .user-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        
        .username {
          margin-left: 10px;
        }
      }
    }
  }
  
  .main {
    background-color: #f0f2f5;
    padding: 20px;
  }
}
</style>
