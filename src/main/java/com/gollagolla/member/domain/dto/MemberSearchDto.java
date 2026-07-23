package com.gollagolla.member.domain.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class MemberSearchDto {

    private Long id;
    private String nickname;
    private String email;

    private MemberSearchDto() {
    }

    @QueryProjection
    public MemberSearchDto(Long id, String nickname, String email) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
    }
}