<template>
  <div class="property-list">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <path d="M10 10C8.55 10 7.021 9.804 5.413 9.412C3.80433 9.02067 2 8.55 2 8C2 7.45 3.80433 6.979 5.413 6.587C7.021 6.19567 8.55 6 10 6C11.45 6 12.979 6.19567 14.587 6.587C16.196 6.979 18 7.45 18 8C18 8.55 16.196 9.02067 14.587 9.412C12.979 9.804 11.45 10 10 10ZM10 20C5.31667 17.7167 3.31267 15.5957 1.988 13.637C0.662667 11.679 0 9.86667 0 8.2C0 5.7 0.804333 3.70833 2.413 2.225C4.021 0.741667 5.88333 0 8 0C10.1167 0 11.979 0.741667 13.587 2.225C15.1957 3.70833 16 5.7 16 8.2C16 9.86667 15.3377 11.679 14.013 13.637C12.6877 15.5957 10.6833 17.7167 8 20Z" fill="#7C6A46"/>
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">Property Management</h1>
          <p class="page-subtitle">Manage your properties and accommodations</p>
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

    <!-- Actions -->
    <div class="actions">
      <button @click="goToCreateProperty" class="btn-primary">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM15 11H11V15H9V11H5V9H9V5H11V9H15V11Z" fill="white"/>
        </svg>
        Create New Property
      </button>
      <button @click="loadProperties" class="btn-secondary">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4C7.58 4 4.01 7.58 4.01 12C4.01 16.42 7.58 20 12 20C15.73 20 18.84 17.45 19.73 14H17.65C16.83 16.33 14.61 18 12 18C8.69 18 6 15.31 6 12C6 8.69 8.69 6 12 6C13.66 6 15.14 6.69 16.22 7.78L13 11H20V4L17.65 6.35Z" fill="#7C6A46"/>
        </svg>
        Refresh
      </button>
      
      <!-- Quick Access Buttons -->
      <div class="quick-actions">
        <button @click="goToRoomTypes" class="btn-quick">
          <svg width="20" height="20" viewBox="0 0 23 20" fill="none">
            <path d="M16.6667 17.7778H18.8889V8.88889H12.2222V17.7778H14.4444V11.1111H16.6667V17.7778ZM1.11111 17.7778V1.11111C1.11111 0.816426 1.22817 0.533811 1.43655 0.325437C1.64492 0.117063 1.92754 0 2.22222 0H17.7778C18.0725 0 18.3551 0.117063 18.5635 0.325437C18.7718 0.533811 18.8889 0.816426 18.8889 1.11111V6.66667H21.1111V17.7778H22.2222V20H0V17.7778H1.11111ZM5.55556 8.88889V11.1111H7.77778V8.88889H5.55556ZM5.55556 13.3333V15.5556H7.77778V13.3333H5.55556ZM5.55556 4.44444V6.66667H7.77778V4.44444H5.55556Z" fill="#7C6A46"/>
          </svg>
          Room Types
        </button>
        <button @click="goToMaintenance" class="btn-quick">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M15.95 10.78C15.98 10.52 16 10.26 16 10C16 9.74 15.98 9.48 15.95 9.22L17.63 7.9C17.78 7.78 17.82 7.56 17.73 7.38L16.13 4.62C16.04 4.44 15.82 4.38 15.64 4.44L13.63 5.24C13.23 4.94 12.79 4.7 12.31 4.52L12 2.42C11.98 2.22 11.8 2.08 11.6 2.08H8.4C8.2 2.08 8.02 2.22 8 2.42L7.69 4.52C7.21 4.7 6.77 4.95 6.37 5.24L4.36 4.44C4.18 4.37 3.96 4.44 3.87 4.62L2.27 7.38C2.18 7.56 2.22 7.78 2.37 7.9L4.05 9.22C4.02 9.48 4 9.75 4 10C4 10.25 4.02 10.52 4.05 10.78L2.37 12.1C2.22 12.22 2.18 12.44 2.27 12.62L3.87 15.38C3.96 15.56 4.18 15.62 4.36 15.56L6.37 14.76C6.77 15.06 7.21 15.3 7.69 15.48L8 17.58C8.02 17.78 8.2 17.92 8.4 17.92H11.6C11.8 17.92 11.98 17.78 12 17.58L12.31 15.48C12.79 15.3 13.23 15.05 13.63 14.76L15.64 15.56C15.82 15.63 16.04 15.56 16.13 15.38L17.73 12.62C17.82 12.44 17.78 12.22 17.63 12.1L15.95 10.78ZM10 13C8.35 13 7 11.65 7 10C7 8.35 8.35 7 10 7C11.65 7 13 8.35 13 10C13 11.65 11.65 13 10 13Z" fill="#7C6A46"/>
          </svg>
          Maintenance
        </button>
        <button @click="goToStatistics" class="btn-quick">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM9 17H7V10H9V17ZM13 17H11V7H13V17ZM17 17H15V13H17V17Z" fill="#7C6A46"/>
          </svg>
          Statistics
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>Loading properties...</p>
    </div>

    <!-- Properties Grid -->
    <div v-else-if="properties.length > 0" class="properties-grid">
      <div v-for="property in properties" :key="property.propertyId" class="property-card">
        <div class="card-header">
          <div class="property-icon">
            <svg width="30" height="30" viewBox="0 0 30 30" fill="none">
              <path d="M15 0L0 10H5L15 3L25 10H30L15 0ZM7.5 7.5V0H10V10L7.5 7.5Z" fill="#7C6A46"/>
            </svg>
          </div>
          <div class="property-status">
            <span :class="property.activeStatus === 1 ? 'status-badge active' : 'status-badge inactive'">
              {{ property.activeStatusString || (property.activeStatus === 1 ? 'Active' : 'Inactive') }}
            </span>
          </div>
        </div>
        
        <div class="card-body">
          <h3 class="property-name">{{ property.propertyName }}</h3>
          <div class="property-info">
            <div class="info-item">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 0C5.79 0 4 1.79 4 4C4 7 8 12 8 12C8 12 12 7 12 4C12 1.79 10.21 0 8 0Z" fill="#7C6A46"/>
              </svg>
              <span>{{ property.typeString }}</span>
            </div>
            <div class="info-item">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M14 2H12V0H10V2H6V0H4V2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2Z" fill="#7C6A46"/>
              </svg>
              <span>{{ property.roomCount || 0 }} rooms</span>
            </div>
          </div>
          <div class="property-address">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M7 0C4.24 0 2 2.24 2 5C2 8.5 7 14 7 14C7 14 12 8.5 12 5C12 2.24 9.76 0 7 0Z" fill="#999"/>
            </svg>
            <span>{{ property.address }}</span>
          </div>
          <div class="property-income">
            <span class="income-label">Total Income</span>
            <span class="income-value">Rp {{ formatCurrency(property.income) }}</span>
          </div>
        </div>
        
        <div class="card-actions">
          <button @click="goToDetail(property.propertyId)" class="btn-action btn-detail">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 3C4.5 3 1.5 5.5 0 9C1.5 12.5 4.5 15 8 15C11.5 15 14.5 12.5 16 9C14.5 5.5 11.5 3 8 3ZM8 13C6.34 13 5 11.66 5 10C5 8.34 6.34 7 8 7C9.66 7 11 8.34 11 10C11 11.66 9.66 13 8 13Z" fill="white"/>
            </svg>
            Detail
          </button>
          <button @click="goToEdit(property.propertyId)" class="btn-action btn-edit">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M0 12.667V16h3.333l9.833-9.833-3.333-3.334L0 12.667zM15.733 3.6a.885.885 0 000-1.255L13.655.267a.885.885 0 00-1.255 0L10.9 1.767l3.333 3.333L15.733 3.6z" fill="white"/>
            </svg>
            Edit
          </button>
          <button @click="deleteProperty(property.propertyId)" class="btn-action btn-delete">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M4 12.666c0 .734.6 1.334 1.333 1.334h5.334c.733 0 1.333-.6 1.333-1.334V4H4v8.666zM12.667 1.333h-2.334L9.667.667H6.333l-.666.666H3.333V2.667h9.334V1.333z" fill="white"/>
            </svg>
            Delete
          </button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
        <path d="M40 0L0 30H10L40 10L70 30H80L40 0ZM20 20V0H25V27.5L20 20Z" fill="#D9D9D9"/>
      </svg>
      <h3>No Properties Found</h3>
      <p>Start by creating your first property to manage accommodations</p>
      <button @click="goToCreateProperty" class="btn-empty-state">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM15 11H11V15H9V11H5V9H9V5H11V9H15V11Z" fill="white"/>
        </svg>
        Create First Property
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { propertyService, type Property } from '@/services/propertyService'

const router = useRouter()

const properties = ref<Property[]>([])
const loading = ref(false)
const error = ref('')

const loadProperties = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await propertyService.getAll()
    if (response.success) {
      properties.value = response.data
    } else {
      error.value = response.message
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load properties'
    console.error('Load properties error:', err)
  } finally {
    loading.value = false
  }
}

const goToCreateProperty = () => {
  router.push('/property/create')
}

const goToDetail = (propertyId: string) => {
  router.push(`/property/${propertyId}`)
}

const goToEdit = (propertyId: string) => {
  router.push(`/property/edit/${propertyId}`)
}

const goToRoomTypes = () => {
  router.push('/room-type')
}

const goToMaintenance = () => {
  router.push('/maintenance')
}

const goToStatistics = () => {
  router.push('/booking/chart')
}

const deleteProperty = async (propertyId: string) => {
  if (!confirm('Are you sure you want to delete this property?')) return

  try {
    const response = await propertyService.delete(propertyId)
    if (response.success) {
      alert('Property deleted successfully')
      loadProperties() // Reload list
    } else {
      alert(response.message)
    }
  } catch (err: any) {
    alert(err.response?.data?.message || 'Failed to delete property')
    console.error('Delete error:', err)
  }
}

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('id-ID').format(value)
}

onMounted(() => {
  loadProperties()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&family=Raleway:wght@500;600;700;800&display=swap');

.property-list {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 60px;
  animation: fadeIn 0.5s ease-out;
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
  margin-bottom: 40px;
  padding: 30px 40px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(124, 106, 70, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 25px;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: white;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(124, 106, 70, 0.15);
}

.header-icon svg {
  transform: scale(2);
}

.header-text {
  flex: 1;
}

.page-title {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  color: #4A4A4A;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  opacity: 0.9;
}

/* Error Message */
.error-message {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #FEE;
  color: #C33;
  padding: 16px 20px;
  border-radius: 10px;
  margin-bottom: 25px;
  border-left: 4px solid #F44336;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 500;
}

/* Actions */
.actions {
  display: flex;
  gap: 15px;
  margin-bottom: 35px;
  flex-wrap: wrap;
  align-items: center;
}

.quick-actions {
  display: flex;
  gap: 12px;
  margin-left: auto;
}

.btn-primary,
.btn-secondary,
.btn-quick {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 28px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-primary {
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.3);
}

.btn-secondary {
  background: white;
  color: #7C6A46;
  border: 2px solid #7C6A46;
}

.btn-secondary:hover {
  background: #FAFAFA;
  transform: translateY(-2px);
}

.btn-quick {
  background: white;
  color: #7C6A46;
  border: 2px solid #E0E0E0;
  padding: 12px 20px;
  font-size: 14px;
}

.btn-quick:hover {
  background: #FAFAFA;
  border-color: #7C6A46;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(124, 106, 70, 0.15);
}

/* Loading State */
.loading {
  text-align: center;
  padding: 80px 20px;
  color: #7C6A46;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 500;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  margin: 0 auto 20px;
  border: 4px solid #FAFAFA;
  border-top: 4px solid #7C6A46;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Properties Grid */
.properties-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

.property-card {
  background: white;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid #F0F0F0;
}

.property-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(124, 106, 70, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px 25px 15px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
}

.property-icon {
  width: 60px;
  height: 60px;
  background: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.15);
}

.property-status {
  display: flex;
  align-items: center;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.active {
  background: #E8F5E9;
  color: #2E7D32;
}

.status-badge.inactive {
  background: #FAFAFA;
  color: #999;
}

.card-body {
  padding: 20px 25px;
}

.property-name {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 15px 0;
}

.property-info {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #4A4A4A;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 500;
}

.property-address {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 400;
  margin-bottom: 15px;
  line-height: 1.5;
}

.property-address svg {
  flex-shrink: 0;
  margin-top: 2px;
}

.property-income {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #FAFAFA;
  border-radius: 10px;
  margin-top: 15px;
}

.income-label {
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 500;
}

.income-value {
  color: #7C6A46;
  font-family: 'Raleway', sans-serif;
  font-size: 18px;
  font-weight: 700;
}

.card-actions {
  display: flex;
  gap: 10px;
  padding: 20px 25px;
  background: #FAFAFA;
  border-top: 1px solid #F0F0F0;
}

.btn-action {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  color: white;
}

.btn-detail {
  background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%);
}

.btn-detail:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.4);
}

.btn-edit {
  background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%);
}

.btn-edit:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.4);
}

.btn-delete {
  background: linear-gradient(135deg, #F44336 0%, #D32F2F 100%);
}

.btn-delete:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.4);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
  border-radius: 15px;
  margin-top: 20px;
}

.empty-state svg {
  margin-bottom: 25px;
  opacity: 0.4;
}

.empty-state h3 {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
}

.empty-state p {
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 400;
  margin: 0 0 30px 0;
}

.btn-empty-state {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 32px;
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(124, 106, 70, 0.3);
}

.btn-empty-state:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.4);
}

/* Responsive Design */
@media (max-width: 1200px) {
  .properties-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }
}

@media (max-width: 768px) {
  .property-list {
    padding: 20px 20px;
  }

  .page-header {
    padding: 20px;
  }

  .header-content {
    flex-direction: column;
    text-align: center;
  }

  .page-title {
    font-size: 32px;
  }

  .properties-grid {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
  }

  .btn-primary,
  .btn-secondary {
    width: 100%;
    justify-content: center;
  }
}
</style>
