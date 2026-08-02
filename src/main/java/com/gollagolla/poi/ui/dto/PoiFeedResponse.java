package com.gollagolla.poi.ui.dto;

import com.gollagolla.poi.application.dto.PoiCardDto;
import lombok.Getter;

import java.util.List;

@Getter
public class PoiFeedResponse {

    private final List<PoiCardDto> content;
    private final int totalPages;

    private PoiFeedResponse(List<PoiCardDto> content, int totalPages) {
        this.content = content;
        this.totalPages = totalPages;
    }

    public static PoiFeedResponse of(List<PoiCardDto> content, int totalPages) {
        return new PoiFeedResponse(content, totalPages);
    }
}
