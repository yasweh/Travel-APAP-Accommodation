import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore, type User } from '@/stores/auth'

describe('Auth Store', () => {
  beforeEach(() => {
    // Create fresh pinia and set it as active
    setActivePinia(createPinia())
    // Clear localStorage
    localStorage.clear()
    // Clear all mocks
    vi.clearAllMocks()
  })

  describe('Initial State', () => {
    it('should have null user when no stored data', () => {
      const store = useAuthStore()
      expect(store.user).toBeNull()
      expect(store.token).toBeNull()
    })

    it('should load user from localStorage on init', () => {
      const userData: User = {
        token: 'test-token',
        type: 'Bearer',
        id: 'user-1',
        username: 'testuser',
        email: 'test@example.com',
        name: 'Test User',
        role: 'Customer'
      }
      localStorage.setItem('auth_token', userData.token)
      localStorage.setItem('auth_user', JSON.stringify(userData))

      // Need to create pinia again to trigger loadFromStorage
      setActivePinia(createPinia())
      const store = useAuthStore()

      expect(store.token).toBe('test-token')
      expect(store.user?.username).toBe('testuser')
    })
  })

  describe('Getters', () => {
    it('isAuthenticated returns false when no token', () => {
      const store = useAuthStore()
      expect(store.isAuthenticated).toBe(false)
    })

    it('isAuthenticated returns true when token exists', () => {
      const store = useAuthStore()
      store.setAuth({
        token: 'test-token',
        type: 'Bearer',
        id: 'user-1',
        username: 'testuser',
        email: 'test@example.com',
        name: 'Test User',
        role: 'Customer'
      })
      expect(store.isAuthenticated).toBe(true)
    })

    it('userRole returns role from user', () => {
      const store = useAuthStore()
      store.setAuth({
        token: 'token',
        type: 'Bearer',
        id: '1',
        username: 'owner',
        email: 'owner@test.com',
        name: 'Owner',
        role: 'Accommodation Owner'
      })
      expect(store.userRole).toBe('Accommodation Owner')
    })

    it('userRole returns null when no user', () => {
      const store = useAuthStore()
      expect(store.userRole).toBeNull()
    })

    it('isSuperadmin returns true for superadmin role', () => {
      const store = useAuthStore()
      store.setAuth({
        token: 'token',
        type: 'Bearer',
        id: '1',
        username: 'admin',
        email: 'admin@test.com',
        name: 'Admin',
        role: 'Superadmin'
      })
      expect(store.isSuperadmin).toBe(true)
    })

    it('isSuperadmin returns false for non-superadmin role', () => {
      const store = useAuthStore()
      store.setAuth({
        token: 'token',
        type: 'Bearer',
        id: '1',
        username: 'customer',
        email: 'customer@test.com',
        name: 'Customer',
        role: 'Customer'
      })
      expect(store.isSuperadmin).toBe(false)
    })

    it('isAccommodationOwner returns true for owner role', () => {
      const store = useAuthStore()
      store.setAuth({
        token: 'token',
        type: 'Bearer',
        id: '1',
        username: 'owner',
        email: 'owner@test.com',
        name: 'Owner',
        role: 'Accommodation Owner'
      })
      expect(store.isAccommodationOwner).toBe(true)
    })

    it('isCustomer returns true for customer role', () => {
      const store = useAuthStore()
      store.setAuth({
        token: 'token',
        type: 'Bearer',
        id: '1',
        username: 'customer',
        email: 'customer@test.com',
        name: 'Customer',
        role: 'Customer'
      })
      expect(store.isCustomer).toBe(true)
    })
  })

  describe('Actions', () => {
    it('setAuth sets user and token', () => {
      const store = useAuthStore()
      const userData: User = {
        token: 'new-token',
        type: 'Bearer',
        id: 'user-2',
        username: 'newuser',
        email: 'new@example.com',
        name: 'New User',
        role: 'Customer'
      }
      
      store.setAuth(userData)

      expect(store.user).toEqual(userData)
      expect(store.token).toBe('new-token')
      expect(localStorage.getItem('auth_token')).toBe('new-token')
      expect(localStorage.getItem('auth_user')).toBe(JSON.stringify(userData))
    })

    it('clearAuth clears user and token', () => {
      const store = useAuthStore()
      store.setAuth({
        token: 'token',
        type: 'Bearer',
        id: '1',
        username: 'user',
        email: 'user@test.com',
        name: 'User',
        role: 'Customer'
      })

      store.clearAuth()

      expect(store.user).toBeNull()
      expect(store.token).toBeNull()
      expect(localStorage.getItem('auth_token')).toBeNull()
      expect(localStorage.getItem('auth_user')).toBeNull()
    })

    it('loadFromStorage with invalid JSON clears auth', () => {
      localStorage.setItem('auth_token', 'valid-token')
      localStorage.setItem('auth_user', 'invalid-json{{{')
      
      // Spy on console.error
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      setActivePinia(createPinia())
      const store = useAuthStore()

      expect(store.user).toBeNull()
      expect(store.token).toBeNull()
      expect(consoleErrorSpy).toHaveBeenCalled()
      
      consoleErrorSpy.mockRestore()
    })

    it('loadFromStorage does nothing when no stored data', () => {
      const store = useAuthStore()
      store.loadFromStorage()
      expect(store.user).toBeNull()
      expect(store.token).toBeNull()
    })
  })
})
