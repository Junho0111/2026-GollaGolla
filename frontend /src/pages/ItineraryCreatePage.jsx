import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { itinerariesApi } from '../api/itineraries'
import { wishlistApi } from '../api/wishlist'
import AppHeader from '../components/AppHeader'
import RegionSelect from '../components/RegionSelect'

const TRANSPORT_OPTIONS = [
  { value: 'CAR', label: '자동차' },
  { value: 'WALK', label: '도보' },
  { value: 'TRANSIT', label: '대중교통' },
]

// 화면 7 — 일정 생성
// "직접 만들기"/"AI로 만들기"는 로그인 화면의 밑줄 탭과 다른 톤을 주기 위해
// 나란히 놓인 두 개의 큰 선택 패널(탑승 등급을 고르는 느낌)로 구성
export default function ItineraryCreatePage() {
  const navigate = useNavigate()

  const [mode, setMode] = useState('MANUAL') // 'MANUAL' | 'AI'
  const [title, setTitle] = useState('')
  const [regionId, setRegionId] = useState(null)
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [transportMode, setTransportMode] = useState('CAR')

  // 찜 목록은 직접/AI 두 모드 모두에서 사용 — 최초 진입 시 한 번만 로드
  const [wishItems, setWishItems] = useState(null)
  const [selectedPoiIds, setSelectedPoiIds] = useState([])

  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  useEffect(() => {
    if (wishItems === null) {
      wishlistApi.getWishlist().then(({ data }) => setWishItems(data))
    }
  }, [wishItems])

  function parseValidationMessage(message = '') {
    const map = {}
    message.split(',').forEach((part) => {
      const [field, ...rest] = part.split(':')
      if (field && rest.length) map[field.trim()] = rest.join(':').trim()
    })
    return map
  }

  function handleApiError(err) {
    const code = err?.response?.data?.code
    const message = err?.response?.data?.message || '일정 생성에 실패했습니다.'
    if (code === 'COMMON_001') {
      setFieldErrors(parseValidationMessage(message))
    } else {
      // DOMAIN_001(직접), POI_001(AI) 등
      setFormError(message)
    }
  }

  function togglePoi(poiId) {
    setSelectedPoiIds((prev) =>
      prev.includes(poiId) ? prev.filter((id) => id !== poiId) : [...prev, poiId]
    )
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setFormError('')
    setFieldErrors({})

    if (mode === 'AI' && selectedPoiIds.length === 0) {
      setFormError('AI로 만들려면 찜 목록에서 장소를 1개 이상 선택해 주세요.')
      return
    }

    setSubmitting(true)
    try {
      if (mode === 'MANUAL') {
        const { data } = await itinerariesApi.createItinerary({
          title,
          regionId,
          startDate,
          endDate,
          transportMode,
        })

        // 찜 목록에서 선택한 장소가 있으면, 1일차에 seq 순서대로 한 번에 담아둔다.
        // (며칠차에 넣을지·순서는 이후 편집 화면에서 사용자가 다시 조정)
        if (selectedPoiIds.length > 0) {
          const items = selectedPoiIds.map((poiId, idx) => ({
            poiId,
            dayNo: 1,
            seq: idx,
            isAnchor: false,
          }))
          try {
            await itinerariesApi.addItemsBulk(data.itineraryId, items)
          } catch {
            // 담기에 실패해도 일정 자체는 이미 생성됐으므로, 편집 화면으로 그대로 이동시켜
            // 사용자가 편집 화면에서 직접 장소를 추가하도록 한다.
          }
        }

        navigate(`/itineraries/${data.itineraryId}`, { state: { dateRange: { startDate, endDate } } })
      } else {
        const { data } = await itinerariesApi.createAiItinerary({
          title,
          regionId,
          startDate,
          endDate,
          transportMode,
          poiIds: selectedPoiIds,
        })
        // aiExplanation은 이 전환 시점에만 노출 가능 (재조회 불가) — 다음 화면으로 state 전달
        navigate(`/itineraries/${data.itinerary.itineraryId}`, {
          state: { aiExplanation: data.aiExplanation, dateRange: { startDate, endDate } },
        })
      }
    } catch (err) {
      handleApiError(err)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-sand">
      <AppHeader />

      <main className="max-w-2xl mx-auto px-5 py-8">
        <h1 className="text-2xl font-semibold text-harbor mb-6">새 일정</h1>

        {/* 생성 방식 선택 패널 */}
        <div className="grid grid-cols-2 gap-3 mb-8">
          <button
            type="button"
            onClick={() => setMode('MANUAL')}
            className={`text-left px-5 py-4 border transition-colors ${
              mode === 'MANUAL' ? 'border-harbor bg-harbor text-sand' : 'border-harbor/20 text-harbor'
            }`}
          >
            <p className="font-mono text-[11px] tracking-widest opacity-70">MANUAL</p>
            <p className="font-medium mt-1">직접 만들기</p>
            <p className={`text-xs mt-1 ${mode === 'MANUAL' ? 'text-sand/70' : 'text-mist'}`}>
              빈 일정을 만들고 하나씩 채워요
            </p>
          </button>
          <button
            type="button"
            onClick={() => setMode('AI')}
            className={`text-left px-5 py-4 border transition-colors ${
              mode === 'AI' ? 'border-harbor bg-harbor text-sand' : 'border-harbor/20 text-harbor'
            }`}
          >
            <p className="font-mono text-[11px] tracking-widest opacity-70">AI</p>
            <p className="font-medium mt-1">AI로 만들기</p>
            <p className={`text-xs mt-1 ${mode === 'AI' ? 'text-sand/70' : 'text-mist'}`}>
              찜한 장소로 동선을 자동 배치해요
            </p>
          </button>
        </div>

        {formError && (
          <div className="mb-5 px-4 py-3 bg-coral/10 border-l-2 border-coral text-sm text-harbor">
            {formError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          <div>
            <label className="block text-sm text-mist mb-1.5" htmlFor="title">
              제목
            </label>
            <input
              id="title"
              type="text"
              required
              maxLength={100}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="예) 서울 3박 4일"
              className="w-full px-3.5 py-2.5 bg-white border border-harbor/15 focus:outline-none focus:border-seaglass"
            />
            {fieldErrors.title && <p className="mt-1 text-xs text-coral">{fieldErrors.title}</p>}
          </div>

          <div>
            <label className="block text-sm text-mist mb-1.5">지역</label>
            <RegionSelect regionId={regionId} onChange={setRegionId} />
            {fieldErrors.regionId && (
              <p className="mt-1 text-xs text-coral">{fieldErrors.regionId}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm text-mist mb-1.5" htmlFor="startDate">
                시작일
              </label>
              <input
                id="startDate"
                type="date"
                required
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-white border border-harbor/15 focus:outline-none focus:border-seaglass font-mono text-sm"
              />
            </div>
            <div>
              <label className="block text-sm text-mist mb-1.5" htmlFor="endDate">
                종료일
              </label>
              <input
                id="endDate"
                type="date"
                required
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-white border border-harbor/15 focus:outline-none focus:border-seaglass font-mono text-sm"
              />
            </div>
          </div>
          {fieldErrors.startDate && <p className="text-xs text-coral -mt-3">{fieldErrors.startDate}</p>}
          {fieldErrors.endDate && <p className="text-xs text-coral -mt-3">{fieldErrors.endDate}</p>}

          <div>
            <label className="block text-sm text-mist mb-1.5">이동 수단</label>
            <div className="flex gap-2">
              {TRANSPORT_OPTIONS.map((opt) => (
                <button
                  key={opt.value}
                  type="button"
                  onClick={() => setTransportMode(opt.value)}
                  className={`px-3.5 py-1.5 text-sm font-mono border transition-colors ${
                    transportMode === opt.value
                      ? 'bg-seaglass border-seaglass text-sand'
                      : 'bg-transparent border-harbor/20 text-harbor'
                  }`}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>

          {mode === 'AI' && (
            <div>
              <label className="block text-sm text-mist mb-1.5">
                찜한 장소에서 선택 ({selectedPoiIds.length}개 선택됨)
              </label>
              {wishItems === null && (
                <p className="text-xs text-mist font-mono py-3">불러오는 중…</p>
              )}
              {wishItems && wishItems.length === 0 && (
                <div className="flex flex-col items-start gap-3 py-4">
                  <p className="text-xs text-mist font-mono">
                    찜한 장소가 없습니다. 먼저 장소를 찜해 주세요.
                  </p>
                  <Link
                    to="/wishlist"
                    className="px-4 py-2 text-sm font-medium bg-harbor text-sand"
                  >
                    찜하러 가기
                  </Link>
                </div>
              )}
              {wishItems && wishItems.length > 0 && (
                <ul className="border border-harbor/10 divide-y divide-harbor/10 max-h-72 overflow-y-auto">
                  {wishItems.map((item) => (
                    <li key={item.poiId}>
                      <label className="flex items-center gap-3 px-3.5 py-2.5 cursor-pointer hover:bg-harbor/[0.03]">
                        <input
                          type="checkbox"
                          checked={selectedPoiIds.includes(item.poiId)}
                          onChange={() => togglePoi(item.poiId)}
                          className="accent-coral"
                        />
                        <span className="text-sm text-harbor">{item.name}</span>
                        <span className="text-xs text-mist font-mono ml-auto">{item.category}</span>
                      </label>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}

          <div className="flex flex-col items-center mt-2 w-full">
            <button
              type="submit"
              disabled={submitting}
              className="w-full py-3 bg-coral text-sand font-medium disabled:opacity-50"
            >
              {submitting ? '만드는 중...' : mode === 'MANUAL' ? '일정 만들기' : 'AI로 일정 만들기'}
            </button>
            {submitting && mode === 'AI' && (
              <p className="mt-3 text-sm text-harbor font-medium animate-pulse">
                최적의 동선을 계산하고 있습니다. 시간이 조금 걸릴 수 있습니다... 🤖
              </p>
            )}
          </div>
        </form>
      </main>
    </div>
  )
}
