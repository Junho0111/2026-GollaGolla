package com.gollagolla.wishlist.ui.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateWishlistVisibilityRequest {

    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isPublic;

    private UpdateWishlistVisibilityRequest() {}

    public UpdateWishlistVisibilityRequest(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}
