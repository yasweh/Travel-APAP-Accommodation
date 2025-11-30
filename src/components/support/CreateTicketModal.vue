<template>
  <div class="modal-overlay" @click="handleOverlayClick">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>Create Support Ticket</h2>
        <button class="close-btn" @click="$emit('close')">&times;</button>
      </div>

      <div class="modal-body">
        <!-- Step 1: Select Service Source -->
        <div v-if="step === 1" class="form-step">
          <h3>Step 1: Select Service</h3>
          <div class="form-group">
            <label>Service Source *</label>
            <select v-model="formData.serviceSource" class="form-control" required>
              <option value="">-- Select Service --</option>
              <option value="ACCOMMODATION">Accommodation</option>
              <option value="FLIGHT">Flight</option>
              <option value="RENTAL">Vehicle Rental</option>
              <option value="TOUR">Tour Package</option>
              <option value="INSURANCE">Insurance</option>
            </select>
          </div>
          <div class="form-actions">
            <button
              class="btn btn-primary"
              @click="nextStep"
              :disabled="!formData.serviceSource"
            >
              Next
            </button>
          </div>
        </div>

        <!-- Step 2: Select Booking -->
        <div v-if="step === 2" class="form-step">
          <h3>Step 2: Select Booking</h3>
          <div class="form-group">
            <label>Your Bookings *</label>
            <div v-if="loadingBookings" class="loading-bookings">Loading bookings...</div>
            <div v-if="bookingsError" class="error">{{ bookingsError }}</div>
            <select
              v-if="!loadingBookings && !bookingsError"
              v-model="formData.externalBookingId"
              class="form-control"
              required
            >
              <option value="">-- Select Booking --</option>
              <option
                v-for="booking in availableBookings"
                :key="getBookingId(booking)"
                :value="getBookingId(booking)"
              >
                {{ formatBookingOption(booking) }}
              </option>
            </select>
            <p v-if="availableBookings.length === 0 && !loadingBookings" class="no-bookings">
              No bookings found for this service.
            </p>
          </div>
          <div class="form-actions">
            <button class="btn btn-secondary" @click="prevStep">Back</button>
            <button
              class="btn btn-primary"
              @click="nextStep"
              :disabled="!formData.externalBookingId"
            >
              Next
            </button>
          </div>
        </div>

        <!-- Step 3: Enter Subject and Message -->
        <div v-if="step === 3" class="form-step">
          <h3>Step 3: Describe Your Issue</h3>
          <div class="form-group">
            <label>Subject *</label>
            <input
              v-model="formData.subject"
              type="text"
              class="form-control"
              placeholder="Brief description of the issue"
              required
            />
          </div>
          <div class="form-group">
            <label>Message *</label>
            <textarea
              v-model="formData.initialMessage"
              class="form-control"
              rows="5"
              placeholder="Detailed description of your issue..."
              required
            ></textarea>
          </div>
          <div class="form-actions">
            <button class="btn btn-secondary" @click="prevStep">Back</button>
            <button
              class="btn btn-primary"
              @click="submitTicket"
              :disabled="!formData.subject || !formData.initialMessage || submitting"
            >
              {{ submitting ? 'Creating...' : 'Create Ticket' }}
            </button>
          </div>
        </div>

        <div v-if="submitError" class="error-message">{{ submitError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import supportTicketService, { type CreateTicketRequest } from '@/services/supportTicketService'

interface Props {
  initialServiceSource?: string
  initialBookingId?: string
}

const props = defineProps<Props>()
const emit = defineEmits(['close', 'ticket-created'])

// State
const step = ref(1)
const formData = ref<CreateTicketRequest>({
  userId: '263d012e-b86a-4813-be96-41e6da78e00d', // John Doe from seeder
  subject: '',
  serviceSource: '',
  externalBookingId: '',
  initialMessage: '',
})

const availableBookings = ref<any[]>([])
const loadingBookings = ref(false)
const bookingsError = ref('')
const submitError = ref('')
const submitting = ref(false)

// Initialize with props if provided
onMounted(() => {
  if (props.initialServiceSource) {
    formData.value.serviceSource = props.initialServiceSource
    step.value = 2 // Skip to booking selection
    fetchBookings(props.initialServiceSource)
  }
  if (props.initialBookingId) {
    formData.value.externalBookingId = props.initialBookingId
    step.value = 3 // Skip to message entry
  }
})

// Watch for service source change to fetch bookings
watch(
  () => formData.value.serviceSource,
  async (newSource) => {
    if (newSource && step.value === 2) {
      await fetchBookings(newSource)
    }
  }
)

// Fetch bookings
const fetchBookings = async (serviceSource: string) => {
  loadingBookings.value = true
  bookingsError.value = ''
  availableBookings.value = []

  try {
    const response = await supportTicketService.getAvailableBookings(
      serviceSource,
      formData.value.userId
    )
    availableBookings.value = response.data
  } catch (err: any) {
    bookingsError.value = err.response?.data?.error || 'Failed to fetch bookings'
    console.error('Error fetching bookings:', err)
  } finally {
    loadingBookings.value = false
  }
}

// Get booking ID from different service formats
const getBookingId = (booking: any): string => {
  return booking.bookingId || booking.id || ''
}

// Format booking option for display
const formatBookingOption = (booking: any): string => {
  const source = formData.value.serviceSource
  const id = getBookingId(booking)

  switch (source) {
    case 'ACCOMMODATION':
      return `${id} - ${booking.propertyName || 'N/A'} (${booking.checkInDate || 'N/A'})`
    case 'FLIGHT':
      return `${id} - ${booking.departureCity || 'N/A'} to ${booking.arrivalCity || 'N/A'}`
    case 'RENTAL':
      return `${id} - ${booking.brand || 'N/A'} ${booking.model || 'N/A'}`
    case 'TOUR':
      return `${id} - ${booking.packageName || 'N/A'}`
    case 'INSURANCE':
      return `${id} - ${booking.service || 'N/A'} Policy`
    default:
      return id
  }
}

// Navigation
const nextStep = async () => {
  if (step.value === 1) {
    step.value = 2
    await fetchBookings(formData.value.serviceSource)
  } else if (step.value === 2) {
    step.value = 3
  }
}

const prevStep = () => {
  if (step.value > 1) {
    step.value--
  }
}

// Submit ticket
const submitTicket = async () => {
  submitting.value = true
  submitError.value = ''

  try {
    await supportTicketService.createTicket(formData.value)
    emit('ticket-created')
  } catch (err: any) {
    submitError.value = err.response?.data?.error || 'Failed to create ticket'
    console.error('Error creating ticket:', err)
  } finally {
    submitting.value = false
  }
}

// Handle overlay click
const handleOverlayClick = () => {
  emit('close')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h2 {
  margin: 0;
  font-size: 24px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 30px;
  cursor: pointer;
  color: #666;
}

.close-btn:hover {
  color: #000;
}

.modal-body {
  padding: 20px;
}

.form-step h3 {
  margin-bottom: 20px;
  font-size: 18px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
}

.form-control:focus {
  outline: none;
  border-color: #007bff;
}

textarea.form-control {
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn:hover:not(:disabled) {
  opacity: 0.8;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.loading-bookings,
.error,
.no-bookings {
  padding: 15px;
  text-align: center;
  color: #666;
}

.error {
  color: #dc3545;
}

.error-message {
  margin-top: 15px;
  padding: 10px;
  background-color: #f8d7da;
  color: #721c24;
  border-radius: 5px;
  text-align: center;
}
</style>
