package com.gollagolla.review.application.dto;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ReviewItemDto {

    private Long reviewId;
    private Long memberId;
    private String nickname;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    private ReviewItemDto() {
    }

    private ReviewItemDto(Long reviewId, Long memberId, String nickname, Integer rating,
                          String content, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.memberId = memberId;
        this.nickname = nickname;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ReviewItemDto of(Long reviewId, Long memberId, String nickname, Integer rating,
                                   String content, LocalDateTime createdAt
    ) {
        return new ReviewItemDto(reviewId, memberId, nickname, rating, content, createdAt);
    }
}
