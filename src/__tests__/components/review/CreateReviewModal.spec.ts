import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import CreateReviewModal from '@/components/review/CreateReviewModal.vue'
import reviewService from '@/services/reviewService'

// Mock the services
vi.mock('@/services/reviewService', () => ({
  default: {
    createReview: vi.fn()
  }
}))

describe('CreateReviewModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(reviewService.createReview).mockResolvedValue({
      success: true,
      message: 'Review created'
    })
  })

  const mountComponent = (props = {}) => {
    return mount(CreateReviewModal, {
      props: {
        bookingId: 'BOOK-12345',
        customerId: 'CUST-123',
        ...props
      }
    })
  }

  describe('Modal Rendering', () => {
    it('should render modal', () => {
      const wrapper = mountComponent()
      expect(wrapper.exists()).toBe(true)
    })

    it('should display modal title', () => {
      const wrapper = mountComponent()
      expect(wrapper.text()).toContain('Write a Review')
    })

    it('should have modal header', () => {
      const wrapper = mountComponent()
      expect(wrapper.find('.modal-header').exists()).toBe(true)
    })

    it('should have modal body', () => {
      const wrapper = mountComponent()
      expect(wrapper.find('.modal-body').exists()).toBe(true)
    })

    it('should have close button', () => {
      const wrapper = mountComponent()
      expect(wrapper.find('.close-btn').exists()).toBe(true)
    })
  })

  describe('Rating Groups', () => {
    it('should have cleanliness rating', () => {
      const wrapper = mountComponent()
      expect(wrapper.text()).toContain('Cleanliness')
    })

    it('should have facilities rating', () => {
      const wrapper = mountComponent()
      expect(wrapper.text()).toContain('Facilities')
    })

    it('should have service rating', () => {
      const wrapper = mountComponent()
      expect(wrapper.text()).toContain('Service')
    })

    it('should have value rating', () => {
      const wrapper = mountComponent()
      expect(wrapper.text()).toContain('Value for Money')
    })

    it('should have 5 star buttons per rating group', () => {
      const wrapper = mountComponent()
      const stars = wrapper.findAll('.star')
      expect(stars.length).toBe(20) // 4 rating groups * 5 stars
    })
  })

  describe('Form Fields', () => {
    it('should have comment textarea', () => {
      const wrapper = mountComponent()
      expect(wrapper.find('textarea').exists()).toBe(true)
    })

    it('should have submit button', () => {
      const wrapper = mountComponent()
      const buttons = wrapper.findAll('button')
      const hasSubmitBtn = buttons.some(b => b.text().includes('Submit Review'))
      expect(hasSubmitBtn).toBe(true)
    })

    it('should have cancel button', () => {
      const wrapper = mountComponent()
      const buttons = wrapper.findAll('button')
      const hasCancelBtn = buttons.some(b => b.text().includes('Cancel'))
      expect(hasCancelBtn).toBe(true)
    })
  })

  describe('Rating Selection', () => {
    it('should update rating when star clicked', async () => {
      const wrapper = mountComponent()
      const stars = wrapper.findAll('.star')
      
      // Click the 3rd star of the first rating group
      await stars[2].trigger('click')
      
      expect(stars[0].classes()).toContain('active')
      expect(stars[1].classes()).toContain('active')
      expect(stars[2].classes()).toContain('active')
    })

    it('should style active stars', async () => {
      const wrapper = mountComponent()
      const stars = wrapper.findAll('.star')
      
      await stars[4].trigger('click') // Click 5th star of first group
      
      expect(stars[0].classes()).toContain('active')
    })
  })

  describe('Form Validation', () => {
    it('should disable submit button if no ratings', () => {
      const wrapper = mountComponent()
      const submitBtn = wrapper.findAll('button').find(b => b.text().includes('Submit Review'))
      expect(submitBtn?.attributes('disabled')).toBeDefined()
    })

    it('should enable submit button when all ratings selected', async () => {
      const wrapper = mountComponent()
      const stars = wrapper.findAll('.star')
      
      // Select all 4 ratings
      await stars[0].trigger('click')  // Cleanliness
      await stars[5].trigger('click')  // Facilities
      await stars[10].trigger('click') // Service
      await stars[15].trigger('click') // Value
      
      const submitBtn = wrapper.findAll('button').find(b => b.text().includes('Submit Review'))
      expect(submitBtn?.attributes('disabled')).toBeUndefined()
    })
  })

  describe('Form Submission', () => {
    it('should call createReview on valid submit', async () => {
      const wrapper = mountComponent()
      const stars = wrapper.findAll('.star')
      
      // Fill all ratings
      await stars[0].trigger('click')
      await stars[5].trigger('click')
      await stars[10].trigger('click')
      await stars[15].trigger('click')
      
      // Fill comment
      await wrapper.find('textarea').setValue('Great experience!')
      
      // Submit form
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()
      
      expect(reviewService.createReview).toHaveBeenCalled()
    })

    it('should emit success on successful submission', async () => {
      const wrapper = mountComponent()
      const stars = wrapper.findAll('.star')
      
      // Fill all ratings
      await stars[0].trigger('click')
      await stars[5].trigger('click')
      await stars[10].trigger('click')
      await stars[15].trigger('click')
      
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()
      
      expect(wrapper.emitted('success')).toBeTruthy()
    })

    it('should emit close on successful submission', async () => {
      const wrapper = mountComponent()
      const stars = wrapper.findAll('.star')
      
      // Fill all ratings
      await stars[0].trigger('click')
      await stars[5].trigger('click')
      await stars[10].trigger('click')
      await stars[15].trigger('click')
      
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()
      
      expect(wrapper.emitted('close')).toBeTruthy()
    })
  })

  describe('Cancel Action', () => {
    it('should emit close when cancel button clicked', async () => {
      const wrapper = mountComponent()
      const cancelBtn = wrapper.findAll('button').find(b => b.text().includes('Cancel'))
      
      await cancelBtn?.trigger('click')
      
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('should emit close when close button clicked', async () => {
      const wrapper = mountComponent()
      await wrapper.find('.close-btn').trigger('click')
      
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('should emit close when clicking overlay', async () => {
      const wrapper = mountComponent()
      await wrapper.find('.create-review-modal').trigger('click')
      
      expect(wrapper.emitted('close')).toBeTruthy()
    })
  })

  describe('Error Handling', () => {
    it('should display error message on failure', async () => {
      vi.mocked(reviewService.createReview).mockRejectedValue({ 
        response: { data: { error: 'Failed to submit' } }
      })
      
      const wrapper = mountComponent()
      const stars = wrapper.findAll('.star')
      
      await stars[0].trigger('click')
      await stars[5].trigger('click')
      await stars[10].trigger('click')
      await stars[15].trigger('click')
      
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()
      
      expect(wrapper.text()).toContain('Failed to submit')
    })

    it('should show error when ratings not completed', async () => {
      const wrapper = mountComponent()
      
      // Only select one rating
      const stars = wrapper.findAll('.star')
      await stars[0].trigger('click')
      
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()
      
      expect(wrapper.text()).toContain('Please rate all categories')
    })
  })

  describe('Loading State', () => {
    it('should show submitting text during submission', async () => {
      vi.mocked(reviewService.createReview).mockImplementation(() => 
        new Promise(resolve => setTimeout(resolve, 1000))
      )
      
      const wrapper = mountComponent()
      const stars = wrapper.findAll('.star')
      
      await stars[0].trigger('click')
      await stars[5].trigger('click')
      await stars[10].trigger('click')
      await stars[15].trigger('click')
      
      wrapper.find('form').trigger('submit.prevent')
      await wrapper.vm.$nextTick()
      
      expect(wrapper.text()).toContain('Submitting...')
    })
  })
})
