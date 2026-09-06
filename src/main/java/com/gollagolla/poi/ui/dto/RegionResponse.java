package com.gollagolla.poi.ui.dto;

import lombok.Getter;

@Getter
public class RegionResponse {

    private Long regionId;
    private String name;
    private Integer depth;

    private RegionResponse() {
    }

    private RegionResponse(Long regionId, String name, Integer depth) {
        this.regionId = regionId;
        this.name = name;
        this.depth = depth;
    }

    public static RegionResponse of(Long regionId, String name, Integer depth) {
        return new RegionResponse(regionId, name, depth);
    }
}
