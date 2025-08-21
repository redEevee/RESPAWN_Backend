package com.shop.respawn.util;

import com.shop.respawn.domain.Grade;
import org.springframework.stereotype.Component;

@Component
public class GradePolicy {

    // 운영에서 바꿀 가능성이 높으면 @Value 주입으로 외부화
    private static final long THRESHOLD_VIP  = 100_000L;  // 예시
    private static final long THRESHOLD_VVIP = 300_000L;  // 예시
    private static final long THRESHOLD_VVIP_PLUS = 500_000L; // 예시 (50 이상은 동일 취급)

    public Grade resolveGrade(long monthlyAmount) {
        if (monthlyAmount >= THRESHOLD_VVIP_PLUS) return Grade.VVIP_PLUS;
        if (monthlyAmount >= THRESHOLD_VVIP)      return Grade.VVIP;
        if (monthlyAmount >= THRESHOLD_VIP)       return Grade.VIP;
        return Grade.BASIC;
    }
}
