<template>
  <div class="member-list">
    <el-card>
      <div slot="header" style="display: flex; justify-content: space-between; align-items: center;">
        <span>用户管理</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">创建用户</el-button>
      </div>
      
      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="角色类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getUserTypeTag(scope.row.userType)" size="small">
              {{ getUserTypeName(scope.row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="会员等级" width="110">
          <template slot-scope="scope">
            <el-tag :type="getLevelTagType(scope.row.memberLevel)" size="small">
              {{ getLevelName(scope.row.memberLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="积分" width="80">
          <template slot-scope="scope">
            {{ scope.row.points || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="累计消费" width="110">
          <template slot-scope="scope">
            ¥{{ scope.row.totalAmount || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" fixed="right" width="150">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
        style="margin-top: 20px; text-align: center;"
      />
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form ref="userForm" :model="userForm" :rules="formRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="!isCreate" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="isCreate" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色类型" prop="userType">
          <el-select v-model="userForm.userType" placeholder="请选择角色类型" style="width: 100%;">
            <el-option label="店员" :value="1" />
            <el-option label="普通用户" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item v-if="!isCreate" label="会员等级">
          <el-tag :type="getLevelTagType(userForm.memberLevel)">
            {{ getLevelName(userForm.memberLevel) }}
          </el-tag>
        </el-form-item>
        <el-form-item v-if="!isCreate" label="积分">
          <span>{{ userForm.points || 0 }}</span>
        </el-form-item>
        <el-form-item v-if="!isCreate" label="累计消费">
          <span>¥{{ userForm.totalAmount || '0.00' }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSave">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'MemberList',
  data() {
    return {
      loading: false,
      submitLoading: false,
      userList: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      dialogTitle: '编辑用户',
      isCreate: false,
      userForm: {
        id: null,
        username: '',
        password: '',
        userType: 2,
        email: '',
        phone: '',
        memberLevel: 0,
        points: 0,
        totalAmount: '0.00'
      },
      formRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        userType: [{ required: true, message: '请选择角色类型', trigger: 'change' }]
      },
      levelNames: ['非会员', '普通会员', '银卡会员', '金卡会员', '钻石卡会员']
    }
  },
  created() {
    this.getUserList()
  },
  methods: {
    async getUserList() {
      this.loading = true
      try {
        const res = await api.sysUser.pageList({
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })
        this.userList = res.records || []
        this.total = res.total || 0
      } catch (error) {
        console.error('获取用户列表失败:', error)
        this.$message.error('获取用户列表失败')
      } finally {
        this.loading = false
      }
    },
    handlePageChange(page) {
      this.pageNum = page
      this.getUserList()
    },
    handleAdd() {
      this.isCreate = true
      this.dialogTitle = '创建用户'
      this.userForm = {
        id: null,
        username: '',
        password: '',
        userType: 2,
        email: '',
        phone: '',
        memberLevel: 0,
        points: 0,
        totalAmount: '0.00'
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.userForm) this.$refs.userForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.isCreate = false
      this.dialogTitle = '编辑用户'
      this.userForm = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.userForm) this.$refs.userForm.clearValidate()
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await api.sysUser.delete(row.id)
          this.$message.success('删除成功')
          this.getUserList()
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    async handleSave() {
      this.$refs.userForm.validate(async (valid) => {
        if (!valid) return
        this.submitLoading = true
        try {
          if (this.isCreate) {
            await api.sysUser.create(this.userForm)
            this.$message.success('创建成功')
          } else {
            await api.sysUser.update(this.userForm)
            this.$message.success('保存成功')
          }
          this.dialogVisible = false
          this.getUserList()
        } catch (error) {
          console.error('保存失败:', error)
          this.$message.error(this.isCreate ? '创建失败: ' + (error.message || '未知错误') : '保存失败')
        } finally {
          this.submitLoading = false
        }
      })
    },
    getUserTypeName(type) {
      if (type === 1) return '店员'
      if (type === 2) return '普通用户'
      return '未知'
    },
    getUserTypeTag(type) {
      if (type === 1) return 'warning'
      if (type === 2) return ''
      return 'info'
    },
    getLevelName(level) {
      return this.levelNames[level] || '非会员'
    },
    getLevelTagType(level) {
      const types = ['info', '', 'warning', 'danger', 'success']
      return types[level] || 'info'
    }
  }
}
</script>

<style scoped>
.member-list {
  padding: 20px;
}
</style>
