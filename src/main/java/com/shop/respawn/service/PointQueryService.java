package com.shop.respawn.service;

import com.shop.respawn.domain.PointTransactionType;
import com.shop.respawn.dto.PointHistoryDto;
import com.shop.respawn.dto.PointLedgerDto;
import com.shop.respawn.repository.PointLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

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
}
