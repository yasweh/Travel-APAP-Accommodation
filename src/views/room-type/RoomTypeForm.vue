<template>
  <div class="room-type-form">
    <h1>Add Room Types for Property</h1>

    <div v-if="error" class="error-message">{{ error }}</div>
    <div v-if="successMessage" class="success-message">{{ successMessage }}</div>

    <form v-if="!loading" @submit.prevent="submitForm" class="form-container">
      <div class="form-group">
        <label>Property *</label>
        <input type="text" :value="propertyName" readonly disabled />
        <small>Property ID: {{ propertyId }}</small>
      </div>

      <!-- Room Types List -->
      <div v-for="(roomType, index) in formData.roomTypes" :key="index" class="room-type-section">
        <div class="room-type-header">
          <h3>Room Type {{ index + 1 }}</h3>
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
            <input
              type="text"
              v-model="roomType.name"
              required
              placeholder="e.g., Deluxe Room, Suite"
            />
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
    } else {
      error.value = response.message || 'Failed to load property'
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load property'
  } finally {
    loading.value = false
  }
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

