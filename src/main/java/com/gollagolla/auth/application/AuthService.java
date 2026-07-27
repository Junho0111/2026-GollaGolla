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

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (member.getProvider() != Provider.LOCAL) {
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_REQUIRED);
        }
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
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
                throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMAIL);
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
