package com.gollagolla.wishlist.application;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import com.gollagolla.poi.domain.Poi;
import com.gollagolla.poi.domain.PoiRepository;
import com.gollagolla.wishlist.application.dto.WishlistItemDto;
import com.gollagolla.wishlist.domain.Wishlist;
import com.gollagolla.wishlist.domain.WishlistRepository;
import com.gollagolla.wishlist.domain.event.WishlistCreatedEvent;
import com.gollagolla.wishlist.domain.event.WishlistDeletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @InjectMocks
    private WishlistService wishlistService;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private PoiRepository poiRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void 내_찜_목록을_조회() {
        // given
        Wishlist wishlist = Wishlist.builder()
                .memberId(1L)
                .poiId(10L)
                .isPublic(true)
                .build();
        Poi poi = mock(Poi.class);
        given(poi.getId()).willReturn(10L);
        given(poi.getName()).willReturn("테스트 장소");

        given(wishlistRepository.findByMemberId(1L)).willReturn(List.of(wishlist));
        given(poiRepository.findAllById(List.of(10L))).willReturn(List.of(poi));

        // when
        List<WishlistItemDto> result = wishlistService.getMyWishlists(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPoiId()).isEqualTo(10L);
        assertThat(result.get(0).getName()).isEqualTo("테스트 장소");
        assertThat(result.get(0).getIsPublic()).isTrue();
    }

    @Test
    void 찜_추가() {
        // given
        given(poiRepository.existsById(10L)).willReturn(true);
        given(wishlistRepository.findByMemberIdAndPoiId(1L, 10L)).willReturn(Optional.empty());

        // when
        wishlistService.createWishlist(1L, 10L, true);

        // then
        verify(wishlistRepository).save(any(Wishlist.class));
        verify(eventPublisher).publishEvent(any(WishlistCreatedEvent.class));
    }

    @Test
    void 찜_추가_시_존재하지_않는_POI면_예외_발생() {
        // given
        given(poiRepository.existsById(10L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> wishlistService.createWishlist(1L, 10L, true))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POI_NOT_FOUND);
    }

    @Test
    void 이미_찜한_장소를_다시_추가하면_예외_발생() {
        // given
        given(poiRepository.existsById(10L)).willReturn(true);
        given(wishlistRepository.findByMemberIdAndPoiId(1L, 10L)).willReturn(Optional.of(mock(Wishlist.class)));

        // when & then
        assertThatThrownBy(() -> wishlistService.createWishlist(1L, 10L, true))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WISHLIST_ALREADY_EXISTS);
    }

    @Test
    void 찜_공개_여부를_수정() {
        // given
        Wishlist wishlist = Wishlist.builder()
                .memberId(1L)
                .poiId(10L)
                .isPublic(true)
                .build();
        given(wishlistRepository.findByMemberIdAndPoiId(1L, 10L)).willReturn(Optional.of(wishlist));

        // when
        wishlistService.updateVisibility(1L, 10L, false);

        // then
        assertThat(wishlist.getIsPublic()).isFalse();
    }

    @Test
    void 존재하지_않는_찜의_공개_여부를_수정_시_예외가발생() {
        // given
        given(wishlistRepository.findByMemberIdAndPoiId(1L, 10L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishlistService.updateVisibility(1L, 10L, false))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WISHLIST_NOT_FOUND);
    }

    @Test
    void 찜_삭제() {
        // given
        Wishlist wishlist = mock(Wishlist.class);
        given(wishlistRepository.findByMemberIdAndPoiId(1L, 10L)).willReturn(Optional.of(wishlist));

        // when
        wishlistService.deleteWishlist(1L, 10L);

        // then
        verify(wishlistRepository).delete(wishlist);
        verify(eventPublisher).publishEvent(any(WishlistDeletedEvent.class));
    }

    @Test
    void 존재하지_않는_찜을_삭제_시_예외_발생() {
        // given
        given(wishlistRepository.findByMemberIdAndPoiId(1L, 10L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishlistService.deleteWishlist(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WISHLIST_NOT_FOUND);
    }
}
