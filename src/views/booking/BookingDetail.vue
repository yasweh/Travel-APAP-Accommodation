<template>
  <div class="booking-detail">
    <div v-if="loading" class="loading">Loading booking details...</div>
    <div v-else-if="error" class="error-message">{{ error }}</div>
    
    <div v-else-if="booking" class="detail-container">
      <!-- Header -->
      <div class="detail-header">
        <div class="header-content">
          <h1>Booking Details {{ booking.bookingId }}</h1>
          <span :class="['status-badge', getStatusClass(booking.status)]">
            {{ getStatusLabel(booking.status) }}
          </span>
        </div>
        <div class="header-actions">
          <button 
            v-if="availableActions.canPay" 
            @click="confirmPayment" 
            class="btn-pay"
          >
            Confirm Payment
          </button>
          <button 
            v-if="availableActions.canRefund" 
            @click="requestRefund" 
            class="btn-refund"
          >
            Request Refund
          </button>
          <button 
            v-if="availableActions.canCancel" 
            @click="confirmCancel" 
            class="btn-cancel"
          >
            Cancel Booking
          </button>
          <button @click="goBack" class="btn-secondary">Back</button>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="content-grid">
        <!-- Left Column: Property & Room Info -->
        <div class="info-section">
          <h2>Property Information</h2>
          <div class="info-card">
            <div class="info-item">
              <label>Property Name:</label>
              <span class="property-name">{{ booking.propertyName }}</span>
            </div>
            <div class="info-item">
              <label>Room Name:</label>
              <span>{{ booking.roomName }}</span>
            </div>
          </div>
        </div>

        <!-- Right Column: Booking Details -->
        <div class="info-section">
          <h2>Booking Information</h2>
          <div class="info-card">
            <div class="info-item">
              <label>Booking ID:</label>
              <span class="booking-id">{{ booking.bookingId }}</span>
            </div>
            <div class="info-item">
              <label>Check-In:</label>
              <span>{{ formatDateTime(booking.checkInDate) }}</span>
            </div>
            <div class="info-item">
              <label>Check-Out:</label>
              <span>{{ formatDateTime(booking.checkOutDate) }}</span>
            </div>
            <div class="info-item">
              <label>Total Days:</label>
              <span class="highlight">{{ booking.totalDays }} day(s)</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Customer Information -->
      <div class="info-section full-width">
        <h2>Customer Information</h2>
        <div class="info-card">
          <div class="info-grid">
            <div class="info-item">
              <label>Customer ID:</label>
              <span>{{ booking.customerId }}</span>
            </div>
            <div class="info-item">
              <label>Customer Name:</label>
              <span class="customer-name">{{ booking.customerName }}</span>
            </div>
            <div class="info-item">
              <label>Email:</label>
              <span>{{ booking.customerEmail }}</span>
            </div>
            <div class="info-item">
              <label>Phone:</label>
              <span>{{ booking.customerPhone }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Pricing Details -->
      <div class="info-section full-width">
        <h2>Payment Details</h2>
        <div class="info-card pricing-card">
          <div class="price-rows">
            <div class="price-row">
              <label>Room Rate ({{ booking.totalDays }} nights):</label>
              <span>Rp {{ formatCurrency(calculateRoomRate()) }}</span>
            </div>
            <div class="price-row" v-if="booking.addOnBreakfast">
              <label>Breakfast ({{ booking.totalDays }} days × Rp 50,000):</label>
              <span>Rp {{ formatCurrency(booking.totalDays * 50000) }}</span>
            </div>
            <div class="price-row" v-else>
              <label>Breakfast:</label>
              <span class="not-included">Not Included</span>
            </div>
            <div class="price-row" v-if="booking.extraPay > 0">
              <label>Extra Payment:</label>
              <span class="extra-pay">+ Rp {{ formatCurrency(booking.extraPay) }}</span>
            </div>
            <div class="price-row total-row">
              <label>Total Price:</label>
              <span class="total-price">Rp {{ formatCurrency(booking.totalPrice) }}</span>
            </div>
            <div class="price-row refund-row" v-if="booking.refund > 0">
              <label>Refund Amount:</label>
              <span class="refund-amount">Rp {{ formatCurrency(booking.refund) }}</span>
            </div>
          </div>

          <div class="capacity-info">
            <label>Number of Guests:</label>
            <span>{{ booking.capacity }} person(s)</span>
          </div>
        </div>
      </div>

      <!-- Timestamps -->
      <div class="info-section full-width">
        <h2>Booking History</h2>
        <div class="info-card">
          <div class="info-grid">
            <div class="info-item">
              <label>Created Date:</label>
              <span>{{ formatDateTime(booking.createdAt) }}</span>
            </div>
            <div class="info-item">
              <label>Last Updated:</label>
              <span>{{ formatDateTime(booking.updatedAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Status Info Banner -->
      <div class="status-info" :class="getStatusClass(booking.status)">
        <div class="status-icon">ℹ️</div>
        <div class="status-text">
          <strong>Status: {{ getStatusLabel(booking.status) }}</strong>
          <p>{{ getStatusDescription(booking.status) }}</p>
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
  canRefund: false,
  canUpdate: false
})
const loading = ref(false)
const error = ref('')

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
        canRefund: false,
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
  if (booking.value.extraPay > 0) {
    baseRate -= booking.value.extraPay
  }
  return baseRate
}

const confirmPayment = async () => {
  if (!confirm('Confirm payment for this booking?')) return

  try {
    const response = await bookingService.pay(booking.value.bookingId)
    if (response.success) {
      alert('Payment confirmed successfully!')
      loadBookingDetail()
    } else {
      alert(response.message)
    }
  } catch (err: any) {
    alert(err.response?.data?.message || 'Failed to confirm payment')
  }
}

const requestRefund = async () => {
  if (!confirm('Request refund for this booking? This action cannot be undone.')) return

  try {
    const response = await bookingService.refund(booking.value.bookingId)
    if (response.success) {
      alert('Refund requested successfully!')
      loadBookingDetail()
    } else {
      alert(response.message)
    }
  } catch (err: any) {
    alert(err.response?.data?.message || 'Failed to request refund')
  }
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
  if (status === 2) return 'status-done'
  return 'status-cancelled'
}

const getStatusLabel = (status: number) => {
  if (status === 0) return 'Waiting for Payment'
  if (status === 1) return 'Payment Confirmed'
  if (status === 2) return 'Done'
  return 'Cancelled / Request Refund'
}

const getStatusDescription = (status: number) => {
  if (status === 0) return 'Please complete the payment to confirm your booking.'
  if (status === 1) return 'Your payment has been confirmed. Enjoy your stay!'
  if (status === 2) return 'This booking has been completed. Thank you!'
  return 'This booking has been cancelled or refund has been requested.'
}

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('id-ID').format(value)
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

onMounted(() => {
  loadBookingDetail()
})
</script>

<style scoped>
.booking-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #666;
  font-size: 18px;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  margin: 20px 0;
}

.detail-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.detail-header {
  background: linear-gradient(135deg, #ff9800 0%, #ff5722 100%);
  color: white;
  padding: 30px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.header-content h1 {
  margin: 0;
  font-size: 28px;
}

.status-badge {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  border: 2px solid white;
}

.status-badge.status-waiting {
  background: rgba(255, 193, 7, 0.3);
}

.status-badge.status-confirmed {
  background: rgba(76, 175, 80, 0.3);
}

.status-badge.status-done {
  background: rgba(33, 150, 243, 0.3);
}

.status-badge.status-cancelled {
  background: rgba(244, 67, 54, 0.3);
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-pay,
.btn-refund,
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

.btn-pay {
  background: #4caf50;
  color: white;
}

.btn-pay:hover {
  background: #45a049;
}

.btn-refund {
  background: #2196f3;
  color: white;
}

.btn-refund:hover {
  background: #0b7dda;
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

.info-section {
  margin-bottom: 20px;
}

.info-section.full-width {
  padding: 0 30px;
  margin-bottom: 30px;
}

.info-section h2 {
  color: #333;
  margin: 0 0 15px 0;
  font-size: 20px;
  border-bottom: 2px solid #ff9800;
  padding-bottom: 10px;
}

.info-card {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #ff9800;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 600;
  color: #666;
  min-width: 150px;
}

.info-item span {
  color: #333;
  text-align: right;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.info-grid .info-item {
  flex-direction: column;
  align-items: flex-start;
  gap: 5px;
}

.info-grid .info-item span {
  text-align: left;
}

.property-name,
.customer-name,
.booking-id {
  font-weight: bold;
  color: #ff9800;
  font-size: 16px;
}

.highlight {
  font-weight: bold;
  color: #ff9800;
}

.pricing-card {
  background: linear-gradient(to bottom, #fff3e0, #ffffff);
}

.price-rows {
  margin-bottom: 20px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.price-row label {
  font-weight: 500;
  color: #666;
}

.price-row span {
  font-weight: 600;
  color: #333;
}

.not-included {
  color: #999;
  font-style: italic;
}

.extra-pay {
  color: #ff9800;
}

.total-row {
  background: #fff;
  padding: 15px;
  margin: 10px -10px;
  border-radius: 4px;
  border: 2px solid #ff9800;
}

.total-row label {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.total-price {
  font-size: 22px;
  font-weight: bold;
  color: #ff9800;
}

.refund-row {
  background: #e3f2fd;
  padding: 15px;
  margin: 10px -10px;
  border-radius: 4px;
  border: 2px solid #2196f3;
}

.refund-amount {
  font-size: 18px;
  font-weight: bold;
  color: #2196f3;
}

.capacity-info {
  display: flex;
  justify-content: space-between;
  padding: 15px;
  background: #fff;
  border-radius: 4px;
  margin-top: 10px;
}

.capacity-info label {
  font-weight: 600;
  color: #666;
}

.capacity-info span {
  font-weight: 600;
  color: #333;
}

.status-info {
  margin: 30px;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  gap: 15px;
  align-items: flex-start;
}

.status-info.status-waiting {
  background: #fff3e0;
  border: 2px solid #ffc107;
}

.status-info.status-confirmed {
  background: #e8f5e9;
  border: 2px solid #4caf50;
}

.status-info.status-done {
  background: #e3f2fd;
  border: 2px solid #2196f3;
}

.status-info.status-cancelled {
  background: #ffebee;
  border: 2px solid #f44336;
}

.status-icon {
  font-size: 32px;
}

.status-text strong {
  display: block;
  margin-bottom: 5px;
  font-size: 16px;
}

.status-text p {
  margin: 0;
  color: #666;
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .header-actions button {
    flex: 1;
  }
}
</style>
