import { useEffect, useRef } from 'react'
import { useNaverMapScript } from '../hooks/useNaverMapScript'

// 여러 마커를 받을 수 있는 형태로 설계 — 지금은 POI 상세에서 1개만 넘기지만,
// 일정 편집 화면에서 여러 장소를 한 지도에 표시할 때도 이 컴포넌트를 그대로 재사용 가능.
// markers: [{ lat, lng, title }]
export default function NaverMapView({ markers, center, zoom = 15 }) {
  const status = useNaverMapScript()
  const mapContainerRef = useRef(null)
  const mapInstanceRef = useRef(null)
  const markerInstancesRef = useRef([])

  const normalizedMarkers = (markers || []).filter((m) => m.lat != null && m.lng != null)
  const effectiveCenter = center || normalizedMarkers[0]

  useEffect(() => {
    console.log('map effect run:', { status, hasContainer: !!mapContainerRef.current, effectiveCenter })
    if (status !== 'ready' || !mapContainerRef.current || !effectiveCenter) return

    try {
      const { naver } = window
      if (!naver || !naver.maps) return // 네이버 객체가 완전히 로드되지 않은 경우 방어

      const centerLatLng = new naver.maps.LatLng(Number(effectiveCenter.lat), Number(effectiveCenter.lng))

      // 지도 인스턴스는 컨테이너당 한 번만 생성하고, 이후엔 center/마커만 갱신
      if (!mapInstanceRef.current) {
        mapInstanceRef.current = new naver.maps.Map(mapContainerRef.current, {
          center: centerLatLng,
          zoom,
        })
        // 컨테이너 크기가 생성 시점에 불안정한 경우 대비 — 강제로 리사이즈 트리거
        requestAnimationFrame(() => {
          if (mapInstanceRef.current) {
            naver.maps.Event.trigger(mapInstanceRef.current, 'resize')
            mapInstanceRef.current.setCenter(centerLatLng)
          }
        })
      } else {
        mapInstanceRef.current.setCenter(centerLatLng)
      }

      // 기존 마커 정리 후 새로 그리기
      markerInstancesRef.current.forEach((m) => m.setMap(null))
      markerInstancesRef.current = normalizedMarkers.map(
        (m) =>
          new naver.maps.Marker({
            position: new naver.maps.LatLng(Number(m.lat), Number(m.lng)),
            map: mapInstanceRef.current,
            title: m.title,
          })
      )

      // 마커가 여러 개면 전부 보이도록 뷰포트 조정
      if (normalizedMarkers.length > 1) {
        const bounds = new naver.maps.LatLngBounds(centerLatLng, centerLatLng)
        normalizedMarkers.forEach((m) => bounds.extend(new naver.maps.LatLng(Number(m.lat), Number(m.lng))))
        mapInstanceRef.current.fitBounds(bounds)
      }
    } catch (err) {
      console.error('Naver Map rendering error:', err)
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
