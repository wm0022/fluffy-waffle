<template>
  <div class="password-container">
    <el-card style="max-width: 500px; margin: 50px auto;">
      <div slot="header">
        <span>修改密码</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="$router.back()">返回</el-button>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'AdminChangePassword',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.form.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      loading: false,
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    userId() {
      return this.$store.getters.userInfo?.id || 1
    }
  },
  methods: {
    async handleSubmit() {
      await this.$refs.form.validate()
      this.loading = true
      try {
        await api.sysUser.changePassword({
          userId: this.userId,
          oldPassword: this.form.oldPassword,
          newPassword: this.form.newPassword
        })
        this.$message.success('密码修改成功，请重新登录')
        this.$store.dispatch('logout')
        this.$router.push('/login')
      } catch (error) {
        this.$message.error('修改密码失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.password-container {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
