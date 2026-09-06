package com.gollagolla.review.application;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import com.gollagolla.poi.domain.Poi;
import com.gollagolla.poi.domain.PoiRepository;
import com.gollagolla.review.application.dto.ReviewCreatedDto;
import com.gollagolla.review.domain.Review;
import com.gollagolla.review.domain.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewCreateService {

    private final ReviewRepository reviewRepository;
    private final PoiRepository poiRepository;

    @Transactional
    public ReviewCreatedDto doCreateReview(Long poiId, Long memberId, Integer rating, String content) {
        if (reviewRepository.findByPoiIdAndMemberId(poiId, memberId).isPresent()) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS,
                    "poiId=" + poiId + ", memberId=" + memberId);
        }

        Review review;
        try {
            review = reviewRepository.save(Review.builder()
                    .poiId(poiId)
                    .memberId(memberId)
                    .rating(rating)
                    .content(content)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS,
                    "DB unique constraint violation: poiId=" + poiId + ", memberId=" + memberId);
        }

        Poi poi = poiRepository.findById(poiId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POI_NOT_FOUND, "poiId=" + poiId));

        poi.applyRating(rating);

        return ReviewCreatedDto.of(review.getId(), poi.getRating().getScore());
    }
}
