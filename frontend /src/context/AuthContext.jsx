import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { tokenStorage } from '../api/tokenStorage'
import { registerAuthFailureHandler } from '../api/client'
import { authApi } from '../api/auth'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const navigate = useNavigate()
  // 앱 로드 시 저장된 accessToken 유무로 초기 로그인 상태 추정 (실제 유효성은 API 호출 시 401로 판별)
  const [isAuthenticated, setIsAuthenticated] = useState(!!tokenStorage.getAccessToken())
  const [member, setMember] = useState(null)

  useEffect(() => {
    // refresh마저 실패했을 때 client.js가 호출하는 콜백 — 로그인 화면으로 강제 이동
    registerAuthFailureHandler(() => {
      setIsAuthenticated(false)
      setMember(null)
      navigate('/login', { replace: true })
    })
  }, [navigate])

  const applyAuthTokens = useCallback((tokens) => {
    tokenStorage.setTokens(tokens)
    setIsAuthenticated(true)
  }, [])

  const signup = useCallback(
    async ({ email, password, nickname }) => {
      const { data } = await authApi.signup({ email, password, nickname })
      applyAuthTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken })
      return data
    },
    [applyAuthTokens]
  )

  const login = useCallback(
    async ({ email, password }) => {
      const { data } = await authApi.login({ email, password })
      applyAuthTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken })
      return data
    },
    [applyAuthTokens]
  )

  const oauthLogin = useCallback(
    async ({ provider, authorizationCode }) => {
      const { data } = await authApi.oauthLogin({ provider, authorizationCode })
      applyAuthTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken })
      return data
    },
    [applyAuthTokens]
  )

  const logout = useCallback(async () => {
    try {
      await authApi.logout()
    } finally {
      tokenStorage.clear()
      setIsAuthenticated(false)
      setMember(null)
      navigate('/login', { replace: true })
    }
  }, [navigate])

  const withdraw = useCallback(async () => {
    await authApi.withdraw()
    tokenStorage.clear()
    setIsAuthenticated(false)
    setMember(null)
    navigate('/login', { replace: true })
  }, [navigate])

  return (
    <AuthContext.Provider
      value={{ isAuthenticated, member, setMember, signup, login, oauthLogin, logout, withdraw }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
