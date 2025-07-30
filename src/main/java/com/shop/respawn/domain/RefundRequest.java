package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "refund_requests")
@Getter @Setter
public class RefundRequest {

    @Id @GeneratedValue
    @Column(name = "refund_requests_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private Long buyerId; // 환불 신청자 ID

    private String reason; // 환불 사유

    private String detail; // 환불 상세 내용

    private LocalDateTime requestTime; // 신청 시점

}
