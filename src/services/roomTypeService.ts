import api from './api'
import type { Property } from './propertyService'

export interface RoomType {
  roomTypeId: string
  roomTypeName: string
  floor: number
  capacity: number
  description?: string
  facility: string
  price: number
  property: Property
}

export interface RoomTypeCreateDTO {
  roomTypeName: string
  floor: number
  capacity: number
  description?: string
  facility: string
  price: number
  property: {
    propertyId: string
  }
}

export const roomTypeService = {
  // Get room types by property ID
  async getByPropertyId(propertyId: string) {
    const response = await api.get<{ success: boolean; message: string; data: RoomType[] }>(
      `/property/updateroom/${propertyId}`
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
