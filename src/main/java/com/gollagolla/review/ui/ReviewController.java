package com.gollagolla.review.ui;

import com.gollagolla.review.application.ReviewCommandService;
import com.gollagolla.review.application.ReviewQueryService;
import com.gollagolla.review.application.dto.ReviewCreatedDto;
import com.gollagolla.review.application.dto.ReviewItemDto;
import com.gollagolla.review.ui.dto.CreateReviewRequest;
import com.gollagolla.review.ui.dto.CreateReviewResponse;
import com.gollagolla.review.ui.dto.ReviewListResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pois/{poiId}/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ReviewListResponse getReviews(
            @PathVariable Long poiId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.") @Max(value = 100, message = "size는 100 이하이어야 합니다.") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        List<ReviewItemDto> items = reviewQueryService.getReviews(poiId, pageable);
        return ReviewListResponse.of(items);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateReviewResponse createReview(
            @PathVariable Long poiId,
            @RequestBody @Valid CreateReviewRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        ReviewCreatedDto dto = reviewCommandService.createReview(poiId, memberId, request.getRating(), request.getContent());
        return CreateReviewResponse.of(dto.getReviewId(), dto.getPoiRatingUpdated());
    }
}
