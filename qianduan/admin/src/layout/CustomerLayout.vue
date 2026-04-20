<template>
  <div class="customer-layout">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'CustomerLayout'
}
</script>

<style lang="scss" scoped>
.customer-layout {
  min-height: 100vh;
}
</style>
