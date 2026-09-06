import { useState } from 'react'
import { searchApi } from '../api/search'
import { useDebouncedValue } from '../hooks/useDebouncedValue'
import { useEffect } from 'react'

const CATEGORY_LABEL = {
  HOTEL: '숙소',
  ATTRACTION: '관광지',
  ACTIVITY: '액티비티',
  RESTAURANT: '맛집',
  FESTIVAL: '축제',
}

// 일정 편집 화면에서 장소를 검색해 현재 일차에 추가하는 모달
export default function AddPoiModal({ onClose, onSelect }) {
  const [keyword, setKeyword] = useState('')
  const debounced = useDebouncedValue(keyword, 350)
  const [results, setResults] = useState(null)

  useEffect(() => {
    const q = debounced.trim()
    if (!q) {
      setResults(null)
      return
    }
    searchApi
      .searchPois(q)
      .then(({ data }) => setResults(data.results))
      .catch(() => setResults([]))
  }, [debounced])

  return (
    <div
      className="fixed inset-0 z-20 bg-harbor/50 flex items-center justify-center px-5"
      onClick={onClose}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="bg-sand w-full max-w-sm border border-harbor/10 max-h-[70vh] flex flex-col"
      >
        <div className="px-6 pt-6 pb-4">
          <h3 className="text-lg font-semibold text-harbor mb-3">장소 추가</h3>
          <input
            autoFocus
            type="search"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="장소 이름으로 검색"
            className="w-full px-3.5 py-2.5 bg-white border border-harbor/15 focus:outline-none focus:border-seaglass text-sm"
          />
        </div>

        <div className="flex-1 overflow-y-auto px-6 pb-6">
          {results === null && (
            <p className="text-xs text-mist font-mono py-4">검색어를 입력해 주세요.</p>
          )}
          {results && results.length === 0 && (
            <p className="text-xs text-mist font-mono py-4">검색 결과가 없습니다.</p>
          )}
          {results && results.length > 0 && (
            <ul className="divide-y divide-harbor/10">
              {results.map((r) => (
                <li key={r.poiId}>
                  <button
                    type="button"
                    onClick={() => onSelect(r)}
                    className="w-full text-left py-3 flex items-center justify-between gap-3 hover:bg-harbor/[0.03]"
                  >
                    <span className="text-sm text-harbor">{r.name}</span>
                    <span className="text-xs text-mist font-mono shrink-0">
                      {r.regionName} · {CATEGORY_LABEL[r.category] || r.category}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="flex justify-end px-6 py-4 border-t border-harbor/10">
          <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-mist">
            닫기
          </button>
        </div>
      </div>
    </div>
  )
}
