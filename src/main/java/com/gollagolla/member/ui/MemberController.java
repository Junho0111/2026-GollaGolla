package com.gollagolla.member.ui;

import com.gollagolla.member.application.MemberQueryService;
import com.gollagolla.member.ui.dto.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberQueryService memberQueryService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public MemberResponse getMyInfo(@AuthenticationPrincipal Long memberId) {
        return memberQueryService.getMyInfo(memberId);
    }
}
