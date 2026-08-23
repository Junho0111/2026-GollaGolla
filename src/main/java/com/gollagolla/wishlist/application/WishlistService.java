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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final PoiRepository poiRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<WishlistItemDto> getMyWishlists(Long memberId) {
        List<Wishlist> wishlists = wishlistRepository.findByMemberId(memberId);
        
        List<Long> poiIds = wishlists.stream()
                .map(Wishlist::getPoiId)
                .toList();

        Map<Long, String> poiNameMap = poiRepository.findAllById(poiIds).stream()
                .collect(Collectors.toMap(Poi::getId, Poi::getName));

        return wishlists.stream()
                .map(wishlist -> WishlistItemDto.of(
                        wishlist.getPoiId(),
                        poiNameMap.getOrDefault(wishlist.getPoiId(), "알 수 없는 장소"),
                        wishlist.getIsPublic()
                ))
                .toList();
    }

    @Transactional
    public void createWishlist(Long memberId, Long poiId, Boolean isPublic) {
        // 존재 여부 확인 (POI)
        if (!poiRepository.existsById(poiId)) {
            throw new BusinessException(ErrorCode.POI_NOT_FOUND, "poiId=" + poiId);
        }

        // 중복 체크
        if (wishlistRepository.findByMemberIdAndPoiId(memberId, poiId).isPresent()) {
            throw new BusinessException(ErrorCode.WISHLIST_ALREADY_EXISTS, 
                    "memberId=" + memberId + ", poiId=" + poiId);
        }

        wishlistRepository.save(Wishlist.builder()
                .memberId(memberId)
                .poiId(poiId)
                .isPublic(isPublic)
                .build());

        eventPublisher.publishEvent(new WishlistCreatedEvent(poiId));
    }

    @Transactional
    public void updateVisibility(Long memberId, Long poiId, Boolean isPublic) {
        Wishlist wishlist = wishlistRepository.findByMemberIdAndPoiId(memberId, poiId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WISHLIST_NOT_FOUND, 
                        "memberId=" + memberId + ", poiId=" + poiId));

        wishlist.changeVisibility(isPublic);
    }

    @Transactional
    public void deleteWishlist(Long memberId, Long poiId) {
        Wishlist wishlist = wishlistRepository.findByMemberIdAndPoiId(memberId, poiId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WISHLIST_NOT_FOUND, 
                        "memberId=" + memberId + ", poiId=" + poiId));

        wishlistRepository.delete(wishlist);
        
        eventPublisher.publishEvent(new WishlistDeletedEvent(poiId));
    }
}
