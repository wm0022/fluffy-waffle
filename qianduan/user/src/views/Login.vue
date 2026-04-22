<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">📚</div>
        <h1>圣惟书店</h1>
        <p class="subtitle">让阅读点亮生活</p>
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
        <p>还没有账号？<el-link type="primary" @click="goToRegister">立即注册</el-link></p>
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
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于 6 位', trigger: 'blur' }
        ]
      },
      rememberMe: false,
      loading: false
    }
  },
  created() {
    // 从本地存储加载记住的账号密码
    const remembered = localStorage.getItem('rememberedUser')
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
        
        const res = await api.auth.login(this.loginForm.username, this.loginForm.password)
        
        // 如果勾选了记住我，保存到本地存储
        if (this.rememberMe) {
          localStorage.setItem('rememberedUser', JSON.stringify({
            username: this.loginForm.username,
            password: this.loginForm.password
          }))
        } else {
          localStorage.removeItem('rememberedUser')
        }
        
        // res 已经是 { token, userInfo } 对象
        this.$store.dispatch('login', {
          token: res.token,
          userInfo: res.userInfo
        })
        
        this.$message.success('登录成功')
        this.$router.push('/customer/home')
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    goToRegister() {
      this.$router.push('/register')
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
  background-size: 400% 400%;
  animation: gradient 15s ease infinite;
  
  @keyframes gradient {
    0% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
    100% { background-position: 0% 50%; }
  }
  
  .login-box {
    width: 420px;
    padding: 50px 40px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
    
    .login-header {
      text-align: center;
      margin-bottom: 40px;
      
      .logo {
        font-size: 64px;
        margin-bottom: 10px;
      }
      
      h1 {
        color: #303133;
        font-size: 28px;
        font-weight: 600;
        margin: 10px 0;
      }
      
      .subtitle {
        color: #909399;
        font-size: 14px;
        margin: 0;
      }
    }
    
    .login-form {
      .el-form-item {
        margin-bottom: 24px;
      }
    }
    
    .login-footer {
      text-align: center;
      margin-top: 24px;
      
      p {
        margin: 0;
        color: #606266;
        font-size: 14px;
      }
    }
  }
}
</style>
