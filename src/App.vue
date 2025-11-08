<script setup lang="ts">
import { RouterView } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
</script>

<template>
  <div class="app-container">
    <Navbar />
  
    <main class="app-main">
      <RouterView />
    </main>
    <Footer />
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
  padding-top: 120px;
}

.app-main {
  flex: 1;
  width: 100%;
  background: #FFF;
}
</style>

