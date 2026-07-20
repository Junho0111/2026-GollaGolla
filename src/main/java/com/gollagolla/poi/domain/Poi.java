package com.gollagolla.poi.domain;

import com.gollagolla.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "poi")
@Getter
@ToString(of = {"id", "name", "category", "regionId"})
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Poi extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private PoiCategory category;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Embedded
    private Coordinate coordinate;

    @Embedded
    private Rating rating = Rating.zero();

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "wish_count", nullable = false)
    private Integer wishCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "popularity_score", precision = 10, scale = 4, nullable = false)
    private BigDecimal popularityScore = BigDecimal.ZERO;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_urls", columnDefinition = "json")
    private List<String> imageUrls;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "open_hours", columnDefinition = "json")
    private Map<String, String> openHours;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "break_time", columnDefinition = "json")
    private Map<String, String> breakTime;

    @Column(name = "closed_days", length = 30)
    private String closedDays;

    @Column(name = "naver_map_url", length = 500)
    private String naverMapUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private DataSource source;

    @Builder
    public Poi(Long regionId, PoiCategory category, String name, String description,
               Coordinate coordinate, String thumbnailUrl, List<String> imageUrls,
               Map<String, String> openHours, Map<String, String> breakTime,
               String closedDays, String naverMapUrl, DataSource source) {
        this.regionId = regionId;
        this.category = category;
        this.name = name;
        this.description = description;
        this.coordinate = coordinate;
        this.rating = Rating.zero();
        this.thumbnailUrl = thumbnailUrl;
        this.imageUrls = imageUrls;
        this.openHours = openHours;
        this.breakTime = breakTime;
        this.closedDays = closedDays;
        this.naverMapUrl = naverMapUrl;
        this.source = source;
    }

    public void applyRating(int score) {
        this.rating = this.rating.add(score, this.reviewCount);
        this.reviewCount++;
    }
}