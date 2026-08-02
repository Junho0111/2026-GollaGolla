package com.gollagolla.poi.domain;

import com.gollagolla.config.EnableJpaAuditingConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import com.gollagolla.poi.application.dto.PoiCardDto;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;

@Import({QPoiRepository.class, EnableJpaAuditingConfig.class})
@DataJpaTest
@Transactional
class QPoiRepositoryTest {

    @TestConfiguration
    static class QuerydslTestConfig {
        @PersistenceContext
        private EntityManager em;

        @Bean
        public JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(em);
        }
    }

    @Autowired
    PoiRepository poiRepository;

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    QPoiRepository qPoiRepository;

    @Test
    void POI_피드_목록_페이징_조회() {
        // given
        Region region = Region.builder()
                .name("region1")
                .depth(1)
                .build();
        regionRepository.save(region);

        Poi poi = Poi.builder()
                .regionId(region.getId())
                .category(PoiCategory.ATTRACTION)
                .name("test poi")
                .coordinate(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO))
                .source(DataSource.INTERNAL)
                .build();
        poiRepository.save(poi);

        // when
        Page<PoiCardDto> result = qPoiRepository.findPoiFeed(region.getId(), PoiCategory.ATTRACTION, PageRequest.of(0, 10), null);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("test poi");
    }
}
