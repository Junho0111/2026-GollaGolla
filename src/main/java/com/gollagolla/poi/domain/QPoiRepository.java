package com.gollagolla.poi.domain;

import com.gollagolla.poi.application.dto.PoiCardDto;
import com.gollagolla.poi.application.dto.PoiSearchResultDto;
import com.gollagolla.poi.application.dto.QPoiCardDto;
import com.gollagolla.poi.application.dto.QPoiSearchResultDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.gollagolla.poi.domain.QPoi.poi;
import static com.gollagolla.poi.domain.QRegion.region;
import static com.querydsl.core.types.dsl.Expressions.asBoolean;

@Repository
@RequiredArgsConstructor
public class QPoiRepository {

    private final JPAQueryFactory queryFactory;

    public Page<PoiCardDto> findPoiFeed(Long regionId, PoiCategory category, Pageable pageable, Long memberId) {
        List<PoiCardDto> content = queryFactory
                .select(new QPoiCardDto(
                        poi.id,
                        poi.name,
                        poi.thumbnailUrl,
                        poi.rating.score,
                        poi.reviewCount,
                        poi.wishCount,
                        poi.popularityScore,
                        asBoolean(false) // Wishlist 구현 후 memberId를 이용한 wishlist join 처리
                ))
                .from(poi)
                .where(eqRegionId(regionId), eqCategory(category))
                .orderBy(poi.popularityScore.desc(), poi.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(poi.count())
                .from(poi)
                .where(eqRegionId(regionId), eqCategory(category));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public List<PoiSearchResultDto> searchByKeyword(String keyword) {
        return queryFactory
                .select(new QPoiSearchResultDto(
                        poi.id,
                        poi.name,
                        poi.category,
                        region.name
                ))
                .from(poi)
                .join(region).on(poi.regionId.eq(region.id))
                .where(
                        poi.name.containsIgnoreCase(keyword)
                        .or(region.name.containsIgnoreCase(keyword))
                )
                .fetch();
    }

    private BooleanExpression eqRegionId(Long regionId) {
        if (regionId == null) {
            return null;
        }
        return poi.regionId.eq(regionId);
    }

    private BooleanExpression eqCategory(PoiCategory category) {
        if (category == null) {
            return null;
        }
        return poi.category.eq(category);
    }
}
