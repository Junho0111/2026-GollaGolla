package com.gollagolla.poi.application;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.poi.application.dto.PoiCardDto;
import com.gollagolla.poi.application.dto.PoiSearchResultDto;
import com.gollagolla.poi.domain.Coordinate;
import com.gollagolla.poi.domain.DataSource;
import com.gollagolla.poi.domain.Poi;
import com.gollagolla.poi.domain.PoiCategory;
import com.gollagolla.poi.domain.PoiRepository;
import com.gollagolla.poi.domain.QPoiRepository;
import com.gollagolla.poi.ui.dto.PoiDetailResponse;
import com.gollagolla.wishlist.domain.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PoiQueryServiceTest {

    @InjectMocks
    PoiQueryService poiQueryService;

    @Mock
    PoiRepository poiRepository;

    @Mock
    QPoiRepository qPoiRepository;

    @Mock
    WishlistRepository wishlistRepository;

    @Test
    void getPoiFeed_성공() {
        // given
        Long regionId = 1L;
        PoiCategory category = PoiCategory.ATTRACTION;
        Pageable pageable = PageRequest.of(0, 20);
        Long memberId = 1L;

        PoiCardDto dto = new PoiCardDto(1L, "poi", "url", null, 0, 0, null, false);
        Page<PoiCardDto> page = new PageImpl<>(List.of(dto));

        given(qPoiRepository.findPoiFeed(regionId, category, pageable, memberId)).willReturn(page);

        // when
        Page<PoiCardDto> result = poiQueryService.getPoiFeed(regionId, category, pageable, memberId);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("poi");
    }

    @Test
    void searchByKeyword_성공() {
        // given
        String keyword = "test";
        PoiSearchResultDto dto = new PoiSearchResultDto(1L, "test poi", PoiCategory.ATTRACTION, "test region");
        given(qPoiRepository.searchByKeyword(keyword)).willReturn(List.of(dto));

        // when
        List<PoiSearchResultDto> result = poiQueryService.searchByKeyword(keyword);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test poi");
    }

    @Test
    void getPoiDetail_성공_조회수_증가() {
        // given
        Long poiId = 1L;
        Poi poi = Poi.builder()
                .regionId(1L)
                .category(PoiCategory.ATTRACTION)
                .name("detail poi")
                .coordinate(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO))
                .source(DataSource.INTERNAL)
                .build();
        
        willDoNothing().given(poiRepository).increaseViewCount(poiId);
        given(poiRepository.findById(poiId)).willReturn(Optional.of(poi));

        // when
        PoiDetailResponse result = poiQueryService.getPoiDetail(poiId, null);

        // then
        assertThat(result.getName()).isEqualTo("detail poi");
        then(poiRepository).should().increaseViewCount(poiId);
    }

    @Test
    void getPoiDetail_존재하지_않으면_예외_발생() {
        // given
        Long poiId = 1L;
        given(poiRepository.findById(poiId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> poiQueryService.getPoiDetail(poiId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 장소입니다.");
    }
}
