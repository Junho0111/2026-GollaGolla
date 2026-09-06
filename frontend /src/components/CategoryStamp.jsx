// 카테고리 필터 — 흔한 필 형태 칩 대신 우표/짐표(luggage tag) 느낌의 각진 스탬프
import { CATEGORIES } from '../api/pois'

export default function CategoryStamp({ selected, onSelect }) {
  return (
    <div className="flex flex-wrap gap-2">
      <button
        type="button"
        onClick={() => onSelect(null)}
        className={`px-3.5 py-1.5 text-sm font-mono border transition-colors ${
          selected === null
            ? 'bg-harbor text-sand border-harbor'
            : 'bg-transparent text-harbor border-harbor/25 hover:border-harbor/50'
        }`}
      >
        전체
      </button>
      {CATEGORIES.map((c) => (
        <button
          key={c.value}
          type="button"
          onClick={() => onSelect(c.value)}
          className={`px-3.5 py-1.5 text-sm font-mono border transition-colors ${
            selected === c.value
              ? 'bg-harbor text-sand border-harbor'
              : 'bg-transparent text-harbor border-harbor/25 hover:border-harbor/50'
          }`}
        >
          {c.label}
        </button>
      ))}
    </div>
  )
}
