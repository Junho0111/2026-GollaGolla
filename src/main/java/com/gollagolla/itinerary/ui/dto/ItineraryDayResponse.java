package com.gollagolla.itinerary.ui.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ItineraryDayResponse {
    private Integer dayNo;
    private List<ItineraryItemResponse> items;

    private ItineraryDayResponse() {}

    private ItineraryDayResponse(Integer dayNo, List<ItineraryItemResponse> items) {
        this.dayNo = dayNo;
        this.items = items;
    }

    public static ItineraryDayResponse of(Integer dayNo, List<ItineraryItemResponse> items) {
        return new ItineraryDayResponse(dayNo, items);
    }
}
