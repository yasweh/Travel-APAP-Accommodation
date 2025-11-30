import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import BookingCard from '@/components/BookingCard.vue'

describe('BookingCard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const createBooking = (type: string, overrides = {}) => {
    const baseBooking = {
      id: 'booking-123',
      bookingId: 'booking-123',
      ...overrides
    }

    switch (type) {
      case 'ACCOMMODATION':
        return {
          ...baseBooking,
          roomName: 'Deluxe Room',
          checkInDate: '2024-01-15',
          checkOutDate: '2024-01-20',
          numGuests: 2
        }
      case 'FLIGHT':
        return {
          ...baseBooking,
          flightNumber: 'GA-123',
          departureCity: 'Jakarta',
          arrivalCity: 'Bali',
          departureDate: '2024-01-15'
        }
      case 'RENTAL':
        return {
          ...baseBooking,
          vehicleName: 'Toyota Avanza',
          pickupDate: '2024-01-15',
          returnDate: '2024-01-20',
          pickupLocation: 'Airport'
        }
      case 'TOUR':
        return {
          ...baseBooking,
          packageName: 'Bali Tour Package',
          tourDate: '2024-01-15',
          duration: 3,
          participants: 4
        }
      case 'INSURANCE':
        return {
          ...baseBooking,
          policyName: 'Travel Insurance Gold',
          coverageType: 'Full Coverage',
          startDate: '2024-01-15',
          endDate: '2024-01-20'
        }
      default:
        return baseBooking
    }
  }

  const mountBookingCard = (booking: any, serviceType: any) => {
    return mount(BookingCard, {
      props: {
        booking,
        serviceType
      }
    })
  }

  describe('Component Rendering', () => {
    it('renders booking card container', () => {
      const wrapper = mountBookingCard(createBooking('ACCOMMODATION'), 'ACCOMMODATION')
      expect(wrapper.find('.booking-card').exists()).toBe(true)
    })

    it('renders booking header', () => {
      const wrapper = mountBookingCard(createBooking('ACCOMMODATION'), 'ACCOMMODATION')
      expect(wrapper.find('.booking-header').exists()).toBe(true)
    })

    it('renders booking icon', () => {
      const wrapper = mountBookingCard(createBooking('ACCOMMODATION'), 'ACCOMMODATION')
      expect(wrapper.find('.booking-icon').exists()).toBe(true)
    })

    it('renders booking info', () => {
      const wrapper = mountBookingCard(createBooking('ACCOMMODATION'), 'ACCOMMODATION')
      expect(wrapper.find('.booking-info').exists()).toBe(true)
    })

    it('renders booking details', () => {
      const wrapper = mountBookingCard(createBooking('ACCOMMODATION'), 'ACCOMMODATION')
      expect(wrapper.find('.booking-details').exists()).toBe(true)
    })

    it('renders booking footer', () => {
      const wrapper = mountBookingCard(createBooking('ACCOMMODATION'), 'ACCOMMODATION')
      expect(wrapper.find('.booking-footer').exists()).toBe(true)
    })

    it('renders create ticket button', () => {
      const wrapper = mountBookingCard(createBooking('ACCOMMODATION'), 'ACCOMMODATION')
      expect(wrapper.find('.btn-create-ticket').exists()).toBe(true)
      expect(wrapper.find('.btn-create-ticket').text()).toContain('Create Support Ticket')
    })
  })

  describe('Accommodation Type', () => {
    it('renders accommodation details correctly', () => {
      const booking = createBooking('ACCOMMODATION')
      const wrapper = mountBookingCard(booking, 'ACCOMMODATION')
      
      expect(wrapper.text()).toContain('Room:')
      expect(wrapper.text()).toContain('Deluxe Room')
      expect(wrapper.text()).toContain('Check-in:')
      expect(wrapper.text()).toContain('Check-out:')
      expect(wrapper.text()).toContain('Guests:')
      expect(wrapper.text()).toContain('2')
    })

    it('renders correct title for accommodation', () => {
      const booking = createBooking('ACCOMMODATION')
      const wrapper = mountBookingCard(booking, 'ACCOMMODATION')
      
      expect(wrapper.find('h3').text()).toBe('Deluxe Room')
    })

    it('renders booking ID', () => {
      const booking = createBooking('ACCOMMODATION')
      const wrapper = mountBookingCard(booking, 'ACCOMMODATION')
      
      expect(wrapper.find('.booking-id').text()).toContain('booking-123')
    })

    it('formats dates correctly', () => {
      const booking = createBooking('ACCOMMODATION')
      const wrapper = mountBookingCard(booking, 'ACCOMMODATION')
      
      expect(wrapper.text()).toContain('Jan 15, 2024')
      expect(wrapper.text()).toContain('Jan 20, 2024')
    })
  })

  describe('Flight Type', () => {
    it('renders flight details correctly', () => {
      const booking = createBooking('FLIGHT')
      const wrapper = mountBookingCard(booking, 'FLIGHT')
      
      expect(wrapper.text()).toContain('Flight:')
      expect(wrapper.text()).toContain('GA-123')
      expect(wrapper.text()).toContain('From:')
      expect(wrapper.text()).toContain('Jakarta')
      expect(wrapper.text()).toContain('To:')
      expect(wrapper.text()).toContain('Bali')
      expect(wrapper.text()).toContain('Date:')
    })

    it('renders correct title for flight', () => {
      const booking = createBooking('FLIGHT')
      const wrapper = mountBookingCard(booking, 'FLIGHT')
      
      expect(wrapper.find('h3').text()).toBe('Jakarta → Bali')
    })
  })

  describe('Rental Type', () => {
    it('renders rental details correctly', () => {
      const booking = createBooking('RENTAL')
      const wrapper = mountBookingCard(booking, 'RENTAL')
      
      expect(wrapper.text()).toContain('Vehicle:')
      expect(wrapper.text()).toContain('Toyota Avanza')
      expect(wrapper.text()).toContain('Pick-up:')
      expect(wrapper.text()).toContain('Return:')
      expect(wrapper.text()).toContain('Location:')
      expect(wrapper.text()).toContain('Airport')
    })

    it('renders correct title for rental', () => {
      const booking = createBooking('RENTAL')
      const wrapper = mountBookingCard(booking, 'RENTAL')
      
      expect(wrapper.find('h3').text()).toBe('Toyota Avanza')
    })
  })

  describe('Tour Type', () => {
    it('renders tour details correctly', () => {
      const booking = createBooking('TOUR')
      const wrapper = mountBookingCard(booking, 'TOUR')
      
      expect(wrapper.text()).toContain('Package:')
      expect(wrapper.text()).toContain('Bali Tour Package')
      expect(wrapper.text()).toContain('Date:')
      expect(wrapper.text()).toContain('Duration:')
      expect(wrapper.text()).toContain('3 days')
      expect(wrapper.text()).toContain('Participants:')
      expect(wrapper.text()).toContain('4')
    })

    it('renders correct title for tour', () => {
      const booking = createBooking('TOUR')
      const wrapper = mountBookingCard(booking, 'TOUR')
      
      expect(wrapper.find('h3').text()).toBe('Bali Tour Package')
    })
  })

  describe('Insurance Type', () => {
    it('renders insurance details correctly', () => {
      const booking = createBooking('INSURANCE')
      const wrapper = mountBookingCard(booking, 'INSURANCE')
      
      expect(wrapper.text()).toContain('Policy:')
      expect(wrapper.text()).toContain('Travel Insurance Gold')
      expect(wrapper.text()).toContain('Coverage:')
      expect(wrapper.text()).toContain('Full Coverage')
      expect(wrapper.text()).toContain('Start:')
      expect(wrapper.text()).toContain('End:')
    })

    it('renders correct title for insurance', () => {
      const booking = createBooking('INSURANCE')
      const wrapper = mountBookingCard(booking, 'INSURANCE')
      
      expect(wrapper.find('h3').text()).toBe('Travel Insurance Gold')
    })
  })

  describe('Event Emission', () => {
    it('emits create-ticket event when button is clicked', async () => {
      const booking = createBooking('ACCOMMODATION')
      const wrapper = mountBookingCard(booking, 'ACCOMMODATION')
      
      await wrapper.find('.btn-create-ticket').trigger('click')
      
      expect(wrapper.emitted('create-ticket')).toBeTruthy()
      expect(wrapper.emitted('create-ticket')![0]).toEqual([booking, 'ACCOMMODATION'])
    })
  })

  describe('Fallback Values', () => {
    it('shows N/A for missing accommodation room name', () => {
      const booking = { id: '123' }
      const wrapper = mountBookingCard(booking, 'ACCOMMODATION')
      expect(wrapper.text()).toContain('N/A')
    })

    it('shows 0 for missing guests count', () => {
      const booking = { id: '123', roomName: 'Room' }
      const wrapper = mountBookingCard(booking, 'ACCOMMODATION')
      expect(wrapper.text()).toContain('0')
    })

    it('shows fallback title for missing data', () => {
      const booking = { id: '123' }
      const wrapper = mountBookingCard(booking, 'ACCOMMODATION')
      expect(wrapper.find('h3').text()).toBe('Accommodation Booking')
    })

    it('shows N/A for invalid date', () => {
      const booking = { id: '123', checkInDate: '' }
      const wrapper = mountBookingCard(booking, 'ACCOMMODATION')
      expect(wrapper.text()).toContain('N/A')
    })

    it('uses bookingId when id is missing', () => {
      const booking = { bookingId: 'alt-123' }
      const wrapper = mountBookingCard(booking, 'ACCOMMODATION')
      expect(wrapper.find('.booking-id').text()).toContain('alt-123')
    })

    it('shows Unknown for missing both id and bookingId', () => {
      const booking = {}
      const wrapper = mountBookingCard(booking, 'ACCOMMODATION')
      expect(wrapper.find('.booking-id').text()).toContain('Unknown')
    })
  })

  describe('Icon Classes', () => {
    it('applies correct icon class for ACCOMMODATION', () => {
      const wrapper = mountBookingCard(createBooking('ACCOMMODATION'), 'ACCOMMODATION')
      expect(wrapper.find('.booking-icon').classes()).toContain('icon-ACCOMMODATION')
    })

    it('applies correct icon class for FLIGHT', () => {
      const wrapper = mountBookingCard(createBooking('FLIGHT'), 'FLIGHT')
      expect(wrapper.find('.booking-icon').classes()).toContain('icon-FLIGHT')
    })

    it('applies correct icon class for RENTAL', () => {
      const wrapper = mountBookingCard(createBooking('RENTAL'), 'RENTAL')
      expect(wrapper.find('.booking-icon').classes()).toContain('icon-RENTAL')
    })

    it('applies correct icon class for TOUR', () => {
      const wrapper = mountBookingCard(createBooking('TOUR'), 'TOUR')
      expect(wrapper.find('.booking-icon').classes()).toContain('icon-TOUR')
    })

    it('applies correct icon class for INSURANCE', () => {
      const wrapper = mountBookingCard(createBooking('INSURANCE'), 'INSURANCE')
      expect(wrapper.find('.booking-icon').classes()).toContain('icon-INSURANCE')
    })
  })

  describe('Detail Grid', () => {
    it('renders detail grid for all service types', () => {
      const types = ['ACCOMMODATION', 'FLIGHT', 'RENTAL', 'TOUR', 'INSURANCE'] as const
      
      types.forEach(type => {
        const wrapper = mountBookingCard(createBooking(type), type)
        expect(wrapper.find('.detail-grid').exists()).toBe(true)
      })
    })

    it('renders correct number of detail items', () => {
      const wrapper = mountBookingCard(createBooking('ACCOMMODATION'), 'ACCOMMODATION')
      expect(wrapper.findAll('.detail-item').length).toBe(4)
    })
  })
})
