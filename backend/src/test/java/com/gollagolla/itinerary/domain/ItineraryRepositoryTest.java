package com.gollagolla.itinerary.domain;

import com.gollagolla.global.config.EnableJpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(EnableJpaAuditingConfig.class)
class ItineraryRepositoryTest {

    @Autowired
    private ItineraryRepository itineraryRepository;

    @Test
    void ID와_MemberId로_일정_조회() {
        // given
        Itinerary itinerary = Itinerary.builder()
                .memberId(1L)
                .title("테스트 일정")
                .regionId(1L)
                .travelPeriod(new TravelPeriod(LocalDate.now(), LocalDate.now().plusDays(1)))
                .genType(GenType.MANUAL)
                .build();
        Itinerary saved = itineraryRepository.save(itinerary);

        // when
        Optional<Itinerary> found = itineraryRepository.findByIdAndMemberId(saved.getId(), 1L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("테스트 일정");
    }

    @Test
    void ShareToken으로_일정_조회() {
        // given
        Itinerary itinerary = Itinerary.builder()
                .memberId(1L)
                .title("공유 일정")
                .regionId(1L)
                .travelPeriod(new TravelPeriod(LocalDate.now(), LocalDate.now().plusDays(1)))
                .genType(GenType.MANUAL)
                .build();
        itinerary.issueShareToken("VALID_TOKEN");
        itineraryRepository.save(itinerary);

        // when
        Optional<Itinerary> found = itineraryRepository.findByShareToken("VALID_TOKEN");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("공유 일정");
    }
}
