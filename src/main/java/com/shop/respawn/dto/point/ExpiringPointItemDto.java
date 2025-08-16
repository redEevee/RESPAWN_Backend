package com.shop.respawn.dto.point;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ExpiringPointItemDto {
    private Long ledgerId;
    private Long remainingAmount;    // 잔여(미사용/미만료)
    private LocalDateTime expiryAt;  // 만료 예정일
    private Long refOrderId;
    private String reason;
}