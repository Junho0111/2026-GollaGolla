import { apiClient } from './client'

export const searchApi = {
  searchPois(q) {
    return apiClient.get('/search', { params: { q, type: 'poi' } })
  },
}
