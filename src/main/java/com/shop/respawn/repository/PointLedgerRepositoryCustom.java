package com.shop.respawn.repository;

import com.shop.respawn.domain.PointLedger;

import java.time.LocalDateTime;
import java.util.List;

public interface PointLedgerRepositoryCustom {
    List<PointLedger> findUsableSaveLedgers(Long buyerId, LocalDateTime now);

    List<PointLedger> findExpireCandidates(Long buyerId, LocalDateTime now);
}
