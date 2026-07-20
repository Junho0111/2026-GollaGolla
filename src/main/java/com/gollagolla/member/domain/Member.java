package com.gollagolla.member.domain;

import com.gollagolla.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@ToString(of = {"id", "email", "nickname", "provider", "role"})
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER;

    @Builder
    public Member(String email, String password, String nickname,
                  Provider provider, String providerId, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.role = (role != null) ? role : Role.USER;
    }
}
