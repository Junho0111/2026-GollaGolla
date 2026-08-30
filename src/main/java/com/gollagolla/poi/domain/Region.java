package com.gollagolla.poi.domain;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "region")
@Getter
@ToString(of = {"id", "parentId", "name", "depth", "areaCode"})
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

    @Column(name = "area_code", length = 10)
    private String areaCode;

    @Builder
    public Region(Long parentId, String name, Integer depth, String areaCode) {
        if (depth == 1 && parentId != null) {
            throw new BusinessException(ErrorCode.INVALID_REGION_HIERARCHY, "Depth1 지역은 상위 지역을 가질 수 없습니다.");
        }
        if (depth == 2 && parentId == null) {
            throw new BusinessException(ErrorCode.INVALID_REGION_HIERARCHY, "Depth2 지역은 상위 지역이 필요합니다.");
        }
        this.parentId = parentId;
        this.name = name;
        this.depth = depth;
        this.areaCode = areaCode;
    }
}
