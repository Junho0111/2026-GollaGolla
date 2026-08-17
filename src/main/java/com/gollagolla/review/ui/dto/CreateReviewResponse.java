package com.gollagolla.review.ui.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateReviewResponse {

    private Long reviewId;
    private BigDecimal poiRatingUpdated;

    private CreateReviewResponse() {
    }

    private CreateReviewResponse(Long reviewId, BigDecimal poiRatingUpdated) {
        this.reviewId = reviewId;
        this.poiRatingUpdated = poiRatingUpdated;
    }

    public static CreateReviewResponse of(Long reviewId, BigDecimal poiRatingUpdated) {
        return new CreateReviewResponse(reviewId, poiRatingUpdated);
    }
}
