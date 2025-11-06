import api from './api'

export interface Property {
  propertyId: string
  propertyName: string
  address: string
  city?: string
  type: number
  typeString?: string
  province: number
  description?: string
  ownerName: string
  ownerId: string
  activeStatus?: number
  activeStatusString?: string
  activeProperty?: number
  income: number
  totalRoom?: number
  roomCount?: number
  createdDate?: string
  updatedDate?: string
}

export interface RoomType {
  roomTypeId: string
  name: string
  price: number
  capacity: number
  facility: string
  floor: number
  description?: string
}

export interface Room {
  roomId: string
  name: string
  availabilityStatus: number
  activeRoom: number
  roomType?: any
}

export interface PropertyDetailResponse {
  success: boolean
  message?: string
  property: Property
  roomTypes: RoomType[]
  rooms: Room[]
}

export interface PropertyUpdateResponse {
  success: boolean
  message?: string
  property: Property
  roomTypes: RoomType[]
}

export interface PropertyCreateDTO {
  propertyName: string
  type: number
  address: string
  province: number
  description?: string
  ownerName: string
  ownerId: string
  roomTypes: Array<{
    name: string
    facility: string
    capacity: number
    price: number
    floor: number
    roomCount: number
    description?: string
  }>
}

export interface PropertyUpdateDTO {
  propertyId: string
  propertyName: string
  address: string
  description?: string
  province?: number
  roomTypes?: Array<{
    roomTypeId?: string
    name: string
    facility: string
    capacity: number
    price: number
    floor: number
    description?: string
  }>
}

export const propertyService = {
  // Get all properties
  async getAll() {
    const response = await api.get<{ success: boolean; message: string; data: Property[] }>(
      '/property'
    )
    return response.data
  },

  // Get property by ID
  async getById(id: string) {
    const response = await api.get<PropertyDetailResponse>(
      `/property/${id}`
    )
    return response.data
  },

  // Get property by ID with date filter
  async getByIdWithFilter(id: string, checkIn: string, checkOut: string) {
    const response = await api.get<PropertyDetailResponse>(
      `/property/${id}`,
      {
        params: { checkIn, checkOut }
      }
    )
    return response.data
  },

  // Create property
  async create(property: PropertyCreateDTO) {
    const response = await api.post<{ success: boolean; message: string; data: Property }>(
      '/property/create',
      property
    )
    return response.data
  },

  // Get property for update
  async getForUpdate(id: string) {
    const response = await api.get<PropertyUpdateResponse>(
      `/property/update/${id}`
    )
    return response.data
  },

  // Update property
  async update(property: PropertyUpdateDTO) {
    const response = await api.put<{ success: boolean; message: string; data: Property }>(
      '/property/update',
      property
    )
    return response.data
  },

  // Delete property
  async delete(id: string) {
    const response = await api.delete<{ success: boolean; message: string }>(
      `/property/delete/${id}`
    )
    return response.data
  },
}
