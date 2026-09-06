package com.gollagolla.member.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    @Query("select m from Member m WHERE m.provider = :provider and m.providerId = :providerId")
    Optional<Member> findByProviderAndProviderId(@Param("provider") Provider provider,
                                                 @Param("providerId") String providerId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
