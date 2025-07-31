package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "refund_request")
@Getter @Setter
public class RefundRequest {
    @Id @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", unique = true)
    private OrderItem orderItem;

    private String refundReason;

    @Column(columnDefinition = "TEXT")
    private String refundDetail;

    private LocalDateTime requestedAt;

}