package com.gollagolla.itinerary.ui.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ItineraryResponse {
    private Long itineraryId;
    private String title;
    private List<ItineraryDayResponse> days;

    private ItineraryResponse() {}

    private ItineraryResponse(Long itineraryId, String title, List<ItineraryDayResponse> days) {
        this.itineraryId = itineraryId;
        this.title = title;
        this.days = days;
    }

    public static ItineraryResponse of(Long itineraryId, String title, List<ItineraryDayResponse> days) {
        return new ItineraryResponse(itineraryId, title, days);
    }
}
