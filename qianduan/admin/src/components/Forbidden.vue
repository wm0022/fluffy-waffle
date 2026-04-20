<template>
  <div class="forbidden-page">
    <div class="forbidden-content">
      <h1 class="code">403</h1>
      <h2>无访问权限</h2>
      <p>抱歉，您没有权限访问该页面。如需帮助，请联系管理员。</p>
      <el-button type="primary" @click="$router.push('/admin/home')">返回首页</el-button>
      <el-button @click="$router.back()">返回上页</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Forbidden'
}
</script>

<style lang="scss" scoped>
.forbidden-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;

  .forbidden-content {
    text-align: center;

    .code {
      font-size: 120px;
      color: #e6a23c;
      font-weight: 700;
      margin: 0;
      line-height: 1.2;
    }

    h2 {
      color: #303133;
      margin: 16px 0 12px;
    }

    p {
      color: #909399;
      margin-bottom: 24px;
    }
  }
}
</style>
