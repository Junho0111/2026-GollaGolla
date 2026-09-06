import { useState } from 'react'

// 공유 링크 생성 후 URL을 보여주고 복사할 수 있는 모달
export default function ShareLinkModal({ url, onClose }) {
  const [copied, setCopied] = useState(false)

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(url)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      // 클립보드 API 미지원 브라우저 — 조용히 무시, 사용자가 직접 드래그 선택 가능
    }
  }

  return (
    <div
      className="fixed inset-0 z-20 bg-harbor/50 flex items-center justify-center px-5"
      onClick={onClose}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="bg-sand w-full max-w-sm border border-harbor/10 px-6 py-6"
      >
        <h3 className="text-lg font-semibold text-harbor mb-1">공유 링크</h3>
        <p className="text-xs text-mist font-mono mb-4">
          이 링크는 만료되지 않습니다. 링크를 아는 누구나 조회할 수 있어요.
        </p>
        <div className="flex gap-2">
          <input
            readOnly
            value={url}
            onFocus={(e) => e.target.select()}
            className="flex-1 px-3 py-2 bg-white border border-harbor/15 text-xs font-mono text-harbor"
          />
          <button
            type="button"
            onClick={handleCopy}
            className="shrink-0 px-4 py-2 text-sm font-medium bg-coral text-sand"
          >
            {copied ? '복사됨' : '복사'}
          </button>
        </div>
        <div className="flex justify-end mt-5">
          <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-mist">
            닫기
          </button>
        </div>
      </div>
    </div>
  )
}
