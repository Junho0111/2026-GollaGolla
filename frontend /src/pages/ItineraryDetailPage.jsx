import { useEffect, useMemo, useState } from 'react'
import { useLocation, useParams } from 'react-router-dom'
import { itinerariesApi } from '../api/itineraries'
import { wishlistApi } from '../api/wishlist'
import AppHeader from '../components/AppHeader'
import AddPoiModal from '../components/AddPoiModal'
import ShareLinkModal from '../components/ShareLinkModal'

// 요일별 탭 대신 "1일차/2일차..." 각진 탭 — 여행 일정표 특유의 D-day 감각을 살림
function daysBetween(startDate, endDate) {
  if (!startDate || !endDate) return null
  const start = new Date(startDate)
  const end = new Date(endDate)
  const diffMs = end.getTime() - start.getTime()
  return Math.max(1, Math.round(diffMs / (1000 * 60 * 60 * 24)) + 1)
}

export default function ItineraryDetailPage() {
  const { id } = useParams()
  const location = useLocation()

  // startDate/endDate는 상세 API에 없어 진입 시점(목록 클릭 / 생성 직후)의 state로 전달받음.
  // 새로고침(F5) 등으로 state가 사라진 경우, 목록 API(GET /itineraries)에서 같은 id를 찾아 보완한다.
  const [dateRange, setDateRange] = useState(location.state?.dateRange || null)
  const totalDays = useMemo(() => daysBetween(dateRange?.startDate, dateRange?.endDate), [dateRange])

  const [itinerary, setItinerary] = useState(null)
  const [activeDay, setActiveDay] = useState(1)

  const [aiToast, setAiToast] = useState(location.state?.aiExplanation || null)
  const [wishItems, setWishItems] = useState(null)

  const [addModalOpen, setAddModalOpen] = useState(false)
  const [shareUrl, setShareUrl] = useState(null)
  const [shareBusy, setShareBusy] = useState(false)

  const [busyItemId, setBusyItemId] = useState(null)
  const [memoDraftId, setMemoDraftId] = useState(null)
  const [memoDraftValue, setMemoDraftValue] = useState('')

  function loadDetail() {
    itinerariesApi.getItineraryDetail(id).then(({ data }) => setItinerary(data))
  }

  useEffect(() => {
    loadDetail()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  useEffect(() => {
    if (wishItems === null) {
      wishlistApi.getWishlist().then(({ data }) => setWishItems(data))
    }
  }, [wishItems])

  useEffect(() => {
    if (dateRange) return // state로 이미 받은 경우 재조회 불필요
    itinerariesApi.getItineraries().then(({ data }) => {
      const matched = data.find((it) => String(it.itineraryId) === String(id))
      if (matched) {
        setDateRange({ startDate: matched.startDate, endDate: matched.endDate })
      }
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  useEffect(() => {
    // 사용자가 직접 X를 눌러서 닫도록 자동 닫기 타이머를 제거함
  }, [aiToast])

  // API가 아이템 없는 일차는 응답에서 생략하므로, 탭 개수는 (전달받은 총 일수) vs (실제 존재하는 최대 dayNo) 중 큰 쪽 사용
  const dayTabCount = useMemo(() => {
    const maxExistingDay = itinerary?.days?.reduce((max, d) => Math.max(max, d.dayNo), 0) || 0
    return Math.max(totalDays || 0, maxExistingDay, 1)
  }, [itinerary, totalDays])

  const currentDayItems = useMemo(() => {
    const day = itinerary?.days?.find((d) => d.dayNo === activeDay)
    return day?.items || []
  }, [itinerary, activeDay])

  async function handleAddPoi(poi) {
    setAddModalOpen(false)
    const nextSeq = currentDayItems.length
    try {
      await itinerariesApi.addItem(id, { poiId: poi.poiId, dayNo: activeDay, seq: nextSeq })
      loadDetail()
    } catch {
      // 실패 시 별도 처리 없이 목록 그대로 유지 (재시도는 다시 추가 버튼으로)
    }
  }

  async function handleMove(item, direction) {
    const idx = currentDayItems.findIndex((i) => i.itemId === item.itemId)
    const targetIdx = idx + direction
    if (targetIdx < 0 || targetIdx >= currentDayItems.length) return

    const targetItem = currentDayItems[targetIdx]
    setBusyItemId(item.itemId)
    try {
      // 두 아이템의 seq를 맞바꿈
      await itinerariesApi.updateItem(id, item.itemId, { seq: targetItem.seq })
      await itinerariesApi.updateItem(id, targetItem.itemId, { seq: item.seq })
      loadDetail()
    } finally {
      setBusyItemId(null)
    }
  }

  async function handleTimeChange(item, field, value) {
    setBusyItemId(item.itemId)
    try {
      await itinerariesApi.updateItem(id, item.itemId, { [field]: value || null })
      loadDetail()
    } finally {
      setBusyItemId(null)
    }
  }

  function openMemoEditor(item) {
    setMemoDraftId(item.itemId)
    setMemoDraftValue(item.memo || '')
  }

  async function saveMemo(itemId) {
    setBusyItemId(itemId)
    try {
      await itinerariesApi.updateItemMemo(id, itemId, memoDraftValue.trim() || null)
      setMemoDraftId(null)
      loadDetail()
    } finally {
      setBusyItemId(null)
    }
  }

  async function handleDeleteItem(itemId) {
    if (!window.confirm('이 장소를 일정에서 삭제할까요?')) return
    setBusyItemId(itemId)
    try {
      await itinerariesApi.deleteItem(id, itemId)
      loadDetail()
    } finally {
      setBusyItemId(null)
    }
  }

  async function handleCreateShare() {
    setShareBusy(true)
    try {
      const { data } = await itinerariesApi.createShareLink(id)
      setShareUrl(data.url)
    } finally {
      setShareBusy(false)
    }
  }

  if (!itinerary) {
    return (
      <div className="min-h-screen bg-sand">
        <AppHeader />
        <main className="max-w-3xl mx-auto px-5 py-8">
          <p className="font-mono text-sm text-mist">불러오는 중…</p>
        </main>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-sand">
      <AppHeader />

      <main className="max-w-3xl mx-auto px-5 py-8">
        <div className="flex items-start justify-between gap-4 mb-6">
          <h1 className="text-2xl font-semibold text-harbor">{itinerary.title}</h1>
          <button
            type="button"
            onClick={handleCreateShare}
            disabled={shareBusy}
            className="shrink-0 px-4 py-2 text-sm font-medium border border-harbor/25 text-harbor disabled:opacity-50"
          >
            {shareBusy ? '생성 중' : '공유 링크'}
          </button>
        </div>

        {/* 일차 탭 */}
        <div className="flex gap-1.5 mb-6 overflow-x-auto">
          {Array.from({ length: dayTabCount }, (_, i) => i + 1).map((dayNo) => (
            <button
              key={dayNo}
              type="button"
              onClick={() => setActiveDay(dayNo)}
              className={`shrink-0 px-4 py-2 text-sm font-mono border-b-2 ${
                activeDay === dayNo
                  ? 'border-coral text-harbor font-medium'
                  : 'border-transparent text-mist hover:text-harbor'
              }`}
            >
              {dayNo}일차
            </button>
          ))}
        </div>

        <button
          type="button"
          onClick={() => setAddModalOpen(true)}
          className="w-full mb-5 py-3 border border-dashed border-harbor/25 text-sm text-mist hover:text-harbor hover:border-harbor/40"
        >
          + 이 일차에 장소 추가
        </button>

        {currentDayItems.length === 0 && (
          <p className="text-center text-mist text-sm py-10 font-mono">
            아직 등록된 장소가 없습니다.
          </p>
        )}

        <ul className="flex flex-col gap-3">
          {currentDayItems.map((item, idx) => (
            <li
              key={item.itemId}
              className={`border border-harbor/10 p-4 ${busyItemId === item.itemId ? 'opacity-50' : ''}`}
            >
              <div className="flex items-start gap-3">
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

                  <div className="flex items-center gap-2 mt-1.5">
                    <input
                      type="time"
                      value={item.startTime?.slice(0, 5) || ''}
                      onChange={(e) => handleTimeChange(item, 'startTime', e.target.value && `${e.target.value}:00`)}
                      className="px-2 py-1 text-xs font-mono bg-white border border-harbor/15"
                    />
                    <span className="text-xs text-mist">~</span>
                    <input
                      type="time"
                      value={item.endTime?.slice(0, 5) || ''}
                      onChange={(e) => handleTimeChange(item, 'endTime', e.target.value && `${e.target.value}:00`)}
                      className="px-2 py-1 text-xs font-mono bg-white border border-harbor/15"
                    />
                  </div>

                  {memoDraftId === item.itemId ? (
                    <div className="mt-2 flex gap-2">
                      <input
                        autoFocus
                        type="text"
                        value={memoDraftValue}
                        onChange={(e) => setMemoDraftValue(e.target.value)}
                        placeholder="메모 입력"
                        className="flex-1 px-2 py-1.5 text-sm bg-white border border-harbor/15 focus:outline-none focus:border-seaglass"
                      />
                      <button
                        type="button"
                        onClick={() => saveMemo(item.itemId)}
                        className="px-3 py-1.5 text-xs font-medium bg-harbor text-sand"
                      >
                        저장
                      </button>
                      <button
                        type="button"
                        onClick={() => setMemoDraftId(null)}
                        className="px-2 py-1.5 text-xs text-mist"
                      >
                        취소
                      </button>
                    </div>
                  ) : (
                    <button
                      type="button"
                      onClick={() => openMemoEditor(item)}
                      className="mt-2 text-xs text-seaglass underline underline-offset-2"
                    >
                      {item.memo ? item.memo : '메모 추가'}
                    </button>
                  )}
                </div>

                <div className="shrink-0 flex flex-col items-center gap-1">
                  <button
                    type="button"
                    onClick={() => handleMove(item, -1)}
                    disabled={idx === 0}
                    aria-label="위로 이동"
                    className="w-7 h-7 flex items-center justify-center text-mist border border-harbor/15 disabled:opacity-30"
                  >
                    ▲
                  </button>
                  <button
                    type="button"
                    onClick={() => handleMove(item, 1)}
                    disabled={idx === currentDayItems.length - 1}
                    aria-label="아래로 이동"
                    className="w-7 h-7 flex items-center justify-center text-mist border border-harbor/15 disabled:opacity-30"
                  >
                    ▼
                  </button>
                  <button
                    type="button"
                    onClick={() => handleDeleteItem(item.itemId)}
                    aria-label="삭제"
                    className="w-7 h-7 flex items-center justify-center text-coral border border-coral/30 mt-1"
                  >
                    ✕
                  </button>
                </div>
              </div>
            </li>
          ))}
        </ul>

        {/* 하단 찜한 장소 목록 패널 */}
        <div className="mt-10 pt-6 border-t border-harbor/20">
          <h2 className="text-lg font-medium text-harbor mb-4">내 찜한 장소에서 추가하기</h2>
          {wishItems === null ? (
            <p className="text-sm font-mono text-mist">불러오는 중...</p>
          ) : wishItems.length === 0 ? (
            <p className="text-sm font-mono text-mist">찜한 장소가 없습니다.</p>
          ) : (
            <ul className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {wishItems.map(item => (
                <li key={item.poiId} className="flex flex-col border border-harbor/10 p-3 hover:border-harbor/30 transition-colors">
                  <div className="flex justify-between items-start mb-2">
                    <span className="text-sm font-medium text-harbor truncate pr-2">{item.name}</span>
                    <span className="text-xs font-mono text-mist shrink-0">{item.category}</span>
                  </div>
                  <button
                    type="button"
                    onClick={() => handleAddPoi({ poiId: item.poiId })}
                    className="mt-auto py-1.5 w-full text-xs font-medium bg-harbor/5 text-harbor border border-harbor/10 hover:bg-harbor/10"
                  >
                    + {activeDay}일차에 추가
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </main>

      {addModalOpen && (
        <AddPoiModal onClose={() => setAddModalOpen(false)} onSelect={handleAddPoi} />
      )}

      {shareUrl && <ShareLinkModal url={shareUrl} onClose={() => setShareUrl(null)} />}

      {aiToast && (
        <div className="fixed bottom-6 left-1/2 -translate-x-1/2 max-w-md w-[90%] bg-harbor text-sand px-5 py-4 border border-sand/10">
          <div className="flex items-start justify-between gap-3">
            <div className="max-h-[50vh] overflow-y-auto pr-2">
              <p className="text-xs font-mono text-sunbeam mb-1">AI 추천 설명</p>
              <p className="text-sm leading-relaxed whitespace-pre-line">
                {aiToast.replace(/([^\n])\s*(첫째 날|둘째 날|셋째 날|넷째 날|다섯째 날|여섯째 날|일곱째 날|[1-9]일차)/g, '$1\n\n$2')}
              </p>
            </div>
            <button
              type="button"
              onClick={() => setAiToast(null)}
              aria-label="닫기"
              className="shrink-0 text-sand/60 hover:text-sand text-sm leading-none mt-0.5"
            >
              ✕
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
