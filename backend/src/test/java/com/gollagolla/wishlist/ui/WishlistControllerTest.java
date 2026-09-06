package com.gollagolla.wishlist.ui;

import com.gollagolla.poi.domain.PoiCategory;
import tools.jackson.databind.ObjectMapper;
import com.gollagolla.auth.support.JwtTokenProvider;
import com.gollagolla.wishlist.application.WishlistService;
import com.gollagolla.wishlist.application.dto.WishlistItemDto;
import com.gollagolla.wishlist.ui.dto.CreateWishlistRequest;
import com.gollagolla.wishlist.ui.dto.UpdateWishlistVisibilityRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
@AutoConfigureMockMvc(addFilters = false)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WishlistService wishlistService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 내_찜_목록_조회() throws Exception {
        // given
        given(wishlistService.getMyWishlists(null))
                .willReturn(List.of(WishlistItemDto.of(
                        1L,
                        "장소1",
                        true,
                        "url",
                        BigDecimal.valueOf(4.5),
                        "address",
                        PoiCategory.ATTRACTION)));

        // when & then
        mockMvc.perform(get("/api/v1/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].poiId").value(1L))
                .andExpect(jsonPath("$[0].name").value("장소1"))
                .andExpect(jsonPath("$[0].isPublic").value(true));
    }

    @Test
    void 찜_추가() throws Exception {
        // given
        CreateWishlistRequest request = new CreateWishlistRequest(10L, true);

        // when & then
        mockMvc.perform(post("/api/v1/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(wishlistService).createWishlist(null, 10L, true);
    }

    @Test
    void 찜_추가_시_POI_ID가_없으면_400_반환() throws Exception {
        // given
        CreateWishlistRequest request = new CreateWishlistRequest(null, true);

        // when & then
        mockMvc.perform(post("/api/v1/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 찜_추가_시_공개_여부가_없으면_400_반환() throws Exception {
        // given
        CreateWishlistRequest request = new CreateWishlistRequest(10L, null);

        // when & then
        mockMvc.perform(post("/api/v1/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 찜_공개_여부를_수정() throws Exception {
        // given
        UpdateWishlistVisibilityRequest request = new UpdateWishlistVisibilityRequest(false);

        // when & then
        mockMvc.perform(patch("/api/v1/wishlist/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(wishlistService).updateVisibility(null, 10L, false);
    }

    @Test
    void 찜_공개_여부_수정_시_공개_여부가_없으면_400_반환() throws Exception {
        // given
        UpdateWishlistVisibilityRequest request = new UpdateWishlistVisibilityRequest(null);

        // when & then
        mockMvc.perform(patch("/api/v1/wishlist/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 찜_삭제() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/wishlist/10"))
                .andExpect(status().isNoContent());

        verify(wishlistService).deleteWishlist(null, 10L);
    }
}
