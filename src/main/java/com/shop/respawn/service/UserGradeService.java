package com.shop.respawn.service;

import com.shop.respawn.util.GradePolicy;
import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.MembershipTier;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.PaymentRepository;
import com.shop.respawn.util.MonthlyPeriodUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class UserGradeService {

    private final BuyerRepository buyerRepository;
    private final PaymentRepository paymentRepository;
    private final CouponService couponService;
    private final GradePolicy gradePolicy;

    // 저번달 기준으로 재계산
    public void recalcBuyerTier(Long buyerId) {
        LocalDateTime[] range = MonthlyPeriodUtil.previousMonthRange();
        LocalDateTime start = range[0];
        LocalDateTime end   = range[1];

        Long monthlyAmount = paymentRepository.sumMonthlyAmountByBuyer(buyerId, start, end);
        MembershipTier newTier = gradePolicy.resolveTier(monthlyAmount);
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("구매자 없음: " + buyerId));
        MembershipTier oldTier = buyer.getMembershipTier();
        if (oldTier != newTier) {
            buyer.updateMembershipTier(newTier);
            // 등급 변경 시 쿠폰 발급
            couponService.issueTierCoupon(buyerId, newTier);
        }
        // 변경감지로 flush
    }

    // 결제 성공 시에도 '저번달' 누적 기준으로 등급 산정
    public void onPaymentSuccess(Long buyerId) {
        recalcBuyerTier(buyerId);
    }
}
