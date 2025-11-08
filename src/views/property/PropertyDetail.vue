<template>
  <div class="property-detail">
    <div v-if="loading" class="loading">Loading property details...</div>
    <div v-else-if="error" class="error-message">{{ error }}</div>
    
    <div v-else-if="property" class="detail-container">
      <!-- Header -->
      <div class="detail-header">
        <div class="header-content">
          <h1>{{ property.propertyName }}</h1>
          <span :class="['status-badge', property.activeStatus === 1 ? 'active' : 'inactive']">
            {{ property.activeStatus === 1 ? 'Active' : 'Inactive' }}
          </span>
        </div>
        <div class="header-actions">
          <button @click="goToEdit" class="btn-edit">Update Property</button>
          <button @click="goToAddRoom" class="btn-primary">Add Room</button>
          <button @click="confirmDelete" class="btn-delete">Delete Property</button>
          <button @click="goBack" class="btn-secondary">Back</button>
        </div>
      </div>

      <!-- Date Filter -->
      <div class="filter-section">
        <h3>Filter Available Rooms</h3>
        <div class="filter-form">
          <div class="form-group">
            <label>Check In Date:</label>
            <input type="date" v-model="filterCheckIn" />
          </div>
          <div class="form-group">
            <label>Check Out Date:</label>
            <input type="date" v-model="filterCheckOut" />
          </div>
          <button @click="applyFilter" class="btn-primary">Apply Filter</button>
          <button @click="clearFilter" class="btn-secondary">Clear Filter</button>
        </div>
        <div v-if="filterActive" class="filter-info">
          Showing available rooms from {{ filterCheckIn }} to {{ filterCheckOut }}
        </div>
      </div>

      <!-- Property Info -->
      <div class="info-section">
        <h2>Property Information</h2>
        <div class="info-grid">
          <div class="info-item">
            <label>Property ID:</label>
            <span>{{ property.propertyId }}</span>
          </div>
          <div class="info-item">
            <label>Type:</label>
            <span>{{ getPropertyType(property.type) }}</span>
          </div>
          <div class="info-item">
            <label>Province:</label>
            <span>{{ getProvinceName(property.province) }}</span>
          </div>
          <div class="info-item">
            <label>Total Rooms:</label>
            <span>{{ property.totalRoom }}</span>
          </div>
          <div class="info-item">
            <label>Income:</label>
            <span>Rp {{ formatCurrency(property.income) }}</span>
          </div>
          <div class="info-item">
            <label>Owner Name:</label>
            <span>{{ property.ownerName }}</span>
          </div>
          <div class="info-item">
            <label>Owner ID:</label>
            <span>{{ property.ownerId }}</span>
          </div>
          <div class="info-item full-width">
            <label>Address:</label>
            <span>{{ property.address }}</span>
          </div>
          <div class="info-item full-width">
            <label>Description:</label>
            <span>{{ property.description || '-' }}</span>
          </div>
          <div class="info-item">
            <label>Created Date:</label>
            <span>{{ formatDate(property.createdDate) }}</span>
          </div>
          <div class="info-item">
            <label>Updated Date:</label>
            <span>{{ formatDate(property.updatedDate) }}</span>
          </div>
        </div>
      </div>

      <!-- Room Types Section -->
      <div class="room-types-section" v-if="roomTypes && roomTypes.length > 0">
        <h2>Room Types & Rooms</h2>
        <div v-for="roomType in roomTypes" :key="roomType.roomTypeId" class="room-type-card">
          <div class="room-type-header">
            <h3>{{ roomType.name }}</h3>
            <span class="price">Rp {{ formatCurrency(roomType.price) }} / night</span>
          </div>
          <div class="room-type-details">
            <div class="detail-row">
              <span class="label">Room Type ID:</span>
              <span>{{ roomType.roomTypeId }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Capacity:</span>
              <span>{{ roomType.capacity }} person(s)</span>
            </div>
            <div class="detail-row">
              <span class="label">Floor:</span>
              <span>{{ roomType.floor }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Facility:</span>
              <span>{{ roomType.facility || '-' }}</span>
            </div>
            <div class="detail-row" v-if="roomType.description">
              <span class="label">Description:</span>
              <span>{{ roomType.description }}</span>
            </div>
          </div>

          <!-- Rooms for this Room Type -->
          <div class="rooms-for-type" v-if="getRoomsByType(roomType.roomTypeId).length > 0">
            <h4>Rooms ({{ getRoomsByType(roomType.roomTypeId).length }} total)</h4>
            <div class="rooms-grid">
              <div 
                v-for="room in getRoomsByType(roomType.roomTypeId)" 
                :key="room.roomId"
                :class="['room-card', getStatusClass(room.availabilityStatus), getActiveClass(room.activeRoom)]"
              >
                <div class="room-header">
                  <h4>{{ room.name || room.roomId }}</h4>
                </div>
                <div class="room-info">
                  <div class="status-badges">
                    <span :class="['badge', room.availabilityStatus === 1 ? 'badge-available' : 'badge-unavailable']">
                      {{ room.availabilityStatus === 1 ? 'Available' : 'Not Available' }}
                    </span>
                    <span :class="['badge', room.activeRoom === 1 ? 'badge-active' : 'badge-inactive']">
                      {{ room.activeRoom === 1 ? 'Active' : 'Maintenance' }}
                    </span>
                  </div>
                  <div class="room-actions">
                    <button @click="goToBook(room.roomId)" class="btn-book" :disabled="room.availabilityStatus !== 1 || room.activeRoom !== 1">
                      Book
                    </button>
                    <button @click="goToMaintenance(room.roomId)" class="btn-maintenance">
                      Maintenance
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-rooms">
            <p>No rooms created for this room type yet.</p>
          </div>
        </div>
      </div>

      <!-- No Room Types -->
      <div v-else class="empty-section">
        <p>No room types configured for this property.</p>
        <button @click="goToAddRoom" class="btn-primary">Add Room Type</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { propertyService } from '@/services/propertyService'

const router = useRouter()
const route = useRoute()

const property = ref<any>(null)
const roomTypes = ref<any[]>([])
const rooms = ref<any[]>([])
const loading = ref(false)
const error = ref('')
const filterCheckIn = ref('')
const filterCheckOut = ref('')
const filterActive = ref(false)

const loadPropertyDetail = async (checkIn?: string, checkOut?: string) => {
  loading.value = true
  error.value = ''
  try {
    const propertyId = route.params.id as string
    let response
    
    if (checkIn && checkOut) {
      // Call with date filter
      response = await propertyService.getByIdWithFilter(propertyId, checkIn, checkOut)
    } else {
      response = await propertyService.getById(propertyId)
    }
    
    if (response.success) {
      // Backend returns: { success, property, roomTypes, rooms }
      property.value = response.property
      roomTypes.value = response.roomTypes || []
      rooms.value = response.rooms || []
      
      console.log('Property loaded:', property.value)
      console.log('Room Types:', roomTypes.value)
      console.log('Rooms:', rooms.value)
    } else {
      error.value = response.message || 'Failed to load property'
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load property details'
    console.error('Load error:', err)
  } finally {
    loading.value = false
  }
}

const getRoomsByType = (roomTypeId: string) => {
  return rooms.value.filter(room => room.roomType?.roomTypeId === roomTypeId)
}

const getRoomTypeName = (roomTypeId: string) => {
  const roomType = roomTypes.value.find(rt => rt.roomTypeId === roomTypeId)
  return roomType?.name || 'Unknown'
}

const getActiveClass = (activeRoom: number) => {
  return activeRoom === 1 ? 'active' : 'inactive'
}

const applyFilter = async () => {
  if (!filterCheckIn.value || !filterCheckOut.value) {
    error.value = 'Please select both check-in and check-out dates'
    return
  }
  
  if (new Date(filterCheckIn.value) >= new Date(filterCheckOut.value)) {
    error.value = 'Check-out date must be after check-in date'
    return
  }
  
  filterActive.value = true
  await loadPropertyDetail(filterCheckIn.value, filterCheckOut.value)
}

const clearFilter = async () => {
  filterCheckIn.value = ''
  filterCheckOut.value = ''
  filterActive.value = false
  await loadPropertyDetail()
}

const goToBook = (roomId: string) => {
  const checkIn = filterCheckIn.value || new Date().toISOString().split('T')[0]
  const checkOut = filterCheckOut.value || new Date(Date.now() + 86400000).toISOString().split('T')[0]
  router.push({
    path: '/booking/create',
    query: { roomId, checkIn, checkOut }
  })
}

const goToMaintenance = (roomId: string) => {
  router.push({
    path: '/maintenance/create',
    query: { roomId }
  })
}

const getPropertyType = (type: number) => {
  const types = ['Hotel', 'Villa', 'Apartemen']
  return types[type] || 'Unknown'
}

const getProvinceName = (province: number) => {
  const provinces = [
    'DKI Jakarta', 'Jawa Barat', 'Jawa Tengah', 'Jawa Timur',
    'Bali', 'Sumatera Utara', 'Sulawesi Selatan', 'Kalimantan Timur'
  ]
  return provinces[province] || 'Unknown'
}

const getStatusClass = (status: number) => {
  if (status === 1) return 'available'
  if (status === 0) return 'not-available'
  return 'maintenance'
}

const getStatusLabel = (status: number) => {
  if (status === 1) return 'Available'
  if (status === 0) return 'Not Available'
  return 'Maintenance'
}

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('id-ID').format(value)
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('id-ID', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goBack = () => {
  router.push('/property')
}

const goToEdit = () => {
  router.push(`/property/edit/${property.value.propertyId}`)
}

const goToAddRoom = () => {
  // Redirect to property update form (single source of truth)
  router.push(`/property/edit/${property.value.propertyId}`)
}

const confirmDelete = async () => {
  if (!confirm(`Are you sure you want to delete property "${property.value.propertyName}"?`)) return

  try {
    const response = await propertyService.delete(property.value.propertyId)
    if (response.success) {
      alert('Property deleted successfully')
      router.push('/property')
    } else {
      alert(response.message)
    }
  } catch (err: any) {
    alert(err.response?.data?.message || 'Failed to delete property')
  }
}

onMounted(() => {
  loadPropertyDetail()
})
</script>

<style scoped>
.property-detail {
  padding: 20px;
  width: 100%;
  max-width: 100%;
  margin: 0;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #666;
  font-size: 18px;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  margin: 20px 0;
}

.detail-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.detail-header {
  background: linear-gradient(135deg, #4caf50 0%, #2196f3 100%);
  color: white;
  padding: 30px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.header-content h1 {
  margin: 0;
  font-size: 32px;
}

.status-badge {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
}

.status-badge.active {
  background: rgba(76, 175, 80, 0.3);
  border: 2px solid #fff;
}

.status-badge.inactive {
  background: rgba(158, 158, 158, 0.3);
  border: 2px solid #fff;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-edit,
.btn-delete,
.btn-secondary,
.btn-primary {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-edit {
  background: #ff9800;
  color: white;
}

.btn-edit:hover {
  background: #e68900;
}

.btn-delete {
  background: #f44336;
  color: white;
}

.btn-delete:hover {
  background: #da190b;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2px solid white;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.3);
}

.btn-primary {
  background: #4caf50;
  color: white;
}

.btn-primary:hover {
  background: #45a049;
}

.info-section,
.room-types-section,
.empty-section {
  padding: 30px;
}

.info-section h2,
.room-types-section h2 {
  color: #333;
  margin: 0 0 20px 0;
  font-size: 24px;
  border-bottom: 2px solid #4caf50;
  padding-bottom: 10px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  font-weight: 600;
  color: #666;
  font-size: 14px;
}

.info-item span {
  color: #333;
  font-size: 16px;
}

.room-type-card {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  border-left: 4px solid #4caf50;
}

.rooms-for-type {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ddd;
}

.rooms-for-type h4 {
  margin: 0 0 15px 0;
  color: #555;
  font-size: 16px;
}

.no-rooms {
  margin-top: 15px;
  padding: 15px;
  background: #fff3cd;
  border-radius: 4px;
  color: #856404;
  text-align: center;
}

.no-rooms p {
  margin: 0;
  font-size: 14px;
}

.room-type-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.room-type-header h3 {
  margin: 0;
  color: #333;
  font-size: 20px;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #4caf50;
}

.room-type-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.detail-row {
  display: flex;
  gap: 10px;
}

.detail-row .label {
  font-weight: 600;
  color: #666;
  min-width: 120px;
}

.rooms-list h4 {
  margin: 20px 0 10px 0;
  color: #555;
  font-size: 16px;
}

.room-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.room-chip {
  padding: 8px 16px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
  border: 2px solid;
}

.room-chip.available {
  background: #e8f5e9;
  border-color: #4caf50;
  color: #2e7d32;
}

.room-chip.not-available {
  background: #ffebee;
  border-color: #f44336;
  color: #c62828;
}

.room-chip.maintenance {
  background: #fff3e0;
  border-color: #ff9800;
  color: #e65100;
}

.room-number {
  font-weight: bold;
  font-size: 14px;
}

.room-status {
  font-size: 11px;
  margin-top: 2px;
}

.empty-section {
  text-align: center;
  padding: 60px;
  color: #999;
}

.empty-section p {
  margin-bottom: 20px;
  font-size: 16px;
}

/* Filter Section */
.filter-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-section h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
}

.filter-form {
  display: flex;
  gap: 15px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-form .form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.filter-form label {
  font-weight: 600;
  color: #555;
  font-size: 14px;
}

.filter-form input[type="date"] {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.filter-info {
  margin-top: 15px;
  padding: 10px;
  background: #e3f2fd;
  border-left: 4px solid #2196f3;
  color: #1565c0;
  font-size: 14px;
}

/* Rooms Section */
.rooms-section {
  margin-top: 30px;
}

.rooms-section h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 24px;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.room-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 2px solid;
  transition: all 0.3s ease;
}

.room-card.available {
  border-color: #4caf50;
}

.room-card.not-available {
  border-color: #f44336;
  opacity: 0.7;
}

.room-card.inactive {
  background: #f5f5f5;
  border-color: #ff9800;
}

.room-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.room-header {
  margin-bottom: 15px;
}

.room-header h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 18px;
}

.room-type-label {
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 4px 8px;
  border-radius: 4px;
}

.room-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.status-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.badge-available {
  background: #e8f5e9;
  color: #2e7d32;
}

.badge-unavailable {
  background: #ffebee;
  color: #c62828;
}

.badge-active {
  background: #e3f2fd;
  color: #1565c0;
}

.badge-inactive {
  background: #fff3e0;
  color: #e65100;
}

.room-actions {
  display: flex;
  gap: 8px;
}

.btn-book {
  flex: 1;
  padding: 8px 16px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s;
}

.btn-book:hover:not(:disabled) {
  background: #1976d2;
}

.btn-book:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-maintenance {
  flex: 1;
  padding: 8px 16px;
  background: #ff9800;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s;
}

.btn-maintenance:hover {
  background: #f57c00;
}
</style>
