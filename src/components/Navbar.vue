<script setup lang="ts">
import { useRouter } from 'vue-router'
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { authService } from '@/services/authService'

const router = useRouter()
const authStore = useAuthStore()

const navigateTo = (path: string) => {
  router.push(path)
}

const currentUser = computed(() => authStore.user)
const isAuthenticated = computed(() => authStore.isAuthenticated)
const isCustomer = computed(() => authStore.user?.role === 'Customer')

const handleLogout = async () => {
  try {
    await authService.logout()
    router.push('/login')
  } catch (error) {
    console.error('Logout failed:', error)
  }
}
</script>

<template>
  <div class="navbar">
    <div class="navbar-bg"></div>
    <div class="nav-links">
      <svg class="logo" width="56" height="32" viewBox="0 0 56 32" fill="none" xmlns="http://www.w3.org/2000/svg" @click="navigateTo('/')">
        <path d="M47.0332 24.8999H55.3332L27.6666 0L0 24.8999H8.29997L27.6666 7.44231L47.0332 24.8999ZM13.8333 5.00765V0H5.53332V12.45L13.8333 5.00765Z" fill="#7C6A46"/>
        <path d="M25.8458 23.7379C25.8458 24.4462 25.6798 25.0936 25.3478 25.6801C25.0269 26.2556 24.5344 26.7204 23.8704 27.0745C23.2175 27.4176 22.4151 27.5891 21.4634 27.5891H19.8532V31.5399H16.5996V19.8369H21.4634C22.4041 19.8369 23.2009 20.0029 23.8538 20.3349C24.5178 20.6669 25.0158 21.1262 25.3478 21.7127C25.6798 22.2992 25.8458 22.9743 25.8458 23.7379ZM21.148 24.9995C22.0665 24.9995 22.5258 24.579 22.5258 23.7379C22.5258 22.8858 22.0665 22.4597 21.148 22.4597H19.8532V24.9995H21.148Z" fill="#7C6A46"/>
        <path d="M38.5405 19.8369L34.5067 31.5399H30.3401L26.2897 19.8369H29.7757L32.4317 28.2863L35.0711 19.8369H38.5405Z" fill="#7C6A46"/>
      </svg>
      <div class="links">
        <div class="link" :class="{ active: $route.path === '/' }" @click="navigateTo('/')">Home</div>
        <div v-if="!isCustomer" class="link" :class="{ active: $route.path.startsWith('/property') }" @click="navigateTo('/property')">Properties</div>
        <div class="link" :class="{ active: $route.path.startsWith('/booking') }" @click="navigateTo('/booking')">Bookings</div>
        <div class="link" :class="{ active: $route.path.startsWith('/support') }" @click="navigateTo('/support')">Support</div>
        <div class="link" :class="{ active: $route.path.startsWith('/reviews') }" @click="navigateTo('/reviews/my-reviews')">Reviews</div>
      </div>
      
      <!-- Auth Container -->
      <div class="auth-section">
        <div v-if="isAuthenticated" class="user-info" @click="navigateTo('/profile')">
          <div class="user-avatar">{{ currentUser?.name?.charAt(0).toUpperCase() }}</div>
          <div class="user-details">
            <div class="user-name">{{ currentUser?.name }}</div>
            <div class="user-role">{{ currentUser?.role }}</div>
          </div>
        </div>
        <button v-if="isAuthenticated" class="logout-btn" @click="handleLogout">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M17 7L15.59 8.41L18.17 11H8V13H18.17L15.59 15.58L17 17L22 12L17 7ZM4 5H12V3H4C2.9 3 2 3.9 2 5V19C2 20.1 2.9 21 4 21H12V19H4V5Z" fill="currentColor"/>
          </svg>
        </button>
        <div v-if="!isAuthenticated" class="guest-actions">
          <button class="login-btn" @click="navigateTo('/login')">Login</button>
        </div>
        <div class="book-btn" @click="navigateTo('/booking/create')">
          <div class="btn-bg"></div>
          <div class="btn-text">Book now</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@500;700&display=swap');

.navbar {
  width: 100%;
  height: 120px;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
}

.navbar-bg {
  width: 100%;
  height: 120px;
  background: #FAFAFA;
  position: absolute;
  left: 0;
  top: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.nav-links {
  width: calc(100% - 240px);
  max-width: 1400px;
  height: 55px;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  top: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 30px;
}

.logo {
  width: 55px;
  height: 32px;
  cursor: pointer;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.logo:hover {
  transform: scale(1.05);
}

.links {
  display: flex;
  gap: 40px;
  align-items: center;
  flex: 1;
  justify-content: center;
}

.link {
  color: #000;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 0;
  white-space: nowrap;
}

.link.active {
  color: #7C6A46;
  font-weight: 700;
  border-bottom: 2px solid #7C6A46;
}

.link:hover {
  color: #7C6A46;
}

/* Auth Section */
.auth-section {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 30px;
  border: 2px solid #F0F0F0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-info:hover {
  border-color: #7C6A46;
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.15);
}

.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
  color: #FFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.user-name {
  color: #1C1C1C;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.user-role {
  color: #7C6A46;
  font-family: 'Poppins', sans-serif;
  font-size: 11px;
  font-weight: 500;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 50%;
  background: #F44336;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.logout-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.3);
}

.guest-actions {
  display: flex;
  gap: 10px;
}

.login-btn {
  padding: 10px 24px;
  border: 2px solid #7C6A46;
  border-radius: 25px;
  background: white;
  color: #7C6A46;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.login-btn:hover {
  background: #7C6A46;
  color: white;
  transform: translateY(-2px);
}

.book-btn {
  width: 145px;
  height: 50px;
  position: relative;
  cursor: pointer;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.book-btn:hover {
  transform: translateY(-2px);
}

.btn-bg {
  width: 145px;
  height: 50px;
  border-radius: 5px;
  background: #7C6A46;
  position: absolute;
}

.btn-text {
  color: #FFF;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 500;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  white-space: nowrap;
}

@media (max-width: 1400px) {
  .nav-links {
    width: calc(100% - 80px);
  }
}

@media (max-width: 768px) {
  .links {
    display: none;
  }
  
  .nav-links {
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  .book-btn {
    width: 140px;
    height: 48px;
  }
  
  .btn-bg {
    width: 140px;
    height: 48px;
  }
  
  .btn-text {
    font-size: 14px;
    left: 35px;
    top: 14px;
  }
}
</style>
