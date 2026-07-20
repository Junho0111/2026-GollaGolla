package com.gollagolla.poi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "region")
@Getter
@ToString(of = {"id", "parentId", "name", "depth"})
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "depth", nullable = false)
    private Integer depth;

    @Builder
    public Region(Long parentId, String name, Integer depth) {
        if (depth == 1 && parentId != null) {
            throw new IllegalArgumentException("Depth1 지역은 상위 지역을 가질 수 없습니다.");
        }
        if (depth == 2 && parentId == null) {
            throw new IllegalArgumentException("Depth2 지역은 상위 지역이 필요합니다.");
        }
        this.parentId = parentId;
        this.name = name;
        this.depth = depth;
    }
}
