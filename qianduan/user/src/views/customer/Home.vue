<template>
  <div class="customer-home">
    <!-- ==================== 顶部轮播图 Banner ==================== -->
    <!-- [占位说明] 轮播图区域 - 建议 3-5 张 promotional banner
         尺寸建议: 1200 x 400px (宽高比 3:1)
         格式: JPG/PNG/WebP, 文件大小 < 500KB
         内容建议: 季节性促销、新书推荐、会员专享、节日活动
         替换方式: 修改 bannerList 数组中的 imageUrl -->
    <section class="banner-section">
      <div class="banner-container">
        <el-carousel height="400px" :interval="5000" arrow="always" indicator-position="outside">
          <el-carousel-item v-for="(banner, index) in bannerList" :key="index">
            <div
              class="banner-item"
              :style="getBannerStyle(banner)"
              @click="handleBannerClick(banner)"
            >
              <!-- 半透明遮罩层，保证文字在任何图片上都清晰可读 -->
              <div class="banner-overlay"></div>
              <div class="banner-content">
                <div class="banner-text">
                  <span v-if="banner.tag" class="banner-tag">{{ banner.tag }}</span>
                  <h2 class="banner-title">{{ banner.title }}</h2>
                  <p class="banner-desc">{{ banner.desc }}</p>
                  <el-button v-if="banner.btnText" type="primary" size="large" round class="banner-btn">
                    {{ banner.btnText }}
                  </el-button>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <!-- ==================== 主体内容区 ==================== -->
    <div class="main-body">
      <div class="main-container">
        <!-- 左侧边栏分类菜单 -->
        <aside class="side-category-bar">
          <div class="side-cat-header">
            <i class="el-icon-menu"></i>
            <span>全部分类</span>
          </div>
          <el-menu :default-active="activeCategory" @select="handleCategoryChange" class="side-menu">
            <el-menu-item index="all">
              <i class="el-icon-s-grid"></i>
              <span>全部图书</span>
            </el-menu-item>
            <el-menu-item index="hot">
              <i class="el-icon-s-finance"></i>
              <span>热销推荐</span>
              <span class="menu-badge hot">HOT</span>
            </el-menu-item>
            <el-menu-item index="new">
              <i class="el-icon-time"></i>
              <span>新品上架</span>
              <span class="menu-badge new">NEW</span>
            </el-menu-item>
            <el-menu-item index="charity">
              <i class="el-icon-s-promotion"></i>
              <span>公益专区</span>
            </el-menu-item>
            <el-menu-item index="member">
              <i class="el-icon-star-on"></i>
              <span>会员服务</span>
            </el-menu-item>
          </el-menu>
        </aside>

        <!-- 右侧内容区 -->
        <main class="content-area">
          <!-- ===== 热销推荐区块 (仅 all 分类显示) ===== -->
          <section v-if="activeCategory === 'all'" class="hot-recommend-section" id="hot-section">
            <div class="section-header">
              <div class="header-left">
                <i class="el-icon-fire section-icon hot-icon"></i>
                <h3 class="section-title">热销推荐</h3>
                <span class="subtitle">大家都在买的好书</span>
              </div>
              <el-button type="text" class="view-more" @click="activeCategory = 'hot'; handleCategoryChange('hot')">
                查看更多 <i class="el-icon-arrow-right"></i>
              </el-button>
            </div>
            <div class="book-row-scroll">
              <div class="book-card-horizontal" v-for="book in hotBooks.slice(0, 4)" :key="'hot-' + book.id"
                @click="goToDetail(book.id)">
                <!-- [占位] 商品封面 - 尺寸 140x190px -->
                <div class="card-cover">
                  <img :src="getBookCover(book.coverImage)" alt="" @error="handleImageError" />
                  <div class="cover-tag tag-hot">热销</div>
                </div>
                <div class="card-info-h">
                  <h4 class="card-title-h">{{ book.bookName }}</h4>
                  <p class="card-author-h">{{ book.author }}</p>
                  <div class="card-price-row">
                    <span class="price-h">&yen;{{ book.sellingPrice || 0 }}</span>
                    <span class="sales-h">已售{{ book.salesVolume || 0 }}件</span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- ===== 新品上架区块 (仅 all 分类显示) ===== -->
          <section v-if="activeCategory === 'all'" class="new-books-section">
            <div class="section-header">
              <div class="header-left">
                <i class="el-icon-refresh-right section-icon new-icon"></i>
                <h3 class="section-title">新品上架</h3>
                <span class="subtitle">最新到店好书</span>
              </div>
              <el-button type="text" class="view-more" @click="activeCategory = 'new'; handleCategoryChange('new')">
                查看更多 <i class="el-icon-arrow-right"></i>
              </el-button>
            </div>
            <div class="book-row-scroll">
              <div class="book-card-horizontal" v-for="book in newBooks.slice(0, 4)" :key="'new-' + book.id"
                @click="goToDetail(book.id)">
                <div class="card-cover">
                  <img :src="getBookCover(book.coverImage)" alt="" @error="handleImageError" />
                  <div class="cover-tag tag-new">新品</div>
                </div>
                <div class="card-info-h">
                  <h4 class="card-title-h">{{ book.bookName }}</h4>
                  <p class="card-author-h">{{ book.author }}</p>
                  <div class="card-price-row">
                    <span class="price-h">&yen;{{ book.sellingPrice || 0 }}</span>
                    <span class="sales-h">新上架</span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- ===== 公益专区横幅 (仅 all 分类显示) ===== -->
          <!-- [占位说明] 公益横幅 - 建议 1200x120px
               可替换为实际的公益活动宣传图 -->
          <section v-if="activeCategory === 'all'" class="charity-banner-section" @click="$router.push('/customer/donation')">
            <div class="charity-banner-inner">
              <div class="charity-left">
                <i class="el-icon-present charity-icon"></i>
                <div>
                  <h3>爱心公益 &middot; 书香传递</h3>
                  <p>每一本书都是一份爱心，让知识传递温暖</p>
                </div>
              </div>
              <el-button type="warning" round size="medium" class="charity-btn">立即参与捐赠</el-button>
            </div>
          </section>

          <!-- ===== 图书列表主体区 ===== -->
          <section class="book-list-section">
            <div class="list-toolbar">
              <div class="toolbar-left">
                <el-radio-group v-model="sortBy" size="small" @change="handleSortChange" class="sort-group">
                  <el-radio-button label="default">综合</el-radio-button>
                  <el-radio-button label="sales">销量</el-radio-button>
                  <el-radio-button label="price">价格</el-radio-button>
                  <el-radio-button label="time">最新</el-radio-button>
                </el-radio-group>
              </div>
              <div class="toolbar-right">
                <span class="result-count">共 <strong>{{ total }}</strong> 本图书</span>
              </div>
            </div>

            <div class="book-grid-modern" v-loading="loading">
              <div v-for="book in bookList" :key="book.id" class="book-card-modern" @click="goToDetail(book.id)">
                <!-- [占位] 商品卡片封面 - 尺寸 200x260px -->
                <div class="modern-cover">
                  <img :src="getBookCover(book.coverImage)" :alt="book.bookName" @error="handleImageError" />
                  <div class="cover-overlay">
                    <el-button type="primary" size="small" circle icon="el-icon-search" class="preview-btn"
                      title="查看详情" @click.stop="goToDetail(book.id)" />
                  </div>
                  <div v-if="book.stockCount <= 5 && book.stockCount > 0" class="stock-tag low-stock">
                    仅剩 {{ book.stockCount }} 件
                  </div>
                  <div v-else-if="book.stockCount === 0" class="stock-tag out-stock">缺货</div>
                </div>
                <div class="modern-info">
                  <h4 class="modern-title" :title="book.bookName">{{ book.bookName }}</h4>
                  <p class="modern-author">{{ book.author }}</p>
                  <p class="modern-publisher">{{ book.publisher || '未知出版社' }}</p>
                  <div class="modern-bottom">
                    <div class="price-area">
                      <span class="current-price">&yen;{{ book.sellingPrice || 0 }}</span>
                      <span v-if="book.originalPrice && book.originalPrice > book.sellingPrice" class="original-price">
                        &yen;{{ book.originalPrice }}
                      </span>
                    </div>
                    <div class="action-area">
                      <el-button type="primary" size="mini" round class="cart-btn"
                        :disabled="!book.stockCount" @click.stop="addToCart(book)">
                        <i class="el-icon-shopping-cart-2"></i> 加购
                      </el-button>
                      <el-button type="danger" size="mini" round class="buy-btn"
                        :disabled="!book.stockCount" @click.stop="buyNow(book)">
                        立购
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 空状态 -->
              <div v-if="!loading && bookList.length === 0" class="empty-state">
                <i class="el-icon-box"></i>
                <p>暂无图书数据</p>
              </div>
            </div>

            <!-- 分页器 -->
            <div class="pagination-area" v-if="total > pageSize">
              <el-pagination
                :current-page.sync="pageNum"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next, jumper"
                background
                @current-change="handlePageChange"
              />
            </div>
          </section>
        </main>
      </div>
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
      hotBooks: [],
      newBooks: [],
      pageNum: 1,
      pageSize: 12,
      total: 0,
      loading: false,
      // ========== 轮播图配置 [可编辑区域] ==========
      // 【重要】将 imageUrl 替换为你自己的图片路径即可
      // 支持格式：绝对路径(https://...) 或 相对于 public 目录的路径(/images/banner1.jpg)
      // 建议尺寸: 1920 x 640px（宽高比 3:1），文件大小 < 800KB，格式 JPG/WebP/PNG
      // 图片会自动 background-size: cover 铺满，不会变形
      bannerList: [
        {
          id: 2,
          tag: '新书速递',
          title: '2026年度畅销书单',
          desc: '编辑精选 · 值得一读再读',
          btnText: '探索书单',
          link: '#hot-section',
          // ↓↓↓ 填入你的图片文件名（图片放在 public/images/ 目录下）↓ ↓ ↓
          imageUrl: '/images/banner1.png'
        },
        {
          id: 3,
          tag: '公益行动',
          title: '书香传递计划',
          desc: '',
          btnText: '了解详情',
          link: '/customer/donation',
          // ↓↓↓ 填入你的图片文件名（图片放在 public/images/ 目录下）↓ ↓ ↓
          imageUrl: '/images/banner2.png'
        }
      ],
      // ========== 分类图标配置 [可编辑区域] ==========
      // 修改 iconUrl 为实际图标路径即可替换分类图标
      // 建议尺寸: 64x64px, 格式 PNG(透明底) / SVG
      categoryIcons: [
        { name: '文学小说', icon: 'el-icon-notebook-2', iconUrl: '', key: 'literature' },
        { name: '经管励志', icon: 'el-icon-data-analysis', iconUrl: '', key: 'business' },
        { name: '人文社科', icon: 'el-icon-reading', iconUrl: '', key: 'humanities' },
        { name: '童书绘本', icon: 'el-icon-star-off', iconUrl: '', key: 'children' },
        { name: '科技编程', icon: 'el-icon-cpu', iconUrl: '', key: 'tech' },
        { name: '艺术设计', icon: 'el-icon-picture', iconUrl: '', key: 'art' },
        { name: '生活休闲', icon: 'el-icon-coffee-cup', iconUrl: '', key: 'lifestyle' },
        { name: '教材教辅', icon: 'el-icon-document-checked', iconUrl: '', key: 'education' }
      ]
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    searchKeyword() {
      return this.$route.query.keyword || ''
    }
  },
  created() {
    if (this.$route.query.tab) {
      this.activeCategory = this.$route.query.tab
    }
    this.getBookList()
    this.getHotBooks()
    this.getNewBooks()
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
      this.loading = true
      try {
        if (this.activeCategory === 'hot') {
          const res = await api.book.getHot(this.pageSize)
          this.bookList = res || []
          this.total = this.bookList.length
        } else if (this.activeCategory === 'new') {
          const res = await api.book.getNew(this.pageSize)
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
      } finally {
        this.loading = false
      }
    },
    async getHotBooks() {
      try {
        this.hotBooks = await api.book.getHot(8) || []
      } catch (e) { this.hotBooks = [] }
    },
    async getNewBooks() {
      try {
        this.newBooks = await api.book.getNew(8) || []
      } catch (e) { this.newBooks = [] }
    },
    getBannerStyle(banner) {
      if (banner.imageUrl && banner.imageUrl.trim()) {
        return {
          backgroundImage: `url('${banner.imageUrl}')`,
          backgroundSize: 'cover',
          backgroundPosition: 'center center',
          backgroundRepeat: 'no-repeat'
        }
      }
      // 无图片时使用默认渐变作为兜底
      return {
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }
    },
    handleBannerClick(banner) {
      if (banner.link) {
        if (banner.link.startsWith('#')) {
          // 锚点滚动（如 #hot-section）
          const el = document.querySelector(banner.link)
          if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
        } else if (banner.link.startsWith('/')) {
          this.$router.push(banner.link)
        } else {
          window.open(banner.link, '_blank')
        }
      }
    },
    handleCategoryIconClick(cat) {
      // TODO: 可扩展为按分类筛选
      this.$message.info(`${cat.name} 分类即将上线`)
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
      if (this.$route.query.tab === category) {
        this.activeCategory = category
        this.pageNum = 1
        this.getBookList()
        return
      }
      this.activeCategory = category
      this.pageNum = 1
      this.$router.replace({ query: { tab: category } }).catch(() => {})
      this.getBookList()
    },
    handleSortChange() {
      this.pageNum = 1
      this.getBookList()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.getBookList()
      window.scrollTo({ top: 400, behavior: 'smooth' })
    },
    goToDetail(bookId) {
      this.$router.push(`/customer/book/${bookId}`)
    },
    async addToCart(book) {
      try {
        await api.cart.add(book.id, 1)
        this.$message.success(`《${book.bookName}》已添加到购物车`)
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
    }
  }
}
</script>

<style lang="scss" scoped>
/* ==================== 变量定义 ==================== */
$primary: #c45a3b;
$primary-light: #e87355;
$primary-dark: #a04228;
$text-main: #2d3436;
$text-secondary: #636e72;
$text-muted: #b2bec3;
$bg-page: #f8f6f3;
$bg-white: #ffffff;
$border-color: #ececec;
$shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.06);
$shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);
$shadow-lg: 0 8px 32px rgba(0, 0, 0, 0.10);
$radius-sm: 8px;
$radius-md: 12px;
$radius-lg: 16px;

/* ==================== 全局容器 ==================== */
.customer-home {
  min-height: 100vh;
  background-color: $bg-page;
}

/* ==================== 轮播图区域 ==================== */
.banner-section {
  width: 100%;

  .banner-container {
    max-width: 100%; /* 全宽轮播，图片铺满 */
    margin: 0 auto;
    overflow: hidden;
    box-shadow: $shadow-md;

    ::v-deep .el-carousel__item {
      overflow: hidden;
    }

    /* 指示器 - 白色系，适配任意背景图 */
    ::v-deep .el-carousel__indicators--outside {
      bottom: 8px;
      position: relative;
      z-index: 5;
      .el-carousel__button {
        width: 24px;
        height: 4px;
        border-radius: 2px;
        background-color: rgba(255, 255, 255, 0.45);
        &.is-active {
          width: 40px;
          background-color: #fff;
          box-shadow: 0 1px 4px rgba(0,0,0,0.2);
        }
      }
    }

    /* 左右箭头 - 增强对比度 */
    ::v-deep .el-carousel__arrow {
      width: 46px;
      height: 46px;
      font-size: 17px;
      background-color: rgba(0, 0, 0, 0.35);
      border: 1px solid rgba(255, 255, 255, 0.15);
      backdrop-filter: blur(4px);
      color: #fff;
      border-radius: 50%;
      transition: all 0.25s ease;
      &:hover {
        background-color: rgba(0, 0, 0, 0.55);
        transform: scale(1.08);
      }
      &--left { left: 12px; }
      &--right { right: 12px; }
    }
  }

  /* 轮播项 - 背景图模式 */
  .banner-item {
    height: 100%;
    cursor: pointer;
    position: relative;
    transition: transform 0.4s ease;

    &:hover {
      transform: scale(1.005);
    }

    /* 半透明渐变遮罩 - 从左到右由深变浅，保证左侧文字清晰 */
    .banner-overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(
        90deg,
        rgba(0, 0, 0, 0.55) 0%,
        rgba(0, 0, 0, 0.30) 40%,
        rgba(0, 0, 0, 0.08) 70%,
        transparent 100%
      );
      z-index: 1;
      pointer-events: none;
    }
  }

  .banner-content {
    max-width: 1200px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: flex-start;
    padding: 0 60px;
    position: relative;
    z-index: 2;
  }

  .banner-text {
    color: #fff;
    flex: 1;
    max-width: 520px;

    .banner-tag {
      display: inline-block;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(6px);
      -webkit-backdrop-filter: blur(6px);
      padding: 5px 16px;
      border-radius: 20px;
      font-size: 13px;
      margin-bottom: 16px;
      letter-spacing: 1.5px;
      border: 1px solid rgba(255, 255, 255, 0.2);
    }
    .banner-title {
      font-size: 42px;
      font-weight: 800;
      margin: 0 0 14px;
      line-height: 1.2;
      text-shadow:
        0 2px 8px rgba(0, 0, 0, 0.35),
        0 1px 3px rgba(0, 0, 0, 0.25);
      letter-spacing: 0.5px;
    }
    .banner-desc {
      font-size: 17px;
      opacity: 0.95;
      margin: 0 0 28px;
      line-height: 1.65;
      text-shadow: 0 1px 4px rgba(0, 0, 0, 0.25);
    }
    .banner-btn {
      font-size: 15px;
      padding: 12px 32px;
      border: none;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
      transition: all 0.3s ease;
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
      }
    }
  }
}

/* ==================== 分类图标区 ==================== */
.category-icons-section {
  background: $bg-white;
  padding: 28px 0;
  margin-top: -20px;
  position: relative;
  z-index: 10;
  border-radius: $radius-lg;
  box-shadow: $shadow-sm;

  .section-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .category-grid {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 12px;
  }

  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 14px 8px;
    border-radius: $radius-sm;
    cursor: pointer;
    transition: all 0.25s ease;

    &:hover {
      background: linear-gradient(135deg, #fef0ef 0%, #fff5f0 100%);
      transform: translateY(-3px);

      .cat-icon {
        background: linear-gradient(135deg, $primary-light, $primary);
        color: #fff;

        i { color: #fff !important; }
        small { color: rgba(255,255,255,0.7) !important; }
      }

      .cat-name { color: $primary; }
    }
  }

  .cat-icon-wrapper {
    margin-bottom: 8px;
  }

  /* 分类图标占位 */
  .cat-icon.placeholder-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, #f0f0f0, #e8e8e8);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    transition: all 0.25s ease;

    i {
      font-size: 26px;
      color: $text-secondary;
    }
    small {
      font-size: 9px;
      color: $text-muted;
      margin-top: 2px;
    }
  }

  .cat-icon:not(.placeholder-icon) {
    width: 60px;
    height: 60px;
    object-fit: contain;
    border-radius: 50%;
  }

  .cat-name {
    font-size: 13px;
    color: $text-main;
    font-weight: 500;
    transition: color 0.25s ease;
  }
}

/* ==================== 主体内容区 ==================== */
.main-body {
  max-width: 1200px;
  margin: 24px auto;
  padding: 0 20px;
}

.main-container {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* ==================== 左侧分类栏 ==================== */
.side-category-bar {
  width: 200px;
  flex-shrink: 0;
  background: $bg-white;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  overflow: hidden;
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow: $shadow-md;
  }

  .side-cat-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px 18px;
    background: linear-gradient(135deg, $primary, $primary-dark);
    color: #fff;
    font-weight: 600;
    font-size: 15px;

    i { font-size: 18px; }
  }

  .side-menu {
    border-right: none !important;
    padding: 8px 0;

    ::v-deep .el-menu-item {
      height: 46px;
      line-height: 46px;
      font-size: 14px;
      position: relative;

      i { margin-right: 8px; color: $text-secondary; }

      &:hover {
        background-color: #fef5f2 !important;
        color: $primary !important;
        i { color: $primary !important; }
      }

      &.is-active {
        background-color: #fef0ef !important;
        color: $primary !important;
        font-weight: 600;
        border-right: 3px solid $primary;
        i { color: $primary !important; }
      }
    }
  }

  .menu-badge {
    position: absolute;
    right: 14px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 10px;
    padding: 1px 6px;
    border-radius: 8px;
    font-weight: 700;
    line-height: 1.4;

    &.hot { background: #ffeaea; color: #e74c3c; }
    &.new { background: #eef9ff; color: #3498db; }
  }
}

/* ==================== 右侧内容区 ==================== */
.content-area {
  flex: 1;
  min-width: 0;
}

/* ---- 区块标题通用样式 ---- */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;

  .header-left {
    display: flex;
    align-items: baseline;
    gap: 10px;
  }

  .section-icon {
    font-size: 22px;
  }
  .hot-icon { color: #e74c3c; }
  .new-icon { color: #3498db; }

  .section-title {
    font-size: 20px;
    font-weight: 700;
    color: $text-main;
    margin: 0;
  }

  .subtitle {
    font-size: 13px;
    color: $text-muted;
    margin-left: 6px;
  }

  .view-more {
    color: $text-secondary;
    font-size: 13px;
    padding: 0;

    &:hover { color: $primary; }
    i { margin-left: 2px; }
  }
}

/* ---- 热销/新品横向滚动卡片 ---- */
.hot-recommend-section,
.new-books-section {
  background: $bg-white;
  border-radius: $radius-md;
  padding: 22px 24px;
  margin-bottom: 20px;
  box-shadow: $shadow-sm;
}

.book-row-scroll {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.book-card-horizontal {
  display: flex;
  gap: 14px;
  padding: 12px;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid transparent;

  &:hover {
    box-shadow: $shadow-sm;
    border-color: #f0e6e2;
    transform: translateY(-2px);
  }

  .card-cover {
    width: 110px;
    height: 150px;
    flex-shrink: 0;
    border-radius: 6px;
    overflow: hidden;
    position: relative;
    background: #fafafa;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    &:hover img { transform: scale(1.05); }

    .cover-tag {
      position: absolute;
      top: 6px;
      left: 6px;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 10px;
      font-weight: 600;
      color: #fff;
    }
    .tag-hot { background: linear-gradient(135deg, #e74c3c, #c0392b); }
    .tag-new { background: linear-gradient(135deg, #3498db, #2980b9); }
  }

  .card-info-h {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 0;

    .card-title-h {
      font-size: 14px;
      font-weight: 600;
      color: $text-main;
      margin: 0 0 6px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .card-author-h {
      font-size: 12px;
      color: $text-secondary;
      margin: 0 0 10px;
    }
    .card-price-row {
      display: flex;
      align-items: baseline;
      gap: 8px;
    }
    .price-h {
      font-size: 17px;
      font-weight: 700;
      color: #e74c3c;
    }
    .sales-h {
      font-size: 11px;
      color: $text-muted;
    }
  }
}

/* ---- 公益横幅 ---- */
.charity-banner-section {
  background: linear-gradient(135deg, #fff5ee 0%, #ffe8dc 50%, #fce4ec 100%);
  border-radius: $radius-md;
  padding: 24px 30px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ffdcc8;
  position: relative;
  overflow: hidden;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }

  .charity-banner-inner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;
    z-index: 1;
  }

  .charity-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .charity-icon {
      font-size: 48px;
      color: #e67e22;
    }
    h3 {
      font-size: 20px;
      color: #c0392b;
      margin: 0 0 4px;
    }
    p {
      font-size: 14px;
      color: #a04000;
      margin: 0;
    }
  }

  .charity-btn {
    background: linear-gradient(135deg, #f39c12, #e67e22);
    border: none;
    font-size: 14px;
    padding: 10px 28px;
    transition: all 0.25s ease;
    &:hover {
      transform: scale(1.05);
      box-shadow: 0 4px 14px rgba(230, 126, 34, 0.4);
    }
  }

  /* 小占位图 */
  .placeholder-img-sm {
    width: 240px;
    height: 80px;
  }

  .placeholder-box.small {
    background: rgba(255, 255, 255, 0.4);
    border-color: rgba(210, 140, 80, 0.3);
    border-radius: 8px;
    color: #b06a30;
    i { font-size: 24px; }
    span { font-size: 12px; }
    small { font-size: 10px; }
  }

  .charity-deco {
    flex-shrink: 0;
  }
}

/* ---- 图书列表主体 ---- */
.book-list-section {
  background: $bg-white;
  border-radius: $radius-md;
  padding: 24px;
  box-shadow: $shadow-sm;
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid $border-color;

  .sort-group {
    ::v-deep .el-radio-button__inner {
      border-radius: 20px !important;
      border: 1px solid #e0e0e0;
      padding: 8px 18px;
      font-size: 13px;
      box-shadow: none !important;
      transition: all 0.2s ease;
    }
    ::v-deep .el-radio-button:first-child .el-radio-button__inner {
      border-radius: 20px !important;
    }
    ::v-deep .el-radio-button:last-child .el-radio-button__inner {
      border-radius: 20px !important;
    }
    ::v-deep .el-radio-button__orig-radio:checked + .el-radio-button__inner {
      background-color: $primary;
      border-color: $primary;
      color: #fff;
      box-shadow: 0 2px 8px rgba($primary, 0.3) !important;
    }
  }

  .result-count {
    font-size: 13px;
    color: $text-secondary;
    strong { color: $primary; }
  }
}

/* ---- 现代化商品网格 ---- */
.book-grid-modern {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  min-height: 200px;
}

.book-card-modern {
  background: $bg-white;
  border-radius: $radius-md;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  border: 1px solid #f0eded;

  &:hover {
    transform: translateY(-6px);
    box-shadow: $shadow-lg;
    border-color: transparent;

    .modern-cover img {
      transform: scale(1.06);
    }
    .cover-opacity {
      opacity: 1;
    }
  }

  /* 封面区域 */
  .modern-cover {
    width: 100%;
    height: 250px;
    overflow: hidden;
    position: relative;
    background: linear-gradient(180deg, #fafafa, #f5f5f5);

    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
      padding: 12px;
      transition: transform 0.4s ease;
    }

    .cover-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.35);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s ease;

      .preview-btn {
        background: $primary;
        border-color: $primary;
        width: 42px;
        height: 42px;

        i { font-size: 18px; }
      }
    }
  }

  /* 库存标签 */
  .stock-tag {
    position: absolute;
    top: 10px;
    right: 10px;
    padding: 3px 10px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 600;

    &.low-stock {
      background: #fff3cd;
      color: #856404;
    }
    &.out-stock {
      background: #f8d7da;
      color: #721c24;
    }
  }

  /* 信息区域 */
  .modern-info {
    padding: 14px 16px 18px;

    .modern-title {
      font-size: 15px;
      font-weight: 600;
      color: $text-main;
      margin: 0 0 6px;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      transition: color 0.2s ease;
    }
    &:hover .modern-title { color: $primary; }

    .modern-author {
      font-size: 13px;
      color: $text-secondary;
      margin: 0 0 4px;
    }
    .modern-publisher {
      font-size: 12px;
      color: $text-muted;
      margin: 0 0 12px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .modern-bottom {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
    }

    .price-area {
      display: flex;
      align-items: baseline;
      gap: 6px;

      .current-price {
        font-size: 19px;
        font-weight: 800;
        color: #e74c3c;
      }
      .original-price {
        font-size: 12px;
        color: $text-muted;
        text-decoration: line-through;
      }
    }

    .action-area {
      display: flex;
      gap: 6px;

      .cart-btn, .buy-btn {
        font-size: 11px;
        padding: 5px 10px;
        border-radius: 14px;
      }
      .cart-btn {
        background: lighten($primary, 38%);
        color: $primary;
        border-color: transparent;
        &:hover {
          background: $primary;
          color: #fff;
        }
      }
      .buy-btn {
        background: #e74c3c;
        border-color: #e74c3c;
        &:hover { background: #c0392b; border-color: #c0392b; }
      }
    }
  }
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  color: $text-muted;

  i { font-size: 56px; margin-bottom: 14px; }
  p { font-size: 15px; margin: 0; }
}

/* ---- 分页器 ---- */
.pagination-area {
  margin-top: 32px;
  display: flex;
  justify-content: center;
  padding-bottom: 8px;

  ::v-deep .el-pagination.is-background {
    .btn-prev, .btn-next, .el-pager li {
      border-radius: 8px;
      font-weight: 500;
    }
    .el-pager li:not(.disabled).is-active {
      background-color: $primary;
      box-shadow: 0 2px 8px rgba($primary, 0.3);
    }
  }
}

/* ==================== 响应式适配 ==================== */
@media screen and (max-width: 1100px) {
  .main-container {
    flex-direction: column;
  }
  .side-category-bar {
    width: 100%;

    .side-menu {
      ::v-deep .el-menu-item {
        display: inline-flex;
        width: auto;
        padding: 0 16px;
        border-right: none !important;
        border-bottom: 3px solid transparent !important;

        &.is-active {
          border-bottom-color: $primary !important;
        }
      }

      /* 横向滚动 */
      display: flex;
      overflow-x: auto;
      padding: 8px 12px;
      white-space: nowrap;
      &::-webkit-scrollbar { height: 4px; }
      &::-webkit-scrollbar-thumb { background: #ddd; border-radius: 2px; }
    }
  }

  .book-grid-modern,
  .book-row-scroll {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media screen and (max-width: 768px) {
  .banner-section {
    .banner-container {
      ::v-deep .el-carousel { height: 260px !important; }
      ::v-deep .el-carousel__arrow {
        width: 36px; height: 36px; font-size: 14px;
        &--left { left: 8px; }
        &--right { right: 8px; }
      }
    }

    .banner-overlay {
      /* 移动端遮罩加深，确保文字可读 */
      background: linear-gradient(
        180deg,
        rgba(0, 0, 0, 0.5) 0%,
        rgba(0, 0, 0, 0.25) 50%,
        rgba(0, 0, 0, 0.4) 100%
      );
    }
  }

  .banner-content {
    flex-direction: column;
    text-align: center;
    padding: 30px 20px;
    justify-content: center;
  }

  .banner-text {
    max-width: 100%;
    .banner-title { font-size: 24px; }
    .banner-desc { font-size: 14px; }
    .banner-btn { padding: 10px 24px; font-size: 14px; }
  }

  .category-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .book-grid-modern,
  .book-row-scroll {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .charity-banner-inner {
    flex-direction: column;
    text-align: center;
    gap: 14px;
  }
  .charity-deco { display: none; }
}
</style>
