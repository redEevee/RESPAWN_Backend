package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "point_ledger",
        indexes = {
                @Index(name = "idx_ledger_buyer_time", columnList = "buyer_id, occurredAt"),
                @Index(name = "idx_ledger_buyer_type", columnList = "buyer_id, type")
        })
public class PointLedger {

    @Id @GeneratedValue
    @Column(name = "ledger_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointTransactionType type;

    // 부호 규칙:
    // SAVE, CANCEL_USE => 양수
    // USE, EXPIRE, CANCEL_SAVE => 음수
    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    // 적립 시에만 의미(만료 예정일)
    private LocalDateTime expiryAt;

    // 레퍼런스(주문/결제/환불 등)
    private Long refOrderId;
    private String reason;
    private String actor; // system, user, admin 등

    public static PointLedger of(Buyer buyer, PointTransactionType type, Long amount,
                                 LocalDateTime occurredAt, LocalDateTime expiryAt,
                                 Long refOrderId, String reason, String actor) {
        PointLedger l = new PointLedger();
        l.buyer = buyer;
        l.type = type;
        l.amount = amount;
        l.occurredAt = occurredAt;
        l.expiryAt = expiryAt;
        l.refOrderId = refOrderId;
        l.reason = reason;
        l.actor = actor;
        return l;
    }
}
