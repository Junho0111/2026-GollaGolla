package com.gollagolla.poi.application;

import com.gollagolla.poi.application.dto.PoiCardDto;
import com.gollagolla.poi.application.dto.PoiSearchResultDto;
import com.gollagolla.poi.domain.Poi;
import com.gollagolla.poi.domain.PoiCategory;
import com.gollagolla.poi.domain.PoiRepository;
import com.gollagolla.poi.domain.QPoiRepository;
import com.gollagolla.poi.ui.dto.PoiDetailResponse;
import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import com.gollagolla.tourapi.client.TourApiClient;
import com.gollagolla.tourapi.client.dto.DetailIntroResponse;
import com.gollagolla.wishlist.domain.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoiQueryService {

    private final PoiRepository poiRepository;
    private final QPoiRepository qPoiRepository;
    private final WishlistRepository wishlistRepository;
    private final TourApiClient tourApiClient;

    @Transactional(readOnly = true)
    public Page<PoiCardDto> getPoiFeed(Long regionId, PoiCategory category, Pageable pageable, Long memberId) {
        return qPoiRepository.findPoiFeed(regionId, category, pageable, memberId);
    }

    @Transactional(readOnly = true)
    public List<PoiSearchResultDto> searchByKeyword(String keyword) {
        return qPoiRepository.searchByKeyword(keyword);
    }

    @Transactional
    public PoiDetailResponse getPoiDetail(Long poiId, Long memberId) {
        Poi poi = poiRepository.findById(poiId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POI_NOT_FOUND, "poiId=" + poiId));

        fetchAndApplyDetailIfAbsent(poi);

        poiRepository.increaseViewCount(poiId);

        boolean isWished = memberId != null && wishlistRepository.existsByMemberIdAndPoiId(memberId, poiId);

        return PoiDetailResponse.of(
                poi.getId(),
                poi.getName(),
                poi.getRating().getScore(),
                poi.getCoordinate().getLat(),
                poi.getCoordinate().getLng(),
                poi.getThumbnailUrl(),
                poi.getImageUrls(),
                poi.getOpenHours(),
                poi.getBreakTime(),
                poi.getClosedDays(),
                poi.getNaverMapUrl(),
                poi.getCategory(),
                poi.getDescription(),
                isWished
        );
    }

    private void fetchAndApplyDetailIfAbsent(Poi poi) {
        if (poi.getSourceId() == null || poi.getContentTypeId() == null) {
            return;
        }

        if (poi.getOpenHours() != null || poi.getClosedDays() != null) {
            return;
        }

        try {
            DetailIntroResponse response = tourApiClient
                    .fetchDetailIntro(poi.getSourceId(), Integer.parseInt(poi.getContentTypeId()));

            if (response == null || response.getItem() == null) {
                return;
            }

            String openHours = response.getItem().getOpenHours();
            String closedDays = response.getItem().getClosedDays();

            Map<String, String> openHoursMap = null;
            if (openHours != null && !openHours.isBlank()) {
                openHoursMap = Map.of("info", openHours);
            }

            poi.updateDetail(openHoursMap, closedDays);

        } catch (Exception e) {
            log.warn("[PoiQueryService] TourAPI 상세 정보 조회 실패 - poiId={}, sourceId={}, error={}",
                    poi.getId(), poi.getSourceId(), e.getMessage());
        }
    }
}
