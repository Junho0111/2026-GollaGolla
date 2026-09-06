import { useEffect, useRef } from 'react'
import { useSearchParams, useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function OAuthCallbackPage() {
  const { provider } = useParams()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { oauthLogin } = useAuth()
  const hasFetched = useRef(false)

  useEffect(() => {
    const code = searchParams.get('code')
    
    // 코드 파라미터가 없거나 이미 요청을 보냈다면 중단 (Strict Mode 중복 요청 방지)
    if (!code || hasFetched.current) return
    hasFetched.current = true

    oauthLogin({ provider, authorizationCode: code })
      .then(() => {
        // 로그인 성공 시 메인 화면으로
        navigate('/', { replace: true })
      })
      .catch((err) => {
        console.error(err)
        alert('소셜 로그인 처리에 실패했습니다.')
        navigate('/login', { replace: true })
      })
  }, [provider, searchParams, oauthLogin, navigate])

  return (
    <div className="min-h-screen bg-harbor flex items-center justify-center">
      <div className="text-center">
        <h2 className="text-xl font-semibold text-sand mb-2">소셜 로그인 처리 중...</h2>
        <p className="text-sm text-mist">잠시만 기다려 주세요.</p>
      </div>
    </div>
  )
}
