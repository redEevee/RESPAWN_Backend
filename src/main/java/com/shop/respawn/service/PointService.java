package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Order;
import com.shop.respawn.domain.Point;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final PointRepository pointRepository;
    private final BuyerRepository buyerRepository;

    /**
     * 결제 금액의 2% 포인트 적립
     */
    public void awardPoints(Long buyerId, Order order) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("구매자를 찾을 수 없습니다."));

        long awardAmount = Math.max(1L, Math.round(order.getTotalAmount() * 0.02)); // 최소 1포인트 보장
        LocalDateTime awardedAt = LocalDateTime.now();
        LocalDateTime expiryDate = awardedAt.plusYears(1); // 유효기간 1년

        Point point = Point.CreatePoint(
                "결제 포인트 적립",
                awardAmount,
                awardedAt,
                expiryDate,
                buyer
        );

        pointRepository.save(point);
    }

    /**
     * 현재 로그인한 사용자의 전체 포인트 총합 조회
     */
    @Transactional(readOnly = true)
    public long getTotalPoints(Long buyerId) {
        return pointRepository.getTotalPointsByBuyerId(buyerId);
    }

    /**
     * 현재 로그인한 사용자의 사용 가능한 전체 포인트 총합 조회
     */
    @Transactional(readOnly = true)
    public long getActiveTotalPoints(Long buyerId) {
        return pointRepository.getActiveTotalPointsByBuyerId(buyerId);
    }
}
