<template>
  <div class="checkout-container">
    <div class="checkout-header">
      <h2>📦 订单确认</h2>
    </div>

    <div class="checkout-content">
      <!-- 订单商品列表 -->
      <el-card class="order-items">
        <div slot="header">
          <span>商品清单</span>
        </div>
        <el-table :data="displayItems" style="width: 100%">
          <el-table-column label="商品信息" width="400">
            <template slot-scope="scope">
              <div class="book-info">
                <img :src="scope.row.coverImage ? 'http://localhost:8080/api' + scope.row.coverImage : '/static/default-book.svg'" alt="封面" class="book-cover" @error="handleImageError" />
                <div class="book-name">{{ scope.row.bookName }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="sellingPrice" label="单价" width="150">
            <template slot-scope="scope">
              ¥{{ scope.row.sellingPrice }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="库存状态" width="120">
            <template slot-scope="scope">
              <span v-if="(scope.row.stockCount || 0) >= scope.row.quantity" class="stock-ok">库存充足</span>
              <span v-else class="stock-out">库存不足</span>
            </template>
          </el-table-column>
          <el-table-column label="小计" width="150">
            <template slot-scope="scope">
              ¥{{ (scope.row.sellingPrice * scope.row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 订单金额 -->
      <el-card class="order-summary">
        <div slot="header">
          <span>订单金额</span>
        </div>
        <div class="summary-row">
          <span>商品金额：</span>
          <span class="amount">¥{{ totalAmount }}</span>
        </div>
        <div class="summary-row" v-if="memberInfo.level > 0">
          <span>会员折扣（{{ memberInfo.levelName }}）：</span>
          <span class="amount discount">-¥{{ discountAmount }}</span>
        </div>
        <div class="summary-row" v-if="memberInfo.level > 0">
          <span>折扣率：</span>
          <span class="amount discount">{{ memberInfo.discountText }}</span>
        </div>
        <div class="summary-row total">
          <span>应付金额：</span>
          <span class="amount">¥{{ payAmount }}</span>
        </div>
      </el-card>

      <!-- 提交订单 -->
      <div class="order-actions">
        <el-button @click="goBack">返回购物车</el-button>
        <el-button type="primary" size="large" :loading="loading" @click="submitOrder">
          提交订单
        </el-button>
      </div>
    </div>

    <!-- 个人信息未完善提示弹窗 -->
    <el-dialog title="温馨提示" :visible.sync="profileDialogVisible" width="420px" :close-on-click-modal="false" :show-close="false">
      <div style="text-align: center; padding: 10px 0 20px;">
        <p style="font-size: 16px; color: #303133; margin-bottom: 12px;">您的个人信息尚未完善</p>
        <p style="font-size: 14px; color: #909399; line-height: 1.6;">
          为了顺利配送您的订单，请先前往<span style="color: #e6a23c; font-weight: bold;">个人中心</span>完善<span style="color: #e6a23c; font-weight: bold;">姓名、联系电话、收货地址</span>等信息。
        </p>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="profileDialogVisible = false">我知道了</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api'
import { mapGetters } from 'vuex'

export default {
  name: 'Checkout',
  data() {
    return {
      cartItems: [],
      buyNowItem: null, // 直接购买的商品
      loading: false,
      memberInfo: {
        level: 0,
        levelName: '非会员',
        discount: 1.0,
        discountText: '无折扣'
      },
      profileDialogVisible: false
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    // 显示的商品列表（支持直接购买和购物车两种模式）
    displayItems() {
      if (this.buyNowItem) {
        return [this.buyNowItem]
      }
      return this.cartItems
    },
    totalAmount() {
      // 如果是直接购买，只计算当前商品
      if (this.buyNowItem) {
        return (this.buyNowItem.sellingPrice * this.buyNowItem.quantity).toFixed(2)
      }
      // 否则计算购物车中所有商品
      const sum = this.cartItems.reduce((total, item) => {
        return total + (item.subtotal || 0)
      }, 0)
      return sum.toFixed(2)
    },
    payAmount() {
      const total = parseFloat(this.totalAmount)
      const discount = this.memberInfo.discount || 1.0
      return (total * discount).toFixed(2)
    },
    discountAmount() {
      const total = parseFloat(this.totalAmount)
      const discount = this.memberInfo.discount || 1.0
      return (total * (1 - discount)).toFixed(2)
    }
  },
  created() {
    this.loadMemberInfo()
    // 检查是否是直接购买模式
    const bookId = this.$route.query.bookId
    if (bookId) {
      // 直接购买模式：加载单个商品信息
      this.loadBuyNowItem(bookId)
    } else {
      // 购物车结算模式：加载购物车商品
      this.loadCartItems()
    }
  },
  methods: {
    async loadMemberInfo() {
      try {
        const res = await api.member.getInfo()
        if (res) {
          this.memberInfo = {
            level: res.memberLevel || 0,
            levelName: res.levelName || '非会员',
            discount: res.discount || 1.0,
            discountText: res.discountText || '无折扣'
          }
        }
      } catch (error) {
        console.error('加载会员信息失败:', error)
      }
    },
    async loadBuyNowItem(bookId) {
      try {
        // 获取图书详情
        const bookList = await api.book.pageList({ pageNum: 1, pageSize: 100 })
        const book = bookList.records.find(b => b.id == bookId)
        if (!book) {
          this.$message.error('图书不存在')
          this.$router.push('/customer/home')
          return
        }
        
        this.buyNowItem = {
          bookId: book.id,
          bookName: book.bookName,
          sellingPrice: book.sellingPrice,
          coverImage: book.coverImage,
          quantity: 1,
          subtotal: book.sellingPrice
        }
      } catch (error) {
        console.error('加载商品信息失败:', error)
        this.$message.error('加载失败')
      }
    },
    async loadCartItems() {
      try {
        const res = await api.cart.list()
        this.cartItems = res || []
        if (this.cartItems.length === 0) {
          this.$message.warning('购物车是空的')
          this.$router.push('/customer/cart')
        }
      } catch (error) {
        console.error('加载购物车失败:', error)
        this.$message.error('加载失败')
      }
    },
    checkProfile() {
      if (!this.userInfo) return true
      const requiredFields = ['realName', 'phone', 'address']
      for (const field of requiredFields) {
        const val = this.userInfo[field]
        if (!val || String(val).trim() === '') return false
      }
      return true
    },
    async submitOrder() {
      // 先检查个人信息是否完善
      if (!this.checkProfile()) {
        this.profileDialogVisible = true
        return
      }
      this.loading = true
      try {
        // 如果是直接购买，先添加到购物车再创建订单
        if (this.buyNowItem) {
          await api.cart.add(this.buyNowItem.bookId, 1)
        }
        
        const res = await api.order.create({})
        console.log('订单创建成功，订单号:', res.orderNo, '订单金额:', res.payAmount)
        this.$message.success('订单创建成功')
        
        // 跳转到支付页面
        this.$router.push({
          path: '/customer/pay',
          query: {
            orderNo: res.orderNo,
            amount: res.payAmount
          }
        })
      } catch (error) {
        console.error('创建订单失败:', error)
        this.$message.error(error.response?.data?.message || '创建订单失败')
      } finally {
        this.loading = false
      }
    },
    goBack() {
      // 根据来源返回不同页面
      if (this.buyNowItem) {
        this.$router.push('/customer/home')
      } else {
        this.$router.push('/customer/cart')
      }
    },
    handleImageError(e) {
      // 图片加载失败时，使用默认图片
      e.target.src = '/static/default-book.svg?t=' + new Date().getTime()
    }
  }
}
</script>

<style lang="scss" scoped>
.checkout-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;

  .checkout-header {
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      color: #303133;
    }
  }

  .checkout-content {
    .order-items {
      margin-bottom: 20px;

      .book-info {
        display: flex;
        align-items: center;

        .book-cover {
          width: 60px;
          height: 80px;
          object-fit: contain;
          margin-right: 10px;
          border-radius: 4px;
        }

        .book-name {
          font-size: 14px;
          color: #303133;
        }
      }

      .stock-ok {
        color: #67c23a;
        font-weight: 500;
      }

      .stock-out {
        color: #f56c6c;
        font-weight: 500;
      }
    }

    .order-summary {
      margin-bottom: 20px;

      .summary-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
        font-size: 14px;

        .amount {
          color: #f56c6c;
          font-size: 18px;
          font-weight: bold;

          &.discount {
            color: #67c23a;
          }
        }

        &.total {
          font-size: 16px;
          font-weight: bold;
        }
      }
    }

    .order-actions {
      text-align: right;

      .el-button {
        margin-left: 10px;
      }
    }
  }
}
</style>
