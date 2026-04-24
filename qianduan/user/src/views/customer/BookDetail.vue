<template>
  <div class="book-detail">
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/customer/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>图书详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div v-if="book" class="detail-content">
      <el-row :gutter="30">
        <el-col :span="8">
          <div class="book-cover">
            <img :src="getCoverUrl(book.coverImage)" :alt="book.bookName" />
          </div>
        </el-col>
        <el-col :span="16">
          <div class="book-info">
            <h1 class="book-title">{{ book.bookName }}</h1>
            <div class="book-meta">
              <span class="sales-count">已售 {{ book.salesVolume || 0 }} 本</span>
            </div>
            <div class="book-price">
              <span class="current-price">¥{{ book.sellingPrice }}</span>
              <span v-if="book.originalPrice && book.originalPrice > book.sellingPrice" class="original-price">¥{{ book.originalPrice }}</span>
              <el-tag v-if="bookDiscountTag" type="danger" size="small">{{ bookDiscountTag }}</el-tag>
            </div>
            <div class="book-attrs">
              <div class="attr-row"><span class="attr-label">作者</span><span class="attr-value">{{ book.author || '暂无' }}</span></div>
              <div v-if="book.editor" class="attr-row"><span class="attr-label">编者</span><span class="attr-value">{{ book.editor }}</span></div>
              <div class="attr-row"><span class="attr-label">出版社</span><span class="attr-value">{{ book.publisher || '暂无' }}</span></div>
              <div v-if="book.publishDate" class="attr-row"><span class="attr-label">出版日期</span><span class="attr-value">{{ book.publishDate }}</span></div>
              <div v-if="book.edition" class="attr-row"><span class="attr-label">版次</span><span class="attr-value">{{ book.edition }}</span></div>
              <div v-if="book.isbn" class="attr-row"><span class="attr-label">ISBN</span><span class="attr-value">{{ book.isbn }}</span></div>
            </div>
            <div class="book-actions">
              <div class="stock-info">
                <span v-if="book.stockCount > 0" class="stock-available">库存：{{ book.stockCount }} 本</span>
                <span v-else class="stock-out">暂无库存</span>
              </div>
              <el-input-number v-model="quantity" :min="1" :max="book.stockCount || 1" size="medium" :disabled="!book.stockCount" />
              <el-button type="primary" icon="el-icon-shopping-cart-2" @click="addToCart" :disabled="!book.stockCount">加入购物车</el-button>
              <el-button type="danger" icon="el-icon-goods" @click="buyNow" :disabled="!book.stockCount">立即购买</el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-tabs v-model="activeTab" style="margin-top: 30px;">
        <el-tab-pane label="图书详情" name="detail">
          <div class="book-description">
            <h3>内容简介</h3>
            <p>{{ book.description || '暂无简介' }}</p>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="'评价(' + reviewCount + ')'" name="review">
          <div class="review-section">
            <div class="review-list">
              <div v-for="review in reviews" :key="review.reviewId" class="review-item">
                <div class="review-header">
                  <span class="reviewer-name">{{ review.isAnonymous ? '匿名用户' : review.customerName }}</span>
                  <el-rate v-model="review.rating" disabled size="small" />
                  <span class="review-time">{{ review.createTime }}</span>
                </div>
                <div class="review-body">{{ review.content }}</div>
                <div v-if="review.replyContent" class="review-reply">
                  <span class="reply-tag">商家回复：</span>{{ review.replyContent }}
                </div>
                <div class="review-ratings" v-if="review.storeRating || review.businessRating || review.serviceRating">
                  <el-tag size="mini" type="info">书店评价: {{ review.storeRating }}分</el-tag>
                  <el-tag size="mini" type="info">业务评价: {{ review.businessRating }}分</el-tag>
                  <el-tag size="mini" type="info">服务评价: {{ review.serviceRating }}分</el-tag>
                </div>
              </div>
              <el-empty v-if="reviews.length === 0" description="暂无评价" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <div v-else class="loading">
      <el-skeleton :rows="10" animated />
    </div>

    <el-dialog title="发表评价" :visible.sync="reviewDialogVisible" width="600px">
      <el-form ref="reviewForm" :model="reviewForm" :rules="reviewRules" label-width="100px">
        <el-form-item label="图书评分" prop="rating">
          <el-rate v-model="reviewForm.rating" show-text :texts="['很差','较差','一般','较好','很好']" />
        </el-form-item>
        <el-form-item label="书店评价">
          <el-rate v-model="reviewForm.storeRating" />
        </el-form-item>
        <el-form-item label="业务评价">
          <el-rate v-model="reviewForm.businessRating" />
        </el-form-item>
        <el-form-item label="服务评价">
          <el-rate v-model="reviewForm.serviceRating" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
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

export default {
  name: 'BookDetail',
  data() {
    return {
      book: null,
      quantity: 1,
      activeTab: 'detail',
      reviews: [],
      reviewCount: 0,
      reviewDialogVisible: false,
      reviewForm: {
        rating: 5,
        storeRating: 5,
        businessRating: 5,
        serviceRating: 5,
        content: '',
        isAnonymous: 0
      },
      reviewRules: {
        rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
        content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    bookDiscountTag() {
      if (!this.book || !this.book.discount) return ''
      const d = this.book.discount
      // 兼容两种格式：0-1（如0.8）或 1-10（如8）
      if (d > 1 && d <= 10) return d.toFixed(1) + '折'
      if (d > 0 && d < 1) return (d * 10).toFixed(1) + '折'
      // 等于1（无折扣/原价）不显示
      return ''
    }
  },
  created() {
    this.loadBook()
  },
  methods: {
    async loadBook() {
      const id = this.$route.params.id
      try {
        this.book = await api.book.getById(id)
        this.loadReviews()
      } catch (e) {
        this.$message.error('加载图书信息失败')
      }
    },
    async loadReviews() {
      if (!this.book) return
      try {
        this.reviews = await api.review.getByBookId(this.book.id)
        this.reviewCount = this.reviews.length
      } catch (e) {
        console.error('加载评价失败', e)
      }
    },
    getCoverUrl(coverImage) {
      if (!coverImage) return ''
      if (coverImage.startsWith('http')) return coverImage
      return `http://localhost:8080/api${coverImage}`
    },
    async addToCart() {
      const user = this.$store.getters.user
      if (!user) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      if (!this.book.stockCount) {
        this.$message.error('该商品暂无库存，无法加入购物车')
        return
      }
      try {
        await api.cart.add(this.book.id, this.quantity)
        this.$message.success('已加入购物车')
      } catch (e) {
        const msg = e.response?.data?.message || e.message || '加入购物车失败'
        this.$message.error(msg)
      }
    },
    buyNow() {
      const user = this.$store.getters.user
      if (!user) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      this.$router.push({ path: '/customer/checkout', query: { bookId: this.book.id, quantity: this.quantity } })
    },
    openReviewDialog() {
      this.reviewDialogVisible = true
      this.reviewForm = {
        rating: 5,
        storeRating: 5,
        businessRating: 5,
        serviceRating: 5,
        content: '',
        isAnonymous: 0
      }
    },
    async submitReview() {
      this.$refs.reviewForm.validate(async (valid) => {
        if (!valid) return
        const user = this.$store.getters.user
        if (!user) {
          this.$message.warning('请先登录')
          return
        }
        try {
          await api.review.add({
            ...this.reviewForm,
            bookId: this.book.id,
            memberId: user.id,
            customerName: user.username
          })
          this.$message.success('评价提交成功，等待审核')
          this.reviewDialogVisible = false
          this.loadReviews()
        } catch (e) {
          this.$message.error('评价提交失败')
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.book-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .breadcrumb {
    margin-bottom: 20px;
  }

  .book-cover {
    img {
      width: 100%;
      max-height: 450px;
      object-fit: contain;
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }
  }

  .book-info {
    .book-title {
      font-size: 24px;
      margin: 0 0 15px;
      color: #303133;
    }

    .book-meta {
      display: flex;
      align-items: center;
      gap: 15px;
      margin-bottom: 15px;

      .sales-count {
        color: #909399;
        font-size: 14px;
      }
    }

    .book-price {
      background: #fef0f0;
      padding: 15px 20px;
      border-radius: 8px;
      margin-bottom: 20px;

      .current-price {
        font-size: 28px;
        color: #f56c6c;
        font-weight: bold;
      }

      .original-price {
        font-size: 16px;
        color: #909399;
        text-decoration: line-through;
        margin-left: 10px;
      }

      .el-tag {
        margin-left: 10px;
      }
    }

    .book-attrs {
      margin-bottom: 20px;

      .attr-row {
        display: flex;
        padding: 8px 0;
        border-bottom: 1px dashed #ebeef5;

        .attr-label {
          width: 80px;
          color: #909399;
          flex-shrink: 0;
        }

        .attr-value {
          color: #303133;
        }
      }
    }

    .book-actions {
      display: flex;
      align-items: center;
      gap: 15px;
      margin-top: 20px;

      .stock-info {
        width: 100%;

        .stock-available {
          color: #67c23a;
          font-size: 14px;
          font-weight: 500;
        }

        .stock-out {
          color: #f56c6c;
          font-size: 14px;
          font-weight: 500;
        }
      }
    }
  }

  .book-description {
    h3 {
      margin-bottom: 15px;
    }
    p {
      line-height: 1.8;
      color: #606266;
    }
  }

  .review-section {
    .review-summary {
      background: #f5f7fa;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 20px;

      .rating-overview {
        text-align: center;

        .avg-score {
          font-size: 48px;
          font-weight: bold;
          color: #ff9900;
        }

        .review-count {
          color: #909399;
          margin-top: 5px;
        }
      }
    }

    .review-list {
      .review-item {
        padding: 15px 0;
        border-bottom: 1px solid #ebeef5;

        .review-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 10px;

          .reviewer-name {
            font-weight: bold;
            color: #303133;
          }

          .review-time {
            color: #909399;
            font-size: 12px;
          }
        }

        .review-body {
          color: #606266;
          line-height: 1.6;
          margin-bottom: 8px;
        }

        .review-reply {
          background: #f0f9eb;
          padding: 10px;
          border-radius: 4px;
          margin-top: 8px;
          font-size: 14px;

          .reply-tag {
            color: #67c23a;
            font-weight: bold;
          }
        }

        .review-ratings {
          display: flex;
          gap: 8px;
          margin-top: 8px;
        }
      }
    }
  }
}
</style>
