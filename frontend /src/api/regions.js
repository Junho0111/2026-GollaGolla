import { apiClient } from './client'

export const regionsApi = {
  // parentId 없이 호출하면 최상위(depth 1) 지역 목록
  getRegions({ parentId, depth } = {}) {
    return apiClient.get('/regions', { params: { parentId, depth } })
  },
}
