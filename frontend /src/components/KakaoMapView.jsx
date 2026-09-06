import { useEffect, useRef } from 'react'
import { useKakaoMapScript } from '../hooks/useKakaoMapScript'

// markers: [{ lat, lng, title }]
export default function KakaoMapView({ markers, center, zoom = 3 }) {
  const status = useKakaoMapScript()
  const mapContainerRef = useRef(null)
  const mapInstanceRef = useRef(null)
  const markerInstancesRef = useRef([])

  const normalizedMarkers = (markers || []).filter((m) => m.lat != null && m.lng != null)
  const effectiveCenter = center || normalizedMarkers[0]

  useEffect(() => {
    if (status !== 'ready' || !mapContainerRef.current || !effectiveCenter) return

    try {
      const { kakao } = window
      if (!kakao || !kakao.maps) return

      const centerLatLng = new kakao.maps.LatLng(Number(effectiveCenter.lat), Number(effectiveCenter.lng))

      // 카카오맵 줌 레벨은 네이버와 다르며, 낮을수록 확대됨 (보통 3~4가 적당)
      const kakaoZoom = 4

      if (!mapInstanceRef.current) {
        mapInstanceRef.current = new kakao.maps.Map(mapContainerRef.current, {
          center: centerLatLng,
          level: kakaoZoom,
        })
        
        // 카카오맵은 relayout()을 통해 리사이즈 문제를 해결함
        requestAnimationFrame(() => {
          if (mapInstanceRef.current) {
            mapInstanceRef.current.relayout()
            mapInstanceRef.current.setCenter(centerLatLng)
          }
        })
      } else {
        mapInstanceRef.current.setCenter(centerLatLng)
      }

      // 기존 마커 정리
      markerInstancesRef.current.forEach((m) => m.setMap(null))
      
      // 새 마커 그리기
      markerInstancesRef.current = normalizedMarkers.map((m) => {
        const markerPosition = new kakao.maps.LatLng(Number(m.lat), Number(m.lng))
        const marker = new kakao.maps.Marker({
          position: markerPosition,
          title: m.title,
        })
        marker.setMap(mapInstanceRef.current)
        return marker
      })

      // 마커가 여러 개면 전부 보이도록 bounds 조정
      if (normalizedMarkers.length > 1) {
        const bounds = new kakao.maps.LatLngBounds()
        normalizedMarkers.forEach((m) => {
          bounds.extend(new kakao.maps.LatLng(Number(m.lat), Number(m.lng)))
        })
        mapInstanceRef.current.setBounds(bounds)
      }
    } catch (err) {
      console.error('Kakao Map rendering error:', err)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status, JSON.stringify(normalizedMarkers), effectiveCenter?.lat, effectiveCenter?.lng, zoom])

  if (status === 'no-key' || status === 'error' || !effectiveCenter) {
    return (
      <div className="aspect-[16/9] w-full flex flex-col items-center justify-center gap-2 bg-harbor/5 border border-harbor/10 text-mist">
        <span className="text-2xl">⛺</span>
        <p className="text-sm font-mono">지도를 불러올 수 없습니다</p>
      </div>
    )
  }

  return (
    <div className="relative aspect-[16/9] w-full bg-harbor/5">
      <div ref={mapContainerRef} className="absolute inset-0" />
      {status === 'loading' && (
        <div className="absolute inset-0 flex items-center justify-center text-mist text-sm font-mono">
          지도를 불러오는 중…
        </div>
      )}
    </div>
  )
}
