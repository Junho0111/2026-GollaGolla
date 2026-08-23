package com.gollagolla.wishlist.domain.event;

import lombok.Getter;

@Getter
public class WishlistCreatedEvent {
    private final Long poiId;

    public WishlistCreatedEvent(Long poiId) {
        this.poiId = poiId;
    }
}
