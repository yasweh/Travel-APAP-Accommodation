<template>
  <div class="support-page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-content">
        <h1>🎫 Support Center</h1>
        <p class="subtitle">Manage support tickets for all your bookings across services</p>
      </div>
    </div>

    <!-- Filter Section -->
    <div class="filter-section">
      <div class="filter-header">
        <h3>📋 All Bookings</h3>
        <div class="filter-controls">
          <label for="sourceFilter">Service:</label>
          <select id="sourceFilter" v-model="selectedSource" @change="filterBookings" class="select-input">
            <option value="">All Services ({{ bookings.length }})</option>
            <option value="tour">🌍 Tour Package</option>
            <option value="rental">🚗 Vehicle Rental</option>
            <option value="insurance">🛡️ Insurance Policy</option>
            <option value="accommodation">🏨 Accommodation</option>
            <option value="flight">✈️ Flight Booking</option>
          </select>
        </div>
      </div>
      
      <div class="summary" v-if="bookingsSummary">
        <span v-for="(count, source) in bookingsSummary" :key="source" 
              class="summary-badge" 
              :class="`badge-${source}`"
              @click="selectedSource = source; filterBookings()">
          <span class="badge-icon">{{ getSourceIcon(source) }}</span>
          <span class="badge-text">{{ capitalizeSource(source) }}</span>
          <span class="badge-count">{{ count }}</span>
        </span>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Loading bookings from all services...</p>
    </div>

    <!-- Error State -->
    <div v-if="error" class="error-state">
      <p>❌ {{ error }}</p>
      <button @click="fetchBookings" class="btn-retry">Retry</button>
    </div>

    <!-- Bookings Grid -->
    <div v-if="!loading && !error" class="bookings-section">
      <div class="section-header">
        <h2>Bookings ({{ filteredBookings.length }})</h2>
        <p v-if="selectedSource" class="filter-active">
          Filtered by: {{ capitalizeSource(selectedSource) }}
        </p>
      </div>
      
      <div v-if="filteredBookings.length === 0" class="no-data">
        <div class="no-data-icon">📭</div>
        <p>No bookings found{{ selectedSource ? ' for ' + capitalizeSource(selectedSource) : '' }}</p>
      </div>

      <div v-else class="bookings-grid">
        <div v-for="booking in filteredBookings" 
             :key="`${booking.source}-${booking.id}`" 
             class="booking-card">
          
          <!-- Card Header -->
          <div class="card-header">
            <span class="service-badge" :class="`badge-${booking.source}`">
              {{ getSourceIcon(booking.source) }} {{ capitalizeSource(booking.source) }}
            </span>
            <span class="booking-id">ID: {{ booking.id }}</span>
          </div>

          <!-- Card Body -->
          <div class="card-body">
            <h3 class="service-name">{{ booking.serviceName || 'N/A' }}</h3>
            <p class="description">{{ booking.description || 'No description' }}</p>
            
            <div class="booking-info">
              <div class="info-row" v-if="booking.startDate">
                <span class="label">📅 Start:</span>
                <span class="value">{{ formatDate(booking.startDate) }}</span>
              </div>
              <div class="info-row" v-if="booking.endDate">
                <span class="label">📅 End:</span>
                <span class="value">{{ formatDate(booking.endDate) }}</span>
              </div>
              <div class="info-row" v-if="booking.quota && booking.source === 'tour'">
                <span class="label">👥 Quota:</span>
                <span class="value">{{ booking.quota }} person(s)</span>
              </div>
              <div class="info-row" v-if="booking.totalPrice || booking.price">
                <span class="label">💰 Price:</span>
                <span class="value">{{ formatPrice(booking.totalPrice || booking.price) }}</span>
              </div>
              <div class="info-row" v-if="booking.status">
                <span class="label">Status:</span>
                <span class="status-badge" :class="getStatusClass(booking.status)">
                  {{ booking.status }}
                </span>
              </div>
            </div>
          </div>

          <!-- Card Actions -->
          <div class="card-actions">
            <button 
              class="btn-create-ticket" 
              @click="openCreateTicketModal(booking)"
              title="Create Support Ticket">
              🎫 Create Support Ticket
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Ticket Modal -->
    <div v-if="showTicketModal" class="modal-overlay" @click="closeTicketModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>📝 Create Support Ticket</h2>
          <button class="btn-close" @click="closeTicketModal">×</button>
        </div>
        
        <div class="modal-body">
          <!-- Auto-filled Booking Info -->
          <div class="booking-preview">
            <h4>📦 Booking Information</h4>
            <div class="preview-grid">
              <div class="preview-item">
                <label>Service:</label>
                <span class="service-badge" :class="`badge-${selectedBooking?.source}`">
                  {{ getSourceIcon(selectedBooking?.source || '') }} {{ capitalizeSource(selectedBooking?.source || '') }}
                </span>
              </div>
              <div class="preview-item">
                <label>Booking ID:</label>
                <span class="mono">{{ selectedBooking?.id }}</span>
              </div>
              <div class="preview-item">
                <label>Service Name:</label>
                <span>{{ selectedBooking?.serviceName }}</span>
              </div>
              <div class="preview-item">
                <label>Status:</label>
                <span class="status-badge" :class="`status-${selectedBooking?.status?.toLowerCase()}`">
                  {{ selectedBooking?.status }}
                </span>
              </div>
            </div>
            <p class="auto-fill-note">ℹ️ Booking details will be automatically linked to this ticket</p>
          </div>

          <!-- Ticket Form -->
          <form @submit.prevent="createTicket" class="ticket-form">
            <div class="form-group">
              <label for="subject">
                Subject / Issue Title <span class="required">*</span>
              </label>
              <input 
                type="text" 
                id="subject" 
                v-model="ticketForm.subject" 
                required
                maxlength="200"
                placeholder="Brief description of your issue (e.g., 'Payment not processed', 'Need to change date')"
                class="form-input">
              <small class="char-count">{{ ticketForm.subject.length }}/200 characters</small>
            </div>

            <div class="form-info">
              <p><strong>What happens next?</strong></p>
              <ul>
                <li>✅ Your ticket will be created with a unique ID</li>
                <li>✅ You can add detailed messages in the ticket thread</li>
                <li>✅ Admin will respond through ticket progress updates</li>
                <li>✅ You'll receive updates until the ticket is resolved</li>
              </ul>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeTicketModal" class="btn-cancel">
                Cancel
              </button>
              <button type="submit" class="btn-submit" :disabled="submitting || !ticketForm.subject.trim()">
                {{ submitting ? '⏳ Creating...' : '✨ Create Ticket' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'

const router = useRouter()

// Types
interface UnifiedBooking {
  id: string
  source: string
  userId?: string
  customerId?: string
  serviceName?: string
  description?: string
  startDate?: string
  endDate?: string
  price?: number
  totalPrice?: number
  quota?: number
  status?: string
  rawData?: any
}

// State
const bookings = ref<UnifiedBooking[]>([])
const selectedSource = ref('')
const loading = ref(false)
const error = ref('')
const bookingsSummary = ref<Record<string, number>>({})

// Ticket Modal
const showTicketModal = ref(false)
const selectedBooking = ref<UnifiedBooking | null>(null)
const submitting = ref(false)
const ticketForm = ref({
  subject: ''
})

// Computed
const filteredBookings = computed(() => {
  if (!selectedSource.value) {
    return bookings.value
  }
  return bookings.value.filter(b => b.source === selectedSource.value)
})

// Methods
const fetchBookings = async () => {
  loading.value = true
  error.value = ''
  
  console.log('========== FETCHING BOOKINGS FOR SUPPORT ==========')
  console.log('Auth token:', localStorage.getItem('auth_token') ? 'EXISTS' : 'NULL')
  console.log('Auth user:', localStorage.getItem('auth_user'))
  
  try {
    console.log('Making request to /support/bookings...')
    const response = await api.get('/support/bookings')
    
    console.log('========== RESPONSE RECEIVED ==========')
    console.log('Full response:', response)
    console.log('Response status:', response.status)
    console.log('Response data:', response.data)
    console.log('Success:', response.data.success)
    console.log('Total bookings:', response.data.total)
    console.log('Summary by source:', response.data.summaryBySource)
    console.log('Current role:', response.data.currentRole)
    console.log('Current userId:', response.data.currentUserId)
    console.log('Bookings data:', response.data.data)
    
    if (response.data.success) {
      bookings.value = response.data.data
      bookingsSummary.value = response.data.summaryBySource
      console.log('Bookings set to state:', bookings.value.length, 'items')
    } else {
      error.value = response.data.message || 'Failed to fetch bookings'
      console.error('API returned success=false:', response.data.message)
    }
  } catch (err: any) {
    console.error('========== ERROR FETCHING BOOKINGS ==========')
    console.error('Error object:', err)
    console.error('Error response:', err.response)
    console.error('Error response data:', err.response?.data)
    console.error('Error message:', err.message)
    error.value = err.response?.data?.message || 'Failed to fetch bookings from services'
  } finally {
    loading.value = false
  }
}

const filterBookings = () => {
  // Trigger recompute of filteredBookings
}

const getSourceIcon = (source: string) => {
  const icons: Record<string, string> = {
    'tour': '🌍',
    'rental': '🚗',
    'insurance': '🛡️',
    'accommodation': '🏨',
    'flight': '✈️'
  }
  return icons[source] || '📦'
}

const capitalizeSource = (source: string) => {
  const names: Record<string, string> = {
    'tour': 'Tour Package',
    'rental': 'Vehicle Rental',
    'insurance': 'Insurance',
    'accommodation': 'Accommodation',
    'flight': 'Flight'
  }
  return names[source] || source.charAt(0).toUpperCase() + source.slice(1)
}

const formatPrice = (price: number | null | undefined) => {
  if (!price) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format(price)
}

const getStatusClass = (status: string | undefined) => {
  if (!status) return ''
  const statusLower = status.toLowerCase()
  // Map various statuses to CSS classes
  const statusMap: Record<string, string> = {
    'waiting': 'status-waiting',
    'pending': 'status-pending',
    'confirmed': 'status-confirmed',
    'active': 'status-active',
    'done': 'status-done',
    'completed': 'status-completed',
    'cancelled': 'status-cancelled',
    'canceled': 'status-cancelled',
    'inactive': 'status-inactive'
  }
  return statusMap[statusLower] || `status-${statusLower}`
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return 'N/A'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('id-ID', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch {
    return dateStr
  }
}

const openCreateTicketModal = (booking: UnifiedBooking) => {
  selectedBooking.value = booking
  ticketForm.value = {
    subject: ''
  }
  showTicketModal.value = true
}

const closeTicketModal = () => {
  showTicketModal.value = false
  selectedBooking.value = null
  ticketForm.value = {
    subject: ''
  }
}

const createTicket = async () => {
  if (!selectedBooking.value || !ticketForm.value.subject.trim()) return
  
  submitting.value = true
  
  try {
    // Map source to ServiceSource enum
    const sourceMapping: Record<string, string> = {
      'tour': 'TOUR_PACKAGE',
      'rental': 'VEHICLE_RENTAL',
      'insurance': 'INSURANCE',
      'accommodation': 'ACCOMMODATION',
      'flight': 'FLIGHT'
    }
    
    const payload = {
      externalServiceSource: sourceMapping[selectedBooking.value.source],
      externalBookingId: selectedBooking.value.id,
      subject: ticketForm.value.subject.trim(),
      description: `Support ticket created for ${capitalizeSource(selectedBooking.value.source)} booking: ${selectedBooking.value.serviceName || selectedBooking.value.id}`,
      priority: 'MEDIUM',
      category: 'BOOKING_ISSUE'
    }
    
    const response = await api.post('/support-tickets', payload)
    
    if (response.data && response.data.ticketId) {
      alert(`✅ Support ticket created successfully!\n\nTicket ID: ${response.data.ticketId}\n\nRedirecting to ticket thread...`)
      closeTicketModal()
      // Navigate to ticket detail page
      router.push(`/support/${response.data.ticketId}`)
    } else {
      alert('Failed to create ticket')
    }
  } catch (err: any) {
    const errorMsg = err.response?.data?.message || err.message || 'Unknown error'
    alert('❌ Failed to create ticket:\n' + errorMsg)
    console.error('Error creating ticket:', err)
  } finally {
    submitting.value = false
  }
}

// Lifecycle
onMounted(() => {
  fetchBookings()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

.support-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 140px 20px 40px;
  font-family: 'Poppins', sans-serif;
}

/* Header */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.header-content h1 {
  margin: 0 0 10px 0;
  font-size: 32px;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
  font-weight: 400;
}

/* Filter Section */
.filter-section {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  margin-bottom: 25px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
}

.filter-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-controls label {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.select-input {
  padding: 10px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  font-family: 'Poppins', sans-serif;
  min-width: 200px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.select-input:hover {
  border-color: #667eea;
}

.select-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.summary-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.summary-badge:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.badge-tour { background: #e3f2fd; color: #1565c0; border-color: #1565c0; }
.badge-rental { background: #f3e5f5; color: #6a1b9a; border-color: #6a1b9a; }
.badge-insurance { background: #fff3e0; color: #e65100; border-color: #e65100; }
.badge-accommodation { background: #e0f2f1; color: #00695c; border-color: #00695c; }
.badge-flight { background: #ffebee; color: #c62828; border-color: #c62828; }

.badge-icon {
  font-size: 16px;
}

.badge-count {
  background: rgba(255,255,255,0.9);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 700;
}

/* Loading & Error States */
.loading-state, .error-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-state {
  color: #c62828;
}

.btn-retry {
  margin-top: 15px;
  padding: 10px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s ease;
}

.btn-retry:hover {
  background: #5568d3;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* Bookings Section */
.bookings-section {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.section-header {
  margin-bottom: 25px;
}

.section-header h2 {
  margin: 0 0 5px 0;
  color: #2c3e50;
  font-size: 24px;
  font-weight: 700;
}

.filter-active {
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
  margin: 0;
}

.no-data {
  text-align: center;
  padding: 60px 20px;
}

.no-data-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.no-data p {
  color: #7f8c8d;
  font-size: 16px;
}

/* Bookings Grid */
.bookings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.booking-card {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.booking-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.12);
  border-color: #667eea;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-bottom: 2px solid #e0e0e0;
}

.service-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.booking-id {
  font-family: 'Courier New', monospace;
  font-size: 11px;
  color: #7f8c8d;
  font-weight: 600;
}

.card-body {
  padding: 20px;
}

.service-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.description {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #7f8c8d;
  line-height: 1.5;
}

.booking-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.info-row .label {
  color: #7f8c8d;
  font-weight: 500;
}

.info-row .value {
  color: #2c3e50;
  font-weight: 600;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-waiting { background: #fff3cd; color: #856404; }
.status-pending { background: #fff3cd; color: #856404; }
.status-confirmed { background: #d1ecf1; color: #0c5460; }
.status-done { background: #d4edda; color: #155724; }
.status-completed { background: #d4edda; color: #155724; }
.status-cancelled { background: #f8d7da; color: #721c24; }
.status-active { background: #d1ecf1; color: #0c5460; }
.status-inactive { background: #e2e3e5; color: #383d41; }

.card-actions {
  padding: 16px;
  background: #f8f9fa;
  border-top: 2px solid #e0e0e0;
}

.btn-create-ticket {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s ease;
}

.btn-create-ticket:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 650px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 30px;
  border-bottom: 2px solid #e0e0e0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px 16px 0 0;
}

.modal-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.btn-close {
  background: rgba(255,255,255,0.2);
  border: none;
  color: white;
  font-size: 32px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  line-height: 1;
  padding: 0;
}

.btn-close:hover {
  background: rgba(255,255,255,0.3);
  transform: rotate(90deg);
}

.modal-body {
  padding: 30px;
}

.booking-preview {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 25px;
  border: 2px solid #e0e0e0;
}

.booking-preview h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.preview-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.preview-item label {
  font-size: 12px;
  color: #7f8c8d;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.preview-item span {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
}

.preview-item .mono {
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.auto-fill-note {
  margin: 12px 0 0 0;
  font-size: 13px;
  color: #667eea;
  font-weight: 500;
}

.ticket-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.required {
  color: #c62828;
}

.form-input {
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.char-count {
  font-size: 12px;
  color: #7f8c8d;
  text-align: right;
}

.form-info {
  background: #e3f2fd;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #1565c0;
}

.form-info p {
  margin: 0 0 8px 0;
  font-weight: 600;
  color: #1565c0;
}

.form-info ul {
  margin: 0;
  padding-left: 20px;
  list-style: none;
}

.form-info li {
  font-size: 13px;
  color: #0c5460;
  margin-bottom: 6px;
  position: relative;
  padding-left: 8px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 10px;
}

.btn-cancel, .btn-submit {
  padding: 12px 28px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: #e0e0e0;
  color: #2c3e50;
}

.btn-cancel:hover {
  background: #bdbdbd;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .bookings-grid {
    grid-template-columns: 1fr;
  }
  
  .preview-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .summary {
    width: 100%;
  }
}
</style>
