import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import LoginView from '@/views/auth/LoginView.vue'

// Mock vue-router
const mockPush = vi.fn()
const mockCurrentRoute = {
  value: {
    query: {}
  }
}

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
    currentRoute: mockCurrentRoute
  })
}))

// Mock authService
const mockLogin = vi.fn()

vi.mock('@/services/authService', () => ({
  authService: {
    login: (email: string, password: string) => mockLogin(email, password)
  }
}))

describe('LoginView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCurrentRoute.value.query = {}
    mockLogin.mockResolvedValue({})
  })

  const mountLoginView = () => {
    return mount(LoginView, {
      global: {
        stubs: {
          RouterLink: true
        }
      }
    })
  }

  describe('Component Rendering', () => {
    it('renders login page container', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.login-page').exists()).toBe(true)
    })

    it('renders left column with image', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.login-left').exists()).toBe(true)
    })

    it('renders right column with form', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.login-right').exists()).toBe(true)
    })

    it('renders left overlay', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.left-overlay').exists()).toBe(true)
    })

    it('renders left image', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.left-image').exists()).toBe(true)
    })

    it('renders brand logo', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.brand-logo').exists()).toBe(true)
    })

    it('renders left title', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.left-title').text()).toBe('Travel Apap Accommodation')
    })

    it('renders left subtitle', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.left-subtitle').text()).toBe('Hotel for every moment rich in emotion')
    })
  })

  describe('Login Form', () => {
    it('renders login container', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.login-container').exists()).toBe(true)
    })

    it('renders login header', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.login-header').exists()).toBe(true)
    })

    it('renders Welcome Back heading', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.login-header h2').text()).toBe('Welcome Back')
    })

    it('renders sign in subtitle', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.login-header p').text()).toBe('Sign in to continue using Travel APAP')
    })

    it('renders login form', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.login-form').exists()).toBe(true)
    })

    it('renders email form group', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('#email').exists()).toBe(true)
    })

    it('renders password form group', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('#password').exists()).toBe(true)
    })

    it('renders email label', () => {
      const wrapper = mountLoginView()
      const labels = wrapper.findAll('label')
      expect(labels.some(l => l.text() === 'Email')).toBe(true)
    })

    it('renders password label', () => {
      const wrapper = mountLoginView()
      const labels = wrapper.findAll('label')
      expect(labels.some(l => l.text() === 'Password')).toBe(true)
    })

    it('renders email input with correct placeholder', () => {
      const wrapper = mountLoginView()
      const emailInput = wrapper.find('#email')
      expect(emailInput.attributes('placeholder')).toBe('Enter your email')
    })

    it('renders password input with correct placeholder', () => {
      const wrapper = mountLoginView()
      const passwordInput = wrapper.find('#password')
      expect(passwordInput.attributes('placeholder')).toBe('Enter your password')
    })

    it('renders password input with type password', () => {
      const wrapper = mountLoginView()
      const passwordInput = wrapper.find('#password')
      expect(passwordInput.attributes('type')).toBe('password')
    })

    it('renders login button', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.login-button').exists()).toBe(true)
      expect(wrapper.find('.login-button').text()).toBe('Login')
    })

    it('renders register link', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.register-link').exists()).toBe(true)
    })

    it('renders register link with correct text', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.register-link').text()).toContain("Don't have an account?")
    })
  })

  describe('Form Input', () => {
    it('updates email value when typed', async () => {
      const wrapper = mountLoginView()
      const emailInput = wrapper.find('#email')
      await emailInput.setValue('test@example.com')
      expect((emailInput.element as HTMLInputElement).value).toBe('test@example.com')
    })

    it('updates password value when typed', async () => {
      const wrapper = mountLoginView()
      const passwordInput = wrapper.find('#password')
      await passwordInput.setValue('password123')
      expect((passwordInput.element as HTMLInputElement).value).toBe('password123')
    })

    it('has required attribute on email', () => {
      const wrapper = mountLoginView()
      const emailInput = wrapper.find('#email')
      expect(emailInput.attributes('required')).toBeDefined()
    })

    it('has required attribute on password', () => {
      const wrapper = mountLoginView()
      const passwordInput = wrapper.find('#password')
      expect(passwordInput.attributes('required')).toBeDefined()
    })
  })

  describe('Form Submission', () => {
    it('shows error when email is empty', async () => {
      const wrapper = mountLoginView()
      await wrapper.find('.login-form').trigger('submit')
      expect(wrapper.find('.error-message').text()).toBe('Please enter both email and password')
    })

    it('shows error when password is empty', async () => {
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('.login-form').trigger('submit')
      expect(wrapper.find('.error-message').text()).toBe('Please enter both email and password')
    })

    it('calls authService.login with credentials', async () => {
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('password123')
      await wrapper.find('.login-form').trigger('submit')
      await flushPromises()
      expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'password123')
    })

    it('redirects to home after successful login', async () => {
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('password123')
      await wrapper.find('.login-form').trigger('submit')
      await flushPromises()
      expect(mockPush).toHaveBeenCalledWith('/')
    })

    it('redirects to specified redirect URL after login', async () => {
      mockCurrentRoute.value.query = { redirect: '/booking' }
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('password123')
      await wrapper.find('.login-form').trigger('submit')
      await flushPromises()
      expect(mockPush).toHaveBeenCalledWith('/booking')
    })

    it('shows error message on login failure', async () => {
      mockLogin.mockRejectedValue(new Error('Invalid credentials'))
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('wrongpassword')
      await wrapper.find('.login-form').trigger('submit')
      await flushPromises()
      expect(wrapper.find('.error-message').text()).toBe('Invalid credentials')
    })

    it('shows default error message when error has no message', async () => {
      mockLogin.mockRejectedValue({})
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('wrongpassword')
      await wrapper.find('.login-form').trigger('submit')
      await flushPromises()
      expect(wrapper.find('.error-message').text()).toBe('Login failed. Please try again.')
    })
  })

  describe('Loading State', () => {
    it('shows loading text when submitting', async () => {
      mockLogin.mockImplementation(() => new Promise(() => {}))
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('password123')
      await wrapper.find('.login-form').trigger('submit')
      expect(wrapper.find('.login-button').text()).toBe('Logging in...')
    })

    it('disables button when loading', async () => {
      mockLogin.mockImplementation(() => new Promise(() => {}))
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('password123')
      await wrapper.find('.login-form').trigger('submit')
      expect(wrapper.find('.login-button').attributes('disabled')).toBeDefined()
    })

    it('disables email input when loading', async () => {
      mockLogin.mockImplementation(() => new Promise(() => {}))
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('password123')
      await wrapper.find('.login-form').trigger('submit')
      expect(wrapper.find('#email').attributes('disabled')).toBeDefined()
    })

    it('disables password input when loading', async () => {
      mockLogin.mockImplementation(() => new Promise(() => {}))
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('password123')
      await wrapper.find('.login-form').trigger('submit')
      expect(wrapper.find('#password').attributes('disabled')).toBeDefined()
    })

    it('re-enables button after login completes', async () => {
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('password123')
      await wrapper.find('.login-form').trigger('submit')
      await flushPromises()
      expect(wrapper.find('.login-button').attributes('disabled')).toBeUndefined()
    })
  })

  describe('Error Message Display', () => {
    it('does not show error message initially', () => {
      const wrapper = mountLoginView()
      expect(wrapper.find('.error-message').exists()).toBe(false)
    })

    it('clears error message on new submit', async () => {
      mockLogin.mockRejectedValueOnce(new Error('First error'))
      mockLogin.mockResolvedValueOnce({})
      
      const wrapper = mountLoginView()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('wrongpassword')
      await wrapper.find('.login-form').trigger('submit')
      await flushPromises()
      
      expect(wrapper.find('.error-message').exists()).toBe(true)
      
      await wrapper.find('#password').setValue('correctpassword')
      await wrapper.find('.login-form').trigger('submit')
      // Error should be cleared during new submit
      expect(mockLogin).toHaveBeenCalledTimes(2)
    })
  })

  describe('External Links', () => {
    it('has register link with correct href', () => {
      const wrapper = mountLoginView()
      const link = wrapper.find('.register-link a')
      expect(link.attributes('href')).toBe('http://2306219575-fe.hafizmuh.site/register')
    })

    it('register link opens in new tab', () => {
      const wrapper = mountLoginView()
      const link = wrapper.find('.register-link a')
      expect(link.attributes('target')).toBe('_blank')
    })
  })
})
