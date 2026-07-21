package com.gollagolla.member.domain;

import com.gollagolla.member.domain.dto.MemberSearchResponse;
import com.gollagolla.member.domain.dto.QMemberSearchResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.gollagolla.member.domain.QMember.member;

@Repository
@RequiredArgsConstructor
public class QMemberRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<Member> findByEmailAndProvider(String email, Provider provider) {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        emailEq(email),
                        providerEq(provider)
                )
                .fetchOne();
        return Optional.ofNullable(findMember);
    }

    public List<MemberSearchResponse> searchByNickname(String nickname) {
        return queryFactory
                .select(new QMemberSearchResponse(
                        member.id,
                        member.nickname,
                        member.email))
                .from(member)
                .where(nicknameContains(nickname))
                .fetch();
    }

    private BooleanExpression emailEq(String email) {
        if (email == null) {
            return null;
        }
        return member.email.eq(email);
    }

    private BooleanExpression providerEq(Provider provider) {
        if (provider == null) {
            return null;
        }
        return member.provider.eq(provider);
    }

    private BooleanExpression nicknameContains(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return null;
        }
        return member.nickname.contains(nickname);
    }
}
