<template>
  <div class="maintenance-form">
    <h1>Add Maintenance Schedule</h1>

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
      <!-- Property Selection -->
      <div class="form-group">
        <label for="propertyId">Property *</label>
        <select id="propertyId" v-model="selectedPropertyId" @change="onPropertyChange" required>
          <option value="">-- Select Property --</option>
          <option v-for="property in properties" :key="property.propertyId" :value="property.propertyId">
            {{ property.propertyName }}
          </option>
        </select>
      </div>

      <!-- Room Type Selection -->
      <div class="form-group">
        <label for="roomTypeId">Room Type *</label>
        <select id="roomTypeId" v-model="selectedRoomTypeId" @change="onRoomTypeChange" required :disabled="!selectedPropertyId">
          <option value="">-- Select Room Type --</option>
          <option v-for="roomType in roomTypes" :key="roomType.roomTypeId" :value="roomType.roomTypeId">
            {{ roomType.name }} (Floor {{ roomType.floor }})
          </option>
        </select>
      </div>

      <!-- Room Selection -->
      <div class="form-group">
        <label for="roomId">Room *</label>
        <select id="roomId" v-model="formData.roomId" required :disabled="!selectedRoomTypeId">
          <option value="">-- Select Room --</option>
          <option v-for="room in rooms" :key="room.roomId" :value="room.roomId">
            {{ room.roomNumber || room.name }}
          </option>
        </select>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label for="startDate">Start Date *</label>
          <input
            type="date"
            id="startDate"
            v-model="formData.startDate"
            required
            :min="minDate"
          />
        </div>
        <div class="form-group">
          <label for="startTime">Start Time *</label>
          <input type="time" id="startTime" v-model="formData.startTime" required />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label for="endDate">End Date *</label>
          <input
            type="date"
            id="endDate"
            v-model="formData.endDate"
            required
            :min="formData.startDate"
          />
        </div>
        <div class="form-group">
          <label for="endTime">End Time *</label>
          <input type="time" id="endTime" v-model="formData.endTime" required />
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="submitting">
          {{ submitting ? 'Adding...' : 'Add Maintenance Schedule' }}
        </button>
        <button type="button" @click="goBack" class="btn-secondary">Cancel</button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { maintenanceService, type MaintenanceCreateDTO } from '@/services/maintenanceService'
import { propertyService, type Property } from '@/services/propertyService'
import { roomTypeService, type RoomType } from '@/services/roomTypeService'

const router = useRouter()

const submitting = ref(false)
const error = ref('')
const successMessage = ref('')

const properties = ref<Property[]>([])
const roomTypes = ref<RoomType[]>([])
const rooms = ref<any[]>([])
const selectedPropertyId = ref('')
const selectedRoomTypeId = ref('')

const formData = reactive({
  roomId: '',
  startDate: '',
  startTime: '08:00',
  endDate: '',
  endTime: '17:00',
})

const minDate = computed(() => {
  const today = new Date()
  return today.toISOString().split('T')[0]
})

const loadProperties = async () => {
  try {
    const response = await propertyService.getAll()
    if (response.success) {
      properties.value = response.data
    }
  } catch (err) {
    console.error('Load properties error:', err)
  }
}

const onPropertyChange = async () => {
  selectedRoomTypeId.value = ''
  formData.roomId = ''
  rooms.value = []
  
  if (!selectedPropertyId.value) {
    roomTypes.value = []
    return
  }

  try {
    const response = await roomTypeService.getByPropertyId(selectedPropertyId.value)
    if (response.success) {
      roomTypes.value = response.data
    }
  } catch (err) {
    console.error('Load room types error:', err)
  }
}

const onRoomTypeChange = async () => {
  formData.roomId = ''
  
  if (!selectedRoomTypeId.value) {
    rooms.value = []
    return
  }

  try {
    const response = await roomTypeService.getById(selectedRoomTypeId.value)
    if (response.success && response.data.listRoom) {
      // Filter only active rooms (activeRoom = 1)
      rooms.value = response.data.listRoom.filter((room: any) => room.activeRoom === 1)
    }
  } catch (err) {
    console.error('Load rooms error:', err)
  }
}

const submitForm = async () => {
  submitting.value = true
  error.value = ''
  successMessage.value = ''

  try {
    const maintenanceData: MaintenanceCreateDTO = {
      roomId: formData.roomId,
      startDate: formData.startDate,
      startTime: formData.startTime,
      endDate: formData.endDate,
      endTime: formData.endTime,
    }

    const response = await maintenanceService.create(maintenanceData)
    if (response.success) {
      successMessage.value = 'Maintenance schedule added successfully!'
      setTimeout(() => {
        router.push('/maintenance')
      }, 1500)
    } else {
      error.value = response.message
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to add maintenance schedule'
    console.error('Submit error:', err)
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/maintenance')
}

onMounted(() => {
  loadProperties()
})
</script>

<style scoped>
.maintenance-form {
  padding: 20px;
  max-width: 700px;
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
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group small {
  display: block;
  margin-top: 5px;
  color: #666;
  font-size: 12px;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #4caf50;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
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
