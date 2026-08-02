package com.gollagolla.poi.ui;

import com.gollagolla.auth.support.JwtTokenProvider;
import com.gollagolla.poi.application.RegionQueryService;
import com.gollagolla.poi.domain.Region;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RegionController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegionControllerTest {

    @MockitoBean
    RegionQueryService regionQueryService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getRegions_파라미터_없이_정상조회() throws Exception {
        // given
        Region mockRegion = mock(Region.class);
        given(mockRegion.getId()).willReturn(1L);
        given(mockRegion.getName()).willReturn("전북");
        given(mockRegion.getDepth()).willReturn(1);

        given(regionQueryService.findRegions(null, null)).willReturn(List.of(mockRegion));

        // when & then
        mockMvc.perform(get("/api/v1/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("전북"))
                .andExpect(jsonPath("$[0].depth").value(1));
    }

    @Test
    void getRegions_depth_파라미터로_정상조회() throws Exception {
        // given
        Region mockRegion = mock(Region.class);
        given(mockRegion.getId()).willReturn(2L);
        given(mockRegion.getName()).willReturn("전주시");
        given(mockRegion.getDepth()).willReturn(2);

        given(regionQueryService.findRegions(null, 2)).willReturn(List.of(mockRegion));

        // when & then
        mockMvc.perform(get("/api/v1/regions")
                        .param("depth", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("전주시"))
                .andExpect(jsonPath("$[0].depth").value(2));
    }

    @Test
    void getRegions_parentId_파라미터로_정상조회() throws Exception {
        // given
        Region mockRegion = mock(Region.class);
        given(mockRegion.getId()).willReturn(2L);
        given(mockRegion.getName()).willReturn("전주시");
        given(mockRegion.getDepth()).willReturn(2);

        given(regionQueryService.findRegions(1L, null)).willReturn(List.of(mockRegion));

        // when & then
        mockMvc.perform(get("/api/v1/regions")
                        .param("parentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("전주시"));
    }
}
