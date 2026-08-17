package com.gollagolla.review.ui.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateReviewRequest {

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점이어야 합니다.")
    @Max(value = 5, message = "평점은 최대 5점이어야 합니다.")
    private Integer rating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    private String content;

    private CreateReviewRequest() {
    }

    public CreateReviewRequest(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}
