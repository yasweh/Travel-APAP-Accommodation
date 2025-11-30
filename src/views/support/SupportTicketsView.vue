<template>
  <div class="support-tickets-container">
    <div class="page-header">
      <h1>Support Tickets</h1>
      <button class="btn btn-primary" @click="showCreateTicketModal = true">
        Create New Ticket
      </button>
    </div>

    <!-- Filters -->
    <div class="filters">
      <select v-model="filterStatus" @change="fetchTickets" class="filter-select">
        <option value="">All Status</option>
        <option value="OPEN">Open</option>
        <option value="IN_PROGRESS">In Progress</option>
        <option value="CLOSED">Closed</option>
      </select>

      <select v-model="filterServiceSource" @change="fetchTickets" class="filter-select">
        <option value="">All Services</option>
        <option value="ACCOMMODATION">Accommodation</option>
        <option value="FLIGHT">Flight</option>
        <option value="RENTAL">Rental</option>
        <option value="TOUR">Tour</option>
        <option value="INSURANCE">Insurance</option>
      </select>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">Loading tickets...</div>

    <!-- Error State -->
    <div v-if="error" class="error">{{ error }}</div>

    <!-- Tickets List -->
    <div v-if="!loading && !error" class="tickets-list">
      <div v-if="tickets.length === 0" class="no-tickets">
        <p>No support tickets found.</p>
        <button class="btn btn-primary" @click="showCreateTicketModal = true">
          Create Your First Ticket
        </button>
      </div>

      <div v-for="ticket in tickets" :key="ticket.id" class="ticket-card">
        <div class="ticket-header">
          <h3>{{ ticket.subject }}</h3>
          <span :class="['status-badge', `status-${ticket.status.toLowerCase()}`]">
            {{ ticket.status }}
          </span>
        </div>

        <div class="ticket-info">
          <div class="info-row">
            <span class="label">Service:</span>
            <span class="value">{{ ticket.serviceSource }}</span>
          </div>
          <div class="info-row">
            <span class="label">Booking ID:</span>
            <span class="value">{{ ticket.externalBookingId }}</span>
          </div>
          <div class="info-row">
            <span class="label">Created:</span>
            <span class="value">{{ formatDate(ticket.createdAt) }}</span>
          </div>
          <div v-if="ticket.unreadMessagesCount > 0" class="info-row">
            <span class="unread-badge">{{ ticket.unreadMessagesCount }} unread messages</span>
          </div>
        </div>

        <div class="ticket-actions">
          <button class="btn btn-secondary" @click="viewTicketDetail(ticket.id)">
            View Details
          </button>
          <button
            v-if="ticket.status === 'OPEN'"
            class="btn btn-danger"
            @click="deleteTicket(ticket.id)"
          >
            Delete
          </button>
        </div>
      </div>
    </div>

    <!-- Create Ticket Modal -->
    <CreateTicketModal
      v-if="showCreateTicketModal"
      @close="showCreateTicketModal = false"
      @ticket-created="onTicketCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import supportTicketService, { type TicketResponse } from '@/services/supportTicketService'
import CreateTicketModal from '@/components/support/CreateTicketModal.vue'

const router = useRouter()

// State
const tickets = ref<TicketResponse[]>([])
const loading = ref(false)
const error = ref('')
const showCreateTicketModal = ref(false)

// Filters
const filterStatus = ref('')
const filterServiceSource = ref('')

// Mock user ID (in real app, get from auth store/session)
const currentUserId = ref('263d012e-b86a-4813-be96-41e6da78e00d') // John Doe from seeder

// Fetch tickets
const fetchTickets = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await supportTicketService.getAllTickets(
      currentUserId.value,
      filterStatus.value || undefined,
      filterServiceSource.value || undefined
    )
    tickets.value = response.data
  } catch (err: any) {
    error.value = err.response?.data?.error || 'Failed to fetch tickets'
    console.error('Error fetching tickets:', err)
  } finally {
    loading.value = false
  }
}

// View ticket detail
const viewTicketDetail = (ticketId: string) => {
  router.push({ name: 'support-ticket-detail', params: { id: ticketId } })
}

// Delete ticket
const deleteTicket = async (ticketId: string) => {
  if (!confirm('Are you sure you want to delete this ticket?')) {
    return
  }

  try {
    await supportTicketService.deleteTicket(ticketId, currentUserId.value)
    await fetchTickets()
    alert('Ticket deleted successfully')
  } catch (err: any) {
    alert(err.response?.data?.error || 'Failed to delete ticket')
  }
}

// Format date
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString()
}

// On ticket created
const onTicketCreated = () => {
  showCreateTicketModal.value = false
  fetchTickets()
}

// On mounted
onMounted(() => {
  fetchTickets()
})
</script>

<style scoped>
.support-tickets-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  margin: 0;
}

.filters {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.filter-select {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
}

.loading,
.error {
  text-align: center;
  padding: 40px;
  font-size: 16px;
}

.error {
  color: #dc3545;
}

.no-tickets {
  text-align: center;
  padding: 60px 20px;
}

.no-tickets p {
  font-size: 18px;
  color: #666;
  margin-bottom: 20px;
}

.tickets-list {
  display: grid;
  gap: 20px;
}

.ticket-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.ticket-header h3 {
  margin: 0;
  font-size: 18px;
}

.status-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.status-open {
  background-color: #ffc107;
  color: #000;
}

.status-in_progress {
  background-color: #17a2b8;
  color: white;
}

.status-closed {
  background-color: #28a745;
  color: white;
}

.ticket-info {
  margin-bottom: 15px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-row .label {
  font-weight: bold;
  width: 120px;
}

.info-row .value {
  color: #666;
}

.unread-badge {
  background-color: #dc3545;
  color: white;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.ticket-actions {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn:hover {
  opacity: 0.8;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}
</style>
