package com.gollagolla.itinerary.application;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.itinerary.domain.*;
import com.gollagolla.itinerary.ui.dto.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class ItineraryServiceTest {

    @Mock
    private ItineraryRepository itineraryRepository;

    @InjectMocks
    private ItineraryService itineraryService;

    @Test
    void 일정_생성() {
        // given
        CreateItineraryRequest req = new CreateItineraryRequest(
                "제목", 1L, LocalDate.now(), LocalDate.now().plusDays(2), TransportMode.CAR);

        Itinerary saved = Itinerary.builder()
                .memberId(1L)
                .title("제목")
                .regionId(1L)
                .travelPeriod(new TravelPeriod(req.getStartDate(), req.getEndDate()))
                .transportMode(req.getTransportMode())
                .genType(GenType.MANUAL)
                .build();
        setField(saved, "id", 100L);
        given(itineraryRepository.save(any(Itinerary.class))).willReturn(saved);

        // when
        Long id = itineraryService.createItinerary(1L, req);

        // then
        assertThat(id).isEqualTo(100L);
    }

    @Test
    void 권한_없는_일정_조회_시_예외_발생() {
        // given
        Itinerary itinerary = Itinerary.builder()
                .memberId(1L)
                .title("제목")
                .regionId(1L)
                .travelPeriod(new TravelPeriod(LocalDate.now(), LocalDate.now()))
                .genType(GenType.MANUAL)
                .build();
        given(itineraryRepository.findById(100L)).willReturn(Optional.of(itinerary));

        // when & then
        assertThatThrownBy(() -> itineraryService.getItinerary(2L, 100L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 아이템_추가() {
        // given
        Itinerary itinerary = Itinerary.builder()
                .memberId(1L)
                .title("제목")
                .regionId(1L)
                .travelPeriod(new TravelPeriod(LocalDate.now(), LocalDate.now()))
                .genType(GenType.MANUAL)
                .build();
        given(itineraryRepository.findById(100L)).willReturn(Optional.of(itinerary));

        CreateItemRequest req = new CreateItemRequest(10L, 1, 0);

        // when
        itineraryService.addItem(1L, 100L, req);

        // then
        assertThat(itinerary.getItems()).hasSize(1);
        assertThat(itinerary.getItems().get(0).getPoiId()).isEqualTo(10L);
    }
}
