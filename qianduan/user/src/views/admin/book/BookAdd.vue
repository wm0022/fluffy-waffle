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
            <el-form-item label="译者">
              <el-input v-model="bookForm.translator" placeholder="请输入译者" />
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
              <el-input v-model="bookForm.edition" placeholder="请输入版次" />
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
          <el-col :span="12">
            <el-form-item label="定价" prop="price">
              <el-input-number v-model="bookForm.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售价">
              <el-input-number v-model="bookForm.salePrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="12">
            <el-form-item label="字数">
              <el-input-number v-model="bookForm.wordCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="页数">
              <el-input-number v-model="bookForm.pageCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="12">
            <el-form-item label="装帧">
              <el-select v-model="bookForm.binding" placeholder="请选择装帧">
                <el-option label="平装" value="平装" />
                <el-option label="精装" value="精装" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="语言">
              <el-select v-model="bookForm.language" placeholder="请选择语言">
                <el-option label="中文" value="中文" />
                <el-option label="英文" value="英文" />
                <el-option label="其他" value="其他" />
              </el-select>
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
            <el-form-item label="预警库存">
              <el-input-number v-model="bookForm.warningStock" :min="0" style="width: 100%" />
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
          <el-col :span="12">
            <el-form-item label="是否热销">
              <el-radio-group v-model="bookForm.isHot">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否新品">
              <el-radio-group v-model="bookForm.isNew">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="12">
            <el-form-item label="是否捐赠图书">
              <el-radio-group v-model="bookForm.isDonation">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="捐赠折扣">
              <el-input-number v-model="bookForm.donationDiscount" :min="0" :max="10" :precision="2" :step="0.1" style="width: 100%" />
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
        translator: '',
        publisher: '',
        publishDate: '',
        edition: '',
        printDate: '',
        price: 0,
        salePrice: 0,
        wordCount: 0,
        pageCount: 0,
        binding: '平装',
        language: '中文',
        stockCount: 0,
        warningStock: 10,
        description: '',
        isHot: 0,
        isNew: 0,
        isDonation: 0,
        donationDiscount: 1.00
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
        price: [
          { required: true, message: '请输入定价', trigger: 'blur' }
        ],
        stockCount: [
          { required: true, message: '请输入库存数量', trigger: 'blur' }
        ]
      },
      submitLoading: false
    }
  },
  methods: {
    async handleSubmit() {
      try {
        await this.$refs.bookForm.validate()
        this.submitLoading = true
        
        if (this.bookForm.salePrice === 0) {
          this.bookForm.salePrice = this.bookForm.price
        }
        
        await api.book.add(this.bookForm)
        this.$message.success('添加成功')
        this.goBack()
      } catch (error) {
        console.error(error)
      } finally {
        this.submitLoading = false
      }
    },
    goBack() {
      this.$router.back()
    }
  }
}
</script>

<style lang="scss" scoped>
.book-add-container {
  .el-card {
    margin-bottom: 20px;
  }
}
</style>
