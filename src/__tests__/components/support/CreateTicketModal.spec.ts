import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import CreateTicketModal from '@/components/support/CreateTicketModal.vue'
import supportTicketService from '@/services/supportTicketService'

// Mock the services
vi.mock('@/services/supportTicketService', () => ({
  default: {
    createTicket: vi.fn(),
    getAvailableBookings: vi.fn()
  }
}))

describe('CreateTicketModal.vue', () => {
  const mockBookings = [
    { bookingId: 'BOOK-001', propertyName: 'Test Hotel', checkInDate: '2024-01-15' },
    { bookingId: 'BOOK-002', propertyName: 'Another Hotel', checkInDate: '2024-02-15' }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(supportTicketService.createTicket).mockResolvedValue({
      success: true,
      message: 'Ticket created'
    })
    vi.mocked(supportTicketService.getAvailableBookings).mockResolvedValue({
      data: mockBookings
    })
  })

  const mountComponent = (props = {}) => {
    return mount(CreateTicketModal, {
      props: {
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
      expect(wrapper.text()).toContain('Create Support Ticket')
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

  describe('Step 1: Service Selection', () => {
    it('should display step 1 by default', () => {
      const wrapper = mountComponent()
      expect(wrapper.text()).toContain('Step 1: Select Service')
    })

    it('should have service source select', () => {
      const wrapper = mountComponent()
      const select = wrapper.find('select')
      expect(select.exists()).toBe(true)
    })

    it('should have accommodation option', () => {
      const wrapper = mountComponent()
      expect(wrapper.text()).toContain('Accommodation')
    })

    it('should have flight option', () => {
      const wrapper = mountComponent()
      expect(wrapper.text()).toContain('Flight')
    })

    it('should have rental option', () => {
      const wrapper = mountComponent()
      expect(wrapper.text()).toContain('Vehicle Rental')
    })

    it('should have next button', () => {
      const wrapper = mountComponent()
      const buttons = wrapper.findAll('button')
      const hasNextBtn = buttons.some(b => b.text().includes('Next'))
      expect(hasNextBtn).toBe(true)
    })

    it('should disable next button without selection', () => {
      const wrapper = mountComponent()
      const nextBtn = wrapper.findAll('button').find(b => b.text().includes('Next'))
      expect(nextBtn?.attributes('disabled')).toBeDefined()
    })

    it('should enable next button after selection', async () => {
      const wrapper = mountComponent()
      const select = wrapper.find('select')
      
      await select.setValue('ACCOMMODATION')
      
      const nextBtn = wrapper.findAll('button').find(b => b.text().includes('Next'))
      expect(nextBtn?.attributes('disabled')).toBeUndefined()
    })
  })

  describe('Step 2: Booking Selection', () => {
    it('should show step 2 after next click', async () => {
      const wrapper = mountComponent()
      const select = wrapper.find('select')
      
      await select.setValue('ACCOMMODATION')
      const nextBtn = wrapper.findAll('button').find(b => b.text().includes('Next'))
      await nextBtn?.trigger('click')
      await flushPromises()
      
      expect(wrapper.text()).toContain('Step 2: Select Booking')
    })

    it('should have back button in step 2', async () => {
      const wrapper = mountComponent()
      const select = wrapper.find('select')
      
      await select.setValue('ACCOMMODATION')
      const nextBtn = wrapper.findAll('button').find(b => b.text().includes('Next'))
      await nextBtn?.trigger('click')
      await flushPromises()
      
      const hasBackBtn = wrapper.findAll('button').some(b => b.text().includes('Back'))
      expect(hasBackBtn).toBe(true)
    })

    it('should fetch bookings on step 2', async () => {
      const wrapper = mountComponent()
      const select = wrapper.find('select')
      
      await select.setValue('ACCOMMODATION')
      const nextBtn = wrapper.findAll('button').find(b => b.text().includes('Next'))
      await nextBtn?.trigger('click')
      await flushPromises()
      
      expect(supportTicketService.getAvailableBookings).toHaveBeenCalled()
    })
  })

  describe('Step 3: Issue Description', () => {
    it('should have subject input in step 3', async () => {
      const wrapper = mountComponent()
      
      // Go to step 2
      await wrapper.find('select').setValue('ACCOMMODATION')
      await wrapper.findAll('button').find(b => b.text().includes('Next'))?.trigger('click')
      await flushPromises()
      
      // Go to step 3
      const selects = wrapper.findAll('select')
      if (selects.length > 0) {
        await selects[0].setValue('BOOK-001')
      }
      await wrapper.findAll('button').find(b => b.text().includes('Next'))?.trigger('click')
      await flushPromises()
      
      expect(wrapper.text()).toContain('Step 3')
    })
  })

  describe('Close Action', () => {
    it('should emit close when close button clicked', async () => {
      const wrapper = mountComponent()
      await wrapper.find('.close-btn').trigger('click')
      
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('should emit close when overlay clicked', async () => {
      const wrapper = mountComponent()
      await wrapper.find('.modal-overlay').trigger('click')
      
      expect(wrapper.emitted('close')).toBeTruthy()
    })
  })

  describe('Initial Props', () => {
    it('should skip to step 2 with initialServiceSource', async () => {
      vi.mocked(supportTicketService.getAvailableBookings).mockResolvedValue({
        data: mockBookings
      })
      
      const wrapper = mountComponent({ initialServiceSource: 'ACCOMMODATION' })
      await flushPromises()
      
      expect(wrapper.text()).toContain('Step 2')
    })

    it('should skip to step 3 with initialBookingId', async () => {
      const wrapper = mountComponent({ 
        initialServiceSource: 'ACCOMMODATION',
        initialBookingId: 'BOOK-001'
      })
      await flushPromises()
      
      expect(wrapper.text()).toContain('Step 3')
    })
  })

  describe('Error Handling', () => {
    it('should display error when fetching bookings fails', async () => {
      vi.mocked(supportTicketService.getAvailableBookings).mockRejectedValue({
        response: { data: { error: 'Failed to fetch' } }
      })
      
      const wrapper = mountComponent()
      
      await wrapper.find('select').setValue('ACCOMMODATION')
      await wrapper.findAll('button').find(b => b.text().includes('Next'))?.trigger('click')
      await flushPromises()
      
      expect(wrapper.text()).toContain('Failed to fetch')
    })

    it('should display no bookings message when empty', async () => {
      vi.mocked(supportTicketService.getAvailableBookings).mockResolvedValue({
        data: []
      })
      
      const wrapper = mountComponent()
      
      await wrapper.find('select').setValue('ACCOMMODATION')
      await wrapper.findAll('button').find(b => b.text().includes('Next'))?.trigger('click')
      await flushPromises()
      
      expect(wrapper.text()).toContain('No bookings found')
    })
  })
})
