package com.gollagolla.wishlist.domain;

import com.gollagolla.config.EnableJpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(EnableJpaAuditingConfig.class)
class WishlistRepositoryTest {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Test
    void memberId와_poiId로_찜을_조회() {
        // given
        Wishlist wishlist = Wishlist.builder()
                .memberId(1L)
                .poiId(10L)
                .isPublic(true)
                .build();
        wishlistRepository.save(wishlist);

        // when
        Optional<Wishlist> found = wishlistRepository.findByMemberIdAndPoiId(1L, 10L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getMemberId()).isEqualTo(1L);
        assertThat(found.get().getPoiId()).isEqualTo(10L);
    }

    @Test
    void memberId로_회원의_모든_찜_목록을_조회() {
        // given
        wishlistRepository.save(Wishlist.builder()
                .memberId(1L)
                .poiId(10L)
                .isPublic(true)
                .build());

        wishlistRepository.save(Wishlist.builder()
                .memberId(1L)
                .poiId(20L)
                .isPublic(true)
                .build());

        wishlistRepository.save(Wishlist.builder()
                .memberId(2L)
                .poiId(30L)
                .isPublic(true)
                .build());

        // when
        List<Wishlist> wishlists = wishlistRepository.findByMemberId(1L);

        // then
        assertThat(wishlists).hasSize(2);
        assertThat(wishlists).extracting("poiId").containsExactlyInAnyOrder(10L, 20L);
    }
}
