<template>
  <div class="review-detail-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-icon">
        <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
          <path d="M20 2L25.39 13.26L38 15.24L29 24.01L31.18 36.52L20 30.77L8.82 36.52L11 24.01L2 15.24L14.61 13.26L20 2Z" fill="white"/>
        </svg>
      </div>
      <div class="header-text">
        <h1 class="page-title">Review Details</h1>
        <p class="page-subtitle">View complete review information</p>
      </div>
    </div>

    <!-- Back Button -->
    <div class="nav-section">
      <button class="btn-back" @click="goBack">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M15.833 10H4.167" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M10 15.833L4.167 10L10 4.167" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>Back to Reviews</span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Loading review details...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="message-box error">
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM11 15H9V13H11V15ZM11 11H9V5H11V11Z" fill="#F44336"/>
      </svg>
      <span>{{ error }}</span>
      <button @click="loadReview" class="btn-retry">Retry</button>
    </div>

    <!-- Review Content -->
    <div v-else-if="review" class="review-content">
      <!-- Overview Card -->
      <div class="overview-card">
        <div class="property-section">
          <h2 class="property-name">{{ review.propertyName }}</h2>
          <div class="review-meta">
            <span class="meta-item">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 0C3.6 0 0 3.6 0 8C0 12.4 3.6 16 8 16C12.4 16 16 12.4 16 8C16 3.6 12.4 0 8 0ZM8 14.4C4.48 14.4 1.6 11.52 1.6 8C1.6 4.48 4.48 1.6 8 1.6C11.52 1.6 14.4 4.48 14.4 8C14.4 11.52 11.52 14.4 8 14.4Z" fill="#7C6A46"/>
                <path d="M8 4C6.9 4 6 4.9 6 6C6 7.1 6.9 8 8 8C9.1 8 10 7.1 10 6C10 4.9 9.1 4 8 4Z" fill="#7C6A46"/>
                <path d="M8 9C5.8 9 4 10.4 4 12H12C12 10.4 10.2 9 8 9Z" fill="#7C6A46"/>
              </svg>
              {{ review.customerName }}
            </span>
            <span class="meta-item">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M14 2H12.8V0H11.2V2H4.8V0H3.2V2H2C0.9 2 0 2.9 0 4V14C0 15.1 0.9 16 2 16H14C15.1 16 16 15.1 16 14V4C16 2.9 15.1 2 14 2ZM14 14H2V6H14V14Z" fill="#7C6A46"/>
              </svg>
              {{ formatDate(review.createdDate) }}
            </span>
          </div>
        </div>
        
        <div class="overall-rating-box">
          <div class="rating-score">{{ review.overallRating.toFixed(1) }}</div>
          <div class="stars-container">
            <svg v-for="n in 5" :key="n" width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path 
                d="M10 0L12.69 6.63L19.51 7.13L14.51 11.37L16.18 18.02L10 14.38L3.82 18.02L5.49 11.37L0.49 7.13L7.31 6.63L10 0Z" 
                :fill="n <= review.overallRating ? '#FFD700' : '#E8E8E8'"
              />
            </svg>
          </div>
          <span class="rating-label">Overall Rating</span>
        </div>
      </div>

      <!-- Detailed Ratings Card -->
      <div class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM9 17H7V10H9V17ZM13 17H11V7H13V17ZM17 17H15V13H17V17Z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">Detailed Ratings</h2>
        </div>

        <div class="ratings-grid">
          <!-- Cleanliness -->
          <div class="rating-item">
            <div class="rating-header">
              <div class="rating-icon cleanliness">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <path d="M16.99 5L15 3H9L7.01 5H2V20H22V5H16.99ZM12 18C9.24 18 7 15.76 7 13S9.24 8 12 8C14.76 8 17 10.24 17 13C17 15.76 14.76 18 12 18Z" fill="white"/>
                </svg>
              </div>
              <div class="rating-info">
                <span class="rating-label">Cleanliness</span>
                <span class="rating-number">{{ review.cleanlinessRating }}/5</span>
              </div>
            </div>
            <div class="rating-bar">
              <div 
                class="rating-fill cleanliness" 
                :style="{ width: (review.cleanlinessRating / 5 * 100) + '%' }"
              ></div>
            </div>
          </div>

          <!-- Facilities -->
          <div class="rating-item">
            <div class="rating-header">
              <div class="rating-icon facilities">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <path d="M1 9L12 2L23 9V22H1V9ZM12 4.5L3 10V20H21V10L12 4.5Z" fill="white"/>
                  <path d="M12 8C10.35 8 9 9.35 9 11C9 12.65 10.35 14 12 14C13.65 14 15 12.65 15 11C15 9.35 13.65 8 12 8Z" fill="white"/>
                </svg>
              </div>
              <div class="rating-info">
                <span class="rating-label">Facilities</span>
                <span class="rating-number">{{ review.facilityRating }}/5</span>
              </div>
            </div>
            <div class="rating-bar">
              <div 
                class="rating-fill facilities" 
                :style="{ width: (review.facilityRating / 5 * 100) + '%' }"
              ></div>
            </div>
          </div>

          <!-- Service -->
          <div class="rating-item">
            <div class="rating-header">
              <div class="rating-icon service">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <path d="M19 3H14.82C14.4 1.84 13.3 1 12 1C10.7 1 9.6 1.84 9.18 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM12 3C12.55 3 13 3.45 13 4C13 4.55 12.55 5 12 5C11.45 5 11 4.55 11 4C11 3.45 11.45 3 12 3Z" fill="white"/>
                </svg>
              </div>
              <div class="rating-info">
                <span class="rating-label">Service</span>
                <span class="rating-number">{{ review.serviceRating }}/5</span>
              </div>
            </div>
            <div class="rating-bar">
              <div 
                class="rating-fill service" 
                :style="{ width: (review.serviceRating / 5 * 100) + '%' }"
              ></div>
            </div>
          </div>

          <!-- Value for Money -->
          <div class="rating-item">
            <div class="rating-header">
              <div class="rating-icon value">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <path d="M11.8 10.9C9.53 10.31 8.8 9.7 8.8 8.75C8.8 7.66 9.81 6.9 11.5 6.9C13.28 6.9 13.94 7.75 14 9H16.21C16.14 7.28 15.09 5.7 13 5.19V3H10V5.16C8.06 5.58 6.5 6.84 6.5 8.77C6.5 11.08 8.41 12.23 11.2 12.9C13.7 13.5 14.2 14.38 14.2 15.31C14.2 16 13.71 17.1 11.5 17.1C9.44 17.1 8.63 16.18 8.52 15H6.32C6.44 17.19 8.08 18.42 10 18.83V21H13V18.85C14.95 18.48 16.5 17.35 16.5 15.3C16.5 12.46 14.07 11.49 11.8 10.9Z" fill="white"/>
                </svg>
              </div>
              <div class="rating-info">
                <span class="rating-label">Value for Money</span>
                <span class="rating-number">{{ review.valueRating }}/5</span>
              </div>
            </div>
            <div class="rating-bar">
              <div 
                class="rating-fill value" 
                :style="{ width: (review.valueRating / 5 * 100) + '%' }"
              ></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Review Comment Card -->
      <div class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M20 2H4C2.9 2 2 2.9 2 4V22L6 18H20C21.1 18 22 17.1 22 16V4C22 2.9 21.1 2 20 2ZM20 16H5.17L4 17.17V4H20V16Z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">Review Comment</h2>
        </div>

        <div class="comment-box">
          <p>{{ review.comment }}</p>
        </div>
      </div>

      <!-- Booking Information Card -->
      <div class="form-card">
        <div class="card-header">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="#7C6A46"/>
          </svg>
          <h2 class="card-title">Booking Information</h2>
        </div>

        <div class="booking-info-grid">
          <div class="info-item">
            <div class="info-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M5 5H15V15H5V5Z" fill="#7C6A46"/>
              </svg>
            </div>
            <div class="info-content">
              <span class="info-label">Booking ID</span>
              <span class="info-value">{{ review.bookingId }}</span>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM10 3C11.66 3 13 4.34 13 6C13 7.66 11.66 9 10 9C8.34 9 7 7.66 7 6C7 4.34 8.34 3 10 3ZM10 17.2C7.5 17.2 5.29 15.92 4 13.98C4.03 11.99 8 10.9 10 10.9C11.99 10.9 15.97 11.99 16 13.98C14.71 15.92 12.5 17.2 10 17.2Z" fill="#7C6A46"/>
              </svg>
            </div>
            <div class="info-content">
              <span class="info-label">Customer</span>
              <span class="info-value">{{ review.customerName }}</span>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M10 0L1 5V15L10 20L19 15V5L10 0ZM10 2.18L16.62 5.89L10 9.6L3.38 5.89L10 2.18Z" fill="#7C6A46"/>
              </svg>
            </div>
            <div class="info-content">
              <span class="info-label">Property</span>
              <span class="info-value">{{ review.propertyName }}</span>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M17 2H15V0H13V2H7V0H5V2H3C1.9 2 1 2.9 1 4V18C1 19.1 1.9 20 3 20H17C18.1 20 19 19.1 19 18V4C19 2.9 18.1 2 17 2ZM17 18H3V7H17V18Z" fill="#7C6A46"/>
              </svg>
            </div>
            <div class="info-content">
              <span class="info-label">Review Date</span>
              <span class="info-value">{{ formatDate(review.createdDate) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import reviewService, { type ReviewResponse } from '@/services/reviewService'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref<string | null>(null)
const review = ref<ReviewResponse | null>(null)

const loadReview = async () => {
  loading.value = true
  error.value = null

  try {
    const reviewId = route.params.reviewId as string
    const response = await reviewService.getReviewDetail(reviewId)
    review.value = response.data
  } catch (err: any) {
    console.error('Failed to load review:', err)
    error.value = err.response?.data?.message || 'Failed to load review details'
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadReview()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Raleway:wght@700;800&family=Poppins:wght@400;500;600&display=swap');

/* Container */
.review-detail-page {
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
.review-content {
  max-width: 1000px;
  margin: 0 auto;
}

/* Overview Card */
.overview-card {
  background: linear-gradient(135deg, #7C6A46 0%, #5A4A30 100%);
  border-radius: 20px;
  padding: 35px;
  margin-bottom: 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 30px;
  box-shadow: 0 8px 30px rgba(124, 106, 70, 0.3);
  color: white;
}

.property-section {
  flex: 1;
  min-width: 250px;
}

.property-name {
  font-family: 'Raleway', sans-serif;
  font-size: 2rem;
  font-weight: 800;
  margin: 0 0 15px 0;
}

.review-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  opacity: 0.9;
}

.meta-item svg path {
  fill: white;
}

.overall-rating-box {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 25px 35px;
  text-align: center;
}

.rating-score {
  font-family: 'Raleway', sans-serif;
  font-size: 4rem;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 10px;
}

.stars-container {
  display: flex;
  gap: 5px;
  justify-content: center;
  margin-bottom: 10px;
}

.rating-label {
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  opacity: 0.9;
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

/* Ratings Grid */
.ratings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.rating-item {
  background: #FAFAFA;
  padding: 20px;
  border-radius: 12px;
  border-left: 4px solid #7C6A46;
}

.rating-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.rating-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.rating-icon.cleanliness {
  background: linear-gradient(135deg, #4FC3F7, #29B6F6);
}

.rating-icon.facilities {
  background: linear-gradient(135deg, #81C784, #66BB6A);
}

.rating-icon.service {
  background: linear-gradient(135deg, #FFB74D, #FFA726);
}

.rating-icon.value {
  background: linear-gradient(135deg, #BA68C8, #AB47BC);
}

.rating-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rating-info .rating-label {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 600;
  color: #1C1C1C;
}

.rating-number {
  font-family: 'Raleway', sans-serif;
  font-size: 1.25rem;
  font-weight: 700;
  color: #7C6A46;
}

.rating-bar {
  height: 12px;
  background: #E8E8E8;
  border-radius: 6px;
  overflow: hidden;
}

.rating-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.8s ease-out;
  position: relative;
}

.rating-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.rating-fill.cleanliness {
  background: linear-gradient(90deg, #4FC3F7, #29B6F6);
}

.rating-fill.facilities {
  background: linear-gradient(90deg, #81C784, #66BB6A);
}

.rating-fill.service {
  background: linear-gradient(90deg, #FFB74D, #FFA726);
}

.rating-fill.value {
  background: linear-gradient(90deg, #BA68C8, #AB47BC);
}

/* Comment Box */
.comment-box {
  background: #FAFAFA;
  border-left: 4px solid #7C6A46;
  border-radius: 12px;
  padding: 25px;
}

.comment-box p {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  line-height: 1.8;
  color: #4A4A4A;
  margin: 0;
  white-space: pre-wrap;
}

/* Booking Info Grid */
.booking-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 15px;
  background: #FAFAFA;
  padding: 18px 20px;
  border-radius: 12px;
  border-left: 4px solid #7C6A46;
  transition: all 0.3s ease;
}

.info-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.info-icon {
  width: 45px;
  height: 45px;
  min-width: 45px;
  background: white;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-family: 'Poppins', sans-serif;
  font-size: 0.8rem;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 600;
  color: #1C1C1C;
}

/* Responsive */
@media (max-width: 768px) {
  .review-detail-page {
    padding: 40px 15px;
  }

  .page-title {
    font-size: 2rem;
  }

  .overview-card {
    flex-direction: column;
    text-align: center;
    padding: 25px;
  }

  .property-name {
    font-size: 1.5rem;
  }

  .review-meta {
    justify-content: center;
  }

  .rating-score {
    font-size: 3rem;
  }

  .ratings-grid {
    grid-template-columns: 1fr;
  }

  .booking-info-grid {
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

  .overall-rating-box {
    padding: 20px;
  }
}
</style>
