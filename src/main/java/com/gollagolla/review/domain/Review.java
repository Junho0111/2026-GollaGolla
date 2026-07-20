package com.gollagolla.review.domain;

import com.gollagolla.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review", uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_poi_member", columnNames = {"poi_id", "member_id"})
})
@Getter
@ToString(of = {"id", "poiId", "memberId", "rating", "verified"})
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poi_id", nullable = false)
    private Long poiId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @Builder
    public Review(Long poiId, Long memberId, Integer rating, String content, boolean verified) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1~5 사이여야 합니다.");
        }
        this.poiId = poiId;
        this.memberId = memberId;
        this.rating = rating;
        this.content = content;
        this.verified = verified;
    }
}