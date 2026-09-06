package com.gollagolla.itinerary.application;

import java.time.LocalTime;
import java.util.List;

public record AiScheduleResult(
    String explanation,
    List<AiScheduleDay> days
) {
    public record AiScheduleDay(
        Integer dayNo,
        List<AiScheduleItem> items
    ) {}

    public record AiScheduleItem(
        Long poiId,
        Integer seq,
        LocalTime startTime,
        LocalTime endTime
    ) {}
}
