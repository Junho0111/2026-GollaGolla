package com.gollagolla.itinerary.ui;

import com.gollagolla.itinerary.application.ItineraryService;
import com.gollagolla.itinerary.ui.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    @PostMapping("/itineraries")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateItineraryResponse createItinerary(
            @RequestBody @Valid CreateItineraryRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        Long id = itineraryService.createItinerary(memberId, request);
        return CreateItineraryResponse.of(id);
    }

    @GetMapping("/itineraries/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ItineraryResponse getItinerary(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId
    ) {
        return itineraryService.getItinerary(memberId, id);
    }

    @PostMapping("/itineraries/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public void addItem(
            @PathVariable Long id,
            @RequestBody @Valid CreateItemRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        itineraryService.addItem(memberId, id, request);
    }

    @PostMapping("/itineraries/{id}/items/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void addItemsBulk(
            @PathVariable Long id,
            @RequestBody @Valid CreateBulkItemRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        itineraryService.addItemsBulk(memberId, id, request);
    }

    @PatchMapping("/itineraries/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestBody @Valid UpdateItemRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        itineraryService.updateItem(memberId, id, itemId, request);
    }

    @DeleteMapping("/itineraries/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @AuthenticationPrincipal Long memberId
    ) {
        itineraryService.deleteItem(memberId, id, itemId);
    }

    @PostMapping("/itineraries/{id}/share")
    @ResponseStatus(HttpStatus.CREATED)
    public ShareResponse shareItinerary(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId
    ) {
        return itineraryService.shareItinerary(memberId, id);
    }

    @GetMapping("/share/{token}")
    @ResponseStatus(HttpStatus.OK)
    public ItineraryResponse getSharedItinerary(@PathVariable String token) {
        return itineraryService.getSharedItinerary(token);
    }
}
