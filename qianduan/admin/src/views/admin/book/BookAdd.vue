<template>
  <div class="book-add-container">
    <el-card>
      <div slot="header">
        <span>添加图书</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="goBack">返回</el-button>
      </div>

      <el-form
        ref="bookForm"
        :model="bookForm"
        :rules="bookRules"
        label-width="120px"
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
            <el-form-item label="编者">
              <el-input v-model="bookForm.editor" placeholder="请输入编者/译者" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="版次">
              <el-input v-model="bookForm.edition" placeholder="如：第1版" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
          <el-col :span="8">
            <el-form-item label="定价" prop="originalPrice">
              <el-input-number v-model="bookForm.originalPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="售价" prop="sellingPrice">
              <el-input-number v-model="bookForm.sellingPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="折扣">
              <el-input-number v-model="bookForm.discount" :min="0" :max="10" :precision="2" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="库存数量" prop="stockCount">
              <el-input-number v-model="bookForm.stockCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上架状态">
              <el-radio-group v-model="bookForm.shelfStatus">
                <el-radio :label="1">上架</el-radio>
                <el-radio :label="0">下架</el-radio>
              </el-radio-group>
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
                <img v-if="bookForm.coverImage" :src="bookForm.coverImage" class="cover-image" />
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

        <el-row>
          <el-col :span="8">
            <el-form-item label="是否热销">
              <el-radio-group v-model="bookForm.isHot">
                <el-radio label="1">是</el-radio>
                <el-radio label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否新品">
              <el-radio-group v-model="bookForm.isNew">
                <el-radio label="1">是</el-radio>
                <el-radio label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否捐赠图书">
              <el-radio-group v-model="bookForm.isDonation">
                <el-radio label="1">是</el-radio>
                <el-radio label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'BookAdd',
  data() {
    return {
      bookForm: {
        isbn: '',
        bookName: '',
        author: '',
        editor: '',
        publisher: '',
        publishDate: '',
        edition: '',
        printDate: '',
        originalPrice: null,
        sellingPrice: null,
        discount: null,
        stockCount: 0,
        shelfStatus: 1,
        coverImage: '',
        description: '',
        isHot: '0',
        isNew: '0',
        isDonation: '0'
      },
      bookRules: {
        isbn: [
          { required: true, message: '请输入 ISBN 编号', trigger: 'blur' }
        ],
        bookName: [
          { required: true, message: '请输入书名', trigger: 'blur' }
        ],
        author: [
          { required: true, message: '请输入作者', trigger: 'blur' }
        ],
        publisher: [
          { required: true, message: '请输入出版社', trigger: 'blur' }
        ],
        originalPrice: [
          { required: true, message: '请输入定价', trigger: 'blur' }
        ],
        sellingPrice: [
          { required: true, message: '请输入售价', trigger: 'blur' }
        ],
        stockCount: [
          { required: true, message: '请输入库存数量', trigger: 'blur' }
        ]
      },
      submitLoading: false
    }
  },
  created() {
    if (this.$route.query.donation === '1') {
      this.bookForm.isDonation = '1'
    }
  },
  methods: {
    async handleSubmit() {
      try {
        await this.$refs.bookForm.validate()
        this.submitLoading = true

        const submitData = {
          isbn: this.bookForm.isbn,
          bookName: this.bookForm.bookName,
          author: this.bookForm.author,
          editor: this.bookForm.editor || null,
          publisher: this.bookForm.publisher,
          publishDate: this.bookForm.publishDate || null,
          edition: this.bookForm.edition || null,
          printDate: this.bookForm.printDate || null,
          originalPrice: this.bookForm.originalPrice,
          sellingPrice: this.bookForm.sellingPrice,
          discount: this.bookForm.discount,
          stockCount: this.bookForm.stockCount || 0,
          shelfStatus: this.bookForm.shelfStatus,
          shelfTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
          coverImage: this.bookForm.coverImage || '',
          description: this.bookForm.description || '',
          isDonation: parseInt(this.bookForm.isDonation) || 0,
          isHot: parseInt(this.bookForm.isHot) || 0,
          isNew: parseInt(this.bookForm.isNew) || 0
        }

        await api.book.add(submitData)
        this.$message.success('添加成功')
        this.goBack()
      } catch (error) {
        console.error('添加图书失败:', error)
        this.$message.error('添加失败: ' + (error.message || '未知错误'))
      } finally {
        this.submitLoading = false
      }
    },
    goBack() {
      const query = this.bookForm.isDonation === '1' ? { donation: '1' } : {}
      this.$router.push({ path: '/admin/book', query })
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
        this.bookForm.coverImage = 'http://localhost:8080/api' + res.url
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
.book-add-container {
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
      object-fit: cover;
      border-radius: 6px;
    }
  }
}
</style>
