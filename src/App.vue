<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'

const route = useRoute()

// Hide navbar and footer on login page
const isLoginPage = computed(() => route.name === 'login')
</script>

<template>
  <div class="app-container" :class="{ 'no-chrome': isLoginPage }">
    <Navbar v-if="!isLoginPage" />
  
    <main class="app-main" :class="{ 'full-height': isLoginPage }">
      <RouterView />
    </main>
    <Footer v-if="!isLoginPage" />
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

.app-container.no-chrome {
  padding-top: 0;
}

.app-main {
  flex: 1;
  width: 100%;
  background: #FFF;
}

.app-main.full-height {
  min-height: 100vh;
}
</style>

