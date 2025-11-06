<template>
  <div class="booking-create">
    <h1>Create New Booking</h1>

    <!-- Error Message -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- Success Message -->
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>

    <!-- Form -->
    <form @submit.prevent="submitForm" class="form-container">
      <!-- Mode Indicator -->
      <div v-if="isRoomPrefilled" class="mode-indicator">
        <span class="badge">📍 Booking with Pre-selected Room</span>
      </div>

      <!-- Cascading Dropdown: Property -> Room Type -> Room (only if not prefilled) -->
      <div v-if="!isRoomPrefilled">
        <div class="form-group">
          <label for="property">Select Property *</label>
          <select id="property" v-model="selectedPropertyId" @change="onPropertyChange" required>
            <option value="">-- Select Property --</option>
            <option v-for="prop in properties" :key="prop.propertyId" :value="prop.propertyId">
              {{ prop.propertyName }}
            </option>
          </select>
        </div>

        <div class="form-group" v-if="selectedPropertyId">
          <label for="roomType">Select Room Type *</label>
          <select id="roomType" v-model="selectedRoomTypeId" @change="onRoomTypeChange" required>
            <option value="">-- Select Room Type --</option>
            <option v-for="rt in roomTypes" :key="rt.roomTypeId" :value="rt.roomTypeId">
              {{ rt.roomTypeName }} (Floor {{ rt.floor }}, Rp {{ formatCurrency(rt.price) }}/night)
            </option>
          </select>
        </div>

        <div class="form-group" v-if="selectedRoomTypeId">
          <label for="room">Select Room *</label>
          <select id="room" v-model="formData.roomId" @change="onRoomChange" required>
            <option value="">-- Select Room --</option>
            <option v-for="room in availableRooms" :key="room.roomId" :value="room.roomId">
              {{ room.name }} (Capacity: {{ room.roomTypeCapacity }})
            </option>
          </select>
        </div>
      </div>

      <!-- Room Details (when selected) -->
      <div v-if="selectedRoom" class="room-details">
        <h3>Room Details</h3>
        <p><strong>Room:</strong> {{ selectedRoom.name }}</p>
        <p><strong>Type:</strong> {{ selectedRoom.roomTypeName }}</p>
        <p><strong>Price:</strong> Rp {{ formatCurrency(selectedRoom.roomTypePrice) }}/night</p>
        <p><strong>Capacity:</strong> {{ selectedRoom.roomTypeCapacity }} people</p>
        <p><strong>Facilities:</strong> {{ selectedRoom.roomTypeFacility }}</p>
      </div>

      <!-- Booking Dates -->
      <div class="form-row">
        <div class="form-group">
          <label for="checkInDate">Check-in Date & Time *</label>
          <input
            type="datetime-local"
            id="checkInDate"
            v-model="formData.checkInDate"
            required
            :min="minDate"
          />
        </div>
        <div class="form-group">
          <label for="checkOutDate">Check-out Date & Time *</label>
          <input
            type="datetime-local"
            id="checkOutDate"
            v-model="formData.checkOutDate"
            required
            :min="formData.checkInDate"
          />
        </div>
      </div>

      <!-- Customer Information -->
      <h3>Customer Information</h3>
      <div class="form-group">
        <label for="customerId">Customer ID (UUID) *</label>
        <input
          type="text"
          id="customerId"
          v-model="formData.customerId"
          required
          placeholder="Auto-generated UUID"
          :disabled="true"
        />
        <small style="color: #666;">Auto-generated UUID for customer identification</small>
      </div>

      <div class="form-group">
        <label for="customerName">Customer Name *</label>
        <input
          type="text"
          id="customerName"
          v-model="formData.customerName"
          required
          placeholder="Enter customer name"
        />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label for="customerEmail">Email *</label>
          <input
            type="email"
            id="customerEmail"
            v-model="formData.customerEmail"
            required
            placeholder="customer@example.com"
          />
        </div>
        <div class="form-group">
          <label for="customerPhone">Phone *</label>
          <input
            type="tel"
            id="customerPhone"
            v-model="formData.customerPhone"
            required
            placeholder="081234567890"
          />
        </div>
      </div>

      <!-- Additional Options -->
      <div class="form-group">
        <label for="capacity">Number of Guests *</label>
        <input
          type="number"
          id="capacity"
          v-model.number="formData.capacity"
          required
          min="1"
          :max="selectedRoom?.roomTypeCapacity || 999"
        />
        <small v-if="selectedRoom" style="color: #666;">
          Maximum capacity: {{ selectedRoom.roomTypeCapacity }} person(s)
        </small>
      </div>

      <div class="form-group checkbox-group">
        <label>
          <input type="checkbox" v-model="formData.addOnBreakfast" />
          Add Breakfast (Rp 50,000 per person per day)
        </label>
      </div>

      <!-- Price Calculation (Preview) -->
      <div v-if="pricePreview" class="price-preview">
        <h3>Price Calculation</h3>
        <p><strong>Duration:</strong> {{ pricePreview.days }} days</p>
        <p><strong>Base Price:</strong> Rp {{ formatCurrency(pricePreview.basePrice) }}</p>
        <p v-if="formData.addOnBreakfast">
          <strong>Breakfast:</strong> Rp {{ formatCurrency(pricePreview.breakfastPrice) }}
        </p>
        <p class="total-price"><strong>Total:</strong> Rp {{ formatCurrency(pricePreview.total) }}</p>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="submitting || !formData.roomId">
          {{ submitting ? 'Creating...' : 'Create Booking' }}
        </button>
        <button type="button" @click="goBack" class="btn-secondary">Cancel</button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { bookingService, type BookingRequestDTO, type RoomDTO } from '@/services/bookingService'
import { v4 as uuidv4 } from 'uuid'

const router = useRouter()
const route = useRoute()

const submitting = ref(false)
const error = ref('')
const successMessage = ref('')

const properties = ref<any[]>([])
const roomTypes = ref<any[]>([])
const availableRooms = ref<RoomDTO[]>([])

const selectedPropertyId = ref('')
const selectedRoomTypeId = ref('')
const selectedRoom = ref<RoomDTO | null>(null)

// Check if room is prefilled from query params (booking from property detail)
const prefilledRoomId = route.query.roomId as string | undefined
const isRoomPrefilled = ref(!!prefilledRoomId)

const formData = reactive({
  roomId: prefilledRoomId || '',
  checkInDate: '',
  checkOutDate: '',
  customerId: uuidv4(), // Auto-generate
  customerName: '',
  customerEmail: '',
  customerPhone: '',
  capacity: 1,
  addOnBreakfast: false,
})

const minDate = computed(() => {
  const today = new Date()
  today.setMinutes(today.getMinutes() - today.getTimezoneOffset())
  return today.toISOString().slice(0, 16)
})

const pricePreview = computed(() => {
  if (!selectedRoom.value || !formData.checkInDate || !formData.checkOutDate) return null

  const checkIn = new Date(formData.checkInDate)
  const checkOut = new Date(formData.checkOutDate)
  const days = Math.ceil((checkOut.getTime() - checkIn.getTime()) / (1000 * 60 * 60 * 24))

  if (days < 1) return null

  const basePrice = selectedRoom.value.roomTypePrice * days
  const breakfastPrice = formData.addOnBreakfast ? 50000 * days : 0
  const total = basePrice + breakfastPrice

  return { days, basePrice, breakfastPrice, total }
})

const loadProperties = async () => {
  try {
    const response = await bookingService.getPropertiesForBooking()
    if (response.success) {
      properties.value = response.data
    }
  } catch (err: any) {
    error.value = 'Failed to load properties'
    console.error(err)
  }
}

const onPropertyChange = async () => {
  selectedRoomTypeId.value = ''
  formData.roomId = ''
  roomTypes.value = []
  availableRooms.value = []
  selectedRoom.value = null

  if (!selectedPropertyId.value) return

  try {
    const response = await bookingService.getRoomTypesByProperty(selectedPropertyId.value)
    if (response.success) {
      roomTypes.value = response.data
    }
  } catch (err: any) {
    error.value = 'Failed to load room types'
    console.error(err)
  }
}

const onRoomTypeChange = async () => {
  formData.roomId = ''
  availableRooms.value = []
  selectedRoom.value = null

  if (!selectedRoomTypeId.value) return

  try {
    const response = await bookingService.getAvailableRooms(
      selectedPropertyId.value,
      selectedRoomTypeId.value
    )
    if (response.success) {
      availableRooms.value = response.data
    }
  } catch (err: any) {
    error.value = 'Failed to load available rooms'
    console.error(err)
  }
}

const onRoomChange = () => {
  selectedRoom.value = availableRooms.value.find((r) => r.roomId === formData.roomId) || null
  if (selectedRoom.value) {
    formData.capacity = 1 // Reset to 1 when room changes
  }
}

const submitForm = async () => {
  // Validate check-out > check-in (minimum 1 day)
  if (formData.checkInDate && formData.checkOutDate) {
    const checkIn = new Date(formData.checkInDate)
    const checkOut = new Date(formData.checkOutDate)
    const diffDays = Math.ceil((checkOut.getTime() - checkIn.getTime()) / (1000 * 60 * 60 * 24))
    
    if (diffDays < 1) {
      error.value = 'Check-out date must be at least 1 day after check-in date'
      return
    }
  }

  // Validate capacity doesn't exceed room type capacity
  if (selectedRoom.value && formData.capacity > selectedRoom.value.roomTypeCapacity) {
    error.value = `Number of guests cannot exceed room capacity (${selectedRoom.value.roomTypeCapacity} people)`
    return
  }

  submitting.value = true
  error.value = ''
  successMessage.value = ''

  try {
    const bookingData: BookingRequestDTO = {
      roomId: formData.roomId,
      propertyId: selectedPropertyId.value,
      roomTypeId: selectedRoomTypeId.value,
      checkInDate: formData.checkInDate,
      checkOutDate: formData.checkOutDate,
      customerId: formData.customerId,
      customerName: formData.customerName,
      customerEmail: formData.customerEmail,
      customerPhone: formData.customerPhone,
      capacity: formData.capacity,
      addOnBreakfast: formData.addOnBreakfast,
    }

    const response = await bookingService.create(bookingData)
    if (response.success) {
      successMessage.value = 'Booking created successfully!'
      setTimeout(() => {
        router.push('/booking')
      }, 1500)
    } else {
      error.value = response.message
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to create booking'
    console.error('Submit error:', err)
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/booking')
}

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('id-ID').format(value)
}

onMounted(() => {
  loadProperties()
  
  // If room is prefilled, load room details
  if (prefilledRoomId) {
    loadPrefilledRoom()
  }
})

// Load prefilled room details when coming from property detail
const loadPrefilledRoom = async () => {
  try {
    const response = await bookingService.getRoomDetails(prefilledRoomId!)
    if (response.success && response.data) {
      selectedRoom.value = response.data
      selectedPropertyId.value = response.data.propertyId || ''
      selectedRoomTypeId.value = response.data.roomTypeId || ''
      formData.capacity = 1
    }
  } catch (err: any) {
    error.value = 'Failed to load room details'
    console.error(err)
  }
}
</script>

<style scoped>
.booking-create {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

h3 {
  color: #555;
  margin-top: 25px;
  margin-bottom: 15px;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.success-message {
  background-color: #efe;
  color: #3c3;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.form-container {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.mode-indicator {
  margin-bottom: 20px;
  padding: 12px;
  background-color: #e3f2fd;
  border-left: 4px solid #2196f3;
  border-radius: 4px;
}

.mode-indicator .badge {
  font-weight: bold;
  color: #1976d2;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #4caf50;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  font-weight: normal;
  cursor: pointer;
}

.checkbox-group input[type='checkbox'] {
  width: auto;
  margin-right: 8px;
}

.room-details {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.room-details h3 {
  margin-top: 0;
}

.room-details p {
  margin: 8px 0;
}

.price-preview {
  background-color: #e3f2fd;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.price-preview h3 {
  margin-top: 0;
}

.price-preview p {
  margin: 8px 0;
}

.total-price {
  font-size: 18px;
  color: #4caf50;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 30px;
}

.btn-primary,
.btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
}

.btn-primary {
  background-color: #4caf50;
  color: white;
  flex: 1;
}

.btn-primary:hover:not(:disabled) {
  background-color: #45a049;
}

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #999;
  color: white;
}

.btn-secondary:hover {
  background-color: #777;
}
</style>
