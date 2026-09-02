package com.gollagolla.tourapi.application;

import com.gollagolla.poi.domain.*;
import com.gollagolla.tourapi.client.TourApiClient;
import com.gollagolla.tourapi.client.dto.AreaBasedListResponse;
import com.gollagolla.tourapi.client.dto.AreaBasedListResponse.PoiItem;
import com.gollagolla.tourapi.client.dto.DetailIntroResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoiSyncService {

    private final TourApiClient tourApiClient;
    private final PoiRepository poiRepository;
    private final RegionRepository regionRepository;

    private static final List<Integer> TARGET_CONTENT_TYPE_IDS = List.of(12, 14, 25, 28, 32, 39, 15);

    private static final Map<String, PoiCategory> CATEGORY_MAP = Map.of(
            "12", PoiCategory.ATTRACTION,   // 관광지
            "14", PoiCategory.ATTRACTION,       // 문화시설
            "25", PoiCategory.ACTIVITY,         // 여행코스
            "28", PoiCategory.ACTIVITY,         // 레포츠
            "32", PoiCategory.HOTEL,            // 숙박
            "39", PoiCategory.RESTAURANT,       // 음식점
            "15", PoiCategory.FESTIVAL          // 축제/행사
    );


    @Transactional
    public void sync() {
        List<Region> regions = regionRepository.findByDepth(1);
        log.info("[TourAPI] POI 동기화 시작 - 대상 지역 수: {}", regions.size());

        for (Region region : regions) {
            if (region.getAreaCode() == null) {
                continue;
            }
            for (int contentTypeId : TARGET_CONTENT_TYPE_IDS) {
                syncPoiByType(region, contentTypeId);
            }
        }

        log.info("[TourAPI] POI 동기화 완료");
    }

    private void syncPoiByType(Region region, int contentTypeId) {
        int pageNo = 1;

        while (true) {
            AreaBasedListResponse response = tourApiClient.fetchPoiList(
                    region.getAreaCode(), contentTypeId, pageNo);

            List<PoiItem> items = response.getItems();
            if (items.isEmpty()) {
                break;
            }

            for (PoiItem item : items) {
                saveIfNotExists(item, region);
            }

            if (items.size() < 100) {
                break;
            }
            pageNo++;
        }
    }

    private void saveIfNotExists(PoiItem item, Region depth1Region) {
        Region targetRegion = depth1Region;
        if (item.sigunguCode() != null && !item.sigunguCode().isBlank()) {
            targetRegion = regionRepository.findByParentIdAndAreaCode(depth1Region.getId(), item.sigunguCode())
                    .orElse(depth1Region);
        }

        if (poiRepository.existsByNameAndRegionId(item.title(), targetRegion.getId())) {
            log.debug("[TourAPI] 이미 존재하는 POI skip: {} (regionId={})", item.title(), targetRegion.getId());
            return;
        }

        Optional<BigDecimal> lat = parseBigDecimal(item.lat());
        Optional<BigDecimal> lng = parseBigDecimal(item.lng());

        if (lat.isEmpty() || lng.isEmpty()) {
            log.debug("[TourAPI] 좌표 없는 POI skip: {}", item.title());
            return;
        }

        if (!CATEGORY_MAP.containsKey(item.contentTypeId())) {
            log.debug("[TourAPI] 알 수 없는 카테고리 POI skip: {} (contentTypeId={})", item.title(), item.contentTypeId());
            return;
        }

        PoiCategory category = CATEGORY_MAP.get(item.contentTypeId());
        Coordinate coordinate = new Coordinate(lat.get(), lng.get());
        
        PoiDetail detail = fetchDetailInfo(item);

        Poi poi = buildPoiEntity(item, targetRegion, coordinate, category, detail.openHours(), detail.closedDays());

        poiRepository.save(poi);
        log.debug("[TourAPI] POI 저장: {} (category={})", poi.getName(), category);
    }

    private PoiDetail fetchDetailInfo(PoiItem item) {
        Map<String, String> openHoursMap = null;
        String closedDaysStr = null;
        try {
            Thread.sleep(200);
            DetailIntroResponse detailRes = tourApiClient.fetchDetailIntro(item.contentId(), Integer.parseInt(item.contentTypeId()));
            if (detailRes != null && detailRes.getItem() != null) {
                String openHours = detailRes.getItem().getOpenHours();
                String closedDays = detailRes.getItem().getClosedDays();

                if (openHours != null && !openHours.isBlank()) {
                    openHoursMap = Map.of("info", openHours);
                }

                closedDaysStr = closedDays;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[TourAPI] 스레드 대기 중 인터럽트 발생 - POI: {}", item.title());
        } catch (Exception e) {
            log.warn("[TourAPI] 상세 정보 조회 실패 - POI: {}", item.title());
        }
        return new PoiDetail(openHoursMap, closedDaysStr);
    }

    private Optional<BigDecimal> parseBigDecimal(String value) {
        try {
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new BigDecimal(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Poi buildPoiEntity(PoiItem item, Region region, Coordinate coordinate,
                               PoiCategory category, Map<String, String> openHours, String closedDays
    ) {
        String thumbnail = item.firstImage() != null && !item.firstImage().isBlank()
                ? item.firstImage() : item.firstImage2();

        List<String> images = buildImageList(item.firstImage(), item.firstImage2());

        return Poi.builder()
                .regionId(region.getId())
                .category(category)
                .name(item.title())
                .coordinate(coordinate)
                .address(item.addr1())
                .thumbnailUrl(thumbnail)
                .imageUrls(images)
                .source(DataSource.TOURAPI)
                .openHours(openHours)
                .closedDays(closedDays)
                .build();
    }

    private List<String> buildImageList(String firstImage, String firstImage2) {
        List<String> images = new ArrayList<>();

        if (firstImage != null && !firstImage.isBlank()) {
            images.add(firstImage);
        }
        if (firstImage2 != null && !firstImage2.isBlank()) {
            images.add(firstImage2);
        }
        return images;
    }

    private record PoiDetail(Map<String, String> openHours, String closedDays) {}
}
