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
}