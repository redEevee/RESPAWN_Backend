package com.shop.respawn.domain;

public enum OrderStatus {
    ORDERED,    // 주문접수
    PAID,       // 결제완료
    CANCELED,   // 주문취소
    RETURNED,   // 반품
    REFUNDED    // 환불
}
