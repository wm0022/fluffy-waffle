<template>
  <div class="pay-container">
    <div class="pay-header">
      <h2>💳 支付订单</h2>
    </div>

    <div class="pay-content">
      <el-card class="pay-info">
        <div slot="header">
          <span>订单信息</span>
        </div>
        <div class="info-row">
          <span>订单编号：</span>
          <span>{{ orderNo }}</span>
        </div>
        <div class="info-row">
          <span>订单金额：</span>
          <span class="amount">¥{{ payAmount }}</span>
        </div>
      </el-card>



      <div class="pay-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button 
          type="primary" 
          size="large" 
          :loading="loading"
          @click="confirmPay"
        >
          确认支付
        </el-button>
      </div>
    </div>

    <!-- 支付成功弹窗 -->
    <el-dialog
      title="支付成功"
      :visible.sync="paySuccessDialog"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="success-content">
        <i class="el-icon-circle-check success-icon"></i>
        <p>支付成功！</p>
        <p>订单号：{{ orderNo }}</p>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="goToOrders">查看订单</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'Pay',
  data() {
    return {
      orderNo: '',
      payAmount: 0,
      loading: false,
      paySuccessDialog: false
    }
  },
  created() {
    this.orderNo = this.$route.query.orderNo
    console.log('支付页面 - 获取订单号:', this.orderNo, '完整路由查询参数:', this.$route.query)
    if (!this.orderNo) {
      this.$message.error('订单号不能为空')
      this.$router.push('/customer/order')
    }
    this.payAmount = this.$route.query.amount || 0
  },
  methods: {
    async confirmPay() {
      console.log('确认支付，订单号:', this.orderNo)
      this.loading = true
      try {
        // 直接传递订单号字符串，而不是对象
        await api.order.pay(this.orderNo)
        this.paySuccessDialog = true
      } catch (error) {
        console.error('支付失败:', error)
        this.$message.error(error.response?.data?.message || '支付失败')
      } finally {
        this.loading = false
      }
    },
    goBack() {
      this.$router.push('/customer/cart')
    },
    goToOrders() {
      this.$router.push('/customer/order')
    }
  }
}
</script>

<style lang="scss" scoped>
.pay-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;

  .pay-header {
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      color: #303133;
    }
  }

  .pay-content {
    .pay-info {
      margin-bottom: 20px;

      .info-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
        font-size: 14px;

        .amount {
          color: #f56c6c;
          font-size: 20px;
          font-weight: bold;
        }
      }
    }



    .pay-actions {
      text-align: right;

      .el-button {
        margin-left: 10px;
      }
    }
  }

  .success-content {
    text-align: center;
    padding: 20px;

    .success-icon {
      font-size: 60px;
      color: #67c23a;
      margin-bottom: 10px;
    }

    p {
      margin: 5px 0;
      color: #606266;
    }
  }
}
</style>
