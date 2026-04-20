<template>
  <div class="book-edit-container">
    <el-card>
      <div slot="header">
        <span>编辑图书</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="goBack">返回</el-button>
      </div>
      
      <el-form
        ref="bookForm"
        :model="bookForm"
        :rules="bookRules"
        label-width="120px"
        v-loading="loading"
      >
        <el-row>
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="bookForm.isbn" placeholder="请输入 ISBN 编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="书名" prop="bookName">
              <el-input v-model="bookForm.bookName" placeholder="请输入书名" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="bookForm.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="8">
            <el-form-item label="出版日期">
              <el-date-picker
                v-model="bookForm.publishDate"
                type="date"
                placeholder="选择出版日期"
                style="width: 100%"
                value-format="yyyy-MM-dd"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="版次">
              <el-input v-model="bookForm.edition" placeholder="如：第1版" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="印刷日期">
              <el-date-picker
                v-model="bookForm.printDate"
                type="date"
                placeholder="选择印刷日期"
                style="width: 100%"
                value-format="yyyy-MM-dd"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="12">
            <el-form-item label="售价" prop="sellingPrice">
              <el-input-number v-model="bookForm.sellingPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存数量" prop="stockCount">
              <el-input-number v-model="bookForm.stockCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="12">
            <el-form-item label="销量">
              <el-input-number v-model="bookForm.salesVolume" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上架时间">
              <el-date-picker
                v-model="bookForm.shelfTime"
                type="datetime"
                placeholder="选择上架时间"
                style="width: 100%"
                value-format="yyyy-MM-dd HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="24">
            <el-form-item label="图书封面">
              <el-upload
                class="cover-uploader"
                drag
                action=""
                :http-request="uploadCover"
                :show-file-list="false"
                :before-upload="beforeCoverUpload"
              >
                <img v-if="bookForm.coverImage" :src="getCoverUrl(bookForm.coverImage)" class="cover-image" />
                <i v-else class="el-icon-plus cover-icon"></i>
                <div class="el-upload__text">
                  将封面图片拖到此处，或<em>点击上传</em>
                </div>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="图书描述">
              <el-input
                v-model="bookForm.description"
                type="textarea"
                :rows="4"
                placeholder="请输入图书描述"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'BookEdit',
  data() {
    return {
      loading: false,
      submitLoading: false,
      bookForm: {
        id: null,
        isbn: '',
        bookName: '',
        author: '',
        publisher: '',
        publishDate: '',
        edition: '',
        printDate: '',
        sellingPrice: 0,
        stockCount: 0,
        salesVolume: 0,
        shelfStatus: 0,
        shelfTime: '',
        coverImage: '',
        description: '',
        isDonation: 0
      },
      bookRules: {
        bookName: [
          { required: true, message: '请输入书名', trigger: 'blur' }
        ],
        author: [
          { required: true, message: '请输入作者', trigger: 'blur' }
        ],
        publisher: [
          { required: true, message: '请输入出版社', trigger: 'blur' }
        ],
        sellingPrice: [
          { required: true, message: '请输入售价', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadBookData()
  },
  methods: {
    async loadBookData() {
      const bookId = this.$route.params.id
      if (!bookId) {
        this.$message.error('图书ID不存在')
        this.goBack()
        return
      }
      
      this.loading = true
      try {
        const res = await api.book.getById(bookId)
        if (res) {
          this.bookForm = {
            id: res.id,
            isbn: res.isbn || '',
            bookName: res.bookName || '',
            author: res.author || '',
            publisher: res.publisher || '',
            publishDate: res.publishDate || '',
            edition: res.edition || '',
            printDate: res.printDate || '',
            sellingPrice: res.sellingPrice || 0,
            stockCount: res.stockCount || 0,
            salesVolume: res.salesVolume || 0,
            shelfStatus: res.shelfStatus || 0,
            shelfTime: res.shelfTime || '',
            coverImage: res.coverImage || '',
            description: res.description || '',
            isDonation: res.isDonation || 0
          }
        }
      } catch (error) {
        console.error('加载图书数据失败:', error)
        this.$message.error('加载图书数据失败')
      } finally {
        this.loading = false
      }
    },
    async handleSubmit() {
      try {
        await this.$refs.bookForm.validate()
        this.submitLoading = true
        
        // 获取当前时间作为上架时间（如果未填写）
        const now = new Date()
        const shelfTime = this.bookForm.shelfTime || now.toISOString().slice(0, 19).replace('T', ' ')
        
        const submitData = {
          id: this.bookForm.id,
          bookName: this.bookForm.bookName,
          author: this.bookForm.author,
          publisher: this.bookForm.publisher,
          publishDate: this.bookForm.publishDate || null,
          edition: this.bookForm.edition || null,
          printDate: this.bookForm.printDate || null,
          isbn: this.bookForm.isbn || null,
          sellingPrice: this.bookForm.sellingPrice || 0,
          stockCount: this.bookForm.stockCount || 0,
          salesVolume: this.bookForm.salesVolume || 0,
          shelfStatus: this.bookForm.shelfStatus,
          shelfTime: shelfTime || null,
          coverImage: this.bookForm.coverImage || '',
          description: this.bookForm.description || '',
          isDonation: this.bookForm.isDonation || 0
        }
        
        await api.book.update(submitData)
        this.$message.success('保存成功')
        this.goBack()
      } catch (error) {
        console.error('保存失败:', error)
        this.$message.error('保存失败: ' + (error.message || '未知错误'))
      } finally {
        this.submitLoading = false
      }
    },
    goBack() {
      this.$router.back()
    },
    getCoverUrl(coverImage) {
      if (!coverImage) return ''
      if (coverImage.startsWith('http')) return coverImage
      return 'http://localhost:8080/api' + coverImage
    },
    beforeCoverUpload(file) {
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('只能上传 JPG/PNG/GIF 格式的图片!')
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB!')
      }
      return isImage && isLt2M
    },
    async uploadCover(file) {
      const formData = new FormData()
      formData.append('file', file.file)
      
      try {
        const res = await api.upload.bookCover(formData)
        this.bookForm.coverImage = res.url
        this.$message.success('封面上传成功')
      } catch (error) {
        console.error('上传失败:', error)
        this.$message.error('上传失败：' + (error.message || '未知错误'))
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.book-edit-container {
  .el-card {
    margin-bottom: 20px;
  }
  
  .cover-uploader {
    width: 100%;
    
    ::v-deep .el-upload {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      width: 100%;
      
      &:hover {
        border-color: #409EFF;
      }
    }
    
    .cover-icon {
      font-size: 28px;
      color: #8c939d;
    }
    
    .cover-image {
      width: 200px;
      height: 300px;
      display: block;
      object-fit: contain;
      border-radius: 6px;
    }
  }
}
</style>
