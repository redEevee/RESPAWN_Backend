package com.shop.respawn.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.respawn.domain.PointLedger;
import com.shop.respawn.domain.PointTransactionType;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.shop.respawn.domain.QPointConsumeLink.pointConsumeLink;
import static com.shop.respawn.domain.QPointLedger.pointLedger;

@Repository
@RequiredArgsConstructor
public class PointLedgerRepositoryImpl implements PointLedgerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 미사용 적립분(SAVE) 후보 조회: 만료일 오름차순, 발생일 오름차순
     */
    @Override
    public List<PointLedger> findUsableSaveLedgers(Long buyerId, LocalDateTime now) {
        // SUM 서브쿼리를 BigDecimal 표현식으로 강제 래핑
        NumberExpression<BigDecimal> consumedSumExpr = getBigDecimalNumberExpression();
        //COALESCE(consumedSum, 0)
        NumberExpression<BigDecimal> consumedSumCoalesced = getConsumedSumCoalesced(consumedSumExpr);
        // NULLS LAST 보장: CASE WHEN expiryAt IS NULL THEN 1 ELSE 0 END ASC
        NumberExpression<Integer> expiryNullsLastKey =
                Expressions.numberTemplate(
                        Integer.class,
                        "case when {0} is null then 1 else 0 end",
                        pointLedger.expiryAt
                );
        return queryFactory
                .selectFrom(pointLedger)
                .where(
                        pointLedger.buyer.id.eq(buyerId),
                        pointLedger.type.eq(PointTransactionType.SAVE),
                        consumedSumCoalesced.lt(pointLedger.amount),
                        pointLedger.expiryAt.isNull().or(pointLedger.expiryAt.gt(now))
                )
                .orderBy(
                        new OrderSpecifier<>(Order.ASC, expiryNullsLastKey),
                        pointLedger.expiryAt.asc(),
                        pointLedger.occurredAt.asc(),
                        pointLedger.id.asc()
                )
                .fetch();
    }

    /**
     * 만료 대상 SAVE 조회
     */
    @Override
    public List<PointLedger> findExpireCandidates(Long buyerId, LocalDateTime now) {
        // 1) SUM 서브쿼리를 BigDecimal 표현식으로 래핑
        NumberExpression<BigDecimal> consumedSumExpr = getBigDecimalNumberExpression();
        // 2) COALESCE(consumedSum, 0)
        NumberExpression<BigDecimal> consumedSumCoalesced = getConsumedSumCoalesced(consumedSumExpr);
        return queryFactory
                .selectFrom(pointLedger)
                .where(
                        pointLedger.buyer.id.eq(buyerId),
                        pointLedger.type.eq(PointTransactionType.SAVE),
                        pointLedger.expiryAt.loe(now),              // expiryAt <= :now
                        consumedSumCoalesced.lt(pointLedger.amount) // 잔여 > 0
                )
                .orderBy(
                        pointLedger.expiryAt.asc(),
                        pointLedger.id.asc()
                )
                .fetch();
    }

    @NotNull
    private static NumberExpression<BigDecimal> getBigDecimalNumberExpression() {
        // SUM 서브쿼리를 BigDecimal 표현식으로 래핑
        return Expressions.numberTemplate(
                BigDecimal.class,
                "{0}",
                JPAExpressions
                        .select(pointConsumeLink.consumedAmount.sum())
                        .from(pointConsumeLink)
                        .where(pointConsumeLink.saveLedger.eq(pointLedger))
        );
    }

    @NotNull
    private static NumberExpression<BigDecimal> getConsumedSumCoalesced(NumberExpression<BigDecimal> consumedSumExpr) {
        // COALESCE(consumedSum, 0)
        return Expressions.numberTemplate(
                BigDecimal.class,
                "coalesce({0}, {1})",
                consumedSumExpr,
                BigDecimal.ZERO
        );
    }
}
