<template>
  <div class="book-list-container">
    <el-card>
      <!-- 标签切换：普通图书 / 捐赠图书 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="普通图书" name="normal">
          <!-- 普通图书视图：搜索 + 表格 + 分页 -->
          <div class="toolbar">
            <el-form :inline="true" :model="normalQuery">
              <el-form-item label="书名">
                <el-input v-model="normalQuery.bookName" placeholder="请输入书名" clearable />
              </el-form-item>
              <el-form-item label="作者">
                <el-input v-model="normalQuery.author" placeholder="请输入作者" clearable />
              </el-form-item>
              <el-form-item label="ISBN">
                <el-input v-model="normalQuery.isbn" placeholder="请输入 ISBN" clearable />
              </el-form-item>
              <el-form-item label="出版社">
                <el-input v-model="normalQuery.publisher" placeholder="请输入出版社" clearable />
              </el-form-item>
              <el-form-item label="上下架">
                <el-select v-model="normalQuery.shelfStatus" placeholder="请选择" clearable>
                  <el-option label="上架" :value="1" />
                  <el-option label="下架" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch('normal')">查询</el-button>
                <el-button @click="handleReset('normal')">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="table-toolbar">
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd('normal')">添加图书</el-button>
            <el-button type="success" icon="el-icon-download">导出</el-button>
          </div>

          <el-table
            :data="normalData"
            v-loading="normalLoading"
            border
            stripe
            style="width: 100%"
            @sort-change="(col) => handleSortChange(col, 'normal')"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="isbn" label="ISBN" width="150" />
            <el-table-column prop="bookName" label="书名" min-width="200" show-overflow-tooltip />
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column prop="publisher" label="出版社" width="150" />
            <el-table-column prop="publishDate" label="出版日期" width="110" />
            <el-table-column prop="edition" label="版次" width="80" />
            <el-table-column prop="printDate" label="印刷日期" width="110" />
            <el-table-column prop="sellingPrice" label="售价" width="80" />
            <el-table-column prop="salesVolume" label="销量" width="80" sortable="custom" />
            <el-table-column prop="shelfTime" label="上架时间" width="180" sortable="custom" />
            <el-table-column prop="stockCount" label="库存" width="80" />
            <el-table-column label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.shelfStatus === 1 ? 'success' : 'info'">
                  {{ scope.row.shelfStatus === 1 ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button type="text" size="small" @click="handleShelf(scope.row)">
                  {{ scope.row.shelfStatus === 1 ? '下架' : '上架' }}
                </el-button>
                <el-button type="text" size="small" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              :current-page="normalPage"
              :page-size="normalPageSize"
              :total="normalTotal"
              layout="total, sizes, prev, pager, next, jumper"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="(size) => handleSizeChange(size, 'normal')"
              @current-change="(page) => handleCurrentChange(page, 'normal')"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="捐赠图书" name="donation">
          <!-- 捐赠图书视图：搜索 + 表格 + 分页（布局与普通图书完全一致） -->
          <div class="toolbar">
            <el-form :inline="true" :model="donationQuery">
              <el-form-item label="书名">
                <el-input v-model="donationQuery.bookName" placeholder="请输入书名" clearable />
              </el-form-item>
              <el-form-item label="作者">
                <el-input v-model="donationQuery.author" placeholder="请输入作者" clearable />
              </el-form-item>
              <el-form-item label="ISBN">
                <el-input v-model="donationQuery.isbn" placeholder="请输入 ISBN" clearable />
              </el-form-item>
              <el-form-item label="出版社">
                <el-input v-model="donationQuery.publisher" placeholder="请输入出版社" clearable />
              </el-form-item>
              <el-form-item label="上下架">
                <el-select v-model="donationQuery.shelfStatus" placeholder="请选择" clearable>
                  <el-option label="上架" :value="1" />
                  <el-option label="下架" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch('donation')">查询</el-button>
                <el-button @click="handleReset('donation')">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="table-toolbar">
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd('donation')">添加图书</el-button>
            <el-button type="success" icon="el-icon-download">导出</el-button>
          </div>

          <el-table
            :data="donationData"
            v-loading="donationLoading"
            border
            stripe
            style="width: 100%"
            @sort-change="(col) => handleSortChange(col, 'donation')"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="isbn" label="ISBN" width="150" />
            <el-table-column prop="bookName" label="书名" min-width="200" show-overflow-tooltip />
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column prop="publisher" label="出版社" width="150" />
            <el-table-column prop="publishDate" label="出版日期" width="110" />
            <el-table-column prop="edition" label="版次" width="80" />
            <el-table-column prop="printDate" label="印刷日期" width="110" />
            <el-table-column prop="sellingPrice" label="售价" width="80" />
            <el-table-column prop="salesVolume" label="销量" width="80" sortable="custom" />
            <el-table-column prop="shelfTime" label="上架时间" width="180" sortable="custom" />
            <el-table-column prop="stockCount" label="库存" width="80" />
            <el-table-column label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.shelfStatus === 1 ? 'success' : 'info'">
                  {{ scope.row.shelfStatus === 1 ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button type="text" size="small" @click="handleShelf(scope.row)">
                  {{ scope.row.shelfStatus === 1 ? '下架' : '上架' }}
                </el-button>
                <el-button type="text" size="small" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              :current-page="donationPage"
              :page-size="donationPageSize"
              :total="donationTotal"
              layout="total, sizes, prev, pager, next, jumper"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="(size) => handleSizeChange(size, 'donation')"
              @current-change="(page) => handleCurrentChange(page, 'donation')"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'BookList',
  data() {
    return {
      activeTab: 'normal',
      // 普通图书视图数据（isDonation = 0）
      normalQuery: this.createEmptyQuery(),
      normalData: [],
      normalLoading: false,
      normalPage: 1,
      normalPageSize: 10,
      normalTotal: 0,
      // 捐赠图书视图数据（isDonation = 1）
      donationQuery: this.createEmptyQuery(),
      donationData: [],
      donationLoading: false,
      donationPage: 1,
      donationPageSize: 10,
      donationTotal: 0
    }
  },
  created() {
    if (this.$route.query.donation === '1') {
      this.activeTab = 'donation'
    }
    this.getList(this.activeTab)
  },
  methods: {
    createEmptyQuery() {
      return {
        bookName: '',
        author: '',
        isbn: '',
        publisher: '',
        shelfStatus: null,
        sortBy: ''
      }
    },
    async getList(tab) {
      const queryObj = tab === 'normal' ? this.normalQuery : this.donationQuery
      const isNormal = tab === 'normal'
      if (isNormal) {
        this.normalLoading = true
      } else {
        this.donationLoading = true
      }
      try {
        const params = {
          pageNum: isNormal ? this.normalPage : this.donationPage,
          pageSize: isNormal ? this.normalPageSize : this.donationPageSize,
          ...queryObj,
          isDonation: isNormal ? 0 : 1
        }
        const res = await api.book.pageList(params)
        if (isNormal) {
          this.normalData = res.records || []
          this.normalTotal = res.total || 0
        } else {
          this.donationData = res.records || []
          this.donationTotal = res.total || 0
        }
      } catch (error) {
        console.error(`获取${isNormal ? '普通' : '捐赠'}图书列表失败:`, error)
        if (isNormal) {
          this.normalData = []
          this.normalTotal = 0
        } else {
          this.donationData = []
          this.donationTotal = 0
        }
      } finally {
        if (isNormal) {
          this.normalLoading = false
        } else {
          this.donationLoading = false
        }
      }
    },
    handleTabClick(tab) {
      const tabName = tab.name
      if (tabName === 'donation' && this.donationData.length === 0) {
        this.getList('donation')
      } else if (tabName === 'normal' && this.normalData.length === 0) {
        this.getList('normal')
      }
    },
    handleSearch(tab) {
      if (tab === 'normal') {
        this.normalPage = 1
      } else {
        this.donationPage = 1
      }
      this.getList(tab)
    },
    handleReset(tab) {
      if (tab === 'normal') {
        this.normalQuery = this.createEmptyQuery()
      } else {
        this.donationQuery = this.createEmptyQuery()
      }
      this.handleSearch(tab)
    },
    handleSortChange(column, tab) {
      const queryObj = tab === 'normal' ? this.normalQuery : this.donationQuery
      if (column.prop === 'salesVolume') {
        queryObj.sortBy = 'sales'
      } else if (column.prop === 'shelfTime') {
        queryObj.sortBy = 'time'
      } else {
        queryObj.sortBy = ''
      }
      this.getList(tab)
    },
    handleAdd(tab) {
      this.$router.push('/admin/book/add' + (tab === 'donation' ? '?donation=1' : ''))
    },
    handleEdit(row) {
      this.$router.push(`/admin/book/edit/${row.id}`)
    },
    async handleShelf(row) {
      const newStatus = row.shelfStatus === 1 ? 0 : 1
      const action = newStatus === 1 ? '上架' : '下架'

      try {
        await this.$confirm(`确定要${action}该图书吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await api.book.updateShelfStatus(row.id, newStatus)
        this.$message.success(`${action}成功`)
        this.refreshCurrentTab(row)
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除图书《${row.bookName}》吗？`, '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await api.book.delete(row.id)
        this.$message.success('删除成功')
        this.refreshCurrentTab(row)
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    refreshCurrentTab(row) {
      const tab = row.isDonation === 1 ? 'donation' : 'normal'
      this.getList(tab)
    },
    handleSizeChange(size, tab) {
      if (tab === 'normal') {
        this.normalPageSize = size
      } else {
        this.donationPageSize = size
      }
      this.getList(tab)
    },
    handleCurrentChange(page, tab) {
      if (tab === 'normal') {
        this.normalPage = page
      } else {
        this.donationPage = page
      }
      this.getList(tab)
    }
  }
}
</script>

<style lang="scss" scoped>
.book-list-container {
  .toolbar {
    margin-bottom: 20px;
  }
  
  .table-toolbar {
    margin-bottom: 20px;
  }
  
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
