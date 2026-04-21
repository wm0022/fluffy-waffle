<template>
  <div class="donation-page">
    <div class="donation-header">
      <h2>📚 爱心赠书</h2>
      <p>您的每一本捐赠图书，都将为他人带来希望</p>
    </div>

    <div class="donation-content">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="提交捐赠" name="submit">
          <el-card>
            <h3>填写捐赠信息</h3>
            <el-form :model="donationForm" :rules="rules" ref="donationForm" label-width="100px">
              <el-divider content-position="left">必填信息</el-divider>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="书名" prop="bookName">
                    <el-input v-model="donationForm.bookName" placeholder="请输入书名"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="出版社" prop="publisher">
                    <el-input v-model="donationForm.publisher" placeholder="请输入出版社"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="数量" prop="quantity">
                    <el-input-number v-model="donationForm.quantity" :min="1" :max="100"></el-input-number>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-divider content-position="left">选填信息</el-divider>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="作者">
                    <el-input v-model="donationForm.author" placeholder="请输入作者"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="ISBN">
                    <el-input v-model="donationForm.isbn" placeholder="请输入ISBN"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="版次">
                    <el-input v-model="donationForm.edition" placeholder="如：第1版"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="装帧">
                    <el-select v-model="donationForm.binding" placeholder="请选择装帧">
                      <el-option label="平装" value="平装"></el-option>
                      <el-option label="精装" value="精装"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="语言">
                    <el-select v-model="donationForm.language" placeholder="请选择语言">
                      <el-option label="中文" value="中文"></el-option>
                      <el-option label="英文" value="英文"></el-option>
                      <el-option label="其他" value="其他"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="估价">
                    <el-input-number v-model="donationForm.price" :min="0" :precision="2"></el-input-number>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="描述">
                    <el-input v-model="donationForm.description" type="textarea" :rows="3" placeholder="请输入图书描述"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item>
                <el-button type="primary" @click="submitDonation">提交捐赠申请</el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="我的捐赠记录" name="records">
          <el-card>
            <el-table :data="donationRecords" style="width: 100%">
              <el-table-column prop="bookName" label="书名" width="200" />
              <el-table-column prop="publisher" label="出版社" width="150" />
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column label="状态" width="100">
                <template slot-scope="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="reviewRemark" label="审核备注" width="200" />
              <el-table-column prop="reviewTime" label="审核时间" width="180" />
              <el-table-column prop="createTime" label="提交时间" width="180" />
            </el-table>
            <div class="empty-records" v-if="donationRecords.length === 0">
              <el-empty description="暂无捐赠记录"></el-empty>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import api from '@/api'
import { mapGetters } from 'vuex'

export default {
  name: 'Donation',
  data() {
    return {
      activeTab: 'submit',
      donationForm: {
        userId: null,
        bookName: '',
        publisher: '',
        quantity: 1,
        author: '',
        isbn: '',
        edition: '',
        binding: '',
        language: '',
        price: 0,
        description: ''
      },
      donationRecords: [],
      rules: {
        bookName: [{ required: true, message: '请输入书名', trigger: 'blur' }],
        publisher: [{ required: true, message: '请输入出版社', trigger: 'blur' }],
        quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    userId() {
      const id = this.userInfo && this.userInfo.id
      if (!id) {
        console.warn('[Donation] userInfo 未加载或 id 缺失，捐赠操作可能异常')
      }
      return id || null
    }
  },
  watch: {
    userId: {
      immediate: true,
      handler(val) {
        this.donationForm.userId = val
      }
    }
  },
  created() {
    this.loadRecords()
  },
  methods: {
    submitDonation() {
      this.$refs.donationForm.validate(async (valid) => {
        if (valid) {
          try {
            await api.donation.submit(this.donationForm)
            this.$message.success('捐赠申请已提交，请等待审核')
            this.resetForm()
            this.loadRecords()
          } catch (error) {
            console.error('提交失败:', error)
            this.$message.error('提交失败')
          }
        }
      })
    },
    resetForm() {
      this.donationForm = {
        userId: this.userId,
        bookName: '',
        publisher: '',
        quantity: 1,
        author: '',
        isbn: '',
        edition: '',
        binding: '',
        language: '',
        price: 0,
        description: ''
      }
      this.$refs.donationForm.resetFields()
    },
    async loadRecords() {
      try {
        const res = await api.donation.myList(this.userId)
        this.donationRecords = res || []
      } catch (error) {
        console.error('加载记录失败:', error)
      }
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
.donation-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  .donation-header {
    text-align: center;
    margin-bottom: 30px;

    h2 {
      margin: 0 0 10px;
      color: #303133;
    }
    p {
      margin: 0;
      color: #909399;
    }
  }

  .donation-content {
    .el-card {
      margin-bottom: 20px;
    }

    .empty-records {
      padding: 40px;
      text-align: center;
    }
  }
}
</style>
