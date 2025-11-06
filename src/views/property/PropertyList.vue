<template>
  <div class="property-list">
    <h1>Property Management</h1>

    <!-- Error Message -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- Actions -->
    <div class="actions">
      <button @click="goToCreateProperty" class="btn-primary">Create New Property</button>
      <button @click="loadProperties" class="btn-secondary">Refresh</button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">Loading properties...</div>

    <!-- Properties Table -->
    <table v-else-if="properties.length > 0" class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Property Name</th>
          <th>Type</th>
          <th>Address</th>
          <th>Room Count</th>
          <th>Income</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="property in properties" :key="property.propertyId">
          <td>{{ property.propertyId }}</td>
          <td>{{ property.propertyName }}</td>
          <td>{{ property.typeString }}</td>
          <td>{{ property.address }}</td>
          <td>{{ property.roomCount || 0 }} rooms</td>
          <td>Rp {{ formatCurrency(property.income) }}</td>
          <td>
            <span :class="property.activeStatus === 1 ? 'status-active' : 'status-inactive'">
              {{ property.activeStatusString || (property.activeStatus === 1 ? 'Active' : 'Inactive') }}
            </span>
          </td>
          <td class="actions-cell">
            <button 
              @click="goToDetail(property.propertyId)" 
              class="btn-detail"
            >
              Detail
            </button>
            <button 
              @click="goToEdit(property.propertyId)" 
              class="btn-edit"
            >
              Edit
            </button>
            <button 
              @click="deleteProperty(property.propertyId)" 
              class="btn-delete"
            >
              Delete
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <p>No properties found. Create your first property!</p>
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
.property-list {
  padding: 20px;
  max-width: 1200px;
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

.actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.btn-primary,
.btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
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

.status-active {
  color: #4caf50;
  font-weight: bold;
}

.status-inactive {
  color: #999;
}

.actions-cell {
  display: flex;
  gap: 8px;
}

.btn-detail,
.btn-edit,
.btn-delete {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.btn-detail {
  background-color: #2196f3;
  color: white;
}

.btn-detail:hover {
  background-color: #0b7dda;
}

.btn-edit {
  background-color: #ff9800;
  color: white;
}

.btn-edit:hover {
  background-color: #e68900;
}

.btn-delete {
  background-color: #f44336;
  color: white;
}

.btn-delete:hover {
  background-color: #da190b;
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
