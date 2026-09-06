package com.gollagolla.poi;

import com.gollagolla.poi.domain.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PoiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    PoiRepository poiRepository;

    @Autowired
    EntityManager entityManager;

    private Region depth1;
    private Region depth2;
    private Poi poi1;

    @BeforeEach
    void setUp() {
        depth1 = regionRepository.save(Region.builder()
                .name("전북")
                .depth(1)
                .build()
        );

        depth2 = regionRepository.save(Region.builder()
                .parentId(depth1.getId()).name("전주시")
                .depth(2)
                .build());

        poi1 = poiRepository.save(Poi.builder()
                .regionId(depth2.getId())
                .category(PoiCategory.ATTRACTION)
                .name("전주 한옥마을")
                .description("전통 한옥 관광지")
                .coordinate(new Coordinate(new BigDecimal("35.8150"), new BigDecimal("127.1522")))
                .thumbnailUrl("https://example.com/thumb1.jpg")
                .imageUrls(List.of("https://example.com/img1.jpg"))
                .openHours(Map.of("월~일", "09:00~21:00"))
                .closedDays("연중무휴")
                .source(DataSource.INTERNAL)
                .build());
    }

    @Test
    void 지역_목록_조회_정상_흐름() throws Exception {
        mockMvc.perform(get("/api/v1/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].depth", everyItem(is(1))));
    }

    @Test
    void POI_피드_조회_정상_흐름() throws Exception {
        mockMvc.perform(get("/api/v1/pois")
                        .param("regionId", String.valueOf(depth2.getId()))
                        .param("category", "ATTRACTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("전주 한옥마을")));
    }

    @Test
    void 키워드_검색_정상_흐름() throws Exception {
        mockMvc.perform(get("/api/v1/search").param("q", "한옥"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].name", containsString("한옥")));
    }

    @Test
    void POI_상세_조회_정상_및_조회수_증가_흐름() throws Exception {
        int beforeViewCount = poiRepository.findById(poi1.getId()).get().getViewCount();

        mockMvc.perform(get("/api/v1/pois/{poiId}", poi1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("전주 한옥마을")));

        entityManager.flush();
        entityManager.clear();
        int afterViewCount = poiRepository.findById(poi1.getId()).get().getViewCount();
        assertThat(afterViewCount).isEqualTo(beforeViewCount + 1);
    }

    @Test
    void 존재하지_않는_POI_상세조회_시_404_반환() throws Exception {
        mockMvc.perform(get("/api/v1/pois/{poiId}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("POI_001")));
    }
}
