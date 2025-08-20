package com.shop.respawn.controller;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.dto.MembershipTierResponse;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.service.UserGradeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final UserGradeQueryService userGradeQueryService;
    private final BuyerRepository buyerRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 1) 내 멤버십 등급 조회 (로그인 필요)
    @GetMapping("/me")
    public MembershipTierResponse myTier(Authentication authentication) {
        String username = authentication.getName();
        Buyer buyer = buyerRepository.findByUsername(username);
        if (buyer == null) throw new RuntimeException("구매자 없음: " + username);

        var result = userGradeQueryService.getPrevMonthTier(buyer.getId());
        String period = result.start().format(FMT) + " ~ " + result.end().format(FMT);
        return new MembershipTierResponse(
                result.buyer().getId(),
                result.buyer().getUsername(),
                result.tier(),
                result.amount(),
                period
        );
    }

    // 2) 특정 구매자 등급 조회 (관리자/운영자용)
    @GetMapping("/{buyerId}")
    public MembershipTierResponse tierByBuyerId(@PathVariable Long buyerId) {
        var result = userGradeQueryService.getPrevMonthTier(buyerId);
        String period = result.start().format(FMT) + " ~ " + result.end().format(FMT);
        return new MembershipTierResponse(
                result.buyer().getId(),
                result.buyer().getUsername(),
                result.tier(),
                result.amount(),
                period
        );
    }
}