package com.shop.respawn.domain;

public enum PointTransactionType {
    SAVE,          // 적립 (+)
    USE,           // 사용 (-)
    EXPIRE,        // 만료 (-)
    CANCEL_SAVE,   // 적립 취소 (-)  또는 정책에 따라 +/-
    CANCEL_USE     // 사용 취소 (+)
}