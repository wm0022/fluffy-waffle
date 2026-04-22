<template>
  <div class="customer-list">
    <el-card>
      <div slot="header" style="display: flex; justify-content: space-between; align-items: center;">
        <span>顾客管理</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新增顾客</el-button>
      </div>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="searchForm" size="small" style="margin-bottom: 16px;">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="customerList" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column label="性别" width="70">
          <template slot-scope="scope">
            {{ scope.row.gender === 1 ? '男' : scope.row.gender === 2 ? '女' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="会员等级" width="110">
          <template slot-scope="scope">
            <el-tag :type="getLevelTagType(scope.row.memberLevel)" size="small">
              {{ getLevelName(scope.row.memberLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="积分" width="80" align="right">
          <template slot-scope="scope">
            {{ scope.row.points || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="累计消费" width="110" align="right">
          <template slot-scope="scope">
            ¥{{ scope.row.totalAmount || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" fixed="right" width="180">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleView(scope.row)">详情</el-button>
            <el-button size="mini" type="warning" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50]"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog title="顾客详情" :visible.sync="viewVisible" width="650px">
      <el-descriptions :column="2" border size="medium">
        <el-descriptions-item label="ID">{{ viewData.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ viewData.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ viewData.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ viewData.gender === 1 ? '男' : viewData.gender === 2 ? '女' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ viewData.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="民族">{{ viewData.nation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="籍贯">{{ viewData.nativePlace || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ viewData.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ viewData.age || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学历">{{ viewData.education || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号" :span="2">{{ viewData.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱" :span="2">{{ viewData.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="住址" :span="2">{{ viewData.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="喜好" :span="2">{{ viewData.preferences || '-' }}</el-descriptions-item>
        <el-descriptions-item label="会员等级">
          <el-tag :type="getLevelTagType(viewData.memberLevel)" size="small">
            {{ getLevelName(viewData.memberLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="积分">{{ viewData.points || 0 }}</el-descriptions-item>
        <el-descriptions-item label="累计消费">¥{{ viewData.totalAmount || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'danger'" size="small">
            {{ viewData.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ viewData.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer">
        <el-button @click="viewVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form ref="customerForm" :model="customerForm" :rules="formRules" label-width="90px" size="small">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="customerForm.username" :disabled="!isCreate" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="isCreate" label="密码" prop="password">
              <el-input v-model="customerForm.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="真实姓名">
              <el-input v-model="customerForm.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="customerForm.gender" placeholder="请选择" style="width: 100%;">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="customerForm.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="customerForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="customerForm.nation" placeholder="如：汉族" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="customerForm.nativePlace" placeholder="请输入籍贯" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="customerForm.birthDate" type="date" value-format="yyyy-MM-dd" style="width: 100%;" placeholder="选择日期" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number v-model="customerForm.age" :min="0" :max="150" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学历">
              <el-select v-model="customerForm.education" placeholder="请选择" style="width: 100%;" clearable>
                <el-option label="小学" value="小学" />
                <el-option label="初中" value="初中" />
                <el-option label="高中" value="高中" />
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="customerForm.status" style="width: 100%;">
                <el-option label="正常" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="邮箱">
          <el-input v-model="customerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="住址">
          <el-input v-model="customerForm.address" placeholder="请输入住址" />
        </el-form-item>
        <el-form-item label="喜好">
          <el-input v-model="customerForm.preferences" type="textarea" :rows="2" placeholder="阅读喜好等" />
        </el-form-item>
        <el-divider content-position="left">统计信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="会员等级">
              <el-tag v-if="!isCreate" :type="getLevelTagType(customerForm.memberLevel)">
                {{ getLevelName(customerForm.memberLevel) }}
              </el-tag>
              <span v-else>-</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="积分">
              <span>{{ customerForm.points || 0 }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="累计消费">
              <span>¥{{ customerForm.totalAmount || '0.00' }}</span>
            </el-form-item>
          </el-col>
        </el-row>
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
  name: 'CustomerList',
  data() {
    return {
      loading: false,
      submitLoading: false,
      customerList: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchForm: {
        username: '',
        realName: '',
        phone: ''
      },
      dialogVisible: false,
      dialogTitle: '编辑顾客',
      isCreate: false,
      customerForm: this.initForm(),
      formRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      // 详情
      viewVisible: false,
      viewData: {},
      levelNames: ['非会员', '普通会员', '银卡会员', '金卡会员', '钻石卡会员']
    }
  },
  created() {
    this.getCustomerList()
  },
  methods: {
    initForm() {
      return {
        id: null,
        username: '',
        password: '',
        realName: '',
        gender: null,
        idCard: '',
        nation: '',
        nativePlace: '',
        birthDate: '',
        age: null,
        education: '',
        phone: '',
        email: '',
        address: '',
        preferences: '',
        status: 1,
        memberLevel: 0,
        points: 0,
        totalAmount: '0.00'
      }
    },
    async getCustomerList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
        // 将非空搜索条件加入请求参数
        if (this.searchForm.username) params.username = this.searchForm.username.trim()
        if (this.searchForm.realName) params.realName = this.searchForm.realName.trim()
        if (this.searchForm.phone) params.phone = this.searchForm.phone.trim()

        const res = await api.customer.pageList(params)
        this.customerList = res.records || []
        this.total = res.total || 0
      } catch (error) {
        console.error('获取顾客列表失败:', error)
        this.$message.error('获取顾客列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.getCustomerList()
    },
    resetSearch() {
      this.searchForm = { username: '', realName: '', phone: '' }
      this.pageNum = 1
      this.getCustomerList()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.getCustomerList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.pageNum = 1
      this.getCustomerList()
    },
    handleView(row) {
      this.viewData = { ...row }
      this.viewVisible = true
    },
    handleAdd() {
      this.isCreate = true
      this.dialogTitle = '新增顾客'
      this.customerForm = this.initForm()
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.customerForm) this.$refs.customerForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.isCreate = false
      this.dialogTitle = '编辑顾客'
      this.customerForm = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.customerForm) this.$refs.customerForm.clearValidate()
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该顾客吗？删除后不可恢复！', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await api.customer.delete(row.id)
          this.$message.success('删除成功')
          this.getCustomerList()
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    async handleSave() {
      this.$refs.customerForm.validate(async (valid) => {
        if (!valid) return
        this.submitLoading = true
        try {
          if (this.isCreate) {
            await api.customer.create(this.customerForm)
            this.$message.success('创建成功')
          } else {
            await api.customer.update(this.customerForm)
            this.$message.success('保存成功')
          }
          this.dialogVisible = false
          this.getCustomerList()
        } catch (error) {
          console.error('保存失败:', error)
          this.$message.error(this.isCreate ? '创建失败' : '保存失败')
        } finally {
          this.submitLoading = false
        }
      })
    },
    getLevelName(level) {
      return this.levelNames[level] || '非会员'
    },
    getLevelTagType(level) {
      const types = ['', '', 'warning', 'danger', 'success']
      return types[level] || ''
    }
  }
}
</script>

<style scoped>
.customer-list {
  padding: 20px;
}
</style>
