<template>
  <div class="book-list-container">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="书名">
            <el-input v-model="queryForm.bookName" placeholder="请输入书名" clearable />
          </el-form-item>
          <el-form-item label="作者">
            <el-input v-model="queryForm.author" placeholder="请输入作者" clearable />
          </el-form-item>
          <el-form-item label="ISBN">
            <el-input v-model="queryForm.isbn" placeholder="请输入 ISBN" clearable />
          </el-form-item>
          <el-form-item label="出版社">
            <el-input v-model="queryForm.publisher" placeholder="请输入出版社" clearable />
          </el-form-item>
          <el-form-item label="上下架">
            <el-select v-model="queryForm.shelfStatus" placeholder="请选择" clearable>
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="table-toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加图书</el-button>
        <el-button type="success" icon="el-icon-download">导出</el-button>
      </div>
      
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        @sort-change="handleSortChange"
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
        <el-table-column label="捐赠" width="70">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isDonation === 1" type="warning" size="mini">是</el-tag>
            <el-tag v-else type="info" size="mini">否</el-tag>
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
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'BookList',
  data() {
    return {
      queryForm: {
        bookName: '',
        author: '',
        isbn: '',
        publisher: '',
        shelfStatus: null,
        sortBy: ''
      },
      tableData: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          ...this.queryForm
        }
        const res = await api.book.pageList(params)
        // res 已经是后端返回的 data 对象，包含 records 和 total
        this.tableData = res.records || []
        this.total = res.total || 0
      } catch (error) {
        console.error('获取图书列表失败:', error)
        this.tableData = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.getList()
    },
    handleReset() {
      this.queryForm = {
        bookName: '',
        author: '',
        isbn: '',
        publisher: '',
        shelfStatus: null,
        sortBy: ''
      }
      this.handleSearch()
    },
    handleSortChange(column) {
      if (column.prop === 'salesVolume') {
        this.queryForm.sortBy = 'sales'
      } else if (column.prop === 'shelfTime') {
        this.queryForm.sortBy = 'time'
      } else {
        this.queryForm.sortBy = ''
      }
      this.getList()
    },
    handleAdd() {
      this.$router.push('/admin/book/add')
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
        this.getList()
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
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getList()
    },
    handleCurrentChange(page) {
      this.pageNum = page
      this.getList()
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
