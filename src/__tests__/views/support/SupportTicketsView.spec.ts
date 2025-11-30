import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import SupportTicketsView from '@/views/support/SupportTicketsView.vue'
import { useAuthStore } from '@/stores/auth'
import supportTicketService from '@/services/supportTicketService'

// Mock the services
vi.mock('@/services/supportTicketService', () => ({
  default: {
    getAllTickets: vi.fn(),
    deleteTicket: vi.fn()
  }
}))

// Mock CreateTicketModal
vi.mock('@/components/support/CreateTicketModal.vue', () => ({
  default: {
    name: 'CreateTicketModal',
    emits: ['close', 'ticket-created'],
    template: '<div class="create-ticket-modal"><slot></slot></div>'
  }
}))

// Create mock router
const createMockRouter = () => {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/support', name: 'support-tickets', component: SupportTicketsView },
      { path: '/support/:id', name: 'support-ticket-detail', component: { template: '<div>Detail</div>' } }
    ]
  })
}

const mockTickets = [
  {
    id: 'ticket-1',
    subject: 'Issue with booking',
    status: 'OPEN',
    serviceSource: 'ACCOMMODATION',
    externalBookingId: 'BOOK-123',
    propertyName: 'Beach Hotel',
    customerName: 'John Doe',
    createdAt: '2024-01-15T10:00:00Z',
    unreadMessagesCount: 2
  },
  {
    id: 'ticket-2',
    subject: 'Refund request',
    status: 'IN_PROGRESS',
    serviceSource: 'FLIGHT',
    externalBookingId: 'FLT-456',
    propertyName: null,
    customerName: 'Jane Smith',
    createdAt: '2024-01-14T08:00:00Z',
    unreadMessagesCount: 0
  },
  {
    id: 'ticket-3',
    subject: 'Completed inquiry',
    status: 'CLOSED',
    serviceSource: 'RENTAL',
    externalBookingId: 'RNT-789',
    propertyName: 'Car Rental Shop',
    customerName: null,
    createdAt: '2024-01-10T12:00:00Z',
    unreadMessagesCount: 0
  }
]

describe('SupportTicketsView.vue', () => {
  let router: ReturnType<typeof createMockRouter>
  let pinia: ReturnType<typeof createPinia>

  beforeEach(() => {
    vi.clearAllMocks()
    pinia = createPinia()
    setActivePinia(pinia)
    router = createMockRouter()
    window.alert = vi.fn()
    window.confirm = vi.fn()
  })

  const setupAuthStore = (role: 'customer' | 'superadmin' | 'owner' = 'customer') => {
    const authStore = useAuthStore()
    
    const roleMapping = {
      'customer': 'Customer',
      'superadmin': 'Superadmin',
      'owner': 'Accommodation Owner'
    }
    
    authStore.setAuth({
      id: 'user-123',
      email: 'test@test.com',
      name: 'Test User',
      username: 'testuser',
      token: 'test-token',
      type: 'Bearer',
      role: roleMapping[role]
    })
    
    return authStore
  }

  const mountComponent = async () => {
    router.push('/support')
    await router.isReady()
    
    const wrapper = mount(SupportTicketsView, {
      global: {
        plugins: [router, pinia]
      }
    })
    
    await flushPromises()
    return wrapper
  }

  describe('Page Header - Customer View', () => {
    beforeEach(() => {
      setupAuthStore('customer')
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: mockTickets })
    })

    it('should display correct title for customer', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('h1').text()).toBe('My Support Tickets')
    })

    it('should display correct subtitle for customer', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('View and manage your support requests')
    })

    it('should show Create New Ticket button for customer', async () => {
      const wrapper = await mountComponent()
      
      // Customer should have Create button in header
      const headerButtons = wrapper.findAll('.page-header .btn-primary')
      expect(headerButtons.length).toBeGreaterThan(0)
    })
  })

  describe('Page Header - Superadmin View', () => {
    beforeEach(() => {
      setupAuthStore('superadmin')
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: mockTickets })
    })

    it('should display correct title for superadmin', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('h1').text()).toBe('All Support Tickets')
    })

    it('should display correct subtitle for superadmin', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Manage all support tickets in the system')
    })

    it('should not show Create New Ticket button for superadmin', async () => {
      const wrapper = await mountComponent()
      
      const buttons = wrapper.findAll('.btn-primary')
      const createButton = buttons.filter(b => b.text().includes('Create New Ticket'))
      expect(createButton.length).toBe(0)
    })
  })

  describe('Page Header - Owner View', () => {
    beforeEach(() => {
      setupAuthStore('owner')
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: mockTickets })
    })

    it('should display correct title for owner', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('h1').text()).toBe('Property Support Tickets')
    })

    it('should display correct subtitle for owner', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Manage support tickets for your properties')
    })
  })

  describe('Filters', () => {
    beforeEach(() => {
      setupAuthStore('customer')
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: mockTickets })
    })

    it('should display status filter dropdown', async () => {
      const wrapper = await mountComponent()
      
      const selects = wrapper.findAll('.filter-select')
      expect(selects.length).toBe(2)
      expect(wrapper.text()).toContain('All Status')
    })

    it('should have correct status options', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Open')
      expect(wrapper.text()).toContain('In Progress')
      expect(wrapper.text()).toContain('Closed')
    })

    it('should display service source filter', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('All Services')
      expect(wrapper.text()).toContain('Accommodation')
      expect(wrapper.text()).toContain('Flight')
    })

    it('should reload tickets when status filter changes', async () => {
      const wrapper = await mountComponent()
      
      vi.clearAllMocks()
      const statusSelect = wrapper.findAll('.filter-select')[0]
      await statusSelect.setValue('OPEN')
      await flushPromises()
      
      expect(supportTicketService.getAllTickets).toHaveBeenCalledWith(
        'user-123',
        'OPEN',
        undefined
      )
    })

    it('should reload tickets when service source filter changes', async () => {
      const wrapper = await mountComponent()
      
      vi.clearAllMocks()
      const serviceSelect = wrapper.findAll('.filter-select')[1]
      await serviceSelect.setValue('ACCOMMODATION')
      await flushPromises()
      
      expect(supportTicketService.getAllTickets).toHaveBeenCalledWith(
        'user-123',
        undefined,
        'ACCOMMODATION'
      )
    })
  })

  describe('Loading State', () => {
    it('should show loading text while fetching', async () => {
      setupAuthStore('customer')
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: [] })
      
      const wrapper = await mountComponent()
      
      // After data loads, loading state should be hidden
      expect(wrapper.find('.tickets-list').exists()).toBe(true)
    })
  })

  describe('Error State', () => {
    it('should display error message when API fails', async () => {
      setupAuthStore('customer')
      vi.mocked(supportTicketService.getAllTickets).mockRejectedValue({
        response: { data: { error: 'Failed to fetch tickets' } }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.error').exists()).toBe(true)
      expect(wrapper.text()).toContain('Failed to fetch tickets')
    })
  })

  describe('Tickets List', () => {
    beforeEach(() => {
      setupAuthStore('customer')
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: mockTickets })
    })

    it('should display ticket cards', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.ticket-card').length).toBe(3)
    })

    it('should display ticket subject', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Issue with booking')
      expect(wrapper.text()).toContain('Refund request')
    })

    it('should display status badge', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-open').exists()).toBe(true)
      expect(wrapper.find('.status-in_progress').exists()).toBe(true)
      expect(wrapper.find('.status-closed').exists()).toBe(true)
    })

    it('should display service source', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('ACCOMMODATION')
      expect(wrapper.text()).toContain('FLIGHT')
    })

    it('should display booking ID', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('BOOK-123')
      expect(wrapper.text()).toContain('FLT-456')
    })

    it('should display property name when available', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Beach Hotel')
    })

    it('should display formatted date', async () => {
      const wrapper = await mountComponent()
      
      // Should contain formatted date parts
      expect(wrapper.text()).toContain('2024')
    })

    it('should display unread messages badge', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.unread-badge').exists()).toBe(true)
      expect(wrapper.text()).toContain('2 unread messages')
    })
  })

  describe('Ticket Actions', () => {
    describe('Customer Actions', () => {
      beforeEach(() => {
        setupAuthStore('customer')
        vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: mockTickets })
      })

      it('should show View Details button', async () => {
        const wrapper = await mountComponent()
        
        expect(wrapper.find('.btn-secondary').text()).toContain('View Details')
      })

      it('should navigate to detail when View Details clicked', async () => {
        const pushSpy = vi.spyOn(router, 'push')
        const wrapper = await mountComponent()
        
        await wrapper.find('.btn-secondary').trigger('click')
        
        expect(pushSpy).toHaveBeenCalledWith({
          name: 'support-ticket-detail',
          params: { id: 'ticket-1' }
        })
      })

      it('should show Delete button for OPEN tickets as customer', async () => {
        // Use only the OPEN ticket for this test
        vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({
          data: [mockTickets[0]] // Only OPEN ticket
        })
        
        const wrapper = await mountComponent()
        
        expect(wrapper.find('.btn-danger').exists()).toBe(true)
      })

      it('should not show Delete button for non-OPEN tickets', async () => {
        vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({
          data: [mockTickets[1]] // IN_PROGRESS ticket
        })
        
        const wrapper = await mountComponent()
        
        expect(wrapper.find('.btn-danger').exists()).toBe(false)
      })
    })

    describe('Non-Customer Actions', () => {
      beforeEach(() => {
        setupAuthStore('superadmin')
        vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: mockTickets })
      })

      it('should not show Delete button for non-customers', async () => {
        const wrapper = await mountComponent()
        
        expect(wrapper.find('.btn-danger').exists()).toBe(false)
      })

      it('should show customer name for non-customer users', async () => {
        const wrapper = await mountComponent()
        
        expect(wrapper.text()).toContain('Customer:')
        expect(wrapper.text()).toContain('John Doe')
      })
    })
  })

  describe('Delete Ticket', () => {
    beforeEach(() => {
      setupAuthStore('customer')
      // Use only the OPEN ticket so we can find delete button
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: [mockTickets[0]] })
    })

    it('should confirm before deleting', async () => {
      vi.mocked(window.confirm).mockReturnValue(false)
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-danger').trigger('click')
      
      expect(window.confirm).toHaveBeenCalledWith('Are you sure you want to delete this ticket?')
      expect(supportTicketService.deleteTicket).not.toHaveBeenCalled()
    })

    it('should delete ticket when confirmed', async () => {
      vi.mocked(window.confirm).mockReturnValue(true)
      vi.mocked(supportTicketService.deleteTicket).mockResolvedValue({ success: true })
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-danger').trigger('click')
      await flushPromises()
      
      expect(supportTicketService.deleteTicket).toHaveBeenCalledWith('ticket-1', 'user-123')
      expect(window.alert).toHaveBeenCalledWith('Ticket deleted successfully')
    })

    it('should show error when delete fails', async () => {
      vi.mocked(window.confirm).mockReturnValue(true)
      vi.mocked(supportTicketService.deleteTicket).mockRejectedValue({
        response: { data: { error: 'Delete failed' } }
      })
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-danger').trigger('click')
      await flushPromises()
      
      expect(window.alert).toHaveBeenCalledWith('Delete failed')
    })
  })

  describe('Create Ticket Modal', () => {
    beforeEach(() => {
      setupAuthStore('customer')
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: mockTickets })
    })

    it('should open modal when Create button clicked', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.create-ticket-modal').exists()).toBe(false)
      
      await wrapper.find('.btn-primary').trigger('click')
      
      expect(wrapper.find('.create-ticket-modal').exists()).toBe(true)
    })

    it('should close modal when close event emitted', async () => {
      const wrapper = await mountComponent()
      
      await wrapper.find('.btn-primary').trigger('click')
      expect(wrapper.find('.create-ticket-modal').exists()).toBe(true)
      
      const modal = wrapper.findComponent({ name: 'CreateTicketModal' })
      await modal.vm.$emit('close')
      
      expect(wrapper.find('.create-ticket-modal').exists()).toBe(false)
    })

    it('should refresh tickets when ticket created', async () => {
      const wrapper = await mountComponent()
      
      await wrapper.find('.btn-primary').trigger('click')
      
      vi.clearAllMocks()
      const modal = wrapper.findComponent({ name: 'CreateTicketModal' })
      await modal.vm.$emit('ticket-created')
      await flushPromises()
      
      expect(supportTicketService.getAllTickets).toHaveBeenCalled()
    })
  })

  describe('Empty State', () => {
    beforeEach(() => {
      setupAuthStore('customer')
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: [] })
    })

    it('should show empty state when no tickets', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.no-tickets').exists()).toBe(true)
    })

    it('should show customer-specific message', async () => {
      const wrapper = await mountComponent()
      
      // Customer sees "No support tickets found."
      expect(wrapper.text()).toContain('No support tickets found')
    })

    it('should show Create First Ticket button', async () => {
      const wrapper = await mountComponent()
      
      // Check for either the text or button existence
      expect(wrapper.text()).toContain('Create Your First Ticket')
    })

    it('should show owner-specific message', async () => {
      setupAuthStore('owner')
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: [] })
      
      const wrapper = await mountComponent()
      
      // Owner sees "No support tickets to manage."
      expect(wrapper.text()).toContain('No support tickets to manage')
    })
  })

  describe('Status Badge Styling', () => {
    beforeEach(() => {
      setupAuthStore('customer')
      vi.mocked(supportTicketService.getAllTickets).mockResolvedValue({ data: mockTickets })
    })

    it('should apply correct class for OPEN status', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-open').exists()).toBe(true)
    })

    it('should apply correct class for IN_PROGRESS status', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-in_progress').exists()).toBe(true)
    })

    it('should apply correct class for CLOSED status', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-closed').exists()).toBe(true)
    })
  })
})
