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

import { propertyService } from '@/services/propertyService'
import api from '@/services/api'

describe('Property Service', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('getAll', () => {
    it('should fetch all properties', async () => {
      const mockProperties = [
        { propertyId: 'p1', propertyName: 'Hotel A' },
        { propertyId: 'p2', propertyName: 'Hotel B' }
      ]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockProperties }
      })

      const result = await propertyService.getAll()

      expect(api.get).toHaveBeenCalledWith('/property', { params: undefined })
      expect(result.data).toEqual(mockProperties)
    })

    it('should fetch properties with filters', async () => {
      const mockProperties = [{ propertyId: 'p1' }]
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, message: 'OK', data: mockProperties }
      })

      const result = await propertyService.getAll({ name: 'Hotel', type: 1, province: 31 })

      expect(api.get).toHaveBeenCalledWith('/property', { params: { name: 'Hotel', type: 1, province: 31 } })
    })
  })

  describe('getById', () => {
    it('should fetch property by ID', async () => {
      const mockProperty = { propertyId: 'p1', propertyName: 'Hotel A' }
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, property: mockProperty, roomTypes: [], rooms: [] }
      })

      const result = await propertyService.getById('p1')

      expect(api.get).toHaveBeenCalledWith('/property/p1')
      expect(result.property).toEqual(mockProperty)
    })
  })

  describe('getByIdWithFilter', () => {
    it('should fetch property with date filter', async () => {
      const mockProperty = { propertyId: 'p1' }
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, property: mockProperty, roomTypes: [], rooms: [] }
      })

      const result = await propertyService.getByIdWithFilter('p1', '2024-01-01', '2024-01-03')

      expect(api.get).toHaveBeenCalledWith('/property/p1', {
        params: { checkIn: '2024-01-01', checkOut: '2024-01-03' }
      })
    })
  })

  describe('create', () => {
    it('should create property', async () => {
      const propertyRequest = {
        propertyName: 'New Hotel',
        type: 1,
        address: '123 Street',
        province: 31,
        ownerName: 'Owner',
        ownerId: 'owner-1',
        roomTypes: []
      }
      vi.mocked(api.post).mockResolvedValue({
        data: { success: true, message: 'Created', data: { propertyId: 'p1' } }
      })

      const result = await propertyService.create(propertyRequest)

      expect(api.post).toHaveBeenCalledWith('/property/create', propertyRequest)
      expect(result.success).toBe(true)
    })
  })

  describe('getForUpdate', () => {
    it('should fetch property for update', async () => {
      const mockData = { property: { propertyId: 'p1' }, roomTypes: [] }
      vi.mocked(api.get).mockResolvedValue({
        data: { success: true, ...mockData }
      })

      const result = await propertyService.getForUpdate('p1')

      expect(api.get).toHaveBeenCalledWith('/property/update/p1')
    })
  })

  describe('update', () => {
    it('should update property', async () => {
      const propertyRequest = {
        propertyId: 'p1',
        propertyName: 'Updated Hotel',
        address: '456 Street'
      }
      vi.mocked(api.put).mockResolvedValue({
        data: { success: true, message: 'Updated', data: { propertyId: 'p1' } }
      })

      const result = await propertyService.update(propertyRequest)

      expect(api.put).toHaveBeenCalledWith('/property/update', propertyRequest)
      expect(result.success).toBe(true)
    })
  })

  describe('delete', () => {
    it('should delete property', async () => {
      vi.mocked(api.delete).mockResolvedValue({
        data: { success: true, message: 'Deleted' }
      })

      const result = await propertyService.delete('p1')

      expect(api.delete).toHaveBeenCalledWith('/property/delete/p1')
      expect(result.success).toBe(true)
    })
  })
})
