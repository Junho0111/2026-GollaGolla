package com.gollagolla.poi.domain;

import com.gollagolla.config.EnableJpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(EnableJpaAuditingConfig.class)
class RegionRepositoryTest {

    @Autowired
    RegionRepository regionRepository;

    @Test
    void Depth로_조회() {
        // given
        Region region1 = Region.builder()
                .name("region1")
                .depth(1)
                .build();
        Region savedRegion1 = regionRepository.save(region1);

        Region region2 = Region.builder()
                .name("region2")
                .depth(2)
                .parentId(savedRegion1.getId())
                .build();
        regionRepository.save(region2);

        // when
        List<Region> result = regionRepository.findByDepth(1);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("region1");
    }

    @Test
    void ParentId로_조회() {
        // given
        Region parent = Region.builder()
                .name("parent")
                .depth(1)
                .build();
        Region savedParent = regionRepository.save(parent);

        Region child = Region.builder()
                .name("child")
                .depth(2)
                .parentId(savedParent.getId())
                .build();
        regionRepository.save(child);

        // when
        List<Region> result = regionRepository.findByParentId(savedParent.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("child");
    }
}
