package com.gollagolla.poi.application.dto;

import com.gollagolla.poi.domain.PoiCategory;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class PoiSearchResultDto {

    private Long poiId;
    private String name;
    private PoiCategory category;
    private String regionName;

    private PoiSearchResultDto() {
    }

    @QueryProjection
    public PoiSearchResultDto(Long poiId, String name, PoiCategory category, String regionName) {
        this.poiId = poiId;
        this.name = name;
        this.category = category;
        this.regionName = regionName;
    }
}
