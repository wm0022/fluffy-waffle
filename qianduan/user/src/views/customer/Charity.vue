<template>
  <div class="charity-page">
    <div class="page-header">
      <h2><i class="el-icon-s-promotion"></i> 公益专区</h2>
      <p>每一本捐赠的书籍，都承载着爱心与希望</p>
    </div>

    <div class="filter-bar">
      <el-input v-model="searchKeyword" placeholder="搜索图书名称" size="small" style="width: 250px;" clearable prefix-icon="el-icon-search" @input="filterBooks" />
    </div>

    <div v-loading="loading" class="book-grid">
      <div v-for="book in filteredBooks" :key="book.id" class="book-card" @click="goToDetail(book.id)">
        <div class="book-cover">
          <img :src="getCoverUrl(book.coverImage)" :alt="book.bookName" />
          <div class="charity-badge">公益</div>
        </div>
        <div class="book-info">
          <h3 class="book-name">{{ book.bookName }}</h3>
          <p class="book-author">{{ book.author }}</p>
          <div class="book-bottom">
            <span class="book-price">¥{{ book.sellingPrice }}</span>
            <div class="book-actions" @click.stop>
              <el-button type="primary" size="mini" icon="el-icon-shopping-cart-2" @click="addToCart(book)">加入购物车</el-button>
              <el-button type="danger" size="mini" @click="buyNow(book)">立即购买</el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="filteredBooks.length === 0 && !loading" description="暂无公益图书" />
    </div>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'Charity',
  data() {
    return {
      books: [],
      searchKeyword: '',
      loading: false
    }
  },
  computed: {
    filteredBooks() {
      if (!this.searchKeyword) return this.books
      const kw = this.searchKeyword.toLowerCase()
      return this.books.filter(b =>
        (b.bookName && b.bookName.toLowerCase().includes(kw)) ||
        (b.author && b.author.toLowerCase().includes(kw))
      )
    }
  },
  created() {
    this.loadBooks()
  },
  methods: {
    async loadBooks() {
      this.loading = true
      try {
        const res = await api.book.pageList({ pageNum: 1, pageSize: 100, isDonation: 1, shelfStatus: 1 })
        this.books = (res.records || []).filter(b => b.isDonation === 1)
      } catch (e) {
        this.$message.error('加载公益图书失败')
      } finally {
        this.loading = false
      }
    },
    filterBooks() {},
    getCoverUrl(coverImage) {
      if (!coverImage) return ''
      if (coverImage.startsWith('http')) return coverImage
      return `http://localhost:8080/api${coverImage}`
    },
    goToDetail(id) {
      this.$router.push(`/customer/book/${id}`)
    },
    async addToCart(book) {
      if (!this.$store.state.token) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      try {
        await api.cart.add(this.$store.state.userInfo.id, book.id, 1)
        this.$message.success('已加入购物车')
      } catch (e) {
        this.$message.error('加入购物车失败')
      }
    },
    buyNow(book) {
      if (!this.$store.state.token) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      this.$router.push({ path: '/customer/checkout', query: { bookId: book.id, quantity: 1 } })
    }
  }
}
</script>

<style lang="scss" scoped>
.charity-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .page-header {
    text-align: center;
    margin-bottom: 30px;

    h2 {
      color: #e6a23c;
      font-size: 28px;
      i { margin-right: 10px; }
    }
    p { color: #909399; margin-top: 8px; }
  }

  .filter-bar {
    margin-bottom: 20px;
  }

  .book-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    min-height: 200px;

    .book-card {
      background: #fff;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);
      cursor: pointer;
      transition: transform 0.3s;

      &:hover { transform: translateY(-3px); }

      .book-cover {
        position: relative;
        height: 220px;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: contain;
          background: #f5f7fa;
        }

        .charity-badge {
          position: absolute;
          top: 10px;
          left: 10px;
          background: #e6a23c;
          color: #fff;
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 12px;
        }
      }

      .book-info {
        padding: 12px;

        .book-name {
          font-size: 14px;
          margin: 0 0 6px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .book-author {
          font-size: 12px;
          color: #909399;
          margin: 0 0 10px;
        }

        .book-bottom {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .book-price {
            color: #f56c6c;
            font-size: 18px;
            font-weight: bold;
          }

          .book-actions {
            display: flex;
            gap: 5px;
          }
        }
      }
    }
  }
}
</style>
