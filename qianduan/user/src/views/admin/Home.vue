<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF;">
              <i class="el-icon-reading"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ bookCount.totalCount || 0 }}</div>
              <div class="stat-label">图书总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A;">
              <i class="el-icon-office-building"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ bookCount.onShelfCount || 0 }}</div>
              <div class="stat-label">上架图书</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <i class="el-icon-shopping-cart-2"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ bookCount.hotBookCount || 0 }}</div>
              <div class="stat-label">热销图书</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C;">
              <i class="el-icon-s-promotion"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ bookCount.donationBookCount || 0 }}</div>
              <div class="stat-label">捐赠图书</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>待办事项</span>
          </div>
          <el-timeline>
            <el-timeline-item timestamp="2024-01-01" placement="top" type="primary">
              <el-card>
                <h4>待审核捐赠申请 3 条</h4>
                <p>请及时处理捐赠申请</p>
              </el-card>
            </el-timeline-item>
            <el-timeline-item timestamp="2024-01-02" placement="top" type="success">
              <el-card>
                <h4>库存预警图书 5 本</h4>
                <p>部分图书库存低于预警值</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>快捷入口</span>
          </div>
          <div class="quick-links">
            <el-button type="primary" icon="el-icon-plus" @click="goToAddBook">添加图书</el-button>
            <el-button type="success" icon="el-icon-document" @click="goToDonation">捐赠申请</el-button>
            <el-button type="warning" icon="el-icon-office-building" @click="goToInventory">库存管理</el-button>
            <el-button type="info" icon="el-icon-user" @click="goToMember">会员管理</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'Home',
  data() {
    return {
      bookCount: {}
    }
  },
  created() {
    this.getBookCount()
  },
  methods: {
    async getBookCount() {
      try {
        const res = await api.book.count()
        this.bookCount = res.data
      } catch (error) {
        console.error(error)
      }
    },
    goToAddBook() {
      this.$router.push('/admin/book/add')
    },
    goToDonation() {
      this.$router.push('/admin/donation-apply')
    },
    goToInventory() {
      this.$router.push('/admin/inventory')
    },
    goToMember() {
      this.$router.push('/admin/member')
    }
  }
}
</script>

<style lang="scss" scoped>
.home-container {
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        
        i {
          font-size: 30px;
          color: #fff;
        }
      }
      
      .stat-info {
        flex: 1;
        
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }
  
  .quick-links {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>
