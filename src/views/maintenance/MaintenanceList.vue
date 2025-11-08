<template>
  <div class="maintenance-list">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <path d="M35 5H30L27.5 2.5C26.875 1.875 26.0625 1.5 25.25 1.5H14.75C13.9375 1.5 13.125 1.875 12.5 2.5L10 5H5C3.625 5 2.5 6.125 2.5 7.5V35C2.5 36.375 3.625 37.5 5 37.5H35C36.375 37.5 37.5 36.375 37.5 35V7.5C37.5 6.125 36.375 5 35 5ZM20 32.5C14.2 32.5 9.5 27.8 9.5 22C9.5 16.2 14.2 11.5 20 11.5C25.8 11.5 30.5 16.2 30.5 22C30.5 27.8 25.8 32.5 20 32.5ZM20 15C16.15 15 13 18.15 13 22C13 25.85 16.15 29 20 29C23.85 29 27 25.85 27 22C27 18.15 23.85 15 20 15Z" fill="#7C6A46"/>
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">Maintenance Management</h1>
          <p class="page-subtitle">Schedule and manage room maintenance activities</p>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters-container">
      <div class="filter-card">
        <div class="filter-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M4.25 5.61C6.57 8.59 10 13 10 13V19C10 19.55 10.45 20 11 20H13C13.55 20 14 19.55 14 19V13C14 13 17.43 8.59 19.75 5.61C20.26 4.95 19.79 4 18.95 4H5.04C4.21 4 3.74 4.95 4.25 5.61Z" fill="#7C6A46"/>
          </svg>
          <span class="filter-title">Filter Maintenance</span>
        </div>
        <div class="filter-inputs">
          <div class="form-group">
            <label for="roomTypeId">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 0L0 5V7L8 12L16 7V5L8 0ZM0 9V11L8 16L16 11V9L8 14L0 9Z" fill="#7C6A46"/>
              </svg>
              Room Type
            </label>
            <select id="roomTypeId" v-model="selectedRoomTypeId" @change="onRoomTypeChange">
              <option value="">-- All Room Types --</option>
              <option v-for="roomType in roomTypes" :key="roomType.roomTypeId" :value="roomType.roomTypeId">
                {{ roomType.name }} (Floor {{ roomType.floor }})
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="roomId">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M12 4V2H10V4H6V2H4V4H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2H12Z" fill="#7C6A46"/>
              </svg>
              Room Number
            </label>
            <select id="roomId" v-model="selectedRoomId" @change="loadMaintenances" :disabled="!selectedRoomTypeId">
              <option value="">-- All Rooms --</option>
              <option v-for="room in rooms" :key="room.roomId" :value="room.roomId">
                {{ room.roomNumber }}
              </option>
            </select>
          </div>

          <button @click="goToCreateMaintenance" class="btn-add">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM15 11H11V15H9V11H5V9H9V5H11V9H15V11Z" fill="white"/>
            </svg>
            Add New Schedule
          </button>
        </div>
      </div>
    </div>

    <!-- Error Message -->
    <div v-if="error" class="error-message">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM11 15H9V13H11V15ZM11 11H9V5H11V11Z" fill="#F44336"/>
      </svg>
      <span>{{ error }}</span>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>Loading maintenance schedules...</p>
    </div>

    <!-- Maintenance Grid -->
    <div v-else-if="maintenances.length > 0" class="maintenance-grid">
      <div v-for="maintenance in maintenances" :key="maintenance.maintenanceId" class="maintenance-card">
        <div class="card-header">
          <div class="maintenance-id">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M17.5 2.5H15L12.5 0C11.875 -0.625 11.0625 -1 10.25 -1H9.75C8.9375 -1 8.125 -0.625 7.5 0L5 2.5H2.5C1.125 2.5 0 3.625 0 5V17.5C0 18.875 1.125 20 2.5 20H17.5C18.875 20 20 18.875 20 17.5V5C20 3.625 18.875 2.5 17.5 2.5Z" fill="#7C6A46"/>
            </svg>
            <span>{{ maintenance.maintenanceId }}</span>
          </div>
          <div class="status-badge active">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M7 0C3.15 0 0 3.15 0 7C0 10.85 3.15 14 7 14C10.85 14 14 10.85 14 7C14 3.15 10.85 0 7 0ZM5.6 10.5L2.1 7L3.15 5.95L5.6 8.4L10.85 3.15L11.9 4.2L5.6 10.5Z" fill="#4CAF50"/>
            </svg>
            Scheduled
          </div>
        </div>
        
        <div class="card-body">
          <div class="maintenance-property">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M9 0L0 6H3L9 2L15 6H18L9 0ZM4.5 4.5V0H6V6L4.5 4.5Z" fill="#7C6A46"/>
            </svg>
            <div>
              <div class="property-name">{{ maintenance.propertyName }}</div>
              <div class="property-detail">{{ maintenance.roomTypeName }}</div>
            </div>
          </div>

          <div class="maintenance-room">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M14 2V0H12V2H6V0H4V2H2C0.9 2 0 2.9 0 4V16C0 17.1 0.9 18 2 18H16C17.1 18 18 17.1 18 16V4C18 2.9 17.1 2 16 2H14Z" fill="#666"/>
            </svg>
            <span>Room {{ maintenance.roomName }}</span>
          </div>

          <div class="maintenance-schedule">
            <div class="schedule-item start">
              <div class="schedule-label">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M7 0C3.15 0 0 3.15 0 7C0 10.85 3.15 14 7 14C10.85 14 14 10.85 14 7C14 3.15 10.85 0 7 0ZM7 12.25C4.1 12.25 1.75 9.9 1.75 7C1.75 4.1 4.1 1.75 7 1.75C9.9 1.75 12.25 4.1 12.25 7C12.25 9.9 9.9 12.25 7 12.25Z" fill="#4CAF50"/>
                </svg>
                Start
              </div>
              <div class="schedule-date">{{ formatDate(maintenance.startDate) }}</div>
              <div class="schedule-time">{{ maintenance.startTime }}</div>
            </div>
            
            <svg width="24" height="20" viewBox="0 0 24 20" fill="none" class="arrow-icon">
              <path d="M14 0L12.59 1.41L18.17 7H0V9H18.17L12.59 14.59L14 16L22 8L14 0Z" fill="#999"/>
            </svg>
            
            <div class="schedule-item end">
              <div class="schedule-label">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M7 0C3.15 0 0 3.15 0 7C0 10.85 3.15 14 7 14C10.85 14 14 10.85 14 7C14 3.15 10.85 0 7 0ZM7 12.25C4.1 12.25 1.75 9.9 1.75 7C1.75 4.1 4.1 1.75 7 1.75C9.9 1.75 12.25 4.1 12.25 7C12.25 9.9 9.9 12.25 7 12.25Z" fill="#F44336"/>
                </svg>
                End
              </div>
              <div class="schedule-date">{{ formatDate(maintenance.endDate) }}</div>
              <div class="schedule-time">{{ maintenance.endTime }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
        <path d="M70 10H60L55 5C53.75 3.75 52.125 3 50.5 3H29.5C27.875 3 26.25 3.75 25 5L20 10H10C7.25 10 5 12.25 5 15V70C5 72.75 7.25 75 10 75H70C72.75 75 75 72.75 75 70V15C75 12.25 72.75 10 70 10ZM40 65C28.4 65 19 55.6 19 44C19 32.4 28.4 23 40 23C51.6 23 61 32.4 61 44C61 55.6 51.6 65 40 65ZM40 30C32.3 30 26 36.3 26 44C26 51.7 32.3 58 40 58C47.7 58 54 51.7 54 44C54 36.3 47.7 30 40 30Z" fill="#D9D9D9"/>
      </svg>
      <h3>No Maintenance Schedules</h3>
      <p>Start by creating your first maintenance schedule</p>
      <button @click="goToCreateMaintenance" class="btn-empty-state">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM15 11H11V15H9V11H5V9H9V5H11V9H15V11Z" fill="white"/>
        </svg>
        Create First Schedule
      </button>
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
@import url('https://fonts.googleapis.com/css2?family=Raleway:wght@700;800&family=Poppins:wght@400;500;600&display=swap');

.maintenance-list {
  max-width: 1400px;
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

/* Filters Container */
.filters-container {
  margin-bottom: 2rem;
  animation: fadeIn 0.9s ease-out;
}

.filter-card {
  background: white;
  border-radius: 15px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(124, 106, 70, 0.1);
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid rgba(124, 106, 70, 0.1);
}

.filter-title {
  font-family: 'Raleway', sans-serif;
  font-size: 1.2rem;
  font-weight: 700;
  color: #1C1C1C;
}

.filter-inputs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  align-items: end;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #4A4A4A;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-group select {
  padding: 0.75rem 1rem;
  border: 2px solid #E8E8E8;
  border-radius: 10px;
  font-size: 1rem;
  font-family: 'Poppins', sans-serif;
  color: #1C1C1C;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.form-group select:hover {
  border-color: #7C6A46;
}

.form-group select:focus {
  outline: none;
  border-color: #7C6A46;
  box-shadow: 0 0 0 3px rgba(124, 106, 70, 0.1);
}

.form-group select:disabled {
  background: #F5F5F5;
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-add {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #7C6A46 0%, #5A4E36 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.3);
  min-width: 200px;
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.4);
}

.btn-add:active {
  transform: translateY(0);
}

/* Error Message */
.error-message {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  padding: 1rem 1.5rem;
  border-radius: 10px;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border-left: 4px solid #F44336;
  animation: fadeIn 0.5s ease-out;
}

.error-message span {
  color: #C62828;
  font-weight: 500;
}

/* Loading */
.loading {
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

/* Maintenance Grid */
.maintenance-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  gap: 1.5rem;
  animation: fadeIn 1s ease-out;
}

.maintenance-card {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(124, 106, 70, 0.1);
  transition: all 0.3s ease;
  animation: fadeIn 0.6s ease-out;
}

.maintenance-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #7C6A46;
}

.card-header {
  background: linear-gradient(135deg, #FAFAFA 0%, #F5F5F5 100%);
  padding: 1.25rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid rgba(124, 106, 70, 0.1);
}

.maintenance-id {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 600;
  color: #1C1C1C;
  font-size: 1rem;
}

.status-badge {
  padding: 0.4rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.status-badge.active {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  color: #2E7D32;
  border: 1px solid #81C784;
}

.card-body {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.maintenance-property {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  background: linear-gradient(135deg, #FAFAFA 0%, #F8F8F8 100%);
  border-radius: 10px;
  border-left: 4px solid #7C6A46;
}

.maintenance-property > div {
  flex: 1;
}

.property-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1C1C1C;
  margin-bottom: 0.25rem;
}

.property-detail {
  font-size: 0.9rem;
  color: #666;
}

.maintenance-room {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: white;
  border: 2px solid #E8E8E8;
  border-radius: 10px;
  font-weight: 500;
  color: #4A4A4A;
}

.maintenance-schedule {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.25rem;
  background: linear-gradient(135deg, #FAFAFA 0%, #F8F8F8 100%);
  border-radius: 10px;
  border: 1px solid rgba(124, 106, 70, 0.1);
}

.schedule-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.schedule-label {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #999;
}

.schedule-date {
  font-size: 1rem;
  font-weight: 600;
  color: #1C1C1C;
}

.schedule-time {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

.arrow-icon {
  flex-shrink: 0;
  opacity: 0.4;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: fadeIn 1s ease-out;
}

.empty-state svg {
  margin-bottom: 1.5rem;
  opacity: 0.6;
}

.empty-state h3 {
  font-family: 'Raleway', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #1C1C1C;
  margin: 0 0 0.75rem 0;
}

.empty-state p {
  color: #666;
  font-size: 1rem;
  margin: 0 0 2rem 0;
}

.btn-empty-state {
  padding: 0.875rem 2rem;
  background: linear-gradient(135deg, #7C6A46 0%, #5A4E36 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.3);
}

.btn-empty-state:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.4);
}

.btn-empty-state:active {
  transform: translateY(0);
}

/* Responsive Design */
@media (max-width: 1200px) {
  .maintenance-grid {
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  }
}

@media (max-width: 768px) {
  .maintenance-list {
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

  .filter-inputs {
    grid-template-columns: 1fr;
  }

  .maintenance-grid {
    grid-template-columns: 1fr;
  }

  .maintenance-schedule {
    flex-direction: column;
    gap: 1rem;
  }

  .arrow-icon {
    transform: rotate(90deg);
  }

  .schedule-item {
    width: 100%;
    align-items: center;
    text-align: center;
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

  .btn-add {
    width: 100%;
  }

  .empty-state {
    padding: 2rem 1rem;
  }
}
</style>
