package com.shop.respawn.dto;

import lombok.Data;

import java.util.List;

@Data
public class VerifyRequest {
    private String impUid;
    private String merchantUid;
    private Long orderId;
    private Long selectedAddressId;
    private List<Long> selectedCartItemIds;
}
