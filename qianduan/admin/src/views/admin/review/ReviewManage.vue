<template>
  <div class="review-manage">
    <el-card>
      <div slot="header">
        <span>评价管理</span>
      </div>
      <div class="search-bar">
        <el-input v-model="searchForm.customerName" placeholder="客户名称" size="small" style="width: 150px;" clearable />
        <el-select v-model="searchForm.auditStatus" placeholder="审核状态" size="small" style="width: 120px;" clearable>
          <el-option label="待审核" :value="1" />
          <el-option label="已通过" :value="2" />
          <el-option label="已拒绝" :value="3" />
        </el-select>
        <el-select v-model="searchForm.rating" placeholder="评分" size="small" style="width: 100px;" clearable>
          <el-option v-for="i in 5" :key="i" :label="i + '星'" :value="i" />
        </el-select>
        <el-button type="primary" size="small" icon="el-icon-search" @click="loadReviews">搜索</el-button>
        <el-button size="small" @click="resetSearch">重置</el-button>
      </div>
      <el-table :data="reviews" border stripe size="small">
        <el-table-column prop="reviewId" label="ID" width="60" />
        <el-table-column prop="customerName" label="客户" width="100">
          <template slot-scope="scope">
            {{ scope.row.isAnonymous ? '匿名用户' : scope.row.customerName }}
          </template>
        </el-table-column>
        <el-table-column prop="bookId" label="图书ID" width="70" />
        <el-table-column prop="rating" label="评分" width="120">
          <template slot-scope="scope">
            <el-rate v-model="scope.row.rating" disabled size="mini" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="多维度评分" width="220">
          <template slot-scope="scope">
            <span v-if="scope.row.storeRating">书店:{{ scope.row.storeRating }}分</span>
            <span v-if="scope.row.businessRating"> 业务:{{ scope.row.businessRating }}分</span>
            <span v-if="scope.row.serviceRating"> 服务:{{ scope.row.serviceRating }}分</span>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.auditStatus === 1 ? 'warning' : scope.row.auditStatus === 2 ? 'success' : 'danger'" size="mini">
              {{ scope.row.auditStatus === 1 ? '待审核' : scope.row.auditStatus === 2 ? '已通过' : '已拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="显示" width="70">
          <template slot-scope="scope">
            <el-switch :value="scope.row.status === 1" @change="toggleVisibility(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column prop="replyContent" label="回复" width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            {{ scope.row.replyContent || '未回复' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.auditStatus === 1" type="success" size="mini" @click="handleAudit(scope.row, 2)">通过</el-button>
            <el-button v-if="scope.row.auditStatus === 1" type="danger" size="mini" @click="handleAudit(scope.row, 3)">拒绝</el-button>
            <el-button v-if="scope.row.auditStatus === 2 && !scope.row.replyContent" type="primary" size="mini" @click="openReplyDialog(scope.row)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        @current-change="handlePageChange"
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 15px; text-align: right;"
      />
    </el-card>

    <el-dialog title="回复评价" :visible.sync="replyDialogVisible" width="500px">
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="请输入回复内容" />
      <span slot="footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'ReviewManage',
  data() {
    return {
      reviews: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchForm: {
        customerName: '',
        auditStatus: null,
        rating: null
      },
      replyDialogVisible: false,
      replyContent: '',
      currentReview: null
    }
  },
  created() {
    this.loadReviews()
  },
  methods: {
    async loadReviews() {
      try {
        const res = await api.review.pageList({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          ...this.searchForm
        })
        this.reviews = res.records || []
        this.total = res.total || 0
      } catch (e) {
        this.$message.error('加载评价列表失败')
      }
    },
    resetSearch() {
      this.searchForm = { customerName: '', auditStatus: null, rating: null }
      this.pageNum = 1
      this.loadReviews()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.loadReviews()
    },
    async handleAudit(review, auditStatus) {
      try {
        await api.review.audit(review.reviewId, {
          auditStatus,
          auditRemark: auditStatus === 2 ? '审核通过' : '内容不合规',
          auditUserId: 1
        })
        this.$message.success(auditStatus === 2 ? '审核通过' : '已拒绝')
        this.loadReviews()
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    async toggleVisibility(review) {
      try {
        await api.review.toggleVisibility(review.reviewId)
        this.$message.success('操作成功')
        this.loadReviews()
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    openReplyDialog(review) {
      this.currentReview = review
      this.replyContent = ''
      this.replyDialogVisible = true
    },
    async submitReply() {
      if (!this.replyContent.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }
      try {
        await api.review.reply(this.currentReview.reviewId, {
          replyContent: this.replyContent,
          replyUserId: 1
        })
        this.$message.success('回复成功')
        this.replyDialogVisible = false
        this.loadReviews()
      } catch (e) {
        this.$message.error('回复失败')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.review-manage {
  .search-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;
    flex-wrap: wrap;
  }
}
</style>
