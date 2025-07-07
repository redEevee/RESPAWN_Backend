package com.shop.respawn.domain;

public enum DeliveryStatus {
    PROCESSING, // 상품준비중
    READY,      // 배송준비중
    SHIPPING,   // 배송중
    DELIVERED   // 배송완료
}
