import axios from 'axios'
import { tokenStorage } from './tokenStorage'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

export const apiClient = axios.create({
  baseURL: BASE_URL,
})

// 로그인 화면으로 강제 이동시키기 위한 콜백 — App 최상단(AuthProvider)에서 등록
let onAuthFailure = () => {
  window.location.href = '/login'
}
export function registerAuthFailureHandler(handler) {
  onAuthFailure = handler
}

// 인증이 필요 없는 엔드포인트 (401 인터셉트/재시도 대상에서 제외)
const AUTH_FREE_PATHS = ['/auth/login', '/auth/signup', '/auth/oauth', '/auth/refresh']

function isAuthFreePath(url = '') {
  return AUTH_FREE_PATHS.some((p) => url.includes(p))
}

// accessToken 자동 첨부
apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken()
  if (token && !isAuthFreePath(config.url)) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 동시 다발 401에 대해 refresh를 한 번만 수행하기 위한 큐
let isRefreshing = false
let pendingQueue = []

function resolveQueue(error, accessToken) {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(accessToken)
  })
  pendingQueue = []
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response, config: originalRequest } = error

    if (!response) {
      return Promise.reject(error)
    }

    const status = response.status
    const code = response.data?.code

    const isAuthError = status === 401 && (code === 'AUTH_007' || code === 'AUTH_009')

    if (!isAuthError || isAuthFreePath(originalRequest.url) || originalRequest._retry) {
      return Promise.reject(error)
    }

    if (isRefreshing) {
      // 이미 갱신 중이면 큐에 대기했다가 새 토큰으로 재시도
      return new Promise((resolve, reject) => {
        pendingQueue.push({ resolve, reject })
      })
        .then((newAccessToken) => {
          originalRequest._retry = true
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return apiClient(originalRequest)
        })
        .catch((err) => Promise.reject(err))
    }

    originalRequest._retry = true
    isRefreshing = true

    try {
      const refreshToken = tokenStorage.getRefreshToken()
      if (!refreshToken) {
        throw new Error('NO_REFRESH_TOKEN')
      }

      // Refresh Token Rotation — 재발급 응답의 새 accessToken/refreshToken으로 갱신
      const { data } = await axios.post(`${BASE_URL}/auth/refresh`, { refreshToken })
      tokenStorage.setTokens({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
      })

      resolveQueue(null, data.accessToken)

      originalRequest.headers.Authorization = `Bearer ${data.accessToken}`
      return apiClient(originalRequest)
    } catch (refreshError) {
      resolveQueue(refreshError, null)
      tokenStorage.clear()
      onAuthFailure()
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  }
)
