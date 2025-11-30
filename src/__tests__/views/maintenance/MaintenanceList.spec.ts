import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import MaintenanceList from '@/views/maintenance/MaintenanceList.vue'
import { maintenanceService } from '@/services/maintenanceService'
import { roomTypeService } from '@/services/roomTypeService'

// Mock the services
vi.mock('@/services/maintenanceService', () => ({
  maintenanceService: {
    getAll: vi.fn(),
    getByRoomId: vi.fn(),
    getByRoomTypeId: vi.fn()
  }
}))

vi.mock('@/services/roomTypeService', () => ({
  roomTypeService: {
    getAll: vi.fn(),
    getById: vi.fn()
  }
}))

// Create mock router
const createMockRouter = () => {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/maintenance', name: 'maintenance-list', component: MaintenanceList },
      { path: '/maintenance/create', name: 'maintenance-create', component: { template: '<div>Create</div>' } }
    ]
  })
}

const mockRoomTypes = [
  { roomTypeId: 'rt-1', name: 'Deluxe Room', floor: 2 },
  { roomTypeId: 'rt-2', name: 'Suite Room', floor: 3 }
]

const mockRooms = [
  { roomId: 'room-1', roomNumber: '101' },
  { roomId: 'room-2', roomNumber: '102' }
]

const mockMaintenances = [
  {
    maintenanceId: 'maint-1',
    propertyName: 'Beach Hotel',
    roomTypeName: 'Deluxe Room',
    roomName: '101',
    startDate: '2024-02-01',
    startTime: '09:00',
    endDate: '2024-02-01',
    endTime: '17:00'
  },
  {
    maintenanceId: 'maint-2',
    propertyName: 'Mountain Resort',
    roomTypeName: 'Suite Room',
    roomName: '301',
    startDate: '2024-02-05',
    startTime: '10:00',
    endDate: '2024-02-06',
    endTime: '15:00'
  }
]

describe('MaintenanceList.vue', () => {
  let router: ReturnType<typeof createMockRouter>

  beforeEach(() => {
    vi.clearAllMocks()
    router = createMockRouter()
  })

  const mountComponent = async () => {
    router.push('/maintenance')
    await router.isReady()
    
    const wrapper = mount(MaintenanceList, {
      global: {
        plugins: [router]
      }
    })
    
    await flushPromises()
    return wrapper
  }

  describe('Page Header', () => {
    beforeEach(() => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockResolvedValue({ success: true, data: mockMaintenances })
    })

    it('should display page title', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.page-title').text()).toBe('Maintenance Management')
    })

    it('should display page subtitle', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Schedule and manage room maintenance activities')
    })

    it('should display header icon', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.header-icon').exists()).toBe(true)
    })
  })

  describe('Filters', () => {
    beforeEach(() => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockResolvedValue({ success: true, data: mockMaintenances })
    })

    it('should display filter card', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.filter-card').exists()).toBe(true)
      expect(wrapper.text()).toContain('Filter Maintenance')
    })

    it('should display room type dropdown', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('#roomTypeId').exists()).toBe(true)
      expect(wrapper.text()).toContain('Room Type')
    })

    it('should populate room types in dropdown', async () => {
      const wrapper = await mountComponent()
      
      const options = wrapper.findAll('#roomTypeId option')
      expect(options.length).toBe(3) // All + 2 room types
      expect(wrapper.text()).toContain('Deluxe Room')
      expect(wrapper.text()).toContain('Suite Room')
    })

    it('should display room number dropdown', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('#roomId').exists()).toBe(true)
      expect(wrapper.text()).toContain('Room Number')
    })

    it('should disable room dropdown when no room type selected', async () => {
      const wrapper = await mountComponent()
      
      const roomSelect = wrapper.find('#roomId')
      expect((roomSelect.element as HTMLSelectElement).disabled).toBe(true)
    })

    it('should enable room dropdown when room type selected', async () => {
      vi.mocked(roomTypeService.getById).mockResolvedValue({
        data: { ...mockRoomTypes[0], rooms: mockRooms }
      })
      vi.mocked(maintenanceService.getByRoomTypeId).mockResolvedValue({
        success: true,
        data: mockMaintenances
      })
      
      const wrapper = await mountComponent()
      
      const roomTypeSelect = wrapper.find('#roomTypeId')
      await roomTypeSelect.setValue('rt-1')
      await flushPromises()
      
      const roomSelect = wrapper.find('#roomId')
      expect((roomSelect.element as HTMLSelectElement).disabled).toBe(false)
    })

    it('should load rooms when room type is selected', async () => {
      vi.mocked(roomTypeService.getById).mockResolvedValue({
        data: { ...mockRoomTypes[0], rooms: mockRooms }
      })
      vi.mocked(maintenanceService.getByRoomTypeId).mockResolvedValue({
        success: true,
        data: mockMaintenances
      })
      
      const wrapper = await mountComponent()
      
      const roomTypeSelect = wrapper.find('#roomTypeId')
      await roomTypeSelect.setValue('rt-1')
      await flushPromises()
      
      expect(roomTypeService.getById).toHaveBeenCalledWith('rt-1')
      expect(wrapper.text()).toContain('101')
      expect(wrapper.text()).toContain('102')
    })

    it('should filter by room type', async () => {
      vi.mocked(roomTypeService.getById).mockResolvedValue({
        data: { ...mockRoomTypes[0], rooms: mockRooms }
      })
      vi.mocked(maintenanceService.getByRoomTypeId).mockResolvedValue({
        success: true,
        data: [mockMaintenances[0]]
      })
      
      const wrapper = await mountComponent()
      
      const roomTypeSelect = wrapper.find('#roomTypeId')
      await roomTypeSelect.setValue('rt-1')
      await flushPromises()
      
      expect(maintenanceService.getByRoomTypeId).toHaveBeenCalledWith('rt-1')
    })

    it('should filter by room', async () => {
      vi.mocked(roomTypeService.getById).mockResolvedValue({
        data: { ...mockRoomTypes[0], rooms: mockRooms }
      })
      vi.mocked(maintenanceService.getByRoomTypeId).mockResolvedValue({
        success: true,
        data: mockMaintenances
      })
      vi.mocked(maintenanceService.getByRoomId).mockResolvedValue({
        success: true,
        data: [mockMaintenances[0]]
      })
      
      const wrapper = await mountComponent()
      
      // Select room type first
      const roomTypeSelect = wrapper.find('#roomTypeId')
      await roomTypeSelect.setValue('rt-1')
      await flushPromises()
      
      // Then select room
      const roomSelect = wrapper.find('#roomId')
      await roomSelect.setValue('room-1')
      await flushPromises()
      
      expect(maintenanceService.getByRoomId).toHaveBeenCalledWith('room-1')
    })

    it('should reset room selection when room type changes', async () => {
      vi.mocked(roomTypeService.getById).mockResolvedValue({
        data: { ...mockRoomTypes[0], rooms: mockRooms }
      })
      vi.mocked(maintenanceService.getByRoomTypeId).mockResolvedValue({
        success: true,
        data: mockMaintenances
      })
      
      const wrapper = await mountComponent()
      
      // Select room type and room
      const roomTypeSelect = wrapper.find('#roomTypeId')
      await roomTypeSelect.setValue('rt-1')
      await flushPromises()
      
      const roomSelect = wrapper.find('#roomId')
      await roomSelect.setValue('room-1')
      
      // Change room type
      await roomTypeSelect.setValue('rt-2')
      await flushPromises()
      
      expect((roomSelect.element as HTMLSelectElement).value).toBe('')
    })
  })

  describe('Add Button', () => {
    beforeEach(() => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockResolvedValue({ success: true, data: mockMaintenances })
    })

    it('should display Add New Schedule button', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-add').exists()).toBe(true)
      expect(wrapper.text()).toContain('Add New Schedule')
    })

    it('should navigate to create page when clicked', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent()
      
      await wrapper.find('.btn-add').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith('/maintenance/create')
    })
  })

  describe('Loading State', () => {
    it('should show loading spinner while fetching', async () => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      let resolvePromise: (value: any) => void
      vi.mocked(maintenanceService.getAll).mockImplementation(() => 
        new Promise(resolve => { resolvePromise = resolve })
      )
      
      router.push('/maintenance')
      await router.isReady()
      
      const wrapper = mount(MaintenanceList, {
        global: { plugins: [router] }
      })
      
      // Check for loading indicator presence in HTML
      expect(wrapper.html().toLowerCase()).toContain('loading')
    })
  })

  describe('Error State', () => {
    it('should display error message when API fails', async () => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockResolvedValue({
        success: false,
        message: 'Failed to load maintenance schedules'
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.error-message').exists()).toBe(true)
      expect(wrapper.text()).toContain('Failed to load maintenance schedules')
    })

    it('should handle network errors gracefully', async () => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockRejectedValue({
        response: { data: { message: 'Network error' } }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.error-message').exists()).toBe(true)
    })
  })

  describe('Maintenance Cards', () => {
    beforeEach(() => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockResolvedValue({ success: true, data: mockMaintenances })
    })

    it('should display maintenance cards', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.maintenance-card').length).toBe(2)
    })

    it('should display maintenance ID', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('maint-1')
      expect(wrapper.text()).toContain('maint-2')
    })

    it('should display property name', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Beach Hotel')
      expect(wrapper.text()).toContain('Mountain Resort')
    })

    it('should display room type name', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Deluxe Room')
      expect(wrapper.text()).toContain('Suite Room')
    })

    it('should display room name', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Room 101')
      expect(wrapper.text()).toContain('Room 301')
    })

    it('should display scheduled status badge', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-badge.active').exists()).toBe(true)
      expect(wrapper.text()).toContain('Scheduled')
    })

    it('should display start date and time', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Start')
      expect(wrapper.text()).toContain('09:00')
    })

    it('should display end date and time', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('End')
      expect(wrapper.text()).toContain('17:00')
    })

    it('should format dates correctly', async () => {
      const wrapper = await mountComponent()
      
      // Should contain formatted date parts
      expect(wrapper.text()).toContain('2024')
      expect(wrapper.text()).toContain('Februari')
    })
  })

  describe('Empty State', () => {
    beforeEach(() => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockResolvedValue({ success: true, data: [] })
    })

    it('should show empty state when no maintenance schedules', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.empty-state').exists()).toBe(true)
      expect(wrapper.text()).toContain('No Maintenance Schedules')
    })

    it('should show create button in empty state', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-empty-state').exists()).toBe(true)
      expect(wrapper.text()).toContain('Create First Schedule')
    })

    it('should navigate to create page from empty state button', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent()
      
      await wrapper.find('.btn-empty-state').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith('/maintenance/create')
    })
  })

  describe('Date Formatting', () => {
    beforeEach(() => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockResolvedValue({ success: true, data: mockMaintenances })
    })

    it('should format date in Indonesian locale', async () => {
      const wrapper = await mountComponent()
      
      // Indonesian date format includes month names in Indonesian
      expect(wrapper.text()).toContain('Februari')
    })

    it('should handle missing date gracefully', async () => {
      vi.mocked(maintenanceService.getAll).mockResolvedValue({
        success: true,
        data: [{ ...mockMaintenances[0], startDate: '' }]
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('N/A')
    })
  })

  describe('Schedule Display', () => {
    beforeEach(() => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockResolvedValue({ success: true, data: mockMaintenances })
    })

    it('should display start and end schedule items', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.schedule-item').length).toBeGreaterThan(0)
    })

    it('should display arrow icon between start and end', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.arrow-icon').exists()).toBe(true)
    })

    it('should display schedule labels', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.schedule-label').length).toBeGreaterThan(0)
    })
  })

  describe('Grid Layout', () => {
    beforeEach(() => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockResolvedValue({ success: true, data: mockMaintenances })
    })

    it('should render maintenances in grid layout', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.maintenance-grid').exists()).toBe(true)
    })
  })

  describe('Card Styling', () => {
    beforeEach(() => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockResolvedValue({ success: true, data: mockMaintenances })
    })

    it('should have card header', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.card-header').length).toBe(2)
    })

    it('should have card body', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.card-body').length).toBe(2)
    })

    it('should display property info section', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.maintenance-property').exists()).toBe(true)
    })

    it('should display room info section', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.maintenance-room').exists()).toBe(true)
    })
  })

  describe('Filter Persistence', () => {
    beforeEach(() => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ data: mockRoomTypes })
      vi.mocked(maintenanceService.getAll).mockResolvedValue({ success: true, data: mockMaintenances })
    })

    it('should show all maintenances when filter is cleared', async () => {
      vi.mocked(roomTypeService.getById).mockResolvedValue({
        data: { ...mockRoomTypes[0], rooms: mockRooms }
      })
      vi.mocked(maintenanceService.getByRoomTypeId).mockResolvedValue({
        success: true,
        data: [mockMaintenances[0]]
      })
      
      const wrapper = await mountComponent()
      
      // Apply filter
      const roomTypeSelect = wrapper.find('#roomTypeId')
      await roomTypeSelect.setValue('rt-1')
      await flushPromises()
      
      expect(maintenanceService.getByRoomTypeId).toHaveBeenCalled()
      
      // Clear filter
      vi.clearAllMocks()
      await roomTypeSelect.setValue('')
      await flushPromises()
      
      // Should show all maintenances from the cached allRoomTypes
      expect(wrapper.findAll('.maintenance-card').length).toBe(2)
    })
  })
})
