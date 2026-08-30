package com.gollagolla.poi.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PoiRepository extends JpaRepository<Poi, Long> {

    boolean existsByNameAndRegionId(String name, Long regionId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Poi p SET p.viewCount = p.viewCount + 1 WHERE p.id = :poiId")
    void increaseViewCount(@Param("poiId") Long poiId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Poi p SET p.wishCount = p.wishCount + 1 WHERE p.id = :poiId")
    void increaseWishCount(@Param("poiId") Long poiId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Poi p SET p.wishCount = p.wishCount - 1 WHERE p.id = :poiId AND p.wishCount > 0")
    void decreaseWishCount(@Param("poiId") Long poiId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Poi p SET p.rating.score = :ratingScore, p.reviewCount = :reviewCount, p.popularityScore = :popularityScore WHERE p.id = :poiId")
    void updateSeedStats(@Param("poiId") Long poiId,
                         @Param("ratingScore") BigDecimal ratingScore,
                         @Param("reviewCount") int reviewCount,
                         @Param("popularityScore") BigDecimal popularityScore);
}
