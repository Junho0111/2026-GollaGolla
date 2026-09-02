package com.gollagolla.review.ui.dto;

import com.gollagolla.review.application.dto.ReviewItemDto;
import lombok.Getter;

import java.util.List;

@Getter
public class ReviewListResponse {

    private List<ReviewItemDto> content;
    private boolean hasNext;

    private ReviewListResponse() {
    }

    private ReviewListResponse(List<ReviewItemDto> content, boolean hasNext) {
        this.content = content;
        this.hasNext = hasNext;
    }

    public static ReviewListResponse of(List<ReviewItemDto> content, boolean hasNext) {
        return new ReviewListResponse(content, hasNext);
    }
}
