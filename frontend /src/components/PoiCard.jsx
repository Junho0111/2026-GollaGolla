import { useNavigate } from 'react-router-dom'

// 카드 하나마다 정보 위계를 살짝 다르게(썸네일 크게, 평점은 모노스페이스 숫자 강조)
// 흔한 균일 그림자/라운드 대신 얇은 하단 보더 하나만 사용
export default function PoiCard({ poi, onToggleWish }) {
  const navigate = useNavigate()

  return (
    <div className="group border-b border-harbor/10 pb-4">
      <button
        type="button"
        onClick={() => navigate(`/pois/${poi.poiId}`)}
        className="block w-full text-left"
      >
        <div className="relative aspect-[4/3] bg-harbor/5 overflow-hidden mb-3">
          {poi.thumbnailUrl ? (
            <img
              src={poi.thumbnailUrl}
              alt={poi.name}
              className="w-full h-full object-cover"
              loading="lazy"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-mist text-xs font-mono">
              NO IMAGE
            </div>
          )}
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation()
              onToggleWish(poi)
            }}
            aria-label={poi.wished ? '찜 해제' : '찜하기'}
            className="absolute top-2 right-2 w-8 h-8 flex items-center justify-center bg-harbor/70 backdrop-blur-sm"
          >
            <span className={poi.wished ? 'text-coral' : 'text-sand'}>
              {poi.wished ? '♥' : '♡'}
            </span>
          </button>
        </div>

        <div className="flex items-start justify-between gap-2">
          <h3 className="text-harbor font-medium leading-snug">{poi.name}</h3>
          <span className="font-mono text-sm text-harbor shrink-0">{poi.rating?.toFixed(1)}</span>
        </div>
        <p className="mt-1 text-xs text-mist font-mono">
          리뷰 {poi.reviewCount} · 찜 {poi.wishCount}
        </p>
      </button>
    </div>
  )
}
