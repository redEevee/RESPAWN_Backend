package com.shop.respawn.domain;

public enum RefundStatus {
    NONE,          // 기본 상태, 환불 요청 없음
    REQUESTED,     // 환불 요청됨
    REFUNDED       // 환불 완료됨
}
