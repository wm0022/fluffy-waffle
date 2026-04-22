<template>
  <div class="donor-manage">
    <el-card>
      <div slot="header">
        <span>爱心赠书人士管理</span>
        <el-button type="primary" size="small" style="float: right;" icon="el-icon-plus" @click="openDialog(null)">新增赠书人士</el-button>
      </div>

      <!-- 搜索栏：扩展多条件搜索 -->
      <div class="search-bar">
        <el-input v-model="searchForm.realName" placeholder="姓名搜索" size="small" style="width: 140px;" clearable />
        <el-input v-model="searchForm.phone" placeholder="电话搜索" size="small" style="width: 150px;" clearable />
        <el-select v-model="searchForm.gender" placeholder="性别" size="small" style="width: 100px;" clearable>
          <el-option label="男" :value="1" />
          <el-option label="女" :value="2" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="状态" size="small" style="width: 100px;" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button size="small" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
      </div>

      <!-- 表格：添加 v-loading + 状态列 + 创建时间列 -->
      <el-table :data="donors" border stripe size="small" max-height="600" v-loading="loading">
        <el-table-column prop="donorNo" label="编号" width="100" align="center" />
        <el-table-column prop="realName" label="姓名" width="80" align="center" />
        <el-table-column prop="idCard" label="身份证号" width="180" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="50" align="center">
          <template slot-scope="scope">{{ scope.row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="120" align="center" />
        <el-table-column prop="nation" label="民族" width="60" align="center" />
        <el-table-column prop="nativePlace" label="籍贯" width="80" align="center" show-overflow-tooltip />
        <el-table-column prop="education" label="学历" width="70" align="center" />
        <el-table-column prop="address" label="住址" min-width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="70" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="mini">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalBooks" label="赠书数量" width="80" align="right" />
        <el-table-column prop="totalAmount" label="赠书金额" width="90" align="right">
          <template slot-scope="scope">{{ scope.row.totalAmount || '0.00' }}</template>
        </el-table-column>
        <el-table-column prop="totalScore" label="积分" width="60" align="right" />
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog :title="editDonor ? '编辑赠书人士' : '新增赠书人士'" :visible.sync="dialogVisible" width="700px" :before-close="handleCloseDialog">
      <el-form ref="donorForm" :model="donorForm" label-width="100px" size="small" :rules="formRules">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="donorForm.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="donorForm.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="donorForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="donorForm.nation" placeholder="请输入民族" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="donorForm.birthDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="donorForm.nativePlace" placeholder="请输入籍贯" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学历">
              <el-select v-model="donorForm.education" style="width: 100%;" clearable placeholder="请选择学历">
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
            <el-form-item label="电话" prop="phone">
              <el-input v-model="donorForm.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="donorForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number v-model="donorForm.age" :min="0" :max="150" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="住址">
          <el-input v-model="donorForm.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="donorForm.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12"></el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="donorForm.remark" type="textarea" :rows="2" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDonor">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'DonorManage',
  data() {
    return {
      donors: [],
      loading: false,
      // 搜索表单（多条件）
      searchForm: { realName: '', phone: '', gender: undefined, status: undefined },
      // 分页
      pageNum: 1,
      pageSize: 10,
      total: 0,
      // 弹窗
      dialogVisible: false,
      editDonor: null,
      donorForm: {
        realName: '', idCard: '', gender: 1, nation: '', nativePlace: '',
        birthDate: '', age: null, education: '', phone: '', email: '',
        address: '', remark: '', status: 1
      },
      // 表单校验规则
      formRules: {
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        idCard: [
          { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证号格式不正确', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadDonors()
  },
  methods: {
    async loadDonors() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
        if (this.searchForm.realName) params.realName = this.searchForm.realName
        if (this.searchForm.phone) params.phone = this.searchForm.phone
        if (this.searchForm.gender !== undefined) params.gender = this.searchForm.gender
        if (this.searchForm.status !== undefined) params.status = this.searchForm.status

        const res = await api.donor.pageList(params)
        this.donors = res.records || []
        this.total = res.total || 0
      } catch (e) {
        console.error('加载赠书人士列表失败:', e)
        this.$message.error('加载赠书人士列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.loadDonors()
    },
    resetSearch() {
      this.searchForm = { realName: '', phone: '', gender: undefined, status: undefined }
      this.pageNum = 1
      this.loadDonors()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.loadDonors()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.pageNum = 1
      this.loadDonors()
    },
    openDialog(donor) {
      this.editDonor = donor
      if (donor) {
        this.donorForm = { ...donor }
      } else {
        this.donorForm = {
          realName: '', idCard: '', gender: 1, nation: '', nativePlace: '',
          birthDate: '', age: null, education: '', phone: '', email: '',
          address: '', remark: '', status: 1
        }
      }
      this.dialogVisible = true
    },
    handleCloseDialog(done) {
      done()
    },
    submitDonor() {
      this.$refs.donorForm.validate(async (valid) => {
        if (!valid) return
        try {
          // 提交前移除统计字段，防止编辑操作覆盖累加数据
          const submitData = { ...this.donorForm }
          delete submitData.totalBooks
          delete submitData.totalAmount
          delete submitData.totalScore
          delete submitData.createTime
          delete submitData.updateTime

          if (this.editDonor) {
            await api.donor.update(submitData)
          } else {
            await api.donor.save(submitData)
          }
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadDonors()
        } catch (e) {
          const msg = e.response?.data?.message || e.message || '保存失败'
          this.$message.error(msg)
        }
      })
    },
    async handleDelete(donor) {
      try {
        await this.$confirm(`确定删除该赠书人士【${donor.realName}】？删除后不可恢复。`, '提示', { type: 'warning' })
        await api.donor.delete(donor.donorId)
        this.$message.success('删除成功')
        this.loadDonors()
      } catch (e) {
        // 取消删除不做处理
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.donor-manage {
  .search-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 15px;
    align-items: center;
  }

  .pagination {
    margin-top: 16px;
    text-align: center;
  }
}
</style>
