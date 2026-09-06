import { useEffect, useState } from 'react'
import { regionsApi } from '../api/regions'

// 계층형 지역 선택 — 상위 선택 시 parentId로 하위 지역을 재조회
export default function RegionSelect({ regionId, onChange }) {
  const [topRegions, setTopRegions] = useState([])
  const [subRegions, setSubRegions] = useState([])
  const [selectedTop, setSelectedTop] = useState('')
  const [selectedSub, setSelectedSub] = useState('')

  useEffect(() => {
    regionsApi.getRegions({ depth: 1 }).then(({ data }) => setTopRegions(data))
  }, [])

  useEffect(() => {
    if (!selectedTop) {
      setSubRegions([])
      setSelectedSub('')
      return
    }
    regionsApi.getRegions({ parentId: selectedTop }).then(({ data }) => setSubRegions(data))
    setSelectedSub('')
  }, [selectedTop])

  function handleTopChange(e) {
    const value = e.target.value
    setSelectedTop(value)
    // 상위만 고른 상태 = 그 지역 전체로 필터링
    onChange(value || null)
  }

  function handleSubChange(e) {
    const value = e.target.value
    setSelectedSub(value)
    onChange(value || selectedTop || null)
  }

  return (
    <div className="flex gap-2">
      <select
        value={selectedTop}
        onChange={handleTopChange}
        className="px-3 py-2 bg-white border border-harbor/15 text-sm text-harbor focus:outline-none focus:border-seaglass"
      >
        <option value="">지역 전체</option>
        {topRegions.map((r) => (
          <option key={r.regionId} value={r.regionId}>
            {r.name}
          </option>
        ))}
      </select>

      {subRegions.length > 0 && (
        <select
          value={selectedSub}
          onChange={handleSubChange}
          className="px-3 py-2 bg-white border border-harbor/15 text-sm text-harbor focus:outline-none focus:border-seaglass"
        >
          <option value="">전체</option>
          {subRegions.map((r) => (
            <option key={r.regionId} value={r.regionId}>
              {r.name}
            </option>
          ))}
        </select>
      )}
    </div>
  )
}
