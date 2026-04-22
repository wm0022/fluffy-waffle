<template>
  <div class="donation-manage">
    <el-card>
      <div slot="header" class="card-header">
        <h3>捐赠审核管理</h3>
      </div>

      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="待审核" name="0"></el-tab-pane>
        <el-tab-pane label="已通过" name="1"></el-tab-pane>
        <el-tab-pane label="已拒绝" name="2"></el-tab-pane>
        <el-tab-pane label="全部" name="all"></el-tab-pane>
      </el-tabs>

      <el-table :data="donationList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="userName" label="捐赠者姓名" width="120" />
        <el-table-column prop="bookName" label="书名" width="180" />
        <el-table-column prop="publisher" label="出版社" width="150" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewRemark" label="审核备注" width="180" />
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="220">
          <template slot-scope="scope">
            <el-button 
              v-if="scope.row.status === 0"
              type="success" 
              size="small"
              @click="showReviewDialog(scope.row, 1)"
            >
              通过
            </el-button>
            <el-button 
              v-if="scope.row.status === 0"
              type="danger" 
              size="small"
              @click="showReviewDialog(scope.row, 2)"
            >
              拒绝
            </el-button>
            <el-button 
              v-if="scope.row.status !== 0"
              type="primary" 
              size="small"
              @click="showEditDialog(scope.row)"
            >
              编辑
            </el-button>
            <span v-if="scope.row.status === 0" class="text-muted">待审核</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="empty-tip" v-if="donationList.length === 0">
        <el-empty description="暂无记录"></el-empty>
      </div>
    </el-card>

    <el-dialog title="审核捐赠申请" :visible.sync="reviewDialogVisible" width="400px">
      <div v-if="currentDonation">
        <p><strong>书名：</strong>{{ currentDonation.bookName }}</p>
        <p><strong>出版社：</strong>{{ currentDonation.publisher }}</p>
        <p><strong>数量：</strong>{{ currentDonation.quantity }}</p>
        <el-form-item label="审核备注">
          <el-input 
            v-model="reviewRemark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入审核备注（选填）"
          ></el-input>
        </el-form-item>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button 
          :type="reviewType === 1 ? 'success' : 'danger'"
          @click="confirmReview"
        >
          {{ reviewType === 1 ? '确认通过' : '确认拒绝' }}
        </el-button>
      </div>
    </el-dialog>

    <el-dialog title="编辑捐赠信息" :visible.sync="editDialogVisible" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="书名">
          <el-input v-model="editForm.bookName" placeholder="请输入书名"></el-input>
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="editForm.publisher" placeholder="请输入出版社"></el-input>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="editForm.quantity" :min="1" :max="100"></el-input-number>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="editForm.author" placeholder="请输入作者"></el-input>
        </el-form-item>
        <el-form-item label="ISBN">
          <el-input v-model="editForm.isbn" placeholder="请输入ISBN"></el-input>
        </el-form-item>
        <el-form-item label="版次">
          <el-input v-model="editForm.edition" placeholder="如：第1版"></el-input>
        </el-form-item>
        <el-form-item label="装帧">
          <el-select v-model="editForm.binding" placeholder="请选择装帧">
            <el-option label="平装" value="平装"></el-option>
            <el-option label="精装" value="精装"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="语言">
          <el-select v-model="editForm.language" placeholder="请选择语言">
            <el-option label="中文" value="中文"></el-option>
            <el-option label="英文" value="英文"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="估价">
          <el-input-number v-model="editForm.price" :min="0" :precision="2"></el-input-number>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入图书描述"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" placeholder="请选择状态">
            <el-option label="已通过" :value="1"></el-option>
            <el-option label="已拒绝" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="editForm.reviewRemark" type="textarea" :rows="2" placeholder="请输入审核备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'DonationManage',
  data() {
    return {
      activeTab: '0',
      donationList: [],
      reviewDialogVisible: false,
      editDialogVisible: false,
      currentDonation: null,
      reviewType: 1,
      reviewRemark: '',
      editForm: {
        id: null,
        bookName: '',
        publisher: '',
        quantity: 1,
        author: '',
        isbn: '',
        edition: '',
        binding: '',
        language: '',
        price: 0,
        description: '',
        status: 1,
        reviewRemark: ''
      }
    }
  },
  created() {
    this.loadDonations()
  },
  methods: {
    async loadDonations() {
      try {
        const status = this.activeTab === 'all' ? null : parseInt(this.activeTab)
        const res = await api.donation.list(status)
        this.donationList = res || []
      } catch (error) {
        console.error('加载捐赠记录失败:', error)
        this.$message.error('加载捐赠记录失败')
      }
    },
    handleTabChange() {
      this.loadDonations()
    },
    showReviewDialog(donation, type) {
      this.currentDonation = donation
      this.reviewType = type
      this.reviewRemark = ''
      this.reviewDialogVisible = true
    },
    async confirmReview() {
      try {
        await api.donation.review(this.currentDonation.id, this.reviewType, this.reviewRemark)
        this.$message.success(this.reviewType === 1 ? '审核通过' : '已拒绝')
        this.reviewDialogVisible = false
        this.loadDonations()
      } catch (error) {
        console.error('审核失败:', error)
        this.$message.error('审核失败')
      }
    },
    showEditDialog(donation) {
      this.editForm = {
        id: donation.id,
        bookName: donation.bookName,
        publisher: donation.publisher,
        quantity: donation.quantity,
        author: donation.author,
        isbn: donation.isbn,
        edition: donation.edition,
        binding: donation.binding,
        language: donation.language,
        price: donation.price,
        description: donation.description,
        status: donation.status,
        reviewRemark: donation.reviewRemark
      }
      this.editDialogVisible = true
    },
    async confirmEdit() {
      try {
        await api.donation.update(this.editForm)
        this.$message.success('保存成功')
        this.editDialogVisible = false
        this.loadDonations()
      } catch (error) {
        console.error('保存失败:', error)
        this.$message.error('保存失败')
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
.donation-manage {
  padding: 20px;

  .card-header {
    h3 {
      margin: 0;
    }
  }

  .text-muted {
    color: #999;
  }

  .empty-tip {
    padding: 40px;
    text-align: center;
  }
}
</style>
