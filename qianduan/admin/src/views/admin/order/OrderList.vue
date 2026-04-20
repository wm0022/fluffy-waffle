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

      <div v-if="activeTab === 'all'">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单号"></el-input>
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="searchForm.status" placeholder="全部">
              <el-option label="全部" value=""></el-option>
              <el-option label="待支付" :value="0"></el-option>
              <el-option label="已支付" :value="1"></el-option>
              <el-option label="已发货" :value="2"></el-option>
              <el-option label="已完成" :value="3"></el-option>
              <el-option label="已取消" :value="4"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchOrders">查询</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="orderList" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="userId" label="用户ID" width="100" />
          <el-table-column label="订单状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="退款状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getRefundStatusType(scope.row.refundStatus)">
                {{ getRefundStatusText(scope.row.refundStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="订单金额" width="120">
            <template slot-scope="scope">
              ¥{{ scope.row.payAmount }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column prop="paymentTime" label="支付时间" width="180">
            <template slot-scope="scope">
              {{ scope.row.paymentTime || '未支付' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button type="text" @click="viewOrder(scope.row)">查看详情</el-button>
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

      <div v-else>
        <el-form :inline="true" :model="refundSearchForm" class="search-form">
          <el-form-item label="订单号">
            <el-input v-model="refundSearchForm.orderNo" placeholder="请输入订单号"></el-input>
          </el-form-item>
          <el-form-item label="退款状态">
            <el-select v-model="refundSearchForm.status" placeholder="全部">
              <el-option label="全部" value=""></el-option>
              <el-option label="审核中" :value="1"></el-option>
              <el-option label="已退款" :value="2"></el-option>
              <el-option label="已拒绝" :value="3"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchRefunds">查询</el-button>
            <el-button @click="resetRefundForm">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="refundList" style="width: 100%">
          <el-table-column prop="id" label="退款ID" width="100" />
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="userId" label="用户ID" width="100" />
          <el-table-column label="退款金额" width="120">
            <template slot-scope="scope">
              ¥{{ scope.row.amount }}
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="退款原因" width="200" />
          <el-table-column prop="remark" label="处理备注" width="200" />
          <el-table-column label="退款状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getRefundStatusType(scope.row.status)">
                {{ getRefundStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="180" />
          <el-table-column prop="handleTime" label="处理时间" width="180">
            <template slot-scope="scope">
              {{ scope.row.handleTime || '未处理' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template slot-scope="scope">
              <el-button 
                v-if="scope.row.status === 1"
                type="success" 
                size="small"
                @click="agreeRefund(scope.row)"
              >
                同意退款
              </el-button>
              <el-button 
                v-if="scope.row.status === 1"
                type="danger" 
                size="small"
                @click="rejectRefund(scope.row)"
              >
                拒绝退款
              </el-button>
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

      <el-dialog title="退款处理" :visible.sync="handleRefundVisible" width="400px">
        <div v-if="currentRefund">
          <p><strong>退款ID：</strong>{{ currentRefund.id }}</p>
          <p><strong>订单号：</strong>{{ currentRefund.orderNo }}</p>
          <p><strong>退款金额：</strong>¥{{ currentRefund.amount }}</p>
          <p><strong>退款原因：</strong>{{ currentRefund.reason }}</p>
          <el-form-item label="处理备注">
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
      searchForm: {
        orderNo: '',
        status: ''
      },
      refundSearchForm: {
        orderNo: '',
        status: ''
      },
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
      handleType: ''
    }
  },
  created() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      try {
        const params = {
          orderNo: this.searchForm.orderNo,
          status: this.searchForm.status
        }
        const res = await api.order.pageList(params)
        this.orderList = res || []
        this.total = this.orderList.length
      } catch (error) {
        console.error('加载订单失败:', error)
        this.$message.error('加载订单失败')
      }
    },
    async loadRefunds() {
      try {
        const params = {
          orderNo: this.refundSearchForm.orderNo,
          status: this.refundSearchForm.status
        }
        const res = await api.order.refundList(params)
        this.refundList = res || []
        this.refundTotal = this.refundList.length
      } catch (error) {
        console.error('加载退款申请失败:', error)
        this.$message.error('加载退款申请失败')
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
      this.searchForm = {
        orderNo: '',
        status: ''
      }
      this.searchOrders()
    },
    searchRefunds() {
      this.refundPageNum = 1
      this.loadRefunds()
    },
    resetRefundForm() {
      this.refundSearchForm = {
        orderNo: '',
        status: ''
      }
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
    getStatusType(status) {
      const types = {
        0: 'warning',
        1: 'success',
        2: 'info',
        3: 'success',
        4: 'danger'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        0: '待支付',
        1: '已支付',
        2: '已发货',
        3: '已完成',
        4: '已取消'
      }
      return texts[status] || '未知状态'
    },
    getRefundStatusType(status) {
      const types = {
        0: 'info',
        1: 'warning',
        2: 'success',
        3: 'danger'
      }
      return types[status] || 'info'
    },
    getRefundStatusText(status) {
      const texts = {
        0: '无',
        1: '审核中',
        2: '已退款',
        3: '已拒绝'
      }
      return texts[status] || '未知'
    },
    viewOrder(order) {
      this.$message.info('订单详情功能开发中，订单号：' + order.orderNo)
    },
    async shipOrder(order) {
      this.$confirm('确认发货该订单？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          this.$message.success('发货成功')
          this.loadOrders()
        } catch (error) {
          console.error('发货失败:', error)
          this.$message.error('发货失败')
        }
      }).catch(() => {
      })
    },
    async completeOrder(order) {
      this.$confirm('确认完成该订单？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          this.$message.success('订单已完成')
          this.loadOrders()
        } catch (error) {
          console.error('完成订单失败:', error)
          this.$message.error('完成订单失败')
        }
      }).catch(() => {
      })
    },
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
        this.$message.success(status === 2 ? '退款成功' : '已拒绝退款')
        this.handleRefundVisible = false
        this.loadRefunds()
      } catch (error) {
        console.error('处理退款失败:', error)
        this.$message.error('处理退款失败')
      }
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
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    text-align: center;
  }

  .text-muted {
    color: #999;
  }
}
</style>