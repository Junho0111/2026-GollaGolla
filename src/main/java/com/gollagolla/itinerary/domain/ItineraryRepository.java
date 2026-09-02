package com.gollagolla.itinerary.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    Optional<Itinerary> findByIdAndMemberId(Long id, Long memberId);

    Optional<Itinerary> findByShareToken(String shareToken);

    List<Itinerary> findAllByMemberIdOrderByIdDesc(Long memberId);
}
