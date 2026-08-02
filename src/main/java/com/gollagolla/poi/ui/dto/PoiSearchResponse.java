package com.gollagolla.poi.ui.dto;

import com.gollagolla.poi.application.dto.PoiSearchResultDto;
import lombok.Getter;

import java.util.List;

@Getter
public class PoiSearchResponse {

    private final List<PoiSearchResultDto> results;

    private PoiSearchResponse(List<PoiSearchResultDto> results) {
        this.results = results;
    }

    public static PoiSearchResponse of(List<PoiSearchResultDto> results) {
        return new PoiSearchResponse(results);
    }
}
