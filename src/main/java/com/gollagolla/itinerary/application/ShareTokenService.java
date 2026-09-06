package com.gollagolla.itinerary.application;

import com.gollagolla.itinerary.domain.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShareTokenService {

    private final ItineraryRepository itineraryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveShareTokenWithNewTransaction(Long itineraryId, String token) {
        itineraryRepository.findById(itineraryId).ifPresent(itinerary -> {
            itinerary.issueShareToken(token);
            itineraryRepository.saveAndFlush(itinerary);
        });
    }
}
