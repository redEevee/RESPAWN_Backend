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
import java.util.List;

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

    /**
     * 포인트 사용 메서드 (만료일이 가까운 순서로 사용)
     */
    public void usePoints(Long buyerId, Long useAmount) {
        if (useAmount <= 0) {
            throw new RuntimeException("사용할 포인트는 0보다 커야 합니다.");
        }

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("구매자를 찾을 수 없습니다."));

        // 사용 가능한 포인트 조회 (만료일 순서대로)
        List<Point> usablePoints = pointRepository.findUsablePointsByBuyerId(buyerId);

        long availablePoints = usablePoints.stream()
                .mapToLong(Point::getAmount)
                .sum();

        if (availablePoints < useAmount) {
            throw new RuntimeException("사용 가능한 포인트가 부족합니다. (사용가능: " + availablePoints + ", 요청: " + useAmount + ")");
        }

        long remainingUseAmount = useAmount;

        // 만료일이 가까운 포인트부터 사용 (FIFO)
        for (Point point : usablePoints) {
            if (remainingUseAmount <= 0) {
                break;
            }

            if (point.getAmount() <= remainingUseAmount) {
                // 포인트 전액 사용
                remainingUseAmount -= point.getAmount();
                point.use();
            } else {
                // 포인트 일부 사용 - 기존 포인트는 사용처리하고 잔액으로 새 포인트 생성
                long usedAmount = remainingUseAmount;
                long remainingAmount = point.getAmount() - usedAmount;

                // 기존 포인트 사용 처리
                point.use();

                // 잔액으로 새 포인트 생성
                Point remainingPoint = Point.CreatePoint(
                        point.getPointName(),
                        remainingAmount,
                        point.getPointAwardedDate(),
                        point.getPointExpiryDate(),
                        buyer
                );

                pointRepository.save(remainingPoint);
                remainingUseAmount = 0;
            }
        }

        // 포인트 사용 기록 저장 (음수로 저장)
        Point usagePoint = Point.CreatePoint(
                "포인트 사용",
                -useAmount,
                LocalDateTime.now(),
                LocalDateTime.now().plusYears(10), // 사용 기록은 만료되지 않도록
                buyer
        );
        usagePoint.use(); // 사용 기록은 즉시 사용 상태로 설정

        pointRepository.save(usagePoint);
    }

    /**
     * 포인트 사용 가능 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean canUsePoints(Long buyerId, Long useAmount) {
        long availablePoints = getActiveTotalPoints(buyerId);
        return availablePoints >= useAmount;
    }

    /**
     * 포인트 사용 내역 조회
     */
    @Transactional(readOnly = true)
    public List<Point> getUsedPoints(Long buyerId) {
        return pointRepository.findUsedPointsByBuyerId(buyerId);
    }

    /**
     * 사용 가능한 포인트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Point> getUsablePoints(Long buyerId) {
        return pointRepository.findUsablePointsByBuyerId(buyerId);
    }
}
