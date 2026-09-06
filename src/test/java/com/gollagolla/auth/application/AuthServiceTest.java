package com.gollagolla.auth.application;

import com.gollagolla.auth.domain.RefreshTokenRepository;
import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.auth.oauth.OAuthClient;
import com.gollagolla.auth.oauth.OAuthClientFactory;
import com.gollagolla.auth.oauth.dto.OAuthUserInfoDto;
import com.gollagolla.auth.support.JwtTokenProvider;
import com.gollagolla.auth.ui.dto.LoginResponse;
import com.gollagolla.auth.ui.dto.OAuthResponse;
import com.gollagolla.auth.ui.dto.SignUpResponse;
import com.gollagolla.member.domain.Member;
import com.gollagolla.member.domain.MemberRepository;
import com.gollagolla.member.domain.Provider;
import com.gollagolla.member.domain.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    AuthService authService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    OAuthClientFactory oAuthClientFactory;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Test
    void 정상_회원가입_토큰_반환() throws Exception {
        // given
        given(memberRepository.existsByEmail("test@test.com")).willReturn(false);
        given(memberRepository.existsByNickname("testNickName")).willReturn(false);
        given(passwordEncoder.encode("testPassword")).willReturn("encoded-pw");

        Member saved = Member.builder()
                .email("test@test.com")
                .password("encoded-pw")
                .nickname("testNickName")
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .build();

        given(memberRepository.save(any(Member.class))).willReturn(saved);
        given(jwtTokenProvider.generateAccessToken(any(), eq(Role.USER))).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(any())).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshTokenExpiry()).willReturn(LocalDateTime.now().plusDays(7));
        given(refreshTokenRepository.save(any())).willReturn(null);

        // when
        SignUpResponse response = authService.signUp("test@test.com", "testPassword", "testNickName");

        // then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void 이메일_중복_회원가입_시_예외_발생() throws Exception {
        // given
        given(memberRepository.existsByEmail("test@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signUp("test@test.com", "testPassword", "testNickName"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 사용 중인 이메일입니다.");
    }

    @Test
    void 닉네임_중복_회원가입_시_예외_발생() throws Exception {
        // given
        given(memberRepository.existsByEmail(any())).willReturn(false);
        given(memberRepository.existsByNickname("중복닉네임")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signUp("test@test.com", "testPassword", "중복닉네임"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 사용 중인 닉네임입니다.");
    }

    @Test
    void 정상_로그인_토큰_반환() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .password("encoded-pw")
                .nickname("testNickName")
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .build();

        given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("testPassword", "encoded-pw")).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(any(), eq(Role.USER))).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(any())).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshTokenExpiry()).willReturn(LocalDateTime.now().plusDays(7));

        // when
        LoginResponse response = authService.login("test@test.com", "testPassword");

        // then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void 존재하지_않는_이메일_로그인_시_예외_발생() throws Exception {
        // given
        given(memberRepository.findByEmail("none@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login("none@test.com", "testPassword"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void 소셜_계정으로_이메일_로그인_시도_시_예외_발생() throws Exception {
        // given
        Member kakaoMember = Member.builder()
                .email("kakao@test.com")
                .nickname("카카오유저")
                .provider(Provider.KAKAO)
                .role(Role.USER)
                .build();

        given(memberRepository.findByEmail("kakao@test.com")).willReturn(Optional.of(kakaoMember));

        // when & then
        assertThatThrownBy(() -> authService.login("kakao@test.com", "testPassword"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("소셜 계정으로 가입된 이메일입니다.");
    }

    @Test
    void 비밀번호_불일치_로그인_시_예외_발생() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .password("encoded-pw")
                .nickname("testNickName")
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .build();

        given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("잘못된비밀번호입력", "encoded-pw")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login("test@test.com", "잘못된비밀번호입력"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void 신규_OAuth_사용자_로그인_시_isNewUser_true() throws Exception {
        // given
        OAuthClient mockClient = mock(OAuthClient.class);
        OAuthUserInfoDto userInfo = new OAuthUserInfoDto("kakao-123", "kakao@test.com", "카카오닉네임");

        given(oAuthClientFactory.getClient(Provider.KAKAO)).willReturn(mockClient);
        given(mockClient.getUserInfo("auth-code")).willReturn(userInfo);
        given(memberRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao-123")).willReturn(Optional.empty());
        given(memberRepository.existsByEmail("kakao@test.com")).willReturn(false);

        Member newMember = Member.builder()
                .email("kakao@test.com")
                .nickname("카카오닉네임")
                .provider(Provider.KAKAO)
                .providerId("kakao-123")
                .role(Role.USER)
                .build();

        given(memberRepository.save(any(Member.class))).willReturn(newMember);
        given(jwtTokenProvider.generateAccessToken(any(), eq(Role.USER))).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(any())).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshTokenExpiry()).willReturn(LocalDateTime.now().plusDays(7));

        // when
        OAuthResponse response = authService.oauthLogin(Provider.KAKAO, "auth-code");

        // then
        assertThat(response.isNewUser()).isTrue();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void 기존_OAuth_사용자_로그인_시_isNewUser_false() throws Exception {
        // given
        OAuthClient mockClient = mock(OAuthClient.class);
        OAuthUserInfoDto userInfo = new OAuthUserInfoDto("kakao-123", "kakao@test.com", "카카오닉네임");

        given(oAuthClientFactory.getClient(Provider.KAKAO)).willReturn(mockClient);
        given(mockClient.getUserInfo("auth-code")).willReturn(userInfo);

        Member existing = Member.builder()
                .email("kakao@test.com")
                .nickname("카카오닉네임")
                .provider(Provider.KAKAO)
                .providerId("kakao-123")
                .role(Role.USER)
                .build();

        given(memberRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao-123")).willReturn(Optional.of(existing));
        given(jwtTokenProvider.generateAccessToken(any(), eq(Role.USER))).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(any())).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshTokenExpiry()).willReturn(LocalDateTime.now().plusDays(7));

        // when
        OAuthResponse response = authService.oauthLogin(Provider.KAKAO, "auth-code");

        // then
        assertThat(response.isNewUser()).isFalse();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void 이미_다른_방식으로_가입된_이메일로_OAuth_로그인_시_예외_발생() throws Exception {
        // given
        OAuthClient mockClient = mock(OAuthClient.class);
        OAuthUserInfoDto userInfo = new OAuthUserInfoDto("kakao-123", "existing@test.com", "닉네임");

        given(oAuthClientFactory.getClient(Provider.KAKAO)).willReturn(mockClient);
        given(mockClient.getUserInfo("auth-code")).willReturn(userInfo);
        given(memberRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao-123")).willReturn(Optional.empty());
        given(memberRepository.existsByEmail("existing@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.oauthLogin(Provider.KAKAO, "auth-code"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 다른 방식으로 가입된 이메일입니다.");
    }

    @Test
    void 로그아웃_시_해당_회원의_RefreshToken_삭제() throws Exception {
        // given
        Long memberId = 1L;
        willDoNothing().given(refreshTokenRepository).deleteByMemberId(1L);

        // when
        authService.logout(memberId);

        // then
        then(refreshTokenRepository).should().deleteByMemberId(memberId);
    }

    @Test
    void 회원탈퇴_시_RefreshToken_삭제_후_Member_삭제() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .email("test@test.com")
                .password("encoded-pw")
                .nickname("테스터")
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        willDoNothing().given(refreshTokenRepository).deleteByMemberId(memberId);
        willDoNothing().given(memberRepository).delete(member);

        // when
        authService.withdraw(memberId);

        // then
        then(refreshTokenRepository).should().deleteByMemberId(memberId);
        then(memberRepository).should().delete(member);
    }

    @Test
    void 회원탈퇴_시_존재하지_않는_회원이면_예외_발생() throws Exception {
        // given
        Long nontMemberId = 1L;
        given(memberRepository.findById(nontMemberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.withdraw(nontMemberId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");

        then(refreshTokenRepository).shouldHaveNoInteractions();
        then(memberRepository).should(never()).delete(any());
    }
}
