package com.gollagolla.itinerary.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "itinerary_item")
@Getter
@ToString(of = {"id", "poiId", "dayNo", "seq", "isAnchor"})
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItineraryItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @Column(name = "poi_id", nullable = false)
    private Long poiId;

    @Column(name = "day_no", nullable = false)
    private Integer dayNo;

    @Column(name = "seq", nullable = false)
    private Integer seq;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_anchor", nullable = false)
    private Boolean isAnchor = false;

    @Column(name = "memo", length = 255)
    private String memo;

    @Builder
    public ItineraryItem(Long poiId, Integer dayNo, Integer seq, LocalTime startTime,
                         LocalTime endTime, Boolean isAnchor, String memo) {
        this.poiId = poiId;
        this.dayNo = dayNo;
        this.seq = seq;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAnchor = (isAnchor != null) ? isAnchor : false;
        this.memo = memo;
    }

    public void reschedule(Integer dayNo, Integer seq, LocalTime startTime,
                           LocalTime endTime, Boolean isAnchor) {
        this.dayNo = dayNo;
        this.seq = seq;
        this.startTime = startTime;
        this.endTime = endTime;
        if (isAnchor != null) this.isAnchor = isAnchor;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    protected void setItinerary(Itinerary itinerary) {
        this.itinerary = itinerary;
    }
}