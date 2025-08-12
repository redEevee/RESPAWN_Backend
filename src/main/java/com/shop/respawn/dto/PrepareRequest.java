package com.shop.respawn.dto;

import lombok.Data;

@Data
public class PrepareRequest {
    private String merchantUid;
    private Long orderId;
}