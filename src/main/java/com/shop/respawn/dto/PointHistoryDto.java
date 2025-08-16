package com.shop.respawn.dto;

import com.shop.respawn.domain.PointTransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PointHistoryDto {
    private Long id;
    private PointTransactionType type;
    private Long amount;        // 원시값(부호 포함)
    private Long absAmount;     // 절대값 UI용
    private LocalDateTime occurredAt;
    private LocalDateTime expiryAt;
    private Long refOrderId;
    private String reason;

    public static PointHistoryDto of(PointLedgerDto pointLedgerDto) {
        return PointHistoryDto.builder()
                .id(pointLedgerDto.getId())
                .type(pointLedgerDto.getType())
                .amount(pointLedgerDto.getAmount())
                .absAmount(Math.abs(pointLedgerDto.getAmount()))
                .occurredAt(pointLedgerDto.getOccurredAt())
                .expiryAt(pointLedgerDto.getExpiryAt())
                .refOrderId(pointLedgerDto.getRefOrderId())
                .reason(pointLedgerDto.getReason())
                .build();
    }
}
