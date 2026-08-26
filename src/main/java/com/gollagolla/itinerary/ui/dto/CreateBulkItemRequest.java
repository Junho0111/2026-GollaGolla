package com.gollagolla.itinerary.ui.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateBulkItemRequest {

    @Valid
    @NotNull(message = "항목 목록은 필수입니다.")
    @NotEmpty(message = "항목 목록은 비어있을 수 없습니다.")
    private List<BulkItemDto> items;

    private CreateBulkItemRequest() {}

    public CreateBulkItemRequest(List<BulkItemDto> items) {
        this.items = items;
    }
}
