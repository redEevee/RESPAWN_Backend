package com.shop.respawn.repository;

import com.shop.respawn.domain.PointLedger;
import com.shop.respawn.domain.PointTransactionType;
import org.springframework.data.jpa.repository.*;

import java.util.List;

public interface PointLedgerRepository extends JpaRepository<PointLedger, Long>, PointLedgerRepositoryCustom {

    List<PointLedger> findByBuyer_IdAndTypeOrderByOccurredAtDesc(Long buyerId, PointTransactionType type);
}
