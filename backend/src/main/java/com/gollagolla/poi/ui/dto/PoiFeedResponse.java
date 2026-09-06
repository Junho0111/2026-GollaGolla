package com.gollagolla.poi.ui.dto;

import com.gollagolla.poi.application.dto.PoiCardDto;
import lombok.Getter;

import java.util.List;

@Getter
public class PoiFeedResponse {

    private final List<PoiCardDto> content;
    private final int totalPages;
    private final boolean hasNext;

    private PoiFeedResponse(List<PoiCardDto> content, int totalPages, boolean hasNext) {
        this.content = content;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
    }

    public static PoiFeedResponse of(List<PoiCardDto> content, int totalPages, boolean hasNext) {
        return new PoiFeedResponse(content, totalPages, hasNext);
    }
}
