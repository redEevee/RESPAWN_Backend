package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.*;

@Entity
@NoArgsConstructor
@Getter
public class Point {

    @Id @GeneratedValue
    @Column(name = "point_id")
    private Long id;

    private String pointName;
    private Long amount;
    private LocalDateTime pointAwardedDate;
    private LocalDateTime pointExpiryDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;

    private Point(String pointName, Long amount, LocalDateTime pointAwardedDate, LocalDateTime pointExpiryDate, Buyer buyer) {
        this.pointName = pointName;
        this.amount = amount;
        this.pointAwardedDate = pointAwardedDate;
        this.pointExpiryDate = pointExpiryDate;
        this.buyer = buyer;
    }

    // 정적 팩토리 메서드
    public static Point CreatePoint(String pointName, Long amount, LocalDateTime pointAwardedDate, LocalDateTime pointExpiryDate, Buyer buyer) {
        return new Point(pointName, amount, pointAwardedDate, pointExpiryDate, buyer);
    }
}
