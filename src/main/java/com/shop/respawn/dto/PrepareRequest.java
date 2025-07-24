package com.shop.respawn.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PrepareRequest {
    private String merchantUid;
    private BigDecimal amount;
}