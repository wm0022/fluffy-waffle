<template>
  <div class="recommend-grid">
    <div v-if="loading" class="grid-loading">
      <i class="el-icon-loading"></i> 加载中...
    </div>

    <div v-else-if="books.length === 0" class="empty-rec">
      暂无推荐
    </div>

    <div v-else class="rec-grid">
      <div v-for="book in books" :key="'rec-' + book.id" class="rec-card" @click="$emit('view', book.id)">
        <div class="rec-cover">
          <img
            :src="getCover(book.coverImage)"
            :alt="book.bookName"
            loading="lazy"
            @error="(e) => e.target.src = '/static/default-book.svg'"
          />
        </div>
        <h4 class="rec-title">{{ book.bookName }}</h4>
        <p class="rec-author">{{ book.author }}</p>
        <div class="rec-price-row">
          <span class="price">&yen;{{ book.sellingPrice || 0 }}</span>
          <span class="sales">已售{{ book.salesVolume || 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RecommendGrid',
  props: {
    books: { type: Array, default: () => [] },
    loading: { type: Boolean, default: false }
  },
  methods: {
    getCover(url) {
      if (!url) return '/static/default-book.svg'
      if (url.startsWith('http')) return url
      return `http://localhost:8080/api${url}`
    }
  }
}
</script>

<style lang="scss" scoped>
$danger: #e74c3c;
$text-1: #2d3436; $text-2: #636e72; $text-3: #b2bec3;

.recommend-grid {
  .grid-loading, .empty-rec {
    text-align: center; padding: 50px 0; color: $text-3; font-size: 14px;
  }
  .rec-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    @media screen and (max-width: 800px) {
      grid-template-columns: repeat(2, 1fr); gap: 14px;
    }
  }

  .rec-card {
    background: #fff;
    border: 1px solid #f0eeec;
    border-radius: 10px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.28s cubic-bezier(.25,.46,.45,.94);
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0,0,0,0.08);
      border-color: transparent;
      .rec-cover img { transform: scale(1.05); }
    }
  }

  .rec-cover {
    height: 200px;
    overflow: hidden;
    background: #fafafa;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 16px;
    img {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
      transition: transform 0.4s ease;
    }
  }

  .rec-title {
    font-size: 14px;
    font-weight: 600;
    color: $text-1;
    margin: 14px 14px 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .rec-author {
    font-size: 12px;
    color: $text-3;
    margin: 0 14px 8px;
  }
  .rec-price-row {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    padding: 0 14px 16px;
    .price { font-size: 17px; font-weight: 700; color: $danger; }
    .sales { font-size: 11px; color: $text-3; }
  }
}
</style>
