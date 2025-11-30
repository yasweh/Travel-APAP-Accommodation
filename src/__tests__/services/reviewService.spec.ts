import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock axios
vi.mock('axios', () => {
  const mockAxiosInstance = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    }
  }
  return {
    default: {
      create: vi.fn(() => mockAxiosInstance)
    }
  }
})

import { reviewService } from '@/services/reviewService'
import api from '@/services/api'

describe('Review Service', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('getAllReviews', () => {
    it('should fetch all reviews', async () => {
      const mockReviews = [
        { reviewId: 'r1', comment: 'Great!' },
        { reviewId: 'r2', comment: 'Good' }
      ]
      vi.mocked(api.get).mockResolvedValue({ data: mockReviews })

      const result = await reviewService.getAllReviews()

      expect(api.get).toHaveBeenCalledWith('/reviews')
      expect(result.data).toEqual(mockReviews)
    })
  })

  describe('getReviewsByProperty', () => {
    it('should fetch reviews for a property', async () => {
      const mockReviews = [{ reviewId: 'r1' }]
      vi.mocked(api.get).mockResolvedValue({ data: mockReviews })

      const result = await reviewService.getReviewsByProperty('p1')

      expect(api.get).toHaveBeenCalledWith('/reviews/property/p1')
    })
  })

  describe('getMyReviews', () => {
    it('should fetch current user reviews', async () => {
      const mockReviews = [{ reviewId: 'r1' }]
      vi.mocked(api.get).mockResolvedValue({ data: mockReviews })

      const result = await reviewService.getMyReviews()

      expect(api.get).toHaveBeenCalledWith('/reviews/my-reviews')
    })
  })

  describe('getReviewsByCustomer', () => {
    it('should fetch reviews by customer ID', async () => {
      const mockReviews = [{ reviewId: 'r1' }]
      vi.mocked(api.get).mockResolvedValue({ data: mockReviews })

      const result = await reviewService.getReviewsByCustomer('c1')

      expect(api.get).toHaveBeenCalledWith('/reviews/customer', { params: { customerId: 'c1' } })
    })
  })

  describe('getReviewDetail', () => {
    it('should fetch review detail', async () => {
      const mockReview = { reviewId: 'r1', comment: 'Great!' }
      vi.mocked(api.get).mockResolvedValue({ data: mockReview })

      const result = await reviewService.getReviewDetail('r1')

      expect(api.get).toHaveBeenCalledWith('/reviews/r1')
    })
  })

  describe('createReview', () => {
    it('should create a review', async () => {
      const reviewRequest = {
        bookingId: 'b1',
        cleanlinessRating: 5,
        facilityRating: 4,
        serviceRating: 5,
        valueRating: 4,
        comment: 'Great stay!'
      }
      vi.mocked(api.post).mockResolvedValue({
        data: { reviewId: 'r1', ...reviewRequest }
      })

      const result = await reviewService.createReview(reviewRequest, 'c1')

      expect(api.post).toHaveBeenCalledWith('/reviews', reviewRequest, { params: { customerId: 'c1' } })
    })
  })

  describe('deleteReview', () => {
    it('should delete a review', async () => {
      vi.mocked(api.delete).mockResolvedValue({ data: { success: true } })

      const result = await reviewService.deleteReview('r1', 'c1')

      expect(api.delete).toHaveBeenCalledWith('/reviews/r1', { params: { customerId: 'c1' } })
    })
  })
})
