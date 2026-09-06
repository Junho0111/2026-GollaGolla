import { apiClient } from './client'

export const itinerariesApi = {
  getItineraries() {
    return apiClient.get('/itineraries')
  },
  getItineraryDetail(id) {
    return apiClient.get(`/itineraries/${id}`)
  },
  createItinerary({ title, regionId, startDate, endDate, transportMode = 'CAR' }) {
    return apiClient.post('/itineraries', { title, regionId, startDate, endDate, transportMode })
  },
  createAiItinerary({ title, regionId, startDate, endDate, transportMode = 'CAR', poiIds }) {
    return apiClient.post('/itineraries/ai', {
      title,
      regionId,
      startDate,
      endDate,
      transportMode,
      poiIds,
    })
  },
  deleteItinerary(id) {
    return apiClient.delete(`/itineraries/${id}`)
  },
  addItem(id, payload) {
    return apiClient.post(`/itineraries/${id}/items`, payload)
  },
  addItemsBulk(id, items) {
    return apiClient.post(`/itineraries/${id}/items/bulk`, { items })
  },
  updateItem(id, itemId, payload) {
    return apiClient.patch(`/itineraries/${id}/items/${itemId}`, payload)
  },
  updateItemMemo(id, itemId, memo) {
    return apiClient.patch(`/itineraries/${id}/items/${itemId}/memo`, { memo })
  },
  deleteItem(id, itemId) {
    return apiClient.delete(`/itineraries/${id}/items/${itemId}`)
  },
  createShareLink(id) {
    return apiClient.post(`/itineraries/${id}/share`)
  },
}

export const shareApi = {
  getSharedItinerary(token) {
    return apiClient.get(`/share/${token}`)
  },
}
