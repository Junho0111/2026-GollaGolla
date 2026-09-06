import { apiClient } from './client'

export const reviewsApi = {
  getReviews(poiId, { page = 0, size = 20 } = {}) {
    return apiClient.get(`/pois/${poiId}/reviews`, { params: { page, size } })
  },
  createReview(poiId, { rating, content }) {
    return apiClient.post(`/pois/${poiId}/reviews`, { rating, content })
  },
}
