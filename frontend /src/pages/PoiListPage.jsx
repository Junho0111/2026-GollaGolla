import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { poisApi } from '../api/pois'
import { wishlistApi } from '../api/wishlist'
import { useAuth } from '../context/AuthContext'
import { useDebouncedValue } from '../hooks/useDebouncedValue'
import CategoryStamp from '../components/CategoryStamp'
import RegionSelect from '../components/RegionSelect'
import PoiCard from '../components/PoiCard'
import Logo from '../components/Logo'

const PAGE_SIZE = 20

export default function PoiListPage() {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()

  const [regionId, setRegionId] = useState(null)
  const [category, setCategory] = useState(null)
  const [searchInput, setSearchInput] = useState('')
  const debouncedSearch = useDebouncedValue(searchInput, 350)

  const [pois, setPois] = useState([])
  const [page, setPage] = useState(0)
  const [hasNext, setHasNext] = useState(false)
  const [loading, setLoading] = useState(false)

  const [searchResults, setSearchResults] = useState(null) // null이면 검색 모드 아님

  const sentinelRef = useRef(null)

  // 필터가 바뀌면 목록을 처음부터 다시 로드
  useEffect(() => {
    if (debouncedSearch.trim()) return // 검색 모드일 땐 피드 로드 안 함
    setPois([])
    setPage(0)
    setHasNext(false)
    loadPage(0, { regionId, category })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [regionId, category])

  // 검색어 처리
  useEffect(() => {
    const keyword = debouncedSearch.trim()
    if (!keyword) {
      setSearchResults(null)
      return
    }
    poisApi
      .searchPois(keyword)
      .then(({ data }) => setSearchResults(data.results))
      .catch(() => setSearchResults([]))
  }, [debouncedSearch])

  const loadPage = useCallback(async (targetPage, filters) => {
    setLoading(true)
    try {
      const { data } = await poisApi.getPois({
        regionId: filters.regionId,
        category: filters.category,
        page: targetPage,
        size: PAGE_SIZE,
      })
      setPois((prev) => (targetPage === 0 ? data.content : [...prev, ...data.content]))
      setHasNext(data.hasNext)
      setPage(targetPage)
    } finally {
      setLoading(false)
    }
  }, [])

  // 무한스크롤 — sentinel 관찰
  useEffect(() => {
    if (debouncedSearch.trim()) return
    const el = sentinelRef.current
    if (!el) return

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasNext && !loading) {
          loadPage(page + 1, { regionId, category })
        }
      },
      { rootMargin: '200px' }
    )
    observer.observe(el)
    return () => observer.disconnect()
  }, [hasNext, loading, page, regionId, category, loadPage, debouncedSearch])

  async function handleToggleWish(poi) {
    if (!isAuthenticated) {
      navigate('/login')
      return
    }
    // 낙관적 업데이트
    setPois((prev) =>
      prev.map((p) =>
        p.poiId === poi.poiId
          ? { ...p, wished: !p.wished, wishCount: p.wishCount + (p.wished ? -1 : 1) }
          : p
      )
    )
    try {
      if (poi.wished) {
        await wishlistApi.removeWish(poi.poiId)
      } else {
        await wishlistApi.addWish({ poiId: poi.poiId, isPublic: true })
      }
    } catch {
      // 실패 시 롤백
      setPois((prev) =>
        prev.map((p) =>
          p.poiId === poi.poiId
            ? { ...p, wished: poi.wished, wishCount: poi.wishCount }
            : p
        )
      )
    }
  }

  const isSearchMode = debouncedSearch.trim().length > 0

  return (
    <div className="min-h-screen bg-sand">
      <header className="sticky top-0 z-10 bg-sand/95 backdrop-blur-sm border-b border-harbor/10">
        <div className="max-w-5xl mx-auto px-5 py-4 flex items-center justify-between gap-4">
          <Logo className="h-10 shrink-0" />
          <input
            type="search"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="장소, 지역으로 검색"
            className="flex-1 max-w-sm px-3.5 py-2 bg-white border border-harbor/15 text-sm focus:outline-none focus:border-seaglass"
          />
          <nav className="flex items-center gap-4 text-sm font-medium shrink-0">
            <button onClick={() => navigate('/wishlist')} className="text-mist hover:text-harbor">
              찜 목록
            </button>
            <button onClick={() => navigate('/itineraries')} className="text-mist hover:text-harbor">
              일정
            </button>
            <button onClick={() => navigate('/mypage')} className="text-mist hover:text-harbor">
              마이페이지
            </button>
          </nav>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-5 py-6">
        {isSearchMode ? (
          <SearchResultList results={searchResults} onNavigate={(id) => navigate(`/pois/${id}`)} />
        ) : (
          <>
            <div className="flex flex-col gap-3 mb-6">
              <RegionSelect regionId={regionId} onChange={setRegionId} />
              <CategoryStamp selected={category} onSelect={setCategory} />
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-x-5 gap-y-6">
              {pois.map((poi) => (
                <PoiCard key={poi.poiId} poi={poi} onToggleWish={handleToggleWish} />
              ))}
            </div>

            {pois.length === 0 && !loading && (
              <p className="text-center text-mist text-sm py-16 font-mono">
                조건에 맞는 장소가 없습니다.
              </p>
            )}

            <div ref={sentinelRef} className="h-10" />
            {loading && (
              <p className="text-center text-mist text-sm py-4 font-mono">불러오는 중…</p>
            )}
          </>
        )}
      </main>
    </div>
  )
}

function SearchResultList({ results, onNavigate }) {
  if (results === null) return null
  if (results.length === 0) {
    return <p className="text-center text-mist text-sm py-16 font-mono">검색 결과가 없습니다.</p>
  }
  return (
    <ul className="flex flex-col divide-y divide-harbor/10">
      {results.map((r) => (
        <li key={r.poiId}>
          <button
            type="button"
            onClick={() => onNavigate(r.poiId)}
            className="w-full text-left py-3.5 flex items-center justify-between gap-3 hover:bg-harbor/[0.03] px-2"
          >
            <span className="text-harbor font-medium">{r.name}</span>
            <span className="text-xs text-mist font-mono">
              {r.regionName} · {r.category}
            </span>
          </button>
        </li>
      ))}
    </ul>
  )
}
