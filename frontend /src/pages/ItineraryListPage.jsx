import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { itinerariesApi } from '../api/itineraries'
import AppHeader from '../components/AppHeader'

const TRANSPORT_LABEL = {
  CAR: '자동차',
  TRANSIT: '대중교통',
  WALK: '도보',
}

// genType 뱃지 — MANUAL/AI를 색으로만 구분하지 않고 형태(각진 스탬프 vs 점선 보더)로도 구분해
// 색약 사용자도 구분 가능하게, 또한 "AI가 만든 티"인 그라디언트 뱃지는 피함
function GenTypeBadge({ genType }) {
  if (genType === 'AI') {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-mono border border-dashed border-sunbeam text-harbor">
        AI 생성
      </span>
    )
  }
  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-mono border border-harbor/25 text-mist">
      직접 생성
    </span>
  )
}

export default function ItineraryListPage() {
  const navigate = useNavigate()
  const [itineraries, setItineraries] = useState(null) // null = 로딩 중
  const [deletingId, setDeletingId] = useState(null)

  useEffect(() => {
    itinerariesApi.getItineraries().then(({ data }) => setItineraries(data))
  }, [])

  async function handleDelete(e, id) {
    e.stopPropagation()
    if (!window.confirm('이 일정을 삭제할까요?')) return
    setDeletingId(id)
    const prev = itineraries
    setItineraries((cur) => cur.filter((it) => it.itineraryId !== id))
    try {
      await itinerariesApi.deleteItinerary(id)
    } catch {
      setItineraries(prev)
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <div className="min-h-screen bg-sand">
      <AppHeader />

      <main className="max-w-3xl mx-auto px-5 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-semibold text-harbor">일정</h1>
          <button
            type="button"
            onClick={() => navigate('/itineraries/new')}
            className="px-4 py-2 text-sm font-medium bg-coral text-sand"
          >
            새 일정
          </button>
        </div>

        {itineraries === null && (
          <p className="text-center text-mist text-sm py-16 font-mono">불러오는 중…</p>
        )}

        {itineraries && itineraries.length === 0 && (
          <div className="flex flex-col items-center gap-4 py-16">
            <p className="text-center text-mist text-sm font-mono">아직 만든 일정이 없습니다.</p>
            <button
              type="button"
              onClick={() => navigate('/itineraries/new')}
              className="px-4 py-2 text-sm font-medium bg-harbor text-sand"
            >
              새 일정 만들기
            </button>
          </div>
        )}

        {itineraries && itineraries.length > 0 && (
          <ul className="flex flex-col divide-y divide-harbor/10">
            {itineraries.map((it) => (
              <li key={it.itineraryId}>
                <button
                  type="button"
                  onClick={() =>
                    navigate(`/itineraries/${it.itineraryId}`, {
                      state: { dateRange: { startDate: it.startDate, endDate: it.endDate } },
                    })
                  }
                  className="w-full text-left py-5 flex items-center justify-between gap-4 hover:bg-harbor/[0.03] px-2"
                >
                  <div className="min-w-0">
                    <div className="flex items-center gap-2 mb-1.5">
                      <GenTypeBadge genType={it.genType} />
                    </div>
                    <h3 className="text-harbor font-medium truncate">{it.title}</h3>
                    <p className="mt-1 font-mono text-xs text-mist">
                      {it.startDate} ~ {it.endDate} · {TRANSPORT_LABEL[it.transportMode] || it.transportMode}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={(e) => handleDelete(e, it.itineraryId)}
                    disabled={deletingId === it.itineraryId}
                    className="shrink-0 px-3 py-1.5 text-xs font-medium text-mist border border-harbor/15 hover:border-coral hover:text-coral disabled:opacity-50"
                  >
                    삭제
                  </button>
                </button>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  )
}
