<template>
  <div class="admin-layout">
    <el-container>
      <el-aside :width="sidebarOpened ? '200px' : '64px'" class="sidebar">
        <div class="logo">
          <span v-if="sidebarOpened" class="logo-text">圣惟书店</span>
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
          <el-menu-item index="/admin/home">
            <i class="el-icon-house"></i>
            <span slot="title">首页</span>
          </el-menu-item>
          <el-menu-item index="/admin/book">
            <i class="el-icon-reading"></i>
            <span slot="title">图书管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/inventory">
            <i class="el-icon-office-building"></i>
            <span slot="title">库存管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/order">
            <i class="el-icon-shopping-cart-2"></i>
            <span slot="title">订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/member">
            <i class="el-icon-user"></i>
            <span slot="title">会员管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/donor">
            <i class="el-icon-s-promotion"></i>
            <span slot="title">捐赠人士</span>
          </el-menu-item>
          <el-menu-item index="/admin/donation-apply">
            <i class="el-icon-document"></i>
            <span slot="title">捐赠申请</span>
          </el-menu-item>
          <el-menu-item index="/admin/donation-accept">
            <i class="el-icon-clipboard"></i>
            <span slot="title">捐赠验收</span>
          </el-menu-item>
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

export default {
  name: 'AdminLayout',
  computed: {
    ...mapGetters(['userInfo']),
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
