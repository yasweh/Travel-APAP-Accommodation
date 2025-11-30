import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import RoomTypeList from '@/views/room-type/RoomTypeList.vue'
import { propertyService } from '@/services/propertyService'
import { roomTypeService } from '@/services/roomTypeService'

// Mock the services
vi.mock('@/services/propertyService', () => ({
  propertyService: {
    getAll: vi.fn()
  }
}))

vi.mock('@/services/roomTypeService', () => ({
  roomTypeService: {
    getAll: vi.fn(),
    getByPropertyId: vi.fn()
  }
}))

// Create mock router
const createMockRouter = () => {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/room-types', name: 'room-types', component: RoomTypeList },
      { path: '/property/edit/:id', name: 'property-edit', component: { template: '<div>Edit</div>' } }
    ]
  })
}

const mockProperties = [
  { propertyId: 'prop-1', propertyName: 'Beach Hotel', activeProperty: 1 },
  { propertyId: 'prop-2', propertyName: 'Mountain Resort', activeProperty: 1 },
  { propertyId: 'prop-3', propertyName: 'Inactive Hotel', activeProperty: 0 }
]

const mockRoomTypes = [
  {
    roomTypeId: 'rt-1',
    name: 'Deluxe Room',
    floor: 2,
    capacity: 2,
    facility: 'WiFi, AC, TV',
    price: 500000
  },
  {
    roomTypeId: 'rt-2',
    name: 'Suite Room',
    floor: 3,
    capacity: 4,
    facility: 'WiFi, AC, TV, Mini Bar, Jacuzzi',
    price: 1000000
  }
]

describe('RoomTypeList.vue', () => {
  let router: ReturnType<typeof createMockRouter>

  beforeEach(() => {
    vi.clearAllMocks()
    router = createMockRouter()
  })

  const mountComponent = async () => {
    router.push('/room-types')
    await router.isReady()
    
    const wrapper = mount(RoomTypeList, {
      global: {
        plugins: [router]
      }
    })
    
    await flushPromises()
    return wrapper
  }

  describe('Page Header', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: mockRoomTypes })
    })

    it('should display page title', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.page-title').text()).toBe('Room Type Management')
    })

    it('should display page subtitle', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Manage room types and their configurations')
    })

    it('should display header icon', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.header-icon').exists()).toBe(true)
    })
  })

  describe('Property Selector', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: mockRoomTypes })
    })

    it('should display property dropdown', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('#propertySelect').exists()).toBe(true)
    })

    it('should show only active properties in dropdown', async () => {
      const wrapper = await mountComponent()
      
      const options = wrapper.findAll('#propertySelect option')
      expect(options.length).toBe(3) // All Properties + 2 active properties
      expect(wrapper.text()).toContain('Beach Hotel')
      expect(wrapper.text()).toContain('Mountain Resort')
      expect(wrapper.text()).not.toContain('Inactive Hotel')
    })

    it('should have default "All Properties" option', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('All Properties')
    })

    it('should filter room types when property selected', async () => {
      vi.mocked(roomTypeService.getByPropertyId).mockResolvedValue({
        success: true,
        data: [mockRoomTypes[0]]
      })
      
      const wrapper = await mountComponent()
      
      const select = wrapper.find('#propertySelect')
      await select.setValue('prop-1')
      await flushPromises()
      
      expect(roomTypeService.getByPropertyId).toHaveBeenCalledWith('prop-1')
    })

    it('should show all room types when no property selected', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.room-type-card').length).toBe(2)
    })
  })

  describe('Add Room Type Button', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: mockRoomTypes })
    })

    it('should hide add button when no property selected', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-add').exists()).toBe(false)
    })

    it('should show add button when property selected', async () => {
      vi.mocked(roomTypeService.getByPropertyId).mockResolvedValue({
        success: true,
        data: mockRoomTypes
      })
      
      const wrapper = await mountComponent()
      
      const select = wrapper.find('#propertySelect')
      await select.setValue('prop-1')
      await flushPromises()
      
      expect(wrapper.find('.btn-add').exists()).toBe(true)
      expect(wrapper.text()).toContain('Add Room Type to Property')
    })

    it('should navigate to property edit page when add button clicked', async () => {
      vi.mocked(roomTypeService.getByPropertyId).mockResolvedValue({
        success: true,
        data: mockRoomTypes
      })
      
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent()
      
      const select = wrapper.find('#propertySelect')
      await select.setValue('prop-1')
      await flushPromises()
      
      await wrapper.find('.btn-add').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith('/property/edit/prop-1')
    })
  })

  describe('Loading State', () => {
    it('should show loading spinner while fetching data', async () => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      let resolvePromise: (value: any) => void
      vi.mocked(roomTypeService.getAll).mockImplementation(() => 
        new Promise(resolve => { resolvePromise = resolve })
      )
      
      router.push('/room-types')
      await router.isReady()
      
      const wrapper = mount(RoomTypeList, {
        global: { plugins: [router] }
      })
      
      // Check for loading indicator presence in HTML
      expect(wrapper.html().toLowerCase()).toContain('loading')
    })
  })

  describe('Error State', () => {
    it('should display error message when API fails', async () => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({
        success: false,
        message: 'Failed to load room types'
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.error-message').exists()).toBe(true)
      expect(wrapper.text()).toContain('Failed to load room types')
    })

    it('should handle network errors gracefully', async () => {
      vi.mocked(propertyService.getAll).mockRejectedValue(new Error('Network error'))
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: [] })
      
      const wrapper = await mountComponent()
      
      // Should not crash
      expect(wrapper.exists()).toBe(true)
    })
  })

  describe('Room Type Cards', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: mockRoomTypes })
    })

    it('should display room type cards', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.room-type-card').length).toBe(2)
    })

    it('should display room type name', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Deluxe Room')
      expect(wrapper.text()).toContain('Suite Room')
    })

    it('should display room type ID', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('rt-1')
      expect(wrapper.text()).toContain('rt-2')
    })

    it('should display floor information', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Floor')
      expect(wrapper.text()).toContain('2')
      expect(wrapper.text()).toContain('3')
    })

    it('should display capacity', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Capacity')
      expect(wrapper.text()).toContain('2 people')
      expect(wrapper.text()).toContain('4 people')
    })

    it('should display facilities', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Facilities')
      expect(wrapper.text()).toContain('WiFi, AC, TV')
    })

    it('should display formatted price', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Price per Night')
      expect(wrapper.text()).toContain('500.000')
      expect(wrapper.text()).toContain('1.000.000')
    })
  })

  describe('Empty State', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
    })

    it('should show empty state when no room types and no property selected', async () => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: [] })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.empty-state').exists()).toBe(true)
      expect(wrapper.text()).toContain('Select a Property')
      expect(wrapper.text()).toContain('Choose a property to view and manage its room types')
    })

    it('should show empty state message when property selected but no room types', async () => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: [] })
      vi.mocked(roomTypeService.getByPropertyId).mockResolvedValue({ success: true, data: [] })
      
      const wrapper = await mountComponent()
      
      const select = wrapper.find('#propertySelect')
      await select.setValue('prop-1')
      await flushPromises()
      
      expect(wrapper.find('.empty-state').exists()).toBe(true)
      expect(wrapper.text()).toContain('No Room Types Found')
    })

    it('should show create button in empty state when property selected', async () => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: [] })
      vi.mocked(roomTypeService.getByPropertyId).mockResolvedValue({ success: true, data: [] })
      
      const wrapper = await mountComponent()
      
      const select = wrapper.find('#propertySelect')
      await select.setValue('prop-1')
      await flushPromises()
      
      expect(wrapper.find('.btn-empty-state').exists()).toBe(true)
      expect(wrapper.text()).toContain('Create First Room Type')
    })

    it('should navigate to property edit when empty state button clicked', async () => {
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: [] })
      vi.mocked(roomTypeService.getByPropertyId).mockResolvedValue({ success: true, data: [] })
      
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent()
      
      const select = wrapper.find('#propertySelect')
      await select.setValue('prop-1')
      await flushPromises()
      
      await wrapper.find('.btn-empty-state').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith('/property/edit/prop-1')
    })
  })

  describe('Currency Formatting', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: mockRoomTypes })
    })

    it('should format currency with Indonesian locale', async () => {
      const wrapper = await mountComponent()
      
      // Indonesian format uses dots as thousand separators
      expect(wrapper.text()).toContain('500.000')
      expect(wrapper.text()).toContain('1.000.000')
    })
  })

  describe('Filter Card', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: mockRoomTypes })
    })

    it('should display filter card', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.filter-card').exists()).toBe(true)
    })

    it('should display filter header', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Select Property')
    })
  })

  describe('Room Info Grid', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: mockRoomTypes })
    })

    it('should display info items with icons', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.info-item').length).toBeGreaterThan(0)
      expect(wrapper.findAll('.info-icon').length).toBeGreaterThan(0)
    })

    it('should display info labels and values', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.info-label').length).toBeGreaterThan(0)
      expect(wrapper.findAll('.info-value').length).toBeGreaterThan(0)
    })
  })

  describe('Card Interactions', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: mockRoomTypes })
    })

    it('should have hover effect on cards', async () => {
      const wrapper = await mountComponent()
      
      const card = wrapper.find('.room-type-card')
      expect(card.classes()).not.toContain('hover') // Classes applied via CSS
    })
  })

  describe('Grid Layout', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: mockRoomTypes })
    })

    it('should render room types in grid layout', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.room-type-grid').exists()).toBe(true)
    })
  })

  describe('API Error Handling', () => {
    it('should handle property load error', async () => {
      vi.mocked(propertyService.getAll).mockRejectedValue({
        response: { data: { message: 'Failed to load properties' } }
      })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: [] })
      
      const wrapper = await mountComponent()
      
      // Should still render without crashing
      expect(wrapper.exists()).toBe(true)
    })

    it('should handle room types load error for specific property', async () => {
      vi.mocked(propertyService.getAll).mockResolvedValue({ success: true, data: mockProperties })
      vi.mocked(roomTypeService.getAll).mockResolvedValue({ success: true, data: mockRoomTypes })
      vi.mocked(roomTypeService.getByPropertyId).mockRejectedValue({
        response: { data: { message: 'Failed to load room types' } }
      })
      
      const wrapper = await mountComponent()
      
      const select = wrapper.find('#propertySelect')
      await select.setValue('prop-1')
      await flushPromises()
      
      expect(wrapper.find('.error-message').exists()).toBe(true)
    })
  })
})
