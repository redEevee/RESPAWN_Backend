package com.shop.respawn.service;

import com.shop.respawn.domain.Grade;
import com.shop.respawn.util.CouponPolicy;
import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Coupon;
import com.shop.respawn.repository.CouponRepository;
import com.shop.respawn.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final BuyerRepository buyerRepository;

    // 등급 변경 시 쿠폰 발급
    public void issueGradeCoupon(Long buyerId, Grade tier) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("구매자 없음: " + buyerId));

        LocalDateTime now = LocalDateTime.now();

        long couponAmount = CouponPolicy.couponAmount(tier);

        Coupon coupon = Coupon.builder()
                .buyer(buyer)
                .code(uniqueCode())
                .name(CouponPolicy.couponName(tier))
                .couponAmount(couponAmount)
                .issuedAt(now)
                .expiresAt(CouponPolicy.defaultExpiry(now))
                .used(false)
                .build();

        couponRepository.save(coupon);
    }

    public void useCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다."));
        if (coupon.isUsed()) {
            throw new RuntimeException("이미 사용된 쿠폰입니다.");
        }
        if (coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("쿠폰 사용기한이 만료되었습니다.");
        }
        coupon.markUsed();
    }

    // 쿠폰 코드 중복 검사
    private String uniqueCode() {
        String code;
        do {
            code = CouponPolicy.generateCode();
        } while (couponRepository.findByCode(code).isPresent());
        return code;
    }
}