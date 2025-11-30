<template>
  <div class="reviews-list-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 17.27L18.18 21L16.54 13.97L22 9.24L14.81 8.63L12 2L9.19 8.63L2 9.24L7.46 13.97L5.82 21L12 17.27Z" fill="#7C6A46"/>
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">{{ pageTitle }}</h1>
          <p class="page-subtitle">{{ pageSubtitle }}</p>
        </div>
      </div>
    </div>

    <!-- Actions and Filters -->
    <div class="actions-filters-container">
      <div class="actions">
        <button @click="loadReviews" class="btn-secondary">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4C7.58 4 4.01 7.58 4.01 12C4.01 16.42 7.58 20 12 20C15.73 20 18.84 17.45 19.73 14H17.65C16.83 16.33 14.61 18 12 18C8.69 18 6 15.31 6 12C6 8.69 8.69 6 12 6C13.66 6 15.14 6.69 16.22 7.78L13 11H20V4L17.65 6.35Z" fill="currentColor"/>
          </svg>
          Refresh Reviews
        </button>
      </div>
      
      <!-- Property Filter (only show for 'all' view) -->
      <div v-if="isAllView" class="filters-section">
        <div class="filter-group">
          <label for="filter-property">Filter by Property</label>
          <select 
            id="filter-property"
            v-model="selectedPropertyId" 
            class="filter-select"
            @change="loadReviews"
          >
            <option value="">All Properties</option>
            <option v-for="property in availableProperties" :key="property.propertyId" :value="property.propertyId">
              {{ property.propertyName }}
            </option>
          </select>
        </div>
        <button v-if="selectedPropertyId" @click="clearPropertyFilter" class="btn-clear-filter">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 6.41L17.59 5L12 10.59L6.41 5L5 6.41L10.59 12L5 17.59L6.41 19L12 13.41L17.59 19L19 17.59L13.41 12L19 6.41Z" fill="currentColor"/>
          </svg>
          Clear
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>Loading reviews...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-message">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="currentColor"/>
      </svg>
      <span>{{ error }}</span>
    </div>

    <!-- Reviews List -->
    <div v-else>
      <!-- Empty State -->
      <div v-if="reviews.length === 0" class="empty-state">
        <svg width="120" height="120" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 17.27L18.18 21L16.54 13.97L22 9.24L14.81 8.63L12 2L9.19 8.63L2 9.24L7.46 13.97L5.82 21L12 17.27Z" fill="currentColor" opacity="0.3"/>
        </svg>
        <h3>No Reviews Yet</h3>
        <p>Be the first to write a review!</p>
      </div>

      <!-- Reviews Grid -->
      <div v-else class="reviews-grid">
        <div v-for="review in reviews" :key="review.reviewId" class="review-card">
          <div class="card-header">
            <div class="property-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M7 13C8.66 13 10 11.66 10 10C10 8.34 8.66 7 7 7C5.34 7 4 8.34 4 10C4 11.66 5.34 13 7 13ZM19 7H11V9H19V7ZM19 11H11V13H19V11ZM16 20H8V16H16V20ZM2 20H6V16H2V20ZM18 20H22V16H18V20Z" fill="#7C6A46"/>
              </svg>
            </div>
            <div class="rating-badge">
              <span class="rating-number">{{ review.overallRating.toFixed(1) }}</span>
              <div class="rating-stars">
                <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= review.overallRating }">★</span>
              </div>
            </div>
          </div>

          <div class="card-body">
            <h3 class="property-name">{{ review.propertyName || 'Property' }}</h3>
            
            <div class="review-meta">
              <div class="meta-item">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z" fill="currentColor"/>
                </svg>
                <span>{{ review.customerName }}</span>
              </div>
              <div class="meta-item">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3.01 3.9 3.01 5L3 19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="currentColor"/>
                </svg>
                <span>{{ formatDate(review.createdDate) }}</span>
              </div>
            </div>

            <div class="rating-breakdown">
              <div class="rating-row">
                <span class="label">Cleanliness</span>
                <div class="mini-stars">
                  <span v-for="n in 5" :key="`clean-${n}`" :class="{ filled: n <= review.cleanlinessRating }">★</span>
                </div>
              </div>
              <div class="rating-row">
                <span class="label">Facilities</span>
                <div class="mini-stars">
                  <span v-for="n in 5" :key="`fac-${n}`" :class="{ filled: n <= review.facilityRating }">★</span>
                </div>
              </div>
              <div class="rating-row">
                <span class="label">Service</span>
                <div class="mini-stars">
                  <span v-for="n in 5" :key="`serv-${n}`" :class="{ filled: n <= review.serviceRating }">★</span>
                </div>
              </div>
              <div class="rating-row">
                <span class="label">Value</span>
                <div class="mini-stars">
                  <span v-for="n in 5" :key="`val-${n}`" :class="{ filled: n <= review.valueRating }">★</span>
                </div>
              </div>
            </div>

            <div v-if="review.comment" class="review-comment">
              <p>{{ review.comment }}</p>
            </div>
          </div>

          <div v-if="isCustomerView" class="card-actions">
            <button @click="viewDetails(review.reviewId)" class="btn-action btn-detail">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 4.5C7 4.5 2.73 7.61 1 12C2.73 16.39 7 19.5 12 19.5C17 19.5 21.27 16.39 23 12C21.27 7.61 17 4.5 12 4.5ZM12 17C9.24 17 7 14.76 7 12C7 9.24 9.24 7 12 7C14.76 7 17 9.24 17 12C17 14.76 14.76 17 12 17ZM12 9C10.34 9 9 10.34 9 12C9 13.66 10.34 15 12 15C13.66 15 15 13.66 15 12C15 10.34 13.66 9 12 9Z" fill="currentColor"/>
              </svg>
              View Details
            </button>
            <button @click="deleteReview(review.reviewId)" class="btn-action btn-delete">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M6 19C6 20.1 6.9 21 8 21H16C17.1 21 18 20.1 18 19V7H6V19ZM19 4H15.5L14.5 3H9.5L8.5 4H5V6H19V4Z" fill="currentColor"/>
              </svg>
              Delete
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import reviewService, { type ReviewResponse } from '@/services/reviewService'
import { propertyService, type Property } from '@/services/propertyService'

const router = useRouter()
const route = useRoute()

// Props: view mode ('all', 'property', or 'customer')
const viewMode = computed(() => {
  if (route.name === 'all-reviews') return 'all'
  if (route.name === 'my-reviews') return 'customer'
  return 'property'
})
const propertyId = computed(() => route.params.propertyId as string)
const customerId = ref('263d012e-b86a-4813-be96-41e6da78e00d') // John Doe from seeder

const isCustomerView = computed(() => viewMode.value === 'customer')
const isAllView = computed(() => viewMode.value === 'all')

const pageTitle = computed(() => {
  if (isAllView.value) return 'All Reviews'
  if (isCustomerView.value) return 'My Reviews'
  return 'Property Reviews'
})

const pageSubtitle = computed(() => {
  if (isAllView.value) return 'Reviews from all properties and bookings'
  if (isCustomerView.value) return 'Reviews you have written'
  return 'All reviews for this property'
})

const loading = ref(false)
const error = ref('')
const reviews = ref<ReviewResponse[]>([])
const selectedPropertyId = ref('')
const availableProperties = ref<Property[]>([])
const allReviews = ref<ReviewResponse[]>([]) // Store all reviews for filtering

const loadProperties = async () => {
  try {
    const response = await propertyService.getAll()
    if (response.success) {
      availableProperties.value = response.data
    }
  } catch (err) {
    console.error('Failed to load properties:', err)
  }
}

const loadReviews = async () => {
  loading.value = true
  error.value = ''

  try {
    if (isAllView.value) {
      const response = await reviewService.getAllReviews()
      allReviews.value = response.data
      
      // Filter by property if selected
      if (selectedPropertyId.value) {
        reviews.value = allReviews.value.filter(r => r.propertyId === selectedPropertyId.value)
      } else {
        reviews.value = allReviews.value
      }
    } else if (isCustomerView.value) {
      const response = await reviewService.getMyReviews()
      reviews.value = response.data
    } else {
      const response = await reviewService.getReviewsByProperty(propertyId.value)
      reviews.value = response.data
    }
  } catch (err: any) {
    console.error('Failed to load reviews:', err)
    error.value = err.response?.data?.error || 'Failed to load reviews'
  } finally {
    loading.value = false
  }
}

const clearPropertyFilter = () => {
  selectedPropertyId.value = ''
  loadReviews()
}

const formatDate = (dateStr: string) => {
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
  } catch {
    return dateStr
  }
}

const viewDetails = (reviewId: string) => {
  router.push(`/reviews/${reviewId}`)
}

const deleteReview = async (reviewId: string) => {
  if (!confirm('Are you sure you want to delete this review?')) return

  try {
    await reviewService.deleteReview(reviewId, customerId.value)
    loadReviews() // Refresh list
  } catch (err: any) {
    console.error('Failed to delete review:', err)
    alert(err.response?.data?.error || 'Failed to delete review')
  }
}

onMounted(() => {
  if (isAllView.value) {
    loadProperties() // Load properties for filter dropdown
  }
  loadReviews()
})
</script>

<style scoped>
.reviews-list-page {
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
.actions-filters-container {
  margin-bottom: 35px;
}

.actions {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
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

/* Filters Section */
.filters-section {
  display: flex;
  gap: 15px;
  align-items: flex-end;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #F0F0F0;
}

.filter-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  color: #4A4A4A;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
}

.filter-select {
  padding: 12px 16px;
  border: 2px solid #E0E0E0;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  background: white;
  color: #1C1C1C;
  cursor: pointer;
}

.filter-select:focus {
  outline: none;
  border-color: #7C6A46;
  box-shadow: 0 0 0 3px rgba(124, 106, 70, 0.1);
}

.btn-clear-filter {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 20px;
  border: 2px solid #E0E0E0;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  background: white;
  color: #666;
}

.btn-clear-filter:hover {
  border-color: #F44336;
  color: #F44336;
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
  margin: 0;
}

/* Reviews Grid */
.reviews-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 30px;
}

.review-card {
  background: white;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid #F0F0F0;
}

.review-card:hover {
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

.rating-badge {
  text-align: right;
}

.rating-number {
  color: #7C6A46;
  font-family: 'Raleway', sans-serif;
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
  display: block;
}

.rating-stars {
  margin-top: 4px;
}

.rating-stars .star {
  color: #D1D5DB;
  font-size: 16px;
}

.rating-stars .star.filled {
  color: #FBC02D;
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

.review-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 500;
}

.rating-breakdown {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 15px;
  background: #FAFAFA;
  border-radius: 10px;
  margin-bottom: 15px;
}

.rating-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rating-row .label {
  color: #666;
  font-family: 'Poppins', sans-serif;
  font-size: 13px;
  font-weight: 500;
}

.mini-stars {
  display: flex;
  gap: 2px;
}

.mini-stars span {
  color: #D1D5DB;
  font-size: 14px;
}

.mini-stars span.filled {
  color: #FBC02D;
}

.review-comment {
  padding: 15px;
  background: linear-gradient(135deg, #FAFAFA 0%, #F0EDE6 100%);
  border-radius: 10px;
  margin-bottom: 15px;
}

.review-comment p {
  color: #4A4A4A;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.6;
  margin: 0;
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

.btn-delete {
  background: linear-gradient(135deg, #F44336 0%, #D32F2F 100%);
}

.btn-delete:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.4);
}

/* Responsive Design */
@media (max-width: 1200px) {
  .reviews-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }
}

@media (max-width: 768px) {
  .reviews-list-page {
    padding: 20px 20px;
  }

  .page-title {
    font-size: 32px;
  }

  .reviews-grid {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
  }

  .rating-breakdown {
    grid-template-columns: 1fr;
  }
}
</style>
