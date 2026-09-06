import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { shareApi } from '../api/itineraries'
import Logo from '../components/Logo'

// 화면 9 — 공유 일정 조회 (비로그인 접근 가능, 읽기 전용)
// 편집 화면과 구분되게 상단에 "공유된 일정" 안내 배너를 붙이고, 조작 UI(추가/이동/삭제/시간수정)는 전부 제거
export default function SharedItineraryPage() {
  const { token } = useParams()
  const navigate = useNavigate()

  const [itinerary, setItinerary] = useState(null)
  const [notFound, setNotFound] = useState(false)
  const [activeDay, setActiveDay] = useState(1)

  useEffect(() => {
    shareApi
      .getSharedItinerary(token)
      .then(({ data }) => {
        setItinerary(data)
        const firstDay = data.days?.[0]?.dayNo
        if (firstDay) setActiveDay(firstDay)
      })
      .catch((err) => {
        if (err?.response?.data?.code === 'ITINERARY_004') setNotFound(true)
      })
  }, [token])

  if (notFound) {
    return (
      <div className="min-h-screen bg-sand flex flex-col items-center justify-center gap-3">
        <Logo className="h-10 mb-2" />
        <p className="font-mono text-sm text-mist">유효하지 않거나 만료된 공유 링크입니다.</p>
        <button onClick={() => navigate('/')} className="text-sm text-seaglass underline">
          골라골라 둘러보기
        </button>
      </div>
    )
  }

  if (!itinerary) {
    return (
      <div className="min-h-screen bg-sand flex items-center justify-center">
        <p className="font-mono text-sm text-mist">불러오는 중…</p>
      </div>
    )
  }

  const currentDayItems = itinerary.days?.find((d) => d.dayNo === activeDay)?.items || []

  return (
    <div className="min-h-screen bg-sand">
      <header className="border-b border-harbor/10">
        <div className="max-w-3xl mx-auto px-5 py-4 flex items-center justify-between">
          <button onClick={() => navigate('/')}>
            <Logo className="h-10" />
          </button>
          <span className="px-2.5 py-1 text-[11px] font-mono border border-dashed border-seaglass text-seaglass">
            공유된 일정 · 읽기 전용
          </span>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-5 py-8">
        <h1 className="text-2xl font-semibold text-harbor mb-6">{itinerary.title}</h1>

        <div className="flex gap-1.5 mb-6 overflow-x-auto">
          {itinerary.days?.map((day) => (
            <button
              key={day.dayNo}
              type="button"
              onClick={() => setActiveDay(day.dayNo)}
              className={`shrink-0 px-4 py-2 text-sm font-mono border-b-2 ${
                activeDay === day.dayNo
                  ? 'border-coral text-harbor font-medium'
                  : 'border-transparent text-mist hover:text-harbor'
              }`}
            >
              {day.dayNo}일차
            </button>
          ))}
        </div>

        {currentDayItems.length === 0 && (
          <p className="text-center text-mist text-sm py-10 font-mono">
            등록된 장소가 없습니다.
          </p>
        )}

        <ul className="flex flex-col gap-3">
          {currentDayItems.map((item) => (
            <li key={item.itemId} className="border border-harbor/10 p-4">
              <button
                type="button"
                onClick={() => navigate(`/pois/${item.poiId}`)}
                className="w-full flex items-start gap-3 text-left"
              >
                <div className="w-16 h-16 shrink-0 bg-harbor/5 overflow-hidden">
                  {item.poiThumbnailUrl ? (
                    <img
                      src={item.poiThumbnailUrl}
                      alt={item.poiName}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-mist text-[9px] font-mono">
                      NO IMAGE
                    </div>
                  )}
                </div>

                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <h3 className="text-harbor font-medium truncate">{item.poiName}</h3>
                    {item.isAnchor && (
                      <span className="shrink-0 px-1.5 py-0.5 text-[10px] font-mono border border-dashed border-sunbeam text-harbor">
                        AI 추천
                      </span>
                    )}
                  </div>
                  {(item.startTime || item.endTime) && (
                    <p className="mt-1 text-xs font-mono text-mist">
                      {item.startTime?.slice(0, 5)} ~ {item.endTime?.slice(0, 5)}
                    </p>
                  )}
                  {item.memo && (
                    <p className="mt-1.5 text-sm text-harbor/80">{item.memo}</p>
                  )}
                </div>
              </button>
            </li>
          ))}
        </ul>
      </main>
    </div>
  )
}
