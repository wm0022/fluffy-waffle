<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>圣惟书店管理系统</h1>
        <p class="subtitle">管理后台</p>
      </div>
      <el-form
        ref="loginForm"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
            size="medium"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            size="medium"
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="medium"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <el-link type="primary" @click="goToCustomer">顾客端入口</el-link>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      },
      rememberMe: false,
      loading: false
    }
  },
  created() {
    // 从本地存储加载记住的账号密码
    const remembered = localStorage.getItem('rememberedAdmin')
    if (remembered) {
      const userData = JSON.parse(remembered)
      this.loginForm.username = userData.username
      this.loginForm.password = userData.password
      this.rememberMe = true
    }
  },
  methods: {
    async handleLogin() {
      try {
        await this.$refs.loginForm.validate()
        this.loading = true
        
        // 迁移后 sys_user 仅存储管理员账户，登录成功即表示是管理员，无需再检查 userType
        const res = await api.auth.login(this.loginForm.username, this.loginForm.password)
        
        // 如果勾选了记住我，保存到本地存储
        if (this.rememberMe) {
          localStorage.setItem('rememberedAdmin', JSON.stringify({
            username: this.loginForm.username,
            password: this.loginForm.password
          }))
        } else {
          localStorage.removeItem('rememberedAdmin')
        }
        
        // res 已经是 { token, userInfo } 对象
        this.$store.dispatch('login', {
          token: res.token,
          userInfo: res.userInfo
        })

        // 登录成功后获取当前用户的菜单权限列表
        try {
          const menus = await api.menu.getMyMenus()
          this.$store.dispatch('setMenus', menus)
        } catch (e) {
          console.warn('获取菜单权限失败，使用默认菜单', e)
        }

        // 获取当前用户的权限标识（用于按钮级控制）
        try {
          const perms = await api.menu.getMyPerms()
          this.$store.dispatch('setPerms', perms)
        } catch (e) {
          console.warn('获取权限标识失败', e)
        }

        this.$message.success('登录成功')

        // 如果有重定向目标则跳回，否则去首页
        const redirect = this.$route.query.redirect || '/admin/home'
        this.$router.push(redirect)
      } catch (error) {
        console.error(error)
        const msg = (error.response && error.response.data && error.response.data.message) || error.message || '登录失败，请重试'
        this.$message.error(msg)
      } finally {
        this.loading = false
      }
    },
    goToCustomer() {
      window.location.href = 'http://localhost:8082'
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .login-box {
    width: 400px;
    padding: 40px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
    
    .login-header {
      text-align: center;
      margin-bottom: 30px;
      
      h1 {
        color: #303133;
        font-size: 24px;
        font-weight: 500;
      }
    }
    
    .login-form {
      .el-form-item {
        margin-bottom: 20px;
      }
    }
    
    .login-footer {
      text-align: center;
      margin-top: 20px;
    }
  }
}
</style>
