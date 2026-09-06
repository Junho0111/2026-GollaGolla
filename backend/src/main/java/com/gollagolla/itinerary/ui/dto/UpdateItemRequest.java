package com.gollagolla.itinerary.ui.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class UpdateItemRequest {

    @NotNull(message = "일차(dayNo)는 필수입니다.")
    @Min(value = 1, message = "일차는 1 이상이어야 합니다.")
    private Integer dayNo;

    @NotNull(message = "순서(seq)는 필수입니다.")
    @Min(value = 0, message = "순서는 0 이상이어야 합니다.")
    private Integer seq;

    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAnchor;

    private UpdateItemRequest() {}

    public UpdateItemRequest(Integer dayNo, Integer seq, LocalTime startTime, LocalTime endTime, Boolean isAnchor) {
        this.dayNo = dayNo;
        this.seq = seq;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAnchor = isAnchor;
    }
}
