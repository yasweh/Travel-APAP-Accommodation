import api from './api'

export interface CreateReviewRequest {
  bookingId: string
  cleanlinessRating: number
  facilityRating: number
  serviceRating: number
  valueRating: number
  comment?: string
}

export interface ReviewResponse {
  reviewId: string
  bookingId: string
  propertyId: string
  customerId: string
  overallRating: number
  cleanlinessRating: number
  facilityRating: number
  serviceRating: number
  valueRating: number
  comment: string
  createdDate: string
  propertyName?: string
  customerName?: string
}

export const reviewService = {
  /**
   * Get all reviews (all properties, all bookings)
   */
  getAllReviews() {
    return api.get<ReviewResponse[]>('/reviews')
  },

  /**
   * Get all reviews for a property
   */
  getReviewsByProperty(propertyId: string) {
    return api.get<ReviewResponse[]>(`/reviews/property/${propertyId}`)
  },

  /**
   * Get all reviews by authenticated customer (my reviews)
   */
  getMyReviews() {
    return api.get<ReviewResponse[]>('/reviews/my-reviews')
  },

  /**
   * Get all reviews by a customer (admin only)
   */
  getReviewsByCustomer(customerId: string) {
    return api.get<ReviewResponse[]>('/reviews/customer', {
      params: { customerId },
    })
  },

  /**
   * Get review detail
   */
  getReviewDetail(reviewId: string) {
    return api.get<ReviewResponse>(`/reviews/${reviewId}`)
  },

  /**
   * Create a new review
   */
  createReview(request: CreateReviewRequest, customerId: string) {
    return api.post<ReviewResponse>('/reviews', request, {
      params: { customerId },
    })
  },

  /**
   * Delete a review
   */
  deleteReview(reviewId: string, customerId: string) {
    return api.delete(`/reviews/${reviewId}`, {
      params: { customerId },
    })
  },
}

export default reviewService
