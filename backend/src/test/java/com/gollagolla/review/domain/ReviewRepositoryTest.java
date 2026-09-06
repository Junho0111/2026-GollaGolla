package com.gollagolla.review.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void 회원_Id와_Poi_Id로_리뷰를_조회() {
        // given
        Review review = Review.builder()
                .memberId(1L)
                .poiId(10L)
                .rating(5)
                .content("좋아요")
                .build();
        reviewRepository.save(review);

        // when
        Optional<Review> findReview = reviewRepository.findByPoiIdAndMemberId(10L, 1L);

        // then
        assertThat(findReview).isPresent();
        assertThat(findReview.get().getRating()).isEqualTo(5);
    }
}
