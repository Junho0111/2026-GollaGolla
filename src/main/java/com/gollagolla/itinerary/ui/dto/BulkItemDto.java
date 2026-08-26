package com.gollagolla.itinerary.ui.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class BulkItemDto {

    @NotNull(message = "POI ID는 필수입니다.")
    private Long poiId;

    private Integer dayNo;
    private Integer seq;
    private LocalTime startTime;
    private Boolean isAnchor;

    private BulkItemDto() {}

    public BulkItemDto(Long poiId, Integer dayNo, Integer seq, LocalTime startTime, Boolean isAnchor) {
        this.poiId = poiId;
        this.dayNo = dayNo;
        this.seq = seq;
        this.startTime = startTime;
        this.isAnchor = isAnchor;
    }
}
