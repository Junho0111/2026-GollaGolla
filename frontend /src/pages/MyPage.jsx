import { useEffect, useState } from 'react'
import { authApi } from '../api/auth'
import { useAuth } from '../context/AuthContext'
import AppHeader from '../components/AppHeader'

const PROVIDER_LABEL = {
  LOCAL: '이메일',
  KAKAO: '카카오',
  NAVER: '네이버',
  GOOGLE: 'Google',
}

// 화면 10 — 마이페이지
// 여권 정보 페이지 느낌으로 구성 — 카드형 프로필 박스 대신 라벨/값이 나란한 담백한 신원 정보 레이아웃
export default function MyPage() {
  const { logout, withdraw } = useAuth()
  const [member, setMember] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    authApi.getMe().then(({ data }) => setMember(data))
  }, [])

  async function handleLogout() {
    setBusy(true)
    try {
      await logout()
    } finally {
      setBusy(false)
    }
  }

  async function handleWithdraw() {
    if (!window.confirm('정말 탈퇴하시겠어요? 이 작업은 되돌릴 수 없습니다.')) return
    setBusy(true)
    try {
      await withdraw()
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="min-h-screen bg-sand">
      <AppHeader />

      <main className="max-w-2xl mx-auto px-5 py-8">
        <h1 className="text-2xl font-semibold text-harbor mb-6">마이페이지</h1>

        {!member && <p className="font-mono text-sm text-mist py-8">불러오는 중…</p>}

        {member && (
          <div className="border border-harbor/10">
            <div className="flex items-center justify-between px-5 py-4 border-b border-harbor/10">
              <span className="text-sm text-mist">닉네임</span>
              <span className="text-harbor font-medium">{member.nickname}</span>
            </div>
            <div className="flex items-center justify-between px-5 py-4 border-b border-harbor/10">
              <span className="text-sm text-mist">이메일</span>
              <span className="text-harbor font-mono text-sm">{member.email}</span>
            </div>
            <div className="flex items-center justify-between px-5 py-4">
              <span className="text-sm text-mist">가입 방식</span>
              <span className="text-harbor font-mono text-sm">
                {PROVIDER_LABEL[member.provider] || member.provider}
              </span>
            </div>
          </div>
        )}

        <div className="flex flex-col gap-2 mt-8">
          <button
            type="button"
            onClick={handleLogout}
            disabled={busy}
            className="w-full py-3 text-sm font-medium border border-harbor/25 text-harbor disabled:opacity-50"
          >
            로그아웃
          </button>
          <button
            type="button"
            onClick={handleWithdraw}
            disabled={busy}
            className="w-full py-3 text-sm font-medium text-coral disabled:opacity-50"
          >
            회원 탈퇴
          </button>
        </div>
      </main>
    </div>
  )
}
