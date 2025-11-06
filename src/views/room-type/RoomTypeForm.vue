<template>
  <div class="room-type-form">
    <h1>Add Room Types for Property</h1>

    <div v-if="error" class="error-message">{{ error }}</div>
    <div v-if="successMessage" class="success-message">{{ successMessage }}</div>

    <form v-if="!loading" @submit.prevent="submitForm" class="form-container">
      <div class="form-group">
        <label>Property *</label>
        <input type="text" :value="propertyName" readonly disabled />
        <small>Property ID: {{ propertyId }} | Type: {{ getPropertyTypeString() }}</small>
      </div>

      <!-- Show Existing Room Types Info -->
      <div v-if="existingRoomTypes.length > 0" class="info-section">
        <h3>Existing Room Types ({{ existingRoomTypes.length }})</h3>
        <div class="existing-room-types">
          <div v-for="rt in existingRoomTypes" :key="rt.roomTypeId" class="existing-room-type-card">
            <div class="card-header">
              <strong>{{ rt.name }}</strong>
              <span class="badge">Floor {{ rt.floor }}</span>
            </div>
            <div class="card-body">
              <p><strong>Rooms:</strong> {{ rt.listRoom?.length || 0 }} rooms</p>
              <p><strong>Capacity:</strong> {{ rt.capacity }} persons | <strong>Price:</strong> Rp {{ rt.price?.toLocaleString() }}</p>
              <p><strong>Facility:</strong> {{ rt.facility }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Add New Room Types Section -->
      <div class="section-title">Add New Room Types</div>
      <p class="section-note">Add new room types to this property</p>

      <!-- Room Types List -->
      <div v-for="(roomType, index) in formData.roomTypes" :key="index" class="room-type-section">
        <div class="room-type-header">
          <h3>New Room Type {{ index + 1 }}</h3>
          <button 
            v-if="formData.roomTypes.length > 1" 
            type="button" 
            @click="removeRoomType(index)" 
            class="btn-remove"
          >
            Remove
          </button>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Room Type Name *</label>
            <select v-model="roomType.name" required>
              <option value="">Select Room Type</option>
              <optgroup v-if="propertyType === 0" label="Hotel Room Types">
                <option value="Single Room">Single Room</option>
                <option value="Double Room">Double Room</option>
                <option value="Deluxe Room">Deluxe Room</option>
                <option value="Superior Room">Superior Room</option>
                <option value="Suite">Suite</option>
                <option value="Family Room">Family Room</option>
              </optgroup>
              <optgroup v-if="propertyType === 1" label="Villa Room Types">
                <option value="Luxury">Luxury</option>
                <option value="Beachfront">Beachfront</option>
                <option value="Mountside">Mountside</option>
                <option value="Eco-friendly">Eco-friendly</option>
                <option value="Romantic">Romantic</option>
              </optgroup>
              <optgroup v-if="propertyType === 2" label="Apartment Room Types">
                <option value="Studio">Studio</option>
                <option value="1BR">1BR</option>
                <option value="2BR">2BR</option>
                <option value="3BR">3BR</option>
                <option value="Penthouse">Penthouse</option>
              </optgroup>
            </select>
          </div>

          <div class="form-group">
            <label>Facility *</label>
            <input
              type="text"
              v-model="roomType.facility"
              required
              placeholder="e.g., AC, WiFi, TV"
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Capacity (persons) *</label>
            <input
              type="number"
              v-model.number="roomType.capacity"
              required
              min="1"
              placeholder="0"
            />
          </div>

          <div class="form-group">
            <label>Price (IDR) *</label>
            <input
              type="number"
              v-model.number="roomType.price"
              required
              min="0"
              placeholder="0"
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Floor *</label>
            <input
              type="number"
              v-model.number="roomType.floor"
              required
              min="0"
              placeholder="0"
            />
          </div>

          <div class="form-group">
            <label>Number of Rooms *</label>
            <input
              type="number"
              v-model.number="roomType.roomCount"
              required
              min="1"
              placeholder="1"
            />
          </div>
        </div>

        <div class="form-group">
          <label>Description</label>
          <textarea
            v-model="roomType.description"
            rows="2"
            placeholder="Room type description"
          ></textarea>
        </div>
      </div>

      <button type="button" @click="addRoomType" class="btn-add-room">
        + Add Another Room Type
      </button>

      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="submitting">
          {{ submitting ? 'Saving...' : 'Save Room Types' }}
        </button>
        <button type="button" @click="goBack" class="btn-secondary">Back</button>
      </div>
    </form>

    <div v-else class="loading">Loading...</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { propertyService } from '@/services/propertyService'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()

interface RoomTypeForm {
  name: string
  facility: string
  capacity: number
  price: number
  floor: number
  roomCount: number
  description: string
}

const propertyId = ref('')
const propertyName = ref('')
const propertyType = ref<number | null>(null)
const existingRoomTypes = ref<any[]>([])
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const successMessage = ref('')

const formData = ref({
  roomTypes: [
    {
      name: '',
      facility: '',
      capacity: 0,
      price: 0,
      floor: 0,
      roomCount: 1,
      description: ''
    }
  ] as RoomTypeForm[]
})

const addRoomType = () => {
  formData.value.roomTypes.push({
    name: '',
    facility: '',
    capacity: 0,
    price: 0,
    floor: 0,
    roomCount: 1,
    description: ''
  })
}

const removeRoomType = (index: number) => {
  if (formData.value.roomTypes.length > 1) {
    formData.value.roomTypes.splice(index, 1)
  }
}

const loadProperty = async () => {
  try {
    loading.value = true
    const id = route.query.propertyId as string || route.params.propertyId as string
    
    if (!id) {
      error.value = 'Property ID is required'
      return
    }

    propertyId.value = id
    const response = await propertyService.getById(id)
    
    if (response.success) {
      // Backend returns: { success, property, roomTypes, rooms }
      propertyName.value = response.property.propertyName
      propertyType.value = response.property.type
      
      // Load existing room types
      if (response.roomTypes && response.roomTypes.length > 0) {
        existingRoomTypes.value = response.roomTypes
      }
    } else {
      error.value = response.message || 'Failed to load property'
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load property'
  } finally {
    loading.value = false
  }
}

const getPropertyTypeString = () => {
  if (propertyType.value === 0) return 'Hotel'
  if (propertyType.value === 1) return 'Villa'
  if (propertyType.value === 2) return 'Apartemen'
  return 'Unknown'
}

const submitForm = async () => {
  // Validation
  if (formData.value.roomTypes.length === 0) {
    error.value = 'At least one room type is required'
    return
  }

  for (let i = 0; i < formData.value.roomTypes.length; i++) {
    const rt = formData.value.roomTypes[i]
    if (rt && (!rt.name || !rt.facility || rt.capacity <= 0 || rt.price <= 0 || rt.floor < 0 || rt.roomCount <= 0)) {
      error.value = `Please fill all required fields for Room Type ${i + 1}`
      return
    }
  }

  try {
    submitting.value = true
    error.value = ''

    const payload = {
      propertyId: propertyId.value,
      roomTypes: formData.value.roomTypes
    }

    const response = await api.post('/property/updateroom', payload)
    
    if (response.data.success) {
      successMessage.value = 'Room types added successfully!'
      setTimeout(() => {
        router.push(`/property/${propertyId.value}`)
      }, 1500)
    } else {
      error.value = response.data.message
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to add room types'
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push(`/property/${propertyId.value}`)
}

onMounted(() => {
  loadProperty()
})
</script>

<style scoped>
.room-type-form {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  color: #333;
  margin-bottom: 30px;
}

.error-message {
  background: #fee;
  color: #c33;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.success-message {
  background: #efe;
  color: #3c3;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 20px;
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.info-section {
  background: #f0f8ff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  border-left: 4px solid #2196f3;
}

.info-section h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
}

.existing-room-types {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.existing-room-type-card {
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  overflow: hidden;
}

.existing-room-type-card .card-header {
  background: #2196f3;
  color: white;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.existing-room-type-card .badge {
  background: rgba(255, 255, 255, 0.3);
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 12px;
}

.existing-room-type-card .card-body {
  padding: 15px;
}

.existing-room-type-card .card-body p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 30px 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #4caf50;
}

.section-note {
  color: #666;
  font-size: 14px;
  margin: -10px 0 20px 0;
  font-style: italic;
}

.form-group {
  margin-bottom: 20px;
}

.form-group small {
  color: #666;
  font-size: 12px;
  display: block;
  margin-top: 5px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

input,
textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 14px;
}

input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

input:focus,
textarea:focus {
  outline: none;
  border-color: #4caf50;
}

.room-type-section {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #4caf50;
}

.room-type-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.room-type-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.btn-remove {
  padding: 6px 12px;
  background: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.btn-remove:hover {
  background: #da190b;
}

.btn-add-room {
  width: 100%;
  padding: 12px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 20px;
}

.btn-add-room:hover {
  background: #0b7dda;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 2px solid #eee;
}

.btn-primary,
.btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.btn-primary {
  background: #4caf50;
  color: white;
  flex: 1;
}

.btn-primary:hover:not(:disabled) {
  background: #45a049;
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.btn-secondary {
  background: #f0f0f0;
  color: #333;
}

.btn-secondary:hover {
  background: #e0e0e0;
}
</style>

