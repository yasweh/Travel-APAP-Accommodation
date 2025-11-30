import api from './api'
import { useAuthStore, type User } from '@/stores/auth'

interface LoginRequest {
  email: string
  password: string
}

interface LoginResponse {
  success: boolean
  message: string
  data: User
}

export const authService = {
  /**
   * Login user with email and password
   */
  async login(email: string, password: string): Promise<User> {
    try {
      const response = await api.post<LoginResponse>('/auth/login', {
        email,
        password
      })

      if (response.data.success && response.data.data) {
        const userData: User = {
          id: response.data.data.id,
          username: response.data.data.username,
          email: response.data.data.email,
          name: response.data.data.name,
          role: response.data.data.role,
          token: response.data.data.token,
          type: response.data.data.type || 'Bearer'
        }
        
        const authStore = useAuthStore()
        authStore.setAuth(userData)
        return userData
      } else {
        throw new Error(response.data.message || 'Login failed')
      }
    } catch (error: any) {
      console.error('Login error:', error)
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message)
      }
      if (error.response?.status === 401) {
        throw new Error('Invalid email or password')
      }
      if (error.response?.status >= 500) {
        throw new Error('Server error. Please try again later.')
      }
      throw new Error('Network error. Please check your connection.')
    }
  },

  /**
   * Logout user
   */
  async logout(): Promise<void> {
    try {
      await api.post('/auth/logout')
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      const authStore = useAuthStore()
      authStore.clearAuth()
    }
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    const authStore = useAuthStore()
    return authStore.isAuthenticated
  },

  /**
   * Get current user
   */
  getCurrentUser(): User | null {
    const authStore = useAuthStore()
    return authStore.user
  }
}
