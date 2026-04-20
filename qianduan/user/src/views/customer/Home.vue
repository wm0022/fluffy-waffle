<template>
  <div class="customer-home">
    <div class="main-content">
      <el-container>
        <el-aside width="200px" class="category-sidebar">
          <el-menu :default-active="activeCategory" @select="handleCategoryChange">
            <el-menu-item index="all">
              <i class="el-icon-menu"></i>
              <span>全部图书</span>
            </el-menu-item>
            <el-menu-item index="hot">
              <i class="el-icon-s-finance"></i>
              <span>热销推荐</span>
            </el-menu-item>
            <el-menu-item index="new">
              <i class="el-icon-time"></i>
              <span>新品上架</span>
            </el-menu-item>
            <el-menu-item index="charity">
              <i class="el-icon-s-promotion"></i>
              <span>公益专区</span>
            </el-menu-item>
            <el-menu-item index="member">
              <i class="el-icon-user"></i>
              <span>会员服务</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="book-list">
          <div class="filter-bar" v-if="activeCategory === 'all'">
            <span>排序：</span>
            <el-radio-group v-model="sortBy" size="small" @change="handleSortChange">
              <el-radio-button label="default">默认</el-radio-button>
              <el-radio-button label="sales">销量</el-radio-button>
              <el-radio-button label="price">价格</el-radio-button>
              <el-radio-button label="time">时间</el-radio-button>
            </el-radio-group>
          </div>

          <el-row :gutter="20" class="book-grid">
            <el-col :span="6" v-for="book in bookList" :key="book.id">
              <el-card class="book-card" shadow="hover" @click.native="goToDetail(book.id)">
                <div class="book-cover">
                  <img :src="getBookCover(book.coverImage)" alt="封面" @error="handleImageError" />
                </div>
                <div class="book-info">
                  <div class="book-title">{{ book.bookName }}</div>
                  <div class="book-author">{{ book.author }}</div>
                  <div class="book-publisher">{{ book.publisher || '未知出版社' }}</div>
                  <div class="book-price">
                    <span class="current-price">¥{{ book.sellingPrice || 0 }}</span>
                    <span class="sales">已售 {{ book.salesVolume || 0 }}</span>
                  </div>
                  <div class="book-stock">库存：{{ book.stockCount || 0 }}</div>
                  <div class="book-actions">
                    <el-button type="primary" size="small" @click.stop="addToCart(book)">
                      加入购物车
                    </el-button>
                    <el-button type="success" size="small" @click.stop="buyNow(book)">
                      直接购买
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <div class="pagination" v-if="activeCategory === 'all' || activeCategory === 'donation'">
            <el-pagination
              :current-page="pageNum"
              :page-size="pageSize"
              :total="total"
              layout="total, prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script>
import api from '@/api'
import { mapGetters } from 'vuex'

export default {
  name: 'CustomerHome',
  data() {
    return {
      activeCategory: 'all',
      sortBy: 'default',
      bookList: [],
      pageNum: 1,
      pageSize: 8,
      total: 0
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    userId() {
      return this.userInfo.id || 1
    },
    searchKeyword() {
      return this.$route.query.keyword || ''
    }
  },
  created() {
    if (this.$route.query.tab) {
      this.activeCategory = this.$route.query.tab
    }
    this.getBookList()
  },
  watch: {
    '$route.query': {
      handler(newQuery) {
        if (newQuery.tab) {
          this.activeCategory = newQuery.tab
        }
        this.pageNum = 1
        this.getBookList()
      },
      immediate: false
    }
  },
  methods: {
    async getBookList() {
      try {
        if (this.activeCategory === 'hot') {
          const res = await api.book.getHot(4)
          this.bookList = res || []
          this.total = this.bookList.length
        } else if (this.activeCategory === 'new') {
          const res = await api.book.getNew(4)
          this.bookList = res || []
          this.total = this.bookList.length
        } else if (this.activeCategory === 'charity') {
          const res = await api.book.pageList({
            pageNum: this.pageNum,
            pageSize: this.pageSize,
            shelfStatus: 1,
            isDonation: 1
          })
          this.bookList = res.records || []
          this.total = res.total || 0
        } else {
          const params = {
            pageNum: this.pageNum,
            pageSize: this.pageSize,
            shelfStatus: 1,
            isDonation: 0,
            sortBy: this.sortBy === 'default' ? '' : this.sortBy
          }

          if (this.searchKeyword && this.searchKeyword.trim()) {
            params.bookName = this.searchKeyword.trim()
            params.author = this.searchKeyword.trim()
            params.publisher = this.searchKeyword.trim()
          }

          const res = await api.book.pageList(params)
          this.bookList = res.records || []
          this.total = res.total || 0
        }
      } catch (error) {
        console.error('获取图书列表失败:', error)
        this.bookList = []
        this.total = 0
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.getBookList()
    },
    handleCategoryChange(category) {
      if (category === 'member') {
        this.$router.push('/customer/member')
        return
      }
      if (category === 'charity') {
        this.activeCategory = 'charity'
        this.pageNum = 1
        this.getBookList()
        return
      }
      this.activeCategory = category
      this.pageNum = 1
      this.$router.replace({ query: { tab: category } })
      this.getBookList()
    },
    handleSortChange() {
      this.pageNum = 1
      this.getBookList()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.getBookList()
    },
    goToDetail(bookId) {
      this.$router.push(`/customer/book/${bookId}`)
    },
    goToCart() {
      this.$router.push('/customer/cart')
    },
    async addToCart(book) {
      try {
        await api.cart.add(this.userId, book.id, 1)
        this.$message.success('已添加到购物车')
      } catch (error) {
        console.error('添加购物车失败:', error)
        this.$message.error('添加失败')
      }
    },
    async buyNow(book) {
      try {
        this.$router.push({
          path: '/customer/checkout',
          query: { bookId: book.id }
        })
      } catch (error) {
        console.error('购买失败:', error)
        this.$message.error('购买失败')
      }
    },
    goToDonation() {
      this.$router.push('/customer/donation')
    },
    getBookCover(coverImage) {
      if (!coverImage || coverImage.trim() === '') {
        return '/static/default-book.svg'
      }
      if (coverImage.startsWith('/uploads/')) {
        return 'http://localhost:8080/api' + coverImage
      }
      return coverImage
    },
    handleImageError(e) {
      e.target.src = '/static/default-book.svg?t=' + new Date().getTime()
    },
    goToMember() {
      this.$router.push('/customer/member')
    },
    handleLogin() {
      this.$router.push('/login')
    }
  }
}
</script>

<style lang="scss" scoped>
.customer-home {
  min-height: 100vh;
  background-color: #f5f5f5;

  .main-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 20px;

    .category-sidebar {
      background-color: #fff;
      padding: 20px 0;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .book-list {
      background-color: #fff;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      min-height: 800px;

      .filter-bar {
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #eee;
      }

      .book-grid {
        display: flex;
        flex-wrap: wrap;
        margin: 0 -10px;
      }

      .book-card {
        margin-bottom: 20px;
        transition: transform 0.3s;
        
        &:hover {
          transform: translateY(-5px);
        }

        .book-cover {
          height: 280px;
          overflow: hidden;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: #f9f9f9;
          margin: -15px -15px 15px;

          img {
            max-width: 100%;
            max-height: 260px;
            object-fit: contain;
            transition: transform 0.3s;
          }
        }

        .book-info {
          .book-title {
            font-size: 16px;
            font-weight: bold;
            margin-bottom: 8px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .book-author {
            font-size: 14px;
            color: #666;
            margin-bottom: 5px;
          }

          .book-publisher {
            font-size: 12px;
            color: #999;
            margin-bottom: 10px;
          }

          .book-price {
            .current-price {
              font-size: 20px;
              color: #ff6700;
              font-weight: bold;
            }
            .sales {
              font-size: 12px;
              color: #999;
              margin-left: 8px;
            }
          }

          .book-stock {
            font-size: 12px;
            color: #999;
            margin: 10px 0;
          }

          .book-actions {
            display: flex;
            justify-content: space-between;
            margin-top: 15px;

            .el-button {
              flex: 1;
              margin: 0 5px;
            }
          }
        }
      }

      .pagination {
        margin-top: 30px;
        text-align: center;
      }
    }
  }
}
</style>
