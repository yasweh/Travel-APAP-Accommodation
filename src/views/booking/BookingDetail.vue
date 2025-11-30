<template>
  <div class="booking-detail">
    <!-- Loading State -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>Loading booking details...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="message-box error">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM11 15H9V13H11V15ZM11 11H9V5H11V11Z" fill="#F44336"/>
      </svg>
      <span>{{ error }}</span>
    </div>
    
    <!-- Main Content -->
    <div v-else-if="booking" class="detail-container">
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-content">
          <div class="header-icon">
            <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
              <path d="M32.5 5H30V2.5C30 1.125 28.875 0 27.5 0H12.5C11.125 0 10 1.125 10 2.5V5H7.5C6.125 5 5 6.125 5 7.5V35C5 36.375 6.125 37.5 7.5 37.5H32.5C33.875 37.5 35 36.375 35 35V7.5C35 6.125 33.875 5 32.5 5ZM12.5 2.5H27.5V5H12.5V2.5ZM32.5 35H7.5V7.5H32.5V35Z" fill="#7C6A46"/>
              <path d="M20 12.5C17.925 12.5 16.25 14.175 16.25 16.25C16.25 18.325 17.925 20 20 20C22.075 20 23.75 18.325 23.75 16.25C23.75 14.175 22.075 12.5 20 12.5Z" fill="#7C6A46"/>
              <path d="M20 22.5C16.5625 22.5 10 24.225 10 27.5V30H30V27.5C30 24.225 23.4375 22.5 20 22.5Z" fill="#7C6A46"/>
            </svg>
          </div>
          <div class="header-text">
            <h1 class="page-title">Booking Details</h1>
            <p class="page-subtitle">{{ booking.bookingId }}</p>
          </div>
        </div>

        <!-- Status Badge -->
        <div class="status-container">
          <span :class="['status-badge', getStatusClass(booking.status)]">
            {{ getStatusLabel(booking.status) }}
          </span>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="action-buttons" v-if="availableActions.canPay || availableActions.canCancel || availableActions.canUpdate || canWriteReview">
        <button 
          v-if="canWriteReview" 
          @click="goToCreateReview" 
          class="btn-action btn-review"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M0 12.6667V16H3.33333L13.1667 6.16667L9.83333 2.83333L0 12.6667Z" fill="white"/>
            <path d="M15.7333 3.6L12.4 0.266667C12.0667 -0.0666667 11.5333 -0.0666667 11.2 0.266667L8.86667 2.6L12.2 5.93333L14.5333 3.6C14.8667 3.26667 14.8667 2.73333 14.5333 2.4L15.7333 3.6Z" fill="white"/>
          </svg>
          Write Review
        </button>
        <button 
          v-if="availableActions.canPay" 
          @click="goToBillService" 
          class="btn-action btn-pay"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M13.3333 2H2.66667C1.93333 2 1.33333 2.6 1.33333 3.33333V12.6667C1.33333 13.4 1.93333 14 2.66667 14H13.3333C14.0667 14 14.6667 13.4 14.6667 12.6667V3.33333C14.6667 2.6 14.0667 2 13.3333 2ZM13.3333 12.6667H2.66667V8H13.3333V12.6667ZM13.3333 5.33333H2.66667V3.33333H13.3333V5.33333Z" fill="white"/>
          </svg>
          Pay via Bill Service
        </button>
        <button 
          v-if="availableActions.canUpdate" 
          @click="goToUpdate" 
          class="btn-action btn-update"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M0 12.6667V16H3.33333L13.1667 6.16667L9.83333 2.83333L0 12.6667Z" fill="white"/>
          </svg>
          Update Booking
        </button>
        <button 
          v-if="availableActions.canCancel" 
          @click="confirmCancel" 
          class="btn-action btn-cancel"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C12.42 16 16 12.42 16 8C16 3.58 12.42 0 8 0ZM11.5 10.09L10.09 11.5L8 9.41L5.91 11.5L4.5 10.09L6.59 8L4.5 5.91L5.91 4.5L8 6.59L10.09 4.5L11.5 5.91L9.41 8L11.5 10.09Z" fill="white"/>
          </svg>
          Cancel Booking
        </button>
        <button @click="goBack" class="btn-action btn-back">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M16 7H3.83L9.42 1.41L8 0L0 8L8 16L9.41 14.59L3.83 9H16V7Z" fill="white"/>
          </svg>
          Back to List
        </button>
      </div>

      <!-- Main Content Grid -->
      <div class="content-grid">
        <!-- Property & Room Information Card -->
        <div class="form-card">
          <div class="card-header">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V5H19V19Z" fill="#7C6A46"/>
            </svg>
            <h2 class="card-title">Property & Room Information</h2>
          </div>

          <div class="detail-grid">
            <div class="detail-item">
              <label>Property Name</label>
              <div class="detail-value property-name">{{ booking.propertyName }}</div>
            </div>
            <div class="detail-item">
              <label>Room Name</label>
              <div class="detail-value">{{ booking.roomName }}</div>
            </div>
          </div>
        </div>

        <!-- Booking Information Card -->
        <div class="form-card">
          <div class="card-header">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="#7C6A46"/>
            </svg>
            <h2 class="card-title">Booking Details</h2>
          </div>

          <div class="detail-grid">
            <div class="detail-item">
              <label>Booking ID</label>
              <div class="detail-value booking-id">{{ booking.bookingId }}</div>
            </div>
            <div class="detail-item">
              <label>Number of Guests</label>
              <div class="detail-value">{{ booking.capacity }} person(s)</div>
            </div>
            <div class="detail-item">
              <label>Check-in Date</label>
              <div class="detail-value">{{ formatDate(booking.checkInDate) }}</div>
            </div>
            <div class="detail-item">
              <label>Check-out Date</label>
              <div class="detail-value">{{ formatDate(booking.checkOutDate) }}</div>
            </div>
            <div class="detail-item">
              <label>Total Days</label>
              <div class="detail-value highlight">{{ booking.totalDays }} day(s)</div>
            </div>
            <div class="detail-item">
              <label>Breakfast</label>
              <div class="detail-value">{{ booking.addOnBreakfast ? 'Included' : 'Not Included' }}</div>
            </div>
          </div>
        </div>

        <!-- Customer Information Card -->
        <div class="form-card">
          <div class="card-header">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z" fill="#7C6A46"/>
            </svg>
            <h2 class="card-title">Customer Information</h2>
          </div>

          <div class="detail-grid">
            <div class="detail-item full-width">
              <label>Customer Name</label>
              <div class="detail-value customer-name">{{ booking.customerName }}</div>
            </div>
            <div class="detail-item">
              <label>Email</label>
              <div class="detail-value">{{ booking.customerEmail }}</div>
            </div>
            <div class="detail-item">
              <label>Phone</label>
              <div class="detail-value">{{ booking.customerPhone }}</div>
            </div>
          </div>
        </div>

        <!-- Payment Details Card -->
        <div class="form-card">
          <div class="card-header">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M20 4H4C2.89 4 2.01 4.89 2.01 6L2 18C2 19.11 2.89 20 4 20H20C21.11 20 22 19.11 22 18V6C22 4.89 21.11 4 20 4ZM20 18H4V12H20V18ZM20 8H4V6H20V8Z" fill="#7C6A46"/>
            </svg>
            <h2 class="card-title">Payment Details</h2>
          </div>

          <div class="price-breakdown">
            <div class="price-row">
              <span class="price-label">Room Rate ({{ booking.totalDays }} night{{ booking.totalDays > 1 ? 's' : '' }})</span>
              <span class="price-value">Rp {{ formatCurrency(calculateRoomRate()) }}</span>
            </div>
            <div class="price-row" v-if="booking.addOnBreakfast">
              <span class="price-label">Breakfast ({{ booking.totalDays }} day{{ booking.totalDays > 1 ? 's' : '' }})</span>
              <span class="price-value">Rp {{ formatCurrency(booking.totalDays * 50000) }}</span>
            </div>
            <div class="price-row total-row">
              <span class="price-label total-label">Total Amount</span>
              <span class="price-value total-value">Rp {{ formatCurrency(booking.totalPrice) }}</span>
            </div>
          </div>
        </div>

        <!-- Booking History Card -->
        <div class="form-card full-width">
          <div class="card-header">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M13 3C8.03 3 4 7.03 4 12H1L4.89 15.89L4.96 16.03L9 12H6C6 8.13 9.13 5 13 5C16.87 5 20 8.13 20 12C20 15.87 16.87 19 13 19C11.07 19 9.32 18.21 8.06 16.94L6.64 18.36C8.27 19.99 10.51 21 13 21C17.97 21 22 16.97 22 12C22 7.03 17.97 3 13 3ZM12 8V13L16.25 15.52L17.02 14.24L13.5 12.15V8H12Z" fill="#7C6A46"/>
            </svg>
            <h2 class="card-title">Booking History</h2>
          </div>

          <div class="detail-grid">
            <div class="detail-item">
              <label>Created Date</label>
              <div class="detail-value">{{ formatDateTime(booking.createdAt) }}</div>
            </div>
            <div class="detail-item">
              <label>Last Updated</label>
              <div class="detail-value">{{ formatDateTime(booking.updatedAt) }}</div>
            </div>
          </div>
        </div>

        <!-- Status Information Banner -->
        <div class="status-info-card" :class="getStatusClass(booking.status)">
          <div class="status-info-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V11H13V17ZM13 9H11V7H13V9Z" fill="currentColor"/>
            </svg>
          </div>
          <div class="status-info-content">
            <h3>{{ getStatusLabel(booking.status) }}</h3>
            <p>{{ getStatusDescription(booking.status) }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { bookingService } from '@/services/bookingService'

const router = useRouter()
const route = useRoute()

const booking = ref<any>(null)
const availableActions = ref({
  canPay: false,
  canCancel: false,
  canUpdate: false
})
const loading = ref(false)
const error = ref('')

// Check if user can write a review
// Can write review if: status = 1 (Payment Confirmed) AND current date >= checkout date
const canWriteReview = computed(() => {
  if (!booking.value) return false
  
  const isConfirmed = booking.value.status === 1
  const checkOutDate = new Date(booking.value.checkOutDate)
  const now = new Date()
  const isPastCheckout = now >= checkOutDate
  
  return isConfirmed && isPastCheckout
})

const loadBookingDetail = async () => {
  loading.value = true
  error.value = ''
  try {
    const bookingId = route.params.id as string
    const response = await bookingService.getById(bookingId)
    
    if (response.success) {
      booking.value = response.data
      availableActions.value = response.availableActions || {
        canPay: false,
        canCancel: false,
        canUpdate: false
      }
    } else {
      error.value = response.message
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load booking details'
    console.error('Load error:', err)
  } finally {
    loading.value = false
  }
}

const calculateRoomRate = () => {
  if (!booking.value) return 0
  let baseRate = booking.value.totalPrice
  if (booking.value.addOnBreakfast) {
    baseRate -= booking.value.totalDays * 50000
  }
  return baseRate
}

// Navigate to Bill Service for payment
const goToBillService = async () => {
  if (!booking.value) return
  
  // Bill Service Frontend URL
  const billServiceFrontendUrl = 'http://2306211660-fe.hafizmuh.site'
  // Bill Service Backend API URL
  const billServiceApiUrl = 'http://2306211660-be.hafizmuh.site/api/bill'
  
  // Create bill payload matching Bill Service's expected format
  const billPayload = {
    customerId: booking.value.customerId,
    serviceName: 'ACCOMMODATION',
    serviceReferenceId: booking.value.bookingId,
    description: `Booking ${booking.value.roomName} at ${booking.value.propertyName} (${booking.value.totalDays} night(s))`,
    amount: booking.value.totalPrice
  }
  
  try {
    // Try to create bill via API first
    const response = await fetch(billServiceApiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(billPayload)
    })
    
    if (response.ok) {
      const result = await response.json()
      console.log('Bill created:', result)
      alert('Bill created successfully! Redirecting to Bill Service to complete payment...')
    } else {
      console.warn('Bill API returned non-ok status, redirecting to frontend anyway')
    }
  } catch (error) {
    console.warn('Could not create bill via API, redirecting to Bill Service frontend:', error)
  }
  
  // Open Bill Service frontend for user to complete payment
  window.open(billServiceFrontendUrl, '_blank')
}

const confirmCancel = async () => {
  if (!confirm('Are you sure you want to cancel this booking?')) return

  try {
    const response = await bookingService.cancel(booking.value.bookingId)
    if (response.success) {
      alert('Booking cancelled successfully!')
      loadBookingDetail()
    } else {
      alert(response.message)
    }
  } catch (err: any) {
    alert(err.response?.data?.message || 'Failed to cancel booking')
  }
}

const getStatusClass = (status: number) => {
  if (status === 0) return 'status-waiting'
  if (status === 1) return 'status-confirmed'
  if (status === 2) return 'status-cancelled'
  return 'status-cancelled'
}

const getStatusLabel = (status: number) => {
  if (status === 0) return 'Waiting for Payment'
  if (status === 1) return 'Payment Confirmed'
  if (status === 2) return 'Cancelled'
  return 'Unknown'
}

const getStatusDescription = (status: number) => {
  if (status === 0) return 'Please complete the payment to confirm your booking.'
  if (status === 1) return 'Your payment has been confirmed. Enjoy your stay!'
  if (status === 2) return 'This booking has been cancelled.'
  return 'Unknown status.'
}

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('id-ID').format(value)
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('id-ID', {
    day: '2-digit',
    month: 'long',
    year: 'numeric'
  })
}

const formatDateTime = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('id-ID', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goBack = () => {
  router.push('/booking')
}

const goToUpdate = () => {
  router.push(`/booking/update/${booking.value.bookingId}`)
}

const goToCreateReview = () => {
  // Navigate to review creation page with bookingId as query param
  router.push({
    path: '/reviews/create',
    query: { bookingId: booking.value.bookingId }
  })
}

onMounted(() => {
  loadBookingDetail()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Raleway:wght@700;800&family=Poppins:wght@400;500;600&display=swap');

/* Container */
.booking-detail {
  min-height: calc(100vh - 200px);
  padding: 60px 20px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F5F5F5 100%);
  animation: fadeIn 0.6s ease-in-out;
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

/* Loading State */
.loading {
  text-align: center;
  padding: 80px 20px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  margin: 0 auto 20px;
  border: 4px solid #E8E8E8;
  border-top-color: #7C6A46;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading p {
  font-family: 'Poppins', sans-serif;
  font-size: 1.1rem;
  color: #666;
}

/* Messages */
.message-box {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px 25px;
  border-radius: 12px;
  margin: 0 auto 30px;
  max-width: 1200px;
  font-family: 'Poppins', sans-serif;
  animation: slideIn 0.4s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-box.error {
  background: #FEE;
  color: #C33;
  border: 2px solid #F44336;
}

.message-box svg {
  flex-shrink: 0;
}

/* Detail Container */
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  animation: fadeIn 0.6s ease-in-out;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #7C6A46 0%, #5A4A30 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(124, 106, 70, 0.3);
}

.header-icon svg {
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.header-text {
  text-align: left;
}

.page-title {
  font-family: 'Raleway', sans-serif;
  font-size: 2.5rem;
  font-weight: 800;
  color: #1C1C1C;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-family: 'Poppins', sans-serif;
  font-size: 1.1rem;
  color: #7C6A46;
  margin: 0;
  font-weight: 500;
}

/* Status Container */
.status-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 12px 24px;
  border-radius: 25px;
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.status-badge.status-waiting {
  background: linear-gradient(135deg, #FFC107 0%, #FF9800 100%);
  color: white;
}

.status-badge.status-confirmed {
  background: linear-gradient(135deg, #4CAF50 0%, #388E3C 100%);
  color: white;
}

.status-badge.status-cancelled {
  background: linear-gradient(135deg, #F44336 0%, #D32F2F 100%);
  color: white;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  flex-wrap: wrap;
  margin-bottom: 40px;
  padding: 0 20px;
}

.btn-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.btn-action:active {
  transform: translateY(0);
}

.btn-review {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-pay {
  background: linear-gradient(135deg, #4CAF50 0%, #388E3C 100%);
  color: white;
}

.btn-update {
  background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%);
  color: white;
}

.btn-cancel {
  background: linear-gradient(135deg, #F44336 0%, #D32F2F 100%);
  color: white;
}

.btn-back {
  background: linear-gradient(135deg, #9E9E9E 0%, #757575 100%);
  color: white;
}

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

/* Form Cards */
.form-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.form-card.full-width {
  grid-column: 1 / -1;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #F5F5F5;
}

.card-header svg {
  flex-shrink: 0;
}

.card-title {
  font-family: 'Raleway', sans-serif;
  font-size: 1.4rem;
  font-weight: 700;
  color: #1C1C1C;
  margin: 0;
}

/* Detail Grid */
.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-item label {
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
  font-weight: 600;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-value {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  color: #1C1C1C;
  font-weight: 500;
  padding: 10px 15px;
  background: #F9F9F9;
  border-radius: 8px;
  border-left: 3px solid #7C6A46;
}

.detail-value.property-name,
.detail-value.customer-name {
  font-weight: 700;
  color: #7C6A46;
}

.detail-value.booking-id {
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  background: #FFF3E0;
  border-left-color: #FF9800;
}

.detail-value.highlight {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  color: #1976D2;
  font-weight: 700;
  border-left-color: #2196F3;
}

/* Price Breakdown */
.price-breakdown {
  background: #F9F9F9;
  border-radius: 12px;
  padding: 20px;
  border: 2px solid #E8E8E8;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-family: 'Poppins', sans-serif;
  border-bottom: 1px solid #E8E8E8;
}

.price-row:last-child {
  border-bottom: none;
}

.price-label {
  font-size: 0.95rem;
  color: #666;
  font-weight: 500;
}

.price-value {
  font-size: 1rem;
  color: #1C1C1C;
  font-weight: 600;
}

.price-row.total-row {
  margin-top: 15px;
  padding-top: 20px;
  border-top: 2px solid #7C6A46;
  border-bottom: none;
}

.total-label {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1C1C1C;
}

.total-value {
  font-size: 1.3rem;
  font-weight: 800;
  color: #7C6A46;
}

/* Status Info Card */
.status-info-card {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 25px 30px;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.6s ease-out;
}

.status-info-card.status-waiting {
  background: linear-gradient(135deg, #FFF9E6 0%, #FFF3CC 100%);
  border-left: 5px solid #FFC107;
}

.status-info-card.status-confirmed {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  border-left: 5px solid #4CAF50;
}

.status-info-card.status-cancelled {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  border-left: 5px solid #F44336;
}

.status-info-icon {
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.status-info-card.status-waiting .status-info-icon {
  color: #FFC107;
}

.status-info-card.status-confirmed .status-info-icon {
  color: #4CAF50;
}

.status-info-card.status-cancelled .status-info-icon {
  color: #F44336;
}

.status-info-content h3 {
  font-family: 'Raleway', sans-serif;
  font-size: 1.3rem;
  font-weight: 700;
  color: #1C1C1C;
  margin: 0 0 8px 0;
}

.status-info-content p {
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  color: #666;
  margin: 0;
  line-height: 1.6;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .page-title {
    font-size: 2rem;
  }
}

@media (max-width: 768px) {
  .booking-detail {
    padding: 40px 15px;
  }
  
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  
  .header-text {
    text-align: center;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .btn-action {
    width: 100%;
    justify-content: center;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .form-card {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }
  
  .page-subtitle {
    font-size: 0.9rem;
  }
  
  .header-icon {
    width: 60px;
    height: 60px;
  }
  
  .header-icon svg {
    width: 30px;
    height: 30px;
  }
}
</style>
  gap: 10px;
  flex-wrap: wrap;
}

.btn-update,
.btn-pay,
.btn-cancel,
.btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-update {
  background: #ff9800;
  color: white;
}

.btn-update:hover {
  background: #f57c00;
}

.btn-pay {
  background: #4caf50;
  color: white;
}

.btn-pay:hover {
  background: #45a049;
}

.btn-cancel {
  background: #f44336;
  color: white;
}

.btn-cancel:hover {
  background: #da190b;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2px solid white;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.3);
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  padding: 30px;
}

/* Responsive Design */
@media (max-width: 1024px) {
