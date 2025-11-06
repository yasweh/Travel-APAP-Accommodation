<template>
  <div class="property-form">
    <h1>{{ isEditMode ? 'Edit Property' : 'Add New Property' }}</h1>

    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <div v-if="successMessage" class="success-message">{{ successMessage }}</div>
    <div v-if="loading" class="loading">Loading...</div>

    <form v-else @submit.prevent="submitForm" class="form-container">
      <!-- Property Basic Info -->
      <div class="section-title">Property Information</div>
      
      <div v-if="isEditMode" class="form-group">
        <label>Property ID</label>
        <input type="text" :value="route.params.id" disabled class="disabled-input" />
      </div>

      <div class="form-group">
        <label>Property Name *</label>
        <input type="text" v-model="formData.propertyName" required placeholder="e.g., Grand Hotel Jakarta" />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>Type *</label>
          <select v-model.number="formData.type" required>
            <option value="" disabled>Select Type</option>
            <option value="0">Hotel</option>
            <option value="1">Villa</option>
            <option value="2">Apartemen</option>
          </select>
        </div>

        <div class="form-group">
          <label>Province *</label>
          <select v-model.number="formData.province" required>
            <option value="" disabled>Select Province</option>
            <option value="0">DKI Jakarta</option>
            <option value="1">Jawa Barat</option>
            <option value="2">Jawa Tengah</option>
            <option value="3">Jawa Timur</option>
            <option value="4">Bali</option>
            <option value="5">Sumatera Utara</option>
            <option value="6">Sulawesi Selatan</option>
            <option value="7">Kalimantan Timur</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label>Address *</label>
        <textarea v-model="formData.address" required rows="3" placeholder="Full address of the property"></textarea>
      </div>

      <div class="form-group">
        <label>Description</label>
        <textarea v-model="formData.description" rows="4" placeholder="Brief description about the property"></textarea>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>Owner Name *</label>
          <input type="text" v-model="formData.ownerName" required placeholder="Owner full name" />
        </div>

        <div class="form-group">
          <label>Owner ID (UUID) *</label>
          <input type="text" v-model="formData.ownerId" required readonly />
        </div>
      </div>

      <!-- Room Types Section -->
      <div class="section-title">Room Types *</div>
      <p class="section-note">At least one room type is required</p>

      <div v-for="(roomType, index) in formData.roomTypes" :key="index" class="room-type-section">
        <div class="room-type-header">
          <h3>Room Type {{ index + 1 }} {{ roomType.roomTypeId ? '(Existing)' : '(New)' }}</h3>
          <button 
            v-if="formData.roomTypes.length > 1 && (!isEditMode || !roomType.roomTypeId)" 
            type="button" 
            @click="removeRoomType(index)" 
            class="btn-remove"
          >
            Remove
          </button>
        </div>

        <!-- Show Room Type ID only for existing room types -->
        <div v-if="isEditMode && roomType.roomTypeId" class="form-group">
          <label>Room Type ID</label>
          <input type="text" :value="roomType.roomTypeId" disabled class="disabled-input" />
        </div>

        <div class="form-row">
          <!-- Show Room Type Name only for new room types, hide for existing ones -->
          <div v-if="!roomType.roomTypeId" class="form-group">
            <label>Room Type Name *</label>
            <select v-model="roomType.name" required>
              <option value="">Select Room Type</option>
              <optgroup v-if="formData.type === 0" label="Hotel Room Types">
                <option value="Single Room">Single Room</option>
                <option value="Double Room">Double Room</option>
                <option value="Deluxe Room">Deluxe Room</option>
                <option value="Superior Room">Superior Room</option>
                <option value="Suite">Suite</option>
                <option value="Family Room">Family Room</option>
              </optgroup>
              <optgroup v-if="formData.type === 1" label="Villa Room Types">
                <option value="Luxury">Luxury</option>
                <option value="Beachfront">Beachfront</option>
                <option value="Mountside">Mountside</option>
                <option value="Eco-friendly">Eco-friendly</option>
                <option value="Romantic">Romantic</option>
              </optgroup>
              <optgroup v-if="formData.type === 2" label="Apartment Room Types">
                <option value="Studio">Studio</option>
                <option value="1BR">1BR</option>
                <option value="2BR">2BR</option>
                <option value="3BR">3BR</option>
                <option value="Penthouse">Penthouse</option>
              </optgroup>
            </select>
          </div>
          
          <!-- Display read-only name for existing room types -->
          <div v-else class="form-group">
            <label>Room Type Name</label>
            <input type="text" :value="roomType.name" disabled class="disabled-input" />
          </div>

          <div class="form-group">
            <label>Facility *</label>
            <input type="text" v-model="roomType.facility" required placeholder="e.g., AC, WiFi, TV" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Capacity (persons) *</label>
            <input type="number" v-model.number="roomType.capacity" required min="1" placeholder="0" />
          </div>

          <div class="form-group">
            <label>Price (IDR) *</label>
            <input type="number" v-model.number="roomType.price" required min="0" placeholder="0" />
          </div>
        </div>

        <!-- Show Floor and Number of Rooms only for new room types (hide for existing) -->
        <div v-if="!roomType.roomTypeId" class="form-row">
          <div class="form-group">
            <label>Floor *</label>
            <input type="number" v-model.number="roomType.floor" required min="0" placeholder="0" />
          </div>

          <div class="form-group">
            <label>Number of Rooms *</label>
            <input type="number" v-model.number="roomType.roomCount" required min="1" placeholder="1" />
            <small class="helper-text">
              Rooms will be numbered based on floor. E.g., Floor 2 with 10 rooms → 201-210
            </small>
          </div>
        </div>

        <div class="form-group">
          <label>Description</label>
          <textarea v-model="roomType.description" rows="2" placeholder="Room type description"></textarea>
        </div>
      </div>

      <button type="button" @click="addRoomType" class="btn-add-room">+ Add Another Room Type</button>

      <!-- Form Actions -->
      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="submitting">
          {{ submitting ? 'Saving...' : (isEditMode ? 'Update Property' : 'Create Property') }}
        </button>
        <button type="button" @click="$router.push('/property')" class="btn-secondary">Cancel</button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { propertyService } from '@/services/propertyService'

const router = useRouter()
const route = useRoute()

interface RoomTypeForm {
  roomTypeId?: string
  name: string
  facility: string
  capacity: number
  price: number
  floor: number
  roomCount: number
  description: string
}

const formData = ref({
  propertyName: '',
  type: '' as any,
  address: '',
  province: '' as any,
  description: '',
  ownerName: '',
  ownerId: generateUUID(),
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

const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const isEditMode = computed(() => !!route.params.id)

function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

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

onMounted(async () => {
  if (isEditMode.value) await loadProperty()
})

const loadProperty = async () => {
  try {
    loading.value = true
    const response = await propertyService.getForUpdate(route.params.id as string)
    if (response.success && response.property) {
      // Backend returns: { success, property, roomTypes }
      const prop = response.property
      formData.value.propertyName = prop.propertyName || ''
      formData.value.type = prop.type ?? ''
      formData.value.address = prop.address || ''
      formData.value.province = prop.province ?? ''
      formData.value.description = prop.description || ''
      formData.value.ownerName = prop.ownerName || ''
      formData.value.ownerId = prop.ownerId || generateUUID()
      
      // Load room types from response
      if (response.roomTypes && response.roomTypes.length > 0) {
        formData.value.roomTypes = response.roomTypes.map((rt: any) => ({
          roomTypeId: rt.roomTypeId,
          name: rt.name || '',
          facility: rt.facility || '',
          capacity: rt.capacity || 0,
          price: rt.price || 0,
          floor: rt.floor || 0,
          roomCount: rt.listRoom?.length || 0, // Calculate from existing rooms
          description: rt.description || ''
        }))
      }
    }
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  // Validation
  if (formData.value.type === '' || formData.value.type === null) {
    errorMessage.value = 'Please select a property type'
    return
  }
  
  if (formData.value.province === '' || formData.value.province === null) {
    errorMessage.value = 'Please select a province'
    return
  }

  if (formData.value.roomTypes.length === 0) {
    errorMessage.value = 'At least one room type is required'
    return
  }

  // Validate each room type
  for (let i = 0; i < formData.value.roomTypes.length; i++) {
    const rt = formData.value.roomTypes[i]
    
    // Skip if room type is undefined
    if (!rt) {
      errorMessage.value = `Room type ${i + 1} is invalid`
      return
    }
    
    // For existing room types (with roomTypeId), only validate editable fields
    if (rt.roomTypeId) {
      if (!rt.name || !rt.facility || rt.capacity <= 0 || rt.price <= 0) {
        errorMessage.value = `Please complete all editable fields for room type ${i + 1}`
        return
      }
    } else {
      // For new room types, validate all fields including floor and roomCount
      if (!rt.name || !rt.facility || rt.capacity <= 0 || rt.price <= 0 || rt.floor < 0 || rt.roomCount <= 0) {
        errorMessage.value = `Please complete all fields for room type ${i + 1}`
        return
      }
    }
  }

  try {
    submitting.value = true
    errorMessage.value = ''
    
    console.log('Submitting payload:', formData.value)
    
    if (isEditMode.value) {
      // For update, send property fields and room types
      // Existing room types: send only editable fields (no floor/roomCount)
      // New room types: send all fields including floor/roomCount
      const updatePayload: any = {
        propertyId: route.params.id as string,
        propertyName: formData.value.propertyName,
        address: formData.value.address,
        description: formData.value.description,
        province: Number(formData.value.province),
        roomTypes: formData.value.roomTypes.map(rt => {
          const roomTypeData: any = {
            name: rt.name,
            facility: rt.facility,
            capacity: Number(rt.capacity),
            price: Number(rt.price),
            description: rt.description
          }
          
          // For existing room types, include roomTypeId and floor (but not roomCount)
          if (rt.roomTypeId) {
            roomTypeData.roomTypeId = rt.roomTypeId
            roomTypeData.floor = Number(rt.floor)
          } else {
            // For new room types, include floor and roomCount
            roomTypeData.floor = Number(rt.floor)
            roomTypeData.roomCount = Number(rt.roomCount)
          }
          
          return roomTypeData
        })
      }
      
      await propertyService.update(updatePayload)
      successMessage.value = 'Property updated successfully!'
    } else {
      // For create, send full data with room types
      const createPayload: any = {
        propertyName: formData.value.propertyName,
        type: Number(formData.value.type),
        address: formData.value.address,
        province: Number(formData.value.province),
        description: formData.value.description,
        ownerName: formData.value.ownerName,
        ownerId: formData.value.ownerId,
        roomTypes: formData.value.roomTypes
      }
      
      const response = await propertyService.create(createPayload)
      console.log('Create response:', response)
      successMessage.value = 'Property created successfully!'
    }
    
    setTimeout(() => router.push('/property'), 1500)
  } catch (error: any) {
    console.error('Submit error:', error)
    errorMessage.value = error.response?.data?.message || error.message || 'Failed to save property'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.property-form { 
  max-width: 1000px; 
  margin: 0 auto; 
  padding: 20px; 
}

h1 { 
  margin-bottom: 30px; 
  color: #333; 
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
  padding: 40px; 
  color: #666; 
}

.form-container { 
  background: white; 
  padding: 30px; 
  border-radius: 8px; 
  box-shadow: 0 2px 8px rgba(0,0,0,0.1); 
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 30px 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #4caf50;
}

.section-title:first-child {
  margin-top: 0;
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

input, select, textarea { 
  width: 100%; 
  padding: 10px; 
  border: 1px solid #ddd; 
  border-radius: 4px; 
  box-sizing: border-box;
  font-size: 14px;
}

input:focus, select:focus, textarea:focus {
  outline: none;
  border-color: #4caf50;
}

.disabled-input {
  background-color: #f0f0f0 !important;
  color: #666 !important;
  cursor: not-allowed !important;
}

input:disabled, select:disabled {
  background-color: #f0f0f0;
  color: #666;
  cursor: not-allowed;
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

.helper-text {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #666;
  font-style: italic;
}

.btn-primary, .btn-secondary { 
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

