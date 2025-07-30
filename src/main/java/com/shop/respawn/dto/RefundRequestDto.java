package com.shop.respawn.dto;

import com.shop.respawn.domain.Order;
import com.shop.respawn.domain.RefundRequest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefundRequestDto {
    private Long refundRequestId;
    private Long orderId;
    private Long buyerId;
    private String reason;
    private String detail;
    private LocalDateTime requestTime;

    // from() 등 정적 팩토리 메서드 구현 (환불 + 주문 → DTO)
    public static RefundRequestDto from(RefundRequest refund, Order order) {
        RefundRequestDto refundRequestDto = new RefundRequestDto();
        refundRequestDto.refundRequestId = refund.getId();
        refundRequestDto.orderId = order.getId();
        refundRequestDto.buyerId = refund.getBuyerId();
        refundRequestDto.reason = refund.getReason();
        refundRequestDto.detail = refund.getDetail();
        refundRequestDto.requestTime = refund.getRequestTime();
        return refundRequestDto;
    }
}