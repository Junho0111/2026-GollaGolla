package com.gollagolla.member.domain.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class MemberSearchResponse {

    private Long id;
    private String nickname;
    private String email;

    private MemberSearchResponse() {
    }

    @QueryProjection
    public MemberSearchResponse(Long id, String nickname, String email) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
    }
}