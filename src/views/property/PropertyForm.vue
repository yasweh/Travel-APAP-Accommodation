<template>
  <div class="property-form">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <path d="M20 5L5 15H10L20 10L30 15H35L20 5ZM12.5 12.5V5H15V15L12.5 12.5Z" fill="#7C6A46"/>
            <path d="M10 17.5V35H17.5V25H22.5V35H30V17.5L20 12.5L10 17.5Z" fill="#7C6A46"/>
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">{{ isEditMode ? 'Edit Property' : 'Create New Property' }}</h1>
          <p class="page-subtitle">{{ isEditMode ? 'Update property information and room types' : 'Add a new property with room type configurations' }}</p>
        </div>
      </div>
    </div>

    <!-- Messages -->
    <div v-if="errorMessage" class="message-box error">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM11 15H9V13H11V15ZM11 11H9V5H11V11Z" fill="#F44336"/>
      </svg>
      <span>{{ errorMessage }}</span>
    </div>
    
    <div v-if="successMessage" class="message-box success">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM8 15L3 10L4.41 8.59L8 12.17L15.59 4.58L17 6L8 15Z" fill="#4CAF50"/>
      </svg>
      <span>{{ successMessage }}</span>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Loading property data...</p>
    </div>

    <form v-else @submit.prevent="submitForm" class="form-container">
      <!-- Property Basic Info Card -->
      <div class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 3L2 9H6L12 6L18 9H22L12 3ZM7.5 7.5V3H9V9L7.5 7.5Z" fill="#7C6A46"/>
            <path d="M6 10.5V21H10.5V15H13.5V21H18V10.5L12 7.5L6 10.5Z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">Property Information</h2>
        </div>
        
        <div v-if="isEditMode" class="form-group">
          <label>
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C12.42 16 16 12.42 16 8C16 3.58 12.42 0 8 0ZM8 14C4.69 14 2 11.31 2 8C2 4.69 4.69 2 8 2C11.31 2 14 4.69 14 8C14 11.31 11.31 14 8 14Z" fill="#7C6A46"/>
            </svg>
            Property ID
          </label>
          <input type="text" :value="route.params.id" disabled class="input-disabled" />
        </div>

        <div class="form-group">
          <label class="required">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 0L0 5V7L8 12L16 7V5L8 0ZM0 9V11L8 16L16 11V9L8 14L0 9Z" fill="#7C6A46"/>
            </svg>
            Property Name
          </label>
          <input type="text" v-model="formData.propertyName" required placeholder="e.g., Grand Hotel Jakarta" />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="required">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M14 2H12V0H10V2H6V0H4V2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2Z" fill="#7C6A46"/>
              </svg>
              Property Type
            </label>
            <select v-model.number="formData.type" required>
              <option value="" disabled>Select Type</option>
              <option value="0">🏨 Hotel</option>
              <option value="1">🏡 Villa</option>
              <option value="2">🏢 Apartemen</option>
            </select>
          </div>

          <div class="form-group">
            <label class="required">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 0C4.13 0 1 3.13 1 7C1 12.25 8 16 8 16C8 16 15 12.25 15 7C15 3.13 11.87 0 8 0ZM8 9.5C6.62 9.5 5.5 8.38 5.5 7C5.5 5.62 6.62 4.5 8 4.5C9.38 4.5 10.5 5.62 10.5 7C10.5 8.38 9.38 9.5 8 9.5Z" fill="#7C6A46"/>
              </svg>
              Province
            </label>
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
          <label class="required">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 0C4.13 0 1 3.13 1 7C1 12.25 8 16 8 16C8 16 15 12.25 15 7C15 3.13 11.87 0 8 0ZM8 9.5C6.62 9.5 5.5 8.38 5.5 7C5.5 5.62 6.62 4.5 8 4.5C9.38 4.5 10.5 5.62 10.5 7C10.5 8.38 9.38 9.5 8 9.5Z" fill="#7C6A46"/>
            </svg>
            Address
          </label>
          <textarea v-model="formData.address" required rows="3" placeholder="Full address of the property"></textarea>
        </div>

        <div class="form-group">
          <label>
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M14 0H2C0.9 0 0 0.9 0 2V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V2C16 0.9 15.1 0 14 0ZM14 14H2V2H14V14Z" fill="#7C6A46"/>
            </svg>
            Description
          </label>
          <textarea v-model="formData.description" rows="4" placeholder="Brief description about the property"></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="required">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 0C5.79 0 4 1.79 4 4C4 6.21 5.79 8 8 8C10.21 8 12 6.21 12 4C12 1.79 10.21 0 8 0ZM8 10C5.33 10 0 11.34 0 14V16H16V14C16 11.34 10.67 10 8 10Z" fill="#7C6A46"/>
              </svg>
              Owner Name
            </label>
            <input type="text" v-model="formData.ownerName" required placeholder="Owner full name" />
          </div>

          <div class="form-group">
            <label class="required">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C12.42 16 16 12.42 16 8C16 3.58 12.42 0 8 0Z" fill="#7C6A46"/>
              </svg>
              Owner ID (UUID)
            </label>
            <input type="text" v-model="formData.ownerId" required readonly class="input-readonly" />
          </div>
        </div>
      </div>

      <!-- Room Types Section -->
      <div class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V5H19V19Z" fill="#7C6A46"/>
            <rect x="7" y="13" width="10" height="2" fill="#7C6A46"/>
            <rect x="7" y="9" width="5" height="2" fill="#7C6A46"/>
          </svg>
          <div class="header-title-section">
            <h2 class="card-title">Room Types Configuration</h2>
            <span class="required-badge">At least one room type required</span>
          </div>
        </div>

        <div v-for="(roomType, index) in formData.roomTypes" :key="index" class="room-type-section">
          <div class="room-type-header">
            <div class="room-type-badge">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path d="M14.25 2.25H13.5V1.5H12V2.25H6V1.5H4.5V2.25H3.75C2.925 2.25 2.25 2.925 2.25 3.75V15.75C2.25 16.575 2.925 17.25 3.75 17.25H14.25C15.075 17.25 15.75 16.575 15.75 15.75V3.75C15.75 2.925 15.075 2.25 14.25 2.25Z" fill="#7C6A46"/>
              </svg>
              <h3>Room Type {{ index + 1 }}</h3>
              <span class="badge">{{ roomType.roomTypeId ? 'Existing' : 'New' }}</span>
            </div>
            <button 
              v-if="formData.roomTypes.length > 1 && (!isEditMode || !roomType.roomTypeId)" 
              type="button" 
              @click="removeRoomType(index)" 
              class="btn-remove"
            >
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M4 8H12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              Remove
            </button>
          </div>

          <!-- Show Room Type ID only for existing room types -->
          <div v-if="isEditMode && roomType.roomTypeId" class="form-group">
            <label>
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C12.42 16 16 12.42 16 8C16 3.58 12.42 0 8 0Z" fill="#999"/>
              </svg>
              Room Type ID
            </label>
            <input type="text" :value="roomType.roomTypeId" disabled class="input-disabled" />
          </div>

          <div class="form-row">
            <!-- Show Room Type Name only for new room types, hide for existing ones -->
            <div v-if="!roomType.roomTypeId" class="form-group">
              <label class="required">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M14 2H12V0H10V2H6V0H4V2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2Z" fill="#7C6A46"/>
                </svg>
                Room Type Name
              </label>
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
              <label>
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M14 2H12V0H10V2H6V0H4V2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2Z" fill="#999"/>
                </svg>
                Room Type Name
              </label>
              <input type="text" :value="roomType.name" disabled class="input-disabled" />
            </div>

            <div class="form-group">
              <label class="required">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M14 0H2C0.9 0 0 0.9 0 2V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V2C16 0.9 15.1 0 14 0Z" fill="#7C6A46"/>
                </svg>
                Facility
              </label>
              <input type="text" v-model="roomType.facility" required placeholder="e.g., AC, WiFi, TV" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="required">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M8 0C5.79 0 4 1.79 4 4C4 6.21 5.79 8 8 8C10.21 8 12 6.21 12 4C12 1.79 10.21 0 8 0ZM8 10C5.33 10 0 11.34 0 14V16H16V14C16 11.34 10.67 10 8 10Z" fill="#7C6A46"/>
                </svg>
                Capacity (persons)
              </label>
              <input type="number" v-model.number="roomType.capacity" required min="1" placeholder="0" />
            </div>

            <div class="form-group">
              <label class="required">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M14 2H11L9 0H7L5 2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2ZM8 13C5.79 13 4 11.21 4 9C4 6.79 5.79 5 8 5C10.21 5 12 6.79 12 9C12 11.21 10.21 13 8 13Z" fill="#7C6A46"/>
                </svg>
                Price (IDR)
              </label>
              <input type="number" v-model.number="roomType.price" required min="0" placeholder="0" />
            </div>
          </div>

          <!-- Show Floor and Number of Rooms only for new room types (hide for existing) -->
          <div v-if="!roomType.roomTypeId" class="form-row">
            <div class="form-group">
              <label class="required">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M12 0H4C2.9 0 2 0.9 2 2V14C2 15.1 2.9 16 4 16H12C13.1 16 14 15.1 14 14V2C14 0.9 13.1 0 12 0ZM10 14H6V12H10V14ZM10 10H6V8H10V10ZM10 6H6V4H10V6Z" fill="#7C6A46"/>
                </svg>
                Floor
              </label>
              <input type="number" v-model.number="roomType.floor" required min="0" placeholder="0" />
            </div>

            <div class="form-group">
              <label class="required">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M2 0C0.9 0 0 0.9 0 2V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V2C16 0.9 15.1 0 14 0H2ZM14 14H2V2H14V14Z" fill="#7C6A46"/>
                </svg>
                Number of Rooms
              </label>
              <input type="number" v-model.number="roomType.roomCount" required min="1" placeholder="1" />
              <small class="helper-text">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                  <path d="M6 0C2.69 0 0 2.69 0 6C0 9.31 2.69 12 6 12C9.31 12 12 9.31 12 6C12 2.69 9.31 0 6 0ZM6.6 9H5.4V5.4H6.6V9ZM6.6 4.2H5.4V3H6.6V4.2Z" fill="#999"/>
                </svg>
                Rooms will be numbered based on floor. E.g., Floor 2 with 10 rooms → 201-210
              </small>
            </div>
          </div>

          <div class="form-group">
            <label>
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M14 0H2C0.9 0 0 0.9 0 2V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V2C16 0.9 15.1 0 14 0ZM14 14H2V2H14V14Z" fill="#7C6A46"/>
              </svg>
              Description
            </label>
            <textarea v-model="roomType.description" rows="2" placeholder="Room type description"></textarea>
          </div>
        </div>

        <button type="button" @click="addRoomType" class="btn-add-room">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM15 11H11V15H9V11H5V9H9V5H11V9H15V11Z" fill="#7C6A46"/>
          </svg>
          Add Another Room Type
        </button>
      </div>

      <!-- Form Actions -->
      <div class="form-actions">
        <button type="submit" class="btn-submit" :disabled="submitting">
          <svg v-if="!submitting" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM8 15L3 10L4.41 8.59L8 12.17L15.59 4.58L17 6L8 15Z" fill="white"/>
          </svg>
          <div v-else class="btn-spinner"></div>
          {{ submitting ? 'Saving...' : (isEditMode ? 'Update Property' : 'Create Property') }}
        </button>
        <button type="button" @click="$router.push('/property')" class="btn-cancel">
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
@import url('https://fonts.googleapis.com/css2?family=Raleway:wght@700;800&family=Poppins:wght@400;500;600&display=swap');

.property-form {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  animation: fadeIn 0.6s ease-out;
  font-family: 'Poppins', sans-serif;
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
  margin-bottom: 2rem;
  animation: fadeIn 0.8s ease-out;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #7C6A46 0%, #5A4E36 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(124, 106, 70, 0.3);
}

.header-text {
  flex: 1;
}

.page-title {
  font-family: 'Raleway', sans-serif;
  font-size: 2.5rem;
  font-weight: 800;
  color: #1C1C1C;
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
  font-weight: 400;
}

/* Messages */
.message-box {
  padding: 1rem 1.5rem;
  border-radius: 10px;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  animation: fadeIn 0.5s ease-out;
  font-weight: 500;
}

.message-box.error {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  border-left: 4px solid #F44336;
  color: #C62828;
}

.message-box.success {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  border-left: 4px solid #4CAF50;
  color: #2E7D32;
}

/* Loading */
.loading-container {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(124, 106, 70, 0.1);
  border-top-color: #7C6A46;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Form Container */
.form-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* Form Card */
.form-card {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(124, 106, 70, 0.1);
  animation: fadeIn 0.9s ease-out;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid rgba(124, 106, 70, 0.1);
}

.card-title {
  font-family: 'Raleway', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #1C1C1C;
  margin: 0;
}

.header-title-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.required-badge {
  padding: 0.35rem 0.75rem;
  background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%);
  color: #E65100;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

/* Form Elements */
.form-group {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: #4A4A4A;
}

label.required::after {
  content: '*';
  color: #F44336;
  margin-left: 0.25rem;
}

input, select, textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #E8E8E8;
  border-radius: 10px;
  font-size: 1rem;
  font-family: 'Poppins', sans-serif;
  color: #1C1C1C;
  background: white;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

input:hover, select:hover, textarea:hover {
  border-color: #7C6A46;
}

input:focus, select:focus, textarea:focus {
  outline: none;
  border-color: #7C6A46;
  box-shadow: 0 0 0 3px rgba(124, 106, 70, 0.1);
}

textarea {
  resize: vertical;
  min-height: 80px;
}

.input-disabled, .input-readonly {
  background: #F5F5F5 !important;
  color: #999 !important;
  cursor: not-allowed !important;
  border-color: #E8E8E8 !important;
}

.helper-text {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-top: 0.5rem;
  font-size: 0.8rem;
  color: #999;
  font-style: italic;
}

/* Room Type Section */
.room-type-section {
  background: linear-gradient(135deg, #FAFAFA 0%, #F8F8F8 100%);
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  border-left: 4px solid #7C6A46;
  transition: all 0.3s ease;
}

.room-type-section:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.room-type-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid rgba(124, 106, 70, 0.1);
}

.room-type-badge {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.room-type-badge h3 {
  font-family: 'Raleway', sans-serif;
  font-size: 1.2rem;
  font-weight: 700;
  color: #1C1C1C;
  margin: 0;
}

.badge {
  padding: 0.25rem 0.75rem;
  background: linear-gradient(135deg, #7C6A46 0%, #5A4E36 100%);
  color: white;
  border-radius: 15px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.btn-remove {
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #F44336 0%, #D32F2F 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  transition: all 0.3s ease;
}

.btn-remove:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.3);
}

.btn-add-room {
  width: 100%;
  padding: 1rem 1.5rem;
  background: linear-gradient(135deg, #7C6A46 0%, #5A4E36 100%);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.3);
  margin-top: 1rem;
}

.btn-add-room:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.4);
}

/* Form Actions */
.form-actions {
  display: flex;
  gap: 1rem;
  padding-top: 2rem;
  border-top: 2px solid #E8E8E8;
}

.btn-submit, .btn-cancel {
  flex: 1;
  padding: 1rem 2rem;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.btn-submit {
  background: linear-gradient(135deg, #7C6A46 0%, #5A4E36 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.3);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.4);
}

.btn-submit:disabled {
  background: #CCC;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-spinner {
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.btn-cancel {
  background: white;
  color: #666;
  border: 2px solid #E8E8E8;
}

.btn-cancel:hover {
  background: #F5F5F5;
  border-color: #7C6A46;
  color: #7C6A46;
}

/* Responsive Design */
@media (max-width: 768px) {
  .property-form {
    padding: 1rem;
  }

  .page-title {
    font-size: 2rem;
  }

  .page-subtitle {
    font-size: 1rem;
  }

  .header-icon {
    width: 60px;
    height: 60px;
  }

  .header-icon svg {
    width: 30px;
    height: 30px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .form-card {
    padding: 1.5rem;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .header-title-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .room-type-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .btn-remove {
    width: 100%;
    justify-content: center;
  }
}
</style>

