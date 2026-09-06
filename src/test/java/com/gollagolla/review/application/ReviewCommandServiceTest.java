package com.gollagolla.review.application;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import com.gollagolla.review.application.dto.ReviewCreatedDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewCommandServiceTest {

    @InjectMocks
    private ReviewCommandService reviewCommandService;

    @Mock
    private ReviewCreateService reviewCreateService;

    @Test
    void 리뷰_작성_성공() {
        // given
        ReviewCreatedDto dto = ReviewCreatedDto.of(100L, new BigDecimal("5.0"));
        given(reviewCreateService.doCreateReview(anyLong(), anyLong(), anyInt(), anyString())).willReturn(dto);

        // when
        reviewCommandService.createReview(1L, 1L, 5, "좋아요");

        // then
        verify(reviewCreateService).doCreateReview(1L, 1L, 5, "좋아요");
    }

    @Test
    void 이미_리뷰가_존재하면_예외_발생() {
        // given
        given(reviewCreateService.doCreateReview(anyLong(), anyLong(), anyInt(), anyString()))
                .willThrow(new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS, "중복"));

        // when & then
        assertThatThrownBy(() -> reviewCommandService.createReview(1L, 1L, 5, "좋아요"))
                .isInstanceOf(BusinessException.class);
    }
}
