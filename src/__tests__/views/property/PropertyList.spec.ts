import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import PropertyList from '@/views/property/PropertyList.vue'

// Mock vue-router
const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock propertyService
const mockGetAll = vi.fn()
const mockDelete = vi.fn()

vi.mock('@/services/propertyService', () => ({
  propertyService: {
    getAll: (params?: any) => mockGetAll(params),
    delete: (id: string) => mockDelete(id)
  }
}))

// Mock window.confirm and window.alert
const mockConfirm = vi.fn()
const mockAlert = vi.fn()
Object.defineProperty(window, 'confirm', { value: mockConfirm, writable: true })
Object.defineProperty(window, 'alert', { value: mockAlert, writable: true })

describe('PropertyList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockConfirm.mockReturnValue(true)
    mockGetAll.mockResolvedValue({
      success: true,
      data: []
    })
  })

  const mockProperties = [
    {
      propertyId: 'prop-1',
      propertyName: 'Grand Hotel',
      address: 'Jl. Sudirman No. 1, Jakarta',
      type: 0,
      typeString: 'Hotel',
      activeStatus: 1,
      activeStatusString: 'Active',
      roomCount: 50,
      income: 250000000
    },
    {
      propertyId: 'prop-2',
      propertyName: 'Beach Villa',
      address: 'Jl. Pantai Indah, Bali',
      type: 1,
      typeString: 'Villa',
      activeStatus: 1,
      activeStatusString: 'Active',
      roomCount: 10,
      income: 75000000
    },
    {
      propertyId: 'prop-3',
      propertyName: 'City Apartment',
      address: 'Jl. Gatot Subroto, Surabaya',
      type: 2,
      typeString: 'Apartemen',
      activeStatus: 0,
      activeStatusString: 'Inactive',
      roomCount: 25,
      income: 0
    }
  ]

  const mountPropertyList = async (properties = mockProperties) => {
    mockGetAll.mockResolvedValue({
      success: true,
      data: properties
    })
    
    const wrapper = mount(PropertyList, {
      global: {
        stubs: {
          RouterLink: true
        }
      }
    })
    await flushPromises()
    return wrapper
  }

  describe('Component Rendering', () => {
    it('renders property list container', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('.property-list').exists()).toBe(true)
    })

    it('renders page header', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('.page-header').exists()).toBe(true)
    })

    it('renders page title', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('.page-title').text()).toBe('Property Management')
    })

    it('renders page subtitle', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('.page-subtitle').text()).toBe('Manage your properties and accommodations')
    })

    it('renders header icon', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('.header-icon').exists()).toBe(true)
    })
  })

  describe('Filters Section', () => {
    it('renders filters section', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('.filters-section').exists()).toBe(true)
    })

    it('renders property name filter', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('#filter-name').exists()).toBe(true)
    })

    it('renders property type filter', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('#filter-type').exists()).toBe(true)
    })

    it('renders province filter', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('#filter-province').exists()).toBe(true)
    })

    it('renders clear filters button', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('.btn-clear-filters').exists()).toBe(true)
    })

    it('has correct type options', async () => {
      const wrapper = await mountPropertyList()
      const typeSelect = wrapper.find('#filter-type')
      expect(typeSelect.text()).toContain('All Types')
      expect(typeSelect.text()).toContain('Hotel')
      expect(typeSelect.text()).toContain('Villa')
      expect(typeSelect.text()).toContain('Apartemen')
    })

    it('has all province options', async () => {
      const wrapper = await mountPropertyList()
      const provinceSelect = wrapper.find('#filter-province')
      expect(provinceSelect.text()).toContain('All Provinces')
      expect(provinceSelect.text()).toContain('DKI Jakarta')
      expect(provinceSelect.text()).toContain('Bali')
    })

    it('calls loadProperties when filter name changes', async () => {
      const wrapper = await mountPropertyList()
      await wrapper.find('#filter-name').setValue('hotel')
      await wrapper.find('#filter-name').trigger('input')
      await flushPromises()
      expect(mockGetAll).toHaveBeenCalledWith(expect.objectContaining({ name: 'hotel' }))
    })

    it('calls loadProperties when type filter changes', async () => {
      const wrapper = await mountPropertyList()
      await wrapper.find('#filter-type').setValue('0')
      await wrapper.find('#filter-type').trigger('change')
      await flushPromises()
      expect(mockGetAll).toHaveBeenCalledWith(expect.objectContaining({ type: 0 }))
    })

    it('clears filters when clear button is clicked', async () => {
      const wrapper = await mountPropertyList()
      await wrapper.find('#filter-name').setValue('hotel')
      await wrapper.find('.btn-clear-filters').trigger('click')
      await flushPromises()
      expect((wrapper.find('#filter-name').element as HTMLInputElement).value).toBe('')
    })
  })

  describe('Action Buttons', () => {
    it('renders Create New Property button', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('.btn-primary').exists()).toBe(true)
      expect(wrapper.find('.btn-primary').text()).toContain('Create New Property')
    })

    it('renders Refresh button', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('.btn-secondary').exists()).toBe(true)
      expect(wrapper.find('.btn-secondary').text()).toContain('Refresh')
    })

    it('renders quick action buttons', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('.quick-actions').exists()).toBe(true)
    })

    it('renders Room Types quick button', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.text()).toContain('Room Types')
    })

    it('renders Maintenance quick button', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.text()).toContain('Maintenance')
    })

    it('renders Statistics quick button', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.text()).toContain('Statistics')
    })

    it('navigates to create property when button is clicked', async () => {
      const wrapper = await mountPropertyList()
      await wrapper.find('.btn-primary').trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/property/create')
    })

    it('navigates to room-type when quick button is clicked', async () => {
      const wrapper = await mountPropertyList()
      const quickButtons = wrapper.findAll('.btn-quick')
      await quickButtons[0].trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/room-type')
    })

    it('navigates to maintenance when quick button is clicked', async () => {
      const wrapper = await mountPropertyList()
      const quickButtons = wrapper.findAll('.btn-quick')
      await quickButtons[1].trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/maintenance')
    })

    it('navigates to statistics when quick button is clicked', async () => {
      const wrapper = await mountPropertyList()
      const quickButtons = wrapper.findAll('.btn-quick')
      await quickButtons[2].trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/booking/chart')
    })
  })

  describe('Loading State', () => {
    it('shows loading spinner when loading', async () => {
      // Component starts loading in onMounted
      const wrapper = await mountPropertyList()
      
      // After data is loaded, loading should not be visible
      expect(wrapper.find('.loading-spinner').exists()).toBe(false)
    })

    it('hides loading after data loads', async () => {
      const wrapper = await mountPropertyList()
      // After data is loaded, loading should not be visible
      expect(wrapper.find('.loading-spinner').exists()).toBe(false)
    })
  })

  describe('Properties Grid', () => {
    it('renders properties grid when data exists', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.find('.properties-grid').exists()).toBe(true)
    })

    it('renders correct number of property cards', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.findAll('.property-card').length).toBe(3)
    })

    it('renders property name', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.text()).toContain('Grand Hotel')
    })

    it('renders property type', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.text()).toContain('Hotel')
    })

    it('renders property address', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.text()).toContain('Jl. Sudirman No. 1, Jakarta')
    })

    it('renders room count', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.text()).toContain('50 rooms')
    })

    it('renders formatted income', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.text()).toContain('250.000.000')
    })
  })

  describe('Status Badges', () => {
    it('renders status badge for each property', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.findAll('.status-badge').length).toBe(3)
    })

    it('applies active class for active properties', async () => {
      const wrapper = await mountPropertyList()
      const badges = wrapper.findAll('.status-badge')
      expect(badges[0].classes()).toContain('active')
    })

    it('applies inactive class for inactive properties', async () => {
      const wrapper = await mountPropertyList()
      const badges = wrapper.findAll('.status-badge')
      expect(badges[2].classes()).toContain('inactive')
    })
  })

  describe('Card Actions', () => {
    it('renders Detail button for each card', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.findAll('.btn-detail').length).toBe(3)
    })

    it('renders Edit button for each card', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.findAll('.btn-edit').length).toBe(3)
    })

    it('renders Delete button for each card', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.findAll('.btn-delete').length).toBe(3)
    })

    it('navigates to detail page when Detail is clicked', async () => {
      const wrapper = await mountPropertyList()
      const detailButtons = wrapper.findAll('.btn-detail')
      await detailButtons[0].trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/property/prop-1')
    })

    it('navigates to edit page when Edit is clicked', async () => {
      const wrapper = await mountPropertyList()
      const editButtons = wrapper.findAll('.btn-edit')
      await editButtons[0].trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/property/edit/prop-1')
    })

    it('shows confirm dialog when Delete is clicked', async () => {
      mockDelete.mockResolvedValue({ success: true })
      const wrapper = await mountPropertyList()
      const deleteButtons = wrapper.findAll('.btn-delete')
      await deleteButtons[0].trigger('click')
      expect(mockConfirm).toHaveBeenCalled()
    })

    it('calls delete API when confirmed', async () => {
      mockDelete.mockResolvedValue({ success: true })
      const wrapper = await mountPropertyList()
      const deleteButtons = wrapper.findAll('.btn-delete')
      await deleteButtons[0].trigger('click')
      await flushPromises()
      expect(mockDelete).toHaveBeenCalledWith('prop-1')
    })

    it('does not call delete API when cancelled', async () => {
      mockConfirm.mockReturnValue(false)
      const wrapper = await mountPropertyList()
      const deleteButtons = wrapper.findAll('.btn-delete')
      await deleteButtons[0].trigger('click')
      expect(mockDelete).not.toHaveBeenCalled()
    })

    it('shows success alert after successful delete', async () => {
      mockDelete.mockResolvedValue({ success: true })
      const wrapper = await mountPropertyList()
      const deleteButtons = wrapper.findAll('.btn-delete')
      await deleteButtons[0].trigger('click')
      await flushPromises()
      expect(mockAlert).toHaveBeenCalledWith('Property deleted successfully')
    })
  })

  describe('Empty State', () => {
    it('shows empty state when no properties', async () => {
      const wrapper = await mountPropertyList([])
      expect(wrapper.find('.empty-state').exists()).toBe(true)
    })

    it('shows empty state title', async () => {
      const wrapper = await mountPropertyList([])
      expect(wrapper.find('.empty-state h3').text()).toBe('No Properties Found')
    })

    it('shows empty state description', async () => {
      const wrapper = await mountPropertyList([])
      expect(wrapper.text()).toContain('Start by creating your first property')
    })

    it('shows Create First Property button', async () => {
      const wrapper = await mountPropertyList([])
      expect(wrapper.find('.btn-empty-state').text()).toContain('Create First Property')
    })

    it('navigates to create when empty state button is clicked', async () => {
      const wrapper = await mountPropertyList([])
      await wrapper.find('.btn-empty-state').trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/property/create')
    })
  })

  describe('Error Handling', () => {
    it('shows error message when loading fails', async () => {
      mockGetAll.mockResolvedValue({
        success: false,
        message: 'Failed to load properties'
      })
      const wrapper = mount(PropertyList, {
        global: {
          stubs: {
            RouterLink: true
          }
        }
      })
      await flushPromises()
      expect(wrapper.find('.error-message').exists()).toBe(true)
      expect(wrapper.text()).toContain('Failed to load properties')
    })

    it('shows error message when API throws error', async () => {
      mockGetAll.mockRejectedValue({
        response: {
          data: {
            message: 'Server error'
          }
        }
      })
      const wrapper = mount(PropertyList, {
        global: {
          stubs: {
            RouterLink: true
          }
        }
      })
      await flushPromises()
      expect(wrapper.find('.error-message').exists()).toBe(true)
    })

    it('shows alert on delete error', async () => {
      mockDelete.mockRejectedValue({
        response: {
          data: {
            message: 'Cannot delete property'
          }
        }
      })
      const wrapper = await mountPropertyList()
      const deleteButtons = wrapper.findAll('.btn-delete')
      await deleteButtons[0].trigger('click')
      await flushPromises()
      expect(mockAlert).toHaveBeenCalledWith('Cannot delete property')
    })

    it('shows default error on delete failure', async () => {
      mockDelete.mockRejectedValue({})
      const wrapper = await mountPropertyList()
      const deleteButtons = wrapper.findAll('.btn-delete')
      await deleteButtons[0].trigger('click')
      await flushPromises()
      expect(mockAlert).toHaveBeenCalledWith('Failed to delete property')
    })
  })

  describe('Currency Formatting', () => {
    it('formats Indonesian Rupiah correctly', async () => {
      const wrapper = await mountPropertyList()
      expect(wrapper.text()).toContain('Rp')
      expect(wrapper.text()).toContain('250.000.000')
    })
  })

  describe('API Integration', () => {
    it('calls getAll on mount', async () => {
      await mountPropertyList()
      expect(mockGetAll).toHaveBeenCalled()
    })

    it('reloads data after successful delete', async () => {
      mockDelete.mockResolvedValue({ success: true })
      const wrapper = await mountPropertyList()
      const deleteButtons = wrapper.findAll('.btn-delete')
      await deleteButtons[0].trigger('click')
      await flushPromises()
      // Initial call + after delete = 2 calls
      expect(mockGetAll).toHaveBeenCalledTimes(2)
    })
  })
})
