package com.shop.respawn.util;

import com.shop.respawn.domain.MembershipTier;

import java.time.LocalDateTime;
import java.util.UUID;

public class CouponPolicy {
    public static String generateCode() {
        return UUID.randomUUID().toString();
    }
    public static String couponName(MembershipTier tier) {
        return tier.name() + " 등급 쿠폰";
    }
    public static LocalDateTime defaultExpiry(LocalDateTime issuedAt) {
        return issuedAt.plusDays(30);
    }
    public static long couponAmount(MembershipTier tier) {
        return switch (tier) {
            case VIP -> 10000L;
            case VVIP -> 30000L;
            case VVIP_PLUS -> 50000L;
            default -> 0;
        };
    }
}