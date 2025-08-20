package com.shop.respawn.controller;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.dto.MembershipTierResponse;
import com.shop.respawn.dto.gradeRecalc.GradeRecalcRequest;
import com.shop.respawn.dto.gradeRecalc.GradeRecalcResponse;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.service.UserGradeQueryService;
import com.shop.respawn.service.UserGradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
@Slf4j
public class MembershipController {

    private final UserGradeQueryService userGradeQueryService;
    private final UserGradeService userGradeService;
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

    // 1) 단일 사용자 강제 갱신
    @PostMapping("/recalc/{buyerId}")
    @Transactional
    public GradeRecalcResponse recalcOne(@PathVariable Long buyerId) {
        userGradeService.recalcBuyerTier(buyerId);
        return new GradeRecalcResponse(1, 1, 0, "buyerId=" + buyerId + " 등급 갱신 완료");
    }

    // 2) 다수/전체 강제 갱신
    @PostMapping("/recalc")
    @Transactional
    public GradeRecalcResponse recalcMany(@RequestBody(required = false) GradeRecalcRequest request) {
        List<Long> targets;
        if (request == null || request.getBuyerIds() == null || request.getBuyerIds().isEmpty()) {
            // 전체 대상
            targets = buyerRepository.findAll()
                    .stream().map(Buyer::getId).toList();
        } else {
            targets = request.getBuyerIds();
        }

        int processed = 0, succeeded = 0, failed = 0;

        for (Long id : targets) {
            processed++;
            try {
                userGradeService.recalcBuyerTier(id);
                succeeded++;
            } catch (Exception e) {
                failed++;
                // 로깅
                log.warn("등급 갱신 실패 buyerId={}", id, e);
            }
        }
        String msg = "processed=" + processed + ", succeeded=" + succeeded + ", failed=" + failed;
        return new GradeRecalcResponse(processed, succeeded, failed, msg);
    }
}