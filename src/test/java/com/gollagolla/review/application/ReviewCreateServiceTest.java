package com.gollagolla.review.application;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import com.gollagolla.poi.domain.DataSource;
import com.gollagolla.poi.domain.Poi;
import com.gollagolla.poi.domain.PoiCategory;
import com.gollagolla.poi.domain.PoiRepository;
import com.gollagolla.review.application.dto.ReviewCreatedDto;
import com.gollagolla.review.domain.Review;
import com.gollagolla.review.domain.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
class ReviewCreateServiceTest {

    @InjectMocks
    private ReviewCreateService reviewCreateService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PoiRepository poiRepository;

    @Test
    void 리뷰_생성_성공_저장_및_Poi_평점_반영() {
        // given
        Poi poi = Poi.builder()
                .regionId(1L)
                .category(PoiCategory.ATTRACTION)
                .name("테스트 장소")
                .source(DataSource.INTERNAL)
                .build();

        Review review = Review.builder()
                .poiId(1L)
                .memberId(1L)
                .rating(5)
                .content("좋아요")
                .build();
        setField(review, "id", 1L);

        given(reviewRepository.findByPoiIdAndMemberId(1L, 1L)).willReturn(Optional.empty());
        given(reviewRepository.save(any(Review.class))).willReturn(review);
        given(poiRepository.findById(1L)).willReturn(Optional.of(poi));

        // when
        ReviewCreatedDto result = reviewCreateService.doCreateReview(1L, 1L, 5, "좋아요");

        // then
        assertThat(result.getReviewId()).isEqualTo(1L);
        verify(reviewRepository).save(any(Review.class));
        verify(poiRepository).findById(1L);
    }

    @Test
    void 이미_리뷰가_존재하면_REVIEW_ALREADY_EXISTS_예외_발생() {
        // given
        Review existingReview = Review.builder()
                .poiId(1L)
                .memberId(1L)
                .rating(5)
                .content("좋아요")
                .build();
        given(reviewRepository.findByPoiIdAndMemberId(1L, 1L)).willReturn(Optional.of(existingReview));

        // when & then
        assertThatThrownBy(() -> reviewCreateService.doCreateReview(1L, 1L, 5, "좋아요"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_ALREADY_EXISTS);
    }

    @Test
    void DB_UNIQUE_제약_위반_시_REVIEW_ALREADY_EXISTS_예외_발생() {
        // given
        given(reviewRepository.findByPoiIdAndMemberId(1L, 1L)).willReturn(Optional.empty());
        given(reviewRepository.save(any(Review.class))).willThrow(new DataIntegrityViolationException("unique violation"));

        // when & then
        assertThatThrownBy(() -> reviewCreateService.doCreateReview(1L, 1L, 5, "좋아요"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_ALREADY_EXISTS);
    }

    @Test
    void Poi가_존재하지_않으면_POI_NOT_FOUND_예외_발생() {
        // given
        Review review = Review.builder().
                poiId(1L)
                .memberId(1L)
                .rating(5)
                .content("좋아요")
                .build();
        setField(review, "id", 1L);

        given(reviewRepository.findByPoiIdAndMemberId(1L, 1L)).willReturn(Optional.empty());
        given(reviewRepository.save(any(Review.class))).willReturn(review);
        given(poiRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewCreateService.doCreateReview(1L, 1L, 5, "좋아요"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POI_NOT_FOUND);
    }
}
