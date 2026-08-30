package com.gollagolla.tourapi.client.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DetailIntroResponse {

    @JsonProperty("response")
    private Response response;

    public DetailItem getItem() {
        if (response == null || response.body == null || response.body.items == null) {
            return null;
        }

        if (response.body.items.item == null || response.body.items.item.isEmpty()) {
            return null;
        }

        return response.body.items.item.get(0);
    }

    private record Response(@JsonProperty("body") Body body) {}

    private record Body(@JsonProperty("items") Items items) {}

    private record Items(@JsonProperty("item") List<DetailItem> item) {
        @JsonCreator
        public static Items fromString(String val) {
            return new Items(List.of());
        }
    }

    public record DetailItem(
            @JsonProperty("opentimefood") String openTimeFood,
            @JsonProperty("restdatefood") String restDateFood,

            @JsonProperty("usetime") String useTime,
            @JsonProperty("restdate") String restDate
    ) {
        public String getOpenHours() {
            if (openTimeFood != null && !openTimeFood.isBlank()) {
                return openTimeFood;
            }
            return useTime;
        }

        public String getClosedDays() {
            if (restDateFood != null && !restDateFood.isBlank()) {
                return restDateFood;
            }
            return restDate;
        }
    }
}
