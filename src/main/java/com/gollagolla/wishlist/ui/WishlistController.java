package com.gollagolla.wishlist.ui;

import com.gollagolla.wishlist.application.WishlistService;
import com.gollagolla.wishlist.application.dto.WishlistItemDto;
import com.gollagolla.wishlist.ui.dto.CreateWishlistRequest;
import com.gollagolla.wishlist.ui.dto.UpdateWishlistVisibilityRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<WishlistItemDto> getMyWishlists(@AuthenticationPrincipal Long memberId) {
        return wishlistService.getMyWishlists(memberId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createWishlist(
            @RequestBody @Valid CreateWishlistRequest request, @AuthenticationPrincipal Long memberId) {
        wishlistService.createWishlist(memberId, request.getPoiId(), request.getIsPublic());
    }

    @PatchMapping("/{poiId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateVisibility(
            @PathVariable Long poiId,
            @RequestBody @Valid UpdateWishlistVisibilityRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        wishlistService.updateVisibility(memberId, poiId, request.getIsPublic());
    }

    @DeleteMapping("/{poiId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWishlist(@PathVariable Long poiId, @AuthenticationPrincipal Long memberId) {
        wishlistService.deleteWishlist(memberId, poiId);
    }
}
