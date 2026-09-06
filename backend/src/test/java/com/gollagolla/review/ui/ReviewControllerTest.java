package com.gollagolla.review.ui;

import tools.jackson.databind.ObjectMapper;
import com.gollagolla.auth.support.JwtTokenProvider;
import com.gollagolla.review.application.ReviewCommandService;
import com.gollagolla.review.application.ReviewQueryService;
import com.gollagolla.review.ui.dto.CreateReviewRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewCommandService reviewCommandService;

    @MockitoBean
    private ReviewQueryService reviewQueryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 리뷰_작성_시_평점이_0이면_400_반환() throws Exception {
        // given
        CreateReviewRequest request = new CreateReviewRequest(0, "좋아요");

        // when & then
        mockMvc.perform(post("/api/v1/pois/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 리뷰_작성_시_평점이_6이면_400_반환() throws Exception {
        // given
        CreateReviewRequest request = new CreateReviewRequest(6, "좋아요");

        // when & then
        mockMvc.perform(post("/api/v1/pois/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 리뷰_목록_조회_시_page가_음수면_400_반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/pois/1/reviews")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 리뷰_목록_조회_시_size가_1_미만이면_400_반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/pois/1/reviews")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 리뷰_목록_조회_시_size가_100_초과면_400_반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/pois/1/reviews")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }
}
