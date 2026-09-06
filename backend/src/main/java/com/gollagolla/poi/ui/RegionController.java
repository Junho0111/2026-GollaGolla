package com.gollagolla.poi.ui;

import com.gollagolla.poi.application.RegionQueryService;
import com.gollagolla.poi.domain.Region;
import com.gollagolla.poi.ui.dto.RegionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionQueryService regionQueryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RegionResponse> getRegions(@RequestParam(required = false) Long parentId, @RequestParam(required = false) Integer depth) {
        List<Region> regions = regionQueryService.findRegions(parentId, depth);
        return regions.stream()
                .map(region -> RegionResponse.of(region.getId(), region.getName(), region.getDepth()))
                .toList();
    }
}
