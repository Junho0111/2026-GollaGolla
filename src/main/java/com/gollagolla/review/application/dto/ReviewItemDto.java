package com.gollagolla.review.application.dto;

import lombok.Getter;

@Getter
public class ReviewItemDto {

    private Long reviewId;
    private String nickname;
    private Integer rating;
    private String content;

    private ReviewItemDto() {
    }

    private ReviewItemDto(Long reviewId, String nickname, Integer rating, String content) {
        this.reviewId = reviewId;
        this.nickname = nickname;
        this.rating = rating;
        this.content = content;
    }

    public static ReviewItemDto of(Long reviewId, String nickname, Integer rating, String content) {
        return new ReviewItemDto(reviewId, nickname, rating, content);
    }
}
