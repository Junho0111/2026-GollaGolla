package com.gollagolla.itinerary.ui.dto;

import com.gollagolla.itinerary.domain.GenType;
import com.gollagolla.itinerary.domain.Itinerary;
import com.gollagolla.itinerary.domain.TransportMode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ItineraryListItemResponse {

    private Long itineraryId;
    private String title;
    private Long regionId;
    private LocalDate startDate;
    private LocalDate endDate;
    private TransportMode transportMode;
    private GenType genType;

    private ItineraryListItemResponse() {}

    private ItineraryListItemResponse(Long itineraryId, String title, Long regionId,
                                      LocalDate startDate, LocalDate endDate,
                                      TransportMode transportMode, GenType genType) {
        this.itineraryId = itineraryId;
        this.title = title;
        this.regionId = regionId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.transportMode = transportMode;
        this.genType = genType;
    }

    public static ItineraryListItemResponse from(Itinerary itinerary) {
        return new ItineraryListItemResponse(
                itinerary.getId(),
                itinerary.getTitle(),
                itinerary.getRegionId(),
                itinerary.getTravelPeriod().getStartDate(),
                itinerary.getTravelPeriod().getEndDate(),
                itinerary.getTransportMode(),
                itinerary.getGenType()
        );
    }
}
