<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-container">
    <header class="app-header">
      <div class="header-content">
        <h1 class="app-title">🏨 Accommodation Management System</h1>
        <nav class="main-nav">
          <router-link to="/" class="nav-link">Home</router-link>
          <router-link to="/property" class="nav-link">Properties</router-link>
          <router-link to="/booking" class="nav-link">Bookings</router-link>
          <router-link to="/booking/chart" class="nav-link">Statistics</router-link>
        </nav>
      </div>
    </header>

    <main class="app-main">
      <RouterView />
    </main>

    <footer class="app-footer">
      <p>&copy; 2025 Accommodation Management System | NPM 2306212083</p>
    </footer>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-header {
  background: linear-gradient(135deg, #4caf50 0%, #2196f3 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 15px 20px;
}

.app-title {
  margin: 0 0 15px 0;
  font-size: 24px;
  font-weight: bold;
  text-align: center;
}

.main-nav {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}

.nav-link {
  padding: 10px 20px;
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  text-decoration: none;
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.3s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.nav-link.router-link-active {
  background-color: rgba(255, 255, 255, 0.4);
  font-weight: bold;
}

.app-main {
  flex: 1;
  background-color: #f5f5f5;
  padding: 20px 0;
}

.app-footer {
  background-color: #333;
  color: white;
  text-align: center;
  padding: 20px;
  margin-top: auto;
}

.app-footer p {
  margin: 0;
  font-size: 14px;
}
</style>
