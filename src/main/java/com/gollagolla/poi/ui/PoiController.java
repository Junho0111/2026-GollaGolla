package com.gollagolla.poi.ui;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import com.gollagolla.poi.application.PoiQueryService;
import com.gollagolla.poi.application.dto.PoiCardDto;
import com.gollagolla.poi.domain.PoiCategory;
import com.gollagolla.poi.ui.dto.PoiDetailResponse;
import com.gollagolla.poi.ui.dto.PoiFeedResponse;
import com.gollagolla.poi.ui.dto.PoiSearchResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class PoiController {

    private final PoiQueryService poiQueryService;

    @GetMapping("/pois")
    @ResponseStatus(HttpStatus.OK)
    public PoiFeedResponse getPoiFeed(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) PoiCategory category,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.") @Max(value = 100, message = "size는 100 이하이어야 합니다.") int size,
            @AuthenticationPrincipal Long memberId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PoiCardDto> result = poiQueryService.getPoiFeed(regionId, category, pageable, memberId);
        return PoiFeedResponse.of(result.getContent(), result.getTotalPages());
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public PoiSearchResponse search(@RequestParam("q") String keyword, @RequestParam(defaultValue = "poi") String type) {
        if (!"poi".equalsIgnoreCase(type)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_SEARCH_TYPE, "type=" + type);
        }
        return PoiSearchResponse.of(poiQueryService.searchByKeyword(keyword));
    }

    @GetMapping("/pois/{poiId}")
    @ResponseStatus(HttpStatus.OK)
    public PoiDetailResponse getPoiDetail(@PathVariable Long poiId) {
        return poiQueryService.getPoiDetail(poiId);
    }
}
