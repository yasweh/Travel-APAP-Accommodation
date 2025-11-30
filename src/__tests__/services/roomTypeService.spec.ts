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

import { roomTypeService } from '@/services/roomTypeService'
import api from '@/services/api'

describe('Room Type Service', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('getAll', () => {
    it('should fetch all room types', async () => {
      const mockRoomTypes = [
        { roomTypeId: 'rt1', name: 'Deluxe' },
        { roomTypeId: 'rt2', name: 'Standard' }
      ]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockRoomTypes }
      })

      const result = await roomTypeService.getAll()

      expect(api.get).toHaveBeenCalledWith('/room-types')
      expect(result.data).toEqual(mockRoomTypes)
    })
  })

  describe('getById', () => {
    it('should fetch room type by ID', async () => {
      const mockRoomType = { roomTypeId: 'rt1', name: 'Deluxe', rooms: [] }
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockRoomType }
      })

      const result = await roomTypeService.getById('rt1')

      expect(api.get).toHaveBeenCalledWith('/room-types/rt1')
      expect(result.data).toEqual(mockRoomType)
    })
  })

  describe('getByPropertyId', () => {
    it('should fetch room types by property ID', async () => {
      const mockRoomTypes = [{ roomTypeId: 'rt1' }]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockRoomTypes }
      })

      const result = await roomTypeService.getByPropertyId('p1')

      expect(api.get).toHaveBeenCalledWith('/property/p1/room-types')
      expect(result.data).toEqual(mockRoomTypes)
    })
  })

  describe('create', () => {
    it('should create room type', async () => {
      const roomTypeRequest = {
        name: 'Suite',
        floor: 3,
        capacity: 4,
        facility: 'WiFi, AC, TV',
        price: 500000,
        propertyId: 'p1'
      }
      vi.mocked(api.post).mockResolvedValue({
        data: { success: true, message: 'Created', data: { roomTypeId: 'rt1' } }
      })

      const result = await roomTypeService.create(roomTypeRequest)

      expect(api.post).toHaveBeenCalledWith('/property/updateroom', roomTypeRequest)
      expect(result.success).toBe(true)
    })
  })
})
