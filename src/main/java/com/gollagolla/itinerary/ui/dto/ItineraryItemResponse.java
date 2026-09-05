package com.gollagolla.itinerary.ui.dto;

import lombok.Getter;

import java.time.LocalTime;

@Getter
public class ItineraryItemResponse {
    private Long itemId;
    private Long poiId;
    private String poiName;
    private String poiThumbnailUrl;
    private Integer seq;
    private Boolean isAnchor;
    private LocalTime startTime;
    private LocalTime endTime;
    private String memo;

    private ItineraryItemResponse() {}

    private ItineraryItemResponse(Long itemId, Long poiId, String poiName, String poiThumbnailUrl, Integer seq,
                                  Boolean isAnchor, LocalTime startTime, LocalTime endTime, String memo) {
        this.itemId = itemId;
        this.poiId = poiId;
        this.poiName = poiName;
        this.poiThumbnailUrl = poiThumbnailUrl;
        this.seq = seq;
        this.isAnchor = isAnchor;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
    }

    public static ItineraryItemResponse of(Long itemId, Long poiId, String poiName, String poiThumbnailUrl, Integer seq,
                                           Boolean isAnchor, LocalTime startTime, LocalTime endTime, String memo
    ) {
        return new ItineraryItemResponse(itemId, poiId, poiName, poiThumbnailUrl, seq, isAnchor, startTime, endTime, memo);
    }
}
