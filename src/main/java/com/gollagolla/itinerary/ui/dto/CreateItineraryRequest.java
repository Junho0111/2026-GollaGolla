package com.gollagolla.itinerary.ui.dto;

import com.gollagolla.itinerary.domain.TransportMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateItineraryRequest {

    @NotBlank(message = "일정 제목은 필수입니다.")
    @Size(max = 100, message = "일정 제목은 최대 100자까지 입력 가능합니다.")
    private String title;

    @NotNull(message = "지역 ID는 필수입니다.")
    private Long regionId;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;

    @NotNull(message = "이동 수단은 필수입니다.")
    private TransportMode transportMode;

    private CreateItineraryRequest() {}

    public CreateItineraryRequest(String title, Long regionId, LocalDate startDate, LocalDate endDate, TransportMode transportMode) {
        this.title = title;
        this.regionId = regionId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.transportMode = transportMode;
    }
}
