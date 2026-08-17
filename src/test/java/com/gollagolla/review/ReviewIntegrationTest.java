package com.gollagolla.review;

import tools.jackson.databind.ObjectMapper;
import com.gollagolla.auth.support.JwtTokenProvider;
import com.gollagolla.member.domain.Member;
import com.gollagolla.member.domain.MemberRepository;
import com.gollagolla.member.domain.Provider;
import com.gollagolla.member.domain.Role;
import com.gollagolla.poi.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PoiRepository poiRepository;

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    private Member member;
    private Member otherMember;
    private Poi poi;
    private String membertoken;
    private String otherMemberToken;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .email("reviewer@test.com")
                .password("pw")
                .nickname("여행러")
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .build());

        otherMember = memberRepository.save(Member.builder()
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

        poi = poiRepository.save(Poi.builder()
                .regionId(region.getId())
                .category(PoiCategory.ATTRACTION)
                .name("테스트 장소")
                .coordinate(new Coordinate(new BigDecimal("37.5665"), new BigDecimal("126.9780")))
                .source(DataSource.INTERNAL)
                .build());

        membertoken = "Bearer " + jwtTokenProvider.generateAccessToken(member.getId(), Role.USER);
        otherMemberToken = "Bearer " + jwtTokenProvider.generateAccessToken(otherMember.getId(), Role.USER);
    }

    @Test
    void 리뷰_작성_및_조회_통합_테스트__가중평균_포함() throws Exception {
        // given
        String body1 = objectMapper.writeValueAsString(Map.of("rating", 5, "content", "최고"));
        String body2 = objectMapper.writeValueAsString(Map.of("rating", 3, "content", "보통"));

        // when & then
        mockMvc.perform(post("/api/v1/pois/{poiId}/reviews", poi.getId())
                        .header("Authorization", membertoken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").isNumber())
                .andExpect(jsonPath("$.poiRatingUpdated").value(5.0));

        // when & then
        mockMvc.perform(post("/api/v1/pois/{poiId}/reviews", poi.getId())
                        .header("Authorization", otherMemberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.poiRatingUpdated").value(4.0));

        // when & then
        mockMvc.perform(get("/api/v1/pois/{poiId}/reviews", poi.getId())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].rating", is(5)))
                .andExpect(jsonPath("$.content[0].content", is("최고")))
                .andExpect(jsonPath("$.content[0].nickname", is("여행러")));
    }

    @Test
    void 동일_회원_중복_리뷰_작성_시_409_반환() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(Map.of("rating", 5, "content", "최고"));

        // when
        mockMvc.perform(post("/api/v1/pois/{poiId}/reviews", poi.getId())
                        .header("Authorization", membertoken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(post("/api/v1/pois/{poiId}/reviews", poi.getId())
                        .header("Authorization", membertoken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
