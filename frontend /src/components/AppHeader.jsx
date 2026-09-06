import { useNavigate, useLocation } from 'react-router-dom'
import Logo from './Logo'

const NAV_ITEMS = [
  { path: '/wishlist', label: '찜 목록' },
  { path: '/itineraries', label: '일정' },
  { path: '/mypage', label: '마이페이지' },
]

// 공통 네비게이션 헤더 — 홈 외 화면에서 재사용 (홈은 검색바가 있어 자체 헤더 유지)
export default function AppHeader() {
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <header className="sticky top-0 z-10 bg-sand/95 backdrop-blur-sm border-b border-harbor/10">
      <div className="max-w-5xl mx-auto px-5 py-4 flex items-center justify-between gap-4">
        <button onClick={() => navigate('/')}>
          <Logo className="h-14 shrink-0" />
        </button>
        <nav className="flex items-center gap-4 text-sm font-medium shrink-0">
          {NAV_ITEMS.map((item) => (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              className={
                location.pathname.startsWith(item.path)
                  ? 'text-harbor'
                  : 'text-mist hover:text-harbor'
              }
            >
              {item.label}
            </button>
          ))}
        </nav>
      </div>
    </header>
  )
}
