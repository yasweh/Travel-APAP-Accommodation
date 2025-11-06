import api from './api'

export interface Maintenance {
  maintenanceId: string
  startDate: string
  startTime: string
  endDate: string
  endTime: string
  room: {
    roomId: string
    name: string
  }
}

export interface MaintenanceCreateDTO {
  roomId: string
  startDate: string
  startTime: string
  endDate: string
  endTime: string
}

export const maintenanceService = {
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
