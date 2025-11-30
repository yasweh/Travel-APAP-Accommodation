import api from './api'

export interface BookingResponseDTO {
  bookingId: string
  roomId: string
  roomName: string
  roomTypeId: string
  roomTypeName: string
  propertyId: string
  propertyName: string
  checkInDate: string
  checkOutDate: string
  totalDays: number
  totalPrice: number
  status: number
  statusString: string
  customerId: string
  customerName: string
  customerEmail: string
  customerPhone: string
  addOnBreakfast: boolean
  capacity: number
  createdAt?: string
  updatedAt?: string
}

export interface BookingDetailDTO extends BookingResponseDTO {
  availableActions?: {
    canPay: boolean
    canCancel: boolean
    canUpdate: boolean
  }
}

export interface BookingRequestDTO {
  roomId?: string
  propertyId?: string
  roomTypeId?: string
  checkInDate: string
  checkOutDate: string
  customerId?: string // Optional - backend auto-generates from authenticated user
  customerName: string
  customerEmail: string
  customerPhone: string
  addOnBreakfast: boolean
  capacity: number
}

export interface RoomDTO {
  roomId: string
  name: string
  roomTypeName: string
  roomTypePrice: number
  roomTypeCapacity: number
  roomTypeFacility: string
  floor: number
  availabilityStatus: number
  activeRoom: number
}

export interface PropertyStatisticsDTO {
  propertyId: string
  propertyName: string
  totalIncome: number
  bookingCount: number
}

export const bookingService = {
  // Get all bookings
  async getAll() {
    const response = await api.get<{
      success: boolean
      message: string
      data: BookingResponseDTO[]
    }>('/bookings')
    return response.data
  },

  // Get booking by ID
  async getById(id: string) {
    const response = await api.get<{
      success: boolean
      message: string
      data: BookingDetailDTO
      availableActions?: {
        canPay: boolean
        canCancel: boolean
        canUpdate: boolean
      }
    }>(`/bookings/${id}`)
    return response.data
  },

  // Get room details for prefilled booking form
  async getRoomDetails(roomId: string) {
    const response = await api.get<{ success: boolean; message: string; data: any }>(
      `/bookings/create/${roomId}`
    )
    return response.data
  },

  // Get properties for cascading dropdown (init)
  async getPropertiesForBooking() {
    const response = await api.get<{ success: boolean; message: string; data: any[] }>(
      '/bookings/create'
    )
    return response.data
  },

  // Get room types by property (cascading step 2)
  async getRoomTypesByProperty(propertyId: string) {
    const response = await api.get<{ success: boolean; message: string; data: any[] }>(
      `/bookings/roomtypes/${propertyId}`
    )
    return response.data
  },

  // Get available rooms (cascading step 3)
  async getAvailableRooms(propertyId: string, roomTypeId: string, checkInDate?: string, checkOutDate?: string) {
    let url = `/bookings/rooms/${propertyId}/${roomTypeId}`
    
    // Add date parameters if provided
    if (checkInDate && checkOutDate) {
      url += `?checkInDate=${checkInDate}&checkOutDate=${checkOutDate}`
    }
    
    const response = await api.get<{ success: boolean; message: string; data: RoomDTO[] }>(url)
    return response.data
  },

  // Create booking
  async create(booking: BookingRequestDTO) {
    const response = await api.post<{
      success: boolean
      message: string
      data: BookingResponseDTO
    }>('/bookings/create', booking)
    return response.data
  },

  // Get booking for update
  async getForUpdate(id: string) {
    const response = await api.get<{ 
      success: boolean
      message: string
      booking: BookingResponseDTO
      properties: any[]
    }>(
      `/bookings/update/${id}`
    )
    return response.data
  },

  // Update booking
  async update(id: string, booking: BookingRequestDTO) {
    const response = await api.put<{
      success: boolean
      message: string
      data: BookingResponseDTO
    }>(`/bookings/update/${id}`, booking)
    return response.data
  },

  // Confirm payment
  async pay(bookingId: string) {
    const response = await api.post<{ success: boolean; message: string }>(
      '/bookings/status/pay',
      { bookingId }
    )
    return response.data
  },

  // Cancel booking
  async cancel(bookingId: string) {
    const response = await api.post<{ success: boolean; message: string }>(
      '/bookings/status/cancel',
      { bookingId }
    )
    return response.data
  },

  // Get monthly statistics for Chart.js
  async getMonthlyStatistics(month: number, year: number) {
    const response = await api.get<{
      success: boolean
      message: string
      data: PropertyStatisticsDTO[]
    }>(`/bookings/chart?month=${month}&year=${year}`)
    return response.data
  },
}
