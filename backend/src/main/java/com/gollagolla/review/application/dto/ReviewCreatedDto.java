package com.gollagolla.review.application.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ReviewCreatedDto {

    private Long reviewId;
    private BigDecimal poiRatingUpdated;

    private ReviewCreatedDto() {
    }

    private ReviewCreatedDto(Long reviewId, BigDecimal poiRatingUpdated) {
        this.reviewId = reviewId;
        this.poiRatingUpdated = poiRatingUpdated;
    }

    public static ReviewCreatedDto of(Long reviewId, BigDecimal poiRatingUpdated) {
        return new ReviewCreatedDto(reviewId, poiRatingUpdated);
    }
}
