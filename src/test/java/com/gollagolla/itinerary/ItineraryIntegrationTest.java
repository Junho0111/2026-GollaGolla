package com.gollagolla.itinerary;

import com.gollagolla.auth.support.JwtTokenProvider;
import com.gollagolla.itinerary.domain.*;
import com.gollagolla.member.domain.Member;
import com.gollagolla.member.domain.MemberRepository;
import com.gollagolla.member.domain.Provider;
import com.gollagolla.member.domain.Role;
import com.gollagolla.poi.domain.Coordinate;
import com.gollagolla.poi.domain.DataSource;
import com.gollagolla.poi.domain.Poi;
import com.gollagolla.poi.domain.PoiCategory;
import com.gollagolla.poi.domain.PoiRepository;
import com.gollagolla.poi.domain.Region;
import com.gollagolla.poi.domain.RegionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;
import com.gollagolla.itinerary.application.ShareTokenService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItineraryIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ItineraryRepository itineraryRepository;

    @Autowired
    ItineraryItemRepository itineraryItemRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PoiRepository poiRepository;

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    ShareTokenService shareTokenService;

    private Member owner;
    private Member other;
    private Poi poi1;
    private Poi poi2;
    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setUp() {
        transactionTemplate.executeWithoutResult(status -> {
            owner = memberRepository.save(Member.builder()
                    .email("owner@test.com")
                    .password("pw")
                    .nickname("나")
                    .provider(Provider.LOCAL)
                    .role(Role.USER)
                    .build());

            other = memberRepository.save(Member.builder()
                    .email("other@test.com")
                    .password("pw")
                    .nickname("다른사람")
                    .provider(Provider.LOCAL)
                    .role(Role.USER)
                    .build());

            Region region = regionRepository.save(Region.builder()
                    .name("서울")
                    .depth(1)
                    .build());

            poi1 = poiRepository.save(Poi.builder()
                    .regionId(region.getId())
                    .category(PoiCategory.ATTRACTION).name("POI 1")
                    .coordinate(new Coordinate(new BigDecimal("37.0"), new BigDecimal("127.0")))
                    .source(DataSource.INTERNAL)
                    .build());

            poi2 = poiRepository.save(Poi.builder()
                    .regionId(region.getId())
                    .category(PoiCategory.RESTAURANT).name("POI 2")
                    .coordinate(new Coordinate(new BigDecimal("37.1"), new BigDecimal("127.1")))
                    .source(DataSource.INTERNAL)
                    .build());
        });

        ownerToken = "Bearer " + jwtTokenProvider.generateAccessToken(owner.getId(), Role.USER);
        otherToken = "Bearer " + jwtTokenProvider.generateAccessToken(other.getId(), Role.USER);
    }

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            itineraryItemRepository.deleteAllInBatch();
            itineraryRepository.deleteAllInBatch();
            poiRepository.deleteAllInBatch();
            regionRepository.deleteAllInBatch();
            memberRepository.deleteAllInBatch();
        });
    }

    @Test
    void 일정_전체_플로우_검증() throws Exception {
        // 일정 생성
        String createReq = objectMapper.writeValueAsString(Map.of(
                "title", "테스트 일정",
                "regionId", 1L,
                "startDate", "2026-08-01",
                "endDate", "2026-08-03",
                "transportMode", "CAR"
        ));

        MvcResult createRes = mockMvc.perform(post("/api/v1/itineraries")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReq))
                .andExpect(status().isCreated())
                .andReturn();

        String resString = createRes.getResponse().getContentAsString();
        Long itineraryId = objectMapper.readTree(resString).get("itineraryId").asLong();

        // 단건 항목 추가
        String itemReq = objectMapper.writeValueAsString(Map.of(
                "poiId", poi1.getId(),
                "dayNo", 1,
                "seq", 0
        ));
        mockMvc.perform(post("/api/v1/itineraries/{id}/items", itineraryId)
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemReq))
                .andExpect(status().isCreated());

        // 다건 항목 추가
        String bulkReq = objectMapper.writeValueAsString(Map.of(
                "items", List.of(
                        Map.of(
                                "poiId", poi2.getId(),
                                "dayNo", 1,
                                "seq", 1
                        )
                )
        ));
        mockMvc.perform(post("/api/v1/itineraries/{id}/items/bulk", itineraryId)
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkReq))
                .andExpect(status().isCreated());

        // 조회 및 seq 기본값 검증
        MvcResult getRes = mockMvc.perform(get("/api/v1/itineraries/{id}", itineraryId)
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("테스트 일정")))
                .andExpect(jsonPath("$.days", hasSize(1)))
                .andExpect(jsonPath("$.days[0].dayNo", is(1)))
                .andExpect(jsonPath("$.days[0].items", hasSize(2)))
                .andExpect(jsonPath("$.days[0].items[0].poiId", is(poi1.getId().intValue())))
                .andExpect(jsonPath("$.days[0].items[0].seq", is(0)))
                .andExpect(jsonPath("$.days[0].items[1].poiId", is(poi2.getId().intValue())))
                .andExpect(jsonPath("$.days[0].items[1].seq", is(1)))
                .andReturn();

        Long itemIdToUpdate = objectMapper.readTree(getRes.getResponse().getContentAsString())
                .get("days")
                .get(0)
                .get("items")
                .get(0)
                .get("itemId")
                .asLong();

        // 항목 수정
        String updateReq = objectMapper.writeValueAsString(Map.of(
                "dayNo", 2,
                "seq", 0,
                "startTime", "10:00:00",
                "isAnchor", true
        ));
        mockMvc.perform(patch("/api/v1/itineraries/{id}/items/{itemId}", itineraryId, itemIdToUpdate)
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateReq))
                .andExpect(status().isOk());

        // 수정 반영 확인
        mockMvc.perform(get("/api/v1/itineraries/{id}", itineraryId)
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days", hasSize(2)));

        // 삭제
        mockMvc.perform(delete("/api/v1/itineraries/{id}/items/{itemId}", itineraryId, itemIdToUpdate)
                        .header("Authorization", ownerToken))
                .andExpect(status().isNoContent());

        // 삭제 반영 확인
        mockMvc.perform(get("/api/v1/itineraries/{id}", itineraryId)
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days", hasSize(1)))
                .andExpect(jsonPath("$.days[0].items", hasSize(1)));
    }

    @Test
    void 타인_일정_접근_시_403() throws Exception {
        // given
        Itinerary itinerary = itineraryRepository.save(Itinerary.builder()
                .memberId(owner.getId())
                .title("오너의 일정")
                .regionId(1L)
                .travelPeriod(new TravelPeriod(LocalDate.now(), LocalDate.now().plusDays(1)))
                .genType(GenType.MANUAL)
                .build());

        // when & then
        mockMvc.perform(get("/api/v1/itineraries/{id}", itinerary.getId())
                        .header("Authorization", otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void 공유_링크_발급_및_비로그인_조회() throws Exception {
        // given
        Itinerary itinerary = itineraryRepository.save(Itinerary.builder()
                .memberId(owner.getId())
                .title("공유할 일정")
                .regionId(1L)
                .travelPeriod(new TravelPeriod(LocalDate.now(), LocalDate.now().plusDays(1)))
                .genType(GenType.MANUAL)
                .build());

        // when & then
        MvcResult shareRes = mockMvc.perform(post("/api/v1/itineraries/{id}/share", itinerary.getId())
                        .header("Authorization", ownerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shareToken", notNullValue()))
                .andExpect(jsonPath("$.url", containsString("/s/")))
                .andReturn();

        String token = objectMapper.readTree(shareRes.getResponse().getContentAsString()).get("shareToken").asText();

        // when & then
        mockMvc.perform(get("/api/v1/share/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("공유할 일정")));
    }

    @Test
    void 공유토큰_충돌_시_UnexpectedRollbackException_검증() throws Exception {
        // given
        Itinerary i1 = itineraryRepository.save(Itinerary.builder()
                .memberId(owner.getId()).title("i1")
                .regionId(1L)
                .travelPeriod(new TravelPeriod(LocalDate.now(), LocalDate.now()))
                .genType(GenType.MANUAL)
                .build());

        Itinerary i2 = itineraryRepository.save(Itinerary.builder()
                .memberId(owner.getId())
                .title("i2")
                .regionId(1L)
                .travelPeriod(new TravelPeriod(LocalDate.now(), LocalDate.now())).genType(GenType.MANUAL)
                .build());

        transactionTemplate.executeWithoutResult(status -> {
            Itinerary temp = itineraryRepository.findById(i1.getId()).orElseThrow();
            temp.issueShareToken("COLLISION_TOKEN");
            itineraryRepository.saveAndFlush(temp);
        });

        // when
        transactionTemplate.executeWithoutResult(status -> {
            try {
                shareTokenService.saveShareTokenWithNewTransaction(i2.getId(), "COLLISION_TOKEN");
            } catch (DataIntegrityViolationException e) {}

            Itinerary temp = itineraryRepository.findById(i2.getId()).orElseThrow();
            temp.issueShareToken("NEW_SAFE_TOKEN");
            itineraryRepository.saveAndFlush(temp);
        });

        // then
        Itinerary finalI2 = itineraryRepository.findById(i2.getId()).orElseThrow();
        assertThat(finalI2.getShareToken()).isEqualTo("NEW_SAFE_TOKEN");
    }
}
