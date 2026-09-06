package com.gollagolla.poi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coordinate {

    @Column(name = "lat", precision = 10, scale = 7, nullable = false)
    private BigDecimal lat;

    @Column(name = "lng", precision = 10, scale = 7, nullable = false)
    private BigDecimal lng;

    public Coordinate(BigDecimal lat, BigDecimal lng) {
        this.lat = lat;
        this.lng = lng;
    }
}
