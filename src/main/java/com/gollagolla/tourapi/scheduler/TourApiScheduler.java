package com.gollagolla.tourapi.scheduler;

import com.gollagolla.tourapi.TourApiProperties;
import com.gollagolla.tourapi.application.PoiSyncService;
import com.gollagolla.tourapi.application.RegionSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourApiScheduler {

    private final RegionSyncService regionSyncService;
    private final PoiSyncService poiSyncService;
    private final TourApiProperties tourApiProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        if (!tourApiProperties.isAutoSync()) {
            log.info("[TourAPI] auto-sync=false, 초기 데이터 동기화 건너뜀");
            return;
        }

        log.info("[TourAPI] 초기 데이터 동기화 시작");
        try {
            regionSyncService.sync();
            poiSyncService.sync();
            log.info("[TourAPI] 초기 데이터 동기화 완료");
        } catch (Exception e) {
            log.error("[TourAPI] 초기 데이터 동기화 실패", e);
        }
    }
}
