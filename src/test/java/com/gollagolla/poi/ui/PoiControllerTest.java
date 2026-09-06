package com.gollagolla.poi.ui;

import com.gollagolla.auth.support.JwtTokenProvider;
import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import com.gollagolla.poi.application.PoiQueryService;
import com.gollagolla.poi.application.dto.PoiCardDto;
import com.gollagolla.poi.application.dto.PoiSearchResultDto;
import com.gollagolla.poi.domain.PoiCategory;
import com.gollagolla.poi.ui.dto.PoiDetailResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PoiController.class)
@AutoConfigureMockMvc(addFilters = false)
class PoiControllerTest {

    @MockitoBean
    PoiQueryService poiQueryService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getPoiFeed_정상조회() throws Exception {
        // given
        PoiCardDto dto = new PoiCardDto(1L, "전주 한옥마을", "url", BigDecimal.valueOf(4.5), 10, 5, BigDecimal.valueOf(100.0), false);
        given(poiQueryService.getPoiFeed(any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1));

        // when & then
        mockMvc.perform(get("/api/v1/pois")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("전주 한옥마을"))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getPoiFeed_size가_100_초과면_400_반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/pois")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"))
                .andExpect(jsonPath("$.message").value(containsString("size")));
    }

    @Test
    void getPoiFeed_size가_1_미만이면_400_반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/pois")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"))
                .andExpect(jsonPath("$.message").value(containsString("size")));
    }

    @Test
    void getPoiFeed_page가_음수면_400_반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/pois")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"))
                .andExpect(jsonPath("$.message").value(containsString("page")));
    }

    @Test
    void search_정상조회() throws Exception {
        // given
        PoiSearchResultDto dto = new PoiSearchResultDto(1L, "전주 한옥마을", PoiCategory.ATTRACTION, "전주시");
        given(poiQueryService.searchByKeyword("전주")).willReturn(List.of(dto));

        // when & then
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "전주")
                        .param("type", "poi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].name").value("전주 한옥마을"))
                .andExpect(jsonPath("$.results[0].regionName").value("전주시"));
    }

    @Test
    void search_미지원_type이면_400_반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "전주")
                        .param("type", "member"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POI_002"))
                .andExpect(jsonPath("$.message").value(containsString("지원하지 않는 검색 타입")));
    }

    @Test
    void getPoiDetail_정상조회() throws Exception {
        // given
        PoiDetailResponse response = PoiDetailResponse.of(1L, "전주 한옥마을", BigDecimal.valueOf(4.5), BigDecimal.ZERO, BigDecimal.ZERO, "url", List.of(), java.util.Map.of("월", "09:00"), java.util.Map.of("월", "12:00-13:00"), "closed", "naver", PoiCategory.ATTRACTION, "desc", false);
        given(poiQueryService.getPoiDetail(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/pois/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("전주 한옥마을"));
    }

    @Test
    void getPoiDetail_존재하지_않는_장소면_404_반환() throws Exception {
        // given
        given(poiQueryService.getPoiDetail(any(), any()))
                .willThrow(new BusinessException(ErrorCode.POI_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/pois/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POI_001"))
                .andExpect(jsonPath("$.message").value(containsString("존재하지 않는 장소")));
    }
}
