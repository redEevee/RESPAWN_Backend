package com.shop.respawn.dto.point;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExpiringPointTotalDto {
    private long totalExpiringThisMonth;
}