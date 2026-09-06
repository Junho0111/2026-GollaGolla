import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

// 인증이 필요한 화면(찜 목록, 일정, 마이페이지 등)을 감싸는 가드.
// 비로그인 상태면 로그인 화면으로 리다이렉트.
export default function RequireAuth({ children }) {
  const { isAuthenticated } = useAuth()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return children
}
