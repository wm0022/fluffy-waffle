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
      <el-col :span="8">
        <el-card class="sales-stat-card">
          <div slot="header">
            <span>今日销售</span>
          </div>
          <div class="sales-stats">
            <div class="stats-item">
              <div class="stats-label">订单数</div>
              <div class="stats-value">{{ salesStats.today.orderCount || 0 }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">销量</div>
              <div class="stats-value">{{ salesStats.today.totalSales || 0 }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">销售额</div>
              <div class="stats-value">¥{{ salesStats.today.totalRevenue || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="sales-stat-card">
          <div slot="header">
            <span>本周销售</span>
          </div>
          <div class="sales-stats">
            <div class="stats-item">
              <div class="stats-label">订单数</div>
              <div class="stats-value">{{ salesStats.week.orderCount || 0 }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">销量</div>
              <div class="stats-value">{{ salesStats.week.totalSales || 0 }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">销售额</div>
              <div class="stats-value">¥{{ salesStats.week.totalRevenue || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="sales-stat-card">
          <div slot="header">
            <span>本月销售</span>
          </div>
          <div class="sales-stats">
            <div class="stats-item">
              <div class="stats-label">订单数</div>
              <div class="stats-value">{{ salesStats.month.orderCount || 0 }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">销量</div>
              <div class="stats-value">{{ salesStats.month.totalSales || 0 }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">销售额</div>
              <div class="stats-value">¥{{ salesStats.month.totalRevenue || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>销售趋势（近7天）</span>
          </div>
          <div ref="salesTrendChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>热销图书TOP5</span>
          </div>
          <el-table :data="topBooks" style="width: 100%" size="small">
            <el-table-column type="index" label="排名" width="60" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.$index < 3 ? 'danger' : 'info'" size="mini">{{ scope.$index + 1 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="bookName" label="图书名称" />
            <el-table-column prop="salesVolume" label="销量" width="80" align="center" />
            <el-table-column prop="revenue" label="销售额" width="100" align="right">
              <template slot-scope="scope">
                ¥{{ scope.row.revenue || 0 }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!topBooks || topBooks.length === 0" description="暂无销售数据" />
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
                <h4>待审核捐赠申请 {{ todoStats.pendingDonationApplyCount }} 条</h4>
                <p>请及时处理捐赠申请</p>
              </el-card>
            </el-timeline-item>
            <el-timeline-item timestamp="2024-01-02" placement="top" type="success">
              <el-card>
                <h4>库存预警图书 {{ todoStats.inventoryWarningCount }} 本</h4>
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
import * as echarts from 'echarts'

export default {
  name: 'Home',
  data() {
    return {
      bookCount: {},
      todoStats: {
        pendingDonationApplyCount: 0,
        inventoryWarningCount: 0
      },
      salesStats: {
        today: { orderCount: 0, totalSales: 0, totalRevenue: 0 },
        week: { orderCount: 0, totalSales: 0, totalRevenue: 0 },
        month: { orderCount: 0, totalSales: 0, totalRevenue: 0 }
      },
      topBooks: [],
      salesTrend: [],
      salesTrendChart: null
    }
  },
  created() {
    this.getBookCount()
    this.getTodoStats()
    this.getSalesStats()
  },
  mounted() {
    this.$nextTick(() => {
      this.initSalesTrendChart()
    })
  },
  methods: {
    async getBookCount() {
      try {
        const res = await api.book.count()
        this.bookCount = res || {}
      } catch (error) {
        console.error('获取图书数量失败:', error)
        this.bookCount = {}
      }
    },
    async getTodoStats() {
      try {
        const res = await api.home.getTodoStats()
        this.todoStats = res || {}
      } catch (error) {
        console.error('获取待办事项统计失败:', error)
        this.todoStats = { pendingDonationApplyCount: 0, inventoryWarningCount: 0 }
      }
    },
    async getSalesStats() {
      try {
        const res = await api.home.getSalesStats()
        this.salesStats = {
          today: res.today || { orderCount: 0, totalSales: 0, totalRevenue: 0 },
          week: res.week || { orderCount: 0, totalSales: 0, totalRevenue: 0 },
          month: res.month || { orderCount: 0, totalSales: 0, totalRevenue: 0 }
        }
        this.topBooks = res.topBooks || []
        this.salesTrend = res.salesTrend || []
        this.$nextTick(() => {
          this.updateSalesTrendChart()
        })
      } catch (error) {
        console.error('获取销售统计失败:', error)
      }
    },
    initSalesTrendChart() {
      this.salesTrendChart = echarts.init(this.$refs.salesTrendChart)
      this.updateSalesTrendChart()
    },
    updateSalesTrendChart() {
      if (!this.salesTrendChart || !this.salesTrend || this.salesTrend.length === 0) return

      const dates = this.salesTrend.map(item => item.date)
      const salesVolumes = this.salesTrend.map(item => item.salesVolume)
      const revenues = this.salesTrend.map(item => item.revenue)

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' }
        },
        legend: {
          data: ['销量', '销售额']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            formatter: (value) => value.substring(5)
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '销量',
            position: 'left'
          },
          {
            type: 'value',
            name: '销售额(元)',
            position: 'right'
          }
        ],
        series: [
          {
            name: '销量',
            type: 'bar',
            data: salesVolumes,
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '销售额',
            type: 'line',
            yAxisIndex: 1,
            data: revenues,
            itemStyle: { color: '#67C23A' },
            smooth: true
          }
        ]
      }

      this.salesTrendChart.setOption(option)
    },
    goToAddBook() {
      this.$router.push('/admin/book/add')
    },
    goToDonation() {
      this.$router.push('/admin/donation-manage')
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
  
  .sales-stat-card {
    .sales-stats {
      display: flex;
      justify-content: space-around;
      
      .stats-item {
        text-align: center;
        
        .stats-label {
          font-size: 12px;
          color: #909399;
          margin-bottom: 8px;
        }
        
        .stats-value {
          font-size: 20px;
          font-weight: bold;
          color: #409EFF;
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
