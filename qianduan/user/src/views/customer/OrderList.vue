<template>
  <div class="order-list">
    <div class="order-header">
      <h2>我的订单</h2>
    </div>

    <div class="order-content" v-if="orderList.length > 0">
      <div v-for="order in orderList" :key="order.id" class="order-card">
        <div class="order-top">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusText(order.status) }}</el-tag>
          <span class="order-time">{{ order.createTime }}</span>
          <span class="order-amount">¥{{ order.payAmount }}</span>
        </div>
        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <div class="item-info">
              <span class="item-name">{{ item.bookName }}</span>
              <span class="item-price">¥{{ item.price }} × {{ item.quantity }}</span>
              <span class="item-subtotal">小计：¥{{ item.subtotal }}</span>
            </div>
            <div class="item-actions" v-if="order.status === 1">
              <el-button v-if="item.reviewed" type="info" size="mini" disabled>已评价</el-button>
              <el-button v-else type="warning" size="mini" @click="openReviewDialog(order, item)">评价</el-button>
            </div>
          </div>
        </div>
        <div class="order-bottom">
          <el-button v-if="order.status === 0" type="primary" size="small" @click="payOrder(order.orderNo)">去支付</el-button>
          <el-button v-if="canApplyRefund(order)" type="danger" size="small" @click="showRefundDialog(order)">申请退款</el-button>
          <el-button v-if="order.refundStatus === 1" type="warning" size="small" disabled>退款审核中</el-button>
          <el-button v-if="order.refundStatus === 2" type="success" size="small" disabled>退款成功</el-button>
          <el-button v-if="order.refundStatus === 3" type="info" size="small" disabled>退款已拒绝</el-button>
        </div>
      </div>
    </div>

    <div class="empty-order" v-else>
      <el-empty description="暂无订单记录">
        <el-button type="primary" @click="goToHome">去逛逛</el-button>
      </el-empty>
    </div>

    <el-dialog title="申请退款" :visible.sync="refundDialogVisible" width="400px">
      <div v-if="currentOrder">
        <p><strong>订单号：</strong>{{ currentOrder.orderNo }}</p>
        <p><strong>订单金额：</strong>¥{{ currentOrder.payAmount }}</p>
        <div style="margin-top: 20px;">
          <label style="display: block; margin-bottom: 8px; font-weight: bold;">退款原因</label>
          <el-input v-model="refundReason" type="textarea" :rows="3" placeholder="请输入退款原因" />
        </div>
      </div>
      <span slot="footer">
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitRefund">确认申请</el-button>
      </span>
    </el-dialog>

    <el-dialog title="发表评价" :visible.sync="reviewDialogVisible" width="600px">
      <div v-if="currentOrderItem" style="margin-bottom: 15px;">
        <strong>图书：</strong>{{ currentOrderItem.bookName }}
      </div>
      <el-form ref="reviewForm" :model="reviewForm" label-width="100px">
        <el-form-item label="图书评分" required>
          <el-rate v-model="reviewForm.rating" show-text :texts="['很差','较差','一般','较好','很好']" />
        </el-form-item>
        <el-form-item label="书店评价">
          <el-rate v-model="reviewForm.storeRating" show-text :texts="['很差','较差','一般','较好','很好']" />
        </el-form-item>
        <el-form-item label="业务评价">
          <el-rate v-model="reviewForm.businessRating" show-text :texts="['很差','较差','一般','较好','很好']" />
        </el-form-item>
        <el-form-item label="服务评价">
          <el-rate v-model="reviewForm.serviceRating" show-text :texts="['很差','较差','一般','较好','很好']" />
        </el-form-item>
        <el-form-item label="评价内容" required>
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请输入评价内容" />
        </el-form-item>
        <el-form-item label="匿名评价">
          <el-switch v-model="reviewForm.isAnonymous" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api'
import { mapGetters } from 'vuex'

export default {
  name: 'OrderList',
  data() {
    return {
      orderList: [],
      refundDialogVisible: false,
      currentOrder: null,
      refundReason: '',
      reviewDialogVisible: false,
      currentOrderItem: null,
      currentOrderForReview: null,
      reviewForm: {
        rating: 5,
        storeRating: 5,
        businessRating: 5,
        serviceRating: 5,
        content: '',
        isAnonymous: 0
      }
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    userId() {
      return this.userInfo.id || 1
    }
  },
  created() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      try {
        const res = await api.order.list(this.userId, { _t: Date.now() })
        this.orderList = res || []
        for (let order of this.orderList) {
          try {
            const items = await api.order.getItems(order.id)
            const itemList = items || []
            for (let item of itemList) {
              try {
                const reviewed = await api.review.checkExists(item.id)
                this.$set(item, 'reviewed', reviewed)
              } catch (e) {
                this.$set(item, 'reviewed', false)
              }
            }
            this.$set(order, 'items', itemList)
          } catch (e) {
            this.$set(order, 'items', [])
          }
        }
      } catch (error) {
        console.error('加载订单失败:', error)
        this.$message.error('加载订单失败')
      }
    },
    getStatusType(status) {
      return { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[status] || 'info'
    },
    getStatusText(status) {
      return { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }[status] || '未知状态'
    },
    canApplyRefund(order) {
      return order.status === 1 && (order.refundStatus === 0 || order.refundStatus === undefined)
    },
    showRefundDialog(order) {
      this.currentOrder = order
      this.refundReason = ''
      this.refundDialogVisible = true
    },
    async submitRefund() {
      if (!this.refundReason.trim()) {
        this.$message.warning('请输入退款原因')
        return
      }
      try {
        await api.order.applyRefund(this.currentOrder.orderNo, this.refundReason)
        this.$message.success('退款申请已提交')
        this.refundDialogVisible = false
        this.loadOrders()
      } catch (error) {
        this.$message.error('提交退款申请失败')
      }
    },
    async payOrder(orderNo) {
      try {
        await this.$confirm('确认支付该订单？', '提示', { type: 'warning' })
        await api.order.pay(orderNo)
        this.$message.success('支付成功')
        this.loadOrders()
      } catch (e) {}
    },
    async openReviewDialog(order, item) {
      try {
        const reviewed = await api.review.checkExists(item.id)
        if (reviewed) {
          this.$message.warning('该图书已评价，不能重复评价')
          this.$set(item, 'reviewed', true)
          return
        }
      } catch (e) {
        // ignore
      }
      this.currentOrderForReview = order
      this.currentOrderItem = item
      this.reviewForm = {
        rating: 5,
        storeRating: 5,
        businessRating: 5,
        serviceRating: 5,
        content: '',
        isAnonymous: 0
      }
      this.reviewDialogVisible = true
    },
    async submitReview() {
      if (!this.reviewForm.content.trim()) {
        this.$message.warning('请输入评价内容')
        return
      }
      if (this.reviewForm.rating === 0) {
        this.$message.warning('请选择图书评分')
        return
      }
      try {
        await api.review.add({
          ...this.reviewForm,
          bookId: this.currentOrderItem.bookId,
          bookName: this.currentOrderItem.bookName,
          orderItemId: this.currentOrderItem.id,
          memberId: this.userId,
          customerName: this.userInfo.username
        })
        this.$message.success('评价提交成功，等待审核')
        this.reviewDialogVisible = false
        this.$set(this.currentOrderItem, 'reviewed', true)
      } catch (e) {
        const msg = (e && e.message) || '评价提交失败'
        this.$message.error(msg)
      }
    },
    goToHome() {
      this.$router.push('/customer/home')
    }
  }
}
</script>

<style lang="scss" scoped>
.order-list {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;

  .order-header {
    margin-bottom: 20px;
    h2 { margin: 0; color: #303133; }
  }

  .order-card {
    background: #fff;
    border-radius: 8px;
    margin-bottom: 15px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    overflow: hidden;

    .order-top {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 12px 20px;
      background: #f5f7fa;
      border-bottom: 1px solid #ebeef5;

      .order-no { font-weight: bold; color: #303133; }
      .order-time { color: #909399; font-size: 13px; }
      .order-amount { margin-left: auto; color: #f56c6c; font-size: 18px; font-weight: bold; }
    }

    .order-items {
      padding: 10px 20px;

      .order-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 0;
        border-bottom: 1px dashed #ebeef5;

        &:last-child { border-bottom: none; }

        .item-info {
          display: flex;
          gap: 20px;
          align-items: center;

          .item-name { font-weight: bold; color: #303133; min-width: 200px; }
          .item-price { color: #909399; }
          .item-subtotal { color: #f56c6c; }
        }
      }
    }

    .order-bottom {
      padding: 10px 20px;
      text-align: right;
      border-top: 1px solid #ebeef5;
    }
  }

  .empty-order {
    background: #fff;
    border-radius: 8px;
    padding: 60px 20px;
    text-align: center;
  }
}
</style>
