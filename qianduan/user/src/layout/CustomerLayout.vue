<template>
  <div class="customer-layout">
    <el-header class="site-header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/customer/home')">
          📚 圣惟书店
        </div>
        <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索图书、作者、出版社"
        prefix-icon="el-icon-search"
        @keyup.enter.native="handleSearch"
        class="search-input"
      >
        <el-button slot="append" type="primary" @click="handleSearch">搜索</el-button>
      </el-input>
    </div>
        <div class="nav-menu">
          <el-menu mode="horizontal" :router="true" :default-active="$route.path" background-color="#fff" text-color="#333" active-text-color="#409EFF">
            <el-menu-item index="/customer/home"><i class="el-icon-s-home"></i> 首页</el-menu-item>
            <el-menu-item index="/customer/cart">
              <i class="el-icon-shopping-cart-2"></i>
              购物车
              <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge" />
            </el-menu-item>
            <el-menu-item index="/customer/donation"><i class="el-icon-present"></i> 图书捐赠</el-menu-item>
          </el-menu>
        </div>
        <div class="user-info">
          <el-dropdown v-if="isLoggedIn" @command="handleUserCommand">
            <span class="user-name">
              <i class="el-icon-user"></i>
              {{ userInfo.username }}
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="center">个人中心</el-dropdown-item>
              <el-dropdown-item command="orders">我的订单</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-button v-else type="primary" @click="$router.push('/login')">登录</el-button>
        </div>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </div>
</template>

<script>
export default {
  name: 'CustomerLayout',
  data() {
    return {
      searchKeyword: '',
      cartCount: 0
    }
  },
  computed: {
    isLoggedIn() {
      return !!this.$store.state.token
    },
    userInfo() {
      return this.$store.state.userInfo || {}
    }
  },
  created() {
    this.loadCartCount()
  },
  methods: {
    loadCartCount() {
      // TODO: 加载购物车数量
      this.cartCount = 0
    },
    handleSearch() {
      const keyword = this.searchKeyword.trim()
      // 避免重复导航到相同路径
      if (this.$route.path === '/customer/home') {
        // 已经在首页，使用replace更新query参数
        this.$router.replace({ 
          path: '/customer/home', 
          query: keyword ? { keyword, tab: 'all' } : { tab: 'all' } 
        }).catch(err => {
          // 忽略NavigationDuplicated错误
          if (err.name !== 'NavigationDuplicated') {
            throw err
          }
        })
      } else {
        this.$router.push({ 
          path: '/customer/home', 
          query: keyword ? { keyword, tab: 'all' } : { tab: 'all' } 
        })
      }
    },
    handleUserCommand(command) {
      if (command === 'logout') {
        this.$store.dispatch('logout')
        this.$message.success('已退出登录')
        this.$router.push('/login')
      } else if (command === 'center') {
        if (this.$route.path !== '/customer/member') {
          this.$router.push('/customer/member').catch(() => {})
        }
      } else if (command === 'orders') {
        if (this.$route.path !== '/customer/order') {
          this.$router.push('/customer/order').catch(() => {})
        }
      } else if (command === 'password') {
        if (this.$route.path !== '/customer/password') {
          this.$router.push('/customer/password').catch(() => {})
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.customer-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.site-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  
  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 60px;
    padding: 0 20px;
    
    .logo {
      font-size: 24px;
      font-weight: bold;
      color: #409EFF;
      cursor: pointer;
      
      &:hover {
        opacity: 0.8;
      }
    }
    
    .search-bar {
      flex: 1;
      max-width: 500px;
      margin: 0 20px;
      
      ::v-deep .el-input {
        .el-input__inner {
          border-radius: 20px 0 0 20px;
        }
        
        .el-input__group {
          border-radius: 20px;
        }
        
        .el-button {
          border-radius: 0 20px 20px 0;
          border-left: none;
        }
      }
    }
    
    .nav-menu {
      flex: 1;
      display: flex;
      justify-content: center;
      
      ::v-deep .el-menu {
        border-bottom: none;
        
        .cart-badge {
          margin-left: 5px;
        }
      }
    }
    
    .user-info {
      .user-name {
        cursor: pointer;
        color: #606266;
        
        &:hover {
          color: #409EFF;
        }
      }
    }
  }
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
