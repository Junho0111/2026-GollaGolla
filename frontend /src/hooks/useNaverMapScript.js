import { useEffect, useState } from 'react'

const NAVER_MAP_CLIENT_ID = import.meta.env.VITE_NAVER_MAP_CLIENT_ID
const SCRIPT_ID = 'naver-map-sdk'

// 모듈 스코프 싱글턴 프라미스 — 여러 컴포넌트가 동시에 훅을 써도
// <script> 태그와 SDK 로드는 앱 전체에서 단 한 번만 일어나게 한다.
let loadPromise = null

function loadNaverMapScript() {
  if (window.naver?.maps) return Promise.resolve()
  if (loadPromise) return loadPromise

  loadPromise = new Promise((resolve, reject) => {
    const existing = document.getElementById(SCRIPT_ID)
    if (existing) {
      if (window.naver?.maps) {
        resolve()
      } else {
        existing.addEventListener('load', () => resolve())
        existing.addEventListener('error', () => reject(new Error('naver map script error')))
      }
      return
    }

    const script = document.createElement('script')
    script.id = SCRIPT_ID
    script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=${NAVER_MAP_CLIENT_ID}`
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('naver map script error'))
    document.head.appendChild(script)
  }).catch((err) => {
    loadPromise = null // 실패 시 재시도 가능하도록 초기화
    throw err
  })

  return loadPromise
}

// 네이버 지도 SDK 로드 상태만 관리하는 훅.
// CLIENT_ID가 없으면 즉시 'no-key', 로드 실패 시 'error', 성공 시 'ready'.
// 지도/마커 렌더링은 이 훅을 쓰는 컴포넌트(NaverMapView 등)가 담당한다.
export function useNaverMapScript() {
  const [status, setStatus] = useState(() => {
    if (!NAVER_MAP_CLIENT_ID) return 'no-key'
    if (window.naver?.maps) return 'ready'
    return 'loading'
  })

  useEffect(() => {
    if (!NAVER_MAP_CLIENT_ID) {
      setStatus('no-key')
      return
    }
    if (window.naver?.maps) {
      setStatus('ready')
      return
    }

    let cancelled = false
    setStatus('loading')
    loadNaverMapScript()
      .then(() => {
        if (!cancelled) setStatus('ready')
      })
      .catch(() => {
        if (!cancelled) setStatus('error')
      })

    return () => {
      cancelled = true
    }
  }, [])

  return status // 'no-key' | 'loading' | 'ready' | 'error'
}
