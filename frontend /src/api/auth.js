import { apiClient } from './client'

export const authApi = {
  signup({ email, password, nickname }) {
    return apiClient.post('/auth/signup', { email, password, nickname })
  },
  login({ email, password }) {
    return apiClient.post('/auth/login', { email, password })
  },
  oauthLogin({ provider, authorizationCode }) {
    return apiClient.post(`/auth/oauth/${provider}`, { authorizationCode })
  },
  logout() {
    return apiClient.delete('/auth/logout')
  },
  withdraw() {
    return apiClient.delete('/auth/withdraw')
  },
  getMe() {
    return apiClient.get('/members/me')
  },
}
