<template>
  <div class="donation-person-manage">
    <el-card>
      <div slot="header" class="card-header">
        <h3>爱心赠书人士管理</h3>
        <div style="display: flex; gap: 10px; align-items: center;">
          <el-input v-model="searchName" placeholder="姓名搜索" size="small" style="width: 150px;" clearable />
          <el-button type="primary" size="small" icon="el-icon-search" @click="loadPersons">搜索</el-button>
          <el-button type="primary" @click="showAddDialog">添加赠书人士</el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="启用" name="1"></el-tab-pane>
        <el-tab-pane label="停用" name="0"></el-tab-pane>
      </el-tabs>

      <el-table :data="personList" border stripe size="small" v-loading="loading" max-height="600">
        <el-table-column prop="donorNo" label="编号" width="100" align="center" />
        <el-table-column prop="realName" label="姓名" width="80" align="center" />
        <el-table-column prop="idCard" label="身份证号" width="180" show-overflow-tooltip />
        <el-table-column label="性别" width="50" align="center">
          <template slot-scope="scope">
            {{ scope.row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="nation" label="民族" width="60" align="center" />
        <el-table-column prop="nativePlace" label="籍贯" width="80" align="center" show-overflow-tooltip />
        <el-table-column prop="education" label="学历" width="70" align="center" />
        <el-table-column prop="phone" label="电话" width="120" align="center" />
        <el-table-column prop="address" label="住址" min-width="140" show-overflow-tooltip />
        <el-table-column prop="totalBooks" label="赠书数量" width="90" align="right" />
        <el-table-column label="赠书金额" width="100" align="right">
          <template slot-scope="scope">¥{{ scope.row.totalAmount || '0.00' }}</template>
        </el-table-column>
        <el-table-column prop="totalScore" label="积分" width="70" align="right" />
        <el-table-column label="状态" width="70" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="mini">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="success"
              size="mini"
              @click="reviewPerson(scope.row, 1)"
            >启用</el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="warning"
              size="mini"
              @click="reviewPerson(scope.row, 0)"
            >停用</el-button>
            <el-button type="danger" size="mini" @click="deletePerson(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框（使用 DonorInfo 字段） -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="650px">
      <el-form ref="personFormRef" :model="personForm" label-width="100px" size="small" :rules="formRules">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="personForm.realName" placeholder="请输入姓名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="personForm.idCard" placeholder="请输入身份证号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="personForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="personForm.phone" placeholder="请输入手机号" maxlength="11"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="personForm.nation" placeholder="请输入民族"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="personForm.nativePlace" placeholder="请输入籍贯"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="personForm.birthDate" type="date" value-format="yyyy-MM-dd" style="width: 100%;" placeholder="选择日期"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历">
              <el-select v-model="personForm.education" placeholder="请选择学历" style="width: 100%;">
                <el-option label="小学" value="小学"></el-option>
                <el-option label="初中" value="初中"></el-option>
                <el-option label="高中" value="高中"></el-option>
                <el-option label="大专" value="大专"></el-option>
                <el-option label="本科" value="本科"></el-option>
                <el-option label="硕士" value="硕士"></el-option>
                <el-option label="博士" value="博士"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="住址">
          <el-input v-model="personForm.address" placeholder="请输入住址"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="赠书数量">
              <el-input-number v-model="personForm.totalBooks" :min="0" style="width: 100%;"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="赠书金额">
              <el-input-number v-model="personForm.totalAmount" :min="0" :precision="2" style="width: 100%;"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="积分">
              <el-input-number v-model="personForm.totalScore" :min="0" style="width: 100%;"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="personForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="personForm.remark" type="textarea" :rows="2" placeholder="备注信息"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePerson">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'DonationPersonManage',
  data() {
    return {
      activeTab: 'all',
      personList: [],
      loading: false,
      searchName: '',
      pageNum: 1,
      pageSize: 15,
      total: 0,
      dialogVisible: false,
      dialogTitle: '添加赠书人士',
      editRow: null,
      // 使用 DonorInfo 字段命名规范
      personForm: {
        donorId: null,
        userId: null,
        idCard: '',
        realName: '',
        gender: 1,
        nation: '',
        nativePlace: '',
        birthDate: '',
        education: '',
        address: '',
        phone: '',
        totalBooks: 0,
        totalAmount: 0,
        totalScore: 0,
        status: 1,
        remark: ''
      },
      formRules: {
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadPersons()
  },
  methods: {
    async loadPersons() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
        if (this.searchName) params.realName = this.searchName

        // 状态过滤
        if (this.activeTab !== 'all') {
          params.status = parseInt(this.activeTab)
        }

        // 统一调用 api.donor.pageList → GET /donor/page → 查 donor_info 表
        const res = await api.donor.pageList(params)
        this.personList = res.records || []
        this.total = res.total || 0
      } catch (error) {
        console.error('加载赠书人士失败:', error)
        this.$message.error('加载赠书人士失败')
      } finally {
        this.loading = false
      }
    },
    handleTabChange() {
      this.pageNum = 1
      this.loadPersons()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.loadPersons()
    },
    showAddDialog() {
      this.dialogTitle = '添加赠书人士'
      this.editRow = null
      this.personForm = {
        donorId: null,
        userId: null,
        idCard: '',
        realName: '',
        gender: 1,
        nation: '',
        nativePlace: '',
        birthDate: '',
        education: '',
        address: '',
        phone: '',
        totalBooks: 0,
        totalAmount: 0,
        totalScore: 0,
        status: 1,
        remark: ''
      }
      this.dialogVisible = true
    },
    showEditDialog(row) {
      this.dialogTitle = '编辑赠书人士'
      this.editRow = row
      this.personForm = { ...row }
      this.dialogVisible = true
    },
    async savePerson() {
      this.$refs.personFormRef.validate(async (valid) => {
        if (!valid) return
        try {
          if (this.personForm.donorId) {
            // 更新
            await api.donor.update(this.personForm)
          } else {
            // 新增
            await api.donor.save(this.personForm)
          }
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadPersons()
        } catch (error) {
          const msg = error.response?.data?.message || error.message || '保存失败'
          this.$message.error(msg)
        }
      })
    },
    async reviewPerson(row, status) {
      try {
        await api.donation.reviewPerson(row.donorId, status)
        this.$message.success(status === 1 ? '已启用' : '已停用')
        this.loadPersons()
      } catch (error) {
        console.error('操作失败:', error)
        this.$message.error('操作失败')
      }
    },
    async deletePerson(row) {
      try {
        await this.$confirm(`确定要删除赠书人士【${row.realName}】吗？删除后不可恢复。`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await api.donor.delete(row.donorId)
        this.$message.success('删除成功')
        this.loadPersons()
      } catch (error) {
        // 取消不做处理
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.donation-person-manage {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
    }
  }

  .pagination {
    margin-top: 16px;
    text-align: center;
  }
}
</style>
