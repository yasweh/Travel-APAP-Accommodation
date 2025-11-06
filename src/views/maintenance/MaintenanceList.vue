<template>
  <div class="maintenance-list">
    <h1>Maintenance Management</h1>

    <!-- Room Selection -->
    <div class="room-selector">
      <label for="roomId">Search by Room ID:</label>
      <input
        type="text"
        id="roomId"
        v-model="searchRoomId"
        placeholder="e.g., APT-0000-004-101"
      />
      <button @click="loadMaintenances" class="btn-primary">Search</button>
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
          <th>Room</th>
          <th>Start Date</th>
          <th>Start Time</th>
          <th>End Date</th>
          <th>End Time</th>
          <th>Description</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="maintenance in maintenances" :key="maintenance.maintenanceId">
          <td>{{ maintenance.maintenanceId }}</td>
          <td>{{ maintenance.room.name }} ({{ maintenance.room.roomId }})</td>
          <td>{{ formatDate(maintenance.startMaintenanceDate) }}</td>
          <td>{{ formatTime(maintenance.startMaintenanceTime) }}</td>
          <td>{{ formatDate(maintenance.endMaintenanceDate) }}</td>
          <td>{{ formatTime(maintenance.endMaintenanceTime) }}</td>
          <td>{{ maintenance.description }}</td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-else-if="searchRoomId" class="empty-state">
      <p>No maintenance schedules found for this room.</p>
    </div>
    <div v-else class="empty-state">
      <p>Enter a Room ID to view maintenance schedules.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { maintenanceService, type Maintenance } from '@/services/maintenanceService'

const router = useRouter()

const maintenances = ref<Maintenance[]>([])
const searchRoomId = ref('')
const loading = ref(false)
const error = ref('')

const loadMaintenances = async () => {
  if (!searchRoomId.value.trim()) {
    error.value = 'Please enter a Room ID'
    return
  }

  loading.value = true
  error.value = ''
  try {
    const response = await maintenanceService.getByRoomId(searchRoomId.value)
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
  return new Date(dateString).toLocaleDateString('id-ID', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

const formatTime = (timeString: string) => {
  return new Date(timeString).toLocaleTimeString('id-ID', {
    hour: '2-digit',
    minute: '2-digit',
  })
}
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

.room-selector {
  display: flex;
  gap: 10px;
  align-items: center;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.room-selector label {
  font-weight: bold;
  color: #333;
}

.room-selector input {
  flex: 1;
  min-width: 200px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.btn-primary,
.btn-secondary {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
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
