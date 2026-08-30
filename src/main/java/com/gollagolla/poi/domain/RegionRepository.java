package com.gollagolla.poi.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByDepth(Integer depth);

    List<Region> findByParentId(Long parentId);

    Optional<Region> findByAreaCode(String areaCode);

    Optional<Region> findByNameAndDepth(String name, Integer depth);

    Optional<Region> findByParentIdAndAreaCode(Long parentId, String areaCode);

    boolean existsByNameAndDepth(String name, Integer depth);

    boolean existsByNameAndParentId(String name, Long parentId);
}
