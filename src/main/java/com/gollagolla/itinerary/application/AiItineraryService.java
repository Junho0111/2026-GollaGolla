package com.gollagolla.itinerary.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import com.gollagolla.itinerary.application.AiScheduleResult.AiScheduleDay;
import com.gollagolla.itinerary.domain.*;
import com.gollagolla.itinerary.ui.dto.CreateAiItineraryRequest;
import com.gollagolla.itinerary.ui.dto.CreateAiItineraryResponse;
import com.gollagolla.itinerary.ui.dto.ItineraryResponse;
import com.gollagolla.poi.domain.Poi;
import com.gollagolla.poi.domain.PoiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gollagolla.itinerary.application.AiScheduleResult.*;
import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiItineraryService {

    private final ChatClient aiChatClient;
    private final PoiRepository poiRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryService itineraryService;
    private final ObjectMapper aiObjectMapper;

    @Transactional
    public CreateAiItineraryResponse createAiItinerary(Long memberId, CreateAiItineraryRequest request) {

        List<Poi> pois = poiRepository.findAllById(request.getPoiIds());
        if (pois.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "POI가 존재하지 않습니다.");
        }

        long totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        String poiJsonData = preparePoiContext(pois);

        BeanOutputConverter<AiScheduleResult> converter = new BeanOutputConverter<>(AiScheduleResult.class);
        String response = promptAndCallLlm(totalDays, request, poiJsonData, converter);

        AiScheduleResult result = parseAndValidateResponse(response, converter, pois);

        TravelPeriod period = new TravelPeriod(request.getStartDate(), request.getEndDate());
        Itinerary itinerary = Itinerary.builder()
                .memberId(memberId)
                .title(request.getTitle())
                .regionId(request.getRegionId())
                .travelPeriod(period)
                .transportMode(request.getTransportMode())
                .genType(GenType.AI)
                .build();

        itineraryRepository.save(itinerary);

        for (AiScheduleDay day : result.days()) {
            for (AiScheduleItem item : day.items()) {
                ItineraryItem itineraryItem = ItineraryItem.builder()
                        .poiId(item.poiId())
                        .dayNo(day.dayNo())
                        .seq(item.seq())
                        .startTime(item.startTime())
                        .endTime(item.endTime())
                        .isAnchor(true)
                        .build();
                itinerary.addItem(itineraryItem);
            }
        }

        itineraryRepository.saveAndFlush(itinerary);

        ItineraryResponse itineraryResponse = itineraryService.getItinerary(memberId, itinerary.getId());

        return new CreateAiItineraryResponse(itineraryResponse, result.explanation());
    }

    private String preparePoiContext(List<Poi> pois) {
        List<Map<String, Object>> poiList = pois.stream()
                .map(poi -> Map.of(
                "poiId", poi.getId(),
                "name", poi.getName(),
                "latitude", poi.getCoordinate() != null ? poi.getCoordinate().getLat() : null,
                "longitude", poi.getCoordinate() != null ? poi.getCoordinate().getLng() : null,
                "openHours", poi.getOpenHours() != null ? poi.getOpenHours() : "Unknown",
                "category", poi.getCategory()
                ))
                .collect(Collectors.toList());
        try {
            return aiObjectMapper.writeValueAsString(poiList);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize POI context", e);
            return "[]";
        }
    }

    private String promptAndCallLlm(long totalDays, CreateAiItineraryRequest request, String poiJsonData,
                                    BeanOutputConverter<AiScheduleResult> converter
    ) {
        String format = converter.getFormat();

        String prompt = """
            당신은 전문가 수준의 여행 플래너입니다. 사용자가 다음 장소(POI)들을 방문하고자 합니다.
            여행 기간은 총 %d일(%s 부터 %s 까지)입니다.
            이동 수단: %s.

            다음은 방문할 장소들의 위치(위도, 경도)와 영업시간 정보입니다: %s.

            당신의 임무는 제공된 모든 장소들을 최적의 일일 일정으로 배치하는 것입니다.
            동선을 고려하여 가까운 장소들을 같은 날짜에 묶어주세요.
            반드시 사용자가 제공한 장소들만 사용해야 합니다.
            새로운 장소를 임의로 추가하지 마세요.
            일정을 이렇게 짠 이유를 explanation 필드에 한국어로 친절하게 설명해주세요.

            %s
            """;

        String finalPrompt = String.format(prompt,
                totalDays,
                request.getStartDate(),
                request.getEndDate(),
                request.getTransportMode(),
                poiJsonData,
                format);

        log.info("Gemini에 프롬프트를 전송하는 중: {}", finalPrompt);

        return aiChatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();
    }

    private AiScheduleResult parseAndValidateResponse(String response, BeanOutputConverter<AiScheduleResult> converter, List<Poi> pois) {
        if (response == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LLM 응답이 비어있습니다.");
        }

        AiScheduleResult result = converter.convert(response);

        if (result == null || result.days() == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LLM 응답을 파싱할 수 없습니다.");
        }

        Set<Long> validPoiIds = pois.stream()
                .map(Poi::getId)
                .collect(Collectors.toSet());

        for (AiScheduleResult.AiScheduleDay day : result.days()) {
            for (AiScheduleResult.AiScheduleItem item : day.items()) {
                if (!validPoiIds.contains(item.poiId())) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                            "LLM이 요청하지 않은 poiId를 반환했습니다: " + item.poiId());
                }
            }
        }
        
        return result;
    }
}
