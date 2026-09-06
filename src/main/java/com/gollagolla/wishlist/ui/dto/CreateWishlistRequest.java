package com.gollagolla.wishlist.ui.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateWishlistRequest {

    @NotNull(message = "POI ID는 필수입니다.")
    private Long poiId;

    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isPublic;

    private CreateWishlistRequest() {}

    public CreateWishlistRequest(Long poiId, Boolean isPublic) {
        this.poiId = poiId;
        this.isPublic = isPublic;
    }
}
