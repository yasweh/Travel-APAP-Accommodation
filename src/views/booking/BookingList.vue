<template>
  <div class="booking-list">
    <h1>Booking Management</h1>

    <!-- Error Message -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- Actions -->
    <div class="actions">
      <button @click="goToCreateBooking" class="btn-primary">Create New Booking</button>
      <button @click="goToChart" class="btn-chart">View Statistics</button>
      <button @click="loadBookings" class="btn-secondary">Refresh</button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">Loading bookings...</div>

    <!-- Bookings Table -->
    <table v-else-if="bookings.length > 0" class="data-table">
      <thead>
        <tr>
          <th>Booking ID</th>
          <th>Property</th>
          <th>Room</th>
          <th>Customer</th>
          <th>Check-in</th>
          <th>Check-out</th>
          <th>Total Price</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="booking in bookings" :key="booking.bookingId">
          <td>{{ booking.bookingId }}</td>
          <td>{{ booking.propertyName }}</td>
          <td>{{ booking.roomName }}</td>
          <td>
            {{ booking.customerName }}<br />
            <small>{{ booking.customerEmail }}</small>
          </td>
          <td>{{ formatDate(booking.checkInDate) }}</td>
          <td>{{ formatDate(booking.checkOutDate) }}</td>
          <td>Rp {{ formatCurrency(booking.totalPrice) }}</td>
          <td>
            <span :class="getStatusClass(booking.status)">
              {{ booking.statusString }}
            </span>
          </td>
          <td class="actions-cell">
            <button 
              @click="goToDetail(booking.bookingId)" 
              class="btn-detail"
            >
              Detail
            </button>
            <button @click="goToUpdate(booking.bookingId)" class="btn-edit" v-if="booking.status === 0">
              Edit
            </button>
            <button @click="confirmPayment(booking.bookingId)" class="btn-pay" v-if="booking.status === 0">
              Pay
            </button>
            <button
              @click="cancelBooking(booking.bookingId)"
              class="btn-cancel"
              v-if="booking.status === 0 || booking.status === 1"
            >
              Cancel
            </button>
            <button @click="refundBooking(booking.bookingId)" class="btn-refund" v-if="booking.status === 1">
              Refund
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <p>No bookings found. Create your first booking!</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookingService, type BookingResponseDTO } from '@/services/bookingService'

const router = useRouter()

const bookings = ref<BookingResponseDTO[]>([])
const loading = ref(false)
const error = ref('')

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

const refundBooking = async (bookingId: string) => {
  if (!confirm('Process refund for this booking?')) return

  try {
    const response = await bookingService.refund(bookingId)
    if (response.success) {
      alert('Refund processed successfully!')
      loadBookings()
    } else {
      alert(response.message)
    }
  } catch (err: any) {
    alert(err.response?.data?.message || 'Failed to process refund')
    console.error('Refund error:', err)
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
      return 'status-done'
    case 3:
      return 'status-refunded'
    default:
      return ''
  }
}

onMounted(() => {
  loadBookings()
})
</script>

<style scoped>
.booking-list {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.btn-primary,
.btn-chart,
.btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  background-color: #4caf50;
  color: white;
}

.btn-primary:hover {
  background-color: #45a049;
}

.btn-chart {
  background-color: #9c27b0;
  color: white;
}

.btn-chart:hover {
  background-color: #7b1fa2;
}

.btn-secondary {
  background-color: #2196f3;
  color: white;
}

.btn-secondary:hover {
  background-color: #0b7dda;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  font-size: 13px;
}

.data-table th,
.data-table td {
  padding: 12px 8px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.data-table th {
  background-color: #f5f5f5;
  font-weight: bold;
  color: #333;
}

.data-table tbody tr:hover {
  background-color: #f9f9f9;
}

.data-table small {
  color: #666;
  font-size: 11px;
}

.status-pending {
  color: #ff9800;
  font-weight: bold;
}

.status-confirmed {
  color: #2196f3;
  font-weight: bold;
}

.status-done {
  color: #4caf50;
  font-weight: bold;
}

.status-refunded {
  color: #9c27b0;
  font-weight: bold;
}

.actions-cell {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.btn-detail,
.btn-edit,
.btn-pay,
.btn-cancel,
.btn-refund {
  padding: 5px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  white-space: nowrap;
}

.btn-detail {
  background-color: #2196f3;
  color: white;
}

.btn-detail:hover {
  background-color: #0b7dda;
}

.btn-edit {
  background-color: #ff9800;
  color: white;
}

.btn-edit:hover {
  background-color: #e68900;
}

.btn-pay {
  background-color: #4caf50;
  color: white;
}

.btn-pay:hover {
  background-color: #45a049;
}

.btn-cancel {
  background-color: #f44336;
  color: white;
}

.btn-cancel:hover {
  background-color: #da190b;
}

.btn-refund {
  background-color: #9c27b0;
  color: white;
}

.btn-refund:hover {
  background-color: #7b1fa2;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state p {
  font-size: 16px;
}
</style>
