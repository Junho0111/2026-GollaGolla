package com.gollagolla.wishlist.application.dto;

import lombok.Getter;

@Getter
public class WishlistItemDto {
    private Long poiId;
    private String name;
    private Boolean isPublic;

    private WishlistItemDto() {}

    private WishlistItemDto(Long poiId, String name, Boolean isPublic) {
        this.poiId = poiId;
        this.name = name;
        this.isPublic = isPublic;
    }

    public static WishlistItemDto of(Long poiId, String name, Boolean isPublic) {
        return new WishlistItemDto(poiId, name, isPublic);
    }
}
