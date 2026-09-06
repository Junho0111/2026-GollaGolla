import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Logo from '../components/Logo'

const OAUTH_PROVIDERS = [
  { key: 'kakao', label: '카카오로 계속하기', bg: '#FEE500', fg: '#181600' },
  { key: 'naver', label: '네이버로 계속하기', bg: '#03C75A', fg: '#FFFFFF' },
  { key: 'google', label: 'Google로 계속하기', bg: '#FFFFFF', fg: '#0F2A3D', border: true },
]

export default function LoginPage() {
  const navigate = useNavigate()
  const { login, signup, oauthLogin } = useAuth()

  const [mode, setMode] = useState('login') // 'login' | 'signup'
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [nickname, setNickname] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('') // 전체 폼 에러 (자격증명 오류, 중복 등)
  const [fieldErrors, setFieldErrors] = useState({}) // COMMON_001 validation 필드별 메시지

  function resetErrors() {
    setFormError('')
    setFieldErrors({})
  }

  // COMMON_001의 "field: message, field2: message2" 형식을 파싱해 필드별로 매핑
  function parseValidationMessage(message = '') {
    const map = {}
    message.split(',').forEach((part) => {
      const [field, ...rest] = part.split(':')
      if (field && rest.length) {
        map[field.trim()] = rest.join(':').trim()
      }
    })
    return map
  }

  function handleApiError(err) {
    const status = err?.response?.status
    const code = err?.response?.data?.code
    const message = err?.response?.data?.message || '알 수 없는 오류가 발생했습니다.'

    if (code === 'COMMON_001') {
      setFieldErrors(parseValidationMessage(message))
      return
    }

    // AUTH_001(이메일 중복), AUTH_002(닉네임 중복), AUTH_003(자격증명 불일치),
    // AUTH_004(소셜 가입 이메일), AUTH_005(다른 방식 가입), AUTH_006/008(oauth) 등
    setFormError(message)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    resetErrors()
    setSubmitting(true)
    try {
      if (mode === 'login') {
        await login({ email, password })
      } else {
        await signup({ email, password, nickname })
      }
      navigate('/', { replace: true })
    } catch (err) {
      handleApiError(err)
    } finally {
      setSubmitting(false)
    }
  }

  async function handleOAuthClick(provider) {
    const REDIRECT_URI = `${window.location.origin}/oauth/callback/${provider}`;

    if (provider === 'kakao') {
      const KAKAO_CLIENT_ID = import.meta.env.VITE_KAKAO_CLIENT_ID;
      // prompt=login을 추가하여 항상 카카오 계정 로그인 창을 띄우도록 강제
      window.location.href = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code&prompt=login`;
    } else if (provider === 'naver') {
      const NAVER_CLIENT_ID = import.meta.env.VITE_NAVER_CLIENT_ID;
      const state = "gollagolla_state";
      // auth_type=reauthenticate를 추가하여 항상 네이버 로그인 창을 띄우도록 강제
      window.location.href = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${NAVER_CLIENT_ID}&state=${state}&redirect_uri=${REDIRECT_URI}&auth_type=reauthenticate`;
    } else if (provider === 'google') {
      const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID;
      // prompt=login을 추가하여 항상 구글 계정 비밀번호를 다시 입력하도록 강제
      window.location.href = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${GOOGLE_CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code&scope=email profile&prompt=login`;
    }
  }

  return (
    <div className="min-h-screen bg-harbor flex items-stretch">
      {/* 왼쪽: 탑승권(보딩패스) 모티프 패널 — 흔한 그라디언트 히어로 대신 여행 실물 오브젝트를 형태로 차용 */}
      <div className="hidden lg:flex w-[46%] relative overflow-hidden bg-harbor">
        <div className="absolute inset-0 opacity-[0.07]" style={{
          backgroundImage: 'radial-gradient(circle, #F6EFE4 1px, transparent 1px)',
          backgroundSize: '18px 18px',
        }} />
        <div className="relative z-10 flex flex-col justify-between p-14 w-full">
          <Logo className="h-36 lg:h-48 w-auto max-w-[80vw] mt-16 lg:mt-20" />

          <div className="border border-sand/25 bg-harbor relative">
            <div className="p-8 flex justify-between items-start">
              <div>
                <p className="font-mono text-[11px] text-seaglass tracking-[0.15em]">DEPARTURE</p>
                <p className="text-sand text-3xl font-semibold mt-1">어디든</p>
              </div>
              <div className="text-right">
                <p className="font-mono text-[11px] text-seaglass tracking-[0.15em]">ARRIVAL</p>
                <p className="text-sand text-3xl font-semibold mt-1">골라골라</p>
              </div>
            </div>
            {/* 절취선 (보딩패스 스텁 경계) */}
            <div className="relative h-0 border-t border-dashed border-sand/30">
              <span className="absolute -left-3 -top-3 w-6 h-6 rounded-full bg-harbor border border-sand/25" />
              <span className="absolute -right-3 -top-3 w-6 h-6 rounded-full bg-harbor border border-sand/25" />
            </div>
            <div className="p-8 flex justify-between items-center">
              <p className="font-mono text-xs text-mist">SEAT 찜 · 일정 · 리뷰</p>
              <p className="font-mono text-xs text-sunbeam">GATE OPEN</p>
            </div>
          </div>

          <p className="text-sand/60 text-sm leading-relaxed max-w-xs">
            가고 싶은 곳을 모으고, 함께 갈 일정을 짜고, 다녀온 곳을 남기는 곳.
          </p>
        </div>
      </div>

      {/* 오른쪽: 폼 패널 */}
      <div className="flex-1 flex items-center justify-center px-6 py-16 bg-sand">
        <div className="w-full max-w-sm">
          <div className="lg:hidden mb-10 mt-16 flex justify-center">
            <Logo className="h-28 w-auto max-w-[80vw]" />
          </div>

          {/* 탭 전환 — 흔한 알약형 세그먼트 대신 밑줄 언더라인 방식 */}
          <div className="flex gap-6 border-b border-harbor/10 mb-8">
            <button
              type="button"
              onClick={() => {
                setMode('login')
                resetErrors()
              }}
              className={`pb-3 text-[15px] font-medium transition-colors ${
                mode === 'login'
                  ? 'text-harbor border-b-2 border-coral'
                  : 'text-mist hover:text-harbor'
              }`}
            >
              로그인
            </button>
            <button
              type="button"
              onClick={() => {
                setMode('signup')
                resetErrors()
              }}
              className={`pb-3 text-[15px] font-medium transition-colors ${
                mode === 'signup'
                  ? 'text-harbor border-b-2 border-coral'
                  : 'text-mist hover:text-harbor'
              }`}
            >
              회원가입
            </button>
          </div>

          {formError && (
            <div className="mb-5 px-4 py-3 bg-coral/10 border-l-2 border-coral text-sm text-harbor">
              {formError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div>
              <label className="block text-sm text-mist mb-1.5" htmlFor="email">
                이메일
              </label>
              <input
                id="email"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                className="w-full px-3.5 py-2.5 bg-white border border-harbor/15 focus:border-seaglass focus:outline-none text-harbor placeholder:text-mist/50"
              />
              {fieldErrors.email && (
                <p className="mt-1 text-xs text-coral">{fieldErrors.email}</p>
              )}
            </div>

            <div>
              <label className="block text-sm text-mist mb-1.5" htmlFor="password">
                비밀번호
              </label>
              <input
                id="password"
                type="password"
                required
                minLength={8}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="8자 이상"
                className="w-full px-3.5 py-2.5 bg-white border border-harbor/15 focus:border-seaglass focus:outline-none text-harbor placeholder:text-mist/50"
              />
              {fieldErrors.password && (
                <p className="mt-1 text-xs text-coral">{fieldErrors.password}</p>
              )}
            </div>

            {mode === 'signup' && (
              <div>
                <label className="block text-sm text-mist mb-1.5" htmlFor="nickname">
                  닉네임
                </label>
                <input
                  id="nickname"
                  type="text"
                  required
                  minLength={2}
                  maxLength={20}
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  placeholder="2~20자"
                  className="w-full px-3.5 py-2.5 bg-white border border-harbor/15 focus:border-seaglass focus:outline-none text-harbor placeholder:text-mist/50"
                />
                {fieldErrors.nickname && (
                  <p className="mt-1 text-xs text-coral">{fieldErrors.nickname}</p>
                )}
              </div>
            )}

            <button
              type="submit"
              disabled={submitting}
              className="mt-2 w-full py-3 bg-coral text-sand font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-coral/90"
            >
              {submitting ? '처리 중' : mode === 'login' ? '로그인' : '회원가입'}
            </button>
          </form>

          <div className="flex items-center gap-3 my-7">
            <div className="flex-1 h-px bg-harbor/10" />
            <span className="text-xs text-mist">또는</span>
            <div className="flex-1 h-px bg-harbor/10" />
          </div>

          <div className="flex flex-col gap-2.5">
            {OAUTH_PROVIDERS.map((p) => (
              <button
                key={p.key}
                type="button"
                disabled={submitting}
                onClick={() => handleOAuthClick(p.key)}
                style={{ backgroundColor: p.bg, color: p.fg }}
                className={`w-full py-2.5 font-medium text-sm disabled:opacity-50 ${
                  p.border ? 'border border-harbor/15' : ''
                }`}
              >
                {p.label}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
