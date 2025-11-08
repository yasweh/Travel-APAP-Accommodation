<template>
  <div class="maintenance-list">
    <h1>Maintenance Management</h1>

    <!-- Filter Dropdowns -->
    <div class="filter-selector">
      <div class="filter-group">
        <label for="roomTypeId">Filter by Room Type:</label>
        <select id="roomTypeId" v-model="selectedRoomTypeId" @change="onRoomTypeChange">
          <option value="">-- All Room Types --</option>
          <option v-for="roomType in roomTypes" :key="roomType.roomTypeId" :value="roomType.roomTypeId">
            {{ roomType.name }} (Floor {{ roomType.floor }})
          </option>
        </select>
      </div>

      <div class="filter-group">
        <label for="roomId">Filter by Room:</label>
        <select id="roomId" v-model="selectedRoomId" @change="loadMaintenances" :disabled="!selectedRoomTypeId">
          <option value="">-- All Rooms --</option>
          <option v-for="room in rooms" :key="room.roomId" :value="room.roomId">
            {{ room.roomNumber }}
          </option>
        </select>
      </div>

      <button @click="goToCreateMaintenance" class="btn-secondary">Add New Schedule</button>
    </div>

    <!-- Error Message -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">Loading maintenance schedules...</div>

    <!-- Maintenance Table -->
    <table v-else-if="maintenances.length > 0" class="data-table">
      <thead>
        <tr>
          <th>Maintenance ID</th>
          <th>Property</th>
          <th>Room Type</th>
          <th>Room</th>
          <th>Start Date</th>
          <th>Start Time</th>
          <th>End Date</th>
          <th>End Time</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="maintenance in maintenances" :key="maintenance.maintenanceId">
          <td>{{ maintenance.maintenanceId }}</td>
          <td>{{ maintenance.propertyName }}</td>
          <td>{{ maintenance.roomTypeName }}</td>
          <td>{{ maintenance.roomName }}</td>
          <td>{{ formatDate(maintenance.startDate) }}</td>
          <td>{{ maintenance.startTime }}</td>
          <td>{{ formatDate(maintenance.endDate) }}</td>
          <td>{{ maintenance.endTime }}</td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <p>No maintenance schedules found.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { maintenanceService, type Maintenance } from '@/services/maintenanceService'
import { roomTypeService, type RoomType } from '@/services/roomTypeService'

const router = useRouter()

const maintenances = ref<Maintenance[]>([])
const roomTypes = ref<RoomType[]>([])
const rooms = ref<any[]>([])
const selectedRoomTypeId = ref('')
const selectedRoomId = ref('')
const loading = ref(false)
const error = ref('')

// Load all room types on mount
const loadRoomTypes = async () => {
  try {
    const response = await roomTypeService.getAll()
    if (response.data) {
      roomTypes.value = response.data
    }
  } catch (err: any) {
    console.error('Load room types error:', err)
  }
}

// When room type is selected, load rooms for that room type
const onRoomTypeChange = () => {
  selectedRoomId.value = '' // Reset room selection
  if (selectedRoomTypeId.value) {
    loadRoomsByRoomType()
  } else {
    rooms.value = []
    loadMaintenances() // Show all if no filter
  }
}

// Load rooms by room type
const loadRoomsByRoomType = async () => {
  try {
    const response = await roomTypeService.getById(selectedRoomTypeId.value)
    if (response.data && response.data.rooms) {
      rooms.value = response.data.rooms
    } else {
      rooms.value = []
    }
    loadMaintenances() // Load maintenances after rooms are loaded
  } catch (err: any) {
    console.error('Load rooms error:', err)
    rooms.value = []
  }
}

// Load maintenances based on filters
const loadMaintenances = async () => {
  loading.value = true
  error.value = ''
  try {
    let response
    
    if (selectedRoomId.value) {
      // Filter by specific room
      response = await maintenanceService.getByRoomId(selectedRoomId.value)
    } else if (selectedRoomTypeId.value) {
      // Filter by room type
      response = await maintenanceService.getByRoomTypeId(selectedRoomTypeId.value)
    } else {
      // Show all maintenance schedules
      response = await maintenanceService.getAll()
    }
    
    if (response.success) {
      maintenances.value = response.data
    } else {
      error.value = response.message
      maintenances.value = []
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load maintenance schedules'
    maintenances.value = []
    console.error('Load maintenances error:', err)
  } finally {
    loading.value = false
  }
}

const goToCreateMaintenance = () => {
  router.push('/maintenance/create')
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('id-ID', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

onMounted(() => {
  loadRoomTypes()
  loadMaintenances() // Load all maintenances by default
})
</script>

<style scoped>
.maintenance-list {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

.filter-selector {
  display: flex;
  gap: 15px;
  align-items: center;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
  min-width: 200px;
}

.filter-group label {
  font-weight: bold;
  color: #333;
  font-size: 14px;
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background-color: white;
}

.filter-group select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.btn-primary,
.btn-secondary {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  align-self: flex-end;
}

.btn-primary {
  background-color: #4caf50;
  color: white;
}

.btn-primary:hover {
  background-color: #45a049;
}

.btn-secondary {
  background-color: #2196f3;
  color: white;
}

.btn-secondary:hover {
  background-color: #0b7dda;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
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
}

.data-table th,
.data-table td {
  padding: 12px;
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

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state p {
  font-size: 16px;
}
</style>
