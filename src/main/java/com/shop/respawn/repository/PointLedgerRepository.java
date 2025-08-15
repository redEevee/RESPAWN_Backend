package com.shop.respawn.repository;

import com.shop.respawn.domain.PointLedger;
import com.shop.respawn.domain.PointTransactionType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PointLedgerRepository extends JpaRepository<PointLedger, Long> {

    // 미사용 적립분(SAVE) 후보 조회: 만료일 오름차순, 발생일 오름차순
    @Query("""
        select l from PointLedger l
        where l.buyer.id = :buyerId
          and l.type = com.shop.respawn.domain.PointTransactionType.SAVE
          and (coalesce((
              select sum(cl.consumedAmount)
              from PointConsumeLink cl
              where cl.saveLedger = l
          ), 0) < l.amount)
          and (l.expiryAt is null or l.expiryAt > :now)
        order by l.expiryAt nulls last, l.occurredAt asc, l.id asc
    """)
    List<PointLedger> findUsableSaveLedgers(@Param("buyerId") Long buyerId, @Param("now") LocalDateTime now);

    // 만료 대상 SAVE
    @Query("""
        select l from PointLedger l
        where l.buyer.id = :buyerId
          and l.type = com.shop.respawn.domain.PointTransactionType.SAVE
          and l.expiryAt <= :now
          and coalesce((
              select sum(cl.consumedAmount)
              from PointConsumeLink cl
              where cl.saveLedger = l
          ), 0) < l.amount
        order by l.expiryAt asc, l.id asc
    """)
    List<PointLedger> findExpireCandidates(@Param("buyerId") Long buyerId, @Param("now") LocalDateTime now);

    List<PointLedger> findByBuyer_IdAndTypeOrderByOccurredAtDesc(Long buyerId, PointTransactionType type);
}
