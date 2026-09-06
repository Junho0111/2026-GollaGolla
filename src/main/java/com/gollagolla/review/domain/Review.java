package com.gollagolla.review.domain;

import com.gollagolla.global.BaseTimeEntity;
import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review", uniqueConstraints = {
        @UniqueConstraint(name = "uq_review_poi_member", columnNames = {"poi_id", "member_id"})})
@Getter
@ToString(of = {"id", "poiId", "memberId", "rating", "content"})
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

    @Builder
    public Review(Long poiId, Long memberId, Integer rating, String content) {
        validateRating(rating);
        this.poiId = poiId;
        this.memberId = memberId;
        this.rating = rating;
        this.content = content;
    }

    private static void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(
                    ErrorCode.REVIEW_INVALID_RATING,
                    "rating=" + rating + " is out of range [rating < 1 || rating > 5]"
            );
        }
    }
}