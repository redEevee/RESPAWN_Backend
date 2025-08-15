package com.shop.respawn.dto;

import com.shop.respawn.domain.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointDto {
    private Long id;
    private String pointName;
    private Long amount;
    private LocalDateTime pointAwardedDate;
    private LocalDateTime pointExpiryDate;
    private boolean used;
    private LocalDateTime usedDate;
    private Long buyerId; // Buyer 객체 대신 ID만 포함

    // Point 엔티티를 DTO로 변환하는 정적 메서드
    public static PointDto from(Point point) {
        return new PointDto(
                point.getId(),
                point.getPointName(),
                point.getAmount(),
                point.getPointAwardedDate(),
                point.getPointExpiryDate(),
                point.isUsed(),
                point.getUsedDate(),
                point.getBuyer() != null ? point.getBuyer().getId() : null
        );
    }
}