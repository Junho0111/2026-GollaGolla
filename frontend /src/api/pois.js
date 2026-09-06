import { apiClient } from './client'

export const CATEGORIES = [
  { value: 'HOTEL', label: '숙소' },
  { value: 'ATTRACTION', label: '관광지' },
  { value: 'ACTIVITY', label: '액티비티' },
  { value: 'RESTAURANT', label: '맛집' },
  { value: 'FESTIVAL', label: '축제' },
]

export const poisApi = {
  getPois({ regionId, category, page = 0, size = 20 } = {}) {
    return apiClient.get('/pois', { params: { regionId, category, page, size } })
  },
  getPoiDetail(poiId) {
    return apiClient.get(`/pois/${poiId}`)
  },
  searchPois(q) {
    return apiClient.get('/search', { params: { q, type: 'poi' } })
  },
}
