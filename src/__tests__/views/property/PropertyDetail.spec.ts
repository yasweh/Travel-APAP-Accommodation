import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import PropertyDetail from '@/views/property/PropertyDetail.vue'
import { propertyService } from '@/services/propertyService'

// Mock the services
vi.mock('@/services/propertyService', () => ({
  propertyService: {
    getById: vi.fn(),
    getByIdWithFilter: vi.fn(),
    delete: vi.fn()
  }
}))

// Create mock router
const createMockRouter = () => {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/property/:id', name: 'property-detail', component: PropertyDetail },
      { path: '/property', name: 'property-list', component: { template: '<div>List</div>' } },
      { path: '/property/edit/:id', name: 'property-edit', component: { template: '<div>Edit</div>' } },
      { path: '/booking/create', name: 'booking-create', component: { template: '<div>Book</div>' } },
      { path: '/maintenance/create', name: 'maintenance-create', component: { template: '<div>Maintenance</div>' } }
    ]
  })
}

const mockProperty = {
  propertyId: 'prop-123',
  propertyName: 'Beach Hotel',
  type: 0,
  province: 4,
  address: '123 Beach Road, Bali',
  description: 'Beautiful beachfront property',
  totalRoom: 20,
  income: 50000000,
  activeStatus: 1,
  ownerName: 'John Owner',
  ownerId: 'owner-123',
  createdDate: '2024-01-01T10:00:00Z',
  updatedDate: '2024-01-15T10:00:00Z'
}

const mockRoomTypes = [
  {
    roomTypeId: 'rt-1',
    name: 'Deluxe Room',
    price: 500000,
    capacity: 2,
    floor: 2,
    facility: 'WiFi, AC, TV',
    description: 'Spacious deluxe room'
  },
  {
    roomTypeId: 'rt-2',
    name: 'Suite',
    price: 1000000,
    capacity: 4,
    floor: 3,
    facility: 'WiFi, AC, TV, Minibar',
    description: 'Luxury suite'
  }
]

const mockRooms = [
  {
    roomId: 'room-1',
    name: 'Room 101',
    availabilityStatus: 1,
    activeRoom: 1,
    roomType: { roomTypeId: 'rt-1' }
  },
  {
    roomId: 'room-2',
    name: 'Room 102',
    availabilityStatus: 0,
    activeRoom: 1,
    roomType: { roomTypeId: 'rt-1' }
  },
  {
    roomId: 'room-3',
    name: 'Room 301',
    availabilityStatus: 1,
    activeRoom: 0,
    roomType: { roomTypeId: 'rt-2' }
  }
]

describe('PropertyDetail.vue', () => {
  let router: ReturnType<typeof createMockRouter>

  beforeEach(() => {
    vi.clearAllMocks()
    router = createMockRouter()
    window.alert = vi.fn()
    window.confirm = vi.fn()
  })

  const mountComponent = async () => {
    router.push('/property/prop-123')
    await router.isReady()
    
    const wrapper = mount(PropertyDetail, {
      global: {
        plugins: [router]
      }
    })
    
    await flushPromises()
    return wrapper
  }

  describe('Loading State', () => {
    it('should show loading spinner while fetching', async () => {
      let resolvePromise: (value: any) => void
      vi.mocked(propertyService.getById).mockImplementation(() => 
        new Promise(resolve => { resolvePromise = resolve })
      )
      
      router.push('/property/prop-123')
      await router.isReady()
      
      const wrapper = mount(PropertyDetail, {
        global: { plugins: [router] }
      })
      
      // Check for loading indicator presence in HTML
      expect(wrapper.html().toLowerCase()).toContain('loading')
    })
  })

  describe('Error State', () => {
    it('should display error message when API fails', async () => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: false,
        message: 'Property not found'
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.error').exists()).toBe(true)
      expect(wrapper.text()).toContain('Property not found')
    })

    it('should handle network errors', async () => {
      vi.mocked(propertyService.getById).mockRejectedValue({
        response: { data: { message: 'Network error' } }
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.error').exists()).toBe(true)
    })
  })

  describe('Property Information Display', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: mockRoomTypes,
        rooms: mockRooms
      })
    })

    it('should display property name in header', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.page-title').text()).toBe('Beach Hotel')
    })

    it('should display property ID', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('prop-123')
    })

    it('should display property type', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Hotel')
    })

    it('should display province name', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Bali')
    })

    it('should display address', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('123 Beach Road, Bali')
    })

    it('should display description', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Beautiful beachfront property')
    })

    it('should display total rooms', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('20')
    })

    it('should display formatted income', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('50.000.000')
    })

    it('should display owner name', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('John Owner')
    })
  })

  describe('Status Badge', () => {
    it('should show active badge for active property', async () => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: { ...mockProperty, activeStatus: 1 },
        roomTypes: [],
        rooms: []
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-active').exists()).toBe(true)
      expect(wrapper.text()).toContain('Active')
    })

    it('should show inactive badge for inactive property', async () => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: { ...mockProperty, activeStatus: 0 },
        roomTypes: [],
        rooms: []
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.status-inactive').exists()).toBe(true)
      expect(wrapper.text()).toContain('Inactive')
    })
  })

  describe('Header Action Buttons', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: mockRoomTypes,
        rooms: mockRooms
      })
    })

    it('should have Update button', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-edit').exists()).toBe(true)
      expect(wrapper.text()).toContain('Update')
    })

    it('should have Add Room button', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-primary').text()).toContain('Add Room')
    })

    it('should have Delete button', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-delete').exists()).toBe(true)
    })

    it('should have Back button', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-secondary').text()).toContain('Back')
    })

    it('should navigate to edit page when Update clicked', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent()
      
      await wrapper.find('.btn-edit').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith('/property/edit/prop-123')
    })

    it('should navigate back to list when Back clicked', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent()
      
      await wrapper.find('.btn-secondary').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith('/property')
    })
  })

  describe('Delete Property', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: [],
        rooms: []
      })
    })

    it('should confirm before deleting', async () => {
      vi.mocked(window.confirm).mockReturnValue(false)
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-delete').trigger('click')
      
      expect(window.confirm).toHaveBeenCalledWith(expect.stringContaining('Beach Hotel'))
      expect(propertyService.delete).not.toHaveBeenCalled()
    })

    it('should delete property when confirmed', async () => {
      vi.mocked(window.confirm).mockReturnValue(true)
      vi.mocked(propertyService.delete).mockResolvedValue({ success: true })
      
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent()
      await wrapper.find('.btn-delete').trigger('click')
      await flushPromises()
      
      expect(propertyService.delete).toHaveBeenCalledWith('prop-123')
      expect(window.alert).toHaveBeenCalledWith('Property deleted successfully')
      expect(pushSpy).toHaveBeenCalledWith('/property')
    })

    it('should show error when delete fails', async () => {
      vi.mocked(window.confirm).mockReturnValue(true)
      vi.mocked(propertyService.delete).mockResolvedValue({
        success: false,
        message: 'Cannot delete'
      })
      
      const wrapper = await mountComponent()
      await wrapper.find('.btn-delete').trigger('click')
      await flushPromises()
      
      expect(window.alert).toHaveBeenCalledWith('Cannot delete')
    })
  })

  describe('Date Filter', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: mockRoomTypes,
        rooms: mockRooms
      })
    })

    it('should display filter form', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Filter Available Rooms')
      expect(wrapper.find('#checkIn').exists()).toBe(true)
      expect(wrapper.find('#checkOut').exists()).toBe(true)
    })

    it('should have Apply Filter button', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-submit').text()).toContain('Apply Filter')
    })

    it('should have Clear Filter button', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-cancel').text()).toContain('Clear Filter')
    })

    it('should show error when dates are missing', async () => {
      const wrapper = await mountComponent()
      
      await wrapper.find('.btn-submit').trigger('click')
      await flushPromises()
      
      expect(wrapper.text()).toContain('Please select both check-in and check-out dates')
    })

    it('should show error when checkout is before checkin', async () => {
      const wrapper = await mountComponent()
      
      await wrapper.find('#checkIn').setValue('2024-02-10')
      await wrapper.find('#checkOut').setValue('2024-02-05')
      await wrapper.find('.btn-submit').trigger('click')
      await flushPromises()
      
      expect(wrapper.text()).toContain('Check-out date must be after check-in date')
    })

    it('should apply filter with valid dates', async () => {
      vi.mocked(propertyService.getByIdWithFilter).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: mockRoomTypes,
        rooms: [mockRooms[0]]
      })
      
      const wrapper = await mountComponent()
      
      await wrapper.find('#checkIn').setValue('2024-02-01')
      await wrapper.find('#checkOut').setValue('2024-02-05')
      await wrapper.find('.btn-submit').trigger('click')
      await flushPromises()
      
      expect(propertyService.getByIdWithFilter).toHaveBeenCalledWith('prop-123', '2024-02-01', '2024-02-05')
      expect(wrapper.find('.filter-badge').exists()).toBe(true)
    })

    it('should clear filter', async () => {
      vi.mocked(propertyService.getByIdWithFilter).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: mockRoomTypes,
        rooms: [mockRooms[0]]
      })
      
      const wrapper = await mountComponent()
      
      // Apply filter first
      await wrapper.find('#checkIn').setValue('2024-02-01')
      await wrapper.find('#checkOut').setValue('2024-02-05')
      await wrapper.find('.btn-submit').trigger('click')
      await flushPromises()
      
      // Clear filter
      vi.clearAllMocks()
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: mockRoomTypes,
        rooms: mockRooms
      })
      
      await wrapper.find('.btn-cancel').trigger('click')
      await flushPromises()
      
      expect(propertyService.getById).toHaveBeenCalled()
    })
  })

  describe('Room Types Display', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: mockRoomTypes,
        rooms: mockRooms
      })
    })

    it('should display room types section', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Room Types & Rooms')
    })

    it('should display room type names', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Deluxe Room')
      expect(wrapper.text()).toContain('Suite')
    })

    it('should display room type prices', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('500.000')
      expect(wrapper.text()).toContain('1.000.000')
    })

    it('should display room type capacity', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('2 person')
      expect(wrapper.text()).toContain('4 person')
    })

    it('should display room type floor', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Floor: 2')
      expect(wrapper.text()).toContain('Floor: 3')
    })

    it('should display room type facilities', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('WiFi, AC, TV')
    })

    it('should display room count badge', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('2 rooms')
      expect(wrapper.text()).toContain('1 rooms')
    })
  })

  describe('Rooms Display', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: mockRoomTypes,
        rooms: mockRooms
      })
    })

    it('should display room cards', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.findAll('.room-card-modern').length).toBe(3)
    })

    it('should display room names', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('Room 101')
      expect(wrapper.text()).toContain('Room 102')
      expect(wrapper.text()).toContain('Room 301')
    })

    it('should display availability badge', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.badge-available').exists()).toBe(true)
      expect(wrapper.find('.badge-unavailable').exists()).toBe(true)
    })

    it('should display active/maintenance badge', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.badge-active').exists()).toBe(true)
      expect(wrapper.find('.badge-inactive').exists()).toBe(true)
    })

    it('should have Book Now button', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-book').exists()).toBe(true)
    })

    it('should have Maintenance button', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.btn-maintenance').exists()).toBe(true)
    })

    it('should disable Book button for unavailable rooms', async () => {
      const wrapper = await mountComponent()
      
      const bookButtons = wrapper.findAll('.btn-book')
      const disabledButtons = bookButtons.filter(b => (b.element as HTMLButtonElement).disabled)
      
      expect(disabledButtons.length).toBeGreaterThan(0)
    })

    it('should navigate to booking page when Book clicked', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent()
      
      const availableBookButton = wrapper.findAll('.btn-book').find(
        b => !(b.element as HTMLButtonElement).disabled
      )
      await availableBookButton?.trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith(expect.objectContaining({
        path: '/booking/create',
        query: expect.objectContaining({ roomId: 'room-1' })
      }))
    })

    it('should navigate to maintenance page when Maintenance clicked', async () => {
      const pushSpy = vi.spyOn(router, 'push')
      const wrapper = await mountComponent()
      
      await wrapper.find('.btn-maintenance').trigger('click')
      
      expect(pushSpy).toHaveBeenCalledWith({
        path: '/maintenance/create',
        query: { roomId: 'room-1' }
      })
    })
  })

  describe('Empty State', () => {
    it('should show empty state when no room types', async () => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: [],
        rooms: []
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.empty-state').exists()).toBe(true)
      expect(wrapper.text()).toContain('No Room Types Yet')
    })

    it('should show empty rooms message when room type has no rooms', async () => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: [mockRoomTypes[0]],
        rooms: []
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.empty-rooms').exists()).toBe(true)
      expect(wrapper.text()).toContain('No rooms created for this room type')
    })
  })

  describe('Property Type Mapping', () => {
    it('should display Hotel for type 0', async () => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: { ...mockProperty, type: 0 },
        roomTypes: [],
        rooms: []
      })
      
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('Hotel')
    })

    it('should display Villa for type 1', async () => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: { ...mockProperty, type: 1 },
        roomTypes: [],
        rooms: []
      })
      
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('Villa')
    })

    it('should display Apartemen for type 2', async () => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: { ...mockProperty, type: 2 },
        roomTypes: [],
        rooms: []
      })
      
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('Apartemen')
    })
  })

  describe('Province Mapping', () => {
    const provinces = [
      'DKI Jakarta', 'Jawa Barat', 'Jawa Tengah', 'Jawa Timur',
      'Bali', 'Sumatera Utara', 'Sulawesi Selatan', 'Kalimantan Timur'
    ]

    provinces.forEach((provinceName, index) => {
      it(`should display ${provinceName} for province ${index}`, async () => {
        vi.mocked(propertyService.getById).mockResolvedValue({
          success: true,
          property: { ...mockProperty, province: index },
          roomTypes: [],
          rooms: []
        })
        
        const wrapper = await mountComponent()
        expect(wrapper.text()).toContain(provinceName)
      })
    })
  })

  describe('Date Formatting', () => {
    beforeEach(() => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: mockProperty,
        roomTypes: [],
        rooms: []
      })
    })

    it('should format created date', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('2024')
    })

    it('should handle missing dates', async () => {
      vi.mocked(propertyService.getById).mockResolvedValue({
        success: true,
        property: { ...mockProperty, createdDate: '' },
        roomTypes: [],
        rooms: []
      })
      
      const wrapper = await mountComponent()
      
      expect(wrapper.text()).toContain('-')
    })
  })
})
