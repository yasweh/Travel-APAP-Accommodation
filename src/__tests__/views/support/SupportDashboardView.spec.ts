import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import SupportDashboardView from '@/views/support/SupportDashboardView.vue'
import supportTicketService from '@/services/supportTicketService'

// Mock the services
vi.mock('@/services/supportTicketService', () => ({
  default: {
    getAvailableBookings: vi.fn(),
    getAllTickets: vi.fn()
  }
}))

// Mock BookingCard component
vi.mock('@/components/BookingCard.vue', () => ({
  default: {
    name: 'BookingCard',
    props: ['booking', 'serviceType'],
    emits: ['create-ticket'],
    template: '<div class="booking-card" @click="$emit(\'create-ticket\', booking, serviceType)">{{ booking.id }}</div>'
  }
}))

// Mock CreateTicketModal component
vi.mock('@/components/support/CreateTicketModal.vue', () => ({
  default: {
    name: 'CreateTicketModal',
    props: ['initialServiceSource', 'initialBookingId'],
    emits: ['close', 'ticket-created'],
    template: '<div class="create-ticket-modal"></div>'
  }
}))

// Create mock router
const createMockRouter = () => {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/support', name: 'support-dashboard', component: SupportDashboardView },
      { path: '/support/tickets/:id', name: 'ticket-detail', component: { template: '<div>Detail</div>' } }
    ]
  })
}

const mockAccommodationBookings = [
  { id: 'acc-1', propertyName: 'Beach Hotel' },
  { id: 'acc-2', propertyName: 'Mountain Resort' }
]

const mockFlightBookings = [
  { id: 'flt-1', flightNumber: 'GA123' }
]

const mockRentalBookings = [
  { id: 'rnt-1', vehicleType: 'SUV' }
]

const mockTourBookings = [
  { id: 'tour-1', tourName: 'Bali Tour' }
]

const mockInsuranceBookings = [
  { id: 'ins-1', policyType: 'Travel' }
]

const mockSupportTickets = [
  {
    id: 'ticket-1',
    subject: 'Issue with booking',
    status: 'OPEN',
    serviceSource: 'ACCOMMODATION',
    createdAt: '2024-01-15T10:00:00Z',
    unreadMessagesCount: 2
  },
  {
    id: 'ticket-2',
    subject: 'Refund request',
    status: 'IN_PROGRESS',
    serviceSource: 'FLIGHT',
    createdAt: '2024-01-14T08:00:00Z',
    unreadMessagesCount: 0
  }
]

describe('SupportDashboardView.vue', () => {
  let router: ReturnType<typeof createMockRouter>

  beforeEach(() => {
    vi.clearAllMocks()
    router = createMockRouter()
  })

  const setupMocks = (options: {
    accommodation?: any[],
    flight?: any[],
    rental?: any[],
    tour?: any[],
    insurance?: any[],
    tickets?: any[]
  } = {}) => {
    vi.mocked(supportTicketService.getAvailableBookings).mockImplementation((serviceType) => {
      switch (serviceType) {
        case 'ACCOMMODATION':
          return Promise.resolve({ data: options.accommodation ?? mockAccommodationBookings })
        case 'FLIGHT':
          return Promise.resolve({ data: options.flight ?? mockFlightBookings })
        case 'RENTAL':
          return Promise.resolve({ data: options.rental ?? mockRentalBookings })
        case 'TOUR':
          return Promise.resolve({ data: options.tour ?? mockTourBookings })
        case 'INSURANCE':
          return Promise.resolve({ data: options.insurance ?? mockInsuranceBookings })
        default:
          return Promise.resolve({ data: [] })
      }
    })
    
    vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({
      data: options.tickets ?? mockSupportTickets
    })
  }

  const mountComponent = async () => {
    router.push('/support')
    await router.isReady()
    
    const wrapper = mount(SupportDashboardView, {
      global: {
        plugins: [router]
      }
    })
    
    await flushPromises()
    return wrapper
  }

  describe('Page Header', () => {
    beforeEach(() => setupMocks())

    it('should display page title', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.page-title').text()).toBe('Support Dashboard')
    })

    it('should display page subtitle', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Manage your bookings and support tickets')
    })

    it('should display header icon', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.header-icon').exists()).toBe(true)
    })
  })

  describe('Refresh Button', () => {
    beforeEach(() => setupMocks())

    it('should have refresh button', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-secondary').exists()).toBe(true)
      expect(wrapper.text()).toContain('Refresh Dashboard')
    })

    it('should reload data when clicked', async () => {
      const wrapper = await mountComponent()
      
      vi.clearAllMocks()
      setupMocks()
      
      await wrapper.find('.btn-secondary').trigger('click')
      await flushPromises()
      
      expect(supportTicketService.getAvailableBookings).toHaveBeenCalled()
      expect(supportTicketService.getAllTickets).toHaveBeenCalled()
    })
  })

  describe('Loading State', () => {
    it('should show loading spinner initially', async () => {
      let resolvePromise: (value: any) => void
      vi.mocked(supportTicketService.getAvailableBookings).mockImplementation(() => 
        new Promise(resolve => { resolvePromise = resolve })
      )
      vi.mocked(supportTicketService.getAllTickets).mockImplementation(() => 
        new Promise(resolve => {})
      )
      
      router.push('/support')
      await router.isReady()
      
      const wrapper = mount(SupportDashboardView, {
        global: { plugins: [router] }
      })
      
      // Check for loading indicator presence in HTML
      expect(wrapper.html().toLowerCase()).toContain('loading')
    })
  })

  describe('Accommodation Bookings Section', () => {
    beforeEach(() => setupMocks())

    it('should display section title', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Accommodation Bookings')
    })

    it('should display booking count badge', async () => {
      const wrapper = await mountComponent()
      
      const badges = wrapper.findAll('.section-badge')
      expect(badges[0].text()).toBe('2')
    })

    it('should display booking cards', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.booking-card').length).toBeGreaterThanOrEqual(2)
    })

    it('should show empty state when no bookings', async () => {
      setupMocks({ accommodation: [] })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('No accommodation bookings found')
    })

    it('should handle loading error', async () => {
      vi.mocked(supportTicketService.getAvailableBookings).mockImplementation((serviceType) => {
        if (serviceType === 'ACCOMMODATION') {
          return Promise.reject(new Error('Failed to load'))
        }
        return Promise.resolve({ data: [] })
      })
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: [] })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Failed to load')
    })
  })

  describe('Flight Bookings Section', () => {
    beforeEach(() => setupMocks())

    it('should display section title', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Flight Bookings')
    })

    it('should display booking count', async () => {
      const wrapper = await mountComponent()
      
      const badges = wrapper.findAll('.section-badge')
      expect(badges[1].text()).toBe('1')
    })
  })

  describe('Rental Bookings Section', () => {
    beforeEach(() => setupMocks())

    it('should display section title', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Vehicle Rentals')
    })
  })

  describe('Tour Bookings Section', () => {
    beforeEach(() => setupMocks())

    it('should display section title', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Tour Packages')
    })
  })

  describe('Insurance Bookings Section', () => {
    beforeEach(() => setupMocks())

    it('should display section title', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Insurance Policies')
    })
  })

  describe('Support Tickets Section', () => {
    beforeEach(() => setupMocks())

    it('should display section title', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('My Support Tickets')
    })

    it('should display ticket cards', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.ticket-card').length).toBe(2)
    })

    it('should display ticket subject', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Issue with booking')
      expect(wrapper.text()).toContain('Refund request')
    })

    it('should display ticket status', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-open').exists()).toBe(true)
      expect(wrapper.find('.status-in_progress').exists()).toBe(true)
    })

    it('should display service source', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('ACCOMMODATION')
      expect(wrapper.text()).toContain('FLIGHT')
    })

    it('should display unread indicator', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.unread-indicator').exists()).toBe(true)
      expect(wrapper.text()).toContain('2 new messages')
    })

    it('should show empty state when no tickets', async () => {
      setupMocks({ tickets: [] })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('No support tickets created yet')
    })

    it('should navigate to ticket detail on click', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent()
      
      await wrapper.find('.ticket-card').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith('/support/tickets/ticket-1')
    })
  })

  describe('Create Ticket Modal', () => {
    beforeEach(() => setupMocks())

    it('should not show modal initially', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.create-ticket-modal').exists()).toBe(false)
    })

    it('should open modal when create-ticket event emitted from BookingCard', async () => {
      const wrapper = await mountComponent()
      
      await wrapper.find('.booking-card').trigger('click')
      
      expect(wrapper.find('.create-ticket-modal').exists()).toBe(true)
    })

    it('should close modal when close event emitted', async () => {
      const wrapper = await mountComponent()
      
      // Open modal
      await wrapper.find('.booking-card').trigger('click')
      expect(wrapper.find('.create-ticket-modal').exists()).toBe(true)
      
      // Close modal
      const modal = wrapper.findComponent({ name: 'CreateTicketModal' })
      await modal.vm.$emit('close')
      
      expect(wrapper.find('.create-ticket-modal').exists()).toBe(false)
    })

    it('should refresh dashboard when ticket created', async () => {
      const wrapper = await mountComponent()
      
      // Open modal
      await wrapper.find('.booking-card').trigger('click')
      
      vi.clearAllMocks()
      setupMocks()
      
      // Emit ticket-created
      const modal = wrapper.findComponent({ name: 'CreateTicketModal' })
      await modal.vm.$emit('ticket-created')
      await flushPromises()
      
      expect(supportTicketService.getAvailableBookings).toHaveBeenCalled()
    })
  })

  describe('Date Formatting', () => {
    beforeEach(() => setupMocks())

    it('should format date correctly', async () => {
      const wrapper = await mountComponent()
      
      // Should contain formatted date
      expect(wrapper.text()).toContain('Jan')
      expect(wrapper.text()).toContain('2024')
    })
  })

  describe('Parallel Data Fetching', () => {
    it('should fetch all services in parallel', async () => {
      setupMocks()
      
      await mountComponent()
      
      // All services should be called
      expect(supportTicketService.getAvailableBookings).toHaveBeenCalledWith('ACCOMMODATION', expect.any(String))
      expect(supportTicketService.getAvailableBookings).toHaveBeenCalledWith('FLIGHT', expect.any(String))
      expect(supportTicketService.getAvailableBookings).toHaveBeenCalledWith('RENTAL', expect.any(String))
      expect(supportTicketService.getAvailableBookings).toHaveBeenCalledWith('TOUR', expect.any(String))
      expect(supportTicketService.getAvailableBookings).toHaveBeenCalledWith('INSURANCE', expect.any(String))
      expect(supportTicketService.getAllTickets).toHaveBeenCalled()
    })

    it('should handle partial failures gracefully', async () => {
      vi.mocked(supportTicketService.getAvailableBookings).mockImplementation((serviceType) => {
        if (serviceType === 'FLIGHT') {
          return Promise.reject(new Error('Failed'))
        }
        return Promise.resolve({ data: [] })
      })
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: [] })
      
      const wrapper = await mountComponent()
      
      // Should still render without crashing
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.text()).toContain('Accommodation Bookings')
      expect(wrapper.text()).toContain('Flight Bookings')
    })
  })

  describe('Section Loading States', () => {
    it('should show loading per section', async () => {
      let resolveAccommodation: Function
      vi.mocked(supportTicketService.getAvailableBookings).mockImplementation((serviceType) => {
        if (serviceType === 'ACCOMMODATION') {
          return new Promise(resolve => { resolveAccommodation = resolve })
        }
        return Promise.resolve({ data: [] })
      })
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: [] })
      
      router.push('/support')
      await router.isReady()
      
      const wrapper = mount(SupportDashboardView, {
        global: { plugins: [router] }
      })
      
      // Check for loading indicator presence in HTML
      expect(wrapper.html().toLowerCase()).toContain('loading')
    })
  })

  describe('Booking Grid Layout', () => {
    beforeEach(() => setupMocks())

    it('should render bookings in grid', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.booking-grid').length).toBeGreaterThan(0)
    })
  })

  describe('Ticket Grid Layout', () => {
    beforeEach(() => setupMocks())

    it('should render tickets in grid', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.tickets-grid').exists()).toBe(true)
    })
  })

  describe('Ticket Status Styling', () => {
    beforeEach(() => setupMocks())

    it('should apply correct class for OPEN status', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-open').exists()).toBe(true)
    })

    it('should apply correct class for IN_PROGRESS status', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-in_progress').exists()).toBe(true)
    })

    it('should apply correct class for CLOSED status', async () => {
      setupMocks({
        tickets: [{ ...mockSupportTickets[0], status: 'CLOSED' }]
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-closed').exists()).toBe(true)
    })
  })
})
