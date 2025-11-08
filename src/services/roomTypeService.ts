import api from './api'
import type { Property } from './propertyService'

export interface RoomType {
  roomTypeId: string
  name: string // Backend uses 'name', not 'roomTypeName'
  floor: number
  capacity: number
  description?: string
  facility: string
  price: number
  property?: Property
  rooms?: any[] // For getById response that includes rooms
}

export interface RoomTypeCreateDTO {
  name: string
  floor: number
  capacity: number
  description?: string
  facility: string
  price: number
  propertyId: string
}

export const roomTypeService = {
  // Get all room types
  async getAll() {
    const response = await api.get<{ success: boolean; message: string; data: RoomType[] }>(
      '/room-types'
    )
    return response.data
  },

  // Get room type by ID with rooms
  async getById(roomTypeId: string) {
    const response = await api.get<{ success: boolean; message: string; data: any }>(
      `/room-types/${roomTypeId}`
    )
    return response.data
  },

  // Get room types by property ID
  async getByPropertyId(propertyId: string) {
    const response = await api.get<{ success: boolean; message: string; data: RoomType[] }>(
      `/property/${propertyId}/room-types`
    )
    return response.data
  },

  // Create room type
  async create(roomType: RoomTypeCreateDTO) {
    const response = await api.post<{ success: boolean; message: string; data: RoomType }>(
      '/property/updateroom',
      roomType
    )
    return response.data
  },
}
