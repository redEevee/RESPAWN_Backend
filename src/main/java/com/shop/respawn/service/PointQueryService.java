package com.shop.respawn.service;

import com.shop.respawn.domain.PointLedger;
import com.shop.respawn.domain.PointTransactionType;
import com.shop.respawn.dto.point.ExpiringPointItemDto;
import com.shop.respawn.dto.point.ExpiringPointTotalDto;
import com.shop.respawn.dto.point.PointHistoryDto;
import com.shop.respawn.dto.point.PointLedgerDto;
import com.shop.respawn.repository.PointLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointQueryService {

    private final PointLedgerRepository pointLedgerRepository;

    // 적립(+, SAVE와 CANCEL_USE) 목록
    public Page<PointLedgerDto> getSaves(Long buyerId, Pageable pageable) {
        EnumSet<PointTransactionType> types = EnumSet.of(
                PointTransactionType.SAVE,
                PointTransactionType.CANCEL_USE
        );
        Page<?> page = pointLedgerRepository.findByBuyerAndTypes(buyerId, types, pageable);
        return page.map(p -> PointLedgerDto.from((com.shop.respawn.domain.PointLedger) p));
    }

    // 사용(-, USE/EXPIRE/CANCEL_SAVE) 목록
    public Page<PointLedgerDto> getUses(Long buyerId, Pageable pageable) {
        EnumSet<PointTransactionType> types = EnumSet.of(
                PointTransactionType.USE,
                PointTransactionType.EXPIRE,
                PointTransactionType.CANCEL_SAVE
        );
        Page<?> page = pointLedgerRepository.findByBuyerAndTypes(buyerId, types, pageable);
        return page.map(p -> PointLedgerDto.from((com.shop.respawn.domain.PointLedger) p));
    }

    // 통합(모든 타입) 목록
    public Page<PointHistoryDto> getAll(Long buyerId, Pageable pageable) {
        Page<?> page = pointLedgerRepository.findAllByBuyer(buyerId, pageable);
        return page.map(p -> PointHistoryDto.of(PointLedgerDto.from((com.shop.respawn.domain.PointLedger) p)));
    }

    // 합계 전용
    public ExpiringPointTotalDto getThisMonthExpiringTotal(Long buyerId) {
        LocalDateTime[] range = thisMonthRange();
        List<PointLedger> candidates = pointLedgerRepository
                .findMonthlyExpireCandidates(buyerId, range[0], range[1]);

        long total = 0L;
        for (PointLedger save : candidates) {
            long consumed = pointLedgerRepository.sumConsumedAmountOfSave(save);
            long remaining = Math.max(0L, save.getAmount() - consumed);
            total += remaining;
        }
        return new ExpiringPointTotalDto(total);
    }

    // 목록 전용
    public List<ExpiringPointItemDto> getThisMonthExpiringList(Long buyerId) {
        LocalDateTime[] range = thisMonthRange();
        List<PointLedger> candidates = pointLedgerRepository
                .findMonthlyExpireCandidates(buyerId, range[0], range[1]);

        return candidates.stream()
                .map(save -> {
                    long consumed = pointLedgerRepository.sumConsumedAmountOfSave(save);
                    long remaining = Math.max(0L, save.getAmount() - consumed);
                    return ExpiringPointItemDto.builder()
                            .ledgerId(save.getId())
                            .remainingAmount(remaining)
                            .expiryAt(save.getExpiryAt())
                            .refOrderId(save.getRefOrderId())
                            .reason(save.getReason())
                            .build();
                })
                .toList();
    }

    private static LocalDateTime[] thisMonthRange() {
        LocalDate today = LocalDate.now();
        LocalDate first = today.withDayOfMonth(1);
        LocalDate last = today.withDayOfMonth(today.lengthOfMonth());
        return new LocalDateTime[] { first.atStartOfDay(), last.atTime(LocalTime.MAX) };
    }
}
