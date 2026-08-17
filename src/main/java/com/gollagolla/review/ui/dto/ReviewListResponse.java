package com.gollagolla.review.ui.dto;

import com.gollagolla.review.application.dto.ReviewItemDto;
import lombok.Getter;

import java.util.List;

@Getter
public class ReviewListResponse {

    private List<ReviewItemDto> content;

    private ReviewListResponse() {
    }

    private ReviewListResponse(List<ReviewItemDto> content) {
        this.content = content;
    }

    public static ReviewListResponse of(List<ReviewItemDto> content) {
        return new ReviewListResponse(content);
    }
}
