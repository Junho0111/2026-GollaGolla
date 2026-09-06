package com.gollagolla.itinerary.ui.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class BulkItemDto {

    @NotNull(message = "POI ID는 필수입니다.")
    private Long poiId;

    @NotNull(message = "일차(dayNo)는 필수입니다.")
    @Min(value = 1, message = "dayNo는 1 이상이어야 합니다.")
    private Integer dayNo;

    @NotNull(message = "순서(seq)는 필수입니다.")
    @Min(value = 0, message = "seq는 0 이상이어야 합니다.")
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
