package com.gollagolla.auth.domain;

import com.gollagolla.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
@Import(QuerydslConfig.class)
class RefreshTokenRepositoryTest {

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Test
    void 토큰_값으로_토큰_조회() throws Exception {
        // given
        RefreshToken token = RefreshToken.builder()
                .memberId(1L)
                .token("토큰")
                .expiresAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(token);

        // when
        Optional<RefreshToken> findToken = refreshTokenRepository.findByToken("토큰");

        // then
        assertThat(findToken.isPresent()).isTrue();
        assertThat(findToken.get().getToken()).isEqualTo("토큰");
        assertThat(findToken.get().getMemberId()).isEqualTo(1L);
        assertThat(findToken.get().isRevoked()).isFalse();
    }

    @Test
    void 회원ID로_토큰_조회() throws Exception {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(1L)
                .token("토큰")
                .expiresAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);

        // when
        Optional<RefreshToken> findToken = refreshTokenRepository.findByMemberId(1L);

        // then
        assertThat(findToken.isPresent()).isTrue();
        assertThat(findToken.get().getMemberId()).isEqualTo(1L);
        assertThat(findToken.get().getToken()).isEqualTo("토큰");
    }

    @Test
    void 토큰_존재_여부_확인() throws Exception {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(1L)
                .token("토큰")
                .expiresAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);

        // when
        boolean exists = refreshTokenRepository.existsByToken("토큰");
        boolean notExists = refreshTokenRepository.existsByToken("존재하지않는_토큰");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void 회원ID로_토큰_삭제() throws Exception {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(1L)
                .token("토큰")
                .expiresAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);
        assertThat(refreshTokenRepository.findByMemberId(1L).isPresent()).isTrue();
        assertThat(refreshTokenRepository.findByMemberId(1L).get().getToken()).isEqualTo("토큰");

        // when
        refreshTokenRepository.deleteByMemberId(1L);

        // then
        assertThat(refreshTokenRepository.findByMemberId(1L).isPresent()).isFalse();
    }

    @Test
    void 토큰_폐기() throws Exception {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(1L)
                .token("토큰")
                .expiresAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);
        assertThat(refreshToken.isRevoked()).isFalse();

        // when
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        // then
        Optional<RefreshToken> findToken = refreshTokenRepository.findByToken("토큰");
        assertThat(findToken.isPresent()).isTrue();
        assertThat(findToken.get().isRevoked()).isTrue();
    }
}
