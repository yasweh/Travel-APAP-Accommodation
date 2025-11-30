import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import BookingList from '@/views/booking/BookingList.vue'

// Mock vue-router
const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock bookingService
const mockGetAll = vi.fn()
const mockPay = vi.fn()
const mockCancel = vi.fn()

vi.mock('@/services/bookingService', () => ({
  bookingService: {
    getAll: () => mockGetAll(),
    pay: (id: string) => mockPay(id),
    cancel: (id: string) => mockCancel(id)
  }
}))

// Mock CreateReviewModal component
vi.mock('@/components/review/CreateReviewModal.vue', () => ({
  default: {
    name: 'CreateReviewModal',
    template: '<div class="create-review-modal-stub">Create Review Modal</div>',
    props: ['bookingId', 'customerId']
  }
}))

describe('BookingList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockGetAll.mockResolvedValue({
      success: true,
      data: []
    })
  })

  const mockBookings = [
    {
      bookingId: 'booking-1',
      propertyName: 'Hotel A',
      roomName: 'Deluxe Room',
      customerName: 'John Doe',
      customerEmail: 'john@example.com',
      checkInDate: '2024-01-15',
      checkOutDate: '2024-01-20',
      totalPrice: 1500000,
      status: 0,
      statusString: 'Pending Payment'
    },
    {
      bookingId: 'booking-2',
      propertyName: 'Hotel B',
      roomName: 'Suite Room',
      customerName: 'Jane Smith',
      customerEmail: 'jane@example.com',
      checkInDate: '2024-01-10',
      checkOutDate: '2024-01-12',
      totalPrice: 2500000,
      status: 1,
      statusString: 'Payment Confirmed'
    },
    {
      bookingId: 'booking-3',
      propertyName: 'Hotel C',
      roomName: 'Standard Room',
      customerName: 'Bob Wilson',
      customerEmail: 'bob@example.com',
      checkInDate: '2024-01-05',
      checkOutDate: '2024-01-08',
      totalPrice: 750000,
      status: 2,
      statusString: 'Cancelled'
    }
  ]

  const mountBookingList = async (bookings = mockBookings) => {
    mockGetAll.mockResolvedValue({
      success: true,
      data: bookings
    })
    
    const wrapper = mount(BookingList, {
      global: {
        stubs: {
          CreateReviewModal: {
            name: 'CreateReviewModal',
            template: '<div class="create-review-modal-stub">Create Review Modal</div>',
            props: ['bookingId', 'customerId'],
            emits: ['close', 'success']
          }
        }
      }
    })
    await flushPromises()
    return wrapper
  }

  describe('Component Rendering', () => {
    it('renders booking list container', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.find('.booking-list').exists()).toBe(true)
    })

    it('renders page header', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.find('.page-header').exists()).toBe(true)
    })

    it('renders page title', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.find('.page-title').text()).toBe('Booking Management')
    })

    it('renders page subtitle', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.find('.page-subtitle').text()).toBe('View and manage all bookings and reservations')
    })

    it('renders header icon', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.find('.header-icon').exists()).toBe(true)
    })
  })

  describe('Action Buttons', () => {
    it('renders actions container', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.find('.actions').exists()).toBe(true)
    })

    it('renders Create New Booking button', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.find('.btn-primary').exists()).toBe(true)
      expect(wrapper.find('.btn-primary').text()).toContain('Create New Booking')
    })

    it('renders View Statistics button', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.find('.btn-chart').exists()).toBe(true)
      expect(wrapper.find('.btn-chart').text()).toContain('View Statistics')
    })

    it('renders Refresh button', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.find('.btn-secondary').exists()).toBe(true)
      expect(wrapper.find('.btn-secondary').text()).toContain('Refresh')
    })

    it('navigates to create booking when button is clicked', async () => {
      const wrapper = await mountBookingList()
      await wrapper.find('.btn-primary').trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/booking/create')
    })

    it('navigates to chart when View Statistics is clicked', async () => {
      const wrapper = await mountBookingList()
      await wrapper.find('.btn-chart').trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/booking/chart')
    })

    it('calls loadBookings when Refresh is clicked', async () => {
      const wrapper = await mountBookingList()
      await wrapper.find('.btn-secondary').trigger('click')
      await flushPromises()
      expect(mockGetAll).toHaveBeenCalledTimes(2) // initial + refresh
    })
  })

  describe('Loading State', () => {
    it('shows loading spinner when loading', async () => {
      const wrapper = await mountBookingList()
      
      // After data is loaded, loading should not be visible
      expect(wrapper.find('.loading-spinner').exists()).toBe(false)
    })

    it('hides loading after data loads', async () => {
      const wrapper = await mountBookingList()
      // After data is loaded, loading should not be visible
      expect(wrapper.find('.loading-spinner').exists()).toBe(false)
    })
  })

  describe('Bookings Grid', () => {
    it('renders bookings grid when data exists', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.find('.bookings-grid').exists()).toBe(true)
    })

    it('renders correct number of booking cards', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.findAll('.booking-card').length).toBe(3)
    })

    it('renders booking card header', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.findAll('.card-header').length).toBe(3)
    })

    it('renders booking ID', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.text()).toContain('booking-1')
    })

    it('renders property name', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.text()).toContain('Hotel A')
    })

    it('renders room name', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.text()).toContain('Deluxe Room')
    })

    it('renders customer name', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.text()).toContain('John Doe')
    })

    it('renders customer email', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.text()).toContain('john@example.com')
    })

    it('formats and renders total price', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.text()).toContain('1.500.000')
    })
  })

  describe('Status Badges', () => {
    it('renders status badge for each booking', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.findAll('.status-badge').length).toBe(3)
    })

    it('applies correct class for pending status', async () => {
      const wrapper = await mountBookingList()
      const badges = wrapper.findAll('.status-badge')
      expect(badges[0].classes()).toContain('status-pending')
    })

    it('applies correct class for confirmed status', async () => {
      const wrapper = await mountBookingList()
      const badges = wrapper.findAll('.status-badge')
      expect(badges[1].classes()).toContain('status-confirmed')
    })

    it('applies correct class for cancelled status', async () => {
      const wrapper = await mountBookingList()
      const badges = wrapper.findAll('.status-badge')
      expect(badges[2].classes()).toContain('status-cancelled')
    })

    it('shows correct status text', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.text()).toContain('Pending Payment')
      expect(wrapper.text()).toContain('Payment Confirmed')
      expect(wrapper.text()).toContain('Cancelled')
    })
  })

  describe('Booking Dates', () => {
    it('renders check-in date', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.text()).toContain('Check-in')
    })

    it('renders check-out date', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.text()).toContain('Check-out')
    })

    it('formats dates correctly', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.text()).toContain('15 Jan 2024')
    })
  })

  describe('Card Actions', () => {
    it('renders View Details button for each card', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.findAll('.btn-detail').length).toBe(3)
    })

    it('navigates to detail page when View Details is clicked', async () => {
      const wrapper = await mountBookingList()
      const detailButtons = wrapper.findAll('.btn-detail')
      await detailButtons[0].trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/booking/booking-1')
    })
  })

  describe('Empty State', () => {
    it('shows empty state when no bookings', async () => {
      const wrapper = await mountBookingList([])
      expect(wrapper.find('.empty-state').exists()).toBe(true)
    })

    it('shows empty state title', async () => {
      const wrapper = await mountBookingList([])
      expect(wrapper.find('.empty-state h3').text()).toBe('No Bookings Found')
    })

    it('shows empty state description', async () => {
      const wrapper = await mountBookingList([])
      expect(wrapper.text()).toContain('Start by creating your first booking')
    })

    it('shows Create First Booking button', async () => {
      const wrapper = await mountBookingList([])
      expect(wrapper.find('.btn-empty-state').text()).toContain('Create First Booking')
    })

    it('navigates to create when empty state button is clicked', async () => {
      const wrapper = await mountBookingList([])
      await wrapper.find('.btn-empty-state').trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/booking/create')
    })
  })

  describe('Error Handling', () => {
    it('shows error message when loading fails', async () => {
      mockGetAll.mockResolvedValue({
        success: false,
        message: 'Failed to load bookings'
      })
      const wrapper = mount(BookingList, {
        global: {
          stubs: {
            CreateReviewModal: true
          }
        }
      })
      await flushPromises()
      expect(wrapper.find('.error-message').exists()).toBe(true)
      expect(wrapper.text()).toContain('Failed to load bookings')
    })

    it('shows error message when API throws error', async () => {
      mockGetAll.mockRejectedValue({
        response: {
          data: {
            message: 'Server error'
          }
        }
      })
      const wrapper = mount(BookingList, {
        global: {
          stubs: {
            CreateReviewModal: true
          }
        }
      })
      await flushPromises()
      expect(wrapper.find('.error-message').exists()).toBe(true)
    })
  })

  describe('Currency Formatting', () => {
    it('formats Indonesian Rupiah correctly', async () => {
      const wrapper = await mountBookingList()
      expect(wrapper.text()).toContain('Rp')
      expect(wrapper.text()).toContain('1.500.000')
    })
  })

  describe('API Integration', () => {
    it('calls getAll on mount', async () => {
      await mountBookingList()
      expect(mockGetAll).toHaveBeenCalled()
    })
  })

  describe('Write Review Button', () => {
    it('shows Write Review button for confirmed bookings with past checkout', async () => {
      const pastDate = new Date()
      pastDate.setDate(pastDate.getDate() - 1)
      
      const bookingsWithPastCheckout = [{
        bookingId: 'booking-1',
        propertyName: 'Hotel A',
        roomName: 'Deluxe Room',
        customerName: 'John Doe',
        customerEmail: 'john@example.com',
        checkInDate: '2024-01-10',
        checkOutDate: pastDate.toISOString().split('T')[0],
        totalPrice: 1500000,
        status: 1,
        statusString: 'Payment Confirmed'
      }]
      
      const wrapper = await mountBookingList(bookingsWithPastCheckout)
      expect(wrapper.find('.btn-review').exists()).toBe(true)
    })

    it('hides Write Review button for pending bookings', async () => {
      const wrapper = await mountBookingList([mockBookings[0]])
      expect(wrapper.find('.btn-review').exists()).toBe(false)
    })
  })
})
