import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { wishlistApi } from '../api/wishlist'
import AppHeader from '../components/AppHeader'

const CATEGORY_LABEL = {
  HOTEL: '숙소',
  ATTRACTION: '관광지',
  ACTIVITY: '액티비티',
  RESTAURANT: '맛집',
  FESTIVAL: '축제',
}

// SCREENS.md: isPublic은 활용 API가 없어 UI에 노출하지 않음 (내부 필드로만 유지)
export default function WishlistPage() {
  const navigate = useNavigate()
  const [items, setItems] = useState(null) // null = 로딩 중
  const [removingId, setRemovingId] = useState(null)

  useEffect(() => {
    wishlistApi.getWishlist().then(({ data }) => setItems(data))
  }, [])

  async function handleRemove(poiId) {
    setRemovingId(poiId)
    const prevItems = items
    setItems((prev) => prev.filter((i) => i.poiId !== poiId))
    try {
      await wishlistApi.removeWish(poiId)
    } catch {
      setItems(prevItems) // 실패 시 롤백
    } finally {
      setRemovingId(null)
    }
  }

  return (
    <div className="min-h-screen bg-sand">
      <AppHeader />

      <main className="max-w-3xl mx-auto px-5 py-8">
        <h1 className="text-2xl font-semibold text-harbor mb-6">찜 목록</h1>

        {items === null && (
          <p className="text-center text-mist text-sm py-16 font-mono">불러오는 중…</p>
        )}

        {items && items.length === 0 && (
          <div className="flex flex-col items-center gap-4 py-16">
            <p className="text-center text-mist text-sm font-mono">
              아직 찜한 장소가 없습니다.
            </p>
            <button
              type="button"
              onClick={() => navigate('/')}
              className="px-4 py-2 text-sm font-medium bg-harbor text-sand"
            >
              장소 둘러보러 가기
            </button>
          </div>
        )}

        {items && items.length > 0 && (
          <ul className="flex flex-col divide-y divide-harbor/10">
            {items.map((item) => (
              <li key={item.poiId} className="py-4 flex items-center gap-4">
                <button
                  type="button"
                  onClick={() => navigate(`/pois/${item.poiId}`)}
                  className="flex items-center gap-4 flex-1 text-left min-w-0"
                >
                  <div className="w-20 h-20 shrink-0 bg-harbor/5 overflow-hidden">
                    {item.thumbnailUrl ? (
                      <img
                        src={item.thumbnailUrl}
                        alt={item.name}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-mist text-[10px] font-mono">
                        NO IMAGE
                      </div>
                    )}
                  </div>
                  <div className="min-w-0">
                    <p className="text-xs font-mono text-seaglass tracking-wide">
                      {CATEGORY_LABEL[item.category] || item.category}
                    </p>
                    <h3 className="text-harbor font-medium truncate">{item.name}</h3>
                    <p className="text-xs text-mist mt-0.5 truncate">{item.address}</p>
                    <p className="font-mono text-xs text-harbor mt-0.5">★ {item.rating?.toFixed(1)}</p>
                  </div>
                </button>
                <button
                  type="button"
                  onClick={() => handleRemove(item.poiId)}
                  disabled={removingId === item.poiId}
                  className="shrink-0 px-3 py-1.5 text-xs font-medium text-mist border border-harbor/15 hover:border-coral hover:text-coral disabled:opacity-50"
                >
                  삭제
                </button>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  )
}
