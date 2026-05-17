<template>
  <div class="book-detail-page">
    <!-- 面包屑导航 -->
    <nav class="breadcrumb-nav">
      <div class="bc-inner">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/customer/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>图书详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </nav>

    <!-- 加载骨架屏 -->
    <div v-if="loading" class="detail-wrapper">
      <div class="skeleton-layout">
        <el-skeleton :rows="0" animated class="sk-cover">
          <template slot="template"><div class="sk-cover-box"></div></template>
        </el-skeleton>
        <el-skeleton :rows="8" animated class="sk-info" />
      </div>
    </div>

    <!-- 主内容 -->
    <main v-else-if="book" class="detail-wrapper">

      <!-- ========== 核心信息区（封面 + 详情） ========== -->
      <section class="core-section">
        <div class="core-inner">
          <!-- 左侧封面 -->
          <div class="cover-area">
            <div class="cover-frame" ref="coverFrame">
              <img
                ref="coverImg"
                :src="imageLoaded ? getCoverUrl(book.coverImage) : 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7'"
                :alt="book.bookName"
                class="book-cover-img"
                :style="{ opacity: imageLoaded ? 1 : 0 }"
                @error="onCoverError"
              />
              <!-- 封面占位 -->
              <div v-show="!imageLoaded" class="cover-placeholder">
                <i class="el-icon-picture-outline"></i>
                <span>加载中...</span>
              </div>
            </div>
            <!-- 封面底部操作 -->
            <div class="cover-actions">
              <button
                class="action-chip"
                :class="{ active: isFavorited }"
                @click.stop="toggleFavorite"
                :title="isFavorited ? '取消收藏' : '加入收藏'"
              >
                <i :class="isFavorited ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
                {{ isFavorited ? '已收藏' : '收藏' }}
              </button>
            </div>
          </div>

          <!-- 右侧信息 -->
          <div class="info-area">
            <!-- 书名 -->
            <h1 class="book-title">{{ book.bookName }}</h1>

            <!-- 评价摘要行 -->
            <div class="meta-row">
              <div class="rating-inline" @click="scrollToReview">
                <el-rate :value="avgRating" disabled show-score text-color="#ff9900"
                  score-template="{value}分" />
                <span class="review-link">{{ reviewCount }}条评价</span>
              </div>
              <span class="sales-text">已售 {{ book.salesVolume || 0 }} 本</span>
            </div>

            <!-- 价格区 -->
            <div class="price-block">
              <div class="price-main">
                <span class="currency">&yen;</span>
                <span class="amount">{{ book.sellingPrice || 0 }}</span>
                <span v-if="discountTag" class="discount-tag">{{ discountTag }}</span>
              </div>
              <span v-if="showOriginalPrice" class="price-original">
                &yen;{{ book.originalPrice }}
              </span>
            </div>

            <!-- 属性列表 -->
            <dl class="attr-list">
              <dt>作者</dt><dd>{{ book.author || '暂无' }}</dd>
              <dt v-if="book.editor">编者</dt><dd v-if="book.editor">{{ book.editor }}</dd>
              <dt>出版社</dt><dd>{{ book.publisher || '暂无' }}</dd>
              <dt v-if="book.publishDate">出版日期</dt><dd v-if="book.publishDate">{{ book.publishDate }}</dd>
              <dt v-if="book.edition">版次</dt><dd v-if="book.edition">{{ book.edition }}</dd>
              <dt v-if="book.isbn">ISBN</dt><dd v-if="book.isbn">{{ book.isbn }}</dd>
            </dl>

            <!-- 库存状态 -->
            <div class="stock-status" :class="stockStatusClass">
              <i :class="stockIcon"></i>
              <span>{{ stockText }}</span>
            </div>

            <!-- 数量选择 -->
            <div class="qty-row">
              <label>数量</label>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="maxQty"
                size="small"
                controls-position="right"
                :disabled="!hasStock"
              />
            </div>

            <!-- 操作按钮组 -->
            <div class="btn-group">
              <button
                class="primary-btn cart-btn"
                :class="{ disabled: !hasStock }"
                :disabled="!hasStock || addingCart"
                @click="handleAddToCart"
              >
                <i :class="addingCart ? 'el-icon-loading' : 'el-icon-shopping-cart-2'"></i>
                {{ addingCart ? '添加中...' : '加入购物车' }}
              </button>
              <button
                class="danger-btn buy-btn"
                :class="{ disabled: !hasStock }"
                :disabled="!hasStock"
                @click="handleBuyNow"
              >
                立即购买
              </button>
            </div>

            <!-- 服务保障 -->
            <div class="service-guarantee">
              <span class="guarantee-item"><i class="el-icon-check"></i> 正版保证</span>
              <span class="guarantee-item"><i class="el-icon-check"></i> 极速发货</span>
              <span class="guarantee-item"><i class="el-icon-check"></i> 7天退换</span>
            </div>
          </div>
        </div>
      </section>

      <!-- ========== Tab 内容区 ========== -->
      <section class="tab-section">
        <div class="tab-header-bar">
          <div
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >{{ tab.label }}</div>
        </div>

        <!-- 图书详情 Tab -->
        <div v-show="activeTab === 'detail'" class="tab-panel">
          <article class="description-card">
            <h3 class="panel-title">内容简介</h3>
            <div class="desc-body" v-html="formatDescription(book.description)"></div>
          </article>
        </div>

        <!-- 评价 Tab -->
        <div v-show="activeTab === 'review'" class="tab-panel">
          <ReviewPanel
            :reviews="reviews"
            :avg-rating="avgRating"
            :total-count="reviewCount"
            :book-id="book.id"
            @refresh="loadReviews"
            @open-dialog="openReviewDialog"
          />
        </div>

        <!-- 推荐图书 Tab -->
        <div v-show="activeTab === 'recommend'" class="tab-panel">
          <RecommendGrid
            :books="recommendBooks"
            :loading="recommendLoading"
            @view="goToDetail"
          />
        </div>
      </section>

    </main>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <i class="el-icon-warning-outline"></i>
      <p>图书信息加载失败</p>
      <el-button type="primary" @click="retryLoad">重新加载</el-button>
    </div>

    <!-- 评价对话框 -->
    <el-dialog
      title="发表评价"
      :visible.sync="reviewDialogVisible"
      width="560px"
      custom-class="review-dialog"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="90px" size="small">
        <el-form-item label="综合评分" prop="rating">
          <el-rate v-model="reviewForm.rating" show-text
            :texts="['很差','较差','一般','较好','很好']" />
        </el-form-item>
        <el-form-item label="书店评分">
          <el-rate v-model="reviewForm.storeRating" />
        </el-form-item>
        <el-form-item label="业务评分">
          <el-rate v-model="reviewForm.businessRating" />
        </el-form-item>
        <el-form-item label="服务评分">
          <el-rate v-model="reviewForm.serviceRating" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="分享您的阅读感受..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="匿名发布">
          <el-switch v-model="reviewForm.isAnonymous" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" size="small" :loading="submittingReview" @click="submitReview">
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api'
import ReviewPanel from './components/ReviewPanel.vue'
import RecommendGrid from './components/RecommendGrid.vue'

export default {
  name: 'BookDetail',
  components: { ReviewPanel, RecommendGrid },
  data() {
    return {
      // ---- 核心数据 ----
      book: null,
      quantity: 1,
      activeTab: 'detail',
      loading: true,

      // ---- 图片懒加载 ----
      imageLoaded: false,
      imageObserver: null,

      // ---- 收藏 ----
      isFavorited: false,

      // ---- 购物车 ----
      addingCart: false,

      // ---- 评价 ----
      reviews: [],
      reviewCount: 0,
      reviewDialogVisible: false,
      submittingReview: false,
      reviewForm: this.createEmptyReview(),
      reviewRules: {
        rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
        content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
      },

      // ---- 推荐 ----
      recommendBooks: [],
      recommendLoading: false,

      // ---- Tabs 定义 ----
      tabs: [
        { key: 'detail', label: '商品详情' },
        { key: 'review', label: '用户评价' },
        { key: 'recommend', label: '猜你喜欢' }
      ]
    }
  },
  computed: {
    hasStock() { return this.book && this.stockCount > 0 },
    stockCount() { return this.book?.stockCount || 0 },
    maxQty() { return Math.min(this.stockCount, 99) },
    stockStatusClass() {
      if (!this.hasStock) return 'out'
      if (this.stockCount <= 10) return 'low'
      return 'ok'
    },
    stockIcon() {
      if (!this.hasStock) return 'el-icon-close-circle-fill'
      if (this.stockCount <= 10) return 'el-icon-warning-outline'
      return 'el-icon-circle-check'
    },
    stockText() {
      if (!this.hasStock) return '暂时缺货'
      if (this.stockCount <= 10) return `仅剩 ${this.stockCount} 件`
      return `库存充足（${this.stockCount} 件）`
    },
    discountTag() {
      const d = this.book?.discount
      if (!d) return ''
      if (d > 1 && d <= 10) return d.toFixed(1) + '折'
      if (d > 0 && d < 1) return (d * 10).toFixed(1) + '折'
      return ''
    },
    showOriginalPrice() {
      const p = this.book?.originalPrice
      return p && p > (this.book?.sellingPrice || 0)
    },
    avgRating() {
      if (!this.reviews.length) return 0
      const sum = this.reviews.reduce((s, r) => s + (r.rating || 0), 0)
      return Number((sum / this.reviews.length).toFixed(1))
    }
  },
  created() {
    this.loadBook()
  },
  beforeDestroy() {
    if (this.imageObserver) this.imageObserver.disconnect()
  },
  methods: {
    /* ====== 数据加载 ====== */
    async loadBook() {
      this.loading = true
      const id = this.$route.params.id
      try {
        const [bookData] = await Promise.all([
          api.book.getById(id),
          this.loadRecommendBooks()
        ])
        this.book = bookData
        this.loadReviews()
        this.$nextTick(this.initLazyLoad)
      } catch (e) {
        console.error('加载图书失败:', e)
        this.$message.error('图书信息加载失败')
      } finally {
        this.loading = false
      }
    },
    retryLoad() { this.loadBook() },

    async loadReviews() {
      if (!this.book?.id) return
      try {
        const list = await api.review.getByBookId(this.book.id)
        this.reviews = Array.isArray(list) ? list : []
        this.reviewCount = this.reviews.length
      } catch (e) {
        console.error('加载评价失败:', e)
        this.reviews = []
        this.reviewCount = 0
      }
    },
    async loadRecommendBooks() {
      this.recommendLoading = true
      try {
        this.recommendBooks = await api.book.getHot(6) || []
      } catch (e) {
        this.recommendBooks = []
      } finally {
        this.recommendLoading = false
      }
    },

    /* ====== 图片懒加载 ====== */
    initLazyLoad() {
      const imgEl = this.$refs.coverImg
      if (!imgEl || !('IntersectionObserver' in window)) {
        this.imageLoaded = true
        return
      }
      this.imageObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.imageLoaded = true
            this.imageObserver.unobserve(imgEl)
          }
        })
      }, { rootMargin: '100px' })
      this.imageObserver.observe(imgEl)
    },
    onCoverError(e) {
      e.target.src = '/static/default-book.svg?t=' + Date.now()
    },

    /* ====== 工具方法 ====== */
    getCoverUrl(coverImage) {
      if (!coverImage) return ''
      if (coverImage.startsWith('http')) return coverImage
      return `http://localhost:8080/api${coverImage}`
    },
    formatDescription(desc) {
      if (!desc) return '<p class="empty-desc">暂无内容简介</p>'
      return desc.replace(/\n/g, '<br/>')
    },
    createEmptyReview() {
      return { rating: 5, storeRating: 5, businessRating: 5, serviceRating: 5, content: '', isAnonymous: 0 }
    },

    /* ====== 交互操作 ====== */
    toggleFavorite() {
      this.isFavorited = !this.isFavorited
      this.$message.success(this.isFavorited ? '已添加到收藏' : '已取消收藏')
    },
    async handleAddToCart() {
      if (!this.$store.state.token) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      if (!this.hasStock) return
      this.addingCart = true
      try {
        await api.cart.add(this.book.id, this.quantity)
        this.$message.success(`《${this.book.bookName}》已加入购物车`)
      } catch (e) {
        this.$message.error(e.response?.data?.message || '加入购物车失败')
      } finally {
        setTimeout(() => { this.addingCart = false }, 500)
      }
    },
    handleBuyNow() {
      if (!this.$store.state.token) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      this.$router.push({
        path: '/customer/checkout',
        query: { bookId: this.book.id, quantity: this.quantity }
      })
    },
    goToDetail(id) {
      if (id === this.book.id) return
      this.$router.push(`/customer/book/${id}`)
      this.loadBook()
    },
    scrollToReview() {
      this.activeTab = 'review'
    },
    openReviewDialog() {
      if (!this.$store.state.token) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      this.reviewForm = this.createEmptyReview()
      this.reviewDialogVisible = true
    },
    async submitReview() {
      this.$refs.reviewFormRef.validate(async valid => {
        if (!valid) return
        this.submittingReview = true
        try {
          const user = this.$store.state.userInfo
          await api.review.add({
            ...this.reviewForm,
            bookId: this.book.id,
            memberId: user.id,
            customerName: user.username
          })
          this.$message.success('评价已提交，等待审核后显示')
          this.reviewDialogVisible = false
          this.loadReviews()
        } catch (e) {
          this.$message.error('提交失败，请重试')
        } finally {
          this.submittingReview = false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
/* ==================== 变量 ==================== */
$primary: #c45a3b;
$primary-light: #e87355;
$danger: #e74c3c;
$success: #27ae60;
$warning: #f39c12;
$text-1: #2d3436;
$text-2: #636e72;
$text-3: #b2bec3;
$bg: #f8f6f3;
$border: #eee8e4;
$radius: 12px;

/* ==================== 页面容器 ==================== */
.book-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 60px;
}

/* 面包屑 */
.breadcrumb-nav {
  background: #fff;
  border-radius: $radius $radius 0 0;
  .bc-inner {
    max-width: 1160px;
    margin: 0 auto;
    padding: 14px 20px;
  }
}

/* 统一包装器 */
.detail-wrapper {
  background: #fff;
  padding: 30px;
  border-radius: 0 0 $radius $radius;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

/* ==================== 骨架屏 ==================== */
.skeleton-layout {
  display: flex;
  gap: 40px;
}
.sk-cover { width: 380px; flex-shrink: 0; }
.sk-cover-box { width: 340px; height: 450px; border-radius: $radius; background: #f0eeeb; }
.sk-info { flex: 1; }

/* ==================== 核心信息区 ==================== */
.core-section {
  margin-bottom: 32px;
}
.core-inner {
  display: flex;
  gap: 48px;

  @media screen and (max-width: 800px) {
    flex-direction: column;
    gap: 28px;
  }
}

/* --- 封面区域 --- */
.cover-area {
  width: 360px;
  flex-shrink: 0;
  @media screen and (max-width: 800px) {
    width: 280px;
    margin: 0 auto;
  }

  .cover-frame {
    position: relative;
    width: 100%;
    aspect-ratio: 3 / 4;
    background: linear-gradient(145deg, #fafafa, #f0ede9);
    border-radius: $radius;
    overflow: hidden;
    box-shadow: 0 8px 30px rgba(0,0,0,0.08);
    display: flex;
    align-items: center;
    justify-content: center;

    .book-cover-img {
      width: 88%;
      height: 92%;
      object-fit: contain;
      transition: opacity 0.4s ease, transform 0.5s cubic-bezier(.22,1,.36,1);
    }
    &:hover .book-cover-img {
      transform: scale(1.04);
    }

    .cover-placeholder {
      position: absolute;
      inset: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: $text-3;
      i { font-size: 48px; margin-bottom: 12px; opacity: 0.5; }
      span { font-size: 13px; }
    }
  }

  .cover-actions {
    margin-top: 16px;
    display: flex;
    justify-content: center;
    .action-chip {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 8px 24px;
      border: 1.5px solid $border;
      border-radius: 24px;
      font-size: 14px;
      color: $text-2;
      cursor: pointer;
      transition: all 0.25s ease;
      background: #fff;
      i { font-size: 18px; }
      &:hover { border-color: $warning; color: $warning; }
      &.active {
        border-color: $warning;
        background: #fef8ef;
        color: $warning;
        i { animation: heartBeat 0.4s; }
      }
    }
  }
}

@keyframes heartBeat {
  0%,100% { transform: scale(1); }
  50% { transform: scale(1.25); }
}

/* --- 信息区域 --- */
.info-area {
  flex: 1;
  min-width: 0;

  .book-title {
    font-size: 26px;
    font-weight: 700;
    color: $text-1;
    line-height: 1.35;
    margin: 0 0 12px;
    word-break: break-word;
  }

  .meta-row {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 18px;
    flex-wrap: wrap;
    .rating-inline {
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      ::v-deep .el-rate__icon { font-size: 17px; }
      ::v-deep .el-rate__text { font-size: 14px; }
    }
    .review-link {
      font-size: 13px;
      color: $primary;
      cursor: pointer;
      &:hover { text-decoration: underline; }
    }
    .sales-text {
      font-size: 13px;
      color: $text-3;
    }
  }

  /* 价格块 */
  .price-block {
    background: linear-gradient(135deg, #fff5f2 0%, #ffece8 100%);
    padding: 18px 22px;
    border-radius: $radius;
    margin-bottom: 20px;
    border: 1px solid #fce4dc;
    display: flex;
    align-items: baseline;
    gap: 12px;
    flex-wrap: wrap;

    .price-main {
      display: flex;
      align-items: baseline;
      .currency { font-size: 20px; color: $danger; font-weight: 700; }
      .amount { font-size: 38px; color: $danger; font-weight: 800; letter-spacing: -1px; }
      .discount-tag {
        margin-left: 8px;
        background: linear-gradient(135deg, $danger, #c0392b);
        color: #fff;
        font-size: 12px;
        font-weight: 700;
        padding: 2px 10px;
        border-radius: 4px;
        vertical-align: super;
      }
    }
    .price-original {
      font-size: 15px;
      color: $text-3;
      text-decoration: line-through;
    }
  }

  /* 属性表 */
  .attr-list {
    display: grid;
    grid-template-columns: auto 1fr;
    row-gap: 10px;
    column-gap: 16px;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #fafafa;
    border-radius: 10px;
    dt {
      color: $text-3;
      font-size: 13px;
      white-space: nowrap;
      &::after { content: '：'; }
    }
    dd {
      font-size: 14px;
      color: $text-1;
      margin: 0;
    }
  }

  /* 库存 */
  .stock-status {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    padding: 6px 14px;
    border-radius: 20px;
    margin-bottom: 16px;
    i { font-size: 16px; }
    &.ok   { background: #edfdf1; color: $success; }
    &.low  { background: #fef8e6; color: $warning; }
    &.out  { background: #fdeeed; color: $danger; }
  }

  /* 数量 */
  .qty-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
    label { font-size: 14px; color: $text-2; font-weight: 500; }
  }

  /* 按钮组 */
  .btn-group {
    display: flex;
    gap: 14px;
    margin-bottom: 20px;
    flex-wrap: wrap;

    .primary-btn, .danger-btn {
      flex: 1;
      min-width: 160px;
      height: 46px;
      border: none;
      border-radius: 24px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      transition: all 0.28s cubic-bezier(.25,.46,.45,.94);

      i { font-size: 18px; }
    }
    .cart-btn {
      background: linear-gradient(135deg, $primary-light, $primary);
      color: #fff;
      &:hover:not(.disabled) {
        box-shadow: 0 6px 20px rgba($primary, 0.4);
        transform: translateY(-2px);
      }
      &.disabled {
        background: #ccc;
        cursor: not-allowed;
      }
    }
    .buy-btn {
      background: linear-gradient(135deg, $danger, #c0392b);
      color: #fff;
      &:hover:not(.disabled) {
        box-shadow: 0 6px 20px rgba($danger, 0.4);
        transform: translateY(-2px);
      }
      &.disabled {
        background: #ccc;
        cursor: not-allowed;
      }
    }
  }

  /* 服务保障 */
  .service-guarantee {
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
    .guarantee-item {
      font-size: 12px;
      color: $text-2;
      display: flex;
      align-items: center;
      gap: 4px;
      i { color: $success; font-size: 14px; }
    }
  }
}

/* ==================== Tab 区域 ==================== */
.tab-section {
  border-top: 1px solid $border;
}
.tab-header-bar {
  display: flex;
  gap: 0;
  border-bottom: 2px solid $border;
  .tab-item {
    padding: 14px 28px;
    font-size: 15px;
    color: $text-2;
    cursor: pointer;
    position: relative;
    transition: all 0.2s;
    font-weight: 500;
    &:hover { color: $primary; }
    &.active {
      color: $primary;
      font-weight: 700;
      &::after {
        content: '';
        position: absolute;
        bottom: -2px;
        left: 20%;
        right: 20%;
        height: 3px;
        background: $primary;
        border-radius: 2px;
      }
    }
  }
}
.tab-panel {
  padding: 28px 0 0;
}

/* --- 商品详情面板 --- */
.description-card {
  .panel-title {
    font-size: 18px;
    font-weight: 600;
    color: $text-1;
    margin: 0 0 16px;
    padding-left: 12px;
    border-left: 3px solid $primary;
  }
  .desc-body {
    font-size: 15px;
    line-height: 1.85;
    color: $text-2;
    padding: 20px 24px;
    background: #fafafa;
    border-radius: 10px;
    .empty-desc { color: $text-3; text-align: center; padding: 30px 0; }
  }
}

/* --- 错误状态 --- */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: $text-3;
  i { font-size: 56px; margin-bottom: 16px; }
  p { font-size: 16px; margin-bottom: 20px; }
}

/* ==================== 响应式 ==================== */
@media screen and (max-width: 768px) {
  .detail-wrapper { padding: 16px; }
  .info-area {
    .book-title { font-size: 21px; }
    .price-block .price-main .amount { font-size: 30px; }
    .attr-list { grid-template-columns: 70px 1fr; }
    .btn-group { flex-direction: row; .primary-btn, .danger-btn { min-width: 0; flex: 1; } }
    .service-guarantee { gap: 12px; }
  }
  .tab-header-bar { overflow-x: auto;
    .tab-item { white-space: nowrap; padding: 12px 18px; font-size: 14px; }
  }
}
</style>
