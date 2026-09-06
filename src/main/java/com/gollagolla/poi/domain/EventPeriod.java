package com.gollagolla.poi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventPeriod {

    @Column(name = "event_start_date")
    private LocalDate startDate;

    @Column(name = "event_end_date")
    private LocalDate endDate;

    public EventPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("축제 시작일이 종료일보다 늦을 수 없습니다.");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
