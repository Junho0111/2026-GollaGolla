import { useState } from 'react'
import { reviewsApi } from '../api/reviews'

// 화면 4 — 리뷰 작성 모달 (정식 컴포넌트)
// 별점은 흔한 채워진 별 아이콘 그리드 대신, 모노스페이스 숫자 다이얼처럼 선택하게 해서
// "평가 도장을 찍는" 느낌으로 처리 (다른 카드류와 톤을 다르게)
export default function ReviewModal({ poiId, onClose, onCreated }) {
  const [rating, setRating] = useState(0)
  const [content, setContent] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [duplicateNotice, setDuplicateNotice] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    if (rating < 1) {
      setError('별점을 선택해 주세요.')
      return
    }
    if (!content.trim()) {
      setError('리뷰 내용을 입력해 주세요.')
      return
    }

    setSubmitting(true)
    try {
      const { data } = await reviewsApi.createReview(poiId, { rating, content: content.trim() })
      onCreated({ poiRatingUpdated: data.poiRatingUpdated })
    } catch (err) {
      const code = err?.response?.data?.code
      const message = err?.response?.data?.message || '리뷰 등록에 실패했습니다.'

      if (code === 'REVIEW_001') {
        // 이미 작성한 장소 — 안내 후 모달 자동 종료 (SCREENS.md 4번 스펙)
        setDuplicateNotice(true)
        setTimeout(onClose, 1400)
      } else {
        // COMMON_001(validation) 포함 — 메시지 그대로 노출
        setError(message)
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div
      className="fixed inset-0 z-20 bg-harbor/50 flex items-center justify-center px-5"
      onClick={onClose}
    >
      <form
        onClick={(e) => e.stopPropagation()}
        onSubmit={handleSubmit}
        className="bg-sand w-full max-w-sm border border-harbor/10"
      >
        <div className="px-6 pt-6 pb-5">
          <h3 className="text-lg font-semibold text-harbor">리뷰 작성</h3>
          <p className="text-xs text-mist mt-1 font-mono">방문 경험을 남겨주세요</p>
        </div>

        <div className="px-6">
          {duplicateNotice ? (
            <p className="text-sm text-coral pb-4">이미 리뷰를 작성한 장소입니다.</p>
          ) : (
            <>
              {error && <p className="text-sm text-coral mb-4">{error}</p>}

              <div className="mb-5">
                <label className="block text-sm text-mist mb-2">별점</label>
                <div className="flex gap-1.5">
                  {[1, 2, 3, 4, 5].map((n) => (
                    <button
                      key={n}
                      type="button"
                      onClick={() => setRating(n)}
                      aria-label={`${n}점`}
                      className={`w-9 h-9 border font-mono text-sm transition-colors ${
                        n <= rating
                          ? 'bg-coral border-coral text-sand'
                          : 'bg-transparent border-harbor/20 text-mist hover:border-harbor/40'
                      }`}
                    >
                      {n}
                    </button>
                  ))}
                </div>
              </div>

              <div className="mb-2">
                <label className="block text-sm text-mist mb-1.5" htmlFor="review-content">
                  내용
                </label>
                <textarea
                  id="review-content"
                  rows={4}
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="어떤 점이 좋았나요?"
                  className="w-full px-3 py-2 bg-white border border-harbor/15 focus:outline-none focus:border-seaglass resize-none placeholder:text-mist/50"
                />
              </div>
            </>
          )}
        </div>

        <div className="flex gap-2 justify-end px-6 py-5">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm text-mist hover:text-harbor"
          >
            취소
          </button>
          {!duplicateNotice && (
            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2 text-sm font-medium bg-coral text-sand disabled:opacity-50"
            >
              {submitting ? '등록 중' : '등록'}
            </button>
          )}
        </div>
      </form>
    </div>
  )
}
