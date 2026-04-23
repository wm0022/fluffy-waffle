<template>
  <div class="cart-container">
    <div class="cart-header">
      <h2>🛒 我的购物车</h2>
    </div>

    <div class="cart-content" v-if="cartList.length > 0">
      <el-table :data="cartList" style="width: 100%">
        <el-table-column prop="bookId" label="图书 ID" width="100" />
        <el-table-column label="图书信息" width="300">
          <template slot-scope="scope">
            <div class="book-info">
              <img :src="scope.row.coverImage ? 'http://localhost:8080/api' + scope.row.coverImage : '/static/default-book.svg'" alt="封面" class="book-cover" @error="handleImageError" />
              <div class="book-name">{{ scope.row.bookName }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="150">
          <template slot-scope="scope">
            ¥{{ scope.row.sellingPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="180">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.quantity" :min="1" :max="scope.row.stockCount || 99" size="small" 
                             @change="(val) => updateQuantity(scope.row, val)" />
            <span v-if="scope.row.quantity > (scope.row.stockCount || 999)" class="stock-warn">库存不足</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="80">
          <template slot-scope="scope">
            <span :class="scope.row.stockCount > 0 ? 'stock-ok' : 'stock-empty'">{{ scope.row.stockCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="小计" width="150">
          <template slot-scope="scope">
            ¥{{ (scope.row.sellingPrice * scope.row.quantity).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button type="danger" size="small" @click="removeFromCart(scope.row.bookId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-footer">
        <div class="cart-total">
          <span>共 {{ cartList.length }} 件商品</span>
          <span class="total-price">合计：¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <div class="cart-actions">
          <el-button @click="clearCart">清空购物车</el-button>
          <el-button type="primary" @click="goToCheckout">去结算</el-button>
        </div>
      </div>
    </div>

    <div class="empty-cart" v-else>
      <el-empty description="购物车空空如也～">
        <el-button type="primary" @click="goToBookList">去逛逛</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script>
import api from '@/api'
import { mapGetters } from 'vuex'

export default {
  name: 'Cart',
  data() {
    return {
      cartList: []
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    totalPrice() {
      return this.cartList.reduce((sum, item) => sum + (item.sellingPrice || 0) * (item.quantity || 1), 0)
    }
  },
  created() {
    this.loadCart()
  },
  methods: {
    async loadCart() {
      try {
        const res = await api.cart.list()
        this.cartList = res || []
      } catch (error) {
        console.error('加载购物车失败:', error)
      }
    },
    async updateQuantity(item, val) {
      if (item.stockCount !== undefined && item.stockCount !== null && val > item.stockCount) {
        this.$message.error(`《${item.bookName}》库存不足，当前库存仅 ${item.stockCount} 本`)
        // 回退数量为库存最大值
        this.$nextTick(() => {
          item.quantity = item.stockCount
        })
        return
      }
      try {
        await api.cart.add(item.bookId, item.quantity)
      } catch (error) {
        const msg = (error.response && error.response.data && error.response.data.message) || error.message || '更新数量失败'
        this.$message.error(msg)
        this.loadCart() // 失败时刷新恢复原始数据
      }
    },
    async removeFromCart(bookId) {
      try {
        await api.cart.remove(bookId)
        this.$message.success('删除成功')
        this.loadCart()
      } catch (error) {
        console.error('删除失败:', error)
      }
    },
    async clearCart() {
      try {
        await api.cart.clear()
        this.$message.success('购物车已清空')
        this.loadCart()
      } catch (error) {
        console.error('清空失败:', error)
      }
    },
    goToCheckout() {
      if (this.cartList.length === 0) {
        this.$message.warning('购物车是空的')
        return
      }
      // 检查是否有缺货商品
      const outOfStock = this.cartList.filter(item => !item.stockCount || item.quantity > item.stockCount)
      if (outOfStock.length > 0) {
        const names = outOfStock.map(item => `《${item.bookName}》`).join('、')
        this.$message.error(`${names} 库存不足，请调整数量后重试`)
        return
      }
      this.$router.push('/customer/checkout')
    },
    goToBookList() {
      this.$router.push('/customer/home')
    },
    handleImageError(e) {
      // 图片加载失败时，使用默认图片
      e.target.src = '/static/default-book.svg?t=' + new Date().getTime()
    }
  }
}
</script>

<style lang="scss" scoped>
.cart-container {
  padding: 20px;
  min-height: calc(100vh - 120px);

  .cart-header {
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      color: #303133;
    }
  }

  .cart-content {
    background: #fff;
    border-radius: 8px;
    padding: 20px;

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
  }

  .cart-footer {
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .cart-total {
      font-size: 16px;
      
      .total-price {
        margin-left: 20px;
        color: #f56c6c;
        font-size: 20px;
        font-weight: bold;
      }
    }

    .cart-actions {
      .el-button {
        margin-left: 10px;
      }
    }
  }

  .empty-cart {
    background: #fff;
    border-radius: 8px;
    padding: 60px 20px;
    text-align: center;
  }

  .stock-warn {
    color: #f56c6c;
    font-size: 12px;
    margin-left: 5px;
  }

  .stock-ok {
    color: #67c23a;
    font-weight: 500;
  }

  .stock-empty {
    color: #f56c6c;
    font-weight: 500;
  }
}
</style>
