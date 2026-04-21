<template>
  <div class="order-list">
    <el-card>
      <div slot="header" class="card-header">
        <h3>订单管理</h3>
        <el-tabs v-model="activeTab" @tab-click="handleTabChange">
          <el-tab-pane label="全部订单" name="all"></el-tab-pane>
          <el-tab-pane label="退款申请" name="refund"></el-tab-pane>
        </el-tabs>
      </div>

      <!-- 全部订单 -->
      <div v-if="activeTab === 'all'">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单号"></el-input>
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="searchForm.status" placeholder="全部">
              <el-option label="全部" :value="undefined" />
              <el-option label="待支付" :value="0" />
              <el-option label="已支付" :value="1" />
              <el-option label="已发货" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchOrders">查询</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="orderList" style="width: 100%" border v-loading="loading">
          <el-table-column prop="orderNo" label="订单号" min-width="180">
            <template slot-scope="scope">
              <span style="color: #409eff; cursor: pointer;" @click="viewOrder(scope.row)">
                {{ scope.row.orderNo }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="userId" label="用户ID" width="90" align="center" />
          <el-table-column label="订单状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="退款状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.refundStatus && scope.row.refundStatus > 0"
                :type="getRefundStatusType(scope.row.refundStatus)" size="small">
                {{ getRefundStatusText(scope.row.refundStatus) }}
              </el-tag>
              <span v-else style="color: #999;">无</span>
            </template>
          </el-table-column>
          <el-table-column label="订单金额" width="120" align="right">
            <template slot-scope="scope">
              ¥{{ scope.row.payAmount }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
          <el-table-column label="支付时间" width="170" align="center">
            <template slot-scope="scope">
              {{ scope.row.paymentTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="viewOrder(scope.row)">详情</el-button>
              <el-button
                v-if="scope.row.status === 1"
                type="text" size="small"
                @click="handleShip(scope.row)"
              >发货</el-button>
              <el-button
                v-if="scope.row.status === 2"
                type="text" size="small"
                @click="handleComplete(scope.row)"
              >完成</el-button>
              <el-button
                v-if="scope.row.status === 0"
                type="text" size="small" style="color: #f56c6c;"
                @click="handleCancel(scope.row)"
              >取消</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination" v-if="total > 0">
          <el-pagination
            :current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <!-- 退款申请 -->
      <div v-else>
        <el-form :inline="true" :model="refundSearchForm" class="search-form">
          <el-form-item label="订单号">
            <el-input v-model="refundSearchForm.orderNo" placeholder="请输入订单号"></el-input>
          </el-form-item>
          <el-form-item label="退款状态">
            <el-select v-model="refundSearchForm.status" placeholder="全部">
              <el-option label="全部" :value="undefined" />
              <el-option label="审核中" :value="1" />
              <el-option label="已通过" :value="2" />
              <el-option label="已拒绝" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchRefunds">查询</el-button>
            <el-button @click="resetRefundForm">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="refundList" style="width: 100%" border v-loading="refundLoading">
          <el-table-column prop="id" label="退款ID" width="80" align="center" />
          <el-table-column prop="orderNo" label="订单号" min-width="180" />
          <el-table-column prop="userId" label="用户ID" width="90" align="center" />
          <el-table-column label="退款金额" width="110" align="right">
            <template slot-scope="scope">
              ¥{{ scope.row.amount }}
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="退款原因" min-width="160" show-overflow-tooltip />
          <el-table-column prop="remark" label="处理备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="退款状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="getRefundStatusType(scope.row.status)" size="small">
                {{ getRefundStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="170" align="center" />
          <el-table-column label="处理时间" width="170" align="center">
            <template slot-scope="scope">
              {{ scope.row.handleTime || '未处理' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template slot-scope="scope">
              <el-button
                v-if="scope.row.status === 1"
                type="success" size="small"
                @click="agreeRefund(scope.row)"
              >同意退款</el-button>
              <el-button
                v-if="scope.row.status === 1"
                type="danger" size="small"
                @click="rejectRefund(scope.row)"
              >拒绝退款</el-button>
              <span v-else class="text-muted">已处理</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination" v-if="refundTotal > 0">
          <el-pagination
            :current-page="refundPageNum"
            :page-size="pageSize"
            :total="refundTotal"
            layout="total, prev, pager, next"
            @current-change="handleRefundPageChange"
          />
        </div>
      </div>

      <!-- 订单详情弹窗 -->
      <el-dialog title="订单详情" :visible.sync="detailVisible" width="650px" :before-close="() => detailVisible = false">
        <div v-if="detailData" class="order-detail">
          <el-descriptions :column="2" border size="medium">
            <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(detailData.status)">{{ getStatusText(detailData.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="总金额">¥{{ detailData.totalAmount }}</el-descriptions-item>
            <el-descriptions-item label="实付金额">¥{{ detailData.payAmount }}</el-descriptions-item>
            <el-descriptions-item label="会员折扣">{{ (detailData.discount * 10).toFixed(1) }}折</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ detailData.paymentTime || '未支付' }}</el-descriptions-item>
          </el-descriptions>

          <h4 style="margin-top: 20px; margin-bottom: 12px;">订单明细</h4>
          <el-table :data="detailItems" border size="small">
            <el-table-column prop="bookName" label="图书名称" />
            <el-table-column prop="price" label="单价" width="90" align="right">
              <template slot-scope="scope">¥{{ scope.row.price }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="70" align="center" />
            <el-table-column label="小计" width="110" align="right">
              <template slot-scope="scope">¥{{ scope.row.subtotal }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-dialog>

      <!-- 退款处理弹窗 -->
      <el-dialog title="退款处理" :visible.sync="handleRefundVisible" width="450px">
        <div v-if="currentRefund">
          <p><strong>退款ID：</strong>{{ currentRefund.id }}</p>
          <p><strong>订单号：</strong>{{ currentRefund.orderNo }}</p>
          <p><strong>退款金额：</strong>¥{{ currentRefund.amount }}</p>
          <p><strong>退款原因：</strong>{{ currentRefund.reason }}</p>
          <el-form-item label="处理备注" style="margin-top: 12px;">
            <el-textarea
              v-model="handleRemark"
              :rows="3"
              placeholder="请输入处理备注（选填）"
            ></el-textarea>
          </el-form-item>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="handleRefundVisible = false">取消</el-button>
          <el-button
            v-if="handleType === 'agree'"
            type="success"
            @click="confirmHandleRefund(2)"
          >
            确认同意退款
          </el-button>
          <el-button
            v-else
            type="danger"
            @click="confirmHandleRefund(3)"
          >
            确认拒绝退款
          </el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'OrderList',
  data() {
    return {
      activeTab: 'all',
      loading: false,
      refundLoading: false,
      searchForm: { orderNo: '', status: undefined },
      refundSearchForm: { orderNo: '', status: undefined },
      orderList: [],
      refundList: [],
      pageNum: 1,
      refundPageNum: 1,
      pageSize: 10,
      total: 0,
      refundTotal: 0,
      handleRefundVisible: false,
      currentRefund: null,
      handleRemark: '',
      handleType: '',
      // 详情弹窗
      detailVisible: false,
      detailData: null,
      detailItems: []
    }
  },
  created() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      this.loading = true
      try {
        const params = {}
        if (this.searchForm.orderNo) params.orderNo = this.searchForm.orderNo
        if (this.searchForm.status !== undefined) params.status = this.searchForm.status
        const res = await api.order.list(params)
        this.orderList = res || []
        this.total = this.orderList.length
      } catch (error) {
        console.error('加载订单失败:', error)
        this.$message.error('加载订单失败')
      } finally {
        this.loading = false
      }
    },
    async loadRefunds() {
      this.refundLoading = true
      try {
        const params = {}
        if (this.refundSearchForm.orderNo) params.orderNo = this.refundSearchForm.orderNo
        if (this.refundSearchForm.status !== undefined) params.status = this.refundSearchForm.status
        const res = await api.order.refundList(params)
        this.refundList = res || []
        this.refundTotal = this.refundList.length
      } catch (error) {
        console.error('加载退款申请失败:', error)
        this.$message.error('加载退款申请失败')
      } finally {
        this.refundLoading = false
      }
    },
    handleTabChange() {
      if (this.activeTab === 'all') {
        this.loadOrders()
      } else {
        this.loadRefunds()
      }
    },
    searchOrders() {
      this.pageNum = 1
      this.loadOrders()
    },
    resetForm() {
      this.searchForm = { orderNo: '', status: undefined }
      this.searchOrders()
    },
    searchRefunds() {
      this.refundPageNum = 1
      this.loadRefunds()
    },
    resetRefundForm() {
      this.refundSearchForm = { orderNo: '', status: undefined }
      this.searchRefunds()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.loadOrders()
    },
    handleRefundPageChange(page) {
      this.refundPageNum = page
      this.loadRefunds()
    },

    // 订单操作
    async viewOrder(row) {
      try {
        this.detailData = row
        const res = await api.order.getDetail(row.id)
        this.detailItems = res.items || []
        this.detailVisible = true
      } catch (error) {
        console.error('获取订单详情失败:', error)
        this.$message.error('获取订单详情失败')
      }
    },
    handleShip(order) {
      this.$confirm(`确认发货订单 ${order.orderNo}？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await api.order.ship(order.id)
          this.$message.success('发货成功')
          this.loadOrders()
        } catch (error) {
          console.error('发货失败:', error)
          this.$message.error(error.response?.data?.message || '发货失败')
        }
      })
    },
    handleComplete(order) {
      this.$confirm(`确认完成订单 ${order.orderNo}？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          await api.order.complete(order.id)
          this.$message.success('订单已完成')
          this.loadOrders()
        } catch (error) {
          console.error('完成订单失败:', error)
          this.$message.error(error.response?.data?.message || '完成订单失败')
        }
      })
    },
    handleCancel(order) {
      this.$confirm(`确认取消订单 ${order.orderNo}？取消后库存将自动释放。`, '提示', {
        confirmButtonText: '确定取消',
        cancelButtonText: '返回',
        type: 'warning'
      }).then(async () => {
        try {
          await api.order.cancel(order.orderNo)
          this.$message.success('订单已取消')
          this.loadOrders()
        } catch (error) {
          console.error('取消订单失败:', error)
          this.$message.error(error.response?.data?.message || '取消订单失败')
        }
      })
    },

    // 退款操作
    agreeRefund(refund) {
      this.currentRefund = refund
      this.handleRemark = ''
      this.handleType = 'agree'
      this.handleRefundVisible = true
    },
    rejectRefund(refund) {
      this.currentRefund = refund
      this.handleRemark = ''
      this.handleType = 'reject'
      this.handleRefundVisible = true
    },
    async confirmHandleRefund(status) {
      try {
        await api.order.handleRefund(this.currentRefund.id, status, this.handleRemark)
        this.$message.success(status === 2 ? '已同意退款' : '已拒绝退款')
        this.handleRefundVisible = false
        this.loadOrders()
        this.loadRefunds()
      } catch (error) {
        console.error('处理退款失败:', error)
        this.$message.error(error.response?.data?.message || '处理退款失败')
      }
    },

    // 状态映射
    getStatusType(status) {
      const types = { 0: 'warning', 1: 'success', 2: 'primary', 3: 'success', 4: 'danger', 5: 'info' }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }
      return texts[status] || '未知'
    },
    getRefundStatusType(status) {
      const types = { 1: 'warning', 2: 'success', 3: 'danger' }
      return types[status] || 'info'
    },
    getRefundStatusText(status) {
      const texts = { 1: '审核中', 2: '已通过', 3: '已拒绝' }
      return texts[status] || '未知'
    }
  }
}
</script>

<style lang="scss" scoped>
.order-list {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
      font-size: 18px;
    }
  }

  .search-form {
    margin-bottom: 16px;
  }

  .pagination {
    margin-top: 16px;
    text-align: center;
  }

  .text-muted {
    color: #999;
    font-size: 12px;
  }

  .order-detail {
    h4 {
      font-size: 15px;
      color: #303133;
    }
  }
}
</style>
