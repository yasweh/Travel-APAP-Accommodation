<template>
  <div class="booking-list">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <path d="M0 34C0 37.4 2.6 40 6 40H34C37.4 40 40 37.4 40 34V18H0V34ZM34 4H30V2C30 0.8 29.2 0 28 0C26.8 0 26 0.8 26 2V4H14V2C14 0.8 13.2 0 12 0C10.8 0 10 0.8 10 2V4H6C2.6 4 0 6.6 0 10V14H40V10C40 6.6 37.4 4 34 4Z" fill="#7C6A46"/>
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">Booking Management</h1>
          <p class="page-subtitle">View and manage all bookings and reservations</p>
        </div>
      </div>
    </div>

    <!-- Error Message -->
    <div v-if="error" class="error-message">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM11 15H9V13H11V15ZM11 11H9V5H11V11Z" fill="#F44336"/>
      </svg>
      <span>{{ error }}</span>
    </div>

    <!-- Actions -->
    <div class="actions">
      <button @click="goToCreateBooking" class="btn-primary">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM15 11H11V15H9V11H5V9H9V5H11V9H15V11Z" fill="white"/>
        </svg>
        Create New Booking
      </button>
      <button @click="goToChart" class="btn-chart">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M18 0H2C0.9 0 0 0.9 0 2V18C0 19.1 0.9 20 2 20H18C19.1 20 20 19.1 20 18V2C20 0.9 19.1 0 18 0ZM8 16H6V8H8V16ZM11 16H9V4H11V16ZM14 16H12V11H14V16Z" fill="white"/>
        </svg>
        View Statistics
      </button>
      <button @click="loadBookings" class="btn-secondary">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4C7.58 4 4.01 7.58 4.01 12C4.01 16.42 7.58 20 12 20C15.73 20 18.84 17.45 19.73 14H17.65C16.83 16.33 14.61 18 12 18C8.69 18 6 15.31 6 12C6 8.69 8.69 6 12 6C13.66 6 15.14 6.69 16.22 7.78L13 11H20V4L17.65 6.35Z" fill="#7C6A46"/>
        </svg>
        Refresh
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>Loading bookings...</p>
    </div>

    <!-- Bookings Grid -->
    <div v-else-if="bookings.length > 0" class="bookings-grid">
      <div v-for="booking in bookings" :key="booking.bookingId" class="booking-card">
        <div class="card-header">
          <div class="booking-id">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 0L0 5V7L10 12L20 7V5L10 0ZM0 9V11L10 16L20 11V9L10 14L0 9Z" fill="#7C6A46"/>
            </svg>
            <span>{{ booking.bookingId }}</span>
          </div>
          <span :class="['status-badge', getStatusClass(booking.status)]">
            {{ booking.statusString }}
          </span>
        </div>
        
        <div class="card-body">
          <div class="booking-property">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M9 0L0 6H3L9 2L15 6H18L9 0ZM4.5 4.5V0H6V6L4.5 4.5Z" fill="#7C6A46"/>
            </svg>
            <div>
              <div class="property-name">{{ booking.propertyName }}</div>
              <div class="room-name">{{ booking.roomName }}</div>
            </div>
          </div>
          
          <div class="booking-customer">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M9 0C6.24 0 4 2.24 4 5C4 7.76 6.24 10 9 10C11.76 10 14 7.76 14 5C14 2.24 11.76 0 9 0ZM9 12C6 12 0 13.5 0 16.5V18H18V16.5C18 13.5 12 12 9 12Z" fill="#666"/>
            </svg>
            <div>
              <div class="customer-name">{{ booking.customerName }}</div>
              <div class="customer-email">{{ booking.customerEmail }}</div>
            </div>
          </div>
          
          <div class="booking-dates">
            <div class="date-item">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M12 2V0H10V2H6V0H4V2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2H12ZM14 14H2V7H14V14Z" fill="#4CAF50"/>
              </svg>
              <div>
                <div class="date-label">Check-in</div>
                <div class="date-value">{{ formatDate(booking.checkInDate) }}</div>
              </div>
            </div>
            <svg width="20" height="16" viewBox="0 0 20 16" fill="none" class="arrow-icon">
              <path d="M12 0L10.59 1.41L16.17 7H0V9H16.17L10.59 14.59L12 16L20 8L12 0Z" fill="#999"/>
            </svg>
            <div class="date-item">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M12 2V0H10V2H6V0H4V2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2H12ZM14 14H2V7H14V14Z" fill="#F44336"/>
              </svg>
              <div>
                <div class="date-label">Check-out</div>
                <div class="date-value">{{ formatDate(booking.checkOutDate) }}</div>
              </div>
            </div>
          </div>
          
          <div class="booking-price">
            <span class="price-label">Total Price</span>
            <span class="price-value">Rp {{ formatCurrency(booking.totalPrice) }}</span>
          </div>
        </div>
        
        <div class="card-actions">
          <button @click="goToDetail(booking.bookingId)" class="btn-action btn-detail">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 3C4.5 3 1.5 5.5 0 9C1.5 12.5 4.5 15 8 15C11.5 15 14.5 12.5 16 9C14.5 5.5 11.5 3 8 3ZM8 13C6.34 13 5 11.66 5 10C5 8.34 6.34 7 8 7C9.66 7 11 8.34 11 10C11 11.66 9.66 13 8 13Z" fill="white"/>
            </svg>
            View Details
          </button>
          <button 
            v-if="canReview(booking)" 
            @click="openReviewModal(booking.bookingId)" 
            class="btn-action btn-review"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M14 0H2C0.9 0 0 0.9 0 2V16L4 12H14C15.1 12 16 11.1 16 10V2C16 0.9 15.1 0 14 0ZM13 9H3V7H13V9ZM13 6H3V4H13V6Z" fill="white"/>
            </svg>
            Write Review
          </button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
        <path d="M0 68C0 74.8 5.2 80 12 80H68C74.8 80 80 74.8 80 68V36H0V68ZM68 8H60V4C60 1.6 58.4 0 56 0C53.6 0 52 1.6 52 4V8H28V4C28 1.6 26.4 0 24 0C21.6 0 20 1.6 20 4V8H12C5.2 8 0 13.2 0 20V28H80V20C80 13.2 74.8 8 68 8Z" fill="#D9D9D9"/>
      </svg>
      <h3>No Bookings Found</h3>
      <p>Start by creating your first booking or reservation</p>
      <button @click="goToCreateBooking" class="btn-empty-state">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM15 11H11V15H9V11H5V9H9V5H11V9H15V11Z" fill="white"/>
        </svg>
        Create First Booking
      </button>
    </div>

    <!-- Review Modal -->
    <CreateReviewModal
      v-if="showReviewModal"
      :booking-id="selectedBookingId"
      :customer-id="customerId"
      @close="closeReviewModal"
      @success="handleReviewSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookingService, type BookingResponseDTO } from '@/services/bookingService'
import CreateReviewModal from '@/components/review/CreateReviewModal.vue'

const router = useRouter()

const bookings = ref<BookingResponseDTO[]>([])
const loading = ref(false)
const error = ref('')

// Review modal state
const showReviewModal = ref(false)
const selectedBookingId = ref('')
const customerId = ref('550e8400-e29b-41d4-a716-446655440000') // Hardcoded for now

const loadBookings = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await bookingService.getAll()
    if (response.success) {
      bookings.value = response.data
    } else {
      error.value = response.message
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load bookings'
    console.error('Load bookings error:', err)
  } finally {
    loading.value = false
  }
}

const goToCreateBooking = () => {
  router.push('/booking/create')
}

const goToChart = () => {
  router.push('/booking/chart')
}

const goToDetail = (bookingId: string) => {
  router.push(`/booking/${bookingId}`)
}

const goToUpdate = (bookingId: string) => {
  router.push(`/booking/update/${bookingId}`)
}

// Check if booking can be reviewed
const canReview = (booking: BookingResponseDTO) => {
  // Can review if: status is Payment Confirmed (1) AND checkout date has passed
  if (booking.status !== 1) return false
  
  const checkoutDate = new Date(booking.checkOutDate)
  const now = new Date()
  return now >= checkoutDate
}

const openReviewModal = (bookingId: string) => {
  selectedBookingId.value = bookingId
  showReviewModal.value = true
}

const closeReviewModal = () => {
  showReviewModal.value = false
  selectedBookingId.value = ''
}

const handleReviewSuccess = () => {
  alert('Review submitted successfully!')
  closeReviewModal()
}

const confirmPayment = async (bookingId: string) => {
  if (!confirm('Confirm payment for this booking?')) return

  try {
    const response = await bookingService.pay(bookingId)
    if (response.success) {
      alert('Payment confirmed successfully!')
      loadBookings()
    } else {
      alert(response.message)
    }
  } catch (err: any) {
    alert(err.response?.data?.message || 'Failed to confirm payment')
    console.error('Payment error:', err)
  }
}

const cancelBooking = async (bookingId: string) => {
  if (!confirm('Are you sure you want to cancel this booking?')) return

  try {
    const response = await bookingService.cancel(bookingId)
    if (response.success) {
      alert('Booking cancelled successfully!')
      loadBookings()
    } else {
      alert(response.message)
    }
  } catch (err: any) {
    alert(err.response?.data?.message || 'Failed to cancel booking')
    console.error('Cancel error:', err)
  }
}

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('id-ID').format(value)
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('id-ID', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

const getStatusClass = (status: number) => {
  switch (status) {
    case 0:
      return 'status-pending'
    case 1:
      return 'status-confirmed'
    case 2:
      return 'status-cancelled'
    default:
      return ''
  }
}

onMounted(() => {
  loadBookings()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&family=Raleway:wght@500;600;700;800&display=swap');

.booking-list {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 60px;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Page Header */
.page-header {
  margin-bottom: 40px;
  padding: 30px 40px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(124, 106, 70, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 25px;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: white;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(124, 106, 70, 0.15);
}

.header-text {
  flex: 1;
}

.page-title {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  color: #4A4A4A;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  opacity: 0.9;
}

/* Error Message */
.error-message {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #FEE;
  color: #C33;
  padding: 16px 20px;
  border-radius: 10px;
  margin-bottom: 25px;
  border-left: 4px solid #F44336;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 500;
}

/* Actions */
.actions {
  display: flex;
  gap: 15px;
  margin-bottom: 35px;
}

.btn-primary,
.btn-chart,
.btn-secondary {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 28px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-primary {
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.3);
}

.btn-chart {
  background: linear-gradient(135deg, #9C27B0 0%, #7B1FA2 100%);
  color: white;
}

.btn-chart:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(156, 39, 176, 0.3);
}

.btn-secondary {
  background: white;
  color: #7C6A46;
  border: 2px solid #7C6A46;
}

.btn-secondary:hover {
  background: #FAFAFA;
  transform: translateY(-2px);
}

/* Loading */
.loading {
  text-align: center;
  padding: 80px 20px;
  color: #7C6A46;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 500;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  margin: 0 auto 20px;
  border: 4px solid #FAFAFA;
  border-top: 4px solid #7C6A46;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Bookings Grid */
.bookings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
}

.booking-card {
  background: white;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid #F0F0F0;
}

.booking-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(124, 106, 70, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
  border-bottom: 1px solid #F0F0F0;
}

.booking-id {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #7C6A46;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 700;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-family: 'Poppins', sans-serif;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-pending {
  background: #FFF3E0;
  color: #F57C00;
}

.status-confirmed {
  background: #E3F2FD;
  color: #1976D2;
}

.status-confirmed {
  background: #E8F5E9;
  color: #2E7D32;
}

.status-cancelled {
  background: #FFEBEE;
  color: #C62828;
}

.card-body {
  padding: 25px;
}

.booking-property {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #F0F0F0;
}

.booking-property svg {
  flex-shrink: 0;
  margin-top: 2px;
}

.property-name {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

.room-name {
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 500;
}

.booking-customer {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
  padding: 15px;
  background: #FAFAFA;
  border-radius: 10px;
}

.booking-customer svg {
  flex-shrink: 0;
  margin-top: 2px;
}

.customer-name {
  color: #1C1C1C;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 2px;
}

.customer-email {
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 400;
}

.booking-dates {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.date-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: white;
  border: 2px solid #F0F0F0;
  border-radius: 10px;
}

.date-item svg {
  flex-shrink: 0;
}

.date-label {
  color: #999;
  font-family: 'Poppins', sans-serif;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 2px;
}

.date-value {
  color: #1C1C1C;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
}

.arrow-icon {
  flex-shrink: 0;
}

.booking-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
  border-radius: 10px;
}

.price-label {
  color: rgba(255, 255, 255, 0.9);
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 500;
}

.price-value {
  color: white;
  font-family: 'Raleway', sans-serif;
  font-size: 20px;
  font-weight: 700;
}

.card-actions {
  padding: 20px 25px;
  background: #FAFAFA;
  border-top: 1px solid #F0F0F0;
  display: flex;
  gap: 12px;
}

.btn-action {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  color: white;
}

.btn-detail {
  background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%);
}

.btn-detail:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.4);
}

.btn-review {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
}

.btn-review:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.4);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
  border-radius: 15px;
  margin-top: 20px;
}

.empty-state svg {
  margin-bottom: 25px;
  opacity: 0.4;
}

.empty-state h3 {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
}

.empty-state p {
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 400;
  margin: 0 0 30px 0;
}

.btn-empty-state {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 32px;
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(124, 106, 70, 0.3);
}

.btn-empty-state:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.4);
}

/* Responsive */
@media (max-width: 1200px) {
  .bookings-grid {
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  }
}

@media (max-width: 768px) {
  .booking-list {
    padding: 20px;
  }

  .page-header {
    padding: 20px;
  }

  .header-content {
    flex-direction: column;
    text-align: center;
  }

  .page-title {
    font-size: 32px;
  }

  .bookings-grid {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
  }

  .btn-primary,
  .btn-chart,
  .btn-secondary {
    width: 100%;
    justify-content: center;
  }

  .booking-dates {
    flex-direction: column;
  }

  .arrow-icon {
    transform: rotate(90deg);
  }
}
</style>
