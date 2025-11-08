<template>
  <div class="room-type-list">
    <h1>Room Type Management</h1>

    <!-- Property Selection -->
    <div class="property-selector">
      <label for="propertySelect">Select Property:</label>
      <select id="propertySelect" v-model="selectedPropertyId" @change="loadRoomTypes">
        <option value="">-- Select a Property --</option>
        <option v-for="prop in properties" :key="prop.propertyId" :value="prop.propertyId">
          {{ prop.propertyName }}
        </option>
      </select>
    </div>

    <!-- Error Message -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- Actions -->
    <div v-if="selectedPropertyId" class="actions">
      <button @click="goToCreateRoomType" class="btn-primary">Create New Room Type</button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">Loading room types...</div>

    <!-- Room Types Table -->
    <table v-else-if="roomTypes.length > 0" class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Room Type Name</th>
          <th>Floor</th>
          <th>Capacity</th>
          <th>Facility</th>
          <th>Price</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="roomType in roomTypes" :key="roomType.roomTypeId">
          <td>{{ roomType.roomTypeId }}</td>
          <td>{{ roomType.name }}</td>
          <td>{{ roomType.floor }}</td>
          <td>{{ roomType.capacity }} people</td>
          <td>{{ roomType.facility }}</td>
          <td>Rp {{ formatCurrency(roomType.price) }}</td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-else-if="selectedPropertyId" class="empty-state">
      <p>No room types found for this property.</p>
    </div>
    <div v-else class="empty-state">
      <p>All room types are displayed. Select a property to filter.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { propertyService, type Property } from '@/services/propertyService'
import { roomTypeService, type RoomType } from '@/services/roomTypeService'

const router = useRouter()

const properties = ref<Property[]>([])
const roomTypes = ref<RoomType[]>([])
const allRoomTypes = ref<RoomType[]>([]) // Store all room types
const selectedPropertyId = ref('')
const loading = ref(false)
const error = ref('')

const loadProperties = async () => {
  try {
    const response = await propertyService.getAll()
    if (response.success) {
      properties.value = response.data.filter((p) => p.activeProperty === 1)
    }
  } catch (err: any) {
    error.value = 'Failed to load properties'
    console.error('Load properties error:', err)
  }
}

const loadAllRoomTypes = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await roomTypeService.getAll()
    if (response.success) {
      allRoomTypes.value = response.data
      roomTypes.value = response.data // Show all by default
    } else {
      error.value = response.message
      allRoomTypes.value = []
      roomTypes.value = []
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load room types'
    allRoomTypes.value = []
    roomTypes.value = []
    console.error('Load room types error:', err)
  } finally {
    loading.value = false
  }
}

const loadRoomTypes = async () => {
  if (!selectedPropertyId.value) {
    // Show all room types when no property selected
    roomTypes.value = allRoomTypes.value
    return
  }

  loading.value = true
  error.value = ''
  try {
    const response = await roomTypeService.getByPropertyId(selectedPropertyId.value)
    if (response.success) {
      roomTypes.value = response.data
    } else {
      error.value = response.message
      roomTypes.value = []
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load room types'
    roomTypes.value = []
    console.error('Load room types error:', err)
  } finally {
    loading.value = false
  }
}

const goToCreateRoomType = () => {
  // Redirect to property update form (single source of truth)
  router.push(`/property/edit/${selectedPropertyId.value}`)
}

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('id-ID').format(value)
}

onMounted(() => {
  loadProperties()
  loadAllRoomTypes() // Load all room types on mount
})
</script>

<style scoped>
.room-type-list {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

.property-selector {
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.property-selector label {
  display: block;
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.property-selector select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.actions {
  margin-bottom: 20px;
}

.btn-primary {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  background-color: #4caf50;
  color: white;
}

.btn-primary:hover {
  background-color: #45a049;
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
