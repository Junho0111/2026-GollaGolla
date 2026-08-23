package com.gollagolla.wishlist.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByMemberIdAndPoiId(Long memberId, Long poiId);

    List<Wishlist> findByMemberId(Long memberId);
}
