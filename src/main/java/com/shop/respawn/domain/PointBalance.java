package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "point_balance")
public class PointBalance {

    @Id
    @Column(name = "buyer_id")
    private Long buyerId;

    @Column(nullable = false)
    private Long total;   // 총 누적 = SAVE + CANCEL_USE + ... - (USE + EXPIRE + CANCEL_SAVE)
    @Column(nullable = false)
    private Long active;  // 가용(미사용/미만료) 합계
    @Column(nullable = false)
    private Long used;    // 사용 누적
    @Column(nullable = false)
    private Long expired; // 만료 누적

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static PointBalance init(Long buyerId) {
        PointBalance b = new PointBalance();
        b.buyerId = buyerId;
        b.total = 0L;
        b.active = 0L;
        b.used = 0L;
        b.expired = 0L;
        b.updatedAt = LocalDateTime.now();
        return b;
    }

    public void addTotal(Long delta) { this.total += delta; touch(); }
    public void addActive(Long delta) { this.active += delta; touch(); }
    public void addUsed(Long delta) { this.used += delta; touch(); }
    public void addExpired(Long delta) { this.expired += delta; touch(); }

    private void touch() { this.updatedAt = LocalDateTime.now(); }
}
