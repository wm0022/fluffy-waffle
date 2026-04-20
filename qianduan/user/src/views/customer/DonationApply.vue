<template>
  <div class="donation-apply">
    <p>图书捐赠申请页面开发中...</p>
    <el-button @click="goBack">返回</el-button>
  </div>
</template>

<script>
export default {
  name: 'DonationApply',
  methods: {
    goBack() {
      this.$router.back()
    }
  }
}
</script>
