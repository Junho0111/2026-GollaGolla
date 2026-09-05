package com.gollagolla.itinerary.application;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import com.gollagolla.itinerary.domain.*;
import com.gollagolla.itinerary.ui.dto.*;
import com.gollagolla.poi.domain.Poi;
import com.gollagolla.poi.domain.PoiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final PoiRepository poiRepository;
    private final ShareTokenService shareTokenService;

    @Value("${app.share-base-url}")
    private String shareBaseUrl;

    @Transactional
    public Long createItinerary(Long memberId, CreateItineraryRequest request) {
        TravelPeriod period = new TravelPeriod(request.getStartDate(), request.getEndDate());
        Itinerary itinerary = Itinerary.builder()
                .memberId(memberId)
                .title(request.getTitle())
                .regionId(request.getRegionId())
                .travelPeriod(period)
                .transportMode(request.getTransportMode())
                .genType(GenType.MANUAL)
                .build();
        return itineraryRepository.save(itinerary).getId();
    }

    @Transactional(readOnly = true)
    public ItineraryResponse getItinerary(Long memberId, Long id) {
        Itinerary itinerary = getItineraryWithAuth(id, memberId);
        return mapToResponse(itinerary);
    }

    @Transactional(readOnly = true)
    public List<ItineraryListItemResponse> getItineraries(Long memberId) {
        return itineraryRepository.findAllByMemberIdOrderByIdDesc(memberId).stream()
                .map(ItineraryListItemResponse::from)
                .toList();
    }

    @Transactional
    public void addItem(Long memberId, Long id, CreateItemRequest request) {
        Itinerary itinerary = getItineraryWithAuth(id, memberId);

        ItineraryItem item = ItineraryItem.builder()
                .poiId(request.getPoiId())
                .dayNo(request.getDayNo())
                .seq(request.getSeq())
                .build();

        itinerary.addItem(item);
    }

    @Transactional
    public void addItemsBulk(Long memberId, Long id, CreateBulkItemRequest request) {
        Itinerary itinerary = getItineraryWithAuth(id, memberId);

        for (BulkItemDto dto : request.getItems()) {
            ItineraryItem item = ItineraryItem.builder()
                    .poiId(dto.getPoiId())
                    .dayNo(dto.getDayNo())
                    .seq(dto.getSeq())
                    .startTime(dto.getStartTime())
                    .isAnchor(dto.getIsAnchor())
                    .build();

            itinerary.addItem(item);
        }
    }

    @Transactional
    public void updateItem(Long memberId, Long id, Long itemId, UpdateItemRequest request) {
        Itinerary itinerary = getItineraryWithAuth(id, memberId);

        ItineraryItem item = itinerary.getItems().stream()
                .filter(itineraryItem -> itineraryItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ITINERARY_ITEM_NOT_FOUND, "itemId=" + itemId));

        item.reschedule(request.getDayNo(), request.getSeq(), request.getStartTime(), request.getEndTime(), request.getIsAnchor());
    }

    @Transactional
    public void updateItemMemo(Long memberId, Long id, Long itemId, String memo) {
        Itinerary itinerary = getItineraryWithAuth(id, memberId);

        ItineraryItem item = itinerary.getItems().stream()
                .filter(itineraryItem -> itineraryItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ITINERARY_ITEM_NOT_FOUND, "itemId=" + itemId));

        item.updateMemo(memo);
    }

    @Transactional
    public void deleteItem(Long memberId, Long id, Long itemId) {
        Itinerary itinerary = getItineraryWithAuth(id, memberId);

        ItineraryItem item = itinerary.getItems().stream()
                .filter(itineraryItem -> itineraryItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ITINERARY_ITEM_NOT_FOUND, "itemId=" + itemId));

        itinerary.getItems().remove(item);
    }

    @Transactional
    public ShareResponse shareItinerary(Long memberId, Long id) {
        Itinerary itinerary = getItineraryWithAuth(id, memberId);

        if (itinerary.getShareToken() != null) {
            return ShareResponse.of(itinerary.getShareToken(), shareBaseUrl + itinerary.getShareToken());
        }

        for (int i = 0; i < 3; i++) {
            try {
                String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                shareTokenService.saveShareTokenWithNewTransaction(id, token);

                return ShareResponse.of(token, shareBaseUrl + token);
            } catch (DataIntegrityViolationException e) {
                log.warn("공유 토큰 충돌 발생, 재시도 = {}회", i + 1);
                if (i == 2) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 생성 실패");
                }
            }
        }
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 생성 실패");
    }

    @Transactional(readOnly = true)
    public ItineraryResponse getSharedItinerary(String token) {
        Itinerary itinerary = itineraryRepository.findByShareToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHARE_TOKEN_NOT_FOUND));
        return mapToResponse(itinerary);
    }

    @Transactional
    public void deleteItinerary(Long memberId, Long id) {
        Itinerary itinerary = getItineraryWithAuth(id, memberId);
        itineraryRepository.delete(itinerary);
    }

    private Itinerary getItineraryWithAuth(Long id, Long memberId) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITINERARY_NOT_FOUND, "id=" + id));

        if (!itinerary.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ITINERARY_FORBIDDEN, "memberId=" + memberId);
        }
        return itinerary;
    }

    private ItineraryResponse mapToResponse(Itinerary itinerary) {
        List<Long> poiIds = itinerary.getItems().stream()
                .map(ItineraryItem::getPoiId)
                .distinct()
                .toList();

        Map<Long, Poi> poiMap = poiRepository.findAllById(poiIds).stream()
                .collect(Collectors.toMap(Poi::getId, poi -> poi));

        Map<Integer, List<ItineraryItem>> groupedByDay = itinerary.getItems().stream()
                .collect(Collectors.groupingBy(ItineraryItem::getDayNo));

        List<ItineraryDayResponse> days = groupedByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<ItineraryItemResponse> itemResponses = entry.getValue().stream()
                            .sorted(Comparator.comparing(ItineraryItem::getSeq))
                            .map(item -> {
                                Poi poi = poiMap.get(item.getPoiId());
                                String poiName = poi != null ? poi.getName() : "Unknown";
                                String thumbnailUrl = poi != null ? poi.getThumbnailUrl() : null;

                                return ItineraryItemResponse.of(
                                        item.getId(),
                                        item.getPoiId(),
                                        poiName,
                                        thumbnailUrl,
                                        item.getSeq(),
                                        item.getIsAnchor(),
                                        item.getStartTime(),
                                        item.getEndTime(),
                                        item.getMemo()
                                );
                            })
                            .toList();
                    return ItineraryDayResponse.of(entry.getKey(), itemResponses);
                })
                .toList();

        return ItineraryResponse.of(itinerary.getId(), itinerary.getTitle(), days);
    }
}
