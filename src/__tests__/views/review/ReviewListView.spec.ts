import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import ReviewListView from '@/views/review/ReviewListView.vue'
import reviewService from '@/services/reviewService'
import { propertyService } from '@/services/propertyService'

// Mock the services
vi.mock('@/services/reviewService', () => ({
  default: {
    getAllReviews: vi.fn(),
    getMyReviews: vi.fn(),
    getReviewsByProperty: vi.fn(),
    deleteReview: vi.fn()
  }
}))

vi.mock('@/services/propertyService', () => ({
  propertyService: {
    getAll: vi.fn()
  }
}))

// Create mock router
const createMockRouter = (routeName = 'all-reviews', params = {}) => {
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/reviews', name: 'all-reviews', component: ReviewListView },
      { path: '/reviews/my-reviews', name: 'my-reviews', component: ReviewListView },
      { path: '/reviews/property/:propertyId', name: 'property-reviews', component: ReviewListView },
      { path: '/reviews/:id', name: 'review-detail', component: { template: '<div>Review Detail</div>' } }
    ]
  })
  return router
}

const mockReviews = [
  {
    reviewId: 'review-1',
    propertyId: 'prop-1',
    propertyName: 'Beach Hotel',
    customerName: 'John Doe',
    overallRating: 4.5,
    cleanlinessRating: 5,
    facilityRating: 4,
    serviceRating: 4,
    valueRating: 5,
    comment: 'Great stay!',
    createdDate: '2024-01-15T10:00:00Z'
  },
  {
    reviewId: 'review-2',
    propertyId: 'prop-2',
    propertyName: 'Mountain Resort',
    customerName: 'Jane Smith',
    overallRating: 3.5,
    cleanlinessRating: 3,
    facilityRating: 4,
    serviceRating: 3,
    valueRating: 4,
    comment: 'Nice view but could be cleaner',
    createdDate: '2024-01-10T14:00:00Z'
  }
]

const mockProperties = [
  { propertyId: 'prop-1', propertyName: 'Beach Hotel' },
  { propertyId: 'prop-2', propertyName: 'Mountain Resort' }
]

describe('ReviewListView.vue', () => {
  let router: ReturnType<typeof createMockRouter>

  beforeEach(() => {
    vi.clearAllMocks()
    router = createMockRouter()
    window.alert = vi.fn()
    window.confirm = vi.fn()
  })

  const mountComponent = async (routeName = 'all-reviews', routePath = '/reviews') => {
    router.push(routePath)
    await router.isReady()
    
    const wrapper = mount(ReviewListView, {
      global: {
        plugins: [router]
      }
    })
    
    await flushPromises()
    return wrapper
  }

  describe('Loading State', () => {
    it('should show loading spinner while fetching reviews', async () => {
      let resolvePromise: (value: any) => void
      vi.mocked(reviewService.getAllReviews).mockImplementation(() => 
        new Promise(resolve => { resolvePromise = resolve })
      )
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: [] })
      
      router.push('/reviews')
      await router.isReady()
      
      const wrapper = mount(ReviewListView, {
        global: { plugins: [router] }
      })
      
      // Check for loading indicator presence in HTML
      expect(wrapper.html().toLowerCase()).toContain('loading')
    })
  })

  describe('Error State', () => {
    it('should display error message when API call fails', async () => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: [] })
      vi.mocked(reviewService.getAllReviews).mockRejectedValue({
        response: { data: { error: 'Failed to load reviews' } }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.error-message').exists()).toBe(true)
      expect(wrapper.text()).toContain('Failed to load reviews')
    })
  })

  describe('All Reviews View', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(reviewService.getAllReviews).mockResolvedValue({ data: mockReviews })
    })

    it('should display correct page title for all reviews', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('All Reviews')
      expect(wrapper.text()).toContain('Reviews from all properties and bookings')
    })

    it('should load and display reviews', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.review-card').length).toBe(2)
      expect(wrapper.text()).toContain('Beach Hotel')
      expect(wrapper.text()).toContain('Mountain Resort')
    })

    it('should display property filter dropdown', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('#filter-property').exists()).toBe(true)
      expect(wrapper.text()).toContain('Filter by Property')
    })

    it('should filter reviews by property', async () => {
      const wrapper = await mountComponent()
      
      const select = wrapper.find('#filter-property')
      await select.setValue('prop-1')
      await flushPromises()
      
      // Should filter to show only Beach Hotel reviews
      const cards = wrapper.findAll('.review-card')
      expect(cards.length).toBe(1)
      expect(wrapper.text()).toContain('Beach Hotel')
    })

    it('should show clear filter button when filter is applied', async () => {
      const wrapper = await mountComponent()
      
      // Initially no clear button
      expect(wrapper.find('.btn-clear-filter').exists()).toBe(false)
      
      // Apply filter
      const select = wrapper.find('#filter-property')
      await select.setValue('prop-1')
      await flushPromises()
      
      // Clear button should appear
      expect(wrapper.find('.btn-clear-filter').exists()).toBe(true)
    })

    it('should clear filter when clear button clicked', async () => {
      const wrapper = await mountComponent()
      
      // Apply filter
      const select = wrapper.find('#filter-property')
      await select.setValue('prop-1')
      await flushPromises()
      
      // Click clear
      await wrapper.find('.btn-clear-filter').trigger('click')
      await flushPromises()
      
      // All reviews should be visible again
      expect(wrapper.findAll('.review-card').length).toBe(2)
    })
  })

  describe('My Reviews View', () => {
    beforeEach(() => {
      vi.mocked(reviewService.getMyReviews).mockResolvedValue({ data: mockReviews })
    })

    it('should display correct page title for my reviews', async () => {
      const wrapper = await mountComponent('my-reviews', '/reviews/my-reviews')
      
      expect(wrapper.text()).toContain('My Reviews')
      expect(wrapper.text()).toContain('Reviews you have written')
    })

    it('should call getMyReviews API', async () => {
      await mountComponent('my-reviews', '/reviews/my-reviews')
      
      expect(reviewService.getMyReviews).toHaveBeenCalled()
    })

    it('should not show property filter', async () => {
      const wrapper = await mountComponent('my-reviews', '/reviews/my-reviews')
      
      expect(wrapper.find('#filter-property').exists()).toBe(false)
    })

    it('should show delete button for customer reviews', async () => {
      const wrapper = await mountComponent('my-reviews', '/reviews/my-reviews')
      
      expect(wrapper.find('.btn-delete').exists()).toBe(true)
    })

    it('should show view details button', async () => {
      const wrapper = await mountComponent('my-reviews', '/reviews/my-reviews')
      
      expect(wrapper.find('.btn-detail').exists()).toBe(true)
    })
  })

  describe('Review Card Display', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(reviewService.getAllReviews).mockResolvedValue({ data: mockReviews })
    })

    it('should display property name', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Beach Hotel')
    })

    it('should display customer name', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('John Doe')
    })

    it('should display overall rating', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.rating-number').text()).toContain('4.5')
    })

    it('should display rating stars', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.star').length).toBeGreaterThan(0)
      expect(wrapper.findAll('.star.filled').length).toBeGreaterThan(0)
    })

    it('should display rating breakdown', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Cleanliness')
      expect(wrapper.text()).toContain('Facilities')
      expect(wrapper.text()).toContain('Service')
      expect(wrapper.text()).toContain('Value')
    })

    it('should display review comment', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Great stay!')
    })

    it('should display formatted date', async () => {
      const wrapper = await mountComponent()
      
      // Should format the date
      expect(wrapper.text()).toContain('2024')
    })
  })

  describe('Empty State', () => {
    it('should show empty state when no reviews', async () => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(reviewService.getAllReviews).mockResolvedValue({ data: [] })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.empty-state').exists()).toBe(true)
      expect(wrapper.text()).toContain('No Reviews Yet')
      expect(wrapper.text()).toContain('Be the first to write a review')
    })
  })

  describe('View Details Action', () => {
    beforeEach(() => {
      vi.mocked(reviewService.getMyReviews).mockResolvedValue({ data: mockReviews })
    })

    it('should navigate to review detail when view details clicked', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      
      const wrapper = await mountComponent('my-reviews', '/reviews/my-reviews')
      await wrapper.find('.btn-detail').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith('/reviews/review-1')
    })
  })

  describe('Delete Review Action', () => {
    beforeEach(() => {
      vi.mocked(reviewService.getMyReviews).mockResolvedValue({ data: mockReviews })
    })

    it('should confirm before deleting', async () => {
      vi.mocked(window.confirm).mockReturnValue(false)
      
      const wrapper = await mountComponent('my-reviews', '/reviews/my-reviews')
      await wrapper.find('.btn-delete').trigger('click')
      
      expect(window.confirm).toHaveBeenCalledWith('Are you sure you want to delete this review?')
      expect(reviewService.deleteReview).not.toHaveBeenCalled()
    })

    it('should delete review when confirmed', async () => {
      vi.mocked(window.confirm).mockReturnValue(true)
      vi.mocked(reviewService.deleteReview).mockResolvedValue({ success: true })
      
      const wrapper = await mountComponent('my-reviews', '/reviews/my-reviews')
      await wrapper.find('.btn-delete').trigger('click')
      await flushPromises()
      
      expect(reviewService.deleteReview).toHaveBeenCalledWith('review-1', expect.any(String))
    })

    it('should show error when delete fails', async () => {
      vi.mocked(window.confirm).mockReturnValue(true)
      vi.mocked(reviewService.deleteReview).mockRejectedValue({
        response: { data: { error: 'Delete failed' } }
      })
      
      const wrapper = await mountComponent('my-reviews', '/reviews/my-reviews')
      await wrapper.find('.btn-delete').trigger('click')
      await flushPromises()
      
      expect(window.alert).toHaveBeenCalledWith('Delete failed')
    })
  })

  describe('Refresh Action', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(reviewService.getAllReviews).mockResolvedValue({ data: mockReviews })
    })

    it('should have refresh button', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-secondary').exists()).toBe(true)
      expect(wrapper.text()).toContain('Refresh Reviews')
    })

    it('should reload reviews when refresh clicked', async () => {
      const wrapper = await mountComponent()
      
      vi.clearAllMocks()
      await wrapper.find('.btn-secondary').trigger('click')
      await flushPromises()
      
      expect(reviewService.getAllReviews).toHaveBeenCalled()
    })
  })

  describe('Rating Stars Display', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(reviewService.getAllReviews).mockResolvedValue({ data: mockReviews })
    })

    it('should display correct number of filled stars based on rating', async () => {
      const wrapper = await mountComponent()
      
      const firstCard = wrapper.findAll('.review-card')[0]
      const ratingStars = firstCard.find('.rating-stars')
      const filledStars = ratingStars.findAll('.star.filled')
      
      // 4.5 rating should have 4 filled stars
      expect(filledStars.length).toBeGreaterThanOrEqual(4)
    })
  })

  describe('Mini Stars in Rating Breakdown', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(reviewService.getAllReviews).mockResolvedValue({ data: mockReviews })
    })

    it('should display mini stars for each rating category', async () => {
      const wrapper = await mountComponent()
      
      const ratingBreakdown = wrapper.find('.rating-breakdown')
      expect(ratingBreakdown.findAll('.mini-stars').length).toBe(4)
    })
  })

  describe('Property Reviews View', () => {
    it('should display correct page title for property reviews', async () => {
      vi.mocked(reviewService.getReviewsByProperty).mockResolvedValue({ data: mockReviews })
      
      const router = createRouter({
        history: createWebHistory(),
        routes: [
          { path: '/reviews/property/:propertyId', name: 'property-reviews', component: ReviewListView }
        ]
      })
      
      router.push('/reviews/property/prop-1')
      await router.isReady()
      
      const wrapper = mount(ReviewListView, {
        global: { plugins: [router] }
      })
      await flushPromises()
      
      expect(wrapper.text()).toContain('Property Reviews')
    })
  })

  describe('Date Formatting', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(reviewService.getAllReviews).mockResolvedValue({ data: mockReviews })
    })

    it('should format date in readable format', async () => {
      const wrapper = await mountComponent()
      
      // Should contain formatted date
      expect(wrapper.text()).toContain('January')
      expect(wrapper.text()).toContain('2024')
    })

    it('should handle invalid date gracefully', async () => {
      vi.mocked(reviewService.getAllReviews).mockResolvedValue({
        data: [{ ...mockReviews[0], createdDate: 'invalid-date' }]
      })
      
      const wrapper = await mountComponent()
      
      // Should not crash
      expect(wrapper.find('.review-card').exists()).toBe(true)
    })
  })

  describe('Review Comment Display', () => {
    it('should display review comment in card', async () => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(reviewService.getAllReviews).mockResolvedValue({ data: mockReviews })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.review-comment').exists()).toBe(true)
      expect(wrapper.text()).toContain('Great stay!')
    })

    it('should not display comment section if no comment', async () => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(reviewService.getAllReviews).mockResolvedValue({
        data: [{ ...mockReviews[0], comment: null }]
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.review-comment').exists()).toBe(false)
    })
  })

  describe('Responsive Grid', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(reviewService.getAllReviews).mockResolvedValue({ data: mockReviews })
    })

    it('should render reviews in grid layout', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.reviews-grid').exists()).toBe(true)
    })
  })
})
