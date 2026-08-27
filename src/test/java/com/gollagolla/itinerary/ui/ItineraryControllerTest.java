package com.gollagolla.itinerary.ui;

import tools.jackson.databind.ObjectMapper;
import com.gollagolla.auth.support.JwtTokenProvider;
import com.gollagolla.itinerary.application.ItineraryService;
import com.gollagolla.itinerary.domain.TransportMode;
import com.gollagolla.itinerary.ui.dto.CreateItemRequest;
import com.gollagolla.itinerary.ui.dto.CreateItineraryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItineraryController.class)
@AutoConfigureMockMvc(addFilters = false)
class ItineraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItineraryService itineraryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 일정_생성_시_제목이_없으면_400_반환() throws Exception {
        // given
        CreateItineraryRequest request = new CreateItineraryRequest(
                "", 1L, LocalDate.now(), LocalDate.now().plusDays(1), TransportMode.CAR);

        // when & then
        mockMvc.perform(post("/api/v1/itineraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 일정_생성_시_제목이_100자를_초과하면_400_반환() throws Exception {
        // given
        String longTitle = "a".repeat(101);
        CreateItineraryRequest request = new CreateItineraryRequest(
                longTitle, 1L, LocalDate.now(), LocalDate.now().plusDays(1), TransportMode.CAR);

        // when & then
        mockMvc.perform(post("/api/v1/itineraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 일정_생성_시_지역_ID가_없으면_400_반환() throws Exception {
        // given
        CreateItineraryRequest request = new CreateItineraryRequest(
                "제목", null, LocalDate.now(), LocalDate.now().plusDays(1), TransportMode.CAR);

        // when & then
        mockMvc.perform(post("/api/v1/itineraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 일정_항목_추가_시_dayNo가_1_미만이면_400_반환() throws Exception {
        // given
        CreateItemRequest request = new CreateItemRequest(1L, 0, 1);

        // when & then
        mockMvc.perform(post("/api/v1/itineraries/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 일정_항목_추가_시_seq가_0_미만이면_400_반환() throws Exception {
        // given
        CreateItemRequest request = new CreateItemRequest(1L, 1, -1);

        // when & then
        mockMvc.perform(post("/api/v1/itineraries/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
