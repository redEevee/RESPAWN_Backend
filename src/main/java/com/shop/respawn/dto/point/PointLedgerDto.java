package com.shop.respawn.dto.point;

import com.shop.respawn.domain.PointLedger;
import com.shop.respawn.domain.PointTransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PointLedgerDto {
    private Long id;
    private PointTransactionType type;
    private Long amount;
    private LocalDateTime occurredAt;
    private LocalDateTime expiryAt;
    private Long refOrderId;
    private String reason;

    public static PointLedgerDto from(PointLedger pointLedger) {
        return PointLedgerDto.builder()
                .id(pointLedger.getId())
                .type(pointLedger.getType())
                .amount(pointLedger.getAmount())
                .occurredAt(pointLedger.getOccurredAt())
                .expiryAt(pointLedger.getExpiryAt())
                .refOrderId(pointLedger.getRefOrderId())
                .reason(pointLedger.getReason())
                .build();
    }
}
