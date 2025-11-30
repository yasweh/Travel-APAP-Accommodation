import api from './api'

export interface UserDetail {
  id: string
  username: string
  email: string
  name: string
  role: string
  gender?: string
  saldo?: number
  createdAt?: string
  updatedAt?: string
  phone?: string
  address?: string
}

export interface UserDetailResponse {
  success: boolean
  data: UserDetail
  message: string
  isLocalData?: boolean
}

/**
 * Service for user-related API calls
 */
export const userService = {
  /**
   * Get current authenticated user's profile
   * Fetches from external user service with full details
   */
  async getCurrentUserProfile(): Promise<UserDetailResponse> {
    const response = await api.get('/users/me')
    return response.data
  },

  /**
   * Search for a user by ID, username, or email
   */
  async getUserDetail(search: string): Promise<UserDetailResponse> {
    const response = await api.get('/users/detail', {
      params: { search }
    })
    return response.data
  }
}

export default userService
