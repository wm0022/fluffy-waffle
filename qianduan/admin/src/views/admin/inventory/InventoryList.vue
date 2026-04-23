<template>
  <div class="inventory-management">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="库存监控" name="monitor">
        <el-card>
          <div slot="header">
            <span>库存实时监控</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="loadInventory">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
          
          <el-table :data="inventoryList" style="width: 100%" v-loading="loading" border>
            <el-table-column prop="bookName" label="图书名称" min-width="180" />
            <el-table-column prop="author" label="作者" min-width="100" />
            <el-table-column label="当前库存" min-width="100" align="center">
              <template slot-scope="scope">
                <span :class="getStockClass(scope.row)">{{ scope.row.stockQuantity }}</span>
              </template>
            </el-table-column>
            <el-table-column label="可用库存" min-width="100" prop="availableQuantity" align="center" />
            <el-table-column label="锁定库存" min-width="100" prop="lockedQuantity" align="center" />
            <el-table-column label="最低库存" min-width="140" align="center">
              <template slot-scope="scope">
                <el-input-number v-model="scope.row.minStock" size="mini" :min="0" :max="1000"
                  :controls="true" controls-position="right" style="width: 110px;"
                  @change="() => handleUpdate(scope.row)" />
              </template>
            </el-table-column>
            <el-table-column label="库存状态" min-width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.stockStatus)">
                  {{ getStatusText(scope.row.stockStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="库存预警" name="warning">
        <el-card>
          <div slot="header">
            <span>库存预警列表</span>
            <el-badge :value="warningList.length" :max="99" style="float: right; margin-top: 5px;">
              <el-button type="danger" size="mini" @click="loadWarningList">刷新预警</el-button>
            </el-badge>
          </div>
          
          <el-alert
            v-if="warningList.length === 0"
            title="暂无库存预警"
            type="success"
            show-icon
            style="margin-bottom: 20px;"
          />
          
          <el-table :data="warningList" style="width: 100%" v-loading="loading">
            <el-table-column prop="bookName" label="图书名称" width="200" />
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column label="当前库存" width="100">
              <template slot-scope="scope">
                <span :style="{ color: scope.row.stockQuantity <= 0 ? '#f56c6c' : '#e6a23c', fontWeight: 'bold' }">
                  {{ scope.row.stockQuantity }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="最低库存" width="100" prop="minStock" />
            <el-table-column label="短缺数量" width="100">
              <template slot-scope="scope">
                <span style="color: #f56c6c; font-weight: bold;">{{ scope.row.shortage }}</span>
              </template>
            </el-table-column>
            <el-table-column label="预警级别" width="120">
              <template slot-scope="scope">
                <el-tag :type="getWarningType(scope.row.warningLevel)">
                  {{ scope.row.warningLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" @click="handleRestock(scope.row)">补货</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="销售统计" name="sales">
        <el-card>
          <div slot="header">
            <span>销售统计分析</span>
            <el-select v-model="periodDays" size="small" style="float: right;" @change="loadSalesStats">
              <el-option label="最近7天" :value="7" />
              <el-option label="最近30天" :value="30" />
              <el-option label="最近90天" :value="90" />
            </el-select>
          </div>
          
          <el-row :gutter="20" style="margin-bottom: 20px;">
            <el-col :span="12">
              <el-card>
                <div class="stat-item">
                  <span class="stat-label">总销量</span>
                  <span class="stat-value">{{ salesStats.totalSales }} 本</span>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <div class="stat-item">
                  <span class="stat-label">总销售额</span>
                  <span class="stat-value">¥{{ salesStats.totalRevenue }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-card style="margin-bottom: 20px;">
            <div slot="header">畅销图书TOP10</div>
            <el-table :data="salesStats.topBooks" style="width: 100%">
              <el-table-column type="index" label="排名" width="60" />
              <el-table-column prop="bookName" label="图书名称" />
              <el-table-column prop="salesVolume" label="销量" width="100" />
              <el-table-column label="销售额" width="120">
                <template slot-scope="scope">
                  ¥{{ scope.row.revenue }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <el-card>
            <div slot="header">每日销售趋势</div>
            <div ref="salesChart" style="height: 400px;"></div>
          </el-card>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="智能补货" name="restock">
        <el-card>
          <div slot="header">
            <span>智能进货建议</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="loadRestockSuggestions">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
          
          <el-alert
            title="补货建议基于近30天销售数据自动生成，仅供参考"
            type="info"
            show-icon
            style="margin-bottom: 20px;"
            :closable="false"
          />
          
          <el-table :data="restockList" style="width: 100%" v-loading="loading">
            <el-table-column prop="bookName" label="图书名称" width="200" />
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column label="当前库存" width="100">
              <template slot-scope="scope">
                <span :class="getStockClass(scope.row)">{{ scope.row.currentStock }}</span>
              </template>
            </el-table-column>
            <el-table-column label="月销量" width="80" prop="monthlySales" />
            <el-table-column label="日均销量" width="80" prop="avgDailySales" />
            <el-table-column label="库存天数" width="80">
              <template slot-scope="scope">
                {{ scope.row.daysOfStock > 99 ? '99+' : scope.row.daysOfStock }} 天
              </template>
            </el-table-column>
            <el-table-column label="补货级别" width="100">
              <template slot-scope="scope">
                <el-tag :type="getRestockType(scope.row.suggestionLevel)">
                  {{ scope.row.suggestionLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="建议补货量" width="100">
              <template slot-scope="scope">
                <span v-if="scope.row.suggestQuantity > 0" style="color: #f56c6c; font-weight: bold;">
                  {{ scope.row.suggestQuantity }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="预估成本" width="100">
              <template slot-scope="scope">
                ¥{{ scope.row.estimatedCost }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="'补货 - ' + (currentBook ? currentBook.bookName : '')" :visible.sync="restockDialogVisible" width="400px">
      <el-form :model="restockForm" label-width="100px">
        <el-form-item label="当前库存">
          <span>{{ currentBook ? currentBook.stockQuantity : 0 }}</span>
        </el-form-item>
        <el-form-item label="补货数量">
          <el-input-number v-model="restockForm.quantity" :min="1" :max="1000" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="restockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRestock" :loading="restockLoading">确定补货</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api'
import * as echarts from 'echarts'

export default {
  name: 'InventoryList',
  data() {
    return {
      activeTab: 'monitor',
      loading: false,
      inventoryList: [],
      warningList: [],
      salesStats: {
        totalSales: 0,
        totalRevenue: '0.00',
        topBooks: [],
        dailySales: []
      },
      periodDays: 7,
      restockList: [],
      restockDialogVisible: false,
      restockLoading: false,
      currentBook: null,
      restockForm: {
        quantity: 10
      },
      salesChart: null
    }
  },
  created() {
    this.loadInventory()
  },
  methods: {
    async loadInventory() {
      this.loading = true
      try {
        const res = await api.inventory.list()
        this.inventoryList = res || []
      } catch (error) {
        console.error('加载库存失败:', error)
        this.$message.error('加载库存失败')
      } finally {
        this.loading = false
      }
    },
    async loadWarningList() {
      try {
        const res = await api.inventory.warning()
        this.warningList = res || []
      } catch (error) {
        console.error('加载预警列表失败:', error)
      }
    },
    async loadSalesStats() {
      try {
        const res = await api.inventory.statistics({ days: this.periodDays })
        this.salesStats = res || {}
        this.$nextTick(() => {
          this.renderSalesChart()
        })
      } catch (error) {
        console.error('加载销售统计失败:', error)
      }
    },
    async loadRestockSuggestions() {
      try {
        const res = await api.inventory.restockSuggestions()
        this.restockList = res || []
      } catch (error) {
        console.error('加载补货建议失败:', error)
      }
    },
    handleTabClick(tab) {
      if (tab.name === 'warning') {
        this.loadWarningList()
      } else if (tab.name === 'sales') {
        this.loadSalesStats()
      } else if (tab.name === 'restock') {
        this.loadRestockSuggestions()
      }
    },
    renderSalesChart() {
      if (!this.$refs.salesChart) return
      
      if (this.salesChart) {
        this.salesChart.dispose()
      }
      
      this.salesChart = echarts.init(this.$refs.salesChart)
      
      const dates = (this.salesStats.dailySales || []).map(item => item.date)
      const salesVolumes = (this.salesStats.dailySales || []).map(item => item.salesVolume)
      const revenues = (this.salesStats.dailySales || []).map(item => parseFloat(item.revenue))
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' }
        },
        legend: {
          data: ['销量', '销售额']
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: [
          { type: 'value', name: '销量（本）', position: 'left' },
          { type: 'value', name: '销售额（元）', position: 'right' }
        ],
        series: [
          { name: '销量', type: 'line', data: salesVolumes, smooth: true },
          { name: '销售额', type: 'bar', data: revenues, yAxisIndex: 1 }
        ]
      }
      
      this.salesChart.setOption(option)
    },
    async handleUpdate(row) {
      try {
        await api.inventory.update({ inventoryId: row.inventoryId, minStock: row.minStock })
        this.$message.success('最低库存已更新')
      } catch (error) {
        console.error('更新最低库存失败:', error)
        this.$message.error('更新最低库存失败')
      }
    },
    handleRestock(row) {
      this.currentBook = row
      this.restockForm.quantity = row.shortage > 0 ? row.shortage : 10
      this.restockDialogVisible = true
    },
    async submitRestock() {
      if (!this.currentBook || !this.restockForm.quantity || this.restockForm.quantity <= 0) {
        this.$message.warning('请输入有效的补货数量')
        return
      }
      this.restockLoading = true
      try {
        // 补货 = 当前库存 + 补货数量 → 通过 update 接口触发 increaseStock
        const newStock = (this.currentBook.stockQuantity || 0) + this.restockForm.quantity
        await api.inventory.update({
          inventoryId: this.currentBook.inventoryId,
          stockQuantity: newStock
        })
        this.$message.success('补货成功')
        this.restockDialogVisible = false
        this.loadInventory()
        this.loadWarningList()
      } catch (error) {
        console.error('补货失败:', error)
        this.$message.error('补货失败，请重试')
      } finally {
        this.restockLoading = false
      }
    },
    getStockClass(row) {
      if (row.stockQuantity <= 0 || row.currentStock <= 0) {
        return 'stock-danger'
      } else if (row.stockQuantity <= (row.minStock || 10) || row.currentStock <= (row.minStock || 10)) {
        return 'stock-warning'
      }
      return 'stock-normal'
    },
    getStatusType(status) {
      if (status === 3) return 'danger'
      if (status === 2) return 'warning'
      return 'success'
    },
    getStatusText(status) {
      if (status === 3) return '缺货'
      if (status === 2) return '偏低'
      return '正常'
    },
    getWarningType(level) {
      if (level === '严重缺货') return 'danger'
      if (level === '库存紧张') return 'warning'
      return 'info'
    },
    getRestockType(level) {
      if (level === '紧急补货') return 'danger'
      if (level === '建议补货') return 'warning'
      if (level === '库存积压') return 'info'
      return 'success'
    }
  }
}
</script>

<style lang="scss" scoped>
.inventory-management {
  padding: 20px;
  
  .stat-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .stat-label {
      font-size: 14px;
      color: #909399;
    }
    
    .stat-value {
      font-size: 20px;
      font-weight: bold;
      color: #303133;
    }
  }
  
  .stock-danger {
    color: #f56c6c;
    font-weight: bold;
  }
  
  .stock-warning {
    color: #e6a23c;
    font-weight: bold;
  }
  
  .stock-normal {
    color: #67c23a;
  }
}
</style>
