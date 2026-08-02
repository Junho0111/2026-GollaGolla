package com.gollagolla.poi.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PoiCardDto {

    private Long poiId;
    private String name;
    private String thumbnailUrl;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer wishCount;
    private BigDecimal popularityScore;
    private boolean wished;

    private PoiCardDto() {
    }

    @QueryProjection
    public PoiCardDto(Long poiId, String name, String thumbnailUrl, BigDecimal rating,
                      Integer reviewCount, Integer wishCount, BigDecimal popularityScore, boolean wished) {
        this.poiId = poiId;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.wishCount = wishCount;
        this.popularityScore = popularityScore;
        this.wished = wished;
    }
}
