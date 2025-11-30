import api from './api'

/**
 * Types/Interfaces for Support Ticket
 */
export interface CreateTicketRequest {
  userId: string
  subject: string
  serviceSource: string
  externalBookingId: string
  initialMessage: string
}

export interface TicketResponse {
  id: string
  userId: string
  subject: string
  status: string
  serviceSource: string
  externalBookingId: string
  propertyId?: string
  propertyName?: string
  customerName?: string
  createdAt: string
  updatedAt: string
  unreadMessagesCount: number
}

export interface TicketDetailResponse {
  id: string
  userId: string
  subject: string
  status: string
  serviceSource: string
  externalBookingId: string
  initialMessage: string
  createdAt: string
  updatedAt: string
  messages: MessageResponse[]
  progressEntries: ProgressResponse[]
  externalBookingData: any
  externalBookingDataAvailable: boolean
}

export interface MessageResponse {
  id: string
  ticketId: string
  senderType: 'CUSTOMER' | 'ADMIN'
  senderId: string
  message: string
  sentAt: string
  readByRecipient: boolean
}

export interface ProgressResponse {
  id: string
  ticketId: string
  actionType: string
  description: string
  performedBy: string
  performedAt: string
}

export interface AddMessageRequest {
  senderId: string
  senderType: 'CUSTOMER' | 'ADMIN'
  message: string
}

export interface AddProgressRequest {
  performedBy: string
  description: string
}

export interface UpdateStatusRequest {
  status: 'OPEN' | 'IN_PROGRESS' | 'CLOSED'
  updatedBy: string
  reason?: string
}

export interface DashboardResponse {
  accommodationBookings: any[]
  flightBookings: any[]
  rentalBookings: any[]
  tourBookings: any[]
  insuranceBookings: any[]
  supportTickets: TicketResponse[]
}

/**
 * Support Ticket Service API calls
 */
export const supportTicketService = {
  /**
   * Get all tickets with optional filters
   * If userId is undefined, backend returns all tickets (for Superadmin)
   */
  getAllTickets(userId?: string, status?: string, serviceSource?: string) {
    const params: { userId?: string; status?: string; serviceSource?: string } = {}
    if (userId) params.userId = userId
    if (status) params.status = status
    if (serviceSource) params.serviceSource = serviceSource
    
    return api.get<TicketResponse[]>('/support-tickets', { params })
  },

  /**
   * Get detailed ticket info
   */
  getTicketDetail(ticketId: string, userId: string) {
    return api.get<TicketDetailResponse>(`/support-tickets/${ticketId}`, {
      params: { userId },
    })
  },

  /**
   * Create a new support ticket
   */
  createTicket(request: CreateTicketRequest) {
    return api.post<TicketResponse>('/support-tickets', request)
  },

  /**
   * Update ticket status
   */
  updateTicketStatus(ticketId: string, request: UpdateStatusRequest) {
    return api.patch<TicketResponse>(`/support-tickets/${ticketId}/status`, request)
  },

  /**
   * Delete a ticket (soft delete)
   */
  deleteTicket(ticketId: string, userId: string) {
    return api.delete(`/support-tickets/${ticketId}`, {
      params: { userId },
    })
  },

  /**
   * Add progress entry to ticket
   */
  addProgress(ticketId: string, request: AddProgressRequest) {
    return api.post<ProgressResponse>(`/support-tickets/${ticketId}/progress`, request)
  },

  /**
   * Delete progress entry
   */
  deleteProgress(ticketId: string, progressId: string) {
    return api.delete(`/support-tickets/${ticketId}/progress/${progressId}`)
  },

  /**
   * Add message to ticket
   */
  addMessage(ticketId: string, request: AddMessageRequest) {
    return api.post<MessageResponse>(`/support-tickets/${ticketId}/messages`, request)
  },

  /**
   * Get all messages for a ticket
   */
  getTicketMessages(ticketId: string) {
    return api.get<MessageResponse[]>(`/support-tickets/${ticketId}/messages`)
  },

  /**
   * Mark messages as read
   */
  markMessagesAsRead(ticketId: string, userId: string) {
    return api.put(`/support-tickets/${ticketId}/messages/mark-read`, null, {
      params: { userId },
    })
  },

  /**
   * Get available bookings from external services
   * If userId is empty/null, backend will return all bookings (for Superadmin)
   */
  getAvailableBookings(serviceSource: string, userId?: string) {
    const params: { serviceSource: string; userId?: string } = { serviceSource }
    if (userId) {
      params.userId = userId
    }
    return api.get<any[]>('/support-tickets/bookings', { params })
  },

  /**
   * Get dashboard data: all bookings from 5 services + user's tickets
   * If userId is empty/null, backend will return all data (for Superadmin)
   */
  getDashboard(userId?: string) {
    const params: { userId?: string } = {}
    if (userId) {
      params.userId = userId
    }
    return api.get<DashboardResponse>('/support-tickets/dashboard', { params })
  },
}

export default supportTicketService
