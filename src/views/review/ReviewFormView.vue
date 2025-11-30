<template>
  <div class="review-form-page">
    <CreateReviewModal
      v-if="bookingId && customerId"
      :bookingId="bookingId"
      :customerId="customerId"
      @close="handleClose"
      @success="handleSuccess"
    />
    <div v-else class="loading">
      Loading...
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import CreateReviewModal from '@/components/review/CreateReviewModal.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const bookingId = ref<string>('')
const customerId = ref<string>('')

onMounted(() => {
  // Get bookingId from query params
  bookingId.value = route.query.bookingId as string

  // Get customerId from authenticated user
  if (authStore.user?.id) {
    customerId.value = authStore.user.id
  }

  // Validate required data
  if (!bookingId.value) {
    alert('Booking ID is required')
    router.push('/booking')
  }

  if (!customerId.value) {
    alert('Please login to write a review')
    router.push('/login')
  }
})

const handleClose = () => {
  router.back()
}

const handleSuccess = () => {
  alert('Review submitted successfully!')
  router.push('/reviews/my-reviews')
}
</script>

<style scoped>
.review-form-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading {
  text-align: center;
  font-size: 18px;
  color: #666;
}
</style>
