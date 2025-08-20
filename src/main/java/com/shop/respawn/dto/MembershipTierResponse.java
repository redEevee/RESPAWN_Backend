package com.shop.respawn.dto;

import com.shop.respawn.domain.MembershipTier;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MembershipTierResponse {
    private Long userId;
    private String username;
    private MembershipTier tier;
    private Long monthlyAmount;   // 산정 기준 금액(저번달 합계)
    private String periodLabel;   // 예: "2025-07-01 ~ 2025-07-31"
}