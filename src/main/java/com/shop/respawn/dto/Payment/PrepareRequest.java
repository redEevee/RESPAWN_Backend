package com.shop.respawn.dto.Payment;

import lombok.Data;

@Data
public class PrepareRequest {
    private String merchantUid;
    private Long orderId;
}