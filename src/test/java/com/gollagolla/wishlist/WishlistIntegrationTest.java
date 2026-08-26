package com.gollagolla.wishlist;

import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;
import com.gollagolla.auth.support.JwtTokenProvider;
import com.gollagolla.member.domain.Member;
import com.gollagolla.member.domain.MemberRepository;
import com.gollagolla.member.domain.Provider;
import com.gollagolla.member.domain.Role;
import com.gollagolla.poi.domain.*;
import com.gollagolla.wishlist.domain.Wishlist;
import com.gollagolla.wishlist.domain.WishlistRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WishlistIntegrationTest {

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
    WishlistRepository wishlistRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    private Member member;
    private Poi poi;
    private String token;

    @BeforeEach
    void setUp() {
        transactionTemplate.executeWithoutResult(status -> {
            member = memberRepository.save(Member.builder()
                    .email("wishlist@test.com")
                    .password("pw")
                    .nickname("찜유저")
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
                    .name("테스트 찜 장소")
                    .coordinate(new Coordinate(new BigDecimal("37.5665"), new BigDecimal("126.9780")))
                    .source(DataSource.INTERNAL)
                    .build());

            poiRepository.updateSeedStats(poi.getId(), BigDecimal.ZERO, 0, BigDecimal.ZERO);
        });

        token = "Bearer " + jwtTokenProvider.generateAccessToken(member.getId(), Role.USER);
    }

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            wishlistRepository.deleteAllInBatch();
            poiRepository.deleteAllInBatch();
            regionRepository.deleteAllInBatch();
            memberRepository.deleteAllInBatch();
        });
        
        jdbcTemplate.execute("ALTER TABLE wishlist ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE poi ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE region ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void 찜하기_전체_라이프_사이클_통합_테스트_생성_조회_수정_삭제() throws Exception {
        // given
        String createBody = objectMapper.writeValueAsString(
                Map.of("poiId", poi.getId(), "isPublic", true));

        // when & then
        mockMvc.perform(post("/api/v1/wishlist")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        Wishlist savedWishlist = wishlistRepository.findByMemberIdAndPoiId(member.getId(), poi.getId()).orElseThrow();
        assertThat(savedWishlist.getIsPublic()).isTrue();

        Poi updatedPoiAfterCreate = poiRepository.findById(poi.getId()).orElseThrow();
        assertThat(updatedPoiAfterCreate.getWishCount()).isEqualTo(1);

        // when & then
        mockMvc.perform(get("/api/v1/wishlist")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].poiId", is(poi.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is("테스트 찜 장소")))
                .andExpect(jsonPath("$[0].isPublic", is(true)));

        // when & then
        String updateBody = objectMapper.writeValueAsString(Map.of("isPublic", false));
        mockMvc.perform(patch("/api/v1/wishlist/{poiId}", poi.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        Wishlist updatedWishlist = wishlistRepository.findById(savedWishlist.getId()).orElseThrow();
        assertThat(updatedWishlist.getIsPublic()).isFalse();

        // when & then
        mockMvc.perform(delete("/api/v1/wishlist/{poiId}", poi.getId())
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        assertThat(wishlistRepository.findByMemberIdAndPoiId(member.getId(), poi.getId())).isEmpty();

        Poi updatedPoiAfterDelete = poiRepository.findById(poi.getId()).orElseThrow();
        assertThat(updatedPoiAfterDelete.getWishCount()).isEqualTo(0);
    }

    @Test
    void 이미_찜한_장소를_다시_추가하면_409_반환() throws Exception {
        // given
        transactionTemplate.executeWithoutResult(status -> {
            wishlistRepository.save(Wishlist.builder()
                    .memberId(member.getId())
                    .poiId(poi.getId())
                    .isPublic(true)
                    .build());
        });

        // when & then
        String body = objectMapper.writeValueAsString(
                Map.of("poiId", poi.getId(), "isPublic", true));

        mockMvc.perform(post("/api/v1/wishlist")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
