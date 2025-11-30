<template>
  <div class="support-dashboard">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V11H13V17ZM13 9H11V7H13V9Z" fill="#7C6A46"/>
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">Support Dashboard</h1>
          <p class="page-subtitle">Manage your bookings and support tickets</p>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div class="actions">
      <button @click="loadDashboard" class="btn-secondary">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4C7.58 4 4.01 7.58 4.01 12C4.01 16.42 7.58 20 12 20C15.73 20 18.84 17.45 19.73 14H17.65C16.83 16.33 14.61 18 12 18C8.69 18 6 15.31 6 12C6 8.69 8.69 6 12 6C13.66 6 15.14 6.69 16.22 7.78L13 11H20V4L17.65 6.35Z" fill="currentColor"/>
        </svg>
        Refresh Dashboard
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>Loading dashboard data...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-message">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="currentColor"/>
      </svg>
      <span>{{ error }}</span>
    </div>

    <!-- Dashboard Content -->
    <div v-else class="dashboard-content">
      <!-- Accommodation Bookings -->
      <section class="booking-section">
        <div class="section-header">
          <div class="section-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M7 13C8.66 13 10 11.66 10 10C10 8.34 8.66 7 7 7C5.34 7 4 8.34 4 10C4 11.66 5.34 13 7 13ZM19 7H11V9H19V7ZM19 11H11V13H19V11ZM16 20H8V16H16V20ZM2 20H6V16H2V20ZM18 20H22V16H18V20Z" fill="#7C6A46"/>
            </svg>
            <h2>Accommodation Bookings</h2>
          </div>
          <span class="section-badge">{{ dashboard.accommodationBookings.length }}</span>
        </div>
        <div v-if="loadingStates.accommodation" class="loading-section">
          <div class="loading-spinner-small"></div>
          <p>Loading accommodations...</p>
        </div>
        <div v-else-if="errorStates.accommodation" class="error-section">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="currentColor"/>
          </svg>
          <p>{{ errorStates.accommodation }}</p>
        </div>
        <div v-else-if="dashboard.accommodationBookings.length === 0" class="section-empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M7 13C8.66 13 10 11.66 10 10C10 8.34 8.66 7 7 7C5.34 7 4 8.34 4 10C4 11.66 5.34 13 7 13ZM19 7H11V9H19V7ZM19 11H11V13H19V11ZM16 20H8V16H16V20ZM2 20H6V16H2V20ZM18 20H22V16H18V20Z" fill="currentColor" opacity="0.3"/>
          </svg>
          <p>No accommodation bookings found</p>
        </div>
        <div v-else class="booking-grid">
          <BookingCard
            v-for="booking in dashboard.accommodationBookings"
            :key="booking.id"
            :booking="booking"
            service-type="ACCOMMODATION"
            @create-ticket="openCreateTicketModal"
          />
        </div>
      </section>

      <!-- Flight Bookings -->
      <section class="booking-section">
        <div class="section-header">
          <div class="section-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M21 16V14L13 9V3.5C13 2.67 12.33 2 11.5 2C10.67 2 10 2.67 10 3.5V9L2 14V16L10 13.5V19L8 20.5V22L11.5 21L15 22V20.5L13 19V13.5L21 16Z" fill="#7C6A46"/>
            </svg>
            <h2>Flight Bookings</h2>
          </div>
          <span class="section-badge">{{ dashboard.flightBookings.length }}</span>
        </div>
        <div v-if="loadingStates.flight" class="loading-section">
          <div class="loading-spinner-small"></div>
          <p>Loading flights...</p>
        </div>
        <div v-else-if="errorStates.flight" class="error-section">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="currentColor"/>
          </svg>
          <p>{{ errorStates.flight }}</p>
        </div>
        <div v-else-if="dashboard.flightBookings.length === 0" class="section-empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 16V14L13 9V3.5C13 2.67 12.33 2 11.5 2C10.67 2 10 2.67 10 3.5V9L2 14V16L10 13.5V19L8 20.5V22L11.5 21L15 22V20.5L13 19V13.5L21 16Z" fill="currentColor" opacity="0.3"/>
          </svg>
          <p>No flight bookings found</p>
        </div>
        <div v-else class="booking-grid">
          <BookingCard
            v-for="booking in dashboard.flightBookings"
            :key="booking.id"
            :booking="booking"
            service-type="FLIGHT"
            @create-ticket="openCreateTicketModal"
          />
        </div>
      </section>

      <!-- Rental Bookings -->
      <section class="booking-section">
        <div class="section-header">
          <div class="section-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5H6.5C5.84 5 5.28 5.42 5.08 6.01L3 12V20C3 20.55 3.45 21 4 21H5C5.55 21 6 20.55 6 20V19H18V20C18 20.55 18.45 21 19 21H20C20.55 21 21 20.55 21 20V12L18.92 6.01ZM6.5 16C5.67 16 5 15.33 5 14.5C5 13.67 5.67 13 6.5 13C7.33 13 8 13.67 8 14.5C8 15.33 7.33 16 6.5 16ZM17.5 16C16.67 16 16 15.33 16 14.5C16 13.67 16.67 13 17.5 13C18.33 13 19 13.67 19 14.5C19 15.33 18.33 16 17.5 16ZM5 11L6.5 6.5H17.5L19 11H5Z" fill="#7C6A46"/>
            </svg>
            <h2>Vehicle Rentals</h2>
          </div>
          <span class="section-badge">{{ dashboard.rentalBookings.length }}</span>
        </div>
        <div v-if="loadingStates.rental" class="loading-section">
          <div class="loading-spinner-small"></div>
          <p>Loading rentals...</p>
        </div>
        <div v-else-if="errorStates.rental" class="error-section">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="currentColor"/>
          </svg>
          <p>{{ errorStates.rental }}</p>
        </div>
        <div v-else-if="dashboard.rentalBookings.length === 0" class="section-empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5H6.5C5.84 5 5.28 5.42 5.08 6.01L3 12V20C3 20.55 3.45 21 4 21H5C5.55 21 6 20.55 6 20V19H18V20C18 20.55 18.45 21 19 21H20C20.55 21 21 20.55 21 20V12L18.92 6.01ZM6.5 16C5.67 16 5 15.33 5 14.5C5 13.67 5.67 13 6.5 13C7.33 13 8 13.67 8 14.5C8 15.33 7.33 16 6.5 16ZM17.5 16C16.67 16 16 15.33 16 14.5C16 13.67 16.67 13 17.5 13C18.33 13 19 13.67 19 14.5C19 15.33 18.33 16 17.5 16ZM5 11L6.5 6.5H17.5L19 11H5Z" fill="currentColor" opacity="0.3"/>
          </svg>
          <p>No rental bookings found</p>
        </div>
        <div v-else class="booking-grid">
          <BookingCard
            v-for="booking in dashboard.rentalBookings"
            :key="booking.id"
            :booking="booking"
            service-type="RENTAL"
            @create-ticket="openCreateTicketModal"
          />
        </div>
      </section>

      <!-- Tour Bookings -->
      <section class="booking-section">
        <div class="section-header">
          <div class="section-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z" fill="#7C6A46"/>
            </svg>
            <h2>Tour Packages</h2>
          </div>
          <span class="section-badge">{{ dashboard.tourBookings.length }}</span>
        </div>
        <div v-if="loadingStates.tour" class="loading-section">
          <div class="loading-spinner-small"></div>
          <p>Loading tours...</p>
        </div>
        <div v-else-if="errorStates.tour" class="error-section">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="currentColor"/>
          </svg>
          <p>{{ errorStates.tour }}</p>
        </div>
        <div v-else-if="dashboard.tourBookings.length === 0" class="section-empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z" fill="currentColor" opacity="0.3"/>
          </svg>
          <p>No tour bookings found</p>
        </div>
        <div v-else class="booking-grid">
          <BookingCard
            v-for="booking in dashboard.tourBookings"
            :key="booking.id"
            :booking="booking"
            service-type="TOUR"
            @create-ticket="openCreateTicketModal"
          />
        </div>
      </section>

      <!-- Insurance Bookings -->
      <section class="booking-section">
        <div class="section-header">
          <div class="section-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 1L3 5V11C3 16.55 6.84 21.74 12 23C17.16 21.74 21 16.55 21 11V5L12 1ZM12 11.99H19C18.47 16.11 15.72 19.78 12 20.93V12H5V6.3L12 3.19V11.99Z" fill="#7C6A46"/>
            </svg>
            <h2>Insurance Policies</h2>
          </div>
          <span class="section-badge">{{ dashboard.insuranceBookings.length }}</span>
        </div>
        <div v-if="loadingStates.insurance" class="loading-section">
          <div class="loading-spinner-small"></div>
          <p>Loading insurance...</p>
        </div>
        <div v-else-if="errorStates.insurance" class="error-section">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="currentColor"/>
          </svg>
          <p>{{ errorStates.insurance }}</p>
        </div>
        <div v-else-if="dashboard.insuranceBookings.length === 0" class="section-empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 1L3 5V11C3 16.55 6.84 21.74 12 23C17.16 21.74 21 16.55 21 11V5L12 1ZM12 11.99H19C18.47 16.11 15.72 19.78 12 20.93V12H5V6.3L12 3.19V11.99Z" fill="currentColor" opacity="0.3"/>
          </svg>
          <p>No insurance policies found</p>
        </div>
        <div v-else class="booking-grid">
          <BookingCard
            v-for="booking in dashboard.insuranceBookings"
            :key="booking.id"
            :booking="booking"
            service-type="INSURANCE"
            @create-ticket="openCreateTicketModal"
          />
        </div>
      </section>

      <!-- My Support Tickets -->
      <section class="tickets-section">
        <div class="section-header">
          <div class="section-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M22 10V6C22 4.9 21.1 4 20 4H4C2.9 4 2 4.9 2 6V10C3.1 10 4 10.9 4 12C4 13.1 3.1 14 2 14V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V14C20.9 14 20 13.1 20 12C20 10.9 20.9 10 22 10ZM13 17.5H11V15.5H13V17.5ZM13 13.5H11V8.5H13V13.5Z" fill="#7C6A46"/>
            </svg>
            <h2>My Support Tickets</h2>
          </div>
          <span class="section-badge">{{ dashboard.supportTickets.length }}</span>
        </div>
        <div v-if="loadingStates.tickets" class="loading-section">
          <div class="loading-spinner-small"></div>
          <p>Loading tickets...</p>
        </div>
        <div v-else-if="errorStates.tickets" class="error-section">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="currentColor"/>
          </svg>
          <p>{{ errorStates.tickets }}</p>
        </div>
        <div v-else-if="dashboard.supportTickets.length === 0" class="section-empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 10V6C22 4.9 21.1 4 20 4H4C2.9 4 2 4.9 2 6V10C3.1 10 4 10.9 4 12C4 13.1 3.1 14 2 14V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V14C20.9 14 20 13.1 20 12C20 10.9 20.9 10 22 10ZM13 17.5H11V15.5H13V17.5ZM13 13.5H11V8.5H13V13.5Z" fill="currentColor" opacity="0.3"/>
          </svg>
          <p>No support tickets created yet</p>
        </div>
        <div v-else class="tickets-grid">
          <div
            v-for="ticket in dashboard.supportTickets"
            :key="ticket.id"
            class="ticket-card"
            @click="viewTicketDetail(ticket.id)"
          >
            <div class="ticket-card-header">
              <div class="ticket-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M22 10V6C22 4.9 21.1 4 20 4H4C2.9 4 2 4.9 2 6V10C3.1 10 4 10.9 4 12C4 13.1 3.1 14 2 14V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V14C20.9 14 20 13.1 20 12C20 10.9 20.9 10 22 10Z" fill="#7C6A46"/>
                </svg>
              </div>
              <span :class="`ticket-status status-${ticket.status.toLowerCase()}`">
                {{ ticket.status }}
              </span>
            </div>
            <div class="ticket-card-body">
              <h3>{{ ticket.subject }}</h3>
              <div class="ticket-meta">
                <div class="ticket-info-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V11H13V17ZM13 9H11V7H13V9Z" fill="currentColor"/>
                  </svg>
                  <span>{{ ticket.serviceSource }}</span>
                </div>
                <div class="ticket-info-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3.01 3.9 3.01 5L3 19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="currentColor"/>
                  </svg>
                  <span>{{ formatDate(ticket.createdAt) }}</span>
                </div>
              </div>
              <div v-if="ticket.unreadMessagesCount > 0" class="unread-indicator">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 4H4C2.9 4 2.01 4.9 2.01 6L2 18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4ZM20 8L12 13L4 8V6L12 11L20 6V8Z" fill="currentColor"/>
                </svg>
                <span>{{ ticket.unreadMessagesCount }} new message{{ ticket.unreadMessagesCount > 1 ? 's' : '' }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- Create Ticket Modal -->
    <CreateTicketModal
      v-if="showCreateModal"
      :initial-service-source="selectedServiceSource"
      :initial-booking-id="selectedBookingId"
      @close="closeCreateTicketModal"
      @ticket-created="handleTicketCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BookingCard from '@/components/BookingCard.vue'
import CreateTicketModal from '@/components/support/CreateTicketModal.vue'
import supportTicketService, { type DashboardResponse } from '@/services/supportTicketService'

const router = useRouter()

// Hardcoded userId for testing (replace with actual auth later)
const userId = ref('263d012e-b86a-4813-be96-41e6da78e00d') // John Doe from seeder

// State
const loading = ref(false)
const error = ref('')
const dashboard = ref<DashboardResponse>({
  accommodationBookings: [],
  flightBookings: [],
  rentalBookings: [],
  tourBookings: [],
  insuranceBookings: [],
  supportTickets: [],
})

// Loading states per service
const loadingStates = ref({
  accommodation: false,
  flight: false,
  rental: false,
  tour: false,
  insurance: false,
  tickets: false,
})

// Error states per service
const errorStates = ref({
  accommodation: '',
  flight: '',
  rental: '',
  tour: '',
  insurance: '',
  tickets: '',
})

// Modal state
const showCreateModal = ref(false)
const selectedServiceSource = ref('')
const selectedBookingId = ref('')

// Load dashboard data with parallel fetching
const loadDashboard = async () => {
  loading.value = true
  error.value = ''

  // Reset error states
  Object.keys(errorStates.value).forEach(key => {
    errorStates.value[key as keyof typeof errorStates.value] = ''
  })

  try {
    // Fetch all services in parallel with individual error handling
    const fetchPromises = [
      // Accommodation
      (async () => {
        loadingStates.value.accommodation = true
        try {
          const response = await supportTicketService.getAvailableBookings('ACCOMMODATION', userId.value)
          dashboard.value.accommodationBookings = response.data
        } catch (err: any) {
          console.error('Failed to load accommodation bookings:', err)
          errorStates.value.accommodation = 'Failed to load'
          dashboard.value.accommodationBookings = []
        } finally {
          loadingStates.value.accommodation = false
        }
      })(),
      
      // Flight
      (async () => {
        loadingStates.value.flight = true
        try {
          const response = await supportTicketService.getAvailableBookings('FLIGHT', userId.value)
          dashboard.value.flightBookings = response.data
        } catch (err: any) {
          console.error('Failed to load flight bookings:', err)
          errorStates.value.flight = 'Failed to load'
          dashboard.value.flightBookings = []
        } finally {
          loadingStates.value.flight = false
        }
      })(),
      
      // Rental
      (async () => {
        loadingStates.value.rental = true
        try {
          const response = await supportTicketService.getAvailableBookings('RENTAL', userId.value)
          dashboard.value.rentalBookings = response.data
        } catch (err: any) {
          console.error('Failed to load rental bookings:', err)
          errorStates.value.rental = 'Failed to load'
          dashboard.value.rentalBookings = []
        } finally {
          loadingStates.value.rental = false
        }
      })(),
      
      // Tour
      (async () => {
        loadingStates.value.tour = true
        try {
          const response = await supportTicketService.getAvailableBookings('TOUR', userId.value)
          dashboard.value.tourBookings = response.data
        } catch (err: any) {
          console.error('Failed to load tour bookings:', err)
          errorStates.value.tour = 'Failed to load'
          dashboard.value.tourBookings = []
        } finally {
          loadingStates.value.tour = false
        }
      })(),
      
      // Insurance
      (async () => {
        loadingStates.value.insurance = true
        try {
          const response = await supportTicketService.getAvailableBookings('INSURANCE', userId.value)
          dashboard.value.insuranceBookings = response.data
        } catch (err: any) {
          console.error('Failed to load insurance bookings:', err)
          errorStates.value.insurance = 'Failed to load'
          dashboard.value.insuranceBookings = []
        } finally {
          loadingStates.value.insurance = false
        }
      })(),
      
      // Support Tickets
      (async () => {
        loadingStates.value.tickets = true
        try {
          const response = await supportTicketService.getAllTickets(userId.value)
          dashboard.value.supportTickets = response.data
        } catch (err: any) {
          console.error('Failed to load support tickets:', err)
          errorStates.value.tickets = 'Failed to load'
          dashboard.value.supportTickets = []
        } finally {
          loadingStates.value.tickets = false
        }
      })(),
    ]

    // Wait for all promises to settle (not fail fast)
    await Promise.allSettled(fetchPromises)

  } catch (err: any) {
    console.error('Unexpected error loading dashboard:', err)
    error.value = 'An unexpected error occurred'
  } finally {
    loading.value = false
  }
}

// Open create ticket modal
const openCreateTicketModal = (booking: any, serviceType: string) => {
  selectedServiceSource.value = serviceType
  selectedBookingId.value = booking.id || booking.bookingId
  showCreateModal.value = true
}

// Close create ticket modal
const closeCreateTicketModal = () => {
  showCreateModal.value = false
  selectedServiceSource.value = ''
  selectedBookingId.value = ''
}

// Handle ticket created
const handleTicketCreated = () => {
  closeCreateTicketModal()
  loadDashboard() // Refresh dashboard
}

// View ticket detail
const viewTicketDetail = (ticketId: string) => {
  router.push(`/support/tickets/${ticketId}`)
}

// Format date
const formatDate = (dateStr: string) => {
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    })
  } catch {
    return dateStr
  }
}

// Load data on mount
onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.support-dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 60px;
  font-family: 'Poppins', sans-serif;
}

/* Page Header */
.page-header {
  margin-bottom: 40px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  width: 60px;
  height: 60px;
  background: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.15);
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

/* Actions */
.actions {
  display: flex;
  gap: 15px;
  margin-bottom: 35px;
}

.btn-secondary {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 28px;
  border: 2px solid #7C6A46;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: white;
  color: #7C6A46;
}

.btn-secondary:hover {
  background: #FAFAFA;
  transform: translateY(-2px);
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

/* Dashboard Content */
.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

/* Booking Sections */
.booking-section {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #F0F0F0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #F0F0F0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title h2 {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.section-badge {
  background: linear-gradient(135deg, #7C6A46 0%, #9B8A68 100%);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
}

/* Loading & Error per section */
.loading-section,
.error-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  text-align: center;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
}

.loading-section {
  color: #7C6A46;
}

.loading-spinner-small {
  width: 30px;
  height: 30px;
  border: 3px solid #FAFAFA;
  border-top: 3px solid #7C6A46;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.error-section {
  color: #C33;
}

.section-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  text-align: center;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
  border-radius: 10px;
}

.section-empty-state p {
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 500;
  margin: 15px 0 0 0;
}

.booking-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 25px;
}

/* Tickets Section */
.tickets-section {
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
}

.tickets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 25px;
}

.ticket-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid #F0F0F0;
  cursor: pointer;
}

.ticket-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(124, 106, 70, 0.2);
}

.ticket-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 20px 12px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
}

.ticket-icon {
  width: 48px;
  height: 48px;
  background: white;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(124, 106, 70, 0.15);
}

.ticket-status {
  padding: 6px 16px;
  border-radius: 20px;
  font-family: 'Poppins', sans-serif;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-open {
  background: #E3F2FD;
  color: #1976D2;
}

.status-in_progress {
  background: #FFF3E0;
  color: #F57C00;
}

.status-closed {
  background: #E8F5E9;
  color: #2E7D32;
}

.ticket-card-body {
  padding: 15px 20px 20px;
}

.ticket-card-body h3 {
  color: #1C1C1C;
  font-family: 'Raleway', sans-serif;
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.ticket-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 12px;
}

.ticket-info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 500;
}

.unread-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #FFEBEE;
  color: #C62828;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 600;
  margin-top: 10px;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .booking-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
  
  .tickets-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }
}

@media (max-width: 768px) {
  .support-dashboard {
    padding: 20px 20px;
  }

  .page-title {
    font-size: 32px;
  }

  .booking-grid,
  .tickets-grid {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
  }
}
</style>
