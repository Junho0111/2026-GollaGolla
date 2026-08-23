package com.gollagolla.wishlist.domain.event;

import lombok.Getter;

@Getter
public class WishlistDeletedEvent {
    private final Long poiId;

    public WishlistDeletedEvent(Long poiId) {
        this.poiId = poiId;
    }
}
