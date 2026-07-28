package com.gollagolla.member.domain;

import com.gollagolla.config.EnableJpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
@Import(EnableJpaAuditingConfig.class)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 이메일로_회원_조회() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@email.com")
                .password("testPassword")
                .nickname("testNickName")
                .provider(Provider.LOCAL)
                .providerId("testId")
                .role(Role.USER)
                .build();

        memberRepository.save(member);

        // when
        Optional<Member> findMember = memberRepository.findByEmail("test@email.com");

        // then
        assertThat(findMember.isPresent()).isTrue();
        assertThat(findMember.get().getNickname()).isEqualTo("testNickName");
        assertThat(findMember.get().getProviderId()).isEqualTo("testId");
        assertThat(findMember.get().getEmail()).isEqualTo("test@email.com");
        assertThat(findMember.get().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void Provider와_ProviderId로_회원_조회() throws Exception {
        // given
        Member member = Member.builder()
                .email("kakao@email.com")
                .nickname("kakaoNickName")
                .provider(Provider.KAKAO)
                .providerId("kakaoId123")
                .role(Role.USER)
                .build();

        memberRepository.save(member);

        // when
        Optional<Member> findMember = memberRepository.findByProviderAndProviderId(Provider.KAKAO, "kakaoId123");

        // then
        assertThat(findMember.isPresent()).isTrue();
        assertThat(findMember.get().getEmail()).isEqualTo("kakao@email.com");
        assertThat(findMember.get().getProvider()).isEqualTo(Provider.KAKAO);
        assertThat(findMember.get().getProviderId()).isEqualTo("kakaoId123");
    }
}