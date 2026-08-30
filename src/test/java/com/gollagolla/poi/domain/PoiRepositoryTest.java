package com.gollagolla.poi.domain;

import com.gollagolla.global.config.EnableJpaAuditingConfig;
import com.gollagolla.global.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import({EnableJpaAuditingConfig.class, QuerydslConfig.class})
class PoiRepositoryTest {

    @Autowired
    PoiRepository poiRepository;

    @Test
    void Poi_등록_및_조회() {
        // given
        Poi poi = Poi.builder()
                .regionId(1L)
                .category(PoiCategory.ATTRACTION)
                .name("test poi")
                .coordinate(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO))
                .source(DataSource.INTERNAL)
                .build();
        
        Poi savedPoi = poiRepository.save(poi);

        // when
        Optional<Poi> found = poiRepository.findById(savedPoi.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("test poi");
    }

    @Test
    void 조회수_증가_검증() {
        // given
        Poi poi = Poi.builder()
                .regionId(1L)
                .category(PoiCategory.ATTRACTION)
                .name("test poi")
                .coordinate(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO))
                .source(DataSource.INTERNAL)
                .build();
        Poi savedPoi = poiRepository.save(poi);
        int initialViewCount = savedPoi.getViewCount();

        // when
        poiRepository.increaseViewCount(savedPoi.getId());

        //then
        Optional<Poi> updatedPoi = poiRepository.findById(savedPoi.getId());
        assertThat(updatedPoi.get().getViewCount()).isEqualTo(initialViewCount + 1);
    }
}
