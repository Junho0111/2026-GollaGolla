package com.gollagolla.poi.application.event;

import com.gollagolla.poi.domain.PoiRepository;
import com.gollagolla.wishlist.domain.event.WishlistCreatedEvent;
import com.gollagolla.wishlist.domain.event.WishlistDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PoiWishCountEventHandler {

    private final PoiRepository poiRepository;
    private final TransactionTemplate newTransactionTemplate;

    private static final int MAX_RETRY = 3;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWishlistCreated(WishlistCreatedEvent event) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            try {
                newTransactionTemplate.executeWithoutResult(status -> {
                    poiRepository.increaseWishCount(event.getPoiId());
                });
                return;
            } catch (ConcurrencyFailureException e) {
                log.warn("[PoiWishCountEventHandler] 데드락/락 충돌, 재시도 {}/{}: poiId={}", attempt + 1, MAX_RETRY, event.getPoiId());
            }
        }
        log.error("[PoiWishCountEventHandler] 찜수 증가 실패 - 최대 재시도 초과: poiId={}", event.getPoiId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWishlistDeleted(WishlistDeletedEvent event) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            try {
                newTransactionTemplate.executeWithoutResult(status -> {
                    poiRepository.decreaseWishCount(event.getPoiId());
                });
                return;
            } catch (ConcurrencyFailureException e) {
                log.warn("[PoiWishCountEventHandler] 데드락/락 충돌, 재시도 {}/{}: poiId={}", attempt + 1, MAX_RETRY, event.getPoiId());
            }
        }
        log.error("[PoiWishCountEventHandler] 찜수 감소 실패 - 최대 재시도 초과: poiId={}", event.getPoiId());
    }
}
