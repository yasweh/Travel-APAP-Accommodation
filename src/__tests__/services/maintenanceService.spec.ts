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

import { maintenanceService } from '@/services/maintenanceService'
import api from '@/services/api'

describe('Maintenance Service', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('getAll', () => {
    it('should fetch all maintenance schedules', async () => {
      const mockMaintenance = [
        { maintenanceId: 1, roomId: 'r1', roomName: 'Room 101' }
      ]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockMaintenance }
      })

      const result = await maintenanceService.getAll()

      expect(api.get).toHaveBeenCalledWith('/property/maintenance')
      expect(result.data).toEqual(mockMaintenance)
    })
  })

  describe('getByRoomTypeId', () => {
    it('should fetch maintenance by room type ID', async () => {
      const mockMaintenance = [{ maintenanceId: 1 }]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockMaintenance }
      })

      const result = await maintenanceService.getByRoomTypeId('rt1')

      expect(api.get).toHaveBeenCalledWith('/property/maintenance/room-type/rt1')
      expect(result.data).toEqual(mockMaintenance)
    })
  })

  describe('getByRoomId', () => {
    it('should fetch maintenance by room ID', async () => {
      const mockMaintenance = [{ maintenanceId: 1 }]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockMaintenance }
      })

      const result = await maintenanceService.getByRoomId('r1')

      expect(api.get).toHaveBeenCalledWith('/property/maintenance/room/r1')
      expect(result.data).toEqual(mockMaintenance)
    })
  })

  describe('create', () => {
    it('should create maintenance schedule', async () => {
      const maintenanceRequest = {
        roomId: 'r1',
        startDate: '2024-01-10',
        startTime: '08:00',
        endDate: '2024-01-10',
        endTime: '12:00'
      }
      vi.mocked(api.post).mockResolvedValue({
        data: { success: true, message: 'Created', data: { maintenanceId: 1 } }
      })

      const result = await maintenanceService.create(maintenanceRequest)

      expect(api.post).toHaveBeenCalledWith('/property/maintenance/add', maintenanceRequest)
      expect(result.success).toBe(true)
    })
  })
})
