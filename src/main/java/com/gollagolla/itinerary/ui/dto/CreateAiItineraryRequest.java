package com.gollagolla.itinerary.ui.dto;

import com.gollagolla.itinerary.domain.TransportMode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class CreateAiItineraryRequest {
    @NotBlank
    private String title;

    @NotNull
    private Long regionId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private TransportMode transportMode;

    @NotEmpty
    private List<Long> poiIds;

    @AssertTrue(message = "시작일은 종료일보다 빨라야 합니다.")
    public boolean isValidPeriod() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !startDate.isAfter(endDate);
    }
}
