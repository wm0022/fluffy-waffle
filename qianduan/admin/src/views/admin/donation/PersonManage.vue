<template>
  <div class="donation-person-manage">
    <el-card>
      <div slot="header" class="card-header">
        <h3>爱心赠书人士管理</h3>
        <el-button type="primary" @click="showAddDialog">添加赠书人士</el-button>
      </div>

      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="待审核" name="0"></el-tab-pane>
        <el-tab-pane label="已通过" name="1"></el-tab-pane>
        <el-tab-pane label="全部" name="all"></el-tab-pane>
      </el-tabs>

      <el-table :data="personList" style="width: 100%">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column label="性别" width="80">
          <template slot-scope="scope">
            {{ scope.row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="ethnicity" label="民族" width="100" />
        <el-table-column prop="hometown" label="籍贯" width="120" />
        <el-table-column prop="birthDate" label="出生日期" width="120" />
        <el-table-column prop="education" label="学历" width="100" />
        <el-table-column prop="address" label="住址" width="180" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="donationCount" label="赠书数量" width="100" />
        <el-table-column label="赠书金额" width="100">
          <template slot-scope="scope">
            ¥{{ scope.row.donationAmount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="80" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button 
              v-if="scope.row.status === 0"
              type="text" 
              size="small"
              @click="reviewPerson(scope.row, 1)"
            >
              通过
            </el-button>
            <el-button 
              v-if="scope.row.status === 0"
              type="text" 
              size="small"
              @click="reviewPerson(scope.row, 2)"
            >
              拒绝
            </el-button>
            <el-button type="text" size="small" @click="deletePerson(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="empty-tip" v-if="personList.length === 0">
        <el-empty description="暂无记录"></el-empty>
      </div>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="personForm" label-width="100px">
        <el-form-item label="身份证号">
          <el-input v-model="personForm.idCard" placeholder="请输入身份证号"></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="personForm.realName" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="personForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="民族">
          <el-input v-model="personForm.ethnicity" placeholder="请输入民族"></el-input>
        </el-form-item>
        <el-form-item label="籍贯">
          <el-input v-model="personForm.hometown" placeholder="请输入籍贯"></el-input>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker
            v-model="personForm.birthDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="学历">
          <el-select v-model="personForm.education" placeholder="请选择学历" style="width: 100%">
            <el-option label="小学" value="小学"></el-option>
            <el-option label="初中" value="初中"></el-option>
            <el-option label="高中" value="高中"></el-option>
            <el-option label="大专" value="大专"></el-option>
            <el-option label="本科" value="本科"></el-option>
            <el-option label="硕士" value="硕士"></el-option>
            <el-option label="博士" value="博士"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="住址">
          <el-input v-model="personForm.address" placeholder="请输入住址"></el-input>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="personForm.phone" placeholder="请输入电话"></el-input>
        </el-form-item>
        <el-form-item label="赠书数量">
          <el-input-number v-model="personForm.donationCount" :min="0" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="赠书金额">
          <el-input-number v-model="personForm.donationAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="personForm.points" :min="0" style="width: 100%"></el-input-number>
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
      dialogVisible: false,
      dialogTitle: '添加赠书人士',
      personForm: {
        id: null,
        userId: null,
        idCard: '',
        realName: '',
        gender: 1,
        ethnicity: '',
        hometown: '',
        birthDate: '',
        education: '',
        address: '',
        phone: '',
        donationCount: 0,
        donationAmount: 0,
        points: 0,
        status: 1
      }
    }
  },
  created() {
    this.loadPersons()
  },
  methods: {
    async loadPersons() {
      try {
        const status = this.activeTab === 'all' ? null : parseInt(this.activeTab)
        const res = await api.donation.personList(status)
        this.personList = res || []
      } catch (error) {
        console.error('加载赠书人士失败:', error)
        this.$message.error('加载赠书人士失败')
      }
    },
    handleTabChange() {
      this.loadPersons()
    },
    showAddDialog() {
      this.dialogTitle = '添加赠书人士'
      this.personForm = {
        id: null,
        userId: null,
        idCard: '',
        realName: '',
        gender: 1,
        ethnicity: '',
        hometown: '',
        birthDate: '',
        education: '',
        address: '',
        phone: '',
        donationCount: 0,
        donationAmount: 0,
        points: 0,
        status: 1
      }
      this.dialogVisible = true
    },
    showEditDialog(row) {
      this.dialogTitle = '编辑赠书人士'
      this.personForm = { ...row }
      this.dialogVisible = true
    },
    async savePerson() {
      try {
        await api.donation.savePerson(this.personForm)
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.loadPersons()
      } catch (error) {
        console.error('保存失败:', error)
        this.$message.error('保存失败')
      }
    },
    async reviewPerson(row, status) {
      try {
        await api.donation.reviewPerson(row.id, status)
        this.$message.success(status === 1 ? '审核通过' : '已拒绝')
        this.loadPersons()
      } catch (error) {
        console.error('审核失败:', error)
        this.$message.error('审核失败')
      }
    },
    async deletePerson(row) {
      this.$confirm(`确定要删除赠书人士 ${row.realName} 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await api.donation.deletePerson(row.id)
          this.$message.success('删除成功')
          this.loadPersons()
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    getStatusType(status) {
      const types = { 0: 'warning', 1: 'success', 2: 'danger' }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
      return texts[status] || '未知'
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

  .empty-tip {
    padding: 40px;
    text-align: center;
  }
}
</style>
