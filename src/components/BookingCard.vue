<template>
  <div class="booking-card">
    <div class="booking-header">
      <div class="booking-icon" :class="`icon-${serviceType}`">
        <i :class="getServiceIcon()"></i>
      </div>
      <div class="booking-info">
        <h3>{{ getBookingTitle() }}</h3>
        <p class="booking-id">ID: {{ getBookingId() }}</p>
      </div>
    </div>

    <div class="booking-details">
      <!-- Accommodation specific details -->
      <div v-if="serviceType === 'ACCOMMODATION'" class="detail-grid">
        <div class="detail-item">
          <span class="label">Room:</span>
          <span class="value">{{ booking.roomName || 'N/A' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Check-in:</span>
          <span class="value">{{ formatDate(booking.checkInDate) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Check-out:</span>
          <span class="value">{{ formatDate(booking.checkOutDate) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Guests:</span>
          <span class="value">{{ booking.numGuests || 0 }}</span>
        </div>
      </div>

      <!-- Flight specific details -->
      <div v-else-if="serviceType === 'FLIGHT'" class="detail-grid">
        <div class="detail-item">
          <span class="label">Flight:</span>
          <span class="value">{{ booking.flightNumber || 'N/A' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">From:</span>
          <span class="value">{{ booking.departureCity || 'N/A' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">To:</span>
          <span class="value">{{ booking.arrivalCity || 'N/A' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Date:</span>
          <span class="value">{{ formatDate(booking.departureDate) }}</span>
        </div>
      </div>

      <!-- Rental specific details -->
      <div v-else-if="serviceType === 'RENTAL'" class="detail-grid">
        <div class="detail-item">
          <span class="label">Vehicle:</span>
          <span class="value">{{ booking.vehicleName || 'N/A' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Pick-up:</span>
          <span class="value">{{ formatDate(booking.pickupDate) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Return:</span>
          <span class="value">{{ formatDate(booking.returnDate) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Location:</span>
          <span class="value">{{ booking.pickupLocation || 'N/A' }}</span>
        </div>
      </div>

      <!-- Tour specific details -->
      <div v-else-if="serviceType === 'TOUR'" class="detail-grid">
        <div class="detail-item">
          <span class="label">Package:</span>
          <span class="value">{{ booking.packageName || 'N/A' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Date:</span>
          <span class="value">{{ formatDate(booking.tourDate) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Duration:</span>
          <span class="value">{{ booking.duration || 'N/A' }} days</span>
        </div>
        <div class="detail-item">
          <span class="label">Participants:</span>
          <span class="value">{{ booking.participants || 0 }}</span>
        </div>
      </div>

      <!-- Insurance specific details -->
      <div v-else-if="serviceType === 'INSURANCE'" class="detail-grid">
        <div class="detail-item">
          <span class="label">Policy:</span>
          <span class="value">{{ booking.policyName || 'N/A' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Coverage:</span>
          <span class="value">{{ booking.coverageType || 'N/A' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">Start:</span>
          <span class="value">{{ formatDate(booking.startDate) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">End:</span>
          <span class="value">{{ formatDate(booking.endDate) }}</span>
        </div>
      </div>
    </div>

    <div class="booking-footer">
      <button class="btn-create-ticket" @click="handleCreateTicket">
        <i class="fas fa-ticket-alt"></i>
        Create Support Ticket
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'

interface Props {
  booking: any
  serviceType: 'ACCOMMODATION' | 'FLIGHT' | 'RENTAL' | 'TOUR' | 'INSURANCE'
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'create-ticket', booking: any, serviceType: string): void
}>()

const getServiceIcon = () => {
  const icons: Record<string, string> = {
    ACCOMMODATION: 'fas fa-hotel',
    FLIGHT: 'fas fa-plane',
    RENTAL: 'fas fa-car',
    TOUR: 'fas fa-suitcase-rolling',
    INSURANCE: 'fas fa-shield-alt',
  }
  return icons[props.serviceType] || 'fas fa-question'
}

const getBookingTitle = () => {
  const titles: Record<string, string> = {
    ACCOMMODATION: props.booking.roomName || 'Accommodation Booking',
    FLIGHT: `${props.booking.departureCity} → ${props.booking.arrivalCity}`,
    RENTAL: props.booking.vehicleName || 'Vehicle Rental',
    TOUR: props.booking.packageName || 'Tour Package',
    INSURANCE: props.booking.policyName || 'Insurance Policy',
  }
  return titles[props.serviceType] || 'Booking'
}

const getBookingId = () => {
  return props.booking.id || props.booking.bookingId || 'Unknown'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return 'N/A'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    })
  } catch {
    return dateStr
  }
}

const handleCreateTicket = () => {
  emit('create-ticket', props.booking, props.serviceType)
}
</script>

<style scoped>
.booking-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.booking-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.booking-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.booking-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.booking-info h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.booking-id {
  margin: 4px 0 0 0;
  font-size: 12px;
  opacity: 0.9;
}

.booking-details {
  padding: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item .label {
  font-size: 11px;
  text-transform: uppercase;
  color: #6b7280;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.detail-item .value {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.booking-footer {
  padding: 12px 16px;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

.btn-create-ticket {
  width: 100%;
  padding: 10px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: opacity 0.2s;
}

.btn-create-ticket:hover {
  opacity: 0.9;
}

.btn-create-ticket i {
  font-size: 14px;
}
</style>
