package com.gollagolla.review.application;

import com.gollagolla.member.domain.Member;
import com.gollagolla.member.domain.MemberRepository;
import com.gollagolla.review.application.dto.ReviewItemDto;
import com.gollagolla.review.domain.Review;
import com.gollagolla.review.domain.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    public List<ReviewItemDto> getReviews(Long poiId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByPoiId(poiId, pageable);
        List<Review> reviews = reviewPage.getContent();

        List<Long> memberIds = reviews.stream()
                .map(Review::getMemberId)
                .distinct()
                .toList();

        Map<Long, String> nicknameMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        return reviews.stream()
                .map(review -> ReviewItemDto.of(
                        review.getId(),
                        review.getMemberId(),
                        nicknameMap.getOrDefault(review.getMemberId(), "알 수 없음"),
                        review.getRating(),
                        review.getContent(),
                        review.getCreatedAt()
                ))
                .toList();
    }
}
