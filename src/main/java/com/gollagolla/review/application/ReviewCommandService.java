package com.gollagolla.review.application;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import com.gollagolla.review.application.dto.ReviewCreatedDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewCommandService {

    private static final int MAX_RETRY = 3;

    private final ReviewCreateService reviewCreateService;

    public ReviewCreatedDto createReview(Long poiId, Long memberId, Integer rating, String content) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            try {
                return reviewCreateService.doCreateReview(poiId, memberId, rating, content);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("[ReviewCommandService] 낙관적 락 충돌, 재시도 {}/{}: poiId={}", attempt + 1, MAX_RETRY, poiId);
            }
        }

        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                "리뷰 저장 중 동시성 충돌 — 재시도 " + MAX_RETRY + "회 초과: poiId=" + poiId);
    }
}
