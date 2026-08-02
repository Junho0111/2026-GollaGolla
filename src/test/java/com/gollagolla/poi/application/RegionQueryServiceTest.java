package com.gollagolla.poi.application;

import com.gollagolla.poi.domain.Region;
import com.gollagolla.poi.domain.RegionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RegionQueryServiceTest {

    @InjectMocks
    RegionQueryService regionQueryService;

    @Mock
    RegionRepository regionRepository;

    @Test
    void getRegions_depth로_조회() {
        // given
        Integer depth = 1;
        Region region = Region.builder()
                .name("region")
                .depth(1)
                .build();

        given(regionRepository.findByDepth(depth)).willReturn(List.of(region));

        // when
        List<Region> result = regionQueryService.findRegions(null, depth);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("region");
        assertThat(result.get(0).getDepth()).isEqualTo(1);
    }

    @Test
    void findRegions_parentId로_조회() {
        // given
        Long parentId = 1L;
        Region region = Region.builder()
                .name("child")
                .depth(1)
                .build();

        given(regionRepository.findByParentId(parentId)).willReturn(List.of(region));

        // when
        List<Region> result = regionQueryService.findRegions(parentId, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("child");
    }

    @Test
    void findRegions_둘다_없으면_depth_1_조회() {
        // given
        Region region = Region.builder()
                .name("default")
                .depth(1)
                .build();

        given(regionRepository.findByDepth(1)).willReturn(List.of(region));

        // when
        List<Region> result = regionQueryService.findRegions(null, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("default");
    }
}
