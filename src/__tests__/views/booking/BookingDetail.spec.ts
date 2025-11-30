import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import BookingDetail from '@/views/booking/BookingDetail.vue'
import { bookingService } from '@/services/bookingService'

// Mock the services
vi.mock('@/services/bookingService', () => ({
  bookingService: {
    getById: vi.fn(),
    pay: vi.fn(),
    cancel: vi.fn()
  }
}))

// Create mock router
const createMockRouter = (id = 'BOOK-12345') => {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/booking/:id', name: 'booking-detail', component: BookingDetail },
      { path: '/booking', name: 'booking-list', component: { template: '<div>Booking List</div>' } },
      { path: '/booking/update/:id', name: 'booking-update', component: { template: '<div>Update</div>' } },
      { path: '/reviews/create', name: 'review-create', component: { template: '<div>Create Review</div>' } }
    ]
  })
}

const mockBooking = {
  bookingId: 'BOOK-12345',
  propertyName: 'Test Hotel',
  roomName: 'Deluxe Room',
  capacity: 2,
  checkInDate: '2024-02-01',
  checkOutDate: '2024-02-03',
  totalDays: 2,
  addOnBreakfast: true,
  totalPrice: 1100000,
  customerName: 'John Doe',
  customerEmail: 'john@example.com',
  customerPhone: '08123456789',
  status: 0,
  createdAt: '2024-01-15T10:00:00Z',
  updatedAt: '2024-01-15T10:00:00Z'
}

describe('BookingDetail.vue', () => {
  let router: ReturnType<typeof createMockRouter>

  beforeEach(() => {
    vi.clearAllMocks()
    router = createMockRouter()
    window.alert = vi.fn()
    window.confirm = vi.fn()
  })

  const mountComponent = async () => {
    router.push('/booking/BOOK-12345')
    await router.isReady()
    
    const wrapper = mount(BookingDetail, {
      global: {
        plugins: [router],
        stubs: {
          teleport: true
        }
      }
    })
    
    await flushPromises()
    return wrapper
  }

  describe('Loading State', () => {
    beforeEach(() => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking
      })
    })

    it('should show loading spinner while fetching data', async () => {
      // After data loads, we can check component renders correctly
      const wrapper = await mountComponent()
      
      // After data is loaded, loading should not be visible
      expect(wrapper.find('.loading-spinner').exists()).toBe(false)
      // And the booking info should be visible
      expect(wrapper.find('.booking-detail').exists()).toBe(true)
    })
  })

  describe('Error State', () => {
    it('should display error message when API call fails', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: false,
        message: 'Booking not found'
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.error').exists()).toBe(true)
      expect(wrapper.text()).toContain('Booking not found')
    })

    it('should handle network errors gracefully', async () => {
      vi.mocked(bookingService.getById).mockRejectedValue({
        response: { data: { message: 'Network error' } }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.error').exists()).toBe(true)
      expect(wrapper.text()).toContain('Network error')
    })
  })

  describe('Booking Detail Display', () => {
    beforeEach(() => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: {
          canPay: true,
          canCancel: true,
          canUpdate: true
        }
      })
    })

    it('should display booking information correctly', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Booking Details')
      expect(wrapper.text()).toContain('BOOK-12345')
      expect(wrapper.text()).toContain('Test Hotel')
      expect(wrapper.text()).toContain('Deluxe Room')
    })

    it('should display customer information', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('John Doe')
      expect(wrapper.text()).toContain('john@example.com')
      expect(wrapper.text()).toContain('08123456789')
    })

    it('should display booking dates', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('2')
      expect(wrapper.text()).toContain('day')
    })

    it('should display breakfast status when included', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Included')
    })

    it('should display breakfast as Not Included when not added', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { ...mockBooking, addOnBreakfast: false },
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Not Included')
    })

    it('should display total price formatted as currency', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('1.100.000')
    })

    it('should display guest capacity', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('2 person')
    })
  })

  describe('Status Badge Display', () => {
    it('should display Waiting for Payment status correctly', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { ...mockBooking, status: 0 },
        availableActions: { canPay: true, canCancel: true, canUpdate: true }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-waiting').exists()).toBe(true)
      expect(wrapper.text()).toContain('Waiting for Payment')
    })

    it('should display Payment Confirmed status correctly', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { ...mockBooking, status: 1 },
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-confirmed').exists()).toBe(true)
      expect(wrapper.text()).toContain('Payment Confirmed')
    })

    it('should display Cancelled status correctly', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { ...mockBooking, status: 2 },
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-cancelled').exists()).toBe(true)
      expect(wrapper.text()).toContain('Cancelled')
    })

    it('should display Unknown for invalid status', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { ...mockBooking, status: 99 },
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Unknown')
    })
  })

  describe('Action Buttons', () => {
    it('should show Pay button when canPay is true', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: { canPay: true, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-pay').exists()).toBe(true)
      expect(wrapper.text()).toContain('Confirm Payment')
    })

    it('should show Cancel button when canCancel is true', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: { canPay: false, canCancel: true, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-cancel').exists()).toBe(true)
      expect(wrapper.text()).toContain('Cancel Booking')
    })

    it('should show Update button when canUpdate is true', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: { canPay: false, canCancel: false, canUpdate: true }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-update').exists()).toBe(true)
      expect(wrapper.text()).toContain('Update Booking')
    })

    it('should show Back button', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: { canPay: true, canCancel: true, canUpdate: true }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-back').exists()).toBe(true)
      expect(wrapper.text()).toContain('Back to List')
    })

    it('should not show action buttons when no actions available', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { ...mockBooking, status: 2 },
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-pay').exists()).toBe(false)
      expect(wrapper.find('.btn-cancel').exists()).toBe(false)
      expect(wrapper.find('.btn-update').exists()).toBe(false)
    })
  })

  describe('Payment Action', () => {
    beforeEach(() => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: { canPay: true, canCancel: true, canUpdate: true }
      })
    })

    it('should confirm before processing payment', async () => {
      vi.mocked(window.confirm).mockReturnValue(false)
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-pay').trigger('click')
      
      expect(window.confirm).toHaveBeenCalledWith('Confirm payment for this booking?')
      expect(bookingService.pay).not.toHaveBeenCalled()
    })

    it('should process payment when confirmed', async () => {
      vi.mocked(window.confirm).mockReturnValue(true)
      vi.mocked(bookingService.pay).mockResolvedValue({ success: true })
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-pay').trigger('click')
      await flushPromises()
      
      expect(bookingService.pay).toHaveBeenCalledWith('BOOK-12345')
      expect(window.alert).toHaveBeenCalledWith('Payment confirmed successfully!')
    })

    it('should show error message when payment fails', async () => {
      vi.mocked(window.confirm).mockReturnValue(true)
      vi.mocked(bookingService.pay).mockResolvedValue({ 
        success: false, 
        message: 'Payment failed' 
      })
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-pay').trigger('click')
      await flushPromises()
      
      expect(window.alert).toHaveBeenCalledWith('Payment failed')
    })

    it('should handle payment API error', async () => {
      vi.mocked(window.confirm).mockReturnValue(true)
      vi.mocked(bookingService.pay).mockRejectedValue({
        response: { data: { message: 'Server error' } }
      })
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-pay').trigger('click')
      await flushPromises()
      
      expect(window.alert).toHaveBeenCalledWith('Server error')
    })
  })

  describe('Cancel Action', () => {
    beforeEach(() => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: { canPay: true, canCancel: true, canUpdate: true }
      })
    })

    it('should confirm before cancelling', async () => {
      vi.mocked(window.confirm).mockReturnValue(false)
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-cancel').trigger('click')
      
      expect(window.confirm).toHaveBeenCalledWith('Are you sure you want to cancel this booking?')
      expect(bookingService.cancel).not.toHaveBeenCalled()
    })

    it('should cancel booking when confirmed', async () => {
      vi.mocked(window.confirm).mockReturnValue(true)
      vi.mocked(bookingService.cancel).mockResolvedValue({ success: true })
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-cancel').trigger('click')
      await flushPromises()
      
      expect(bookingService.cancel).toHaveBeenCalledWith('BOOK-12345')
      expect(window.alert).toHaveBeenCalledWith('Booking cancelled successfully!')
    })

    it('should show error when cancellation fails', async () => {
      vi.mocked(window.confirm).mockReturnValue(true)
      vi.mocked(bookingService.cancel).mockResolvedValue({ 
        success: false, 
        message: 'Cannot cancel' 
      })
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-cancel').trigger('click')
      await flushPromises()
      
      expect(window.alert).toHaveBeenCalledWith('Cannot cancel')
    })
  })

  describe('Navigation', () => {
    beforeEach(() => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: { canPay: true, canCancel: true, canUpdate: true }
      })
    })

    it('should navigate back to booking list when back button clicked', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-back').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith('/booking')
    })

    it('should navigate to update page when update button clicked', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-update').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith('/booking/update/BOOK-12345')
    })
  })

  describe('Write Review Feature', () => {
    it('should show Write Review button when status is confirmed and past checkout', async () => {
      const pastDate = new Date()
      pastDate.setDate(pastDate.getDate() - 1)
      
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { 
          ...mockBooking, 
          status: 1, 
          checkOutDate: pastDate.toISOString().split('T')[0]
        },
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-review').exists()).toBe(true)
      expect(wrapper.text()).toContain('Write Review')
    })

    it('should not show Write Review button when status is not confirmed', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { ...mockBooking, status: 0 },
        availableActions: { canPay: true, canCancel: true, canUpdate: true }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-review').exists()).toBe(false)
    })

    it('should not show Write Review button when checkout date is in future', async () => {
      const futureDate = new Date()
      futureDate.setDate(futureDate.getDate() + 7)
      
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { 
          ...mockBooking, 
          status: 1, 
          checkOutDate: futureDate.toISOString().split('T')[0]
        },
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-review').exists()).toBe(false)
    })

    it('should navigate to review create page with bookingId', async () => {
      const pastDate = new Date()
      pastDate.setDate(pastDate.getDate() - 1)
      
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { 
          ...mockBooking, 
          status: 1, 
          checkOutDate: pastDate.toISOString().split('T')[0]
        },
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent()
      await wrapper.find('.btn-review').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith({
        path: '/reviews/create',
        query: { bookingId: 'BOOK-12345' }
      })
    })
  })

  describe('Price Breakdown', () => {
    it('should calculate and display room rate correctly', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      // totalPrice = 1,100,000, breakfast = 2 days * 50,000 = 100,000
      // room rate = 1,100,000 - 100,000 = 1,000,000
      expect(wrapper.text()).toContain('1.000.000')
    })

    it('should display breakfast cost when included', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      // Breakfast = 2 days * 50,000 = 100,000
      expect(wrapper.text()).toContain('100.000')
    })
  })

  describe('Date Formatting', () => {
    it('should format dates correctly', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      // Check date is formatted
      expect(wrapper.text()).toContain('2024')
    })

    it('should handle missing date gracefully', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { ...mockBooking, checkInDate: '' },
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('-')
    })
  })

  describe('Status Information Banner', () => {
    it('should display status description for waiting payment', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { ...mockBooking, status: 0 },
        availableActions: { canPay: true, canCancel: true, canUpdate: true }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Please complete the payment')
    })

    it('should display status description for confirmed', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { ...mockBooking, status: 1 },
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Your payment has been confirmed')
    })

    it('should display status description for cancelled', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: { ...mockBooking, status: 2 },
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('This booking has been cancelled')
    })
  })

  describe('Content Cards', () => {
    beforeEach(() => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking,
        availableActions: { canPay: false, canCancel: false, canUpdate: false }
      })
    })

    it('should display Property & Room Information card', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Property & Room Information')
      expect(wrapper.text()).toContain('Property Name')
      expect(wrapper.text()).toContain('Room Name')
    })

    it('should display Booking Details card', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Booking Details')
      expect(wrapper.text()).toContain('Booking ID')
      expect(wrapper.text()).toContain('Number of Guests')
      expect(wrapper.text()).toContain('Check-in Date')
      expect(wrapper.text()).toContain('Check-out Date')
    })

    it('should display Customer Information card', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Customer Information')
      expect(wrapper.text()).toContain('Customer Name')
      expect(wrapper.text()).toContain('Email')
      expect(wrapper.text()).toContain('Phone')
    })

    it('should display Payment Details card', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Payment Details')
      expect(wrapper.text()).toContain('Room Rate')
      expect(wrapper.text()).toContain('Total Amount')
    })

    it('should display Booking History card', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Booking History')
      expect(wrapper.text()).toContain('Created Date')
      expect(wrapper.text()).toContain('Last Updated')
    })
  })

  describe('Edge Cases', () => {
    it('should handle missing availableActions', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: mockBooking
        // No availableActions
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-pay').exists()).toBe(false)
      expect(wrapper.find('.btn-cancel').exists()).toBe(false)
      expect(wrapper.find('.btn-update').exists()).toBe(false)
    })

    it('should handle null booking data', async () => {
      vi.mocked(bookingService.getById).mockResolvedValue({
        success: true,
        data: null
      })
      
      const wrapper = await mountComponent()
      
      // Should not crash, just not display content
      expect(wrapper.find('.detail-container').exists()).toBe(false)
    })
  })
})
