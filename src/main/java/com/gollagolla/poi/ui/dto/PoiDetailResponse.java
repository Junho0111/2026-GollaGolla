package com.gollagolla.poi.ui.dto;

import com.gollagolla.poi.domain.PoiCategory;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
public class PoiDetailResponse {

    private Long poiId;
    private String name;
    private BigDecimal rating;
    private BigDecimal lat;
    private BigDecimal lng;
    private String thumbnailUrl;
    private List<String> imageUrls;
    private Map<String, String> openHours;
    private Map<String, String> breakTime;
    private String closedDays;
    private String naverMapUrl;
    private PoiCategory category;
    private String description;

    private PoiDetailResponse() {
    }

    private PoiDetailResponse(Long poiId, String name, BigDecimal rating,
                              BigDecimal lat, BigDecimal lng, String thumbnailUrl,
                              List<String> imageUrls, Map<String, String> openHours,
                              Map<String, String> breakTime, String closedDays,
                              String naverMapUrl, PoiCategory category, String description) {
        this.poiId = poiId;
        this.name = name;
        this.rating = rating;
        this.lat = lat;
        this.lng = lng;
        this.thumbnailUrl = thumbnailUrl;
        this.imageUrls = imageUrls;
        this.openHours = openHours;
        this.breakTime = breakTime;
        this.closedDays = closedDays;
        this.naverMapUrl = naverMapUrl;
        this.category = category;
        this.description = description;
    }

    public static PoiDetailResponse of(Long poiId, String name, BigDecimal rating,
                                       BigDecimal lat, BigDecimal lng, String thumbnailUrl,
                                       List<String> imageUrls, Map<String, String> openHours,
                                       Map<String, String> breakTime, String closedDays,
                                       String naverMapUrl, PoiCategory category, String description) {
        return new PoiDetailResponse(
                poiId,
                name,
                rating,
                lat,
                lng,
                thumbnailUrl,
                imageUrls,
                openHours,
                breakTime,
                closedDays,
                naverMapUrl,
                category,
                description);
    }
}
