package com.gollagolla.member.ui.dto;

import com.gollagolla.member.domain.Member;
import com.gollagolla.member.domain.Provider;
import lombok.Getter;

@Getter
public class MemberResponse {

    private Long memberId;
    private String email;
    private String nickname;
    private Provider provider;

    private MemberResponse() {}

    private MemberResponse(Long memberId, String email, String nickname, Provider provider) {
        this.memberId = memberId;
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
    }

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProvider()
        );
    }
}
