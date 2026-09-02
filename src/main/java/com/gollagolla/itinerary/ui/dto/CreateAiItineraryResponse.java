package com.gollagolla.itinerary.ui.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateAiItineraryResponse {

    private ItineraryResponse itinerary;

    private String aiExplanation;
}
