<template>
  <div class="property-detail">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <path d="M20 2L2 12V38H38V12L20 2ZM20 6.5L34 14.5V34H6V14.5L20 6.5Z" fill="#7C6A46"/>
            <path d="M14 18H10V22H14V18ZM20 18H16V22H20V18ZM26 18H22V22H26V18ZM30 18H30V22H30V18Z" fill="#7C6A46"/>
            <path d="M14 26H10V30H14V26ZM20 26H16V30H20V26ZM26 26H22V30H26V26Z" fill="#7C6A46"/>
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">{{ property?.propertyName || 'Property Details' }}</h1>
          <p class="page-subtitle">View property information and manage rooms</p>
        </div>
        <span v-if="property" :class="['status-badge', property.activeStatus === 1 ? 'status-active' : 'status-inactive']">
          {{ property.activeStatus === 1 ? 'Active' : 'Inactive' }}
        </span>
      </div>
      <div class="header-actions" v-if="property">
        <button @click="goToEdit" class="btn-action btn-edit">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M0 12.667V16h3.333l9.833-9.833-3.333-3.334L0 12.667zm15.74-9.073c.347-.347.347-.907 0-1.254L13.66.26c-.346-.347-.906-.347-1.253 0l-1.627 1.627 3.334 3.333 1.626-1.626z" fill="currentColor"/>
          </svg>
          Update
        </button>
        <button @click="goToAddRoom" class="btn-action btn-primary">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M14 7H9V2c0-.55-.45-1-1-1s-1 .45-1 1v5H2c-.55 0-1 .45-1 1s.45 1 1 1h5v5c0 .55.45 1 1 1s1-.45 1-1V9h5c.55 0 1-.45 1-1s-.45-1-1-1z" fill="currentColor"/>
          </svg>
          Add Room
        </button>
        <button @click="confirmDelete" class="btn-action btn-delete">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M5 0h6v2H5V0zm9 3H2v1h1v11c0 .55.45 1 1 1h8c.55 0 1-.45 1-1V4h1V3zM4 15V5h8v10H4z" fill="currentColor"/>
          </svg>
          Delete
        </button>
        <button @click="goBack" class="btn-action btn-secondary">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M16 7H3.83l5.59-5.59L8 0 0 8l8 8 1.41-1.41L3.83 9H16V7z" fill="currentColor"/>
          </svg>
          Back
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Loading property details...</p>
    </div>

    <!-- Error Message -->
    <div v-else-if="error" class="message-box error">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM11 15H9V13H11V15ZM11 11H9V5H11V11Z" fill="#F44336"/>
      </svg>
      <span>{{ error }}</span>
    </div>
    
    <div v-else-if="property" class="content-container">

      <!-- Date Filter Card -->
      <div class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M19 4H18V2H16V4H8V2H6V4H5C3.89 4 3 4.9 3 6V20C3 21.1 3.89 22 5 22H19C20.11 22 21 21.1 21 20V6C21 4.9 20.11 4 19 4ZM19 20H5V9H19V20Z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">Filter Available Rooms</h2>
        </div>
        <div class="filter-form">
          <div class="form-row">
            <div class="form-group">
              <label for="checkIn">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M5.5 7h5v5h-5V7z" fill="#7C6A46"/>
                </svg>
                Check In Date
              </label>
              <input type="date" id="checkIn" v-model="filterCheckIn" />
            </div>
            <div class="form-group">
              <label for="checkOut">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M5.5 7h5v5h-5V7z" fill="#7C6A46"/>
                </svg>
                Check Out Date
              </label>
              <input type="date" id="checkOut" v-model="filterCheckOut" />
            </div>
          </div>
          <div class="form-actions">
            <button @click="applyFilter" class="btn-submit" type="button">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M6 10.5l-3-3 1.41-1.41L6 7.67l5.59-5.59L13 3.5 6 10.5z" fill="currentColor"/>
              </svg>
              Apply Filter
            </button>
            <button @click="clearFilter" class="btn-cancel" type="button">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 0C3.58 0 0 3.58 0 8s3.58 8 8 8 8-3.58 8-8-3.58-8-8-8zm4 10.59L10.59 12 8 9.41 5.41 12 4 10.59 6.59 8 4 5.41 5.41 4 8 6.59 10.59 4 12 5.41 9.41 8 12 10.59z" fill="currentColor"/>
              </svg>
              Clear Filter
            </button>
          </div>
        </div>
        <div v-if="filterActive" class="filter-badge">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 0C3.58 0 0 3.58 0 8s3.58 8 8 8 8-3.58 8-8-3.58-8-8-8zm1 12H7V7h2v5zm0-6H7V4h2v2z" fill="#2196F3"/>
          </svg>
          Showing available rooms from {{ filterCheckIn }} to {{ filterCheckOut }}
        </div>
      </div>

      <!-- Property Information Card -->
      <div class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5zm0 11.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V7.3l7-3.11v8.8z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">Property Information</h2>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <label>Property ID</label>
            <span class="info-value">{{ property.propertyId }}</span>
          </div>
          <div class="info-item">
            <label>Type</label>
            <span class="info-value">{{ getPropertyType(property.type) }}</span>
          </div>
          <div class="info-item">
            <label>Province</label>
            <span class="info-value">{{ getProvinceName(property.province) }}</span>
          </div>
          <div class="info-item">
            <label>Total Rooms</label>
            <span class="info-value">{{ property.totalRoom }}</span>
          </div>
          <div class="info-item">
            <label>Income</label>
            <span class="info-value highlight">Rp {{ formatCurrency(property.income) }}</span>
          </div>
          <div class="info-item">
            <label>Owner Name</label>
            <span class="info-value">{{ property.ownerName }}</span>
          </div>
          <div class="info-item">
            <label>Owner ID</label>
            <span class="info-value">{{ property.ownerId }}</span>
          </div>
          <div class="info-item full-width">
            <label>Address</label>
            <span class="info-value">{{ property.address }}</span>
          </div>
          <div class="info-item full-width">
            <label>Description</label>
            <span class="info-value">{{ property.description || '-' }}</span>
          </div>
          <div class="info-item">
            <label>Created Date</label>
            <span class="info-value">{{ formatDate(property.createdDate) }}</span>
          </div>
          <div class="info-item">
            <label>Updated Date</label>
            <span class="info-value">{{ formatDate(property.updatedDate) }}</span>
          </div>
        </div>
      </div>

      <!-- Room Types & Rooms Section -->
      <div v-if="roomTypes && roomTypes.length > 0" class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z" fill="#7C6A46"/>
            <path d="M7 10h2v7H7zm4-3h2v10h-2zm4 6h2v4h-2z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">Room Types & Rooms</h2>
        </div>
        
        <div v-for="roomType in roomTypes" :key="roomType.roomTypeId" class="room-type-section">
          <div class="room-type-header">
            <div class="room-type-title">
              <h3>{{ roomType.name }}</h3>
              <span class="room-type-price">Rp {{ formatCurrency(roomType.price) }} / night</span>
            </div>
            <div class="room-type-badge">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 0L0 4v8c0 4.42 3.07 8.55 8 9.57 4.93-1.02 8-5.15 8-9.57V4L8 0z" fill="#7C6A46"/>
              </svg>
              <span>{{ getRoomsByType(roomType.roomTypeId).length }} rooms</span>
            </div>
          </div>
          
          <div class="room-type-info">
            <div class="info-tag">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M7 0C3.13 0 0 3.13 0 7s3.13 7 7 7 7-3.13 7-7-3.13-7-7-7zm0 10.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z" fill="#7C6A46"/>
              </svg>
              ID: {{ roomType.roomTypeId }}
            </div>
            <div class="info-tag">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M7 0C3.13 0 0 3.13 0 7s3.13 7 7 7 7-3.13 7-7-3.13-7-7-7zm3.5 9.5h-7v-5h7v5z" fill="#7C6A46"/>
              </svg>
              Capacity: {{ roomType.capacity }} person(s)
            </div>
            <div class="info-tag">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M12 0H2C.9 0 0 .9 0 2v10c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V2c0-1.1-.9-2-2-2z" fill="#7C6A46"/>
              </svg>
              Floor: {{ roomType.floor }}
            </div>
            <div v-if="roomType.facility" class="info-tag">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M7 0L0 7l7 7 7-7-7-7zm0 10L4 7l3-3 3 3-3 3z" fill="#7C6A46"/>
              </svg>
              {{ roomType.facility }}
            </div>
          </div>
          
          <div v-if="roomType.description" class="room-type-description">
            {{ roomType.description }}
          </div>

          <!-- Rooms Grid -->
          <div class="rooms-grid" v-if="getRoomsByType(roomType.roomTypeId).length > 0">
            <div 
              v-for="room in getRoomsByType(roomType.roomTypeId)" 
              :key="room.roomId"
              class="room-card-modern"
            >
              <div class="room-card-header">
                <div class="room-card-title">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <path d="M16 2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" fill="#7C6A46"/>
                  </svg>
                  <h4>{{ room.name || room.roomId }}</h4>
                </div>
                <div class="room-card-badges">
                  <span :class="['room-badge', room.availabilityStatus === 1 ? 'badge-available' : 'badge-unavailable']">
                    {{ room.availabilityStatus === 1 ? 'Available' : 'Booked' }}
                  </span>
                  <span :class="['room-badge', room.activeRoom === 1 ? 'badge-active' : 'badge-inactive']">
                    {{ room.activeRoom === 1 ? 'Active' : 'Maintenance' }}
                  </span>
                </div>
              </div>
              <div class="room-card-actions">
                <button 
                  @click="goToBook(room.roomId)" 
                  class="btn-room-action btn-book"
                  :disabled="room.availabilityStatus !== 1 || room.activeRoom !== 1"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M14 0H2C.9 0 0 .9 0 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V2c0-1.1-.9-2-2-2zm-2 9H9v5H7V9H4l4-6 4 6z" fill="currentColor"/>
                  </svg>
                  Book Now
                </button>
                <button 
                  @click="goToMaintenance(room.roomId)" 
                  class="btn-room-action btn-maintenance"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M15.5 6.5l-1.8-1.8c-.3-.3-.8-.3-1.1 0l-.9.9-2.6-2.6.9-.9c.3-.3.3-.8 0-1.1L8.2.2c-.3-.3-.8-.3-1.1 0L.5 6.8c-.3.3-.3.8 0 1.1l1.8 1.8c.3.3.8.3 1.1 0l.9-.9 2.6 2.6-.9.9c-.3.3-.3.8 0 1.1l1.8 1.8c.3.3.8.3 1.1 0l6.6-6.6c.3-.3.3-.8 0-1.1z" fill="currentColor"/>
                  </svg>
                  Maintenance
                </button>
              </div>
            </div>
          </div>
          <div v-else class="empty-rooms">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <path d="M24 4C12.96 4 4 12.96 4 24s8.96 20 20 20 20-8.96 20-20S35.04 4 24 4zm0 36c-8.84 0-16-7.16-16-16S15.16 8 24 8s16 7.16 16 16-7.16 16-16 16z" fill="#CCC"/>
            </svg>
            <p>No rooms created for this room type yet</p>
          </div>
        </div>
      </div>

      <!-- No Room Types -->
      <div v-else class="form-card empty-state">
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
          <path d="M32 8L8 20v20c0 14.8 10.24 28.64 24 32 13.76-3.36 24-17.2 24-32V20L32 8z" stroke="#CCC" stroke-width="4" fill="none"/>
        </svg>
        <h3>No Room Types Yet</h3>
        <p>This property doesn't have any room types configured.</p>
        <button @click="goToAddRoom" class="btn-submit">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M14 7H9V2c0-.55-.45-1-1-1s-1 .45-1 1v5H2c-.55 0-1 .45-1 1s.45 1 1 1h5v5c0 .55.45 1 1 1s1-.45 1-1V9h5c.55 0 1-.45 1-1s-.45-1-1-1z" fill="currentColor"/>
          </svg>
          Add Room Type
        </button>
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
@import url('https://fonts.googleapis.com/css2?family=Raleway:wght@300;400;500;600;700&family=Poppins:wght@300;400;500;600;700&display=swap');

.property-detail {
  min-height: 100vh;
  background: linear-gradient(135deg, #F5F3EF 0%, #E8E6E2 100%);
  padding: 40px 20px;
  font-family: 'Poppins', sans-serif;
}

/* Page Header */
.page-header {
  background: linear-gradient(135deg, #7C6A46 0%, #5A4A30 100%);
  border-radius: 20px;
  padding: 40px;
  margin-bottom: 30px;
  box-shadow: 0 10px 30px rgba(124, 106, 70, 0.3);
  animation: slideDown 0.5s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 25px;
  position: relative;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.header-text {
  flex: 1;
}

.page-title {
  font-family: 'Raleway', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.page-subtitle {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 400;
}

.status-badge {
  padding: 10px 20px;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  animation: pulse 2s ease-in-out infinite;
}

.status-active {
  background: rgba(76, 175, 80, 0.25);
  color: #C8E6C9;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.status-inactive {
  background: rgba(158, 158, 158, 0.25);
  color: #E0E0E0;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn-action {
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.btn-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.btn-action:active {
  transform: translateY(0);
}

.btn-edit {
  background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%);
  color: white;
}

.btn-primary {
  background: linear-gradient(135deg, #4CAF50 0%, #388E3C 100%);
  color: white;
}

.btn-delete {
  background: linear-gradient(135deg, #F44336 0%, #D32F2F 100%);
  color: white;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
}

/* Loading State */
.loading-container {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.spinner {
  width: 60px;
  height: 60px;
  border: 4px solid #F5F3EF;
  border-top-color: #7C6A46;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-container p {
  color: #666;
  font-size: 18px;
  margin: 0;
}

/* Message Box */
.message-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 25px;
  font-weight: 500;
  animation: slideDown 0.3s ease-out;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.message-box.error {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  color: #C62828;
  border-left: 4px solid #F44336;
}

.message-box.success {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  color: #2E7D32;
  border-left: 4px solid #4CAF50;
}

/* Content Container */
.content-container {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

/* Form Card */
.form-card {
  background: white;
  border-radius: 20px;
  padding: 35px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  animation: fadeIn 0.5s ease-out;
  transition: all 0.3s ease;
}

.form-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #F5F3EF;
}

.card-title {
  font-family: 'Raleway', sans-serif;
  font-size: 1.5rem;
  font-weight: 600;
  color: #7C6A46;
  margin: 0;
}

/* Filter Form */
.filter-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #555;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-group input[type="date"] {
  padding: 12px 16px;
  border: 2px solid #E8E6E2;
  border-radius: 10px;
  font-size: 14px;
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s ease;
  background: white;
}

.form-group input[type="date"]:focus {
  outline: none;
  border-color: #7C6A46;
  box-shadow: 0 0 0 4px rgba(124, 106, 70, 0.1);
}

.form-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn-submit,
.btn-cancel {
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 140px;
  justify-content: center;
}

.btn-submit {
  background: linear-gradient(135deg, #7C6A46 0%, #5A4A30 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(124, 106, 70, 0.3);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel {
  background: white;
  color: #666;
  border: 2px solid #E8E8E8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.btn-cancel:hover {
  background: #F5F5F5;
  border-color: #D0D0D0;
  transform: translateY(-2px);
}

.filter-badge {
  margin-top: 15px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  border-left: 4px solid #2196F3;
  border-radius: 10px;
  color: #1565C0;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
}

/* Info Grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F5F5F5 100%);
  border-radius: 12px;
  border-left: 4px solid #7C6A46;
  transition: all 0.3s ease;
}

.info-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  font-weight: 600;
  color: #7C6A46;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  color: #333;
  font-size: 16px;
  font-weight: 500;
}

.info-value.highlight {
  color: #7C6A46;
  font-weight: 700;
  font-size: 18px;
}

/* Room Type Section */
.room-type-section {
  padding: 25px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F5F5F5 100%);
  border-radius: 16px;
  margin-bottom: 20px;
  border: 2px solid #E8E6E2;
  transition: all 0.3s ease;
}

.room-type-section:hover {
  border-color: #7C6A46;
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.15);
}

.room-type-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #E8E6E2;
}

.room-type-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.room-type-title h3 {
  font-family: 'Raleway', sans-serif;
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.room-type-price {
  font-size: 1.2rem;
  font-weight: 700;
  color: #7C6A46;
  background: rgba(124, 106, 70, 0.1);
  padding: 8px 16px;
  border-radius: 20px;
}

.room-type-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: white;
  border-radius: 20px;
  color: #7C6A46;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.room-type-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 15px;
}

.info-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: white;
  border-radius: 20px;
  font-size: 13px;
  color: #666;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.room-type-description {
  padding: 15px;
  background: white;
  border-radius: 10px;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 20px;
  border-left: 3px solid #7C6A46;
}

/* Rooms Grid */
.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.room-card-modern {
  background: white;
  border-radius: 16px;
  padding: 20px;
  border: 2px solid #E8E6E2;
  transition: all 0.3s ease;
  animation: fadeIn 0.3s ease-out;
}

.room-card-modern:hover {
  border-color: #7C6A46;
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(124, 106, 70, 0.2);
}

.room-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #F5F3EF;
}

.room-card-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.room-card-title h4 {
  font-family: 'Raleway', sans-serif;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.room-card-badges {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}

.room-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-available {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  color: #2E7D32;
}

.badge-unavailable {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%);
  color: #C62828;
}

.badge-active {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  color: #1565C0;
}

.badge-inactive {
  background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%);
  color: #E65100;
}

.room-card-actions {
  display: flex;
  gap: 10px;
}

.btn-room-action {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  font-size: 13px;
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn-book {
  background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
}

.btn-book:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(33, 150, 243, 0.4);
}

.btn-book:disabled {
  background: linear-gradient(135deg, #BDBDBD 0%, #9E9E9E 100%);
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-maintenance {
  background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
}

.btn-maintenance:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(255, 152, 0, 0.4);
}

/* Empty Rooms */
.empty-rooms {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 12px;
  margin-top: 20px;
  border: 2px dashed #E8E6E2;
}

.empty-rooms svg {
  opacity: 0.5;
  margin-bottom: 15px;
}

.empty-rooms p {
  color: #999;
  font-size: 14px;
  margin: 0;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 40px;
}

.empty-state svg {
  opacity: 0.3;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-family: 'Raleway', sans-serif;
  font-size: 1.5rem;
  color: #7C6A46;
  margin: 0 0 10px 0;
}

.empty-state p {
  color: #999;
  font-size: 14px;
  margin: 0 0 25px 0;
}

/* Responsive Design */
@media (max-width: 768px) {
  .property-detail {
    padding: 20px 15px;
  }

  .page-header {
    padding: 30px 25px;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-icon {
    width: 60px;
    height: 60px;
  }

  .page-title {
    font-size: 1.8rem;
  }

  .page-subtitle {
    font-size: 1rem;
  }

  .header-actions {
    width: 100%;
  }

  .btn-action {
    flex: 1;
    min-width: auto;
  }

  .form-card {
    padding: 25px 20px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .rooms-grid {
    grid-template-columns: 1fr;
  }

  .room-type-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }

  .card-title {
    font-size: 1.2rem;
  }

  .room-card-actions {
    flex-direction: column;
  }
}
</style>
