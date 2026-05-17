<template>
  <div class="review-panel">
    <!-- 评价概览 -->
    <div class="review-overview" v-if="totalCount > 0">
      <div class="overview-left">
        <span class="avg-score">{{ avgRating || '--' }}</span>
        <el-rate :value="Number(avgRating) || 0" disabled text-color="#ff9900" />
        <span class="total-text">{{ totalCount }} 条评价</span>
      </div>
      <button class="write-btn" @click="$emit('open-dialog')">
        <i class="el-icon-edit-outline"></i> 写评价
      </button>
    </div>

    <!-- 评价列表 -->
    <div class="review-list">
      <TransitionGroup name="review-fade" tag="div" class="list-inner">
        <article v-for="item in displayReviews" :key="item.reviewId" class="review-card">
          <header class="r-header">
            <div class="user-info">
              <div class="avatar">
                {{ (item.customerName || '匿')[0].toUpperCase() }}
              </div>
              <span class="name">{{ item.isAnonymous ? '匿名用户' : item.customerName }}</span>
            </div>
            <el-rate :value="item.rating || 0" disabled size="mini" />
          </header>

          <p class="r-content">{{ item.content }}</p>

          <footer class="r-footer">
            <time>{{ formatTime(item.createTime) }}</time>
            <div v-if="hasSubRatings(item)" class="sub-ratings">
              <span><b>书店</b> {{ item.storeRating }}分</span>
              <span><b>业务</b> {{ item.businessRating }}分</span>
              <span><b>服务</b> {{ item.serviceRating }}分</span>
            </div>
          </footer>

          <div v-if="item.replyContent" class="reply-box">
            <i class="el-icon-chat-dot-round"></i>
            <strong>商家回复：</strong>{{ item.replyContent }}
          </div>
        </article>
      </TransitionGroup>

      <div v-if="!reviews.length && !loading" class="empty-reviews">
        <i class="el-icon-chat-line-square"></i>
        <p>暂无评价，快来发表第一条吧~</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ReviewPanel',
  props: {
    reviews: { type: Array, default: () => [] },
    avgRating: { type: Number, default: 0 },
    totalCount: { type: Number, default: 0 },
    bookId: { type: [String, Number], required: true }
  },
  computed: {
    displayReviews() { return this.reviews.slice(0, 20) }
  },
  methods: {
    hasSubRatings(item) {
      return item.storeRating || item.businessRating || item.serviceRating
    },
    formatTime(time) {
      if (!time) return ''
      return time.replace(/T/, ' ').substring(0, 16)
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #c45a3b;
$text-1: #2d3436; $text-2: #636e72; $text-3: #b2bec3;

.review-panel {
  .review-overview {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    background: linear-gradient(135deg, #fef9f7, #fff5f2);
    border-radius: 12px;
    margin-bottom: 22px;
    border: 1px solid #f0ddd6;

    .overview-left {
      display: flex;
      align-items: center;
      gap: 14px;
      .avg-score {
        font-size: 36px;
        font-weight: 800;
        color: #ff9900;
        line-height: 1;
      }
      .total-text {
        font-size: 13px;
        color: $text-3;
        margin-left: 4px;
      }
    }
    .write-btn {
      padding: 8px 22px;
      border: 1.5px solid $primary;
      border-radius: 20px;
      color: $primary;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.25s ease;
      background: #fff;
      i { margin-right: 4px; }
      &:hover {
        background: $primary; color: #fff;
      }
    }
  }

  .list-inner {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .review-card {
    padding: 18px 20px;
    background: #fafafa;
    border-radius: 10px;
    border: 1px solid transparent;
    transition: all 0.25s ease;
    &:hover {
      background: #fff;
      box-shadow: 0 2px 10px rgba(0,0,0,0.06);
      border-color: #eee;
    }

    .r-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      .user-info {
        display: flex;
        align-items: center;
        gap: 10px;
        .avatar {
          width: 36px; height: 36px;
          border-radius: 50%;
          background: linear-gradient(135deg, $primary, #d47a5c);
          color: #fff;
          display: flex; align-items: center; justify-content: center;
          font-size: 15px; font-weight: 600;
        }
        .name { font-size: 14px; font-weight: 600; color: $text-1; }
      }
    }
    .r-content {
      font-size: 14px; line-height: 1.7; color: $text-1;
      margin: 0 0 10px; white-space: pre-wrap;
    }
    .r-footer {
      display: flex; justify-content: space-between; align-items: center;
      time { font-size: 12px; color: $text-3; }
      .sub-ratings {
        display: flex; gap: 12px;
        span {
          font-size: 11px; color: $text-3;
          b { color: $text-2; margin-right: 2px; }
        }
      }
    }
    .reply-box {
      margin-top: 10px; padding: 10px 14px;
      background: #edfdf1; border-radius: 6px;
      font-size: 13px; color: #27ae60;
      i { margin-right: 5px; }
      strong { color: #1e8449; }
    }
  }

  .empty-reviews {
    text-align: center; padding: 50px 0; color: $text-3;
    i { font-size: 48px; opacity: 0.4; display: block; margin-bottom: 12px; }
    p { font-size: 14px; margin: 0; }
  }
}

/* 过渡动画 */
.review-fade-enter-active, .review-fade-leave-active { transition: all 0.35s ease; }
.review-fade-enter, .review-fade-leave-to { opacity: 0; transform: translateY(8px); }
</style>
