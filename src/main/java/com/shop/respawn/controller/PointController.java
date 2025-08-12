package com.shop.respawn.controller;

import com.shop.respawn.service.PointService;
import com.shop.respawn.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
