<template>
  <div class="ticket-detail-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-icon">
        <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
          <path d="M32 5H28C28 2.24 25.76 0 23 0H17C14.24 0 12 2.24 12 5H8C5.8 5 4 6.8 4 9V34C4 36.2 5.8 38 8 38H32C34.2 38 36 36.2 36 34V9C36 6.8 34.2 5 32 5ZM20 3C21.66 3 23 4.34 23 6C23 7.66 21.66 9 20 9C18.34 9 17 7.66 17 6C17 4.34 18.34 3 20 3ZM32 34H8V9H12V12H28V9H32V34Z" fill="white"/>
        </svg>
      </div>
      <div class="header-text">
        <h1 class="page-title">Ticket Details</h1>
        <p class="page-subtitle">View and manage support ticket</p>
      </div>
    </div>

    <!-- Back Button -->
    <div class="nav-section">
      <button class="btn-back" @click="goBack">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M15.833 10H4.167" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M10 15.833L4.167 10L10 4.167" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>Back to Support Dashboard</span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Loading ticket details...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="message-box error">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM11 15H9V13H11V15ZM11 11H9V5H11V11Z" fill="#F44336"/>
      </svg>
      <span>{{ error }}</span>
      <button @click="fetchTicketDetail" class="btn-retry">Retry</button>
    </div>

    <!-- Ticket Content -->
    <div v-else-if="ticket" class="ticket-content">
      <!-- Ticket Info Card -->
      <div class="form-card ticket-summary">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V5H19V19Z" fill="#7C6A46"/>
            <path d="M12 7C10.9 7 10 7.9 10 9C10 10.1 10.9 11 12 11C13.1 11 14 10.1 14 9C14 7.9 13.1 7 12 7Z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">{{ ticket.subject }}</h2>
        </div>
        
        <div class="ticket-badges">
          <span :class="['status-badge', getStatusClass(ticket.status)]">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <circle cx="6" cy="6" r="5" fill="currentColor"/>
            </svg>
            {{ ticket.status }}
          </span>
          <span class="service-badge">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M12 1H2C1.45 1 1 1.45 1 2V12C1 12.55 1.45 13 2 13H12C12.55 13 13 12.55 13 12V2C13 1.45 12.55 1 12 1Z" fill="currentColor"/>
            </svg>
            {{ ticket.serviceSource }}
          </span>
        </div>

        <div class="ticket-meta-grid">
          <div class="meta-item">
            <span class="meta-label">Ticket ID</span>
            <span class="meta-value">{{ ticket.id.substring(0, 8) }}...</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Created</span>
            <span class="meta-value">{{ formatDate(ticket.createdAt) }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Last Updated</span>
            <span class="meta-value">{{ formatDate(ticket.updatedAt) }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Booking ID</span>
            <span class="meta-value">{{ ticket.externalBookingId || 'N/A' }}</span>
          </div>
        </div>
      </div>

      <!-- Tab Navigation -->
      <div class="tab-navigation">
        <button
          :class="['tab-btn', { active: activeTab === 'messages' }]"
          @click="activeTab = 'messages'"
        >
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <path d="M16 0H2C0.9 0 0 0.9 0 2V12C0 13.1 0.9 14 2 14H14L18 18V2C18 0.9 17.1 0 16 0ZM16 11.17L14.83 10H2V2H16V11.17Z" fill="currentColor"/>
          </svg>
          Messages
          <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'booking' }]"
          @click="activeTab = 'booking'"
        >
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <path d="M16 2H14V0H12V2H6V0H4V2H2C0.9 2 0 2.9 0 4V16C0 17.1 0.9 18 2 18H16C17.1 18 18 17.1 18 16V4C18 2.9 17.1 2 16 2ZM16 16H2V7H16V16ZM16 5H2V4H16V5Z" fill="currentColor"/>
          </svg>
          Booking Info
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'timeline' }]"
          @click="activeTab = 'timeline'"
        >
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <path d="M9 0C4.03 0 0 4.03 0 9C0 13.97 4.03 18 9 18C13.97 18 18 13.97 18 9C18 4.03 13.97 0 9 0ZM9 16C5.13 16 2 12.87 2 9C2 5.13 5.13 2 9 2C12.87 2 16 5.13 16 9C16 12.87 12.87 16 9 16ZM9.5 4.5H8V10L12.75 12.85L13.5 11.62L9.5 9.25V4.5Z" fill="currentColor"/>
          </svg>
          Timeline
        </button>
      </div>

      <!-- Messages Tab -->
      <div v-if="activeTab === 'messages'" class="tab-panel">
        <div class="form-card">
          <div class="card-header">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M20 2H4C2.9 2 2 2.9 2 4V22L6 18H20C21.1 18 22 17.1 22 16V4C22 2.9 21.1 2 20 2ZM20 16H5.17L4 17.17V4H20V16Z" fill="#7C6A46"/>
            </svg>
            <h2 class="card-title">Conversation</h2>
          </div>

          <div class="messages-container" ref="messagesContainer">
            <div v-if="ticket.messages.length === 0" class="empty-messages">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <path d="M40 4H8C5.8 4 4 5.8 4 8V44L12 36H40C42.2 36 44 34.2 44 32V8C44 5.8 42.2 4 40 4Z" fill="#E8E8E8"/>
              </svg>
              <p>No messages yet</p>
            </div>

            <div 
              v-for="message in ticket.messages" 
              :key="message.id"
              :class="['message-bubble', message.senderType.toLowerCase()]"
            >
              <div class="message-header">
                <span class="sender">{{ message.senderType === 'CUSTOMER' ? 'You' : 'Support Agent' }}</span>
                <span class="time">{{ formatDate(message.sentAt) }}</span>
              </div>
              <div class="message-body">{{ message.message }}</div>
            </div>
          </div>

          <!-- Message Input -->
          <div class="message-composer" v-if="ticket.status !== 'CLOSED'">
            <textarea
              v-model="newMessage"
              placeholder="Type your message here..."
              rows="3"
              @keydown.ctrl.enter="sendMessage"
            ></textarea>
            <button 
              class="btn-submit" 
              @click="sendMessage"
              :disabled="!newMessage.trim() || sending"
            >
              <svg v-if="!sending" width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M1.5 17.5L18.5 10L1.5 2.5V8.33L13.5 10L1.5 11.67V17.5Z" fill="white"/>
              </svg>
              <div v-else class="btn-spinner"></div>
              {{ sending ? 'Sending...' : 'Send Message' }}
            </button>
          </div>

          <div v-else class="ticket-closed-notice">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM8 15L3 10L4.41 8.59L8 12.17L15.59 4.58L17 6L8 15Z" fill="#4CAF50"/>
            </svg>
            <span>This ticket has been closed</span>
          </div>
        </div>
      </div>

      <!-- Booking Info Tab -->
      <div v-if="activeTab === 'booking'" class="tab-panel">
        <div class="form-card">
          <div class="card-header">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="#7C6A46"/>
            </svg>
            <h2 class="card-title">Booking Information</h2>
          </div>

          <div v-if="ticket.externalBookingDataAvailable && ticket.externalBookingData" class="booking-details-grid">
            <div 
              v-for="(value, key) in ticket.externalBookingData" 
              :key="key"
              class="detail-item"
            >
              <span class="detail-label">{{ formatKey(key) }}</span>
              <span class="detail-value">{{ formatValue(value) }}</span>
            </div>
          </div>

          <div v-else class="empty-state">
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
              <path d="M53.33 10.67H48V5.33C48 2.39 45.61 0 42.67 0H21.33C18.39 0 16 2.39 16 5.33V10.67H10.67C7.73 10.67 5.33 13.06 5.33 16V53.33C5.33 56.27 7.73 58.67 10.67 58.67H53.33C56.27 58.67 58.67 56.27 58.67 53.33V16C58.67 13.06 56.27 10.67 53.33 10.67ZM21.33 5.33H42.67V10.67H21.33V5.33ZM53.33 53.33H10.67V16H53.33V53.33Z" fill="#E8E8E8"/>
            </svg>
            <p>Booking information is not available</p>
            <span class="helper-text">The external booking service may be temporarily unavailable</span>
          </div>
        </div>
      </div>

      <!-- Timeline Tab -->
      <div v-if="activeTab === 'timeline'" class="tab-panel">
        <div class="form-card">
          <div class="card-header">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 20C7.58 20 4 16.42 4 12C4 7.58 7.58 4 12 4C16.42 4 20 7.58 20 12C20 16.42 16.42 20 12 20ZM12.5 7H11V13L16.25 16.15L17 14.92L12.5 12.25V7Z" fill="#7C6A46"/>
            </svg>
            <h2 class="card-title">Progress Timeline</h2>
          </div>

          <div class="timeline-container">
            <div 
              v-for="(entry, index) in ticket.progressEntries" 
              :key="entry.id"
              class="timeline-item"
            >
              <div class="timeline-marker">
                <div :class="['timeline-dot', { latest: index === 0 }]"></div>
                <div v-if="index < ticket.progressEntries.length - 1" class="timeline-line"></div>
              </div>
              <div class="timeline-content">
                <div class="timeline-header">
                  <span class="action-type">{{ entry.actionType }}</span>
                  <span class="timestamp">{{ formatDate(entry.performedAt) }}</span>
                </div>
                <p class="timeline-description">{{ entry.description }}</p>
              </div>
            </div>

            <div v-if="ticket.progressEntries.length === 0" class="empty-timeline">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <path d="M24 4C12.96 4 4 12.96 4 24C4 35.04 12.96 44 24 44C35.04 44 44 35.04 44 24C44 12.96 35.04 4 24 4ZM24 40C15.18 40 8 32.82 8 24C8 15.18 15.18 8 24 8C32.82 8 40 15.18 40 24C40 32.82 32.82 40 24 40ZM25 12H22V26L34.5 33.3L36 30.84L25 24.5V12Z" fill="#E8E8E8"/>
              </svg>
              <p>No progress entries yet</p>
            </div>
          </div>

          <!-- Add Progress Form -->
          <div v-if="ticket.status !== 'CLOSED'" class="add-progress-section">
            <h3 class="section-title">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM15 11H11V15H9V11H5V9H9V5H11V9H15V11Z" fill="#7C6A46"/>
              </svg>
              Add Progress Update
            </h3>
            <textarea
              v-model="newProgress"
              placeholder="Describe the progress or action taken..."
              rows="4"
            ></textarea>
            <button 
              class="btn-secondary"
              @click="addProgress"
              :disabled="!newProgress.trim() || addingProgress"
            >
              {{ addingProgress ? 'Adding...' : 'Add Progress' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Action Footer -->
      <div class="form-actions" v-if="ticket.status !== 'CLOSED'">
        <button class="btn-success" @click="closeTicket">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM8 15L3 10L4.41 8.59L8 12.17L15.59 4.58L17 6L8 15Z" fill="white"/>
          </svg>
          Close Ticket
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import supportTicketService, { type TicketDetailResponse } from '@/services/supportTicketService'

const route = useRoute()
const router = useRouter()

// State
const ticket = ref<TicketDetailResponse | null>(null)
const loading = ref(false)
const error = ref('')
const activeTab = ref('messages')
const newMessage = ref('')
const newProgress = ref('')
const sending = ref(false)
const addingProgress = ref(false)
const unreadCount = ref(0)
const messagesContainer = ref<HTMLElement | null>(null)

// Mock user ID (John Doe from seeder)
const currentUserId = ref('263d012e-b86a-4813-be96-41e6da78e00d')

// Fetch ticket detail
const fetchTicketDetail = async () => {
  loading.value = true
  error.value = ''
  try {
    const ticketId = route.params.id as string
    const response = await supportTicketService.getTicketDetail(ticketId, currentUserId.value)
    ticket.value = response.data

    unreadCount.value = ticket.value.messages.filter(
      (m) => !m.readByRecipient && m.senderId !== currentUserId.value
    ).length

    if (unreadCount.value > 0) {
      await supportTicketService.markMessagesAsRead(ticketId, currentUserId.value)
    }

    await nextTick()
    scrollToBottom()
  } catch (err: any) {
    error.value = err.response?.data?.error || 'Failed to fetch ticket details'
    console.error('Error fetching ticket:', err)
  } finally {
    loading.value = false
  }
}

// Send message
const sendMessage = async () => {
  if (!newMessage.value.trim() || !ticket.value) return

  sending.value = true
  try {
    await supportTicketService.addMessage(ticket.value.id, {
      senderId: currentUserId.value,
      senderType: 'CUSTOMER',
      message: newMessage.value.trim(),
    })

    newMessage.value = ''
    await fetchTicketDetail()
  } catch (err: any) {
    alert(err.response?.data?.error || 'Failed to send message')
  } finally {
    sending.value = false
  }
}

// Add progress
const addProgress = async () => {
  if (!newProgress.value.trim() || !ticket.value) return

  addingProgress.value = true
  try {
    await supportTicketService.addProgress(ticket.value.id, {
      performedBy: currentUserId.value,
      description: newProgress.value.trim(),
    })

    newProgress.value = ''
    await fetchTicketDetail()
    activeTab.value = 'timeline'
  } catch (err: any) {
    alert(err.response?.data?.error || 'Failed to add progress')
  } finally {
    addingProgress.value = false
  }
}

// Close ticket
const closeTicket = async () => {
  if (!ticket.value) return
  if (!confirm('Are you sure you want to close this ticket?')) return

  try {
    await supportTicketService.updateTicketStatus(ticket.value.id, {
      status: 'CLOSED',
      updatedBy: currentUserId.value,
      reason: 'Ticket closed by customer',
    })

    await fetchTicketDetail()
    alert('Ticket closed successfully')
  } catch (err: any) {
    alert(err.response?.data?.error || 'Failed to close ticket')
  }
}

// Go back
const goBack = () => {
  router.push({ name: 'support-dashboard' })
}

// Scroll to bottom
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Format date
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Get status class
const getStatusClass = (status: string) => {
  const classes: Record<string, string> = {
    OPEN: 'status-open',
    IN_PROGRESS: 'status-progress',
    CLOSED: 'status-closed',
  }
  return classes[status] || 'status-open'
}

// Format key for display
const formatKey = (key: string | number): string => {
  const strKey = String(key)
  return strKey
    .replace(/([A-Z])/g, ' $1')
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, (l) => l.toUpperCase())
}

// Format value for display
const formatValue = (value: any): string => {
  if (value === null || value === undefined) return '-'
  if (typeof value === 'boolean') return value ? 'Yes' : 'No'
  if (typeof value === 'object') return JSON.stringify(value, null, 2)
  return String(value)
}

// On mounted
onMounted(() => {
  fetchTicketDetail()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Raleway:wght@700;800&family=Poppins:wght@400;500;600&display=swap');

/* Container */
.ticket-detail-page {
  min-height: calc(100vh - 100px);
  padding: 60px 20px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F5F5F5 100%);
  animation: fadeIn 0.6s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Page Header */
.page-header {
  text-align: center;
  margin-bottom: 50px;
  animation: fadeIn 0.6s ease-in-out;
}

.header-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #7C6A46 0%, #5A4A30 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(124, 106, 70, 0.3);
}

.header-icon svg {
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.header-text h1 {
  font-family: 'Raleway', sans-serif;
  font-size: 2.5rem;
  font-weight: 800;
  color: #1C1C1C;
  margin: 0 0 10px 0;
  letter-spacing: -0.5px;
}

.header-text p {
  font-family: 'Poppins', sans-serif;
  font-size: 1.1rem;
  color: #4A4A4A;
  margin: 0;
}

.page-title {
  font-family: 'Raleway', sans-serif;
  font-size: 2.5rem;
  font-weight: 800;
  color: #1C1C1C;
  margin: 0;
}

.page-subtitle {
  font-family: 'Poppins', sans-serif;
  font-size: 1.1rem;
  color: #4A4A4A;
  margin: 0;
}

/* Navigation */
.nav-section {
  max-width: 1000px;
  margin: 0 auto 30px;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: white;
  border: 2px solid #7C6A46;
  color: #7C6A46;
  padding: 10px 20px;
  border-radius: 10px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-back:hover {
  background: #7C6A46;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.3);
}

/* Loading & Error */
.loading-container {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #E8E8E8;
  border-top-color: #7C6A46;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.message-box {
  max-width: 1000px;
  margin: 0 auto 30px;
  padding: 18px 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 15px;
  font-family: 'Poppins', sans-serif;
}

.message-box.error {
  background: linear-gradient(135deg, #FFF5F5 0%, #FFE5E5 100%);
  border-left: 4px solid #E53E3E;
  color: #C53030;
}

.btn-retry {
  margin-left: auto;
  background: #E53E3E;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}

/* Content Area */
.ticket-content {
  max-width: 1000px;
  margin: 0 auto;
}

/* Form Card */
.form-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 25px;
  transition: all 0.3s ease;
}

.form-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #F5F5F5;
}

.card-title {
  font-family: 'Raleway', sans-serif;
  font-size: 1.4rem;
  font-weight: 700;
  color: #1C1C1C;
  margin: 0;
}

/* Ticket Summary */
.ticket-badges {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.status-badge, .service-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-open {
  background: #FFF3E0;
  color: #F57C00;
}

.status-progress {
  background: #E3F2FD;
  color: #1976D2;
}

.status-closed {
  background: #E8F5E9;
  color: #388E3C;
}

.service-badge {
  background: #F5F5F5;
  color: #7C6A46;
}

.ticket-meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-family: 'Poppins', sans-serif;
  font-size: 0.8rem;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.meta-value {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 600;
  color: #1C1C1C;
}

/* Tab Navigation */
.tab-navigation {
  display: flex;
  gap: 10px;
  margin-bottom: 25px;
  background: white;
  padding: 8px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 20px;
  background: transparent;
  border: none;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 600;
  color: #888;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  background: #F5F5F5;
  color: #7C6A46;
}

.tab-btn.active {
  background: linear-gradient(135deg, #7C6A46 0%, #5A4A30 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.3);
}

.tab-btn .badge {
  background: #E53E3E;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.75rem;
}

/* Messages Container */
.messages-container {
  max-height: 400px;
  overflow-y: auto;
  padding: 20px;
  background: #FAFAFA;
  border-radius: 12px;
  margin-bottom: 20px;
}

.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #7C6A46;
  border-radius: 3px;
}

.empty-messages {
  text-align: center;
  padding: 40px;
  color: #888;
}

.empty-messages svg {
  margin-bottom: 15px;
}

.message-bubble {
  max-width: 75%;
  margin-bottom: 15px;
  padding: 15px 20px;
  border-radius: 15px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-bubble.customer {
  background: linear-gradient(135deg, #7C6A46 0%, #5A4A30 100%);
  color: white;
  margin-left: auto;
  border-bottom-right-radius: 5px;
}

.message-bubble.admin {
  background: white;
  border: 2px solid #E8E8E8;
  color: #1C1C1C;
  border-bottom-left-radius: 5px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.8rem;
  opacity: 0.8;
}

.message-body {
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* Message Composer */
.message-composer {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-composer textarea {
  width: 100%;
  padding: 15px;
  border: 2px solid #E8E8E8;
  border-radius: 12px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  resize: vertical;
  transition: border-color 0.3s;
}

.message-composer textarea:focus {
  outline: none;
  border-color: #7C6A46;
}

.ticket-closed-notice {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 20px;
  background: #E8F5E9;
  border-radius: 10px;
  color: #388E3C;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
}

/* Booking Details Grid */
.booking-details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.detail-item {
  background: #FAFAFA;
  padding: 15px 20px;
  border-radius: 10px;
  border-left: 4px solid #7C6A46;
}

.detail-label {
  display: block;
  font-family: 'Poppins', sans-serif;
  font-size: 0.8rem;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 5px;
}

.detail-value {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 600;
  color: #1C1C1C;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 40px;
  color: #888;
}

.empty-state p {
  font-family: 'Poppins', sans-serif;
  font-size: 1.1rem;
  margin: 15px 0 5px;
}

.helper-text {
  font-size: 0.9rem;
  color: #AAA;
}

/* Timeline */
.timeline-container {
  position: relative;
}

.timeline-item {
  display: flex;
  gap: 20px;
  margin-bottom: 25px;
}

.timeline-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.timeline-dot {
  width: 16px;
  height: 16px;
  background: #E8E8E8;
  border: 3px solid #7C6A46;
  border-radius: 50%;
}

.timeline-dot.latest {
  background: #7C6A46;
  box-shadow: 0 0 0 4px rgba(124, 106, 70, 0.2);
}

.timeline-line {
  flex: 1;
  width: 2px;
  background: #E8E8E8;
  margin-top: 5px;
}

.timeline-content {
  flex: 1;
  background: #FAFAFA;
  padding: 20px;
  border-radius: 12px;
  border: 2px solid #E8E8E8;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.action-type {
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
  font-weight: 600;
  color: #7C6A46;
  text-transform: uppercase;
}

.timestamp {
  font-size: 0.85rem;
  color: #888;
}

.timeline-description {
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  color: #4A4A4A;
  margin: 0;
  line-height: 1.6;
}

.empty-timeline {
  text-align: center;
  padding: 40px;
  color: #888;
}

/* Add Progress Section */
.add-progress-section {
  margin-top: 30px;
  padding-top: 25px;
  border-top: 2px dashed #E8E8E8;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: 'Raleway', sans-serif;
  font-size: 1.1rem;
  font-weight: 700;
  color: #1C1C1C;
  margin-bottom: 15px;
}

.add-progress-section textarea {
  width: 100%;
  padding: 15px;
  border: 2px solid #E8E8E8;
  border-radius: 12px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  resize: vertical;
  margin-bottom: 15px;
  transition: border-color 0.3s;
}

.add-progress-section textarea:focus {
  outline: none;
  border-color: #7C6A46;
}

/* Buttons */
.btn-submit, .btn-success {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #7C6A46 0%, #5A4A30 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(124, 106, 70, 0.3);
}

.btn-submit:hover:not(:disabled), .btn-success:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 106, 70, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: white;
  color: #7C6A46;
  border: 2px solid #7C6A46;
  border-radius: 10px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-secondary:hover:not(:disabled) {
  background: #7C6A46;
  color: white;
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-success {
  background: linear-gradient(135deg, #43A047 0%, #2E7D32 100%);
  box-shadow: 0 4px 15px rgba(67, 160, 71, 0.3);
}

.btn-success:hover:not(:disabled) {
  box-shadow: 0 6px 20px rgba(67, 160, 71, 0.4);
}

.btn-spinner {
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* Form Actions */
.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* Responsive */
@media (max-width: 768px) {
  .ticket-detail-page {
    padding: 40px 15px;
  }

  .page-title {
    font-size: 2rem;
  }

  .tab-navigation {
    flex-direction: column;
  }

  .tab-btn {
    justify-content: flex-start;
  }

  .message-bubble {
    max-width: 90%;
  }

  .ticket-meta-grid {
    grid-template-columns: 1fr 1fr;
  }

  .booking-details-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .header-icon {
    width: 60px;
    height: 60px;
  }

  .page-title {
    font-size: 1.75rem;
  }

  .form-card {
    padding: 20px;
  }

  .ticket-meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
