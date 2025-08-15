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

    // 포인트 사용 여부 추가
    private boolean used = false;

    // 포인트 사용 날짜 추가
    private LocalDateTime usedDate;

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

    // 포인트 사용 메서드
    public void use() {
        this.used = true;
        this.usedDate = LocalDateTime.now();
    }

    // 포인트 사용 가능 여부 확인
    public boolean isUsable() {
        return !used && LocalDateTime.now().isBefore(pointExpiryDate);
    }
}
