import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock axios
vi.mock('axios', () => {
  const mockAxiosInstance = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    }
  }
  return {
    default: {
      create: vi.fn(() => mockAxiosInstance)
    }
  }
})

import { supportTicketService } from '@/services/supportTicketService'
import api from '@/services/api'

describe('Support Ticket Service', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('getAllTickets', () => {
    it('should fetch all tickets', async () => {
      const mockTickets = [
        { id: 't1', subject: 'Issue 1' },
        { id: 't2', subject: 'Issue 2' }
      ]
      vi.mocked(api.get).mockResolvedValue({ data: mockTickets })

      const result = await supportTicketService.getAllTickets('u1')

      expect(api.get).toHaveBeenCalledWith('/support-tickets', {
        params: { userId: 'u1', status: undefined, serviceSource: undefined }
      })
    })

    it('should fetch tickets with filters', async () => {
      const mockTickets = [{ id: 't1' }]
      vi.mocked(api.get).mockResolvedValue({ data: mockTickets })

      const result = await supportTicketService.getAllTickets('u1', 'OPEN', 'ACCOMMODATION')

      expect(api.get).toHaveBeenCalledWith('/support-tickets', {
        params: { userId: 'u1', status: 'OPEN', serviceSource: 'ACCOMMODATION' }
      })
    })
  })

  describe('getTicketDetail', () => {
    it('should fetch ticket detail', async () => {
      const mockTicket = { id: 't1', subject: 'Issue', messages: [], progressEntries: [] }
      vi.mocked(api.get).mockResolvedValue({ data: mockTicket })

      const result = await supportTicketService.getTicketDetail('t1', 'u1')

      expect(api.get).toHaveBeenCalledWith('/support-tickets/t1', { params: { userId: 'u1' } })
    })
  })

  describe('createTicket', () => {
    it('should create ticket', async () => {
      const ticketRequest = {
        userId: 'u1',
        subject: 'New Issue',
        serviceSource: 'ACCOMMODATION',
        externalBookingId: 'b1',
        initialMessage: 'I have a problem'
      }
      vi.mocked(api.post).mockResolvedValue({ data: { id: 't1', ...ticketRequest } })

      const result = await supportTicketService.createTicket(ticketRequest)

      expect(api.post).toHaveBeenCalledWith('/support-tickets', ticketRequest)
    })
  })

  describe('updateTicketStatus', () => {
    it('should update ticket status', async () => {
      const statusRequest = {
        status: 'IN_PROGRESS' as const,
        updatedBy: 'admin1',
        reason: 'Processing'
      }
      vi.mocked(api.patch).mockResolvedValue({ data: { id: 't1', status: 'IN_PROGRESS' } })

      const result = await supportTicketService.updateTicketStatus('t1', statusRequest)

      expect(api.patch).toHaveBeenCalledWith('/support-tickets/t1/status', statusRequest)
    })
  })

  describe('deleteTicket', () => {
    it('should delete ticket', async () => {
      vi.mocked(api.delete).mockResolvedValue({ data: { success: true } })

      const result = await supportTicketService.deleteTicket('t1', 'u1')

      expect(api.delete).toHaveBeenCalledWith('/support-tickets/t1', { params: { userId: 'u1' } })
    })
  })

  describe('addProgress', () => {
    it('should add progress entry', async () => {
      const progressRequest = {
        performedBy: 'admin1',
        description: 'Investigating issue'
      }
      vi.mocked(api.post).mockResolvedValue({ data: { id: 'p1', ...progressRequest } })

      const result = await supportTicketService.addProgress('t1', progressRequest)

      expect(api.post).toHaveBeenCalledWith('/support-tickets/t1/progress', progressRequest)
    })
  })

  describe('deleteProgress', () => {
    it('should delete progress entry', async () => {
      vi.mocked(api.delete).mockResolvedValue({ data: { success: true } })

      const result = await supportTicketService.deleteProgress('t1', 'p1')

      expect(api.delete).toHaveBeenCalledWith('/support-tickets/t1/progress/p1')
    })
  })

  describe('addMessage', () => {
    it('should add message', async () => {
      const messageRequest = {
        senderId: 'u1',
        senderType: 'CUSTOMER' as const,
        message: 'Thank you for your help'
      }
      vi.mocked(api.post).mockResolvedValue({ data: { id: 'm1', ...messageRequest } })

      const result = await supportTicketService.addMessage('t1', messageRequest)

      expect(api.post).toHaveBeenCalledWith('/support-tickets/t1/messages', messageRequest)
    })
  })

  describe('getTicketMessages', () => {
    it('should fetch ticket messages', async () => {
      const mockMessages = [{ id: 'm1', message: 'Hello' }]
      vi.mocked(api.get).mockResolvedValue({ data: mockMessages })

      const result = await supportTicketService.getTicketMessages('t1')

      expect(api.get).toHaveBeenCalledWith('/support-tickets/t1/messages')
    })
  })

  describe('markMessagesAsRead', () => {
    it('should mark messages as read', async () => {
      vi.mocked(api.put).mockResolvedValue({ data: { success: true } })

      const result = await supportTicketService.markMessagesAsRead('t1', 'u1')

      expect(api.put).toHaveBeenCalledWith('/support-tickets/t1/messages/mark-read', null, {
        params: { userId: 'u1' }
      })
    })
  })

  describe('getAvailableBookings', () => {
    it('should fetch available bookings', async () => {
      const mockBookings = [{ id: 'b1' }]
      vi.mocked(api.get).mockResolvedValue({ data: mockBookings })

      const result = await supportTicketService.getAvailableBookings('ACCOMMODATION', 'u1')

      expect(api.get).toHaveBeenCalledWith('/support-tickets/bookings', {
        params: { serviceSource: 'ACCOMMODATION', userId: 'u1' }
      })
    })
  })

  describe('getDashboard', () => {
    it('should fetch dashboard data', async () => {
      const mockDashboard = {
        accommodationBookings: [],
        flightBookings: [],
        rentalBookings: [],
        tourBookings: [],
        insuranceBookings: [],
        supportTickets: []
      }
      vi.mocked(api.get).mockResolvedValue({ data: mockDashboard })

      const result = await supportTicketService.getDashboard('u1')

      expect(api.get).toHaveBeenCalledWith('/support-tickets/dashboard', {
        params: { userId: 'u1' }
      })
    })
  })
})
