package com.shop.respawn.controller;

import com.shop.respawn.domain.Point;
import com.shop.respawn.dto.PointDto;
import com.shop.respawn.service.PointService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    /**
     * 모든 포인트 조회 메서드
     */
    @GetMapping("/total")
    public long getMyTotalPoints(HttpSession session) {
        Long buyerId = (Long) session.getAttribute("userId");
        return pointService.getTotalPoints(buyerId);
    }

    /**
     * 사용 가능한 포인트 조회 메서드
     */
    @GetMapping("/total/active")
    public long getMyActiveTotalPoints(HttpSession session) {
        Long buyerId = (Long) session.getAttribute("userId");
        return pointService.getActiveTotalPoints(buyerId);
    }

    /**
     * 포인트 사용 메서드
     */
    @PostMapping("/use")
    public ResponseEntity<Map<String, Object>> usePoints(
            @RequestBody Map<String, Long> request,
            HttpSession session) {
        try {
            Long buyerId = (Long) session.getAttribute("userId");
            Long useAmount = request.get("amount");

            if (useAmount == null || useAmount <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "사용할 포인트는 0보다 커야 합니다."));
            }

            pointService.usePoints(buyerId, useAmount);

            long remainingPoints = pointService.getActiveTotalPoints(buyerId);

            return ResponseEntity.ok(Map.of(
                    "message", "포인트가 성공적으로 사용되었습니다.",
                    "usedAmount", useAmount,
                    "remainingPoints", remainingPoints
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 포인트 사용 가능 여부 확인
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkPointsUsage(
            @RequestBody Map<String, Long> request,
            HttpSession session) {
        try {
            Long buyerId = (Long) session.getAttribute("userId");
            Long useAmount = request.get("amount");

            if (useAmount == null || useAmount <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "확인할 포인트는 0보다 커야 합니다."));
            }

            boolean canUse = pointService.canUsePoints(buyerId, useAmount);
            long availablePoints = pointService.getActiveTotalPoints(buyerId);

            return ResponseEntity.ok(Map.of(
                    "canUse", canUse,
                    "availablePoints", availablePoints,
                    "requestedAmount", useAmount
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 포인트 사용 내역 조회
     */
    @GetMapping("/history/used")
    public ResponseEntity<List<PointDto>> getUsedPointsHistory(HttpSession session) {
        try {
            Long buyerId = (Long) session.getAttribute("userId");
            List<Point> usedPoints = pointService.getUsedPoints(buyerId);

            // Point 엔티티를 DTO로 변환
            List<PointDto> usedPointDtos = usedPoints.stream()
                    .map(PointDto::from)
                    .collect(toList());

            return ResponseEntity.ok(usedPointDtos);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 사용 가능한 포인트 목록 조회
     */
    @GetMapping("/usable")
    public ResponseEntity<List<PointDto>> getUsablePoints(HttpSession session) {
        try {
            Long buyerId = (Long) session.getAttribute("userId");
            List<Point> usablePoints = pointService.getUsablePoints(buyerId);

            // Point 엔티티를 DTO로 변환
            List<PointDto> usablePointDtos = usablePoints.stream()
                    .map(PointDto::from)
                    .collect(toList());

            return ResponseEntity.ok(usablePointDtos);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
