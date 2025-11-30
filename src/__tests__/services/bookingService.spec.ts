import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import axios from 'axios'

// Mock axios
vi.mock('axios', () => {
  const mockAxiosInstance = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
    interceptors: {
      request: {
        use: vi.fn()
      },
      response: {
        use: vi.fn()
      }
    }
  }
  return {
    default: {
      create: vi.fn(() => mockAxiosInstance)
    }
  }
})

// Import after mocking
import { bookingService } from '@/services/bookingService'
import api from '@/services/api'

describe('Booking Service', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('getAll', () => {
    it('should fetch all bookings', async () => {
      const mockBookings = [
        { bookingId: 'b1', propertyName: 'Hotel A' },
        { bookingId: 'b2', propertyName: 'Hotel B' }
      ]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockBookings }
      })

      const result = await bookingService.getAll()

      expect(api.get).toHaveBeenCalledWith('/bookings')
      expect(result.success).toBe(true)
      expect(result.data).toEqual(mockBookings)
    })
  })

  describe('getById', () => {
    it('should fetch booking by ID', async () => {
      const mockBooking = { bookingId: 'b1', propertyName: 'Hotel A' }
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockBooking }
      })

      const result = await bookingService.getById('b1')

      expect(api.get).toHaveBeenCalledWith('/bookings/b1')
      expect(result.data).toEqual(mockBooking)
    })
  })

  describe('getRoomDetails', () => {
    it('should fetch room details for booking', async () => {
      const mockRoom = { roomId: 'r1', name: 'Room 101' }
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockRoom }
      })

      const result = await bookingService.getRoomDetails('r1')

      expect(api.get).toHaveBeenCalledWith('/bookings/create/r1')
      expect(result.data).toEqual(mockRoom)
    })
  })

  describe('getPropertiesForBooking', () => {
    it('should fetch properties for booking dropdown', async () => {
      const mockProperties = [{ propertyId: 'p1' }]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockProperties }
      })

      const result = await bookingService.getPropertiesForBooking()

      expect(api.get).toHaveBeenCalledWith('/bookings/create')
      expect(result.data).toEqual(mockProperties)
    })
  })

  describe('getRoomTypesByProperty', () => {
    it('should fetch room types by property ID', async () => {
      const mockRoomTypes = [{ roomTypeId: 'rt1' }]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockRoomTypes }
      })

      const result = await bookingService.getRoomTypesByProperty('p1')

      expect(api.get).toHaveBeenCalledWith('/bookings/roomtypes/p1')
      expect(result.data).toEqual(mockRoomTypes)
    })
  })

  describe('getAvailableRooms', () => {
    it('should fetch available rooms without dates', async () => {
      const mockRooms = [{ roomId: 'r1' }]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockRooms }
      })

      const result = await bookingService.getAvailableRooms('p1', 'rt1')

      expect(api.get).toHaveBeenCalledWith('/bookings/rooms/p1/rt1')
      expect(result.data).toEqual(mockRooms)
    })

    it('should fetch available rooms with dates', async () => {
      const mockRooms = [{ roomId: 'r1' }]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockRooms }
      })

      const result = await bookingService.getAvailableRooms('p1', 'rt1', '2024-01-01', '2024-01-03')

      expect(api.get).toHaveBeenCalledWith('/bookings/rooms/p1/rt1?checkInDate=2024-01-01&checkOutDate=2024-01-03')
      expect(result.data).toEqual(mockRooms)
    })
  })

  describe('create', () => {
    it('should create booking', async () => {
      const bookingRequest = {
        roomId: 'r1',
        checkInDate: '2024-01-01',
        checkOutDate: '2024-01-03',
        customerName: 'John',
        customerEmail: 'john@test.com',
        customerPhone: '123456',
        addOnBreakfast: false,
        capacity: 2
      }
      const mockResponse = { bookingId: 'b1', ...bookingRequest }
      vi.mocked(api.post).mockResolvedValue({
        data: { success: true, message: 'Created', data: mockResponse }
      })

      const result = await bookingService.create(bookingRequest)

      expect(api.post).toHaveBeenCalledWith('/bookings/create', bookingRequest)
      expect(result.success).toBe(true)
    })
  })

  describe('getForUpdate', () => {
    it('should fetch booking for update', async () => {
      const mockData = { booking: { bookingId: 'b1' }, properties: [] }
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', ...mockData }
      })

      const result = await bookingService.getForUpdate('b1')

      expect(api.get).toHaveBeenCalledWith('/bookings/update/b1')
    })
  })

  describe('update', () => {
    it('should update booking', async () => {
      const bookingRequest = {
        roomId: 'r1',
        checkInDate: '2024-01-01',
        checkOutDate: '2024-01-03',
        customerName: 'John Updated',
        customerEmail: 'john@test.com',
        customerPhone: '123456',
        addOnBreakfast: true,
        capacity: 2
      }
      vi.mocked(api.put).mockResolvedValue({
        data: { success: true, message: 'Updated', data: { bookingId: 'b1' } }
      })

      const result = await bookingService.update('b1', bookingRequest)

      expect(api.put).toHaveBeenCalledWith('/bookings/update/b1', bookingRequest)
      expect(result.success).toBe(true)
    })
  })

  // Payment is now handled via Bill Service, so pay() method was removed
  // See BookingDetail.vue goToBillService() for the new payment flow

  describe('cancel', () => {
    it('should cancel booking', async () => {
      vi.mocked(api.post).mockResolvedValue({
        data: { success: true, message: 'Booking cancelled' }
      })

      const result = await bookingService.cancel('b1')

      expect(api.post).toHaveBeenCalledWith('/bookings/status/cancel', { bookingId: 'b1' })
      expect(result.success).toBe(true)
    })
  })

  describe('getMonthlyStatistics', () => {
    it('should fetch monthly statistics', async () => {
      const mockStats = [
        { propertyId: 'p1', propertyName: 'Hotel A', totalIncome: 1000000, bookingCount: 5 }
      ]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockStats }
      })

      const result = await bookingService.getMonthlyStatistics(1, 2024)

      expect(api.get).toHaveBeenCalledWith('/bookings/chart?month=1&year=2024')
      expect(result.data).toEqual(mockStats)
    })
  })
})
