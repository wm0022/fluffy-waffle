<template>
  <div class="donor-manage">
    <el-card>
      <div slot="header">
        <span>爱心赠书人士管理</span>
        <el-button type="primary" size="small" style="float: right;" icon="el-icon-plus" @click="openDialog(null)">新增赠书人士</el-button>
      </div>
      <div class="search-bar">
        <el-input v-model="searchName" placeholder="姓名搜索" size="small" style="width: 150px;" clearable />
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadDonors">搜索</el-button>
      </div>
      <el-table :data="donors" border stripe size="small" max-height="600">
        <el-table-column prop="donorNo" label="编号" width="100" />
        <el-table-column prop="realName" label="姓名" width="80" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="gender" label="性别" width="50">
          <template slot-scope="scope">{{ scope.row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="nation" label="民族" width="60" />
        <el-table-column prop="nativePlace" label="籍贯" width="80" />
        <el-table-column prop="birthDate" label="出生日期" width="100" />
        <el-table-column prop="age" label="年龄" width="50" />
        <el-table-column prop="education" label="学历" width="60" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="address" label="住址" min-width="150" show-overflow-tooltip />
        <el-table-column prop="totalBooks" label="赠书数量" width="80" />
        <el-table-column prop="totalAmount" label="赠书金额" width="90" />
        <el-table-column prop="totalScore" label="积分" width="60" />
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="editDonor ? '编辑赠书人士' : '新增赠书人士'" :visible.sync="dialogVisible" width="700px">
      <el-form ref="donorForm" :model="donorForm" label-width="100px" size="small">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName" :rules="[{required:true,message:'请输入姓名',trigger:'blur'}]">
              <el-input v-model="donorForm.realName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="donorForm.idCard" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="donorForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="donorForm.nation" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="donorForm.birthDate" type="date" value-format="yyyy-MM-dd" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="donorForm.nativePlace" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学历">
              <el-select v-model="donorForm.education" style="width: 100%;" clearable>
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
            <el-form-item label="电话">
              <el-input v-model="donorForm.phone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="donorForm.email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number v-model="donorForm.age" :min="0" :max="150" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="住址">
          <el-input v-model="donorForm.address" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="donorForm.remark" type="textarea" :rows="2" />
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
      searchName: '',
      dialogVisible: false,
      editDonor: null,
      donorForm: {
        realName: '', idCard: '', gender: 1, nation: '', nativePlace: '',
        birthDate: '', age: null, education: '', phone: '', email: '',
        address: '', remark: ''
      }
    }
  },
  created() {
    this.loadDonors()
  },
  methods: {
    async loadDonors() {
      try {
        this.donors = await api.donor.list()
      } catch (e) {
        this.$message.error('加载赠书人士列表失败')
      }
    },
    openDialog(donor) {
      this.editDonor = donor
      if (donor) {
        this.donorForm = { ...donor }
      } else {
        this.donorForm = {
          realName: '', idCard: '', gender: 1, nation: '', nativePlace: '',
          birthDate: '', age: null, education: '', phone: '', email: '',
          address: '', remark: ''
        }
      }
      this.dialogVisible = true
    },
    async submitDonor() {
      this.$refs.donorForm.validate(async (valid) => {
        if (!valid) return
        try {
          if (this.editDonor) {
            await api.donor.update(this.donorForm)
          } else {
            await api.donor.save(this.donorForm)
          }
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadDonors()
        } catch (e) {
          this.$message.error('保存失败')
        }
      })
    },
    async handleDelete(donor) {
      try {
        await this.$confirm('确定删除该赠书人士？', '提示', { type: 'warning' })
        await api.donor.delete(donor.donorId)
        this.$message.success('删除成功')
        this.loadDonors()
      } catch (e) {}
    }
  }
}
</script>

<style lang="scss" scoped>
.donor-manage {
  .search-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;
  }
}
</style>
