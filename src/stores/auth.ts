import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export interface User {
  token: string
  type: string
  id: string
  username: string
  email: string
  name: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!token.value)
  const userRole = computed(() => user.value?.role || null)
  const isSuperadmin = computed(() => user.value?.role === 'Superadmin')
  const isAccommodationOwner = computed(() => user.value?.role === 'Accommodation Owner')
  const isCustomer = computed(() => user.value?.role === 'Customer')

  // Actions
  function setAuth(userData: User) {
    user.value = userData
    token.value = userData.token
    
    // Store in localStorage for persistence
    localStorage.setItem('auth_token', userData.token)
    localStorage.setItem('auth_user', JSON.stringify(userData))
  }

  function clearAuth() {
    user.value = null
    token.value = null
    
    // Clear localStorage
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
  }

  function loadFromStorage() {
    const storedToken = localStorage.getItem('auth_token')
    const storedUser = localStorage.getItem('auth_user')
    
    if (storedToken && storedUser) {
      try {
        token.value = storedToken
        user.value = JSON.parse(storedUser)
      } catch (e) {
        console.error('Failed to parse stored user data:', e)
        clearAuth()
      }
    }
  }

  // Initialize from localStorage on store creation
  loadFromStorage()

  return {
    user,
    token,
    isAuthenticated,
    userRole,
    isSuperadmin,
    isAccommodationOwner,
    isCustomer,
    setAuth,
    clearAuth,
    loadFromStorage
  }
})
