package com.gollagolla.wishlist.domain;

import com.gollagolla.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlist", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wishlist_member_poi", columnNames = {"member_id", "poi_id"})
})
@Getter
@ToString(of = {"id", "memberId", "poiId", "isPublic"})
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "poi_id", nullable = false)
    private Long poiId;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Builder
    public Wishlist(Long memberId, Long poiId, Boolean isPublic) {
        this.memberId = memberId;
        this.poiId = poiId;
        this.isPublic = (isPublic != null) ? isPublic : true;
    }

    public void changeVisibility(boolean isPublic) {
        this.isPublic = isPublic;
    }
}