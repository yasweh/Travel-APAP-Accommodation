import api from './api'

export interface Maintenance {
  maintenanceId: number
  roomId: string
  roomName: string
  roomTypeId: string
  roomTypeName: string
  propertyId: string
  propertyName: string
  startDate: string
  startTime: string
  endDate: string
  endTime: string
}

export interface MaintenanceCreateDTO {
  roomId: string
  startDate: string
  startTime: string
  endDate: string
  endTime: string
}

export const maintenanceService = {
  // Get all maintenance schedules
  async getAll() {
    const response = await api.get<{ success: boolean; message: string; data: Maintenance[] }>(
      '/property/maintenance'
    )
    return response.data
  },

  // Get maintenance schedules by room type ID
  async getByRoomTypeId(roomTypeId: string) {
    const response = await api.get<{ success: boolean; message: string; data: Maintenance[] }>(
      `/property/maintenance/room-type/${roomTypeId}`
    )
    return response.data
  },

  // Get maintenance schedules by room ID
  async getByRoomId(roomId: string) {
    const response = await api.get<{ success: boolean; message: string; data: Maintenance[] }>(
      `/property/maintenance/room/${roomId}`
    )
    return response.data
  },

  // Add maintenance schedule
  async create(maintenance: MaintenanceCreateDTO) {
    const response = await api.post<{ success: boolean; message: string; data: Maintenance }>(
      '/property/maintenance/add',
      maintenance
    )
    return response.data
  },
}
