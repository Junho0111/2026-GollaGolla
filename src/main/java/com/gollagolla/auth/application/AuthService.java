package com.gollagolla.auth.application;

import com.gollagolla.auth.domain.RefreshToken;
import com.gollagolla.auth.domain.RefreshTokenRepository;
import com.gollagolla.auth.oauth.OAuthClientFactory;
import com.gollagolla.auth.oauth.dto.OAuthUserInfoDto;
import com.gollagolla.auth.support.JwtTokenProvider;
import com.gollagolla.auth.ui.dto.LoginResponse;
import com.gollagolla.auth.ui.dto.OAuthResponse;
import com.gollagolla.auth.ui.dto.SignUpResponse;
import com.gollagolla.member.domain.Member;
import com.gollagolla.member.domain.MemberRepository;
import com.gollagolla.member.domain.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthClientFactory oAuthClientFactory;

    @Transactional
    public SignUpResponse signUp(String email, String password, String nickname) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .provider(Provider.LOCAL)
                .build();
        memberRepository.save(member);

        String accessToken  = jwtTokenProvider.generateAccessToken(member.getId(), member.getRole());
        String refreshToken = issueRefreshToken(member.getId());

        return new SignUpResponse(member.getId(), accessToken, refreshToken);
    }

    @Transactional
    public LoginResponse login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (member.getProvider() != Provider.LOCAL) {
            throw new IllegalStateException("소셜 계정으로 가입된 이메일입니다. OAuth 로그인을 이용해 주세요.");
        }
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        refreshTokenRepository.deleteByMemberId(member.getId());

        String accessToken  = jwtTokenProvider.generateAccessToken(member.getId(), member.getRole());
        String refreshToken = issueRefreshToken(member.getId());

        return LoginResponse.of(accessToken, refreshToken);
    }

    @Transactional
    public OAuthResponse oauthLogin(Provider provider, String authorizationCode) {
        OAuthUserInfoDto userInfo = oAuthClientFactory
                .getClient(provider)
                .getUserInfo(authorizationCode);

        Optional<Member> existingMember = memberRepository
                .findByProviderAndProviderId(provider, userInfo.getProviderId());

        boolean isNewUser = existingMember.isEmpty();

        Member member = existingMember.orElseGet(() -> {
            if (userInfo.getEmail() != null && memberRepository.existsByEmail(userInfo.getEmail())) {
                throw new IllegalStateException("이미 다른 방식으로 가입된 이메일입니다.");
            }

            return memberRepository.save(Member.builder()
                    .email(userInfo.getEmail())
                    .nickname(userInfo.getNickname())
                    .provider(provider)
                    .providerId(userInfo.getProviderId())
                    .build());
        });

        String accessToken = jwtTokenProvider.generateAccessToken(member.getId(), member.getRole());
        String refreshToken = issueRefreshToken(member.getId());

        return new OAuthResponse(accessToken, refreshToken, isNewUser);
    }

    private String issueRefreshToken(Long memberId) {
        String rawToken = jwtTokenProvider.generateRefreshToken(memberId);
        LocalDateTime expiresAt = jwtTokenProvider.getRefreshTokenExpiry();

        refreshTokenRepository.save(RefreshToken.builder()
                .memberId(memberId)
                .token(rawToken)
                .expiresAt(expiresAt)
                .build());

        return rawToken;
    }
}
