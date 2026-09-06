import { apiClient } from './client'

export const wishlistApi = {
  getWishlist() {
    return apiClient.get('/wishlist')
  },
  addWish({ poiId, isPublic }) {
    return apiClient.post('/wishlist', { poiId, isPublic })
  },
  updateWishVisibility(poiId, isPublic) {
    return apiClient.patch(`/wishlist/${poiId}`, { isPublic })
  },
  removeWish(poiId) {
    return apiClient.delete(`/wishlist/${poiId}`)
  },
}
