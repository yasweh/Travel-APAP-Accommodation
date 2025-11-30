import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Navbar from '@/components/Navbar.vue'

// Mock vue-router
const mockPush = vi.fn()
const mockRoute = {
  path: '/',
  name: 'home'
}

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  }),
  useRoute: () => mockRoute
}))

// Mock authService
const mockLogout = vi.fn()
vi.mock('@/services/authService', () => ({
  authService: {
    logout: () => mockLogout()
  }
}))

// Mock auth store
vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(() => ({
    user: null,
    isAuthenticated: false
  }))
}))

import { useAuthStore } from '@/stores/auth'

describe('Navbar', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  const mountNavbar = (authState = { user: null, isAuthenticated: false }) => {
    vi.mocked(useAuthStore).mockReturnValue({
      user: authState.user,
      isAuthenticated: authState.isAuthenticated,
      userRole: authState.user?.role || null
    } as any)

    return mount(Navbar, {
      global: {
        stubs: {
          RouterLink: true
        },
        mocks: {
          $route: mockRoute
        }
      }
    })
  }

  describe('Component Rendering', () => {
    it('renders navbar container', () => {
      const wrapper = mountNavbar()
      expect(wrapper.find('.navbar').exists()).toBe(true)
    })

    it('renders navbar background', () => {
      const wrapper = mountNavbar()
      expect(wrapper.find('.navbar-bg').exists()).toBe(true)
    })

    it('renders logo', () => {
      const wrapper = mountNavbar()
      expect(wrapper.find('.logo').exists()).toBe(true)
    })

    it('renders navigation links', () => {
      const wrapper = mountNavbar()
      expect(wrapper.find('.links').exists()).toBe(true)
    })

    it('renders Home link', () => {
      const wrapper = mountNavbar()
      const links = wrapper.findAll('.link')
      expect(links.some(l => l.text() === 'Home')).toBe(true)
    })

    it('renders Bookings link', () => {
      const wrapper = mountNavbar()
      const links = wrapper.findAll('.link')
      expect(links.some(l => l.text() === 'Bookings')).toBe(true)
    })

    it('renders Support link', () => {
      const wrapper = mountNavbar()
      const links = wrapper.findAll('.link')
      expect(links.some(l => l.text() === 'Support')).toBe(true)
    })

    it('renders Reviews link', () => {
      const wrapper = mountNavbar()
      const links = wrapper.findAll('.link')
      expect(links.some(l => l.text() === 'Reviews')).toBe(true)
    })

    it('renders Book now button', () => {
      const wrapper = mountNavbar()
      expect(wrapper.find('.book-btn').exists()).toBe(true)
      expect(wrapper.find('.btn-text').text()).toBe('Book now')
    })
  })

  describe('Guest User', () => {
    it('shows login button when not authenticated', () => {
      const wrapper = mountNavbar({ user: null, isAuthenticated: false })
      expect(wrapper.find('.login-btn').exists()).toBe(true)
    })

    it('does not show user info when not authenticated', () => {
      const wrapper = mountNavbar({ user: null, isAuthenticated: false })
      expect(wrapper.find('.user-info').exists()).toBe(false)
    })

    it('does not show logout button when not authenticated', () => {
      const wrapper = mountNavbar({ user: null, isAuthenticated: false })
      expect(wrapper.find('.logout-btn').exists()).toBe(false)
    })
  })

  describe('Authenticated User', () => {
    const authenticatedUser = {
      id: '1',
      name: 'John Doe',
      email: 'john@example.com',
      role: 'Customer'
    }

    it('shows user info when authenticated', () => {
      const wrapper = mountNavbar({ user: authenticatedUser, isAuthenticated: true })
      expect(wrapper.find('.user-info').exists()).toBe(true)
    })

    it('shows user avatar with first letter of name', () => {
      const wrapper = mountNavbar({ user: authenticatedUser, isAuthenticated: true })
      expect(wrapper.find('.user-avatar').text()).toBe('J')
    })

    it('shows user name', () => {
      const wrapper = mountNavbar({ user: authenticatedUser, isAuthenticated: true })
      expect(wrapper.find('.user-name').text()).toBe('John Doe')
    })

    it('shows user role', () => {
      const wrapper = mountNavbar({ user: authenticatedUser, isAuthenticated: true })
      expect(wrapper.find('.user-role').text()).toBe('Customer')
    })

    it('shows logout button when authenticated', () => {
      const wrapper = mountNavbar({ user: authenticatedUser, isAuthenticated: true })
      expect(wrapper.find('.logout-btn').exists()).toBe(true)
    })

    it('does not show login button when authenticated', () => {
      const wrapper = mountNavbar({ user: authenticatedUser, isAuthenticated: true })
      expect(wrapper.find('.login-btn').exists()).toBe(false)
    })

    it('hides Properties link for Customer role', () => {
      const wrapper = mountNavbar({ user: authenticatedUser, isAuthenticated: true })
      const links = wrapper.findAll('.link')
      expect(links.some(l => l.text() === 'Properties')).toBe(false)
    })
  })

  describe('Non-Customer Roles', () => {
    const adminUser = {
      id: '1',
      name: 'Admin User',
      email: 'admin@example.com',
      role: 'Superadmin'
    }

    it('shows Properties link for non-Customer role', () => {
      const wrapper = mountNavbar({ user: adminUser, isAuthenticated: true })
      const links = wrapper.findAll('.link')
      expect(links.some(l => l.text() === 'Properties')).toBe(true)
    })

    it('shows user avatar with first letter for admin', () => {
      const wrapper = mountNavbar({ user: adminUser, isAuthenticated: true })
      expect(wrapper.find('.user-avatar').text()).toBe('A')
    })
  })

  describe('Navigation', () => {
    it('navigates to home when logo is clicked', async () => {
      const wrapper = mountNavbar()
      await wrapper.find('.logo').trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/')
    })

    it('navigates to home when Home link is clicked', async () => {
      const wrapper = mountNavbar()
      const homeLink = wrapper.findAll('.link').find(l => l.text() === 'Home')
      await homeLink?.trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/')
    })

    it('navigates to bookings when Bookings link is clicked', async () => {
      const wrapper = mountNavbar()
      const bookingsLink = wrapper.findAll('.link').find(l => l.text() === 'Bookings')
      await bookingsLink?.trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/booking')
    })

    it('navigates to support when Support link is clicked', async () => {
      const wrapper = mountNavbar()
      const supportLink = wrapper.findAll('.link').find(l => l.text() === 'Support')
      await supportLink?.trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/support')
    })

    it('navigates to reviews when Reviews link is clicked', async () => {
      const wrapper = mountNavbar()
      const reviewsLink = wrapper.findAll('.link').find(l => l.text() === 'Reviews')
      await reviewsLink?.trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/reviews/my-reviews')
    })

    it('navigates to booking create when Book now is clicked', async () => {
      const wrapper = mountNavbar()
      await wrapper.find('.book-btn').trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/booking/create')
    })

    it('navigates to login when login button is clicked', async () => {
      const wrapper = mountNavbar({ user: null, isAuthenticated: false })
      await wrapper.find('.login-btn').trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/login')
    })
  })

  describe('Logout', () => {
    const authenticatedUser = {
      id: '1',
      name: 'John Doe',
      email: 'john@example.com',
      role: 'Customer'
    }

    it('calls logout service when logout button is clicked', async () => {
      mockLogout.mockResolvedValue({})
      const wrapper = mountNavbar({ user: authenticatedUser, isAuthenticated: true })
      await wrapper.find('.logout-btn').trigger('click')
      await flushPromises()
      expect(mockLogout).toHaveBeenCalled()
    })

    it('redirects to login after successful logout', async () => {
      mockLogout.mockResolvedValue({})
      const wrapper = mountNavbar({ user: authenticatedUser, isAuthenticated: true })
      await wrapper.find('.logout-btn').trigger('click')
      await flushPromises()
      expect(mockPush).toHaveBeenCalledWith('/login')
    })

    it('handles logout error gracefully', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      mockLogout.mockRejectedValue(new Error('Logout failed'))
      const wrapper = mountNavbar({ user: authenticatedUser, isAuthenticated: true })
      await wrapper.find('.logout-btn').trigger('click')
      await flushPromises()
      expect(consoleErrorSpy).toHaveBeenCalledWith('Logout failed:', expect.any(Error))
      consoleErrorSpy.mockRestore()
    })
  })

  describe('Active Link State', () => {
    it('applies active class based on route', () => {
      mockRoute.path = '/'
      const wrapper = mountNavbar()
      const homeLink = wrapper.findAll('.link').find(l => l.text() === 'Home')
      expect(homeLink?.classes()).toContain('active')
    })
  })

  describe('Responsive Behavior', () => {
    it('has proper CSS classes for responsive design', () => {
      const wrapper = mountNavbar()
      expect(wrapper.find('.nav-links').exists()).toBe(true)
      expect(wrapper.find('.auth-section').exists()).toBe(true)
    })
  })
})
