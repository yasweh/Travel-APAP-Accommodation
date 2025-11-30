<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userService, type UserDetail } from '@/services/userService'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'

const router = useRouter()
const authStore = useAuthStore()

const userDetail = ref<UserDetail | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const isLocalData = ref(false)

onMounted(async () => {
  await fetchUserProfile()
})

const fetchUserProfile = async () => {
  loading.value = true
  error.value = null

  try {
    const response = await userService.getCurrentUserProfile()
    
    if (response.success) {
      userDetail.value = response.data
      isLocalData.value = response.isLocalData || false
    } else {
      error.value = response.message || 'Failed to fetch user profile'
    }
  } catch (e: unknown) {
    console.error('Error fetching user profile:', e)
    error.value = e instanceof Error ? e.message : 'An error occurred while fetching profile'
    
    // Fallback to auth store data
    if (authStore.user) {
      userDetail.value = {
        id: authStore.user.id,
        username: authStore.user.username,
        email: authStore.user.email,
        name: authStore.user.name,
        role: authStore.user.role
      }
      isLocalData.value = true
      error.value = null
    }
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString: string | undefined) => {
  if (!dateString) return '-'
  try {
    return new Date(dateString).toLocaleDateString('id-ID', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return dateString
  }
}

const formatCurrency = (amount: number | undefined) => {
  if (amount === undefined || amount === null) return 'Rp 0'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}

const getRoleColor = (role: string) => {
  switch (role) {
    case 'Superadmin':
      return '#E91E63'
    case 'Accommodation Owner':
      return '#2196F3'
    case 'Customer':
      return '#4CAF50'
    default:
      return '#7C6A46'
  }
}

const getRoleBadgeClass = (role: string) => {
  switch (role) {
    case 'Superadmin':
      return 'badge-superadmin'
    case 'Accommodation Owner':
      return 'badge-owner'
    case 'Customer':
      return 'badge-customer'
    default:
      return 'badge-default'
  }
}

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="page-container">
    <Navbar />
    
    <main class="main-content">
      <div class="content-wrapper">
        <!-- Back Button -->
        <button class="back-btn" @click="goBack">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 11H7.83L13.42 5.41L12 4L4 12L12 20L13.41 18.59L7.83 13H20V11Z" fill="currentColor"/>
          </svg>
          Back
        </button>
        
        <!-- Page Title -->
        <h1 class="page-title">My Profile</h1>
        
        <!-- Loading State -->
        <div v-if="loading" class="loading-container">
          <div class="spinner"></div>
          <p>Loading profile...</p>
        </div>
        
        <!-- Error State -->
        <div v-else-if="error" class="error-container">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="#F44336"/>
          </svg>
          <h3>Error Loading Profile</h3>
          <p>{{ error }}</p>
          <button class="retry-btn" @click="fetchUserProfile">Try Again</button>
        </div>
        
        <!-- Profile Content -->
        <div v-else-if="userDetail" class="profile-container">
          <!-- Local Data Notice -->
          <div v-if="isLocalData" class="notice-banner">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="currentColor"/>
            </svg>
            <span>Showing data from local session. External user service may be unavailable.</span>
          </div>
          
          <!-- Profile Card -->
          <div class="profile-card">
            <!-- Avatar Section -->
            <div class="avatar-section">
              <div class="avatar" :style="{ backgroundColor: getRoleColor(userDetail.role) }">
                {{ userDetail.name?.charAt(0).toUpperCase() || 'U' }}
              </div>
              <h2 class="user-name">{{ userDetail.name }}</h2>
              <span class="role-badge" :class="getRoleBadgeClass(userDetail.role)">
                {{ userDetail.role }}
              </span>
            </div>
            
            <!-- Info Section -->
            <div class="info-section">
              <!-- Saldo Card -->
              <div v-if="userDetail.saldo !== undefined && userDetail.saldo !== null" class="saldo-card">
                <div class="saldo-label">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21 18V19C21 20.1 20.1 21 19 21H5C3.89 21 3 20.1 3 19V5C3 3.9 3.89 3 5 3H19C20.1 3 21 3.9 21 5V6H12C10.89 6 10 6.9 10 8V16C10 17.1 10.89 18 12 18H21ZM12 16H22V8H12V16ZM16 13.5C15.17 13.5 14.5 12.83 14.5 12C14.5 11.17 15.17 10.5 16 10.5C16.83 10.5 17.5 11.17 17.5 12C17.5 12.83 16.83 13.5 16 13.5Z" fill="#4CAF50"/>
                  </svg>
                  Saldo
                </div>
                <div class="saldo-value">{{ formatCurrency(userDetail.saldo) }}</div>
              </div>
              
              <div class="info-grid">
                <!-- Username -->
                <div class="info-item">
                  <div class="info-label">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z" fill="#7C6A46"/>
                    </svg>
                    Username
                  </div>
                  <div class="info-value">{{ userDetail.username }}</div>
                </div>
                
                <!-- Email -->
                <div class="info-item">
                  <div class="info-label">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M20 4H4C2.9 4 2.01 4.9 2.01 6L2 18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4ZM20 8L12 13L4 8V6L12 11L20 6V8Z" fill="#7C6A46"/>
                    </svg>
                    Email
                  </div>
                  <div class="info-value">{{ userDetail.email }}</div>
                </div>
                
                <!-- Gender -->
                <div class="info-item">
                  <div class="info-label">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 6C13.93 6 15.5 7.57 15.5 9.5C15.5 11.43 13.93 13 12 13C10.07 13 8.5 11.43 8.5 9.5C8.5 7.57 10.07 6 12 6ZM12 20C9.97 20 7.57 19.18 5.86 17.12C7.55 15.8 9.68 15 12 15C14.32 15 16.45 15.8 18.14 17.12C16.43 19.18 14.03 20 12 20Z" fill="#7C6A46"/>
                    </svg>
                    Gender
                  </div>
                  <div class="info-value">{{ userDetail.gender || '-' }}</div>
                </div>
                
                <!-- User ID -->
                <div class="info-item">
                  <div class="info-label">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M14 2H6C4.9 2 4.01 2.9 4.01 4L4 20C4 21.1 4.89 22 5.99 22H18C19.1 22 20 21.1 20 20V8L14 2ZM6 4H13L18 9V20H6V4ZM8 16H16V18H8V16ZM8 12H16V14H8V12Z" fill="#7C6A46"/>
                    </svg>
                    User ID
                  </div>
                  <div class="info-value info-id">{{ userDetail.id }}</div>
                </div>
                
                <!-- Phone (if available) -->
                <div v-if="userDetail.phone" class="info-item">
                  <div class="info-label">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M6.62 10.79C8.06 13.62 10.38 15.93 13.21 17.38L15.41 15.18C15.68 14.91 16.08 14.82 16.43 14.94C17.55 15.31 18.76 15.51 20 15.51C20.55 15.51 21 15.96 21 16.51V20C21 20.55 20.55 21 20 21C10.61 21 3 13.39 3 4C3 3.45 3.45 3 4 3H7.5C8.05 3 8.5 3.45 8.5 4C8.5 5.25 8.7 6.45 9.07 7.57C9.18 7.92 9.1 8.31 8.82 8.59L6.62 10.79Z" fill="#7C6A46"/>
                    </svg>
                    Phone
                  </div>
                  <div class="info-value">{{ userDetail.phone }}</div>
                </div>
                
                <!-- Address (if available) -->
                <div v-if="userDetail.address" class="info-item full-width">
                  <div class="info-label">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="#7C6A46"/>
                    </svg>
                    Address
                  </div>
                  <div class="info-value">{{ userDetail.address }}</div>
                </div>
                
                <!-- Created At (if available) -->
                <div v-if="userDetail.createdAt" class="info-item">
                  <div class="info-label">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3.01 3.9 3.01 5L3 19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19ZM12 11H17V16H12V11Z" fill="#7C6A46"/>
                    </svg>
                    Member Since
                  </div>
                  <div class="info-value">{{ formatDate(userDetail.createdAt) }}</div>
                </div>
                
                <!-- Updated At (if available) -->
                <div v-if="userDetail.updatedAt" class="info-item">
                  <div class="info-label">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4C7.58 4 4.01 7.58 4.01 12C4.01 16.42 7.58 20 12 20C15.73 20 18.84 17.45 19.73 14H17.65C16.83 16.33 14.61 18 12 18C8.69 18 6 15.31 6 12C6 8.69 8.69 6 12 6C13.66 6 15.14 6.69 16.22 7.78L13 11H20V4L17.65 6.35Z" fill="#7C6A46"/>
                    </svg>
                    Last Updated
                  </div>
                  <div class="info-value">{{ formatDate(userDetail.updatedAt) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
    
    <Footer />
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

.page-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #FAFAFA;
}

.main-content {
  flex: 1;
  padding-top: 140px;
  padding-bottom: 60px;
}

.content-wrapper {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: white;
  border: 2px solid #E0E0E0;
  border-radius: 8px;
  color: #333;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 24px;
}

.back-btn:hover {
  border-color: #7C6A46;
  color: #7C6A46;
}

.page-title {
  font-family: 'Poppins', sans-serif;
  font-size: 32px;
  font-weight: 700;
  color: #1C1C1C;
  margin-bottom: 32px;
}

/* Loading State */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 16px;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #F0F0F0;
  border-top-color: #7C6A46;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-container p {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  color: #666;
}

/* Error State */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 16px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.error-container h3 {
  font-family: 'Poppins', sans-serif;
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.error-container p {
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  color: #666;
  margin: 0;
  text-align: center;
}

.retry-btn {
  padding: 12px 24px;
  background: #7C6A46;
  color: white;
  border: none;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.retry-btn:hover {
  background: #5D4F35;
}

/* Notice Banner */
.notice-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  background: #FFF3E0;
  border-radius: 12px;
  border-left: 4px solid #FF9800;
  margin-bottom: 24px;
  color: #E65100;
}

.notice-banner span {
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
}

/* Profile Card */
.profile-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px 30px;
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Poppins', sans-serif;
  font-size: 42px;
  font-weight: 700;
  color: white;
  border: 4px solid rgba(255, 255, 255, 0.3);
  margin-bottom: 16px;
}

.user-name {
  font-family: 'Poppins', sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin: 0 0 12px;
  text-align: center;
}

.role-badge {
  padding: 8px 20px;
  border-radius: 20px;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 600;
}

.badge-superadmin {
  background: rgba(233, 30, 99, 0.2);
  color: #FCE4EC;
  border: 1px solid rgba(233, 30, 99, 0.4);
}

.badge-owner {
  background: rgba(33, 150, 243, 0.2);
  color: #E3F2FD;
  border: 1px solid rgba(33, 150, 243, 0.4);
}

.badge-customer {
  background: rgba(76, 175, 80, 0.2);
  color: #E8F5E9;
  border: 1px solid rgba(76, 175, 80, 0.4);
}

.badge-default {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

/* Info Section */
.info-section {
  padding: 32px;
}

/* Saldo Card */
.saldo-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  border-radius: 16px;
  margin-bottom: 28px;
  border: 2px solid #A5D6A7;
}

.saldo-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #2E7D32;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.saldo-value {
  font-family: 'Poppins', sans-serif;
  font-size: 32px;
  font-weight: 700;
  color: #1B5E20;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 500;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 500;
  color: #333;
  word-break: break-word;
}

.info-id {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #666;
  background: #F5F5F5;
  padding: 8px 12px;
  border-radius: 6px;
}

@media (max-width: 600px) {
  .page-title {
    font-size: 24px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .avatar {
    width: 80px;
    height: 80px;
    font-size: 32px;
  }
  
  .user-name {
    font-size: 22px;
  }
  
  .info-section {
    padding: 24px 16px;
  }
}
</style>
