import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Footer from '@/components/Footer.vue'

// Mock vue-router
const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

describe('Footer', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const mountFooter = () => {
    return mount(Footer, {
      global: {
        stubs: {
          RouterLink: true
        }
      }
    })
  }

  describe('Component Rendering', () => {
    it('renders footer container', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.footer').exists()).toBe(true)
    })

    it('renders footer background', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.footer-bg').exists()).toBe(true)
    })

    it('renders footer content', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.footer-content').exists()).toBe(true)
    })

    it('renders footer brand name', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.footer-brand').exists()).toBe(true)
      expect(wrapper.find('.footer-brand').text()).toBe('Travel Apap Accommodation')
    })

    it('renders footer description', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.footer-description').exists()).toBe(true)
    })

    it('renders all footer columns', () => {
      const wrapper = mountFooter()
      const columns = wrapper.findAll('.footer-column')
      expect(columns.length).toBe(5)
    })
  })

  describe('Quick Links Section', () => {
    it('renders Quick links heading', () => {
      const wrapper = mountFooter()
      const headings = wrapper.findAll('.footer-heading')
      expect(headings.some(h => h.text() === 'Quick links')).toBe(true)
    })

    it('renders Room booking link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'Room booking')).toBe(true)
    })

    it('renders Rooms link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'Rooms')).toBe(true)
    })

    it('renders Maintenance link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'Maintenance')).toBe(true)
    })

    it('renders Properties link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'Properties')).toBe(true)
    })
  })

  describe('Company Section', () => {
    it('renders Company heading', () => {
      const wrapper = mountFooter()
      const headings = wrapper.findAll('.footer-heading')
      expect(headings.some(h => h.text() === 'Company')).toBe(true)
    })

    it('renders Privacy policy link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'Privacy policy')).toBe(true)
    })

    it('renders Refund policy link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'Refund policy')).toBe(true)
    })

    it('renders F.A.Q link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'F.A.Q')).toBe(true)
    })

    it('renders About link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'About')).toBe(true)
    })
  })

  describe('Social Media Section', () => {
    it('renders Social media heading', () => {
      const wrapper = mountFooter()
      const headings = wrapper.findAll('.footer-heading')
      expect(headings.some(h => h.text() === 'Social media')).toBe(true)
    })

    it('renders Facebook link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'Facebook')).toBe(true)
    })

    it('renders Twitter link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'Twitter')).toBe(true)
    })

    it('renders Instagram link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'Instagram')).toBe(true)
    })

    it('renders Linkedin link', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      expect(links.some(l => l.text() === 'Linkedin')).toBe(true)
    })
  })

  describe('Newsletter Section', () => {
    it('renders Newsletter heading', () => {
      const wrapper = mountFooter()
      const headings = wrapper.findAll('.footer-heading')
      expect(headings.some(h => h.text() === 'Newsletter')).toBe(true)
    })

    it('renders newsletter description text', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.footer-newsletter-text').exists()).toBe(true)
    })

    it('renders newsletter input container', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.newsletter-input').exists()).toBe(true)
    })

    it('renders email input field', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.newsletter-email').exists()).toBe(true)
    })

    it('has correct placeholder on email input', () => {
      const wrapper = mountFooter()
      const input = wrapper.find('.newsletter-email')
      expect(input.attributes('placeholder')).toBe('Enter your email')
    })

    it('renders subscribe button', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.subscribe-btn').exists()).toBe(true)
    })

    it('renders subscribe button text', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.subscribe-text').text()).toBe('Subscribe')
    })
  })

  describe('Navigation', () => {
    it('navigates to booking create when Room booking is clicked', async () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      const roomBookingLink = links.find(l => l.text() === 'Room booking')
      await roomBookingLink?.trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/booking/create')
    })

    it('navigates to room-type when Rooms is clicked', async () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      const roomsLink = links.find(l => l.text() === 'Rooms')
      await roomsLink?.trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/room-type')
    })

    it('navigates to maintenance when Maintenance is clicked', async () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      const maintenanceLink = links.find(l => l.text() === 'Maintenance')
      await maintenanceLink?.trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/maintenance')
    })

    it('navigates to property when Properties is clicked', async () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('.footer-link')
      const propertiesLink = links.find(l => l.text() === 'Properties')
      await propertiesLink?.trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/property')
    })
  })

  describe('Email Input', () => {
    it('accepts email input', async () => {
      const wrapper = mountFooter()
      const input = wrapper.find('.newsletter-email')
      await input.setValue('test@example.com')
      expect((input.element as HTMLInputElement).value).toBe('test@example.com')
    })

    it('has type email attribute', () => {
      const wrapper = mountFooter()
      const input = wrapper.find('.newsletter-email')
      expect(input.attributes('type')).toBe('email')
    })
  })

  describe('CSS Classes', () => {
    it('has newsletter input background', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.newsletter-input-bg').exists()).toBe(true)
    })

    it('has subscribe button background', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.subscribe-bg').exists()).toBe(true)
    })
  })
})
