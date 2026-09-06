package com.gollagolla.tourapi.application;

import com.gollagolla.poi.domain.Region;
import com.gollagolla.poi.domain.RegionRepository;
import com.gollagolla.tourapi.TourApiProperties;
import com.gollagolla.tourapi.client.TourApiClient;
import com.gollagolla.tourapi.client.dto.AreaCodeResponse.AreaCodeItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionSyncService {

    private final TourApiClient tourApiClient;
    private final RegionRepository regionRepository;
    private final TourApiProperties properties;

    @Transactional
    public void sync() {
        List<String> targetAreaCodes = properties.getTargetAreaCodes();
        log.info("[TourAPI] Region 동기화 시작 - 대상 areaCode: {}", targetAreaCodes);

        List<AreaCodeItem> allAreaItems = tourApiClient.fetchAreaCodes().getItems();

        for (String targetCode : targetAreaCodes) {
            allAreaItems.stream()
                    .filter(item -> item.code().equals(targetCode))
                    .findFirst()
                    .ifPresent(item -> syncDepth1(item, targetCode));
        }

        log.info("[TourAPI] Region 동기화 완료");
    }

    private void syncDepth1(AreaCodeItem areaItem, String areaCode) {
        if (regionRepository.existsByNameAndDepth(areaItem.name(), 1)) {
            log.info("[TourAPI] 이미 존재하는 Depth1 지역 skip: {}", areaItem.name());
            syncDepth2(areaCode, findDepth1Id(areaItem.name()));
            return;
        }

        Region depth1 = regionRepository.save(Region.builder()
                .name(areaItem.name())
                .depth(1)
                .areaCode(areaCode)
                .build());
        log.info("[TourAPI] Depth1 지역 저장: {} (areaCode={})", depth1.getName(), areaCode);

        syncDepth2(areaCode, depth1.getId());
    }

    private void syncDepth2(String areaCode, Long parentId) {
        List<AreaCodeItem> depth2Items = tourApiClient.fetchDepth2Codes(areaCode).getItems();

        for (AreaCodeItem depth2Item : depth2Items) {
            if (regionRepository.existsByNameAndParentId(depth2Item.name(), parentId)) {
                log.info("[TourAPI] 이미 존재하는 Depth2 지역 skip: {}", depth2Item.name());
                continue;
            }

            Region depth2 = regionRepository.save(Region.builder()
                    .name(depth2Item.name())
                    .depth(2)
                    .parentId(parentId)
                    .areaCode(depth2Item.code())
                    .build());
            log.info("[TourAPI] Depth2 지역 저장: {} (areaCode={})", depth2.getName(), depth2Item.code());
        }
    }

    private Long findDepth1Id(String name) {
        return regionRepository.findByNameAndDepth(name, 1)
                .map(Region::getId)
                .orElseThrow(() -> new IllegalStateException("Depth1 지역을 찾을 수 없음: " + name));
    }
}
