package com.gollagolla.poi.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByDepth(Integer depth);

    List<Region> findByParentId(Long parentId);
}
