package com.gollagolla.tourapi.client;

import com.gollagolla.tourapi.TourApiProperties;
import com.gollagolla.tourapi.client.dto.AreaCodeResponse;
import com.gollagolla.tourapi.client.dto.AreaBasedListResponse;
import com.gollagolla.tourapi.client.dto.DetailIntroResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourApiClient {

    private final RestClient restClient;
    private final TourApiProperties properties;

    private static final String MOBILE_OS = "ETC";
    private static final String MOBILE_APP = "GollaGolla";
    private static final String TYPE = "json";
    private static final int NUM_OF_ROWS = 100;

    /**
     * 시/도 광역 지역 코드 목록 조회
     * 관광공사 areaCode API: /areaCode2
     */
    public AreaCodeResponse fetchAreaCodes() {
        return restClient.get()
                .uri(buildBaseUri("/areaCode2")
                        .queryParam("pageNo", 1)
                        .build(true)
                        .toUri())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("[TourAPI] 지역 코드 조회 실패: {} {}", response.getStatusCode(), response.getStatusText());
                })
                .body(AreaCodeResponse.class);
    }

    /**
     * 특정 시/도 하위의 시/군/구 코드 목록 조회
     * 관광공사 areaCode API: /areaCode2 (areaCode 파라미터 포함)
     */
    public AreaCodeResponse fetchDepth2Codes(String areaCode) {
        return restClient.get()
                .uri(buildBaseUri("/areaCode2")
                        .queryParam("pageNo", 1)
                        .queryParam("areaCode", areaCode)
                        .build(true).toUri())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("[TourAPI] 시군구 코드 조회 실패: {} {}", response.getStatusCode(), response.getStatusText());
                })
                .body(AreaCodeResponse.class);
    }

    /**
     * 특정 지역의 관광지 목록 조회 (페이지 단위)
     * 관광공사 areaBasedList API: /areaBasedList2
     */
    public AreaBasedListResponse fetchPoiList(String areaCode, int contentTypeId, int pageNo) {
        return restClient.get()
                .uri(buildBaseUri("/areaBasedList2")
                        .queryParam("pageNo", pageNo)
                        .queryParam("areaCode", areaCode)
                        .queryParam("contentTypeId", contentTypeId)
                        .build(true).toUri())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("[TourAPI] 장소(POI) 목록 조회 실패: {} {}", response.getStatusCode(), response.getStatusText());
                })
                .body(AreaBasedListResponse.class);
    }

    /**
     * 특정 장소의 상세 정보(영업시간, 휴무일 등) 조회
     * 관광공사 detailIntro API: /detailIntro2
     */
    public DetailIntroResponse fetchDetailIntro(String contentId, int contentTypeId) {
        return restClient.get()
                .uri(buildBaseUri("/detailIntro2")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .build(true).toUri())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("[TourAPI] 상세 정보 조회 실패: {} {}", response.getStatusCode(), response.getStatusText());
                })
                .body(DetailIntroResponse.class);
    }

    private UriComponentsBuilder buildBaseUri(String path) {
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl() + path)
                .queryParam("serviceKey", properties.getServiceKey())
                .queryParam("numOfRows", NUM_OF_ROWS)
                .queryParam("MobileOS", MOBILE_OS)
                .queryParam("MobileApp", MOBILE_APP)
                .queryParam("_type", TYPE);
    }
}
