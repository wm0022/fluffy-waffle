<template>
  <div class="cart-page">
    <!-- ========== 页面标题区 ========== -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <i class="el-icon-shopping-cart-2"></i>
          我的购物车
        </h2>
        <span v-if="cartList.length > 0" class="header-hint">
          共 <strong>{{ cartList.length }}</strong> 种商品
        </span>
      </div>
    </div>

    <!-- ===== 购车主内容 ===== -->
    <template v-if="cartList.length > 0">
      <div class="cart-main">
        <!-- 表头栏 -->
        <div class="cart-table-head">
          <span class="col-check">
            <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          </span>
          <span class="col-info">商品信息</span>
          <span class="col-price">单价</span>
          <span class="col-qty">数量</span>
          <span class="col-stock">库存</span>
          <span class="col-subtotal">小计</span>
          <span class="col-action">操作</span>
        </div>

        <!-- 商品列表 -->
        <transition-group name="cart-list" tag="div" class="cart-body" appear>
          <div
            v-for="(item, index) in cartList"
            :key="'cart-' + item.bookId + (item.cartId || '')"
            class="cart-item"
            :class="{ 'is-deleting': deletingId === item.bookId }"
          >
            <span class="col-check">
              <el-checkbox
                :value="selectedIds.includes(item.bookId)"
                @change="(val) => toggleSelect(item.bookId, val)"
              />
            </span>

            <!-- 商品信息 -->
            <span class="col-info">
              <div class="product-cell">
                <div class="cover-wrap" @click="goToDetail(item.bookId)">
                  <img
                    :src="getCoverUrl(item.coverImage)"
                    :alt="item.bookName"
                    class="prod-cover"
                    loading="lazy"
                    @error="onImgError($event)"
                  />
                </div>
                <div class="info-text">
                  <h4 class="prod-name" @click="goToDetail(item.bookId)" :title="item.bookName">{{ item.bookName }}</h4>
                  <p class="prod-author">{{ item.author || '' }}</p>
                  <el-tag
                    v-if="!item.stockCount || item.quantity > (item.stockCount || 0)"
                    size="mini"
                    type="danger"
                    effect="dark"
                    class="stock-tag-danger"
                  >
                    库存不足
                  </el-tag>
                </div>
              </div>
            </span>

            <!-- 单价 -->
            <span class="col-price">
              <span class="price-text">&yen;{{ formatPrice(item.sellingPrice) }}</span>
            </span>

            <!-- 数量（带防抖） -->
            <span class="col-qty">
              <div class="qty-stepper">
                <button
                  class="stepper-btn minus"
                  :disabled="item.quantity <= 1 || updatingQty[item.bookId]"
                  @click="stepQuantity(item, -1)"
                >
                  <i class="el-icon-minus"></i>
                </button>
                <input
                  type="text"
                  class="stepper-input"
                  :value="item.quantity"
                  :readonly="updatingQty[item.bookId]"
                  @change="(e) => onQtyInput(item, e.target.value)"
                />
                <button
                  class="stepper-btn plus"
                  :disabled="item.quantity >= (item.stockCount || 99) || updatingQty[item.bookId]"
                  @click="stepQuantity(item, 1)"
                >
                  <i class="el-icon-plus"></i>
                </button>
              </div>
              <transition name="fade-tip">
                <span v-if="updatingQty[item.bookId]" class="syncing-hint">
                  <i class="el-icon-loading"></i> 同步中
                </span>
              </transition>
            </span>

            <!-- 库存 -->
            <span class="col-stock">
              <span :class="stockClass(item.stockCount)">{{ item.stockCount || 0 }}</span>
            </span>

            <!-- 小计 -->
            <span class="col-subtotal">
              <span class="subtotal-price">&yen;{{ calcSubtotal(item) }}</span>
            </span>

            <!-- 操作 -->
            <span class="col-action">
              <button class="action-btn del-btn" @click="handleRemove(item)">
                <i class="el-icon-delete"></i>
                删除
              </button>
            </span>
          </div>
        </transition-group>
      </div>

      <!-- ===== 底部结算栏（吸底）===== -->
      <div class="cart-footer-bar">
        <div class="footer-left">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <button class="link-btn" @click="confirmClearCart">
            清空已选商品
          </button>
        </div>
        <div class="footer-right">
          <div class="summary-row">
            <span class="summary-label">已选 <em>{{ selectedItems.length }}</em> 件</span>
            <span class="summary-total">
              合计：<strong>&yen;{{ formatPrice(selectedTotal) }}</strong>
            </span>
          </div>
          <el-button
            type="primary"
            size="large"
            round
            class="checkout-btn"
            :disabled="selectedItems.length === 0"
            :loading="checkoutLoading"
            @click="goToCheckout"
          >
            去结算
          </el-button>
        </div>
      </div>
    </template>

    <!-- ===== 空购物车状态 ===== -->
    <div v-else class="empty-cart-state">
      <div class="empty-visual">
        <div class="empty-cart-icon">
          <i class="el-icon-shopping-cart-full"></i>
        </div>
      </div>
      <h3 class="empty-title">购物车空空如也</h3>
      <p class="empty-desc">快去挑选心仪的图书吧，好货不等人~</p>
      <div class="empty-actions">
        <el-button type="primary" round size="large" icon="el-icon-goods" @click="$router.push('/customer/home')">
          去逛逛
        </el-button>
        <el-button round size="large" icon="el-icon-present" @click="$router.push('/customer/charity')">
          公益专区
        </el-button>
      </div>
      <div class="empty-tips">
        <p><i class="el-icon-star-off"></i> 新人首单立减优惠进行中</p>
        <p><i class="el-icon-time"></i> 每日 10:00 / 20:00 限量秒杀</p>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/api'

// ==================== 防抖函数 ====================
function debounce(fn, delay = 500) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}

const CART_CACHE_KEY = 'shopping_cart_cache'
const CACHE_EXPIRE_MINUTES = 30

export default {
  name: 'ShoppingCart',
  data() {
    return {
      cartList: [],
      selectedIds: [],         // 已选中的 bookId 列表
      selectAll: false,
      deletingId: null,
      checkoutLoading: false,
      updatingQty: {},         // { [bookId]: boolean }
      // 防抖更新数量 —— 延迟 600ms 才真正发请求，避免频繁请求
      debouncedUpdate: debounce(function (item, newQty) {
        this.doUpdateQuantity(item, newQty)
      }, 600),
      pendingQtyMap: {}        // 本地暂存的数量 { bookId: quantity }，用于乐观更新回显
    }
  },
  computed: {
    selectedItems() {
      return this.cartList.filter(item => this.selectedIds.includes(item.bookId))
    },
    selectedTotal() {
      return this.selectedItems.reduce((sum, it) => sum + (it.sellingPrice || 0) * (it.quantity || 0), 0)
    }
  },
  created() {
    this.loadCart()
  },
  methods: {

    /* ========== 数据加载 & 缓存 ========== */

    async loadCart() {
      try {
        const res = await api.cart.list()
        const list = Array.isArray(res) ? res : []
        // 尝试从本地缓存恢复选中状态和数量
        this.restoreFromCache(list)
        this.cartList = list
      } catch (error) {
        console.error('加载购物车失败:', error)
        // 网络异常时尝试从纯本地缓存展示（降级体验）
        const cached = this.readCache()
        if (cached && cached.list && cached.list.length > 0) {
          this.cartList = cached.list
          this.selectedIds = cached.selectedIds || []
          this.$message.warning('网络异常，显示的是本地缓存数据')
        }
      }
    },

    /* ========== 本地缓存（防止刷新丢失） ========== */

    saveCache() {
      try {
        localStorage.setItem(CART_CACHE_KEY, JSON.stringify({
          list: this.cartList.map(it => ({
            bookId: it.bookId, cartId: it.cartId,
            bookName: it.bookName, coverImage: it.coverImage,
            author: it.author, sellingPrice: it.sellingPrice,
            stockCount: it.stockCount, quantity: it.quantity
          })),
          selectedIds: [...this.selectedIds],
          timestamp: Date.now()
        }))
      } catch (e) { /* ignore */ }
    },

    readCache() {
      try {
        const raw = localStorage.getItem(CART_CACHE_KEY)
        if (!raw) return null
        const data = JSON.parse(raw)
        // 超过 30 分钟的缓存视为过期
        if (Date.now() - data.timestamp > CACHE_EXPIRE_MINUTES * 60 * 1000) {
          localStorage.removeItem(CART_CACHE_KEY)
          return null
        }
        return data
      } catch (e) { return null }
    },

    clearCache() {
      try { localStorage.removeItem(CART_CACHE_KEY) } catch (e) { /* ignore */ }
    },

    restoreFromCache(serverList) {
      const cache = this.readCache()
      if (!cache) {
        // 无缓存时默认全选
        this.selectedIds = serverList.map(it => it.bookId)
        this.selectAll = true
        return
      }
      // 用缓存恢复数量（如果用户刚改了但还没同步）
      for (const sv of serverList) {
        const cachedItem = cache.list.find(c => c.bookId === sv.bookId)
        if (cachedItem && cachedItem.quantity !== undefined) {
          sv.quantity = cachedItem.quantity
        }
      }
      this.selectedIds = cache.selectedIds.filter(id => serverList.some(s => s.bookId === id))
      this.selectAll = this.selectedIds.length === serverList.length && serverList.length > 0
    },

    /* ========== 数量操作（带防抖 + 乐观更新） ========== */

    stepQuantity(item, delta) {
      const newVal = (item.quantity || 1) + delta
      const maxStock = item.stockCount || 99
      if (newVal < 1 || newVal > maxStock) return
      // 立即更新 UI（乐观）
      this.$set(this.updatingQty, item.bookId, true)
      this.$set(item, 'quantity', newVal)
      this.saveCache()
      // 防抖提交到后端
      this.debouncedUpdate(item, newVal)
    },

    onQtyInput(item, valStr) {
      const num = parseInt(valStr, 10)
      if (isNaN(num) || num < 1) {
        this.$set(item, 'quantity', 1)
        return
      }
      const maxStock = item.stockCount || 99
      const clamped = Math.min(num, maxStock)
      if (clamped !== num) this.$message.warning(`最多购买 ${maxStock} 本`)
      this.$set(this.updatingQty, item.bookId, true)
      this.$set(item, 'quantity', clamped)
      this.saveCache()
      this.debouncedUpdate(item, clamped)
    },

    async doUpdateQuantity(item, qty) {
      try {
        await api.cart.add(item.bookId, qty)
      } catch (error) {
        const msg = error?.response?.data?.message || error?.message || '更新失败'
        this.$message.error(msg)
        // 失败时重新拉取列表回退
        await this.loadCart()
      } finally {
        this.$set(this.updatingQty, item.bookId, false)
        this.saveCache()
      }
    },

    /* ========== 选择逻辑 ========== */

    toggleSelect(bookId, checked) {
      if (checked) {
        this.selectedIds.push(bookId)
      } else {
        this.selectedIds = this.selectedIds.filter(id => id !== bookId)
      }
      this.selectAll = this.selectedIds.length === this.cartList.length
    },

    handleSelectAll(checked) {
      this.selectedIds = checked ? this.cartList.map(it => it.bookId) : []
    },

    /* ========== 删除 ========== */

    handleRemove(item) {
      this.$confirm(
        `确定要删除《${item.bookName}》吗？`,
        '确认删除',
        { confirmButtonText: '删除', cancelButtonText: '再想想', type: 'warning' }
      ).then(async () => {
        this.deletingId = item.bookId
        try {
          await api.cart.remove(item.bookId)
          this.$message.success('已移除购物车')
          // 动画结束后再从数组移除（延迟 300ms 配合 CSS transition）
          setTimeout(() => {
            this.cartList = this.cartList.filter(it => it.bookId !== item.bookId)
            this.selectedIds = this.selectedIds.filter(id => id !== item.bookId)
            this.deletingId = null
            this.selectAll = this.selectedIds.length === this.cartList.length
            this.saveCache()
          }, 300)
        } catch (error) {
          this.deletingId = null
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    confirmClearCart() {
      if (this.selectedIds.length === 0) {
        this.$message.info('请先选择要清空的商品')
        return
      }
      const count = this.selectedIds.length
      this.$confirm(`确定要删除已选的 ${count} 件商品吗？`, '清空提示', {
        confirmButtonText: '确认清空',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 逐个删除（或调 clear 接口）
          for (const bid of this.selectedIds) {
            await api.cart.remove(bid)
          }
          this.$message.success(`已清空 ${count} 件商品`)
          this.loadCart()
          this.clearCache()
        } catch (e) {
          this.$message.error('部分商品删除失败，请刷新页面重试')
        }
      }).catch(() => {})
    },

    /* ========== 结算 ========== */

    async goToCheckout() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请先选择要结算的商品')
        return
      }
      // 校验库存
      const outOfStock = this.selectedItems.filter(it =>
        !it.stockCount || it.quantity > (it.stockCount || 0)
      )
      if (outOfStock.length > 0) {
        const names = outOfStock.map(it => `《${it.bookName}》`).join('、')
        this.$message.error(`${names} 库存不足，请调整数量`)
        return
      }

      this.checkoutLoading = true
      try {
        // 将选中商品信息传递给结算页（通过 query 或 store）
        this.$router.push({
          path: '/customer/checkout',
          query: { ids: this.selectedItems.map(it => it.bookId).join(',') }
        })
      } finally {
        this.checkoutLoading = false
      }
    },

    /* ========== 工具方法 ========== */

    getCoverUrl(coverPath) {
      if (!coverPath) return '/static/default-book.svg'
      if (coverPath.startsWith('http')) return coverPath
      return `http://localhost:8080/api${coverPath}`
    },

    formatPrice(val) {
      return Number(val || 0).toFixed(2)
    },

    calcSubtotal(item) {
      return ((item.sellingPrice || 0) * (item.quantity || 0)).toFixed(2)
    },

    stockClass(count) {
      if (!count || count === 0) return 'stock-none'
      if (count <= 5) return 'stock-low'
      return 'stock-ok'
    },

    goToDetail(bookId) {
      this.$router.push(`/customer/book/${bookId}`)
    },

    onImgError(e) {
      e.target.src = '/static/default-book.svg?t=' + Date.now()
    }
  },
  beforeDestroy() {
    this.saveCache()
  },
  watch: {
    cartList: {
      deep: true,
      handler() {
        this.saveCache()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
/* ==================== 变量 ==================== */
$primary: #c45a3b;
$primary-light: #e8735a;
$bg-page: #f8f6f3;
$border-color: #eeebe8;
$text-main: #333;
$text-muted: #999;
$radius-sm: 6px;
$radius-md: 10px;

/* ==================== 页面容器 ==================== */
.cart-page {
  min-height: calc(100vh - 60px);
  background: $bg-page;
  padding-bottom: 90px; // 为底部结算栏留空间
}

/* ========== 标题区 ========== */
.page-header {
  background: #fff;
  border-bottom: 1px solid $border-color;
  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px 30px;
    display: flex;
    align-items: center;
  }
  .page-title {
    font-size: 22px;
    font-weight: 700;
    color: $text-main;
    margin: 0;
    i { margin-right: 8px; color: $primary; }
  }
  .header-hint {
    margin-left: 16px;
    font-size: 13px;
    color: $text-muted;
    strong { color: $primary; }
  }
}

/* ========== 主内容表格区 ========== */
.cart-main {
  max-width: 1200px;
  margin: 20px auto 0;
  background: #fff;
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

/* 表头 */
.cart-table-head {
  display: flex;
  align-items: center;
  padding: 14px 24px;
  background: #faf9f7;
  border-bottom: 1px solid $border-color;
  font-size: 13px;
  color: $text-muted;
  font-weight: 600;
  letter-spacing: 0.3px;
}

/* 列宽分配 */
.col-check   { width: 50px; text-align: left; }
.col-info    { flex: 1; min-width: 280px; }
.col-price   { width: 110px; text-align: center; }
.col-qty     { width: 160px; text-align: center; }
.col-stock   { width: 80px; text-align: center; }
.col-subtotal{ width: 120px; text-align: right; }
.col-action  { width: 100px; text-align: center; }

/* ========== 单行商品 ========== */
.cart-body {
  .cart-item {
    display: flex;
    align-items: center;
    padding: 18px 24px;
    border-bottom: 1px solid $border-color;
    transition: all 0.35s ease;
    &:last-child { border-bottom: none; }
    &:hover {
      background: #fdfcfb;
    }

    /* 删除动画：向左滑出 + 透明度淡出 */
    &.is-deleting {
      transform: translateX(-40px);
      opacity: 0;
      pointer-events: none;
      height: 0 !important;
      padding: 0 24px;
      overflow: hidden;
    }
  }
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
}

.cover-wrap {
  flex-shrink: 0;
  width: 72px;
  height: 96px;
  border-radius: $radius-sm;
  overflow: hidden;
  border: 1px solid $border-color;
  transition: box-shadow 0.25s ease;
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  .prod-cover {
    width: 100%;
    height: 100%;
    object-fit: contain;
    background: #f5f4f2;
  }
}

.info-text {
  flex: 1;
  min-width: 0;
  .prod-name {
    font-size: 15px;
    font-weight: 600;
    color: $text-main;
    line-height: 1.4;
    margin: 0 0 4px;
    cursor: pointer;
    &:hover { color: $primary; text-decoration: underline; }
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  .prod-author {
    font-size: 12px;
    color: $text-muted;
    margin: 0;
  }
  .stock-tag-danger { margin-top: 6px; }
}

/* ---------- 价格列 ---------- */
.price-text {
  font-size: 15px;
  color: $text-main;
  font-weight: 500;
}

.subtotal-price {
  font-size: 16px;
  color: $primary;
  font-weight: 700;
}

/* ---------- 数量步进器 ---------- */
.qty-stepper {
  display: inline-flex;
  align-items: center;
  border: 1px solid $border-color;
  border-radius: $radius-sm;
  overflow: hidden;
  background: #fff;
  transition: border-color 0.2s;
  &:focus-within {
    border-color: $primary-light;
  }
  .stepper-btn {
    width: 32px;
    height: 34px;
    border: none;
    background: transparent;
    color: #666;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    transition: all 0.2s;
    &:not(:disabled):hover {
      background: $bg-page;
      color: $primary;
    }
    &:disabled { color: #ccc; cursor: not-allowed; }
  }
  .stepper-input {
    width: 42px;
    height: 34px;
    text-align: center;
    border: none;
    outline: none;
    font-size: 14px;
    font-weight: 600;
    color: $text-main;
    background: #fafafa;
    border-left: 1px solid $border-color;
    border-right: 1px solid $border-color;
    -moz-appearance: textfield;
    &::-webkit-inner-spin-button,
    &::-webkit-outer-spin-button { -webkit-appearance: none; }
  }
}

.syncing-hint {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: $primary-light;
  margin-top: 4px;
  animation: pulse-opacity 1s infinite;
}

@keyframes pulse-opacity {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.fade-tip-enter-active, .fade-tip-leave-active {
  transition: all 0.25s ease;
}
.fade-tip-enter, .fade-tip-leave-to { opacity: 0; transform: translateY(-4px); }

/* ---------- 库存状态 ---------- */
.stock-ok    { color: #67c23a; font-weight: 500; }
.stock-low   { color: #e6a23c; font-weight: 500; }
.stock-none { color: #f56c6c; font-weight: 500; }

/* ---------- 操作按钮 ---------- */
.action-btn {
  border: none;
  background: none;
  cursor: pointer;
  font-size: 13px;
  color: $text-muted;
  padding: 4px 10px;
  border-radius: $radius-sm;
  transition: all 0.2s;
  i { margin-right: 3px; }
  &:hover {
    background: #fef0f0;
    color: #f56c6c;
  }
}

/* ==================== 底部结算栏（固定底部） ==================== */
.cart-footer-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 1400px;
  background: #fff;
  border-top: 1px solid $border-color;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.06);
  z-index: 99;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 40px;
  border-radius: 0 0 $radius-md $radius-md;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 20px;
  .link-btn {
    border: none;
    background: none;
    color: $text-muted;
    cursor: pointer;
    font-size: 13px;
    &:hover { color: #f56c6c; }
  }
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.summary-row {
  text-align: right;
  .summary-label {
    font-size: 13px;
    color: $text-muted;
    em { font-style: normal; color: $primary; font-weight: 700; }
  }
  .summary-total {
    display: block;
    font-size: 14px;
    strong {
      font-size: 26px;
      color: $primary;
      letter-spacing: -0.5px;
    }
  }
}

.checkout-btn {
  min-width: 140px;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, $primary 0%, $primary-light 100%);
  border: none;
  box-shadow: 0 4px 14px rgba($primary, 0.35);
  transition: all 0.3s ease;
  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba($primary, 0.45);
  }
  &:active:not(:disabled) {
    transform: translateY(0);
  }
  &:disabled {
    background: #ddd;
    color: #bbb;
    box-shadow: none;
  }
}

/* ==================== TransitionGroup 动画 ==================== */
.cart-list-enter-active,
.cart-list-leave-active {
  transition: all 0.4s cubic-bezier(.55,0,.1,1);
}
.cart-list-enter {
  opacity: 0;
  transform: translateX(20px);
}
.cart-list-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
.cart-list-move {
  transition: transform 0.4s cubic-bezier(.55,0,.1,1);
}

/* ==================== 空购物车状态 ==================== */
.empty-cart-state {
  max-width: 520px;
  margin: 80px auto;
  text-align: center;
  padding: 48px 32px;
  background: #fff;
  border-radius: $radius-md;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);

  .empty-visual {
    position: relative;
    display: inline-block;
    margin-bottom: 24px;
  }
  .empty-cart-icon {
    width: 120px;
    height: 120px;
    line-height: 120px;
    border-radius: 50%;
    background: linear-gradient(135deg, #fef0ec 0%, #fce4dc 100%);
    display: inline-block;
    i {
      font-size: 52px;
      color: $primary;
      opacity: 0.65;
    }
  }
  .empty-title {
    font-size: 20px;
    font-weight: 700;
    color: $text-main;
    margin: 0 0 8px;
  }
  .empty-desc {
    font-size: 14px;
    color: $text-muted;
    margin: 0 0 28px;
  }
  .empty-actions {
    display: flex;
    justify-content: center;
    gap: 14px;
    margin-bottom: 28px;
  }
  .empty-tips {
    border-top: 1px dashed $border-color;
    padding-top: 18px;
    p {
      font-size: 12px;
      color: $text-muted;
      margin: 4px 0;
      i { margin-right: 4px; color: $primary-light; }
    }
  }
}

/* ==================== 响应式适配 ==================== */
@media screen and (max-width: 900px) {
  .col-stock, .col-price { display: none; }
  .cart-footer-bar { padding: 12px 20px; }
  .summary-row .summary-total strong { font-size: 20px; }
  .checkout-btn { min-width: 100px; font-size: 14px; }
}
@media screen and (max-width: 640px) {
  .page-header .header-content { padding: 14px 16px; }
  .cart-main { margin: 10px; }
  .cart-item { padding: 12px 12px; flex-wrap: wrap; gap: 8px; }
  .col-check, .col-subtotal, .col-action { width: 40px; }
  .col-info { min-width: 0; flex-basis: calc(100% - 200px); }
  .col-qty { width: 130px; }
  .cover-wrap { width: 56px; height: 74px; }
  .cart-footer-bar { flex-direction: column; gap: 10px; padding: 10px 16px; }
  .footer-right { justify-content: space-between; width: 100%; }
}
</style>
