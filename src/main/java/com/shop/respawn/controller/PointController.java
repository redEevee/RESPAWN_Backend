package com.shop.respawn.controller;

import com.shop.respawn.dto.PointHistoryDto;
import com.shop.respawn.dto.PointLedgerDto;
import com.shop.respawn.service.LedgerPointService;
import com.shop.respawn.service.PointQueryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final LedgerPointService ledgerPointService;
    private final PointQueryService pointQueryService;

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

    // 적립 내역: /api/points/saves?page=0&size=20&sort=occurredAt,desc
    @GetMapping("/saves")
    public Page<PointLedgerDto> getSaves(HttpSession session,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(defaultValue = "occurredAt,desc") String sort) {
        Long buyerId = (Long) session.getAttribute("userId");
        Sort sortObj = toSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return pointQueryService.getSaves(buyerId, pageable);
    }

    // 사용 내역: /api/points/uses?page=0&size=20&sort=occurredAt,desc
    @GetMapping("/uses")
    public Page<PointLedgerDto> getUses(HttpSession session,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(defaultValue = "occurredAt,desc") String sort) {
        Long buyerId = (Long) session.getAttribute("userId");
        Sort sortObj = toSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return pointQueryService.getUses(buyerId, pageable);
    }

    // 통합 내역: /api/points/history?page=0&size=20&sort=occurredAt,desc
    @GetMapping("/history")
    public Page<PointHistoryDto> getAll(HttpSession session,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(defaultValue = "occurredAt,desc") String sort) {
        Long buyerId = (Long) session.getAttribute("userId");
        Sort sortObj = toSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return pointQueryService.getAll(buyerId, pageable);
    }

    private Sort toSort(String sortParam) {
        // "occurredAt,desc" 또는 "amount,asc" 형태를 1개 받아 처리
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Order.desc("occurredAt")).and(Sort.by(Sort.Order.desc("id")));
        }
        String[] parts = sortParam.split(",");
        String prop = parts[0];
        String dir = (parts.length > 1) ? parts[1] : "desc";
        Sort.Order order = "asc".equalsIgnoreCase(dir) ? Sort.Order.asc(prop) : Sort.Order.desc(prop);
        // 기본 tie-break
        return Sort.by(order).and(Sort.by(Sort.Order.desc("id")));
    }
}