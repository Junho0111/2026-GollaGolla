package com.gollagolla.review.application;

import com.gollagolla.member.domain.Member;
import com.gollagolla.member.domain.MemberRepository;
import com.gollagolla.review.application.dto.ReviewItemDto;
import com.gollagolla.review.domain.Review;
import com.gollagolla.review.domain.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
class ReviewQueryServiceTest {

    @InjectMocks
    private ReviewQueryService reviewQueryService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void 리뷰_목록_페이징_조회_성공() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        Review review = Review.builder()
                .poiId(1L)
                .memberId(1L)
                .rating(5).
                content("좋아요")
                .build();
        setField(review, "id", 1L);
        Page<Review> page = new PageImpl<>(List.of(review));

        Member member = Member.builder()
                .nickname("테스트유저")
                .build();
        setField(member, "id", 1L);

        given(reviewRepository.findByPoiId(any(), any())).willReturn(page);
        given(memberRepository.findAllById(any())).willReturn(List.of(member));

        // when
        org.springframework.data.domain.Slice<ReviewItemDto> results = reviewQueryService.getReviews(1L, pageable);

        // then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getNickname()).isEqualTo("테스트유저");
    }

    @Test
    void 탈퇴한_회원의_리뷰_조회_시_닉네임은_알_수_없음으로_표시() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        Review review = Review.builder()
                .poiId(1L)
                .memberId(99L) // 99L 회원은 탈퇴하였지만 정책상 리뷰는 남은 상태
                .rating(4)
                .content("좋아요")
                .build();
        setField(review, "id", 1L);
        Page<Review> page = new PageImpl<>(List.of(review));

        given(reviewRepository.findByPoiId(any(), any())).willReturn(page);
        given(memberRepository.findAllById(any())).willReturn(List.of()); // 탈퇴로 인해 조회 결과 없음

        // when
        org.springframework.data.domain.Slice<ReviewItemDto> results = reviewQueryService.getReviews(1L, pageable);

        // then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getNickname()).isEqualTo("알 수 없음");
    }
}
