<template>
  <div class="booking-update">
    <h1>Update Booking</h1>

    <!-- Error Message -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- Success Message -->
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">Loading booking data...</div>

    <!-- Form -->
    <form v-else @submit.prevent="submitForm" class="form-container">
      <!-- Property Dropdown -->
      <div class="form-group">
        <label for="property">Property *</label>
        <select 
          id="property" 
          v-model="selectedPropertyId" 
          @change="onPropertyChange" 
          required
        >
          <option value="">-- Select Property --</option>
          <option 
            v-for="prop in properties" 
            :key="prop.propertyId" 
            :value="prop.propertyId"
          >
            {{ prop.propertyName }}
          </option>
        </select>
      </div>

      <!-- Room Type Dropdown -->
      <div class="form-group">
        <label for="roomType">Room Type *</label>
        <select 
          id="roomType" 
          v-model="selectedRoomTypeId" 
          @change="onRoomTypeChange" 
          required
          :disabled="!selectedPropertyId"
        >
          <option value="">-- Select Room Type --</option>
          <option 
            v-for="rt in roomTypes" 
            :key="rt.roomTypeId" 
            :value="rt.roomTypeId"
          >
            {{ rt.roomTypeName }}
          </option>
        </select>
      </div>

      <!-- Room Name Dropdown -->
      <div class="form-group">
        <label for="room">Room Name *</label>
        <select 
          id="room" 
          v-model="formData.roomId" 
          @change="onRoomChange" 
          required
          :disabled="!selectedRoomTypeId"
        >
          <option value="">-- Select Room --</option>
          <option 
            v-for="room in availableRooms" 
            :key="room.roomId" 
            :value="room.roomId"
          >
            {{ room.name }}
          </option>
        </select>
      </div>

      <!-- Capacity -->
      <div class="form-group">
        <label for="capacity">Capacity *</label>
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

      <!-- Check-in Date -->
      <div class="form-group">
        <label for="checkInDate">Check-in Date *</label>
        <input
          type="date"
          id="checkInDate"
          v-model="formData.checkInDate"
          required
          :min="minDate"
        />
      </div>

      <!-- Check-out Date -->
      <div class="form-group">
        <label for="checkOutDate">Check-out Date *</label>
        <input
          type="date"
          id="checkOutDate"
          v-model="formData.checkOutDate"
          required
          :min="formData.checkInDate"
        />
      </div>

      <!-- Customer ID (Disabled) -->
      <div class="form-group">
        <label for="customerId">Customer ID *</label>
        <input
          type="text"
          id="customerId"
          v-model="formData.customerId"
          required
          disabled
        />
      </div>

      <!-- Customer Name -->
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

      <!-- Customer Email -->
      <div class="form-group">
        <label for="customerEmail">Customer Email *</label>
        <input
          type="email"
          id="customerEmail"
          v-model="formData.customerEmail"
          required
          placeholder="customer@example.com"
        />
      </div>

      <!-- Customer Phone -->
      <div class="form-group">
        <label for="customerPhone">Customer Phone *</label>
        <input
          type="tel"
          id="customerPhone"
          v-model="formData.customerPhone"
          required
          placeholder="081234567890"
        />
      </div>

      <!-- Breakfast Dropdown -->
      <div class="form-group">
        <label for="breakfast">Breakfast (+ Rp50.000) *</label>
        <select id="breakfast" v-model="formData.addOnBreakfast" required>
          <option :value="true">Yes</option>
          <option :value="false">No</option>
        </select>
      </div>

      <!-- Form Actions -->
      <div class="form-actions">
        <button type="button" @click="goBack" class="btn-secondary">Back</button>
        <button type="submit" class="btn-primary" :disabled="submitting">
          {{ submitting ? 'Saving...' : 'Save' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { bookingService, type BookingRequestDTO, type RoomDTO } from '@/services/bookingService'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const successMessage = ref('')

const properties = ref<any[]>([])
const roomTypes = ref<any[]>([])
const availableRooms = ref<RoomDTO[]>([])

const selectedPropertyId = ref('')
const selectedRoomTypeId = ref('')
const selectedRoom = ref<RoomDTO | null>(null)

const formData = reactive({
  roomId: '',
  checkInDate: '',
  checkOutDate: '',
  customerId: '',
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

// Load existing booking data
const loadBooking = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const bookingId = route.params.id as string
    const response = await bookingService.getForUpdate(bookingId)
    
    if (response.success && response.data) {
      const booking = response.data
      
      // Populate form
      formData.roomId = booking.roomId
      formData.checkInDate = booking.checkInDate.split('T')[0]
      formData.checkOutDate = booking.checkOutDate.split('T')[0]
      formData.customerId = booking.customerId
      formData.customerName = booking.customerName
      formData.customerEmail = booking.customerEmail
      formData.customerPhone = booking.customerPhone
      formData.capacity = booking.capacity
      formData.addOnBreakfast = booking.addOnBreakfast
      
      // Set selected IDs for dropdowns
      selectedPropertyId.value = booking.propertyId || ''
      selectedRoomTypeId.value = booking.roomTypeId || ''
      
      // Load cascading data
      await loadProperties()
      if (selectedPropertyId.value) {
        await onPropertyChange()
      }
      if (selectedRoomTypeId.value) {
        await onRoomTypeChange()
      }
    } else {
      error.value = response.message || 'Failed to load booking'
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load booking'
    console.error('Load error:', err)
  } finally {
    loading.value = false
  }
}

const loadProperties = async () => {
  try {
    const response = await bookingService.getPropertiesForBooking()
    if (response.success) {
      properties.value = response.data
    }
  } catch (err: any) {
    console.error('Failed to load properties:', err)
  }
}

const onPropertyChange = async () => {
  roomTypes.value = []
  availableRooms.value = []
  selectedRoomTypeId.value = ''
  formData.roomId = ''
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
  availableRooms.value = []
  formData.roomId = ''
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
}

const submitForm = async () => {
  // Validation: Check-out must be after check-in (minimum 1 day)
  if (formData.checkInDate && formData.checkOutDate) {
    const checkIn = new Date(formData.checkInDate)
    const checkOut = new Date(formData.checkOutDate)
    const diffDays = Math.ceil((checkOut.getTime() - checkIn.getTime()) / (1000 * 60 * 60 * 24))
    
    if (diffDays < 1) {
      error.value = 'Check-out date must be at least 1 day after check-in date'
      return
    }
  }

  // Validation: Check-in must be today or later
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const checkInDate = new Date(formData.checkInDate)
  checkInDate.setHours(0, 0, 0, 0)
  
  if (checkInDate < today) {
    error.value = 'Check-in date must be today or later'
    return
  }

  // Validation: Capacity must not exceed room capacity
  if (selectedRoom.value && formData.capacity > selectedRoom.value.roomTypeCapacity) {
    error.value = `Number of guests cannot exceed room capacity (${selectedRoom.value.roomTypeCapacity} people)`
    return
  }

  // Validation: No empty fields
  if (!formData.roomId || !formData.checkInDate || !formData.checkOutDate || 
      !formData.customerName || !formData.customerEmail || !formData.customerPhone) {
    error.value = 'Please fill in all required fields'
    return
  }

  submitting.value = true
  error.value = ''
  successMessage.value = ''

  try {
    const bookingId = route.params.id as string
    const bookingData: BookingRequestDTO = {
      roomId: formData.roomId,
      propertyId: selectedPropertyId.value,
      roomTypeId: selectedRoomTypeId.value,
      checkInDate: formData.checkInDate + 'T00:00:00',
      checkOutDate: formData.checkOutDate + 'T00:00:00',
      customerId: formData.customerId,
      customerName: formData.customerName,
      customerEmail: formData.customerEmail,
      customerPhone: formData.customerPhone,
      capacity: formData.capacity,
      addOnBreakfast: formData.addOnBreakfast,
    }

    const response = await bookingService.update(bookingId, bookingData)
    if (response.success) {
      successMessage.value = 'Booking updated successfully!'
      setTimeout(() => {
        router.push('/booking')
      }, 1500)
    } else {
      error.value = response.message
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to update booking'
    console.error('Submit error:', err)
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/booking')
}

onMounted(() => {
  loadBooking()
})
</script>

<style scoped>
.booking-update {
  padding: 20px;
  max-width: 800px;
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

.success-message {
  background-color: #efe;
  color: #3c3;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #666;
  font-size: 18px;
}

.form-container {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #4caf50;
}

.form-group select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
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
