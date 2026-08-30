package com.gollagolla.tourapi.client.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AreaCodeResponse {

    @JsonProperty("response")
    private Response response;

    public List<AreaCodeItem> getItems() {
        if (response == null || response.body == null || response.body.items == null) {
            return List.of();
        }
        return response.body.items.item;
    }

    private record Response(@JsonProperty("body") Body body) {}

    private record Body(@JsonProperty("items") Items items) {}

    private record Items(@JsonProperty("item") List<AreaCodeItem> item) {
        @JsonCreator
        public static Items fromString(String val) {
            return new Items(List.of());
        }
    }

    public record AreaCodeItem(
            @JsonProperty("code") String code,
            @JsonProperty("name") String name
    ) {}
}
