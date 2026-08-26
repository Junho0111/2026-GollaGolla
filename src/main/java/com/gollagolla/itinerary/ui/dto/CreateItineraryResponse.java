package com.gollagolla.itinerary.ui.dto;

import lombok.Getter;

@Getter
public class CreateItineraryResponse {
    private Long itineraryId;

    private CreateItineraryResponse() {}

    private CreateItineraryResponse(Long itineraryId) {
        this.itineraryId = itineraryId;
    }

    public static CreateItineraryResponse of(Long itineraryId) {
        return new CreateItineraryResponse(itineraryId);
    }
}
