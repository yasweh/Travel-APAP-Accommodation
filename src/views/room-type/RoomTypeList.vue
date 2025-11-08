<template>
  <div class="room-type-list">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <path d="M32.5 7.5H30V5C30 3.625 28.875 2.5 27.5 2.5H12.5C11.125 2.5 10 3.625 10 5V7.5H7.5C6.125 7.5 5 8.625 5 10V32.5C5 33.875 6.125 35 7.5 35H32.5C33.875 35 35 33.875 35 32.5V10C35 8.625 33.875 7.5 32.5 7.5ZM12.5 5H27.5V7.5H12.5V5ZM32.5 32.5H7.5V10H32.5V32.5Z" fill="#7C6A46"/>
            <path d="M20 13.75C17.925 13.75 16.25 15.425 16.25 17.5C16.25 19.575 17.925 21.25 20 21.25C22.075 21.25 23.75 19.575 23.75 17.5C23.75 15.425 22.075 13.75 20 13.75Z" fill="#7C6A46"/>
            <path d="M20 23.75C16.5625 23.75 10 25.475 10 28.75V30H30V28.75C30 25.475 23.4375 23.75 20 23.75Z" fill="#7C6A46"/>
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">Room Type Management</h1>
          <p class="page-subtitle">Manage room types and their configurations</p>
        </div>
      </div>
    </div>

    <!-- Property Selector -->
    <div class="filters-container">
      <div class="filter-card">
        <div class="filter-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7V9L12 14L22 9V7L12 2ZM2 11V13L12 18L22 13V11L12 16L2 11Z" fill="#7C6A46"/>
          </svg>
          <span class="filter-title">Select Property</span>
        </div>
        <div class="filter-content">
          <div class="form-group">
            <label for="propertySelect">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 0L0 5V7L8 12L16 7V5L8 0ZM0 9V11L8 16L16 11V9L8 14L0 9Z" fill="#7C6A46"/>
              </svg>
              Property
            </label>
            <select id="propertySelect" v-model="selectedPropertyId" @change="loadRoomTypes">
              <option value="">-- All Properties --</option>
              <option v-for="prop in properties" :key="prop.propertyId" :value="prop.propertyId">
                {{ prop.propertyName }}
              </option>
            </select>
          </div>

          <button v-if="selectedPropertyId" @click="goToCreateRoomType" class="btn-add">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM15 11H11V15H9V11H5V9H9V5H11V9H15V11Z" fill="white"/>
            </svg>
            Add Room Type to Property
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
      <p>Loading room types...</p>
    </div>

    <!-- Room Types Grid -->
    <div v-else-if="roomTypes.length > 0" class="room-type-grid">
      <div v-for="roomType in roomTypes" :key="roomType.roomTypeId" class="room-type-card">
        <div class="card-header">
          <div class="room-type-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V5H19V19Z" fill="#7C6A46"/>
              <rect x="7" y="13" width="10" height="2" fill="#7C6A46"/>
              <rect x="7" y="9" width="5" height="2" fill="#7C6A46"/>
            </svg>
          </div>
          <div class="room-type-name">
            <h3>{{ roomType.name }}</h3>
            <span class="room-type-id">ID: {{ roomType.roomTypeId }}</span>
          </div>
        </div>

        <div class="card-body">
          <div class="room-info-grid">
            <div class="info-item">
              <div class="info-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <path d="M17.5 2.5H15L12.5 0C11.875 -0.625 11.0625 -1 10.25 -1H9.75C8.9375 -1 8.125 -0.625 7.5 0L5 2.5H2.5C1.125 2.5 0 3.625 0 5V17.5C0 18.875 1.125 20 2.5 20H17.5C18.875 20 20 18.875 20 17.5V5C20 3.625 18.875 2.5 17.5 2.5Z" fill="#7C6A46"/>
                </svg>
              </div>
              <div class="info-content">
                <span class="info-label">Floor</span>
                <span class="info-value">{{ roomType.floor }}</span>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <path d="M10 0C7.925 0 6.25 1.675 6.25 3.75C6.25 5.825 7.925 7.5 10 7.5C12.075 7.5 13.75 5.825 13.75 3.75C13.75 1.675 12.075 0 10 0ZM10 10C6.5625 10 0 11.725 0 15V17.5H20V15C20 11.725 13.4375 10 10 10Z" fill="#4CAF50"/>
                </svg>
              </div>
              <div class="info-content">
                <span class="info-label">Capacity</span>
                <span class="info-value">{{ roomType.capacity }} people</span>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <path d="M17.5 0H2.5C1.125 0 0 1.125 0 2.5V15C0 16.375 1.125 17.5 2.5 17.5H7.5V20H12.5V17.5H17.5C18.875 17.5 20 16.375 20 15V2.5C20 1.125 18.875 0 17.5 0ZM17.5 15H2.5V2.5H17.5V15Z" fill="#FF9800"/>
                </svg>
              </div>
              <div class="info-content">
                <span class="info-label">Facilities</span>
                <span class="info-value facility">{{ roomType.facility }}</span>
              </div>
            </div>
          </div>

          <div class="price-section">
            <span class="price-label">Price per Night</span>
            <span class="price-value">Rp {{ formatCurrency(roomType.price) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
        <path d="M65 15H60V10C60 7.25 57.75 5 55 5H25C22.25 5 20 7.25 20 10V15H15C12.25 15 10 17.25 10 20V65C10 67.75 12.25 70 15 70H65C67.75 70 70 67.75 70 65V20C70 17.25 67.75 15 65 15ZM25 10H55V15H25V10ZM65 65H15V20H65V65Z" fill="#D9D9D9"/>
        <path d="M40 27.5C35.85 27.5 32.5 30.85 32.5 35C32.5 39.15 35.85 42.5 40 42.5C44.15 42.5 47.5 39.15 47.5 35C47.5 30.85 44.15 27.5 40 27.5Z" fill="#D9D9D9"/>
        <path d="M40 47.5C33.125 47.5 20 50.95 20 57.5V60H60V57.5C60 50.95 46.875 47.5 40 47.5Z" fill="#D9D9D9"/>
      </svg>
      <h3>{{ selectedPropertyId ? 'No Room Types Found' : 'Select a Property' }}</h3>
      <p>{{ selectedPropertyId ? 'This property has no room types yet.' : 'Choose a property to view and manage its room types' }}</p>
      <button v-if="selectedPropertyId" @click="goToCreateRoomType" class="btn-empty-state">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM15 11H11V15H9V11H5V9H9V5H11V9H15V11Z" fill="white"/>
        </svg>
        Create First Room Type
      </button>
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
@import url('https://fonts.googleapis.com/css2?family=Raleway:wght@700;800&family=Poppins:wght@400;500;600&display=swap');

.room-type-list {
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

.filter-content {
  display: flex;
  gap: 1.5rem;
  align-items: end;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
  min-width: 280px;
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
  white-space: nowrap;
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

/* Room Type Grid */
.room-type-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 1.5rem;
  animation: fadeIn 1s ease-out;
}

.room-type-card {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(124, 106, 70, 0.1);
  transition: all 0.3s ease;
  animation: fadeIn 0.6s ease-out;
}

.room-type-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #7C6A46;
}

.card-header {
  background: linear-gradient(135deg, #7C6A46 0%, #5A4E36 100%);
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.room-type-icon {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.room-type-icon svg {
  filter: brightness(0) invert(1);
}

.room-type-name {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.room-type-name h3 {
  margin: 0;
  font-family: 'Raleway', sans-serif;
  font-size: 1.3rem;
  font-weight: 700;
  color: white;
  letter-spacing: -0.3px;
}

.room-type-id {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.card-body {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.room-info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: linear-gradient(135deg, #FAFAFA 0%, #F8F8F8 100%);
  border-radius: 10px;
  border-left: 3px solid transparent;
  transition: all 0.3s ease;
}

.info-item:nth-child(1) {
  border-left-color: #7C6A46;
}

.info-item:nth-child(2) {
  border-left-color: #4CAF50;
}

.info-item:nth-child(3) {
  border-left-color: #FF9800;
}

.info-icon {
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.info-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 1rem;
  font-weight: 600;
  color: #1C1C1C;
}

.info-value.facility {
  color: #666;
  font-weight: 500;
  font-size: 0.95rem;
}

.price-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1.25rem;
  background: linear-gradient(135deg, #7C6A46 0%, #5A4E36 100%);
  border-radius: 10px;
  margin-top: 0.5rem;
}

.price-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.price-value {
  font-family: 'Raleway', sans-serif;
  font-size: 1.75rem;
  font-weight: 700;
  color: white;
  letter-spacing: -0.5px;
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
  .room-type-grid {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .room-type-list {
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

  .filter-content {
    flex-direction: column;
  }

  .form-group {
    width: 100%;
  }

  .btn-add {
    width: 100%;
  }

  .room-type-grid {
    grid-template-columns: 1fr;
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

  .room-type-name h3 {
    font-size: 1.1rem;
  }

  .price-value {
    font-size: 1.5rem;
  }

  .empty-state {
    padding: 2rem 1rem;
  }
}
</style>
