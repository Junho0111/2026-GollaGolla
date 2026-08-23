package com.gollagolla.poi.application.event;

import com.gollagolla.poi.domain.PoiRepository;
import com.gollagolla.wishlist.domain.event.WishlistCreatedEvent;
import com.gollagolla.wishlist.domain.event.WishlistDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PoiWishCountEventHandler {

    private final PoiRepository poiRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onWishlistCreated(WishlistCreatedEvent event) {
        log.info("[PoiWishCountEventHandler] 찜 추가 커밋 확인, poiId={} wishCount + 1 실행", event.getPoiId());
        poiRepository.increaseWishCount(event.getPoiId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onWishlistDeleted(WishlistDeletedEvent event) {
        log.info("[PoiWishCountEventHandler] 찜 삭제 커밋 확인, poiId={} wishCount - 1 실행", event.getPoiId());
        poiRepository.decreaseWishCount(event.getPoiId());
    }
}
