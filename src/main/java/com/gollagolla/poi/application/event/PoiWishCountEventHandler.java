package com.gollagolla.poi.application.event;

import com.gollagolla.poi.domain.PoiRepository;
import com.gollagolla.wishlist.domain.event.WishlistCreatedEvent;
import com.gollagolla.wishlist.domain.event.WishlistDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PoiWishCountEventHandler {

    private final PoiRepository poiRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onWishlistCreated(WishlistCreatedEvent event) {
        poiRepository.increaseWishCount(event.getPoiId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onWishlistDeleted(WishlistDeletedEvent event) {
        poiRepository.decreaseWishCount(event.getPoiId());
    }
}
