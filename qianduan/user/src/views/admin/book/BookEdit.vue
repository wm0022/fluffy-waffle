<template>
  <div class="book-edit">
    <el-card>
      <div slot="header">编辑图书</div>
      <p>编辑图书功能开发中...</p>
      <el-button @click="goBack">返回</el-button>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'BookEdit',
  methods: {
    goBack() {
      this.$router.back()
    }
  }
}
</script>
