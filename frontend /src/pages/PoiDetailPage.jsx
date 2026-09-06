import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { poisApi } from '../api/pois'
import { reviewsApi } from '../api/reviews'
import { wishlistApi } from '../api/wishlist'
import { useAuth } from '../context/AuthContext'
import KakaoMapView from '../components/KakaoMapView'
import ReviewModal from '../components/ReviewModal'

const DAY_ORDER = ['월', '화', '수', '목', '금', '토', '일']

export default function PoiDetailPage() {
  const { poiId } = useParams()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()

  const [poi, setPoi] = useState(null)
  const [notFound, setNotFound] = useState(false)
  const [activeImage, setActiveImage] = useState(0)
  const [wishBusy, setWishBusy] = useState(false)

  const [reviews, setReviews] = useState([])
  const [reviewPage, setReviewPage] = useState(0)
  const [reviewHasNext, setReviewHasNext] = useState(false)
  const [reviewLoading, setReviewLoading] = useState(false)

  const [reviewModalOpen, setReviewModalOpen] = useState(false)

  const sentinelRef = useRef(null)

  useEffect(() => {
    setPoi(null)
    setNotFound(false)
    poisApi
      .getPoiDetail(poiId)
      .then(({ data }) => setPoi(data))
      .catch((err) => {
        if (err?.response?.data?.code === 'POI_001') setNotFound(true)
      })
  }, [poiId])

  const loadReviews = useCallback(
    async (targetPage) => {
      setReviewLoading(true)
      try {
        const { data } = await reviewsApi.getReviews(poiId, { page: targetPage })
        setReviews((prev) => (targetPage === 0 ? data.content : [...prev, ...data.content]))
        setReviewHasNext(data.hasNext)
        setReviewPage(targetPage)
      } finally {
        setReviewLoading(false)
      }
    },
    [poiId]
  )

  useEffect(() => {
    setReviews([])
    setReviewPage(0)
    setReviewHasNext(false)
    loadReviews(0)
  }, [loadReviews])

  useEffect(() => {
    const el = sentinelRef.current
    if (!el) return
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && reviewHasNext && !reviewLoading) {
          loadReviews(reviewPage + 1)
        }
      },
      { rootMargin: '150px' }
    )
    observer.observe(el)
    return () => observer.disconnect()
  }, [reviewHasNext, reviewLoading, reviewPage, loadReviews])

  async function handleToggleWish() {
    if (!isAuthenticated) {
      navigate('/login')
      return
    }
    if (!poi || wishBusy) return
    setWishBusy(true)
    const wasWished = poi.isWished
    setPoi((prev) => ({ ...prev, isWished: !wasWished }))
    try {
      if (wasWished) {
        await wishlistApi.removeWish(poi.poiId)
      } else {
        await wishlistApi.addWish({ poiId: poi.poiId, isPublic: true })
      }
    } catch {
      setPoi((prev) => ({ ...prev, isWished: wasWished }))
    } finally {
      setWishBusy(false)
    }
  }

  function handleReviewClick() {
    if (!isAuthenticated) {
      navigate('/login')
      return
    }
    setReviewModalOpen(true)
  }

  function handleReviewCreated({ poiRatingUpdated }) {
    setReviewModalOpen(false)
    setPoi((prev) => ({ ...prev, rating: poiRatingUpdated }))
    setReviews([])
    setReviewPage(0)
    setReviewHasNext(false)
    loadReviews(0)
  }

  if (notFound) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center gap-3 bg-sand text-harbor">
        <p className="font-mono text-sm text-mist">존재하지 않는 장소입니다.</p>
        <button onClick={() => navigate('/')} className="text-sm text-seaglass underline">
          목록으로 돌아가기
        </button>
      </div>
    )
  }

  if (!poi) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-sand">
        <p className="font-mono text-sm text-mist">불러오는 중…</p>
      </div>
    )
  }

  const images = poi.imageUrls?.length ? poi.imageUrls : [poi.thumbnailUrl].filter(Boolean)

  return (
    <div className="min-h-screen bg-sand">
      <header className="max-w-3xl mx-auto px-5 pt-6">
        <button onClick={() => navigate(-1)} className="text-sm text-mist hover:text-harbor">
          ← 목록으로
        </button>
      </header>

      <main className="max-w-3xl mx-auto px-5 pb-24">
        {/* 이미지 갤러리 — 메인 1장 + 하단 필름스트립 썸네일 */}
        <div className="mt-4">
          <div className="aspect-[4/3] bg-harbor/5 overflow-hidden">
            {images[activeImage] ? (
              <img
                src={images[activeImage]}
                alt={poi.name}
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-mist text-xs font-mono">
                NO IMAGE
              </div>
            )}
          </div>
          {images.length > 1 && (
            <div className="flex gap-2 mt-2 overflow-x-auto">
              {images.map((url, idx) => (
                <button
                  key={url + idx}
                  onClick={() => setActiveImage(idx)}
                  className={`w-16 h-16 shrink-0 overflow-hidden ${
                    idx === activeImage ? 'ring-2 ring-coral' : 'opacity-70'
                  }`}
                >
                  <img src={url} alt="" className="w-full h-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* 헤더: 이름 / 평점 / 찜 */}
        <div className="flex items-start justify-between gap-4 mt-6">
          <div>
            <p className="text-xs font-mono text-seaglass tracking-wide">{poi.category}</p>
            <h1 className="text-2xl font-semibold text-harbor mt-1">{poi.name}</h1>
            <p className="mt-1 font-mono text-sm text-harbor">★ {poi.rating?.toFixed(1)}</p>
          </div>
          <button
            type="button"
            onClick={handleToggleWish}
            disabled={wishBusy}
            className={`shrink-0 px-4 py-2 border text-sm font-medium disabled:opacity-50 ${
              poi.isWished
                ? 'bg-coral text-sand border-coral'
                : 'bg-transparent text-harbor border-harbor/25'
            }`}
          >
            {poi.isWished ? '♥ 찜했어요' : '♡ 찜하기'}
          </button>
        </div>

        {poi.description && (
          <p className="mt-4 text-[15px] text-harbor/80 leading-relaxed whitespace-pre-line">
            {poi.description}
          </p>
        )}

        <div className="mt-8 mb-12">
          <h2 className="text-xl font-semibold text-harbor mb-4">위치</h2>
          <KakaoMapView markers={[{ lat: poi.lat, lng: poi.lng, title: poi.name }]} />
          {poi.naverMapUrl && (
            <a
              href={poi.naverMapUrl}
              target="_blank"
              rel="noreferrer"
              className="mt-2 inline-block text-sm text-seaglass underline underline-offset-2"
            >
              카카오맵에서 보기 (추후 카카오 URL로 변경 필요)
            </a>
          )}
        </div>

        {/* 영업시간 / 휴무일 */}
        <OpeningHoursTable openHours={poi.openHours} breakTime={poi.breakTime} closedDays={poi.closedDays} />

        {/* 리뷰 */}
        <section className="mt-10">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-harbor">리뷰</h2>
            <button
              type="button"
              onClick={handleReviewClick}
              className="text-sm font-medium text-sand bg-harbor px-4 py-2"
            >
              리뷰 작성
            </button>
          </div>

          {reviews.length === 0 && !reviewLoading && (
            <p className="text-sm text-mist font-mono py-8 text-center">
              아직 작성된 리뷰가 없습니다.
            </p>
          )}

          <ul className="flex flex-col divide-y divide-harbor/10">
            {reviews.map((r) => (
              <li key={r.reviewId} className="py-4">
                <div className="flex items-center justify-between">
                  <span className="font-medium text-harbor text-sm">{r.nickname}</span>
                  <span className="font-mono text-xs text-mist">
                    {r.createdAt?.slice(0, 10)}
                  </span>
                </div>
                <p className="font-mono text-sm text-coral mt-1">{'★'.repeat(r.rating)}</p>
                <p className="mt-1.5 text-[15px] text-harbor/85 leading-relaxed">{r.content}</p>
              </li>
            ))}
          </ul>

          <div ref={sentinelRef} className="h-8" />
          {reviewLoading && (
            <p className="text-center text-mist text-xs font-mono py-3">불러오는 중…</p>
          )}
        </section>
      </main>

      {reviewModalOpen && (
        <ReviewModal
          poiId={poi.poiId}
          onClose={() => setReviewModalOpen(false)}
          onCreated={handleReviewCreated}
        />
      )}
    </div>
  )
}

function OpeningHoursTable({ openHours, breakTime, closedDays }) {
  const hasHours = openHours && Object.keys(openHours).length > 0
  if (!hasHours && !closedDays) return null

  return (
    <div className="mt-8 border-t border-harbor/10 pt-6">
      <h2 className="text-lg font-semibold text-harbor mb-3">이용 안내</h2>
      {hasHours && (
        <table className="w-full text-sm font-mono">
          <tbody>
            {DAY_ORDER.filter((d) => openHours[d]).map((day) => (
              <tr key={day} className="border-b border-harbor/5">
                <td className="py-1.5 pr-4 text-mist w-10">{day}</td>
                <td className="py-1.5 text-harbor">{openHours[day]}</td>
                <td className="py-1.5 pl-4 text-mist text-xs">
                  {breakTime?.[day] ? `휴게 ${breakTime[day]}` : ''}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {closedDays && (
        <p className="mt-2 text-sm text-mist">
          휴무일 <span className="text-harbor">{closedDays}</span>
        </p>
      )}
    </div>
  )
}
