package com.gollagolla.poi.application;

import com.gollagolla.poi.domain.Region;
import com.gollagolla.poi.domain.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionQueryService {

    private final RegionRepository regionRepository;

    @Transactional(readOnly = true)
    public List<Region> findRegions(Long parentId, Integer depth) {
        if (parentId != null) {
            return regionRepository.findByParentId(parentId);
        }

        int targetDepth = 1;
        if (depth != null) {
            targetDepth = depth;
        }

        return regionRepository.findByDepth(targetDepth);
    }
}
