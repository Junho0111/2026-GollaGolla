import { useEffect, useState } from 'react'

const KAKAO_MAP_API_KEY = import.meta.env.VITE_KAKAO_MAP_API_KEY
const SCRIPT_ID = 'kakao-map-sdk'

let loadPromise = null

function loadKakaoMapScript() {
  if (window.kakao?.maps) return Promise.resolve()
  if (loadPromise) return loadPromise

  loadPromise = new Promise((resolve, reject) => {
    const existing = document.getElementById(SCRIPT_ID)
    if (existing) {
      if (window.kakao?.maps) {
        resolve()
      } else {
        existing.addEventListener('load', () => window.kakao.maps.load(resolve))
        existing.addEventListener('error', () => reject(new Error('kakao map script error')))
      }
      return
    }

    const script = document.createElement('script')
    script.id = SCRIPT_ID
    // React에서 비동기로 불러올 때는 autoload=false 파라미터가 필수입니다.
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_MAP_API_KEY}&autoload=false`
    script.async = true
    script.onload = () => {
      window.kakao.maps.load(resolve)
    }
    script.onerror = () => reject(new Error('kakao map script error'))
    document.head.appendChild(script)
  }).catch((err) => {
    loadPromise = null
    throw err
  })

  return loadPromise
}

export function useKakaoMapScript() {
  const [status, setStatus] = useState(() => {
    if (!KAKAO_MAP_API_KEY) return 'no-key'
    if (window.kakao?.maps) return 'ready'
    return 'loading'
  })

  useEffect(() => {
    if (!KAKAO_MAP_API_KEY) {
      setStatus('no-key')
      return
    }
    if (window.kakao?.maps) {
      setStatus('ready')
      return
    }

    let cancelled = false
    setStatus('loading')
    loadKakaoMapScript()
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
