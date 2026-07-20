package com.gollagolla.poi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rating {

    @Column(name = "rating", precision = 2, scale = 1, nullable = false)
    private BigDecimal score = BigDecimal.ZERO;

    public static Rating zero() {
        return new Rating();
    }

    public Rating add(int newScore, int previousReviewCount) {
        BigDecimal totalBefore = this.score.multiply(BigDecimal.valueOf(previousReviewCount));
        BigDecimal newTotal = totalBefore.add(BigDecimal.valueOf(newScore));
        BigDecimal newAverage = newTotal
                .divide(BigDecimal.valueOf(previousReviewCount + 1), 10, RoundingMode.HALF_UP)
                .setScale(1, RoundingMode.HALF_UP);
        return new Rating(newAverage);
    }

    private Rating(BigDecimal score) {
        this.score = score;
    }
}
