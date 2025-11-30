<template>
  <div class="create-review-modal" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Write a Review</h2>
        <button class="close-btn" @click="$emit('close')">&times;</button>
      </div>

      <div class="modal-body">
        <form @submit.prevent="submitReview">
          <!-- Cleanliness Rating -->
          <div class="rating-group">
            <label>Cleanliness *</label>
            <div class="stars">
              <button
                v-for="n in 5"
                :key="`clean-${n}`"
                type="button"
                class="star"
                :class="{ active: formData.cleanlinessRating >= n }"
                @click="formData.cleanlinessRating = n"
              >
                ★
              </button>
            </div>
          </div>

          <!-- Facility Rating -->
          <div class="rating-group">
            <label>Facilities *</label>
            <div class="stars">
              <button
                v-for="n in 5"
                :key="`facility-${n}`"
                type="button"
                class="star"
                :class="{ active: formData.facilityRating >= n }"
                @click="formData.facilityRating = n"
              >
                ★
              </button>
            </div>
          </div>

          <!-- Service Rating -->
          <div class="rating-group">
            <label>Service *</label>
            <div class="stars">
              <button
                v-for="n in 5"
                :key="`service-${n}`"
                type="button"
                class="star"
                :class="{ active: formData.serviceRating >= n }"
                @click="formData.serviceRating = n"
              >
                ★
              </button>
            </div>
          </div>

          <!-- Value Rating -->
          <div class="rating-group">
            <label>Value for Money *</label>
            <div class="stars">
              <button
                v-for="n in 5"
                :key="`value-${n}`"
                type="button"
                class="star"
                :class="{ active: formData.valueRating >= n }"
                @click="formData.valueRating = n"
              >
                ★
              </button>
            </div>
          </div>

          <!-- Comment -->
          <div class="form-group">
            <label>Your Review</label>
            <textarea
              v-model="formData.comment"
              rows="5"
              placeholder="Share your experience with this accommodation..."
              class="form-control"
            ></textarea>
          </div>

          <!-- Error Message -->
          <div v-if="error" class="error-message">
            {{ error }}
          </div>

          <!-- Actions -->
          <div class="modal-actions">
            <button type="button" class="btn-secondary" @click="$emit('close')">Cancel</button>
            <button
              type="submit"
              class="btn-primary"
              :disabled="!isFormValid || submitting"
            >
              {{ submitting ? 'Submitting...' : 'Submit Review' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import reviewService, { type CreateReviewRequest } from '@/services/reviewService'

interface Props {
  bookingId: string
  customerId: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const formData = ref<CreateReviewRequest>({
  bookingId: props.bookingId,
  cleanlinessRating: 0,
  facilityRating: 0,
  serviceRating: 0,
  valueRating: 0,
  comment: '',
})

const submitting = ref(false)
const error = ref('')

const isFormValid = computed(() => {
  return (
    formData.value.cleanlinessRating > 0 &&
    formData.value.facilityRating > 0 &&
    formData.value.serviceRating > 0 &&
    formData.value.valueRating > 0
  )
})

const submitReview = async () => {
  if (!isFormValid.value) {
    error.value = 'Please rate all categories'
    return
  }

  submitting.value = true
  error.value = ''

  try {
    await reviewService.createReview(formData.value, props.customerId)
    emit('success')
    emit('close')
  } catch (err: any) {
    console.error('Failed to create review:', err)
    error.value = err.response?.data?.error || 'Failed to submit review'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-review-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 32px;
  color: #9ca3af;
  cursor: pointer;
  line-height: 1;
  padding: 0;
}

.close-btn:hover {
  color: #4b5563;
}

.modal-body {
  padding: 24px;
}

.rating-group {
  margin-bottom: 24px;
}

.rating-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 8px;
  color: #374151;
}

.stars {
  display: flex;
  gap: 8px;
}

.star {
  font-size: 32px;
  color: #d1d5db;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  padding: 0;
}

.star:hover,
.star.active {
  color: #fbbf24;
  transform: scale(1.1);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 8px;
  color: #374151;
}

.form-control {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
}

.form-control:focus {
  outline: none;
  border-color: #667eea;
}

.error-message {
  background: #fee2e2;
  color: #dc2626;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.btn-secondary,
.btn-primary {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: opacity 0.2s;
}

.btn-secondary {
  background: #e5e7eb;
  color: #374151;
}

.btn-secondary:hover {
  background: #d1d5db;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
