import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import ReviewFormView from '@/views/review/ReviewFormView.vue'
import { useAuthStore } from '@/stores/auth'

// Mock CreateReviewModal component
vi.mock('@/components/review/CreateReviewModal.vue', () => ({
  default: {
    name: 'CreateReviewModal',
    props: ['bookingId', 'customerId'],
    emits: ['close', 'success'],
    template: '<div class="create-review-modal"><slot></slot></div>'
  }
}))

// Create mock router
const createMockRouter = () => {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/reviews/create', name: 'review-create', component: ReviewFormView },
      { path: '/booking', name: 'booking-list', component: { template: '<div>Booking</div>' } },
      { path: '/login', name: 'login', component: { template: '<div>Login</div>' } },
      { path: '/reviews/my-reviews', name: 'my-reviews', component: { template: '<div>My Reviews</div>' } }
    ]
  })
}

describe('ReviewFormView.vue', () => {
  let router: ReturnType<typeof createMockRouter>
  let pinia: ReturnType<typeof createPinia>

  beforeEach(() => {
    vi.clearAllMocks()
    pinia = createPinia()
    setActivePinia(pinia)
    router = createMockRouter()
    window.alert = vi.fn()
  })

  const mountComponent = async (query: Record<string, string> = {}) => {
    router.push({ path: '/reviews/create', query })
    await router.isReady()
    
    const wrapper = mount(ReviewFormView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    await flushPromises()
    return wrapper
  }

  describe('Loading State', () => {
    it('should show loading when bookingId is missing', async () => {
      const authStore = useAuthStore()
      authStore.user = { id: 'user-123', email: 'test@test.com', name: 'Test User' }
      
      const wrapper = await mountComponent({})
      
      expect(wrapper.find('.loading').exists()).toBe(true)
      expect(wrapper.text()).toContain('Loading')
    })

    it('should show loading when user is not logged in', async () => {
      const authStore = useAuthStore()
      authStore.user = null
      
      const wrapper = await mountComponent({ bookingId: 'BOOK-123' })
      
      expect(wrapper.find('.loading').exists()).toBe(true)
    })
  })

  describe('Validation', () => {
    it('should redirect to booking page if bookingId is missing', async () => {
      const authStore = useAuthStore()
      authStore.user = { id: 'user-123', email: 'test@test.com', name: 'Test User' }
      
      const pushSpy = vi.spyOn(router, 'push')
      await mountComponent({})
      
      expect(window.alert).toHaveBeenCalledWith('Booking ID is required')
      expect(pushSpy).toHaveBeenCalledWith('/booking')
    })

    it('should redirect to login if user is not authenticated', async () => {
      const authStore = useAuthStore()
      authStore.user = null
      
      const pushSpy = vi.spyOn(router, 'push')
      await mountComponent({ bookingId: 'BOOK-123' })
      
      expect(window.alert).toHaveBeenCalledWith('Please login to write a review')
      expect(pushSpy).toHaveBeenCalledWith('/login')
    })
  })

  describe('CreateReviewModal Integration', () => {
    it('should render CreateReviewModal when bookingId and customerId are present', async () => {
      const authStore = useAuthStore()
      authStore.user = { id: 'user-123', email: 'test@test.com', name: 'Test User' }
      
      const wrapper = await mountComponent({ bookingId: 'BOOK-123' })
      
      expect(wrapper.find('.create-review-modal').exists()).toBe(true)
    })

    it('should pass correct props to CreateReviewModal', async () => {
      const authStore = useAuthStore()
      authStore.user = { id: 'user-123', email: 'test@test.com', name: 'Test User' }
      
      const wrapper = await mountComponent({ bookingId: 'BOOK-123' })
      
      const modal = wrapper.findComponent({ name: 'CreateReviewModal' })
      expect(modal.props('bookingId')).toBe('BOOK-123')
      expect(modal.props('customerId')).toBe('user-123')
    })
  })

  describe('Event Handling', () => {
    it('should navigate back when close event is emitted', async () => {
      const authStore = useAuthStore()
      authStore.user = { id: 'user-123', email: 'test@test.com', name: 'Test User' }
      
      const backSpy = vi.spyOn(router, 'back')
      const wrapper = await mountComponent({ bookingId: 'BOOK-123' })
      
      const modal = wrapper.findComponent({ name: 'CreateReviewModal' })
      await modal.vm.$emit('close')
      
      expect(backSpy).toHaveBeenCalled()
    })

    it('should show success message and navigate when success event is emitted', async () => {
      const authStore = useAuthStore()
      authStore.user = { id: 'user-123', email: 'test@test.com', name: 'Test User' }
      
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent({ bookingId: 'BOOK-123' })
      
      const modal = wrapper.findComponent({ name: 'CreateReviewModal' })
      await modal.vm.$emit('success')
      
      expect(window.alert).toHaveBeenCalledWith('Review submitted successfully!')
      expect(pushSpy).toHaveBeenCalledWith('/reviews/my-reviews')
    })
  })

  describe('Page Layout', () => {
    it('should have full height container', async () => {
      const authStore = useAuthStore()
      authStore.user = { id: 'user-123', email: 'test@test.com', name: 'Test User' }
      
      const wrapper = await mountComponent({ bookingId: 'BOOK-123' })
      
      expect(wrapper.find('.review-form-page').exists()).toBe(true)
    })
  })

  describe('Query Parameter Handling', () => {
    it('should extract bookingId from query params', async () => {
      const authStore = useAuthStore()
      authStore.user = { id: 'user-123', email: 'test@test.com', name: 'Test User' }
      
      const wrapper = await mountComponent({ bookingId: 'BOOK-ABC-123' })
      
      const modal = wrapper.findComponent({ name: 'CreateReviewModal' })
      expect(modal.props('bookingId')).toBe('BOOK-ABC-123')
    })
  })

  describe('Auth Store Integration', () => {
    it('should get customerId from auth store user', async () => {
      const authStore = useAuthStore()
      authStore.user = { id: 'custom-user-id', email: 'test@test.com', name: 'Test User' }
      
      const wrapper = await mountComponent({ bookingId: 'BOOK-123' })
      
      const modal = wrapper.findComponent({ name: 'CreateReviewModal' })
      expect(modal.props('customerId')).toBe('custom-user-id')
    })
  })
})
