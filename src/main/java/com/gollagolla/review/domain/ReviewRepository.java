package com.gollagolla.review.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByPoiIdAndMemberId(Long poiId, Long memberId);

    Page<Review> findByPoiId(Long poiId, Pageable pageable);
}
