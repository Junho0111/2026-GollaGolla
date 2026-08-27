package com.gollagolla.itinerary.domain;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.poi.domain.DataSource;
import com.gollagolla.poi.domain.EventPeriod;
import com.gollagolla.poi.domain.Poi;
import com.gollagolla.poi.domain.PoiCategory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class ItineraryTest {

    @Test
    void 축제의_종료일이_일정의_특정_날짜보다_이전이면_예외_발생() {
        // given
        Itinerary itinerary = Itinerary.builder()
                .memberId(1L)
                .title("테스트 일정")
                .regionId(1L)
                .travelPeriod(new TravelPeriod(
                        LocalDate.of(2026, 12, 1),
                        LocalDate.of(2026, 12, 5)))
                .genType(GenType.AI)
                .build();

        Poi expiredFestival = Poi.builder()
                .regionId(1L)
                .category(PoiCategory.FESTIVAL)
                .name("종료된 축제")
                .eventPeriod(new EventPeriod(
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)))
                .source(DataSource.TOURAPI)
                .build();

        // when & then
        assertThatThrownBy(() -> itinerary.validateFestivalPoi(expiredFestival, LocalDate.of(2026, 12, 1)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 축제의_종료일이_일정의_특정_날짜보다_이후거나_같으면_정상_통과() {
        // given
        Itinerary itinerary = Itinerary.builder()
                .memberId(1L)
                .title("테스트 일정")
                .regionId(1L)
                .travelPeriod(new TravelPeriod(
                        LocalDate.of(2026, 12, 1),
                        LocalDate.of(2026, 12, 5)))
                .genType(GenType.AI)
                .build();

        Poi validFestival = Poi.builder()
                .regionId(1L)
                .category(PoiCategory.FESTIVAL)
                .name("진행중인 축제")
                .eventPeriod(new EventPeriod(
                        LocalDate.of(2026, 12, 1),
                        LocalDate.of(2026, 12, 31)))
                .source(DataSource.TOURAPI)
                .build();

        // when & then
        assertThatCode(() -> itinerary.validateFestivalPoi(validFestival, LocalDate.of(2026, 12, 1)))
                .doesNotThrowAnyException();
    }
}
