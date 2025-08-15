package com.shop.respawn.controller;

import com.shop.respawn.service.LedgerPointService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final LedgerPointService ledgerPointService;

    @GetMapping("/total")
    public long getMyTotalPointsV2(HttpSession session) {
        Long buyerId = (Long) session.getAttribute("userId");
        return ledgerPointService.getTotal(buyerId);
    }

    @GetMapping("/total/active")
    public long getMyActiveTotalPointsV2(HttpSession session) {
        Long buyerId = (Long) session.getAttribute("userId");
        return ledgerPointService.getActive(buyerId);
    }
}