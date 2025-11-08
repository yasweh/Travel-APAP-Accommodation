<template>
  <div class="booking-create">
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
          <h1 class="page-title">Create New Booking</h1>
          <p class="page-subtitle">Book a room for your customer</p>
        </div>
      </div>
    </div>

    <!-- Messages -->
    <div v-if="error" class="message-box error">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM11 15H9V13H11V15ZM11 11H9V5H11V11Z" fill="#F44336"/>
      </svg>
      <span>{{ error }}</span>
    </div>

    <div v-if="successMessage" class="message-box success">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM8 15L3 10L4.41 8.59L8 12.17L15.59 4.58L17 6L8 15Z" fill="#4CAF50"/>
      </svg>
      <span>{{ successMessage }}</span>
    </div>

    <!-- Form -->
    <form @submit.prevent="submitForm" class="form-container">
      <!-- Mode Indicator -->
      <div v-if="isRoomPrefilled" class="mode-badge">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM8 15L3 10L4.41 8.59L8 12.17L15.59 4.58L17 6L8 15Z" fill="#4CAF50"/>
        </svg>
        <span>Booking with Pre-selected Room</span>
      </div>

      <!-- Room Selection Card -->
      <div v-if="!isRoomPrefilled" class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V5H19V19Z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">Room Selection</h2>
        </div>

        <div class="form-group">
          <label for="property" class="required">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 0L0 5V7L8 12L16 7V5L8 0Z" fill="#7C6A46"/>
            </svg>
            Select Property
          </label>
          <select id="property" v-model="selectedPropertyId" @change="onPropertyChange" required>
            <option value="">-- Select Property --</option>
            <option v-for="prop in properties" :key="prop.propertyId" :value="prop.propertyId">
              {{ prop.propertyName }}
            </option>
          </select>
        </div>

        <div class="form-group" v-if="selectedPropertyId">
          <label for="roomType" class="required">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M14 2H12V0H10V2H6V0H4V2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2Z" fill="#7C6A46"/>
            </svg>
            Select Room Type
          </label>
          <select id="roomType" v-model="selectedRoomTypeId" @change="onRoomTypeChange" required>
            <option value="">-- Select Room Type --</option>
            <option v-for="rt in roomTypes" :key="rt.roomTypeId" :value="rt.roomTypeId">
              {{ rt.name }} (Floor {{ rt.floor }}, Rp {{ formatCurrency(rt.price) }}/night)
            </option>
          </select>
        </div>

        <div class="form-group" v-if="selectedRoomTypeId">
          <label for="room" class="required">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M2 0C0.9 0 0 0.9 0 2V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V2C16 0.9 15.1 0 14 0H2Z" fill="#7C6A46"/>
            </svg>
            Select Room
          </label>
          <select id="room" v-model="formData.roomId" @change="onRoomChange" required>
            <option value="">-- Select Room --</option>
            <option v-for="room in availableRooms" :key="room.roomId" :value="room.roomId">
              {{ room.name }} (Capacity: {{ room.roomTypeCapacity }})
            </option>
          </select>
        </div>
      </div>

      <!-- Room Details Card -->
      <div v-if="selectedRoom" class="room-details-card">
        <div class="details-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3Z" fill="#7C6A46"/>
          </svg>
          <h3>Selected Room Details</h3>
        </div>
        <div class="details-grid">
          <div class="detail-item">
            <span class="detail-label">Room</span>
            <span class="detail-value">{{ selectedRoom.name }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Type</span>
            <span class="detail-value">{{ selectedRoom.roomTypeName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Price</span>
            <span class="detail-value price">Rp {{ formatCurrency(selectedRoom.roomTypePrice) }}/night</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Capacity</span>
            <span class="detail-value">{{ selectedRoom.roomTypeCapacity }} people</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">Facilities</span>
            <span class="detail-value">{{ selectedRoom.roomTypeFacility }}</span>
          </div>
        </div>
      </div>

      <!-- Booking Dates Card -->
      <div class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">Booking Dates</h2>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="checkInDate" class="required">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M14 2H13V0H11V2H5V0H3V2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2Z" fill="#4CAF50"/>
              </svg>
              Check-in Date
            </label>
            <input
              type="date"
              id="checkInDate"
              v-model="formData.checkInDate"
              required
              :min="minDate"
            />
          </div>
          <div class="form-group">
            <label for="checkOutDate" class="required">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M14 2H13V0H11V2H5V0H3V2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2Z" fill="#F44336"/>
              </svg>
              Check-out Date
            </label>
            <input
              type="date"
              id="checkOutDate"
              v-model="formData.checkOutDate"
              required
              :min="formData.checkInDate"
            />
          </div>
        </div>
      </div>

      <!-- Customer Information Card -->
      <div class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 6C13.1 6 14 6.9 14 8C14 9.1 13.1 10 12 10C10.9 10 10 9.1 10 8C10 6.9 10.9 6 12 6ZM12 15C14.7 15 17.8 16.29 18 17H6C6.23 16.28 9.31 15 12 15ZM12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4ZM12 13C9.33 13 4 14.34 4 17V19H20V17C20 14.34 14.67 13 12 13Z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">Customer Information</h2>
        </div>

        <div class="form-group">
          <label for="customerId">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C12.42 16 16 12.42 16 8C16 3.58 12.42 0 8 0Z" fill="#999"/>
            </svg>
            Customer ID (UUID)
          </label>
          <input
            type="text"
            id="customerId"
            v-model="formData.customerId"
            required
            placeholder="Auto-generated UUID"
            :disabled="true"
            class="input-disabled"
          />
          <small class="helper-text">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M6 0C2.69 0 0 2.69 0 6C0 9.31 2.69 12 6 12C9.31 12 12 9.31 12 6C12 2.69 9.31 0 6 0Z" fill="#999"/>
            </svg>
            Auto-generated UUID for customer identification
          </small>
        </div>

        <div class="form-group">
          <label for="customerName" class="required">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 0C5.79 0 4 1.79 4 4C4 6.21 5.79 8 8 8C10.21 8 12 6.21 12 4C12 1.79 10.21 0 8 0ZM8 10C5.33 10 0 11.34 0 14V16H16V14C16 11.34 10.67 10 8 10Z" fill="#7C6A46"/>
            </svg>
            Customer Name
          </label>
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
            <label for="customerEmail" class="required">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M14 2H2C0.9 2 0 2.9 0 4V12C0 13.1 0.9 14 2 14H14C15.1 14 16 13.1 16 12V4C16 2.9 15.1 2 14 2ZM14 12H2V6L8 9L14 6V12ZM8 7L2 4H14L8 7Z" fill="#7C6A46"/>
              </svg>
              Email
            </label>
            <input
              type="email"
              id="customerEmail"
              v-model="formData.customerEmail"
              required
              placeholder="customer@example.com"
            />
          </div>
          <div class="form-group">
            <label for="customerPhone" class="required">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M3.2 7.2C4.8 10.4 7.6 13.2 10.8 14.8L13.2 12.4C13.5 12.1 13.9 12 14.3 12.1C15.5 12.5 16.8 12.7 18 12.7C18.6 12.7 19 13.1 19 13.7V18C19 18.6 18.6 19 18 19C8.1 19 0 10.9 0 1C0 0.4 0.4 0 1 0H5.5C6.1 0 6.5 0.4 6.5 1C6.5 2.2 6.7 3.4 7.1 4.6C7.2 5 7.1 5.4 6.8 5.7L4.4 8.1C5.2 9.8 6.2 11.3 7.6 12.4C8.7 13.8 10.2 14.8 11.9 15.6L14.3 13.2C14.6 12.9 15 12.8 15.4 12.9C16.6 13.3 17.8 13.5 19 13.5C19.6 13.5 20 13.9 20 14.5V19C20 19.6 19.6 20 19 20C8.5 20 0 11.5 0 1C0 0.4 0.4 0 1 0H5.5C6.1 0 6.5 0.4 6.5 1C6.5 2.2 6.7 3.4 7.1 4.6C7.2 5 7.1 5.4 6.8 5.7L3.2 7.2Z" fill="#7C6A46"/>
              </svg>
              Phone
            </label>
            <input
              type="tel"
              id="customerPhone"
              v-model="formData.customerPhone"
              required
              placeholder="081234567890"
            />
          </div>
        </div>
      </div>

      <!-- Additional Information Card -->
      <div class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V11H13V17ZM13 9H11V7H13V9Z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">Additional Information</h2>
        </div>

        <div class="form-group">
          <label for="capacity" class="required">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 0C5.79 0 4 1.79 4 4C4 6.21 5.79 8 8 8C10.21 8 12 6.21 12 4C12 1.79 10.21 0 8 0ZM8 10C5.33 10 0 11.34 0 14V16H16V14C16 11.34 10.67 10 8 10Z" fill="#7C6A46"/>
            </svg>
            Number of Guests
          </label>
          <input
            type="number"
            id="capacity"
            v-model.number="formData.capacity"
            required
            min="1"
            :max="selectedRoom?.roomTypeCapacity || 999"
          />
          <small v-if="selectedRoom" class="helper-text">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M6 0C2.69 0 0 2.69 0 6C0 9.31 2.69 12 6 12C9.31 12 12 9.31 12 6C12 2.69 9.31 0 6 0Z" fill="#999"/>
            </svg>
            Maximum capacity: {{ selectedRoom.roomTypeCapacity }} person(s)
          </small>
        </div>

        <div class="form-group checkbox-group">
          <label class="checkbox-label">
            <input type="checkbox" v-model="formData.addOnBreakfast" />
            <span class="checkbox-text">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M14 2H11L9 0H7L5 2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2Z" fill="#7C6A46"/>
              </svg>
              Add Breakfast (Rp 50,000 per person per day)
            </span>
          </label>
        </div>
      </div>

      <!-- Price Calculation -->
      <div v-if="pricePreview" class="price-preview-card">
        <div class="preview-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 20C7.59 20 4 16.41 4 12C4 7.59 7.59 4 12 4C16.41 4 20 7.59 20 12C20 16.41 16.41 20 12 20Z" fill="#7C6A46"/>
            <path d="M12.5 7H11V13L16.25 16.15L17 14.92L12.5 12.25V7Z" fill="#7C6A46"/>
          </svg>
          <h3>Price Calculation</h3>
        </div>
        <div class="preview-content">
          <div class="preview-item">
            <span class="preview-label">Duration</span>
            <span class="preview-value">{{ pricePreview.days }} days</span>
          </div>
          <div class="preview-item">
            <span class="preview-label">Base Price</span>
            <span class="preview-value">Rp {{ formatCurrency(pricePreview.basePrice) }}</span>
          </div>
          <div v-if="formData.addOnBreakfast" class="preview-item">
            <span class="preview-label">Breakfast</span>
            <span class="preview-value">Rp {{ formatCurrency(pricePreview.breakfastPrice) }}</span>
          </div>
          <div class="preview-total">
            <span class="total-label">Total Price</span>
            <span class="total-value">Rp {{ formatCurrency(pricePreview.total) }}</span>
          </div>
        </div>
      </div>

      <!-- Form Actions -->
      <div class="form-actions">
        <button type="submit" class="btn-submit" :disabled="submitting || !formData.roomId">
          <svg v-if="!submitting" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM8 15L3 10L4.41 8.59L8 12.17L15.59 4.58L17 6L8 15Z" fill="white"/>
          </svg>
          <div v-else class="btn-spinner"></div>
          {{ submitting ? 'Creating...' : 'Create Booking' }}
        </button>
        <button type="button" @click="goBack" class="btn-cancel">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM15 13.59L13.59 15L10 11.41L6.41 15L5 13.59L8.59 10L5 6.41L6.41 5L10 8.59L13.59 5L15 6.41L11.41 10L15 13.59Z" fill="#666"/>
          </svg>
          Cancel
        </button>
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
  checkInDate: (route.query.checkIn as string) || '',
  checkOutDate: (route.query.checkOut as string) || '',
  customerId: uuidv4(), // Auto-generate
  customerName: '',
  customerEmail: '',
  customerPhone: '',
  capacity: 1,
  addOnBreakfast: false,
})

const minDate = computed(() => {
  const today = new Date()
  return today.toISOString().split('T')[0]
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

  // Load rooms based on dates if provided
  await loadAvailableRooms()
}

const loadAvailableRooms = async () => {
  if (!selectedRoomTypeId.value) return

  try {
    const response = await bookingService.getAvailableRooms(
      selectedPropertyId.value,
      selectedRoomTypeId.value,
      formData.checkInDate || undefined,
      formData.checkOutDate || undefined
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

// Watch for date changes and reload rooms
watch([() => formData.checkInDate, () => formData.checkOutDate], () => {
  if (selectedRoomTypeId.value && formData.checkInDate && formData.checkOutDate) {
    loadAvailableRooms()
  }
})

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
@import url('https://fonts.googleapis.com/css2?family=Raleway:wght@700;800&family=Poppins:wght@400;500;600&display=swap');

/* Container */
.booking-create-container {
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

/* Page Header */
.page-header {
  text-align: center;
  margin-bottom: 50px;
  animation: fadeIn 0.6s ease-in-out;
}

.header-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
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

.header-text h1 {
  font-family: 'Raleway', sans-serif;
  font-size: 2.5rem;
  font-weight: 800;
  color: #1C1C1C;
  margin: 0 0 10px 0;
  letter-spacing: -0.5px;
}

.header-text p {
  font-family: 'Poppins', sans-serif;
  font-size: 1.1rem;
  color: #4A4A4A;
  margin: 0;
}

/* Message Box */
.message-box {
  max-width: 900px;
  margin: 0 auto 30px;
  padding: 18px 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 15px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  animation: slideIn 0.4s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.message-box.error {
  background: linear-gradient(135deg, #FFF5F5 0%, #FFE5E5 100%);
  border-left: 4px solid #E53E3E;
  color: #C53030;
}

.message-box.success {
  background: linear-gradient(135deg, #F0FFF4 0%, #E5FFE9 100%);
  border-left: 4px solid #38A169;
  color: #276749;
}

.message-box svg {
  flex-shrink: 0;
}

/* Form Container */
.form-container {
  max-width: 900px;
  margin: 0 auto;
}

.booking-form {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

/* Mode Badge */
.mode-badge {
  background: linear-gradient(135deg, #E8E1D5 0%, #D4CFC5 100%);
  border: 2px solid #7C6A46;
  border-radius: 10px;
  padding: 15px 20px;
  text-align: center;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  color: #7C6A46;
  box-shadow: 0 4px 10px rgba(124, 106, 70, 0.15);
}

/* Form Card */
.form-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.form-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
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

/* Form Groups */
.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group label,
.form-row .form-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 600;
  color: #1C1C1C;
  margin-bottom: 10px;
}

.form-group label svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.form-group label .required {
  color: #E53E3E;
  margin-left: 4px;
}

/* Input Fields */
.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #E8E8E8;
  border-radius: 10px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  color: #1C1C1C;
  transition: all 0.3s ease;
  background: white;
}

.form-group input:hover,
.form-group select:hover,
.form-group textarea:hover {
  border-color: #7C6A46;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #7C6A46;
  box-shadow: 0 0 0 3px rgba(124, 106, 70, 0.1);
}

.form-group input:disabled,
.form-group select:disabled {
  background: #F5F5F5;
  cursor: not-allowed;
  opacity: 0.6;
}

.form-group select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='12' height='8' viewBox='0 0 12 8' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1L6 6L11 1' stroke='%237C6A46' stroke-width='2' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 16px center;
  padding-right: 45px;
}

.form-group small {
  display: block;
  margin-top: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
  color: #666;
}

/* Room Details Grid */
.room-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F5F5F5 100%);
  border-radius: 12px;
  border: 2px solid #E8E8E8;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.detail-label {
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
  font-weight: 600;
  color: #1C1C1C;
}

.detail-value.highlight {
  color: #7C6A46;
  font-size: 1.2rem;
}

/* Checkbox Group */
.checkbox-group {
  padding: 20px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F5F5F5 100%);
  border-radius: 12px;
  border: 2px solid #E8E8E8;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  color: #1C1C1C;
}

.checkbox-label input[type="checkbox"] {
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: #7C6A46;
}

.checkbox-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

/* Price Preview Card */
.price-preview-card {
  background: linear-gradient(135deg, #7C6A46 0%, #5A4A30 100%);
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 8px 25px rgba(124, 106, 70, 0.3);
  color: white;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
}

.preview-header svg path {
  fill: white !important;
}

.preview-header h3 {
  font-family: 'Raleway', sans-serif;
  font-size: 1.4rem;
  font-weight: 700;
  margin: 0;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.preview-label {
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 500;
  opacity: 0.9;
}

.preview-value {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 600;
}

.preview-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0 0 0;
  margin-top: 10px;
  border-top: 2px solid rgba(255, 255, 255, 0.3);
}

.total-label {
  font-family: 'Raleway', sans-serif;
  font-size: 1.2rem;
  font-weight: 700;
}

.total-value {
  font-family: 'Raleway', sans-serif;
  font-size: 1.6rem;
  font-weight: 800;
  color: #FFD700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

/* Form Actions */
.form-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
}

.btn-submit,
.btn-cancel {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 32px;
  border: none;
  border-radius: 12px;
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 180px;
}

.btn-submit {
  background: linear-gradient(135deg, #7C6A46 0%, #5A4A30 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(124, 106, 70, 0.3);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel {
  background: white;
  color: #666;
  border: 2px solid #E8E8E8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.btn-cancel:hover {
  background: #F5F5F5;
  border-color: #D0D0D0;
  transform: translateY(-2px);
}

.btn-spinner {
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Responsive Design */
@media (max-width: 768px) {
  .booking-create-container {
    padding: 40px 15px;
  }

  .header-text h1 {
    font-size: 2rem;
  }

  .header-text p {
    font-size: 1rem;
  }

  .form-card {
    padding: 20px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .room-details {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn-submit,
  .btn-cancel {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .header-icon {
    width: 60px;
    height: 60px;
  }

  .header-text h1 {
    font-size: 1.75rem;
  }

  .card-title {
    font-size: 1.2rem;
  }

  .form-card {
    padding: 15px;
  }
}
</style>
