package com.gollagolla.member.domain;

import com.gollagolla.member.domain.dto.MemberSearchResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Import({QMemberRepository.class})
@DataJpaTest
@Transactional
class QMemberRepositoryTest {

    @TestConfiguration
    static class QuerydslTestConfig {

        @PersistenceContext
        private EntityManager em;

        @Bean
        public JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(em);
        }
    }

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    QMemberRepository qMemberRepository;

    @Test
    void 이메일과_인증수단으로_회원_조회() throws Exception {
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
        Optional<Member> findMember = qMemberRepository.findByEmailAndProvider("kakao@email.com", Provider.KAKAO);

        // then
        assertThat(findMember.isPresent()).isTrue();
        assertThat(findMember.get().getEmail()).isEqualTo("kakao@email.com");
        assertThat(findMember.get().getProvider()).isEqualTo(Provider.KAKAO);
    }

    @Test
    void 닉네임_부분_검색() throws Exception {
        // given
        Member localMember = Member.builder()
                .email("loacl@email.com")
                .nickname("로컬")
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .build();

        Member kakaoMember = Member.builder()
                .email("kakao@email.com")
                .nickname("카카오유저")
                .provider(Provider.KAKAO)
                .providerId("kakaoId")
                .role(Role.USER)
                .build();

        Member naverMember = Member.builder()
                .email("naver@email.com")
                .nickname("네이버유저")
                .provider(Provider.NAVER)
                .providerId("naverId")
                .role(Role.USER)
                .build();

        memberRepository.save(localMember);
        memberRepository.save(kakaoMember);
        memberRepository.save(naverMember);

        // when
        List<MemberSearchResponse> findNaverMember = qMemberRepository.searchByNickname("유저");

        // then
        assertThat(findNaverMember).hasSize(2);
        assertThat(findNaverMember)
                .extracting(MemberSearchResponse::getNickname)
                .containsExactlyInAnyOrder("카카오유저", "네이버유저");
    }
}
