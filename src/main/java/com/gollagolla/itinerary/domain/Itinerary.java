package com.gollagolla.itinerary.domain;

import com.gollagolla.global.BaseTimeEntity;
import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.poi.domain.Poi;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import static com.gollagolla.global.exception.ErrorCode.*;
import static com.gollagolla.poi.domain.PoiCategory.*;

@Entity
@Table(name = "itinerary")
@Getter
@ToString(of = {"id", "memberId", "title", "regionId", "genType"})
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Itinerary extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Embedded
    private TravelPeriod travelPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", length = 10, nullable = false)
    private TransportMode transportMode = TransportMode.CAR;

    @Enumerated(EnumType.STRING)
    @Column(name = "gen_type", length = 10, nullable = false)
    private GenType genType;

    @Column(name = "share_token", length = 64, unique = true)
    private String shareToken;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItineraryItem> items = new ArrayList<>();

    @Builder
    public Itinerary(Long memberId, String title, Long regionId, TravelPeriod travelPeriod,
                     TransportMode transportMode, GenType genType) {
        this.memberId = memberId;
        this.title = title;
        this.regionId = regionId;
        this.travelPeriod = travelPeriod;
        this.transportMode = transportMode != null ? transportMode : TransportMode.CAR;
        this.genType = genType;
    }

    public void addItem(ItineraryItem item) {
        this.items.add(item);
        item.setItinerary(this);
    }

    public void issueShareToken(String token) {
        this.shareToken = token;
    }

    public void validateFestivalPoi(Poi poi, LocalDate targetDate) {
        if (poi.getCategory() == FESTIVAL && poi.getEventPeriod() != null && poi.getEventPeriod().getEndDate() != null) {
            if (targetDate.isAfter(poi.getEventPeriod().getEndDate())) {
                throw new BusinessException(INVALID_INPUT_VALUE);
            }
        }
    }
}