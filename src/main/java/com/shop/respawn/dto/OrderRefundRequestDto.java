package com.shop.respawn.dto;

import lombok.Data;

@Data
public class OrderRefundRequestDto {
    private String reason;   // 환불 사유
    private String detail;   // 상세내용
}
