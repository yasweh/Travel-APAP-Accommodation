import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import HomeView from '@/views/HomeView.vue'

// Mock vue-router
const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock services
const mockPropertyGetAll = vi.fn()
const mockBookingGetAll = vi.fn()

vi.mock('@/services/propertyService', () => ({
  propertyService: {
    getAll: () => mockPropertyGetAll()
  }
}))

vi.mock('@/services/bookingService', () => ({
  bookingService: {
    getAll: () => mockBookingGetAll()
  }
}))

describe('HomeView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    
    // Default mock responses
    mockPropertyGetAll.mockResolvedValue({
      success: true,
      data: [{ id: '1' }, { id: '2' }, { id: '3' }]
    })
    
    mockBookingGetAll.mockResolvedValue({
      success: true,
      data: [{ id: '1' }, { id: '2' }]
    })
  })

  const mountHomeView = async () => {
    const wrapper = mount(HomeView, {
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
    it('renders homepage container', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.homepage').exists()).toBe(true)
    })

    it('renders hero section', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.hero-section').exists()).toBe(true)
    })

    it('renders hero background', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.hero-bg').exists()).toBe(true)
    })

    it('renders brand name', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.brand-name').text()).toBe('Travel Apap Accommodation')
    })

    it('renders hero title', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.hero-title').text()).toBe('Hotel for every moment rich in emotion')
    })

    it('renders hero subtitle', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.hero-subtitle').text()).toContain('Every moment feels like the first time')
    })

    it('renders hero image', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.hero-image').exists()).toBe(true)
    })

    it('renders Book now button in hero', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.book-btn-hero').exists()).toBe(true)
      expect(wrapper.find('.btn-text-hero').text()).toBe('Book now')
    })
  })

  describe('Statistics Section', () => {
    it('renders quick booking section', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.quick-booking').exists()).toBe(true)
    })

    it('renders booking controls', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.booking-controls').exists()).toBe(true)
    })

    it('renders stat items', async () => {
      const wrapper = await mountHomeView()
      const statItems = wrapper.findAll('.stat-item')
      expect(statItems.length).toBe(3)
    })

    it('shows correct properties count', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Properties')
      expect(wrapper.text()).toContain('3')
    })

    it('shows correct bookings count', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Bookings')
      expect(wrapper.text()).toContain('2')
    })

    it('calculates room types estimate', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Room Types')
      expect(wrapper.text()).toContain('9') // 3 properties * 3
    })

    it('shows loading state initially', async () => {
      mockPropertyGetAll.mockImplementation(() => new Promise(() => {}))
      const wrapper = mount(HomeView, {
        global: {
          stubs: {
            RouterLink: true
          }
        }
      })
      expect(wrapper.text()).toContain('...')
    })

    it('renders Book Now button in stats section', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.book-now-btn').exists()).toBe(true)
      expect(wrapper.find('.book-now-text').text()).toBe('Book Now')
    })
  })

  describe('Facilities Section', () => {
    it('renders facilities title', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.facilities-title').text()).toBe('Our Facilities')
    })

    it('renders facilities subtitle', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.facilities-subtitle').text()).toContain('modern (5 star) hotel facilities')
    })

    it('renders facility rows', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.findAll('.facilities-row').length).toBe(2)
    })

    it('renders facility cards', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.findAll('.facility-card').length).toBe(8)
    })

    it('renders swimming pool facility', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Swimming Pool')
    })

    it('renders wifi facility', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Wifi')
    })

    it('renders breakfast facility', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Breakfast')
    })

    it('renders gym facility', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Gym')
    })

    it('renders game center facility', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Game center')
    })

    it('renders 24/7 Light facility', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('24/7 Light')
    })

    it('renders laundry facility', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Laundry')
    })

    it('renders parking space facility', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Parking space')
    })
  })

  describe('Rooms Section', () => {
    it('renders rooms section background', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.rooms-bg').exists()).toBe(true)
    })

    it('renders rooms title', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.rooms-title').text()).toBe('Luxurious Rooms')
    })

    it('renders rooms subtitle', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.rooms-subtitle').text()).toBe('All room are design for your comfort')
    })

    it('renders rooms divider', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.rooms-divider').exists()).toBe(true)
    })

    it('renders rooms grid', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.rooms-grid').exists()).toBe(true)
    })

    it('renders room cards', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.findAll('.room-card').length).toBe(3)
    })

    it('renders room images', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.findAll('.room-image').length).toBe(3)
    })

    it('renders availability buttons', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.findAll('.avail-btn').length).toBe(3)
    })

    it('renders room descriptions', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.findAll('.room-description').length).toBe(3)
    })
  })

  describe('Testimonials Section', () => {
    it('renders testimonials section', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.testimonials-section').exists()).toBe(true)
    })

    it('renders testimonials title', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.testimonials-title').text()).toBe('Testimonies')
    })

    it('renders testimonials grid', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.find('.testimonials-grid').exists()).toBe(true)
    })

    it('renders testimonial cards', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.findAll('.testimonial-card').length).toBe(3)
    })

    it('renders testimonial ratings', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.findAll('.testimonial-rating').length).toBe(3)
    })

    it('renders testimonial texts', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.findAll('.testimonial-text').length).toBe(3)
    })

    it('renders testimonial authors', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.findAll('.testimonial-author').length).toBe(3)
    })

    it('shows John Smith testimonial', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('John Smith')
    })

    it('shows Sarah Johnson testimonial', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Sarah Johnson')
    })

    it('shows Michael Brown testimonial', async () => {
      const wrapper = await mountHomeView()
      expect(wrapper.text()).toContain('Michael Brown')
    })
  })

  describe('Navigation', () => {
    it('navigates to booking create when hero Book now is clicked', async () => {
      const wrapper = await mountHomeView()
      await wrapper.find('.book-btn-hero').trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/booking/create')
    })

    it('navigates to property when properties stat is clicked', async () => {
      const wrapper = await mountHomeView()
      const statItems = wrapper.findAll('.stat-item')
      await statItems[0].trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/property')
    })

    it('navigates to booking when bookings stat is clicked', async () => {
      const wrapper = await mountHomeView()
      const statItems = wrapper.findAll('.stat-item')
      await statItems[1].trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/booking')
    })

    it('navigates to room-type when room types stat is clicked', async () => {
      const wrapper = await mountHomeView()
      const statItems = wrapper.findAll('.stat-item')
      await statItems[2].trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/room-type')
    })

    it('navigates to booking create when Book Now button is clicked', async () => {
      const wrapper = await mountHomeView()
      await wrapper.find('.book-now-btn').trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/booking/create')
    })

    it('navigates to room-type when room card is clicked', async () => {
      const wrapper = await mountHomeView()
      const roomCards = wrapper.findAll('.room-card')
      await roomCards[0].trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/room-type')
    })

    it('navigates to property when facility card is clicked', async () => {
      const wrapper = await mountHomeView()
      const facilityCards = wrapper.findAll('.facility-card')
      await facilityCards[0].trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/property')
    })
  })

  describe('Error Handling', () => {
    it('handles property service error gracefully', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      mockPropertyGetAll.mockRejectedValue(new Error('Network error'))
      
      const wrapper = await mountHomeView()
      
      expect(wrapper.find('.homepage').exists()).toBe(true)
      consoleErrorSpy.mockRestore()
    })

    it('handles booking service error gracefully', async () => {
      mockBookingGetAll.mockRejectedValue(new Error('Network error'))
      
      const wrapper = await mountHomeView()
      
      expect(wrapper.find('.homepage').exists()).toBe(true)
    })

    it('sets bookings to 0 when booking service fails', async () => {
      mockBookingGetAll.mockRejectedValue(new Error('Network error'))
      
      const wrapper = await mountHomeView()
      
      expect(wrapper.text()).toContain('0')
    })
  })

  describe('API Integration', () => {
    it('calls propertyService.getAll on mount', async () => {
      await mountHomeView()
      expect(mockPropertyGetAll).toHaveBeenCalled()
    })

    it('calls bookingService.getAll on mount', async () => {
      await mountHomeView()
      expect(mockBookingGetAll).toHaveBeenCalled()
    })

    it('handles unsuccessful property response', async () => {
      mockPropertyGetAll.mockResolvedValue({
        success: false,
        data: []
      })
      
      const wrapper = await mountHomeView()
      
      expect(wrapper.text()).toContain('0')
    })

    it('handles unsuccessful booking response', async () => {
      mockBookingGetAll.mockResolvedValue({
        success: false,
        data: []
      })
      
      const wrapper = await mountHomeView()
      
      expect(wrapper.text()).toContain('0')
    })
  })
})
