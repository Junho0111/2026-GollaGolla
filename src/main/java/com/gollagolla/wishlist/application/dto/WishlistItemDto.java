package com.gollagolla.wishlist.application.dto;

import com.gollagolla.poi.domain.PoiCategory;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class WishlistItemDto {
    private Long poiId;
    private String name;
    private Boolean isPublic;
    private String thumbnailUrl;
    private BigDecimal rating;
    private String address;
    private PoiCategory category;

    private WishlistItemDto() {}

    private WishlistItemDto(Long poiId, String name, Boolean isPublic, String thumbnailUrl,
                            BigDecimal rating, String address, PoiCategory category) {
        this.poiId = poiId;
        this.name = name;
        this.isPublic = isPublic;
        this.thumbnailUrl = thumbnailUrl;
        this.rating = rating;
        this.address = address;
        this.category = category;
    }

    public static WishlistItemDto of(Long poiId, String name, Boolean isPublic, String thumbnailUrl,
                                     BigDecimal rating, String address, PoiCategory category
    ) {
        return new WishlistItemDto(poiId, name, isPublic, thumbnailUrl, rating, address, category);
    }
}
