package com.gollagolla.tourapi.client.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AreaBasedListResponse {

    @JsonProperty("response")
    private Response response;

    public List<PoiItem> getItems() {
        if (response == null || response.body == null || response.body.items == null) {
            return List.of();
        }
        return response.body.items.item;
    }

    public int getTotalCount() {
        if (response == null || response.body == null) {
            return 0;
        }
        return response.body.totalCount;
    }

    private record Response(@JsonProperty("body") Body body) {}

    private record Body(
            @JsonProperty("items") Items items,
            @JsonProperty("totalCount") int totalCount
    ) {}

    private record Items(@JsonProperty("item") List<PoiItem> item) {
        @JsonCreator
        public static Items fromString(String val) {
            return new Items(List.of());
        }
    }

    public record PoiItem(
            @JsonProperty("contentid") String contentId,
            @JsonProperty("contenttypeid") String contentTypeId,
            @JsonProperty("title") String title,
            @JsonProperty("addr1") String addr1,
            @JsonProperty("mapy") String lat,
            @JsonProperty("mapx") String lng,
            @JsonProperty("firstimage") String firstImage,
            @JsonProperty("firstimage2") String firstImage2,
            @JsonProperty("sigungucode") String sigunguCode
    ) {}
}
